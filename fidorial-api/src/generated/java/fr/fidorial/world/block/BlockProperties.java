package fr.fidorial.world.block;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.block.data.FaceAttachable;
import fr.fidorial.world.block.data.Orientable;
import fr.fidorial.world.block.data.type.Chest;
import fr.fidorial.world.block.data.type.Comparator;
import fr.fidorial.world.block.data.type.Door;
import fr.fidorial.world.block.data.type.RedstoneWire;
import fr.fidorial.world.block.data.type.Slab;
import fr.fidorial.world.block.data.type.Speleothem;
import fr.fidorial.world.block.data.type.Stairs;
import fr.fidorial.world.block.property.BambooFoliage;
import fr.fidorial.world.block.property.BedEnd;
import fr.fidorial.world.block.property.BellMount;
import fr.fidorial.world.block.property.CreakingHeartPhase;
import fr.fidorial.world.block.property.DripleafTilt;
import fr.fidorial.world.block.property.NoteInstrument;
import fr.fidorial.world.block.property.Orientation;
import fr.fidorial.world.block.property.PistonKind;
import fr.fidorial.world.block.property.PotentSulfurPhase;
import fr.fidorial.world.block.property.RailTrack;
import fr.fidorial.world.block.property.SculkPhase;
import fr.fidorial.world.block.property.ShelfConnection;
import fr.fidorial.world.block.property.StatuePose;
import fr.fidorial.world.block.property.StructureBlockMode;
import fr.fidorial.world.block.property.TestMode;
import fr.fidorial.world.block.property.TrialSpawnerPhase;
import fr.fidorial.world.block.property.VaultPhase;
import fr.fidorial.world.block.property.WallHeight;

/**
 * The properties of the vanilla blocks.
 *
 * <p>Generated from Mojang's block report; do not edit.</p>
 *
 * @since 0.1.0
 */
public final class BlockProperties {
    public static final BlockProperty.Range AGE_0_1 = new BlockProperty.Range(Names.AGE, 0, 1);

    public static final BlockProperty.Range AGE_0_15 = new BlockProperty.Range(Names.AGE, 0, 15);

    public static final BlockProperty.Range AGE_0_2 = new BlockProperty.Range(Names.AGE, 0, 2);

    public static final BlockProperty.Range AGE_0_25 = new BlockProperty.Range(Names.AGE, 0, 25);

    public static final BlockProperty.Range AGE_0_3 = new BlockProperty.Range(Names.AGE, 0, 3);

    public static final BlockProperty.Range AGE_0_4 = new BlockProperty.Range(Names.AGE, 0, 4);

    public static final BlockProperty.Range AGE_0_5 = new BlockProperty.Range(Names.AGE, 0, 5);

    public static final BlockProperty.Range AGE_0_7 = new BlockProperty.Range(Names.AGE, 0, 7);

    public static final BlockProperty.Flag ATTACHED = new BlockProperty.Flag(Names.ATTACHED);

    public static final BlockProperty.Choice<BellMount> ATTACHMENT = BlockProperty.Choice.of(Names.ATTACHMENT, BellMount.class, BellMount.FLOOR, BellMount.CEILING, BellMount.SINGLE_WALL, BellMount.DOUBLE_WALL);

    public static final BlockProperty.Choice<Orientable.Axis> AXIS = BlockProperty.Choice.of(Names.AXIS, Orientable.Axis.class, Orientable.Axis.X, Orientable.Axis.Y, Orientable.Axis.Z);

    public static final BlockProperty.Flag BERRIES = new BlockProperty.Flag(Names.BERRIES);

    public static final BlockProperty.Range BITES = new BlockProperty.Range(Names.BITES, 0, 6);

    public static final BlockProperty.Flag BLOOM = new BlockProperty.Flag(Names.BLOOM);

    public static final BlockProperty.Flag BOTTOM = new BlockProperty.Flag(Names.BOTTOM);

    public static final BlockProperty.Range CANDLES = new BlockProperty.Range(Names.CANDLES, 1, 4);

    public static final BlockProperty.Flag CAN_SUMMON = new BlockProperty.Flag(Names.CAN_SUMMON);

    public static final BlockProperty.Range CHARGES = new BlockProperty.Range(Names.CHARGES, 0, 4);

    public static final BlockProperty.Choice<Chest.Type> CHEST_TYPE = BlockProperty.Choice.of(Names.TYPE, Chest.Type.class, Chest.Type.SINGLE, Chest.Type.LEFT, Chest.Type.RIGHT);

    public static final BlockProperty.Choice<Comparator.Mode> COMPARATOR_MODE = BlockProperty.Choice.of(Names.MODE, Comparator.Mode.class, Comparator.Mode.COMPARE, Comparator.Mode.SUBTRACT);

    public static final BlockProperty.Flag CONDITIONAL = new BlockProperty.Flag(Names.CONDITIONAL);

    public static final BlockProperty.Choice<StatuePose> COPPER_GOLEM_POSE = BlockProperty.Choice.of(Names.COPPER_GOLEM_POSE, StatuePose.class, StatuePose.STANDING, StatuePose.SITTING, StatuePose.RUNNING, StatuePose.STAR);

    public static final BlockProperty.Flag CRACKED = new BlockProperty.Flag(Names.CRACKED);

    public static final BlockProperty.Flag CRAFTING = new BlockProperty.Flag(Names.CRAFTING);

    public static final BlockProperty.Choice<CreakingHeartPhase> CREAKING_HEART_STATE = BlockProperty.Choice.of(Names.CREAKING_HEART_STATE, CreakingHeartPhase.class, CreakingHeartPhase.UPROOTED, CreakingHeartPhase.DORMANT, CreakingHeartPhase.AWAKE);

    public static final BlockProperty.Range DELAY = new BlockProperty.Range(Names.DELAY, 1, 4);

    public static final BlockProperty.Flag DISARMED = new BlockProperty.Flag(Names.DISARMED);

    public static final BlockProperty.Range DISTANCE_0_7 = new BlockProperty.Range(Names.DISTANCE, 0, 7);

    public static final BlockProperty.Range DISTANCE_1_7 = new BlockProperty.Range(Names.DISTANCE, 1, 7);

    public static final BlockProperty.Choice<Bisected.Half> DOUBLE_BLOCK_HALF = BlockProperty.Choice.of(Names.HALF, Bisected.Half.class, Bisected.Half.UPPER, Bisected.Half.LOWER);

    public static final BlockProperty.Flag DOWN = new BlockProperty.Flag(Names.DOWN);

    public static final BlockProperty.Flag DRAG = new BlockProperty.Flag(Names.DRAG);

    public static final BlockProperty.Range DUSTED = new BlockProperty.Range(Names.DUSTED, 0, 3);

    public static final BlockProperty.Flag EAST = new BlockProperty.Flag(Names.EAST);

    public static final BlockProperty.Range EGGS = new BlockProperty.Range(Names.EGGS, 1, 4);

    public static final BlockProperty.Flag ENABLED = new BlockProperty.Flag(Names.ENABLED);

    public static final BlockProperty.Flag EXTENDED = new BlockProperty.Flag(Names.EXTENDED);

    public static final BlockProperty.Flag EYE = new BlockProperty.Flag(Names.EYE);

    public static final BlockProperty.Choice<FaceAttachable.AttachedFace> FACE = BlockProperty.Choice.of(Names.FACE, FaceAttachable.AttachedFace.class, FaceAttachable.AttachedFace.FLOOR, FaceAttachable.AttachedFace.WALL, FaceAttachable.AttachedFace.CEILING);

    public static final BlockProperty.Choice<BlockFace> FACING = BlockProperty.Choice.of(Names.FACING, BlockFace.class, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

    public static final BlockProperty.Range FLOWER_AMOUNT = new BlockProperty.Range(Names.FLOWER_AMOUNT, 1, 4);

    public static final BlockProperty.Choice<Bisected.Half> HALF = BlockProperty.Choice.of(Names.HALF, Bisected.Half.class, Bisected.Half.TOP, Bisected.Half.BOTTOM);

    public static final BlockProperty.Flag HANGING = new BlockProperty.Flag(Names.HANGING);

    public static final BlockProperty.Flag HAS_BOOK = new BlockProperty.Flag(Names.HAS_BOOK);

    public static final BlockProperty.Flag HAS_BOTTLE_0 = new BlockProperty.Flag(Names.HAS_BOTTLE_0);

    public static final BlockProperty.Flag HAS_BOTTLE_1 = new BlockProperty.Flag(Names.HAS_BOTTLE_1);

    public static final BlockProperty.Flag HAS_BOTTLE_2 = new BlockProperty.Flag(Names.HAS_BOTTLE_2);

    public static final BlockProperty.Flag HAS_RECORD = new BlockProperty.Flag(Names.HAS_RECORD);

    public static final BlockProperty.Range HATCH = new BlockProperty.Range(Names.HATCH, 0, 2);

    public static final BlockProperty.Choice<Door.Hinge> HINGE = BlockProperty.Choice.of(Names.HINGE, Door.Hinge.class, Door.Hinge.LEFT, Door.Hinge.RIGHT);

    public static final BlockProperty.Range HONEY_LEVEL = new BlockProperty.Range(Names.HONEY_LEVEL, 0, 5);

    public static final BlockProperty.Choice<BlockFace> HOPPER_FACING = BlockProperty.Choice.of(Names.FACING, BlockFace.class, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

    public static final BlockProperty.Choice<Orientable.Axis> HORIZONTAL_AXIS = BlockProperty.Choice.of(Names.AXIS, Orientable.Axis.class, Orientable.Axis.X, Orientable.Axis.Z);

    public static final BlockProperty.Choice<BlockFace> HORIZONTAL_FACING = BlockProperty.Choice.of(Names.FACING, BlockFace.class, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

    public static final BlockProperty.Range HYDRATION = new BlockProperty.Range(Names.HYDRATION, 0, 3);

    public static final BlockProperty.Choice<NoteInstrument> INSTRUMENT = BlockProperty.Choice.of(Names.INSTRUMENT, NoteInstrument.class, NoteInstrument.HARP, NoteInstrument.BASEDRUM, NoteInstrument.SNARE, NoteInstrument.HAT, NoteInstrument.BASS, NoteInstrument.FLUTE, NoteInstrument.BELL, NoteInstrument.GUITAR, NoteInstrument.CHIME, NoteInstrument.XYLOPHONE, NoteInstrument.IRON_XYLOPHONE, NoteInstrument.COW_BELL, NoteInstrument.DIDGERIDOO, NoteInstrument.BIT, NoteInstrument.BANJO, NoteInstrument.PLING, NoteInstrument.TRUMPET, NoteInstrument.TRUMPET_EXPOSED, NoteInstrument.TRUMPET_OXIDIZED, NoteInstrument.TRUMPET_WEATHERED, NoteInstrument.ZOMBIE, NoteInstrument.SKELETON, NoteInstrument.CREEPER, NoteInstrument.DRAGON, NoteInstrument.WITHER_SKELETON, NoteInstrument.PIGLIN, NoteInstrument.CUSTOM_HEAD);

    public static final BlockProperty.Flag INVERTED = new BlockProperty.Flag(Names.INVERTED);

    public static final BlockProperty.Flag IN_WALL = new BlockProperty.Flag(Names.IN_WALL);

    public static final BlockProperty.Range LAYERS = new BlockProperty.Range(Names.LAYERS, 1, 8);

    public static final BlockProperty.Choice<BambooFoliage> LEAVES = BlockProperty.Choice.of(Names.LEAVES, BambooFoliage.class, BambooFoliage.NONE, BambooFoliage.SMALL, BambooFoliage.LARGE);

    public static final BlockProperty.Range LEVEL_0_15 = new BlockProperty.Range(Names.LEVEL, 0, 15);

    public static final BlockProperty.Range LEVEL_0_8 = new BlockProperty.Range(Names.LEVEL, 0, 8);

    public static final BlockProperty.Range LEVEL_1_3 = new BlockProperty.Range(Names.LEVEL, 1, 3);

    public static final BlockProperty.Flag LIT = new BlockProperty.Flag(Names.LIT);

    public static final BlockProperty.Flag LOCKED = new BlockProperty.Flag(Names.LOCKED);

    public static final BlockProperty.Range MOISTURE = new BlockProperty.Range(Names.MOISTURE, 0, 7);

    public static final BlockProperty.Flag NATURAL = new BlockProperty.Flag(Names.NATURAL);

    public static final BlockProperty.Flag NORTH = new BlockProperty.Flag(Names.NORTH);

    public static final BlockProperty.Range NOTE = new BlockProperty.Range(Names.NOTE, 0, 24);

    public static final BlockProperty.Flag OCCUPIED = new BlockProperty.Flag(Names.OCCUPIED);

    public static final BlockProperty.Flag OMINOUS = new BlockProperty.Flag(Names.OMINOUS);

    public static final BlockProperty.Flag OPEN = new BlockProperty.Flag(Names.OPEN);

    public static final BlockProperty.Choice<Orientation> ORIENTATION = BlockProperty.Choice.of(Names.ORIENTATION, Orientation.class, Orientation.DOWN_EAST, Orientation.DOWN_NORTH, Orientation.DOWN_SOUTH, Orientation.DOWN_WEST, Orientation.UP_EAST, Orientation.UP_NORTH, Orientation.UP_SOUTH, Orientation.UP_WEST, Orientation.WEST_UP, Orientation.EAST_UP, Orientation.NORTH_UP, Orientation.SOUTH_UP);

    public static final BlockProperty.Choice<BedEnd> PART = BlockProperty.Choice.of(Names.PART, BedEnd.class, BedEnd.HEAD, BedEnd.FOOT);

    public static final BlockProperty.Flag PERSISTENT = new BlockProperty.Flag(Names.PERSISTENT);

    public static final BlockProperty.Range PICKLES = new BlockProperty.Range(Names.PICKLES, 1, 4);

    public static final BlockProperty.Choice<PistonKind> PISTON_TYPE = BlockProperty.Choice.of(Names.TYPE, PistonKind.class, PistonKind.NORMAL, PistonKind.STICKY);

    public static final BlockProperty.Flag PLAYLIST_MODE = new BlockProperty.Flag(Names.PLAYLIST_MODE);

    public static final BlockProperty.Choice<PotentSulfurPhase> POTENT_SULFUR_STATE = BlockProperty.Choice.of(Names.POTENT_SULFUR_STATE, PotentSulfurPhase.class, PotentSulfurPhase.DRY, PotentSulfurPhase.WET, PotentSulfurPhase.DORMANT, PotentSulfurPhase.ERUPTING, PotentSulfurPhase.CONTINUOUS);

    public static final BlockProperty.Range POWER = new BlockProperty.Range(Names.POWER, 0, 15);

    public static final BlockProperty.Flag POWERED = new BlockProperty.Flag(Names.POWERED);

    public static final BlockProperty.Choice<RailTrack> RAIL_SHAPE = BlockProperty.Choice.of(Names.SHAPE, RailTrack.class, RailTrack.NORTH_SOUTH, RailTrack.EAST_WEST, RailTrack.ASCENDING_EAST, RailTrack.ASCENDING_WEST, RailTrack.ASCENDING_NORTH, RailTrack.ASCENDING_SOUTH, RailTrack.SOUTH_EAST, RailTrack.SOUTH_WEST, RailTrack.NORTH_WEST, RailTrack.NORTH_EAST);

    public static final BlockProperty.Choice<RedstoneWire.Connection> REDSTONE_EAST = BlockProperty.Choice.of(Names.EAST, RedstoneWire.Connection.class, RedstoneWire.Connection.UP, RedstoneWire.Connection.SIDE, RedstoneWire.Connection.NONE);

    public static final BlockProperty.Choice<RedstoneWire.Connection> REDSTONE_NORTH = BlockProperty.Choice.of(Names.NORTH, RedstoneWire.Connection.class, RedstoneWire.Connection.UP, RedstoneWire.Connection.SIDE, RedstoneWire.Connection.NONE);

    public static final BlockProperty.Choice<RedstoneWire.Connection> REDSTONE_SOUTH = BlockProperty.Choice.of(Names.SOUTH, RedstoneWire.Connection.class, RedstoneWire.Connection.UP, RedstoneWire.Connection.SIDE, RedstoneWire.Connection.NONE);

    public static final BlockProperty.Choice<RedstoneWire.Connection> REDSTONE_WEST = BlockProperty.Choice.of(Names.WEST, RedstoneWire.Connection.class, RedstoneWire.Connection.UP, RedstoneWire.Connection.SIDE, RedstoneWire.Connection.NONE);

    public static final BlockProperty.Range ROTATION = new BlockProperty.Range(Names.ROTATION, 0, 15);

    public static final BlockProperty.Choice<SculkPhase> SCULK_SENSOR_PHASE = BlockProperty.Choice.of(Names.SCULK_SENSOR_PHASE, SculkPhase.class, SculkPhase.INACTIVE, SculkPhase.ACTIVE, SculkPhase.COOLDOWN);

    public static final BlockProperty.Range SEGMENT_AMOUNT = new BlockProperty.Range(Names.SEGMENT_AMOUNT, 1, 4);

    public static final BlockProperty.Flag SHORT = new BlockProperty.Flag(Names.SHORT);

    public static final BlockProperty.Flag SHRIEKING = new BlockProperty.Flag(Names.SHRIEKING);

    public static final BlockProperty.Choice<ShelfConnection> SIDE_CHAIN = BlockProperty.Choice.of(Names.SIDE_CHAIN, ShelfConnection.class, ShelfConnection.UNCONNECTED, ShelfConnection.RIGHT, ShelfConnection.CENTER, ShelfConnection.LEFT);

    public static final BlockProperty.Flag SIGNAL_FIRE = new BlockProperty.Flag(Names.SIGNAL_FIRE);

    public static final BlockProperty.Choice<Slab.Type> SLAB_TYPE = BlockProperty.Choice.of(Names.TYPE, Slab.Type.class, Slab.Type.TOP, Slab.Type.BOTTOM, Slab.Type.DOUBLE);

    public static final BlockProperty.Flag SLOT_0_OCCUPIED = new BlockProperty.Flag(Names.SLOT_0_OCCUPIED);

    public static final BlockProperty.Flag SLOT_1_OCCUPIED = new BlockProperty.Flag(Names.SLOT_1_OCCUPIED);

    public static final BlockProperty.Flag SLOT_2_OCCUPIED = new BlockProperty.Flag(Names.SLOT_2_OCCUPIED);

    public static final BlockProperty.Flag SLOT_3_OCCUPIED = new BlockProperty.Flag(Names.SLOT_3_OCCUPIED);

    public static final BlockProperty.Flag SLOT_4_OCCUPIED = new BlockProperty.Flag(Names.SLOT_4_OCCUPIED);

    public static final BlockProperty.Flag SLOT_5_OCCUPIED = new BlockProperty.Flag(Names.SLOT_5_OCCUPIED);

    public static final BlockProperty.Flag SNOWY = new BlockProperty.Flag(Names.SNOWY);

    public static final BlockProperty.Flag SOUTH = new BlockProperty.Flag(Names.SOUTH);

    public static final BlockProperty.Range STAGE = new BlockProperty.Range(Names.STAGE, 0, 1);

    public static final BlockProperty.Choice<Stairs.Shape> STAIRS_SHAPE = BlockProperty.Choice.of(Names.SHAPE, Stairs.Shape.class, Stairs.Shape.STRAIGHT, Stairs.Shape.INNER_LEFT, Stairs.Shape.INNER_RIGHT, Stairs.Shape.OUTER_LEFT, Stairs.Shape.OUTER_RIGHT);

    public static final BlockProperty.Choice<RailTrack> STRAIGHT_RAIL_SHAPE = BlockProperty.Choice.of(Names.SHAPE, RailTrack.class, RailTrack.NORTH_SOUTH, RailTrack.EAST_WEST, RailTrack.ASCENDING_EAST, RailTrack.ASCENDING_WEST, RailTrack.ASCENDING_NORTH, RailTrack.ASCENDING_SOUTH);

    public static final BlockProperty.Choice<StructureBlockMode> STRUCTURE_BLOCK_MODE = BlockProperty.Choice.of(Names.MODE, StructureBlockMode.class, StructureBlockMode.SAVE, StructureBlockMode.LOAD, StructureBlockMode.CORNER, StructureBlockMode.DATA);

    public static final BlockProperty.Choice<TestMode> TEST_BLOCK_MODE = BlockProperty.Choice.of(Names.MODE, TestMode.class, TestMode.START, TestMode.LOG, TestMode.FAIL, TestMode.ACCEPT);

    public static final BlockProperty.Choice<Speleothem.Thickness> THICKNESS = BlockProperty.Choice.of(Names.THICKNESS, Speleothem.Thickness.class, Speleothem.Thickness.TIP_MERGE, Speleothem.Thickness.TIP, Speleothem.Thickness.FRUSTUM, Speleothem.Thickness.MIDDLE, Speleothem.Thickness.BASE);

    public static final BlockProperty.Choice<DripleafTilt> TILT = BlockProperty.Choice.of(Names.TILT, DripleafTilt.class, DripleafTilt.NONE, DripleafTilt.UNSTABLE, DripleafTilt.PARTIAL, DripleafTilt.FULL);

    public static final BlockProperty.Flag TIP = new BlockProperty.Flag(Names.TIP);

    public static final BlockProperty.Choice<TrialSpawnerPhase> TRIAL_SPAWNER_STATE = BlockProperty.Choice.of(Names.TRIAL_SPAWNER_STATE, TrialSpawnerPhase.class, TrialSpawnerPhase.INACTIVE, TrialSpawnerPhase.WAITING_FOR_PLAYERS, TrialSpawnerPhase.ACTIVE, TrialSpawnerPhase.WAITING_FOR_REWARD_EJECTION, TrialSpawnerPhase.EJECTING_REWARD, TrialSpawnerPhase.COOLDOWN);

    public static final BlockProperty.Flag TRIGGERED = new BlockProperty.Flag(Names.TRIGGERED);

    public static final BlockProperty.Flag UNSTABLE = new BlockProperty.Flag(Names.UNSTABLE);

    public static final BlockProperty.Flag UP = new BlockProperty.Flag(Names.UP);

    public static final BlockProperty.Choice<VaultPhase> VAULT_STATE = BlockProperty.Choice.of(Names.VAULT_STATE, VaultPhase.class, VaultPhase.INACTIVE, VaultPhase.ACTIVE, VaultPhase.UNLOCKING, VaultPhase.EJECTING);

    public static final BlockProperty.Choice<BlockFace> VERTICAL_DIRECTION = BlockProperty.Choice.of(Names.VERTICAL_DIRECTION, BlockFace.class, BlockFace.UP, BlockFace.DOWN);

    public static final BlockProperty.Choice<WallHeight> WALL_EAST = BlockProperty.Choice.of(Names.EAST, WallHeight.class, WallHeight.NONE, WallHeight.LOW, WallHeight.TALL);

    public static final BlockProperty.Choice<WallHeight> WALL_NORTH = BlockProperty.Choice.of(Names.NORTH, WallHeight.class, WallHeight.NONE, WallHeight.LOW, WallHeight.TALL);

    public static final BlockProperty.Choice<WallHeight> WALL_SOUTH = BlockProperty.Choice.of(Names.SOUTH, WallHeight.class, WallHeight.NONE, WallHeight.LOW, WallHeight.TALL);

    public static final BlockProperty.Choice<WallHeight> WALL_WEST = BlockProperty.Choice.of(Names.WEST, WallHeight.class, WallHeight.NONE, WallHeight.LOW, WallHeight.TALL);

    public static final BlockProperty.Flag WATERLOGGED = new BlockProperty.Flag(Names.WATERLOGGED);

    public static final BlockProperty.Flag WEST = new BlockProperty.Flag(Names.WEST);

    private BlockProperties() {
    }

    /**
     * The names of the vanilla block properties, each shared by every property of that name.
     *
     * @since 0.1.0
     */
    public static final class Names {
        public static final String AGE = "age";

        public static final String ATTACHED = "attached";

        public static final String ATTACHMENT = "attachment";

        public static final String AXIS = "axis";

        public static final String BERRIES = "berries";

        public static final String BITES = "bites";

        public static final String BLOOM = "bloom";

        public static final String BOTTOM = "bottom";

        public static final String CAN_SUMMON = "can_summon";

        public static final String CANDLES = "candles";

        public static final String CHARGES = "charges";

        public static final String CONDITIONAL = "conditional";

        public static final String COPPER_GOLEM_POSE = "copper_golem_pose";

        public static final String CRACKED = "cracked";

        public static final String CRAFTING = "crafting";

        public static final String CREAKING_HEART_STATE = "creaking_heart_state";

        public static final String DELAY = "delay";

        public static final String DISARMED = "disarmed";

        public static final String DISTANCE = "distance";

        public static final String DOWN = "down";

        public static final String DRAG = "drag";

        public static final String DUSTED = "dusted";

        public static final String EAST = "east";

        public static final String EGGS = "eggs";

        public static final String ENABLED = "enabled";

        public static final String EXTENDED = "extended";

        public static final String EYE = "eye";

        public static final String FACE = "face";

        public static final String FACING = "facing";

        public static final String FLOWER_AMOUNT = "flower_amount";

        public static final String HALF = "half";

        public static final String HANGING = "hanging";

        public static final String HAS_BOOK = "has_book";

        public static final String HAS_BOTTLE_0 = "has_bottle_0";

        public static final String HAS_BOTTLE_1 = "has_bottle_1";

        public static final String HAS_BOTTLE_2 = "has_bottle_2";

        public static final String HAS_RECORD = "has_record";

        public static final String HATCH = "hatch";

        public static final String HINGE = "hinge";

        public static final String HONEY_LEVEL = "honey_level";

        public static final String HYDRATION = "hydration";

        public static final String IN_WALL = "in_wall";

        public static final String INSTRUMENT = "instrument";

        public static final String INVERTED = "inverted";

        public static final String LAYERS = "layers";

        public static final String LEAVES = "leaves";

        public static final String LEVEL = "level";

        public static final String LIT = "lit";

        public static final String LOCKED = "locked";

        public static final String MODE = "mode";

        public static final String MOISTURE = "moisture";

        public static final String NATURAL = "natural";

        public static final String NORTH = "north";

        public static final String NOTE = "note";

        public static final String OCCUPIED = "occupied";

        public static final String OMINOUS = "ominous";

        public static final String OPEN = "open";

        public static final String ORIENTATION = "orientation";

        public static final String PART = "part";

        public static final String PERSISTENT = "persistent";

        public static final String PICKLES = "pickles";

        public static final String PLAYLIST_MODE = "playlist_mode";

        public static final String POTENT_SULFUR_STATE = "potent_sulfur_state";

        public static final String POWER = "power";

        public static final String POWERED = "powered";

        public static final String ROTATION = "rotation";

        public static final String SCULK_SENSOR_PHASE = "sculk_sensor_phase";

        public static final String SEGMENT_AMOUNT = "segment_amount";

        public static final String SHAPE = "shape";

        public static final String SHORT = "short";

        public static final String SHRIEKING = "shrieking";

        public static final String SIDE_CHAIN = "side_chain";

        public static final String SIGNAL_FIRE = "signal_fire";

        public static final String SLOT_0_OCCUPIED = "slot_0_occupied";

        public static final String SLOT_1_OCCUPIED = "slot_1_occupied";

        public static final String SLOT_2_OCCUPIED = "slot_2_occupied";

        public static final String SLOT_3_OCCUPIED = "slot_3_occupied";

        public static final String SLOT_4_OCCUPIED = "slot_4_occupied";

        public static final String SLOT_5_OCCUPIED = "slot_5_occupied";

        public static final String SNOWY = "snowy";

        public static final String SOUTH = "south";

        public static final String STAGE = "stage";

        public static final String THICKNESS = "thickness";

        public static final String TILT = "tilt";

        public static final String TIP = "tip";

        public static final String TRIAL_SPAWNER_STATE = "trial_spawner_state";

        public static final String TRIGGERED = "triggered";

        public static final String TYPE = "type";

        public static final String UNSTABLE = "unstable";

        public static final String UP = "up";

        public static final String VAULT_STATE = "vault_state";

        public static final String VERTICAL_DIRECTION = "vertical_direction";

        public static final String WATERLOGGED = "waterlogged";

        public static final String WEST = "west";

        private Names() {
        }
    }
}
