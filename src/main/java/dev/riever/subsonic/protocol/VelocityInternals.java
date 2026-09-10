package dev.riever.subsonic.protocol;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.riever.subsonic.util.ReflectionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public final class VelocityInternals {
    public static class Connections {
        public static final String MINECRAFT_DECODER = "minecraft-decoder";
        public static final String MINECRAFT_ENCODER = "minecraft-encoder";
        public static final String HANDLER = "handler";
    }

    public static Channel getServerChannel(Channel backendChannel) {
        try {
            Object mc = backendChannel.pipeline().get(Connections.HANDLER);
            if (mc == null) {
                return null;
            }
            Object vsc = ReflectionUtils.invoke(mc, "getAssociation");
            if (vsc == null) {
                return null;
            }
            Object player = ReflectionUtils.invoke(vsc, "getPlayer");
            if (player == null) {
                return null;
            }
            Object serverConn = ReflectionUtils.invoke(player, "getConnection");
            if (serverConn == null) {
                return null;
            }
            return (Channel) ReflectionUtils.invoke(serverConn, "getChannel");
        } catch (Exception e) {
            return null;
        }
    }

    public static MinecraftPackets.Protocol getProtocol(Channel ch, boolean clientBound) {
        Object mc = ch.pipeline().get(Connections.HANDLER);
        if (mc == null) {
            return null;
        }
        try {
            Enum<?> state = (Enum<?>) ReflectionUtils.invoke(mc, "getState");
            Enum<?> version = (Enum<?>) ReflectionUtils.invoke(mc, "getProtocolVersion");
            if (state == null || version == null) {
                return null;
            }
            return new MinecraftPackets.Protocol(version.name(), state.name(), clientBound);
        } catch (ReflectiveOperationException e) {
            return null;
        }
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

    public static String getServerName(Channel ch) {
        Object mc = ch.pipeline().get(Connections.HANDLER);
        if (mc == null) {
            return null;
        }
        try {
            Object assoc = ReflectionUtils.invoke(mc, "getAssociation");
            if (assoc instanceof ServerConnection sc) {
                return sc.getServerInfo().getName();
            } else if (assoc instanceof Player player) {
                return player.getCurrentServer()
                        .map(sc -> sc.getServerInfo().getName())
                        .orElse(null);
            }
            return null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
