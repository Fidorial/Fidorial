package fr.euphyllia.fidorial.server.configuration;

import alternate.current.wire.UpdateOrder;
import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs;
import fr.euphyllia.fidorial.server.inventory.HiddenRows;
import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.euphyllia.fidorial.server.world.redstone.DustImplementation;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.BarrelInventory;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;

import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.commented;
import static fr.euphyllia.fidorial.server.codecs.configuration.ConfigurationCodecs.configRecord;

public record WorldConfiguration(Gameplay gameplay, Storage storage, Redstone redstone, Mobs mobs, Blocks blocks) {

    public static final WorldConfiguration DEFAULTS =
            new WorldConfiguration(Gameplay.DEFAULTS, Storage.DEFAULTS, Redstone.DEFAULTS, Mobs.DEFAULTS, Blocks.DEFAULTS);

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
                .field("redstone", WorldConfiguration::redstone, commented(Redstone.codec(defaults.redstone(), writeDefaults),
                        "Redstone settings"))
                .field("mobs", WorldConfiguration::mobs, commented(Mobs.codec(defaults.mobs(), writeDefaults),
                        "What the mobs of the world may do, mob by mob.\n"
                                + "The settings letting a mob change blocks only narrow the mob_griefing game rule, which must allow it too"))
                .field("blocks", WorldConfiguration::blocks, commented(Blocks.codec(defaults.blocks(), writeDefaults),
                        "Settings of the blocks of the world, block by block"))
                .build();
    }

    public record Blocks(Barrel barrel) {

        public static final Blocks DEFAULTS = new Blocks(Barrel.DEFAULTS);

        static Codec<Blocks> codec(final Blocks defaults, final boolean writeDefaults) {
            return configRecord(Blocks.class, defaults, writeDefaults)
                    .field("barrel", Blocks::barrel, commented(Barrel.codec(defaults.barrel(), writeDefaults),
                            "The size of the barrels"))
                    .build();
        }
    }

    public record Barrel(int rows, HiddenRows hiddenRows) {

        public static final Barrel DEFAULTS = new Barrel(BarrelInventory.DEFAULT_ROWS, HiddenRows.CLEAR);

        static Codec<Barrel> codec(final Barrel defaults, final boolean writeDefaults) {
            return configRecord(Barrel.class, defaults, writeDefaults)
                    .field("rows", Barrel::rows, commented(Codec.intRange(1, BarrelInventory.MAX_ROWS),
                            "The rows of slots of every barrel of the world, from 1 to 6"))
                    .field("hidden-rows", Barrel::hiddenRows, commented(ConfigurationCodecs.HIDDEN_ROWS,
                            "What becomes of the items of the rows a barrel no longer shows, once rows is lowered.\n"
                                    + "Available options are: " + HiddenRows.names() + ".\n"
                                    + "clear empties those rows when a player opens the barrel: the items are lost, and logged.\n"
                                    + "keep keeps the items out of reach, until rows is raised again.\n"
                                    + "Either way, breaking a barrel spills every item it holds, those of the rows no longer shown included"))
                    .build();
        }
    }

    public record Gameplay(
            boolean pvp,
            GameMode defaultGameMode,
            boolean generateStructures,
            Spawn spawn
    ) {

        public static final Gameplay DEFAULTS = new Gameplay(
                true, GameMode.SURVIVAL, true, new Spawn(8.5, -48.0, 8.5, 0, 0));

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

        private static Codec<Spawn> spawnCodec(final Spawn defaults, final boolean writeDefaults) {
            return configRecord(Spawn.class, defaults, writeDefaults)
                    .field("x", Spawn::x, Codec.DOUBLE)
                    .field("y", Spawn::y, Codec.DOUBLE)
                    .field("z", Spawn::z, Codec.DOUBLE)
                    .field("yaw", Spawn::yaw, Codec.FLOAT)
                    .field("pitch", Spawn::pitch, Codec.FLOAT)
                    .build();
        }

        public record Spawn(double x, double y, double z, float yaw, float pitch) {
            public Location location(final World world) {
                return Location.of(world, x, y, z, yaw, pitch);
            }
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

    public record Redstone(DustImplementation dustImplementation, AlternateCurrent alternateCurrent) {

        public static final Redstone DEFAULTS = new Redstone(DustImplementation.VANILLA, AlternateCurrent.DEFAULTS);

        static Codec<Redstone> codec(final Redstone defaults, final boolean writeDefaults) {
            return configRecord(Redstone.class, defaults, writeDefaults)
                    .field("dust-implementation", Redstone::dustImplementation, commented(ConfigurationCodecs.DUST_IMPLEMENTATION,
                            "How redstone dust works out the power it carries. Available options are: " + DustImplementation.names() + ".\n"
                                    + "vanilla is Vanilla's: each dust updates on its own, in an order that depends on where it is.\n"
                                    + "alternate-current is Space Walker's Alternate Current (https://github.com/SpaceWalkerRS/alternate-current):\n"
                                    + "a whole network of dust is updated at once, which lags far less, and the blocks around it are\n"
                                    + "updated in an order that follows the flow of power rather than the position of the dust.\n"
                                    + "Circuits relying on Vanilla's location-dependent update order may behave differently with it"))
                    .field("alternate-current", Redstone::alternateCurrent, commented(AlternateCurrent.codec(defaults.alternateCurrent(), writeDefaults),
                            "Settings of Alternate Current, used when dust-implementation is alternate-current"))
                    .build();
        }
    }

    public record AlternateCurrent(UpdateOrder updateOrder) {

        public static final AlternateCurrent DEFAULTS = new AlternateCurrent(UpdateOrder.HORIZONTAL_FIRST_OUTWARD);

        static Codec<AlternateCurrent> codec(final AlternateCurrent defaults, final boolean writeDefaults) {
            return configRecord(AlternateCurrent.class, defaults, writeDefaults)
                    .field("update-order", AlternateCurrent::updateOrder, commented(ConfigurationCodecs.UPDATE_ORDER,
                            "The order the blocks around a dust are updated in, relative to the direction power flows through it.\n"
                                    + "Available options are: " + ConfigurationCodecs.UPDATE_ORDERS + ".\n"
                                    + "horizontal-first-outward, the default, updates the direct neighbours first, then the diagonal ones,\n"
                                    + "then those two blocks away, horizontal before vertical in each group. inward is the reverse,\n"
                                    + "vertical-first puts vertical neighbours before horizontal ones"))
                    .build();
        }
    }

    public record Mobs(Creeper creeper, Enderman enderman, Silverfish silverfish, Zombie zombie) {

        public static final Mobs DEFAULTS = new Mobs(Creeper.DEFAULTS, Enderman.DEFAULTS, Silverfish.DEFAULTS, Zombie.DEFAULTS);

        static Codec<Mobs> codec(final Mobs defaults, final boolean writeDefaults) {
            return configRecord(Mobs.class, defaults, writeDefaults)
                    .field("creeper", Mobs::creeper, commented(Creeper.codec(defaults.creeper(), writeDefaults),
                            "Creepers"))
                    .field("enderman", Mobs::enderman, commented(Enderman.codec(defaults.enderman(), writeDefaults),
                            "Endermen"))
                    .field("silverfish", Mobs::silverfish, commented(Silverfish.codec(defaults.silverfish(), writeDefaults),
                            "Silverfish"))
                    .field("zombie", Mobs::zombie, commented(Zombie.codec(defaults.zombie(), writeDefaults),
                            "Zombies, husks, drowned and frostbites"))
                    .build();
        }

        public record Creeper(boolean explosionsBreakBlocks) {

            public static final Creeper DEFAULTS = new Creeper(true);

            static Codec<Creeper> codec(final Creeper defaults, final boolean writeDefaults) {
                return configRecord(Creeper.class, defaults, writeDefaults)
                        .field("explosions-break-blocks", Creeper::explosionsBreakBlocks, commented(Codec.BOOL,
                                "Whether creeper explosions break blocks. They hurt and push entities either way"))
                        .build();
            }
        }

        public record Enderman(boolean movesBlocks) {

            public static final Enderman DEFAULTS = new Enderman(true);

            static Codec<Enderman> codec(final Enderman defaults, final boolean writeDefaults) {
                return configRecord(Enderman.class, defaults, writeDefaults)
                        .field("moves-blocks", Enderman::movesBlocks, commented(Codec.BOOL,
                                "Whether endermen pick blocks up and put them down elsewhere.\n"
                                        + "An enderman already carrying a block keeps it when this is false"))
                        .build();
            }
        }

        public record Silverfish(boolean hidesInBlocks) {

            public static final Silverfish DEFAULTS = new Silverfish(true);

            static Codec<Silverfish> codec(final Silverfish defaults, final boolean writeDefaults) {
                return configRecord(Silverfish.class, defaults, writeDefaults)
                        .field("hides-in-blocks", Silverfish::hidesInBlocks, commented(Codec.BOOL,
                                "Whether silverfish slip into stone and the like, turning them into infested blocks"))
                        .build();
            }
        }

        public record Zombie(boolean breaksDoors, boolean convertsInWater, boolean convertsInPowderSnow) {

            public static final Zombie DEFAULTS = new Zombie(true, true, true);

            static Codec<Zombie> codec(final Zombie defaults, final boolean writeDefaults) {
                return configRecord(Zombie.class, defaults, writeDefaults)
                        .field("breaks-doors", Zombie::breaksDoors, commented(Codec.BOOL,
                                "Whether the zombies able to break wooden doors do so, which they only do on hard difficulty"))
                        .field("converts-in-water", Zombie::convertsInWater, commented(Codec.BOOL,
                                "Whether a zombie kept under water turns into a drowned, and a husk or a frostbite into a zombie"))
                        .field("converts-in-powder-snow", Zombie::convertsInPowderSnow, commented(Codec.BOOL,
                                "Whether a zombie kept in powder snow turns into a frostbite, and a husk or a drowned into a zombie"))
                        .build();
            }
        }
    }
}
