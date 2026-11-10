package fr.euphyllia.fidorial.server.registry.data;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    int FISHING_BOBBER_ENTITY_ID = 162;

    /**
     * {@code minecraft:fox}
     */
    int FOX_ENTITY_ID = 55;

    /**
     * {@code minecraft:frog}
     */
    int FROG_ENTITY_ID = 56;

    /**
     * {@code minecraft:frostbite}
     */
    int FROSTBITE_ENTITY_ID = 57;

    /**
     * {@code minecraft:furnace_minecart}
     */
    int FURNACE_MINECART_ENTITY_ID = 58;

    /**
     * {@code minecraft:ghast}
     */
    int GHAST_ENTITY_ID = 59;

    /**
     * {@code minecraft:giant}
     */
    int GIANT_ENTITY_ID = 61;

    /**
     * {@code minecraft:glow_item_frame}
     */
    int GLOW_ITEM_FRAME_ENTITY_ID = 62;

    /**
     * {@code minecraft:glow_squid}
     */
    int GLOW_SQUID_ENTITY_ID = 63;

    /**
     * {@code minecraft:goat}
     */
    int GOAT_ENTITY_ID = 64;

    /**
     * {@code minecraft:guardian}
     */
    int GUARDIAN_ENTITY_ID = 65;

    /**
     * {@code minecraft:happy_ghast}
     */
    int HAPPY_GHAST_ENTITY_ID = 60;

    /**
     * {@code minecraft:hoglin}
     */
    int HOGLIN_ENTITY_ID = 66;

    /**
     * {@code minecraft:hopper_minecart}
     */
    int HOPPER_MINECART_ENTITY_ID = 67;

    /**
     * {@code minecraft:horse}
     */
    int HORSE_ENTITY_ID = 68;

    /**
     * {@code minecraft:husk}
     */
    int HUSK_ENTITY_ID = 69;

    /**
     * {@code minecraft:ice_ball}
     */
    int ICE_BALL_ENTITY_ID = 70;

    /**
     * {@code minecraft:illusioner}
     */
    int ILLUSIONER_ENTITY_ID = 71;

    /**
     * {@code minecraft:interaction}
     */
    int INTERACTION_ENTITY_ID = 72;

    /**
     * {@code minecraft:iron_golem}
     */
    int IRON_GOLEM_ENTITY_ID = 73;

    /**
     * {@code minecraft:item}
     */
    int ITEM_ENTITY_ID = 74;

    /**
     * {@code minecraft:item_display}
     */
    int ITEM_DISPLAY_ENTITY_ID = 75;

    /**
     * {@code minecraft:item_frame}
     */
    int ITEM_FRAME_ENTITY_ID = 76;

    /**
     * {@code minecraft:jungle_boat}
     */
    int JUNGLE_BOAT_ENTITY_ID = 77;

    /**
     * {@code minecraft:jungle_chest_boat}
     */
    int JUNGLE_CHEST_BOAT_ENTITY_ID = 78;

    /**
     * {@code minecraft:leash_knot}
     */
    int LEASH_KNOT_ENTITY_ID = 79;

    /**
     * {@code minecraft:lightning_bolt}
     */
    int LIGHTNING_BOLT_ENTITY_ID = 80;

    /**
     * {@code minecraft:lingering_potion}
     */
    int LINGERING_POTION_ENTITY_ID = 111;

    /**
     * {@code minecraft:llama}
     */
    int LLAMA_ENTITY_ID = 81;

    /**
     * {@code minecraft:llama_spit}
     */
    int LLAMA_SPIT_ENTITY_ID = 82;

    /**
     * {@code minecraft:magma_cube}
     */
    int MAGMA_CUBE_ENTITY_ID = 83;

    /**
     * {@code minecraft:mangrove_boat}
     */
    int MANGROVE_BOAT_ENTITY_ID = 84;

    /**
     * {@code minecraft:mangrove_chest_boat}
     */
    int MANGROVE_CHEST_BOAT_ENTITY_ID = 85;

    /**
     * {@code minecraft:mannequin}
     */
    int MANNEQUIN_ENTITY_ID = 86;

    /**
     * {@code minecraft:marker}
     */
    int MARKER_ENTITY_ID = 87;

    /**
     * {@code minecraft:minecart}
     */
    int MINECART_ENTITY_ID = 88;

    /**
     * {@code minecraft:mooshroom}
     */
    int MOOSHROOM_ENTITY_ID = 89;

    /**
     * {@code minecraft:mule}
     */
    int MULE_ENTITY_ID = 90;

    /**
     * {@code minecraft:nautilus}
     */
    int NAUTILUS_ENTITY_ID = 91;

    /**
     * {@code minecraft:oak_boat}
     */
    int OAK_BOAT_ENTITY_ID = 92;

    /**
     * {@code minecraft:oak_chest_boat}
     */
    int OAK_CHEST_BOAT_ENTITY_ID = 93;

    /**
     * {@code minecraft:ocelot}
     */
    int OCELOT_ENTITY_ID = 94;

    /**
     * {@code minecraft:ominous_item_spawner}
     */
    int OMINOUS_ITEM_SPAWNER_ENTITY_ID = 95;

    /**
     * {@code minecraft:painting}
     */
    int PAINTING_ENTITY_ID = 96;

    /**
     * {@code minecraft:pale_oak_boat}
     */
    int PALE_OAK_BOAT_ENTITY_ID = 97;

    /**
     * {@code minecraft:pale_oak_chest_boat}
     */
    int PALE_OAK_CHEST_BOAT_ENTITY_ID = 98;

    /**
     * {@code minecraft:panda}
     */
    int PANDA_ENTITY_ID = 99;

    /**
     * {@code minecraft:parched}
     */
    int PARCHED_ENTITY_ID = 100;

    /**
     * {@code minecraft:parrot}
     */
    int PARROT_ENTITY_ID = 101;

    /**
     * {@code minecraft:phantom}
     */
    int PHANTOM_ENTITY_ID = 102;

    /**
     * {@code minecraft:pig}
     */
    int PIG_ENTITY_ID = 103;

    /**
     * {@code minecraft:piglin}
     */
    int PIGLIN_ENTITY_ID = 104;

    /**
     * {@code minecraft:piglin_brute}
     */
    int PIGLIN_BRUTE_ENTITY_ID = 105;

    /**
     * {@code minecraft:pillager}
     */
    int PILLAGER_ENTITY_ID = 106;

    /**
     * {@code minecraft:player}
     */
    int PLAYER_ENTITY_ID = 161;

    /**
     * {@code minecraft:polar_bear}
     */
    int POLAR_BEAR_ENTITY_ID = 107;

    /**
     * {@code minecraft:poplar_boat}
     */
    int POPLAR_BOAT_ENTITY_ID = 108;

    /**
     * {@code minecraft:poplar_chest_boat}
     */
    int POPLAR_CHEST_BOAT_ENTITY_ID = 109;

    /**
     * {@code minecraft:pufferfish}
     */
    int PUFFERFISH_ENTITY_ID = 112;

    /**
     * {@code minecraft:rabbit}
     */
    int RABBIT_ENTITY_ID = 113;

    /**
     * {@code minecraft:ravager}
     */
    int RAVAGER_ENTITY_ID = 114;

    /**
     * {@code minecraft:salmon}
     */
    int SALMON_ENTITY_ID = 115;

    /**
     * {@code minecraft:sheep}
     */
    int SHEEP_ENTITY_ID = 116;

    /**
     * {@code minecraft:shulker}
     */
    int SHULKER_ENTITY_ID = 117;

    /**
     * {@code minecraft:shulker_bullet}
     */
    int SHULKER_BULLET_ENTITY_ID = 118;

    /**
     * {@code minecraft:silverfish}
     */
    int SILVERFISH_ENTITY_ID = 119;

    /**
     * {@code minecraft:skeleton}
     */
    int SKELETON_ENTITY_ID = 120;

    /**
     * {@code minecraft:skeleton_horse}
     */
    int SKELETON_HORSE_ENTITY_ID = 121;

    /**
     * {@code minecraft:slime}
     */
    int SLIME_ENTITY_ID = 122;

    /**
     * {@code minecraft:small_fireball}
     */
    int SMALL_FIREBALL_ENTITY_ID = 123;

    /**
     * {@code minecraft:sniffer}
     */
    int SNIFFER_ENTITY_ID = 124;

    /**
     * {@code minecraft:snow_golem}
     */
    int SNOW_GOLEM_ENTITY_ID = 126;

    /**
     * {@code minecraft:snowball}
     */
    int SNOWBALL_ENTITY_ID = 125;

    /**
     * {@code minecraft:spawner_minecart}
     */
    int SPAWNER_MINECART_ENTITY_ID = 127;

    /**
     * {@code minecraft:spectral_arrow}
     */
    int SPECTRAL_ARROW_ENTITY_ID = 128;

    /**
     * {@code minecraft:spider}
     */
    int SPIDER_ENTITY_ID = 129;

    /**
     * {@code minecraft:splash_potion}
     */
    int SPLASH_POTION_ENTITY_ID = 110;

    /**
     * {@code minecraft:spruce_boat}
     */
    int SPRUCE_BOAT_ENTITY_ID = 130;

    /**
     * {@code minecraft:spruce_chest_boat}
     */
    int SPRUCE_CHEST_BOAT_ENTITY_ID = 131;

    /**
     * {@code minecraft:squid}
     */
    int SQUID_ENTITY_ID = 132;

    /**
     * {@code minecraft:stray}
     */
    int STRAY_ENTITY_ID = 133;

    /**
     * {@code minecraft:strider}
     */
    int STRIDER_ENTITY_ID = 134;

    /**
     * {@code minecraft:sulfur_cube}
     */
    int SULFUR_CUBE_ENTITY_ID = 135;

    /**
     * {@code minecraft:tadpole}
     */
    int TADPOLE_ENTITY_ID = 136;

    /**
     * {@code minecraft:text_display}
     */
    int TEXT_DISPLAY_ENTITY_ID = 137;

    /**
     * {@code minecraft:tnt}
     */
    int TNT_ENTITY_ID = 138;

    /**
     * {@code minecraft:tnt_minecart}
     */
    int TNT_MINECART_ENTITY_ID = 139;

    /**
     * {@code minecraft:trader_llama}
     */
    int TRADER_LLAMA_ENTITY_ID = 140;

    /**
     * {@code minecraft:trident}
     */
    int TRIDENT_ENTITY_ID = 141;

    /**
     * {@code minecraft:tropical_fish}
     */
    int TROPICAL_FISH_ENTITY_ID = 142;

    /**
     * {@code minecraft:turtle}
     */
    int TURTLE_ENTITY_ID = 143;

    /**
     * {@code minecraft:vex}
     */
    int VEX_ENTITY_ID = 144;

    /**
     * {@code minecraft:villager}
     */
    int VILLAGER_ENTITY_ID = 145;

    /**
     * {@code minecraft:vindicator}
     */
    int VINDICATOR_ENTITY_ID = 146;

    /**
     * {@code minecraft:wandering_trader}
     */
    int WANDERING_TRADER_ENTITY_ID = 147;

    /**
     * {@code minecraft:warden}
     */
    int WARDEN_ENTITY_ID = 148;

    /**
     * {@code minecraft:wind_charge}
     */
    int WIND_CHARGE_ENTITY_ID = 149;

    /**
     * {@code minecraft:witch}
     */
    int WITCH_ENTITY_ID = 150;

    /**
     * {@code minecraft:wither}
     */
    int WITHER_ENTITY_ID = 151;

    /**
     * {@code minecraft:wither_skeleton}
     */
    int WITHER_SKELETON_ENTITY_ID = 152;

    /**
     * {@code minecraft:wither_skull}
     */
    int WITHER_SKULL_ENTITY_ID = 153;

    /**
     * {@code minecraft:wolf}
     */
    int WOLF_ENTITY_ID = 154;

    /**
     * {@code minecraft:zoglin}
     */
    int ZOGLIN_ENTITY_ID = 155;

    /**
     * {@code minecraft:zombie}
     */
    int ZOMBIE_ENTITY_ID = 156;

    /**
     * {@code minecraft:zombie_horse}
     */
    int ZOMBIE_HORSE_ENTITY_ID = 157;

    /**
     * {@code minecraft:zombie_nautilus}
     */
    int ZOMBIE_NAUTILUS_ENTITY_ID = 158;

    /**
     * {@code minecraft:zombie_villager}
     */
    int ZOMBIE_VILLAGER_ENTITY_ID = 159;

    /**
     * {@code minecraft:zombified_piglin}
     */
    int ZOMBIFIED_PIGLIN_ENTITY_ID = 160;

    /**
     * Immutable identifier to protocol ID lookup table.
     */
    Object2IntMap<Key> BY_IDENTIFIER = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("acacia_boat"),
            Key.key("acacia_chest_boat"),
            Key.key("allay"),
            Key.key("area_effect_cloud"),
            Key.key("armadillo"),
            Key.key("armor_stand"),
            Key.key("arrow"),
            Key.key("axolotl"),
            Key.key("bamboo_chest_raft"),
            Key.key("bamboo_raft"),
            Key.key("bat"),
            Key.key("bee"),
            Key.key("birch_boat"),
            Key.key("birch_chest_boat"),
            Key.key("blaze"),
            Key.key("block_display"),
            Key.key("bogged"),
            Key.key("breeze"),
            Key.key("breeze_wind_charge"),
            Key.key("camel"),
            Key.key("camel_husk"),
            Key.key("cat"),
            Key.key("cave_spider"),
            Key.key("cherry_boat"),
            Key.key("cherry_chest_boat"),
            Key.key("chest_minecart"),
            Key.key("chicken"),
            Key.key("cod"),
            Key.key("command_block_minecart"),
            Key.key("copper_golem"),
            Key.key("cow"),
            Key.key("creaking"),
            Key.key("creeper"),
            Key.key("cushion"),
            Key.key("dark_oak_boat"),
            Key.key("dark_oak_chest_boat"),
            Key.key("dolphin"),
            Key.key("donkey"),
            Key.key("dragon_fireball"),
            Key.key("drowned"),
            Key.key("egg"),
            Key.key("elder_guardian"),
            Key.key("end_crystal"),
            Key.key("ender_dragon"),
            Key.key("ender_pearl"),
            Key.key("enderman"),
            Key.key("endermite"),
            Key.key("evoker"),
            Key.key("evoker_fangs"),
            Key.key("experience_bottle"),
            Key.key("experience_orb"),
            Key.key("eye_of_ender"),
            Key.key("falling_block"),
            Key.key("fireball"),
            Key.key("firework_rocket"),
            Key.key("fishing_bobber"),
            Key.key("fox"),
            Key.key("frog"),
            Key.key("frostbite"),
            Key.key("furnace_minecart"),
            Key.key("ghast"),
            Key.key("giant"),
            Key.key("glow_item_frame"),
            Key.key("glow_squid"),
            Key.key("goat"),
            Key.key("guardian"),
            Key.key("happy_ghast"),
            Key.key("hoglin"),
            Key.key("hopper_minecart"),
            Key.key("horse"),
            Key.key("husk"),
            Key.key("ice_ball"),
            Key.key("illusioner"),
            Key.key("interaction"),
            Key.key("iron_golem"),
            Key.key("item"),
            Key.key("item_display"),
            Key.key("item_frame"),
            Key.key("jungle_boat"),
            Key.key("jungle_chest_boat"),
            Key.key("leash_knot"),
            Key.key("lightning_bolt"),
            Key.key("lingering_potion"),
            Key.key("llama"),
            Key.key("llama_spit"),
            Key.key("magma_cube"),
            Key.key("mangrove_boat"),
            Key.key("mangrove_chest_boat"),
            Key.key("mannequin"),
            Key.key("marker"),
            Key.key("minecart"),
            Key.key("mooshroom"),
            Key.key("mule"),
            Key.key("nautilus"),
            Key.key("oak_boat"),
            Key.key("oak_chest_boat"),
            Key.key("ocelot"),
            Key.key("ominous_item_spawner"),
            Key.key("painting"),
            Key.key("pale_oak_boat"),
            Key.key("pale_oak_chest_boat"),
            Key.key("panda"),
            Key.key("parched"),
            Key.key("parrot"),
            Key.key("phantom"),
            Key.key("pig"),
            Key.key("piglin"),
            Key.key("piglin_brute"),
            Key.key("pillager"),
            Key.key("player"),
            Key.key("polar_bear"),
            Key.key("poplar_boat"),
            Key.key("poplar_chest_boat"),
            Key.key("pufferfish"),
            Key.key("rabbit"),
            Key.key("ravager"),
            Key.key("salmon"),
            Key.key("sheep"),
            Key.key("shulker"),
            Key.key("shulker_bullet"),
            Key.key("silverfish"),
            Key.key("skeleton"),
            Key.key("skeleton_horse"),
            Key.key("slime"),
            Key.key("small_fireball"),
            Key.key("sniffer"),
            Key.key("snow_golem"),
            Key.key("snowball"),
            Key.key("spawner_minecart"),
            Key.key("spectral_arrow"),
            Key.key("spider"),
            Key.key("splash_potion"),
            Key.key("spruce_boat"),
            Key.key("spruce_chest_boat"),
            Key.key("squid"),
            Key.key("stray"),
            Key.key("strider"),
            Key.key("sulfur_cube"),
            Key.key("tadpole"),
            Key.key("text_display"),
            Key.key("tnt"),
            Key.key("tnt_minecart"),
            Key.key("trader_llama"),
            Key.key("trident"),
            Key.key("tropical_fish"),
            Key.key("turtle"),
            Key.key("vex"),
            Key.key("villager"),
            Key.key("vindicator"),
            Key.key("wandering_trader"),
            Key.key("warden"),
            Key.key("wind_charge"),
            Key.key("witch"),
            Key.key("wither"),
            Key.key("wither_skeleton"),
            Key.key("wither_skull"),
            Key.key("wolf"),
            Key.key("zoglin"),
            Key.key("zombie"),
            Key.key("zombie_horse"),
            Key.key("zombie_nautilus"),
            Key.key("zombie_villager"),
            Key.key("zombified_piglin")
        },
        new int[] {
            ACACIA_BOAT_ENTITY_ID,
            ACACIA_CHEST_BOAT_ENTITY_ID,
            ALLAY_ENTITY_ID,
            AREA_EFFECT_CLOUD_ENTITY_ID,
            ARMADILLO_ENTITY_ID,
            ARMOR_STAND_ENTITY_ID,
            ARROW_ENTITY_ID,
            AXOLOTL_ENTITY_ID,
            BAMBOO_CHEST_RAFT_ENTITY_ID,
            BAMBOO_RAFT_ENTITY_ID,
            BAT_ENTITY_ID,
            BEE_ENTITY_ID,
            BIRCH_BOAT_ENTITY_ID,
            BIRCH_CHEST_BOAT_ENTITY_ID,
            BLAZE_ENTITY_ID,
            BLOCK_DISPLAY_ENTITY_ID,
            BOGGED_ENTITY_ID,
            BREEZE_ENTITY_ID,
            BREEZE_WIND_CHARGE_ENTITY_ID,
            CAMEL_ENTITY_ID,
            CAMEL_HUSK_ENTITY_ID,
            CAT_ENTITY_ID,
            CAVE_SPIDER_ENTITY_ID,
            CHERRY_BOAT_ENTITY_ID,
            CHERRY_CHEST_BOAT_ENTITY_ID,
            CHEST_MINECART_ENTITY_ID,
            CHICKEN_ENTITY_ID,
            COD_ENTITY_ID,
            COMMAND_BLOCK_MINECART_ENTITY_ID,
            COPPER_GOLEM_ENTITY_ID,
            COW_ENTITY_ID,
            CREAKING_ENTITY_ID,
            CREEPER_ENTITY_ID,
            CUSHION_ENTITY_ID,
            DARK_OAK_BOAT_ENTITY_ID,
            DARK_OAK_CHEST_BOAT_ENTITY_ID,
            DOLPHIN_ENTITY_ID,
            DONKEY_ENTITY_ID,
            DRAGON_FIREBALL_ENTITY_ID,
            DROWNED_ENTITY_ID,
            EGG_ENTITY_ID,
            ELDER_GUARDIAN_ENTITY_ID,
            END_CRYSTAL_ENTITY_ID,
            ENDER_DRAGON_ENTITY_ID,
            ENDER_PEARL_ENTITY_ID,
            ENDERMAN_ENTITY_ID,
            ENDERMITE_ENTITY_ID,
            EVOKER_ENTITY_ID,
            EVOKER_FANGS_ENTITY_ID,
            EXPERIENCE_BOTTLE_ENTITY_ID,
            EXPERIENCE_ORB_ENTITY_ID,
            EYE_OF_ENDER_ENTITY_ID,
            FALLING_BLOCK_ENTITY_ID,
            FIREBALL_ENTITY_ID,
            FIREWORK_ROCKET_ENTITY_ID,
            FISHING_BOBBER_ENTITY_ID,
            FOX_ENTITY_ID,
            FROG_ENTITY_ID,
            FROSTBITE_ENTITY_ID,
            FURNACE_MINECART_ENTITY_ID,
            GHAST_ENTITY_ID,
            GIANT_ENTITY_ID,
            GLOW_ITEM_FRAME_ENTITY_ID,
            GLOW_SQUID_ENTITY_ID,
            GOAT_ENTITY_ID,
            GUARDIAN_ENTITY_ID,
            HAPPY_GHAST_ENTITY_ID,
            HOGLIN_ENTITY_ID,
            HOPPER_MINECART_ENTITY_ID,
            HORSE_ENTITY_ID,
            HUSK_ENTITY_ID,
            ICE_BALL_ENTITY_ID,
            ILLUSIONER_ENTITY_ID,
            INTERACTION_ENTITY_ID,
            IRON_GOLEM_ENTITY_ID,
            ITEM_ENTITY_ID,
            ITEM_DISPLAY_ENTITY_ID,
            ITEM_FRAME_ENTITY_ID,
            JUNGLE_BOAT_ENTITY_ID,
            JUNGLE_CHEST_BOAT_ENTITY_ID,
            LEASH_KNOT_ENTITY_ID,
            LIGHTNING_BOLT_ENTITY_ID,
            LINGERING_POTION_ENTITY_ID,
            LLAMA_ENTITY_ID,
            LLAMA_SPIT_ENTITY_ID,
            MAGMA_CUBE_ENTITY_ID,
            MANGROVE_BOAT_ENTITY_ID,
            MANGROVE_CHEST_BOAT_ENTITY_ID,
            MANNEQUIN_ENTITY_ID,
            MARKER_ENTITY_ID,
            MINECART_ENTITY_ID,
            MOOSHROOM_ENTITY_ID,
            MULE_ENTITY_ID,
            NAUTILUS_ENTITY_ID,
            OAK_BOAT_ENTITY_ID,
            OAK_CHEST_BOAT_ENTITY_ID,
            OCELOT_ENTITY_ID,
            OMINOUS_ITEM_SPAWNER_ENTITY_ID,
            PAINTING_ENTITY_ID,
            PALE_OAK_BOAT_ENTITY_ID,
            PALE_OAK_CHEST_BOAT_ENTITY_ID,
            PANDA_ENTITY_ID,
            PARCHED_ENTITY_ID,
            PARROT_ENTITY_ID,
            PHANTOM_ENTITY_ID,
            PIG_ENTITY_ID,
            PIGLIN_ENTITY_ID,
            PIGLIN_BRUTE_ENTITY_ID,
            PILLAGER_ENTITY_ID,
            PLAYER_ENTITY_ID,
            POLAR_BEAR_ENTITY_ID,
            POPLAR_BOAT_ENTITY_ID,
            POPLAR_CHEST_BOAT_ENTITY_ID,
            PUFFERFISH_ENTITY_ID,
            RABBIT_ENTITY_ID,
            RAVAGER_ENTITY_ID,
            SALMON_ENTITY_ID,
            SHEEP_ENTITY_ID,
            SHULKER_ENTITY_ID,
            SHULKER_BULLET_ENTITY_ID,
            SILVERFISH_ENTITY_ID,
            SKELETON_ENTITY_ID,
            SKELETON_HORSE_ENTITY_ID,
            SLIME_ENTITY_ID,
            SMALL_FIREBALL_ENTITY_ID,
            SNIFFER_ENTITY_ID,
            SNOW_GOLEM_ENTITY_ID,
            SNOWBALL_ENTITY_ID,
            SPAWNER_MINECART_ENTITY_ID,
            SPECTRAL_ARROW_ENTITY_ID,
            SPIDER_ENTITY_ID,
            SPLASH_POTION_ENTITY_ID,
            SPRUCE_BOAT_ENTITY_ID,
            SPRUCE_CHEST_BOAT_ENTITY_ID,
            SQUID_ENTITY_ID,
            STRAY_ENTITY_ID,
            STRIDER_ENTITY_ID,
            SULFUR_CUBE_ENTITY_ID,
            TADPOLE_ENTITY_ID,
            TEXT_DISPLAY_ENTITY_ID,
            TNT_ENTITY_ID,
            TNT_MINECART_ENTITY_ID,
            TRADER_LLAMA_ENTITY_ID,
            TRIDENT_ENTITY_ID,
            TROPICAL_FISH_ENTITY_ID,
            TURTLE_ENTITY_ID,
            VEX_ENTITY_ID,
            VILLAGER_ENTITY_ID,
            VINDICATOR_ENTITY_ID,
            WANDERING_TRADER_ENTITY_ID,
            WARDEN_ENTITY_ID,
            WIND_CHARGE_ENTITY_ID,
            WITCH_ENTITY_ID,
            WITHER_ENTITY_ID,
            WITHER_SKELETON_ENTITY_ID,
            WITHER_SKULL_ENTITY_ID,
            WOLF_ENTITY_ID,
            ZOGLIN_ENTITY_ID,
            ZOMBIE_ENTITY_ID,
            ZOMBIE_HORSE_ENTITY_ID,
            ZOMBIE_NAUTILUS_ENTITY_ID,
            ZOMBIE_VILLAGER_ENTITY_ID,
            ZOMBIFIED_PIGLIN_ENTITY_ID
        }
    ));

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
