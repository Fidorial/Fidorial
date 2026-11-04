package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:block_entity_type} registry.
 */
public final class BlockEntityTypeKeys {
    /**
     * Key for {@code minecraft:banner}.
     */
    public static final TypedKey<BlockEntityType> BANNER = create("banner");

    /**
     * Key for {@code minecraft:barrel}.
     */
    public static final TypedKey<BlockEntityType> BARREL = create("barrel");

    /**
     * Key for {@code minecraft:beacon}.
     */
    public static final TypedKey<BlockEntityType> BEACON = create("beacon");

    /**
     * Key for {@code minecraft:beehive}.
     */
    public static final TypedKey<BlockEntityType> BEEHIVE = create("beehive");

    /**
     * Key for {@code minecraft:bell}.
     */
    public static final TypedKey<BlockEntityType> BELL = create("bell");

    /**
     * Key for {@code minecraft:blast_furnace}.
     */
    public static final TypedKey<BlockEntityType> BLAST_FURNACE = create("blast_furnace");

    /**
     * Key for {@code minecraft:brewing_stand}.
     */
    public static final TypedKey<BlockEntityType> BREWING_STAND = create("brewing_stand");

    /**
     * Key for {@code minecraft:brushable_block}.
     */
    public static final TypedKey<BlockEntityType> BRUSHABLE_BLOCK = create("brushable_block");

    /**
     * Key for {@code minecraft:calibrated_sculk_sensor}.
     */
    public static final TypedKey<BlockEntityType> CALIBRATED_SCULK_SENSOR = create("calibrated_sculk_sensor");

    /**
     * Key for {@code minecraft:campfire}.
     */
    public static final TypedKey<BlockEntityType> CAMPFIRE = create("campfire");

    /**
     * Key for {@code minecraft:chest}.
     */
    public static final TypedKey<BlockEntityType> CHEST = create("chest");

    /**
     * Key for {@code minecraft:chiseled_bookshelf}.
     */
    public static final TypedKey<BlockEntityType> CHISELED_BOOKSHELF = create("chiseled_bookshelf");

    /**
     * Key for {@code minecraft:command_block}.
     */
    public static final TypedKey<BlockEntityType> COMMAND_BLOCK = create("command_block");

    /**
     * Key for {@code minecraft:comparator}.
     */
    public static final TypedKey<BlockEntityType> COMPARATOR = create("comparator");

    /**
     * Key for {@code minecraft:conduit}.
     */
    public static final TypedKey<BlockEntityType> CONDUIT = create("conduit");

    /**
     * Key for {@code minecraft:copper_golem_statue}.
     */
    public static final TypedKey<BlockEntityType> COPPER_GOLEM_STATUE = create("copper_golem_statue");

    /**
     * Key for {@code minecraft:crafter}.
     */
    public static final TypedKey<BlockEntityType> CRAFTER = create("crafter");

    /**
     * Key for {@code minecraft:creaking_heart}.
     */
    public static final TypedKey<BlockEntityType> CREAKING_HEART = create("creaking_heart");

    /**
     * Key for {@code minecraft:daylight_detector}.
     */
    public static final TypedKey<BlockEntityType> DAYLIGHT_DETECTOR = create("daylight_detector");

    /**
     * Key for {@code minecraft:decorated_pot}.
     */
    public static final TypedKey<BlockEntityType> DECORATED_POT = create("decorated_pot");

    /**
     * Key for {@code minecraft:dispenser}.
     */
    public static final TypedKey<BlockEntityType> DISPENSER = create("dispenser");

    /**
     * Key for {@code minecraft:dropper}.
     */
    public static final TypedKey<BlockEntityType> DROPPER = create("dropper");

    /**
     * Key for {@code minecraft:enchanting_table}.
     */
    public static final TypedKey<BlockEntityType> ENCHANTING_TABLE = create("enchanting_table");

    /**
     * Key for {@code minecraft:end_gateway}.
     */
    public static final TypedKey<BlockEntityType> END_GATEWAY = create("end_gateway");

    /**
     * Key for {@code minecraft:end_portal}.
     */
    public static final TypedKey<BlockEntityType> END_PORTAL = create("end_portal");

    /**
     * Key for {@code minecraft:ender_chest}.
     */
    public static final TypedKey<BlockEntityType> ENDER_CHEST = create("ender_chest");

    /**
     * Key for {@code minecraft:furnace}.
     */
    public static final TypedKey<BlockEntityType> FURNACE = create("furnace");

    /**
     * Key for {@code minecraft:hanging_sign}.
     */
    public static final TypedKey<BlockEntityType> HANGING_SIGN = create("hanging_sign");

    /**
     * Key for {@code minecraft:hopper}.
     */
    public static final TypedKey<BlockEntityType> HOPPER = create("hopper");

    /**
     * Key for {@code minecraft:jigsaw}.
     */
    public static final TypedKey<BlockEntityType> JIGSAW = create("jigsaw");

    /**
     * Key for {@code minecraft:jukebox}.
     */
    public static final TypedKey<BlockEntityType> JUKEBOX = create("jukebox");

    /**
     * Key for {@code minecraft:lectern}.
     */
    public static final TypedKey<BlockEntityType> LECTERN = create("lectern");

    /**
     * Key for {@code minecraft:mob_spawner}.
     */
    public static final TypedKey<BlockEntityType> MOB_SPAWNER = create("mob_spawner");

    /**
     * Key for {@code minecraft:piston}.
     */
    public static final TypedKey<BlockEntityType> PISTON = create("piston");

    /**
     * Key for {@code minecraft:potent_sulfur}.
     */
    public static final TypedKey<BlockEntityType> POTENT_SULFUR = create("potent_sulfur");

    /**
     * Key for {@code minecraft:sculk_catalyst}.
     */
    public static final TypedKey<BlockEntityType> SCULK_CATALYST = create("sculk_catalyst");

    /**
     * Key for {@code minecraft:sculk_sensor}.
     */
    public static final TypedKey<BlockEntityType> SCULK_SENSOR = create("sculk_sensor");

    /**
     * Key for {@code minecraft:sculk_shrieker}.
     */
    public static final TypedKey<BlockEntityType> SCULK_SHRIEKER = create("sculk_shrieker");

    /**
     * Key for {@code minecraft:shelf}.
     */
    public static final TypedKey<BlockEntityType> SHELF = create("shelf");

    /**
     * Key for {@code minecraft:shulker_box}.
     */
    public static final TypedKey<BlockEntityType> SHULKER_BOX = create("shulker_box");

    /**
     * Key for {@code minecraft:sign}.
     */
    public static final TypedKey<BlockEntityType> SIGN = create("sign");

    /**
     * Key for {@code minecraft:skull}.
     */
    public static final TypedKey<BlockEntityType> SKULL = create("skull");

    /**
     * Key for {@code minecraft:smoker}.
     */
    public static final TypedKey<BlockEntityType> SMOKER = create("smoker");

    /**
     * Key for {@code minecraft:structure_block}.
     */
    public static final TypedKey<BlockEntityType> STRUCTURE_BLOCK = create("structure_block");

    /**
     * Key for {@code minecraft:test_block}.
     */
    public static final TypedKey<BlockEntityType> TEST_BLOCK = create("test_block");

    /**
     * Key for {@code minecraft:test_instance_block}.
     */
    public static final TypedKey<BlockEntityType> TEST_INSTANCE_BLOCK = create("test_instance_block");

    /**
     * Key for {@code minecraft:trapped_chest}.
     */
    public static final TypedKey<BlockEntityType> TRAPPED_CHEST = create("trapped_chest");

    /**
     * Key for {@code minecraft:trial_spawner}.
     */
    public static final TypedKey<BlockEntityType> TRIAL_SPAWNER = create("trial_spawner");

    /**
     * Key for {@code minecraft:vault}.
     */
    public static final TypedKey<BlockEntityType> VAULT = create("vault");

    private static final List<TypedKey<BlockEntityType>> VALUES = List.of(
        BANNER,
        BARREL,
        BEACON,
        BEEHIVE,
        BELL,
        BLAST_FURNACE,
        BREWING_STAND,
        BRUSHABLE_BLOCK,
        CALIBRATED_SCULK_SENSOR,
        CAMPFIRE,
        CHEST,
        CHISELED_BOOKSHELF,
        COMMAND_BLOCK,
        COMPARATOR,
        CONDUIT,
        COPPER_GOLEM_STATUE,
        CRAFTER,
        CREAKING_HEART,
        DAYLIGHT_DETECTOR,
        DECORATED_POT,
        DISPENSER,
        DROPPER,
        ENCHANTING_TABLE,
        END_GATEWAY,
        END_PORTAL,
        ENDER_CHEST,
        FURNACE,
        HANGING_SIGN,
        HOPPER,
        JIGSAW,
        JUKEBOX,
        LECTERN,
        MOB_SPAWNER,
        PISTON,
        POTENT_SULFUR,
        SCULK_CATALYST,
        SCULK_SENSOR,
        SCULK_SHRIEKER,
        SHELF,
        SHULKER_BOX,
        SIGN,
        SKULL,
        SMOKER,
        STRUCTURE_BLOCK,
        TEST_BLOCK,
        TEST_INSTANCE_BLOCK,
        TRAPPED_CHEST,
        TRIAL_SPAWNER,
        VAULT
    );

    private BlockEntityTypeKeys() {
        throw new UnsupportedOperationException("BlockEntityTypeKeys cannot be instantiated.");
    }

    private static TypedKey<BlockEntityType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.BLOCK_ENTITY_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<BlockEntityType>> values() {
        return VALUES.stream();
    }
}
