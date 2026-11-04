package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CustomStat;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:custom_stat} registry.
 */
public final class CustomStatKeys {
    /**
     * Key for {@code minecraft:animals_bred}.
     */
    public static final TypedKey<CustomStat> ANIMALS_BRED = create("animals_bred");

    /**
     * Key for {@code minecraft:aviate_one_cm}.
     */
    public static final TypedKey<CustomStat> AVIATE_ONE_CM = create("aviate_one_cm");

    /**
     * Key for {@code minecraft:bell_ring}.
     */
    public static final TypedKey<CustomStat> BELL_RING = create("bell_ring");

    /**
     * Key for {@code minecraft:boat_one_cm}.
     */
    public static final TypedKey<CustomStat> BOAT_ONE_CM = create("boat_one_cm");

    /**
     * Key for {@code minecraft:clean_armor}.
     */
    public static final TypedKey<CustomStat> CLEAN_ARMOR = create("clean_armor");

    /**
     * Key for {@code minecraft:clean_banner}.
     */
    public static final TypedKey<CustomStat> CLEAN_BANNER = create("clean_banner");

    /**
     * Key for {@code minecraft:clean_shulker_box}.
     */
    public static final TypedKey<CustomStat> CLEAN_SHULKER_BOX = create("clean_shulker_box");

    /**
     * Key for {@code minecraft:climb_one_cm}.
     */
    public static final TypedKey<CustomStat> CLIMB_ONE_CM = create("climb_one_cm");

    /**
     * Key for {@code minecraft:crouch_one_cm}.
     */
    public static final TypedKey<CustomStat> CROUCH_ONE_CM = create("crouch_one_cm");

    /**
     * Key for {@code minecraft:damage_absorbed}.
     */
    public static final TypedKey<CustomStat> DAMAGE_ABSORBED = create("damage_absorbed");

    /**
     * Key for {@code minecraft:damage_blocked_by_shield}.
     */
    public static final TypedKey<CustomStat> DAMAGE_BLOCKED_BY_SHIELD = create("damage_blocked_by_shield");

    /**
     * Key for {@code minecraft:damage_dealt}.
     */
    public static final TypedKey<CustomStat> DAMAGE_DEALT = create("damage_dealt");

    /**
     * Key for {@code minecraft:damage_dealt_absorbed}.
     */
    public static final TypedKey<CustomStat> DAMAGE_DEALT_ABSORBED = create("damage_dealt_absorbed");

    /**
     * Key for {@code minecraft:damage_dealt_resisted}.
     */
    public static final TypedKey<CustomStat> DAMAGE_DEALT_RESISTED = create("damage_dealt_resisted");

    /**
     * Key for {@code minecraft:damage_resisted}.
     */
    public static final TypedKey<CustomStat> DAMAGE_RESISTED = create("damage_resisted");

    /**
     * Key for {@code minecraft:damage_taken}.
     */
    public static final TypedKey<CustomStat> DAMAGE_TAKEN = create("damage_taken");

    /**
     * Key for {@code minecraft:deaths}.
     */
    public static final TypedKey<CustomStat> DEATHS = create("deaths");

    /**
     * Key for {@code minecraft:drop}.
     */
    public static final TypedKey<CustomStat> DROP = create("drop");

    /**
     * Key for {@code minecraft:eat_cake_slice}.
     */
    public static final TypedKey<CustomStat> EAT_CAKE_SLICE = create("eat_cake_slice");

    /**
     * Key for {@code minecraft:enchant_item}.
     */
    public static final TypedKey<CustomStat> ENCHANT_ITEM = create("enchant_item");

    /**
     * Key for {@code minecraft:fall_one_cm}.
     */
    public static final TypedKey<CustomStat> FALL_ONE_CM = create("fall_one_cm");

    /**
     * Key for {@code minecraft:fill_cauldron}.
     */
    public static final TypedKey<CustomStat> FILL_CAULDRON = create("fill_cauldron");

    /**
     * Key for {@code minecraft:fish_caught}.
     */
    public static final TypedKey<CustomStat> FISH_CAUGHT = create("fish_caught");

    /**
     * Key for {@code minecraft:fly_one_cm}.
     */
    public static final TypedKey<CustomStat> FLY_ONE_CM = create("fly_one_cm");

    /**
     * Key for {@code minecraft:happy_ghast_one_cm}.
     */
    public static final TypedKey<CustomStat> HAPPY_GHAST_ONE_CM = create("happy_ghast_one_cm");

    /**
     * Key for {@code minecraft:horse_one_cm}.
     */
    public static final TypedKey<CustomStat> HORSE_ONE_CM = create("horse_one_cm");

    /**
     * Key for {@code minecraft:inspect_dispenser}.
     */
    public static final TypedKey<CustomStat> INSPECT_DISPENSER = create("inspect_dispenser");

    /**
     * Key for {@code minecraft:inspect_dropper}.
     */
    public static final TypedKey<CustomStat> INSPECT_DROPPER = create("inspect_dropper");

    /**
     * Key for {@code minecraft:inspect_hopper}.
     */
    public static final TypedKey<CustomStat> INSPECT_HOPPER = create("inspect_hopper");

    /**
     * Key for {@code minecraft:interact_with_anvil}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_ANVIL = create("interact_with_anvil");

    /**
     * Key for {@code minecraft:interact_with_beacon}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_BEACON = create("interact_with_beacon");

    /**
     * Key for {@code minecraft:interact_with_blast_furnace}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_BLAST_FURNACE = create("interact_with_blast_furnace");

    /**
     * Key for {@code minecraft:interact_with_brewingstand}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_BREWINGSTAND = create("interact_with_brewingstand");

    /**
     * Key for {@code minecraft:interact_with_campfire}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_CAMPFIRE = create("interact_with_campfire");

    /**
     * Key for {@code minecraft:interact_with_cartography_table}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_CARTOGRAPHY_TABLE = create("interact_with_cartography_table");

    /**
     * Key for {@code minecraft:interact_with_crafting_table}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_CRAFTING_TABLE = create("interact_with_crafting_table");

    /**
     * Key for {@code minecraft:interact_with_furnace}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_FURNACE = create("interact_with_furnace");

    /**
     * Key for {@code minecraft:interact_with_grindstone}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_GRINDSTONE = create("interact_with_grindstone");

    /**
     * Key for {@code minecraft:interact_with_lectern}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_LECTERN = create("interact_with_lectern");

    /**
     * Key for {@code minecraft:interact_with_loom}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_LOOM = create("interact_with_loom");

    /**
     * Key for {@code minecraft:interact_with_smithing_table}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_SMITHING_TABLE = create("interact_with_smithing_table");

    /**
     * Key for {@code minecraft:interact_with_smoker}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_SMOKER = create("interact_with_smoker");

    /**
     * Key for {@code minecraft:interact_with_stonecutter}.
     */
    public static final TypedKey<CustomStat> INTERACT_WITH_STONECUTTER = create("interact_with_stonecutter");

    /**
     * Key for {@code minecraft:jump}.
     */
    public static final TypedKey<CustomStat> JUMP = create("jump");

    /**
     * Key for {@code minecraft:leave_game}.
     */
    public static final TypedKey<CustomStat> LEAVE_GAME = create("leave_game");

    /**
     * Key for {@code minecraft:minecart_one_cm}.
     */
    public static final TypedKey<CustomStat> MINECART_ONE_CM = create("minecart_one_cm");

    /**
     * Key for {@code minecraft:mob_kills}.
     */
    public static final TypedKey<CustomStat> MOB_KILLS = create("mob_kills");

    /**
     * Key for {@code minecraft:nautilus_one_cm}.
     */
    public static final TypedKey<CustomStat> NAUTILUS_ONE_CM = create("nautilus_one_cm");

    /**
     * Key for {@code minecraft:open_barrel}.
     */
    public static final TypedKey<CustomStat> OPEN_BARREL = create("open_barrel");

    /**
     * Key for {@code minecraft:open_chest}.
     */
    public static final TypedKey<CustomStat> OPEN_CHEST = create("open_chest");

    /**
     * Key for {@code minecraft:open_enderchest}.
     */
    public static final TypedKey<CustomStat> OPEN_ENDERCHEST = create("open_enderchest");

    /**
     * Key for {@code minecraft:open_shulker_box}.
     */
    public static final TypedKey<CustomStat> OPEN_SHULKER_BOX = create("open_shulker_box");

    /**
     * Key for {@code minecraft:pig_one_cm}.
     */
    public static final TypedKey<CustomStat> PIG_ONE_CM = create("pig_one_cm");

    /**
     * Key for {@code minecraft:play_noteblock}.
     */
    public static final TypedKey<CustomStat> PLAY_NOTEBLOCK = create("play_noteblock");

    /**
     * Key for {@code minecraft:play_record}.
     */
    public static final TypedKey<CustomStat> PLAY_RECORD = create("play_record");

    /**
     * Key for {@code minecraft:play_time}.
     */
    public static final TypedKey<CustomStat> PLAY_TIME = create("play_time");

    /**
     * Key for {@code minecraft:player_kills}.
     */
    public static final TypedKey<CustomStat> PLAYER_KILLS = create("player_kills");

    /**
     * Key for {@code minecraft:pot_flower}.
     */
    public static final TypedKey<CustomStat> POT_FLOWER = create("pot_flower");

    /**
     * Key for {@code minecraft:raid_trigger}.
     */
    public static final TypedKey<CustomStat> RAID_TRIGGER = create("raid_trigger");

    /**
     * Key for {@code minecraft:raid_win}.
     */
    public static final TypedKey<CustomStat> RAID_WIN = create("raid_win");

    /**
     * Key for {@code minecraft:sleep_in_bed}.
     */
    public static final TypedKey<CustomStat> SLEEP_IN_BED = create("sleep_in_bed");

    /**
     * Key for {@code minecraft:sleep_in_straw_bed}.
     */
    public static final TypedKey<CustomStat> SLEEP_IN_STRAW_BED = create("sleep_in_straw_bed");

    /**
     * Key for {@code minecraft:sneak_time}.
     */
    public static final TypedKey<CustomStat> SNEAK_TIME = create("sneak_time");

    /**
     * Key for {@code minecraft:sprint_one_cm}.
     */
    public static final TypedKey<CustomStat> SPRINT_ONE_CM = create("sprint_one_cm");

    /**
     * Key for {@code minecraft:strider_one_cm}.
     */
    public static final TypedKey<CustomStat> STRIDER_ONE_CM = create("strider_one_cm");

    /**
     * Key for {@code minecraft:swim_one_cm}.
     */
    public static final TypedKey<CustomStat> SWIM_ONE_CM = create("swim_one_cm");

    /**
     * Key for {@code minecraft:talked_to_villager}.
     */
    public static final TypedKey<CustomStat> TALKED_TO_VILLAGER = create("talked_to_villager");

    /**
     * Key for {@code minecraft:target_hit}.
     */
    public static final TypedKey<CustomStat> TARGET_HIT = create("target_hit");

    /**
     * Key for {@code minecraft:time_since_death}.
     */
    public static final TypedKey<CustomStat> TIME_SINCE_DEATH = create("time_since_death");

    /**
     * Key for {@code minecraft:time_since_rest}.
     */
    public static final TypedKey<CustomStat> TIME_SINCE_REST = create("time_since_rest");

    /**
     * Key for {@code minecraft:total_world_time}.
     */
    public static final TypedKey<CustomStat> TOTAL_WORLD_TIME = create("total_world_time");

    /**
     * Key for {@code minecraft:traded_with_villager}.
     */
    public static final TypedKey<CustomStat> TRADED_WITH_VILLAGER = create("traded_with_villager");

    /**
     * Key for {@code minecraft:trigger_trapped_chest}.
     */
    public static final TypedKey<CustomStat> TRIGGER_TRAPPED_CHEST = create("trigger_trapped_chest");

    /**
     * Key for {@code minecraft:tune_noteblock}.
     */
    public static final TypedKey<CustomStat> TUNE_NOTEBLOCK = create("tune_noteblock");

    /**
     * Key for {@code minecraft:use_cauldron}.
     */
    public static final TypedKey<CustomStat> USE_CAULDRON = create("use_cauldron");

    /**
     * Key for {@code minecraft:walk_on_water_one_cm}.
     */
    public static final TypedKey<CustomStat> WALK_ON_WATER_ONE_CM = create("walk_on_water_one_cm");

    /**
     * Key for {@code minecraft:walk_one_cm}.
     */
    public static final TypedKey<CustomStat> WALK_ONE_CM = create("walk_one_cm");

    /**
     * Key for {@code minecraft:walk_under_water_one_cm}.
     */
    public static final TypedKey<CustomStat> WALK_UNDER_WATER_ONE_CM = create("walk_under_water_one_cm");

    private static final List<TypedKey<CustomStat>> VALUES = List.of(
        ANIMALS_BRED,
        AVIATE_ONE_CM,
        BELL_RING,
        BOAT_ONE_CM,
        CLEAN_ARMOR,
        CLEAN_BANNER,
        CLEAN_SHULKER_BOX,
        CLIMB_ONE_CM,
        CROUCH_ONE_CM,
        DAMAGE_ABSORBED,
        DAMAGE_BLOCKED_BY_SHIELD,
        DAMAGE_DEALT,
        DAMAGE_DEALT_ABSORBED,
        DAMAGE_DEALT_RESISTED,
        DAMAGE_RESISTED,
        DAMAGE_TAKEN,
        DEATHS,
        DROP,
        EAT_CAKE_SLICE,
        ENCHANT_ITEM,
        FALL_ONE_CM,
        FILL_CAULDRON,
        FISH_CAUGHT,
        FLY_ONE_CM,
        HAPPY_GHAST_ONE_CM,
        HORSE_ONE_CM,
        INSPECT_DISPENSER,
        INSPECT_DROPPER,
        INSPECT_HOPPER,
        INTERACT_WITH_ANVIL,
        INTERACT_WITH_BEACON,
        INTERACT_WITH_BLAST_FURNACE,
        INTERACT_WITH_BREWINGSTAND,
        INTERACT_WITH_CAMPFIRE,
        INTERACT_WITH_CARTOGRAPHY_TABLE,
        INTERACT_WITH_CRAFTING_TABLE,
        INTERACT_WITH_FURNACE,
        INTERACT_WITH_GRINDSTONE,
        INTERACT_WITH_LECTERN,
        INTERACT_WITH_LOOM,
        INTERACT_WITH_SMITHING_TABLE,
        INTERACT_WITH_SMOKER,
        INTERACT_WITH_STONECUTTER,
        JUMP,
        LEAVE_GAME,
        MINECART_ONE_CM,
        MOB_KILLS,
        NAUTILUS_ONE_CM,
        OPEN_BARREL,
        OPEN_CHEST,
        OPEN_ENDERCHEST,
        OPEN_SHULKER_BOX,
        PIG_ONE_CM,
        PLAY_NOTEBLOCK,
        PLAY_RECORD,
        PLAY_TIME,
        PLAYER_KILLS,
        POT_FLOWER,
        RAID_TRIGGER,
        RAID_WIN,
        SLEEP_IN_BED,
        SLEEP_IN_STRAW_BED,
        SNEAK_TIME,
        SPRINT_ONE_CM,
        STRIDER_ONE_CM,
        SWIM_ONE_CM,
        TALKED_TO_VILLAGER,
        TARGET_HIT,
        TIME_SINCE_DEATH,
        TIME_SINCE_REST,
        TOTAL_WORLD_TIME,
        TRADED_WITH_VILLAGER,
        TRIGGER_TRAPPED_CHEST,
        TUNE_NOTEBLOCK,
        USE_CAULDRON,
        WALK_ON_WATER_ONE_CM,
        WALK_ONE_CM,
        WALK_UNDER_WATER_ONE_CM
    );

    private CustomStatKeys() {
        throw new UnsupportedOperationException("CustomStatKeys cannot be instantiated.");
    }

    private static TypedKey<CustomStat> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.CUSTOM_STAT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<CustomStat>> values() {
        return VALUES.stream();
    }
}
