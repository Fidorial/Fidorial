package fr.euphyllia.fidorial.server.configuration;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.fidorial.entity.GameMode;
import fr.fidorial.world.Location;

import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.commented;
import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.configRecord;

public record WorldConfiguration(Gameplay gameplay) {

    public static final WorldConfiguration DEFAULTS = new WorldConfiguration(Gameplay.DEFAULTS);

    public static final Codec<WorldConfiguration> CODEC = codec(DEFAULTS);

    /**
     * The codec for a file whose missing settings fall back to {@code defaults}.
     */
    static Codec<WorldConfiguration> codec(final WorldConfiguration defaults) {
        return configRecord(WorldConfiguration.class, defaults)
                .field("gameplay", WorldConfiguration::gameplay, commented(Gameplay.codec(defaults.gameplay()),
                        "Gameplay settings"))
                .build();
    }

    public record Gameplay(
            boolean pvp,
            GameMode defaultGameMode,
            boolean generateStructures,
            Location spawn
    ) {

        public static final Gameplay DEFAULTS = new Gameplay(
                true, GameMode.SURVIVAL, true, new Location(8.5, -48.0, 8.5, 0f, 0f));

        static Codec<Gameplay> codec(final Gameplay defaults) {
            return configRecord(Gameplay.class, defaults)
                    .field("pvp", Gameplay::pvp, commented(Codec.BOOL,
                            "Whether PVP is enabled"))
                    .field("default-game-mode", Gameplay::defaultGameMode, commented(ConfigurationCodecs.GAME_MODE,
                            "The gamemode given to players joining for the first time. Available options are: survival, creative, adventure, spectator"))
                    .field("generate-structures", Gameplay::generateStructures, commented(Codec.BOOL,
                            "Whether to generate structures. Effective only when a datapack containing them is loaded"))
                    .field("spawn", Gameplay::spawn, commented(spawnCodec(defaults.spawn()),
                            "The spawn position and rotation for joining players. Defaults to Vanilla's superflat default spawn position"))
                    .build();
        }

        private static Codec<Location> spawnCodec(final Location defaults) {
            return configRecord(Location.class, defaults)
                    .field("x", Location::x, Codec.DOUBLE)
                    .field("y", Location::y, Codec.DOUBLE)
                    .field("z", Location::z, Codec.DOUBLE)
                    .field("yaw", Location::yaw, Codec.FLOAT)
                    .field("pitch", Location::pitch, Codec.FLOAT)
                    .build();
        }
    }
}
