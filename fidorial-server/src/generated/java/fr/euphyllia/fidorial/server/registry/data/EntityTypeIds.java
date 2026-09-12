package fr.euphyllia.fidorial.server.registry.data;

import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for entries in the {@code minecraft:entity_type} registry.
 *
 * <p>Generated from Mojang's registry report; do not edit.</p>
 */
public interface EntityTypeIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:acacia_boat}
     */
    int ACACIA_BOAT_ENTITY_ID = 0;

    /**
     * {@code minecraft:acacia_chest_boat}
     */
    int ACACIA_CHEST_BOAT_ENTITY_ID = 1;

    /**
     * {@code minecraft:allay}
     */
    int ALLAY_ENTITY_ID = 2;

    /**
     * {@code minecraft:area_effect_cloud}
     */
    int AREA_EFFECT_CLOUD_ENTITY_ID = 3;

    /**
     * {@code minecraft:armadillo}
     */
    int ARMADILLO_ENTITY_ID = 4;

    /**
     * {@code minecraft:armor_stand}
     */
    int ARMOR_STAND_ENTITY_ID = 5;

    /**
     * {@code minecraft:arrow}
     */
    int ARROW_ENTITY_ID = 6;

    /**
     * {@code minecraft:axolotl}
     */
    int AXOLOTL_ENTITY_ID = 7;

    /**
     * {@code minecraft:bamboo_chest_raft}
     */
    int BAMBOO_CHEST_RAFT_ENTITY_ID = 8;

    /**
     * {@code minecraft:bamboo_raft}
     */
    int BAMBOO_RAFT_ENTITY_ID = 9;

    /**
     * {@code minecraft:bat}
     */
    int BAT_ENTITY_ID = 10;

    /**
     * {@code minecraft:bee}
     */
    int BEE_ENTITY_ID = 11;

    /**
     * {@code minecraft:birch_boat}
     */
    int BIRCH_BOAT_ENTITY_ID = 12;

    /**
     * {@code minecraft:birch_chest_boat}
     */
    int BIRCH_CHEST_BOAT_ENTITY_ID = 13;

    /**
     * {@code minecraft:blaze}
     */
    int BLAZE_ENTITY_ID = 14;

    /**
     * {@code minecraft:block_display}
     */
    int BLOCK_DISPLAY_ENTITY_ID = 15;

    /**
     * {@code minecraft:bogged}
     */
    int BOGGED_ENTITY_ID = 16;

    /**
     * {@code minecraft:breeze}
     */
    int BREEZE_ENTITY_ID = 17;

    /**
     * {@code minecraft:breeze_wind_charge}
     */
    int BREEZE_WIND_CHARGE_ENTITY_ID = 18;

    /**
     * {@code minecraft:camel}
     */
    int CAMEL_ENTITY_ID = 19;

    /**
     * {@code minecraft:camel_husk}
     */
    int CAMEL_HUSK_ENTITY_ID = 20;

    /**
     * {@code minecraft:cat}
     */
    int CAT_ENTITY_ID = 21;

    /**
     * {@code minecraft:cave_spider}
     */
    int CAVE_SPIDER_ENTITY_ID = 22;

    /**
     * {@code minecraft:cherry_boat}
     */
    int CHERRY_BOAT_ENTITY_ID = 23;

    /**
     * {@code minecraft:cherry_chest_boat}
     */
    int CHERRY_CHEST_BOAT_ENTITY_ID = 24;

    /**
     * {@code minecraft:chest_minecart}
     */
    int CHEST_MINECART_ENTITY_ID = 25;

    /**
     * {@code minecraft:chicken}
     */
    int CHICKEN_ENTITY_ID = 26;

    /**
     * {@code minecraft:cod}
     */
    int COD_ENTITY_ID = 27;

    /**
     * {@code minecraft:command_block_minecart}
     */
    int COMMAND_BLOCK_MINECART_ENTITY_ID = 29;

    /**
     * {@code minecraft:copper_golem}
     */
    int COPPER_GOLEM_ENTITY_ID = 28;

    /**
     * {@code minecraft:cow}
     */
    int COW_ENTITY_ID = 30;

    /**
     * {@code minecraft:creaking}
     */
    int CREAKING_ENTITY_ID = 31;

    /**
     * {@code minecraft:creeper}
     */
    int CREEPER_ENTITY_ID = 32;

    /**
     * {@code minecraft:cushion}
     */
    int CUSHION_ENTITY_ID = 33;

    /**
     * {@code minecraft:dark_oak_boat}
     */
    int DARK_OAK_BOAT_ENTITY_ID = 34;

    /**
     * {@code minecraft:dark_oak_chest_boat}
     */
    int DARK_OAK_CHEST_BOAT_ENTITY_ID = 35;

    /**
     * {@code minecraft:dolphin}
     */
    int DOLPHIN_ENTITY_ID = 36;

    /**
     * {@code minecraft:donkey}
     */
    int DONKEY_ENTITY_ID = 37;

    /**
     * {@code minecraft:dragon_fireball}
     */
    int DRAGON_FIREBALL_ENTITY_ID = 38;

    /**
     * {@code minecraft:drowned}
     */
    int DROWNED_ENTITY_ID = 39;

    /**
     * {@code minecraft:egg}
     */
    int EGG_ENTITY_ID = 40;

    /**
     * {@code minecraft:elder_guardian}
     */
    int ELDER_GUARDIAN_ENTITY_ID = 41;

    /**
     * {@code minecraft:end_crystal}
     */
    int END_CRYSTAL_ENTITY_ID = 46;

    /**
     * {@code minecraft:ender_dragon}
     */
    int ENDER_DRAGON_ENTITY_ID = 44;

    /**
     * {@code minecraft:ender_pearl}
     */
    int ENDER_PEARL_ENTITY_ID = 45;

    /**
     * {@code minecraft:enderman}
     */
    int ENDERMAN_ENTITY_ID = 42;

    /**
     * {@code minecraft:endermite}
     */
    int ENDERMITE_ENTITY_ID = 43;

    /**
     * {@code minecraft:evoker}
     */
    int EVOKER_ENTITY_ID = 47;

    /**
     * {@code minecraft:evoker_fangs}
     */
    int EVOKER_FANGS_ENTITY_ID = 48;

    /**
     * {@code minecraft:experience_bottle}
     */
    int EXPERIENCE_BOTTLE_ENTITY_ID = 49;

    /**
     * {@code minecraft:experience_orb}
     */
    int EXPERIENCE_ORB_ENTITY_ID = 50;

    /**
     * {@code minecraft:eye_of_ender}
     */
    int EYE_OF_ENDER_ENTITY_ID = 51;

    /**
     * {@code minecraft:falling_block}
     */
    int FALLING_BLOCK_ENTITY_ID = 52;

    /**
     * {@code minecraft:fireball}
     */
    int FIREBALL_ENTITY_ID = 53;

    /**
     * {@code minecraft:firework_rocket}
     */
    int FIREWORK_ROCKET_ENTITY_ID = 54;

    /**
     * {@code minecraft:fishing_bobber}
     */
    int FISHING_BOBBER_ENTITY_ID = 160;

    /**
     * {@code minecraft:fox}
     */
    int FOX_ENTITY_ID = 55;

    /**
     * {@code minecraft:frog}
     */
    int FROG_ENTITY_ID = 56;

    /**
     * {@code minecraft:furnace_minecart}
     */
    int FURNACE_MINECART_ENTITY_ID = 57;

    /**
     * {@code minecraft:ghast}
     */
    int GHAST_ENTITY_ID = 58;

    /**
     * {@code minecraft:giant}
     */
    int GIANT_ENTITY_ID = 60;

    /**
     * {@code minecraft:glow_item_frame}
     */
    int GLOW_ITEM_FRAME_ENTITY_ID = 61;

    /**
     * {@code minecraft:glow_squid}
     */
    int GLOW_SQUID_ENTITY_ID = 62;

    /**
     * {@code minecraft:goat}
     */
    int GOAT_ENTITY_ID = 63;

    /**
     * {@code minecraft:guardian}
     */
    int GUARDIAN_ENTITY_ID = 64;

    /**
     * {@code minecraft:happy_ghast}
     */
    int HAPPY_GHAST_ENTITY_ID = 59;

    /**
     * {@code minecraft:hoglin}
     */
    int HOGLIN_ENTITY_ID = 65;

    /**
     * {@code minecraft:hopper_minecart}
     */
    int HOPPER_MINECART_ENTITY_ID = 66;

    /**
     * {@code minecraft:horse}
     */
    int HORSE_ENTITY_ID = 67;

    /**
     * {@code minecraft:husk}
     */
    int HUSK_ENTITY_ID = 68;

    /**
     * {@code minecraft:illusioner}
     */
    int ILLUSIONER_ENTITY_ID = 69;

    /**
     * {@code minecraft:interaction}
     */
    int INTERACTION_ENTITY_ID = 70;

    /**
     * {@code minecraft:iron_golem}
     */
    int IRON_GOLEM_ENTITY_ID = 71;

    /**
     * {@code minecraft:item}
     */
    int ITEM_ENTITY_ID = 72;

    /**
     * {@code minecraft:item_display}
     */
    int ITEM_DISPLAY_ENTITY_ID = 73;

    /**
     * {@code minecraft:item_frame}
     */
    int ITEM_FRAME_ENTITY_ID = 74;

    /**
     * {@code minecraft:jungle_boat}
     */
    int JUNGLE_BOAT_ENTITY_ID = 75;

    /**
     * {@code minecraft:jungle_chest_boat}
     */
    int JUNGLE_CHEST_BOAT_ENTITY_ID = 76;

    /**
     * {@code minecraft:leash_knot}
     */
    int LEASH_KNOT_ENTITY_ID = 77;

    /**
     * {@code minecraft:lightning_bolt}
     */
    int LIGHTNING_BOLT_ENTITY_ID = 78;

    /**
     * {@code minecraft:lingering_potion}
     */
    int LINGERING_POTION_ENTITY_ID = 109;

    /**
     * {@code minecraft:llama}
     */
    int LLAMA_ENTITY_ID = 79;

    /**
     * {@code minecraft:llama_spit}
     */
    int LLAMA_SPIT_ENTITY_ID = 80;

    /**
     * {@code minecraft:magma_cube}
     */
    int MAGMA_CUBE_ENTITY_ID = 81;

    /**
     * {@code minecraft:mangrove_boat}
     */
    int MANGROVE_BOAT_ENTITY_ID = 82;

    /**
     * {@code minecraft:mangrove_chest_boat}
     */
    int MANGROVE_CHEST_BOAT_ENTITY_ID = 83;

    /**
     * {@code minecraft:mannequin}
     */
    int MANNEQUIN_ENTITY_ID = 84;

    /**
     * {@code minecraft:marker}
     */
    int MARKER_ENTITY_ID = 85;

    /**
     * {@code minecraft:minecart}
     */
    int MINECART_ENTITY_ID = 86;

    /**
     * {@code minecraft:mooshroom}
     */
    int MOOSHROOM_ENTITY_ID = 87;

    /**
     * {@code minecraft:mule}
     */
    int MULE_ENTITY_ID = 88;

    /**
     * {@code minecraft:nautilus}
     */
    int NAUTILUS_ENTITY_ID = 89;

    /**
     * {@code minecraft:oak_boat}
     */
    int OAK_BOAT_ENTITY_ID = 90;

    /**
     * {@code minecraft:oak_chest_boat}
     */
    int OAK_CHEST_BOAT_ENTITY_ID = 91;

    /**
     * {@code minecraft:ocelot}
     */
    int OCELOT_ENTITY_ID = 92;

    /**
     * {@code minecraft:ominous_item_spawner}
     */
    int OMINOUS_ITEM_SPAWNER_ENTITY_ID = 93;

    /**
     * {@code minecraft:painting}
     */
    int PAINTING_ENTITY_ID = 94;

    /**
     * {@code minecraft:pale_oak_boat}
     */
    int PALE_OAK_BOAT_ENTITY_ID = 95;

    /**
     * {@code minecraft:pale_oak_chest_boat}
     */
    int PALE_OAK_CHEST_BOAT_ENTITY_ID = 96;

    /**
     * {@code minecraft:panda}
     */
    int PANDA_ENTITY_ID = 97;

    /**
     * {@code minecraft:parched}
     */
    int PARCHED_ENTITY_ID = 98;

    /**
     * {@code minecraft:parrot}
     */
    int PARROT_ENTITY_ID = 99;

    /**
     * {@code minecraft:phantom}
     */
    int PHANTOM_ENTITY_ID = 100;

    /**
     * {@code minecraft:pig}
     */
    int PIG_ENTITY_ID = 101;

    /**
     * {@code minecraft:piglin}
     */
    int PIGLIN_ENTITY_ID = 102;

    /**
     * {@code minecraft:piglin_brute}
     */
    int PIGLIN_BRUTE_ENTITY_ID = 103;

    /**
     * {@code minecraft:pillager}
     */
    int PILLAGER_ENTITY_ID = 104;

    /**
     * {@code minecraft:player}
     */
    int PLAYER_ENTITY_ID = 159;

    /**
     * {@code minecraft:polar_bear}
     */
    int POLAR_BEAR_ENTITY_ID = 105;

    /**
     * {@code minecraft:poplar_boat}
     */
    int POPLAR_BOAT_ENTITY_ID = 106;

    /**
     * {@code minecraft:poplar_chest_boat}
     */
    int POPLAR_CHEST_BOAT_ENTITY_ID = 107;

    /**
     * {@code minecraft:pufferfish}
     */
    int PUFFERFISH_ENTITY_ID = 110;

    /**
     * {@code minecraft:rabbit}
     */
    int RABBIT_ENTITY_ID = 111;

    /**
     * {@code minecraft:ravager}
     */
    int RAVAGER_ENTITY_ID = 112;

    /**
     * {@code minecraft:salmon}
     */
    int SALMON_ENTITY_ID = 113;

    /**
     * {@code minecraft:sheep}
     */
    int SHEEP_ENTITY_ID = 114;

    /**
     * {@code minecraft:shulker}
     */
    int SHULKER_ENTITY_ID = 115;

    /**
     * {@code minecraft:shulker_bullet}
     */
    int SHULKER_BULLET_ENTITY_ID = 116;

    /**
     * {@code minecraft:silverfish}
     */
    int SILVERFISH_ENTITY_ID = 117;

    /**
     * {@code minecraft:skeleton}
     */
    int SKELETON_ENTITY_ID = 118;

    /**
     * {@code minecraft:skeleton_horse}
     */
    int SKELETON_HORSE_ENTITY_ID = 119;

    /**
     * {@code minecraft:slime}
     */
    int SLIME_ENTITY_ID = 120;

    /**
     * {@code minecraft:small_fireball}
     */
    int SMALL_FIREBALL_ENTITY_ID = 121;

    /**
     * {@code minecraft:sniffer}
     */
    int SNIFFER_ENTITY_ID = 122;

    /**
     * {@code minecraft:snow_golem}
     */
    int SNOW_GOLEM_ENTITY_ID = 124;

    /**
     * {@code minecraft:snowball}
     */
    int SNOWBALL_ENTITY_ID = 123;

    /**
     * {@code minecraft:spawner_minecart}
     */
    int SPAWNER_MINECART_ENTITY_ID = 125;

    /**
     * {@code minecraft:spectral_arrow}
     */
    int SPECTRAL_ARROW_ENTITY_ID = 126;

    /**
     * {@code minecraft:spider}
     */
    int SPIDER_ENTITY_ID = 127;

    /**
     * {@code minecraft:splash_potion}
     */
    int SPLASH_POTION_ENTITY_ID = 108;

    /**
     * {@code minecraft:spruce_boat}
     */
    int SPRUCE_BOAT_ENTITY_ID = 128;

    /**
     * {@code minecraft:spruce_chest_boat}
     */
    int SPRUCE_CHEST_BOAT_ENTITY_ID = 129;

    /**
     * {@code minecraft:squid}
     */
    int SQUID_ENTITY_ID = 130;

    /**
     * {@code minecraft:stray}
     */
    int STRAY_ENTITY_ID = 131;

    /**
     * {@code minecraft:strider}
     */
    int STRIDER_ENTITY_ID = 132;

    /**
     * {@code minecraft:sulfur_cube}
     */
    int SULFUR_CUBE_ENTITY_ID = 133;

    /**
     * {@code minecraft:tadpole}
     */
    int TADPOLE_ENTITY_ID = 134;

    /**
     * {@code minecraft:text_display}
     */
    int TEXT_DISPLAY_ENTITY_ID = 135;

    /**
     * {@code minecraft:tnt}
     */
    int TNT_ENTITY_ID = 136;

    /**
     * {@code minecraft:tnt_minecart}
     */
    int TNT_MINECART_ENTITY_ID = 137;

    /**
     * {@code minecraft:trader_llama}
     */
    int TRADER_LLAMA_ENTITY_ID = 138;

    /**
     * {@code minecraft:trident}
     */
    int TRIDENT_ENTITY_ID = 139;

    /**
     * {@code minecraft:tropical_fish}
     */
    int TROPICAL_FISH_ENTITY_ID = 140;

    /**
     * {@code minecraft:turtle}
     */
    int TURTLE_ENTITY_ID = 141;

    /**
     * {@code minecraft:vex}
     */
    int VEX_ENTITY_ID = 142;

    /**
     * {@code minecraft:villager}
     */
    int VILLAGER_ENTITY_ID = 143;

    /**
     * {@code minecraft:vindicator}
     */
    int VINDICATOR_ENTITY_ID = 144;

    /**
     * {@code minecraft:wandering_trader}
     */
    int WANDERING_TRADER_ENTITY_ID = 145;

    /**
     * {@code minecraft:warden}
     */
    int WARDEN_ENTITY_ID = 146;

    /**
     * {@code minecraft:wind_charge}
     */
    int WIND_CHARGE_ENTITY_ID = 147;

    /**
     * {@code minecraft:witch}
     */
    int WITCH_ENTITY_ID = 148;

    /**
     * {@code minecraft:wither}
     */
    int WITHER_ENTITY_ID = 149;

    /**
     * {@code minecraft:wither_skeleton}
     */
    int WITHER_SKELETON_ENTITY_ID = 150;

    /**
     * {@code minecraft:wither_skull}
     */
    int WITHER_SKULL_ENTITY_ID = 151;

    /**
     * {@code minecraft:wolf}
     */
    int WOLF_ENTITY_ID = 152;

    /**
     * {@code minecraft:zoglin}
     */
    int ZOGLIN_ENTITY_ID = 153;

    /**
     * {@code minecraft:zombie}
     */
    int ZOMBIE_ENTITY_ID = 154;

    /**
     * {@code minecraft:zombie_horse}
     */
    int ZOMBIE_HORSE_ENTITY_ID = 155;

    /**
     * {@code minecraft:zombie_nautilus}
     */
    int ZOMBIE_NAUTILUS_ENTITY_ID = 156;

    /**
     * {@code minecraft:zombie_villager}
     */
    int ZOMBIE_VILLAGER_ENTITY_ID = 157;

    /**
     * {@code minecraft:zombified_piglin}
     */
    int ZOMBIFIED_PIGLIN_ENTITY_ID = 158;

    /**
     * Immutable identifier to protocol ID lookup table.
     */
    Map<Key, Integer> BY_IDENTIFIER = Map.ofEntries(
        Map.entry(Key.key("acacia_boat"), ACACIA_BOAT_ENTITY_ID),
        Map.entry(Key.key("acacia_chest_boat"), ACACIA_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("allay"), ALLAY_ENTITY_ID),
        Map.entry(Key.key("area_effect_cloud"), AREA_EFFECT_CLOUD_ENTITY_ID),
        Map.entry(Key.key("armadillo"), ARMADILLO_ENTITY_ID),
        Map.entry(Key.key("armor_stand"), ARMOR_STAND_ENTITY_ID),
        Map.entry(Key.key("arrow"), ARROW_ENTITY_ID),
        Map.entry(Key.key("axolotl"), AXOLOTL_ENTITY_ID),
        Map.entry(Key.key("bamboo_chest_raft"), BAMBOO_CHEST_RAFT_ENTITY_ID),
        Map.entry(Key.key("bamboo_raft"), BAMBOO_RAFT_ENTITY_ID),
        Map.entry(Key.key("bat"), BAT_ENTITY_ID),
        Map.entry(Key.key("bee"), BEE_ENTITY_ID),
        Map.entry(Key.key("birch_boat"), BIRCH_BOAT_ENTITY_ID),
        Map.entry(Key.key("birch_chest_boat"), BIRCH_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("blaze"), BLAZE_ENTITY_ID),
        Map.entry(Key.key("block_display"), BLOCK_DISPLAY_ENTITY_ID),
        Map.entry(Key.key("bogged"), BOGGED_ENTITY_ID),
        Map.entry(Key.key("breeze"), BREEZE_ENTITY_ID),
        Map.entry(Key.key("breeze_wind_charge"), BREEZE_WIND_CHARGE_ENTITY_ID),
        Map.entry(Key.key("camel"), CAMEL_ENTITY_ID),
        Map.entry(Key.key("camel_husk"), CAMEL_HUSK_ENTITY_ID),
        Map.entry(Key.key("cat"), CAT_ENTITY_ID),
        Map.entry(Key.key("cave_spider"), CAVE_SPIDER_ENTITY_ID),
        Map.entry(Key.key("cherry_boat"), CHERRY_BOAT_ENTITY_ID),
        Map.entry(Key.key("cherry_chest_boat"), CHERRY_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("chest_minecart"), CHEST_MINECART_ENTITY_ID),
        Map.entry(Key.key("chicken"), CHICKEN_ENTITY_ID),
        Map.entry(Key.key("cod"), COD_ENTITY_ID),
        Map.entry(Key.key("command_block_minecart"), COMMAND_BLOCK_MINECART_ENTITY_ID),
        Map.entry(Key.key("copper_golem"), COPPER_GOLEM_ENTITY_ID),
        Map.entry(Key.key("cow"), COW_ENTITY_ID),
        Map.entry(Key.key("creaking"), CREAKING_ENTITY_ID),
        Map.entry(Key.key("creeper"), CREEPER_ENTITY_ID),
        Map.entry(Key.key("cushion"), CUSHION_ENTITY_ID),
        Map.entry(Key.key("dark_oak_boat"), DARK_OAK_BOAT_ENTITY_ID),
        Map.entry(Key.key("dark_oak_chest_boat"), DARK_OAK_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("dolphin"), DOLPHIN_ENTITY_ID),
        Map.entry(Key.key("donkey"), DONKEY_ENTITY_ID),
        Map.entry(Key.key("dragon_fireball"), DRAGON_FIREBALL_ENTITY_ID),
        Map.entry(Key.key("drowned"), DROWNED_ENTITY_ID),
        Map.entry(Key.key("egg"), EGG_ENTITY_ID),
        Map.entry(Key.key("elder_guardian"), ELDER_GUARDIAN_ENTITY_ID),
        Map.entry(Key.key("end_crystal"), END_CRYSTAL_ENTITY_ID),
        Map.entry(Key.key("ender_dragon"), ENDER_DRAGON_ENTITY_ID),
        Map.entry(Key.key("ender_pearl"), ENDER_PEARL_ENTITY_ID),
        Map.entry(Key.key("enderman"), ENDERMAN_ENTITY_ID),
        Map.entry(Key.key("endermite"), ENDERMITE_ENTITY_ID),
        Map.entry(Key.key("evoker"), EVOKER_ENTITY_ID),
        Map.entry(Key.key("evoker_fangs"), EVOKER_FANGS_ENTITY_ID),
        Map.entry(Key.key("experience_bottle"), EXPERIENCE_BOTTLE_ENTITY_ID),
        Map.entry(Key.key("experience_orb"), EXPERIENCE_ORB_ENTITY_ID),
        Map.entry(Key.key("eye_of_ender"), EYE_OF_ENDER_ENTITY_ID),
        Map.entry(Key.key("falling_block"), FALLING_BLOCK_ENTITY_ID),
        Map.entry(Key.key("fireball"), FIREBALL_ENTITY_ID),
        Map.entry(Key.key("firework_rocket"), FIREWORK_ROCKET_ENTITY_ID),
        Map.entry(Key.key("fishing_bobber"), FISHING_BOBBER_ENTITY_ID),
        Map.entry(Key.key("fox"), FOX_ENTITY_ID),
        Map.entry(Key.key("frog"), FROG_ENTITY_ID),
        Map.entry(Key.key("furnace_minecart"), FURNACE_MINECART_ENTITY_ID),
        Map.entry(Key.key("ghast"), GHAST_ENTITY_ID),
        Map.entry(Key.key("giant"), GIANT_ENTITY_ID),
        Map.entry(Key.key("glow_item_frame"), GLOW_ITEM_FRAME_ENTITY_ID),
        Map.entry(Key.key("glow_squid"), GLOW_SQUID_ENTITY_ID),
        Map.entry(Key.key("goat"), GOAT_ENTITY_ID),
        Map.entry(Key.key("guardian"), GUARDIAN_ENTITY_ID),
        Map.entry(Key.key("happy_ghast"), HAPPY_GHAST_ENTITY_ID),
        Map.entry(Key.key("hoglin"), HOGLIN_ENTITY_ID),
        Map.entry(Key.key("hopper_minecart"), HOPPER_MINECART_ENTITY_ID),
        Map.entry(Key.key("horse"), HORSE_ENTITY_ID),
        Map.entry(Key.key("husk"), HUSK_ENTITY_ID),
        Map.entry(Key.key("illusioner"), ILLUSIONER_ENTITY_ID),
        Map.entry(Key.key("interaction"), INTERACTION_ENTITY_ID),
        Map.entry(Key.key("iron_golem"), IRON_GOLEM_ENTITY_ID),
        Map.entry(Key.key("item"), ITEM_ENTITY_ID),
        Map.entry(Key.key("item_display"), ITEM_DISPLAY_ENTITY_ID),
        Map.entry(Key.key("item_frame"), ITEM_FRAME_ENTITY_ID),
        Map.entry(Key.key("jungle_boat"), JUNGLE_BOAT_ENTITY_ID),
        Map.entry(Key.key("jungle_chest_boat"), JUNGLE_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("leash_knot"), LEASH_KNOT_ENTITY_ID),
        Map.entry(Key.key("lightning_bolt"), LIGHTNING_BOLT_ENTITY_ID),
        Map.entry(Key.key("lingering_potion"), LINGERING_POTION_ENTITY_ID),
        Map.entry(Key.key("llama"), LLAMA_ENTITY_ID),
        Map.entry(Key.key("llama_spit"), LLAMA_SPIT_ENTITY_ID),
        Map.entry(Key.key("magma_cube"), MAGMA_CUBE_ENTITY_ID),
        Map.entry(Key.key("mangrove_boat"), MANGROVE_BOAT_ENTITY_ID),
        Map.entry(Key.key("mangrove_chest_boat"), MANGROVE_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("mannequin"), MANNEQUIN_ENTITY_ID),
        Map.entry(Key.key("marker"), MARKER_ENTITY_ID),
        Map.entry(Key.key("minecart"), MINECART_ENTITY_ID),
        Map.entry(Key.key("mooshroom"), MOOSHROOM_ENTITY_ID),
        Map.entry(Key.key("mule"), MULE_ENTITY_ID),
        Map.entry(Key.key("nautilus"), NAUTILUS_ENTITY_ID),
        Map.entry(Key.key("oak_boat"), OAK_BOAT_ENTITY_ID),
        Map.entry(Key.key("oak_chest_boat"), OAK_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("ocelot"), OCELOT_ENTITY_ID),
        Map.entry(Key.key("ominous_item_spawner"), OMINOUS_ITEM_SPAWNER_ENTITY_ID),
        Map.entry(Key.key("painting"), PAINTING_ENTITY_ID),
        Map.entry(Key.key("pale_oak_boat"), PALE_OAK_BOAT_ENTITY_ID),
        Map.entry(Key.key("pale_oak_chest_boat"), PALE_OAK_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("panda"), PANDA_ENTITY_ID),
        Map.entry(Key.key("parched"), PARCHED_ENTITY_ID),
        Map.entry(Key.key("parrot"), PARROT_ENTITY_ID),
        Map.entry(Key.key("phantom"), PHANTOM_ENTITY_ID),
        Map.entry(Key.key("pig"), PIG_ENTITY_ID),
        Map.entry(Key.key("piglin"), PIGLIN_ENTITY_ID),
        Map.entry(Key.key("piglin_brute"), PIGLIN_BRUTE_ENTITY_ID),
        Map.entry(Key.key("pillager"), PILLAGER_ENTITY_ID),
        Map.entry(Key.key("player"), PLAYER_ENTITY_ID),
        Map.entry(Key.key("polar_bear"), POLAR_BEAR_ENTITY_ID),
        Map.entry(Key.key("poplar_boat"), POPLAR_BOAT_ENTITY_ID),
        Map.entry(Key.key("poplar_chest_boat"), POPLAR_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("pufferfish"), PUFFERFISH_ENTITY_ID),
        Map.entry(Key.key("rabbit"), RABBIT_ENTITY_ID),
        Map.entry(Key.key("ravager"), RAVAGER_ENTITY_ID),
        Map.entry(Key.key("salmon"), SALMON_ENTITY_ID),
        Map.entry(Key.key("sheep"), SHEEP_ENTITY_ID),
        Map.entry(Key.key("shulker"), SHULKER_ENTITY_ID),
        Map.entry(Key.key("shulker_bullet"), SHULKER_BULLET_ENTITY_ID),
        Map.entry(Key.key("silverfish"), SILVERFISH_ENTITY_ID),
        Map.entry(Key.key("skeleton"), SKELETON_ENTITY_ID),
        Map.entry(Key.key("skeleton_horse"), SKELETON_HORSE_ENTITY_ID),
        Map.entry(Key.key("slime"), SLIME_ENTITY_ID),
        Map.entry(Key.key("small_fireball"), SMALL_FIREBALL_ENTITY_ID),
        Map.entry(Key.key("sniffer"), SNIFFER_ENTITY_ID),
        Map.entry(Key.key("snow_golem"), SNOW_GOLEM_ENTITY_ID),
        Map.entry(Key.key("snowball"), SNOWBALL_ENTITY_ID),
        Map.entry(Key.key("spawner_minecart"), SPAWNER_MINECART_ENTITY_ID),
        Map.entry(Key.key("spectral_arrow"), SPECTRAL_ARROW_ENTITY_ID),
        Map.entry(Key.key("spider"), SPIDER_ENTITY_ID),
        Map.entry(Key.key("splash_potion"), SPLASH_POTION_ENTITY_ID),
        Map.entry(Key.key("spruce_boat"), SPRUCE_BOAT_ENTITY_ID),
        Map.entry(Key.key("spruce_chest_boat"), SPRUCE_CHEST_BOAT_ENTITY_ID),
        Map.entry(Key.key("squid"), SQUID_ENTITY_ID),
        Map.entry(Key.key("stray"), STRAY_ENTITY_ID),
        Map.entry(Key.key("strider"), STRIDER_ENTITY_ID),
        Map.entry(Key.key("sulfur_cube"), SULFUR_CUBE_ENTITY_ID),
        Map.entry(Key.key("tadpole"), TADPOLE_ENTITY_ID),
        Map.entry(Key.key("text_display"), TEXT_DISPLAY_ENTITY_ID),
        Map.entry(Key.key("tnt"), TNT_ENTITY_ID),
        Map.entry(Key.key("tnt_minecart"), TNT_MINECART_ENTITY_ID),
        Map.entry(Key.key("trader_llama"), TRADER_LLAMA_ENTITY_ID),
        Map.entry(Key.key("trident"), TRIDENT_ENTITY_ID),
        Map.entry(Key.key("tropical_fish"), TROPICAL_FISH_ENTITY_ID),
        Map.entry(Key.key("turtle"), TURTLE_ENTITY_ID),
        Map.entry(Key.key("vex"), VEX_ENTITY_ID),
        Map.entry(Key.key("villager"), VILLAGER_ENTITY_ID),
        Map.entry(Key.key("vindicator"), VINDICATOR_ENTITY_ID),
        Map.entry(Key.key("wandering_trader"), WANDERING_TRADER_ENTITY_ID),
        Map.entry(Key.key("warden"), WARDEN_ENTITY_ID),
        Map.entry(Key.key("wind_charge"), WIND_CHARGE_ENTITY_ID),
        Map.entry(Key.key("witch"), WITCH_ENTITY_ID),
        Map.entry(Key.key("wither"), WITHER_ENTITY_ID),
        Map.entry(Key.key("wither_skeleton"), WITHER_SKELETON_ENTITY_ID),
        Map.entry(Key.key("wither_skull"), WITHER_SKULL_ENTITY_ID),
        Map.entry(Key.key("wolf"), WOLF_ENTITY_ID),
        Map.entry(Key.key("zoglin"), ZOGLIN_ENTITY_ID),
        Map.entry(Key.key("zombie"), ZOMBIE_ENTITY_ID),
        Map.entry(Key.key("zombie_horse"), ZOMBIE_HORSE_ENTITY_ID),
        Map.entry(Key.key("zombie_nautilus"), ZOMBIE_NAUTILUS_ENTITY_ID),
        Map.entry(Key.key("zombie_villager"), ZOMBIE_VILLAGER_ENTITY_ID),
        Map.entry(Key.key("zombified_piglin"), ZOMBIFIED_PIGLIN_ENTITY_ID)
    );

    /**
     * Resolves the protocol ID for a namespaced identifier.
     *
     * @param identifier namespaced identifier, e.g. {@code Key.key("minecraft", "chest")}
     * @return the protocol ID, or {@link #UNKNOWN} when the identifier is unknown
     */
    static int id(final Key identifier) {
        return BY_IDENTIFIER.getOrDefault(identifier, UNKNOWN);
    }
}
