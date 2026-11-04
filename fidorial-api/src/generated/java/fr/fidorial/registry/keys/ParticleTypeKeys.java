package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ParticleType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:particle_type} registry.
 */
public final class ParticleTypeKeys {
    /**
     * Key for {@code minecraft:angry_villager}.
     */
    public static final TypedKey<ParticleType> ANGRY_VILLAGER = create("angry_villager");

    /**
     * Key for {@code minecraft:ash}.
     */
    public static final TypedKey<ParticleType> ASH = create("ash");

    /**
     * Key for {@code minecraft:block}.
     */
    public static final TypedKey<ParticleType> BLOCK = create("block");

    /**
     * Key for {@code minecraft:block_crumble}.
     */
    public static final TypedKey<ParticleType> BLOCK_CRUMBLE = create("block_crumble");

    /**
     * Key for {@code minecraft:block_marker}.
     */
    public static final TypedKey<ParticleType> BLOCK_MARKER = create("block_marker");

    /**
     * Key for {@code minecraft:bubble}.
     */
    public static final TypedKey<ParticleType> BUBBLE = create("bubble");

    /**
     * Key for {@code minecraft:bubble_column_up}.
     */
    public static final TypedKey<ParticleType> BUBBLE_COLUMN_UP = create("bubble_column_up");

    /**
     * Key for {@code minecraft:bubble_pop}.
     */
    public static final TypedKey<ParticleType> BUBBLE_POP = create("bubble_pop");

    /**
     * Key for {@code minecraft:campfire_cosy_smoke}.
     */
    public static final TypedKey<ParticleType> CAMPFIRE_COSY_SMOKE = create("campfire_cosy_smoke");

    /**
     * Key for {@code minecraft:campfire_signal_smoke}.
     */
    public static final TypedKey<ParticleType> CAMPFIRE_SIGNAL_SMOKE = create("campfire_signal_smoke");

    /**
     * Key for {@code minecraft:cherry_leaves}.
     */
    public static final TypedKey<ParticleType> CHERRY_LEAVES = create("cherry_leaves");

    /**
     * Key for {@code minecraft:cloud}.
     */
    public static final TypedKey<ParticleType> CLOUD = create("cloud");

    /**
     * Key for {@code minecraft:composter}.
     */
    public static final TypedKey<ParticleType> COMPOSTER = create("composter");

    /**
     * Key for {@code minecraft:copper_fire_flame}.
     */
    public static final TypedKey<ParticleType> COPPER_FIRE_FLAME = create("copper_fire_flame");

    /**
     * Key for {@code minecraft:crimson_spore}.
     */
    public static final TypedKey<ParticleType> CRIMSON_SPORE = create("crimson_spore");

    /**
     * Key for {@code minecraft:crit}.
     */
    public static final TypedKey<ParticleType> CRIT = create("crit");

    /**
     * Key for {@code minecraft:current_down}.
     */
    public static final TypedKey<ParticleType> CURRENT_DOWN = create("current_down");

    /**
     * Key for {@code minecraft:damage_indicator}.
     */
    public static final TypedKey<ParticleType> DAMAGE_INDICATOR = create("damage_indicator");

    /**
     * Key for {@code minecraft:dolphin}.
     */
    public static final TypedKey<ParticleType> DOLPHIN = create("dolphin");

    /**
     * Key for {@code minecraft:dragon_breath}.
     */
    public static final TypedKey<ParticleType> DRAGON_BREATH = create("dragon_breath");

    /**
     * Key for {@code minecraft:dripping_dripstone_lava}.
     */
    public static final TypedKey<ParticleType> DRIPPING_DRIPSTONE_LAVA = create("dripping_dripstone_lava");

    /**
     * Key for {@code minecraft:dripping_dripstone_water}.
     */
    public static final TypedKey<ParticleType> DRIPPING_DRIPSTONE_WATER = create("dripping_dripstone_water");

    /**
     * Key for {@code minecraft:dripping_honey}.
     */
    public static final TypedKey<ParticleType> DRIPPING_HONEY = create("dripping_honey");

    /**
     * Key for {@code minecraft:dripping_lava}.
     */
    public static final TypedKey<ParticleType> DRIPPING_LAVA = create("dripping_lava");

    /**
     * Key for {@code minecraft:dripping_obsidian_tear}.
     */
    public static final TypedKey<ParticleType> DRIPPING_OBSIDIAN_TEAR = create("dripping_obsidian_tear");

    /**
     * Key for {@code minecraft:dripping_water}.
     */
    public static final TypedKey<ParticleType> DRIPPING_WATER = create("dripping_water");

    /**
     * Key for {@code minecraft:dust}.
     */
    public static final TypedKey<ParticleType> DUST = create("dust");

    /**
     * Key for {@code minecraft:dust_color_transition}.
     */
    public static final TypedKey<ParticleType> DUST_COLOR_TRANSITION = create("dust_color_transition");

    /**
     * Key for {@code minecraft:dust_pillar}.
     */
    public static final TypedKey<ParticleType> DUST_PILLAR = create("dust_pillar");

    /**
     * Key for {@code minecraft:dust_plume}.
     */
    public static final TypedKey<ParticleType> DUST_PLUME = create("dust_plume");

    /**
     * Key for {@code minecraft:effect}.
     */
    public static final TypedKey<ParticleType> EFFECT = create("effect");

    /**
     * Key for {@code minecraft:egg_crack}.
     */
    public static final TypedKey<ParticleType> EGG_CRACK = create("egg_crack");

    /**
     * Key for {@code minecraft:elder_guardian}.
     */
    public static final TypedKey<ParticleType> ELDER_GUARDIAN = create("elder_guardian");

    /**
     * Key for {@code minecraft:electric_spark}.
     */
    public static final TypedKey<ParticleType> ELECTRIC_SPARK = create("electric_spark");

    /**
     * Key for {@code minecraft:enchant}.
     */
    public static final TypedKey<ParticleType> ENCHANT = create("enchant");

    /**
     * Key for {@code minecraft:enchanted_hit}.
     */
    public static final TypedKey<ParticleType> ENCHANTED_HIT = create("enchanted_hit");

    /**
     * Key for {@code minecraft:end_rod}.
     */
    public static final TypedKey<ParticleType> END_ROD = create("end_rod");

    /**
     * Key for {@code minecraft:entity_effect}.
     */
    public static final TypedKey<ParticleType> ENTITY_EFFECT = create("entity_effect");

    /**
     * Key for {@code minecraft:explosion}.
     */
    public static final TypedKey<ParticleType> EXPLOSION = create("explosion");

    /**
     * Key for {@code minecraft:explosion_emitter}.
     */
    public static final TypedKey<ParticleType> EXPLOSION_EMITTER = create("explosion_emitter");

    /**
     * Key for {@code minecraft:falling_dripstone_lava}.
     */
    public static final TypedKey<ParticleType> FALLING_DRIPSTONE_LAVA = create("falling_dripstone_lava");

    /**
     * Key for {@code minecraft:falling_dripstone_water}.
     */
    public static final TypedKey<ParticleType> FALLING_DRIPSTONE_WATER = create("falling_dripstone_water");

    /**
     * Key for {@code minecraft:falling_dust}.
     */
    public static final TypedKey<ParticleType> FALLING_DUST = create("falling_dust");

    /**
     * Key for {@code minecraft:falling_honey}.
     */
    public static final TypedKey<ParticleType> FALLING_HONEY = create("falling_honey");

    /**
     * Key for {@code minecraft:falling_lava}.
     */
    public static final TypedKey<ParticleType> FALLING_LAVA = create("falling_lava");

    /**
     * Key for {@code minecraft:falling_nectar}.
     */
    public static final TypedKey<ParticleType> FALLING_NECTAR = create("falling_nectar");

    /**
     * Key for {@code minecraft:falling_obsidian_tear}.
     */
    public static final TypedKey<ParticleType> FALLING_OBSIDIAN_TEAR = create("falling_obsidian_tear");

    /**
     * Key for {@code minecraft:falling_spore_blossom}.
     */
    public static final TypedKey<ParticleType> FALLING_SPORE_BLOSSOM = create("falling_spore_blossom");

    /**
     * Key for {@code minecraft:falling_water}.
     */
    public static final TypedKey<ParticleType> FALLING_WATER = create("falling_water");

    /**
     * Key for {@code minecraft:firefly}.
     */
    public static final TypedKey<ParticleType> FIREFLY = create("firefly");

    /**
     * Key for {@code minecraft:firework}.
     */
    public static final TypedKey<ParticleType> FIREWORK = create("firework");

    /**
     * Key for {@code minecraft:fishing}.
     */
    public static final TypedKey<ParticleType> FISHING = create("fishing");

    /**
     * Key for {@code minecraft:flame}.
     */
    public static final TypedKey<ParticleType> FLAME = create("flame");

    /**
     * Key for {@code minecraft:flash}.
     */
    public static final TypedKey<ParticleType> FLASH = create("flash");

    /**
     * Key for {@code minecraft:freezing}.
     */
    public static final TypedKey<ParticleType> FREEZING = create("freezing");

    /**
     * Key for {@code minecraft:geyser}.
     */
    public static final TypedKey<ParticleType> GEYSER = create("geyser");

    /**
     * Key for {@code minecraft:geyser_base}.
     */
    public static final TypedKey<ParticleType> GEYSER_BASE = create("geyser_base");

    /**
     * Key for {@code minecraft:geyser_plume}.
     */
    public static final TypedKey<ParticleType> GEYSER_PLUME = create("geyser_plume");

    /**
     * Key for {@code minecraft:geyser_poof}.
     */
    public static final TypedKey<ParticleType> GEYSER_POOF = create("geyser_poof");

    /**
     * Key for {@code minecraft:glow}.
     */
    public static final TypedKey<ParticleType> GLOW = create("glow");

    /**
     * Key for {@code minecraft:glow_squid_ink}.
     */
    public static final TypedKey<ParticleType> GLOW_SQUID_INK = create("glow_squid_ink");

    /**
     * Key for {@code minecraft:gust}.
     */
    public static final TypedKey<ParticleType> GUST = create("gust");

    /**
     * Key for {@code minecraft:gust_emitter_large}.
     */
    public static final TypedKey<ParticleType> GUST_EMITTER_LARGE = create("gust_emitter_large");

    /**
     * Key for {@code minecraft:gust_emitter_small}.
     */
    public static final TypedKey<ParticleType> GUST_EMITTER_SMALL = create("gust_emitter_small");

    /**
     * Key for {@code minecraft:happy_villager}.
     */
    public static final TypedKey<ParticleType> HAPPY_VILLAGER = create("happy_villager");

    /**
     * Key for {@code minecraft:heart}.
     */
    public static final TypedKey<ParticleType> HEART = create("heart");

    /**
     * Key for {@code minecraft:infested}.
     */
    public static final TypedKey<ParticleType> INFESTED = create("infested");

    /**
     * Key for {@code minecraft:instant_effect}.
     */
    public static final TypedKey<ParticleType> INSTANT_EFFECT = create("instant_effect");

    /**
     * Key for {@code minecraft:item}.
     */
    public static final TypedKey<ParticleType> ITEM = create("item");

    /**
     * Key for {@code minecraft:item_cobweb}.
     */
    public static final TypedKey<ParticleType> ITEM_COBWEB = create("item_cobweb");

    /**
     * Key for {@code minecraft:item_slime}.
     */
    public static final TypedKey<ParticleType> ITEM_SLIME = create("item_slime");

    /**
     * Key for {@code minecraft:landing_honey}.
     */
    public static final TypedKey<ParticleType> LANDING_HONEY = create("landing_honey");

    /**
     * Key for {@code minecraft:landing_lava}.
     */
    public static final TypedKey<ParticleType> LANDING_LAVA = create("landing_lava");

    /**
     * Key for {@code minecraft:landing_obsidian_tear}.
     */
    public static final TypedKey<ParticleType> LANDING_OBSIDIAN_TEAR = create("landing_obsidian_tear");

    /**
     * Key for {@code minecraft:large_smoke}.
     */
    public static final TypedKey<ParticleType> LARGE_SMOKE = create("large_smoke");

    /**
     * Key for {@code minecraft:lava}.
     */
    public static final TypedKey<ParticleType> LAVA = create("lava");

    /**
     * Key for {@code minecraft:mycelium}.
     */
    public static final TypedKey<ParticleType> MYCELIUM = create("mycelium");

    /**
     * Key for {@code minecraft:nautilus}.
     */
    public static final TypedKey<ParticleType> NAUTILUS = create("nautilus");

    /**
     * Key for {@code minecraft:note}.
     */
    public static final TypedKey<ParticleType> NOTE = create("note");

    /**
     * Key for {@code minecraft:noxious_gas}.
     */
    public static final TypedKey<ParticleType> NOXIOUS_GAS = create("noxious_gas");

    /**
     * Key for {@code minecraft:noxious_gas_cloud}.
     */
    public static final TypedKey<ParticleType> NOXIOUS_GAS_CLOUD = create("noxious_gas_cloud");

    /**
     * Key for {@code minecraft:ominous_spawning}.
     */
    public static final TypedKey<ParticleType> OMINOUS_SPAWNING = create("ominous_spawning");

    /**
     * Key for {@code minecraft:orange_poplar_leaves}.
     */
    public static final TypedKey<ParticleType> ORANGE_POPLAR_LEAVES = create("orange_poplar_leaves");

    /**
     * Key for {@code minecraft:pale_oak_leaves}.
     */
    public static final TypedKey<ParticleType> PALE_OAK_LEAVES = create("pale_oak_leaves");

    /**
     * Key for {@code minecraft:pause_mob_growth}.
     */
    public static final TypedKey<ParticleType> PAUSE_MOB_GROWTH = create("pause_mob_growth");

    /**
     * Key for {@code minecraft:poof}.
     */
    public static final TypedKey<ParticleType> POOF = create("poof");

    /**
     * Key for {@code minecraft:portal}.
     */
    public static final TypedKey<ParticleType> PORTAL = create("portal");

    /**
     * Key for {@code minecraft:raid_omen}.
     */
    public static final TypedKey<ParticleType> RAID_OMEN = create("raid_omen");

    /**
     * Key for {@code minecraft:rain}.
     */
    public static final TypedKey<ParticleType> RAIN = create("rain");

    /**
     * Key for {@code minecraft:red_poplar_leaves}.
     */
    public static final TypedKey<ParticleType> RED_POPLAR_LEAVES = create("red_poplar_leaves");

    /**
     * Key for {@code minecraft:reset_mob_growth}.
     */
    public static final TypedKey<ParticleType> RESET_MOB_GROWTH = create("reset_mob_growth");

    /**
     * Key for {@code minecraft:reverse_portal}.
     */
    public static final TypedKey<ParticleType> REVERSE_PORTAL = create("reverse_portal");

    /**
     * Key for {@code minecraft:scrape}.
     */
    public static final TypedKey<ParticleType> SCRAPE = create("scrape");

    /**
     * Key for {@code minecraft:sculk_charge}.
     */
    public static final TypedKey<ParticleType> SCULK_CHARGE = create("sculk_charge");

    /**
     * Key for {@code minecraft:sculk_charge_pop}.
     */
    public static final TypedKey<ParticleType> SCULK_CHARGE_POP = create("sculk_charge_pop");

    /**
     * Key for {@code minecraft:sculk_soul}.
     */
    public static final TypedKey<ParticleType> SCULK_SOUL = create("sculk_soul");

    /**
     * Key for {@code minecraft:shriek}.
     */
    public static final TypedKey<ParticleType> SHRIEK = create("shriek");

    /**
     * Key for {@code minecraft:small_flame}.
     */
    public static final TypedKey<ParticleType> SMALL_FLAME = create("small_flame");

    /**
     * Key for {@code minecraft:small_gust}.
     */
    public static final TypedKey<ParticleType> SMALL_GUST = create("small_gust");

    /**
     * Key for {@code minecraft:smoke}.
     */
    public static final TypedKey<ParticleType> SMOKE = create("smoke");

    /**
     * Key for {@code minecraft:sneeze}.
     */
    public static final TypedKey<ParticleType> SNEEZE = create("sneeze");

    /**
     * Key for {@code minecraft:snowflake}.
     */
    public static final TypedKey<ParticleType> SNOWFLAKE = create("snowflake");

    /**
     * Key for {@code minecraft:sonic_boom}.
     */
    public static final TypedKey<ParticleType> SONIC_BOOM = create("sonic_boom");

    /**
     * Key for {@code minecraft:soul}.
     */
    public static final TypedKey<ParticleType> SOUL = create("soul");

    /**
     * Key for {@code minecraft:soul_fire_flame}.
     */
    public static final TypedKey<ParticleType> SOUL_FIRE_FLAME = create("soul_fire_flame");

    /**
     * Key for {@code minecraft:spit}.
     */
    public static final TypedKey<ParticleType> SPIT = create("spit");

    /**
     * Key for {@code minecraft:splash}.
     */
    public static final TypedKey<ParticleType> SPLASH = create("splash");

    /**
     * Key for {@code minecraft:spore_blossom_air}.
     */
    public static final TypedKey<ParticleType> SPORE_BLOSSOM_AIR = create("spore_blossom_air");

    /**
     * Key for {@code minecraft:squid_ink}.
     */
    public static final TypedKey<ParticleType> SQUID_INK = create("squid_ink");

    /**
     * Key for {@code minecraft:sulfur_bubbles}.
     */
    public static final TypedKey<ParticleType> SULFUR_BUBBLES = create("sulfur_bubbles");

    /**
     * Key for {@code minecraft:sulfur_cube_goo}.
     */
    public static final TypedKey<ParticleType> SULFUR_CUBE_GOO = create("sulfur_cube_goo");

    /**
     * Key for {@code minecraft:sweep_attack}.
     */
    public static final TypedKey<ParticleType> SWEEP_ATTACK = create("sweep_attack");

    /**
     * Key for {@code minecraft:tinted_leaves}.
     */
    public static final TypedKey<ParticleType> TINTED_LEAVES = create("tinted_leaves");

    /**
     * Key for {@code minecraft:totem_of_undying}.
     */
    public static final TypedKey<ParticleType> TOTEM_OF_UNDYING = create("totem_of_undying");

    /**
     * Key for {@code minecraft:trail}.
     */
    public static final TypedKey<ParticleType> TRAIL = create("trail");

    /**
     * Key for {@code minecraft:trial_omen}.
     */
    public static final TypedKey<ParticleType> TRIAL_OMEN = create("trial_omen");

    /**
     * Key for {@code minecraft:trial_spawner_detection}.
     */
    public static final TypedKey<ParticleType> TRIAL_SPAWNER_DETECTION = create("trial_spawner_detection");

    /**
     * Key for {@code minecraft:trial_spawner_detection_ominous}.
     */
    public static final TypedKey<ParticleType> TRIAL_SPAWNER_DETECTION_OMINOUS = create("trial_spawner_detection_ominous");

    /**
     * Key for {@code minecraft:underwater}.
     */
    public static final TypedKey<ParticleType> UNDERWATER = create("underwater");

    /**
     * Key for {@code minecraft:vault_connection}.
     */
    public static final TypedKey<ParticleType> VAULT_CONNECTION = create("vault_connection");

    /**
     * Key for {@code minecraft:vibration}.
     */
    public static final TypedKey<ParticleType> VIBRATION = create("vibration");

    /**
     * Key for {@code minecraft:warped_spore}.
     */
    public static final TypedKey<ParticleType> WARPED_SPORE = create("warped_spore");

    /**
     * Key for {@code minecraft:wax_off}.
     */
    public static final TypedKey<ParticleType> WAX_OFF = create("wax_off");

    /**
     * Key for {@code minecraft:wax_on}.
     */
    public static final TypedKey<ParticleType> WAX_ON = create("wax_on");

    /**
     * Key for {@code minecraft:white_ash}.
     */
    public static final TypedKey<ParticleType> WHITE_ASH = create("white_ash");

    /**
     * Key for {@code minecraft:white_smoke}.
     */
    public static final TypedKey<ParticleType> WHITE_SMOKE = create("white_smoke");

    /**
     * Key for {@code minecraft:witch}.
     */
    public static final TypedKey<ParticleType> WITCH = create("witch");

    /**
     * Key for {@code minecraft:yellow_poplar_leaves}.
     */
    public static final TypedKey<ParticleType> YELLOW_POPLAR_LEAVES = create("yellow_poplar_leaves");

    private static final List<TypedKey<ParticleType>> VALUES = List.of(
        ANGRY_VILLAGER,
        ASH,
        BLOCK,
        BLOCK_CRUMBLE,
        BLOCK_MARKER,
        BUBBLE,
        BUBBLE_COLUMN_UP,
        BUBBLE_POP,
        CAMPFIRE_COSY_SMOKE,
        CAMPFIRE_SIGNAL_SMOKE,
        CHERRY_LEAVES,
        CLOUD,
        COMPOSTER,
        COPPER_FIRE_FLAME,
        CRIMSON_SPORE,
        CRIT,
        CURRENT_DOWN,
        DAMAGE_INDICATOR,
        DOLPHIN,
        DRAGON_BREATH,
        DRIPPING_DRIPSTONE_LAVA,
        DRIPPING_DRIPSTONE_WATER,
        DRIPPING_HONEY,
        DRIPPING_LAVA,
        DRIPPING_OBSIDIAN_TEAR,
        DRIPPING_WATER,
        DUST,
        DUST_COLOR_TRANSITION,
        DUST_PILLAR,
        DUST_PLUME,
        EFFECT,
        EGG_CRACK,
        ELDER_GUARDIAN,
        ELECTRIC_SPARK,
        ENCHANT,
        ENCHANTED_HIT,
        END_ROD,
        ENTITY_EFFECT,
        EXPLOSION,
        EXPLOSION_EMITTER,
        FALLING_DRIPSTONE_LAVA,
        FALLING_DRIPSTONE_WATER,
        FALLING_DUST,
        FALLING_HONEY,
        FALLING_LAVA,
        FALLING_NECTAR,
        FALLING_OBSIDIAN_TEAR,
        FALLING_SPORE_BLOSSOM,
        FALLING_WATER,
        FIREFLY,
        FIREWORK,
        FISHING,
        FLAME,
        FLASH,
        FREEZING,
        GEYSER,
        GEYSER_BASE,
        GEYSER_PLUME,
        GEYSER_POOF,
        GLOW,
        GLOW_SQUID_INK,
        GUST,
        GUST_EMITTER_LARGE,
        GUST_EMITTER_SMALL,
        HAPPY_VILLAGER,
        HEART,
        INFESTED,
        INSTANT_EFFECT,
        ITEM,
        ITEM_COBWEB,
        ITEM_SLIME,
        LANDING_HONEY,
        LANDING_LAVA,
        LANDING_OBSIDIAN_TEAR,
        LARGE_SMOKE,
        LAVA,
        MYCELIUM,
        NAUTILUS,
        NOTE,
        NOXIOUS_GAS,
        NOXIOUS_GAS_CLOUD,
        OMINOUS_SPAWNING,
        ORANGE_POPLAR_LEAVES,
        PALE_OAK_LEAVES,
        PAUSE_MOB_GROWTH,
        POOF,
        PORTAL,
        RAID_OMEN,
        RAIN,
        RED_POPLAR_LEAVES,
        RESET_MOB_GROWTH,
        REVERSE_PORTAL,
        SCRAPE,
        SCULK_CHARGE,
        SCULK_CHARGE_POP,
        SCULK_SOUL,
        SHRIEK,
        SMALL_FLAME,
        SMALL_GUST,
        SMOKE,
        SNEEZE,
        SNOWFLAKE,
        SONIC_BOOM,
        SOUL,
        SOUL_FIRE_FLAME,
        SPIT,
        SPLASH,
        SPORE_BLOSSOM_AIR,
        SQUID_INK,
        SULFUR_BUBBLES,
        SULFUR_CUBE_GOO,
        SWEEP_ATTACK,
        TINTED_LEAVES,
        TOTEM_OF_UNDYING,
        TRAIL,
        TRIAL_OMEN,
        TRIAL_SPAWNER_DETECTION,
        TRIAL_SPAWNER_DETECTION_OMINOUS,
        UNDERWATER,
        VAULT_CONNECTION,
        VIBRATION,
        WARPED_SPORE,
        WAX_OFF,
        WAX_ON,
        WHITE_ASH,
        WHITE_SMOKE,
        WITCH,
        YELLOW_POPLAR_LEAVES
    );

    private ParticleTypeKeys() {
        throw new UnsupportedOperationException("ParticleTypeKeys cannot be instantiated.");
    }

    private static TypedKey<ParticleType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.PARTICLE_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<ParticleType>> values() {
        return VALUES.stream();
    }
}
