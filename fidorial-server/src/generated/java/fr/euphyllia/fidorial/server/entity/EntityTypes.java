package fr.euphyllia.fidorial.server.entity;

import fr.fidorial.entity.EntityType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * Every entity type of the targeted Minecraft version.
 *
 * <p>Network IDs come from Mojang's registry report and hitboxes from
 * PrismarineJS's {@code minecraft-data}; do not edit. To change a spawn
 * category, edit {@code EntityCategories} in the registry generator and
 * regenerate.</p>
 */
public final class EntityTypes {
    private static final Map<Key, EntityType> BY_KEY = new ConcurrentHashMap<>();

    private static final Map<Key, Integer> NETWORK_IDS = new ConcurrentHashMap<>();

    /**
     * {@code minecraft:acacia_boat}, network ID {@code 0}.
     */
    public static final EntityType ACACIA_BOAT = vanilla("acacia_boat", EntityType.Category.MISC, 0, 1.375f, 0.5625f);

    /**
     * {@code minecraft:acacia_chest_boat}, network ID {@code 1}.
     */
    public static final EntityType ACACIA_CHEST_BOAT = vanilla("acacia_chest_boat", EntityType.Category.MISC, 1, 1.375f, 0.5625f);

    /**
     * {@code minecraft:allay}, network ID {@code 2}.
     */
    public static final EntityType ALLAY = vanilla("allay", EntityType.Category.CREATURE, 2, 0.35f, 0.6f);

    /**
     * {@code minecraft:area_effect_cloud}, network ID {@code 3}.
     */
    public static final EntityType AREA_EFFECT_CLOUD = vanilla("area_effect_cloud", EntityType.Category.MISC, 3, 6.0f, 0.5f);

    /**
     * {@code minecraft:armadillo}, network ID {@code 4}.
     */
    public static final EntityType ARMADILLO = vanilla("armadillo", EntityType.Category.CREATURE, 4, 0.7f, 0.65f);

    /**
     * {@code minecraft:armor_stand}, network ID {@code 5}.
     */
    public static final EntityType ARMOR_STAND = vanilla("armor_stand", EntityType.Category.MISC, 5, 0.5f, 1.975f);

    /**
     * {@code minecraft:arrow}, network ID {@code 6}.
     */
    public static final EntityType ARROW = vanilla("arrow", EntityType.Category.MISC, 6, 0.5f, 0.5f);

    /**
     * {@code minecraft:axolotl}, network ID {@code 7}.
     */
    public static final EntityType AXOLOTL = vanilla("axolotl", EntityType.Category.WATER_CREATURE, 7, 0.75f, 0.42f);

    /**
     * {@code minecraft:bamboo_chest_raft}, network ID {@code 8}.
     */
    public static final EntityType BAMBOO_CHEST_RAFT = vanilla("bamboo_chest_raft", EntityType.Category.MISC, 8, 1.375f, 0.5625f);

    /**
     * {@code minecraft:bamboo_raft}, network ID {@code 9}.
     */
    public static final EntityType BAMBOO_RAFT = vanilla("bamboo_raft", EntityType.Category.MISC, 9, 1.375f, 0.5625f);

    /**
     * {@code minecraft:bat}, network ID {@code 10}.
     */
    public static final EntityType BAT = vanilla("bat", EntityType.Category.AMBIENT, 10, 0.5f, 0.9f);

    /**
     * {@code minecraft:bee}, network ID {@code 11}.
     */
    public static final EntityType BEE = vanilla("bee", EntityType.Category.CREATURE, 11, 0.55f, 0.5f);

    /**
     * {@code minecraft:birch_boat}, network ID {@code 12}.
     */
    public static final EntityType BIRCH_BOAT = vanilla("birch_boat", EntityType.Category.MISC, 12, 1.375f, 0.5625f);

    /**
     * {@code minecraft:birch_chest_boat}, network ID {@code 13}.
     */
    public static final EntityType BIRCH_CHEST_BOAT = vanilla("birch_chest_boat", EntityType.Category.MISC, 13, 1.375f, 0.5625f);

    /**
     * {@code minecraft:blaze}, network ID {@code 14}.
     */
    public static final EntityType BLAZE = vanilla("blaze", EntityType.Category.MONSTER, 14, 0.6f, 1.8f);

    /**
     * {@code minecraft:block_display}, network ID {@code 15}.
     */
    public static final EntityType BLOCK_DISPLAY = vanilla("block_display", EntityType.Category.MISC, 15, 0.0f, 0.0f);

    /**
     * {@code minecraft:bogged}, network ID {@code 16}.
     */
    public static final EntityType BOGGED = vanilla("bogged", EntityType.Category.MONSTER, 16, 0.6f, 1.99f);

    /**
     * {@code minecraft:breeze}, network ID {@code 17}.
     */
    public static final EntityType BREEZE = vanilla("breeze", EntityType.Category.MONSTER, 17, 0.6f, 1.77f);

    /**
     * {@code minecraft:breeze_wind_charge}, network ID {@code 18}.
     */
    public static final EntityType BREEZE_WIND_CHARGE = vanilla("breeze_wind_charge", EntityType.Category.MISC, 18, 0.3125f, 0.3125f);

    /**
     * {@code minecraft:camel}, network ID {@code 19}.
     */
    public static final EntityType CAMEL = vanilla("camel", EntityType.Category.CREATURE, 19, 1.7f, 2.375f);

    /**
     * {@code minecraft:camel_husk}, network ID {@code 20}.
     */
    public static final EntityType CAMEL_HUSK = vanilla("camel_husk", EntityType.Category.MONSTER, 20, 1.7f, 2.375f);

    /**
     * {@code minecraft:cat}, network ID {@code 21}.
     */
    public static final EntityType CAT = vanilla("cat", EntityType.Category.CREATURE, 21, 0.6f, 0.7f);

    /**
     * {@code minecraft:cave_spider}, network ID {@code 22}.
     */
    public static final EntityType CAVE_SPIDER = vanilla("cave_spider", EntityType.Category.MONSTER, 22, 0.7f, 0.5f);

    /**
     * {@code minecraft:cherry_boat}, network ID {@code 23}.
     */
    public static final EntityType CHERRY_BOAT = vanilla("cherry_boat", EntityType.Category.MISC, 23, 1.375f, 0.5625f);

    /**
     * {@code minecraft:cherry_chest_boat}, network ID {@code 24}.
     */
    public static final EntityType CHERRY_CHEST_BOAT = vanilla("cherry_chest_boat", EntityType.Category.MISC, 24, 1.375f, 0.5625f);

    /**
     * {@code minecraft:chest_minecart}, network ID {@code 25}.
     */
    public static final EntityType CHEST_MINECART = vanilla("chest_minecart", EntityType.Category.MISC, 25, 0.98f, 0.7f);

    /**
     * {@code minecraft:chicken}, network ID {@code 26}.
     */
    public static final EntityType CHICKEN = vanilla("chicken", EntityType.Category.CREATURE, 26, 0.4f, 0.7f);

    /**
     * {@code minecraft:cod}, network ID {@code 27}.
     */
    public static final EntityType COD = vanilla("cod", EntityType.Category.WATER_CREATURE, 27, 0.5f, 0.3f);

    /**
     * {@code minecraft:command_block_minecart}, network ID {@code 29}.
     */
    public static final EntityType COMMAND_BLOCK_MINECART = vanilla("command_block_minecart", EntityType.Category.MISC, 29, 0.98f, 0.7f);

    /**
     * {@code minecraft:copper_golem}, network ID {@code 28}.
     */
    public static final EntityType COPPER_GOLEM = vanilla("copper_golem", EntityType.Category.MISC, 28, 0.49f, 0.98f);

    /**
     * {@code minecraft:cow}, network ID {@code 30}.
     */
    public static final EntityType COW = vanilla("cow", EntityType.Category.CREATURE, 30, 0.9f, 1.4f);

    /**
     * {@code minecraft:creaking}, network ID {@code 31}.
     */
    public static final EntityType CREAKING = vanilla("creaking", EntityType.Category.MONSTER, 31, 0.9f, 2.7f);

    /**
     * {@code minecraft:creeper}, network ID {@code 32}.
     */
    public static final EntityType CREEPER = vanilla("creeper", EntityType.Category.MONSTER, 32, 0.6f, 1.7f);

    /**
     * {@code minecraft:cushion}, network ID {@code 33}.
     */
    public static final EntityType CUSHION = vanilla("cushion", EntityType.Category.MISC, 33, 1.0f, 0.25f);

    /**
     * {@code minecraft:dark_oak_boat}, network ID {@code 34}.
     */
    public static final EntityType DARK_OAK_BOAT = vanilla("dark_oak_boat", EntityType.Category.MISC, 34, 1.375f, 0.5625f);

    /**
     * {@code minecraft:dark_oak_chest_boat}, network ID {@code 35}.
     */
    public static final EntityType DARK_OAK_CHEST_BOAT = vanilla("dark_oak_chest_boat", EntityType.Category.MISC, 35, 1.375f, 0.5625f);

    /**
     * {@code minecraft:dolphin}, network ID {@code 36}.
     */
    public static final EntityType DOLPHIN = vanilla("dolphin", EntityType.Category.WATER_CREATURE, 36, 0.9f, 0.6f);

    /**
     * {@code minecraft:donkey}, network ID {@code 37}.
     */
    public static final EntityType DONKEY = vanilla("donkey", EntityType.Category.CREATURE, 37, 1.3964844f, 1.5f);

    /**
     * {@code minecraft:dragon_fireball}, network ID {@code 38}.
     */
    public static final EntityType DRAGON_FIREBALL = vanilla("dragon_fireball", EntityType.Category.MISC, 38, 1.0f, 1.0f);

    /**
     * {@code minecraft:drowned}, network ID {@code 39}.
     */
    public static final EntityType DROWNED = vanilla("drowned", EntityType.Category.MONSTER, 39, 0.6f, 1.95f);

    /**
     * {@code minecraft:egg}, network ID {@code 40}.
     */
    public static final EntityType EGG = vanilla("egg", EntityType.Category.MISC, 40, 0.25f, 0.25f);

    /**
     * {@code minecraft:elder_guardian}, network ID {@code 41}.
     */
    public static final EntityType ELDER_GUARDIAN = vanilla("elder_guardian", EntityType.Category.MONSTER, 41, 1.9975f, 1.9975f);

    /**
     * {@code minecraft:end_crystal}, network ID {@code 46}.
     */
    public static final EntityType END_CRYSTAL = vanilla("end_crystal", EntityType.Category.MISC, 46, 2.0f, 2.0f);

    /**
     * {@code minecraft:ender_dragon}, network ID {@code 44}.
     */
    public static final EntityType ENDER_DRAGON = vanilla("ender_dragon", EntityType.Category.MONSTER, 44, 16.0f, 8.0f);

    /**
     * {@code minecraft:ender_pearl}, network ID {@code 45}.
     */
    public static final EntityType ENDER_PEARL = vanilla("ender_pearl", EntityType.Category.MISC, 45, 0.25f, 0.25f);

    /**
     * {@code minecraft:enderman}, network ID {@code 42}.
     */
    public static final EntityType ENDERMAN = vanilla("enderman", EntityType.Category.MONSTER, 42, 0.6f, 2.9f);

    /**
     * {@code minecraft:endermite}, network ID {@code 43}.
     */
    public static final EntityType ENDERMITE = vanilla("endermite", EntityType.Category.MONSTER, 43, 0.4f, 0.3f);

    /**
     * {@code minecraft:evoker}, network ID {@code 47}.
     */
    public static final EntityType EVOKER = vanilla("evoker", EntityType.Category.MONSTER, 47, 0.6f, 1.95f);

    /**
     * {@code minecraft:evoker_fangs}, network ID {@code 48}.
     */
    public static final EntityType EVOKER_FANGS = vanilla("evoker_fangs", EntityType.Category.MISC, 48, 0.5f, 0.8f);

    /**
     * {@code minecraft:experience_bottle}, network ID {@code 49}.
     */
    public static final EntityType EXPERIENCE_BOTTLE = vanilla("experience_bottle", EntityType.Category.MISC, 49, 0.25f, 0.25f);

    /**
     * {@code minecraft:experience_orb}, network ID {@code 50}.
     */
    public static final EntityType EXPERIENCE_ORB = vanilla("experience_orb", EntityType.Category.MISC, 50, 0.5f, 0.5f);

    /**
     * {@code minecraft:eye_of_ender}, network ID {@code 51}.
     */
    public static final EntityType EYE_OF_ENDER = vanilla("eye_of_ender", EntityType.Category.MISC, 51, 0.25f, 0.25f);

    /**
     * {@code minecraft:falling_block}, network ID {@code 52}.
     */
    public static final EntityType FALLING_BLOCK = vanilla("falling_block", EntityType.Category.MISC, 52, 0.98f, 0.98f);

    /**
     * {@code minecraft:fireball}, network ID {@code 53}.
     */
    public static final EntityType FIREBALL = vanilla("fireball", EntityType.Category.MISC, 53, 1.0f, 1.0f);

    /**
     * {@code minecraft:firework_rocket}, network ID {@code 54}.
     */
    public static final EntityType FIREWORK_ROCKET = vanilla("firework_rocket", EntityType.Category.MISC, 54, 0.25f, 0.25f);

    /**
     * {@code minecraft:fishing_bobber}, network ID {@code 160}.
     */
    public static final EntityType FISHING_BOBBER = vanilla("fishing_bobber", EntityType.Category.MISC, 160, 0.25f, 0.25f);

    /**
     * {@code minecraft:fox}, network ID {@code 55}.
     */
    public static final EntityType FOX = vanilla("fox", EntityType.Category.CREATURE, 55, 0.6f, 0.7f);

    /**
     * {@code minecraft:frog}, network ID {@code 56}.
     */
    public static final EntityType FROG = vanilla("frog", EntityType.Category.CREATURE, 56, 0.5f, 0.5f);

    /**
     * {@code minecraft:furnace_minecart}, network ID {@code 57}.
     */
    public static final EntityType FURNACE_MINECART = vanilla("furnace_minecart", EntityType.Category.MISC, 57, 0.98f, 0.7f);

    /**
     * {@code minecraft:ghast}, network ID {@code 58}.
     */
    public static final EntityType GHAST = vanilla("ghast", EntityType.Category.MONSTER, 58, 4.0f, 4.0f);

    /**
     * {@code minecraft:giant}, network ID {@code 60}.
     */
    public static final EntityType GIANT = vanilla("giant", EntityType.Category.MONSTER, 60, 3.6f, 12.0f);

    /**
     * {@code minecraft:glow_item_frame}, network ID {@code 61}.
     */
    public static final EntityType GLOW_ITEM_FRAME = vanilla("glow_item_frame", EntityType.Category.MISC, 61, 0.5f, 0.5f);

    /**
     * {@code minecraft:glow_squid}, network ID {@code 62}.
     */
    public static final EntityType GLOW_SQUID = vanilla("glow_squid", EntityType.Category.WATER_CREATURE, 62, 0.8f, 0.8f);

    /**
     * {@code minecraft:goat}, network ID {@code 63}.
     */
    public static final EntityType GOAT = vanilla("goat", EntityType.Category.CREATURE, 63, 0.9f, 1.3f);

    /**
     * {@code minecraft:guardian}, network ID {@code 64}.
     */
    public static final EntityType GUARDIAN = vanilla("guardian", EntityType.Category.MONSTER, 64, 0.85f, 0.85f);

    /**
     * {@code minecraft:happy_ghast}, network ID {@code 59}.
     */
    public static final EntityType HAPPY_GHAST = vanilla("happy_ghast", EntityType.Category.CREATURE, 59, 4.0f, 4.0f);

    /**
     * {@code minecraft:hoglin}, network ID {@code 65}.
     */
    public static final EntityType HOGLIN = vanilla("hoglin", EntityType.Category.MONSTER, 65, 1.3964844f, 1.4f);

    /**
     * {@code minecraft:hopper_minecart}, network ID {@code 66}.
     */
    public static final EntityType HOPPER_MINECART = vanilla("hopper_minecart", EntityType.Category.MISC, 66, 0.98f, 0.7f);

    /**
     * {@code minecraft:horse}, network ID {@code 67}.
     */
    public static final EntityType HORSE = vanilla("horse", EntityType.Category.CREATURE, 67, 1.3964844f, 1.6f);

    /**
     * {@code minecraft:husk}, network ID {@code 68}.
     */
    public static final EntityType HUSK = vanilla("husk", EntityType.Category.MONSTER, 68, 0.6f, 1.95f);

    /**
     * {@code minecraft:illusioner}, network ID {@code 69}.
     */
    public static final EntityType ILLUSIONER = vanilla("illusioner", EntityType.Category.MONSTER, 69, 0.6f, 1.95f);

    /**
     * {@code minecraft:interaction}, network ID {@code 70}.
     */
    public static final EntityType INTERACTION = vanilla("interaction", EntityType.Category.MISC, 70, 0.0f, 0.0f);

    /**
     * {@code minecraft:iron_golem}, network ID {@code 71}.
     */
    public static final EntityType IRON_GOLEM = vanilla("iron_golem", EntityType.Category.MISC, 71, 1.4f, 2.7f);

    /**
     * {@code minecraft:item}, network ID {@code 72}.
     */
    public static final EntityType ITEM = vanilla("item", EntityType.Category.MISC, 72, 0.25f, 0.25f);

    /**
     * {@code minecraft:item_display}, network ID {@code 73}.
     */
    public static final EntityType ITEM_DISPLAY = vanilla("item_display", EntityType.Category.MISC, 73, 0.0f, 0.0f);

    /**
     * {@code minecraft:item_frame}, network ID {@code 74}.
     */
    public static final EntityType ITEM_FRAME = vanilla("item_frame", EntityType.Category.MISC, 74, 0.5f, 0.5f);

    /**
     * {@code minecraft:jungle_boat}, network ID {@code 75}.
     */
    public static final EntityType JUNGLE_BOAT = vanilla("jungle_boat", EntityType.Category.MISC, 75, 1.375f, 0.5625f);

    /**
     * {@code minecraft:jungle_chest_boat}, network ID {@code 76}.
     */
    public static final EntityType JUNGLE_CHEST_BOAT = vanilla("jungle_chest_boat", EntityType.Category.MISC, 76, 1.375f, 0.5625f);

    /**
     * {@code minecraft:leash_knot}, network ID {@code 77}.
     */
    public static final EntityType LEASH_KNOT = vanilla("leash_knot", EntityType.Category.MISC, 77, 0.375f, 0.5f);

    /**
     * {@code minecraft:lightning_bolt}, network ID {@code 78}.
     */
    public static final EntityType LIGHTNING_BOLT = vanilla("lightning_bolt", EntityType.Category.MISC, 78, 0.0f, 0.0f);

    /**
     * {@code minecraft:lingering_potion}, network ID {@code 109}.
     */
    public static final EntityType LINGERING_POTION = vanilla("lingering_potion", EntityType.Category.MISC, 109, 0.25f, 0.25f);

    /**
     * {@code minecraft:llama}, network ID {@code 79}.
     */
    public static final EntityType LLAMA = vanilla("llama", EntityType.Category.CREATURE, 79, 0.9f, 1.87f);

    /**
     * {@code minecraft:llama_spit}, network ID {@code 80}.
     */
    public static final EntityType LLAMA_SPIT = vanilla("llama_spit", EntityType.Category.MISC, 80, 0.25f, 0.25f);

    /**
     * {@code minecraft:magma_cube}, network ID {@code 81}.
     */
    public static final EntityType MAGMA_CUBE = vanilla("magma_cube", EntityType.Category.MONSTER, 81, 0.52f, 0.52f);

    /**
     * {@code minecraft:mangrove_boat}, network ID {@code 82}.
     */
    public static final EntityType MANGROVE_BOAT = vanilla("mangrove_boat", EntityType.Category.MISC, 82, 1.375f, 0.5625f);

    /**
     * {@code minecraft:mangrove_chest_boat}, network ID {@code 83}.
     */
    public static final EntityType MANGROVE_CHEST_BOAT = vanilla("mangrove_chest_boat", EntityType.Category.MISC, 83, 1.375f, 0.5625f);

    /**
     * {@code minecraft:mannequin}, network ID {@code 84}.
     */
    public static final EntityType MANNEQUIN = vanilla("mannequin", EntityType.Category.MISC, 84, 0.6f, 1.8f);

    /**
     * {@code minecraft:marker}, network ID {@code 85}.
     */
    public static final EntityType MARKER = vanilla("marker", EntityType.Category.MISC, 85, 0.0f, 0.0f);

    /**
     * {@code minecraft:minecart}, network ID {@code 86}.
     */
    public static final EntityType MINECART = vanilla("minecart", EntityType.Category.MISC, 86, 0.98f, 0.7f);

    /**
     * {@code minecraft:mooshroom}, network ID {@code 87}.
     */
    public static final EntityType MOOSHROOM = vanilla("mooshroom", EntityType.Category.CREATURE, 87, 0.9f, 1.4f);

    /**
     * {@code minecraft:mule}, network ID {@code 88}.
     */
    public static final EntityType MULE = vanilla("mule", EntityType.Category.CREATURE, 88, 1.3964844f, 1.6f);

    /**
     * {@code minecraft:nautilus}, network ID {@code 89}.
     */
    public static final EntityType NAUTILUS = vanilla("nautilus", EntityType.Category.WATER_CREATURE, 89, 0.875f, 0.95f);

    /**
     * {@code minecraft:oak_boat}, network ID {@code 90}.
     */
    public static final EntityType OAK_BOAT = vanilla("oak_boat", EntityType.Category.MISC, 90, 1.375f, 0.5625f);

    /**
     * {@code minecraft:oak_chest_boat}, network ID {@code 91}.
     */
    public static final EntityType OAK_CHEST_BOAT = vanilla("oak_chest_boat", EntityType.Category.MISC, 91, 1.375f, 0.5625f);

    /**
     * {@code minecraft:ocelot}, network ID {@code 92}.
     */
    public static final EntityType OCELOT = vanilla("ocelot", EntityType.Category.CREATURE, 92, 0.6f, 0.7f);

    /**
     * {@code minecraft:ominous_item_spawner}, network ID {@code 93}.
     */
    public static final EntityType OMINOUS_ITEM_SPAWNER = vanilla("ominous_item_spawner", EntityType.Category.MISC, 93, 0.25f, 0.25f);

    /**
     * {@code minecraft:painting}, network ID {@code 94}.
     */
    public static final EntityType PAINTING = vanilla("painting", EntityType.Category.MISC, 94, 0.5f, 0.5f);

    /**
     * {@code minecraft:pale_oak_boat}, network ID {@code 95}.
     */
    public static final EntityType PALE_OAK_BOAT = vanilla("pale_oak_boat", EntityType.Category.MISC, 95, 1.375f, 0.5625f);

    /**
     * {@code minecraft:pale_oak_chest_boat}, network ID {@code 96}.
     */
    public static final EntityType PALE_OAK_CHEST_BOAT = vanilla("pale_oak_chest_boat", EntityType.Category.MISC, 96, 1.375f, 0.5625f);

    /**
     * {@code minecraft:panda}, network ID {@code 97}.
     */
    public static final EntityType PANDA = vanilla("panda", EntityType.Category.CREATURE, 97, 1.3f, 1.25f);

    /**
     * {@code minecraft:parched}, network ID {@code 98}.
     */
    public static final EntityType PARCHED = vanilla("parched", EntityType.Category.MONSTER, 98, 0.6f, 1.99f);

    /**
     * {@code minecraft:parrot}, network ID {@code 99}.
     */
    public static final EntityType PARROT = vanilla("parrot", EntityType.Category.CREATURE, 99, 0.5f, 0.9f);

    /**
     * {@code minecraft:phantom}, network ID {@code 100}.
     */
    public static final EntityType PHANTOM = vanilla("phantom", EntityType.Category.MONSTER, 100, 0.9f, 0.5f);

    /**
     * {@code minecraft:pig}, network ID {@code 101}.
     */
    public static final EntityType PIG = vanilla("pig", EntityType.Category.CREATURE, 101, 0.9f, 0.9f);

    /**
     * {@code minecraft:piglin}, network ID {@code 102}.
     */
    public static final EntityType PIGLIN = vanilla("piglin", EntityType.Category.MONSTER, 102, 0.6f, 1.95f);

    /**
     * {@code minecraft:piglin_brute}, network ID {@code 103}.
     */
    public static final EntityType PIGLIN_BRUTE = vanilla("piglin_brute", EntityType.Category.MONSTER, 103, 0.6f, 1.95f);

    /**
     * {@code minecraft:pillager}, network ID {@code 104}.
     */
    public static final EntityType PILLAGER = vanilla("pillager", EntityType.Category.MONSTER, 104, 0.6f, 1.95f);

    /**
     * {@code minecraft:player}, network ID {@code 159}.
     */
    public static final EntityType PLAYER = vanilla("player", EntityType.Category.PLAYER, 159, 0.6f, 1.8f);

    /**
     * {@code minecraft:polar_bear}, network ID {@code 105}.
     */
    public static final EntityType POLAR_BEAR = vanilla("polar_bear", EntityType.Category.CREATURE, 105, 1.4f, 1.4f);

    /**
     * {@code minecraft:poplar_boat}, network ID {@code 106}.
     */
    public static final EntityType POPLAR_BOAT = vanilla("poplar_boat", EntityType.Category.MISC, 106, 1.375f, 0.5625f);

    /**
     * {@code minecraft:poplar_chest_boat}, network ID {@code 107}.
     */
    public static final EntityType POPLAR_CHEST_BOAT = vanilla("poplar_chest_boat", EntityType.Category.MISC, 107, 1.375f, 0.5625f);

    /**
     * {@code minecraft:pufferfish}, network ID {@code 110}.
     */
    public static final EntityType PUFFERFISH = vanilla("pufferfish", EntityType.Category.WATER_CREATURE, 110, 0.7f, 0.7f);

    /**
     * {@code minecraft:rabbit}, network ID {@code 111}.
     */
    public static final EntityType RABBIT = vanilla("rabbit", EntityType.Category.CREATURE, 111, 0.49f, 0.6f);

    /**
     * {@code minecraft:ravager}, network ID {@code 112}.
     */
    public static final EntityType RAVAGER = vanilla("ravager", EntityType.Category.MONSTER, 112, 1.95f, 2.2f);

    /**
     * {@code minecraft:salmon}, network ID {@code 113}.
     */
    public static final EntityType SALMON = vanilla("salmon", EntityType.Category.WATER_CREATURE, 113, 0.7f, 0.4f);

    /**
     * {@code minecraft:sheep}, network ID {@code 114}.
     */
    public static final EntityType SHEEP = vanilla("sheep", EntityType.Category.CREATURE, 114, 0.9f, 1.3f);

    /**
     * {@code minecraft:shulker}, network ID {@code 115}.
     */
    public static final EntityType SHULKER = vanilla("shulker", EntityType.Category.MONSTER, 115, 1.0f, 1.0f);

    /**
     * {@code minecraft:shulker_bullet}, network ID {@code 116}.
     */
    public static final EntityType SHULKER_BULLET = vanilla("shulker_bullet", EntityType.Category.MISC, 116, 0.3125f, 0.3125f);

    /**
     * {@code minecraft:silverfish}, network ID {@code 117}.
     */
    public static final EntityType SILVERFISH = vanilla("silverfish", EntityType.Category.MONSTER, 117, 0.4f, 0.3f);

    /**
     * {@code minecraft:skeleton}, network ID {@code 118}.
     */
    public static final EntityType SKELETON = vanilla("skeleton", EntityType.Category.MONSTER, 118, 0.6f, 1.99f);

    /**
     * {@code minecraft:skeleton_horse}, network ID {@code 119}.
     */
    public static final EntityType SKELETON_HORSE = vanilla("skeleton_horse", EntityType.Category.CREATURE, 119, 1.3964844f, 1.6f);

    /**
     * {@code minecraft:slime}, network ID {@code 120}.
     */
    public static final EntityType SLIME = vanilla("slime", EntityType.Category.MONSTER, 120, 0.52f, 0.52f);

    /**
     * {@code minecraft:small_fireball}, network ID {@code 121}.
     */
    public static final EntityType SMALL_FIREBALL = vanilla("small_fireball", EntityType.Category.MISC, 121, 0.3125f, 0.3125f);

    /**
     * {@code minecraft:sniffer}, network ID {@code 122}.
     */
    public static final EntityType SNIFFER = vanilla("sniffer", EntityType.Category.CREATURE, 122, 1.9f, 1.75f);

    /**
     * {@code minecraft:snow_golem}, network ID {@code 124}.
     */
    public static final EntityType SNOW_GOLEM = vanilla("snow_golem", EntityType.Category.MISC, 124, 0.7f, 1.9f);

    /**
     * {@code minecraft:snowball}, network ID {@code 123}.
     */
    public static final EntityType SNOWBALL = vanilla("snowball", EntityType.Category.MISC, 123, 0.25f, 0.25f);

    /**
     * {@code minecraft:spawner_minecart}, network ID {@code 125}.
     */
    public static final EntityType SPAWNER_MINECART = vanilla("spawner_minecart", EntityType.Category.MISC, 125, 0.98f, 0.7f);

    /**
     * {@code minecraft:spectral_arrow}, network ID {@code 126}.
     */
    public static final EntityType SPECTRAL_ARROW = vanilla("spectral_arrow", EntityType.Category.MISC, 126, 0.5f, 0.5f);

    /**
     * {@code minecraft:spider}, network ID {@code 127}.
     */
    public static final EntityType SPIDER = vanilla("spider", EntityType.Category.MONSTER, 127, 1.4f, 0.9f);

    /**
     * {@code minecraft:splash_potion}, network ID {@code 108}.
     */
    public static final EntityType SPLASH_POTION = vanilla("splash_potion", EntityType.Category.MISC, 108, 0.25f, 0.25f);

    /**
     * {@code minecraft:spruce_boat}, network ID {@code 128}.
     */
    public static final EntityType SPRUCE_BOAT = vanilla("spruce_boat", EntityType.Category.MISC, 128, 1.375f, 0.5625f);

    /**
     * {@code minecraft:spruce_chest_boat}, network ID {@code 129}.
     */
    public static final EntityType SPRUCE_CHEST_BOAT = vanilla("spruce_chest_boat", EntityType.Category.MISC, 129, 1.375f, 0.5625f);

    /**
     * {@code minecraft:squid}, network ID {@code 130}.
     */
    public static final EntityType SQUID = vanilla("squid", EntityType.Category.WATER_CREATURE, 130, 0.8f, 0.8f);

    /**
     * {@code minecraft:stray}, network ID {@code 131}.
     */
    public static final EntityType STRAY = vanilla("stray", EntityType.Category.MONSTER, 131, 0.6f, 1.99f);

    /**
     * {@code minecraft:strider}, network ID {@code 132}.
     */
    public static final EntityType STRIDER = vanilla("strider", EntityType.Category.CREATURE, 132, 0.9f, 1.7f);

    /**
     * {@code minecraft:sulfur_cube}, network ID {@code 133}.
     */
    public static final EntityType SULFUR_CUBE = vanilla("sulfur_cube", EntityType.Category.MONSTER, 133, 0.49f, 0.49f);

    /**
     * {@code minecraft:tadpole}, network ID {@code 134}.
     */
    public static final EntityType TADPOLE = vanilla("tadpole", EntityType.Category.WATER_CREATURE, 134, 0.4f, 0.3f);

    /**
     * {@code minecraft:text_display}, network ID {@code 135}.
     */
    public static final EntityType TEXT_DISPLAY = vanilla("text_display", EntityType.Category.MISC, 135, 0.0f, 0.0f);

    /**
     * {@code minecraft:tnt}, network ID {@code 136}.
     */
    public static final EntityType TNT = vanilla("tnt", EntityType.Category.MISC, 136, 0.98f, 0.98f);

    /**
     * {@code minecraft:tnt_minecart}, network ID {@code 137}.
     */
    public static final EntityType TNT_MINECART = vanilla("tnt_minecart", EntityType.Category.MISC, 137, 0.98f, 0.7f);

    /**
     * {@code minecraft:trader_llama}, network ID {@code 138}.
     */
    public static final EntityType TRADER_LLAMA = vanilla("trader_llama", EntityType.Category.CREATURE, 138, 0.9f, 1.87f);

    /**
     * {@code minecraft:trident}, network ID {@code 139}.
     */
    public static final EntityType TRIDENT = vanilla("trident", EntityType.Category.MISC, 139, 0.5f, 0.5f);

    /**
     * {@code minecraft:tropical_fish}, network ID {@code 140}.
     */
    public static final EntityType TROPICAL_FISH = vanilla("tropical_fish", EntityType.Category.WATER_CREATURE, 140, 0.5f, 0.4f);

    /**
     * {@code minecraft:turtle}, network ID {@code 141}.
     */
    public static final EntityType TURTLE = vanilla("turtle", EntityType.Category.CREATURE, 141, 1.2f, 0.4f);

    /**
     * {@code minecraft:vex}, network ID {@code 142}.
     */
    public static final EntityType VEX = vanilla("vex", EntityType.Category.MONSTER, 142, 0.4f, 0.8f);

    /**
     * {@code minecraft:villager}, network ID {@code 143}.
     */
    public static final EntityType VILLAGER = vanilla("villager", EntityType.Category.CREATURE, 143, 0.6f, 1.95f);

    /**
     * {@code minecraft:vindicator}, network ID {@code 144}.
     */
    public static final EntityType VINDICATOR = vanilla("vindicator", EntityType.Category.MONSTER, 144, 0.6f, 1.95f);

    /**
     * {@code minecraft:wandering_trader}, network ID {@code 145}.
     */
    public static final EntityType WANDERING_TRADER = vanilla("wandering_trader", EntityType.Category.CREATURE, 145, 0.6f, 1.95f);

    /**
     * {@code minecraft:warden}, network ID {@code 146}.
     */
    public static final EntityType WARDEN = vanilla("warden", EntityType.Category.MONSTER, 146, 0.9f, 2.9f);

    /**
     * {@code minecraft:wind_charge}, network ID {@code 147}.
     */
    public static final EntityType WIND_CHARGE = vanilla("wind_charge", EntityType.Category.MISC, 147, 0.3125f, 0.3125f);

    /**
     * {@code minecraft:witch}, network ID {@code 148}.
     */
    public static final EntityType WITCH = vanilla("witch", EntityType.Category.MONSTER, 148, 0.6f, 1.95f);

    /**
     * {@code minecraft:wither}, network ID {@code 149}.
     */
    public static final EntityType WITHER = vanilla("wither", EntityType.Category.MONSTER, 149, 0.9f, 3.5f);

    /**
     * {@code minecraft:wither_skeleton}, network ID {@code 150}.
     */
    public static final EntityType WITHER_SKELETON = vanilla("wither_skeleton", EntityType.Category.MONSTER, 150, 0.7f, 2.4f);

    /**
     * {@code minecraft:wither_skull}, network ID {@code 151}.
     */
    public static final EntityType WITHER_SKULL = vanilla("wither_skull", EntityType.Category.MISC, 151, 0.3125f, 0.3125f);

    /**
     * {@code minecraft:wolf}, network ID {@code 152}.
     */
    public static final EntityType WOLF = vanilla("wolf", EntityType.Category.CREATURE, 152, 0.6f, 0.85f);

    /**
     * {@code minecraft:zoglin}, network ID {@code 153}.
     */
    public static final EntityType ZOGLIN = vanilla("zoglin", EntityType.Category.MONSTER, 153, 1.3964844f, 1.4f);

    /**
     * {@code minecraft:zombie}, network ID {@code 154}.
     */
    public static final EntityType ZOMBIE = vanilla("zombie", EntityType.Category.MONSTER, 154, 0.6f, 1.95f);

    /**
     * {@code minecraft:zombie_horse}, network ID {@code 155}.
     */
    public static final EntityType ZOMBIE_HORSE = vanilla("zombie_horse", EntityType.Category.CREATURE, 155, 1.3964844f, 1.6f);

    /**
     * {@code minecraft:zombie_nautilus}, network ID {@code 156}.
     */
    public static final EntityType ZOMBIE_NAUTILUS = vanilla("zombie_nautilus", EntityType.Category.MONSTER, 156, 0.875f, 0.95f);

    /**
     * {@code minecraft:zombie_villager}, network ID {@code 157}.
     */
    public static final EntityType ZOMBIE_VILLAGER = vanilla("zombie_villager", EntityType.Category.MONSTER, 157, 0.6f, 1.95f);

    /**
     * {@code minecraft:zombified_piglin}, network ID {@code 158}.
     */
    public static final EntityType ZOMBIFIED_PIGLIN = vanilla("zombified_piglin", EntityType.Category.MONSTER, 158, 0.6f, 1.95f);

    private EntityTypes() {
    }

    private static EntityType vanilla(final String name, final EntityType.Category category,
            final int networkId, final float width, final float height) {
        final Key key = Key.key(name);
        final EntityType type = new EntityType(key, category, width, height);
        register(type);
        NETWORK_IDS.put(key, networkId);
        return type;
    }

    /**
     * Registers a type that no vanilla constant already claims.
     *
     * @param type the type to register
     * @return the registered type
     * @throws IllegalStateException if the key is already taken
     */
    public static EntityType register(final EntityType type) {
        final EntityType previous = BY_KEY.putIfAbsent(type.key(), type);
        if (previous != null) {
            throw new IllegalStateException("Entity type already registered : " + type.key());
        }
        return type;
    }

    /**
     * Registers a plugin-defined type, rendered client-side as some vanilla type.
     *
     * @param type      the synthetic type
     * @param networkId the network ID of the vanilla type it is rendered as
     * @return the registered type
     */
    public static EntityType registerCustom(final EntityType type, final int networkId) {
        BY_KEY.put(type.key(), type);
        NETWORK_IDS.put(type.key(), networkId);
        return type;
    }

    public static boolean unregister(final Key key) {
        NETWORK_IDS.remove(key);
        return BY_KEY.remove(key) != null;
    }

    public static @Nullable EntityType get(final Key key) {
        return BY_KEY.get(key);
    }

    public static Iterable<EntityType> values() {
        return BY_KEY.values();
    }

    /**
     * @param type the entity type
     * @return the ID written by {@code add_entity}
     * @throws IllegalStateException if the type was never registered
     */
    public static int networkId(final EntityType type) {
        final Integer id = NETWORK_IDS.get(type.key());
        if (id == null) {
            throw new IllegalStateException("No network ID for the entity type " + type.key());
        }
        return id;
    }

    public static boolean hasNetworkId(final EntityType type) {
        return NETWORK_IDS.containsKey(type.key());
    }
}
