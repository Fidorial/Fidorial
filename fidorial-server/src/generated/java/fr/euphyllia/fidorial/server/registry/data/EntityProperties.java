package fr.euphyllia.fidorial.server.registry.data;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.kyori.adventure.key.Key;

/**
 * Per-entity hitbox dimensions.
 *
 * <p>Joined from Mojang's entity registry report and PrismarineJS's
 * {@code minecraft-data} entities report; do not edit.</p>
 *
 * <p>These are the type's <em>defaults</em>. An individual entity may
 * scale them (a baby, a slime's size, a sitting pose), so read the
 * entity's own dimensions when you have one in hand.</p>
 */
public final class EntityProperties {
    private static final Object2FloatOpenHashMap<Key> WIDTH = new Object2FloatOpenHashMap<>();

    private static final Object2FloatOpenHashMap<Key> HEIGHT = new Object2FloatOpenHashMap<>();

    static {
        WIDTH.defaultReturnValue(0.6f);
        HEIGHT.defaultReturnValue(1.8f);
    }

    private EntityProperties() {
        throw new UnsupportedOperationException("EntityProperties cannot be instantiated.");
    }

    /**
     * @param entity namespaced entity type identifier
     * @return hitbox width in blocks; {@code 0.6} when the entity type has no recorded data
     */
    public static float width(final Key entity) {
        return WIDTH.getFloat(entity);
    }

    /**
     * @param entity namespaced entity type identifier
     * @return hitbox height in blocks; {@code 1.8} when the entity type has no recorded data
     */
    public static float height(final Key entity) {
        return HEIGHT.getFloat(entity);
    }

    private static void register(final Key entity, final float width, final float height) {
        WIDTH.put(entity, width);
        HEIGHT.put(entity, height);
    }

    private static void registerEntities0() {
        register(Key.key("acacia_boat"), 1.375f, 0.5625f);
        register(Key.key("acacia_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("allay"), 0.35f, 0.6f);
        register(Key.key("area_effect_cloud"), 6.0f, 0.5f);
        register(Key.key("armadillo"), 0.7f, 0.65f);
        register(Key.key("armor_stand"), 0.5f, 1.975f);
        register(Key.key("arrow"), 0.5f, 0.5f);
        register(Key.key("axolotl"), 0.75f, 0.42f);
        register(Key.key("bamboo_chest_raft"), 1.375f, 0.5625f);
        register(Key.key("bamboo_raft"), 1.375f, 0.5625f);
        register(Key.key("bat"), 0.5f, 0.9f);
        register(Key.key("bee"), 0.55f, 0.5f);
        register(Key.key("birch_boat"), 1.375f, 0.5625f);
        register(Key.key("birch_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("blaze"), 0.6f, 1.8f);
        register(Key.key("block_display"), 0.0f, 0.0f);
        register(Key.key("bogged"), 0.6f, 1.99f);
        register(Key.key("breeze"), 0.6f, 1.77f);
        register(Key.key("breeze_wind_charge"), 0.3125f, 0.3125f);
        register(Key.key("camel"), 1.7f, 2.375f);
        register(Key.key("camel_husk"), 1.7f, 2.375f);
        register(Key.key("cat"), 0.6f, 0.7f);
        register(Key.key("cave_spider"), 0.7f, 0.5f);
        register(Key.key("cherry_boat"), 1.375f, 0.5625f);
        register(Key.key("cherry_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("chest_minecart"), 0.98f, 0.7f);
        register(Key.key("chicken"), 0.4f, 0.7f);
        register(Key.key("cod"), 0.5f, 0.3f);
        register(Key.key("command_block_minecart"), 0.98f, 0.7f);
        register(Key.key("copper_golem"), 0.49f, 0.98f);
        register(Key.key("cow"), 0.9f, 1.4f);
        register(Key.key("creaking"), 0.9f, 2.7f);
        register(Key.key("creeper"), 0.6f, 1.7f);
        register(Key.key("cushion"), 1.0f, 0.25f);
        register(Key.key("dark_oak_boat"), 1.375f, 0.5625f);
        register(Key.key("dark_oak_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("dolphin"), 0.9f, 0.6f);
        register(Key.key("donkey"), 1.3964844f, 1.5f);
        register(Key.key("dragon_fireball"), 1.0f, 1.0f);
        register(Key.key("drowned"), 0.6f, 1.95f);
        register(Key.key("egg"), 0.25f, 0.25f);
        register(Key.key("elder_guardian"), 1.9975f, 1.9975f);
        register(Key.key("end_crystal"), 2.0f, 2.0f);
        register(Key.key("ender_dragon"), 16.0f, 8.0f);
        register(Key.key("ender_pearl"), 0.25f, 0.25f);
        register(Key.key("enderman"), 0.6f, 2.9f);
        register(Key.key("endermite"), 0.4f, 0.3f);
        register(Key.key("evoker"), 0.6f, 1.95f);
        register(Key.key("evoker_fangs"), 0.5f, 0.8f);
        register(Key.key("experience_bottle"), 0.25f, 0.25f);
        register(Key.key("experience_orb"), 0.5f, 0.5f);
        register(Key.key("eye_of_ender"), 0.25f, 0.25f);
        register(Key.key("falling_block"), 0.98f, 0.98f);
        register(Key.key("fireball"), 1.0f, 1.0f);
        register(Key.key("firework_rocket"), 0.25f, 0.25f);
        register(Key.key("fishing_bobber"), 0.25f, 0.25f);
        register(Key.key("fox"), 0.6f, 0.7f);
        register(Key.key("frog"), 0.5f, 0.5f);
        register(Key.key("furnace_minecart"), 0.98f, 0.7f);
        register(Key.key("ghast"), 4.0f, 4.0f);
        register(Key.key("giant"), 3.6f, 12.0f);
        register(Key.key("glow_item_frame"), 0.5f, 0.5f);
        register(Key.key("glow_squid"), 0.8f, 0.8f);
        register(Key.key("goat"), 0.9f, 1.3f);
        register(Key.key("guardian"), 0.85f, 0.85f);
        register(Key.key("happy_ghast"), 4.0f, 4.0f);
        register(Key.key("hoglin"), 1.3964844f, 1.4f);
        register(Key.key("hopper_minecart"), 0.98f, 0.7f);
        register(Key.key("horse"), 1.3964844f, 1.6f);
        register(Key.key("husk"), 0.6f, 1.95f);
        register(Key.key("illusioner"), 0.6f, 1.95f);
        register(Key.key("interaction"), 0.0f, 0.0f);
        register(Key.key("iron_golem"), 1.4f, 2.7f);
        register(Key.key("item"), 0.25f, 0.25f);
        register(Key.key("item_display"), 0.0f, 0.0f);
        register(Key.key("item_frame"), 0.5f, 0.5f);
        register(Key.key("jungle_boat"), 1.375f, 0.5625f);
        register(Key.key("jungle_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("leash_knot"), 0.375f, 0.5f);
        register(Key.key("lightning_bolt"), 0.0f, 0.0f);
        register(Key.key("lingering_potion"), 0.25f, 0.25f);
        register(Key.key("llama"), 0.9f, 1.87f);
        register(Key.key("llama_spit"), 0.25f, 0.25f);
        register(Key.key("magma_cube"), 0.52f, 0.52f);
        register(Key.key("mangrove_boat"), 1.375f, 0.5625f);
        register(Key.key("mangrove_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("mannequin"), 0.6f, 1.8f);
        register(Key.key("marker"), 0.0f, 0.0f);
        register(Key.key("minecart"), 0.98f, 0.7f);
        register(Key.key("mooshroom"), 0.9f, 1.4f);
        register(Key.key("mule"), 1.3964844f, 1.6f);
        register(Key.key("nautilus"), 0.875f, 0.95f);
        register(Key.key("oak_boat"), 1.375f, 0.5625f);
        register(Key.key("oak_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("ocelot"), 0.6f, 0.7f);
        register(Key.key("ominous_item_spawner"), 0.25f, 0.25f);
        register(Key.key("painting"), 0.5f, 0.5f);
        register(Key.key("pale_oak_boat"), 1.375f, 0.5625f);
        register(Key.key("pale_oak_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("panda"), 1.3f, 1.25f);
        register(Key.key("parched"), 0.6f, 1.99f);
        register(Key.key("parrot"), 0.5f, 0.9f);
        register(Key.key("phantom"), 0.9f, 0.5f);
        register(Key.key("pig"), 0.9f, 0.9f);
        register(Key.key("piglin"), 0.6f, 1.95f);
        register(Key.key("piglin_brute"), 0.6f, 1.95f);
        register(Key.key("pillager"), 0.6f, 1.95f);
        register(Key.key("player"), 0.6f, 1.8f);
        register(Key.key("polar_bear"), 1.4f, 1.4f);
        register(Key.key("poplar_boat"), 1.375f, 0.5625f);
        register(Key.key("poplar_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("pufferfish"), 0.7f, 0.7f);
        register(Key.key("rabbit"), 0.49f, 0.6f);
        register(Key.key("ravager"), 1.95f, 2.2f);
        register(Key.key("salmon"), 0.7f, 0.4f);
        register(Key.key("sheep"), 0.9f, 1.3f);
        register(Key.key("shulker"), 1.0f, 1.0f);
        register(Key.key("shulker_bullet"), 0.3125f, 0.3125f);
        register(Key.key("silverfish"), 0.4f, 0.3f);
        register(Key.key("skeleton"), 0.6f, 1.99f);
        register(Key.key("skeleton_horse"), 1.3964844f, 1.6f);
        register(Key.key("slime"), 0.52f, 0.52f);
        register(Key.key("small_fireball"), 0.3125f, 0.3125f);
        register(Key.key("sniffer"), 1.9f, 1.75f);
        register(Key.key("snow_golem"), 0.7f, 1.9f);
        register(Key.key("snowball"), 0.25f, 0.25f);
        register(Key.key("spawner_minecart"), 0.98f, 0.7f);
        register(Key.key("spectral_arrow"), 0.5f, 0.5f);
        register(Key.key("spider"), 1.4f, 0.9f);
        register(Key.key("splash_potion"), 0.25f, 0.25f);
        register(Key.key("spruce_boat"), 1.375f, 0.5625f);
        register(Key.key("spruce_chest_boat"), 1.375f, 0.5625f);
        register(Key.key("squid"), 0.8f, 0.8f);
        register(Key.key("stray"), 0.6f, 1.99f);
        register(Key.key("strider"), 0.9f, 1.7f);
        register(Key.key("sulfur_cube"), 0.49f, 0.49f);
        register(Key.key("tadpole"), 0.4f, 0.3f);
        register(Key.key("text_display"), 0.0f, 0.0f);
        register(Key.key("tnt"), 0.98f, 0.98f);
        register(Key.key("tnt_minecart"), 0.98f, 0.7f);
        register(Key.key("trader_llama"), 0.9f, 1.87f);
        register(Key.key("trident"), 0.5f, 0.5f);
        register(Key.key("tropical_fish"), 0.5f, 0.4f);
        register(Key.key("turtle"), 1.2f, 0.4f);
        register(Key.key("vex"), 0.4f, 0.8f);
        register(Key.key("villager"), 0.6f, 1.95f);
        register(Key.key("vindicator"), 0.6f, 1.95f);
        register(Key.key("wandering_trader"), 0.6f, 1.95f);
        register(Key.key("warden"), 0.9f, 2.9f);
        register(Key.key("wind_charge"), 0.3125f, 0.3125f);
        register(Key.key("witch"), 0.6f, 1.95f);
        register(Key.key("wither"), 0.9f, 3.5f);
        register(Key.key("wither_skeleton"), 0.7f, 2.4f);
        register(Key.key("wither_skull"), 0.3125f, 0.3125f);
        register(Key.key("wolf"), 0.6f, 0.85f);
        register(Key.key("zoglin"), 1.3964844f, 1.4f);
        register(Key.key("zombie"), 0.6f, 1.95f);
        register(Key.key("zombie_horse"), 1.3964844f, 1.6f);
        register(Key.key("zombie_nautilus"), 0.875f, 0.95f);
        register(Key.key("zombie_villager"), 0.6f, 1.95f);
        register(Key.key("zombified_piglin"), 0.6f, 1.95f);
    }

    /**
     * Fills the lookup tables. Idempotent, and cheap enough to call
     * from the static initialiser of whatever needs it first.
     */
    public static void bootstrap() {
        registerEntities0();
    }
}
