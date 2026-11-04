package dev.riever.subsonic.inject;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.riever.subsonic.handler.DimensionPatchHandler;
import dev.riever.subsonic.handler.JoinGameStripHandler;
import dev.riever.subsonic.protocol.VelocityInternals;
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
            VelocityInternals.verify();
        } catch (IllegalStateException e) {
            logger.error("Failed to inject.", e);
            return;
        }

        ChannelInitializer<Channel> original = VelocityInternals.getServerInitializer(server);
        ChannelInitializer<Channel> wrapped = new WrappedChannelInitializer(original, this::dimensionIdPatcher);
        VelocityInternals.setServerInitializer(server, wrapped);

        ChannelInitializer<Channel> backendOriginal = VelocityInternals.getBackendInitializer(server);
        ChannelInitializer<Channel> backendWrapped = new WrappedChannelInitializer(backendOriginal, this::dimensionIdStripper);
        VelocityInternals.setBackendInitializer(server, backendWrapped);
    }

    private void dimensionIdStripper(Channel ch) {
        ch.pipeline().addBefore(
                VelocityInternals.Connections.MINECRAFT_DECODER,
                "gtnh-dim-id-stripper",
                new JoinGameStripHandler(this.serverNames)
        );
    }

    private void dimensionIdPatcher(Channel ch) {
        ch.pipeline().addBefore(
                VelocityInternals.Connections.MINECRAFT_ENCODER,
                "gtnh-dim-id-patcher",
                new DimensionPatchHandler()
        );
    }
}
