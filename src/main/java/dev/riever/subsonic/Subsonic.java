package dev.riever.subsonic;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.riever.subsonic.config.SubsonicConfig;
import dev.riever.subsonic.config.SubsonicConfigManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import dev.riever.subsonic.inject.VelocityInjector;

@Plugin(
        id = "subsonic",
        name = "Subsonic",
        version = "0.1.0-SNAPSHOT",
        url = "https://example.org",
        description = "Some compatibility layers for Velocity and modpacks.",
        authors = {"Riever"}
)
public class Subsonic {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private SubsonicConfigManager configManager;

    @Inject
    public Subsonic(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Hello from Subsonic!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Path configFile = this.dataDirectory.resolve("config.yml");
        this.configManager = new SubsonicConfigManager(configFile, logger);
        SubsonicConfig config = this.configManager.initialize();
        Set<String> serverNames = new HashSet<>();
        for (SubsonicConfig.Server server : config.servers()) {
            if (server.fixDimensionId()) {
                serverNames.add(server.name());
            }
        }
        VelocityInjector injector = new VelocityInjector(this.server, this.logger, serverNames);
        injector.inject();
    }
}
