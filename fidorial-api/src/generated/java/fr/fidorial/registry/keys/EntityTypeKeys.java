package fr.fidorial.registry.keys;

import fr.fidorial.entity.EntityType;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:entity_type} registry.
 */
public final class EntityTypeKeys {
    /**
     * Key for {@code minecraft:acacia_boat}.
     */
    public static final TypedKey<EntityType> ACACIA_BOAT = create("acacia_boat");

    /**
     * Key for {@code minecraft:acacia_chest_boat}.
     */
    public static final TypedKey<EntityType> ACACIA_CHEST_BOAT = create("acacia_chest_boat");

    /**
     * Key for {@code minecraft:allay}.
     */
    public static final TypedKey<EntityType> ALLAY = create("allay");

    /**
     * Key for {@code minecraft:area_effect_cloud}.
     */
    public static final TypedKey<EntityType> AREA_EFFECT_CLOUD = create("area_effect_cloud");

    /**
     * Key for {@code minecraft:armadillo}.
     */
    public static final TypedKey<EntityType> ARMADILLO = create("armadillo");

    /**
     * Key for {@code minecraft:armor_stand}.
     */
    public static final TypedKey<EntityType> ARMOR_STAND = create("armor_stand");

    /**
     * Key for {@code minecraft:arrow}.
     */
    public static final TypedKey<EntityType> ARROW = create("arrow");

    /**
     * Key for {@code minecraft:axolotl}.
     */
    public static final TypedKey<EntityType> AXOLOTL = create("axolotl");

    /**
     * Key for {@code minecraft:bamboo_chest_raft}.
     */
    public static final TypedKey<EntityType> BAMBOO_CHEST_RAFT = create("bamboo_chest_raft");

    /**
     * Key for {@code minecraft:bamboo_raft}.
     */
    public static final TypedKey<EntityType> BAMBOO_RAFT = create("bamboo_raft");

    /**
     * Key for {@code minecraft:bat}.
     */
    public static final TypedKey<EntityType> BAT = create("bat");

    /**
     * Key for {@code minecraft:bee}.
     */
    public static final TypedKey<EntityType> BEE = create("bee");

    /**
     * Key for {@code minecraft:birch_boat}.
     */
    public static final TypedKey<EntityType> BIRCH_BOAT = create("birch_boat");

    /**
     * Key for {@code minecraft:birch_chest_boat}.
     */
    public static final TypedKey<EntityType> BIRCH_CHEST_BOAT = create("birch_chest_boat");

    /**
     * Key for {@code minecraft:blaze}.
     */
    public static final TypedKey<EntityType> BLAZE = create("blaze");

    /**
     * Key for {@code minecraft:block_display}.
     */
    public static final TypedKey<EntityType> BLOCK_DISPLAY = create("block_display");

    /**
     * Key for {@code minecraft:bogged}.
     */
    public static final TypedKey<EntityType> BOGGED = create("bogged");

    /**
     * Key for {@code minecraft:breeze}.
     */
    public static final TypedKey<EntityType> BREEZE = create("breeze");

    /**
     * Key for {@code minecraft:breeze_wind_charge}.
     */
    public static final TypedKey<EntityType> BREEZE_WIND_CHARGE = create("breeze_wind_charge");

    /**
     * Key for {@code minecraft:camel}.
     */
    public static final TypedKey<EntityType> CAMEL = create("camel");

    /**
     * Key for {@code minecraft:camel_husk}.
     */
    public static final TypedKey<EntityType> CAMEL_HUSK = create("camel_husk");

    /**
     * Key for {@code minecraft:cat}.
     */
    public static final TypedKey<EntityType> CAT = create("cat");

    /**
     * Key for {@code minecraft:cave_spider}.
     */
    public static final TypedKey<EntityType> CAVE_SPIDER = create("cave_spider");

    /**
     * Key for {@code minecraft:cherry_boat}.
     */
    public static final TypedKey<EntityType> CHERRY_BOAT = create("cherry_boat");

    /**
     * Key for {@code minecraft:cherry_chest_boat}.
     */
    public static final TypedKey<EntityType> CHERRY_CHEST_BOAT = create("cherry_chest_boat");

    /**
     * Key for {@code minecraft:chest_minecart}.
     */
    public static final TypedKey<EntityType> CHEST_MINECART = create("chest_minecart");

    /**
     * Key for {@code minecraft:chicken}.
     */
    public static final TypedKey<EntityType> CHICKEN = create("chicken");

    /**
     * Key for {@code minecraft:cod}.
     */
    public static final TypedKey<EntityType> COD = create("cod");

    /**
     * Key for {@code minecraft:command_block_minecart}.
     */
    public static final TypedKey<EntityType> COMMAND_BLOCK_MINECART = create("command_block_minecart");

    /**
     * Key for {@code minecraft:copper_golem}.
     */
    public static final TypedKey<EntityType> COPPER_GOLEM = create("copper_golem");

    /**
     * Key for {@code minecraft:cow}.
     */
    public static final TypedKey<EntityType> COW = create("cow");

    /**
     * Key for {@code minecraft:creaking}.
     */
    public static final TypedKey<EntityType> CREAKING = create("creaking");

    /**
     * Key for {@code minecraft:creeper}.
     */
    public static final TypedKey<EntityType> CREEPER = create("creeper");

    /**
     * Key for {@code minecraft:cushion}.
     */
    public static final TypedKey<EntityType> CUSHION = create("cushion");

    /**
     * Key for {@code minecraft:dark_oak_boat}.
     */
    public static final TypedKey<EntityType> DARK_OAK_BOAT = create("dark_oak_boat");

    /**
     * Key for {@code minecraft:dark_oak_chest_boat}.
     */
    public static final TypedKey<EntityType> DARK_OAK_CHEST_BOAT = create("dark_oak_chest_boat");

    /**
     * Key for {@code minecraft:dolphin}.
     */
    public static final TypedKey<EntityType> DOLPHIN = create("dolphin");

    /**
     * Key for {@code minecraft:donkey}.
     */
    public static final TypedKey<EntityType> DONKEY = create("donkey");

    /**
     * Key for {@code minecraft:dragon_fireball}.
     */
    public static final TypedKey<EntityType> DRAGON_FIREBALL = create("dragon_fireball");

    /**
     * Key for {@code minecraft:drowned}.
     */
    public static final TypedKey<EntityType> DROWNED = create("drowned");

    /**
     * Key for {@code minecraft:egg}.
     */
    public static final TypedKey<EntityType> EGG = create("egg");

    /**
     * Key for {@code minecraft:elder_guardian}.
     */
    public static final TypedKey<EntityType> ELDER_GUARDIAN = create("elder_guardian");

    /**
     * Key for {@code minecraft:end_crystal}.
     */
    public static final TypedKey<EntityType> END_CRYSTAL = create("end_crystal");

    /**
     * Key for {@code minecraft:ender_dragon}.
     */
    public static final TypedKey<EntityType> ENDER_DRAGON = create("ender_dragon");

    /**
     * Key for {@code minecraft:ender_pearl}.
     */
    public static final TypedKey<EntityType> ENDER_PEARL = create("ender_pearl");

    /**
     * Key for {@code minecraft:enderman}.
     */
    public static final TypedKey<EntityType> ENDERMAN = create("enderman");

    /**
     * Key for {@code minecraft:endermite}.
     */
    public static final TypedKey<EntityType> ENDERMITE = create("endermite");

    /**
     * Key for {@code minecraft:evoker}.
     */
    public static final TypedKey<EntityType> EVOKER = create("evoker");

    /**
     * Key for {@code minecraft:evoker_fangs}.
     */
    public static final TypedKey<EntityType> EVOKER_FANGS = create("evoker_fangs");

    /**
     * Key for {@code minecraft:experience_bottle}.
     */
    public static final TypedKey<EntityType> EXPERIENCE_BOTTLE = create("experience_bottle");

    /**
     * Key for {@code minecraft:experience_orb}.
     */
    public static final TypedKey<EntityType> EXPERIENCE_ORB = create("experience_orb");

    /**
     * Key for {@code minecraft:eye_of_ender}.
     */
    public static final TypedKey<EntityType> EYE_OF_ENDER = create("eye_of_ender");

    /**
     * Key for {@code minecraft:falling_block}.
     */
    public static final TypedKey<EntityType> FALLING_BLOCK = create("falling_block");

    /**
     * Key for {@code minecraft:fireball}.
     */
    public static final TypedKey<EntityType> FIREBALL = create("fireball");

    /**
     * Key for {@code minecraft:firework_rocket}.
     */
    public static final TypedKey<EntityType> FIREWORK_ROCKET = create("firework_rocket");

    /**
     * Key for {@code minecraft:fishing_bobber}.
     */
    public static final TypedKey<EntityType> FISHING_BOBBER = create("fishing_bobber");

    /**
     * Key for {@code minecraft:fox}.
     */
    public static final TypedKey<EntityType> FOX = create("fox");

    /**
     * Key for {@code minecraft:frog}.
     */
    public static final TypedKey<EntityType> FROG = create("frog");

    /**
     * Key for {@code minecraft:frostbite}.
     */
    public static final TypedKey<EntityType> FROSTBITE = create("frostbite");

    /**
     * Key for {@code minecraft:furnace_minecart}.
     */
    public static final TypedKey<EntityType> FURNACE_MINECART = create("furnace_minecart");

    /**
     * Key for {@code minecraft:ghast}.
     */
    public static final TypedKey<EntityType> GHAST = create("ghast");

    /**
     * Key for {@code minecraft:giant}.
     */
    public static final TypedKey<EntityType> GIANT = create("giant");

    /**
     * Key for {@code minecraft:glow_item_frame}.
     */
    public static final TypedKey<EntityType> GLOW_ITEM_FRAME = create("glow_item_frame");

    /**
     * Key for {@code minecraft:glow_squid}.
     */
    public static final TypedKey<EntityType> GLOW_SQUID = create("glow_squid");

    /**
     * Key for {@code minecraft:goat}.
     */
    public static final TypedKey<EntityType> GOAT = create("goat");

    /**
     * Key for {@code minecraft:guardian}.
     */
    public static final TypedKey<EntityType> GUARDIAN = create("guardian");

    /**
     * Key for {@code minecraft:happy_ghast}.
     */
    public static final TypedKey<EntityType> HAPPY_GHAST = create("happy_ghast");

    /**
     * Key for {@code minecraft:hoglin}.
     */
    public static final TypedKey<EntityType> HOGLIN = create("hoglin");

    /**
     * Key for {@code minecraft:hopper_minecart}.
     */
    public static final TypedKey<EntityType> HOPPER_MINECART = create("hopper_minecart");

    /**
     * Key for {@code minecraft:horse}.
     */
    public static final TypedKey<EntityType> HORSE = create("horse");

    /**
     * Key for {@code minecraft:husk}.
     */
    public static final TypedKey<EntityType> HUSK = create("husk");

    /**
     * Key for {@code minecraft:ice_ball}.
     */
    public static final TypedKey<EntityType> ICE_BALL = create("ice_ball");

    /**
     * Key for {@code minecraft:illusioner}.
     */
    public static final TypedKey<EntityType> ILLUSIONER = create("illusioner");

    /**
     * Key for {@code minecraft:interaction}.
     */
    public static final TypedKey<EntityType> INTERACTION = create("interaction");

    /**
     * Key for {@code minecraft:iron_golem}.
     */
    public static final TypedKey<EntityType> IRON_GOLEM = create("iron_golem");

    /**
     * Key for {@code minecraft:item}.
     */
    public static final TypedKey<EntityType> ITEM = create("item");

    /**
     * Key for {@code minecraft:item_display}.
     */
    public static final TypedKey<EntityType> ITEM_DISPLAY = create("item_display");

    /**
     * Key for {@code minecraft:item_frame}.
     */
    public static final TypedKey<EntityType> ITEM_FRAME = create("item_frame");

    /**
     * Key for {@code minecraft:jungle_boat}.
     */
    public static final TypedKey<EntityType> JUNGLE_BOAT = create("jungle_boat");

    /**
     * Key for {@code minecraft:jungle_chest_boat}.
     */
    public static final TypedKey<EntityType> JUNGLE_CHEST_BOAT = create("jungle_chest_boat");

    /**
     * Key for {@code minecraft:leash_knot}.
     */
    public static final TypedKey<EntityType> LEASH_KNOT = create("leash_knot");

    /**
     * Key for {@code minecraft:lightning_bolt}.
     */
    public static final TypedKey<EntityType> LIGHTNING_BOLT = create("lightning_bolt");

    /**
     * Key for {@code minecraft:lingering_potion}.
     */
    public static final TypedKey<EntityType> LINGERING_POTION = create("lingering_potion");

    /**
     * Key for {@code minecraft:llama}.
     */
    public static final TypedKey<EntityType> LLAMA = create("llama");

    /**
     * Key for {@code minecraft:llama_spit}.
     */
    public static final TypedKey<EntityType> LLAMA_SPIT = create("llama_spit");

    /**
     * Key for {@code minecraft:magma_cube}.
     */
    public static final TypedKey<EntityType> MAGMA_CUBE = create("magma_cube");

    /**
     * Key for {@code minecraft:mangrove_boat}.
     */
    public static final TypedKey<EntityType> MANGROVE_BOAT = create("mangrove_boat");

    /**
     * Key for {@code minecraft:mangrove_chest_boat}.
     */
    public static final TypedKey<EntityType> MANGROVE_CHEST_BOAT = create("mangrove_chest_boat");

    /**
     * Key for {@code minecraft:mannequin}.
     */
    public static final TypedKey<EntityType> MANNEQUIN = create("mannequin");

    /**
     * Key for {@code minecraft:marker}.
     */
    public static final TypedKey<EntityType> MARKER = create("marker");

    /**
     * Key for {@code minecraft:minecart}.
     */
    public static final TypedKey<EntityType> MINECART = create("minecart");

    /**
     * Key for {@code minecraft:mooshroom}.
     */
    public static final TypedKey<EntityType> MOOSHROOM = create("mooshroom");

    /**
     * Key for {@code minecraft:mule}.
     */
    public static final TypedKey<EntityType> MULE = create("mule");

    /**
     * Key for {@code minecraft:nautilus}.
     */
    public static final TypedKey<EntityType> NAUTILUS = create("nautilus");

    /**
     * Key for {@code minecraft:oak_boat}.
     */
    public static final TypedKey<EntityType> OAK_BOAT = create("oak_boat");

    /**
     * Key for {@code minecraft:oak_chest_boat}.
     */
    public static final TypedKey<EntityType> OAK_CHEST_BOAT = create("oak_chest_boat");

    /**
     * Key for {@code minecraft:ocelot}.
     */
    public static final TypedKey<EntityType> OCELOT = create("ocelot");

    /**
     * Key for {@code minecraft:ominous_item_spawner}.
     */
    public static final TypedKey<EntityType> OMINOUS_ITEM_SPAWNER = create("ominous_item_spawner");

    /**
     * Key for {@code minecraft:painting}.
     */
    public static final TypedKey<EntityType> PAINTING = create("painting");

    /**
     * Key for {@code minecraft:pale_oak_boat}.
     */
    public static final TypedKey<EntityType> PALE_OAK_BOAT = create("pale_oak_boat");

    /**
     * Key for {@code minecraft:pale_oak_chest_boat}.
     */
    public static final TypedKey<EntityType> PALE_OAK_CHEST_BOAT = create("pale_oak_chest_boat");

    /**
     * Key for {@code minecraft:panda}.
     */
    public static final TypedKey<EntityType> PANDA = create("panda");

    /**
     * Key for {@code minecraft:parched}.
     */
    public static final TypedKey<EntityType> PARCHED = create("parched");

    /**
     * Key for {@code minecraft:parrot}.
     */
    public static final TypedKey<EntityType> PARROT = create("parrot");

    /**
     * Key for {@code minecraft:phantom}.
     */
    public static final TypedKey<EntityType> PHANTOM = create("phantom");

    /**
     * Key for {@code minecraft:pig}.
     */
    public static final TypedKey<EntityType> PIG = create("pig");

    /**
     * Key for {@code minecraft:piglin}.
     */
    public static final TypedKey<EntityType> PIGLIN = create("piglin");

    /**
     * Key for {@code minecraft:piglin_brute}.
     */
    public static final TypedKey<EntityType> PIGLIN_BRUTE = create("piglin_brute");

    /**
     * Key for {@code minecraft:pillager}.
     */
    public static final TypedKey<EntityType> PILLAGER = create("pillager");

    /**
     * Key for {@code minecraft:player}.
     */
    public static final TypedKey<EntityType> PLAYER = create("player");

    /**
     * Key for {@code minecraft:polar_bear}.
     */
    public static final TypedKey<EntityType> POLAR_BEAR = create("polar_bear");

    /**
     * Key for {@code minecraft:poplar_boat}.
     */
    public static final TypedKey<EntityType> POPLAR_BOAT = create("poplar_boat");

    /**
     * Key for {@code minecraft:poplar_chest_boat}.
     */
    public static final TypedKey<EntityType> POPLAR_CHEST_BOAT = create("poplar_chest_boat");

    /**
     * Key for {@code minecraft:pufferfish}.
     */
    public static final TypedKey<EntityType> PUFFERFISH = create("pufferfish");

    /**
     * Key for {@code minecraft:rabbit}.
     */
    public static final TypedKey<EntityType> RABBIT = create("rabbit");

    /**
     * Key for {@code minecraft:ravager}.
     */
    public static final TypedKey<EntityType> RAVAGER = create("ravager");

    /**
     * Key for {@code minecraft:salmon}.
     */
    public static final TypedKey<EntityType> SALMON = create("salmon");

    /**
     * Key for {@code minecraft:sheep}.
     */
    public static final TypedKey<EntityType> SHEEP = create("sheep");

    /**
     * Key for {@code minecraft:shulker}.
     */
    public static final TypedKey<EntityType> SHULKER = create("shulker");

    /**
     * Key for {@code minecraft:shulker_bullet}.
     */
    public static final TypedKey<EntityType> SHULKER_BULLET = create("shulker_bullet");

    /**
     * Key for {@code minecraft:silverfish}.
     */
    public static final TypedKey<EntityType> SILVERFISH = create("silverfish");

    /**
     * Key for {@code minecraft:skeleton}.
     */
    public static final TypedKey<EntityType> SKELETON = create("skeleton");

    /**
     * Key for {@code minecraft:skeleton_horse}.
     */
    public static final TypedKey<EntityType> SKELETON_HORSE = create("skeleton_horse");

    /**
     * Key for {@code minecraft:slime}.
     */
    public static final TypedKey<EntityType> SLIME = create("slime");

    /**
     * Key for {@code minecraft:small_fireball}.
     */
    public static final TypedKey<EntityType> SMALL_FIREBALL = create("small_fireball");

    /**
     * Key for {@code minecraft:sniffer}.
     */
    public static final TypedKey<EntityType> SNIFFER = create("sniffer");

    /**
     * Key for {@code minecraft:snow_golem}.
     */
    public static final TypedKey<EntityType> SNOW_GOLEM = create("snow_golem");

    /**
     * Key for {@code minecraft:snowball}.
     */
    public static final TypedKey<EntityType> SNOWBALL = create("snowball");

    /**
     * Key for {@code minecraft:spawner_minecart}.
     */
    public static final TypedKey<EntityType> SPAWNER_MINECART = create("spawner_minecart");

    /**
     * Key for {@code minecraft:spectral_arrow}.
     */
    public static final TypedKey<EntityType> SPECTRAL_ARROW = create("spectral_arrow");

    /**
     * Key for {@code minecraft:spider}.
     */
    public static final TypedKey<EntityType> SPIDER = create("spider");

    /**
     * Key for {@code minecraft:splash_potion}.
     */
    public static final TypedKey<EntityType> SPLASH_POTION = create("splash_potion");

    /**
     * Key for {@code minecraft:spruce_boat}.
     */
    public static final TypedKey<EntityType> SPRUCE_BOAT = create("spruce_boat");

    /**
     * Key for {@code minecraft:spruce_chest_boat}.
     */
    public static final TypedKey<EntityType> SPRUCE_CHEST_BOAT = create("spruce_chest_boat");

    /**
     * Key for {@code minecraft:squid}.
     */
    public static final TypedKey<EntityType> SQUID = create("squid");

    /**
     * Key for {@code minecraft:stray}.
     */
    public static final TypedKey<EntityType> STRAY = create("stray");

    /**
     * Key for {@code minecraft:strider}.
     */
    public static final TypedKey<EntityType> STRIDER = create("strider");

    /**
     * Key for {@code minecraft:sulfur_cube}.
     */
    public static final TypedKey<EntityType> SULFUR_CUBE = create("sulfur_cube");

    /**
     * Key for {@code minecraft:tadpole}.
     */
    public static final TypedKey<EntityType> TADPOLE = create("tadpole");

    /**
     * Key for {@code minecraft:text_display}.
     */
    public static final TypedKey<EntityType> TEXT_DISPLAY = create("text_display");

    /**
     * Key for {@code minecraft:tnt}.
     */
    public static final TypedKey<EntityType> TNT = create("tnt");

    /**
     * Key for {@code minecraft:tnt_minecart}.
     */
    public static final TypedKey<EntityType> TNT_MINECART = create("tnt_minecart");

    /**
     * Key for {@code minecraft:trader_llama}.
     */
    public static final TypedKey<EntityType> TRADER_LLAMA = create("trader_llama");

    /**
     * Key for {@code minecraft:trident}.
     */
    public static final TypedKey<EntityType> TRIDENT = create("trident");

    /**
     * Key for {@code minecraft:tropical_fish}.
     */
    public static final TypedKey<EntityType> TROPICAL_FISH = create("tropical_fish");

    /**
     * Key for {@code minecraft:turtle}.
     */
    public static final TypedKey<EntityType> TURTLE = create("turtle");

    /**
     * Key for {@code minecraft:vex}.
     */
    public static final TypedKey<EntityType> VEX = create("vex");

    /**
     * Key for {@code minecraft:villager}.
     */
    public static final TypedKey<EntityType> VILLAGER = create("villager");

    /**
     * Key for {@code minecraft:vindicator}.
     */
    public static final TypedKey<EntityType> VINDICATOR = create("vindicator");

    /**
     * Key for {@code minecraft:wandering_trader}.
     */
    public static final TypedKey<EntityType> WANDERING_TRADER = create("wandering_trader");

    /**
     * Key for {@code minecraft:warden}.
     */
    public static final TypedKey<EntityType> WARDEN = create("warden");

    /**
     * Key for {@code minecraft:wind_charge}.
     */
    public static final TypedKey<EntityType> WIND_CHARGE = create("wind_charge");

    /**
     * Key for {@code minecraft:witch}.
     */
    public static final TypedKey<EntityType> WITCH = create("witch");

    /**
     * Key for {@code minecraft:wither}.
     */
    public static final TypedKey<EntityType> WITHER = create("wither");

    /**
     * Key for {@code minecraft:wither_skeleton}.
     */
    public static final TypedKey<EntityType> WITHER_SKELETON = create("wither_skeleton");

    /**
     * Key for {@code minecraft:wither_skull}.
     */
    public static final TypedKey<EntityType> WITHER_SKULL = create("wither_skull");

    /**
     * Key for {@code minecraft:wolf}.
     */
    public static final TypedKey<EntityType> WOLF = create("wolf");

    /**
     * Key for {@code minecraft:zoglin}.
     */
    public static final TypedKey<EntityType> ZOGLIN = create("zoglin");

    /**
     * Key for {@code minecraft:zombie}.
     */
    public static final TypedKey<EntityType> ZOMBIE = create("zombie");

    /**
     * Key for {@code minecraft:zombie_horse}.
     */
    public static final TypedKey<EntityType> ZOMBIE_HORSE = create("zombie_horse");

    /**
     * Key for {@code minecraft:zombie_nautilus}.
     */
    public static final TypedKey<EntityType> ZOMBIE_NAUTILUS = create("zombie_nautilus");

    /**
     * Key for {@code minecraft:zombie_villager}.
     */
    public static final TypedKey<EntityType> ZOMBIE_VILLAGER = create("zombie_villager");

    /**
     * Key for {@code minecraft:zombified_piglin}.
     */
    public static final TypedKey<EntityType> ZOMBIFIED_PIGLIN = create("zombified_piglin");

    private static final List<TypedKey<EntityType>> VALUES = List.of(
        ACACIA_BOAT,
        ACACIA_CHEST_BOAT,
        ALLAY,
        AREA_EFFECT_CLOUD,
        ARMADILLO,
        ARMOR_STAND,
        ARROW,
        AXOLOTL,
        BAMBOO_CHEST_RAFT,
        BAMBOO_RAFT,
        BAT,
        BEE,
        BIRCH_BOAT,
        BIRCH_CHEST_BOAT,
        BLAZE,
        BLOCK_DISPLAY,
        BOGGED,
        BREEZE,
        BREEZE_WIND_CHARGE,
        CAMEL,
        CAMEL_HUSK,
        CAT,
        CAVE_SPIDER,
        CHERRY_BOAT,
        CHERRY_CHEST_BOAT,
        CHEST_MINECART,
        CHICKEN,
        COD,
        COMMAND_BLOCK_MINECART,
        COPPER_GOLEM,
        COW,
        CREAKING,
        CREEPER,
        CUSHION,
        DARK_OAK_BOAT,
        DARK_OAK_CHEST_BOAT,
        DOLPHIN,
        DONKEY,
        DRAGON_FIREBALL,
        DROWNED,
        EGG,
        ELDER_GUARDIAN,
        END_CRYSTAL,
        ENDER_DRAGON,
        ENDER_PEARL,
        ENDERMAN,
        ENDERMITE,
        EVOKER,
        EVOKER_FANGS,
        EXPERIENCE_BOTTLE,
        EXPERIENCE_ORB,
        EYE_OF_ENDER,
        FALLING_BLOCK,
        FIREBALL,
        FIREWORK_ROCKET,
        FISHING_BOBBER,
        FOX,
        FROG,
        FROSTBITE,
        FURNACE_MINECART,
        GHAST,
        GIANT,
        GLOW_ITEM_FRAME,
        GLOW_SQUID,
        GOAT,
        GUARDIAN,
        HAPPY_GHAST,
        HOGLIN,
        HOPPER_MINECART,
        HORSE,
        HUSK,
        ICE_BALL,
        ILLUSIONER,
        INTERACTION,
        IRON_GOLEM,
        ITEM,
        ITEM_DISPLAY,
        ITEM_FRAME,
        JUNGLE_BOAT,
        JUNGLE_CHEST_BOAT,
        LEASH_KNOT,
        LIGHTNING_BOLT,
        LINGERING_POTION,
        LLAMA,
        LLAMA_SPIT,
        MAGMA_CUBE,
        MANGROVE_BOAT,
        MANGROVE_CHEST_BOAT,
        MANNEQUIN,
        MARKER,
        MINECART,
        MOOSHROOM,
        MULE,
        NAUTILUS,
        OAK_BOAT,
        OAK_CHEST_BOAT,
        OCELOT,
        OMINOUS_ITEM_SPAWNER,
        PAINTING,
        PALE_OAK_BOAT,
        PALE_OAK_CHEST_BOAT,
        PANDA,
        PARCHED,
        PARROT,
        PHANTOM,
        PIG,
        PIGLIN,
        PIGLIN_BRUTE,
        PILLAGER,
        PLAYER,
        POLAR_BEAR,
        POPLAR_BOAT,
        POPLAR_CHEST_BOAT,
        PUFFERFISH,
        RABBIT,
        RAVAGER,
        SALMON,
        SHEEP,
        SHULKER,
        SHULKER_BULLET,
        SILVERFISH,
        SKELETON,
        SKELETON_HORSE,
        SLIME,
        SMALL_FIREBALL,
        SNIFFER,
        SNOW_GOLEM,
        SNOWBALL,
        SPAWNER_MINECART,
        SPECTRAL_ARROW,
        SPIDER,
        SPLASH_POTION,
        SPRUCE_BOAT,
        SPRUCE_CHEST_BOAT,
        SQUID,
        STRAY,
        STRIDER,
        SULFUR_CUBE,
        TADPOLE,
        TEXT_DISPLAY,
        TNT,
        TNT_MINECART,
        TRADER_LLAMA,
        TRIDENT,
        TROPICAL_FISH,
        TURTLE,
        VEX,
        VILLAGER,
        VINDICATOR,
        WANDERING_TRADER,
        WARDEN,
        WIND_CHARGE,
        WITCH,
        WITHER,
        WITHER_SKELETON,
        WITHER_SKULL,
        WOLF,
        ZOGLIN,
        ZOMBIE,
        ZOMBIE_HORSE,
        ZOMBIE_NAUTILUS,
        ZOMBIE_VILLAGER,
        ZOMBIFIED_PIGLIN
    );

    private EntityTypeKeys() {
        throw new UnsupportedOperationException("EntityTypeKeys cannot be instantiated.");
    }

    private static TypedKey<EntityType> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.ENTITY_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<EntityType>> values() {
        return VALUES.stream();
    }
}
