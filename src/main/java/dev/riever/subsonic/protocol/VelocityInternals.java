package dev.riever.subsonic.protocol;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.subsonic.util.ReflectionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import static dev.riever.subsonic.util.ReflectionUtils.invoke;

public final class VelocityInternals {
    public static class Connections {
        public static final String MINECRAFT_DECODER = "minecraft-decoder";
        public static final String MINECRAFT_ENCODER = "minecraft-encoder";
        public static final String HANDLER = "handler";
    }

    public static void verify() {
        try {
            Handles.init();
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Unsupported Velocity version: " + cause.getMessage() + "." +
                    " If you think this is a bug, feel free to report to the issue tracker.", cause);
        }
    }

    public static Channel getServerChannel(Channel backendChannel) {
        Object mc = getConnection(backendChannel);
        if (mc == null) {
            return null;
        }
        Object vsc = invoke(Handles.MC_GET_ASSOCIATION, mc);
        if (!(vsc instanceof ServerConnection sc)) {
            return null;
        }
        Player player = sc.getPlayer();
        if (player == null) {
            return null;
        }
        Object serverConn = invoke(Handles.PLAYER_GET_CONNECTION, player);
        if (serverConn == null) {
            return null;
        }
        return (Channel) invoke(Handles.MC_GET_CHANNEL, serverConn);
    }

    public static MinecraftPackets.Protocol getProtocol(Channel ch, boolean clientBound) {
        Object mc = getConnection(ch);
        if (mc == null) {
            return null;
        }
        Enum<?> state = (Enum<?>) invoke(Handles.MC_GET_STATE, mc);
        Enum<?> version = (Enum<?>) invoke(Handles.MC_GET_PROTOCOL_VERSION, mc);
        if (state == null || version == null) {
            return null;
        }
        return new MinecraftPackets.Protocol(version.name(), state.name(), clientBound);
    }

    public static boolean isJoinGamePacket(Channel ch, ByteBuf buf) {
        MinecraftPackets.VarInt varInt = MinecraftPackets.readVarInt(buf);
        if (varInt == null) {
            return false;
        }
        int packetId = varInt.value();
        MinecraftPackets.Protocol proto = VelocityInternals.getProtocol(ch, true);
        return MinecraftPackets.isLegacyJoinGamePacket(proto, packetId);
    }

    public static boolean isRespawnPacket(Channel ch, ByteBuf buf) {
        MinecraftPackets.VarInt varInt = MinecraftPackets.readVarInt(buf);
        if (varInt == null) {
            return false;
        }
        int packetId = varInt.value();
        MinecraftPackets.Protocol proto = VelocityInternals.getProtocol(ch, true);
        return MinecraftPackets.isLegacyRespawnPacket(proto, packetId);
    }

    public static String getServerName(Channel ch) {
        Object mc = getConnection(ch);
        if (mc == null) {
            return null;
        }
        Object assoc = invoke(Handles.MC_GET_ASSOCIATION, mc);
        if (assoc instanceof ServerConnection sc) {
            return sc.getServerInfo().getName();
        } else if (assoc instanceof Player player) {
            return player.getCurrentServer()
                    .map(sc -> sc.getServerInfo().getName())
                    .orElse(null);
        }
        return null;
    }

    public static ChannelInitializer<Channel> getServerInitializer(ProxyServer server) {
        return getInitializerHolder(Handles.CM_GET_SERVER_HOLDER, server).get();
    }

    public static void setServerInitializer(ProxyServer server, ChannelInitializer<Channel> ci) {
        replaceInitializer(Handles.SERVER_CI_HOLDER_SET, getInitializerHolder(Handles.CM_GET_SERVER_HOLDER, server), ci);
    }

    public static ChannelInitializer<Channel> getBackendInitializer(ProxyServer server) {
        return getInitializerHolder(Handles.CM_GET_BACKEND_HOLDER, server).get();
    }

    public static void setBackendInitializer(ProxyServer server, ChannelInitializer<Channel> ci) {
        replaceInitializer(Handles.BACKEND_CI_HOLDER_SET, getInitializerHolder(Handles.CM_GET_BACKEND_HOLDER, server), ci);
    }

    private static Object getConnection(Channel ch) {
        Object handler = ch.pipeline().get(Connections.HANDLER);
        return Handles.MINECRAFT_CONNECTION.isInstance(handler) ? handler : null;
    }

    @SuppressWarnings("unchecked")
    private static Supplier<ChannelInitializer<Channel>> getInitializerHolder(MethodHandle getHolderMethod, ProxyServer server) {
        return (Supplier<ChannelInitializer<Channel>>) invoke(getHolderMethod, invoke(Handles.SERVER_GET_CM, server));
    }

    private static void replaceInitializer(MethodHandle setMethod, Object holder, ChannelInitializer<Channel> ci) {
        try {
            setMethod.invokeExact(holder, (ChannelInitializer<?>) ci);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    private static final class Handles {
        private static final String PKG = "com.velocitypowered.proxy.";
        private static final MethodType GETTER = MethodType.methodType(Object.class, Object.class);

        static final Class<?> MINECRAFT_CONNECTION = load("connection.MinecraftConnection");
        static final MethodHandle MC_GET_STATE =
                getter(MINECRAFT_CONNECTION, "getState");
        static final MethodHandle MC_GET_PROTOCOL_VERSION =
                getter(MINECRAFT_CONNECTION, "getProtocolVersion");
        static final MethodHandle MC_GET_ASSOCIATION =
                getter(MINECRAFT_CONNECTION, "getAssociation");
        static final MethodHandle MC_GET_CHANNEL =
                getter(MINECRAFT_CONNECTION, "getChannel");

        static final MethodHandle PLAYER_GET_CONNECTION =
                getter(load("connection.client.ConnectedPlayer"), "getConnection");

        static final MethodHandle SERVER_GET_CM = ReflectionUtils.fieldGetter(load("VelocityServer"), "cm", GETTER);

        private static final Class<?> CONNECTION_MANAGER = load("network.ConnectionManager");
        static final MethodHandle CM_GET_SERVER_HOLDER =
                getter(CONNECTION_MANAGER, "getServerChannelInitializer");
        static final MethodHandle CM_GET_BACKEND_HOLDER =
                getter(CONNECTION_MANAGER, "getBackendChannelInitializer");

        private static final MethodType CI_SETTER = MethodType.methodType(void.class, Object.class, ChannelInitializer.class);
        static final MethodHandle SERVER_CI_HOLDER_SET =
                channelSetter(load("network.ServerChannelInitializerHolder"));
        static final MethodHandle BACKEND_CI_HOLDER_SET =
                channelSetter(load("network.BackendChannelInitializerHolder"));

        private static Class<?> load(String name) {
            return ReflectionUtils.load(PKG + name, VelocityInternals.class.getClassLoader());
        }

        private static MethodHandle getter(Class<?> clazz, String name) {
            return ReflectionUtils.method(clazz, name, GETTER);
        }

        private static MethodHandle channelSetter(Class<?> holder) {
            return ReflectionUtils.method(holder, "set", CI_SETTER, ChannelInitializer.class);
        }

        static void init() {}
    }
}
