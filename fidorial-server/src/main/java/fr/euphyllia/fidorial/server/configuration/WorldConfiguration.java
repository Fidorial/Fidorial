package fr.euphyllia.fidorial.server.configuration;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.fidorial.entity.GameMode;
import fr.fidorial.world.Location;

import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.commented;
import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.configRecord;

public record WorldConfiguration(Gameplay gameplay, Storage storage) {

    public static final WorldConfiguration DEFAULTS = new WorldConfiguration(Gameplay.DEFAULTS, Storage.DEFAULTS);

    public static final Codec<WorldConfiguration> CODEC = codec(DEFAULTS, true);

    /**
     * The codec for a file whose missing settings fall back to {@code defaults}. Encodes only the settings that
     * differ from {@code defaults}.
     */
    public static Codec<WorldConfiguration> sparseCodec(final WorldConfiguration defaults) {
        return codec(defaults, false);
    }

    private static Codec<WorldConfiguration> codec(final WorldConfiguration defaults, final boolean writeDefaults) {
        return configRecord(WorldConfiguration.class, defaults, writeDefaults)
                .field("gameplay", WorldConfiguration::gameplay, commented(Gameplay.codec(defaults.gameplay(), writeDefaults),
                        "Gameplay settings"))
                .field("storage", WorldConfiguration::storage, commented(Storage.codec(defaults.storage(), writeDefaults),
                        "World storage settings"))
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

        static Codec<Gameplay> codec(final Gameplay defaults, final boolean writeDefaults) {
            return configRecord(Gameplay.class, defaults, writeDefaults)
                    .field("pvp", Gameplay::pvp, commented(Codec.BOOL,
                            "Whether PVP is enabled"))
                    .field("default-game-mode", Gameplay::defaultGameMode, commented(ConfigurationCodecs.GAME_MODE,
                            "The gamemode given to players joining for the first time. Available options are: survival, creative, adventure, spectator"))
                    .field("generate-structures", Gameplay::generateStructures, commented(Codec.BOOL,
                            "Whether to generate structures. Effective only when a datapack containing them is loaded"))
                    .field("spawn", Gameplay::spawn, commented(spawnCodec(defaults.spawn(), writeDefaults),
                            "The spawn position and rotation for joining players. Defaults to Vanilla's superflat default spawn position"))
                    .build();
        }

        private static Codec<Location> spawnCodec(final Location defaults, final boolean writeDefaults) {
            return configRecord(Location.class, defaults, writeDefaults)
                    .field("x", Location::x, Codec.DOUBLE)
                    .field("y", Location::y, Codec.DOUBLE)
                    .field("z", Location::z, Codec.DOUBLE)
                    .field("yaw", Location::yaw, Codec.FLOAT)
                    .field("pitch", Location::pitch, Codec.FLOAT)
                    .build();
        }
    }


    public record Storage(RegionCompression regionCompression, boolean convertExistingChunks) {

        public static final Storage DEFAULTS = new Storage(RegionCompression.DEFAULT, true);

        static Codec<Storage> codec(final Storage defaults, final boolean writeDefaults) {
            return configRecord(Storage.class, defaults, writeDefaults)
                    .field("region-compression", Storage::regionCompression, commented(ConfigurationCodecs.REGION_COMPRESSION,
                            "The compression used for chunks and entities in region files (.mca). Available options are: "
                                    + RegionCompression.names() + ".\n"
                                    + "zlib is Vanilla's default. lz4 is much faster but makes larger files. none is the fastest and the largest.\n"
                                    + "zstd makes smaller files than zlib at a similar or lower CPU cost, but Vanilla cannot read it:\n"
                                    + "switch back to zlib (or lz4) and let the world convert before opening it with Vanilla.\n"
                                    + "Every format can always be read: chunks saved in another format are rewritten in this one when they load"))
                    .field("convert-existing-chunks", Storage::convertExistingChunks, commented(Codec.BOOL,
                            "Whether to rewrite every saved chunk of the world in the new format the next time it loads, after region-compression changes.\n"
                                    + "This happens once per change and may take a while on a large world.\n"
                                    + "When false, chunks are only converted as they load"))
                    .build();
        }
    }
}
