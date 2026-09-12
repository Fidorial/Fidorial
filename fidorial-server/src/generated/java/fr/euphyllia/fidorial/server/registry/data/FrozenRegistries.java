package fr.euphyllia.fidorial.server.registry.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Entries and tags of the registries whose network IDs the client hard-codes,
 * in {@code protocol_id} order.
 *
 * <p>Generated from Mojang's registry report and tag files; do not edit.</p>
 *
 * <p>Every list is indexed by network ID. An ID Mojang leaves unused is
 * padded with a {@code fidorial:unused_<id>} placeholder so the indices stay aligned.</p>
 */
public final class FrozenRegistries {
    private static final Map<Key, List<Key>> ENTRIES = Map.ofEntries(
        Map.entry(Key.key("attribute"), attribute()),
        Map.entry(Key.key("block"), block()),
        Map.entry(Key.key("data_component_type"), dataComponentType()),
        Map.entry(Key.key("fluid"), fluid()),
        Map.entry(Key.key("item"), item()),
        Map.entry(Key.key("menu"), menu())
    );

    private static final Map<Key, Map<Key, List<Key>>> TAGS = Map.ofEntries(
        Map.entry(Key.key("attribute"), attributeTags()),
        Map.entry(Key.key("block"), blockTags()),
        Map.entry(Key.key("data_component_type"), dataComponentTypeTags()),
        Map.entry(Key.key("fluid"), fluidTags()),
        Map.entry(Key.key("item"), itemTags()),
        Map.entry(Key.key("menu"), menuTags())
    );

    private FrozenRegistries() {
        throw new UnsupportedOperationException("FrozenRegistries cannot be instantiated.");
    }

    /**
     * @return every frozen registry, keyed by registry identifier;
     *         each list is indexed by network ID
     */
    public static Map<Key, List<Key>> entries() {
        return ENTRIES;
    }

    /**
     * @return the tags of every frozen registry, keyed by registry identifier
     *         then by tag identifier; a registry without tags maps to an
     *         empty map rather than being absent
     */
    public static Map<Key, Map<Key, List<Key>>> tags() {
        return TAGS;
    }

    private static void attribute0(final List<Key> entries) {
        entries.add(Key.key("air_drag_modifier"));
        entries.add(Key.key("armor"));
        entries.add(Key.key("armor_toughness"));
        entries.add(Key.key("attack_damage"));
        entries.add(Key.key("attack_knockback"));
        entries.add(Key.key("attack_speed"));
        entries.add(Key.key("below_name_distance"));
        entries.add(Key.key("block_break_speed"));
        entries.add(Key.key("block_interaction_range"));
        entries.add(Key.key("bounciness"));
        entries.add(Key.key("burning_time"));
        entries.add(Key.key("camera_distance"));
        entries.add(Key.key("explosion_knockback_resistance"));
        entries.add(Key.key("entity_interaction_range"));
        entries.add(Key.key("fall_damage_multiplier"));
        entries.add(Key.key("flying_speed"));
        entries.add(Key.key("follow_range"));
        entries.add(Key.key("friction_modifier"));
        entries.add(Key.key("gravity"));
        entries.add(Key.key("jump_strength"));
        entries.add(Key.key("knockback_resistance"));
        entries.add(Key.key("luck"));
        entries.add(Key.key("max_absorption"));
        entries.add(Key.key("max_health"));
        entries.add(Key.key("mining_efficiency"));
        entries.add(Key.key("movement_efficiency"));
        entries.add(Key.key("movement_speed"));
        entries.add(Key.key("name_tag_distance"));
        entries.add(Key.key("oxygen_bonus"));
        entries.add(Key.key("safe_fall_distance"));
        entries.add(Key.key("scale"));
        entries.add(Key.key("sneaking_speed"));
        entries.add(Key.key("spawn_reinforcements"));
        entries.add(Key.key("step_height"));
        entries.add(Key.key("submerged_mining_speed"));
        entries.add(Key.key("sweeping_damage_ratio"));
        entries.add(Key.key("tempt_range"));
        entries.add(Key.key("water_movement_efficiency"));
        entries.add(Key.key("waypoint_transmit_range"));
        entries.add(Key.key("waypoint_receive_range"));
    }

    /**
     * @return {@code minecraft:attribute}, indexed by network ID
     */
    private static List<Key> attribute() {
        final List<Key> entries = new ArrayList<>(40);
        attribute0(entries);
        return List.copyOf(entries);
    }

    /**
     * @return the tags of {@code minecraft:attribute}, keyed by tag identifier
     */
    private static Map<Key, List<Key>> attributeTags() {
        return Map.of();
    }

    private static void block0(final List<Key> entries) {
        entries.add(Key.key("air"));
        entries.add(Key.key("stone"));
        entries.add(Key.key("granite"));
        entries.add(Key.key("polished_granite"));
        entries.add(Key.key("diorite"));
        entries.add(Key.key("polished_diorite"));
        entries.add(Key.key("andesite"));
        entries.add(Key.key("polished_andesite"));
        entries.add(Key.key("grass_block"));
        entries.add(Key.key("dirt"));
        entries.add(Key.key("coarse_dirt"));
        entries.add(Key.key("podzol"));
        entries.add(Key.key("cobblestone"));
        entries.add(Key.key("oak_planks"));
        entries.add(Key.key("spruce_planks"));
        entries.add(Key.key("birch_planks"));
        entries.add(Key.key("jungle_planks"));
        entries.add(Key.key("acacia_planks"));
        entries.add(Key.key("cherry_planks"));
        entries.add(Key.key("dark_oak_planks"));
        entries.add(Key.key("pale_oak_wood"));
        entries.add(Key.key("pale_oak_planks"));
        entries.add(Key.key("mangrove_planks"));
        entries.add(Key.key("poplar_planks"));
        entries.add(Key.key("bamboo_planks"));
        entries.add(Key.key("bamboo_mosaic"));
        entries.add(Key.key("oak_sapling"));
        entries.add(Key.key("spruce_sapling"));
        entries.add(Key.key("birch_sapling"));
        entries.add(Key.key("jungle_sapling"));
        entries.add(Key.key("acacia_sapling"));
        entries.add(Key.key("cherry_sapling"));
        entries.add(Key.key("dark_oak_sapling"));
        entries.add(Key.key("pale_oak_sapling"));
        entries.add(Key.key("mangrove_propagule"));
        entries.add(Key.key("poplar_sapling"));
        entries.add(Key.key("bedrock"));
        entries.add(Key.key("water"));
        entries.add(Key.key("lava"));
        entries.add(Key.key("sand"));
        entries.add(Key.key("suspicious_sand"));
        entries.add(Key.key("red_sand"));
        entries.add(Key.key("gravel"));
        entries.add(Key.key("suspicious_gravel"));
        entries.add(Key.key("gold_ore"));
        entries.add(Key.key("deepslate_gold_ore"));
        entries.add(Key.key("iron_ore"));
        entries.add(Key.key("deepslate_iron_ore"));
        entries.add(Key.key("coal_ore"));
        entries.add(Key.key("deepslate_coal_ore"));
        entries.add(Key.key("nether_gold_ore"));
        entries.add(Key.key("oak_log"));
        entries.add(Key.key("spruce_log"));
        entries.add(Key.key("birch_log"));
        entries.add(Key.key("jungle_log"));
        entries.add(Key.key("acacia_log"));
        entries.add(Key.key("cherry_log"));
        entries.add(Key.key("dark_oak_log"));
        entries.add(Key.key("pale_oak_log"));
        entries.add(Key.key("mangrove_log"));
        entries.add(Key.key("poplar_log"));
        entries.add(Key.key("mangrove_roots"));
        entries.add(Key.key("muddy_mangrove_roots"));
        entries.add(Key.key("bamboo_block"));
        entries.add(Key.key("stripped_spruce_log"));
        entries.add(Key.key("stripped_birch_log"));
        entries.add(Key.key("stripped_jungle_log"));
        entries.add(Key.key("stripped_acacia_log"));
        entries.add(Key.key("stripped_cherry_log"));
        entries.add(Key.key("stripped_dark_oak_log"));
        entries.add(Key.key("stripped_pale_oak_log"));
        entries.add(Key.key("stripped_oak_log"));
        entries.add(Key.key("stripped_mangrove_log"));
        entries.add(Key.key("stripped_poplar_log"));
        entries.add(Key.key("stripped_bamboo_block"));
        entries.add(Key.key("oak_wood"));
        entries.add(Key.key("spruce_wood"));
        entries.add(Key.key("birch_wood"));
        entries.add(Key.key("jungle_wood"));
        entries.add(Key.key("acacia_wood"));
        entries.add(Key.key("cherry_wood"));
        entries.add(Key.key("dark_oak_wood"));
        entries.add(Key.key("mangrove_wood"));
        entries.add(Key.key("poplar_wood"));
        entries.add(Key.key("stripped_oak_wood"));
        entries.add(Key.key("stripped_spruce_wood"));
        entries.add(Key.key("stripped_birch_wood"));
        entries.add(Key.key("stripped_jungle_wood"));
        entries.add(Key.key("stripped_acacia_wood"));
        entries.add(Key.key("stripped_cherry_wood"));
        entries.add(Key.key("stripped_dark_oak_wood"));
        entries.add(Key.key("stripped_pale_oak_wood"));
        entries.add(Key.key("stripped_mangrove_wood"));
        entries.add(Key.key("stripped_poplar_wood"));
        entries.add(Key.key("oak_leaves"));
        entries.add(Key.key("spruce_leaves"));
        entries.add(Key.key("birch_leaves"));
        entries.add(Key.key("jungle_leaves"));
        entries.add(Key.key("acacia_leaves"));
        entries.add(Key.key("cherry_leaves"));
        entries.add(Key.key("dark_oak_leaves"));
        entries.add(Key.key("pale_oak_leaves"));
        entries.add(Key.key("mangrove_leaves"));
        entries.add(Key.key("red_poplar_leaves"));
        entries.add(Key.key("orange_poplar_leaves"));
        entries.add(Key.key("yellow_poplar_leaves"));
        entries.add(Key.key("azalea_leaves"));
        entries.add(Key.key("flowering_azalea_leaves"));
        entries.add(Key.key("sponge"));
        entries.add(Key.key("wet_sponge"));
        entries.add(Key.key("glass"));
        entries.add(Key.key("lapis_ore"));
        entries.add(Key.key("deepslate_lapis_ore"));
        entries.add(Key.key("lapis_block"));
        entries.add(Key.key("dispenser"));
        entries.add(Key.key("sandstone"));
        entries.add(Key.key("chiseled_sandstone"));
        entries.add(Key.key("cut_sandstone"));
        entries.add(Key.key("note_block"));
        entries.add(Key.key("white_bed"));
        entries.add(Key.key("orange_bed"));
        entries.add(Key.key("magenta_bed"));
        entries.add(Key.key("light_blue_bed"));
        entries.add(Key.key("yellow_bed"));
        entries.add(Key.key("lime_bed"));
        entries.add(Key.key("pink_bed"));
        entries.add(Key.key("gray_bed"));
        entries.add(Key.key("light_gray_bed"));
        entries.add(Key.key("cyan_bed"));
        entries.add(Key.key("purple_bed"));
        entries.add(Key.key("blue_bed"));
        entries.add(Key.key("brown_bed"));
        entries.add(Key.key("green_bed"));
        entries.add(Key.key("red_bed"));
        entries.add(Key.key("black_bed"));
        entries.add(Key.key("straw_bed"));
        entries.add(Key.key("powered_rail"));
        entries.add(Key.key("detector_rail"));
        entries.add(Key.key("sticky_piston"));
        entries.add(Key.key("cobweb"));
        entries.add(Key.key("short_grass"));
        entries.add(Key.key("fern"));
        entries.add(Key.key("dead_bush"));
        entries.add(Key.key("bush"));
        entries.add(Key.key("red_shrub"));
        entries.add(Key.key("short_dry_grass"));
        entries.add(Key.key("tall_dry_grass"));
        entries.add(Key.key("seagrass"));
        entries.add(Key.key("tall_seagrass"));
        entries.add(Key.key("piston"));
        entries.add(Key.key("piston_head"));
        entries.add(Key.key("white_wool"));
        entries.add(Key.key("orange_wool"));
        entries.add(Key.key("magenta_wool"));
        entries.add(Key.key("light_blue_wool"));
        entries.add(Key.key("yellow_wool"));
        entries.add(Key.key("lime_wool"));
        entries.add(Key.key("pink_wool"));
        entries.add(Key.key("gray_wool"));
        entries.add(Key.key("light_gray_wool"));
        entries.add(Key.key("cyan_wool"));
        entries.add(Key.key("purple_wool"));
        entries.add(Key.key("blue_wool"));
        entries.add(Key.key("brown_wool"));
        entries.add(Key.key("green_wool"));
        entries.add(Key.key("red_wool"));
        entries.add(Key.key("black_wool"));
        entries.add(Key.key("white_wool_stairs"));
        entries.add(Key.key("orange_wool_stairs"));
        entries.add(Key.key("magenta_wool_stairs"));
        entries.add(Key.key("light_blue_wool_stairs"));
        entries.add(Key.key("yellow_wool_stairs"));
        entries.add(Key.key("lime_wool_stairs"));
        entries.add(Key.key("pink_wool_stairs"));
        entries.add(Key.key("gray_wool_stairs"));
        entries.add(Key.key("light_gray_wool_stairs"));
        entries.add(Key.key("cyan_wool_stairs"));
        entries.add(Key.key("purple_wool_stairs"));
        entries.add(Key.key("blue_wool_stairs"));
        entries.add(Key.key("brown_wool_stairs"));
        entries.add(Key.key("green_wool_stairs"));
        entries.add(Key.key("red_wool_stairs"));
        entries.add(Key.key("black_wool_stairs"));
        entries.add(Key.key("white_wool_slab"));
        entries.add(Key.key("orange_wool_slab"));
        entries.add(Key.key("magenta_wool_slab"));
        entries.add(Key.key("light_blue_wool_slab"));
        entries.add(Key.key("yellow_wool_slab"));
        entries.add(Key.key("lime_wool_slab"));
        entries.add(Key.key("pink_wool_slab"));
        entries.add(Key.key("gray_wool_slab"));
        entries.add(Key.key("light_gray_wool_slab"));
        entries.add(Key.key("cyan_wool_slab"));
        entries.add(Key.key("purple_wool_slab"));
        entries.add(Key.key("blue_wool_slab"));
        entries.add(Key.key("brown_wool_slab"));
        entries.add(Key.key("green_wool_slab"));
        entries.add(Key.key("red_wool_slab"));
        entries.add(Key.key("black_wool_slab"));
        entries.add(Key.key("moving_piston"));
    }

    private static void block1(final List<Key> entries) {
        entries.add(Key.key("dandelion"));
        entries.add(Key.key("golden_dandelion"));
        entries.add(Key.key("torchflower"));
        entries.add(Key.key("poppy"));
        entries.add(Key.key("blue_orchid"));
        entries.add(Key.key("allium"));
        entries.add(Key.key("azure_bluet"));
        entries.add(Key.key("red_tulip"));
        entries.add(Key.key("orange_tulip"));
        entries.add(Key.key("white_tulip"));
        entries.add(Key.key("pink_tulip"));
        entries.add(Key.key("oxeye_daisy"));
        entries.add(Key.key("cornflower"));
        entries.add(Key.key("wither_rose"));
        entries.add(Key.key("lily_of_the_valley"));
        entries.add(Key.key("brown_mushroom"));
        entries.add(Key.key("red_mushroom"));
        entries.add(Key.key("gold_block"));
        entries.add(Key.key("iron_block"));
        entries.add(Key.key("bricks"));
        entries.add(Key.key("tnt"));
        entries.add(Key.key("bookshelf"));
        entries.add(Key.key("chiseled_bookshelf"));
        entries.add(Key.key("acacia_shelf"));
        entries.add(Key.key("bamboo_shelf"));
        entries.add(Key.key("birch_shelf"));
        entries.add(Key.key("cherry_shelf"));
        entries.add(Key.key("crimson_shelf"));
        entries.add(Key.key("dark_oak_shelf"));
        entries.add(Key.key("jungle_shelf"));
        entries.add(Key.key("mangrove_shelf"));
        entries.add(Key.key("oak_shelf"));
        entries.add(Key.key("pale_oak_shelf"));
        entries.add(Key.key("poplar_shelf"));
        entries.add(Key.key("spruce_shelf"));
        entries.add(Key.key("warped_shelf"));
        entries.add(Key.key("mossy_cobblestone"));
        entries.add(Key.key("obsidian"));
        entries.add(Key.key("torch"));
        entries.add(Key.key("wall_torch"));
        entries.add(Key.key("fire"));
        entries.add(Key.key("soul_fire"));
        entries.add(Key.key("spawner"));
        entries.add(Key.key("creaking_heart"));
        entries.add(Key.key("oak_stairs"));
        entries.add(Key.key("chest"));
        entries.add(Key.key("redstone_wire"));
        entries.add(Key.key("diamond_ore"));
        entries.add(Key.key("deepslate_diamond_ore"));
        entries.add(Key.key("diamond_block"));
        entries.add(Key.key("crafting_table"));
        entries.add(Key.key("wheat"));
        entries.add(Key.key("farmland"));
        entries.add(Key.key("furnace"));
        entries.add(Key.key("oak_sign"));
        entries.add(Key.key("spruce_sign"));
        entries.add(Key.key("birch_sign"));
        entries.add(Key.key("acacia_sign"));
        entries.add(Key.key("cherry_sign"));
        entries.add(Key.key("jungle_sign"));
        entries.add(Key.key("dark_oak_sign"));
        entries.add(Key.key("pale_oak_sign"));
        entries.add(Key.key("mangrove_sign"));
        entries.add(Key.key("poplar_sign"));
        entries.add(Key.key("bamboo_sign"));
        entries.add(Key.key("oak_door"));
        entries.add(Key.key("ladder"));
        entries.add(Key.key("rail"));
        entries.add(Key.key("cobblestone_stairs"));
        entries.add(Key.key("oak_wall_sign"));
        entries.add(Key.key("spruce_wall_sign"));
        entries.add(Key.key("birch_wall_sign"));
        entries.add(Key.key("acacia_wall_sign"));
        entries.add(Key.key("cherry_wall_sign"));
        entries.add(Key.key("jungle_wall_sign"));
        entries.add(Key.key("dark_oak_wall_sign"));
        entries.add(Key.key("pale_oak_wall_sign"));
        entries.add(Key.key("mangrove_wall_sign"));
        entries.add(Key.key("poplar_wall_sign"));
        entries.add(Key.key("bamboo_wall_sign"));
        entries.add(Key.key("oak_hanging_sign"));
        entries.add(Key.key("spruce_hanging_sign"));
        entries.add(Key.key("birch_hanging_sign"));
        entries.add(Key.key("acacia_hanging_sign"));
        entries.add(Key.key("cherry_hanging_sign"));
        entries.add(Key.key("jungle_hanging_sign"));
        entries.add(Key.key("dark_oak_hanging_sign"));
        entries.add(Key.key("pale_oak_hanging_sign"));
        entries.add(Key.key("crimson_hanging_sign"));
        entries.add(Key.key("warped_hanging_sign"));
        entries.add(Key.key("mangrove_hanging_sign"));
        entries.add(Key.key("poplar_hanging_sign"));
        entries.add(Key.key("bamboo_hanging_sign"));
        entries.add(Key.key("oak_wall_hanging_sign"));
        entries.add(Key.key("spruce_wall_hanging_sign"));
        entries.add(Key.key("birch_wall_hanging_sign"));
        entries.add(Key.key("acacia_wall_hanging_sign"));
        entries.add(Key.key("cherry_wall_hanging_sign"));
        entries.add(Key.key("jungle_wall_hanging_sign"));
        entries.add(Key.key("dark_oak_wall_hanging_sign"));
        entries.add(Key.key("pale_oak_wall_hanging_sign"));
        entries.add(Key.key("mangrove_wall_hanging_sign"));
        entries.add(Key.key("poplar_wall_hanging_sign"));
        entries.add(Key.key("crimson_wall_hanging_sign"));
        entries.add(Key.key("warped_wall_hanging_sign"));
        entries.add(Key.key("bamboo_wall_hanging_sign"));
        entries.add(Key.key("lever"));
        entries.add(Key.key("stone_pressure_plate"));
        entries.add(Key.key("iron_door"));
        entries.add(Key.key("oak_pressure_plate"));
        entries.add(Key.key("spruce_pressure_plate"));
        entries.add(Key.key("birch_pressure_plate"));
        entries.add(Key.key("jungle_pressure_plate"));
        entries.add(Key.key("acacia_pressure_plate"));
        entries.add(Key.key("cherry_pressure_plate"));
        entries.add(Key.key("dark_oak_pressure_plate"));
        entries.add(Key.key("pale_oak_pressure_plate"));
        entries.add(Key.key("mangrove_pressure_plate"));
        entries.add(Key.key("poplar_pressure_plate"));
        entries.add(Key.key("bamboo_pressure_plate"));
        entries.add(Key.key("redstone_ore"));
        entries.add(Key.key("deepslate_redstone_ore"));
        entries.add(Key.key("redstone_torch"));
        entries.add(Key.key("redstone_wall_torch"));
        entries.add(Key.key("stone_button"));
        entries.add(Key.key("snow"));
        entries.add(Key.key("ice"));
        entries.add(Key.key("snow_block"));
        entries.add(Key.key("cactus"));
        entries.add(Key.key("cactus_flower"));
        entries.add(Key.key("clay"));
        entries.add(Key.key("sugar_cane"));
        entries.add(Key.key("jukebox"));
        entries.add(Key.key("oak_fence"));
        entries.add(Key.key("netherrack"));
        entries.add(Key.key("soul_sand"));
        entries.add(Key.key("soul_soil"));
        entries.add(Key.key("basalt"));
        entries.add(Key.key("polished_basalt"));
        entries.add(Key.key("soul_torch"));
        entries.add(Key.key("soul_wall_torch"));
        entries.add(Key.key("copper_torch"));
        entries.add(Key.key("copper_wall_torch"));
        entries.add(Key.key("glowstone"));
        entries.add(Key.key("nether_portal"));
        entries.add(Key.key("carved_pumpkin"));
        entries.add(Key.key("jack_o_lantern"));
        entries.add(Key.key("cake"));
        entries.add(Key.key("repeater"));
        entries.add(Key.key("white_stained_glass"));
        entries.add(Key.key("orange_stained_glass"));
        entries.add(Key.key("magenta_stained_glass"));
        entries.add(Key.key("light_blue_stained_glass"));
        entries.add(Key.key("yellow_stained_glass"));
        entries.add(Key.key("lime_stained_glass"));
        entries.add(Key.key("pink_stained_glass"));
        entries.add(Key.key("gray_stained_glass"));
        entries.add(Key.key("light_gray_stained_glass"));
        entries.add(Key.key("cyan_stained_glass"));
        entries.add(Key.key("purple_stained_glass"));
        entries.add(Key.key("blue_stained_glass"));
        entries.add(Key.key("brown_stained_glass"));
        entries.add(Key.key("green_stained_glass"));
        entries.add(Key.key("red_stained_glass"));
        entries.add(Key.key("black_stained_glass"));
        entries.add(Key.key("oak_trapdoor"));
        entries.add(Key.key("spruce_trapdoor"));
        entries.add(Key.key("birch_trapdoor"));
        entries.add(Key.key("jungle_trapdoor"));
        entries.add(Key.key("acacia_trapdoor"));
        entries.add(Key.key("cherry_trapdoor"));
        entries.add(Key.key("dark_oak_trapdoor"));
        entries.add(Key.key("pale_oak_trapdoor"));
        entries.add(Key.key("mangrove_trapdoor"));
        entries.add(Key.key("poplar_trapdoor"));
        entries.add(Key.key("bamboo_trapdoor"));
        entries.add(Key.key("stone_bricks"));
        entries.add(Key.key("mossy_stone_bricks"));
        entries.add(Key.key("cracked_stone_bricks"));
        entries.add(Key.key("chiseled_stone_bricks"));
        entries.add(Key.key("packed_mud"));
        entries.add(Key.key("mud_bricks"));
        entries.add(Key.key("infested_stone"));
        entries.add(Key.key("infested_cobblestone"));
        entries.add(Key.key("infested_stone_bricks"));
        entries.add(Key.key("infested_mossy_stone_bricks"));
        entries.add(Key.key("infested_cracked_stone_bricks"));
        entries.add(Key.key("infested_chiseled_stone_bricks"));
        entries.add(Key.key("brown_mushroom_block"));
        entries.add(Key.key("red_mushroom_block"));
        entries.add(Key.key("mushroom_stem"));
        entries.add(Key.key("iron_bars"));
        entries.add(Key.key("copper_bars"));
        entries.add(Key.key("exposed_copper_bars"));
        entries.add(Key.key("weathered_copper_bars"));
        entries.add(Key.key("oxidized_copper_bars"));
        entries.add(Key.key("waxed_copper_bars"));
        entries.add(Key.key("waxed_exposed_copper_bars"));
        entries.add(Key.key("waxed_weathered_copper_bars"));
        entries.add(Key.key("waxed_oxidized_copper_bars"));
    }

    private static void block2(final List<Key> entries) {
        entries.add(Key.key("iron_chain"));
        entries.add(Key.key("copper_chain"));
        entries.add(Key.key("exposed_copper_chain"));
        entries.add(Key.key("weathered_copper_chain"));
        entries.add(Key.key("oxidized_copper_chain"));
        entries.add(Key.key("waxed_copper_chain"));
        entries.add(Key.key("waxed_exposed_copper_chain"));
        entries.add(Key.key("waxed_weathered_copper_chain"));
        entries.add(Key.key("waxed_oxidized_copper_chain"));
        entries.add(Key.key("glass_pane"));
        entries.add(Key.key("pumpkin"));
        entries.add(Key.key("melon"));
        entries.add(Key.key("attached_pumpkin_stem"));
        entries.add(Key.key("attached_melon_stem"));
        entries.add(Key.key("pumpkin_stem"));
        entries.add(Key.key("melon_stem"));
        entries.add(Key.key("vine"));
        entries.add(Key.key("glow_lichen"));
        entries.add(Key.key("resin_clump"));
        entries.add(Key.key("oak_fence_gate"));
        entries.add(Key.key("brick_stairs"));
        entries.add(Key.key("stone_brick_stairs"));
        entries.add(Key.key("mud_brick_stairs"));
        entries.add(Key.key("mycelium"));
        entries.add(Key.key("lily_pad"));
        entries.add(Key.key("resin_block"));
        entries.add(Key.key("resin_bricks"));
        entries.add(Key.key("resin_brick_stairs"));
        entries.add(Key.key("resin_brick_slab"));
        entries.add(Key.key("resin_brick_wall"));
        entries.add(Key.key("chiseled_resin_bricks"));
        entries.add(Key.key("nether_bricks"));
        entries.add(Key.key("nether_brick_fence"));
        entries.add(Key.key("nether_brick_stairs"));
        entries.add(Key.key("nether_wart"));
        entries.add(Key.key("enchanting_table"));
        entries.add(Key.key("brewing_stand"));
        entries.add(Key.key("cauldron"));
        entries.add(Key.key("water_cauldron"));
        entries.add(Key.key("lava_cauldron"));
        entries.add(Key.key("powder_snow_cauldron"));
        entries.add(Key.key("end_portal"));
        entries.add(Key.key("end_portal_frame"));
        entries.add(Key.key("end_stone"));
        entries.add(Key.key("dragon_egg"));
        entries.add(Key.key("redstone_lamp"));
        entries.add(Key.key("cocoa"));
        entries.add(Key.key("shelf_mushroom"));
        entries.add(Key.key("sandstone_stairs"));
        entries.add(Key.key("emerald_ore"));
        entries.add(Key.key("deepslate_emerald_ore"));
        entries.add(Key.key("ender_chest"));
        entries.add(Key.key("tripwire_hook"));
        entries.add(Key.key("tripwire"));
        entries.add(Key.key("emerald_block"));
        entries.add(Key.key("spruce_stairs"));
        entries.add(Key.key("birch_stairs"));
        entries.add(Key.key("jungle_stairs"));
        entries.add(Key.key("command_block"));
        entries.add(Key.key("beacon"));
        entries.add(Key.key("cobblestone_wall"));
        entries.add(Key.key("mossy_cobblestone_wall"));
        entries.add(Key.key("flower_pot"));
        entries.add(Key.key("potted_torchflower"));
        entries.add(Key.key("potted_oak_sapling"));
        entries.add(Key.key("potted_spruce_sapling"));
        entries.add(Key.key("potted_birch_sapling"));
        entries.add(Key.key("potted_jungle_sapling"));
        entries.add(Key.key("potted_acacia_sapling"));
        entries.add(Key.key("potted_cherry_sapling"));
        entries.add(Key.key("potted_dark_oak_sapling"));
        entries.add(Key.key("potted_pale_oak_sapling"));
        entries.add(Key.key("potted_poplar_sapling"));
        entries.add(Key.key("potted_mangrove_propagule"));
        entries.add(Key.key("potted_fern"));
        entries.add(Key.key("potted_dandelion"));
        entries.add(Key.key("potted_golden_dandelion"));
        entries.add(Key.key("potted_poppy"));
        entries.add(Key.key("potted_blue_orchid"));
        entries.add(Key.key("potted_allium"));
        entries.add(Key.key("potted_azure_bluet"));
        entries.add(Key.key("potted_red_tulip"));
        entries.add(Key.key("potted_orange_tulip"));
        entries.add(Key.key("potted_white_tulip"));
        entries.add(Key.key("potted_pink_tulip"));
        entries.add(Key.key("potted_oxeye_daisy"));
        entries.add(Key.key("potted_cornflower"));
        entries.add(Key.key("potted_lily_of_the_valley"));
        entries.add(Key.key("potted_wither_rose"));
        entries.add(Key.key("potted_red_mushroom"));
        entries.add(Key.key("potted_brown_mushroom"));
        entries.add(Key.key("potted_dead_bush"));
        entries.add(Key.key("potted_cactus"));
        entries.add(Key.key("carrots"));
        entries.add(Key.key("potatoes"));
        entries.add(Key.key("oak_button"));
        entries.add(Key.key("spruce_button"));
        entries.add(Key.key("birch_button"));
        entries.add(Key.key("jungle_button"));
        entries.add(Key.key("acacia_button"));
        entries.add(Key.key("cherry_button"));
        entries.add(Key.key("dark_oak_button"));
        entries.add(Key.key("pale_oak_button"));
        entries.add(Key.key("mangrove_button"));
        entries.add(Key.key("poplar_button"));
        entries.add(Key.key("bamboo_button"));
        entries.add(Key.key("skeleton_skull"));
        entries.add(Key.key("skeleton_wall_skull"));
        entries.add(Key.key("wither_skeleton_skull"));
        entries.add(Key.key("wither_skeleton_wall_skull"));
        entries.add(Key.key("zombie_head"));
        entries.add(Key.key("zombie_wall_head"));
        entries.add(Key.key("player_head"));
        entries.add(Key.key("player_wall_head"));
        entries.add(Key.key("creeper_head"));
        entries.add(Key.key("creeper_wall_head"));
        entries.add(Key.key("dragon_head"));
        entries.add(Key.key("dragon_wall_head"));
        entries.add(Key.key("piglin_head"));
        entries.add(Key.key("piglin_wall_head"));
        entries.add(Key.key("anvil"));
        entries.add(Key.key("chipped_anvil"));
        entries.add(Key.key("damaged_anvil"));
        entries.add(Key.key("trapped_chest"));
        entries.add(Key.key("light_weighted_pressure_plate"));
        entries.add(Key.key("heavy_weighted_pressure_plate"));
        entries.add(Key.key("comparator"));
        entries.add(Key.key("daylight_detector"));
        entries.add(Key.key("redstone_block"));
        entries.add(Key.key("nether_quartz_ore"));
        entries.add(Key.key("hopper"));
        entries.add(Key.key("quartz_block"));
        entries.add(Key.key("chiseled_quartz_block"));
        entries.add(Key.key("quartz_pillar"));
        entries.add(Key.key("quartz_stairs"));
        entries.add(Key.key("activator_rail"));
        entries.add(Key.key("dropper"));
        entries.add(Key.key("white_terracotta"));
        entries.add(Key.key("orange_terracotta"));
        entries.add(Key.key("magenta_terracotta"));
        entries.add(Key.key("light_blue_terracotta"));
        entries.add(Key.key("yellow_terracotta"));
        entries.add(Key.key("lime_terracotta"));
        entries.add(Key.key("pink_terracotta"));
        entries.add(Key.key("gray_terracotta"));
        entries.add(Key.key("light_gray_terracotta"));
        entries.add(Key.key("cyan_terracotta"));
        entries.add(Key.key("purple_terracotta"));
        entries.add(Key.key("blue_terracotta"));
        entries.add(Key.key("brown_terracotta"));
        entries.add(Key.key("green_terracotta"));
        entries.add(Key.key("red_terracotta"));
        entries.add(Key.key("black_terracotta"));
        entries.add(Key.key("white_stained_glass_pane"));
        entries.add(Key.key("orange_stained_glass_pane"));
        entries.add(Key.key("magenta_stained_glass_pane"));
        entries.add(Key.key("light_blue_stained_glass_pane"));
        entries.add(Key.key("yellow_stained_glass_pane"));
        entries.add(Key.key("lime_stained_glass_pane"));
        entries.add(Key.key("pink_stained_glass_pane"));
        entries.add(Key.key("gray_stained_glass_pane"));
        entries.add(Key.key("light_gray_stained_glass_pane"));
        entries.add(Key.key("cyan_stained_glass_pane"));
        entries.add(Key.key("purple_stained_glass_pane"));
        entries.add(Key.key("blue_stained_glass_pane"));
        entries.add(Key.key("brown_stained_glass_pane"));
        entries.add(Key.key("green_stained_glass_pane"));
        entries.add(Key.key("red_stained_glass_pane"));
        entries.add(Key.key("black_stained_glass_pane"));
        entries.add(Key.key("acacia_stairs"));
        entries.add(Key.key("cherry_stairs"));
        entries.add(Key.key("dark_oak_stairs"));
        entries.add(Key.key("pale_oak_stairs"));
        entries.add(Key.key("mangrove_stairs"));
        entries.add(Key.key("poplar_stairs"));
        entries.add(Key.key("bamboo_stairs"));
        entries.add(Key.key("bamboo_mosaic_stairs"));
        entries.add(Key.key("slime_block"));
        entries.add(Key.key("barrier"));
        entries.add(Key.key("light"));
        entries.add(Key.key("iron_trapdoor"));
        entries.add(Key.key("prismarine"));
        entries.add(Key.key("prismarine_bricks"));
        entries.add(Key.key("dark_prismarine"));
        entries.add(Key.key("prismarine_stairs"));
        entries.add(Key.key("prismarine_brick_stairs"));
        entries.add(Key.key("dark_prismarine_stairs"));
        entries.add(Key.key("prismarine_slab"));
        entries.add(Key.key("prismarine_brick_slab"));
        entries.add(Key.key("dark_prismarine_slab"));
        entries.add(Key.key("sea_lantern"));
        entries.add(Key.key("hay_block"));
        entries.add(Key.key("white_carpet"));
        entries.add(Key.key("orange_carpet"));
        entries.add(Key.key("magenta_carpet"));
        entries.add(Key.key("light_blue_carpet"));
        entries.add(Key.key("yellow_carpet"));
        entries.add(Key.key("lime_carpet"));
        entries.add(Key.key("pink_carpet"));
        entries.add(Key.key("gray_carpet"));
    }

    private static void block3(final List<Key> entries) {
        entries.add(Key.key("light_gray_carpet"));
        entries.add(Key.key("cyan_carpet"));
        entries.add(Key.key("purple_carpet"));
        entries.add(Key.key("blue_carpet"));
        entries.add(Key.key("brown_carpet"));
        entries.add(Key.key("green_carpet"));
        entries.add(Key.key("red_carpet"));
        entries.add(Key.key("black_carpet"));
        entries.add(Key.key("terracotta"));
        entries.add(Key.key("coal_block"));
        entries.add(Key.key("packed_ice"));
        entries.add(Key.key("sunflower"));
        entries.add(Key.key("lilac"));
        entries.add(Key.key("rose_bush"));
        entries.add(Key.key("peony"));
        entries.add(Key.key("tall_grass"));
        entries.add(Key.key("large_fern"));
        entries.add(Key.key("white_banner"));
        entries.add(Key.key("orange_banner"));
        entries.add(Key.key("magenta_banner"));
        entries.add(Key.key("light_blue_banner"));
        entries.add(Key.key("yellow_banner"));
        entries.add(Key.key("lime_banner"));
        entries.add(Key.key("pink_banner"));
        entries.add(Key.key("gray_banner"));
        entries.add(Key.key("light_gray_banner"));
        entries.add(Key.key("cyan_banner"));
        entries.add(Key.key("purple_banner"));
        entries.add(Key.key("blue_banner"));
        entries.add(Key.key("brown_banner"));
        entries.add(Key.key("green_banner"));
        entries.add(Key.key("red_banner"));
        entries.add(Key.key("black_banner"));
        entries.add(Key.key("white_wall_banner"));
        entries.add(Key.key("orange_wall_banner"));
        entries.add(Key.key("magenta_wall_banner"));
        entries.add(Key.key("light_blue_wall_banner"));
        entries.add(Key.key("yellow_wall_banner"));
        entries.add(Key.key("lime_wall_banner"));
        entries.add(Key.key("pink_wall_banner"));
        entries.add(Key.key("gray_wall_banner"));
        entries.add(Key.key("light_gray_wall_banner"));
        entries.add(Key.key("cyan_wall_banner"));
        entries.add(Key.key("purple_wall_banner"));
        entries.add(Key.key("blue_wall_banner"));
        entries.add(Key.key("brown_wall_banner"));
        entries.add(Key.key("green_wall_banner"));
        entries.add(Key.key("red_wall_banner"));
        entries.add(Key.key("black_wall_banner"));
        entries.add(Key.key("red_sandstone"));
        entries.add(Key.key("chiseled_red_sandstone"));
        entries.add(Key.key("cut_red_sandstone"));
        entries.add(Key.key("red_sandstone_stairs"));
        entries.add(Key.key("oak_slab"));
        entries.add(Key.key("spruce_slab"));
        entries.add(Key.key("birch_slab"));
        entries.add(Key.key("jungle_slab"));
        entries.add(Key.key("acacia_slab"));
        entries.add(Key.key("cherry_slab"));
        entries.add(Key.key("dark_oak_slab"));
        entries.add(Key.key("pale_oak_slab"));
        entries.add(Key.key("mangrove_slab"));
        entries.add(Key.key("poplar_slab"));
        entries.add(Key.key("bamboo_slab"));
        entries.add(Key.key("bamboo_mosaic_slab"));
        entries.add(Key.key("stone_slab"));
        entries.add(Key.key("sandstone_slab"));
        entries.add(Key.key("cut_sandstone_slab"));
        entries.add(Key.key("petrified_oak_slab"));
        entries.add(Key.key("cobblestone_slab"));
        entries.add(Key.key("brick_slab"));
        entries.add(Key.key("stone_brick_slab"));
        entries.add(Key.key("mud_brick_slab"));
        entries.add(Key.key("nether_brick_slab"));
        entries.add(Key.key("quartz_slab"));
        entries.add(Key.key("red_sandstone_slab"));
        entries.add(Key.key("cut_red_sandstone_slab"));
        entries.add(Key.key("smooth_stone"));
        entries.add(Key.key("smooth_stone_slab"));
        entries.add(Key.key("smooth_sandstone"));
        entries.add(Key.key("smooth_quartz"));
        entries.add(Key.key("smooth_red_sandstone"));
        entries.add(Key.key("spruce_fence_gate"));
        entries.add(Key.key("birch_fence_gate"));
        entries.add(Key.key("jungle_fence_gate"));
        entries.add(Key.key("acacia_fence_gate"));
        entries.add(Key.key("cherry_fence_gate"));
        entries.add(Key.key("dark_oak_fence_gate"));
        entries.add(Key.key("pale_oak_fence_gate"));
        entries.add(Key.key("mangrove_fence_gate"));
        entries.add(Key.key("poplar_fence_gate"));
        entries.add(Key.key("bamboo_fence_gate"));
        entries.add(Key.key("spruce_fence"));
        entries.add(Key.key("birch_fence"));
        entries.add(Key.key("jungle_fence"));
        entries.add(Key.key("acacia_fence"));
        entries.add(Key.key("cherry_fence"));
        entries.add(Key.key("dark_oak_fence"));
        entries.add(Key.key("pale_oak_fence"));
        entries.add(Key.key("mangrove_fence"));
        entries.add(Key.key("poplar_fence"));
        entries.add(Key.key("bamboo_fence"));
        entries.add(Key.key("spruce_door"));
        entries.add(Key.key("birch_door"));
        entries.add(Key.key("jungle_door"));
        entries.add(Key.key("acacia_door"));
        entries.add(Key.key("cherry_door"));
        entries.add(Key.key("dark_oak_door"));
        entries.add(Key.key("pale_oak_door"));
        entries.add(Key.key("mangrove_door"));
        entries.add(Key.key("poplar_door"));
        entries.add(Key.key("bamboo_door"));
        entries.add(Key.key("end_rod"));
        entries.add(Key.key("chorus_plant"));
        entries.add(Key.key("chorus_flower"));
        entries.add(Key.key("purpur_block"));
        entries.add(Key.key("purpur_slab"));
        entries.add(Key.key("purpur_pillar"));
        entries.add(Key.key("purpur_stairs"));
        entries.add(Key.key("end_stone_bricks"));
        entries.add(Key.key("torchflower_crop"));
        entries.add(Key.key("pitcher_crop"));
        entries.add(Key.key("pitcher_plant"));
        entries.add(Key.key("beetroots"));
        entries.add(Key.key("dirt_path"));
        entries.add(Key.key("end_gateway"));
        entries.add(Key.key("repeating_command_block"));
        entries.add(Key.key("chain_command_block"));
        entries.add(Key.key("frosted_ice"));
        entries.add(Key.key("magma_block"));
        entries.add(Key.key("nether_wart_block"));
        entries.add(Key.key("red_nether_bricks"));
        entries.add(Key.key("bone_block"));
        entries.add(Key.key("structure_void"));
        entries.add(Key.key("observer"));
        entries.add(Key.key("shulker_box"));
        entries.add(Key.key("white_shulker_box"));
        entries.add(Key.key("orange_shulker_box"));
        entries.add(Key.key("magenta_shulker_box"));
        entries.add(Key.key("light_blue_shulker_box"));
        entries.add(Key.key("yellow_shulker_box"));
        entries.add(Key.key("lime_shulker_box"));
        entries.add(Key.key("pink_shulker_box"));
        entries.add(Key.key("gray_shulker_box"));
        entries.add(Key.key("light_gray_shulker_box"));
        entries.add(Key.key("cyan_shulker_box"));
        entries.add(Key.key("purple_shulker_box"));
        entries.add(Key.key("blue_shulker_box"));
        entries.add(Key.key("brown_shulker_box"));
        entries.add(Key.key("green_shulker_box"));
        entries.add(Key.key("red_shulker_box"));
        entries.add(Key.key("black_shulker_box"));
        entries.add(Key.key("white_glazed_terracotta"));
        entries.add(Key.key("orange_glazed_terracotta"));
        entries.add(Key.key("magenta_glazed_terracotta"));
        entries.add(Key.key("light_blue_glazed_terracotta"));
        entries.add(Key.key("yellow_glazed_terracotta"));
        entries.add(Key.key("lime_glazed_terracotta"));
        entries.add(Key.key("pink_glazed_terracotta"));
        entries.add(Key.key("gray_glazed_terracotta"));
        entries.add(Key.key("light_gray_glazed_terracotta"));
        entries.add(Key.key("cyan_glazed_terracotta"));
        entries.add(Key.key("purple_glazed_terracotta"));
        entries.add(Key.key("blue_glazed_terracotta"));
        entries.add(Key.key("brown_glazed_terracotta"));
        entries.add(Key.key("green_glazed_terracotta"));
        entries.add(Key.key("red_glazed_terracotta"));
        entries.add(Key.key("black_glazed_terracotta"));
        entries.add(Key.key("white_concrete"));
        entries.add(Key.key("orange_concrete"));
        entries.add(Key.key("magenta_concrete"));
        entries.add(Key.key("light_blue_concrete"));
        entries.add(Key.key("yellow_concrete"));
        entries.add(Key.key("lime_concrete"));
        entries.add(Key.key("pink_concrete"));
        entries.add(Key.key("gray_concrete"));
        entries.add(Key.key("light_gray_concrete"));
        entries.add(Key.key("cyan_concrete"));
        entries.add(Key.key("purple_concrete"));
        entries.add(Key.key("blue_concrete"));
        entries.add(Key.key("brown_concrete"));
        entries.add(Key.key("green_concrete"));
        entries.add(Key.key("red_concrete"));
        entries.add(Key.key("black_concrete"));
        entries.add(Key.key("white_concrete_stairs"));
        entries.add(Key.key("orange_concrete_stairs"));
        entries.add(Key.key("magenta_concrete_stairs"));
        entries.add(Key.key("light_blue_concrete_stairs"));
        entries.add(Key.key("yellow_concrete_stairs"));
        entries.add(Key.key("lime_concrete_stairs"));
        entries.add(Key.key("pink_concrete_stairs"));
        entries.add(Key.key("gray_concrete_stairs"));
        entries.add(Key.key("light_gray_concrete_stairs"));
        entries.add(Key.key("cyan_concrete_stairs"));
        entries.add(Key.key("purple_concrete_stairs"));
        entries.add(Key.key("blue_concrete_stairs"));
        entries.add(Key.key("brown_concrete_stairs"));
        entries.add(Key.key("green_concrete_stairs"));
        entries.add(Key.key("red_concrete_stairs"));
        entries.add(Key.key("black_concrete_stairs"));
    }

    private static void block4(final List<Key> entries) {
        entries.add(Key.key("white_concrete_slab"));
        entries.add(Key.key("orange_concrete_slab"));
        entries.add(Key.key("magenta_concrete_slab"));
        entries.add(Key.key("light_blue_concrete_slab"));
        entries.add(Key.key("yellow_concrete_slab"));
        entries.add(Key.key("lime_concrete_slab"));
        entries.add(Key.key("pink_concrete_slab"));
        entries.add(Key.key("gray_concrete_slab"));
        entries.add(Key.key("light_gray_concrete_slab"));
        entries.add(Key.key("cyan_concrete_slab"));
        entries.add(Key.key("purple_concrete_slab"));
        entries.add(Key.key("blue_concrete_slab"));
        entries.add(Key.key("brown_concrete_slab"));
        entries.add(Key.key("green_concrete_slab"));
        entries.add(Key.key("red_concrete_slab"));
        entries.add(Key.key("black_concrete_slab"));
        entries.add(Key.key("white_concrete_powder"));
        entries.add(Key.key("orange_concrete_powder"));
        entries.add(Key.key("magenta_concrete_powder"));
        entries.add(Key.key("light_blue_concrete_powder"));
        entries.add(Key.key("yellow_concrete_powder"));
        entries.add(Key.key("lime_concrete_powder"));
        entries.add(Key.key("pink_concrete_powder"));
        entries.add(Key.key("gray_concrete_powder"));
        entries.add(Key.key("light_gray_concrete_powder"));
        entries.add(Key.key("cyan_concrete_powder"));
        entries.add(Key.key("purple_concrete_powder"));
        entries.add(Key.key("blue_concrete_powder"));
        entries.add(Key.key("brown_concrete_powder"));
        entries.add(Key.key("green_concrete_powder"));
        entries.add(Key.key("red_concrete_powder"));
        entries.add(Key.key("black_concrete_powder"));
        entries.add(Key.key("kelp"));
        entries.add(Key.key("kelp_plant"));
        entries.add(Key.key("dried_kelp_block"));
        entries.add(Key.key("turtle_egg"));
        entries.add(Key.key("sniffer_egg"));
        entries.add(Key.key("dried_ghast"));
        entries.add(Key.key("dead_tube_coral_block"));
        entries.add(Key.key("dead_brain_coral_block"));
        entries.add(Key.key("dead_bubble_coral_block"));
        entries.add(Key.key("dead_fire_coral_block"));
        entries.add(Key.key("dead_horn_coral_block"));
        entries.add(Key.key("tube_coral_block"));
        entries.add(Key.key("brain_coral_block"));
        entries.add(Key.key("bubble_coral_block"));
        entries.add(Key.key("fire_coral_block"));
        entries.add(Key.key("horn_coral_block"));
        entries.add(Key.key("dead_tube_coral"));
        entries.add(Key.key("dead_brain_coral"));
        entries.add(Key.key("dead_bubble_coral"));
        entries.add(Key.key("dead_fire_coral"));
        entries.add(Key.key("dead_horn_coral"));
        entries.add(Key.key("tube_coral"));
        entries.add(Key.key("brain_coral"));
        entries.add(Key.key("bubble_coral"));
        entries.add(Key.key("fire_coral"));
        entries.add(Key.key("horn_coral"));
        entries.add(Key.key("dead_tube_coral_fan"));
        entries.add(Key.key("dead_brain_coral_fan"));
        entries.add(Key.key("dead_bubble_coral_fan"));
        entries.add(Key.key("dead_fire_coral_fan"));
        entries.add(Key.key("dead_horn_coral_fan"));
        entries.add(Key.key("tube_coral_fan"));
        entries.add(Key.key("brain_coral_fan"));
        entries.add(Key.key("bubble_coral_fan"));
        entries.add(Key.key("fire_coral_fan"));
        entries.add(Key.key("horn_coral_fan"));
        entries.add(Key.key("dead_tube_coral_wall_fan"));
        entries.add(Key.key("dead_brain_coral_wall_fan"));
        entries.add(Key.key("dead_bubble_coral_wall_fan"));
        entries.add(Key.key("dead_fire_coral_wall_fan"));
        entries.add(Key.key("dead_horn_coral_wall_fan"));
        entries.add(Key.key("tube_coral_wall_fan"));
        entries.add(Key.key("brain_coral_wall_fan"));
        entries.add(Key.key("bubble_coral_wall_fan"));
        entries.add(Key.key("fire_coral_wall_fan"));
        entries.add(Key.key("horn_coral_wall_fan"));
        entries.add(Key.key("sea_pickle"));
        entries.add(Key.key("blue_ice"));
        entries.add(Key.key("conduit"));
        entries.add(Key.key("bamboo_sapling"));
        entries.add(Key.key("bamboo"));
        entries.add(Key.key("potted_bamboo"));
        entries.add(Key.key("void_air"));
        entries.add(Key.key("cave_air"));
        entries.add(Key.key("bubble_column"));
        entries.add(Key.key("polished_granite_stairs"));
        entries.add(Key.key("smooth_red_sandstone_stairs"));
        entries.add(Key.key("mossy_stone_brick_stairs"));
        entries.add(Key.key("polished_diorite_stairs"));
        entries.add(Key.key("mossy_cobblestone_stairs"));
        entries.add(Key.key("end_stone_brick_stairs"));
        entries.add(Key.key("stone_stairs"));
        entries.add(Key.key("smooth_sandstone_stairs"));
        entries.add(Key.key("smooth_quartz_stairs"));
        entries.add(Key.key("granite_stairs"));
        entries.add(Key.key("andesite_stairs"));
        entries.add(Key.key("red_nether_brick_stairs"));
        entries.add(Key.key("polished_andesite_stairs"));
        entries.add(Key.key("diorite_stairs"));
        entries.add(Key.key("polished_granite_slab"));
        entries.add(Key.key("smooth_red_sandstone_slab"));
        entries.add(Key.key("mossy_stone_brick_slab"));
        entries.add(Key.key("polished_diorite_slab"));
        entries.add(Key.key("mossy_cobblestone_slab"));
        entries.add(Key.key("end_stone_brick_slab"));
        entries.add(Key.key("smooth_sandstone_slab"));
        entries.add(Key.key("smooth_quartz_slab"));
        entries.add(Key.key("granite_slab"));
        entries.add(Key.key("andesite_slab"));
        entries.add(Key.key("red_nether_brick_slab"));
        entries.add(Key.key("polished_andesite_slab"));
        entries.add(Key.key("diorite_slab"));
        entries.add(Key.key("brick_wall"));
        entries.add(Key.key("prismarine_wall"));
        entries.add(Key.key("red_sandstone_wall"));
        entries.add(Key.key("mossy_stone_brick_wall"));
        entries.add(Key.key("granite_wall"));
        entries.add(Key.key("stone_brick_wall"));
        entries.add(Key.key("mud_brick_wall"));
        entries.add(Key.key("nether_brick_wall"));
        entries.add(Key.key("andesite_wall"));
        entries.add(Key.key("red_nether_brick_wall"));
        entries.add(Key.key("sandstone_wall"));
        entries.add(Key.key("end_stone_brick_wall"));
        entries.add(Key.key("diorite_wall"));
        entries.add(Key.key("scaffolding"));
        entries.add(Key.key("loom"));
        entries.add(Key.key("barrel"));
        entries.add(Key.key("smoker"));
        entries.add(Key.key("blast_furnace"));
        entries.add(Key.key("cartography_table"));
        entries.add(Key.key("fletching_table"));
        entries.add(Key.key("grindstone"));
        entries.add(Key.key("lectern"));
        entries.add(Key.key("smithing_table"));
        entries.add(Key.key("stonecutter"));
        entries.add(Key.key("bell"));
        entries.add(Key.key("lantern"));
        entries.add(Key.key("soul_lantern"));
        entries.add(Key.key("copper_lantern"));
        entries.add(Key.key("exposed_copper_lantern"));
        entries.add(Key.key("weathered_copper_lantern"));
        entries.add(Key.key("oxidized_copper_lantern"));
        entries.add(Key.key("waxed_copper_lantern"));
        entries.add(Key.key("waxed_exposed_copper_lantern"));
        entries.add(Key.key("waxed_weathered_copper_lantern"));
        entries.add(Key.key("waxed_oxidized_copper_lantern"));
        entries.add(Key.key("campfire"));
        entries.add(Key.key("soul_campfire"));
        entries.add(Key.key("sweet_berry_bush"));
        entries.add(Key.key("warped_stem"));
        entries.add(Key.key("stripped_warped_stem"));
        entries.add(Key.key("warped_hyphae"));
        entries.add(Key.key("stripped_warped_hyphae"));
        entries.add(Key.key("warped_nylium"));
        entries.add(Key.key("warped_fungus"));
        entries.add(Key.key("warped_wart_block"));
        entries.add(Key.key("warped_roots"));
        entries.add(Key.key("nether_sprouts"));
        entries.add(Key.key("crimson_stem"));
        entries.add(Key.key("stripped_crimson_stem"));
        entries.add(Key.key("crimson_hyphae"));
        entries.add(Key.key("stripped_crimson_hyphae"));
        entries.add(Key.key("crimson_nylium"));
        entries.add(Key.key("crimson_fungus"));
        entries.add(Key.key("shroomlight"));
        entries.add(Key.key("weeping_vines"));
        entries.add(Key.key("weeping_vines_plant"));
        entries.add(Key.key("twisting_vines"));
        entries.add(Key.key("twisting_vines_plant"));
        entries.add(Key.key("crimson_roots"));
        entries.add(Key.key("crimson_planks"));
        entries.add(Key.key("warped_planks"));
        entries.add(Key.key("crimson_slab"));
        entries.add(Key.key("warped_slab"));
        entries.add(Key.key("crimson_pressure_plate"));
        entries.add(Key.key("warped_pressure_plate"));
        entries.add(Key.key("crimson_fence"));
        entries.add(Key.key("warped_fence"));
        entries.add(Key.key("crimson_trapdoor"));
        entries.add(Key.key("warped_trapdoor"));
        entries.add(Key.key("crimson_fence_gate"));
        entries.add(Key.key("warped_fence_gate"));
        entries.add(Key.key("crimson_stairs"));
        entries.add(Key.key("warped_stairs"));
        entries.add(Key.key("crimson_button"));
        entries.add(Key.key("warped_button"));
        entries.add(Key.key("crimson_door"));
        entries.add(Key.key("warped_door"));
        entries.add(Key.key("crimson_sign"));
        entries.add(Key.key("warped_sign"));
        entries.add(Key.key("crimson_wall_sign"));
        entries.add(Key.key("warped_wall_sign"));
        entries.add(Key.key("structure_block"));
        entries.add(Key.key("jigsaw"));
        entries.add(Key.key("test_block"));
        entries.add(Key.key("test_instance_block"));
        entries.add(Key.key("composter"));
    }

    private static void block5(final List<Key> entries) {
        entries.add(Key.key("target"));
        entries.add(Key.key("bee_nest"));
        entries.add(Key.key("beehive"));
        entries.add(Key.key("honey_block"));
        entries.add(Key.key("honeycomb_block"));
        entries.add(Key.key("netherite_block"));
        entries.add(Key.key("ancient_debris"));
        entries.add(Key.key("crying_obsidian"));
        entries.add(Key.key("respawn_anchor"));
        entries.add(Key.key("potted_crimson_fungus"));
        entries.add(Key.key("potted_warped_fungus"));
        entries.add(Key.key("potted_crimson_roots"));
        entries.add(Key.key("potted_warped_roots"));
        entries.add(Key.key("lodestone"));
        entries.add(Key.key("blackstone"));
        entries.add(Key.key("blackstone_stairs"));
        entries.add(Key.key("blackstone_wall"));
        entries.add(Key.key("blackstone_slab"));
        entries.add(Key.key("polished_blackstone"));
        entries.add(Key.key("polished_blackstone_bricks"));
        entries.add(Key.key("cracked_polished_blackstone_bricks"));
        entries.add(Key.key("chiseled_polished_blackstone"));
        entries.add(Key.key("polished_blackstone_brick_slab"));
        entries.add(Key.key("polished_blackstone_brick_stairs"));
        entries.add(Key.key("polished_blackstone_brick_wall"));
        entries.add(Key.key("gilded_blackstone"));
        entries.add(Key.key("polished_blackstone_stairs"));
        entries.add(Key.key("polished_blackstone_slab"));
        entries.add(Key.key("polished_blackstone_pressure_plate"));
        entries.add(Key.key("polished_blackstone_button"));
        entries.add(Key.key("polished_blackstone_wall"));
        entries.add(Key.key("chiseled_nether_bricks"));
        entries.add(Key.key("cracked_nether_bricks"));
        entries.add(Key.key("quartz_bricks"));
        entries.add(Key.key("candle"));
        entries.add(Key.key("white_candle"));
        entries.add(Key.key("orange_candle"));
        entries.add(Key.key("magenta_candle"));
        entries.add(Key.key("light_blue_candle"));
        entries.add(Key.key("yellow_candle"));
        entries.add(Key.key("lime_candle"));
        entries.add(Key.key("pink_candle"));
        entries.add(Key.key("gray_candle"));
        entries.add(Key.key("light_gray_candle"));
        entries.add(Key.key("cyan_candle"));
        entries.add(Key.key("purple_candle"));
        entries.add(Key.key("blue_candle"));
        entries.add(Key.key("brown_candle"));
        entries.add(Key.key("green_candle"));
        entries.add(Key.key("red_candle"));
        entries.add(Key.key("black_candle"));
        entries.add(Key.key("candle_cake"));
        entries.add(Key.key("white_candle_cake"));
        entries.add(Key.key("orange_candle_cake"));
        entries.add(Key.key("magenta_candle_cake"));
        entries.add(Key.key("light_blue_candle_cake"));
        entries.add(Key.key("yellow_candle_cake"));
        entries.add(Key.key("lime_candle_cake"));
        entries.add(Key.key("pink_candle_cake"));
        entries.add(Key.key("gray_candle_cake"));
        entries.add(Key.key("light_gray_candle_cake"));
        entries.add(Key.key("cyan_candle_cake"));
        entries.add(Key.key("purple_candle_cake"));
        entries.add(Key.key("blue_candle_cake"));
        entries.add(Key.key("brown_candle_cake"));
        entries.add(Key.key("green_candle_cake"));
        entries.add(Key.key("red_candle_cake"));
        entries.add(Key.key("black_candle_cake"));
        entries.add(Key.key("amethyst_block"));
        entries.add(Key.key("budding_amethyst"));
        entries.add(Key.key("amethyst_cluster"));
        entries.add(Key.key("large_amethyst_bud"));
        entries.add(Key.key("medium_amethyst_bud"));
        entries.add(Key.key("small_amethyst_bud"));
        entries.add(Key.key("tuff"));
        entries.add(Key.key("tuff_slab"));
        entries.add(Key.key("tuff_stairs"));
        entries.add(Key.key("tuff_wall"));
        entries.add(Key.key("polished_tuff"));
        entries.add(Key.key("polished_tuff_slab"));
        entries.add(Key.key("polished_tuff_stairs"));
        entries.add(Key.key("polished_tuff_wall"));
        entries.add(Key.key("chiseled_tuff"));
        entries.add(Key.key("tuff_bricks"));
        entries.add(Key.key("tuff_brick_slab"));
        entries.add(Key.key("tuff_brick_stairs"));
        entries.add(Key.key("tuff_brick_wall"));
        entries.add(Key.key("chiseled_tuff_bricks"));
        entries.add(Key.key("sulfur"));
        entries.add(Key.key("potent_sulfur"));
        entries.add(Key.key("sulfur_slab"));
        entries.add(Key.key("sulfur_stairs"));
        entries.add(Key.key("sulfur_wall"));
        entries.add(Key.key("polished_sulfur"));
        entries.add(Key.key("polished_sulfur_slab"));
        entries.add(Key.key("polished_sulfur_stairs"));
        entries.add(Key.key("polished_sulfur_wall"));
        entries.add(Key.key("sulfur_bricks"));
        entries.add(Key.key("sulfur_brick_slab"));
        entries.add(Key.key("sulfur_brick_stairs"));
        entries.add(Key.key("sulfur_brick_wall"));
        entries.add(Key.key("chiseled_sulfur"));
        entries.add(Key.key("cinnabar"));
        entries.add(Key.key("cinnabar_slab"));
        entries.add(Key.key("cinnabar_stairs"));
        entries.add(Key.key("cinnabar_wall"));
        entries.add(Key.key("polished_cinnabar"));
        entries.add(Key.key("polished_cinnabar_slab"));
        entries.add(Key.key("polished_cinnabar_stairs"));
        entries.add(Key.key("polished_cinnabar_wall"));
        entries.add(Key.key("cinnabar_bricks"));
        entries.add(Key.key("cinnabar_brick_slab"));
        entries.add(Key.key("cinnabar_brick_stairs"));
        entries.add(Key.key("cinnabar_brick_wall"));
        entries.add(Key.key("chiseled_cinnabar"));
        entries.add(Key.key("calcite"));
        entries.add(Key.key("tinted_glass"));
        entries.add(Key.key("powder_snow"));
        entries.add(Key.key("sculk_sensor"));
        entries.add(Key.key("calibrated_sculk_sensor"));
        entries.add(Key.key("sculk"));
        entries.add(Key.key("sculk_vein"));
        entries.add(Key.key("sculk_catalyst"));
        entries.add(Key.key("sculk_shrieker"));
        entries.add(Key.key("copper_block"));
        entries.add(Key.key("exposed_copper"));
        entries.add(Key.key("weathered_copper"));
        entries.add(Key.key("oxidized_copper"));
        entries.add(Key.key("waxed_copper_block"));
        entries.add(Key.key("waxed_exposed_copper"));
        entries.add(Key.key("waxed_weathered_copper"));
        entries.add(Key.key("waxed_oxidized_copper"));
        entries.add(Key.key("copper_ore"));
        entries.add(Key.key("deepslate_copper_ore"));
        entries.add(Key.key("cut_copper"));
        entries.add(Key.key("exposed_cut_copper"));
        entries.add(Key.key("weathered_cut_copper"));
        entries.add(Key.key("oxidized_cut_copper"));
        entries.add(Key.key("waxed_cut_copper"));
        entries.add(Key.key("waxed_exposed_cut_copper"));
        entries.add(Key.key("waxed_weathered_cut_copper"));
        entries.add(Key.key("waxed_oxidized_cut_copper"));
        entries.add(Key.key("chiseled_copper"));
        entries.add(Key.key("exposed_chiseled_copper"));
        entries.add(Key.key("weathered_chiseled_copper"));
        entries.add(Key.key("oxidized_chiseled_copper"));
        entries.add(Key.key("waxed_chiseled_copper"));
        entries.add(Key.key("waxed_exposed_chiseled_copper"));
        entries.add(Key.key("waxed_weathered_chiseled_copper"));
        entries.add(Key.key("waxed_oxidized_chiseled_copper"));
        entries.add(Key.key("cut_copper_stairs"));
        entries.add(Key.key("exposed_cut_copper_stairs"));
        entries.add(Key.key("weathered_cut_copper_stairs"));
        entries.add(Key.key("oxidized_cut_copper_stairs"));
        entries.add(Key.key("waxed_cut_copper_stairs"));
        entries.add(Key.key("waxed_exposed_cut_copper_stairs"));
        entries.add(Key.key("waxed_weathered_cut_copper_stairs"));
        entries.add(Key.key("waxed_oxidized_cut_copper_stairs"));
        entries.add(Key.key("cut_copper_slab"));
        entries.add(Key.key("exposed_cut_copper_slab"));
        entries.add(Key.key("weathered_cut_copper_slab"));
        entries.add(Key.key("oxidized_cut_copper_slab"));
        entries.add(Key.key("waxed_cut_copper_slab"));
        entries.add(Key.key("waxed_exposed_cut_copper_slab"));
        entries.add(Key.key("waxed_weathered_cut_copper_slab"));
        entries.add(Key.key("waxed_oxidized_cut_copper_slab"));
        entries.add(Key.key("copper_door"));
        entries.add(Key.key("exposed_copper_door"));
        entries.add(Key.key("weathered_copper_door"));
        entries.add(Key.key("oxidized_copper_door"));
        entries.add(Key.key("waxed_copper_door"));
        entries.add(Key.key("waxed_exposed_copper_door"));
        entries.add(Key.key("waxed_weathered_copper_door"));
        entries.add(Key.key("waxed_oxidized_copper_door"));
        entries.add(Key.key("copper_trapdoor"));
        entries.add(Key.key("exposed_copper_trapdoor"));
        entries.add(Key.key("weathered_copper_trapdoor"));
        entries.add(Key.key("oxidized_copper_trapdoor"));
        entries.add(Key.key("waxed_copper_trapdoor"));
        entries.add(Key.key("waxed_exposed_copper_trapdoor"));
        entries.add(Key.key("waxed_weathered_copper_trapdoor"));
        entries.add(Key.key("waxed_oxidized_copper_trapdoor"));
        entries.add(Key.key("copper_grate"));
        entries.add(Key.key("exposed_copper_grate"));
        entries.add(Key.key("weathered_copper_grate"));
        entries.add(Key.key("oxidized_copper_grate"));
        entries.add(Key.key("waxed_copper_grate"));
        entries.add(Key.key("waxed_exposed_copper_grate"));
        entries.add(Key.key("waxed_weathered_copper_grate"));
        entries.add(Key.key("waxed_oxidized_copper_grate"));
        entries.add(Key.key("copper_bulb"));
        entries.add(Key.key("exposed_copper_bulb"));
        entries.add(Key.key("weathered_copper_bulb"));
        entries.add(Key.key("oxidized_copper_bulb"));
        entries.add(Key.key("waxed_copper_bulb"));
        entries.add(Key.key("waxed_exposed_copper_bulb"));
        entries.add(Key.key("waxed_weathered_copper_bulb"));
        entries.add(Key.key("waxed_oxidized_copper_bulb"));
        entries.add(Key.key("copper_chest"));
        entries.add(Key.key("exposed_copper_chest"));
    }

    private static void block6(final List<Key> entries) {
        entries.add(Key.key("weathered_copper_chest"));
        entries.add(Key.key("oxidized_copper_chest"));
        entries.add(Key.key("waxed_copper_chest"));
        entries.add(Key.key("waxed_exposed_copper_chest"));
        entries.add(Key.key("waxed_weathered_copper_chest"));
        entries.add(Key.key("waxed_oxidized_copper_chest"));
        entries.add(Key.key("copper_golem_statue"));
        entries.add(Key.key("exposed_copper_golem_statue"));
        entries.add(Key.key("weathered_copper_golem_statue"));
        entries.add(Key.key("oxidized_copper_golem_statue"));
        entries.add(Key.key("waxed_copper_golem_statue"));
        entries.add(Key.key("waxed_exposed_copper_golem_statue"));
        entries.add(Key.key("waxed_weathered_copper_golem_statue"));
        entries.add(Key.key("waxed_oxidized_copper_golem_statue"));
        entries.add(Key.key("lightning_rod"));
        entries.add(Key.key("exposed_lightning_rod"));
        entries.add(Key.key("weathered_lightning_rod"));
        entries.add(Key.key("oxidized_lightning_rod"));
        entries.add(Key.key("waxed_lightning_rod"));
        entries.add(Key.key("waxed_exposed_lightning_rod"));
        entries.add(Key.key("waxed_weathered_lightning_rod"));
        entries.add(Key.key("waxed_oxidized_lightning_rod"));
        entries.add(Key.key("dripstone_block"));
        entries.add(Key.key("pointed_dripstone"));
        entries.add(Key.key("sulfur_spike"));
        entries.add(Key.key("cave_vines"));
        entries.add(Key.key("cave_vines_plant"));
        entries.add(Key.key("spore_blossom"));
        entries.add(Key.key("azalea"));
        entries.add(Key.key("flowering_azalea"));
        entries.add(Key.key("moss_carpet"));
        entries.add(Key.key("pink_petals"));
        entries.add(Key.key("wildflowers"));
        entries.add(Key.key("leaf_litter"));
        entries.add(Key.key("moss_block"));
        entries.add(Key.key("big_dripleaf"));
        entries.add(Key.key("big_dripleaf_stem"));
        entries.add(Key.key("small_dripleaf"));
        entries.add(Key.key("hanging_roots"));
        entries.add(Key.key("rooted_dirt"));
        entries.add(Key.key("mud"));
        entries.add(Key.key("deepslate"));
        entries.add(Key.key("cobbled_deepslate"));
        entries.add(Key.key("cobbled_deepslate_stairs"));
        entries.add(Key.key("cobbled_deepslate_slab"));
        entries.add(Key.key("cobbled_deepslate_wall"));
        entries.add(Key.key("polished_deepslate"));
        entries.add(Key.key("polished_deepslate_stairs"));
        entries.add(Key.key("polished_deepslate_slab"));
        entries.add(Key.key("polished_deepslate_wall"));
        entries.add(Key.key("deepslate_tiles"));
        entries.add(Key.key("deepslate_tile_stairs"));
        entries.add(Key.key("deepslate_tile_slab"));
        entries.add(Key.key("deepslate_tile_wall"));
        entries.add(Key.key("deepslate_bricks"));
        entries.add(Key.key("deepslate_brick_stairs"));
        entries.add(Key.key("deepslate_brick_slab"));
        entries.add(Key.key("deepslate_brick_wall"));
        entries.add(Key.key("chiseled_deepslate"));
        entries.add(Key.key("cracked_deepslate_bricks"));
        entries.add(Key.key("cracked_deepslate_tiles"));
        entries.add(Key.key("infested_deepslate"));
        entries.add(Key.key("smooth_basalt"));
        entries.add(Key.key("raw_iron_block"));
        entries.add(Key.key("raw_copper_block"));
        entries.add(Key.key("raw_gold_block"));
        entries.add(Key.key("potted_azalea_bush"));
        entries.add(Key.key("potted_flowering_azalea_bush"));
        entries.add(Key.key("ochre_froglight"));
        entries.add(Key.key("verdant_froglight"));
        entries.add(Key.key("pearlescent_froglight"));
        entries.add(Key.key("frogspawn"));
        entries.add(Key.key("reinforced_deepslate"));
        entries.add(Key.key("decorated_pot"));
        entries.add(Key.key("crafter"));
        entries.add(Key.key("trial_spawner"));
        entries.add(Key.key("vault"));
        entries.add(Key.key("heavy_core"));
        entries.add(Key.key("pale_moss_block"));
        entries.add(Key.key("pale_moss_carpet"));
        entries.add(Key.key("pale_hanging_moss"));
        entries.add(Key.key("open_eyeblossom"));
        entries.add(Key.key("closed_eyeblossom"));
        entries.add(Key.key("potted_open_eyeblossom"));
        entries.add(Key.key("potted_closed_eyeblossom"));
        entries.add(Key.key("firefly_bush"));
    }

    /**
     * @return {@code minecraft:block}, indexed by network ID
     */
    private static List<Key> block() {
        final List<Key> entries = new ArrayList<>(1286);
        block0(entries);
        block1(entries);
        block2(entries);
        block3(entries);
        block4(entries);
        block5(entries);
        block6(entries);
        return List.copyOf(entries);
    }

    private static void blockTags0(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("acacia_logs"), List.of(Key.key("acacia_log"), Key.key("acacia_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood")));
        tags.put(Key.key("air"), List.of(Key.key("air"), Key.key("cave_air"), Key.key("void_air")));
        tags.put(Key.key("all_hanging_signs"), List.of(Key.key("acacia_hanging_sign"),
                Key.key("acacia_wall_hanging_sign"), Key.key("bamboo_hanging_sign"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("birch_hanging_sign"),
                Key.key("birch_wall_hanging_sign"), Key.key("cherry_hanging_sign"),
                Key.key("cherry_wall_hanging_sign"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_wall_hanging_sign"), Key.key("dark_oak_hanging_sign"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("jungle_hanging_sign"),
                Key.key("jungle_wall_hanging_sign"), Key.key("mangrove_hanging_sign"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("oak_hanging_sign"),
                Key.key("oak_wall_hanging_sign"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("poplar_hanging_sign"),
                Key.key("poplar_wall_hanging_sign"), Key.key("spruce_hanging_sign"),
                Key.key("spruce_wall_hanging_sign"), Key.key("warped_hanging_sign"),
                Key.key("warped_wall_hanging_sign")));
        tags.put(Key.key("all_signs"), List.of(Key.key("acacia_hanging_sign"),
                Key.key("acacia_sign"), Key.key("acacia_wall_hanging_sign"),
                Key.key("acacia_wall_sign"), Key.key("bamboo_hanging_sign"), Key.key("bamboo_sign"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("bamboo_wall_sign"),
                Key.key("birch_hanging_sign"), Key.key("birch_sign"),
                Key.key("birch_wall_hanging_sign"), Key.key("birch_wall_sign"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_sign"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wall_sign"),
                Key.key("crimson_hanging_sign"), Key.key("crimson_sign"),
                Key.key("crimson_wall_hanging_sign"), Key.key("crimson_wall_sign"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_sign"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wall_sign"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_sign"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wall_sign"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_sign"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("mangrove_wall_sign"),
                Key.key("oak_hanging_sign"), Key.key("oak_sign"), Key.key("oak_wall_hanging_sign"),
                Key.key("oak_wall_sign"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_sign"), Key.key("pale_oak_wall_hanging_sign"),
                Key.key("pale_oak_wall_sign"), Key.key("poplar_hanging_sign"),
                Key.key("poplar_sign"), Key.key("poplar_wall_hanging_sign"),
                Key.key("poplar_wall_sign"), Key.key("spruce_hanging_sign"), Key.key("spruce_sign"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wall_sign"),
                Key.key("warped_hanging_sign"), Key.key("warped_sign"),
                Key.key("warped_wall_hanging_sign"), Key.key("warped_wall_sign")));
        tags.put(Key.key("ancient_city_replaceable"), List.of(Key.key("cobbled_deepslate"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_wall"), Key.key("deepslate_tiles"), Key.key("gray_wool")));
        tags.put(Key.key("animals_spawnable_on"), List.of(Key.key("grass_block")));
        tags.put(Key.key("anvil"), List.of(Key.key("anvil"), Key.key("chipped_anvil"),
                Key.key("damaged_anvil")));
        tags.put(Key.key("armadillo_spawnable_on"), List.of(Key.key("brown_terracotta"),
                Key.key("coarse_dirt"), Key.key("grass_block"), Key.key("light_gray_terracotta"),
                Key.key("orange_terracotta"), Key.key("red_sand"), Key.key("red_terracotta"),
                Key.key("terracotta"), Key.key("white_terracotta"), Key.key("yellow_terracotta")));
        tags.put(Key.key("axolotls_spawnable_on"), List.of(Key.key("clay")));
        tags.put(Key.key("azalea_grows_on"), List.of(Key.key("black_terracotta"),
                Key.key("blue_terracotta"), Key.key("brown_terracotta"), Key.key("coarse_dirt"),
                Key.key("cyan_terracotta"), Key.key("dirt"), Key.key("grass_block"),
                Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("orange_terracotta"), Key.key("pale_moss_block"),
                Key.key("pink_terracotta"), Key.key("podzol"), Key.key("powder_snow"),
                Key.key("purple_terracotta"), Key.key("red_sand"), Key.key("red_terracotta"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("snow_block"),
                Key.key("suspicious_sand"), Key.key("terracotta"), Key.key("white_terracotta"),
                Key.key("yellow_terracotta")));
        tags.put(Key.key("azalea_root_replaceable"), List.of(Key.key("andesite"),
                Key.key("black_terracotta"), Key.key("blue_terracotta"),
                Key.key("brown_terracotta"), Key.key("clay"), Key.key("coarse_dirt"),
                Key.key("cyan_terracotta"), Key.key("deepslate"), Key.key("diorite"),
                Key.key("dirt"), Key.key("granite"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("orange_terracotta"), Key.key("pale_moss_block"),
                Key.key("pink_terracotta"), Key.key("podzol"), Key.key("powder_snow"),
                Key.key("purple_terracotta"), Key.key("red_sand"), Key.key("red_terracotta"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("snow_block"), Key.key("stone"),
                Key.key("terracotta"), Key.key("tuff"), Key.key("white_terracotta"),
                Key.key("yellow_terracotta")));
    }

    private static void blockTags1(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("badlands_terracotta"), List.of(Key.key("brown_terracotta"),
                Key.key("light_gray_terracotta"), Key.key("orange_terracotta"),
                Key.key("red_terracotta"), Key.key("terracotta"), Key.key("white_terracotta"),
                Key.key("yellow_terracotta")));
        tags.put(Key.key("bamboo_blocks"), List.of(Key.key("bamboo_block"),
                Key.key("stripped_bamboo_block")));
        tags.put(Key.key("banners"), List.of(Key.key("black_banner"), Key.key("black_wall_banner"),
                Key.key("blue_banner"), Key.key("blue_wall_banner"), Key.key("brown_banner"),
                Key.key("brown_wall_banner"), Key.key("cyan_banner"), Key.key("cyan_wall_banner"),
                Key.key("gray_banner"), Key.key("gray_wall_banner"), Key.key("green_banner"),
                Key.key("green_wall_banner"), Key.key("light_blue_banner"),
                Key.key("light_blue_wall_banner"), Key.key("light_gray_banner"),
                Key.key("light_gray_wall_banner"), Key.key("lime_banner"),
                Key.key("lime_wall_banner"), Key.key("magenta_banner"),
                Key.key("magenta_wall_banner"), Key.key("orange_banner"),
                Key.key("orange_wall_banner"), Key.key("pink_banner"), Key.key("pink_wall_banner"),
                Key.key("purple_banner"), Key.key("purple_wall_banner"), Key.key("red_banner"),
                Key.key("red_wall_banner"), Key.key("white_banner"), Key.key("white_wall_banner"),
                Key.key("yellow_banner"), Key.key("yellow_wall_banner")));
        tags.put(Key.key("bars"), List.of(Key.key("copper_bars"), Key.key("exposed_copper_bars"),
                Key.key("iron_bars"), Key.key("oxidized_copper_bars"), Key.key("waxed_copper_bars"),
                Key.key("waxed_exposed_copper_bars"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_weathered_copper_bars"), Key.key("weathered_copper_bars")));
        tags.put(Key.key("base_stone_nether"), List.of(Key.key("basalt"), Key.key("blackstone"),
                Key.key("netherrack")));
        tags.put(Key.key("base_stone_overworld"), List.of(Key.key("andesite"), Key.key("deepslate"),
                Key.key("diorite"), Key.key("granite"), Key.key("stone"), Key.key("tuff")));
        tags.put(Key.key("bats_spawnable_on"), List.of(Key.key("andesite"), Key.key("deepslate"),
                Key.key("diorite"), Key.key("granite"), Key.key("stone"), Key.key("tuff")));
        tags.put(Key.key("beacon_base_blocks"), List.of(Key.key("diamond_block"),
                Key.key("emerald_block"), Key.key("gold_block"), Key.key("iron_block"),
                Key.key("netherite_block")));
        tags.put(Key.key("beds"), List.of(Key.key("black_bed"), Key.key("blue_bed"),
                Key.key("brown_bed"), Key.key("cyan_bed"), Key.key("gray_bed"),
                Key.key("green_bed"), Key.key("light_blue_bed"), Key.key("light_gray_bed"),
                Key.key("lime_bed"), Key.key("magenta_bed"), Key.key("orange_bed"),
                Key.key("pink_bed"), Key.key("purple_bed"), Key.key("red_bed"),
                Key.key("white_bed"), Key.key("yellow_bed")));
        tags.put(Key.key("bee_attractive"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("cactus_flower"), Key.key("cherry_leaves"),
                Key.key("chorus_flower"), Key.key("cornflower"), Key.key("dandelion"),
                Key.key("flowering_azalea"), Key.key("flowering_azalea_leaves"), Key.key("lilac"),
                Key.key("lily_of_the_valley"), Key.key("mangrove_propagule"),
                Key.key("open_eyeblossom"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("peony"), Key.key("pink_petals"), Key.key("pink_tulip"),
                Key.key("pitcher_plant"), Key.key("poppy"), Key.key("red_tulip"),
                Key.key("rose_bush"), Key.key("spore_blossom"), Key.key("sunflower"),
                Key.key("torchflower"), Key.key("white_tulip"), Key.key("wildflowers"),
                Key.key("wither_rose")));
        tags.put(Key.key("bee_growables"), List.of(Key.key("beetroots"), Key.key("carrots"),
                Key.key("cave_vines"), Key.key("cave_vines_plant"), Key.key("melon_stem"),
                Key.key("pitcher_crop"), Key.key("potatoes"), Key.key("pumpkin_stem"),
                Key.key("sweet_berry_bush"), Key.key("torchflower_crop"), Key.key("wheat")));
        tags.put(Key.key("beehives"), List.of(Key.key("bee_nest"), Key.key("beehive")));
        tags.put(Key.key("beneath_bamboo_podzol_replaceable"), List.of(Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("beneath_tree_podzol_replaceable"), List.of(Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("birch_logs"), List.of(Key.key("birch_log"), Key.key("birch_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood")));
    }

    private static void blockTags2(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_dolphin_jump"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags3(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_fluid_flow"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_sign"), Key.key("acacia_slab"), Key.key("acacia_stairs"),
                Key.key("acacia_trapdoor"), Key.key("acacia_wall_hanging_sign"),
                Key.key("acacia_wall_sign"), Key.key("acacia_wood"), Key.key("amethyst_block"),
                Key.key("amethyst_cluster"), Key.key("ancient_debris"), Key.key("andesite"),
                Key.key("andesite_slab"), Key.key("andesite_stairs"), Key.key("andesite_wall"),
                Key.key("anvil"), Key.key("azalea_leaves"), Key.key("bamboo"),
                Key.key("bamboo_block"), Key.key("bamboo_door"), Key.key("bamboo_fence"),
                Key.key("bamboo_fence_gate"), Key.key("bamboo_hanging_sign"),
                Key.key("bamboo_mosaic"), Key.key("bamboo_mosaic_slab"),
                Key.key("bamboo_mosaic_stairs"), Key.key("bamboo_planks"),
                Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"), Key.key("bamboo_sign"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("bamboo_wall_sign"), Key.key("barrel"),
                Key.key("barrier"), Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"),
                Key.key("bee_nest"), Key.key("beehive"), Key.key("bell"), Key.key("birch_door"),
                Key.key("birch_fence"), Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"),
                Key.key("birch_leaves"), Key.key("birch_log"), Key.key("birch_planks"),
                Key.key("birch_pressure_plate"), Key.key("birch_shelf"), Key.key("birch_sign"),
                Key.key("birch_slab"), Key.key("birch_stairs"), Key.key("birch_trapdoor"),
                Key.key("birch_wall_hanging_sign"), Key.key("birch_wall_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_sign"), Key.key("cherry_slab"), Key.key("cherry_stairs"),
                Key.key("cherry_trapdoor"), Key.key("cherry_wall_hanging_sign"),
                Key.key("cherry_wall_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_sign"), Key.key("crimson_slab"), Key.key("crimson_stairs"),
                Key.key("crimson_stem"), Key.key("crimson_trapdoor"),
                Key.key("crimson_wall_hanging_sign"), Key.key("crimson_wall_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_sign"), Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"),
                Key.key("dark_oak_trapdoor"), Key.key("dark_oak_wall_hanging_sign"),
                Key.key("dark_oak_wall_sign"), Key.key("dark_oak_wood"), Key.key("dark_prismarine"),
                Key.key("dark_prismarine_slab"), Key.key("dark_prismarine_stairs"),
                Key.key("daylight_detector"), Key.key("dead_brain_coral"),
                Key.key("dead_brain_coral_block"), Key.key("dead_brain_coral_fan"),
                Key.key("dead_brain_coral_wall_fan"), Key.key("dead_bubble_coral"),
                Key.key("dead_bubble_coral_block"), Key.key("dead_bubble_coral_fan"),
                Key.key("dead_bubble_coral_wall_fan"), Key.key("dead_fire_coral"),
                Key.key("dead_fire_coral_block"), Key.key("dead_fire_coral_fan"),
                Key.key("dead_fire_coral_wall_fan"), Key.key("dead_horn_coral"),
                Key.key("dead_horn_coral_block"), Key.key("dead_horn_coral_fan"),
                Key.key("dead_horn_coral_wall_fan"), Key.key("dead_tube_coral"),
                Key.key("dead_tube_coral_block"), Key.key("dead_tube_coral_fan"),
                Key.key("dead_tube_coral_wall_fan"), Key.key("decorated_pot"), Key.key("deepslate"),
                Key.key("deepslate_brick_slab"), Key.key("deepslate_brick_stairs"),
                Key.key("deepslate_brick_wall"), Key.key("deepslate_bricks"),
                Key.key("deepslate_coal_ore"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("deepslate_tile_slab"), Key.key("deepslate_tile_stairs"),
                Key.key("deepslate_tile_wall"), Key.key("deepslate_tiles"),
                Key.key("diamond_block"), Key.key("diamond_ore"), Key.key("diorite"),
                Key.key("diorite_slab"), Key.key("diorite_stairs"), Key.key("diorite_wall"),
                Key.key("dirt"), Key.key("dirt_path"), Key.key("dispenser"), Key.key("dragon_egg"),
                Key.key("dried_ghast"), Key.key("dried_kelp_block"), Key.key("dripstone_block"),
                Key.key("dropper"), Key.key("emerald_block"), Key.key("emerald_ore"),
                Key.key("enchanting_table"), Key.key("end_portal_frame"), Key.key("end_stone"),
                Key.key("end_stone_brick_slab"), Key.key("end_stone_brick_stairs"),
                Key.key("end_stone_brick_wall"), Key.key("end_stone_bricks"),
                Key.key("ender_chest"), Key.key("exposed_chiseled_copper"),
                Key.key("exposed_copper"), Key.key("exposed_copper_bars"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chain"),
                Key.key("exposed_copper_chest"), Key.key("exposed_copper_door"),
                Key.key("exposed_copper_grate"), Key.key("exposed_copper_lantern"),
                Key.key("exposed_copper_trapdoor"), Key.key("exposed_cut_copper"),
                Key.key("exposed_cut_copper_slab"), Key.key("exposed_cut_copper_stairs"),
                Key.key("exposed_lightning_rod"), Key.key("farmland"), Key.key("fire_coral_block"),
                Key.key("fletching_table"), Key.key("flowering_azalea_leaves"),
                Key.key("frosted_ice"), Key.key("furnace"), Key.key("gilded_blackstone"),
                Key.key("glass"), Key.key("glass_pane"), Key.key("glowstone"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("granite"),
                Key.key("granite_slab"), Key.key("granite_stairs"), Key.key("granite_wall"),
                Key.key("grass_block"), Key.key("gravel"), Key.key("gray_banner"),
                Key.key("gray_bed"), Key.key("gray_candle_cake"), Key.key("gray_concrete"),
                Key.key("gray_concrete_powder"), Key.key("gray_concrete_slab"),
                Key.key("gray_concrete_stairs"), Key.key("gray_glazed_terracotta"),
                Key.key("gray_shulker_box"), Key.key("gray_stained_glass"),
                Key.key("gray_stained_glass_pane"), Key.key("gray_terracotta"),
                Key.key("gray_wall_banner"), Key.key("gray_wool"), Key.key("gray_wool_slab"),
                Key.key("gray_wool_stairs"), Key.key("green_banner"), Key.key("green_bed"),
                Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_sign"), Key.key("jungle_slab"), Key.key("jungle_stairs"),
                Key.key("jungle_trapdoor"), Key.key("jungle_wall_hanging_sign"),
                Key.key("jungle_wall_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_sign"), Key.key("mangrove_slab"),
                Key.key("mangrove_stairs"), Key.key("mangrove_trapdoor"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("mangrove_wall_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_sign"), Key.key("oak_slab"),
                Key.key("oak_stairs"), Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"),
                Key.key("oak_wall_sign"), Key.key("oak_wood"), Key.key("observer"),
                Key.key("obsidian"), Key.key("ochre_froglight"), Key.key("orange_banner"),
                Key.key("orange_bed"), Key.key("orange_candle_cake"), Key.key("orange_concrete"),
                Key.key("orange_concrete_powder"), Key.key("orange_concrete_slab"),
                Key.key("orange_concrete_stairs"), Key.key("orange_glazed_terracotta"),
                Key.key("orange_poplar_leaves"), Key.key("orange_shulker_box"),
                Key.key("orange_stained_glass"), Key.key("orange_stained_glass_pane"),
                Key.key("orange_terracotta"), Key.key("orange_wall_banner"), Key.key("orange_wool"),
                Key.key("orange_wool_slab"), Key.key("orange_wool_stairs"),
                Key.key("oxidized_chiseled_copper"), Key.key("oxidized_copper"),
                Key.key("oxidized_copper_bars"), Key.key("oxidized_copper_bulb"),
                Key.key("oxidized_copper_chain"), Key.key("oxidized_copper_chest"),
                Key.key("oxidized_copper_door"), Key.key("oxidized_copper_grate"),
                Key.key("oxidized_copper_lantern"), Key.key("oxidized_copper_trapdoor"),
                Key.key("oxidized_cut_copper"), Key.key("oxidized_cut_copper_slab"),
                Key.key("oxidized_cut_copper_stairs"), Key.key("oxidized_lightning_rod"),
                Key.key("packed_ice"), Key.key("packed_mud"), Key.key("pale_moss_block"),
                Key.key("pale_oak_door"), Key.key("pale_oak_fence"), Key.key("pale_oak_fence_gate"),
                Key.key("pale_oak_hanging_sign"), Key.key("pale_oak_leaves"),
                Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_sign"), Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"),
                Key.key("pale_oak_trapdoor"), Key.key("pale_oak_wall_hanging_sign"),
                Key.key("pale_oak_wall_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_sign"),
                Key.key("poplar_slab"), Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wall_sign"),
                Key.key("poplar_wood"), Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"),
                Key.key("prismarine"), Key.key("prismarine_brick_slab"),
                Key.key("prismarine_brick_stairs"), Key.key("prismarine_bricks"),
                Key.key("prismarine_slab"), Key.key("prismarine_stairs"),
                Key.key("prismarine_wall"), Key.key("pumpkin"), Key.key("purple_banner"),
                Key.key("purple_bed"), Key.key("purple_candle_cake"), Key.key("purple_concrete"),
                Key.key("purple_concrete_powder"), Key.key("purple_concrete_slab"),
                Key.key("purple_concrete_stairs"), Key.key("purple_glazed_terracotta"),
                Key.key("purple_shulker_box"), Key.key("purple_stained_glass"),
                Key.key("purple_stained_glass_pane"), Key.key("purple_terracotta"),
                Key.key("purple_wall_banner"), Key.key("purple_wool"), Key.key("purple_wool_slab"),
                Key.key("purple_wool_stairs"), Key.key("purpur_block"), Key.key("purpur_pillar"),
                Key.key("purpur_slab"), Key.key("purpur_stairs"), Key.key("quartz_block"),
                Key.key("quartz_bricks"), Key.key("quartz_pillar"), Key.key("quartz_slab"),
                Key.key("quartz_stairs"), Key.key("raw_copper_block"), Key.key("raw_gold_block"),
                Key.key("raw_iron_block"), Key.key("red_banner"), Key.key("red_bed"),
                Key.key("red_candle_cake"), Key.key("red_concrete"), Key.key("red_concrete_powder"),
                Key.key("red_concrete_slab"), Key.key("red_concrete_stairs"),
                Key.key("red_glazed_terracotta"), Key.key("red_mushroom_block"),
                Key.key("red_nether_brick_slab"), Key.key("red_nether_brick_stairs"),
                Key.key("red_nether_brick_wall"), Key.key("red_nether_bricks"),
                Key.key("red_poplar_leaves"), Key.key("red_sand"), Key.key("red_sandstone"),
                Key.key("red_sandstone_slab"), Key.key("red_sandstone_stairs"),
                Key.key("red_sandstone_wall"), Key.key("red_shulker_box"),
                Key.key("red_stained_glass"), Key.key("red_stained_glass_pane"),
                Key.key("red_terracotta"), Key.key("red_wall_banner"), Key.key("red_wool"),
                Key.key("red_wool_slab"), Key.key("red_wool_stairs"), Key.key("redstone_block"),
                Key.key("redstone_lamp"), Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_sign"), Key.key("spruce_slab"), Key.key("spruce_stairs"),
                Key.key("spruce_trapdoor"), Key.key("spruce_wall_hanging_sign"),
                Key.key("spruce_wall_sign"), Key.key("spruce_wood"), Key.key("sticky_piston"),
                Key.key("stone"), Key.key("stone_brick_slab"), Key.key("stone_brick_stairs"),
                Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_sign"), Key.key("warped_slab"), Key.key("warped_stairs"),
                Key.key("warped_stem"), Key.key("warped_trapdoor"),
                Key.key("warped_wall_hanging_sign"), Key.key("warped_wall_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags4(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_lava_fire_spread"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags5(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_motion"), List.of(Key.key("acacia_door"), Key.key("acacia_fence"),
                Key.key("acacia_fence_gate"), Key.key("acacia_hanging_sign"),
                Key.key("acacia_leaves"), Key.key("acacia_log"), Key.key("acacia_planks"),
                Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"), Key.key("acacia_slab"),
                Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags6(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_motion_in_heightmap"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags7(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_motion_in_heightmap_no_leaves"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_log"), Key.key("acacia_planks"),
                Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"), Key.key("acacia_slab"),
                Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("bamboo"),
                Key.key("bamboo_block"), Key.key("bamboo_door"), Key.key("bamboo_fence"),
                Key.key("bamboo_fence_gate"), Key.key("bamboo_hanging_sign"),
                Key.key("bamboo_mosaic"), Key.key("bamboo_mosaic_slab"),
                Key.key("bamboo_mosaic_stairs"), Key.key("bamboo_planks"),
                Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"), Key.key("bamboo_slab"),
                Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_log"),
                Key.key("birch_planks"), Key.key("birch_pressure_plate"), Key.key("birch_shelf"),
                Key.key("birch_slab"), Key.key("birch_stairs"), Key.key("birch_trapdoor"),
                Key.key("birch_wall_hanging_sign"), Key.key("birch_wood"), Key.key("black_banner"),
                Key.key("black_bed"), Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_log"), Key.key("cherry_planks"),
                Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"), Key.key("cherry_slab"),
                Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_log"),
                Key.key("dark_oak_planks"), Key.key("dark_oak_pressure_plate"),
                Key.key("dark_oak_shelf"), Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"),
                Key.key("dark_oak_trapdoor"), Key.key("dark_oak_wall_hanging_sign"),
                Key.key("dark_oak_wood"), Key.key("dark_prismarine"),
                Key.key("dark_prismarine_slab"), Key.key("dark_prismarine_stairs"),
                Key.key("daylight_detector"), Key.key("dead_brain_coral"),
                Key.key("dead_brain_coral_block"), Key.key("dead_brain_coral_fan"),
                Key.key("dead_brain_coral_wall_fan"), Key.key("dead_bubble_coral"),
                Key.key("dead_bubble_coral_block"), Key.key("dead_bubble_coral_fan"),
                Key.key("dead_bubble_coral_wall_fan"), Key.key("dead_fire_coral"),
                Key.key("dead_fire_coral_block"), Key.key("dead_fire_coral_fan"),
                Key.key("dead_fire_coral_wall_fan"), Key.key("dead_horn_coral"),
                Key.key("dead_horn_coral_block"), Key.key("dead_horn_coral_fan"),
                Key.key("dead_horn_coral_wall_fan"), Key.key("dead_tube_coral"),
                Key.key("dead_tube_coral_block"), Key.key("dead_tube_coral_fan"),
                Key.key("dead_tube_coral_wall_fan"), Key.key("decorated_pot"), Key.key("deepslate"),
                Key.key("deepslate_brick_slab"), Key.key("deepslate_brick_stairs"),
                Key.key("deepslate_brick_wall"), Key.key("deepslate_bricks"),
                Key.key("deepslate_coal_ore"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("deepslate_tile_slab"), Key.key("deepslate_tile_stairs"),
                Key.key("deepslate_tile_wall"), Key.key("deepslate_tiles"),
                Key.key("diamond_block"), Key.key("diamond_ore"), Key.key("diorite"),
                Key.key("diorite_slab"), Key.key("diorite_stairs"), Key.key("diorite_wall"),
                Key.key("dirt"), Key.key("dirt_path"), Key.key("dispenser"), Key.key("dragon_egg"),
                Key.key("dried_ghast"), Key.key("dried_kelp_block"), Key.key("dripstone_block"),
                Key.key("dropper"), Key.key("emerald_block"), Key.key("emerald_ore"),
                Key.key("enchanting_table"), Key.key("end_portal_frame"), Key.key("end_stone"),
                Key.key("end_stone_brick_slab"), Key.key("end_stone_brick_stairs"),
                Key.key("end_stone_brick_wall"), Key.key("end_stone_bricks"),
                Key.key("ender_chest"), Key.key("exposed_chiseled_copper"),
                Key.key("exposed_copper"), Key.key("exposed_copper_bars"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chain"),
                Key.key("exposed_copper_chest"), Key.key("exposed_copper_door"),
                Key.key("exposed_copper_grate"), Key.key("exposed_copper_lantern"),
                Key.key("exposed_copper_trapdoor"), Key.key("exposed_cut_copper"),
                Key.key("exposed_cut_copper_slab"), Key.key("exposed_cut_copper_stairs"),
                Key.key("exposed_lightning_rod"), Key.key("farmland"), Key.key("fire_coral_block"),
                Key.key("fletching_table"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_log"), Key.key("jungle_planks"),
                Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"), Key.key("jungle_slab"),
                Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_log"),
                Key.key("mangrove_planks"), Key.key("mangrove_pressure_plate"),
                Key.key("mangrove_roots"), Key.key("mangrove_shelf"), Key.key("mangrove_slab"),
                Key.key("mangrove_stairs"), Key.key("mangrove_trapdoor"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("mangrove_wood"),
                Key.key("medium_amethyst_bud"), Key.key("melon"), Key.key("moss_block"),
                Key.key("mossy_cobblestone"), Key.key("mossy_cobblestone_slab"),
                Key.key("mossy_cobblestone_stairs"), Key.key("mossy_cobblestone_wall"),
                Key.key("mossy_stone_brick_slab"), Key.key("mossy_stone_brick_stairs"),
                Key.key("mossy_stone_brick_wall"), Key.key("mossy_stone_bricks"),
                Key.key("moving_piston"), Key.key("mud"), Key.key("mud_brick_slab"),
                Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"), Key.key("mud_bricks"),
                Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"), Key.key("mycelium"),
                Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_log"),
                Key.key("oak_planks"), Key.key("oak_pressure_plate"), Key.key("oak_shelf"),
                Key.key("oak_slab"), Key.key("oak_stairs"), Key.key("oak_trapdoor"),
                Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"), Key.key("observer"),
                Key.key("obsidian"), Key.key("ochre_froglight"), Key.key("orange_banner"),
                Key.key("orange_bed"), Key.key("orange_candle_cake"), Key.key("orange_concrete"),
                Key.key("orange_concrete_powder"), Key.key("orange_concrete_slab"),
                Key.key("orange_concrete_stairs"), Key.key("orange_glazed_terracotta"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_sand"), Key.key("red_sandstone"),
                Key.key("red_sandstone_slab"), Key.key("red_sandstone_stairs"),
                Key.key("red_sandstone_wall"), Key.key("red_shulker_box"),
                Key.key("red_stained_glass"), Key.key("red_stained_glass_pane"),
                Key.key("red_terracotta"), Key.key("red_wall_banner"), Key.key("red_wool"),
                Key.key("red_wool_slab"), Key.key("red_wool_stairs"), Key.key("redstone_block"),
                Key.key("redstone_lamp"), Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_log"), Key.key("spruce_planks"),
                Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"), Key.key("spruce_slab"),
                Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_shulker_box"), Key.key("yellow_stained_glass"),
                Key.key("yellow_stained_glass_pane"), Key.key("yellow_terracotta"),
                Key.key("yellow_wall_banner"), Key.key("yellow_wool"), Key.key("yellow_wool_slab"),
                Key.key("yellow_wool_stairs")));
    }

    private static void blockTags8(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_motion_no_leaves"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_log"), Key.key("acacia_planks"),
                Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"), Key.key("acacia_slab"),
                Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("bamboo"),
                Key.key("bamboo_block"), Key.key("bamboo_door"), Key.key("bamboo_fence"),
                Key.key("bamboo_fence_gate"), Key.key("bamboo_hanging_sign"),
                Key.key("bamboo_mosaic"), Key.key("bamboo_mosaic_slab"),
                Key.key("bamboo_mosaic_stairs"), Key.key("bamboo_planks"),
                Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"), Key.key("bamboo_slab"),
                Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_log"),
                Key.key("birch_planks"), Key.key("birch_pressure_plate"), Key.key("birch_shelf"),
                Key.key("birch_slab"), Key.key("birch_stairs"), Key.key("birch_trapdoor"),
                Key.key("birch_wall_hanging_sign"), Key.key("birch_wood"), Key.key("black_banner"),
                Key.key("black_bed"), Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_log"), Key.key("cherry_planks"),
                Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"), Key.key("cherry_slab"),
                Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_log"),
                Key.key("dark_oak_planks"), Key.key("dark_oak_pressure_plate"),
                Key.key("dark_oak_shelf"), Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"),
                Key.key("dark_oak_trapdoor"), Key.key("dark_oak_wall_hanging_sign"),
                Key.key("dark_oak_wood"), Key.key("dark_prismarine"),
                Key.key("dark_prismarine_slab"), Key.key("dark_prismarine_stairs"),
                Key.key("daylight_detector"), Key.key("dead_brain_coral"),
                Key.key("dead_brain_coral_block"), Key.key("dead_brain_coral_fan"),
                Key.key("dead_brain_coral_wall_fan"), Key.key("dead_bubble_coral"),
                Key.key("dead_bubble_coral_block"), Key.key("dead_bubble_coral_fan"),
                Key.key("dead_bubble_coral_wall_fan"), Key.key("dead_fire_coral"),
                Key.key("dead_fire_coral_block"), Key.key("dead_fire_coral_fan"),
                Key.key("dead_fire_coral_wall_fan"), Key.key("dead_horn_coral"),
                Key.key("dead_horn_coral_block"), Key.key("dead_horn_coral_fan"),
                Key.key("dead_horn_coral_wall_fan"), Key.key("dead_tube_coral"),
                Key.key("dead_tube_coral_block"), Key.key("dead_tube_coral_fan"),
                Key.key("dead_tube_coral_wall_fan"), Key.key("decorated_pot"), Key.key("deepslate"),
                Key.key("deepslate_brick_slab"), Key.key("deepslate_brick_stairs"),
                Key.key("deepslate_brick_wall"), Key.key("deepslate_bricks"),
                Key.key("deepslate_coal_ore"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("deepslate_tile_slab"), Key.key("deepslate_tile_stairs"),
                Key.key("deepslate_tile_wall"), Key.key("deepslate_tiles"),
                Key.key("diamond_block"), Key.key("diamond_ore"), Key.key("diorite"),
                Key.key("diorite_slab"), Key.key("diorite_stairs"), Key.key("diorite_wall"),
                Key.key("dirt"), Key.key("dirt_path"), Key.key("dispenser"), Key.key("dragon_egg"),
                Key.key("dried_ghast"), Key.key("dried_kelp_block"), Key.key("dripstone_block"),
                Key.key("dropper"), Key.key("emerald_block"), Key.key("emerald_ore"),
                Key.key("enchanting_table"), Key.key("end_portal_frame"), Key.key("end_stone"),
                Key.key("end_stone_brick_slab"), Key.key("end_stone_brick_stairs"),
                Key.key("end_stone_brick_wall"), Key.key("end_stone_bricks"),
                Key.key("ender_chest"), Key.key("exposed_chiseled_copper"),
                Key.key("exposed_copper"), Key.key("exposed_copper_bars"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chain"),
                Key.key("exposed_copper_chest"), Key.key("exposed_copper_door"),
                Key.key("exposed_copper_grate"), Key.key("exposed_copper_lantern"),
                Key.key("exposed_copper_trapdoor"), Key.key("exposed_cut_copper"),
                Key.key("exposed_cut_copper_slab"), Key.key("exposed_cut_copper_stairs"),
                Key.key("exposed_lightning_rod"), Key.key("farmland"), Key.key("fire_coral_block"),
                Key.key("fletching_table"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_log"), Key.key("jungle_planks"),
                Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"), Key.key("jungle_slab"),
                Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_log"),
                Key.key("mangrove_planks"), Key.key("mangrove_pressure_plate"),
                Key.key("mangrove_roots"), Key.key("mangrove_shelf"), Key.key("mangrove_slab"),
                Key.key("mangrove_stairs"), Key.key("mangrove_trapdoor"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("mangrove_wood"),
                Key.key("medium_amethyst_bud"), Key.key("melon"), Key.key("moss_block"),
                Key.key("mossy_cobblestone"), Key.key("mossy_cobblestone_slab"),
                Key.key("mossy_cobblestone_stairs"), Key.key("mossy_cobblestone_wall"),
                Key.key("mossy_stone_brick_slab"), Key.key("mossy_stone_brick_stairs"),
                Key.key("mossy_stone_brick_wall"), Key.key("mossy_stone_bricks"),
                Key.key("moving_piston"), Key.key("mud"), Key.key("mud_brick_slab"),
                Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"), Key.key("mud_bricks"),
                Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"), Key.key("mycelium"),
                Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_log"),
                Key.key("oak_planks"), Key.key("oak_pressure_plate"), Key.key("oak_shelf"),
                Key.key("oak_slab"), Key.key("oak_stairs"), Key.key("oak_trapdoor"),
                Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"), Key.key("observer"),
                Key.key("obsidian"), Key.key("ochre_froglight"), Key.key("orange_banner"),
                Key.key("orange_bed"), Key.key("orange_candle_cake"), Key.key("orange_concrete"),
                Key.key("orange_concrete_powder"), Key.key("orange_concrete_slab"),
                Key.key("orange_concrete_stairs"), Key.key("orange_glazed_terracotta"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_sand"), Key.key("red_sandstone"),
                Key.key("red_sandstone_slab"), Key.key("red_sandstone_stairs"),
                Key.key("red_sandstone_wall"), Key.key("red_shulker_box"),
                Key.key("red_stained_glass"), Key.key("red_stained_glass_pane"),
                Key.key("red_terracotta"), Key.key("red_wall_banner"), Key.key("red_wool"),
                Key.key("red_wool_slab"), Key.key("red_wool_stairs"), Key.key("redstone_block"),
                Key.key("redstone_lamp"), Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_log"), Key.key("spruce_planks"),
                Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"), Key.key("spruce_slab"),
                Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_shulker_box"), Key.key("yellow_stained_glass"),
                Key.key("yellow_stained_glass_pane"), Key.key("yellow_terracotta"),
                Key.key("yellow_wall_banner"), Key.key("yellow_wool"), Key.key("yellow_wool_slab"),
                Key.key("yellow_wool_stairs")));
    }

    private static void blockTags9(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("blocks_wind_charge_explosions"), List.of(Key.key("barrier"),
                Key.key("bedrock")));
        tags.put(Key.key("buttons"), List.of(Key.key("acacia_button"), Key.key("bamboo_button"),
                Key.key("birch_button"), Key.key("cherry_button"), Key.key("crimson_button"),
                Key.key("dark_oak_button"), Key.key("jungle_button"), Key.key("mangrove_button"),
                Key.key("oak_button"), Key.key("pale_oak_button"),
                Key.key("polished_blackstone_button"), Key.key("poplar_button"),
                Key.key("spruce_button"), Key.key("stone_button"), Key.key("warped_button")));
        tags.put(Key.key("camel_sand_step_sound_blocks"), List.of(Key.key("black_concrete_powder"),
                Key.key("blue_concrete_powder"), Key.key("brown_concrete_powder"),
                Key.key("cyan_concrete_powder"), Key.key("gray_concrete_powder"),
                Key.key("green_concrete_powder"), Key.key("light_blue_concrete_powder"),
                Key.key("light_gray_concrete_powder"), Key.key("lime_concrete_powder"),
                Key.key("magenta_concrete_powder"), Key.key("orange_concrete_powder"),
                Key.key("pink_concrete_powder"), Key.key("purple_concrete_powder"),
                Key.key("red_concrete_powder"), Key.key("red_sand"), Key.key("sand"),
                Key.key("suspicious_sand"), Key.key("white_concrete_powder"),
                Key.key("yellow_concrete_powder")));
        tags.put(Key.key("camels_spawnable_on"), List.of(Key.key("red_sand"), Key.key("sand"),
                Key.key("suspicious_sand")));
        tags.put(Key.key("campfires"), List.of(Key.key("campfire"), Key.key("soul_campfire")));
        tags.put(Key.key("can_glide_through"), List.of(Key.key("cave_vines"),
                Key.key("cave_vines_plant"), Key.key("twisting_vines"),
                Key.key("twisting_vines_plant"), Key.key("vine"), Key.key("weeping_vines"),
                Key.key("weeping_vines_plant")));
        tags.put(Key.key("candle_cakes"), List.of(Key.key("black_candle_cake"),
                Key.key("blue_candle_cake"), Key.key("brown_candle_cake"), Key.key("candle_cake"),
                Key.key("cyan_candle_cake"), Key.key("gray_candle_cake"),
                Key.key("green_candle_cake"), Key.key("light_blue_candle_cake"),
                Key.key("light_gray_candle_cake"), Key.key("lime_candle_cake"),
                Key.key("magenta_candle_cake"), Key.key("orange_candle_cake"),
                Key.key("pink_candle_cake"), Key.key("purple_candle_cake"),
                Key.key("red_candle_cake"), Key.key("white_candle_cake"),
                Key.key("yellow_candle_cake")));
        tags.put(Key.key("candles"), List.of(Key.key("black_candle"), Key.key("blue_candle"),
                Key.key("brown_candle"), Key.key("candle"), Key.key("cyan_candle"),
                Key.key("gray_candle"), Key.key("green_candle"), Key.key("light_blue_candle"),
                Key.key("light_gray_candle"), Key.key("lime_candle"), Key.key("magenta_candle"),
                Key.key("orange_candle"), Key.key("pink_candle"), Key.key("purple_candle"),
                Key.key("red_candle"), Key.key("white_candle"), Key.key("yellow_candle")));
        tags.put(Key.key("cannot_place_basalt_pillar_on"), List.of(Key.key("bedrock"),
                Key.key("chest"), Key.key("lava"), Key.key("magma_block"),
                Key.key("nether_brick_fence"), Key.key("nether_brick_stairs"),
                Key.key("nether_bricks"), Key.key("nether_wart"), Key.key("soul_sand"),
                Key.key("spawner")));
        tags.put(Key.key("cannot_replace_below_tree_trunk"), List.of(Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("rooted_dirt")));
        tags.put(Key.key("cannot_support_kelp"), List.of(Key.key("magma_block")));
        tags.put(Key.key("cannot_support_seagrass"), List.of(Key.key("magma_block")));
        tags.put(Key.key("cannot_support_snow_layer"), List.of(Key.key("barrier"), Key.key("ice"),
                Key.key("packed_ice")));
        tags.put(Key.key("cat_does_not_teleport_to"), List.of(Key.key("cactus"),
                Key.key("campfire"), Key.key("fire"), Key.key("lava_cauldron"),
                Key.key("magma_block"), Key.key("pointed_dripstone"), Key.key("powder_snow"),
                Key.key("soul_campfire"), Key.key("soul_fire"), Key.key("sweet_berry_bush"),
                Key.key("wither_rose")));
        tags.put(Key.key("cats_can_lie_on"), List.of(Key.key("black_bed"), Key.key("blue_bed"),
                Key.key("brown_bed"), Key.key("cyan_bed"), Key.key("gray_bed"),
                Key.key("green_bed"), Key.key("light_blue_bed"), Key.key("light_gray_bed"),
                Key.key("lime_bed"), Key.key("magenta_bed"), Key.key("orange_bed"),
                Key.key("pink_bed"), Key.key("purple_bed"), Key.key("red_bed"),
                Key.key("white_bed"), Key.key("yellow_bed")));
        tags.put(Key.key("cats_can_sit_on"), List.of(Key.key("black_bed"), Key.key("blue_bed"),
                Key.key("brown_bed"), Key.key("chest"), Key.key("cyan_bed"), Key.key("furnace"),
                Key.key("gray_bed"), Key.key("green_bed"), Key.key("light_blue_bed"),
                Key.key("light_gray_bed"), Key.key("lime_bed"), Key.key("magenta_bed"),
                Key.key("orange_bed"), Key.key("pink_bed"), Key.key("purple_bed"),
                Key.key("red_bed"), Key.key("white_bed"), Key.key("yellow_bed")));
    }

    private static void blockTags10(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("cauldrons"), List.of(Key.key("cauldron"), Key.key("lava_cauldron"),
                Key.key("powder_snow_cauldron"), Key.key("water_cauldron")));
        tags.put(Key.key("causes_continuous_geyser_eruptions"), List.of(Key.key("lava")));
        tags.put(Key.key("causes_periodic_geyser_eruptions"), List.of(Key.key("magma_block")));
        tags.put(Key.key("causes_suffocation"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags11(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("cave_vines"), List.of(Key.key("cave_vines"),
                Key.key("cave_vines_plant")));
        tags.put(Key.key("ceiling_hanging_signs"), List.of(Key.key("acacia_hanging_sign"),
                Key.key("bamboo_hanging_sign"), Key.key("birch_hanging_sign"),
                Key.key("cherry_hanging_sign"), Key.key("crimson_hanging_sign"),
                Key.key("dark_oak_hanging_sign"), Key.key("jungle_hanging_sign"),
                Key.key("mangrove_hanging_sign"), Key.key("oak_hanging_sign"),
                Key.key("pale_oak_hanging_sign"), Key.key("poplar_hanging_sign"),
                Key.key("spruce_hanging_sign"), Key.key("warped_hanging_sign")));
        tags.put(Key.key("chains"), List.of(Key.key("copper_chain"),
                Key.key("exposed_copper_chain"), Key.key("iron_chain"),
                Key.key("oxidized_copper_chain"), Key.key("waxed_copper_chain"),
                Key.key("waxed_exposed_copper_chain"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_weathered_copper_chain"), Key.key("weathered_copper_chain")));
        tags.put(Key.key("cherry_logs"), List.of(Key.key("cherry_log"), Key.key("cherry_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood")));
        tags.put(Key.key("climbable"), List.of(Key.key("cave_vines"), Key.key("cave_vines_plant"),
                Key.key("ladder"), Key.key("scaffolding"), Key.key("twisting_vines"),
                Key.key("twisting_vines_plant"), Key.key("vine"), Key.key("weeping_vines"),
                Key.key("weeping_vines_plant")));
        tags.put(Key.key("coal_ores"), List.of(Key.key("coal_ore"), Key.key("deepslate_coal_ore")));
        tags.put(Key.key("combination_step_sound_blocks"), List.of(Key.key("black_carpet"),
                Key.key("blue_carpet"), Key.key("brown_carpet"), Key.key("crimson_roots"),
                Key.key("cyan_carpet"), Key.key("gray_carpet"), Key.key("green_carpet"),
                Key.key("light_blue_carpet"), Key.key("light_gray_carpet"), Key.key("lime_carpet"),
                Key.key("magenta_carpet"), Key.key("moss_carpet"), Key.key("nether_sprouts"),
                Key.key("orange_carpet"), Key.key("pale_moss_carpet"), Key.key("pink_carpet"),
                Key.key("purple_carpet"), Key.key("red_carpet"), Key.key("resin_clump"),
                Key.key("snow"), Key.key("warped_roots"), Key.key("white_carpet"),
                Key.key("yellow_carpet")));
        tags.put(Key.key("completes_find_tree_tutorial"), List.of(Key.key("acacia_leaves"),
                Key.key("acacia_log"), Key.key("acacia_wood"), Key.key("azalea_leaves"),
                Key.key("birch_leaves"), Key.key("birch_log"), Key.key("birch_wood"),
                Key.key("cherry_leaves"), Key.key("cherry_log"), Key.key("cherry_wood"),
                Key.key("crimson_hyphae"), Key.key("crimson_stem"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_wood"),
                Key.key("flowering_azalea_leaves"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_wood"), Key.key("mangrove_leaves"), Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("nether_wart_block"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_wood"), Key.key("orange_poplar_leaves"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_wood"),
                Key.key("poplar_log"), Key.key("poplar_wood"), Key.key("red_poplar_leaves"),
                Key.key("spruce_leaves"), Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("warped_hyphae"), Key.key("warped_stem"), Key.key("warped_wart_block"),
                Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("concrete"), List.of(Key.key("black_concrete"), Key.key("blue_concrete"),
                Key.key("brown_concrete"), Key.key("cyan_concrete"), Key.key("gray_concrete"),
                Key.key("green_concrete"), Key.key("light_blue_concrete"),
                Key.key("light_gray_concrete"), Key.key("lime_concrete"),
                Key.key("magenta_concrete"), Key.key("orange_concrete"), Key.key("pink_concrete"),
                Key.key("purple_concrete"), Key.key("red_concrete"), Key.key("white_concrete"),
                Key.key("yellow_concrete")));
        tags.put(Key.key("concrete_powders"), List.of(Key.key("black_concrete_powder"),
                Key.key("blue_concrete_powder"), Key.key("brown_concrete_powder"),
                Key.key("cyan_concrete_powder"), Key.key("gray_concrete_powder"),
                Key.key("green_concrete_powder"), Key.key("light_blue_concrete_powder"),
                Key.key("light_gray_concrete_powder"), Key.key("lime_concrete_powder"),
                Key.key("magenta_concrete_powder"), Key.key("orange_concrete_powder"),
                Key.key("pink_concrete_powder"), Key.key("purple_concrete_powder"),
                Key.key("red_concrete_powder"), Key.key("white_concrete_powder"),
                Key.key("yellow_concrete_powder")));
    }

    private static void blockTags12(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("concrete_slabs"), List.of(Key.key("black_concrete_slab"),
                Key.key("blue_concrete_slab"), Key.key("brown_concrete_slab"),
                Key.key("cyan_concrete_slab"), Key.key("gray_concrete_slab"),
                Key.key("green_concrete_slab"), Key.key("light_blue_concrete_slab"),
                Key.key("light_gray_concrete_slab"), Key.key("lime_concrete_slab"),
                Key.key("magenta_concrete_slab"), Key.key("orange_concrete_slab"),
                Key.key("pink_concrete_slab"), Key.key("purple_concrete_slab"),
                Key.key("red_concrete_slab"), Key.key("white_concrete_slab"),
                Key.key("yellow_concrete_slab")));
        tags.put(Key.key("concrete_stairs"), List.of(Key.key("black_concrete_stairs"),
                Key.key("blue_concrete_stairs"), Key.key("brown_concrete_stairs"),
                Key.key("cyan_concrete_stairs"), Key.key("gray_concrete_stairs"),
                Key.key("green_concrete_stairs"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_gray_concrete_stairs"), Key.key("lime_concrete_stairs"),
                Key.key("magenta_concrete_stairs"), Key.key("orange_concrete_stairs"),
                Key.key("pink_concrete_stairs"), Key.key("purple_concrete_stairs"),
                Key.key("red_concrete_stairs"), Key.key("white_concrete_stairs"),
                Key.key("yellow_concrete_stairs")));
        tags.put(Key.key("conduit_effect_block"), List.of(Key.key("dark_prismarine"),
                Key.key("prismarine"), Key.key("prismarine_bricks"), Key.key("sea_lantern")));
        tags.put(Key.key("convertible_to_mud"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("rooted_dirt")));
        tags.put(Key.key("copper"), List.of(Key.key("copper_block"), Key.key("exposed_copper"),
                Key.key("oxidized_copper"), Key.key("waxed_copper_block"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_oxidized_copper"),
                Key.key("waxed_weathered_copper"), Key.key("weathered_copper")));
        tags.put(Key.key("copper_chests"), List.of(Key.key("copper_chest"),
                Key.key("exposed_copper_chest"), Key.key("oxidized_copper_chest"),
                Key.key("waxed_copper_chest"), Key.key("waxed_exposed_copper_chest"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_weathered_copper_chest"),
                Key.key("weathered_copper_chest")));
        tags.put(Key.key("copper_golem_statues"), List.of(Key.key("copper_golem_statue"),
                Key.key("exposed_copper_golem_statue"), Key.key("oxidized_copper_golem_statue"),
                Key.key("waxed_copper_golem_statue"), Key.key("waxed_exposed_copper_golem_statue"),
                Key.key("waxed_oxidized_copper_golem_statue"),
                Key.key("waxed_weathered_copper_golem_statue"),
                Key.key("weathered_copper_golem_statue")));
        tags.put(Key.key("copper_ores"), List.of(Key.key("copper_ore"),
                Key.key("deepslate_copper_ore")));
        tags.put(Key.key("coral_blocks"), List.of(Key.key("brain_coral_block"),
                Key.key("bubble_coral_block"), Key.key("fire_coral_block"),
                Key.key("horn_coral_block"), Key.key("tube_coral_block")));
        tags.put(Key.key("coral_plants"), List.of(Key.key("brain_coral"), Key.key("bubble_coral"),
                Key.key("fire_coral"), Key.key("horn_coral"), Key.key("tube_coral")));
        tags.put(Key.key("corals"), List.of(Key.key("brain_coral"), Key.key("brain_coral_fan"),
                Key.key("bubble_coral"), Key.key("bubble_coral_fan"), Key.key("fire_coral"),
                Key.key("fire_coral_fan"), Key.key("horn_coral"), Key.key("horn_coral_fan"),
                Key.key("tube_coral"), Key.key("tube_coral_fan")));
        tags.put(Key.key("crimson_stems"), List.of(Key.key("crimson_hyphae"),
                Key.key("crimson_stem"), Key.key("stripped_crimson_hyphae"),
                Key.key("stripped_crimson_stem")));
        tags.put(Key.key("crops"), List.of(Key.key("beetroots"), Key.key("carrots"),
                Key.key("melon_stem"), Key.key("pitcher_crop"), Key.key("potatoes"),
                Key.key("pumpkin_stem"), Key.key("torchflower_crop"), Key.key("wheat")));
        tags.put(Key.key("crystal_sound_blocks"), List.of(Key.key("amethyst_block"),
                Key.key("budding_amethyst")));
        tags.put(Key.key("cushion_uses_collision_shape"), List.of(Key.key("cauldron"),
                Key.key("composter"), Key.key("hopper"), Key.key("lava_cauldron"),
                Key.key("powder_snow_cauldron"), Key.key("water_cauldron")));
        tags.put(Key.key("dampens_vibrations"), List.of(Key.key("black_carpet"),
                Key.key("black_wool"), Key.key("black_wool_slab"), Key.key("black_wool_stairs"),
                Key.key("blue_carpet"), Key.key("blue_wool"), Key.key("blue_wool_slab"),
                Key.key("blue_wool_stairs"), Key.key("brown_carpet"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"), Key.key("cyan_carpet"),
                Key.key("cyan_wool"), Key.key("cyan_wool_slab"), Key.key("cyan_wool_stairs"),
                Key.key("gray_carpet"), Key.key("gray_wool"), Key.key("gray_wool_slab"),
                Key.key("gray_wool_stairs"), Key.key("green_carpet"), Key.key("green_wool"),
                Key.key("green_wool_slab"), Key.key("green_wool_stairs"),
                Key.key("light_blue_carpet"), Key.key("light_blue_wool"),
                Key.key("light_blue_wool_slab"), Key.key("light_blue_wool_stairs"),
                Key.key("light_gray_carpet"), Key.key("light_gray_wool"),
                Key.key("light_gray_wool_slab"), Key.key("light_gray_wool_stairs"),
                Key.key("lime_carpet"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("magenta_carpet"), Key.key("magenta_wool"),
                Key.key("magenta_wool_slab"), Key.key("magenta_wool_stairs"),
                Key.key("orange_carpet"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("pink_carpet"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("purple_carpet"),
                Key.key("purple_wool"), Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"),
                Key.key("red_carpet"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("white_carpet"), Key.key("white_wool"),
                Key.key("white_wool_slab"), Key.key("white_wool_stairs"), Key.key("yellow_carpet"),
                Key.key("yellow_wool"), Key.key("yellow_wool_slab"),
                Key.key("yellow_wool_stairs")));
    }

    private static void blockTags13(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("dangerous_for_teleportation"), List.of(Key.key("cactus"),
                Key.key("campfire"), Key.key("fire"), Key.key("lava_cauldron"),
                Key.key("magma_block"), Key.key("pointed_dripstone"), Key.key("powder_snow"),
                Key.key("soul_campfire"), Key.key("soul_fire"), Key.key("sweet_berry_bush"),
                Key.key("wither_rose")));
        tags.put(Key.key("dark_oak_logs"), List.of(Key.key("dark_oak_log"),
                Key.key("dark_oak_wood"), Key.key("stripped_dark_oak_log"),
                Key.key("stripped_dark_oak_wood")));
        tags.put(Key.key("deepslate_ore_replaceables"), List.of(Key.key("deepslate")));
        tags.put(Key.key("default_immune_to"), List.<Key>of());
        tags.put(Key.key("diamond_ores"), List.of(Key.key("deepslate_diamond_ore"),
                Key.key("diamond_ore")));
        tags.put(Key.key("dirt"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("rooted_dirt")));
        tags.put(Key.key("does_not_block_hoppers"), List.of(Key.key("bee_nest"),
                Key.key("beehive")));
        tags.put(Key.key("doors"), List.of(Key.key("acacia_door"), Key.key("bamboo_door"),
                Key.key("birch_door"), Key.key("cherry_door"), Key.key("copper_door"),
                Key.key("crimson_door"), Key.key("dark_oak_door"), Key.key("exposed_copper_door"),
                Key.key("iron_door"), Key.key("jungle_door"), Key.key("mangrove_door"),
                Key.key("oak_door"), Key.key("oxidized_copper_door"), Key.key("pale_oak_door"),
                Key.key("poplar_door"), Key.key("spruce_door"), Key.key("warped_door"),
                Key.key("waxed_copper_door"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_oxidized_copper_door"), Key.key("waxed_weathered_copper_door"),
                Key.key("weathered_copper_door")));
        tags.put(Key.key("dragon_immune"), List.of(Key.key("barrier"), Key.key("bedrock"),
                Key.key("chain_command_block"), Key.key("command_block"),
                Key.key("crying_obsidian"), Key.key("end_gateway"), Key.key("end_portal"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("iron_bars"),
                Key.key("jigsaw"), Key.key("moving_piston"), Key.key("obsidian"),
                Key.key("reinforced_deepslate"), Key.key("repeating_command_block"),
                Key.key("respawn_anchor"), Key.key("structure_block"), Key.key("test_block"),
                Key.key("test_instance_block")));
        tags.put(Key.key("dragon_transparent"), List.of(Key.key("fire"), Key.key("light"),
                Key.key("soul_fire")));
        tags.put(Key.key("dripstone_replaceable_blocks"), List.of(Key.key("andesite"),
                Key.key("deepslate"), Key.key("diorite"), Key.key("granite"), Key.key("stone"),
                Key.key("tuff")));
        tags.put(Key.key("edible_for_sheep"), List.of(Key.key("fern"), Key.key("short_dry_grass"),
                Key.key("short_grass"), Key.key("tall_dry_grass")));
        tags.put(Key.key("emerald_ores"), List.of(Key.key("deepslate_emerald_ore"),
                Key.key("emerald_ore")));
        tags.put(Key.key("enables_bubble_column_drag_down"), List.of(Key.key("magma_block")));
        tags.put(Key.key("enables_bubble_column_push_up"), List.of(Key.key("soul_sand")));
        tags.put(Key.key("enchantment_power_provider"), List.of(Key.key("bookshelf")));
        tags.put(Key.key("enchantment_power_transmitter"), List.of(Key.key("air"),
                Key.key("bubble_column"), Key.key("bush"), Key.key("cave_air"),
                Key.key("crimson_roots"), Key.key("dead_bush"), Key.key("fern"), Key.key("fire"),
                Key.key("glow_lichen"), Key.key("hanging_roots"), Key.key("large_fern"),
                Key.key("lava"), Key.key("leaf_litter"), Key.key("light"),
                Key.key("nether_sprouts"), Key.key("red_shrub"), Key.key("resin_clump"),
                Key.key("seagrass"), Key.key("short_dry_grass"), Key.key("short_grass"),
                Key.key("snow"), Key.key("soul_fire"), Key.key("structure_void"),
                Key.key("tall_dry_grass"), Key.key("tall_grass"), Key.key("tall_seagrass"),
                Key.key("vine"), Key.key("void_air"), Key.key("warped_roots"), Key.key("water")));
        tags.put(Key.key("enderman_does_not_teleport_to"), List.of(Key.key("bedrock"),
                Key.key("cactus"), Key.key("campfire"), Key.key("fire"), Key.key("lava_cauldron"),
                Key.key("magma_block"), Key.key("pointed_dripstone"), Key.key("powder_snow"),
                Key.key("soul_campfire"), Key.key("soul_fire"), Key.key("sweet_berry_bush"),
                Key.key("wither_rose")));
        tags.put(Key.key("enderman_holdable"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("brown_mushroom"), Key.key("cactus"),
                Key.key("cactus_flower"), Key.key("carved_pumpkin"), Key.key("clay"),
                Key.key("closed_eyeblossom"), Key.key("coarse_dirt"), Key.key("cornflower"),
                Key.key("crimson_fungus"), Key.key("crimson_nylium"), Key.key("crimson_roots"),
                Key.key("dandelion"), Key.key("dirt"), Key.key("golden_dandelion"),
                Key.key("grass_block"), Key.key("gravel"), Key.key("lily_of_the_valley"),
                Key.key("melon"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("open_eyeblossom"),
                Key.key("orange_tulip"), Key.key("oxeye_daisy"), Key.key("pale_moss_block"),
                Key.key("pink_tulip"), Key.key("podzol"), Key.key("poppy"), Key.key("pumpkin"),
                Key.key("red_mushroom"), Key.key("red_sand"), Key.key("red_tulip"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("tnt"), Key.key("torchflower"),
                Key.key("warped_fungus"), Key.key("warped_nylium"), Key.key("warped_roots"),
                Key.key("white_tulip"), Key.key("wither_rose")));
    }

    private static void blockTags14(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("entities_can_teleport_to"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags15(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("fall_damage_resetting"), List.of(Key.key("cave_vines"),
                Key.key("cave_vines_plant"), Key.key("cobweb"), Key.key("ladder"),
                Key.key("scaffolding"), Key.key("sweet_berry_bush"), Key.key("twisting_vines"),
                Key.key("twisting_vines_plant"), Key.key("vine"), Key.key("weeping_vines"),
                Key.key("weeping_vines_plant")));
        tags.put(Key.key("features_cannot_replace"), List.of(Key.key("bedrock"), Key.key("chest"),
                Key.key("end_portal_frame"), Key.key("reinforced_deepslate"), Key.key("spawner"),
                Key.key("trial_spawner"), Key.key("vault")));
        tags.put(Key.key("fence_gates"), List.of(Key.key("acacia_fence_gate"),
                Key.key("bamboo_fence_gate"), Key.key("birch_fence_gate"),
                Key.key("cherry_fence_gate"), Key.key("crimson_fence_gate"),
                Key.key("dark_oak_fence_gate"), Key.key("jungle_fence_gate"),
                Key.key("mangrove_fence_gate"), Key.key("oak_fence_gate"),
                Key.key("pale_oak_fence_gate"), Key.key("poplar_fence_gate"),
                Key.key("spruce_fence_gate"), Key.key("warped_fence_gate")));
        tags.put(Key.key("fences"), List.of(Key.key("acacia_fence"), Key.key("bamboo_fence"),
                Key.key("birch_fence"), Key.key("cherry_fence"), Key.key("crimson_fence"),
                Key.key("dark_oak_fence"), Key.key("jungle_fence"), Key.key("mangrove_fence"),
                Key.key("nether_brick_fence"), Key.key("oak_fence"), Key.key("pale_oak_fence"),
                Key.key("poplar_fence"), Key.key("spruce_fence"), Key.key("warped_fence")));
        tags.put(Key.key("fire"), List.of(Key.key("fire"), Key.key("soul_fire")));
        tags.put(Key.key("flower_pots"), List.of(Key.key("flower_pot"),
                Key.key("potted_acacia_sapling"), Key.key("potted_allium"),
                Key.key("potted_azalea_bush"), Key.key("potted_azure_bluet"),
                Key.key("potted_bamboo"), Key.key("potted_birch_sapling"),
                Key.key("potted_blue_orchid"), Key.key("potted_brown_mushroom"),
                Key.key("potted_cactus"), Key.key("potted_cherry_sapling"),
                Key.key("potted_closed_eyeblossom"), Key.key("potted_cornflower"),
                Key.key("potted_crimson_fungus"), Key.key("potted_crimson_roots"),
                Key.key("potted_dandelion"), Key.key("potted_dark_oak_sapling"),
                Key.key("potted_dead_bush"), Key.key("potted_fern"),
                Key.key("potted_flowering_azalea_bush"), Key.key("potted_golden_dandelion"),
                Key.key("potted_jungle_sapling"), Key.key("potted_lily_of_the_valley"),
                Key.key("potted_mangrove_propagule"), Key.key("potted_oak_sapling"),
                Key.key("potted_open_eyeblossom"), Key.key("potted_orange_tulip"),
                Key.key("potted_oxeye_daisy"), Key.key("potted_pale_oak_sapling"),
                Key.key("potted_pink_tulip"), Key.key("potted_poplar_sapling"),
                Key.key("potted_poppy"), Key.key("potted_red_mushroom"),
                Key.key("potted_red_tulip"), Key.key("potted_spruce_sapling"),
                Key.key("potted_torchflower"), Key.key("potted_warped_fungus"),
                Key.key("potted_warped_roots"), Key.key("potted_white_tulip"),
                Key.key("potted_wither_rose")));
        tags.put(Key.key("flowers"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("cactus_flower"), Key.key("cherry_leaves"),
                Key.key("chorus_flower"), Key.key("closed_eyeblossom"), Key.key("cornflower"),
                Key.key("dandelion"), Key.key("flowering_azalea"),
                Key.key("flowering_azalea_leaves"), Key.key("golden_dandelion"), Key.key("lilac"),
                Key.key("lily_of_the_valley"), Key.key("mangrove_propagule"),
                Key.key("open_eyeblossom"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("peony"), Key.key("pink_petals"), Key.key("pink_tulip"),
                Key.key("pitcher_plant"), Key.key("poppy"), Key.key("red_tulip"),
                Key.key("rose_bush"), Key.key("spore_blossom"), Key.key("sunflower"),
                Key.key("torchflower"), Key.key("white_tulip"), Key.key("wildflowers"),
                Key.key("wither_rose")));
        tags.put(Key.key("forest_rock_can_place_on"), List.of(Key.key("andesite"),
                Key.key("coarse_dirt"), Key.key("deepslate"), Key.key("diorite"), Key.key("dirt"),
                Key.key("granite"), Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("stone"), Key.key("tuff")));
        tags.put(Key.key("fox_immune_to"), List.of(Key.key("sweet_berry_bush")));
        tags.put(Key.key("foxes_spawnable_on"), List.of(Key.key("coarse_dirt"),
                Key.key("grass_block"), Key.key("podzol"), Key.key("snow"), Key.key("snow_block")));
        tags.put(Key.key("frog_prefer_jump_to"), List.of(Key.key("big_dripleaf"),
                Key.key("lily_pad")));
        tags.put(Key.key("frogs_spawnable_on"), List.of(Key.key("grass_block"),
                Key.key("mangrove_roots"), Key.key("mud"), Key.key("muddy_mangrove_roots")));
        tags.put(Key.key("geode_invalid_blocks"), List.of(Key.key("bedrock"), Key.key("blue_ice"),
                Key.key("ice"), Key.key("lava"), Key.key("packed_ice"), Key.key("water")));
    }

    private static void blockTags16(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("glazed_terracotta"), List.of(Key.key("black_glazed_terracotta"),
                Key.key("blue_glazed_terracotta"), Key.key("brown_glazed_terracotta"),
                Key.key("cyan_glazed_terracotta"), Key.key("gray_glazed_terracotta"),
                Key.key("green_glazed_terracotta"), Key.key("light_blue_glazed_terracotta"),
                Key.key("light_gray_glazed_terracotta"), Key.key("lime_glazed_terracotta"),
                Key.key("magenta_glazed_terracotta"), Key.key("orange_glazed_terracotta"),
                Key.key("pink_glazed_terracotta"), Key.key("purple_glazed_terracotta"),
                Key.key("red_glazed_terracotta"), Key.key("white_glazed_terracotta"),
                Key.key("yellow_glazed_terracotta")));
        tags.put(Key.key("goats_spawnable_on"), List.of(Key.key("grass_block"), Key.key("gravel"),
                Key.key("packed_ice"), Key.key("snow"), Key.key("snow_block"), Key.key("stone")));
        tags.put(Key.key("gold_ores"), List.of(Key.key("deepslate_gold_ore"), Key.key("gold_ore"),
                Key.key("nether_gold_ore")));
        tags.put(Key.key("grass_blocks"), List.of(Key.key("grass_block"), Key.key("mycelium"),
                Key.key("podzol")));
        tags.put(Key.key("grows_crops"), List.of(Key.key("farmland")));
        tags.put(Key.key("guarded_by_piglins"), List.of(Key.key("barrel"),
                Key.key("black_shulker_box"), Key.key("blue_shulker_box"),
                Key.key("brown_shulker_box"), Key.key("chest"), Key.key("copper_chest"),
                Key.key("cyan_shulker_box"), Key.key("deepslate_gold_ore"), Key.key("ender_chest"),
                Key.key("exposed_copper_chest"), Key.key("gilded_blackstone"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("gray_shulker_box"),
                Key.key("green_shulker_box"), Key.key("light_blue_shulker_box"),
                Key.key("light_gray_shulker_box"), Key.key("lime_shulker_box"),
                Key.key("magenta_shulker_box"), Key.key("nether_gold_ore"),
                Key.key("orange_shulker_box"), Key.key("oxidized_copper_chest"),
                Key.key("pink_shulker_box"), Key.key("purple_shulker_box"),
                Key.key("raw_gold_block"), Key.key("red_shulker_box"), Key.key("shulker_box"),
                Key.key("trapped_chest"), Key.key("waxed_copper_chest"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_oxidized_copper_chest"),
                Key.key("waxed_weathered_copper_chest"), Key.key("weathered_copper_chest"),
                Key.key("white_shulker_box"), Key.key("yellow_shulker_box")));
        tags.put(Key.key("happy_ghast_avoids"), List.of(Key.key("cactus"), Key.key("fire"),
                Key.key("magma_block"), Key.key("pointed_dripstone"), Key.key("sulfur_spike"),
                Key.key("sweet_berry_bush"), Key.key("wither_rose")));
        tags.put(Key.key("height_specific_ore_replaceables"), List.of(Key.key("tuff")));
        tags.put(Key.key("hoglin_repellents"), List.of(Key.key("nether_portal"),
                Key.key("potted_warped_fungus"), Key.key("respawn_anchor"),
                Key.key("warped_fungus")));
        tags.put(Key.key("huge_brown_mushroom_can_place_on"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("grass_block"),
                Key.key("moss_block"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("mycelium"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("rooted_dirt"), Key.key("warped_nylium")));
        tags.put(Key.key("huge_red_mushroom_can_place_on"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("grass_block"),
                Key.key("moss_block"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("mycelium"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("rooted_dirt"), Key.key("warped_nylium")));
        tags.put(Key.key("ice"), List.of(Key.key("blue_ice"), Key.key("frosted_ice"),
                Key.key("ice"), Key.key("packed_ice")));
        tags.put(Key.key("ice_melts_when_destroyed_above"), List.of(Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_leaves"), Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wood"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("azalea_leaves"),
                Key.key("bamboo"), Key.key("bamboo_block"), Key.key("bamboo_door"),
                Key.key("bamboo_fence"), Key.key("bamboo_fence_gate"),
                Key.key("bamboo_hanging_sign"), Key.key("bamboo_mosaic"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_mosaic_stairs"),
                Key.key("bamboo_planks"), Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("barrel"), Key.key("barrier"),
                Key.key("basalt"), Key.key("beacon"), Key.key("bedrock"), Key.key("bee_nest"),
                Key.key("beehive"), Key.key("bell"), Key.key("birch_door"), Key.key("birch_fence"),
                Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"), Key.key("birch_leaves"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_slab"), Key.key("birch_stairs"),
                Key.key("birch_trapdoor"), Key.key("birch_wall_hanging_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_bed"),
                Key.key("black_candle_cake"), Key.key("black_concrete"),
                Key.key("black_concrete_powder"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_stained_glass"),
                Key.key("black_stained_glass_pane"), Key.key("black_terracotta"),
                Key.key("black_wall_banner"), Key.key("black_wool"), Key.key("black_wool_slab"),
                Key.key("black_wool_stairs"), Key.key("blackstone"), Key.key("blackstone_slab"),
                Key.key("blackstone_stairs"), Key.key("blackstone_wall"), Key.key("blast_furnace"),
                Key.key("blue_banner"), Key.key("blue_bed"), Key.key("blue_candle_cake"),
                Key.key("blue_concrete"), Key.key("blue_concrete_powder"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_stained_glass"), Key.key("blue_stained_glass_pane"),
                Key.key("blue_terracotta"), Key.key("blue_wall_banner"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("bone_block"),
                Key.key("bookshelf"), Key.key("brain_coral_block"), Key.key("brewing_stand"),
                Key.key("brick_slab"), Key.key("brick_stairs"), Key.key("brick_wall"),
                Key.key("bricks"), Key.key("brown_banner"), Key.key("brown_bed"),
                Key.key("brown_candle_cake"), Key.key("brown_concrete"),
                Key.key("brown_concrete_powder"), Key.key("brown_concrete_slab"),
                Key.key("brown_concrete_stairs"), Key.key("brown_glazed_terracotta"),
                Key.key("brown_mushroom_block"), Key.key("brown_shulker_box"),
                Key.key("brown_stained_glass"), Key.key("brown_stained_glass_pane"),
                Key.key("brown_terracotta"), Key.key("brown_wall_banner"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"),
                Key.key("bubble_coral_block"), Key.key("budding_amethyst"), Key.key("cactus"),
                Key.key("cake"), Key.key("calcite"), Key.key("calibrated_sculk_sensor"),
                Key.key("campfire"), Key.key("candle_cake"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cauldron"), Key.key("chain_command_block"),
                Key.key("cherry_door"), Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wood"), Key.key("chest"),
                Key.key("chipped_anvil"), Key.key("chiseled_bookshelf"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("clay"), Key.key("coal_block"), Key.key("coal_ore"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobbled_deepslate_stairs"), Key.key("cobbled_deepslate_wall"),
                Key.key("cobblestone"), Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("command_block"), Key.key("composter"),
                Key.key("conduit"), Key.key("copper_bars"), Key.key("copper_block"),
                Key.key("copper_bulb"), Key.key("copper_chain"), Key.key("copper_chest"),
                Key.key("copper_door"), Key.key("copper_grate"), Key.key("copper_lantern"),
                Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crafter"), Key.key("crafting_table"),
                Key.key("creaking_heart"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_slab"), Key.key("crimson_stairs"), Key.key("crimson_stem"),
                Key.key("crimson_trapdoor"), Key.key("crimson_wall_hanging_sign"),
                Key.key("crying_obsidian"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("cut_red_sandstone"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone"),
                Key.key("cut_sandstone_slab"), Key.key("cyan_banner"), Key.key("cyan_bed"),
                Key.key("cyan_candle_cake"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_concrete_slab"),
                Key.key("cyan_concrete_stairs"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_shulker_box"), Key.key("cyan_stained_glass"),
                Key.key("cyan_stained_glass_pane"), Key.key("cyan_terracotta"),
                Key.key("cyan_wall_banner"), Key.key("cyan_wool"), Key.key("cyan_wool_slab"),
                Key.key("cyan_wool_stairs"), Key.key("damaged_anvil"), Key.key("dark_oak_door"),
                Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_planks"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_shelf"),
                Key.key("dark_oak_slab"), Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wood"),
                Key.key("dark_prismarine"), Key.key("dark_prismarine_slab"),
                Key.key("dark_prismarine_stairs"), Key.key("daylight_detector"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("decorated_pot"), Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("diorite_slab"), Key.key("diorite_stairs"),
                Key.key("diorite_wall"), Key.key("dirt"), Key.key("dirt_path"),
                Key.key("dispenser"), Key.key("dragon_egg"), Key.key("dried_ghast"),
                Key.key("dried_kelp_block"), Key.key("dripstone_block"), Key.key("dropper"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("enchanting_table"),
                Key.key("end_portal_frame"), Key.key("end_stone"), Key.key("end_stone_brick_slab"),
                Key.key("end_stone_brick_stairs"), Key.key("end_stone_brick_wall"),
                Key.key("end_stone_bricks"), Key.key("ender_chest"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bars"), Key.key("exposed_copper_bulb"),
                Key.key("exposed_copper_chain"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_door"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("farmland"), Key.key("fire_coral_block"), Key.key("fletching_table"),
                Key.key("flowering_azalea_leaves"), Key.key("frosted_ice"), Key.key("furnace"),
                Key.key("gilded_blackstone"), Key.key("glass"), Key.key("glass_pane"),
                Key.key("glowstone"), Key.key("gold_block"), Key.key("gold_ore"),
                Key.key("granite"), Key.key("granite_slab"), Key.key("granite_stairs"),
                Key.key("granite_wall"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_banner"), Key.key("gray_bed"), Key.key("gray_candle_cake"),
                Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_concrete_slab"), Key.key("gray_concrete_stairs"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_shulker_box"),
                Key.key("gray_stained_glass"), Key.key("gray_stained_glass_pane"),
                Key.key("gray_terracotta"), Key.key("gray_wall_banner"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_banner"),
                Key.key("green_bed"), Key.key("green_candle_cake"), Key.key("green_concrete"),
                Key.key("green_concrete_powder"), Key.key("green_concrete_slab"),
                Key.key("green_concrete_stairs"), Key.key("green_glazed_terracotta"),
                Key.key("green_shulker_box"), Key.key("green_stained_glass"),
                Key.key("green_stained_glass_pane"), Key.key("green_terracotta"),
                Key.key("green_wall_banner"), Key.key("green_wool"), Key.key("green_wool_slab"),
                Key.key("green_wool_stairs"), Key.key("grindstone"), Key.key("hay_block"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("honey_block"),
                Key.key("honeycomb_block"), Key.key("hopper"), Key.key("horn_coral_block"),
                Key.key("ice"), Key.key("infested_chiseled_stone_bricks"),
                Key.key("infested_cobblestone"), Key.key("infested_cracked_stone_bricks"),
                Key.key("infested_deepslate"), Key.key("infested_mossy_stone_bricks"),
                Key.key("infested_stone"), Key.key("infested_stone_bricks"), Key.key("iron_bars"),
                Key.key("iron_block"), Key.key("iron_chain"), Key.key("iron_door"),
                Key.key("iron_ore"), Key.key("iron_trapdoor"), Key.key("jack_o_lantern"),
                Key.key("jigsaw"), Key.key("jukebox"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wood"), Key.key("lantern"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("large_amethyst_bud"),
                Key.key("lava_cauldron"), Key.key("lectern"), Key.key("light_blue_banner"),
                Key.key("light_blue_bed"), Key.key("light_blue_candle_cake"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_shulker_box"),
                Key.key("light_blue_stained_glass"), Key.key("light_blue_stained_glass_pane"),
                Key.key("light_blue_terracotta"), Key.key("light_blue_wall_banner"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_banner"),
                Key.key("light_gray_bed"), Key.key("light_gray_candle_cake"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_powder"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_concrete_stairs"),
                Key.key("light_gray_glazed_terracotta"), Key.key("light_gray_shulker_box"),
                Key.key("light_gray_stained_glass"), Key.key("light_gray_stained_glass_pane"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wall_banner"),
                Key.key("light_gray_wool"), Key.key("light_gray_wool_slab"),
                Key.key("light_gray_wool_stairs"), Key.key("light_weighted_pressure_plate"),
                Key.key("lightning_rod"), Key.key("lime_banner"), Key.key("lime_bed"),
                Key.key("lime_candle_cake"), Key.key("lime_concrete"),
                Key.key("lime_concrete_powder"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_stained_glass"),
                Key.key("lime_stained_glass_pane"), Key.key("lime_terracotta"),
                Key.key("lime_wall_banner"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("lodestone"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_bed"), Key.key("magenta_candle_cake"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_concrete_stairs"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_shulker_box"),
                Key.key("magenta_stained_glass"), Key.key("magenta_stained_glass_pane"),
                Key.key("magenta_terracotta"), Key.key("magenta_wall_banner"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("magma_block"), Key.key("mangrove_door"),
                Key.key("mangrove_fence"), Key.key("mangrove_fence_gate"),
                Key.key("mangrove_hanging_sign"), Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_slab"), Key.key("mangrove_stairs"),
                Key.key("mangrove_trapdoor"), Key.key("mangrove_wall_hanging_sign"),
                Key.key("mangrove_wood"), Key.key("medium_amethyst_bud"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"),
                Key.key("mossy_cobblestone_slab"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_slab"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mossy_stone_brick_wall"),
                Key.key("mossy_stone_bricks"), Key.key("moving_piston"), Key.key("mud"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"), Key.key("mushroom_stem"),
                Key.key("mycelium"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("note_block"), Key.key("oak_door"), Key.key("oak_fence"),
                Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_slab"), Key.key("oak_stairs"),
                Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"), Key.key("oak_wood"),
                Key.key("observer"), Key.key("obsidian"), Key.key("ochre_froglight"),
                Key.key("orange_banner"), Key.key("orange_bed"), Key.key("orange_candle_cake"),
                Key.key("orange_concrete"), Key.key("orange_concrete_powder"),
                Key.key("orange_concrete_slab"), Key.key("orange_concrete_stairs"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_poplar_leaves"),
                Key.key("orange_shulker_box"), Key.key("orange_stained_glass"),
                Key.key("orange_stained_glass_pane"), Key.key("orange_terracotta"),
                Key.key("orange_wall_banner"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bars"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chain"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_door"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("pale_moss_block"), Key.key("pale_oak_door"), Key.key("pale_oak_fence"),
                Key.key("pale_oak_fence_gate"), Key.key("pale_oak_hanging_sign"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_planks"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_shelf"),
                Key.key("pale_oak_slab"), Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("petrified_oak_slab"),
                Key.key("pink_banner"), Key.key("pink_bed"), Key.key("pink_candle_cake"),
                Key.key("pink_concrete"), Key.key("pink_concrete_powder"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_stained_glass"), Key.key("pink_stained_glass_pane"),
                Key.key("pink_terracotta"), Key.key("pink_wall_banner"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("piston"),
                Key.key("piston_head"), Key.key("podzol"), Key.key("pointed_dripstone"),
                Key.key("polished_andesite"), Key.key("polished_andesite_slab"),
                Key.key("polished_andesite_stairs"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("poplar_door"), Key.key("poplar_fence"), Key.key("poplar_fence_gate"),
                Key.key("poplar_hanging_sign"), Key.key("poplar_log"), Key.key("poplar_planks"),
                Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"), Key.key("poplar_slab"),
                Key.key("poplar_stairs"), Key.key("poplar_trapdoor"),
                Key.key("poplar_wall_hanging_sign"), Key.key("poplar_wood"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("prismarine"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_brick_stairs"),
                Key.key("prismarine_bricks"), Key.key("prismarine_slab"),
                Key.key("prismarine_stairs"), Key.key("prismarine_wall"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_bed"), Key.key("purple_candle_cake"),
                Key.key("purple_concrete"), Key.key("purple_concrete_powder"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_stained_glass"), Key.key("purple_stained_glass_pane"),
                Key.key("purple_terracotta"), Key.key("purple_wall_banner"), Key.key("purple_wool"),
                Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("purpur_slab"), Key.key("purpur_stairs"),
                Key.key("quartz_block"), Key.key("quartz_bricks"), Key.key("quartz_pillar"),
                Key.key("quartz_slab"), Key.key("quartz_stairs"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_banner"),
                Key.key("red_bed"), Key.key("red_candle_cake"), Key.key("red_concrete"),
                Key.key("red_concrete_powder"), Key.key("red_concrete_slab"),
                Key.key("red_concrete_stairs"), Key.key("red_glazed_terracotta"),
                Key.key("red_mushroom_block"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_poplar_leaves"), Key.key("red_sand"),
                Key.key("red_sandstone"), Key.key("red_sandstone_slab"),
                Key.key("red_sandstone_stairs"), Key.key("red_sandstone_wall"),
                Key.key("red_shulker_box"), Key.key("red_stained_glass"),
                Key.key("red_stained_glass_pane"), Key.key("red_terracotta"),
                Key.key("red_wall_banner"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("redstone_block"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("resin_block"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("sandstone_slab"), Key.key("sandstone_stairs"), Key.key("sandstone_wall"),
                Key.key("sculk"), Key.key("sculk_catalyst"), Key.key("sculk_sensor"),
                Key.key("sculk_shrieker"), Key.key("sculk_vein"), Key.key("sea_lantern"),
                Key.key("shroomlight"), Key.key("shulker_box"), Key.key("slime_block"),
                Key.key("small_amethyst_bud"), Key.key("smithing_table"), Key.key("smoker"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_quartz_slab"),
                Key.key("smooth_quartz_stairs"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_red_sandstone_slab"), Key.key("smooth_red_sandstone_stairs"),
                Key.key("smooth_sandstone"), Key.key("smooth_sandstone_slab"),
                Key.key("smooth_sandstone_stairs"), Key.key("smooth_stone"),
                Key.key("smooth_stone_slab"), Key.key("sniffer_egg"), Key.key("snow_block"),
                Key.key("soul_campfire"), Key.key("soul_lantern"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("spawner"), Key.key("sponge"), Key.key("spruce_door"),
                Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_leaves"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wood"),
                Key.key("sticky_piston"), Key.key("stone"), Key.key("stone_brick_slab"),
                Key.key("stone_brick_stairs"), Key.key("stone_brick_wall"), Key.key("stone_bricks"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("straw_bed"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("structure_block"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("suspicious_gravel"),
                Key.key("suspicious_sand"), Key.key("target"), Key.key("terracotta"),
                Key.key("test_block"), Key.key("test_instance_block"), Key.key("tinted_glass"),
                Key.key("tnt"), Key.key("trapped_chest"), Key.key("trial_spawner"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("turtle_egg"), Key.key("vault"), Key.key("verdant_froglight"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_pressure_plate"), Key.key("warped_shelf"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wart_block"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bars"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chain"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_grate"), Key.key("weathered_copper_lantern"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod"), Key.key("wet_sponge"), Key.key("white_banner"),
                Key.key("white_bed"), Key.key("white_candle_cake"), Key.key("white_concrete"),
                Key.key("white_concrete_powder"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_stained_glass"),
                Key.key("white_stained_glass_pane"), Key.key("white_terracotta"),
                Key.key("white_wall_banner"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_banner"), Key.key("yellow_bed"),
                Key.key("yellow_candle_cake"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_poplar_leaves"), Key.key("yellow_shulker_box"),
                Key.key("yellow_stained_glass"), Key.key("yellow_stained_glass_pane"),
                Key.key("yellow_terracotta"), Key.key("yellow_wall_banner"), Key.key("yellow_wool"),
                Key.key("yellow_wool_slab"), Key.key("yellow_wool_stairs")));
    }

    private static void blockTags17(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("ice_spike_replaceable"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("grass_block"), Key.key("ice"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("snow_block")));
        tags.put(Key.key("impermeable"), List.of(Key.key("barrier"), Key.key("black_stained_glass"),
                Key.key("blue_stained_glass"), Key.key("brown_stained_glass"),
                Key.key("cyan_stained_glass"), Key.key("glass"), Key.key("gray_stained_glass"),
                Key.key("green_stained_glass"), Key.key("light_blue_stained_glass"),
                Key.key("light_gray_stained_glass"), Key.key("lime_stained_glass"),
                Key.key("magenta_stained_glass"), Key.key("orange_stained_glass"),
                Key.key("pink_stained_glass"), Key.key("purple_stained_glass"),
                Key.key("red_stained_glass"), Key.key("tinted_glass"),
                Key.key("white_stained_glass"), Key.key("yellow_stained_glass")));
        tags.put(Key.key("incorrect_for_copper_tool"), List.of(Key.key("ancient_debris"),
                Key.key("crying_obsidian"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("gold_block"),
                Key.key("gold_ore"), Key.key("netherite_block"), Key.key("obsidian"),
                Key.key("raw_gold_block"), Key.key("redstone_ore"), Key.key("respawn_anchor")));
        tags.put(Key.key("incorrect_for_diamond_tool"), List.<Key>of());
        tags.put(Key.key("incorrect_for_gold_tool"), List.of(Key.key("ancient_debris"),
                Key.key("chiseled_copper"), Key.key("copper_block"), Key.key("copper_bulb"),
                Key.key("copper_chest"), Key.key("copper_grate"), Key.key("copper_ore"),
                Key.key("copper_trapdoor"), Key.key("crafter"), Key.key("crying_obsidian"),
                Key.key("cut_copper"), Key.key("cut_copper_slab"), Key.key("cut_copper_stairs"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("emerald_block"), Key.key("emerald_ore"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_grate"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("iron_block"),
                Key.key("iron_ore"), Key.key("lapis_block"), Key.key("lapis_ore"),
                Key.key("lightning_rod"), Key.key("netherite_block"), Key.key("obsidian"),
                Key.key("oxidized_chiseled_copper"), Key.key("oxidized_copper"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chest"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_trapdoor"),
                Key.key("oxidized_cut_copper"), Key.key("oxidized_cut_copper_slab"),
                Key.key("oxidized_cut_copper_stairs"), Key.key("oxidized_lightning_rod"),
                Key.key("raw_copper_block"), Key.key("raw_gold_block"), Key.key("raw_iron_block"),
                Key.key("redstone_ore"), Key.key("respawn_anchor"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_block"),
                Key.key("waxed_copper_bulb"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_grate"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bulb"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_grate"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bulb"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_grate"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bulb"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_grate"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bulb"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_grate"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod")));
    }

    private static void blockTags18(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("incorrect_for_iron_tool"), List.of(Key.key("ancient_debris"),
                Key.key("crying_obsidian"), Key.key("netherite_block"), Key.key("obsidian"),
                Key.key("respawn_anchor")));
        tags.put(Key.key("incorrect_for_netherite_tool"), List.<Key>of());
        tags.put(Key.key("incorrect_for_stone_tool"), List.of(Key.key("ancient_debris"),
                Key.key("crying_obsidian"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("gold_block"),
                Key.key("gold_ore"), Key.key("netherite_block"), Key.key("obsidian"),
                Key.key("raw_gold_block"), Key.key("redstone_ore"), Key.key("respawn_anchor")));
        tags.put(Key.key("incorrect_for_wooden_tool"), List.of(Key.key("ancient_debris"),
                Key.key("chiseled_copper"), Key.key("copper_block"), Key.key("copper_bulb"),
                Key.key("copper_chest"), Key.key("copper_grate"), Key.key("copper_ore"),
                Key.key("copper_trapdoor"), Key.key("crafter"), Key.key("crying_obsidian"),
                Key.key("cut_copper"), Key.key("cut_copper_slab"), Key.key("cut_copper_stairs"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("emerald_block"), Key.key("emerald_ore"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_grate"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("iron_block"),
                Key.key("iron_ore"), Key.key("lapis_block"), Key.key("lapis_ore"),
                Key.key("lightning_rod"), Key.key("netherite_block"), Key.key("obsidian"),
                Key.key("oxidized_chiseled_copper"), Key.key("oxidized_copper"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_copper_chest"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_trapdoor"),
                Key.key("oxidized_cut_copper"), Key.key("oxidized_cut_copper_slab"),
                Key.key("oxidized_cut_copper_stairs"), Key.key("oxidized_lightning_rod"),
                Key.key("raw_copper_block"), Key.key("raw_gold_block"), Key.key("raw_iron_block"),
                Key.key("redstone_ore"), Key.key("respawn_anchor"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_block"),
                Key.key("waxed_copper_bulb"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_grate"), Key.key("waxed_copper_trapdoor"),
                Key.key("waxed_cut_copper"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_chiseled_copper"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_exposed_copper_bulb"),
                Key.key("waxed_exposed_copper_chest"), Key.key("waxed_exposed_copper_grate"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bulb"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_grate"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bulb"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_grate"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bulb"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_grate"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod")));
        tags.put(Key.key("infiniburn_end"), List.of(Key.key("bedrock"), Key.key("magma_block"),
                Key.key("netherrack")));
        tags.put(Key.key("infiniburn_nether"), List.of(Key.key("magma_block"),
                Key.key("netherrack")));
        tags.put(Key.key("infiniburn_overworld"), List.of(Key.key("magma_block"),
                Key.key("netherrack")));
        tags.put(Key.key("inside_step_sound_blocks"), List.of(Key.key("glow_lichen"),
                Key.key("leaf_litter"), Key.key("lily_pad"), Key.key("pink_petals"),
                Key.key("powder_snow"), Key.key("sculk_vein"), Key.key("small_amethyst_bud"),
                Key.key("wildflowers")));
        tags.put(Key.key("invalid_spawn_inside"), List.of(Key.key("end_gateway"),
                Key.key("end_portal")));
        tags.put(Key.key("iron_ores"), List.of(Key.key("deepslate_iron_ore"), Key.key("iron_ore")));
        tags.put(Key.key("jungle_logs"), List.of(Key.key("jungle_log"), Key.key("jungle_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood")));
    }

    private static void blockTags19(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("lanterns"), List.of(Key.key("copper_lantern"),
                Key.key("exposed_copper_lantern"), Key.key("lantern"),
                Key.key("oxidized_copper_lantern"), Key.key("soul_lantern"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_oxidized_copper_lantern"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("weathered_copper_lantern")));
        tags.put(Key.key("lapis_ores"), List.of(Key.key("deepslate_lapis_ore"),
                Key.key("lapis_ore")));
        tags.put(Key.key("lava_pool_stone_cannot_replace"), List.of(Key.key("acacia_leaves"),
                Key.key("acacia_log"), Key.key("acacia_wood"), Key.key("azalea_leaves"),
                Key.key("bedrock"), Key.key("birch_leaves"), Key.key("birch_log"),
                Key.key("birch_wood"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_wood"), Key.key("chest"), Key.key("crimson_hyphae"),
                Key.key("crimson_stem"), Key.key("dark_oak_leaves"), Key.key("dark_oak_log"),
                Key.key("dark_oak_wood"), Key.key("end_portal_frame"),
                Key.key("flowering_azalea_leaves"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_wood"), Key.key("mangrove_leaves"), Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("oak_leaves"), Key.key("oak_log"),
                Key.key("oak_wood"), Key.key("orange_poplar_leaves"), Key.key("pale_oak_leaves"),
                Key.key("pale_oak_log"), Key.key("pale_oak_wood"), Key.key("poplar_log"),
                Key.key("poplar_wood"), Key.key("red_poplar_leaves"),
                Key.key("reinforced_deepslate"), Key.key("spawner"), Key.key("spruce_leaves"),
                Key.key("spruce_log"), Key.key("spruce_wood"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_birch_log"),
                Key.key("stripped_birch_wood"), Key.key("stripped_cherry_log"),
                Key.key("stripped_cherry_wood"), Key.key("stripped_crimson_hyphae"),
                Key.key("stripped_crimson_stem"), Key.key("stripped_dark_oak_log"),
                Key.key("stripped_dark_oak_wood"), Key.key("stripped_jungle_log"),
                Key.key("stripped_jungle_wood"), Key.key("stripped_mangrove_log"),
                Key.key("stripped_mangrove_wood"), Key.key("stripped_oak_log"),
                Key.key("stripped_oak_wood"), Key.key("stripped_pale_oak_log"),
                Key.key("stripped_pale_oak_wood"), Key.key("stripped_poplar_log"),
                Key.key("stripped_poplar_wood"), Key.key("stripped_spruce_log"),
                Key.key("stripped_spruce_wood"), Key.key("stripped_warped_hyphae"),
                Key.key("stripped_warped_stem"), Key.key("trial_spawner"), Key.key("vault"),
                Key.key("warped_hyphae"), Key.key("warped_stem"), Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("leaves"), List.of(Key.key("acacia_leaves"), Key.key("azalea_leaves"),
                Key.key("birch_leaves"), Key.key("cherry_leaves"), Key.key("dark_oak_leaves"),
                Key.key("flowering_azalea_leaves"), Key.key("jungle_leaves"),
                Key.key("mangrove_leaves"), Key.key("oak_leaves"), Key.key("orange_poplar_leaves"),
                Key.key("pale_oak_leaves"), Key.key("red_poplar_leaves"), Key.key("spruce_leaves"),
                Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("lightning_rods"), List.of(Key.key("exposed_lightning_rod"),
                Key.key("lightning_rod"), Key.key("oxidized_lightning_rod"),
                Key.key("waxed_exposed_lightning_rod"), Key.key("waxed_lightning_rod"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_lightning_rod"),
                Key.key("weathered_lightning_rod")));
        tags.put(Key.key("logs"), List.of(Key.key("acacia_log"), Key.key("acacia_wood"),
                Key.key("birch_log"), Key.key("birch_wood"), Key.key("cherry_log"),
                Key.key("cherry_wood"), Key.key("crimson_hyphae"), Key.key("crimson_stem"),
                Key.key("dark_oak_log"), Key.key("dark_oak_wood"), Key.key("jungle_log"),
                Key.key("jungle_wood"), Key.key("mangrove_log"), Key.key("mangrove_wood"),
                Key.key("oak_log"), Key.key("oak_wood"), Key.key("pale_oak_log"),
                Key.key("pale_oak_wood"), Key.key("poplar_log"), Key.key("poplar_wood"),
                Key.key("spruce_log"), Key.key("spruce_wood"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_birch_log"),
                Key.key("stripped_birch_wood"), Key.key("stripped_cherry_log"),
                Key.key("stripped_cherry_wood"), Key.key("stripped_crimson_hyphae"),
                Key.key("stripped_crimson_stem"), Key.key("stripped_dark_oak_log"),
                Key.key("stripped_dark_oak_wood"), Key.key("stripped_jungle_log"),
                Key.key("stripped_jungle_wood"), Key.key("stripped_mangrove_log"),
                Key.key("stripped_mangrove_wood"), Key.key("stripped_oak_log"),
                Key.key("stripped_oak_wood"), Key.key("stripped_pale_oak_log"),
                Key.key("stripped_pale_oak_wood"), Key.key("stripped_poplar_log"),
                Key.key("stripped_poplar_wood"), Key.key("stripped_spruce_log"),
                Key.key("stripped_spruce_wood"), Key.key("stripped_warped_hyphae"),
                Key.key("stripped_warped_stem"), Key.key("warped_hyphae"), Key.key("warped_stem")));
    }

    private static void blockTags20(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("logs_that_burn"), List.of(Key.key("acacia_log"), Key.key("acacia_wood"),
                Key.key("birch_log"), Key.key("birch_wood"), Key.key("cherry_log"),
                Key.key("cherry_wood"), Key.key("dark_oak_log"), Key.key("dark_oak_wood"),
                Key.key("jungle_log"), Key.key("jungle_wood"), Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("oak_log"), Key.key("oak_wood"),
                Key.key("pale_oak_log"), Key.key("pale_oak_wood"), Key.key("poplar_log"),
                Key.key("poplar_wood"), Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood")));
        tags.put(Key.key("lush_ground_replaceable"), List.of(Key.key("andesite"),
                Key.key("cave_vines"), Key.key("cave_vines_plant"), Key.key("clay"),
                Key.key("coarse_dirt"), Key.key("deepslate"), Key.key("diorite"), Key.key("dirt"),
                Key.key("granite"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("moss_block"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("mycelium"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("stone"), Key.key("tuff")));
        tags.put(Key.key("maintains_farmland"), List.of(Key.key("acacia_fence_gate"),
                Key.key("attached_melon_stem"), Key.key("attached_pumpkin_stem"),
                Key.key("bamboo_fence_gate"), Key.key("beetroots"), Key.key("birch_fence_gate"),
                Key.key("carrots"), Key.key("cherry_fence_gate"), Key.key("crimson_fence_gate"),
                Key.key("dark_oak_fence_gate"), Key.key("jungle_fence_gate"),
                Key.key("mangrove_fence_gate"), Key.key("melon_stem"), Key.key("moving_piston"),
                Key.key("oak_fence_gate"), Key.key("pale_oak_fence_gate"), Key.key("pitcher_crop"),
                Key.key("poplar_fence_gate"), Key.key("potatoes"), Key.key("pumpkin_stem"),
                Key.key("spruce_fence_gate"), Key.key("torchflower"), Key.key("torchflower_crop"),
                Key.key("warped_fence_gate"), Key.key("wheat")));
        tags.put(Key.key("mangrove_logs"), List.of(Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("stripped_mangrove_log"),
                Key.key("stripped_mangrove_wood")));
        tags.put(Key.key("mangrove_logs_can_grow_through"), List.of(Key.key("mangrove_leaves"),
                Key.key("mangrove_log"), Key.key("mangrove_propagule"), Key.key("mangrove_roots"),
                Key.key("moss_carpet"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("vine")));
        tags.put(Key.key("mangrove_roots_can_grow_through"), List.of(Key.key("mangrove_propagule"),
                Key.key("mangrove_roots"), Key.key("moss_carpet"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("snow"), Key.key("vine")));
        tags.put(Key.key("mineable/axe"), List.of(Key.key("acacia_button"), Key.key("acacia_door"),
                Key.key("acacia_fence"), Key.key("acacia_fence_gate"),
                Key.key("acacia_hanging_sign"), Key.key("acacia_log"), Key.key("acacia_planks"),
                Key.key("acacia_pressure_plate"), Key.key("acacia_shelf"), Key.key("acacia_sign"),
                Key.key("acacia_slab"), Key.key("acacia_stairs"), Key.key("acacia_trapdoor"),
                Key.key("acacia_wall_hanging_sign"), Key.key("acacia_wall_sign"),
                Key.key("acacia_wood"), Key.key("bamboo"), Key.key("bamboo_block"),
                Key.key("bamboo_button"), Key.key("bamboo_door"), Key.key("bamboo_fence"),
                Key.key("bamboo_fence_gate"), Key.key("bamboo_hanging_sign"),
                Key.key("bamboo_mosaic"), Key.key("bamboo_mosaic_slab"),
                Key.key("bamboo_mosaic_stairs"), Key.key("bamboo_planks"),
                Key.key("bamboo_pressure_plate"), Key.key("bamboo_shelf"), Key.key("bamboo_sign"),
                Key.key("bamboo_slab"), Key.key("bamboo_stairs"), Key.key("bamboo_trapdoor"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("bamboo_wall_sign"), Key.key("barrel"),
                Key.key("bee_nest"), Key.key("beehive"), Key.key("big_dripleaf"),
                Key.key("big_dripleaf_stem"), Key.key("birch_button"), Key.key("birch_door"),
                Key.key("birch_fence"), Key.key("birch_fence_gate"), Key.key("birch_hanging_sign"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_pressure_plate"),
                Key.key("birch_shelf"), Key.key("birch_sign"), Key.key("birch_slab"),
                Key.key("birch_stairs"), Key.key("birch_trapdoor"),
                Key.key("birch_wall_hanging_sign"), Key.key("birch_wall_sign"),
                Key.key("birch_wood"), Key.key("black_banner"), Key.key("black_wall_banner"),
                Key.key("blue_banner"), Key.key("blue_wall_banner"), Key.key("bookshelf"),
                Key.key("brown_banner"), Key.key("brown_mushroom_block"),
                Key.key("brown_wall_banner"), Key.key("campfire"), Key.key("cartography_table"),
                Key.key("carved_pumpkin"), Key.key("cherry_button"), Key.key("cherry_door"),
                Key.key("cherry_fence"), Key.key("cherry_fence_gate"),
                Key.key("cherry_hanging_sign"), Key.key("cherry_log"), Key.key("cherry_planks"),
                Key.key("cherry_pressure_plate"), Key.key("cherry_shelf"), Key.key("cherry_sign"),
                Key.key("cherry_slab"), Key.key("cherry_stairs"), Key.key("cherry_trapdoor"),
                Key.key("cherry_wall_hanging_sign"), Key.key("cherry_wall_sign"),
                Key.key("cherry_wood"), Key.key("chest"), Key.key("chiseled_bookshelf"),
                Key.key("chorus_flower"), Key.key("chorus_plant"), Key.key("cocoa"),
                Key.key("composter"), Key.key("crafting_table"), Key.key("creaking_heart"),
                Key.key("crimson_button"), Key.key("crimson_door"), Key.key("crimson_fence"),
                Key.key("crimson_fence_gate"), Key.key("crimson_hanging_sign"),
                Key.key("crimson_hyphae"), Key.key("crimson_planks"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_shelf"),
                Key.key("crimson_sign"), Key.key("crimson_slab"), Key.key("crimson_stairs"),
                Key.key("crimson_stem"), Key.key("crimson_trapdoor"),
                Key.key("crimson_wall_hanging_sign"), Key.key("crimson_wall_sign"),
                Key.key("cyan_banner"), Key.key("cyan_wall_banner"), Key.key("dark_oak_button"),
                Key.key("dark_oak_door"), Key.key("dark_oak_fence"), Key.key("dark_oak_fence_gate"),
                Key.key("dark_oak_hanging_sign"), Key.key("dark_oak_log"),
                Key.key("dark_oak_planks"), Key.key("dark_oak_pressure_plate"),
                Key.key("dark_oak_shelf"), Key.key("dark_oak_sign"), Key.key("dark_oak_slab"),
                Key.key("dark_oak_stairs"), Key.key("dark_oak_trapdoor"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("dark_oak_wall_sign"),
                Key.key("dark_oak_wood"), Key.key("daylight_detector"), Key.key("fletching_table"),
                Key.key("glow_lichen"), Key.key("gray_banner"), Key.key("gray_wall_banner"),
                Key.key("green_banner"), Key.key("green_wall_banner"), Key.key("jack_o_lantern"),
                Key.key("jukebox"), Key.key("jungle_button"), Key.key("jungle_door"),
                Key.key("jungle_fence"), Key.key("jungle_fence_gate"),
                Key.key("jungle_hanging_sign"), Key.key("jungle_log"), Key.key("jungle_planks"),
                Key.key("jungle_pressure_plate"), Key.key("jungle_shelf"), Key.key("jungle_sign"),
                Key.key("jungle_slab"), Key.key("jungle_stairs"), Key.key("jungle_trapdoor"),
                Key.key("jungle_wall_hanging_sign"), Key.key("jungle_wall_sign"),
                Key.key("jungle_wood"), Key.key("ladder"), Key.key("lectern"),
                Key.key("light_blue_banner"), Key.key("light_blue_wall_banner"),
                Key.key("light_gray_banner"), Key.key("light_gray_wall_banner"),
                Key.key("lime_banner"), Key.key("lime_wall_banner"), Key.key("loom"),
                Key.key("magenta_banner"), Key.key("magenta_wall_banner"),
                Key.key("mangrove_button"), Key.key("mangrove_door"), Key.key("mangrove_fence"),
                Key.key("mangrove_fence_gate"), Key.key("mangrove_hanging_sign"),
                Key.key("mangrove_log"), Key.key("mangrove_planks"),
                Key.key("mangrove_pressure_plate"), Key.key("mangrove_roots"),
                Key.key("mangrove_shelf"), Key.key("mangrove_sign"), Key.key("mangrove_slab"),
                Key.key("mangrove_stairs"), Key.key("mangrove_trapdoor"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("mangrove_wall_sign"),
                Key.key("mangrove_wood"), Key.key("melon"), Key.key("mushroom_stem"),
                Key.key("note_block"), Key.key("oak_button"), Key.key("oak_door"),
                Key.key("oak_fence"), Key.key("oak_fence_gate"), Key.key("oak_hanging_sign"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_pressure_plate"),
                Key.key("oak_shelf"), Key.key("oak_sign"), Key.key("oak_slab"),
                Key.key("oak_stairs"), Key.key("oak_trapdoor"), Key.key("oak_wall_hanging_sign"),
                Key.key("oak_wall_sign"), Key.key("oak_wood"), Key.key("orange_banner"),
                Key.key("orange_wall_banner"), Key.key("pale_oak_button"), Key.key("pale_oak_door"),
                Key.key("pale_oak_fence"), Key.key("pale_oak_fence_gate"),
                Key.key("pale_oak_hanging_sign"), Key.key("pale_oak_log"),
                Key.key("pale_oak_planks"), Key.key("pale_oak_pressure_plate"),
                Key.key("pale_oak_shelf"), Key.key("pale_oak_sign"), Key.key("pale_oak_slab"),
                Key.key("pale_oak_stairs"), Key.key("pale_oak_trapdoor"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("pale_oak_wall_sign"),
                Key.key("pale_oak_wood"), Key.key("pink_banner"), Key.key("pink_wall_banner"),
                Key.key("poplar_button"), Key.key("poplar_door"), Key.key("poplar_fence"),
                Key.key("poplar_fence_gate"), Key.key("poplar_hanging_sign"), Key.key("poplar_log"),
                Key.key("poplar_planks"), Key.key("poplar_pressure_plate"), Key.key("poplar_shelf"),
                Key.key("poplar_sign"), Key.key("poplar_slab"), Key.key("poplar_stairs"),
                Key.key("poplar_trapdoor"), Key.key("poplar_wall_hanging_sign"),
                Key.key("poplar_wall_sign"), Key.key("poplar_wood"), Key.key("pumpkin"),
                Key.key("purple_banner"), Key.key("purple_wall_banner"), Key.key("red_banner"),
                Key.key("red_mushroom_block"), Key.key("red_wall_banner"),
                Key.key("smithing_table"), Key.key("soul_campfire"), Key.key("spruce_button"),
                Key.key("spruce_door"), Key.key("spruce_fence"), Key.key("spruce_fence_gate"),
                Key.key("spruce_hanging_sign"), Key.key("spruce_log"), Key.key("spruce_planks"),
                Key.key("spruce_pressure_plate"), Key.key("spruce_shelf"), Key.key("spruce_sign"),
                Key.key("spruce_slab"), Key.key("spruce_stairs"), Key.key("spruce_trapdoor"),
                Key.key("spruce_wall_hanging_sign"), Key.key("spruce_wall_sign"),
                Key.key("spruce_wood"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("trapped_chest"), Key.key("vine"), Key.key("warped_button"),
                Key.key("warped_door"), Key.key("warped_fence"), Key.key("warped_fence_gate"),
                Key.key("warped_hanging_sign"), Key.key("warped_hyphae"), Key.key("warped_planks"),
                Key.key("warped_pressure_plate"), Key.key("warped_shelf"), Key.key("warped_sign"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor"), Key.key("warped_wall_hanging_sign"),
                Key.key("warped_wall_sign"), Key.key("white_banner"), Key.key("white_wall_banner"),
                Key.key("yellow_banner"), Key.key("yellow_wall_banner")));
    }

    private static void blockTags21(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("mineable/hoe"), List.of(Key.key("acacia_leaves"),
                Key.key("azalea_leaves"), Key.key("birch_leaves"),
                Key.key("calibrated_sculk_sensor"), Key.key("cherry_leaves"),
                Key.key("dark_oak_leaves"), Key.key("dried_kelp_block"),
                Key.key("flowering_azalea_leaves"), Key.key("hay_block"), Key.key("jungle_leaves"),
                Key.key("mangrove_leaves"), Key.key("moss_block"), Key.key("moss_carpet"),
                Key.key("nether_wart_block"), Key.key("oak_leaves"),
                Key.key("orange_poplar_leaves"), Key.key("pale_moss_block"),
                Key.key("pale_moss_carpet"), Key.key("pale_oak_leaves"),
                Key.key("red_poplar_leaves"), Key.key("sculk"), Key.key("sculk_catalyst"),
                Key.key("sculk_sensor"), Key.key("sculk_shrieker"), Key.key("sculk_vein"),
                Key.key("shroomlight"), Key.key("sponge"), Key.key("spruce_leaves"),
                Key.key("straw_bed"), Key.key("target"), Key.key("warped_wart_block"),
                Key.key("wet_sponge"), Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("mineable/pickaxe"), List.of(Key.key("activator_rail"),
                Key.key("amethyst_block"), Key.key("amethyst_cluster"), Key.key("ancient_debris"),
                Key.key("andesite"), Key.key("andesite_slab"), Key.key("andesite_stairs"),
                Key.key("andesite_wall"), Key.key("anvil"), Key.key("basalt"), Key.key("bell"),
                Key.key("black_concrete"), Key.key("black_concrete_slab"),
                Key.key("black_concrete_stairs"), Key.key("black_glazed_terracotta"),
                Key.key("black_shulker_box"), Key.key("black_terracotta"), Key.key("blackstone"),
                Key.key("blackstone_slab"), Key.key("blackstone_stairs"),
                Key.key("blackstone_wall"), Key.key("blast_furnace"), Key.key("blue_concrete"),
                Key.key("blue_concrete_slab"), Key.key("blue_concrete_stairs"),
                Key.key("blue_glazed_terracotta"), Key.key("blue_ice"), Key.key("blue_shulker_box"),
                Key.key("blue_terracotta"), Key.key("bone_block"), Key.key("brain_coral_block"),
                Key.key("brewing_stand"), Key.key("brick_slab"), Key.key("brick_stairs"),
                Key.key("brick_wall"), Key.key("bricks"), Key.key("brown_concrete"),
                Key.key("brown_concrete_slab"), Key.key("brown_concrete_stairs"),
                Key.key("brown_glazed_terracotta"), Key.key("brown_shulker_box"),
                Key.key("brown_terracotta"), Key.key("bubble_coral_block"),
                Key.key("budding_amethyst"), Key.key("calcite"), Key.key("cauldron"),
                Key.key("chipped_anvil"), Key.key("chiseled_cinnabar"), Key.key("chiseled_copper"),
                Key.key("chiseled_deepslate"), Key.key("chiseled_nether_bricks"),
                Key.key("chiseled_polished_blackstone"), Key.key("chiseled_quartz_block"),
                Key.key("chiseled_red_sandstone"), Key.key("chiseled_resin_bricks"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_brick_slab"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_brick_wall"), Key.key("cinnabar_bricks"),
                Key.key("cinnabar_slab"), Key.key("cinnabar_stairs"), Key.key("cinnabar_wall"),
                Key.key("coal_block"), Key.key("coal_ore"), Key.key("cobbled_deepslate"),
                Key.key("cobbled_deepslate_slab"), Key.key("cobbled_deepslate_stairs"),
                Key.key("cobbled_deepslate_wall"), Key.key("cobblestone"),
                Key.key("cobblestone_slab"), Key.key("cobblestone_stairs"),
                Key.key("cobblestone_wall"), Key.key("conduit"), Key.key("copper_bars"),
                Key.key("copper_block"), Key.key("copper_bulb"), Key.key("copper_chain"),
                Key.key("copper_chest"), Key.key("copper_door"), Key.key("copper_golem_statue"),
                Key.key("copper_grate"), Key.key("copper_lantern"), Key.key("copper_ore"),
                Key.key("copper_trapdoor"), Key.key("cracked_deepslate_bricks"),
                Key.key("cracked_deepslate_tiles"), Key.key("cracked_nether_bricks"),
                Key.key("cracked_polished_blackstone_bricks"), Key.key("cracked_stone_bricks"),
                Key.key("crafter"), Key.key("crimson_nylium"), Key.key("crying_obsidian"),
                Key.key("cut_copper"), Key.key("cut_copper_slab"), Key.key("cut_copper_stairs"),
                Key.key("cut_red_sandstone"), Key.key("cut_red_sandstone_slab"),
                Key.key("cut_sandstone"), Key.key("cut_sandstone_slab"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_slab"), Key.key("cyan_concrete_stairs"),
                Key.key("cyan_glazed_terracotta"), Key.key("cyan_shulker_box"),
                Key.key("cyan_terracotta"), Key.key("damaged_anvil"), Key.key("dark_prismarine"),
                Key.key("dark_prismarine_slab"), Key.key("dark_prismarine_stairs"),
                Key.key("dead_brain_coral"), Key.key("dead_brain_coral_block"),
                Key.key("dead_brain_coral_fan"), Key.key("dead_brain_coral_wall_fan"),
                Key.key("dead_bubble_coral"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_bubble_coral_fan"), Key.key("dead_bubble_coral_wall_fan"),
                Key.key("dead_fire_coral"), Key.key("dead_fire_coral_block"),
                Key.key("dead_fire_coral_fan"), Key.key("dead_fire_coral_wall_fan"),
                Key.key("dead_horn_coral"), Key.key("dead_horn_coral_block"),
                Key.key("dead_horn_coral_fan"), Key.key("dead_horn_coral_wall_fan"),
                Key.key("dead_tube_coral"), Key.key("dead_tube_coral_block"),
                Key.key("dead_tube_coral_fan"), Key.key("dead_tube_coral_wall_fan"),
                Key.key("deepslate"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_brick_stairs"), Key.key("deepslate_brick_wall"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_copper_ore"), Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("deepslate_tile_slab"),
                Key.key("deepslate_tile_stairs"), Key.key("deepslate_tile_wall"),
                Key.key("deepslate_tiles"), Key.key("detector_rail"), Key.key("diamond_block"),
                Key.key("diamond_ore"), Key.key("diorite"), Key.key("diorite_slab"),
                Key.key("diorite_stairs"), Key.key("diorite_wall"), Key.key("dispenser"),
                Key.key("dripstone_block"), Key.key("dropper"), Key.key("emerald_block"),
                Key.key("emerald_ore"), Key.key("enchanting_table"), Key.key("end_stone"),
                Key.key("end_stone_brick_slab"), Key.key("end_stone_brick_stairs"),
                Key.key("end_stone_brick_wall"), Key.key("end_stone_bricks"),
                Key.key("ender_chest"), Key.key("exposed_chiseled_copper"),
                Key.key("exposed_copper"), Key.key("exposed_copper_bars"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chain"),
                Key.key("exposed_copper_chest"), Key.key("exposed_copper_door"),
                Key.key("exposed_copper_golem_statue"), Key.key("exposed_copper_grate"),
                Key.key("exposed_copper_lantern"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("fire_coral_block"), Key.key("furnace"), Key.key("gilded_blackstone"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("granite"),
                Key.key("granite_slab"), Key.key("granite_stairs"), Key.key("granite_wall"),
                Key.key("gray_concrete"), Key.key("gray_concrete_slab"),
                Key.key("gray_concrete_stairs"), Key.key("gray_glazed_terracotta"),
                Key.key("gray_shulker_box"), Key.key("gray_terracotta"), Key.key("green_concrete"),
                Key.key("green_concrete_slab"), Key.key("green_concrete_stairs"),
                Key.key("green_glazed_terracotta"), Key.key("green_shulker_box"),
                Key.key("green_terracotta"), Key.key("grindstone"), Key.key("heavy_core"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("hopper"),
                Key.key("horn_coral_block"), Key.key("ice"),
                Key.key("infested_chiseled_stone_bricks"), Key.key("infested_cobblestone"),
                Key.key("infested_cracked_stone_bricks"), Key.key("infested_deepslate"),
                Key.key("infested_mossy_stone_bricks"), Key.key("infested_stone"),
                Key.key("infested_stone_bricks"), Key.key("iron_bars"), Key.key("iron_block"),
                Key.key("iron_chain"), Key.key("iron_door"), Key.key("iron_ore"),
                Key.key("iron_trapdoor"), Key.key("lantern"), Key.key("lapis_block"),
                Key.key("lapis_ore"), Key.key("large_amethyst_bud"), Key.key("lava_cauldron"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_slab"),
                Key.key("light_blue_concrete_stairs"), Key.key("light_blue_glazed_terracotta"),
                Key.key("light_blue_shulker_box"), Key.key("light_blue_terracotta"),
                Key.key("light_gray_concrete"), Key.key("light_gray_concrete_slab"),
                Key.key("light_gray_concrete_stairs"), Key.key("light_gray_glazed_terracotta"),
                Key.key("light_gray_shulker_box"), Key.key("light_gray_terracotta"),
                Key.key("light_weighted_pressure_plate"), Key.key("lightning_rod"),
                Key.key("lime_concrete"), Key.key("lime_concrete_slab"),
                Key.key("lime_concrete_stairs"), Key.key("lime_glazed_terracotta"),
                Key.key("lime_shulker_box"), Key.key("lime_terracotta"), Key.key("lodestone"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_slab"),
                Key.key("magenta_concrete_stairs"), Key.key("magenta_glazed_terracotta"),
                Key.key("magenta_shulker_box"), Key.key("magenta_terracotta"),
                Key.key("magma_block"), Key.key("medium_amethyst_bud"),
                Key.key("mossy_cobblestone"), Key.key("mossy_cobblestone_slab"),
                Key.key("mossy_cobblestone_stairs"), Key.key("mossy_cobblestone_wall"),
                Key.key("mossy_stone_brick_slab"), Key.key("mossy_stone_brick_stairs"),
                Key.key("mossy_stone_brick_wall"), Key.key("mossy_stone_bricks"),
                Key.key("mud_brick_slab"), Key.key("mud_brick_stairs"), Key.key("mud_brick_wall"),
                Key.key("mud_bricks"), Key.key("nether_brick_fence"), Key.key("nether_brick_slab"),
                Key.key("nether_brick_stairs"), Key.key("nether_brick_wall"),
                Key.key("nether_bricks"), Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("netherite_block"), Key.key("netherrack"), Key.key("observer"),
                Key.key("obsidian"), Key.key("orange_concrete"), Key.key("orange_concrete_slab"),
                Key.key("orange_concrete_stairs"), Key.key("orange_glazed_terracotta"),
                Key.key("orange_shulker_box"), Key.key("orange_terracotta"),
                Key.key("oxidized_chiseled_copper"), Key.key("oxidized_copper"),
                Key.key("oxidized_copper_bars"), Key.key("oxidized_copper_bulb"),
                Key.key("oxidized_copper_chain"), Key.key("oxidized_copper_chest"),
                Key.key("oxidized_copper_door"), Key.key("oxidized_copper_golem_statue"),
                Key.key("oxidized_copper_grate"), Key.key("oxidized_copper_lantern"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("packed_ice"), Key.key("packed_mud"),
                Key.key("petrified_oak_slab"), Key.key("pink_concrete"),
                Key.key("pink_concrete_slab"), Key.key("pink_concrete_stairs"),
                Key.key("pink_glazed_terracotta"), Key.key("pink_shulker_box"),
                Key.key("pink_terracotta"), Key.key("piston"), Key.key("piston_head"),
                Key.key("pointed_dripstone"), Key.key("polished_andesite"),
                Key.key("polished_andesite_slab"), Key.key("polished_andesite_stairs"),
                Key.key("polished_basalt"), Key.key("polished_blackstone"),
                Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_blackstone_button"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("polished_blackstone_slab"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_cinnabar_stairs"), Key.key("polished_cinnabar_wall"),
                Key.key("polished_deepslate"), Key.key("polished_deepslate_slab"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_deepslate_wall"),
                Key.key("polished_diorite"), Key.key("polished_diorite_slab"),
                Key.key("polished_diorite_stairs"), Key.key("polished_granite"),
                Key.key("polished_granite_slab"), Key.key("polished_granite_stairs"),
                Key.key("polished_sulfur"), Key.key("polished_sulfur_slab"),
                Key.key("polished_sulfur_stairs"), Key.key("polished_sulfur_wall"),
                Key.key("polished_tuff"), Key.key("polished_tuff_slab"),
                Key.key("polished_tuff_stairs"), Key.key("polished_tuff_wall"),
                Key.key("potent_sulfur"), Key.key("powder_snow_cauldron"), Key.key("powered_rail"),
                Key.key("prismarine"), Key.key("prismarine_brick_slab"),
                Key.key("prismarine_brick_stairs"), Key.key("prismarine_bricks"),
                Key.key("prismarine_slab"), Key.key("prismarine_stairs"),
                Key.key("prismarine_wall"), Key.key("purple_concrete"),
                Key.key("purple_concrete_slab"), Key.key("purple_concrete_stairs"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_shulker_box"),
                Key.key("purple_terracotta"), Key.key("purpur_block"), Key.key("purpur_pillar"),
                Key.key("purpur_slab"), Key.key("purpur_stairs"), Key.key("quartz_block"),
                Key.key("quartz_bricks"), Key.key("quartz_pillar"), Key.key("quartz_slab"),
                Key.key("quartz_stairs"), Key.key("rail"), Key.key("raw_copper_block"),
                Key.key("raw_gold_block"), Key.key("raw_iron_block"), Key.key("red_concrete"),
                Key.key("red_concrete_slab"), Key.key("red_concrete_stairs"),
                Key.key("red_glazed_terracotta"), Key.key("red_nether_brick_slab"),
                Key.key("red_nether_brick_stairs"), Key.key("red_nether_brick_wall"),
                Key.key("red_nether_bricks"), Key.key("red_sandstone"),
                Key.key("red_sandstone_slab"), Key.key("red_sandstone_stairs"),
                Key.key("red_sandstone_wall"), Key.key("red_shulker_box"),
                Key.key("red_terracotta"), Key.key("redstone_block"), Key.key("redstone_ore"),
                Key.key("resin_brick_slab"), Key.key("resin_brick_stairs"),
                Key.key("resin_brick_wall"), Key.key("resin_bricks"), Key.key("respawn_anchor"),
                Key.key("sandstone"), Key.key("sandstone_slab"), Key.key("sandstone_stairs"),
                Key.key("sandstone_wall"), Key.key("shulker_box"), Key.key("small_amethyst_bud"),
                Key.key("smoker"), Key.key("smooth_basalt"), Key.key("smooth_quartz"),
                Key.key("smooth_quartz_slab"), Key.key("smooth_quartz_stairs"),
                Key.key("smooth_red_sandstone"), Key.key("smooth_red_sandstone_slab"),
                Key.key("smooth_red_sandstone_stairs"), Key.key("smooth_sandstone"),
                Key.key("smooth_sandstone_slab"), Key.key("smooth_sandstone_stairs"),
                Key.key("smooth_stone"), Key.key("smooth_stone_slab"), Key.key("soul_lantern"),
                Key.key("spawner"), Key.key("sticky_piston"), Key.key("stone"),
                Key.key("stone_brick_slab"), Key.key("stone_brick_stairs"),
                Key.key("stone_brick_wall"), Key.key("stone_bricks"), Key.key("stone_button"),
                Key.key("stone_pressure_plate"), Key.key("stone_slab"), Key.key("stone_stairs"),
                Key.key("stonecutter"), Key.key("sulfur"), Key.key("sulfur_brick_slab"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_brick_wall"),
                Key.key("sulfur_bricks"), Key.key("sulfur_slab"), Key.key("sulfur_spike"),
                Key.key("sulfur_stairs"), Key.key("sulfur_wall"), Key.key("terracotta"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_brick_slab"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_brick_wall"), Key.key("tuff_bricks"),
                Key.key("tuff_slab"), Key.key("tuff_stairs"), Key.key("tuff_wall"),
                Key.key("warped_nylium"), Key.key("water_cauldron"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_bars"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chain"), Key.key("waxed_copper_chest"),
                Key.key("waxed_copper_door"), Key.key("waxed_copper_golem_statue"),
                Key.key("waxed_copper_grate"), Key.key("waxed_copper_lantern"),
                Key.key("waxed_copper_trapdoor"), Key.key("waxed_cut_copper"),
                Key.key("waxed_cut_copper_slab"), Key.key("waxed_cut_copper_stairs"),
                Key.key("waxed_exposed_chiseled_copper"), Key.key("waxed_exposed_copper"),
                Key.key("waxed_exposed_copper_bars"), Key.key("waxed_exposed_copper_bulb"),
                Key.key("waxed_exposed_copper_chain"), Key.key("waxed_exposed_copper_chest"),
                Key.key("waxed_exposed_copper_door"), Key.key("waxed_exposed_copper_golem_statue"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_exposed_copper_trapdoor"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_door"),
                Key.key("waxed_oxidized_copper_golem_statue"),
                Key.key("waxed_oxidized_copper_grate"), Key.key("waxed_oxidized_copper_lantern"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bars"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_copper_chain"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_door"),
                Key.key("waxed_weathered_copper_golem_statue"),
                Key.key("waxed_weathered_copper_grate"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bars"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_copper_chain"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_door"),
                Key.key("weathered_copper_golem_statue"), Key.key("weathered_copper_grate"),
                Key.key("weathered_copper_lantern"), Key.key("weathered_copper_trapdoor"),
                Key.key("weathered_cut_copper"), Key.key("weathered_cut_copper_slab"),
                Key.key("weathered_cut_copper_stairs"), Key.key("weathered_lightning_rod"),
                Key.key("white_concrete"), Key.key("white_concrete_slab"),
                Key.key("white_concrete_stairs"), Key.key("white_glazed_terracotta"),
                Key.key("white_shulker_box"), Key.key("white_terracotta"),
                Key.key("yellow_concrete"), Key.key("yellow_concrete_slab"),
                Key.key("yellow_concrete_stairs"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_shulker_box"), Key.key("yellow_terracotta")));
    }

    private static void blockTags22(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("mineable/shovel"), List.of(Key.key("black_concrete_powder"),
                Key.key("blue_concrete_powder"), Key.key("brown_concrete_powder"), Key.key("clay"),
                Key.key("coarse_dirt"), Key.key("cyan_concrete_powder"), Key.key("dirt"),
                Key.key("dirt_path"), Key.key("farmland"), Key.key("grass_block"),
                Key.key("gravel"), Key.key("gray_concrete_powder"),
                Key.key("green_concrete_powder"), Key.key("light_blue_concrete_powder"),
                Key.key("light_gray_concrete_powder"), Key.key("lime_concrete_powder"),
                Key.key("magenta_concrete_powder"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("mycelium"), Key.key("orange_concrete_powder"),
                Key.key("pink_concrete_powder"), Key.key("podzol"),
                Key.key("purple_concrete_powder"), Key.key("red_concrete_powder"),
                Key.key("red_sand"), Key.key("rooted_dirt"), Key.key("sand"), Key.key("snow"),
                Key.key("snow_block"), Key.key("soul_sand"), Key.key("soul_soil"),
                Key.key("suspicious_gravel"), Key.key("suspicious_sand"),
                Key.key("white_concrete_powder"), Key.key("yellow_concrete_powder")));
        tags.put(Key.key("mob_interactable_doors"), List.of(Key.key("acacia_door"),
                Key.key("bamboo_door"), Key.key("birch_door"), Key.key("cherry_door"),
                Key.key("copper_door"), Key.key("crimson_door"), Key.key("dark_oak_door"),
                Key.key("exposed_copper_door"), Key.key("jungle_door"), Key.key("mangrove_door"),
                Key.key("oak_door"), Key.key("oxidized_copper_door"), Key.key("pale_oak_door"),
                Key.key("poplar_door"), Key.key("spruce_door"), Key.key("warped_door"),
                Key.key("waxed_copper_door"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_oxidized_copper_door"), Key.key("waxed_weathered_copper_door"),
                Key.key("weathered_copper_door")));
        tags.put(Key.key("mooshrooms_spawnable_on"), List.of(Key.key("mycelium")));
        tags.put(Key.key("moss_blocks"), List.of(Key.key("moss_block"),
                Key.key("pale_moss_block")));
        tags.put(Key.key("moss_replaceable"), List.of(Key.key("andesite"), Key.key("cave_vines"),
                Key.key("cave_vines_plant"), Key.key("coarse_dirt"), Key.key("deepslate"),
                Key.key("diorite"), Key.key("dirt"), Key.key("granite"), Key.key("grass_block"),
                Key.key("moss_block"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("mycelium"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("rooted_dirt"), Key.key("stone"), Key.key("tuff")));
        tags.put(Key.key("mud"), List.of(Key.key("mud"), Key.key("muddy_mangrove_roots")));
        tags.put(Key.key("needs_diamond_tool"), List.of(Key.key("ancient_debris"),
                Key.key("crying_obsidian"), Key.key("netherite_block"), Key.key("obsidian"),
                Key.key("respawn_anchor")));
        tags.put(Key.key("needs_iron_tool"), List.of(Key.key("deepslate_diamond_ore"),
                Key.key("deepslate_emerald_ore"), Key.key("deepslate_gold_ore"),
                Key.key("deepslate_redstone_ore"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("emerald_block"), Key.key("emerald_ore"), Key.key("gold_block"),
                Key.key("gold_ore"), Key.key("raw_gold_block"), Key.key("redstone_ore")));
        tags.put(Key.key("needs_stone_tool"), List.of(Key.key("chiseled_copper"),
                Key.key("copper_block"), Key.key("copper_bulb"), Key.key("copper_chest"),
                Key.key("copper_grate"), Key.key("copper_ore"), Key.key("copper_trapdoor"),
                Key.key("crafter"), Key.key("cut_copper"), Key.key("cut_copper_slab"),
                Key.key("cut_copper_stairs"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_iron_ore"), Key.key("deepslate_lapis_ore"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_copper_chest"),
                Key.key("exposed_copper_grate"), Key.key("exposed_copper_trapdoor"),
                Key.key("exposed_cut_copper"), Key.key("exposed_cut_copper_slab"),
                Key.key("exposed_cut_copper_stairs"), Key.key("exposed_lightning_rod"),
                Key.key("iron_block"), Key.key("iron_ore"), Key.key("lapis_block"),
                Key.key("lapis_ore"), Key.key("lightning_rod"), Key.key("oxidized_chiseled_copper"),
                Key.key("oxidized_copper"), Key.key("oxidized_copper_bulb"),
                Key.key("oxidized_copper_chest"), Key.key("oxidized_copper_grate"),
                Key.key("oxidized_copper_trapdoor"), Key.key("oxidized_cut_copper"),
                Key.key("oxidized_cut_copper_slab"), Key.key("oxidized_cut_copper_stairs"),
                Key.key("oxidized_lightning_rod"), Key.key("raw_copper_block"),
                Key.key("raw_iron_block"), Key.key("waxed_chiseled_copper"),
                Key.key("waxed_copper_block"), Key.key("waxed_copper_bulb"),
                Key.key("waxed_copper_chest"), Key.key("waxed_copper_grate"),
                Key.key("waxed_copper_trapdoor"), Key.key("waxed_cut_copper"),
                Key.key("waxed_cut_copper_slab"), Key.key("waxed_cut_copper_stairs"),
                Key.key("waxed_exposed_chiseled_copper"), Key.key("waxed_exposed_copper"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_copper_chest"),
                Key.key("waxed_exposed_copper_grate"), Key.key("waxed_exposed_copper_trapdoor"),
                Key.key("waxed_exposed_cut_copper"), Key.key("waxed_exposed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_stairs"), Key.key("waxed_exposed_lightning_rod"),
                Key.key("waxed_lightning_rod"), Key.key("waxed_oxidized_chiseled_copper"),
                Key.key("waxed_oxidized_copper"), Key.key("waxed_oxidized_copper_bulb"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_oxidized_copper_grate"),
                Key.key("waxed_oxidized_copper_trapdoor"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_chiseled_copper"),
                Key.key("waxed_weathered_copper"), Key.key("waxed_weathered_copper_bulb"),
                Key.key("waxed_weathered_copper_chest"), Key.key("waxed_weathered_copper_grate"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("waxed_weathered_cut_copper"),
                Key.key("waxed_weathered_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("waxed_weathered_lightning_rod"), Key.key("weathered_chiseled_copper"),
                Key.key("weathered_copper"), Key.key("weathered_copper_bulb"),
                Key.key("weathered_copper_chest"), Key.key("weathered_copper_grate"),
                Key.key("weathered_copper_trapdoor"), Key.key("weathered_cut_copper"),
                Key.key("weathered_cut_copper_slab"), Key.key("weathered_cut_copper_stairs"),
                Key.key("weathered_lightning_rod")));
    }

    private static void blockTags23(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("nether_portal_frame"), List.of(Key.key("obsidian")));
        tags.put(Key.key("nylium"), List.of(Key.key("crimson_nylium"), Key.key("warped_nylium")));
        tags.put(Key.key("oak_logs"), List.of(Key.key("oak_log"), Key.key("oak_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood")));
        tags.put(Key.key("occludes_vibration_signals"), List.of(Key.key("black_wool"),
                Key.key("blue_wool"), Key.key("brown_wool"), Key.key("cyan_wool"),
                Key.key("gray_wool"), Key.key("green_wool"), Key.key("light_blue_wool"),
                Key.key("light_gray_wool"), Key.key("lime_wool"), Key.key("magenta_wool"),
                Key.key("orange_wool"), Key.key("pink_wool"), Key.key("purple_wool"),
                Key.key("red_wool"), Key.key("white_wool"), Key.key("yellow_wool")));
        tags.put(Key.key("ores"), List.of(Key.key("coal_ore"), Key.key("copper_ore"),
                Key.key("deepslate_coal_ore"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("diamond_ore"), Key.key("emerald_ore"), Key.key("gold_ore"),
                Key.key("iron_ore"), Key.key("lapis_ore"), Key.key("nether_gold_ore"),
                Key.key("nether_quartz_ore"), Key.key("redstone_ore")));
        tags.put(Key.key("overrides_mushroom_light_requirement"), List.of(Key.key("crimson_nylium"),
                Key.key("mycelium"), Key.key("podzol"), Key.key("warped_nylium")));
        tags.put(Key.key("overworld_natural_logs"), List.of(Key.key("acacia_log"),
                Key.key("birch_log"), Key.key("cherry_log"), Key.key("dark_oak_log"),
                Key.key("jungle_log"), Key.key("mangrove_log"), Key.key("oak_log"),
                Key.key("pale_oak_log"), Key.key("poplar_log"), Key.key("spruce_log")));
        tags.put(Key.key("pale_oak_logs"), List.of(Key.key("pale_oak_log"),
                Key.key("pale_oak_wood"), Key.key("stripped_pale_oak_log"),
                Key.key("stripped_pale_oak_wood")));
        tags.put(Key.key("parrots_spawnable_on"), List.of(Key.key("acacia_leaves"),
                Key.key("acacia_log"), Key.key("acacia_wood"), Key.key("air"),
                Key.key("azalea_leaves"), Key.key("birch_leaves"), Key.key("birch_log"),
                Key.key("birch_wood"), Key.key("cherry_leaves"), Key.key("cherry_log"),
                Key.key("cherry_wood"), Key.key("crimson_hyphae"), Key.key("crimson_stem"),
                Key.key("dark_oak_leaves"), Key.key("dark_oak_log"), Key.key("dark_oak_wood"),
                Key.key("flowering_azalea_leaves"), Key.key("grass_block"),
                Key.key("jungle_leaves"), Key.key("jungle_log"), Key.key("jungle_wood"),
                Key.key("mangrove_leaves"), Key.key("mangrove_log"), Key.key("mangrove_wood"),
                Key.key("oak_leaves"), Key.key("oak_log"), Key.key("oak_wood"),
                Key.key("orange_poplar_leaves"), Key.key("pale_oak_leaves"),
                Key.key("pale_oak_log"), Key.key("pale_oak_wood"), Key.key("poplar_log"),
                Key.key("poplar_wood"), Key.key("red_poplar_leaves"), Key.key("spruce_leaves"),
                Key.key("spruce_log"), Key.key("spruce_wood"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_birch_log"),
                Key.key("stripped_birch_wood"), Key.key("stripped_cherry_log"),
                Key.key("stripped_cherry_wood"), Key.key("stripped_crimson_hyphae"),
                Key.key("stripped_crimson_stem"), Key.key("stripped_dark_oak_log"),
                Key.key("stripped_dark_oak_wood"), Key.key("stripped_jungle_log"),
                Key.key("stripped_jungle_wood"), Key.key("stripped_mangrove_log"),
                Key.key("stripped_mangrove_wood"), Key.key("stripped_oak_log"),
                Key.key("stripped_oak_wood"), Key.key("stripped_pale_oak_log"),
                Key.key("stripped_pale_oak_wood"), Key.key("stripped_poplar_log"),
                Key.key("stripped_poplar_wood"), Key.key("stripped_spruce_log"),
                Key.key("stripped_spruce_wood"), Key.key("stripped_warped_hyphae"),
                Key.key("stripped_warped_stem"), Key.key("warped_hyphae"), Key.key("warped_stem"),
                Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("piglin_repellents"), List.of(Key.key("soul_campfire"),
                Key.key("soul_fire"), Key.key("soul_lantern"), Key.key("soul_torch"),
                Key.key("soul_wall_torch")));
        tags.put(Key.key("planks"), List.of(Key.key("acacia_planks"), Key.key("bamboo_planks"),
                Key.key("birch_planks"), Key.key("cherry_planks"), Key.key("crimson_planks"),
                Key.key("dark_oak_planks"), Key.key("jungle_planks"), Key.key("mangrove_planks"),
                Key.key("oak_planks"), Key.key("pale_oak_planks"), Key.key("poplar_planks"),
                Key.key("spruce_planks"), Key.key("warped_planks")));
        tags.put(Key.key("polar_bear_immune_to"), List.of(Key.key("powder_snow")));
        tags.put(Key.key("polar_bears_spawnable_on_alternate"), List.of(Key.key("ice")));
        tags.put(Key.key("poplar_logs"), List.of(Key.key("poplar_log"), Key.key("poplar_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood")));
        tags.put(Key.key("portals"), List.of(Key.key("end_gateway"), Key.key("end_portal"),
                Key.key("nether_portal")));
    }

    private static void blockTags24(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("pressure_plates"), List.of(Key.key("acacia_pressure_plate"),
                Key.key("bamboo_pressure_plate"), Key.key("birch_pressure_plate"),
                Key.key("cherry_pressure_plate"), Key.key("crimson_pressure_plate"),
                Key.key("dark_oak_pressure_plate"), Key.key("heavy_weighted_pressure_plate"),
                Key.key("jungle_pressure_plate"), Key.key("light_weighted_pressure_plate"),
                Key.key("mangrove_pressure_plate"), Key.key("oak_pressure_plate"),
                Key.key("pale_oak_pressure_plate"), Key.key("polished_blackstone_pressure_plate"),
                Key.key("poplar_pressure_plate"), Key.key("spruce_pressure_plate"),
                Key.key("stone_pressure_plate"), Key.key("warped_pressure_plate")));
        tags.put(Key.key("prevent_mob_spawning_inside"), List.of(Key.key("activator_rail"),
                Key.key("detector_rail"), Key.key("powered_rail"), Key.key("rail")));
        tags.put(Key.key("prevents_nearby_leaf_decay"), List.of(Key.key("acacia_log"),
                Key.key("acacia_wood"), Key.key("birch_log"), Key.key("birch_wood"),
                Key.key("cherry_log"), Key.key("cherry_wood"), Key.key("crimson_hyphae"),
                Key.key("crimson_stem"), Key.key("dark_oak_log"), Key.key("dark_oak_wood"),
                Key.key("jungle_log"), Key.key("jungle_wood"), Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("oak_log"), Key.key("oak_wood"),
                Key.key("pale_oak_log"), Key.key("pale_oak_wood"), Key.key("poplar_log"),
                Key.key("poplar_wood"), Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("warped_hyphae"), Key.key("warped_stem")));
        tags.put(Key.key("rabbits_spawnable_on"), List.of(Key.key("grass_block"), Key.key("sand"),
                Key.key("snow"), Key.key("snow_block")));
        tags.put(Key.key("rails"), List.of(Key.key("activator_rail"), Key.key("detector_rail"),
                Key.key("powered_rail"), Key.key("rail")));
        tags.put(Key.key("redstone_ores"), List.of(Key.key("deepslate_redstone_ore"),
                Key.key("redstone_ore")));
        tags.put(Key.key("replaceable"), List.of(Key.key("air"), Key.key("bubble_column"),
                Key.key("bush"), Key.key("cave_air"), Key.key("crimson_roots"),
                Key.key("dead_bush"), Key.key("fern"), Key.key("fire"), Key.key("glow_lichen"),
                Key.key("hanging_roots"), Key.key("large_fern"), Key.key("lava"),
                Key.key("leaf_litter"), Key.key("light"), Key.key("nether_sprouts"),
                Key.key("red_shrub"), Key.key("resin_clump"), Key.key("seagrass"),
                Key.key("short_dry_grass"), Key.key("short_grass"), Key.key("snow"),
                Key.key("soul_fire"), Key.key("structure_void"), Key.key("tall_dry_grass"),
                Key.key("tall_grass"), Key.key("tall_seagrass"), Key.key("vine"),
                Key.key("void_air"), Key.key("warped_roots"), Key.key("water")));
        tags.put(Key.key("replaceable_by_mushrooms"), List.of(Key.key("acacia_leaves"),
                Key.key("allium"), Key.key("azalea_leaves"), Key.key("azure_bluet"),
                Key.key("birch_leaves"), Key.key("blue_orchid"), Key.key("brown_mushroom"),
                Key.key("brown_mushroom_block"), Key.key("bush"), Key.key("cherry_leaves"),
                Key.key("closed_eyeblossom"), Key.key("cornflower"), Key.key("crimson_roots"),
                Key.key("dandelion"), Key.key("dark_oak_leaves"), Key.key("dead_bush"),
                Key.key("fern"), Key.key("firefly_bush"), Key.key("flowering_azalea_leaves"),
                Key.key("glow_lichen"), Key.key("golden_dandelion"), Key.key("hanging_roots"),
                Key.key("jungle_leaves"), Key.key("large_fern"), Key.key("leaf_litter"),
                Key.key("lilac"), Key.key("lily_of_the_valley"), Key.key("mangrove_leaves"),
                Key.key("nether_sprouts"), Key.key("oak_leaves"), Key.key("open_eyeblossom"),
                Key.key("orange_poplar_leaves"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("pale_moss_carpet"), Key.key("pale_oak_leaves"), Key.key("peony"),
                Key.key("pink_tulip"), Key.key("pitcher_plant"), Key.key("poppy"),
                Key.key("red_mushroom"), Key.key("red_mushroom_block"),
                Key.key("red_poplar_leaves"), Key.key("red_shrub"), Key.key("red_tulip"),
                Key.key("rose_bush"), Key.key("seagrass"), Key.key("short_dry_grass"),
                Key.key("short_grass"), Key.key("spruce_leaves"), Key.key("sunflower"),
                Key.key("tall_dry_grass"), Key.key("tall_grass"), Key.key("tall_seagrass"),
                Key.key("torchflower"), Key.key("vine"), Key.key("warped_roots"), Key.key("water"),
                Key.key("white_tulip"), Key.key("wither_rose"), Key.key("yellow_poplar_leaves")));
    }

    private static void blockTags25(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("replaceable_by_trees"), List.of(Key.key("acacia_leaves"),
                Key.key("allium"), Key.key("azalea_leaves"), Key.key("azure_bluet"),
                Key.key("birch_leaves"), Key.key("blue_orchid"), Key.key("bush"),
                Key.key("cherry_leaves"), Key.key("closed_eyeblossom"), Key.key("cornflower"),
                Key.key("crimson_roots"), Key.key("dandelion"), Key.key("dark_oak_leaves"),
                Key.key("dead_bush"), Key.key("fern"), Key.key("firefly_bush"),
                Key.key("flowering_azalea_leaves"), Key.key("glow_lichen"),
                Key.key("golden_dandelion"), Key.key("hanging_roots"), Key.key("jungle_leaves"),
                Key.key("large_fern"), Key.key("leaf_litter"), Key.key("lilac"),
                Key.key("lily_of_the_valley"), Key.key("mangrove_leaves"),
                Key.key("nether_sprouts"), Key.key("oak_leaves"), Key.key("open_eyeblossom"),
                Key.key("orange_poplar_leaves"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("pale_moss_carpet"), Key.key("pale_oak_leaves"), Key.key("peony"),
                Key.key("pink_tulip"), Key.key("pitcher_plant"), Key.key("poppy"),
                Key.key("red_poplar_leaves"), Key.key("red_tulip"), Key.key("rose_bush"),
                Key.key("seagrass"), Key.key("shelf_mushroom"), Key.key("short_dry_grass"),
                Key.key("short_grass"), Key.key("spruce_leaves"), Key.key("sunflower"),
                Key.key("tall_dry_grass"), Key.key("tall_grass"), Key.key("tall_seagrass"),
                Key.key("torchflower"), Key.key("vine"), Key.key("warped_roots"), Key.key("water"),
                Key.key("white_tulip"), Key.key("wither_rose"), Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("required_for_poplar_leaf_ambience"), List.of(Key.key("acacia_log"),
                Key.key("birch_log"), Key.key("cherry_log"), Key.key("dark_oak_log"),
                Key.key("jungle_log"), Key.key("mangrove_log"), Key.key("oak_log"),
                Key.key("pale_oak_log"), Key.key("poplar_log"), Key.key("spruce_log")));
        tags.put(Key.key("sand"), List.of(Key.key("red_sand"), Key.key("sand"),
                Key.key("suspicious_sand")));
        tags.put(Key.key("saplings"), List.of(Key.key("acacia_sapling"), Key.key("azalea"),
                Key.key("birch_sapling"), Key.key("cherry_sapling"), Key.key("dark_oak_sapling"),
                Key.key("flowering_azalea"), Key.key("jungle_sapling"),
                Key.key("mangrove_propagule"), Key.key("oak_sapling"), Key.key("pale_oak_sapling"),
                Key.key("poplar_sapling"), Key.key("spruce_sapling")));
        tags.put(Key.key("sculk_growth_inhibitors"), List.of(Key.key("sculk_sensor"),
                Key.key("sculk_shrieker")));
        tags.put(Key.key("sculk_replaceable"), List.of(Key.key("andesite"), Key.key("basalt"),
                Key.key("black_terracotta"), Key.key("blackstone"), Key.key("blue_terracotta"),
                Key.key("brown_terracotta"), Key.key("calcite"), Key.key("cinnabar"),
                Key.key("clay"), Key.key("coarse_dirt"), Key.key("crimson_nylium"),
                Key.key("cyan_terracotta"), Key.key("deepslate"), Key.key("diorite"),
                Key.key("dirt"), Key.key("dripstone_block"), Key.key("end_stone"),
                Key.key("granite"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("netherrack"), Key.key("orange_terracotta"), Key.key("pale_moss_block"),
                Key.key("pink_terracotta"), Key.key("podzol"), Key.key("purple_terracotta"),
                Key.key("red_sand"), Key.key("red_sandstone"), Key.key("red_terracotta"),
                Key.key("rooted_dirt"), Key.key("sand"), Key.key("sandstone"),
                Key.key("smooth_basalt"), Key.key("soul_sand"), Key.key("soul_soil"),
                Key.key("stone"), Key.key("sulfur"), Key.key("terracotta"), Key.key("tuff"),
                Key.key("warped_nylium"), Key.key("white_terracotta"),
                Key.key("yellow_terracotta")));
        tags.put(Key.key("sculk_replaceable_world_gen"), List.of(Key.key("andesite"),
                Key.key("basalt"), Key.key("black_terracotta"), Key.key("blackstone"),
                Key.key("blue_terracotta"), Key.key("brown_terracotta"), Key.key("calcite"),
                Key.key("cinnabar"), Key.key("clay"), Key.key("coarse_dirt"),
                Key.key("cobbled_deepslate"), Key.key("cracked_deepslate_bricks"),
                Key.key("cracked_deepslate_tiles"), Key.key("crimson_nylium"),
                Key.key("cyan_terracotta"), Key.key("deepslate"), Key.key("deepslate_bricks"),
                Key.key("deepslate_tiles"), Key.key("diorite"), Key.key("dirt"),
                Key.key("dripstone_block"), Key.key("end_stone"), Key.key("granite"),
                Key.key("grass_block"), Key.key("gravel"), Key.key("gray_terracotta"),
                Key.key("green_terracotta"), Key.key("light_blue_terracotta"),
                Key.key("light_gray_terracotta"), Key.key("lime_terracotta"),
                Key.key("magenta_terracotta"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("netherrack"),
                Key.key("orange_terracotta"), Key.key("pale_moss_block"),
                Key.key("pink_terracotta"), Key.key("podzol"), Key.key("polished_deepslate"),
                Key.key("purple_terracotta"), Key.key("red_sand"), Key.key("red_sandstone"),
                Key.key("red_terracotta"), Key.key("rooted_dirt"), Key.key("sand"),
                Key.key("sandstone"), Key.key("smooth_basalt"), Key.key("soul_sand"),
                Key.key("soul_soil"), Key.key("stone"), Key.key("sulfur"), Key.key("terracotta"),
                Key.key("tuff"), Key.key("warped_nylium"), Key.key("white_terracotta"),
                Key.key("yellow_terracotta")));
    }

    private static void blockTags26(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("shears_extreme_breaking_speed"), List.of(Key.key("acacia_leaves"),
                Key.key("azalea_leaves"), Key.key("birch_leaves"), Key.key("cherry_leaves"),
                Key.key("dark_oak_leaves"), Key.key("flowering_azalea_leaves"),
                Key.key("jungle_leaves"), Key.key("mangrove_leaves"), Key.key("oak_leaves"),
                Key.key("orange_poplar_leaves"), Key.key("pale_oak_leaves"),
                Key.key("red_poplar_leaves"), Key.key("spruce_leaves"),
                Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("shears_major_breaking_speed"), List.of(Key.key("black_wool"),
                Key.key("black_wool_slab"), Key.key("black_wool_stairs"), Key.key("blue_wool"),
                Key.key("blue_wool_slab"), Key.key("blue_wool_stairs"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"), Key.key("cyan_wool"),
                Key.key("cyan_wool_slab"), Key.key("cyan_wool_stairs"), Key.key("gray_wool"),
                Key.key("gray_wool_slab"), Key.key("gray_wool_stairs"), Key.key("green_wool"),
                Key.key("green_wool_slab"), Key.key("green_wool_stairs"),
                Key.key("light_blue_wool"), Key.key("light_blue_wool_slab"),
                Key.key("light_blue_wool_stairs"), Key.key("light_gray_wool"),
                Key.key("light_gray_wool_slab"), Key.key("light_gray_wool_stairs"),
                Key.key("lime_wool"), Key.key("lime_wool_slab"), Key.key("lime_wool_stairs"),
                Key.key("magenta_wool"), Key.key("magenta_wool_slab"),
                Key.key("magenta_wool_stairs"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("pink_wool"), Key.key("pink_wool_slab"),
                Key.key("pink_wool_stairs"), Key.key("purple_wool"), Key.key("purple_wool_slab"),
                Key.key("purple_wool_stairs"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("white_wool"), Key.key("white_wool_slab"),
                Key.key("white_wool_stairs"), Key.key("yellow_wool"), Key.key("yellow_wool_slab"),
                Key.key("yellow_wool_stairs")));
        tags.put(Key.key("shears_minor_breaking_speed"), List.of(Key.key("glow_lichen"),
                Key.key("vine")));
        tags.put(Key.key("shulker_boxes"), List.of(Key.key("black_shulker_box"),
                Key.key("blue_shulker_box"), Key.key("brown_shulker_box"),
                Key.key("cyan_shulker_box"), Key.key("gray_shulker_box"),
                Key.key("green_shulker_box"), Key.key("light_blue_shulker_box"),
                Key.key("light_gray_shulker_box"), Key.key("lime_shulker_box"),
                Key.key("magenta_shulker_box"), Key.key("orange_shulker_box"),
                Key.key("pink_shulker_box"), Key.key("purple_shulker_box"),
                Key.key("red_shulker_box"), Key.key("shulker_box"), Key.key("white_shulker_box"),
                Key.key("yellow_shulker_box")));
        tags.put(Key.key("shulker_does_not_teleport_to"), List.of(Key.key("bedrock")));
        tags.put(Key.key("signs"), List.of(Key.key("acacia_sign"), Key.key("acacia_wall_sign"),
                Key.key("bamboo_sign"), Key.key("bamboo_wall_sign"), Key.key("birch_sign"),
                Key.key("birch_wall_sign"), Key.key("cherry_sign"), Key.key("cherry_wall_sign"),
                Key.key("crimson_sign"), Key.key("crimson_wall_sign"), Key.key("dark_oak_sign"),
                Key.key("dark_oak_wall_sign"), Key.key("jungle_sign"), Key.key("jungle_wall_sign"),
                Key.key("mangrove_sign"), Key.key("mangrove_wall_sign"), Key.key("oak_sign"),
                Key.key("oak_wall_sign"), Key.key("pale_oak_sign"), Key.key("pale_oak_wall_sign"),
                Key.key("poplar_sign"), Key.key("poplar_wall_sign"), Key.key("spruce_sign"),
                Key.key("spruce_wall_sign"), Key.key("warped_sign"), Key.key("warped_wall_sign")));
        tags.put(Key.key("skulls"), List.of(Key.key("creeper_head"), Key.key("dragon_head"),
                Key.key("piglin_head"), Key.key("player_head"), Key.key("skeleton_skull"),
                Key.key("wither_skeleton_skull"), Key.key("zombie_head")));
        tags.put(Key.key("slabs"), List.of(Key.key("acacia_slab"), Key.key("andesite_slab"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_slab"), Key.key("birch_slab"),
                Key.key("black_concrete_slab"), Key.key("black_wool_slab"),
                Key.key("blackstone_slab"), Key.key("blue_concrete_slab"),
                Key.key("blue_wool_slab"), Key.key("brick_slab"), Key.key("brown_concrete_slab"),
                Key.key("brown_wool_slab"), Key.key("cherry_slab"), Key.key("cinnabar_brick_slab"),
                Key.key("cinnabar_slab"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobblestone_slab"), Key.key("crimson_slab"), Key.key("cut_copper_slab"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone_slab"),
                Key.key("cyan_concrete_slab"), Key.key("cyan_wool_slab"), Key.key("dark_oak_slab"),
                Key.key("dark_prismarine_slab"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_tile_slab"), Key.key("diorite_slab"),
                Key.key("end_stone_brick_slab"), Key.key("exposed_cut_copper_slab"),
                Key.key("granite_slab"), Key.key("gray_concrete_slab"), Key.key("gray_wool_slab"),
                Key.key("green_concrete_slab"), Key.key("green_wool_slab"), Key.key("jungle_slab"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_wool_slab"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_wool_slab"),
                Key.key("lime_concrete_slab"), Key.key("lime_wool_slab"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_wool_slab"),
                Key.key("mangrove_slab"), Key.key("mossy_cobblestone_slab"),
                Key.key("mossy_stone_brick_slab"), Key.key("mud_brick_slab"),
                Key.key("nether_brick_slab"), Key.key("oak_slab"), Key.key("orange_concrete_slab"),
                Key.key("orange_wool_slab"), Key.key("oxidized_cut_copper_slab"),
                Key.key("pale_oak_slab"), Key.key("petrified_oak_slab"),
                Key.key("pink_concrete_slab"), Key.key("pink_wool_slab"),
                Key.key("polished_andesite_slab"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_slab"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_deepslate_slab"), Key.key("polished_diorite_slab"),
                Key.key("polished_granite_slab"), Key.key("polished_sulfur_slab"),
                Key.key("polished_tuff_slab"), Key.key("poplar_slab"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_slab"),
                Key.key("purple_concrete_slab"), Key.key("purple_wool_slab"),
                Key.key("purpur_slab"), Key.key("quartz_slab"), Key.key("red_concrete_slab"),
                Key.key("red_nether_brick_slab"), Key.key("red_sandstone_slab"),
                Key.key("red_wool_slab"), Key.key("resin_brick_slab"), Key.key("sandstone_slab"),
                Key.key("smooth_quartz_slab"), Key.key("smooth_red_sandstone_slab"),
                Key.key("smooth_sandstone_slab"), Key.key("smooth_stone_slab"),
                Key.key("spruce_slab"), Key.key("stone_brick_slab"), Key.key("stone_slab"),
                Key.key("sulfur_brick_slab"), Key.key("sulfur_slab"), Key.key("tuff_brick_slab"),
                Key.key("tuff_slab"), Key.key("warped_slab"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_slab"), Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_slab"), Key.key("weathered_cut_copper_slab"),
                Key.key("white_concrete_slab"), Key.key("white_wool_slab"),
                Key.key("yellow_concrete_slab"), Key.key("yellow_wool_slab")));
    }

    private static void blockTags27(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("small_flowers"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("closed_eyeblossom"), Key.key("cornflower"),
                Key.key("dandelion"), Key.key("golden_dandelion"), Key.key("lily_of_the_valley"),
                Key.key("open_eyeblossom"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("pink_tulip"), Key.key("poppy"), Key.key("red_tulip"),
                Key.key("torchflower"), Key.key("white_tulip"), Key.key("wither_rose")));
        tags.put(Key.key("smelts_to_glass"), List.of(Key.key("red_sand"), Key.key("sand")));
        tags.put(Key.key("snaps_goat_horn"), List.of(Key.key("acacia_log"), Key.key("birch_log"),
                Key.key("cherry_log"), Key.key("coal_ore"), Key.key("copper_ore"),
                Key.key("dark_oak_log"), Key.key("emerald_ore"), Key.key("iron_ore"),
                Key.key("jungle_log"), Key.key("mangrove_log"), Key.key("oak_log"),
                Key.key("packed_ice"), Key.key("pale_oak_log"), Key.key("poplar_log"),
                Key.key("spruce_log"), Key.key("stone")));
        tags.put(Key.key("sniffer_diggable_block"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("rooted_dirt")));
        tags.put(Key.key("sniffer_egg_hatch_boost"), List.of(Key.key("moss_block")));
        tags.put(Key.key("snow"), List.of(Key.key("powder_snow"), Key.key("snow"),
                Key.key("snow_block")));
        tags.put(Key.key("snow_golem_immune_to"), List.of(Key.key("powder_snow")));
        tags.put(Key.key("soul_fire_base_blocks"), List.of(Key.key("soul_sand"),
                Key.key("soul_soil")));
        tags.put(Key.key("soul_speed_blocks"), List.of(Key.key("soul_sand"), Key.key("soul_soil")));
        tags.put(Key.key("speeds_up_zombie_villager_curing"), List.of(Key.key("black_bed"),
                Key.key("blue_bed"), Key.key("brown_bed"), Key.key("cyan_bed"), Key.key("gray_bed"),
                Key.key("green_bed"), Key.key("iron_bars"), Key.key("light_blue_bed"),
                Key.key("light_gray_bed"), Key.key("lime_bed"), Key.key("magenta_bed"),
                Key.key("orange_bed"), Key.key("pink_bed"), Key.key("purple_bed"),
                Key.key("red_bed"), Key.key("white_bed"), Key.key("yellow_bed")));
        tags.put(Key.key("speleothems"), List.of(Key.key("pointed_dripstone"),
                Key.key("sulfur_spike")));
        tags.put(Key.key("spruce_logs"), List.of(Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood")));
        tags.put(Key.key("stairs"), List.of(Key.key("acacia_stairs"), Key.key("andesite_stairs"),
                Key.key("bamboo_mosaic_stairs"), Key.key("bamboo_stairs"), Key.key("birch_stairs"),
                Key.key("black_concrete_stairs"), Key.key("black_wool_stairs"),
                Key.key("blackstone_stairs"), Key.key("blue_concrete_stairs"),
                Key.key("blue_wool_stairs"), Key.key("brick_stairs"),
                Key.key("brown_concrete_stairs"), Key.key("brown_wool_stairs"),
                Key.key("cherry_stairs"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_stairs"), Key.key("cobbled_deepslate_stairs"),
                Key.key("cobblestone_stairs"), Key.key("crimson_stairs"),
                Key.key("cut_copper_stairs"), Key.key("cyan_concrete_stairs"),
                Key.key("cyan_wool_stairs"), Key.key("dark_oak_stairs"),
                Key.key("dark_prismarine_stairs"), Key.key("deepslate_brick_stairs"),
                Key.key("deepslate_tile_stairs"), Key.key("diorite_stairs"),
                Key.key("end_stone_brick_stairs"), Key.key("exposed_cut_copper_stairs"),
                Key.key("granite_stairs"), Key.key("gray_concrete_stairs"),
                Key.key("gray_wool_stairs"), Key.key("green_concrete_stairs"),
                Key.key("green_wool_stairs"), Key.key("jungle_stairs"),
                Key.key("light_blue_concrete_stairs"), Key.key("light_blue_wool_stairs"),
                Key.key("light_gray_concrete_stairs"), Key.key("light_gray_wool_stairs"),
                Key.key("lime_concrete_stairs"), Key.key("lime_wool_stairs"),
                Key.key("magenta_concrete_stairs"), Key.key("magenta_wool_stairs"),
                Key.key("mangrove_stairs"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mud_brick_stairs"),
                Key.key("nether_brick_stairs"), Key.key("oak_stairs"),
                Key.key("orange_concrete_stairs"), Key.key("orange_wool_stairs"),
                Key.key("oxidized_cut_copper_stairs"), Key.key("pale_oak_stairs"),
                Key.key("pink_concrete_stairs"), Key.key("pink_wool_stairs"),
                Key.key("polished_andesite_stairs"), Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_cinnabar_stairs"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_diorite_stairs"),
                Key.key("polished_granite_stairs"), Key.key("polished_sulfur_stairs"),
                Key.key("polished_tuff_stairs"), Key.key("poplar_stairs"),
                Key.key("prismarine_brick_stairs"), Key.key("prismarine_stairs"),
                Key.key("purple_concrete_stairs"), Key.key("purple_wool_stairs"),
                Key.key("purpur_stairs"), Key.key("quartz_stairs"), Key.key("red_concrete_stairs"),
                Key.key("red_nether_brick_stairs"), Key.key("red_sandstone_stairs"),
                Key.key("red_wool_stairs"), Key.key("resin_brick_stairs"),
                Key.key("sandstone_stairs"), Key.key("smooth_quartz_stairs"),
                Key.key("smooth_red_sandstone_stairs"), Key.key("smooth_sandstone_stairs"),
                Key.key("spruce_stairs"), Key.key("stone_brick_stairs"), Key.key("stone_stairs"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_stairs"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_stairs"), Key.key("warped_stairs"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_cut_copper_stairs"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("weathered_cut_copper_stairs"), Key.key("white_concrete_stairs"),
                Key.key("white_wool_stairs"), Key.key("yellow_concrete_stairs"),
                Key.key("yellow_wool_stairs")));
    }

    private static void blockTags28(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("standing_signs"), List.of(Key.key("acacia_sign"), Key.key("bamboo_sign"),
                Key.key("birch_sign"), Key.key("cherry_sign"), Key.key("crimson_sign"),
                Key.key("dark_oak_sign"), Key.key("jungle_sign"), Key.key("mangrove_sign"),
                Key.key("oak_sign"), Key.key("pale_oak_sign"), Key.key("poplar_sign"),
                Key.key("spruce_sign"), Key.key("warped_sign")));
        tags.put(Key.key("stone_bricks"), List.of(Key.key("chiseled_stone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("mossy_stone_bricks"),
                Key.key("stone_bricks")));
        tags.put(Key.key("stone_buttons"), List.of(Key.key("polished_blackstone_button"),
                Key.key("stone_button")));
        tags.put(Key.key("stone_ore_replaceables"), List.of(Key.key("andesite"), Key.key("diorite"),
                Key.key("granite"), Key.key("stone")));
        tags.put(Key.key("stone_pressure_plates"), List.of(Key.key("polished_blackstone_pressure_plate"),
                Key.key("stone_pressure_plate")));
        tags.put(Key.key("stray_immune_to"), List.of(Key.key("powder_snow")));
        tags.put(Key.key("strider_warm_blocks"), List.of(Key.key("lava")));
        tags.put(Key.key("substrate_overworld"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("sulfur_spike_replaceable_blocks"), List.of(Key.key("cinnabar"),
                Key.key("sulfur")));
        tags.put(Key.key("support_override_cactus_flower"), List.of(Key.key("cactus"),
                Key.key("farmland")));
        tags.put(Key.key("support_override_snow_layer"), List.of(Key.key("honey_block"),
                Key.key("mud"), Key.key("soul_sand")));
        tags.put(Key.key("supports_azalea"), List.of(Key.key("clay"), Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("pale_moss_block"), Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_bamboo"), List.of(Key.key("bamboo"), Key.key("bamboo_sapling"),
                Key.key("coarse_dirt"), Key.key("dirt"), Key.key("grass_block"), Key.key("gravel"),
                Key.key("moss_block"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("mycelium"), Key.key("pale_moss_block"), Key.key("podzol"),
                Key.key("red_sand"), Key.key("rooted_dirt"), Key.key("sand"),
                Key.key("suspicious_gravel"), Key.key("suspicious_sand")));
        tags.put(Key.key("supports_big_dripleaf"), List.of(Key.key("clay"), Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_cactus"), List.of(Key.key("red_sand"), Key.key("sand"),
                Key.key("suspicious_sand")));
        tags.put(Key.key("supports_chorus_flower"), List.of(Key.key("end_stone")));
        tags.put(Key.key("supports_chorus_plant"), List.of(Key.key("end_stone")));
        tags.put(Key.key("supports_cocoa"), List.of(Key.key("jungle_log"), Key.key("jungle_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood")));
        tags.put(Key.key("supports_crimson_fungus"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("soul_soil"),
                Key.key("warped_nylium")));
        tags.put(Key.key("supports_crimson_roots"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("soul_soil"),
                Key.key("warped_nylium")));
        tags.put(Key.key("supports_crops"), List.of(Key.key("farmland")));
        tags.put(Key.key("supports_dry_vegetation"), List.of(Key.key("black_terracotta"),
                Key.key("blue_terracotta"), Key.key("brown_terracotta"), Key.key("coarse_dirt"),
                Key.key("cyan_terracotta"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("orange_terracotta"), Key.key("pale_moss_block"),
                Key.key("pink_terracotta"), Key.key("podzol"), Key.key("purple_terracotta"),
                Key.key("red_sand"), Key.key("red_terracotta"), Key.key("rooted_dirt"),
                Key.key("sand"), Key.key("suspicious_sand"), Key.key("terracotta"),
                Key.key("white_terracotta"), Key.key("yellow_terracotta")));
    }

    private static void blockTags29(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("supports_frogspawn"), List.<Key>of());
        tags.put(Key.key("supports_hanging_mangrove_propagule"), List.of(Key.key("mangrove_leaves")));
        tags.put(Key.key("supports_lily_pad"), List.of(Key.key("frosted_ice"), Key.key("ice")));
        tags.put(Key.key("supports_mangrove_propagule"), List.of(Key.key("clay"),
                Key.key("coarse_dirt"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_melon_stem"), List.of(Key.key("farmland")));
        tags.put(Key.key("supports_melon_stem_fruit"), List.of(Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("pale_moss_block"), Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_nether_sprouts"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("soul_soil"),
                Key.key("warped_nylium")));
        tags.put(Key.key("supports_nether_wart"), List.of(Key.key("soul_sand")));
        tags.put(Key.key("supports_pumpkin_stem"), List.of(Key.key("farmland")));
        tags.put(Key.key("supports_pumpkin_stem_fruit"), List.of(Key.key("coarse_dirt"),
                Key.key("dirt"), Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"),
                Key.key("mud"), Key.key("muddy_mangrove_roots"), Key.key("mycelium"),
                Key.key("pale_moss_block"), Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_small_dripleaf"), List.of(Key.key("clay"),
                Key.key("moss_block")));
        tags.put(Key.key("supports_stem_crops"), List.of(Key.key("farmland")));
        tags.put(Key.key("supports_stem_fruit"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_sugar_cane"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("red_sand"), Key.key("rooted_dirt"), Key.key("sand"),
                Key.key("suspicious_sand")));
        tags.put(Key.key("supports_sugar_cane_adjacently"), List.of(Key.key("frosted_ice")));
        tags.put(Key.key("supports_vegetation"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt")));
        tags.put(Key.key("supports_warped_fungus"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("soul_soil"),
                Key.key("warped_nylium")));
        tags.put(Key.key("supports_warped_roots"), List.of(Key.key("coarse_dirt"),
                Key.key("crimson_nylium"), Key.key("dirt"), Key.key("farmland"),
                Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("pale_moss_block"),
                Key.key("podzol"), Key.key("rooted_dirt"), Key.key("soul_soil"),
                Key.key("warped_nylium")));
        tags.put(Key.key("supports_wither_rose"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("farmland"), Key.key("grass_block"), Key.key("moss_block"), Key.key("mud"),
                Key.key("muddy_mangrove_roots"), Key.key("mycelium"), Key.key("netherrack"),
                Key.key("pale_moss_block"), Key.key("podzol"), Key.key("rooted_dirt"),
                Key.key("soul_sand"), Key.key("soul_soil")));
        tags.put(Key.key("suppresses_bounce"), List.of(Key.key("honey_block")));
        tags.put(Key.key("sword_efficient"), List.of(Key.key("acacia_leaves"),
                Key.key("azalea_leaves"), Key.key("big_dripleaf"), Key.key("big_dripleaf_stem"),
                Key.key("birch_leaves"), Key.key("carved_pumpkin"), Key.key("cherry_leaves"),
                Key.key("chorus_flower"), Key.key("chorus_plant"), Key.key("cocoa"),
                Key.key("dark_oak_leaves"), Key.key("flowering_azalea_leaves"),
                Key.key("glow_lichen"), Key.key("jack_o_lantern"), Key.key("jungle_leaves"),
                Key.key("mangrove_leaves"), Key.key("melon"), Key.key("oak_leaves"),
                Key.key("orange_poplar_leaves"), Key.key("pale_oak_leaves"), Key.key("pumpkin"),
                Key.key("red_poplar_leaves"), Key.key("spruce_leaves"), Key.key("vine"),
                Key.key("yellow_poplar_leaves")));
    }

    private static void blockTags30(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("sword_instantly_mines"), List.of(Key.key("bamboo"),
                Key.key("bamboo_sapling")));
        tags.put(Key.key("terracotta"), List.of(Key.key("black_terracotta"),
                Key.key("blue_terracotta"), Key.key("brown_terracotta"), Key.key("cyan_terracotta"),
                Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"),
                Key.key("orange_terracotta"), Key.key("pink_terracotta"),
                Key.key("purple_terracotta"), Key.key("red_terracotta"), Key.key("terracotta"),
                Key.key("white_terracotta"), Key.key("yellow_terracotta")));
        tags.put(Key.key("trail_ruins_replaceable"), List.of(Key.key("gravel")));
        tags.put(Key.key("trapdoors"), List.of(Key.key("acacia_trapdoor"),
                Key.key("bamboo_trapdoor"), Key.key("birch_trapdoor"), Key.key("cherry_trapdoor"),
                Key.key("copper_trapdoor"), Key.key("crimson_trapdoor"),
                Key.key("dark_oak_trapdoor"), Key.key("exposed_copper_trapdoor"),
                Key.key("iron_trapdoor"), Key.key("jungle_trapdoor"), Key.key("mangrove_trapdoor"),
                Key.key("oak_trapdoor"), Key.key("oxidized_copper_trapdoor"),
                Key.key("pale_oak_trapdoor"), Key.key("poplar_trapdoor"),
                Key.key("spruce_trapdoor"), Key.key("warped_trapdoor"),
                Key.key("waxed_copper_trapdoor"), Key.key("waxed_exposed_copper_trapdoor"),
                Key.key("waxed_oxidized_copper_trapdoor"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("weathered_copper_trapdoor")));
        tags.put(Key.key("triggers_ambient_desert_dry_vegetation_block_sounds"), List.of(Key.key("black_terracotta"),
                Key.key("blue_terracotta"), Key.key("brown_terracotta"), Key.key("cyan_terracotta"),
                Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"),
                Key.key("orange_terracotta"), Key.key("pink_terracotta"),
                Key.key("purple_terracotta"), Key.key("red_sand"), Key.key("red_terracotta"),
                Key.key("sand"), Key.key("terracotta"), Key.key("white_terracotta"),
                Key.key("yellow_terracotta")));
        tags.put(Key.key("triggers_ambient_desert_sand_block_sounds"), List.of(Key.key("red_sand"),
                Key.key("sand")));
        tags.put(Key.key("triggers_ambient_dried_ghast_block_sounds"), List.of(Key.key("soul_sand"),
                Key.key("soul_soil")));
        tags.put(Key.key("turns_into_dirt_path"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("grass_block"), Key.key("mycelium"), Key.key("podzol"),
                Key.key("rooted_dirt")));
        tags.put(Key.key("turns_into_farmland"), List.of(Key.key("dirt"), Key.key("dirt_path"),
                Key.key("grass_block")));
        tags.put(Key.key("uncarvable"), List.of(Key.key("bedrock")));
        tags.put(Key.key("underwater_bonemeals"), List.of(Key.key("brain_coral"),
                Key.key("brain_coral_fan"), Key.key("brain_coral_wall_fan"),
                Key.key("bubble_coral"), Key.key("bubble_coral_fan"),
                Key.key("bubble_coral_wall_fan"), Key.key("fire_coral"), Key.key("fire_coral_fan"),
                Key.key("fire_coral_wall_fan"), Key.key("horn_coral"), Key.key("horn_coral_fan"),
                Key.key("horn_coral_wall_fan"), Key.key("seagrass"), Key.key("tube_coral"),
                Key.key("tube_coral_fan"), Key.key("tube_coral_wall_fan")));
        tags.put(Key.key("unstable_bottom_center"), List.of(Key.key("acacia_fence_gate"),
                Key.key("bamboo_fence_gate"), Key.key("birch_fence_gate"),
                Key.key("cherry_fence_gate"), Key.key("crimson_fence_gate"),
                Key.key("dark_oak_fence_gate"), Key.key("jungle_fence_gate"),
                Key.key("mangrove_fence_gate"), Key.key("oak_fence_gate"),
                Key.key("pale_oak_fence_gate"), Key.key("poplar_fence_gate"),
                Key.key("spruce_fence_gate"), Key.key("warped_fence_gate")));
        tags.put(Key.key("valid_spawn"), List.of(Key.key("grass_block"), Key.key("podzol")));
        tags.put(Key.key("vibration_resonators"), List.of(Key.key("amethyst_block")));
        tags.put(Key.key("villager_babies_can_jump_on_bed"), List.of(Key.key("black_bed"),
                Key.key("blue_bed"), Key.key("brown_bed"), Key.key("cyan_bed"), Key.key("gray_bed"),
                Key.key("green_bed"), Key.key("light_blue_bed"), Key.key("light_gray_bed"),
                Key.key("lime_bed"), Key.key("magenta_bed"), Key.key("orange_bed"),
                Key.key("pink_bed"), Key.key("purple_bed"), Key.key("red_bed"),
                Key.key("white_bed"), Key.key("yellow_bed")));
        tags.put(Key.key("villagers_can_sleep_on_bed"), List.of(Key.key("black_bed"),
                Key.key("blue_bed"), Key.key("brown_bed"), Key.key("cyan_bed"), Key.key("gray_bed"),
                Key.key("green_bed"), Key.key("light_blue_bed"), Key.key("light_gray_bed"),
                Key.key("lime_bed"), Key.key("magenta_bed"), Key.key("orange_bed"),
                Key.key("pink_bed"), Key.key("purple_bed"), Key.key("red_bed"),
                Key.key("white_bed"), Key.key("yellow_bed")));
        tags.put(Key.key("wall_corals"), List.of(Key.key("brain_coral_wall_fan"),
                Key.key("bubble_coral_wall_fan"), Key.key("fire_coral_wall_fan"),
                Key.key("horn_coral_wall_fan"), Key.key("tube_coral_wall_fan")));
        tags.put(Key.key("wall_hanging_signs"), List.of(Key.key("acacia_wall_hanging_sign"),
                Key.key("bamboo_wall_hanging_sign"), Key.key("birch_wall_hanging_sign"),
                Key.key("cherry_wall_hanging_sign"), Key.key("crimson_wall_hanging_sign"),
                Key.key("dark_oak_wall_hanging_sign"), Key.key("jungle_wall_hanging_sign"),
                Key.key("mangrove_wall_hanging_sign"), Key.key("oak_wall_hanging_sign"),
                Key.key("pale_oak_wall_hanging_sign"), Key.key("poplar_wall_hanging_sign"),
                Key.key("spruce_wall_hanging_sign"), Key.key("warped_wall_hanging_sign")));
    }

    private static void blockTags31(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("wall_post_override"), List.of(Key.key("acacia_pressure_plate"),
                Key.key("acacia_sign"), Key.key("acacia_wall_sign"),
                Key.key("bamboo_pressure_plate"), Key.key("bamboo_sign"),
                Key.key("bamboo_wall_sign"), Key.key("birch_pressure_plate"), Key.key("birch_sign"),
                Key.key("birch_wall_sign"), Key.key("black_banner"), Key.key("black_wall_banner"),
                Key.key("blue_banner"), Key.key("blue_wall_banner"), Key.key("brown_banner"),
                Key.key("brown_wall_banner"), Key.key("cactus_flower"),
                Key.key("cherry_pressure_plate"), Key.key("cherry_sign"),
                Key.key("cherry_wall_sign"), Key.key("copper_torch"),
                Key.key("crimson_pressure_plate"), Key.key("crimson_sign"),
                Key.key("crimson_wall_sign"), Key.key("cyan_banner"), Key.key("cyan_wall_banner"),
                Key.key("dark_oak_pressure_plate"), Key.key("dark_oak_sign"),
                Key.key("dark_oak_wall_sign"), Key.key("gray_banner"), Key.key("gray_wall_banner"),
                Key.key("green_banner"), Key.key("green_wall_banner"),
                Key.key("heavy_weighted_pressure_plate"), Key.key("jungle_pressure_plate"),
                Key.key("jungle_sign"), Key.key("jungle_wall_sign"), Key.key("light_blue_banner"),
                Key.key("light_blue_wall_banner"), Key.key("light_gray_banner"),
                Key.key("light_gray_wall_banner"), Key.key("light_weighted_pressure_plate"),
                Key.key("lime_banner"), Key.key("lime_wall_banner"), Key.key("magenta_banner"),
                Key.key("magenta_wall_banner"), Key.key("mangrove_pressure_plate"),
                Key.key("mangrove_sign"), Key.key("mangrove_wall_sign"),
                Key.key("oak_pressure_plate"), Key.key("oak_sign"), Key.key("oak_wall_sign"),
                Key.key("orange_banner"), Key.key("orange_wall_banner"),
                Key.key("pale_oak_pressure_plate"), Key.key("pale_oak_sign"),
                Key.key("pale_oak_wall_sign"), Key.key("pink_banner"), Key.key("pink_wall_banner"),
                Key.key("polished_blackstone_pressure_plate"), Key.key("poplar_pressure_plate"),
                Key.key("poplar_sign"), Key.key("poplar_wall_sign"), Key.key("purple_banner"),
                Key.key("purple_wall_banner"), Key.key("red_banner"), Key.key("red_wall_banner"),
                Key.key("redstone_torch"), Key.key("soul_torch"), Key.key("spruce_pressure_plate"),
                Key.key("spruce_sign"), Key.key("spruce_wall_sign"),
                Key.key("stone_pressure_plate"), Key.key("torch"), Key.key("tripwire"),
                Key.key("warped_pressure_plate"), Key.key("warped_sign"),
                Key.key("warped_wall_sign"), Key.key("white_banner"), Key.key("white_wall_banner"),
                Key.key("yellow_banner"), Key.key("yellow_wall_banner")));
        tags.put(Key.key("wall_signs"), List.of(Key.key("acacia_wall_sign"),
                Key.key("bamboo_wall_sign"), Key.key("birch_wall_sign"),
                Key.key("cherry_wall_sign"), Key.key("crimson_wall_sign"),
                Key.key("dark_oak_wall_sign"), Key.key("jungle_wall_sign"),
                Key.key("mangrove_wall_sign"), Key.key("oak_wall_sign"),
                Key.key("pale_oak_wall_sign"), Key.key("poplar_wall_sign"),
                Key.key("spruce_wall_sign"), Key.key("warped_wall_sign")));
        tags.put(Key.key("walls"), List.of(Key.key("andesite_wall"), Key.key("blackstone_wall"),
                Key.key("brick_wall"), Key.key("cinnabar_brick_wall"), Key.key("cinnabar_wall"),
                Key.key("cobbled_deepslate_wall"), Key.key("cobblestone_wall"),
                Key.key("deepslate_brick_wall"), Key.key("deepslate_tile_wall"),
                Key.key("diorite_wall"), Key.key("end_stone_brick_wall"), Key.key("granite_wall"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_wall"),
                Key.key("mud_brick_wall"), Key.key("nether_brick_wall"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar_wall"), Key.key("polished_deepslate_wall"),
                Key.key("polished_sulfur_wall"), Key.key("polished_tuff_wall"),
                Key.key("prismarine_wall"), Key.key("red_nether_brick_wall"),
                Key.key("red_sandstone_wall"), Key.key("resin_brick_wall"),
                Key.key("sandstone_wall"), Key.key("stone_brick_wall"),
                Key.key("sulfur_brick_wall"), Key.key("sulfur_wall"), Key.key("tuff_brick_wall"),
                Key.key("tuff_wall")));
        tags.put(Key.key("warped_stems"), List.of(Key.key("stripped_warped_hyphae"),
                Key.key("stripped_warped_stem"), Key.key("warped_hyphae"), Key.key("warped_stem")));
        tags.put(Key.key("wart_blocks"), List.of(Key.key("nether_wart_block"),
                Key.key("warped_wart_block")));
        tags.put(Key.key("washed_away_by_fluids"), List.of(Key.key("acacia_button"),
                Key.key("acacia_sapling"), Key.key("activator_rail"), Key.key("air"),
                Key.key("allium"), Key.key("attached_melon_stem"), Key.key("attached_pumpkin_stem"),
                Key.key("azalea"), Key.key("azure_bluet"), Key.key("bamboo_button"),
                Key.key("bamboo_sapling"), Key.key("beetroots"), Key.key("big_dripleaf"),
                Key.key("big_dripleaf_stem"), Key.key("birch_button"), Key.key("birch_sapling"),
                Key.key("black_candle"), Key.key("black_carpet"), Key.key("blue_candle"),
                Key.key("blue_carpet"), Key.key("blue_orchid"), Key.key("brain_coral"),
                Key.key("brain_coral_fan"), Key.key("brain_coral_wall_fan"),
                Key.key("brown_candle"), Key.key("brown_carpet"), Key.key("brown_mushroom"),
                Key.key("bubble_coral"), Key.key("bubble_coral_fan"),
                Key.key("bubble_coral_wall_fan"), Key.key("bush"), Key.key("cactus_flower"),
                Key.key("candle"), Key.key("carrots"), Key.key("cave_air"), Key.key("cave_vines"),
                Key.key("cave_vines_plant"), Key.key("cherry_button"), Key.key("cherry_sapling"),
                Key.key("chorus_flower"), Key.key("chorus_plant"), Key.key("closed_eyeblossom"),
                Key.key("cobweb"), Key.key("cocoa"), Key.key("comparator"),
                Key.key("copper_golem_statue"), Key.key("copper_torch"),
                Key.key("copper_wall_torch"), Key.key("cornflower"), Key.key("creeper_head"),
                Key.key("creeper_wall_head"), Key.key("crimson_button"), Key.key("crimson_fungus"),
                Key.key("crimson_roots"), Key.key("cyan_candle"), Key.key("cyan_carpet"),
                Key.key("dandelion"), Key.key("dark_oak_button"), Key.key("dark_oak_sapling"),
                Key.key("dead_bush"), Key.key("detector_rail"), Key.key("dragon_head"),
                Key.key("dragon_wall_head"), Key.key("end_rod"),
                Key.key("exposed_copper_golem_statue"), Key.key("fern"), Key.key("fire"),
                Key.key("fire_coral"), Key.key("fire_coral_fan"), Key.key("fire_coral_wall_fan"),
                Key.key("firefly_bush"), Key.key("flower_pot"), Key.key("flowering_azalea"),
                Key.key("frogspawn"), Key.key("glow_lichen"), Key.key("golden_dandelion"),
                Key.key("gray_candle"), Key.key("gray_carpet"), Key.key("green_candle"),
                Key.key("green_carpet"), Key.key("hanging_roots"), Key.key("heavy_core"),
                Key.key("horn_coral"), Key.key("horn_coral_fan"), Key.key("horn_coral_wall_fan"),
                Key.key("jungle_button"), Key.key("jungle_sapling"), Key.key("kelp"),
                Key.key("kelp_plant"), Key.key("large_fern"), Key.key("lava"),
                Key.key("leaf_litter"), Key.key("lever"), Key.key("light"),
                Key.key("light_blue_candle"), Key.key("light_blue_carpet"),
                Key.key("light_gray_candle"), Key.key("light_gray_carpet"), Key.key("lilac"),
                Key.key("lily_of_the_valley"), Key.key("lily_pad"), Key.key("lime_candle"),
                Key.key("lime_carpet"), Key.key("magenta_candle"), Key.key("magenta_carpet"),
                Key.key("mangrove_button"), Key.key("mangrove_propagule"), Key.key("melon_stem"),
                Key.key("moss_carpet"), Key.key("nether_sprouts"), Key.key("nether_wart"),
                Key.key("oak_button"), Key.key("oak_sapling"), Key.key("open_eyeblossom"),
                Key.key("orange_candle"), Key.key("orange_carpet"), Key.key("orange_tulip"),
                Key.key("oxeye_daisy"), Key.key("oxidized_copper_golem_statue"),
                Key.key("pale_hanging_moss"), Key.key("pale_moss_carpet"),
                Key.key("pale_oak_button"), Key.key("pale_oak_sapling"), Key.key("peony"),
                Key.key("piglin_head"), Key.key("piglin_wall_head"), Key.key("pink_candle"),
                Key.key("pink_carpet"), Key.key("pink_petals"), Key.key("pink_tulip"),
                Key.key("pitcher_crop"), Key.key("pitcher_plant"), Key.key("player_head"),
                Key.key("player_wall_head"), Key.key("polished_blackstone_button"),
                Key.key("poplar_button"), Key.key("poplar_sapling"), Key.key("poppy"),
                Key.key("potatoes"), Key.key("potted_acacia_sapling"), Key.key("potted_allium"),
                Key.key("potted_azalea_bush"), Key.key("potted_azure_bluet"),
                Key.key("potted_bamboo"), Key.key("potted_birch_sapling"),
                Key.key("potted_blue_orchid"), Key.key("potted_brown_mushroom"),
                Key.key("potted_cactus"), Key.key("potted_cherry_sapling"),
                Key.key("potted_closed_eyeblossom"), Key.key("potted_cornflower"),
                Key.key("potted_crimson_fungus"), Key.key("potted_crimson_roots"),
                Key.key("potted_dandelion"), Key.key("potted_dark_oak_sapling"),
                Key.key("potted_dead_bush"), Key.key("potted_fern"),
                Key.key("potted_flowering_azalea_bush"), Key.key("potted_golden_dandelion"),
                Key.key("potted_jungle_sapling"), Key.key("potted_lily_of_the_valley"),
                Key.key("potted_mangrove_propagule"), Key.key("potted_oak_sapling"),
                Key.key("potted_open_eyeblossom"), Key.key("potted_orange_tulip"),
                Key.key("potted_oxeye_daisy"), Key.key("potted_pale_oak_sapling"),
                Key.key("potted_pink_tulip"), Key.key("potted_poplar_sapling"),
                Key.key("potted_poppy"), Key.key("potted_red_mushroom"),
                Key.key("potted_red_tulip"), Key.key("potted_spruce_sapling"),
                Key.key("potted_torchflower"), Key.key("potted_warped_fungus"),
                Key.key("potted_warped_roots"), Key.key("potted_white_tulip"),
                Key.key("potted_wither_rose"), Key.key("powder_snow"), Key.key("powered_rail"),
                Key.key("pumpkin_stem"), Key.key("purple_candle"), Key.key("purple_carpet"),
                Key.key("rail"), Key.key("red_candle"), Key.key("red_carpet"),
                Key.key("red_mushroom"), Key.key("red_shrub"), Key.key("red_tulip"),
                Key.key("redstone_torch"), Key.key("redstone_wall_torch"), Key.key("redstone_wire"),
                Key.key("repeater"), Key.key("resin_clump"), Key.key("rose_bush"),
                Key.key("scaffolding"), Key.key("sea_pickle"), Key.key("seagrass"),
                Key.key("shelf_mushroom"), Key.key("short_dry_grass"), Key.key("short_grass"),
                Key.key("skeleton_skull"), Key.key("skeleton_wall_skull"),
                Key.key("small_dripleaf"), Key.key("snow"), Key.key("soul_fire"),
                Key.key("soul_torch"), Key.key("soul_wall_torch"), Key.key("spore_blossom"),
                Key.key("spruce_button"), Key.key("spruce_sapling"), Key.key("stone_button"),
                Key.key("straw_bed"), Key.key("sunflower"), Key.key("sweet_berry_bush"),
                Key.key("tall_dry_grass"), Key.key("tall_grass"), Key.key("tall_seagrass"),
                Key.key("torch"), Key.key("torchflower"), Key.key("torchflower_crop"),
                Key.key("tripwire"), Key.key("tripwire_hook"), Key.key("tube_coral"),
                Key.key("tube_coral_fan"), Key.key("tube_coral_wall_fan"),
                Key.key("twisting_vines"), Key.key("twisting_vines_plant"), Key.key("vine"),
                Key.key("void_air"), Key.key("wall_torch"), Key.key("warped_button"),
                Key.key("warped_fungus"), Key.key("warped_roots"), Key.key("water"),
                Key.key("waxed_copper_golem_statue"), Key.key("waxed_exposed_copper_golem_statue"),
                Key.key("waxed_oxidized_copper_golem_statue"),
                Key.key("waxed_weathered_copper_golem_statue"),
                Key.key("weathered_copper_golem_statue"), Key.key("weeping_vines"),
                Key.key("weeping_vines_plant"), Key.key("wheat"), Key.key("white_candle"),
                Key.key("white_carpet"), Key.key("white_tulip"), Key.key("wildflowers"),
                Key.key("wither_rose"), Key.key("wither_skeleton_skull"),
                Key.key("wither_skeleton_wall_skull"), Key.key("yellow_candle"),
                Key.key("yellow_carpet"), Key.key("zombie_head"), Key.key("zombie_wall_head")));
    }

    private static void blockTags32(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("wither_immune"), List.of(Key.key("barrier"), Key.key("bedrock"),
                Key.key("chain_command_block"), Key.key("command_block"), Key.key("end_gateway"),
                Key.key("end_portal"), Key.key("end_portal_frame"), Key.key("jigsaw"),
                Key.key("light"), Key.key("moving_piston"), Key.key("reinforced_deepslate"),
                Key.key("repeating_command_block"), Key.key("structure_block"),
                Key.key("test_block"), Key.key("test_instance_block")));
        tags.put(Key.key("wither_immune_to"), List.of(Key.key("wither_rose")));
        tags.put(Key.key("wither_skeleton_immune_to"), List.of(Key.key("wither_rose")));
        tags.put(Key.key("wither_summon_base_blocks"), List.of(Key.key("soul_sand"),
                Key.key("soul_soil")));
        tags.put(Key.key("wolves_spawnable_on"), List.of(Key.key("coarse_dirt"),
                Key.key("grass_block"), Key.key("podzol"), Key.key("snow"), Key.key("snow_block")));
        tags.put(Key.key("wooden_buttons"), List.of(Key.key("acacia_button"),
                Key.key("bamboo_button"), Key.key("birch_button"), Key.key("cherry_button"),
                Key.key("crimson_button"), Key.key("dark_oak_button"), Key.key("jungle_button"),
                Key.key("mangrove_button"), Key.key("oak_button"), Key.key("pale_oak_button"),
                Key.key("poplar_button"), Key.key("spruce_button"), Key.key("warped_button")));
        tags.put(Key.key("wooden_doors"), List.of(Key.key("acacia_door"), Key.key("bamboo_door"),
                Key.key("birch_door"), Key.key("cherry_door"), Key.key("crimson_door"),
                Key.key("dark_oak_door"), Key.key("jungle_door"), Key.key("mangrove_door"),
                Key.key("oak_door"), Key.key("pale_oak_door"), Key.key("poplar_door"),
                Key.key("spruce_door"), Key.key("warped_door")));
        tags.put(Key.key("wooden_fences"), List.of(Key.key("acacia_fence"), Key.key("bamboo_fence"),
                Key.key("birch_fence"), Key.key("cherry_fence"), Key.key("crimson_fence"),
                Key.key("dark_oak_fence"), Key.key("jungle_fence"), Key.key("mangrove_fence"),
                Key.key("oak_fence"), Key.key("pale_oak_fence"), Key.key("poplar_fence"),
                Key.key("spruce_fence"), Key.key("warped_fence")));
        tags.put(Key.key("wooden_pressure_plates"), List.of(Key.key("acacia_pressure_plate"),
                Key.key("bamboo_pressure_plate"), Key.key("birch_pressure_plate"),
                Key.key("cherry_pressure_plate"), Key.key("crimson_pressure_plate"),
                Key.key("dark_oak_pressure_plate"), Key.key("jungle_pressure_plate"),
                Key.key("mangrove_pressure_plate"), Key.key("oak_pressure_plate"),
                Key.key("pale_oak_pressure_plate"), Key.key("poplar_pressure_plate"),
                Key.key("spruce_pressure_plate"), Key.key("warped_pressure_plate")));
        tags.put(Key.key("wooden_shelves"), List.of(Key.key("acacia_shelf"),
                Key.key("bamboo_shelf"), Key.key("birch_shelf"), Key.key("cherry_shelf"),
                Key.key("crimson_shelf"), Key.key("dark_oak_shelf"), Key.key("jungle_shelf"),
                Key.key("mangrove_shelf"), Key.key("oak_shelf"), Key.key("pale_oak_shelf"),
                Key.key("poplar_shelf"), Key.key("spruce_shelf"), Key.key("warped_shelf")));
        tags.put(Key.key("wooden_slabs"), List.of(Key.key("acacia_slab"), Key.key("bamboo_slab"),
                Key.key("birch_slab"), Key.key("cherry_slab"), Key.key("crimson_slab"),
                Key.key("dark_oak_slab"), Key.key("jungle_slab"), Key.key("mangrove_slab"),
                Key.key("oak_slab"), Key.key("pale_oak_slab"), Key.key("poplar_slab"),
                Key.key("spruce_slab"), Key.key("warped_slab")));
        tags.put(Key.key("wooden_stairs"), List.of(Key.key("acacia_stairs"),
                Key.key("bamboo_stairs"), Key.key("birch_stairs"), Key.key("cherry_stairs"),
                Key.key("crimson_stairs"), Key.key("dark_oak_stairs"), Key.key("jungle_stairs"),
                Key.key("mangrove_stairs"), Key.key("oak_stairs"), Key.key("pale_oak_stairs"),
                Key.key("poplar_stairs"), Key.key("spruce_stairs"), Key.key("warped_stairs")));
        tags.put(Key.key("wooden_trapdoors"), List.of(Key.key("acacia_trapdoor"),
                Key.key("bamboo_trapdoor"), Key.key("birch_trapdoor"), Key.key("cherry_trapdoor"),
                Key.key("crimson_trapdoor"), Key.key("dark_oak_trapdoor"),
                Key.key("jungle_trapdoor"), Key.key("mangrove_trapdoor"), Key.key("oak_trapdoor"),
                Key.key("pale_oak_trapdoor"), Key.key("poplar_trapdoor"),
                Key.key("spruce_trapdoor"), Key.key("warped_trapdoor")));
        tags.put(Key.key("wool"), List.of(Key.key("black_wool"), Key.key("blue_wool"),
                Key.key("brown_wool"), Key.key("cyan_wool"), Key.key("gray_wool"),
                Key.key("green_wool"), Key.key("light_blue_wool"), Key.key("light_gray_wool"),
                Key.key("lime_wool"), Key.key("magenta_wool"), Key.key("orange_wool"),
                Key.key("pink_wool"), Key.key("purple_wool"), Key.key("red_wool"),
                Key.key("white_wool"), Key.key("yellow_wool")));
        tags.put(Key.key("wool_carpets"), List.of(Key.key("black_carpet"), Key.key("blue_carpet"),
                Key.key("brown_carpet"), Key.key("cyan_carpet"), Key.key("gray_carpet"),
                Key.key("green_carpet"), Key.key("light_blue_carpet"), Key.key("light_gray_carpet"),
                Key.key("lime_carpet"), Key.key("magenta_carpet"), Key.key("orange_carpet"),
                Key.key("pink_carpet"), Key.key("purple_carpet"), Key.key("red_carpet"),
                Key.key("white_carpet"), Key.key("yellow_carpet")));
    }

    private static void blockTags33(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("wool_slabs"), List.of(Key.key("black_wool_slab"),
                Key.key("blue_wool_slab"), Key.key("brown_wool_slab"), Key.key("cyan_wool_slab"),
                Key.key("gray_wool_slab"), Key.key("green_wool_slab"),
                Key.key("light_blue_wool_slab"), Key.key("light_gray_wool_slab"),
                Key.key("lime_wool_slab"), Key.key("magenta_wool_slab"),
                Key.key("orange_wool_slab"), Key.key("pink_wool_slab"), Key.key("purple_wool_slab"),
                Key.key("red_wool_slab"), Key.key("white_wool_slab"), Key.key("yellow_wool_slab")));
        tags.put(Key.key("wool_stairs"), List.of(Key.key("black_wool_stairs"),
                Key.key("blue_wool_stairs"), Key.key("brown_wool_stairs"),
                Key.key("cyan_wool_stairs"), Key.key("gray_wool_stairs"),
                Key.key("green_wool_stairs"), Key.key("light_blue_wool_stairs"),
                Key.key("light_gray_wool_stairs"), Key.key("lime_wool_stairs"),
                Key.key("magenta_wool_stairs"), Key.key("orange_wool_stairs"),
                Key.key("pink_wool_stairs"), Key.key("purple_wool_stairs"),
                Key.key("red_wool_stairs"), Key.key("white_wool_stairs"),
                Key.key("yellow_wool_stairs")));
    }

    /**
     * @return the tags of {@code minecraft:block}, keyed by tag identifier
     */
    private static Map<Key, List<Key>> blockTags() {
        final Map<Key, List<Key>> tags = new LinkedHashMap<>(300);
        blockTags0(tags);
        blockTags1(tags);
        blockTags2(tags);
        blockTags3(tags);
        blockTags4(tags);
        blockTags5(tags);
        blockTags6(tags);
        blockTags7(tags);
        blockTags8(tags);
        blockTags9(tags);
        blockTags10(tags);
        blockTags11(tags);
        blockTags12(tags);
        blockTags13(tags);
        blockTags14(tags);
        blockTags15(tags);
        blockTags16(tags);
        blockTags17(tags);
        blockTags18(tags);
        blockTags19(tags);
        blockTags20(tags);
        blockTags21(tags);
        blockTags22(tags);
        blockTags23(tags);
        blockTags24(tags);
        blockTags25(tags);
        blockTags26(tags);
        blockTags27(tags);
        blockTags28(tags);
        blockTags29(tags);
        blockTags30(tags);
        blockTags31(tags);
        blockTags32(tags);
        blockTags33(tags);
        return Map.copyOf(tags);
    }

    private static void dataComponentType0(final List<Key> entries) {
        entries.add(Key.key("custom_data"));
        entries.add(Key.key("max_stack_size"));
        entries.add(Key.key("max_damage"));
        entries.add(Key.key("damage"));
        entries.add(Key.key("unbreakable"));
        entries.add(Key.key("use_effects"));
        entries.add(Key.key("custom_name"));
        entries.add(Key.key("minimum_attack_charge"));
        entries.add(Key.key("damage_type"));
        entries.add(Key.key("item_name"));
        entries.add(Key.key("item_model"));
        entries.add(Key.key("lore"));
        entries.add(Key.key("rarity"));
        entries.add(Key.key("enchantments"));
        entries.add(Key.key("can_place_on"));
        entries.add(Key.key("can_break"));
        entries.add(Key.key("attribute_modifiers"));
        entries.add(Key.key("custom_model_data"));
        entries.add(Key.key("tooltip_display"));
        entries.add(Key.key("repair_cost"));
        entries.add(Key.key("creative_slot_lock"));
        entries.add(Key.key("enchantment_glint_override"));
        entries.add(Key.key("intangible_projectile"));
        entries.add(Key.key("food"));
        entries.add(Key.key("consumable"));
        entries.add(Key.key("use_remainder"));
        entries.add(Key.key("use_cooldown"));
        entries.add(Key.key("damage_resistant"));
        entries.add(Key.key("tool"));
        entries.add(Key.key("weapon"));
        entries.add(Key.key("attack_range"));
        entries.add(Key.key("enchantable"));
        entries.add(Key.key("equippable"));
        entries.add(Key.key("repairable"));
        entries.add(Key.key("glider"));
        entries.add(Key.key("tooltip_style"));
        entries.add(Key.key("death_protection"));
        entries.add(Key.key("blocks_attacks"));
        entries.add(Key.key("piercing_weapon"));
        entries.add(Key.key("kinetic_weapon"));
        entries.add(Key.key("attack_animation"));
        entries.add(Key.key("interact_animation"));
        entries.add(Key.key("additional_trade_cost"));
        entries.add(Key.key("block_transformer"));
        entries.add(Key.key("villager_food"));
        entries.add(Key.key("stored_enchantments"));
        entries.add(Key.key("dye"));
        entries.add(Key.key("dyed_color"));
        entries.add(Key.key("map_id"));
        entries.add(Key.key("map_decorations"));
        entries.add(Key.key("map_post_processing"));
        entries.add(Key.key("charged_projectiles"));
        entries.add(Key.key("bundle_contents"));
        entries.add(Key.key("potion_contents"));
        entries.add(Key.key("potion_duration_scale"));
        entries.add(Key.key("suspicious_stew_effects"));
        entries.add(Key.key("writable_book_content"));
        entries.add(Key.key("written_book_content"));
        entries.add(Key.key("trim"));
        entries.add(Key.key("debug_stick_state"));
        entries.add(Key.key("entity_data"));
        entries.add(Key.key("bucket_entity_data"));
        entries.add(Key.key("block_entity_data"));
        entries.add(Key.key("instrument"));
        entries.add(Key.key("provides_trim_material"));
        entries.add(Key.key("ominous_bottle_amplifier"));
        entries.add(Key.key("jukebox_playable"));
        entries.add(Key.key("provides_banner_patterns"));
        entries.add(Key.key("recipes"));
        entries.add(Key.key("lodestone_tracker"));
        entries.add(Key.key("firework_explosion"));
        entries.add(Key.key("fireworks"));
        entries.add(Key.key("profile"));
        entries.add(Key.key("note_block_sound"));
        entries.add(Key.key("banner_patterns"));
        entries.add(Key.key("base_color"));
        entries.add(Key.key("pot_decorations"));
        entries.add(Key.key("container"));
        entries.add(Key.key("block_state"));
        entries.add(Key.key("bees"));
        entries.add(Key.key("sulfur_cube_content"));
        entries.add(Key.key("lock"));
        entries.add(Key.key("container_loot"));
        entries.add(Key.key("break_sound"));
        entries.add(Key.key("compostable"));
        entries.add(Key.key("cooking_fuel"));
        entries.add(Key.key("brewing_fuel"));
        entries.add(Key.key("mob_visibility"));
        entries.add(Key.key("villager/variant"));
        entries.add(Key.key("wolf/variant"));
        entries.add(Key.key("wolf/sound_variant"));
        entries.add(Key.key("wolf/collar"));
        entries.add(Key.key("fox/variant"));
        entries.add(Key.key("salmon/size"));
        entries.add(Key.key("parrot/variant"));
        entries.add(Key.key("tropical_fish/pattern"));
        entries.add(Key.key("tropical_fish/base_color"));
        entries.add(Key.key("tropical_fish/pattern_color"));
        entries.add(Key.key("mooshroom/variant"));
        entries.add(Key.key("rabbit/variant"));
        entries.add(Key.key("pig/variant"));
        entries.add(Key.key("pig/sound_variant"));
        entries.add(Key.key("cow/variant"));
        entries.add(Key.key("cow/sound_variant"));
        entries.add(Key.key("chicken/variant"));
        entries.add(Key.key("chicken/sound_variant"));
        entries.add(Key.key("zombie_nautilus/variant"));
        entries.add(Key.key("frog/variant"));
        entries.add(Key.key("horse/variant"));
        entries.add(Key.key("painting/variant"));
        entries.add(Key.key("llama/variant"));
        entries.add(Key.key("axolotl/variant"));
        entries.add(Key.key("cat/variant"));
        entries.add(Key.key("cat/sound_variant"));
        entries.add(Key.key("cat/collar"));
        entries.add(Key.key("sheep/color"));
        entries.add(Key.key("shulker/color"));
        entries.add(Key.key("provides_pottery_pattern"));
        entries.add(Key.key("sign_text_front"));
        entries.add(Key.key("sign_text_back"));
        entries.add(Key.key("waxed"));
        entries.add(Key.key("cushion/color"));
    }

    /**
     * @return {@code minecraft:data_component_type}, indexed by network ID
     */
    private static List<Key> dataComponentType() {
        final List<Key> entries = new ArrayList<>(122);
        dataComponentType0(entries);
        return List.copyOf(entries);
    }

    /**
     * @return the tags of {@code minecraft:data_component_type}, keyed by tag identifier
     */
    private static Map<Key, List<Key>> dataComponentTypeTags() {
        return Map.of();
    }

    private static void fluid0(final List<Key> entries) {
        entries.add(Key.key("empty"));
        entries.add(Key.key("flowing_water"));
        entries.add(Key.key("water"));
        entries.add(Key.key("flowing_lava"));
        entries.add(Key.key("lava"));
    }

    /**
     * @return {@code minecraft:fluid}, indexed by network ID
     */
    private static List<Key> fluid() {
        final List<Key> entries = new ArrayList<>(5);
        fluid0(entries);
        return List.copyOf(entries);
    }

    private static void fluidTags0(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("axolotl_tries_to_find"), List.of(Key.key("water")));
        tags.put(Key.key("bubble_column_can_occupy"), List.of(Key.key("water")));
        tags.put(Key.key("dolphin_tries_to_find"), List.of(Key.key("water")));
        tags.put(Key.key("entity_floatable"), List.of(Key.key("flowing_water"), Key.key("water")));
        tags.put(Key.key("frog_tries_to_find_land_near"), List.of(Key.key("water")));
        tags.put(Key.key("lava"), List.of(Key.key("flowing_lava"), Key.key("lava")));
        tags.put(Key.key("supports_frogspawn"), List.of(Key.key("water")));
        tags.put(Key.key("supports_lily_pad"), List.of(Key.key("water")));
        tags.put(Key.key("supports_sugar_cane_adjacently"), List.of(Key.key("flowing_water"),
                Key.key("water")));
        tags.put(Key.key("water"), List.of(Key.key("flowing_water"), Key.key("water")));
    }

    /**
     * @return the tags of {@code minecraft:fluid}, keyed by tag identifier
     */
    private static Map<Key, List<Key>> fluidTags() {
        final Map<Key, List<Key>> tags = new LinkedHashMap<>(10);
        fluidTags0(tags);
        return Map.copyOf(tags);
    }

    private static void item0(final List<Key> entries) {
        entries.add(Key.key("air"));
        entries.add(Key.key("stone"));
        entries.add(Key.key("granite"));
        entries.add(Key.key("polished_granite"));
        entries.add(Key.key("diorite"));
        entries.add(Key.key("polished_diorite"));
        entries.add(Key.key("andesite"));
        entries.add(Key.key("polished_andesite"));
        entries.add(Key.key("deepslate"));
        entries.add(Key.key("cobbled_deepslate"));
        entries.add(Key.key("polished_deepslate"));
        entries.add(Key.key("calcite"));
        entries.add(Key.key("tuff"));
        entries.add(Key.key("tuff_slab"));
        entries.add(Key.key("tuff_stairs"));
        entries.add(Key.key("tuff_wall"));
        entries.add(Key.key("chiseled_tuff"));
        entries.add(Key.key("polished_tuff"));
        entries.add(Key.key("polished_tuff_slab"));
        entries.add(Key.key("polished_tuff_stairs"));
        entries.add(Key.key("polished_tuff_wall"));
        entries.add(Key.key("tuff_bricks"));
        entries.add(Key.key("tuff_brick_slab"));
        entries.add(Key.key("tuff_brick_stairs"));
        entries.add(Key.key("tuff_brick_wall"));
        entries.add(Key.key("chiseled_tuff_bricks"));
        entries.add(Key.key("sulfur"));
        entries.add(Key.key("potent_sulfur"));
        entries.add(Key.key("sulfur_slab"));
        entries.add(Key.key("sulfur_stairs"));
        entries.add(Key.key("sulfur_wall"));
        entries.add(Key.key("polished_sulfur"));
        entries.add(Key.key("polished_sulfur_slab"));
        entries.add(Key.key("polished_sulfur_stairs"));
        entries.add(Key.key("polished_sulfur_wall"));
        entries.add(Key.key("sulfur_bricks"));
        entries.add(Key.key("sulfur_brick_slab"));
        entries.add(Key.key("sulfur_brick_stairs"));
        entries.add(Key.key("sulfur_brick_wall"));
        entries.add(Key.key("chiseled_sulfur"));
        entries.add(Key.key("cinnabar"));
        entries.add(Key.key("cinnabar_slab"));
        entries.add(Key.key("cinnabar_stairs"));
        entries.add(Key.key("cinnabar_wall"));
        entries.add(Key.key("polished_cinnabar"));
        entries.add(Key.key("polished_cinnabar_slab"));
        entries.add(Key.key("polished_cinnabar_stairs"));
        entries.add(Key.key("polished_cinnabar_wall"));
        entries.add(Key.key("cinnabar_bricks"));
        entries.add(Key.key("cinnabar_brick_slab"));
        entries.add(Key.key("cinnabar_brick_stairs"));
        entries.add(Key.key("cinnabar_brick_wall"));
        entries.add(Key.key("chiseled_cinnabar"));
        entries.add(Key.key("dripstone_block"));
        entries.add(Key.key("grass_block"));
        entries.add(Key.key("dirt"));
        entries.add(Key.key("coarse_dirt"));
        entries.add(Key.key("podzol"));
        entries.add(Key.key("rooted_dirt"));
        entries.add(Key.key("mud"));
        entries.add(Key.key("crimson_nylium"));
        entries.add(Key.key("warped_nylium"));
        entries.add(Key.key("cobblestone"));
        entries.add(Key.key("oak_planks"));
        entries.add(Key.key("spruce_planks"));
        entries.add(Key.key("birch_planks"));
        entries.add(Key.key("jungle_planks"));
        entries.add(Key.key("acacia_planks"));
        entries.add(Key.key("cherry_planks"));
        entries.add(Key.key("dark_oak_planks"));
        entries.add(Key.key("pale_oak_planks"));
        entries.add(Key.key("mangrove_planks"));
        entries.add(Key.key("poplar_planks"));
        entries.add(Key.key("bamboo_planks"));
        entries.add(Key.key("crimson_planks"));
        entries.add(Key.key("warped_planks"));
        entries.add(Key.key("bamboo_mosaic"));
        entries.add(Key.key("oak_sapling"));
        entries.add(Key.key("spruce_sapling"));
        entries.add(Key.key("birch_sapling"));
        entries.add(Key.key("jungle_sapling"));
        entries.add(Key.key("acacia_sapling"));
        entries.add(Key.key("cherry_sapling"));
        entries.add(Key.key("dark_oak_sapling"));
        entries.add(Key.key("pale_oak_sapling"));
        entries.add(Key.key("mangrove_propagule"));
        entries.add(Key.key("poplar_sapling"));
        entries.add(Key.key("bedrock"));
        entries.add(Key.key("sand"));
        entries.add(Key.key("suspicious_sand"));
        entries.add(Key.key("suspicious_gravel"));
        entries.add(Key.key("red_sand"));
        entries.add(Key.key("gravel"));
        entries.add(Key.key("coal_ore"));
        entries.add(Key.key("deepslate_coal_ore"));
        entries.add(Key.key("iron_ore"));
        entries.add(Key.key("deepslate_iron_ore"));
        entries.add(Key.key("copper_ore"));
        entries.add(Key.key("deepslate_copper_ore"));
        entries.add(Key.key("gold_ore"));
        entries.add(Key.key("deepslate_gold_ore"));
        entries.add(Key.key("redstone_ore"));
        entries.add(Key.key("deepslate_redstone_ore"));
        entries.add(Key.key("emerald_ore"));
        entries.add(Key.key("deepslate_emerald_ore"));
        entries.add(Key.key("lapis_ore"));
        entries.add(Key.key("deepslate_lapis_ore"));
        entries.add(Key.key("diamond_ore"));
        entries.add(Key.key("deepslate_diamond_ore"));
        entries.add(Key.key("nether_gold_ore"));
        entries.add(Key.key("nether_quartz_ore"));
        entries.add(Key.key("ancient_debris"));
        entries.add(Key.key("coal_block"));
        entries.add(Key.key("raw_iron_block"));
        entries.add(Key.key("raw_copper_block"));
        entries.add(Key.key("raw_gold_block"));
        entries.add(Key.key("heavy_core"));
        entries.add(Key.key("amethyst_block"));
        entries.add(Key.key("budding_amethyst"));
        entries.add(Key.key("iron_block"));
        entries.add(Key.key("copper_block"));
        entries.add(Key.key("exposed_copper"));
        entries.add(Key.key("weathered_copper"));
        entries.add(Key.key("oxidized_copper"));
        entries.add(Key.key("waxed_copper_block"));
        entries.add(Key.key("waxed_exposed_copper"));
        entries.add(Key.key("waxed_weathered_copper"));
        entries.add(Key.key("waxed_oxidized_copper"));
        entries.add(Key.key("gold_block"));
        entries.add(Key.key("diamond_block"));
        entries.add(Key.key("netherite_block"));
        entries.add(Key.key("chiseled_copper"));
        entries.add(Key.key("exposed_chiseled_copper"));
        entries.add(Key.key("weathered_chiseled_copper"));
        entries.add(Key.key("oxidized_chiseled_copper"));
        entries.add(Key.key("waxed_chiseled_copper"));
        entries.add(Key.key("waxed_exposed_chiseled_copper"));
        entries.add(Key.key("waxed_weathered_chiseled_copper"));
        entries.add(Key.key("waxed_oxidized_chiseled_copper"));
        entries.add(Key.key("cut_copper"));
        entries.add(Key.key("exposed_cut_copper"));
        entries.add(Key.key("weathered_cut_copper"));
        entries.add(Key.key("oxidized_cut_copper"));
        entries.add(Key.key("waxed_cut_copper"));
        entries.add(Key.key("waxed_exposed_cut_copper"));
        entries.add(Key.key("waxed_weathered_cut_copper"));
        entries.add(Key.key("waxed_oxidized_cut_copper"));
        entries.add(Key.key("cut_copper_stairs"));
        entries.add(Key.key("exposed_cut_copper_stairs"));
        entries.add(Key.key("weathered_cut_copper_stairs"));
        entries.add(Key.key("oxidized_cut_copper_stairs"));
        entries.add(Key.key("waxed_cut_copper_stairs"));
        entries.add(Key.key("waxed_exposed_cut_copper_stairs"));
        entries.add(Key.key("waxed_weathered_cut_copper_stairs"));
        entries.add(Key.key("waxed_oxidized_cut_copper_stairs"));
        entries.add(Key.key("cut_copper_slab"));
        entries.add(Key.key("exposed_cut_copper_slab"));
        entries.add(Key.key("weathered_cut_copper_slab"));
        entries.add(Key.key("oxidized_cut_copper_slab"));
        entries.add(Key.key("waxed_cut_copper_slab"));
        entries.add(Key.key("waxed_exposed_cut_copper_slab"));
        entries.add(Key.key("waxed_weathered_cut_copper_slab"));
        entries.add(Key.key("waxed_oxidized_cut_copper_slab"));
        entries.add(Key.key("oak_log"));
        entries.add(Key.key("spruce_log"));
        entries.add(Key.key("birch_log"));
        entries.add(Key.key("jungle_log"));
        entries.add(Key.key("acacia_log"));
        entries.add(Key.key("cherry_log"));
        entries.add(Key.key("pale_oak_log"));
        entries.add(Key.key("dark_oak_log"));
        entries.add(Key.key("mangrove_log"));
        entries.add(Key.key("poplar_log"));
        entries.add(Key.key("mangrove_roots"));
        entries.add(Key.key("muddy_mangrove_roots"));
        entries.add(Key.key("crimson_stem"));
        entries.add(Key.key("warped_stem"));
        entries.add(Key.key("bamboo_block"));
        entries.add(Key.key("stripped_oak_log"));
        entries.add(Key.key("stripped_spruce_log"));
        entries.add(Key.key("stripped_birch_log"));
        entries.add(Key.key("stripped_jungle_log"));
        entries.add(Key.key("stripped_acacia_log"));
        entries.add(Key.key("stripped_cherry_log"));
        entries.add(Key.key("stripped_dark_oak_log"));
        entries.add(Key.key("stripped_pale_oak_log"));
        entries.add(Key.key("stripped_mangrove_log"));
        entries.add(Key.key("stripped_poplar_log"));
        entries.add(Key.key("stripped_crimson_stem"));
        entries.add(Key.key("stripped_warped_stem"));
        entries.add(Key.key("stripped_oak_wood"));
        entries.add(Key.key("stripped_spruce_wood"));
        entries.add(Key.key("stripped_birch_wood"));
        entries.add(Key.key("stripped_jungle_wood"));
        entries.add(Key.key("stripped_acacia_wood"));
        entries.add(Key.key("stripped_cherry_wood"));
        entries.add(Key.key("stripped_dark_oak_wood"));
        entries.add(Key.key("stripped_pale_oak_wood"));
        entries.add(Key.key("stripped_mangrove_wood"));
        entries.add(Key.key("stripped_poplar_wood"));
    }

    private static void item1(final List<Key> entries) {
        entries.add(Key.key("stripped_crimson_hyphae"));
        entries.add(Key.key("stripped_warped_hyphae"));
        entries.add(Key.key("stripped_bamboo_block"));
        entries.add(Key.key("oak_wood"));
        entries.add(Key.key("spruce_wood"));
        entries.add(Key.key("birch_wood"));
        entries.add(Key.key("jungle_wood"));
        entries.add(Key.key("acacia_wood"));
        entries.add(Key.key("cherry_wood"));
        entries.add(Key.key("pale_oak_wood"));
        entries.add(Key.key("dark_oak_wood"));
        entries.add(Key.key("mangrove_wood"));
        entries.add(Key.key("poplar_wood"));
        entries.add(Key.key("crimson_hyphae"));
        entries.add(Key.key("warped_hyphae"));
        entries.add(Key.key("oak_leaves"));
        entries.add(Key.key("spruce_leaves"));
        entries.add(Key.key("birch_leaves"));
        entries.add(Key.key("jungle_leaves"));
        entries.add(Key.key("acacia_leaves"));
        entries.add(Key.key("cherry_leaves"));
        entries.add(Key.key("dark_oak_leaves"));
        entries.add(Key.key("pale_oak_leaves"));
        entries.add(Key.key("mangrove_leaves"));
        entries.add(Key.key("red_poplar_leaves"));
        entries.add(Key.key("orange_poplar_leaves"));
        entries.add(Key.key("yellow_poplar_leaves"));
        entries.add(Key.key("azalea_leaves"));
        entries.add(Key.key("flowering_azalea_leaves"));
        entries.add(Key.key("sponge"));
        entries.add(Key.key("wet_sponge"));
        entries.add(Key.key("glass"));
        entries.add(Key.key("tinted_glass"));
        entries.add(Key.key("lapis_block"));
        entries.add(Key.key("sandstone"));
        entries.add(Key.key("chiseled_sandstone"));
        entries.add(Key.key("cut_sandstone"));
        entries.add(Key.key("cobweb"));
        entries.add(Key.key("short_grass"));
        entries.add(Key.key("fern"));
        entries.add(Key.key("bush"));
        entries.add(Key.key("red_shrub"));
        entries.add(Key.key("azalea"));
        entries.add(Key.key("flowering_azalea"));
        entries.add(Key.key("dead_bush"));
        entries.add(Key.key("firefly_bush"));
        entries.add(Key.key("short_dry_grass"));
        entries.add(Key.key("tall_dry_grass"));
        entries.add(Key.key("seagrass"));
        entries.add(Key.key("sea_pickle"));
        entries.add(Key.key("white_wool"));
        entries.add(Key.key("orange_wool"));
        entries.add(Key.key("magenta_wool"));
        entries.add(Key.key("light_blue_wool"));
        entries.add(Key.key("yellow_wool"));
        entries.add(Key.key("lime_wool"));
        entries.add(Key.key("pink_wool"));
        entries.add(Key.key("gray_wool"));
        entries.add(Key.key("light_gray_wool"));
        entries.add(Key.key("cyan_wool"));
        entries.add(Key.key("purple_wool"));
        entries.add(Key.key("blue_wool"));
        entries.add(Key.key("brown_wool"));
        entries.add(Key.key("green_wool"));
        entries.add(Key.key("red_wool"));
        entries.add(Key.key("black_wool"));
        entries.add(Key.key("white_wool_stairs"));
        entries.add(Key.key("orange_wool_stairs"));
        entries.add(Key.key("magenta_wool_stairs"));
        entries.add(Key.key("light_blue_wool_stairs"));
        entries.add(Key.key("yellow_wool_stairs"));
        entries.add(Key.key("lime_wool_stairs"));
        entries.add(Key.key("pink_wool_stairs"));
        entries.add(Key.key("gray_wool_stairs"));
        entries.add(Key.key("light_gray_wool_stairs"));
        entries.add(Key.key("cyan_wool_stairs"));
        entries.add(Key.key("purple_wool_stairs"));
        entries.add(Key.key("blue_wool_stairs"));
        entries.add(Key.key("brown_wool_stairs"));
        entries.add(Key.key("green_wool_stairs"));
        entries.add(Key.key("red_wool_stairs"));
        entries.add(Key.key("black_wool_stairs"));
        entries.add(Key.key("white_wool_slab"));
        entries.add(Key.key("orange_wool_slab"));
        entries.add(Key.key("magenta_wool_slab"));
        entries.add(Key.key("light_blue_wool_slab"));
        entries.add(Key.key("yellow_wool_slab"));
        entries.add(Key.key("lime_wool_slab"));
        entries.add(Key.key("pink_wool_slab"));
        entries.add(Key.key("gray_wool_slab"));
        entries.add(Key.key("light_gray_wool_slab"));
        entries.add(Key.key("cyan_wool_slab"));
        entries.add(Key.key("purple_wool_slab"));
        entries.add(Key.key("blue_wool_slab"));
        entries.add(Key.key("brown_wool_slab"));
        entries.add(Key.key("green_wool_slab"));
        entries.add(Key.key("red_wool_slab"));
        entries.add(Key.key("black_wool_slab"));
        entries.add(Key.key("dandelion"));
        entries.add(Key.key("golden_dandelion"));
        entries.add(Key.key("open_eyeblossom"));
        entries.add(Key.key("closed_eyeblossom"));
        entries.add(Key.key("poppy"));
        entries.add(Key.key("blue_orchid"));
        entries.add(Key.key("allium"));
        entries.add(Key.key("azure_bluet"));
        entries.add(Key.key("red_tulip"));
        entries.add(Key.key("orange_tulip"));
        entries.add(Key.key("white_tulip"));
        entries.add(Key.key("pink_tulip"));
        entries.add(Key.key("oxeye_daisy"));
        entries.add(Key.key("cornflower"));
        entries.add(Key.key("lily_of_the_valley"));
        entries.add(Key.key("wither_rose"));
        entries.add(Key.key("torchflower"));
        entries.add(Key.key("pitcher_plant"));
        entries.add(Key.key("spore_blossom"));
        entries.add(Key.key("brown_mushroom"));
        entries.add(Key.key("red_mushroom"));
        entries.add(Key.key("shelf_mushroom"));
        entries.add(Key.key("crimson_fungus"));
        entries.add(Key.key("warped_fungus"));
        entries.add(Key.key("crimson_roots"));
        entries.add(Key.key("warped_roots"));
        entries.add(Key.key("nether_sprouts"));
        entries.add(Key.key("weeping_vines"));
        entries.add(Key.key("twisting_vines"));
        entries.add(Key.key("sugar_cane"));
        entries.add(Key.key("kelp"));
        entries.add(Key.key("pink_petals"));
        entries.add(Key.key("wildflowers"));
        entries.add(Key.key("leaf_litter"));
        entries.add(Key.key("moss_carpet"));
        entries.add(Key.key("moss_block"));
        entries.add(Key.key("pale_moss_carpet"));
        entries.add(Key.key("pale_hanging_moss"));
        entries.add(Key.key("pale_moss_block"));
        entries.add(Key.key("hanging_roots"));
        entries.add(Key.key("big_dripleaf"));
        entries.add(Key.key("small_dripleaf"));
        entries.add(Key.key("bamboo"));
        entries.add(Key.key("oak_slab"));
        entries.add(Key.key("spruce_slab"));
        entries.add(Key.key("birch_slab"));
        entries.add(Key.key("jungle_slab"));
        entries.add(Key.key("acacia_slab"));
        entries.add(Key.key("cherry_slab"));
        entries.add(Key.key("dark_oak_slab"));
        entries.add(Key.key("pale_oak_slab"));
        entries.add(Key.key("mangrove_slab"));
        entries.add(Key.key("poplar_slab"));
        entries.add(Key.key("bamboo_slab"));
        entries.add(Key.key("bamboo_mosaic_slab"));
        entries.add(Key.key("crimson_slab"));
        entries.add(Key.key("warped_slab"));
        entries.add(Key.key("stone_slab"));
        entries.add(Key.key("smooth_stone_slab"));
        entries.add(Key.key("sandstone_slab"));
        entries.add(Key.key("cut_sandstone_slab"));
        entries.add(Key.key("petrified_oak_slab"));
        entries.add(Key.key("cobblestone_slab"));
        entries.add(Key.key("brick_slab"));
        entries.add(Key.key("stone_brick_slab"));
        entries.add(Key.key("mud_brick_slab"));
        entries.add(Key.key("nether_brick_slab"));
        entries.add(Key.key("quartz_slab"));
        entries.add(Key.key("red_sandstone_slab"));
        entries.add(Key.key("cut_red_sandstone_slab"));
        entries.add(Key.key("purpur_slab"));
        entries.add(Key.key("prismarine_slab"));
        entries.add(Key.key("prismarine_brick_slab"));
        entries.add(Key.key("dark_prismarine_slab"));
        entries.add(Key.key("smooth_quartz"));
        entries.add(Key.key("smooth_red_sandstone"));
        entries.add(Key.key("smooth_sandstone"));
        entries.add(Key.key("smooth_stone"));
        entries.add(Key.key("bricks"));
        entries.add(Key.key("acacia_shelf"));
        entries.add(Key.key("bamboo_shelf"));
        entries.add(Key.key("birch_shelf"));
        entries.add(Key.key("cherry_shelf"));
        entries.add(Key.key("crimson_shelf"));
        entries.add(Key.key("dark_oak_shelf"));
        entries.add(Key.key("jungle_shelf"));
        entries.add(Key.key("mangrove_shelf"));
        entries.add(Key.key("poplar_shelf"));
        entries.add(Key.key("oak_shelf"));
        entries.add(Key.key("pale_oak_shelf"));
        entries.add(Key.key("spruce_shelf"));
        entries.add(Key.key("warped_shelf"));
        entries.add(Key.key("bookshelf"));
        entries.add(Key.key("chiseled_bookshelf"));
        entries.add(Key.key("decorated_pot"));
        entries.add(Key.key("mossy_cobblestone"));
        entries.add(Key.key("obsidian"));
        entries.add(Key.key("torch"));
        entries.add(Key.key("end_rod"));
        entries.add(Key.key("chorus_plant"));
        entries.add(Key.key("chorus_flower"));
        entries.add(Key.key("purpur_block"));
    }

    private static void item2(final List<Key> entries) {
        entries.add(Key.key("purpur_pillar"));
        entries.add(Key.key("purpur_stairs"));
        entries.add(Key.key("spawner"));
        entries.add(Key.key("creaking_heart"));
        entries.add(Key.key("chest"));
        entries.add(Key.key("crafting_table"));
        entries.add(Key.key("farmland"));
        entries.add(Key.key("furnace"));
        entries.add(Key.key("ladder"));
        entries.add(Key.key("cobblestone_stairs"));
        entries.add(Key.key("snow"));
        entries.add(Key.key("ice"));
        entries.add(Key.key("snow_block"));
        entries.add(Key.key("cactus"));
        entries.add(Key.key("cactus_flower"));
        entries.add(Key.key("clay"));
        entries.add(Key.key("jukebox"));
        entries.add(Key.key("oak_fence"));
        entries.add(Key.key("spruce_fence"));
        entries.add(Key.key("birch_fence"));
        entries.add(Key.key("jungle_fence"));
        entries.add(Key.key("acacia_fence"));
        entries.add(Key.key("cherry_fence"));
        entries.add(Key.key("dark_oak_fence"));
        entries.add(Key.key("pale_oak_fence"));
        entries.add(Key.key("mangrove_fence"));
        entries.add(Key.key("poplar_fence"));
        entries.add(Key.key("bamboo_fence"));
        entries.add(Key.key("crimson_fence"));
        entries.add(Key.key("warped_fence"));
        entries.add(Key.key("pumpkin"));
        entries.add(Key.key("carved_pumpkin"));
        entries.add(Key.key("jack_o_lantern"));
        entries.add(Key.key("netherrack"));
        entries.add(Key.key("soul_sand"));
        entries.add(Key.key("soul_soil"));
        entries.add(Key.key("basalt"));
        entries.add(Key.key("polished_basalt"));
        entries.add(Key.key("smooth_basalt"));
        entries.add(Key.key("soul_torch"));
        entries.add(Key.key("copper_torch"));
        entries.add(Key.key("glowstone"));
        entries.add(Key.key("infested_stone"));
        entries.add(Key.key("infested_cobblestone"));
        entries.add(Key.key("infested_stone_bricks"));
        entries.add(Key.key("infested_mossy_stone_bricks"));
        entries.add(Key.key("infested_cracked_stone_bricks"));
        entries.add(Key.key("infested_chiseled_stone_bricks"));
        entries.add(Key.key("infested_deepslate"));
        entries.add(Key.key("stone_bricks"));
        entries.add(Key.key("mossy_stone_bricks"));
        entries.add(Key.key("cracked_stone_bricks"));
        entries.add(Key.key("chiseled_stone_bricks"));
        entries.add(Key.key("packed_mud"));
        entries.add(Key.key("mud_bricks"));
        entries.add(Key.key("deepslate_bricks"));
        entries.add(Key.key("cracked_deepslate_bricks"));
        entries.add(Key.key("deepslate_tiles"));
        entries.add(Key.key("cracked_deepslate_tiles"));
        entries.add(Key.key("chiseled_deepslate"));
        entries.add(Key.key("reinforced_deepslate"));
        entries.add(Key.key("brown_mushroom_block"));
        entries.add(Key.key("red_mushroom_block"));
        entries.add(Key.key("mushroom_stem"));
        entries.add(Key.key("iron_bars"));
        entries.add(Key.key("copper_bars"));
        entries.add(Key.key("exposed_copper_bars"));
        entries.add(Key.key("weathered_copper_bars"));
        entries.add(Key.key("oxidized_copper_bars"));
        entries.add(Key.key("waxed_copper_bars"));
        entries.add(Key.key("waxed_exposed_copper_bars"));
        entries.add(Key.key("waxed_weathered_copper_bars"));
        entries.add(Key.key("waxed_oxidized_copper_bars"));
        entries.add(Key.key("iron_chain"));
        entries.add(Key.key("copper_chain"));
        entries.add(Key.key("exposed_copper_chain"));
        entries.add(Key.key("weathered_copper_chain"));
        entries.add(Key.key("oxidized_copper_chain"));
        entries.add(Key.key("waxed_copper_chain"));
        entries.add(Key.key("waxed_exposed_copper_chain"));
        entries.add(Key.key("waxed_weathered_copper_chain"));
        entries.add(Key.key("waxed_oxidized_copper_chain"));
        entries.add(Key.key("glass_pane"));
        entries.add(Key.key("melon"));
        entries.add(Key.key("vine"));
        entries.add(Key.key("glow_lichen"));
        entries.add(Key.key("resin_clump"));
        entries.add(Key.key("resin_block"));
        entries.add(Key.key("resin_bricks"));
        entries.add(Key.key("resin_brick_stairs"));
        entries.add(Key.key("resin_brick_slab"));
        entries.add(Key.key("resin_brick_wall"));
        entries.add(Key.key("chiseled_resin_bricks"));
        entries.add(Key.key("brick_stairs"));
        entries.add(Key.key("stone_brick_stairs"));
        entries.add(Key.key("mud_brick_stairs"));
        entries.add(Key.key("mycelium"));
        entries.add(Key.key("lily_pad"));
        entries.add(Key.key("nether_bricks"));
        entries.add(Key.key("cracked_nether_bricks"));
        entries.add(Key.key("chiseled_nether_bricks"));
        entries.add(Key.key("nether_brick_fence"));
        entries.add(Key.key("nether_brick_stairs"));
        entries.add(Key.key("sculk"));
        entries.add(Key.key("sculk_vein"));
        entries.add(Key.key("sculk_catalyst"));
        entries.add(Key.key("sculk_shrieker"));
        entries.add(Key.key("enchanting_table"));
        entries.add(Key.key("end_portal_frame"));
        entries.add(Key.key("end_stone"));
        entries.add(Key.key("end_stone_bricks"));
        entries.add(Key.key("dragon_egg"));
        entries.add(Key.key("sandstone_stairs"));
        entries.add(Key.key("ender_chest"));
        entries.add(Key.key("emerald_block"));
        entries.add(Key.key("oak_stairs"));
        entries.add(Key.key("spruce_stairs"));
        entries.add(Key.key("birch_stairs"));
        entries.add(Key.key("jungle_stairs"));
        entries.add(Key.key("acacia_stairs"));
        entries.add(Key.key("cherry_stairs"));
        entries.add(Key.key("dark_oak_stairs"));
        entries.add(Key.key("pale_oak_stairs"));
        entries.add(Key.key("mangrove_stairs"));
        entries.add(Key.key("poplar_stairs"));
        entries.add(Key.key("bamboo_stairs"));
        entries.add(Key.key("bamboo_mosaic_stairs"));
        entries.add(Key.key("crimson_stairs"));
        entries.add(Key.key("warped_stairs"));
        entries.add(Key.key("command_block"));
        entries.add(Key.key("beacon"));
        entries.add(Key.key("cobblestone_wall"));
        entries.add(Key.key("mossy_cobblestone_wall"));
        entries.add(Key.key("brick_wall"));
        entries.add(Key.key("prismarine_wall"));
        entries.add(Key.key("red_sandstone_wall"));
        entries.add(Key.key("mossy_stone_brick_wall"));
        entries.add(Key.key("granite_wall"));
        entries.add(Key.key("stone_brick_wall"));
        entries.add(Key.key("mud_brick_wall"));
        entries.add(Key.key("nether_brick_wall"));
        entries.add(Key.key("andesite_wall"));
        entries.add(Key.key("red_nether_brick_wall"));
        entries.add(Key.key("sandstone_wall"));
        entries.add(Key.key("end_stone_brick_wall"));
        entries.add(Key.key("diorite_wall"));
        entries.add(Key.key("blackstone_wall"));
        entries.add(Key.key("polished_blackstone_wall"));
        entries.add(Key.key("polished_blackstone_brick_wall"));
        entries.add(Key.key("cobbled_deepslate_wall"));
        entries.add(Key.key("polished_deepslate_wall"));
        entries.add(Key.key("deepslate_brick_wall"));
        entries.add(Key.key("deepslate_tile_wall"));
        entries.add(Key.key("anvil"));
        entries.add(Key.key("chipped_anvil"));
        entries.add(Key.key("damaged_anvil"));
        entries.add(Key.key("chiseled_quartz_block"));
        entries.add(Key.key("quartz_block"));
        entries.add(Key.key("quartz_bricks"));
        entries.add(Key.key("quartz_pillar"));
        entries.add(Key.key("quartz_stairs"));
        entries.add(Key.key("white_terracotta"));
        entries.add(Key.key("orange_terracotta"));
        entries.add(Key.key("magenta_terracotta"));
        entries.add(Key.key("light_blue_terracotta"));
        entries.add(Key.key("yellow_terracotta"));
        entries.add(Key.key("lime_terracotta"));
        entries.add(Key.key("pink_terracotta"));
        entries.add(Key.key("gray_terracotta"));
        entries.add(Key.key("light_gray_terracotta"));
        entries.add(Key.key("cyan_terracotta"));
        entries.add(Key.key("purple_terracotta"));
        entries.add(Key.key("blue_terracotta"));
        entries.add(Key.key("brown_terracotta"));
        entries.add(Key.key("green_terracotta"));
        entries.add(Key.key("red_terracotta"));
        entries.add(Key.key("black_terracotta"));
        entries.add(Key.key("barrier"));
        entries.add(Key.key("light"));
        entries.add(Key.key("hay_block"));
        entries.add(Key.key("white_carpet"));
        entries.add(Key.key("orange_carpet"));
        entries.add(Key.key("magenta_carpet"));
        entries.add(Key.key("light_blue_carpet"));
        entries.add(Key.key("yellow_carpet"));
        entries.add(Key.key("lime_carpet"));
        entries.add(Key.key("pink_carpet"));
        entries.add(Key.key("gray_carpet"));
        entries.add(Key.key("light_gray_carpet"));
        entries.add(Key.key("cyan_carpet"));
        entries.add(Key.key("purple_carpet"));
        entries.add(Key.key("blue_carpet"));
        entries.add(Key.key("brown_carpet"));
        entries.add(Key.key("green_carpet"));
        entries.add(Key.key("red_carpet"));
        entries.add(Key.key("black_carpet"));
        entries.add(Key.key("terracotta"));
        entries.add(Key.key("packed_ice"));
        entries.add(Key.key("dirt_path"));
        entries.add(Key.key("sunflower"));
    }

    private static void item3(final List<Key> entries) {
        entries.add(Key.key("lilac"));
        entries.add(Key.key("rose_bush"));
        entries.add(Key.key("peony"));
        entries.add(Key.key("tall_grass"));
        entries.add(Key.key("large_fern"));
        entries.add(Key.key("white_stained_glass"));
        entries.add(Key.key("orange_stained_glass"));
        entries.add(Key.key("magenta_stained_glass"));
        entries.add(Key.key("light_blue_stained_glass"));
        entries.add(Key.key("yellow_stained_glass"));
        entries.add(Key.key("lime_stained_glass"));
        entries.add(Key.key("pink_stained_glass"));
        entries.add(Key.key("gray_stained_glass"));
        entries.add(Key.key("light_gray_stained_glass"));
        entries.add(Key.key("cyan_stained_glass"));
        entries.add(Key.key("purple_stained_glass"));
        entries.add(Key.key("blue_stained_glass"));
        entries.add(Key.key("brown_stained_glass"));
        entries.add(Key.key("green_stained_glass"));
        entries.add(Key.key("red_stained_glass"));
        entries.add(Key.key("black_stained_glass"));
        entries.add(Key.key("white_stained_glass_pane"));
        entries.add(Key.key("orange_stained_glass_pane"));
        entries.add(Key.key("magenta_stained_glass_pane"));
        entries.add(Key.key("light_blue_stained_glass_pane"));
        entries.add(Key.key("yellow_stained_glass_pane"));
        entries.add(Key.key("lime_stained_glass_pane"));
        entries.add(Key.key("pink_stained_glass_pane"));
        entries.add(Key.key("gray_stained_glass_pane"));
        entries.add(Key.key("light_gray_stained_glass_pane"));
        entries.add(Key.key("cyan_stained_glass_pane"));
        entries.add(Key.key("purple_stained_glass_pane"));
        entries.add(Key.key("blue_stained_glass_pane"));
        entries.add(Key.key("brown_stained_glass_pane"));
        entries.add(Key.key("green_stained_glass_pane"));
        entries.add(Key.key("red_stained_glass_pane"));
        entries.add(Key.key("black_stained_glass_pane"));
        entries.add(Key.key("prismarine"));
        entries.add(Key.key("prismarine_bricks"));
        entries.add(Key.key("dark_prismarine"));
        entries.add(Key.key("prismarine_stairs"));
        entries.add(Key.key("prismarine_brick_stairs"));
        entries.add(Key.key("dark_prismarine_stairs"));
        entries.add(Key.key("sea_lantern"));
        entries.add(Key.key("red_sandstone"));
        entries.add(Key.key("chiseled_red_sandstone"));
        entries.add(Key.key("cut_red_sandstone"));
        entries.add(Key.key("red_sandstone_stairs"));
        entries.add(Key.key("repeating_command_block"));
        entries.add(Key.key("chain_command_block"));
        entries.add(Key.key("magma_block"));
        entries.add(Key.key("nether_wart_block"));
        entries.add(Key.key("warped_wart_block"));
        entries.add(Key.key("red_nether_bricks"));
        entries.add(Key.key("bone_block"));
        entries.add(Key.key("structure_void"));
        entries.add(Key.key("shulker_box"));
        entries.add(Key.key("white_shulker_box"));
        entries.add(Key.key("orange_shulker_box"));
        entries.add(Key.key("magenta_shulker_box"));
        entries.add(Key.key("light_blue_shulker_box"));
        entries.add(Key.key("yellow_shulker_box"));
        entries.add(Key.key("lime_shulker_box"));
        entries.add(Key.key("pink_shulker_box"));
        entries.add(Key.key("gray_shulker_box"));
        entries.add(Key.key("light_gray_shulker_box"));
        entries.add(Key.key("cyan_shulker_box"));
        entries.add(Key.key("purple_shulker_box"));
        entries.add(Key.key("blue_shulker_box"));
        entries.add(Key.key("brown_shulker_box"));
        entries.add(Key.key("green_shulker_box"));
        entries.add(Key.key("red_shulker_box"));
        entries.add(Key.key("black_shulker_box"));
        entries.add(Key.key("white_glazed_terracotta"));
        entries.add(Key.key("orange_glazed_terracotta"));
        entries.add(Key.key("magenta_glazed_terracotta"));
        entries.add(Key.key("light_blue_glazed_terracotta"));
        entries.add(Key.key("yellow_glazed_terracotta"));
        entries.add(Key.key("lime_glazed_terracotta"));
        entries.add(Key.key("pink_glazed_terracotta"));
        entries.add(Key.key("gray_glazed_terracotta"));
        entries.add(Key.key("light_gray_glazed_terracotta"));
        entries.add(Key.key("cyan_glazed_terracotta"));
        entries.add(Key.key("purple_glazed_terracotta"));
        entries.add(Key.key("blue_glazed_terracotta"));
        entries.add(Key.key("brown_glazed_terracotta"));
        entries.add(Key.key("green_glazed_terracotta"));
        entries.add(Key.key("red_glazed_terracotta"));
        entries.add(Key.key("black_glazed_terracotta"));
        entries.add(Key.key("white_concrete"));
        entries.add(Key.key("orange_concrete"));
        entries.add(Key.key("magenta_concrete"));
        entries.add(Key.key("light_blue_concrete"));
        entries.add(Key.key("yellow_concrete"));
        entries.add(Key.key("lime_concrete"));
        entries.add(Key.key("pink_concrete"));
        entries.add(Key.key("gray_concrete"));
        entries.add(Key.key("light_gray_concrete"));
        entries.add(Key.key("cyan_concrete"));
        entries.add(Key.key("purple_concrete"));
        entries.add(Key.key("blue_concrete"));
        entries.add(Key.key("brown_concrete"));
        entries.add(Key.key("green_concrete"));
        entries.add(Key.key("red_concrete"));
        entries.add(Key.key("black_concrete"));
        entries.add(Key.key("white_concrete_stairs"));
        entries.add(Key.key("orange_concrete_stairs"));
        entries.add(Key.key("magenta_concrete_stairs"));
        entries.add(Key.key("light_blue_concrete_stairs"));
        entries.add(Key.key("yellow_concrete_stairs"));
        entries.add(Key.key("lime_concrete_stairs"));
        entries.add(Key.key("pink_concrete_stairs"));
        entries.add(Key.key("gray_concrete_stairs"));
        entries.add(Key.key("light_gray_concrete_stairs"));
        entries.add(Key.key("cyan_concrete_stairs"));
        entries.add(Key.key("purple_concrete_stairs"));
        entries.add(Key.key("blue_concrete_stairs"));
        entries.add(Key.key("brown_concrete_stairs"));
        entries.add(Key.key("green_concrete_stairs"));
        entries.add(Key.key("red_concrete_stairs"));
        entries.add(Key.key("black_concrete_stairs"));
        entries.add(Key.key("white_concrete_slab"));
        entries.add(Key.key("orange_concrete_slab"));
        entries.add(Key.key("magenta_concrete_slab"));
        entries.add(Key.key("light_blue_concrete_slab"));
        entries.add(Key.key("yellow_concrete_slab"));
        entries.add(Key.key("lime_concrete_slab"));
        entries.add(Key.key("pink_concrete_slab"));
        entries.add(Key.key("gray_concrete_slab"));
        entries.add(Key.key("light_gray_concrete_slab"));
        entries.add(Key.key("cyan_concrete_slab"));
        entries.add(Key.key("purple_concrete_slab"));
        entries.add(Key.key("blue_concrete_slab"));
        entries.add(Key.key("brown_concrete_slab"));
        entries.add(Key.key("green_concrete_slab"));
        entries.add(Key.key("red_concrete_slab"));
        entries.add(Key.key("black_concrete_slab"));
        entries.add(Key.key("white_concrete_powder"));
        entries.add(Key.key("orange_concrete_powder"));
        entries.add(Key.key("magenta_concrete_powder"));
        entries.add(Key.key("light_blue_concrete_powder"));
        entries.add(Key.key("yellow_concrete_powder"));
        entries.add(Key.key("lime_concrete_powder"));
        entries.add(Key.key("pink_concrete_powder"));
        entries.add(Key.key("gray_concrete_powder"));
        entries.add(Key.key("light_gray_concrete_powder"));
        entries.add(Key.key("cyan_concrete_powder"));
        entries.add(Key.key("purple_concrete_powder"));
        entries.add(Key.key("blue_concrete_powder"));
        entries.add(Key.key("brown_concrete_powder"));
        entries.add(Key.key("green_concrete_powder"));
        entries.add(Key.key("red_concrete_powder"));
        entries.add(Key.key("black_concrete_powder"));
        entries.add(Key.key("turtle_egg"));
        entries.add(Key.key("sniffer_egg"));
        entries.add(Key.key("dried_ghast"));
        entries.add(Key.key("dead_tube_coral_block"));
        entries.add(Key.key("dead_brain_coral_block"));
        entries.add(Key.key("dead_bubble_coral_block"));
        entries.add(Key.key("dead_fire_coral_block"));
        entries.add(Key.key("dead_horn_coral_block"));
        entries.add(Key.key("tube_coral_block"));
        entries.add(Key.key("brain_coral_block"));
        entries.add(Key.key("bubble_coral_block"));
        entries.add(Key.key("fire_coral_block"));
        entries.add(Key.key("horn_coral_block"));
        entries.add(Key.key("tube_coral"));
        entries.add(Key.key("brain_coral"));
        entries.add(Key.key("bubble_coral"));
        entries.add(Key.key("fire_coral"));
        entries.add(Key.key("horn_coral"));
        entries.add(Key.key("dead_brain_coral"));
        entries.add(Key.key("dead_bubble_coral"));
        entries.add(Key.key("dead_fire_coral"));
        entries.add(Key.key("dead_horn_coral"));
        entries.add(Key.key("dead_tube_coral"));
        entries.add(Key.key("tube_coral_fan"));
        entries.add(Key.key("brain_coral_fan"));
        entries.add(Key.key("bubble_coral_fan"));
        entries.add(Key.key("fire_coral_fan"));
        entries.add(Key.key("horn_coral_fan"));
        entries.add(Key.key("dead_tube_coral_fan"));
        entries.add(Key.key("dead_brain_coral_fan"));
        entries.add(Key.key("dead_bubble_coral_fan"));
        entries.add(Key.key("dead_fire_coral_fan"));
        entries.add(Key.key("dead_horn_coral_fan"));
        entries.add(Key.key("blue_ice"));
        entries.add(Key.key("conduit"));
        entries.add(Key.key("polished_granite_stairs"));
        entries.add(Key.key("smooth_red_sandstone_stairs"));
        entries.add(Key.key("mossy_stone_brick_stairs"));
        entries.add(Key.key("polished_diorite_stairs"));
        entries.add(Key.key("mossy_cobblestone_stairs"));
        entries.add(Key.key("end_stone_brick_stairs"));
        entries.add(Key.key("stone_stairs"));
        entries.add(Key.key("smooth_sandstone_stairs"));
        entries.add(Key.key("smooth_quartz_stairs"));
        entries.add(Key.key("granite_stairs"));
        entries.add(Key.key("andesite_stairs"));
        entries.add(Key.key("red_nether_brick_stairs"));
    }

    private static void item4(final List<Key> entries) {
        entries.add(Key.key("polished_andesite_stairs"));
        entries.add(Key.key("diorite_stairs"));
        entries.add(Key.key("cobbled_deepslate_stairs"));
        entries.add(Key.key("polished_deepslate_stairs"));
        entries.add(Key.key("deepslate_brick_stairs"));
        entries.add(Key.key("deepslate_tile_stairs"));
        entries.add(Key.key("polished_granite_slab"));
        entries.add(Key.key("smooth_red_sandstone_slab"));
        entries.add(Key.key("mossy_stone_brick_slab"));
        entries.add(Key.key("polished_diorite_slab"));
        entries.add(Key.key("mossy_cobblestone_slab"));
        entries.add(Key.key("end_stone_brick_slab"));
        entries.add(Key.key("smooth_sandstone_slab"));
        entries.add(Key.key("smooth_quartz_slab"));
        entries.add(Key.key("granite_slab"));
        entries.add(Key.key("andesite_slab"));
        entries.add(Key.key("red_nether_brick_slab"));
        entries.add(Key.key("polished_andesite_slab"));
        entries.add(Key.key("diorite_slab"));
        entries.add(Key.key("cobbled_deepslate_slab"));
        entries.add(Key.key("polished_deepslate_slab"));
        entries.add(Key.key("deepslate_brick_slab"));
        entries.add(Key.key("deepslate_tile_slab"));
        entries.add(Key.key("scaffolding"));
        entries.add(Key.key("redstone"));
        entries.add(Key.key("redstone_torch"));
        entries.add(Key.key("redstone_block"));
        entries.add(Key.key("repeater"));
        entries.add(Key.key("comparator"));
        entries.add(Key.key("piston"));
        entries.add(Key.key("sticky_piston"));
        entries.add(Key.key("slime_block"));
        entries.add(Key.key("honey_block"));
        entries.add(Key.key("observer"));
        entries.add(Key.key("hopper"));
        entries.add(Key.key("dispenser"));
        entries.add(Key.key("dropper"));
        entries.add(Key.key("lectern"));
        entries.add(Key.key("target"));
        entries.add(Key.key("lever"));
        entries.add(Key.key("lightning_rod"));
        entries.add(Key.key("exposed_lightning_rod"));
        entries.add(Key.key("weathered_lightning_rod"));
        entries.add(Key.key("oxidized_lightning_rod"));
        entries.add(Key.key("waxed_lightning_rod"));
        entries.add(Key.key("waxed_exposed_lightning_rod"));
        entries.add(Key.key("waxed_weathered_lightning_rod"));
        entries.add(Key.key("waxed_oxidized_lightning_rod"));
        entries.add(Key.key("daylight_detector"));
        entries.add(Key.key("sculk_sensor"));
        entries.add(Key.key("calibrated_sculk_sensor"));
        entries.add(Key.key("tripwire_hook"));
        entries.add(Key.key("trapped_chest"));
        entries.add(Key.key("tnt"));
        entries.add(Key.key("redstone_lamp"));
        entries.add(Key.key("note_block"));
        entries.add(Key.key("stone_button"));
        entries.add(Key.key("polished_blackstone_button"));
        entries.add(Key.key("oak_button"));
        entries.add(Key.key("spruce_button"));
        entries.add(Key.key("birch_button"));
        entries.add(Key.key("jungle_button"));
        entries.add(Key.key("acacia_button"));
        entries.add(Key.key("cherry_button"));
        entries.add(Key.key("dark_oak_button"));
        entries.add(Key.key("pale_oak_button"));
        entries.add(Key.key("mangrove_button"));
        entries.add(Key.key("poplar_button"));
        entries.add(Key.key("bamboo_button"));
        entries.add(Key.key("crimson_button"));
        entries.add(Key.key("warped_button"));
        entries.add(Key.key("stone_pressure_plate"));
        entries.add(Key.key("polished_blackstone_pressure_plate"));
        entries.add(Key.key("light_weighted_pressure_plate"));
        entries.add(Key.key("heavy_weighted_pressure_plate"));
        entries.add(Key.key("oak_pressure_plate"));
        entries.add(Key.key("spruce_pressure_plate"));
        entries.add(Key.key("birch_pressure_plate"));
        entries.add(Key.key("jungle_pressure_plate"));
        entries.add(Key.key("acacia_pressure_plate"));
        entries.add(Key.key("cherry_pressure_plate"));
        entries.add(Key.key("dark_oak_pressure_plate"));
        entries.add(Key.key("pale_oak_pressure_plate"));
        entries.add(Key.key("mangrove_pressure_plate"));
        entries.add(Key.key("poplar_pressure_plate"));
        entries.add(Key.key("bamboo_pressure_plate"));
        entries.add(Key.key("crimson_pressure_plate"));
        entries.add(Key.key("warped_pressure_plate"));
        entries.add(Key.key("iron_door"));
        entries.add(Key.key("oak_door"));
        entries.add(Key.key("spruce_door"));
        entries.add(Key.key("birch_door"));
        entries.add(Key.key("jungle_door"));
        entries.add(Key.key("acacia_door"));
        entries.add(Key.key("cherry_door"));
        entries.add(Key.key("dark_oak_door"));
        entries.add(Key.key("pale_oak_door"));
        entries.add(Key.key("mangrove_door"));
        entries.add(Key.key("poplar_door"));
        entries.add(Key.key("bamboo_door"));
        entries.add(Key.key("crimson_door"));
        entries.add(Key.key("warped_door"));
        entries.add(Key.key("copper_door"));
        entries.add(Key.key("exposed_copper_door"));
        entries.add(Key.key("weathered_copper_door"));
        entries.add(Key.key("oxidized_copper_door"));
        entries.add(Key.key("waxed_copper_door"));
        entries.add(Key.key("waxed_exposed_copper_door"));
        entries.add(Key.key("waxed_weathered_copper_door"));
        entries.add(Key.key("waxed_oxidized_copper_door"));
        entries.add(Key.key("iron_trapdoor"));
        entries.add(Key.key("oak_trapdoor"));
        entries.add(Key.key("spruce_trapdoor"));
        entries.add(Key.key("birch_trapdoor"));
        entries.add(Key.key("jungle_trapdoor"));
        entries.add(Key.key("acacia_trapdoor"));
        entries.add(Key.key("cherry_trapdoor"));
        entries.add(Key.key("dark_oak_trapdoor"));
        entries.add(Key.key("pale_oak_trapdoor"));
        entries.add(Key.key("mangrove_trapdoor"));
        entries.add(Key.key("poplar_trapdoor"));
        entries.add(Key.key("bamboo_trapdoor"));
        entries.add(Key.key("crimson_trapdoor"));
        entries.add(Key.key("warped_trapdoor"));
        entries.add(Key.key("copper_trapdoor"));
        entries.add(Key.key("exposed_copper_trapdoor"));
        entries.add(Key.key("weathered_copper_trapdoor"));
        entries.add(Key.key("oxidized_copper_trapdoor"));
        entries.add(Key.key("waxed_copper_trapdoor"));
        entries.add(Key.key("waxed_exposed_copper_trapdoor"));
        entries.add(Key.key("waxed_weathered_copper_trapdoor"));
        entries.add(Key.key("waxed_oxidized_copper_trapdoor"));
        entries.add(Key.key("oak_fence_gate"));
        entries.add(Key.key("spruce_fence_gate"));
        entries.add(Key.key("birch_fence_gate"));
        entries.add(Key.key("jungle_fence_gate"));
        entries.add(Key.key("acacia_fence_gate"));
        entries.add(Key.key("cherry_fence_gate"));
        entries.add(Key.key("dark_oak_fence_gate"));
        entries.add(Key.key("pale_oak_fence_gate"));
        entries.add(Key.key("mangrove_fence_gate"));
        entries.add(Key.key("poplar_fence_gate"));
        entries.add(Key.key("bamboo_fence_gate"));
        entries.add(Key.key("crimson_fence_gate"));
        entries.add(Key.key("warped_fence_gate"));
        entries.add(Key.key("powered_rail"));
        entries.add(Key.key("detector_rail"));
        entries.add(Key.key("rail"));
        entries.add(Key.key("activator_rail"));
        entries.add(Key.key("saddle"));
        entries.add(Key.key("white_harness"));
        entries.add(Key.key("orange_harness"));
        entries.add(Key.key("magenta_harness"));
        entries.add(Key.key("light_blue_harness"));
        entries.add(Key.key("yellow_harness"));
        entries.add(Key.key("lime_harness"));
        entries.add(Key.key("pink_harness"));
        entries.add(Key.key("gray_harness"));
        entries.add(Key.key("light_gray_harness"));
        entries.add(Key.key("cyan_harness"));
        entries.add(Key.key("purple_harness"));
        entries.add(Key.key("blue_harness"));
        entries.add(Key.key("brown_harness"));
        entries.add(Key.key("green_harness"));
        entries.add(Key.key("red_harness"));
        entries.add(Key.key("black_harness"));
        entries.add(Key.key("minecart"));
        entries.add(Key.key("chest_minecart"));
        entries.add(Key.key("furnace_minecart"));
        entries.add(Key.key("tnt_minecart"));
        entries.add(Key.key("hopper_minecart"));
        entries.add(Key.key("carrot_on_a_stick"));
        entries.add(Key.key("warped_fungus_on_a_stick"));
        entries.add(Key.key("phantom_membrane"));
        entries.add(Key.key("elytra"));
        entries.add(Key.key("oak_boat"));
        entries.add(Key.key("oak_chest_boat"));
        entries.add(Key.key("spruce_boat"));
        entries.add(Key.key("spruce_chest_boat"));
        entries.add(Key.key("birch_boat"));
        entries.add(Key.key("birch_chest_boat"));
        entries.add(Key.key("jungle_boat"));
        entries.add(Key.key("jungle_chest_boat"));
        entries.add(Key.key("acacia_boat"));
        entries.add(Key.key("acacia_chest_boat"));
        entries.add(Key.key("cherry_boat"));
        entries.add(Key.key("cherry_chest_boat"));
        entries.add(Key.key("dark_oak_boat"));
        entries.add(Key.key("dark_oak_chest_boat"));
        entries.add(Key.key("pale_oak_boat"));
        entries.add(Key.key("pale_oak_chest_boat"));
        entries.add(Key.key("mangrove_boat"));
        entries.add(Key.key("mangrove_chest_boat"));
        entries.add(Key.key("poplar_boat"));
        entries.add(Key.key("poplar_chest_boat"));
        entries.add(Key.key("bamboo_raft"));
        entries.add(Key.key("bamboo_chest_raft"));
        entries.add(Key.key("structure_block"));
        entries.add(Key.key("jigsaw"));
        entries.add(Key.key("test_block"));
    }

    private static void item5(final List<Key> entries) {
        entries.add(Key.key("test_instance_block"));
        entries.add(Key.key("turtle_helmet"));
        entries.add(Key.key("turtle_scute"));
        entries.add(Key.key("armadillo_scute"));
        entries.add(Key.key("wolf_armor"));
        entries.add(Key.key("flint_and_steel"));
        entries.add(Key.key("bowl"));
        entries.add(Key.key("apple"));
        entries.add(Key.key("bow"));
        entries.add(Key.key("arrow"));
        entries.add(Key.key("coal"));
        entries.add(Key.key("charcoal"));
        entries.add(Key.key("diamond"));
        entries.add(Key.key("emerald"));
        entries.add(Key.key("lapis_lazuli"));
        entries.add(Key.key("quartz"));
        entries.add(Key.key("amethyst_shard"));
        entries.add(Key.key("raw_iron"));
        entries.add(Key.key("iron_ingot"));
        entries.add(Key.key("raw_copper"));
        entries.add(Key.key("copper_ingot"));
        entries.add(Key.key("raw_gold"));
        entries.add(Key.key("gold_ingot"));
        entries.add(Key.key("netherite_ingot"));
        entries.add(Key.key("netherite_scrap"));
        entries.add(Key.key("wooden_sword"));
        entries.add(Key.key("wooden_shovel"));
        entries.add(Key.key("wooden_pickaxe"));
        entries.add(Key.key("wooden_axe"));
        entries.add(Key.key("wooden_hoe"));
        entries.add(Key.key("copper_sword"));
        entries.add(Key.key("copper_shovel"));
        entries.add(Key.key("copper_pickaxe"));
        entries.add(Key.key("copper_axe"));
        entries.add(Key.key("copper_hoe"));
        entries.add(Key.key("stone_sword"));
        entries.add(Key.key("stone_shovel"));
        entries.add(Key.key("stone_pickaxe"));
        entries.add(Key.key("stone_axe"));
        entries.add(Key.key("stone_hoe"));
        entries.add(Key.key("golden_sword"));
        entries.add(Key.key("golden_shovel"));
        entries.add(Key.key("golden_pickaxe"));
        entries.add(Key.key("golden_axe"));
        entries.add(Key.key("golden_hoe"));
        entries.add(Key.key("iron_sword"));
        entries.add(Key.key("iron_shovel"));
        entries.add(Key.key("iron_pickaxe"));
        entries.add(Key.key("iron_axe"));
        entries.add(Key.key("iron_hoe"));
        entries.add(Key.key("diamond_sword"));
        entries.add(Key.key("diamond_shovel"));
        entries.add(Key.key("diamond_pickaxe"));
        entries.add(Key.key("diamond_axe"));
        entries.add(Key.key("diamond_hoe"));
        entries.add(Key.key("netherite_sword"));
        entries.add(Key.key("netherite_shovel"));
        entries.add(Key.key("netherite_pickaxe"));
        entries.add(Key.key("netherite_axe"));
        entries.add(Key.key("netherite_hoe"));
        entries.add(Key.key("stick"));
        entries.add(Key.key("mushroom_stew"));
        entries.add(Key.key("string"));
        entries.add(Key.key("feather"));
        entries.add(Key.key("gunpowder"));
        entries.add(Key.key("wheat_seeds"));
        entries.add(Key.key("wheat"));
        entries.add(Key.key("bread"));
        entries.add(Key.key("leather_helmet"));
        entries.add(Key.key("leather_chestplate"));
        entries.add(Key.key("leather_leggings"));
        entries.add(Key.key("leather_boots"));
        entries.add(Key.key("copper_helmet"));
        entries.add(Key.key("copper_chestplate"));
        entries.add(Key.key("copper_leggings"));
        entries.add(Key.key("copper_boots"));
        entries.add(Key.key("chainmail_helmet"));
        entries.add(Key.key("chainmail_chestplate"));
        entries.add(Key.key("chainmail_leggings"));
        entries.add(Key.key("chainmail_boots"));
        entries.add(Key.key("iron_helmet"));
        entries.add(Key.key("iron_chestplate"));
        entries.add(Key.key("iron_leggings"));
        entries.add(Key.key("iron_boots"));
        entries.add(Key.key("diamond_helmet"));
        entries.add(Key.key("diamond_chestplate"));
        entries.add(Key.key("diamond_leggings"));
        entries.add(Key.key("diamond_boots"));
        entries.add(Key.key("golden_helmet"));
        entries.add(Key.key("golden_chestplate"));
        entries.add(Key.key("golden_leggings"));
        entries.add(Key.key("golden_boots"));
        entries.add(Key.key("netherite_helmet"));
        entries.add(Key.key("netherite_chestplate"));
        entries.add(Key.key("netherite_leggings"));
        entries.add(Key.key("netherite_boots"));
        entries.add(Key.key("flint"));
        entries.add(Key.key("porkchop"));
        entries.add(Key.key("cooked_porkchop"));
        entries.add(Key.key("painting"));
        entries.add(Key.key("golden_apple"));
        entries.add(Key.key("enchanted_golden_apple"));
        entries.add(Key.key("oak_sign"));
        entries.add(Key.key("spruce_sign"));
        entries.add(Key.key("birch_sign"));
        entries.add(Key.key("jungle_sign"));
        entries.add(Key.key("acacia_sign"));
        entries.add(Key.key("cherry_sign"));
        entries.add(Key.key("dark_oak_sign"));
        entries.add(Key.key("pale_oak_sign"));
        entries.add(Key.key("mangrove_sign"));
        entries.add(Key.key("poplar_sign"));
        entries.add(Key.key("bamboo_sign"));
        entries.add(Key.key("crimson_sign"));
        entries.add(Key.key("warped_sign"));
        entries.add(Key.key("oak_hanging_sign"));
        entries.add(Key.key("spruce_hanging_sign"));
        entries.add(Key.key("birch_hanging_sign"));
        entries.add(Key.key("jungle_hanging_sign"));
        entries.add(Key.key("acacia_hanging_sign"));
        entries.add(Key.key("cherry_hanging_sign"));
        entries.add(Key.key("dark_oak_hanging_sign"));
        entries.add(Key.key("pale_oak_hanging_sign"));
        entries.add(Key.key("mangrove_hanging_sign"));
        entries.add(Key.key("poplar_hanging_sign"));
        entries.add(Key.key("bamboo_hanging_sign"));
        entries.add(Key.key("crimson_hanging_sign"));
        entries.add(Key.key("warped_hanging_sign"));
        entries.add(Key.key("bucket"));
        entries.add(Key.key("water_bucket"));
        entries.add(Key.key("lava_bucket"));
        entries.add(Key.key("powder_snow_bucket"));
        entries.add(Key.key("snowball"));
        entries.add(Key.key("leather"));
        entries.add(Key.key("milk_bucket"));
        entries.add(Key.key("pufferfish_bucket"));
        entries.add(Key.key("salmon_bucket"));
        entries.add(Key.key("cod_bucket"));
        entries.add(Key.key("tropical_fish_bucket"));
        entries.add(Key.key("axolotl_bucket"));
        entries.add(Key.key("sulfur_cube_bucket"));
        entries.add(Key.key("tadpole_bucket"));
        entries.add(Key.key("brick"));
        entries.add(Key.key("clay_ball"));
        entries.add(Key.key("dried_kelp_block"));
        entries.add(Key.key("paper"));
        entries.add(Key.key("book"));
        entries.add(Key.key("slime_ball"));
        entries.add(Key.key("egg"));
        entries.add(Key.key("blue_egg"));
        entries.add(Key.key("brown_egg"));
        entries.add(Key.key("compass"));
        entries.add(Key.key("recovery_compass"));
        entries.add(Key.key("bundle"));
        entries.add(Key.key("white_bundle"));
        entries.add(Key.key("orange_bundle"));
        entries.add(Key.key("magenta_bundle"));
        entries.add(Key.key("light_blue_bundle"));
        entries.add(Key.key("yellow_bundle"));
        entries.add(Key.key("lime_bundle"));
        entries.add(Key.key("pink_bundle"));
        entries.add(Key.key("gray_bundle"));
        entries.add(Key.key("light_gray_bundle"));
        entries.add(Key.key("cyan_bundle"));
        entries.add(Key.key("purple_bundle"));
        entries.add(Key.key("blue_bundle"));
        entries.add(Key.key("brown_bundle"));
        entries.add(Key.key("green_bundle"));
        entries.add(Key.key("red_bundle"));
        entries.add(Key.key("black_bundle"));
        entries.add(Key.key("white_cushion"));
        entries.add(Key.key("orange_cushion"));
        entries.add(Key.key("magenta_cushion"));
        entries.add(Key.key("light_blue_cushion"));
        entries.add(Key.key("yellow_cushion"));
        entries.add(Key.key("lime_cushion"));
        entries.add(Key.key("pink_cushion"));
        entries.add(Key.key("gray_cushion"));
        entries.add(Key.key("light_gray_cushion"));
        entries.add(Key.key("cyan_cushion"));
        entries.add(Key.key("purple_cushion"));
        entries.add(Key.key("blue_cushion"));
        entries.add(Key.key("brown_cushion"));
        entries.add(Key.key("green_cushion"));
        entries.add(Key.key("red_cushion"));
        entries.add(Key.key("black_cushion"));
        entries.add(Key.key("fishing_rod"));
        entries.add(Key.key("clock"));
        entries.add(Key.key("spyglass"));
        entries.add(Key.key("glowstone_dust"));
        entries.add(Key.key("cod"));
        entries.add(Key.key("salmon"));
        entries.add(Key.key("tropical_fish"));
        entries.add(Key.key("pufferfish"));
        entries.add(Key.key("cooked_cod"));
        entries.add(Key.key("cooked_salmon"));
        entries.add(Key.key("ink_sac"));
        entries.add(Key.key("glow_ink_sac"));
        entries.add(Key.key("cocoa_beans"));
        entries.add(Key.key("white_dye"));
    }

    private static void item6(final List<Key> entries) {
        entries.add(Key.key("orange_dye"));
        entries.add(Key.key("magenta_dye"));
        entries.add(Key.key("light_blue_dye"));
        entries.add(Key.key("yellow_dye"));
        entries.add(Key.key("lime_dye"));
        entries.add(Key.key("pink_dye"));
        entries.add(Key.key("gray_dye"));
        entries.add(Key.key("light_gray_dye"));
        entries.add(Key.key("cyan_dye"));
        entries.add(Key.key("purple_dye"));
        entries.add(Key.key("blue_dye"));
        entries.add(Key.key("brown_dye"));
        entries.add(Key.key("green_dye"));
        entries.add(Key.key("red_dye"));
        entries.add(Key.key("black_dye"));
        entries.add(Key.key("bone_meal"));
        entries.add(Key.key("bone"));
        entries.add(Key.key("sugar"));
        entries.add(Key.key("cake"));
        entries.add(Key.key("white_bed"));
        entries.add(Key.key("orange_bed"));
        entries.add(Key.key("magenta_bed"));
        entries.add(Key.key("light_blue_bed"));
        entries.add(Key.key("yellow_bed"));
        entries.add(Key.key("lime_bed"));
        entries.add(Key.key("pink_bed"));
        entries.add(Key.key("gray_bed"));
        entries.add(Key.key("light_gray_bed"));
        entries.add(Key.key("cyan_bed"));
        entries.add(Key.key("purple_bed"));
        entries.add(Key.key("blue_bed"));
        entries.add(Key.key("brown_bed"));
        entries.add(Key.key("green_bed"));
        entries.add(Key.key("red_bed"));
        entries.add(Key.key("black_bed"));
        entries.add(Key.key("straw_bed"));
        entries.add(Key.key("cookie"));
        entries.add(Key.key("crafter"));
        entries.add(Key.key("filled_map"));
        entries.add(Key.key("ocean_monument_map"));
        entries.add(Key.key("woodland_mansion_map"));
        entries.add(Key.key("buried_trial_chambers_map"));
        entries.add(Key.key("jungle_pyramid_map"));
        entries.add(Key.key("swamp_hut_map"));
        entries.add(Key.key("desert_village_map"));
        entries.add(Key.key("plains_village_map"));
        entries.add(Key.key("savanna_village_map"));
        entries.add(Key.key("snowy_village_map"));
        entries.add(Key.key("taiga_village_map"));
        entries.add(Key.key("buried_treasure_map"));
        entries.add(Key.key("buried_ancient_city_map"));
        entries.add(Key.key("buried_mineshaft_map"));
        entries.add(Key.key("desert_pyramid_map"));
        entries.add(Key.key("abandoned_camp_map"));
        entries.add(Key.key("warm_ocean_ruins_map"));
        entries.add(Key.key("shears"));
        entries.add(Key.key("melon_slice"));
        entries.add(Key.key("dried_kelp"));
        entries.add(Key.key("pumpkin_seeds"));
        entries.add(Key.key("melon_seeds"));
        entries.add(Key.key("beef"));
        entries.add(Key.key("cooked_beef"));
        entries.add(Key.key("chicken"));
        entries.add(Key.key("cooked_chicken"));
        entries.add(Key.key("rotten_flesh"));
        entries.add(Key.key("ender_pearl"));
        entries.add(Key.key("blaze_rod"));
        entries.add(Key.key("ghast_tear"));
        entries.add(Key.key("gold_nugget"));
        entries.add(Key.key("nether_wart"));
        entries.add(Key.key("glass_bottle"));
        entries.add(Key.key("potion"));
        entries.add(Key.key("spider_eye"));
        entries.add(Key.key("fermented_spider_eye"));
        entries.add(Key.key("blaze_powder"));
        entries.add(Key.key("magma_cream"));
        entries.add(Key.key("brewing_stand"));
        entries.add(Key.key("cauldron"));
        entries.add(Key.key("ender_eye"));
        entries.add(Key.key("glistering_melon_slice"));
        entries.add(Key.key("chicken_spawn_egg"));
        entries.add(Key.key("cow_spawn_egg"));
        entries.add(Key.key("pig_spawn_egg"));
        entries.add(Key.key("sheep_spawn_egg"));
        entries.add(Key.key("camel_spawn_egg"));
        entries.add(Key.key("donkey_spawn_egg"));
        entries.add(Key.key("horse_spawn_egg"));
        entries.add(Key.key("mule_spawn_egg"));
        entries.add(Key.key("cat_spawn_egg"));
        entries.add(Key.key("parrot_spawn_egg"));
        entries.add(Key.key("wolf_spawn_egg"));
        entries.add(Key.key("armadillo_spawn_egg"));
        entries.add(Key.key("bat_spawn_egg"));
        entries.add(Key.key("bee_spawn_egg"));
        entries.add(Key.key("fox_spawn_egg"));
        entries.add(Key.key("goat_spawn_egg"));
        entries.add(Key.key("llama_spawn_egg"));
        entries.add(Key.key("ocelot_spawn_egg"));
        entries.add(Key.key("panda_spawn_egg"));
        entries.add(Key.key("polar_bear_spawn_egg"));
        entries.add(Key.key("rabbit_spawn_egg"));
        entries.add(Key.key("axolotl_spawn_egg"));
        entries.add(Key.key("cod_spawn_egg"));
        entries.add(Key.key("dolphin_spawn_egg"));
        entries.add(Key.key("frog_spawn_egg"));
        entries.add(Key.key("glow_squid_spawn_egg"));
        entries.add(Key.key("nautilus_spawn_egg"));
        entries.add(Key.key("pufferfish_spawn_egg"));
        entries.add(Key.key("salmon_spawn_egg"));
        entries.add(Key.key("squid_spawn_egg"));
        entries.add(Key.key("tadpole_spawn_egg"));
        entries.add(Key.key("tropical_fish_spawn_egg"));
        entries.add(Key.key("turtle_spawn_egg"));
        entries.add(Key.key("allay_spawn_egg"));
        entries.add(Key.key("mooshroom_spawn_egg"));
        entries.add(Key.key("sniffer_spawn_egg"));
        entries.add(Key.key("sulfur_cube_spawn_egg"));
        entries.add(Key.key("copper_golem_spawn_egg"));
        entries.add(Key.key("iron_golem_spawn_egg"));
        entries.add(Key.key("snow_golem_spawn_egg"));
        entries.add(Key.key("trader_llama_spawn_egg"));
        entries.add(Key.key("villager_spawn_egg"));
        entries.add(Key.key("wandering_trader_spawn_egg"));
        entries.add(Key.key("bogged_spawn_egg"));
        entries.add(Key.key("camel_husk_spawn_egg"));
        entries.add(Key.key("drowned_spawn_egg"));
        entries.add(Key.key("husk_spawn_egg"));
        entries.add(Key.key("parched_spawn_egg"));
        entries.add(Key.key("skeleton_spawn_egg"));
        entries.add(Key.key("skeleton_horse_spawn_egg"));
        entries.add(Key.key("stray_spawn_egg"));
        entries.add(Key.key("wither_spawn_egg"));
        entries.add(Key.key("wither_skeleton_spawn_egg"));
        entries.add(Key.key("zombie_spawn_egg"));
        entries.add(Key.key("zombie_horse_spawn_egg"));
        entries.add(Key.key("zombie_nautilus_spawn_egg"));
        entries.add(Key.key("zombie_villager_spawn_egg"));
        entries.add(Key.key("cave_spider_spawn_egg"));
        entries.add(Key.key("spider_spawn_egg"));
        entries.add(Key.key("breeze_spawn_egg"));
        entries.add(Key.key("creaking_spawn_egg"));
        entries.add(Key.key("creeper_spawn_egg"));
        entries.add(Key.key("elder_guardian_spawn_egg"));
        entries.add(Key.key("guardian_spawn_egg"));
        entries.add(Key.key("phantom_spawn_egg"));
        entries.add(Key.key("silverfish_spawn_egg"));
        entries.add(Key.key("slime_spawn_egg"));
        entries.add(Key.key("warden_spawn_egg"));
        entries.add(Key.key("witch_spawn_egg"));
        entries.add(Key.key("evoker_spawn_egg"));
        entries.add(Key.key("pillager_spawn_egg"));
        entries.add(Key.key("ravager_spawn_egg"));
        entries.add(Key.key("vindicator_spawn_egg"));
        entries.add(Key.key("vex_spawn_egg"));
        entries.add(Key.key("blaze_spawn_egg"));
        entries.add(Key.key("ghast_spawn_egg"));
        entries.add(Key.key("happy_ghast_spawn_egg"));
        entries.add(Key.key("hoglin_spawn_egg"));
        entries.add(Key.key("magma_cube_spawn_egg"));
        entries.add(Key.key("piglin_spawn_egg"));
        entries.add(Key.key("piglin_brute_spawn_egg"));
        entries.add(Key.key("strider_spawn_egg"));
        entries.add(Key.key("zoglin_spawn_egg"));
        entries.add(Key.key("zombified_piglin_spawn_egg"));
        entries.add(Key.key("ender_dragon_spawn_egg"));
        entries.add(Key.key("enderman_spawn_egg"));
        entries.add(Key.key("endermite_spawn_egg"));
        entries.add(Key.key("shulker_spawn_egg"));
        entries.add(Key.key("experience_bottle"));
        entries.add(Key.key("fire_charge"));
        entries.add(Key.key("wind_charge"));
        entries.add(Key.key("writable_book"));
        entries.add(Key.key("written_book"));
        entries.add(Key.key("breeze_rod"));
        entries.add(Key.key("mace"));
        entries.add(Key.key("item_frame"));
        entries.add(Key.key("glow_item_frame"));
        entries.add(Key.key("flower_pot"));
        entries.add(Key.key("carrot"));
        entries.add(Key.key("potato"));
        entries.add(Key.key("baked_potato"));
        entries.add(Key.key("poisonous_potato"));
        entries.add(Key.key("map"));
        entries.add(Key.key("golden_carrot"));
        entries.add(Key.key("skeleton_skull"));
        entries.add(Key.key("wither_skeleton_skull"));
        entries.add(Key.key("player_head"));
        entries.add(Key.key("zombie_head"));
        entries.add(Key.key("creeper_head"));
        entries.add(Key.key("dragon_head"));
        entries.add(Key.key("piglin_head"));
        entries.add(Key.key("nether_star"));
        entries.add(Key.key("pumpkin_pie"));
        entries.add(Key.key("firework_rocket"));
        entries.add(Key.key("firework_star"));
        entries.add(Key.key("enchanted_book"));
        entries.add(Key.key("nether_brick"));
        entries.add(Key.key("resin_brick"));
        entries.add(Key.key("prismarine_shard"));
        entries.add(Key.key("prismarine_crystals"));
    }

    private static void item7(final List<Key> entries) {
        entries.add(Key.key("rabbit"));
        entries.add(Key.key("cooked_rabbit"));
        entries.add(Key.key("rabbit_stew"));
        entries.add(Key.key("rabbit_foot"));
        entries.add(Key.key("rabbit_hide"));
        entries.add(Key.key("armor_stand"));
        entries.add(Key.key("copper_horse_armor"));
        entries.add(Key.key("iron_horse_armor"));
        entries.add(Key.key("golden_horse_armor"));
        entries.add(Key.key("diamond_horse_armor"));
        entries.add(Key.key("netherite_horse_armor"));
        entries.add(Key.key("leather_horse_armor"));
        entries.add(Key.key("lead"));
        entries.add(Key.key("name_tag"));
        entries.add(Key.key("command_block_minecart"));
        entries.add(Key.key("mutton"));
        entries.add(Key.key("cooked_mutton"));
        entries.add(Key.key("white_banner"));
        entries.add(Key.key("orange_banner"));
        entries.add(Key.key("magenta_banner"));
        entries.add(Key.key("light_blue_banner"));
        entries.add(Key.key("yellow_banner"));
        entries.add(Key.key("lime_banner"));
        entries.add(Key.key("pink_banner"));
        entries.add(Key.key("gray_banner"));
        entries.add(Key.key("light_gray_banner"));
        entries.add(Key.key("cyan_banner"));
        entries.add(Key.key("purple_banner"));
        entries.add(Key.key("blue_banner"));
        entries.add(Key.key("brown_banner"));
        entries.add(Key.key("green_banner"));
        entries.add(Key.key("red_banner"));
        entries.add(Key.key("black_banner"));
        entries.add(Key.key("end_crystal"));
        entries.add(Key.key("chorus_fruit"));
        entries.add(Key.key("popped_chorus_fruit"));
        entries.add(Key.key("torchflower_seeds"));
        entries.add(Key.key("pitcher_pod"));
        entries.add(Key.key("beetroot"));
        entries.add(Key.key("beetroot_seeds"));
        entries.add(Key.key("beetroot_soup"));
        entries.add(Key.key("dragon_breath"));
        entries.add(Key.key("splash_potion"));
        entries.add(Key.key("spectral_arrow"));
        entries.add(Key.key("tipped_arrow"));
        entries.add(Key.key("lingering_potion"));
        entries.add(Key.key("shield"));
        entries.add(Key.key("wooden_spear"));
        entries.add(Key.key("stone_spear"));
        entries.add(Key.key("copper_spear"));
        entries.add(Key.key("iron_spear"));
        entries.add(Key.key("golden_spear"));
        entries.add(Key.key("diamond_spear"));
        entries.add(Key.key("netherite_spear"));
        entries.add(Key.key("totem_of_undying"));
        entries.add(Key.key("shulker_shell"));
        entries.add(Key.key("iron_nugget"));
        entries.add(Key.key("copper_nugget"));
        entries.add(Key.key("knowledge_book"));
        entries.add(Key.key("debug_stick"));
        entries.add(Key.key("music_disc_13"));
        entries.add(Key.key("music_disc_cat"));
        entries.add(Key.key("music_disc_blocks"));
        entries.add(Key.key("music_disc_bounce"));
        entries.add(Key.key("music_disc_chirp"));
        entries.add(Key.key("music_disc_creator"));
        entries.add(Key.key("music_disc_creator_music_box"));
        entries.add(Key.key("music_disc_far"));
        entries.add(Key.key("music_disc_lava_chicken"));
        entries.add(Key.key("music_disc_mall"));
        entries.add(Key.key("music_disc_mellohi"));
        entries.add(Key.key("music_disc_stal"));
        entries.add(Key.key("music_disc_strad"));
        entries.add(Key.key("music_disc_ward"));
        entries.add(Key.key("music_disc_11"));
        entries.add(Key.key("music_disc_wait"));
        entries.add(Key.key("music_disc_otherside"));
        entries.add(Key.key("music_disc_relic"));
        entries.add(Key.key("music_disc_5"));
        entries.add(Key.key("music_disc_pigstep"));
        entries.add(Key.key("music_disc_precipice"));
        entries.add(Key.key("music_disc_tears"));
        entries.add(Key.key("disc_fragment_5"));
        entries.add(Key.key("trident"));
        entries.add(Key.key("nautilus_shell"));
        entries.add(Key.key("iron_nautilus_armor"));
        entries.add(Key.key("golden_nautilus_armor"));
        entries.add(Key.key("diamond_nautilus_armor"));
        entries.add(Key.key("netherite_nautilus_armor"));
        entries.add(Key.key("copper_nautilus_armor"));
        entries.add(Key.key("heart_of_the_sea"));
        entries.add(Key.key("crossbow"));
        entries.add(Key.key("suspicious_stew"));
        entries.add(Key.key("loom"));
        entries.add(Key.key("flower_banner_pattern"));
        entries.add(Key.key("creeper_banner_pattern"));
        entries.add(Key.key("skull_banner_pattern"));
        entries.add(Key.key("mojang_banner_pattern"));
        entries.add(Key.key("globe_banner_pattern"));
        entries.add(Key.key("piglin_banner_pattern"));
        entries.add(Key.key("flow_banner_pattern"));
        entries.add(Key.key("guster_banner_pattern"));
        entries.add(Key.key("field_masoned_banner_pattern"));
        entries.add(Key.key("bordure_indented_banner_pattern"));
        entries.add(Key.key("goat_horn"));
        entries.add(Key.key("composter"));
        entries.add(Key.key("barrel"));
        entries.add(Key.key("smoker"));
        entries.add(Key.key("blast_furnace"));
        entries.add(Key.key("cartography_table"));
        entries.add(Key.key("fletching_table"));
        entries.add(Key.key("grindstone"));
        entries.add(Key.key("smithing_table"));
        entries.add(Key.key("stonecutter"));
        entries.add(Key.key("bell"));
        entries.add(Key.key("lantern"));
        entries.add(Key.key("soul_lantern"));
        entries.add(Key.key("copper_lantern"));
        entries.add(Key.key("exposed_copper_lantern"));
        entries.add(Key.key("weathered_copper_lantern"));
        entries.add(Key.key("oxidized_copper_lantern"));
        entries.add(Key.key("waxed_copper_lantern"));
        entries.add(Key.key("waxed_exposed_copper_lantern"));
        entries.add(Key.key("waxed_weathered_copper_lantern"));
        entries.add(Key.key("waxed_oxidized_copper_lantern"));
        entries.add(Key.key("sweet_berries"));
        entries.add(Key.key("glow_berries"));
        entries.add(Key.key("campfire"));
        entries.add(Key.key("soul_campfire"));
        entries.add(Key.key("shroomlight"));
        entries.add(Key.key("honeycomb"));
        entries.add(Key.key("bee_nest"));
        entries.add(Key.key("beehive"));
        entries.add(Key.key("honey_bottle"));
        entries.add(Key.key("honeycomb_block"));
        entries.add(Key.key("lodestone"));
        entries.add(Key.key("crying_obsidian"));
        entries.add(Key.key("blackstone"));
        entries.add(Key.key("blackstone_slab"));
        entries.add(Key.key("blackstone_stairs"));
        entries.add(Key.key("gilded_blackstone"));
        entries.add(Key.key("polished_blackstone"));
        entries.add(Key.key("polished_blackstone_slab"));
        entries.add(Key.key("polished_blackstone_stairs"));
        entries.add(Key.key("chiseled_polished_blackstone"));
        entries.add(Key.key("polished_blackstone_bricks"));
        entries.add(Key.key("polished_blackstone_brick_slab"));
        entries.add(Key.key("polished_blackstone_brick_stairs"));
        entries.add(Key.key("cracked_polished_blackstone_bricks"));
        entries.add(Key.key("respawn_anchor"));
        entries.add(Key.key("candle"));
        entries.add(Key.key("white_candle"));
        entries.add(Key.key("orange_candle"));
        entries.add(Key.key("magenta_candle"));
        entries.add(Key.key("light_blue_candle"));
        entries.add(Key.key("yellow_candle"));
        entries.add(Key.key("lime_candle"));
        entries.add(Key.key("pink_candle"));
        entries.add(Key.key("gray_candle"));
        entries.add(Key.key("light_gray_candle"));
        entries.add(Key.key("cyan_candle"));
        entries.add(Key.key("purple_candle"));
        entries.add(Key.key("blue_candle"));
        entries.add(Key.key("brown_candle"));
        entries.add(Key.key("green_candle"));
        entries.add(Key.key("red_candle"));
        entries.add(Key.key("black_candle"));
        entries.add(Key.key("small_amethyst_bud"));
        entries.add(Key.key("medium_amethyst_bud"));
        entries.add(Key.key("large_amethyst_bud"));
        entries.add(Key.key("amethyst_cluster"));
        entries.add(Key.key("pointed_dripstone"));
        entries.add(Key.key("sulfur_spike"));
        entries.add(Key.key("ochre_froglight"));
        entries.add(Key.key("verdant_froglight"));
        entries.add(Key.key("pearlescent_froglight"));
        entries.add(Key.key("frogspawn"));
        entries.add(Key.key("echo_shard"));
        entries.add(Key.key("brush"));
        entries.add(Key.key("netherite_upgrade_smithing_template"));
        entries.add(Key.key("sentry_armor_trim_smithing_template"));
        entries.add(Key.key("dune_armor_trim_smithing_template"));
        entries.add(Key.key("coast_armor_trim_smithing_template"));
        entries.add(Key.key("wild_armor_trim_smithing_template"));
        entries.add(Key.key("ward_armor_trim_smithing_template"));
        entries.add(Key.key("eye_armor_trim_smithing_template"));
        entries.add(Key.key("vex_armor_trim_smithing_template"));
        entries.add(Key.key("tide_armor_trim_smithing_template"));
        entries.add(Key.key("snout_armor_trim_smithing_template"));
        entries.add(Key.key("rib_armor_trim_smithing_template"));
        entries.add(Key.key("spire_armor_trim_smithing_template"));
        entries.add(Key.key("wayfinder_armor_trim_smithing_template"));
        entries.add(Key.key("shaper_armor_trim_smithing_template"));
        entries.add(Key.key("silence_armor_trim_smithing_template"));
        entries.add(Key.key("raiser_armor_trim_smithing_template"));
        entries.add(Key.key("host_armor_trim_smithing_template"));
        entries.add(Key.key("flow_armor_trim_smithing_template"));
        entries.add(Key.key("bolt_armor_trim_smithing_template"));
        entries.add(Key.key("angler_pottery_sherd"));
        entries.add(Key.key("archer_pottery_sherd"));
    }

    private static void item8(final List<Key> entries) {
        entries.add(Key.key("arms_up_pottery_sherd"));
        entries.add(Key.key("blade_pottery_sherd"));
        entries.add(Key.key("brewer_pottery_sherd"));
        entries.add(Key.key("burn_pottery_sherd"));
        entries.add(Key.key("danger_pottery_sherd"));
        entries.add(Key.key("explorer_pottery_sherd"));
        entries.add(Key.key("flow_pottery_sherd"));
        entries.add(Key.key("friend_pottery_sherd"));
        entries.add(Key.key("guster_pottery_sherd"));
        entries.add(Key.key("heart_pottery_sherd"));
        entries.add(Key.key("heartbreak_pottery_sherd"));
        entries.add(Key.key("howl_pottery_sherd"));
        entries.add(Key.key("miner_pottery_sherd"));
        entries.add(Key.key("mourner_pottery_sherd"));
        entries.add(Key.key("plenty_pottery_sherd"));
        entries.add(Key.key("prize_pottery_sherd"));
        entries.add(Key.key("scrape_pottery_sherd"));
        entries.add(Key.key("sheaf_pottery_sherd"));
        entries.add(Key.key("shelter_pottery_sherd"));
        entries.add(Key.key("skull_pottery_sherd"));
        entries.add(Key.key("snort_pottery_sherd"));
        entries.add(Key.key("copper_grate"));
        entries.add(Key.key("exposed_copper_grate"));
        entries.add(Key.key("weathered_copper_grate"));
        entries.add(Key.key("oxidized_copper_grate"));
        entries.add(Key.key("waxed_copper_grate"));
        entries.add(Key.key("waxed_exposed_copper_grate"));
        entries.add(Key.key("waxed_weathered_copper_grate"));
        entries.add(Key.key("waxed_oxidized_copper_grate"));
        entries.add(Key.key("copper_bulb"));
        entries.add(Key.key("exposed_copper_bulb"));
        entries.add(Key.key("weathered_copper_bulb"));
        entries.add(Key.key("oxidized_copper_bulb"));
        entries.add(Key.key("waxed_copper_bulb"));
        entries.add(Key.key("waxed_exposed_copper_bulb"));
        entries.add(Key.key("waxed_weathered_copper_bulb"));
        entries.add(Key.key("waxed_oxidized_copper_bulb"));
        entries.add(Key.key("copper_chest"));
        entries.add(Key.key("exposed_copper_chest"));
        entries.add(Key.key("weathered_copper_chest"));
        entries.add(Key.key("oxidized_copper_chest"));
        entries.add(Key.key("waxed_copper_chest"));
        entries.add(Key.key("waxed_exposed_copper_chest"));
        entries.add(Key.key("waxed_weathered_copper_chest"));
        entries.add(Key.key("waxed_oxidized_copper_chest"));
        entries.add(Key.key("copper_golem_statue"));
        entries.add(Key.key("exposed_copper_golem_statue"));
        entries.add(Key.key("weathered_copper_golem_statue"));
        entries.add(Key.key("oxidized_copper_golem_statue"));
        entries.add(Key.key("waxed_copper_golem_statue"));
        entries.add(Key.key("waxed_exposed_copper_golem_statue"));
        entries.add(Key.key("waxed_weathered_copper_golem_statue"));
        entries.add(Key.key("waxed_oxidized_copper_golem_statue"));
        entries.add(Key.key("trial_spawner"));
        entries.add(Key.key("trial_key"));
        entries.add(Key.key("ominous_trial_key"));
        entries.add(Key.key("vault"));
        entries.add(Key.key("ominous_bottle"));
    }

    /**
     * @return {@code minecraft:item}, indexed by network ID
     */
    private static List<Key> item() {
        final List<Key> entries = new ArrayList<>(1658);
        item0(entries);
        item1(entries);
        item2(entries);
        item3(entries);
        item4(entries);
        item5(entries);
        item6(entries);
        item7(entries);
        item8(entries);
        return List.copyOf(entries);
    }

    private static void itemTags0(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("acacia_logs"), List.of(Key.key("acacia_log"), Key.key("acacia_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood")));
        tags.put(Key.key("anvil"), List.of(Key.key("anvil"), Key.key("chipped_anvil"),
                Key.key("damaged_anvil")));
        tags.put(Key.key("armadillo_food"), List.of(Key.key("spider_eye")));
        tags.put(Key.key("arrows"), List.of(Key.key("arrow"), Key.key("spectral_arrow"),
                Key.key("tipped_arrow")));
        tags.put(Key.key("axes"), List.of(Key.key("copper_axe"), Key.key("diamond_axe"),
                Key.key("golden_axe"), Key.key("iron_axe"), Key.key("netherite_axe"),
                Key.key("stone_axe"), Key.key("wooden_axe")));
        tags.put(Key.key("axolotl_food"), List.of(Key.key("tropical_fish_bucket")));
        tags.put(Key.key("bamboo_blocks"), List.of(Key.key("bamboo_block"),
                Key.key("stripped_bamboo_block")));
        tags.put(Key.key("banners"), List.of(Key.key("black_banner"), Key.key("blue_banner"),
                Key.key("brown_banner"), Key.key("cyan_banner"), Key.key("gray_banner"),
                Key.key("green_banner"), Key.key("light_blue_banner"), Key.key("light_gray_banner"),
                Key.key("lime_banner"), Key.key("magenta_banner"), Key.key("orange_banner"),
                Key.key("pink_banner"), Key.key("purple_banner"), Key.key("red_banner"),
                Key.key("white_banner"), Key.key("yellow_banner")));
        tags.put(Key.key("bars"), List.of(Key.key("copper_bars"), Key.key("exposed_copper_bars"),
                Key.key("iron_bars"), Key.key("oxidized_copper_bars"), Key.key("waxed_copper_bars"),
                Key.key("waxed_exposed_copper_bars"), Key.key("waxed_oxidized_copper_bars"),
                Key.key("waxed_weathered_copper_bars"), Key.key("weathered_copper_bars")));
        tags.put(Key.key("beacon_payment_items"), List.of(Key.key("diamond"), Key.key("emerald"),
                Key.key("gold_ingot"), Key.key("iron_ingot"), Key.key("netherite_ingot")));
        tags.put(Key.key("beds"), List.of(Key.key("black_bed"), Key.key("blue_bed"),
                Key.key("brown_bed"), Key.key("cyan_bed"), Key.key("gray_bed"),
                Key.key("green_bed"), Key.key("light_blue_bed"), Key.key("light_gray_bed"),
                Key.key("lime_bed"), Key.key("magenta_bed"), Key.key("orange_bed"),
                Key.key("pink_bed"), Key.key("purple_bed"), Key.key("red_bed"),
                Key.key("white_bed"), Key.key("yellow_bed")));
        tags.put(Key.key("bee_food"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("cactus_flower"), Key.key("cherry_leaves"),
                Key.key("chorus_flower"), Key.key("cornflower"), Key.key("dandelion"),
                Key.key("flowering_azalea"), Key.key("flowering_azalea_leaves"), Key.key("lilac"),
                Key.key("lily_of_the_valley"), Key.key("mangrove_propagule"),
                Key.key("open_eyeblossom"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("peony"), Key.key("pink_petals"), Key.key("pink_tulip"),
                Key.key("pitcher_plant"), Key.key("poppy"), Key.key("red_tulip"),
                Key.key("rose_bush"), Key.key("spore_blossom"), Key.key("sunflower"),
                Key.key("torchflower"), Key.key("white_tulip"), Key.key("wildflowers"),
                Key.key("wither_rose")));
        tags.put(Key.key("birch_logs"), List.of(Key.key("birch_log"), Key.key("birch_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood")));
        tags.put(Key.key("boats"), List.of(Key.key("acacia_boat"), Key.key("acacia_chest_boat"),
                Key.key("bamboo_chest_raft"), Key.key("bamboo_raft"), Key.key("birch_boat"),
                Key.key("birch_chest_boat"), Key.key("cherry_boat"), Key.key("cherry_chest_boat"),
                Key.key("dark_oak_boat"), Key.key("dark_oak_chest_boat"), Key.key("jungle_boat"),
                Key.key("jungle_chest_boat"), Key.key("mangrove_boat"),
                Key.key("mangrove_chest_boat"), Key.key("oak_boat"), Key.key("oak_chest_boat"),
                Key.key("pale_oak_boat"), Key.key("pale_oak_chest_boat"), Key.key("poplar_boat"),
                Key.key("poplar_chest_boat"), Key.key("spruce_boat"),
                Key.key("spruce_chest_boat")));
        tags.put(Key.key("book_cloning_target"), List.of(Key.key("writable_book")));
        tags.put(Key.key("bookshelf_books"), List.of(Key.key("book"), Key.key("enchanted_book"),
                Key.key("knowledge_book"), Key.key("writable_book"), Key.key("written_book")));
        tags.put(Key.key("breaks_decorated_pots"), List.of(Key.key("copper_axe"),
                Key.key("copper_hoe"), Key.key("copper_pickaxe"), Key.key("copper_shovel"),
                Key.key("copper_sword"), Key.key("diamond_axe"), Key.key("diamond_hoe"),
                Key.key("diamond_pickaxe"), Key.key("diamond_shovel"), Key.key("diamond_sword"),
                Key.key("golden_axe"), Key.key("golden_hoe"), Key.key("golden_pickaxe"),
                Key.key("golden_shovel"), Key.key("golden_sword"), Key.key("iron_axe"),
                Key.key("iron_hoe"), Key.key("iron_pickaxe"), Key.key("iron_shovel"),
                Key.key("iron_sword"), Key.key("mace"), Key.key("netherite_axe"),
                Key.key("netherite_hoe"), Key.key("netherite_pickaxe"), Key.key("netherite_shovel"),
                Key.key("netherite_sword"), Key.key("stone_axe"), Key.key("stone_hoe"),
                Key.key("stone_pickaxe"), Key.key("stone_shovel"), Key.key("stone_sword"),
                Key.key("trident"), Key.key("wooden_axe"), Key.key("wooden_hoe"),
                Key.key("wooden_pickaxe"), Key.key("wooden_shovel"), Key.key("wooden_sword")));
    }

    private static void itemTags1(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("brewing_potion_inputs"), List.of(Key.key("glass_bottle"),
                Key.key("lingering_potion"), Key.key("potion"), Key.key("splash_potion")));
        tags.put(Key.key("bundles"), List.of(Key.key("black_bundle"), Key.key("blue_bundle"),
                Key.key("brown_bundle"), Key.key("bundle"), Key.key("cyan_bundle"),
                Key.key("gray_bundle"), Key.key("green_bundle"), Key.key("light_blue_bundle"),
                Key.key("light_gray_bundle"), Key.key("lime_bundle"), Key.key("magenta_bundle"),
                Key.key("orange_bundle"), Key.key("pink_bundle"), Key.key("purple_bundle"),
                Key.key("red_bundle"), Key.key("white_bundle"), Key.key("yellow_bundle")));
        tags.put(Key.key("buttons"), List.of(Key.key("acacia_button"), Key.key("bamboo_button"),
                Key.key("birch_button"), Key.key("cherry_button"), Key.key("crimson_button"),
                Key.key("dark_oak_button"), Key.key("jungle_button"), Key.key("mangrove_button"),
                Key.key("oak_button"), Key.key("pale_oak_button"),
                Key.key("polished_blackstone_button"), Key.key("poplar_button"),
                Key.key("spruce_button"), Key.key("stone_button"), Key.key("warped_button")));
        tags.put(Key.key("camel_food"), List.of(Key.key("cactus")));
        tags.put(Key.key("camel_husk_food"), List.of(Key.key("rabbit_foot")));
        tags.put(Key.key("candles"), List.of(Key.key("black_candle"), Key.key("blue_candle"),
                Key.key("brown_candle"), Key.key("candle"), Key.key("cyan_candle"),
                Key.key("gray_candle"), Key.key("green_candle"), Key.key("light_blue_candle"),
                Key.key("light_gray_candle"), Key.key("lime_candle"), Key.key("magenta_candle"),
                Key.key("orange_candle"), Key.key("pink_candle"), Key.key("purple_candle"),
                Key.key("red_candle"), Key.key("white_candle"), Key.key("yellow_candle")));
        tags.put(Key.key("cat_collar_dyes"), List.of(Key.key("black_dye"), Key.key("blue_dye"),
                Key.key("brown_dye"), Key.key("cyan_dye"), Key.key("gray_dye"),
                Key.key("green_dye"), Key.key("light_blue_dye"), Key.key("light_gray_dye"),
                Key.key("lime_dye"), Key.key("magenta_dye"), Key.key("orange_dye"),
                Key.key("pink_dye"), Key.key("purple_dye"), Key.key("red_dye"),
                Key.key("white_dye"), Key.key("yellow_dye")));
        tags.put(Key.key("cat_food"), List.of(Key.key("cod"), Key.key("salmon")));
        tags.put(Key.key("cauldron_can_remove_dye"), List.of(Key.key("leather_boots"),
                Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_horse_armor"), Key.key("leather_leggings"),
                Key.key("wolf_armor")));
        tags.put(Key.key("chains"), List.of(Key.key("copper_chain"),
                Key.key("exposed_copper_chain"), Key.key("iron_chain"),
                Key.key("oxidized_copper_chain"), Key.key("waxed_copper_chain"),
                Key.key("waxed_exposed_copper_chain"), Key.key("waxed_oxidized_copper_chain"),
                Key.key("waxed_weathered_copper_chain"), Key.key("weathered_copper_chain")));
        tags.put(Key.key("cherry_logs"), List.of(Key.key("cherry_log"), Key.key("cherry_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood")));
        tags.put(Key.key("chest_armor"), List.of(Key.key("chainmail_chestplate"),
                Key.key("copper_chestplate"), Key.key("diamond_chestplate"),
                Key.key("golden_chestplate"), Key.key("iron_chestplate"),
                Key.key("leather_chestplate"), Key.key("netherite_chestplate")));
        tags.put(Key.key("chest_boats"), List.of(Key.key("acacia_chest_boat"),
                Key.key("bamboo_chest_raft"), Key.key("birch_chest_boat"),
                Key.key("cherry_chest_boat"), Key.key("dark_oak_chest_boat"),
                Key.key("jungle_chest_boat"), Key.key("mangrove_chest_boat"),
                Key.key("oak_chest_boat"), Key.key("pale_oak_chest_boat"),
                Key.key("poplar_chest_boat"), Key.key("spruce_chest_boat")));
        tags.put(Key.key("chicken_food"), List.of(Key.key("beetroot_seeds"), Key.key("melon_seeds"),
                Key.key("pitcher_pod"), Key.key("pumpkin_seeds"), Key.key("torchflower_seeds"),
                Key.key("wheat_seeds")));
        tags.put(Key.key("clonable_maps"), List.of(Key.key("abandoned_camp_map"),
                Key.key("buried_ancient_city_map"), Key.key("buried_mineshaft_map"),
                Key.key("buried_treasure_map"), Key.key("buried_trial_chambers_map"),
                Key.key("desert_pyramid_map"), Key.key("desert_village_map"), Key.key("filled_map"),
                Key.key("jungle_pyramid_map"), Key.key("ocean_monument_map"),
                Key.key("plains_village_map"), Key.key("savanna_village_map"),
                Key.key("snowy_village_map"), Key.key("swamp_hut_map"),
                Key.key("taiga_village_map"), Key.key("warm_ocean_ruins_map"),
                Key.key("woodland_mansion_map")));
        tags.put(Key.key("cluster_max_harvestables"), List.of(Key.key("copper_pickaxe"),
                Key.key("diamond_pickaxe"), Key.key("golden_pickaxe"), Key.key("iron_pickaxe"),
                Key.key("netherite_pickaxe"), Key.key("stone_pickaxe"), Key.key("wooden_pickaxe")));
        tags.put(Key.key("coal_ores"), List.of(Key.key("coal_ore"), Key.key("deepslate_coal_ore")));
        tags.put(Key.key("coals"), List.of(Key.key("charcoal"), Key.key("coal")));
        tags.put(Key.key("compasses"), List.of(Key.key("compass"), Key.key("recovery_compass")));
        tags.put(Key.key("completes_find_tree_tutorial"), List.of(Key.key("acacia_leaves"),
                Key.key("acacia_log"), Key.key("acacia_wood"), Key.key("azalea_leaves"),
                Key.key("birch_leaves"), Key.key("birch_log"), Key.key("birch_wood"),
                Key.key("cherry_leaves"), Key.key("cherry_log"), Key.key("cherry_wood"),
                Key.key("crimson_hyphae"), Key.key("crimson_stem"), Key.key("dark_oak_leaves"),
                Key.key("dark_oak_log"), Key.key("dark_oak_wood"),
                Key.key("flowering_azalea_leaves"), Key.key("jungle_leaves"), Key.key("jungle_log"),
                Key.key("jungle_wood"), Key.key("mangrove_leaves"), Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("nether_wart_block"), Key.key("oak_leaves"),
                Key.key("oak_log"), Key.key("oak_wood"), Key.key("orange_poplar_leaves"),
                Key.key("pale_oak_leaves"), Key.key("pale_oak_log"), Key.key("pale_oak_wood"),
                Key.key("poplar_log"), Key.key("poplar_wood"), Key.key("red_poplar_leaves"),
                Key.key("spruce_leaves"), Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("warped_hyphae"), Key.key("warped_stem"), Key.key("warped_wart_block"),
                Key.key("yellow_poplar_leaves")));
    }

    private static void itemTags2(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("concrete"), List.of(Key.key("black_concrete"), Key.key("blue_concrete"),
                Key.key("brown_concrete"), Key.key("cyan_concrete"), Key.key("gray_concrete"),
                Key.key("green_concrete"), Key.key("light_blue_concrete"),
                Key.key("light_gray_concrete"), Key.key("lime_concrete"),
                Key.key("magenta_concrete"), Key.key("orange_concrete"), Key.key("pink_concrete"),
                Key.key("purple_concrete"), Key.key("red_concrete"), Key.key("white_concrete"),
                Key.key("yellow_concrete")));
        tags.put(Key.key("concrete_powders"), List.of(Key.key("black_concrete_powder"),
                Key.key("blue_concrete_powder"), Key.key("brown_concrete_powder"),
                Key.key("cyan_concrete_powder"), Key.key("gray_concrete_powder"),
                Key.key("green_concrete_powder"), Key.key("light_blue_concrete_powder"),
                Key.key("light_gray_concrete_powder"), Key.key("lime_concrete_powder"),
                Key.key("magenta_concrete_powder"), Key.key("orange_concrete_powder"),
                Key.key("pink_concrete_powder"), Key.key("purple_concrete_powder"),
                Key.key("red_concrete_powder"), Key.key("white_concrete_powder"),
                Key.key("yellow_concrete_powder")));
        tags.put(Key.key("concrete_slabs"), List.of(Key.key("black_concrete_slab"),
                Key.key("blue_concrete_slab"), Key.key("brown_concrete_slab"),
                Key.key("cyan_concrete_slab"), Key.key("gray_concrete_slab"),
                Key.key("green_concrete_slab"), Key.key("light_blue_concrete_slab"),
                Key.key("light_gray_concrete_slab"), Key.key("lime_concrete_slab"),
                Key.key("magenta_concrete_slab"), Key.key("orange_concrete_slab"),
                Key.key("pink_concrete_slab"), Key.key("purple_concrete_slab"),
                Key.key("red_concrete_slab"), Key.key("white_concrete_slab"),
                Key.key("yellow_concrete_slab")));
        tags.put(Key.key("concrete_stairs"), List.of(Key.key("black_concrete_stairs"),
                Key.key("blue_concrete_stairs"), Key.key("brown_concrete_stairs"),
                Key.key("cyan_concrete_stairs"), Key.key("gray_concrete_stairs"),
                Key.key("green_concrete_stairs"), Key.key("light_blue_concrete_stairs"),
                Key.key("light_gray_concrete_stairs"), Key.key("lime_concrete_stairs"),
                Key.key("magenta_concrete_stairs"), Key.key("orange_concrete_stairs"),
                Key.key("pink_concrete_stairs"), Key.key("purple_concrete_stairs"),
                Key.key("red_concrete_stairs"), Key.key("white_concrete_stairs"),
                Key.key("yellow_concrete_stairs")));
        tags.put(Key.key("copper"), List.of(Key.key("copper_block"), Key.key("exposed_copper"),
                Key.key("oxidized_copper"), Key.key("waxed_copper_block"),
                Key.key("waxed_exposed_copper"), Key.key("waxed_oxidized_copper"),
                Key.key("waxed_weathered_copper"), Key.key("weathered_copper")));
        tags.put(Key.key("copper_chests"), List.of(Key.key("copper_chest"),
                Key.key("exposed_copper_chest"), Key.key("oxidized_copper_chest"),
                Key.key("waxed_copper_chest"), Key.key("waxed_exposed_copper_chest"),
                Key.key("waxed_oxidized_copper_chest"), Key.key("waxed_weathered_copper_chest"),
                Key.key("weathered_copper_chest")));
        tags.put(Key.key("copper_golem_statues"), List.of(Key.key("copper_golem_statue"),
                Key.key("exposed_copper_golem_statue"), Key.key("oxidized_copper_golem_statue"),
                Key.key("waxed_copper_golem_statue"), Key.key("waxed_exposed_copper_golem_statue"),
                Key.key("waxed_oxidized_copper_golem_statue"),
                Key.key("waxed_weathered_copper_golem_statue"),
                Key.key("weathered_copper_golem_statue")));
        tags.put(Key.key("copper_ores"), List.of(Key.key("copper_ore"),
                Key.key("deepslate_copper_ore")));
        tags.put(Key.key("copper_tool_materials"), List.of(Key.key("copper_ingot")));
        tags.put(Key.key("cow_food"), List.of(Key.key("wheat")));
        tags.put(Key.key("creeper_drop_music_discs"), List.of(Key.key("music_disc_11"),
                Key.key("music_disc_13"), Key.key("music_disc_blocks"), Key.key("music_disc_cat"),
                Key.key("music_disc_chirp"), Key.key("music_disc_far"), Key.key("music_disc_mall"),
                Key.key("music_disc_mellohi"), Key.key("music_disc_stal"),
                Key.key("music_disc_strad"), Key.key("music_disc_wait"),
                Key.key("music_disc_ward")));
        tags.put(Key.key("creeper_igniters"), List.of(Key.key("fire_charge"),
                Key.key("flint_and_steel")));
        tags.put(Key.key("crimson_stems"), List.of(Key.key("crimson_hyphae"),
                Key.key("crimson_stem"), Key.key("stripped_crimson_hyphae"),
                Key.key("stripped_crimson_stem")));
        tags.put(Key.key("cushions"), List.of(Key.key("black_cushion"), Key.key("blue_cushion"),
                Key.key("brown_cushion"), Key.key("cyan_cushion"), Key.key("gray_cushion"),
                Key.key("green_cushion"), Key.key("light_blue_cushion"),
                Key.key("light_gray_cushion"), Key.key("lime_cushion"), Key.key("magenta_cushion"),
                Key.key("orange_cushion"), Key.key("pink_cushion"), Key.key("purple_cushion"),
                Key.key("red_cushion"), Key.key("white_cushion"), Key.key("yellow_cushion")));
        tags.put(Key.key("dampens_vibrations"), List.of(Key.key("black_carpet"),
                Key.key("black_wool"), Key.key("black_wool_slab"), Key.key("black_wool_stairs"),
                Key.key("blue_carpet"), Key.key("blue_wool"), Key.key("blue_wool_slab"),
                Key.key("blue_wool_stairs"), Key.key("brown_carpet"), Key.key("brown_wool"),
                Key.key("brown_wool_slab"), Key.key("brown_wool_stairs"), Key.key("cyan_carpet"),
                Key.key("cyan_wool"), Key.key("cyan_wool_slab"), Key.key("cyan_wool_stairs"),
                Key.key("gray_carpet"), Key.key("gray_wool"), Key.key("gray_wool_slab"),
                Key.key("gray_wool_stairs"), Key.key("green_carpet"), Key.key("green_wool"),
                Key.key("green_wool_slab"), Key.key("green_wool_stairs"),
                Key.key("light_blue_carpet"), Key.key("light_blue_wool"),
                Key.key("light_blue_wool_slab"), Key.key("light_blue_wool_stairs"),
                Key.key("light_gray_carpet"), Key.key("light_gray_wool"),
                Key.key("light_gray_wool_slab"), Key.key("light_gray_wool_stairs"),
                Key.key("lime_carpet"), Key.key("lime_wool"), Key.key("lime_wool_slab"),
                Key.key("lime_wool_stairs"), Key.key("magenta_carpet"), Key.key("magenta_wool"),
                Key.key("magenta_wool_slab"), Key.key("magenta_wool_stairs"),
                Key.key("orange_carpet"), Key.key("orange_wool"), Key.key("orange_wool_slab"),
                Key.key("orange_wool_stairs"), Key.key("pink_carpet"), Key.key("pink_wool"),
                Key.key("pink_wool_slab"), Key.key("pink_wool_stairs"), Key.key("purple_carpet"),
                Key.key("purple_wool"), Key.key("purple_wool_slab"), Key.key("purple_wool_stairs"),
                Key.key("red_carpet"), Key.key("red_wool"), Key.key("red_wool_slab"),
                Key.key("red_wool_stairs"), Key.key("white_carpet"), Key.key("white_wool"),
                Key.key("white_wool_slab"), Key.key("white_wool_stairs"), Key.key("yellow_carpet"),
                Key.key("yellow_wool"), Key.key("yellow_wool_slab"),
                Key.key("yellow_wool_stairs")));
    }

    private static void itemTags3(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("dark_oak_logs"), List.of(Key.key("dark_oak_log"),
                Key.key("dark_oak_wood"), Key.key("stripped_dark_oak_log"),
                Key.key("stripped_dark_oak_wood")));
        tags.put(Key.key("decorated_pot_ingredients"), List.of(Key.key("angler_pottery_sherd"),
                Key.key("archer_pottery_sherd"), Key.key("arms_up_pottery_sherd"),
                Key.key("blade_pottery_sherd"), Key.key("brewer_pottery_sherd"), Key.key("brick"),
                Key.key("burn_pottery_sherd"), Key.key("danger_pottery_sherd"),
                Key.key("explorer_pottery_sherd"), Key.key("flow_pottery_sherd"),
                Key.key("friend_pottery_sherd"), Key.key("guster_pottery_sherd"),
                Key.key("heart_pottery_sherd"), Key.key("heartbreak_pottery_sherd"),
                Key.key("howl_pottery_sherd"), Key.key("miner_pottery_sherd"),
                Key.key("mourner_pottery_sherd"), Key.key("plenty_pottery_sherd"),
                Key.key("prize_pottery_sherd"), Key.key("scrape_pottery_sherd"),
                Key.key("sheaf_pottery_sherd"), Key.key("shelter_pottery_sherd"),
                Key.key("skull_pottery_sherd"), Key.key("snort_pottery_sherd")));
        tags.put(Key.key("decorated_pot_sherds"), List.of(Key.key("angler_pottery_sherd"),
                Key.key("archer_pottery_sherd"), Key.key("arms_up_pottery_sherd"),
                Key.key("blade_pottery_sherd"), Key.key("brewer_pottery_sherd"),
                Key.key("burn_pottery_sherd"), Key.key("danger_pottery_sherd"),
                Key.key("explorer_pottery_sherd"), Key.key("flow_pottery_sherd"),
                Key.key("friend_pottery_sherd"), Key.key("guster_pottery_sherd"),
                Key.key("heart_pottery_sherd"), Key.key("heartbreak_pottery_sherd"),
                Key.key("howl_pottery_sherd"), Key.key("miner_pottery_sherd"),
                Key.key("mourner_pottery_sherd"), Key.key("plenty_pottery_sherd"),
                Key.key("prize_pottery_sherd"), Key.key("scrape_pottery_sherd"),
                Key.key("sheaf_pottery_sherd"), Key.key("shelter_pottery_sherd"),
                Key.key("skull_pottery_sherd"), Key.key("snort_pottery_sherd")));
        tags.put(Key.key("diamond_ores"), List.of(Key.key("deepslate_diamond_ore"),
                Key.key("diamond_ore")));
        tags.put(Key.key("diamond_tool_materials"), List.of(Key.key("diamond")));
        tags.put(Key.key("dirt"), List.of(Key.key("coarse_dirt"), Key.key("dirt"),
                Key.key("rooted_dirt")));
        tags.put(Key.key("doors"), List.of(Key.key("acacia_door"), Key.key("bamboo_door"),
                Key.key("birch_door"), Key.key("cherry_door"), Key.key("copper_door"),
                Key.key("crimson_door"), Key.key("dark_oak_door"), Key.key("exposed_copper_door"),
                Key.key("iron_door"), Key.key("jungle_door"), Key.key("mangrove_door"),
                Key.key("oak_door"), Key.key("oxidized_copper_door"), Key.key("pale_oak_door"),
                Key.key("poplar_door"), Key.key("spruce_door"), Key.key("warped_door"),
                Key.key("waxed_copper_door"), Key.key("waxed_exposed_copper_door"),
                Key.key("waxed_oxidized_copper_door"), Key.key("waxed_weathered_copper_door"),
                Key.key("weathered_copper_door")));
        tags.put(Key.key("douses_campfires"), List.of(Key.key("copper_shovel"),
                Key.key("diamond_shovel"), Key.key("golden_shovel"), Key.key("iron_shovel"),
                Key.key("netherite_shovel"), Key.key("stone_shovel"), Key.key("wooden_shovel")));
        tags.put(Key.key("drowned_preferred_weapons"), List.of(Key.key("trident")));
        tags.put(Key.key("duplicates_allays"), List.of(Key.key("amethyst_shard")));
        tags.put(Key.key("dyes"), List.of(Key.key("black_dye"), Key.key("blue_dye"),
                Key.key("brown_dye"), Key.key("cyan_dye"), Key.key("gray_dye"),
                Key.key("green_dye"), Key.key("light_blue_dye"), Key.key("light_gray_dye"),
                Key.key("lime_dye"), Key.key("magenta_dye"), Key.key("orange_dye"),
                Key.key("pink_dye"), Key.key("purple_dye"), Key.key("red_dye"),
                Key.key("white_dye"), Key.key("yellow_dye")));
        tags.put(Key.key("eggs"), List.of(Key.key("blue_egg"), Key.key("brown_egg"),
                Key.key("egg")));
        tags.put(Key.key("emerald_ores"), List.of(Key.key("deepslate_emerald_ore"),
                Key.key("emerald_ore")));
        tags.put(Key.key("enchantable/armor"), List.of(Key.key("chainmail_boots"),
                Key.key("chainmail_chestplate"), Key.key("chainmail_helmet"),
                Key.key("chainmail_leggings"), Key.key("copper_boots"),
                Key.key("copper_chestplate"), Key.key("copper_helmet"), Key.key("copper_leggings"),
                Key.key("diamond_boots"), Key.key("diamond_chestplate"), Key.key("diamond_helmet"),
                Key.key("diamond_leggings"), Key.key("golden_boots"), Key.key("golden_chestplate"),
                Key.key("golden_helmet"), Key.key("golden_leggings"), Key.key("iron_boots"),
                Key.key("iron_chestplate"), Key.key("iron_helmet"), Key.key("iron_leggings"),
                Key.key("leather_boots"), Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_leggings"), Key.key("netherite_boots"),
                Key.key("netherite_chestplate"), Key.key("netherite_helmet"),
                Key.key("netherite_leggings"), Key.key("turtle_helmet")));
        tags.put(Key.key("enchantable/bow"), List.of(Key.key("bow")));
        tags.put(Key.key("enchantable/chest_armor"), List.of(Key.key("chainmail_chestplate"),
                Key.key("copper_chestplate"), Key.key("diamond_chestplate"),
                Key.key("golden_chestplate"), Key.key("iron_chestplate"),
                Key.key("leather_chestplate"), Key.key("netherite_chestplate")));
        tags.put(Key.key("enchantable/crossbow"), List.of(Key.key("crossbow")));
        tags.put(Key.key("enchantable/durability"), List.of(Key.key("bow"), Key.key("brush"),
                Key.key("carrot_on_a_stick"), Key.key("chainmail_boots"),
                Key.key("chainmail_chestplate"), Key.key("chainmail_helmet"),
                Key.key("chainmail_leggings"), Key.key("copper_axe"), Key.key("copper_boots"),
                Key.key("copper_chestplate"), Key.key("copper_helmet"), Key.key("copper_hoe"),
                Key.key("copper_leggings"), Key.key("copper_pickaxe"), Key.key("copper_shovel"),
                Key.key("copper_spear"), Key.key("copper_sword"), Key.key("crossbow"),
                Key.key("diamond_axe"), Key.key("diamond_boots"), Key.key("diamond_chestplate"),
                Key.key("diamond_helmet"), Key.key("diamond_hoe"), Key.key("diamond_leggings"),
                Key.key("diamond_pickaxe"), Key.key("diamond_shovel"), Key.key("diamond_spear"),
                Key.key("diamond_sword"), Key.key("elytra"), Key.key("fishing_rod"),
                Key.key("flint_and_steel"), Key.key("golden_axe"), Key.key("golden_boots"),
                Key.key("golden_chestplate"), Key.key("golden_helmet"), Key.key("golden_hoe"),
                Key.key("golden_leggings"), Key.key("golden_pickaxe"), Key.key("golden_shovel"),
                Key.key("golden_spear"), Key.key("golden_sword"), Key.key("iron_axe"),
                Key.key("iron_boots"), Key.key("iron_chestplate"), Key.key("iron_helmet"),
                Key.key("iron_hoe"), Key.key("iron_leggings"), Key.key("iron_pickaxe"),
                Key.key("iron_shovel"), Key.key("iron_spear"), Key.key("iron_sword"),
                Key.key("leather_boots"), Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_leggings"), Key.key("mace"), Key.key("netherite_axe"),
                Key.key("netherite_boots"), Key.key("netherite_chestplate"),
                Key.key("netherite_helmet"), Key.key("netherite_hoe"),
                Key.key("netherite_leggings"), Key.key("netherite_pickaxe"),
                Key.key("netherite_shovel"), Key.key("netherite_spear"), Key.key("netherite_sword"),
                Key.key("shears"), Key.key("shield"), Key.key("stone_axe"), Key.key("stone_hoe"),
                Key.key("stone_pickaxe"), Key.key("stone_shovel"), Key.key("stone_spear"),
                Key.key("stone_sword"), Key.key("trident"), Key.key("turtle_helmet"),
                Key.key("warped_fungus_on_a_stick"), Key.key("wooden_axe"), Key.key("wooden_hoe"),
                Key.key("wooden_pickaxe"), Key.key("wooden_shovel"), Key.key("wooden_spear"),
                Key.key("wooden_sword")));
    }

    private static void itemTags4(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("enchantable/equippable"), List.of(Key.key("carved_pumpkin"),
                Key.key("chainmail_boots"), Key.key("chainmail_chestplate"),
                Key.key("chainmail_helmet"), Key.key("chainmail_leggings"), Key.key("copper_boots"),
                Key.key("copper_chestplate"), Key.key("copper_helmet"), Key.key("copper_leggings"),
                Key.key("creeper_head"), Key.key("diamond_boots"), Key.key("diamond_chestplate"),
                Key.key("diamond_helmet"), Key.key("diamond_leggings"), Key.key("dragon_head"),
                Key.key("elytra"), Key.key("golden_boots"), Key.key("golden_chestplate"),
                Key.key("golden_helmet"), Key.key("golden_leggings"), Key.key("iron_boots"),
                Key.key("iron_chestplate"), Key.key("iron_helmet"), Key.key("iron_leggings"),
                Key.key("leather_boots"), Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_leggings"), Key.key("netherite_boots"),
                Key.key("netherite_chestplate"), Key.key("netherite_helmet"),
                Key.key("netherite_leggings"), Key.key("piglin_head"), Key.key("player_head"),
                Key.key("skeleton_skull"), Key.key("turtle_helmet"),
                Key.key("wither_skeleton_skull"), Key.key("zombie_head")));
        tags.put(Key.key("enchantable/fire_aspect"), List.of(Key.key("copper_spear"),
                Key.key("copper_sword"), Key.key("diamond_spear"), Key.key("diamond_sword"),
                Key.key("golden_spear"), Key.key("golden_sword"), Key.key("iron_spear"),
                Key.key("iron_sword"), Key.key("mace"), Key.key("netherite_spear"),
                Key.key("netherite_sword"), Key.key("stone_spear"), Key.key("stone_sword"),
                Key.key("wooden_spear"), Key.key("wooden_sword")));
        tags.put(Key.key("enchantable/fishing"), List.of(Key.key("fishing_rod")));
        tags.put(Key.key("enchantable/foot_armor"), List.of(Key.key("chainmail_boots"),
                Key.key("copper_boots"), Key.key("diamond_boots"), Key.key("golden_boots"),
                Key.key("iron_boots"), Key.key("leather_boots"), Key.key("netherite_boots")));
        tags.put(Key.key("enchantable/head_armor"), List.of(Key.key("chainmail_helmet"),
                Key.key("copper_helmet"), Key.key("diamond_helmet"), Key.key("golden_helmet"),
                Key.key("iron_helmet"), Key.key("leather_helmet"), Key.key("netherite_helmet"),
                Key.key("turtle_helmet")));
        tags.put(Key.key("enchantable/leg_armor"), List.of(Key.key("chainmail_leggings"),
                Key.key("copper_leggings"), Key.key("diamond_leggings"), Key.key("golden_leggings"),
                Key.key("iron_leggings"), Key.key("leather_leggings"),
                Key.key("netherite_leggings")));
        tags.put(Key.key("enchantable/lunge"), List.of(Key.key("copper_spear"),
                Key.key("diamond_spear"), Key.key("golden_spear"), Key.key("iron_spear"),
                Key.key("netherite_spear"), Key.key("stone_spear"), Key.key("wooden_spear")));
        tags.put(Key.key("enchantable/mace"), List.of(Key.key("mace")));
        tags.put(Key.key("enchantable/melee_weapon"), List.of(Key.key("copper_spear"),
                Key.key("copper_sword"), Key.key("diamond_spear"), Key.key("diamond_sword"),
                Key.key("golden_spear"), Key.key("golden_sword"), Key.key("iron_spear"),
                Key.key("iron_sword"), Key.key("netherite_spear"), Key.key("netherite_sword"),
                Key.key("stone_spear"), Key.key("stone_sword"), Key.key("wooden_spear"),
                Key.key("wooden_sword")));
        tags.put(Key.key("enchantable/mining"), List.of(Key.key("copper_axe"),
                Key.key("copper_hoe"), Key.key("copper_pickaxe"), Key.key("copper_shovel"),
                Key.key("diamond_axe"), Key.key("diamond_hoe"), Key.key("diamond_pickaxe"),
                Key.key("diamond_shovel"), Key.key("golden_axe"), Key.key("golden_hoe"),
                Key.key("golden_pickaxe"), Key.key("golden_shovel"), Key.key("iron_axe"),
                Key.key("iron_hoe"), Key.key("iron_pickaxe"), Key.key("iron_shovel"),
                Key.key("netherite_axe"), Key.key("netherite_hoe"), Key.key("netherite_pickaxe"),
                Key.key("netherite_shovel"), Key.key("shears"), Key.key("stone_axe"),
                Key.key("stone_hoe"), Key.key("stone_pickaxe"), Key.key("stone_shovel"),
                Key.key("wooden_axe"), Key.key("wooden_hoe"), Key.key("wooden_pickaxe"),
                Key.key("wooden_shovel")));
        tags.put(Key.key("enchantable/mining_loot"), List.of(Key.key("copper_axe"),
                Key.key("copper_hoe"), Key.key("copper_pickaxe"), Key.key("copper_shovel"),
                Key.key("diamond_axe"), Key.key("diamond_hoe"), Key.key("diamond_pickaxe"),
                Key.key("diamond_shovel"), Key.key("golden_axe"), Key.key("golden_hoe"),
                Key.key("golden_pickaxe"), Key.key("golden_shovel"), Key.key("iron_axe"),
                Key.key("iron_hoe"), Key.key("iron_pickaxe"), Key.key("iron_shovel"),
                Key.key("netherite_axe"), Key.key("netherite_hoe"), Key.key("netherite_pickaxe"),
                Key.key("netherite_shovel"), Key.key("stone_axe"), Key.key("stone_hoe"),
                Key.key("stone_pickaxe"), Key.key("stone_shovel"), Key.key("wooden_axe"),
                Key.key("wooden_hoe"), Key.key("wooden_pickaxe"), Key.key("wooden_shovel")));
    }

    private static void itemTags5(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("enchantable/sharp_weapon"), List.of(Key.key("copper_axe"),
                Key.key("copper_spear"), Key.key("copper_sword"), Key.key("diamond_axe"),
                Key.key("diamond_spear"), Key.key("diamond_sword"), Key.key("golden_axe"),
                Key.key("golden_spear"), Key.key("golden_sword"), Key.key("iron_axe"),
                Key.key("iron_spear"), Key.key("iron_sword"), Key.key("netherite_axe"),
                Key.key("netherite_spear"), Key.key("netherite_sword"), Key.key("stone_axe"),
                Key.key("stone_spear"), Key.key("stone_sword"), Key.key("wooden_axe"),
                Key.key("wooden_spear"), Key.key("wooden_sword")));
        tags.put(Key.key("enchantable/sweeping"), List.of(Key.key("copper_sword"),
                Key.key("diamond_sword"), Key.key("golden_sword"), Key.key("iron_sword"),
                Key.key("netherite_sword"), Key.key("stone_sword"), Key.key("wooden_sword")));
        tags.put(Key.key("enchantable/trident"), List.of(Key.key("trident")));
        tags.put(Key.key("enchantable/vanishing"), List.of(Key.key("bow"), Key.key("brush"),
                Key.key("carrot_on_a_stick"), Key.key("carved_pumpkin"), Key.key("chainmail_boots"),
                Key.key("chainmail_chestplate"), Key.key("chainmail_helmet"),
                Key.key("chainmail_leggings"), Key.key("compass"), Key.key("copper_axe"),
                Key.key("copper_boots"), Key.key("copper_chestplate"), Key.key("copper_helmet"),
                Key.key("copper_hoe"), Key.key("copper_leggings"), Key.key("copper_pickaxe"),
                Key.key("copper_shovel"), Key.key("copper_spear"), Key.key("copper_sword"),
                Key.key("creeper_head"), Key.key("crossbow"), Key.key("diamond_axe"),
                Key.key("diamond_boots"), Key.key("diamond_chestplate"), Key.key("diamond_helmet"),
                Key.key("diamond_hoe"), Key.key("diamond_leggings"), Key.key("diamond_pickaxe"),
                Key.key("diamond_shovel"), Key.key("diamond_spear"), Key.key("diamond_sword"),
                Key.key("dragon_head"), Key.key("elytra"), Key.key("fishing_rod"),
                Key.key("flint_and_steel"), Key.key("golden_axe"), Key.key("golden_boots"),
                Key.key("golden_chestplate"), Key.key("golden_helmet"), Key.key("golden_hoe"),
                Key.key("golden_leggings"), Key.key("golden_pickaxe"), Key.key("golden_shovel"),
                Key.key("golden_spear"), Key.key("golden_sword"), Key.key("iron_axe"),
                Key.key("iron_boots"), Key.key("iron_chestplate"), Key.key("iron_helmet"),
                Key.key("iron_hoe"), Key.key("iron_leggings"), Key.key("iron_pickaxe"),
                Key.key("iron_shovel"), Key.key("iron_spear"), Key.key("iron_sword"),
                Key.key("leather_boots"), Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_leggings"), Key.key("mace"), Key.key("netherite_axe"),
                Key.key("netherite_boots"), Key.key("netherite_chestplate"),
                Key.key("netherite_helmet"), Key.key("netherite_hoe"),
                Key.key("netherite_leggings"), Key.key("netherite_pickaxe"),
                Key.key("netherite_shovel"), Key.key("netherite_spear"), Key.key("netherite_sword"),
                Key.key("piglin_head"), Key.key("player_head"), Key.key("shears"),
                Key.key("shield"), Key.key("skeleton_skull"), Key.key("stone_axe"),
                Key.key("stone_hoe"), Key.key("stone_pickaxe"), Key.key("stone_shovel"),
                Key.key("stone_spear"), Key.key("stone_sword"), Key.key("trident"),
                Key.key("turtle_helmet"), Key.key("warped_fungus_on_a_stick"),
                Key.key("wither_skeleton_skull"), Key.key("wooden_axe"), Key.key("wooden_hoe"),
                Key.key("wooden_pickaxe"), Key.key("wooden_shovel"), Key.key("wooden_spear"),
                Key.key("wooden_sword"), Key.key("zombie_head")));
        tags.put(Key.key("enchantable/weapon"), List.of(Key.key("copper_axe"),
                Key.key("copper_spear"), Key.key("copper_sword"), Key.key("diamond_axe"),
                Key.key("diamond_spear"), Key.key("diamond_sword"), Key.key("golden_axe"),
                Key.key("golden_spear"), Key.key("golden_sword"), Key.key("iron_axe"),
                Key.key("iron_spear"), Key.key("iron_sword"), Key.key("mace"),
                Key.key("netherite_axe"), Key.key("netherite_spear"), Key.key("netherite_sword"),
                Key.key("stone_axe"), Key.key("stone_spear"), Key.key("stone_sword"),
                Key.key("wooden_axe"), Key.key("wooden_spear"), Key.key("wooden_sword")));
        tags.put(Key.key("extendable_maps"), List.of(Key.key("filled_map")));
        tags.put(Key.key("fence_gates"), List.of(Key.key("acacia_fence_gate"),
                Key.key("bamboo_fence_gate"), Key.key("birch_fence_gate"),
                Key.key("cherry_fence_gate"), Key.key("crimson_fence_gate"),
                Key.key("dark_oak_fence_gate"), Key.key("jungle_fence_gate"),
                Key.key("mangrove_fence_gate"), Key.key("oak_fence_gate"),
                Key.key("pale_oak_fence_gate"), Key.key("poplar_fence_gate"),
                Key.key("spruce_fence_gate"), Key.key("warped_fence_gate")));
    }

    private static void itemTags6(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("fences"), List.of(Key.key("acacia_fence"), Key.key("bamboo_fence"),
                Key.key("birch_fence"), Key.key("cherry_fence"), Key.key("crimson_fence"),
                Key.key("dark_oak_fence"), Key.key("jungle_fence"), Key.key("mangrove_fence"),
                Key.key("nether_brick_fence"), Key.key("oak_fence"), Key.key("pale_oak_fence"),
                Key.key("poplar_fence"), Key.key("spruce_fence"), Key.key("warped_fence")));
        tags.put(Key.key("fishes"), List.of(Key.key("cod"), Key.key("cooked_cod"),
                Key.key("cooked_salmon"), Key.key("pufferfish"), Key.key("salmon"),
                Key.key("tropical_fish")));
        tags.put(Key.key("flowers"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("cactus_flower"), Key.key("cherry_leaves"),
                Key.key("chorus_flower"), Key.key("closed_eyeblossom"), Key.key("cornflower"),
                Key.key("dandelion"), Key.key("flowering_azalea"),
                Key.key("flowering_azalea_leaves"), Key.key("golden_dandelion"), Key.key("lilac"),
                Key.key("lily_of_the_valley"), Key.key("mangrove_propagule"),
                Key.key("open_eyeblossom"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("peony"), Key.key("pink_petals"), Key.key("pink_tulip"),
                Key.key("pitcher_plant"), Key.key("poppy"), Key.key("red_tulip"),
                Key.key("rose_bush"), Key.key("spore_blossom"), Key.key("sunflower"),
                Key.key("torchflower"), Key.key("white_tulip"), Key.key("wildflowers"),
                Key.key("wither_rose")));
        tags.put(Key.key("foot_armor"), List.of(Key.key("chainmail_boots"), Key.key("copper_boots"),
                Key.key("diamond_boots"), Key.key("golden_boots"), Key.key("iron_boots"),
                Key.key("leather_boots"), Key.key("netherite_boots")));
        tags.put(Key.key("fox_food"), List.of(Key.key("glow_berries"), Key.key("sweet_berries")));
        tags.put(Key.key("freeze_immune_wearables"), List.of(Key.key("leather_boots"),
                Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_horse_armor"), Key.key("leather_leggings")));
        tags.put(Key.key("frog_food"), List.of(Key.key("slime_ball")));
        tags.put(Key.key("furnace_fuel_bottom_takeable"), List.of(Key.key("bucket"),
                Key.key("water_bucket")));
        tags.put(Key.key("furnace_minecart_fuel"), List.of(Key.key("charcoal"), Key.key("coal")));
        tags.put(Key.key("gaze_disguise_equipment"), List.of(Key.key("carved_pumpkin")));
        tags.put(Key.key("glazed_terracotta"), List.of(Key.key("black_glazed_terracotta"),
                Key.key("blue_glazed_terracotta"), Key.key("brown_glazed_terracotta"),
                Key.key("cyan_glazed_terracotta"), Key.key("gray_glazed_terracotta"),
                Key.key("green_glazed_terracotta"), Key.key("light_blue_glazed_terracotta"),
                Key.key("light_gray_glazed_terracotta"), Key.key("lime_glazed_terracotta"),
                Key.key("magenta_glazed_terracotta"), Key.key("orange_glazed_terracotta"),
                Key.key("pink_glazed_terracotta"), Key.key("purple_glazed_terracotta"),
                Key.key("red_glazed_terracotta"), Key.key("white_glazed_terracotta"),
                Key.key("yellow_glazed_terracotta")));
        tags.put(Key.key("goat_food"), List.of(Key.key("wheat")));
        tags.put(Key.key("gold_ores"), List.of(Key.key("deepslate_gold_ore"), Key.key("gold_ore"),
                Key.key("nether_gold_ore")));
        tags.put(Key.key("gold_tool_materials"), List.of(Key.key("gold_ingot")));
        tags.put(Key.key("grass_blocks"), List.of(Key.key("grass_block"), Key.key("mycelium"),
                Key.key("podzol")));
        tags.put(Key.key("hanging_signs"), List.of(Key.key("acacia_hanging_sign"),
                Key.key("bamboo_hanging_sign"), Key.key("birch_hanging_sign"),
                Key.key("cherry_hanging_sign"), Key.key("crimson_hanging_sign"),
                Key.key("dark_oak_hanging_sign"), Key.key("jungle_hanging_sign"),
                Key.key("mangrove_hanging_sign"), Key.key("oak_hanging_sign"),
                Key.key("pale_oak_hanging_sign"), Key.key("poplar_hanging_sign"),
                Key.key("spruce_hanging_sign"), Key.key("warped_hanging_sign")));
        tags.put(Key.key("happy_ghast_food"), List.of(Key.key("snowball")));
        tags.put(Key.key("happy_ghast_tempt_items"), List.of(Key.key("black_harness"),
                Key.key("blue_harness"), Key.key("brown_harness"), Key.key("cyan_harness"),
                Key.key("gray_harness"), Key.key("green_harness"), Key.key("light_blue_harness"),
                Key.key("light_gray_harness"), Key.key("lime_harness"), Key.key("magenta_harness"),
                Key.key("orange_harness"), Key.key("pink_harness"), Key.key("purple_harness"),
                Key.key("red_harness"), Key.key("snowball"), Key.key("white_harness"),
                Key.key("yellow_harness")));
        tags.put(Key.key("harnesses"), List.of(Key.key("black_harness"), Key.key("blue_harness"),
                Key.key("brown_harness"), Key.key("cyan_harness"), Key.key("gray_harness"),
                Key.key("green_harness"), Key.key("light_blue_harness"),
                Key.key("light_gray_harness"), Key.key("lime_harness"), Key.key("magenta_harness"),
                Key.key("orange_harness"), Key.key("pink_harness"), Key.key("purple_harness"),
                Key.key("red_harness"), Key.key("white_harness"), Key.key("yellow_harness")));
        tags.put(Key.key("head_armor"), List.of(Key.key("chainmail_helmet"),
                Key.key("copper_helmet"), Key.key("diamond_helmet"), Key.key("golden_helmet"),
                Key.key("iron_helmet"), Key.key("leather_helmet"), Key.key("netherite_helmet"),
                Key.key("turtle_helmet")));
    }

    private static void itemTags7(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("hoes"), List.of(Key.key("copper_hoe"), Key.key("diamond_hoe"),
                Key.key("golden_hoe"), Key.key("iron_hoe"), Key.key("netherite_hoe"),
                Key.key("stone_hoe"), Key.key("wooden_hoe")));
        tags.put(Key.key("hoglin_food"), List.of(Key.key("crimson_fungus")));
        tags.put(Key.key("horse_food"), List.of(Key.key("apple"), Key.key("carrot"),
                Key.key("enchanted_golden_apple"), Key.key("golden_apple"),
                Key.key("golden_carrot"), Key.key("hay_block"), Key.key("sugar"),
                Key.key("wheat")));
        tags.put(Key.key("horse_tempt_items"), List.of(Key.key("enchanted_golden_apple"),
                Key.key("golden_apple"), Key.key("golden_carrot")));
        tags.put(Key.key("ignored_by_piglin_babies"), List.of(Key.key("leather")));
        tags.put(Key.key("iron_ores"), List.of(Key.key("deepslate_iron_ore"), Key.key("iron_ore")));
        tags.put(Key.key("iron_tool_materials"), List.of(Key.key("iron_ingot")));
        tags.put(Key.key("jungle_logs"), List.of(Key.key("jungle_log"), Key.key("jungle_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood")));
        tags.put(Key.key("lanterns"), List.of(Key.key("copper_lantern"),
                Key.key("exposed_copper_lantern"), Key.key("lantern"),
                Key.key("oxidized_copper_lantern"), Key.key("soul_lantern"),
                Key.key("waxed_copper_lantern"), Key.key("waxed_exposed_copper_lantern"),
                Key.key("waxed_oxidized_copper_lantern"), Key.key("waxed_weathered_copper_lantern"),
                Key.key("weathered_copper_lantern")));
        tags.put(Key.key("lapis_ores"), List.of(Key.key("deepslate_lapis_ore"),
                Key.key("lapis_ore")));
        tags.put(Key.key("leaves"), List.of(Key.key("acacia_leaves"), Key.key("azalea_leaves"),
                Key.key("birch_leaves"), Key.key("cherry_leaves"), Key.key("dark_oak_leaves"),
                Key.key("flowering_azalea_leaves"), Key.key("jungle_leaves"),
                Key.key("mangrove_leaves"), Key.key("oak_leaves"), Key.key("orange_poplar_leaves"),
                Key.key("pale_oak_leaves"), Key.key("red_poplar_leaves"), Key.key("spruce_leaves"),
                Key.key("yellow_poplar_leaves")));
        tags.put(Key.key("lectern_books"), List.of(Key.key("writable_book"),
                Key.key("written_book")));
        tags.put(Key.key("leg_armor"), List.of(Key.key("chainmail_leggings"),
                Key.key("copper_leggings"), Key.key("diamond_leggings"), Key.key("golden_leggings"),
                Key.key("iron_leggings"), Key.key("leather_leggings"),
                Key.key("netherite_leggings")));
        tags.put(Key.key("lightning_rods"), List.of(Key.key("exposed_lightning_rod"),
                Key.key("lightning_rod"), Key.key("oxidized_lightning_rod"),
                Key.key("waxed_exposed_lightning_rod"), Key.key("waxed_lightning_rod"),
                Key.key("waxed_oxidized_lightning_rod"), Key.key("waxed_weathered_lightning_rod"),
                Key.key("weathered_lightning_rod")));
        tags.put(Key.key("llama_food"), List.of(Key.key("hay_block"), Key.key("wheat")));
        tags.put(Key.key("llama_tempt_items"), List.of(Key.key("hay_block")));
        tags.put(Key.key("logs"), List.of(Key.key("acacia_log"), Key.key("acacia_wood"),
                Key.key("birch_log"), Key.key("birch_wood"), Key.key("cherry_log"),
                Key.key("cherry_wood"), Key.key("crimson_hyphae"), Key.key("crimson_stem"),
                Key.key("dark_oak_log"), Key.key("dark_oak_wood"), Key.key("jungle_log"),
                Key.key("jungle_wood"), Key.key("mangrove_log"), Key.key("mangrove_wood"),
                Key.key("oak_log"), Key.key("oak_wood"), Key.key("pale_oak_log"),
                Key.key("pale_oak_wood"), Key.key("poplar_log"), Key.key("poplar_wood"),
                Key.key("spruce_log"), Key.key("spruce_wood"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_birch_log"),
                Key.key("stripped_birch_wood"), Key.key("stripped_cherry_log"),
                Key.key("stripped_cherry_wood"), Key.key("stripped_crimson_hyphae"),
                Key.key("stripped_crimson_stem"), Key.key("stripped_dark_oak_log"),
                Key.key("stripped_dark_oak_wood"), Key.key("stripped_jungle_log"),
                Key.key("stripped_jungle_wood"), Key.key("stripped_mangrove_log"),
                Key.key("stripped_mangrove_wood"), Key.key("stripped_oak_log"),
                Key.key("stripped_oak_wood"), Key.key("stripped_pale_oak_log"),
                Key.key("stripped_pale_oak_wood"), Key.key("stripped_poplar_log"),
                Key.key("stripped_poplar_wood"), Key.key("stripped_spruce_log"),
                Key.key("stripped_spruce_wood"), Key.key("stripped_warped_hyphae"),
                Key.key("stripped_warped_stem"), Key.key("warped_hyphae"), Key.key("warped_stem")));
        tags.put(Key.key("logs_that_burn"), List.of(Key.key("acacia_log"), Key.key("acacia_wood"),
                Key.key("birch_log"), Key.key("birch_wood"), Key.key("cherry_log"),
                Key.key("cherry_wood"), Key.key("dark_oak_log"), Key.key("dark_oak_wood"),
                Key.key("jungle_log"), Key.key("jungle_wood"), Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("oak_log"), Key.key("oak_wood"),
                Key.key("pale_oak_log"), Key.key("pale_oak_wood"), Key.key("poplar_log"),
                Key.key("poplar_wood"), Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_acacia_log"), Key.key("stripped_acacia_wood"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood")));
    }

    private static void itemTags8(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("loom_dyes"), List.of(Key.key("black_dye"), Key.key("blue_dye"),
                Key.key("brown_dye"), Key.key("cyan_dye"), Key.key("gray_dye"),
                Key.key("green_dye"), Key.key("light_blue_dye"), Key.key("light_gray_dye"),
                Key.key("lime_dye"), Key.key("magenta_dye"), Key.key("orange_dye"),
                Key.key("pink_dye"), Key.key("purple_dye"), Key.key("red_dye"),
                Key.key("white_dye"), Key.key("yellow_dye")));
        tags.put(Key.key("loom_patterns"), List.of(Key.key("bordure_indented_banner_pattern"),
                Key.key("creeper_banner_pattern"), Key.key("field_masoned_banner_pattern"),
                Key.key("flow_banner_pattern"), Key.key("flower_banner_pattern"),
                Key.key("globe_banner_pattern"), Key.key("guster_banner_pattern"),
                Key.key("mojang_banner_pattern"), Key.key("piglin_banner_pattern"),
                Key.key("skull_banner_pattern")));
        tags.put(Key.key("mangrove_logs"), List.of(Key.key("mangrove_log"),
                Key.key("mangrove_wood"), Key.key("stripped_mangrove_log"),
                Key.key("stripped_mangrove_wood")));
        tags.put(Key.key("map_invisibility_equipment"), List.of(Key.key("carved_pumpkin")));
        tags.put(Key.key("meat"), List.of(Key.key("beef"), Key.key("chicken"),
                Key.key("cooked_beef"), Key.key("cooked_chicken"), Key.key("cooked_mutton"),
                Key.key("cooked_porkchop"), Key.key("cooked_rabbit"), Key.key("mutton"),
                Key.key("porkchop"), Key.key("rabbit"), Key.key("rotten_flesh")));
        tags.put(Key.key("metal_nuggets"), List.of(Key.key("copper_nugget"), Key.key("gold_nugget"),
                Key.key("iron_nugget")));
        tags.put(Key.key("moss_blocks"), List.of(Key.key("moss_block"),
                Key.key("pale_moss_block")));
        tags.put(Key.key("mud"), List.of(Key.key("mud"), Key.key("muddy_mangrove_roots")));
        tags.put(Key.key("mushrooms"), List.of(Key.key("brown_mushroom"), Key.key("red_mushroom"),
                Key.key("shelf_mushroom")));
        tags.put(Key.key("nautilus_bucket_food"), List.of(Key.key("cod_bucket"),
                Key.key("pufferfish_bucket"), Key.key("salmon_bucket"),
                Key.key("tropical_fish_bucket")));
        tags.put(Key.key("nautilus_food"), List.of(Key.key("cod"), Key.key("cod_bucket"),
                Key.key("cooked_cod"), Key.key("cooked_salmon"), Key.key("pufferfish"),
                Key.key("pufferfish_bucket"), Key.key("salmon"), Key.key("salmon_bucket"),
                Key.key("tropical_fish"), Key.key("tropical_fish_bucket")));
        tags.put(Key.key("nautilus_taming_items"), List.of(Key.key("pufferfish"),
                Key.key("pufferfish_bucket")));
        tags.put(Key.key("netherite_tool_materials"), List.of(Key.key("netherite_ingot")));
        tags.put(Key.key("non_flammable_wood"), List.of(Key.key("crimson_button"),
                Key.key("crimson_door"), Key.key("crimson_fence"), Key.key("crimson_fence_gate"),
                Key.key("crimson_hanging_sign"), Key.key("crimson_hyphae"),
                Key.key("crimson_planks"), Key.key("crimson_pressure_plate"),
                Key.key("crimson_shelf"), Key.key("crimson_sign"), Key.key("crimson_slab"),
                Key.key("crimson_stairs"), Key.key("crimson_stem"), Key.key("crimson_trapdoor"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("warped_button"), Key.key("warped_door"), Key.key("warped_fence"),
                Key.key("warped_fence_gate"), Key.key("warped_hanging_sign"),
                Key.key("warped_hyphae"), Key.key("warped_planks"),
                Key.key("warped_pressure_plate"), Key.key("warped_shelf"), Key.key("warped_sign"),
                Key.key("warped_slab"), Key.key("warped_stairs"), Key.key("warped_stem"),
                Key.key("warped_trapdoor")));
        tags.put(Key.key("noteblock_top_instruments"), List.of(Key.key("creeper_head"),
                Key.key("dragon_head"), Key.key("piglin_head"), Key.key("player_head"),
                Key.key("skeleton_skull"), Key.key("wither_skeleton_skull"),
                Key.key("zombie_head")));
        tags.put(Key.key("oak_logs"), List.of(Key.key("oak_log"), Key.key("oak_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood")));
        tags.put(Key.key("ocelot_food"), List.of(Key.key("cod"), Key.key("salmon")));
        tags.put(Key.key("ores"), List.of(Key.key("coal_ore"), Key.key("copper_ore"),
                Key.key("deepslate_coal_ore"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("diamond_ore"), Key.key("emerald_ore"), Key.key("gold_ore"),
                Key.key("iron_ore"), Key.key("lapis_ore"), Key.key("nether_gold_ore"),
                Key.key("nether_quartz_ore"), Key.key("redstone_ore")));
        tags.put(Key.key("pale_oak_logs"), List.of(Key.key("pale_oak_log"),
                Key.key("pale_oak_wood"), Key.key("stripped_pale_oak_log"),
                Key.key("stripped_pale_oak_wood")));
        tags.put(Key.key("panda_eats_from_ground"), List.of(Key.key("bamboo"), Key.key("cake")));
        tags.put(Key.key("panda_food"), List.of(Key.key("bamboo")));
        tags.put(Key.key("parrot_food"), List.of(Key.key("beetroot_seeds"), Key.key("melon_seeds"),
                Key.key("pitcher_pod"), Key.key("pumpkin_seeds"), Key.key("torchflower_seeds"),
                Key.key("wheat_seeds")));
        tags.put(Key.key("parrot_poisonous_food"), List.of(Key.key("cookie")));
        tags.put(Key.key("pickaxes"), List.of(Key.key("copper_pickaxe"), Key.key("diamond_pickaxe"),
                Key.key("golden_pickaxe"), Key.key("iron_pickaxe"), Key.key("netherite_pickaxe"),
                Key.key("stone_pickaxe"), Key.key("wooden_pickaxe")));
    }

    private static void itemTags9(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("pig_food"), List.of(Key.key("beetroot"), Key.key("carrot"),
                Key.key("potato")));
        tags.put(Key.key("piglin_food"), List.of(Key.key("cooked_porkchop"), Key.key("porkchop")));
        tags.put(Key.key("piglin_loved"), List.of(Key.key("bell"), Key.key("clock"),
                Key.key("deepslate_gold_ore"), Key.key("enchanted_golden_apple"),
                Key.key("gilded_blackstone"), Key.key("glistering_melon_slice"),
                Key.key("gold_block"), Key.key("gold_ingot"), Key.key("gold_ore"),
                Key.key("golden_apple"), Key.key("golden_axe"), Key.key("golden_boots"),
                Key.key("golden_carrot"), Key.key("golden_chestplate"), Key.key("golden_dandelion"),
                Key.key("golden_helmet"), Key.key("golden_hoe"), Key.key("golden_horse_armor"),
                Key.key("golden_leggings"), Key.key("golden_nautilus_armor"),
                Key.key("golden_pickaxe"), Key.key("golden_shovel"), Key.key("golden_spear"),
                Key.key("golden_sword"), Key.key("light_weighted_pressure_plate"),
                Key.key("nether_gold_ore"), Key.key("raw_gold"), Key.key("raw_gold_block")));
        tags.put(Key.key("piglin_preferred_weapons"), List.of(Key.key("crossbow"),
                Key.key("golden_spear")));
        tags.put(Key.key("piglin_repellents"), List.of(Key.key("soul_campfire"),
                Key.key("soul_lantern"), Key.key("soul_torch")));
        tags.put(Key.key("piglin_safe_armor"), List.of(Key.key("golden_boots"),
                Key.key("golden_chestplate"), Key.key("golden_helmet"),
                Key.key("golden_leggings")));
        tags.put(Key.key("pillager_preferred_weapons"), List.of(Key.key("crossbow")));
        tags.put(Key.key("planks"), List.of(Key.key("acacia_planks"), Key.key("bamboo_planks"),
                Key.key("birch_planks"), Key.key("cherry_planks"), Key.key("crimson_planks"),
                Key.key("dark_oak_planks"), Key.key("jungle_planks"), Key.key("mangrove_planks"),
                Key.key("oak_planks"), Key.key("pale_oak_planks"), Key.key("poplar_planks"),
                Key.key("spruce_planks"), Key.key("warped_planks")));
        tags.put(Key.key("poplar_logs"), List.of(Key.key("poplar_log"), Key.key("poplar_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood")));
        tags.put(Key.key("rabbit_food"), List.of(Key.key("carrot"), Key.key("dandelion"),
                Key.key("golden_carrot")));
        tags.put(Key.key("rails"), List.of(Key.key("activator_rail"), Key.key("detector_rail"),
                Key.key("powered_rail"), Key.key("rail")));
        tags.put(Key.key("redstone_ores"), List.of(Key.key("deepslate_redstone_ore"),
                Key.key("redstone_ore")));
        tags.put(Key.key("repairs_chain_armor"), List.of(Key.key("iron_ingot")));
        tags.put(Key.key("repairs_copper_armor"), List.of(Key.key("copper_ingot")));
        tags.put(Key.key("repairs_diamond_armor"), List.of(Key.key("diamond")));
        tags.put(Key.key("repairs_gold_armor"), List.of(Key.key("gold_ingot")));
        tags.put(Key.key("repairs_iron_armor"), List.of(Key.key("iron_ingot")));
        tags.put(Key.key("repairs_leather_armor"), List.of(Key.key("leather")));
        tags.put(Key.key("repairs_netherite_armor"), List.of(Key.key("netherite_ingot")));
        tags.put(Key.key("repairs_turtle_helmet"), List.of(Key.key("turtle_scute")));
        tags.put(Key.key("repairs_wolf_armor"), List.of(Key.key("armadillo_scute")));
        tags.put(Key.key("sand"), List.of(Key.key("red_sand"), Key.key("sand"),
                Key.key("suspicious_sand")));
        tags.put(Key.key("saplings"), List.of(Key.key("acacia_sapling"), Key.key("azalea"),
                Key.key("birch_sapling"), Key.key("cherry_sapling"), Key.key("dark_oak_sapling"),
                Key.key("flowering_azalea"), Key.key("jungle_sapling"),
                Key.key("mangrove_propagule"), Key.key("oak_sapling"), Key.key("pale_oak_sapling"),
                Key.key("poplar_sapling"), Key.key("spruce_sapling")));
        tags.put(Key.key("shearable_from_copper_golem"), List.of(Key.key("poppy")));
        tags.put(Key.key("sheep_food"), List.of(Key.key("wheat")));
        tags.put(Key.key("shovels"), List.of(Key.key("copper_shovel"), Key.key("diamond_shovel"),
                Key.key("golden_shovel"), Key.key("iron_shovel"), Key.key("netherite_shovel"),
                Key.key("stone_shovel"), Key.key("wooden_shovel")));
        tags.put(Key.key("shulker_boxes"), List.of(Key.key("black_shulker_box"),
                Key.key("blue_shulker_box"), Key.key("brown_shulker_box"),
                Key.key("cyan_shulker_box"), Key.key("gray_shulker_box"),
                Key.key("green_shulker_box"), Key.key("light_blue_shulker_box"),
                Key.key("light_gray_shulker_box"), Key.key("lime_shulker_box"),
                Key.key("magenta_shulker_box"), Key.key("orange_shulker_box"),
                Key.key("pink_shulker_box"), Key.key("purple_shulker_box"),
                Key.key("red_shulker_box"), Key.key("shulker_box"), Key.key("white_shulker_box"),
                Key.key("yellow_shulker_box")));
        tags.put(Key.key("signs"), List.of(Key.key("acacia_sign"), Key.key("bamboo_sign"),
                Key.key("birch_sign"), Key.key("cherry_sign"), Key.key("crimson_sign"),
                Key.key("dark_oak_sign"), Key.key("jungle_sign"), Key.key("mangrove_sign"),
                Key.key("oak_sign"), Key.key("pale_oak_sign"), Key.key("poplar_sign"),
                Key.key("spruce_sign"), Key.key("warped_sign")));
        tags.put(Key.key("skeleton_preferred_weapons"), List.of(Key.key("bow")));
        tags.put(Key.key("skulls"), List.of(Key.key("creeper_head"), Key.key("dragon_head"),
                Key.key("piglin_head"), Key.key("player_head"), Key.key("skeleton_skull"),
                Key.key("wither_skeleton_skull"), Key.key("zombie_head")));
        tags.put(Key.key("slabs"), List.of(Key.key("acacia_slab"), Key.key("andesite_slab"),
                Key.key("bamboo_mosaic_slab"), Key.key("bamboo_slab"), Key.key("birch_slab"),
                Key.key("black_concrete_slab"), Key.key("black_wool_slab"),
                Key.key("blackstone_slab"), Key.key("blue_concrete_slab"),
                Key.key("blue_wool_slab"), Key.key("brick_slab"), Key.key("brown_concrete_slab"),
                Key.key("brown_wool_slab"), Key.key("cherry_slab"), Key.key("cinnabar_brick_slab"),
                Key.key("cinnabar_slab"), Key.key("cobbled_deepslate_slab"),
                Key.key("cobblestone_slab"), Key.key("crimson_slab"), Key.key("cut_copper_slab"),
                Key.key("cut_red_sandstone_slab"), Key.key("cut_sandstone_slab"),
                Key.key("cyan_concrete_slab"), Key.key("cyan_wool_slab"), Key.key("dark_oak_slab"),
                Key.key("dark_prismarine_slab"), Key.key("deepslate_brick_slab"),
                Key.key("deepslate_tile_slab"), Key.key("diorite_slab"),
                Key.key("end_stone_brick_slab"), Key.key("exposed_cut_copper_slab"),
                Key.key("granite_slab"), Key.key("gray_concrete_slab"), Key.key("gray_wool_slab"),
                Key.key("green_concrete_slab"), Key.key("green_wool_slab"), Key.key("jungle_slab"),
                Key.key("light_blue_concrete_slab"), Key.key("light_blue_wool_slab"),
                Key.key("light_gray_concrete_slab"), Key.key("light_gray_wool_slab"),
                Key.key("lime_concrete_slab"), Key.key("lime_wool_slab"),
                Key.key("magenta_concrete_slab"), Key.key("magenta_wool_slab"),
                Key.key("mangrove_slab"), Key.key("mossy_cobblestone_slab"),
                Key.key("mossy_stone_brick_slab"), Key.key("mud_brick_slab"),
                Key.key("nether_brick_slab"), Key.key("oak_slab"), Key.key("orange_concrete_slab"),
                Key.key("orange_wool_slab"), Key.key("oxidized_cut_copper_slab"),
                Key.key("pale_oak_slab"), Key.key("petrified_oak_slab"),
                Key.key("pink_concrete_slab"), Key.key("pink_wool_slab"),
                Key.key("polished_andesite_slab"), Key.key("polished_blackstone_brick_slab"),
                Key.key("polished_blackstone_slab"), Key.key("polished_cinnabar_slab"),
                Key.key("polished_deepslate_slab"), Key.key("polished_diorite_slab"),
                Key.key("polished_granite_slab"), Key.key("polished_sulfur_slab"),
                Key.key("polished_tuff_slab"), Key.key("poplar_slab"),
                Key.key("prismarine_brick_slab"), Key.key("prismarine_slab"),
                Key.key("purple_concrete_slab"), Key.key("purple_wool_slab"),
                Key.key("purpur_slab"), Key.key("quartz_slab"), Key.key("red_concrete_slab"),
                Key.key("red_nether_brick_slab"), Key.key("red_sandstone_slab"),
                Key.key("red_wool_slab"), Key.key("resin_brick_slab"), Key.key("sandstone_slab"),
                Key.key("smooth_quartz_slab"), Key.key("smooth_red_sandstone_slab"),
                Key.key("smooth_sandstone_slab"), Key.key("smooth_stone_slab"),
                Key.key("spruce_slab"), Key.key("stone_brick_slab"), Key.key("stone_slab"),
                Key.key("sulfur_brick_slab"), Key.key("sulfur_slab"), Key.key("tuff_brick_slab"),
                Key.key("tuff_slab"), Key.key("warped_slab"), Key.key("waxed_cut_copper_slab"),
                Key.key("waxed_exposed_cut_copper_slab"), Key.key("waxed_oxidized_cut_copper_slab"),
                Key.key("waxed_weathered_cut_copper_slab"), Key.key("weathered_cut_copper_slab"),
                Key.key("white_concrete_slab"), Key.key("white_wool_slab"),
                Key.key("yellow_concrete_slab"), Key.key("yellow_wool_slab")));
    }

    private static void itemTags10(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("small_flowers"), List.of(Key.key("allium"), Key.key("azure_bluet"),
                Key.key("blue_orchid"), Key.key("closed_eyeblossom"), Key.key("cornflower"),
                Key.key("dandelion"), Key.key("golden_dandelion"), Key.key("lily_of_the_valley"),
                Key.key("open_eyeblossom"), Key.key("orange_tulip"), Key.key("oxeye_daisy"),
                Key.key("pink_tulip"), Key.key("poppy"), Key.key("red_tulip"),
                Key.key("torchflower"), Key.key("white_tulip"), Key.key("wither_rose")));
        tags.put(Key.key("smelts_to_glass"), List.of(Key.key("red_sand"), Key.key("sand")));
        tags.put(Key.key("sniffer_food"), List.of(Key.key("torchflower_seeds")));
        tags.put(Key.key("soul_fire_base_blocks"), List.of(Key.key("soul_sand"),
                Key.key("soul_soil")));
        tags.put(Key.key("spears"), List.of(Key.key("copper_spear"), Key.key("diamond_spear"),
                Key.key("golden_spear"), Key.key("iron_spear"), Key.key("netherite_spear"),
                Key.key("stone_spear"), Key.key("wooden_spear")));
        tags.put(Key.key("spruce_logs"), List.of(Key.key("spruce_log"), Key.key("spruce_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood")));
        tags.put(Key.key("stairs"), List.of(Key.key("acacia_stairs"), Key.key("andesite_stairs"),
                Key.key("bamboo_mosaic_stairs"), Key.key("bamboo_stairs"), Key.key("birch_stairs"),
                Key.key("black_concrete_stairs"), Key.key("black_wool_stairs"),
                Key.key("blackstone_stairs"), Key.key("blue_concrete_stairs"),
                Key.key("blue_wool_stairs"), Key.key("brick_stairs"),
                Key.key("brown_concrete_stairs"), Key.key("brown_wool_stairs"),
                Key.key("cherry_stairs"), Key.key("cinnabar_brick_stairs"),
                Key.key("cinnabar_stairs"), Key.key("cobbled_deepslate_stairs"),
                Key.key("cobblestone_stairs"), Key.key("crimson_stairs"),
                Key.key("cut_copper_stairs"), Key.key("cyan_concrete_stairs"),
                Key.key("cyan_wool_stairs"), Key.key("dark_oak_stairs"),
                Key.key("dark_prismarine_stairs"), Key.key("deepslate_brick_stairs"),
                Key.key("deepslate_tile_stairs"), Key.key("diorite_stairs"),
                Key.key("end_stone_brick_stairs"), Key.key("exposed_cut_copper_stairs"),
                Key.key("granite_stairs"), Key.key("gray_concrete_stairs"),
                Key.key("gray_wool_stairs"), Key.key("green_concrete_stairs"),
                Key.key("green_wool_stairs"), Key.key("jungle_stairs"),
                Key.key("light_blue_concrete_stairs"), Key.key("light_blue_wool_stairs"),
                Key.key("light_gray_concrete_stairs"), Key.key("light_gray_wool_stairs"),
                Key.key("lime_concrete_stairs"), Key.key("lime_wool_stairs"),
                Key.key("magenta_concrete_stairs"), Key.key("magenta_wool_stairs"),
                Key.key("mangrove_stairs"), Key.key("mossy_cobblestone_stairs"),
                Key.key("mossy_stone_brick_stairs"), Key.key("mud_brick_stairs"),
                Key.key("nether_brick_stairs"), Key.key("oak_stairs"),
                Key.key("orange_concrete_stairs"), Key.key("orange_wool_stairs"),
                Key.key("oxidized_cut_copper_stairs"), Key.key("pale_oak_stairs"),
                Key.key("pink_concrete_stairs"), Key.key("pink_wool_stairs"),
                Key.key("polished_andesite_stairs"), Key.key("polished_blackstone_brick_stairs"),
                Key.key("polished_blackstone_stairs"), Key.key("polished_cinnabar_stairs"),
                Key.key("polished_deepslate_stairs"), Key.key("polished_diorite_stairs"),
                Key.key("polished_granite_stairs"), Key.key("polished_sulfur_stairs"),
                Key.key("polished_tuff_stairs"), Key.key("poplar_stairs"),
                Key.key("prismarine_brick_stairs"), Key.key("prismarine_stairs"),
                Key.key("purple_concrete_stairs"), Key.key("purple_wool_stairs"),
                Key.key("purpur_stairs"), Key.key("quartz_stairs"), Key.key("red_concrete_stairs"),
                Key.key("red_nether_brick_stairs"), Key.key("red_sandstone_stairs"),
                Key.key("red_wool_stairs"), Key.key("resin_brick_stairs"),
                Key.key("sandstone_stairs"), Key.key("smooth_quartz_stairs"),
                Key.key("smooth_red_sandstone_stairs"), Key.key("smooth_sandstone_stairs"),
                Key.key("spruce_stairs"), Key.key("stone_brick_stairs"), Key.key("stone_stairs"),
                Key.key("sulfur_brick_stairs"), Key.key("sulfur_stairs"),
                Key.key("tuff_brick_stairs"), Key.key("tuff_stairs"), Key.key("warped_stairs"),
                Key.key("waxed_cut_copper_stairs"), Key.key("waxed_exposed_cut_copper_stairs"),
                Key.key("waxed_oxidized_cut_copper_stairs"),
                Key.key("waxed_weathered_cut_copper_stairs"),
                Key.key("weathered_cut_copper_stairs"), Key.key("white_concrete_stairs"),
                Key.key("white_wool_stairs"), Key.key("yellow_concrete_stairs"),
                Key.key("yellow_wool_stairs")));
        tags.put(Key.key("stone_bricks"), List.of(Key.key("chiseled_stone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("mossy_stone_bricks"),
                Key.key("stone_bricks")));
        tags.put(Key.key("stone_buttons"), List.of(Key.key("polished_blackstone_button"),
                Key.key("stone_button")));
        tags.put(Key.key("stone_crafting_materials"), List.of(Key.key("blackstone"),
                Key.key("cobbled_deepslate"), Key.key("cobblestone")));
        tags.put(Key.key("stone_tool_materials"), List.of(Key.key("blackstone"),
                Key.key("cobbled_deepslate"), Key.key("cobblestone")));
        tags.put(Key.key("strider_food"), List.of(Key.key("warped_fungus")));
        tags.put(Key.key("strider_tempt_items"), List.of(Key.key("warped_fungus"),
                Key.key("warped_fungus_on_a_stick")));
        tags.put(Key.key("sulfur_cube_archetype/bouncy"), List.of(Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_wood"), Key.key("bamboo_block"),
                Key.key("bamboo_mosaic"), Key.key("bamboo_planks"), Key.key("birch_log"),
                Key.key("birch_planks"), Key.key("birch_wood"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_wood"), Key.key("crimson_hyphae"),
                Key.key("crimson_planks"), Key.key("crimson_stem"), Key.key("dark_oak_log"),
                Key.key("dark_oak_planks"), Key.key("dark_oak_wood"), Key.key("jungle_log"),
                Key.key("jungle_planks"), Key.key("jungle_wood"), Key.key("mangrove_log"),
                Key.key("mangrove_planks"), Key.key("mangrove_wood"), Key.key("oak_log"),
                Key.key("oak_planks"), Key.key("oak_wood"), Key.key("pale_oak_log"),
                Key.key("pale_oak_planks"), Key.key("pale_oak_wood"), Key.key("poplar_log"),
                Key.key("poplar_planks"), Key.key("poplar_wood"), Key.key("spruce_log"),
                Key.key("spruce_planks"), Key.key("spruce_wood"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("warped_hyphae"), Key.key("warped_planks"), Key.key("warped_stem")));
    }

    private static void itemTags11(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("sulfur_cube_archetype/explosive"), List.of(Key.key("tnt")));
        tags.put(Key.key("sulfur_cube_archetype/fast_flat"), List.of(Key.key("brain_coral_block"),
                Key.key("bubble_coral_block"), Key.key("carved_pumpkin"),
                Key.key("chiseled_resin_bricks"), Key.key("dead_brain_coral_block"),
                Key.key("dead_bubble_coral_block"), Key.key("dead_fire_coral_block"),
                Key.key("dead_horn_coral_block"), Key.key("dead_tube_coral_block"),
                Key.key("dried_kelp_block"), Key.key("fire_coral_block"), Key.key("hay_block"),
                Key.key("horn_coral_block"), Key.key("jack_o_lantern"), Key.key("melon"),
                Key.key("moss_block"), Key.key("ochre_froglight"), Key.key("pale_moss_block"),
                Key.key("pearlescent_froglight"), Key.key("pumpkin"), Key.key("resin_block"),
                Key.key("resin_bricks"), Key.key("sponge"), Key.key("tube_coral_block"),
                Key.key("verdant_froglight"), Key.key("wet_sponge")));
        tags.put(Key.key("sulfur_cube_archetype/fast_sliding"), List.of(Key.key("blue_ice"),
                Key.key("packed_ice"), Key.key("snow_block")));
        tags.put(Key.key("sulfur_cube_archetype/high_resistance"), List.of(Key.key("soul_sand"),
                Key.key("soul_soil")));
        tags.put(Key.key("sulfur_cube_archetype/hot"), List.of(Key.key("magma_block")));
        tags.put(Key.key("sulfur_cube_archetype/light"), List.of(Key.key("black_wool"),
                Key.key("blue_wool"), Key.key("brown_wool"), Key.key("cyan_wool"),
                Key.key("gray_wool"), Key.key("green_wool"), Key.key("light_blue_wool"),
                Key.key("light_gray_wool"), Key.key("lime_wool"), Key.key("magenta_wool"),
                Key.key("orange_wool"), Key.key("pink_wool"), Key.key("purple_wool"),
                Key.key("red_wool"), Key.key("white_wool"), Key.key("yellow_wool")));
        tags.put(Key.key("sulfur_cube_archetype/regular"), List.of(Key.key("black_concrete_powder"),
                Key.key("blue_concrete_powder"), Key.key("bone_block"),
                Key.key("brown_concrete_powder"), Key.key("clay"), Key.key("coal_block"),
                Key.key("coarse_dirt"), Key.key("cyan_concrete_powder"), Key.key("dirt"),
                Key.key("grass_block"), Key.key("gray_concrete_powder"),
                Key.key("green_concrete_powder"), Key.key("light_blue_concrete_powder"),
                Key.key("light_gray_concrete_powder"), Key.key("lime_concrete_powder"),
                Key.key("magenta_concrete_powder"), Key.key("mud"), Key.key("muddy_mangrove_roots"),
                Key.key("orange_concrete_powder"), Key.key("packed_mud"),
                Key.key("pink_concrete_powder"), Key.key("podzol"),
                Key.key("purple_concrete_powder"), Key.key("red_concrete_powder"),
                Key.key("rooted_dirt"), Key.key("white_concrete_powder"),
                Key.key("yellow_concrete_powder")));
        tags.put(Key.key("sulfur_cube_archetype/slow_bouncy"), List.of(Key.key("amethyst_block"),
                Key.key("andesite"), Key.key("basalt"), Key.key("black_concrete"),
                Key.key("black_glazed_terracotta"), Key.key("black_terracotta"),
                Key.key("blackstone"), Key.key("blue_concrete"), Key.key("blue_glazed_terracotta"),
                Key.key("blue_terracotta"), Key.key("bricks"), Key.key("brown_concrete"),
                Key.key("brown_glazed_terracotta"), Key.key("brown_terracotta"), Key.key("calcite"),
                Key.key("chiseled_cinnabar"), Key.key("chiseled_deepslate"),
                Key.key("chiseled_nether_bricks"), Key.key("chiseled_polished_blackstone"),
                Key.key("chiseled_quartz_block"), Key.key("chiseled_red_sandstone"),
                Key.key("chiseled_sandstone"), Key.key("chiseled_stone_bricks"),
                Key.key("chiseled_sulfur"), Key.key("chiseled_tuff"),
                Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"), Key.key("cinnabar_bricks"),
                Key.key("coal_ore"), Key.key("cobbled_deepslate"), Key.key("cobblestone"),
                Key.key("cracked_deepslate_bricks"), Key.key("cracked_deepslate_tiles"),
                Key.key("cracked_nether_bricks"), Key.key("cracked_polished_blackstone_bricks"),
                Key.key("cracked_stone_bricks"), Key.key("crimson_nylium"),
                Key.key("crying_obsidian"), Key.key("cut_red_sandstone"), Key.key("cut_sandstone"),
                Key.key("cyan_concrete"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_terracotta"), Key.key("dark_prismarine"), Key.key("deepslate"),
                Key.key("deepslate_bricks"), Key.key("deepslate_coal_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("dripstone_block"), Key.key("emerald_block"),
                Key.key("emerald_ore"), Key.key("end_stone"), Key.key("end_stone_bricks"),
                Key.key("gilded_blackstone"), Key.key("glowstone"), Key.key("granite"),
                Key.key("gray_concrete"), Key.key("gray_glazed_terracotta"),
                Key.key("gray_terracotta"), Key.key("green_concrete"),
                Key.key("green_glazed_terracotta"), Key.key("green_terracotta"),
                Key.key("lapis_block"), Key.key("lapis_ore"), Key.key("light_blue_concrete"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_terracotta"),
                Key.key("light_gray_concrete"), Key.key("light_gray_glazed_terracotta"),
                Key.key("light_gray_terracotta"), Key.key("lime_concrete"),
                Key.key("lime_glazed_terracotta"), Key.key("lime_terracotta"),
                Key.key("magenta_concrete"), Key.key("magenta_glazed_terracotta"),
                Key.key("magenta_terracotta"), Key.key("mossy_cobblestone"),
                Key.key("mossy_stone_bricks"), Key.key("mud_bricks"), Key.key("nether_bricks"),
                Key.key("nether_quartz_ore"), Key.key("netherrack"), Key.key("observer"),
                Key.key("obsidian"), Key.key("orange_concrete"),
                Key.key("orange_glazed_terracotta"), Key.key("orange_terracotta"),
                Key.key("pink_concrete"), Key.key("pink_glazed_terracotta"),
                Key.key("pink_terracotta"), Key.key("polished_andesite"),
                Key.key("polished_basalt"), Key.key("polished_blackstone"),
                Key.key("polished_blackstone_bricks"), Key.key("polished_cinnabar"),
                Key.key("polished_deepslate"), Key.key("polished_diorite"),
                Key.key("polished_granite"), Key.key("polished_sulfur"), Key.key("polished_tuff"),
                Key.key("prismarine"), Key.key("prismarine_bricks"), Key.key("purple_concrete"),
                Key.key("purple_glazed_terracotta"), Key.key("purple_terracotta"),
                Key.key("purpur_block"), Key.key("purpur_pillar"), Key.key("quartz_block"),
                Key.key("quartz_bricks"), Key.key("quartz_pillar"), Key.key("red_concrete"),
                Key.key("red_glazed_terracotta"), Key.key("red_nether_bricks"),
                Key.key("red_sandstone"), Key.key("red_terracotta"), Key.key("redstone_lamp"),
                Key.key("redstone_ore"), Key.key("sandstone"), Key.key("sea_lantern"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_sandstone"), Key.key("smooth_stone"), Key.key("stone"),
                Key.key("stone_bricks"), Key.key("sulfur"), Key.key("sulfur_bricks"),
                Key.key("terracotta"), Key.key("tuff"), Key.key("tuff_bricks"),
                Key.key("warped_nylium"), Key.key("white_concrete"),
                Key.key("white_glazed_terracotta"), Key.key("white_terracotta"),
                Key.key("yellow_concrete"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_terracotta")));
    }

    private static void itemTags12(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("sulfur_cube_archetype/slow_flat"), List.of(Key.key("ancient_debris"),
                Key.key("chiseled_copper"), Key.key("copper_block"), Key.key("copper_bulb"),
                Key.key("copper_ore"), Key.key("cut_copper"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_cut_copper"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("iron_block"),
                Key.key("iron_ore"), Key.key("nether_gold_ore"), Key.key("netherite_block"),
                Key.key("oxidized_chiseled_copper"), Key.key("oxidized_copper"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_cut_copper"),
                Key.key("raw_copper_block"), Key.key("raw_gold_block"), Key.key("raw_iron_block"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_block"),
                Key.key("waxed_copper_bulb"), Key.key("waxed_cut_copper"),
                Key.key("waxed_exposed_chiseled_copper"), Key.key("waxed_exposed_copper"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_oxidized_chiseled_copper"), Key.key("waxed_oxidized_copper"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_weathered_chiseled_copper"), Key.key("waxed_weathered_copper"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_cut_copper"),
                Key.key("weathered_chiseled_copper"), Key.key("weathered_copper"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_cut_copper")));
        tags.put(Key.key("sulfur_cube_archetype/slow_sliding"), List.of(Key.key("brown_mushroom_block"),
                Key.key("mushroom_stem"), Key.key("mycelium"), Key.key("nether_wart_block"),
                Key.key("red_mushroom_block"), Key.key("shroomlight"),
                Key.key("warped_wart_block")));
        tags.put(Key.key("sulfur_cube_archetype/sticky"), List.of(Key.key("honeycomb_block")));
        tags.put(Key.key("sulfur_cube_food"), List.of(Key.key("slime_ball")));
        tags.put(Key.key("sulfur_cube_swallowable"), List.of(Key.key("acacia_log"),
                Key.key("acacia_planks"), Key.key("acacia_wood"), Key.key("amethyst_block"),
                Key.key("ancient_debris"), Key.key("andesite"), Key.key("bamboo_block"),
                Key.key("bamboo_mosaic"), Key.key("bamboo_planks"), Key.key("basalt"),
                Key.key("birch_log"), Key.key("birch_planks"), Key.key("birch_wood"),
                Key.key("black_concrete"), Key.key("black_concrete_powder"),
                Key.key("black_glazed_terracotta"), Key.key("black_terracotta"),
                Key.key("black_wool"), Key.key("blackstone"), Key.key("blue_concrete"),
                Key.key("blue_concrete_powder"), Key.key("blue_glazed_terracotta"),
                Key.key("blue_ice"), Key.key("blue_terracotta"), Key.key("blue_wool"),
                Key.key("bone_block"), Key.key("brain_coral_block"), Key.key("bricks"),
                Key.key("brown_concrete"), Key.key("brown_concrete_powder"),
                Key.key("brown_glazed_terracotta"), Key.key("brown_mushroom_block"),
                Key.key("brown_terracotta"), Key.key("brown_wool"), Key.key("bubble_coral_block"),
                Key.key("calcite"), Key.key("carved_pumpkin"), Key.key("cherry_log"),
                Key.key("cherry_planks"), Key.key("cherry_wood"), Key.key("chiseled_cinnabar"),
                Key.key("chiseled_copper"), Key.key("chiseled_deepslate"),
                Key.key("chiseled_nether_bricks"), Key.key("chiseled_polished_blackstone"),
                Key.key("chiseled_quartz_block"), Key.key("chiseled_red_sandstone"),
                Key.key("chiseled_resin_bricks"), Key.key("chiseled_sandstone"),
                Key.key("chiseled_stone_bricks"), Key.key("chiseled_sulfur"),
                Key.key("chiseled_tuff"), Key.key("chiseled_tuff_bricks"), Key.key("cinnabar"),
                Key.key("cinnabar_bricks"), Key.key("clay"), Key.key("coal_block"),
                Key.key("coal_ore"), Key.key("coarse_dirt"), Key.key("cobbled_deepslate"),
                Key.key("cobblestone"), Key.key("copper_block"), Key.key("copper_bulb"),
                Key.key("copper_ore"), Key.key("cracked_deepslate_bricks"),
                Key.key("cracked_deepslate_tiles"), Key.key("cracked_nether_bricks"),
                Key.key("cracked_polished_blackstone_bricks"), Key.key("cracked_stone_bricks"),
                Key.key("crimson_hyphae"), Key.key("crimson_nylium"), Key.key("crimson_planks"),
                Key.key("crimson_stem"), Key.key("crying_obsidian"), Key.key("cut_copper"),
                Key.key("cut_red_sandstone"), Key.key("cut_sandstone"), Key.key("cyan_concrete"),
                Key.key("cyan_concrete_powder"), Key.key("cyan_glazed_terracotta"),
                Key.key("cyan_terracotta"), Key.key("cyan_wool"), Key.key("dark_oak_log"),
                Key.key("dark_oak_planks"), Key.key("dark_oak_wood"), Key.key("dark_prismarine"),
                Key.key("dead_brain_coral_block"), Key.key("dead_bubble_coral_block"),
                Key.key("dead_fire_coral_block"), Key.key("dead_horn_coral_block"),
                Key.key("dead_tube_coral_block"), Key.key("deepslate"), Key.key("deepslate_bricks"),
                Key.key("deepslate_coal_ore"), Key.key("deepslate_copper_ore"),
                Key.key("deepslate_diamond_ore"), Key.key("deepslate_emerald_ore"),
                Key.key("deepslate_gold_ore"), Key.key("deepslate_iron_ore"),
                Key.key("deepslate_lapis_ore"), Key.key("deepslate_redstone_ore"),
                Key.key("deepslate_tiles"), Key.key("diamond_block"), Key.key("diamond_ore"),
                Key.key("diorite"), Key.key("dirt"), Key.key("dried_kelp_block"),
                Key.key("dripstone_block"), Key.key("emerald_block"), Key.key("emerald_ore"),
                Key.key("end_stone"), Key.key("end_stone_bricks"),
                Key.key("exposed_chiseled_copper"), Key.key("exposed_copper"),
                Key.key("exposed_copper_bulb"), Key.key("exposed_cut_copper"),
                Key.key("fire_coral_block"), Key.key("gilded_blackstone"), Key.key("glowstone"),
                Key.key("gold_block"), Key.key("gold_ore"), Key.key("granite"),
                Key.key("grass_block"), Key.key("gray_concrete"), Key.key("gray_concrete_powder"),
                Key.key("gray_glazed_terracotta"), Key.key("gray_terracotta"), Key.key("gray_wool"),
                Key.key("green_concrete"), Key.key("green_concrete_powder"),
                Key.key("green_glazed_terracotta"), Key.key("green_terracotta"),
                Key.key("green_wool"), Key.key("hay_block"), Key.key("honeycomb_block"),
                Key.key("horn_coral_block"), Key.key("iron_block"), Key.key("iron_ore"),
                Key.key("jack_o_lantern"), Key.key("jungle_log"), Key.key("jungle_planks"),
                Key.key("jungle_wood"), Key.key("lapis_block"), Key.key("lapis_ore"),
                Key.key("light_blue_concrete"), Key.key("light_blue_concrete_powder"),
                Key.key("light_blue_glazed_terracotta"), Key.key("light_blue_terracotta"),
                Key.key("light_blue_wool"), Key.key("light_gray_concrete"),
                Key.key("light_gray_concrete_powder"), Key.key("light_gray_glazed_terracotta"),
                Key.key("light_gray_terracotta"), Key.key("light_gray_wool"),
                Key.key("lime_concrete"), Key.key("lime_concrete_powder"),
                Key.key("lime_glazed_terracotta"), Key.key("lime_terracotta"), Key.key("lime_wool"),
                Key.key("magenta_concrete"), Key.key("magenta_concrete_powder"),
                Key.key("magenta_glazed_terracotta"), Key.key("magenta_terracotta"),
                Key.key("magenta_wool"), Key.key("magma_block"), Key.key("mangrove_log"),
                Key.key("mangrove_planks"), Key.key("mangrove_wood"), Key.key("melon"),
                Key.key("moss_block"), Key.key("mossy_cobblestone"), Key.key("mossy_stone_bricks"),
                Key.key("mud"), Key.key("mud_bricks"), Key.key("muddy_mangrove_roots"),
                Key.key("mushroom_stem"), Key.key("mycelium"), Key.key("nether_bricks"),
                Key.key("nether_gold_ore"), Key.key("nether_quartz_ore"),
                Key.key("nether_wart_block"), Key.key("netherite_block"), Key.key("netherrack"),
                Key.key("oak_log"), Key.key("oak_planks"), Key.key("oak_wood"), Key.key("observer"),
                Key.key("obsidian"), Key.key("ochre_froglight"), Key.key("orange_concrete"),
                Key.key("orange_concrete_powder"), Key.key("orange_glazed_terracotta"),
                Key.key("orange_terracotta"), Key.key("orange_wool"),
                Key.key("oxidized_chiseled_copper"), Key.key("oxidized_copper"),
                Key.key("oxidized_copper_bulb"), Key.key("oxidized_cut_copper"),
                Key.key("packed_ice"), Key.key("packed_mud"), Key.key("pale_moss_block"),
                Key.key("pale_oak_log"), Key.key("pale_oak_planks"), Key.key("pale_oak_wood"),
                Key.key("pearlescent_froglight"), Key.key("pink_concrete"),
                Key.key("pink_concrete_powder"), Key.key("pink_glazed_terracotta"),
                Key.key("pink_terracotta"), Key.key("pink_wool"), Key.key("podzol"),
                Key.key("polished_andesite"), Key.key("polished_basalt"),
                Key.key("polished_blackstone"), Key.key("polished_blackstone_bricks"),
                Key.key("polished_cinnabar"), Key.key("polished_deepslate"),
                Key.key("polished_diorite"), Key.key("polished_granite"),
                Key.key("polished_sulfur"), Key.key("polished_tuff"), Key.key("poplar_log"),
                Key.key("poplar_planks"), Key.key("poplar_wood"), Key.key("prismarine"),
                Key.key("prismarine_bricks"), Key.key("pumpkin"), Key.key("purple_concrete"),
                Key.key("purple_concrete_powder"), Key.key("purple_glazed_terracotta"),
                Key.key("purple_terracotta"), Key.key("purple_wool"), Key.key("purpur_block"),
                Key.key("purpur_pillar"), Key.key("quartz_block"), Key.key("quartz_bricks"),
                Key.key("quartz_pillar"), Key.key("raw_copper_block"), Key.key("raw_gold_block"),
                Key.key("raw_iron_block"), Key.key("red_concrete"), Key.key("red_concrete_powder"),
                Key.key("red_glazed_terracotta"), Key.key("red_mushroom_block"),
                Key.key("red_nether_bricks"), Key.key("red_sandstone"), Key.key("red_terracotta"),
                Key.key("red_wool"), Key.key("redstone_lamp"), Key.key("redstone_ore"),
                Key.key("resin_block"), Key.key("resin_bricks"), Key.key("rooted_dirt"),
                Key.key("sandstone"), Key.key("sea_lantern"), Key.key("shroomlight"),
                Key.key("smooth_basalt"), Key.key("smooth_quartz"), Key.key("smooth_red_sandstone"),
                Key.key("smooth_sandstone"), Key.key("smooth_stone"), Key.key("snow_block"),
                Key.key("soul_sand"), Key.key("soul_soil"), Key.key("sponge"),
                Key.key("spruce_log"), Key.key("spruce_planks"), Key.key("spruce_wood"),
                Key.key("stone"), Key.key("stone_bricks"), Key.key("stripped_acacia_log"),
                Key.key("stripped_acacia_wood"), Key.key("stripped_bamboo_block"),
                Key.key("stripped_birch_log"), Key.key("stripped_birch_wood"),
                Key.key("stripped_cherry_log"), Key.key("stripped_cherry_wood"),
                Key.key("stripped_crimson_hyphae"), Key.key("stripped_crimson_stem"),
                Key.key("stripped_dark_oak_log"), Key.key("stripped_dark_oak_wood"),
                Key.key("stripped_jungle_log"), Key.key("stripped_jungle_wood"),
                Key.key("stripped_mangrove_log"), Key.key("stripped_mangrove_wood"),
                Key.key("stripped_oak_log"), Key.key("stripped_oak_wood"),
                Key.key("stripped_pale_oak_log"), Key.key("stripped_pale_oak_wood"),
                Key.key("stripped_poplar_log"), Key.key("stripped_poplar_wood"),
                Key.key("stripped_spruce_log"), Key.key("stripped_spruce_wood"),
                Key.key("stripped_warped_hyphae"), Key.key("stripped_warped_stem"),
                Key.key("sulfur"), Key.key("sulfur_bricks"), Key.key("terracotta"), Key.key("tnt"),
                Key.key("tube_coral_block"), Key.key("tuff"), Key.key("tuff_bricks"),
                Key.key("verdant_froglight"), Key.key("warped_hyphae"), Key.key("warped_nylium"),
                Key.key("warped_planks"), Key.key("warped_stem"), Key.key("warped_wart_block"),
                Key.key("waxed_chiseled_copper"), Key.key("waxed_copper_block"),
                Key.key("waxed_copper_bulb"), Key.key("waxed_cut_copper"),
                Key.key("waxed_exposed_chiseled_copper"), Key.key("waxed_exposed_copper"),
                Key.key("waxed_exposed_copper_bulb"), Key.key("waxed_exposed_cut_copper"),
                Key.key("waxed_oxidized_chiseled_copper"), Key.key("waxed_oxidized_copper"),
                Key.key("waxed_oxidized_copper_bulb"), Key.key("waxed_oxidized_cut_copper"),
                Key.key("waxed_weathered_chiseled_copper"), Key.key("waxed_weathered_copper"),
                Key.key("waxed_weathered_copper_bulb"), Key.key("waxed_weathered_cut_copper"),
                Key.key("weathered_chiseled_copper"), Key.key("weathered_copper"),
                Key.key("weathered_copper_bulb"), Key.key("weathered_cut_copper"),
                Key.key("wet_sponge"), Key.key("white_concrete"), Key.key("white_concrete_powder"),
                Key.key("white_glazed_terracotta"), Key.key("white_terracotta"),
                Key.key("white_wool"), Key.key("yellow_concrete"),
                Key.key("yellow_concrete_powder"), Key.key("yellow_glazed_terracotta"),
                Key.key("yellow_terracotta"), Key.key("yellow_wool")));
    }

    private static void itemTags13(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("swords"), List.of(Key.key("copper_sword"), Key.key("diamond_sword"),
                Key.key("golden_sword"), Key.key("iron_sword"), Key.key("netherite_sword"),
                Key.key("stone_sword"), Key.key("wooden_sword")));
        tags.put(Key.key("terracotta"), List.of(Key.key("black_terracotta"),
                Key.key("blue_terracotta"), Key.key("brown_terracotta"), Key.key("cyan_terracotta"),
                Key.key("gray_terracotta"), Key.key("green_terracotta"),
                Key.key("light_blue_terracotta"), Key.key("light_gray_terracotta"),
                Key.key("lime_terracotta"), Key.key("magenta_terracotta"),
                Key.key("orange_terracotta"), Key.key("pink_terracotta"),
                Key.key("purple_terracotta"), Key.key("red_terracotta"), Key.key("terracotta"),
                Key.key("white_terracotta"), Key.key("yellow_terracotta")));
        tags.put(Key.key("trapdoors"), List.of(Key.key("acacia_trapdoor"),
                Key.key("bamboo_trapdoor"), Key.key("birch_trapdoor"), Key.key("cherry_trapdoor"),
                Key.key("copper_trapdoor"), Key.key("crimson_trapdoor"),
                Key.key("dark_oak_trapdoor"), Key.key("exposed_copper_trapdoor"),
                Key.key("iron_trapdoor"), Key.key("jungle_trapdoor"), Key.key("mangrove_trapdoor"),
                Key.key("oak_trapdoor"), Key.key("oxidized_copper_trapdoor"),
                Key.key("pale_oak_trapdoor"), Key.key("poplar_trapdoor"),
                Key.key("spruce_trapdoor"), Key.key("warped_trapdoor"),
                Key.key("waxed_copper_trapdoor"), Key.key("waxed_exposed_copper_trapdoor"),
                Key.key("waxed_oxidized_copper_trapdoor"),
                Key.key("waxed_weathered_copper_trapdoor"), Key.key("weathered_copper_trapdoor")));
        tags.put(Key.key("trim_materials"), List.of(Key.key("amethyst_shard"),
                Key.key("copper_ingot"), Key.key("diamond"), Key.key("emerald"),
                Key.key("gold_ingot"), Key.key("iron_ingot"), Key.key("lapis_lazuli"),
                Key.key("netherite_ingot"), Key.key("quartz"), Key.key("redstone"),
                Key.key("resin_brick")));
        tags.put(Key.key("trimmable_armor"), List.of(Key.key("chainmail_boots"),
                Key.key("chainmail_chestplate"), Key.key("chainmail_helmet"),
                Key.key("chainmail_leggings"), Key.key("copper_boots"),
                Key.key("copper_chestplate"), Key.key("copper_helmet"), Key.key("copper_leggings"),
                Key.key("diamond_boots"), Key.key("diamond_chestplate"), Key.key("diamond_helmet"),
                Key.key("diamond_leggings"), Key.key("golden_boots"), Key.key("golden_chestplate"),
                Key.key("golden_helmet"), Key.key("golden_leggings"), Key.key("iron_boots"),
                Key.key("iron_chestplate"), Key.key("iron_helmet"), Key.key("iron_leggings"),
                Key.key("leather_boots"), Key.key("leather_chestplate"), Key.key("leather_helmet"),
                Key.key("leather_leggings"), Key.key("netherite_boots"),
                Key.key("netherite_chestplate"), Key.key("netherite_helmet"),
                Key.key("netherite_leggings"), Key.key("turtle_helmet")));
        tags.put(Key.key("turtle_food"), List.of(Key.key("seagrass")));
        tags.put(Key.key("villager_picks_up"), List.of(Key.key("beetroot"),
                Key.key("beetroot_seeds"), Key.key("bread"), Key.key("carrot"),
                Key.key("pitcher_pod"), Key.key("potato"), Key.key("torchflower_seeds"),
                Key.key("wheat"), Key.key("wheat_seeds")));
        tags.put(Key.key("villager_plantable_seeds"), List.of(Key.key("beetroot_seeds"),
                Key.key("carrot"), Key.key("pitcher_pod"), Key.key("potato"),
                Key.key("torchflower_seeds"), Key.key("wheat_seeds")));
        tags.put(Key.key("walls"), List.of(Key.key("andesite_wall"), Key.key("blackstone_wall"),
                Key.key("brick_wall"), Key.key("cinnabar_brick_wall"), Key.key("cinnabar_wall"),
                Key.key("cobbled_deepslate_wall"), Key.key("cobblestone_wall"),
                Key.key("deepslate_brick_wall"), Key.key("deepslate_tile_wall"),
                Key.key("diorite_wall"), Key.key("end_stone_brick_wall"), Key.key("granite_wall"),
                Key.key("mossy_cobblestone_wall"), Key.key("mossy_stone_brick_wall"),
                Key.key("mud_brick_wall"), Key.key("nether_brick_wall"),
                Key.key("polished_blackstone_brick_wall"), Key.key("polished_blackstone_wall"),
                Key.key("polished_cinnabar_wall"), Key.key("polished_deepslate_wall"),
                Key.key("polished_sulfur_wall"), Key.key("polished_tuff_wall"),
                Key.key("prismarine_wall"), Key.key("red_nether_brick_wall"),
                Key.key("red_sandstone_wall"), Key.key("resin_brick_wall"),
                Key.key("sandstone_wall"), Key.key("stone_brick_wall"),
                Key.key("sulfur_brick_wall"), Key.key("sulfur_wall"), Key.key("tuff_brick_wall"),
                Key.key("tuff_wall")));
        tags.put(Key.key("warped_stems"), List.of(Key.key("stripped_warped_hyphae"),
                Key.key("stripped_warped_stem"), Key.key("warped_hyphae"), Key.key("warped_stem")));
        tags.put(Key.key("wart_blocks"), List.of(Key.key("nether_wart_block"),
                Key.key("warped_wart_block")));
        tags.put(Key.key("wither_skeleton_disliked_weapons"), List.of(Key.key("bow"),
                Key.key("crossbow")));
        tags.put(Key.key("wolf_collar_dyes"), List.of(Key.key("black_dye"), Key.key("blue_dye"),
                Key.key("brown_dye"), Key.key("cyan_dye"), Key.key("gray_dye"),
                Key.key("green_dye"), Key.key("light_blue_dye"), Key.key("light_gray_dye"),
                Key.key("lime_dye"), Key.key("magenta_dye"), Key.key("orange_dye"),
                Key.key("pink_dye"), Key.key("purple_dye"), Key.key("red_dye"),
                Key.key("white_dye"), Key.key("yellow_dye")));
    }

    private static void itemTags14(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("wolf_food"), List.of(Key.key("beef"), Key.key("chicken"), Key.key("cod"),
                Key.key("cooked_beef"), Key.key("cooked_chicken"), Key.key("cooked_cod"),
                Key.key("cooked_mutton"), Key.key("cooked_porkchop"), Key.key("cooked_rabbit"),
                Key.key("cooked_salmon"), Key.key("mutton"), Key.key("porkchop"),
                Key.key("pufferfish"), Key.key("rabbit"), Key.key("rabbit_stew"),
                Key.key("rotten_flesh"), Key.key("salmon"), Key.key("tropical_fish")));
        tags.put(Key.key("wooden_buttons"), List.of(Key.key("acacia_button"),
                Key.key("bamboo_button"), Key.key("birch_button"), Key.key("cherry_button"),
                Key.key("crimson_button"), Key.key("dark_oak_button"), Key.key("jungle_button"),
                Key.key("mangrove_button"), Key.key("oak_button"), Key.key("pale_oak_button"),
                Key.key("poplar_button"), Key.key("spruce_button"), Key.key("warped_button")));
        tags.put(Key.key("wooden_doors"), List.of(Key.key("acacia_door"), Key.key("bamboo_door"),
                Key.key("birch_door"), Key.key("cherry_door"), Key.key("crimson_door"),
                Key.key("dark_oak_door"), Key.key("jungle_door"), Key.key("mangrove_door"),
                Key.key("oak_door"), Key.key("pale_oak_door"), Key.key("poplar_door"),
                Key.key("spruce_door"), Key.key("warped_door")));
        tags.put(Key.key("wooden_fences"), List.of(Key.key("acacia_fence"), Key.key("bamboo_fence"),
                Key.key("birch_fence"), Key.key("cherry_fence"), Key.key("crimson_fence"),
                Key.key("dark_oak_fence"), Key.key("jungle_fence"), Key.key("mangrove_fence"),
                Key.key("oak_fence"), Key.key("pale_oak_fence"), Key.key("poplar_fence"),
                Key.key("spruce_fence"), Key.key("warped_fence")));
        tags.put(Key.key("wooden_pressure_plates"), List.of(Key.key("acacia_pressure_plate"),
                Key.key("bamboo_pressure_plate"), Key.key("birch_pressure_plate"),
                Key.key("cherry_pressure_plate"), Key.key("crimson_pressure_plate"),
                Key.key("dark_oak_pressure_plate"), Key.key("jungle_pressure_plate"),
                Key.key("mangrove_pressure_plate"), Key.key("oak_pressure_plate"),
                Key.key("pale_oak_pressure_plate"), Key.key("poplar_pressure_plate"),
                Key.key("spruce_pressure_plate"), Key.key("warped_pressure_plate")));
        tags.put(Key.key("wooden_shelves"), List.of(Key.key("acacia_shelf"),
                Key.key("bamboo_shelf"), Key.key("birch_shelf"), Key.key("cherry_shelf"),
                Key.key("crimson_shelf"), Key.key("dark_oak_shelf"), Key.key("jungle_shelf"),
                Key.key("mangrove_shelf"), Key.key("oak_shelf"), Key.key("pale_oak_shelf"),
                Key.key("poplar_shelf"), Key.key("spruce_shelf"), Key.key("warped_shelf")));
        tags.put(Key.key("wooden_slabs"), List.of(Key.key("acacia_slab"), Key.key("bamboo_slab"),
                Key.key("birch_slab"), Key.key("cherry_slab"), Key.key("crimson_slab"),
                Key.key("dark_oak_slab"), Key.key("jungle_slab"), Key.key("mangrove_slab"),
                Key.key("oak_slab"), Key.key("pale_oak_slab"), Key.key("poplar_slab"),
                Key.key("spruce_slab"), Key.key("warped_slab")));
        tags.put(Key.key("wooden_stairs"), List.of(Key.key("acacia_stairs"),
                Key.key("bamboo_stairs"), Key.key("birch_stairs"), Key.key("cherry_stairs"),
                Key.key("crimson_stairs"), Key.key("dark_oak_stairs"), Key.key("jungle_stairs"),
                Key.key("mangrove_stairs"), Key.key("oak_stairs"), Key.key("pale_oak_stairs"),
                Key.key("poplar_stairs"), Key.key("spruce_stairs"), Key.key("warped_stairs")));
        tags.put(Key.key("wooden_tool_materials"), List.of(Key.key("acacia_planks"),
                Key.key("bamboo_planks"), Key.key("birch_planks"), Key.key("cherry_planks"),
                Key.key("crimson_planks"), Key.key("dark_oak_planks"), Key.key("jungle_planks"),
                Key.key("mangrove_planks"), Key.key("oak_planks"), Key.key("pale_oak_planks"),
                Key.key("poplar_planks"), Key.key("spruce_planks"), Key.key("warped_planks")));
        tags.put(Key.key("wooden_trapdoors"), List.of(Key.key("acacia_trapdoor"),
                Key.key("bamboo_trapdoor"), Key.key("birch_trapdoor"), Key.key("cherry_trapdoor"),
                Key.key("crimson_trapdoor"), Key.key("dark_oak_trapdoor"),
                Key.key("jungle_trapdoor"), Key.key("mangrove_trapdoor"), Key.key("oak_trapdoor"),
                Key.key("pale_oak_trapdoor"), Key.key("poplar_trapdoor"),
                Key.key("spruce_trapdoor"), Key.key("warped_trapdoor")));
        tags.put(Key.key("wool"), List.of(Key.key("black_wool"), Key.key("blue_wool"),
                Key.key("brown_wool"), Key.key("cyan_wool"), Key.key("gray_wool"),
                Key.key("green_wool"), Key.key("light_blue_wool"), Key.key("light_gray_wool"),
                Key.key("lime_wool"), Key.key("magenta_wool"), Key.key("orange_wool"),
                Key.key("pink_wool"), Key.key("purple_wool"), Key.key("red_wool"),
                Key.key("white_wool"), Key.key("yellow_wool")));
    }

    private static void itemTags15(final Map<Key, List<Key>> tags) {
        tags.put(Key.key("wool_carpets"), List.of(Key.key("black_carpet"), Key.key("blue_carpet"),
                Key.key("brown_carpet"), Key.key("cyan_carpet"), Key.key("gray_carpet"),
                Key.key("green_carpet"), Key.key("light_blue_carpet"), Key.key("light_gray_carpet"),
                Key.key("lime_carpet"), Key.key("magenta_carpet"), Key.key("orange_carpet"),
                Key.key("pink_carpet"), Key.key("purple_carpet"), Key.key("red_carpet"),
                Key.key("white_carpet"), Key.key("yellow_carpet")));
        tags.put(Key.key("wool_slabs"), List.of(Key.key("black_wool_slab"),
                Key.key("blue_wool_slab"), Key.key("brown_wool_slab"), Key.key("cyan_wool_slab"),
                Key.key("gray_wool_slab"), Key.key("green_wool_slab"),
                Key.key("light_blue_wool_slab"), Key.key("light_gray_wool_slab"),
                Key.key("lime_wool_slab"), Key.key("magenta_wool_slab"),
                Key.key("orange_wool_slab"), Key.key("pink_wool_slab"), Key.key("purple_wool_slab"),
                Key.key("red_wool_slab"), Key.key("white_wool_slab"), Key.key("yellow_wool_slab")));
        tags.put(Key.key("wool_stairs"), List.of(Key.key("black_wool_stairs"),
                Key.key("blue_wool_stairs"), Key.key("brown_wool_stairs"),
                Key.key("cyan_wool_stairs"), Key.key("gray_wool_stairs"),
                Key.key("green_wool_stairs"), Key.key("light_blue_wool_stairs"),
                Key.key("light_gray_wool_stairs"), Key.key("lime_wool_stairs"),
                Key.key("magenta_wool_stairs"), Key.key("orange_wool_stairs"),
                Key.key("pink_wool_stairs"), Key.key("purple_wool_stairs"),
                Key.key("red_wool_stairs"), Key.key("white_wool_stairs"),
                Key.key("yellow_wool_stairs")));
        tags.put(Key.key("zombie_horse_food"), List.of(Key.key("red_mushroom")));
    }

    /**
     * @return the tags of {@code minecraft:item}, keyed by tag identifier
     */
    private static Map<Key, List<Key>> itemTags() {
        final Map<Key, List<Key>> tags = new LinkedHashMap<>(236);
        itemTags0(tags);
        itemTags1(tags);
        itemTags2(tags);
        itemTags3(tags);
        itemTags4(tags);
        itemTags5(tags);
        itemTags6(tags);
        itemTags7(tags);
        itemTags8(tags);
        itemTags9(tags);
        itemTags10(tags);
        itemTags11(tags);
        itemTags12(tags);
        itemTags13(tags);
        itemTags14(tags);
        itemTags15(tags);
        return Map.copyOf(tags);
    }

    private static void menu0(final List<Key> entries) {
        entries.add(Key.key("generic_9x1"));
        entries.add(Key.key("generic_9x2"));
        entries.add(Key.key("generic_9x3"));
        entries.add(Key.key("generic_9x4"));
        entries.add(Key.key("generic_9x5"));
        entries.add(Key.key("generic_9x6"));
        entries.add(Key.key("generic_3x3"));
        entries.add(Key.key("crafter_3x3"));
        entries.add(Key.key("anvil"));
        entries.add(Key.key("beacon"));
        entries.add(Key.key("blast_furnace"));
        entries.add(Key.key("brewing_stand"));
        entries.add(Key.key("crafting"));
        entries.add(Key.key("enchantment"));
        entries.add(Key.key("furnace"));
        entries.add(Key.key("grindstone"));
        entries.add(Key.key("hopper"));
        entries.add(Key.key("lectern"));
        entries.add(Key.key("loom"));
        entries.add(Key.key("merchant"));
        entries.add(Key.key("shulker_box"));
        entries.add(Key.key("smithing"));
        entries.add(Key.key("smoker"));
        entries.add(Key.key("cartography_table"));
        entries.add(Key.key("stonecutter"));
    }

    /**
     * @return {@code minecraft:menu}, indexed by network ID
     */
    private static List<Key> menu() {
        final List<Key> entries = new ArrayList<>(25);
        menu0(entries);
        return List.copyOf(entries);
    }

    /**
     * @return the tags of {@code minecraft:menu}, keyed by tag identifier
     */
    private static Map<Key, List<Key>> menuTags() {
        return Map.of();
    }
}
