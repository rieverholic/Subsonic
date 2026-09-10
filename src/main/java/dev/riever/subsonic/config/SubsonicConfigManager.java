package dev.riever.subsonic.config;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class SubsonicConfigManager {
    private final YamlConfigurationLoader loader;
    private final Path configFile;
    private final Logger logger;
    private CommentedConfigurationNode root;
    private SubsonicConfig config;

    public SubsonicConfigManager(Path configFile, Logger logger) {
        this.configFile = configFile;
        this.loader = YamlConfigurationLoader.builder()
                .path(configFile)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        this.logger = logger;
    }

    public SubsonicConfig initialize() {
        try {
            if (!Files.exists(this.configFile)) {
                this.root = this.loader.createNode();
                this.root.set(SubsonicConfig.class, new SubsonicConfig());
                this.loader.save(this.root);
                this.logger.warn("Open the config file and fill in the relevant fields. (See the config.yml file for more information.)");
            }
            return this.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SubsonicConfig load() {
        try {
            this.root = this.loader.load();
            this.config = this.root.get(SubsonicConfig.class);
            return this.config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SubsonicConfig getConfig() {
        return this.config;
    }
}