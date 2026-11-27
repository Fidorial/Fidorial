package fr.fidorial.registrygen.model;

import com.palantir.javapoet.ClassName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The distinct block properties of Mojang's block report, typed and named once for the API and the server.
 *
 * @since 0.1.0
 */
public final class BlockPropertyCatalog {

    private static final String WORLD = "fr.fidorial.world";
    private static final String BLOCK_DATA = "fr.fidorial.world.block.data";
    private static final String BLOCK_DATA_TYPE = "fr.fidorial.world.block.data.type";
    private static final String BLOCK_PROPERTY = "fr.fidorial.world.block.property";

    private static final ClassName BLOCK_FACE = ClassName.get(WORLD, "BlockFace");
    private static final ClassName AXIS = ClassName.get(BLOCK_DATA, "Orientable", "Axis");
    private static final ClassName HALF = ClassName.get(BLOCK_DATA, "Bisected", "Half");
    private static final ClassName WALL_HEIGHT = ClassName.get(BLOCK_PROPERTY, "WallHeight");
    private static final ClassName CONNECTION = ClassName.get(BLOCK_DATA_TYPE, "RedstoneWire", "Connection");
    private static final ClassName RAIL_TRACK = ClassName.get(BLOCK_PROPERTY, "RailTrack");

    // an unknown set of values fails the generation, so that each gets an enum on purpose
    private static final List<ChoiceBinding> CHOICES = List.of(
            choice("FACING", "facing", BLOCK_FACE, "north", "east", "south", "west", "up", "down"),
            choice("HORIZONTAL_FACING", "facing", BLOCK_FACE, "north", "south", "west", "east"),
            choice("HOPPER_FACING", "facing", BLOCK_FACE, "down", "north", "south", "west", "east"),
            choice("VERTICAL_DIRECTION", "vertical_direction", BLOCK_FACE, "up", "down"),
            choice("AXIS", "axis", AXIS, "x", "y", "z"),
            choice("HORIZONTAL_AXIS", "axis", AXIS, "x", "z"),
            choice("HALF", "half", HALF, "top", "bottom"),
            choice("DOUBLE_BLOCK_HALF", "half", HALF, "upper", "lower"),
            choice("FACE", "face", ClassName.get(BLOCK_DATA, "FaceAttachable", "AttachedFace"), "floor", "wall", "ceiling"),
            choice("HINGE", "hinge", ClassName.get(BLOCK_DATA_TYPE, "Door", "Hinge"), "left", "right"),
            choice("SLAB_TYPE", "type", ClassName.get(BLOCK_DATA_TYPE, "Slab", "Type"), "top", "bottom", "double"),
            choice("CHEST_TYPE", "type", ClassName.get(BLOCK_DATA_TYPE, "Chest", "Type"), "single", "left", "right"),
            choice("PISTON_TYPE", "type", ClassName.get(BLOCK_PROPERTY, "PistonKind"), "normal", "sticky"),
            choice("STAIRS_SHAPE", "shape", ClassName.get(BLOCK_DATA_TYPE, "Stairs", "Shape"),
                    "straight", "inner_left", "inner_right", "outer_left", "outer_right"),
            choice("STRAIGHT_RAIL_SHAPE", "shape", RAIL_TRACK,
                    "north_south", "east_west", "ascending_east", "ascending_west", "ascending_north", "ascending_south"),
            choice("RAIL_SHAPE", "shape", RAIL_TRACK,
                    "north_south", "east_west", "ascending_east", "ascending_west", "ascending_north", "ascending_south",
                    "south_east", "south_west", "north_west", "north_east"),
            choice("COMPARATOR_MODE", "mode", ClassName.get(BLOCK_DATA_TYPE, "Comparator", "Mode"), "compare", "subtract"),
            choice("STRUCTURE_BLOCK_MODE", "mode", ClassName.get(BLOCK_PROPERTY, "StructureBlockMode"), "save", "load", "corner", "data"),
            choice("TEST_BLOCK_MODE", "mode", ClassName.get(BLOCK_PROPERTY, "TestMode"), "start", "log", "fail", "accept"),
            choice("THICKNESS", "thickness", ClassName.get(BLOCK_DATA_TYPE, "Speleothem", "Thickness"),
                    "tip_merge", "tip", "frustum", "middle", "base"),
            choice("WALL_EAST", "east", WALL_HEIGHT, "none", "low", "tall"),
            choice("WALL_NORTH", "north", WALL_HEIGHT, "none", "low", "tall"),
            choice("WALL_SOUTH", "south", WALL_HEIGHT, "none", "low", "tall"),
            choice("WALL_WEST", "west", WALL_HEIGHT, "none", "low", "tall"),
            choice("REDSTONE_EAST", "east", CONNECTION, "up", "side", "none"),
            choice("REDSTONE_NORTH", "north", CONNECTION, "up", "side", "none"),
            choice("REDSTONE_SOUTH", "south", CONNECTION, "up", "side", "none"),
            choice("REDSTONE_WEST", "west", CONNECTION, "up", "side", "none"),
            choice("SIDE_CHAIN", "side_chain", ClassName.get(BLOCK_PROPERTY, "ShelfConnection"), "unconnected", "right", "center", "left"),
            choice("LEAVES", "leaves", ClassName.get(BLOCK_PROPERTY, "BambooFoliage"), "none", "small", "large"),
            choice("ATTACHMENT", "attachment", ClassName.get(BLOCK_PROPERTY, "BellMount"), "floor", "ceiling", "single_wall", "double_wall"),
            choice("TILT", "tilt", ClassName.get(BLOCK_PROPERTY, "DripleafTilt"), "none", "unstable", "partial", "full"),
            choice("PART", "part", ClassName.get(BLOCK_PROPERTY, "BedEnd"), "head", "foot"),
            choice("SCULK_SENSOR_PHASE", "sculk_sensor_phase", ClassName.get(BLOCK_PROPERTY, "SculkPhase"), "inactive", "active", "cooldown"),
            choice("COPPER_GOLEM_POSE", "copper_golem_pose", ClassName.get(BLOCK_PROPERTY, "StatuePose"), "standing", "sitting", "running", "star"),
            choice("ORIENTATION", "orientation", ClassName.get(BLOCK_PROPERTY, "Orientation"),
                    "down_east", "down_north", "down_south", "down_west", "up_east", "up_north", "up_south", "up_west",
                    "west_up", "east_up", "north_up", "south_up"),
            choice("CREAKING_HEART_STATE", "creaking_heart_state", ClassName.get(BLOCK_PROPERTY, "CreakingHeartPhase"), "uprooted", "dormant", "awake"),
            choice("INSTRUMENT", "instrument", ClassName.get(BLOCK_PROPERTY, "NoteInstrument"),
                    "harp", "basedrum", "snare", "hat", "bass", "flute", "bell", "guitar", "chime", "xylophone", "iron_xylophone",
                    "cow_bell", "didgeridoo", "bit", "banjo", "pling", "trumpet", "trumpet_exposed", "trumpet_oxidized",
                    "trumpet_weathered", "zombie", "skeleton", "creeper", "dragon", "wither_skeleton", "piglin", "custom_head"),
            choice("POTENT_SULFUR_STATE", "potent_sulfur_state", ClassName.get(BLOCK_PROPERTY, "PotentSulfurPhase"),
                    "dry", "wet", "dormant", "erupting", "continuous"),
            choice("TRIAL_SPAWNER_STATE", "trial_spawner_state", ClassName.get(BLOCK_PROPERTY, "TrialSpawnerPhase"),
                    "inactive", "waiting_for_players", "active", "waiting_for_reward_ejection", "ejecting_reward", "cooldown"),
            choice("VAULT_STATE", "vault_state", ClassName.get(BLOCK_PROPERTY, "VaultPhase"), "inactive", "active", "unlocking", "ejecting")
    );

    private final Map<BlockPropertyDefinition, Entry> entries;

    private BlockPropertyCatalog(final Map<BlockPropertyDefinition, Entry> entries) {
        this.entries = entries;
    }

    public record Entry(String constant, BlockPropertyDefinition definition, Kind kind, ClassName enumType) {
    }

    public enum Kind {
        FLAG,
        RANGE,
        CHOICE
    }

    private record ChoiceBinding(String constant, BlockPropertyDefinition definition, ClassName enumType) {
    }

    private static ChoiceBinding choice(final String constant, final String name, final ClassName enumType, final String... values) {
        return new ChoiceBinding(constant, new BlockPropertyDefinition(name, List.of(values)), enumType);
    }

    public static BlockPropertyCatalog of(final List<BlockReportDefinition> blocks) {
        final Map<BlockPropertyDefinition, ChoiceBinding> choices = new HashMap<>();
        for (final ChoiceBinding binding : CHOICES) {
            choices.put(binding.definition(), binding);
        }
        final Map<String, Integer> rangesByName = new HashMap<>();
        final Map<BlockPropertyDefinition, Kind> kinds = new LinkedHashMap<>();
        for (final BlockReportDefinition block : blocks) {
            for (final BlockPropertyDefinition property : block.properties()) {
                if (!kinds.containsKey(property)) {
                    final Kind kind = kindOf(property);
                    kinds.put(property, kind);
                    if (kind == Kind.RANGE) {
                        rangesByName.merge(property.name(), 1, Integer::sum);
                    }
                }
            }
        }
        final Map<BlockPropertyDefinition, Entry> entries = new LinkedHashMap<>();
        final Map<String, BlockPropertyDefinition> constants = new HashMap<>();
        for (final Map.Entry<BlockPropertyDefinition, Kind> typed : kinds.entrySet()) {
            final BlockPropertyDefinition property = typed.getKey();
            final Entry entry = switch (typed.getValue()) {
                case FLAG -> new Entry(upper(property.name()), property, Kind.FLAG, null);
                case RANGE -> new Entry(rangesByName.get(property.name()) > 1
                        ? upper(property.name()) + "_" + property.values().getFirst() + "_" + property.values().getLast()
                        : upper(property.name()), property, Kind.RANGE, null);
                case CHOICE -> {
                    final ChoiceBinding binding = choices.get(property);
                    if (binding == null) {
                        throw new IllegalStateException("The block property '" + property.name() + "' with the values "
                                + property.values() + " has no enum: add it to BlockPropertyCatalog.CHOICES");
                    }
                    yield new Entry(binding.constant(), property, Kind.CHOICE, binding.enumType());
                }
            };
            final BlockPropertyDefinition clash = constants.putIfAbsent(entry.constant(), property);
            if (clash != null) {
                throw new IllegalStateException("The block properties " + clash + " and " + property
                        + " would both be named " + entry.constant());
            }
            entries.put(property, entry);
        }
        return new BlockPropertyCatalog(entries);
    }

    private static Kind kindOf(final BlockPropertyDefinition property) {
        final List<String> values = property.values();
        if (values.equals(List.of("true", "false"))) {
            return Kind.FLAG;
        }
        if (values.stream().allMatch(value -> !value.isEmpty() && value.chars().allMatch(c -> c >= '0' && c <= '9'))) {
            final int first = Integer.parseInt(values.getFirst());
            for (int index = 0; index < values.size(); index++) {
                if (Integer.parseInt(values.get(index)) != first + index) {
                    throw new IllegalStateException("The block property '" + property.name()
                            + "' has integer values that are not consecutive: " + values);
                }
            }
            return Kind.RANGE;
        }
        return Kind.CHOICE;
    }

    private static String upper(final String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    public Entry entry(final BlockPropertyDefinition property) {
        final Entry entry = entries.get(property);
        if (entry == null) {
            throw new IllegalStateException("The block property " + property + " is not in the catalog");
        }
        return entry;
    }

    public List<Entry> entries() {
        final List<Entry> sorted = new ArrayList<>(entries.values());
        sorted.sort(Comparator.comparing(Entry::constant));
        return sorted;
    }
}
