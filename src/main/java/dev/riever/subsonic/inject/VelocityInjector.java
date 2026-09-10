package dev.riever.subsonic.inject;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.riever.subsonic.handler.JoinGameAppendHandler;
import dev.riever.subsonic.handler.JoinGameStripHandler;
import dev.riever.subsonic.protocol.VelocityInternals;
import dev.riever.subsonic.util.ReflectionUtils;
import org.slf4j.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.util.Set;

public class VelocityInjector {
    private final ProxyServer server;
    private final Logger logger;
    private final Set<String> serverNames;

    public VelocityInjector(ProxyServer server, Logger logger, Set<String> serverNames) {
        this.server = server;
        this.logger = logger;
        this.serverNames = serverNames;
    }

    public void inject() {
        try {
            Object cm = ReflectionUtils.get(this.server, "cm");
            Object proxyHolder = ReflectionUtils.invoke(cm, "getServerChannelInitializer");
            ChannelInitializer<?> original = (ChannelInitializer<?>) ReflectionUtils.invoke(proxyHolder, "get");
            ChannelInitializer<Channel> wrapped = new WrappedChannelInitializer(original, this::dimensionIdAppender);
            ReflectionUtils.invoke(proxyHolder, "set", ChannelInitializer.class, wrapped);

            Object backendHolder = ReflectionUtils.invoke(cm, "getBackendChannelInitializer");
            ChannelInitializer<?> backendOriginal = (ChannelInitializer<?>) ReflectionUtils.invoke(backendHolder, "get");
            ChannelInitializer<Channel> backendWrapped = new WrappedChannelInitializer(backendOriginal, this::dimensionIdStripper);
            ReflectionUtils.invoke(backendHolder, "set", ChannelInitializer.class, backendWrapped);
        } catch (Throwable t) {
            logger.error("Reflection error: ", t);
        }
    }

    private void dimensionIdStripper(Channel ch) {
        ch.pipeline().addBefore(
                VelocityInternals.Connections.MINECRAFT_DECODER,
                "dimension-id-stripper",
                new JoinGameStripHandler(this.serverNames)
        );
    }

    private void dimensionIdAppender(Channel ch) {
        ch.pipeline().addBefore(
                VelocityInternals.Connections.MINECRAFT_ENCODER,
                "dimension-id-appender",
                new JoinGameAppendHandler(this.serverNames)
        );
    }
}
