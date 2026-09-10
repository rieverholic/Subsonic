package dev.riever.subsonic.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
public record SubsonicConfig(
        @Setting
        List<Server> servers
) {
    @ConfigSerializable
    public record Server(
            @Comment("Server name, as in Velocity config.")
            @Setting(value = "name")
            String name,

            @Comment("Fix dimension ID to prevent dimension mismatch.")
            @Setting(value = "fix-dim-overflow")
            boolean fixDimensionId
    ) {
        public Server {
            fixDimensionId = true;
        }
    }

    public SubsonicConfig() {
        this(List.of(new Server("", true)));
    }
}
