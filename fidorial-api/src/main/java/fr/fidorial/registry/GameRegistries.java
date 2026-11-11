package fr.fidorial.registry;

import fr.fidorial.Server;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.effect.MobEffectRegistry;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.item.ItemRegistry;
import fr.fidorial.item.enchantment.Enchantments;
import fr.fidorial.permission.PermissionRegistry;
import fr.fidorial.recipe.RecipeRegistry;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingRegistry;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.WorldGenerator;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneRegistry;
import org.jetbrains.annotations.Contract;

/**
 * The server-wide registries: every {@linkplain #registry(RegistryKey) data registry}, with the
 * vanilla entries and those plugins add, and the content defined in code (mobs, effects, items,
 * recipes, permissions, redstone and farming behaviours).
 *
 * @see Server#registries()
 * @since 0.1.0
 */
public interface GameRegistries {

    /**
     * Gets a registry.
     *
     * <p>Every {@link RegistryKey} has one, populated with the vanilla entries from the start.</p>
     *
     * @param key the key of the registry
     * @param <T> the type of the values
     * @return the registry
     * @throws IllegalArgumentException if the server has no such registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    <T> Registry<T> registry(RegistryKey<T> key);

    /**
     * Gets a registry plugins can add entries to, such as {@link RegistryKey#BIOME},
     * {@link RegistryKey#DIMENSION_TYPE}, {@link RegistryKey#CHAT_TYPE}, {@link RegistryKey#DIALOG}
     * or {@link RegistryKey#ENCHANTMENT}.
     *
     * <p>Biomes registered there may be referenced by {@link WorldGenerator generators} through
     * {@link GeneratedChunk#setBiome(int, int, int, TypedKey)}, and dimension types returned by
     * {@link WorldGenerator#dimensionType()}.</p>
     *
     * @param key the key of the registry
     * @param <T> the type of the values
     * @return the registry
     * @throws IllegalArgumentException if the registry is not writable
     * @since 0.1.0
     */
    @Contract(pure = true)
    <T> WritableRegistry<T> writable(RegistryKey<T> key);

    /**
     * Gets the server-wide mob registry.
     *
     * <p>Where plugins {@linkplain MobRegistry#attach attach behaviours} to
     * the built-in mobs, and {@linkplain MobRegistry#register register}
     * mobs of their own.</p>
     *
     * @return the mob registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    MobRegistry mobs();

    /**
     * Gets the server-wide effect registry.
     *
     * <p>Where plugins create the {@linkplain MobEffectInstance effects} they
     * apply, {@linkplain MobEffectRegistry#attach give behaviour} to vanilla effects and
     * {@linkplain MobEffectRegistry#register add} effects of their own.</p>
     *
     * @return the effect registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    MobEffectRegistry effects();

    /**
     * Gets the enchantment behaviours and helpers.
     *
     * <p>The enchantments themselves live in the {@link RegistryKey#ENCHANTMENT} registry. This is
     * where plugins {@linkplain Enchantments#define define} enchantments and
     * {@linkplain Enchantments#attach give behaviour} to them, vanilla ones included.</p>
     *
     * @return the enchantments
     * @since 0.1.0
     */
    @Contract(pure = true)
    Enchantments enchantments();

    /**
     * Gets the server-wide item registry.
     *
     * <p>Where plugins {@linkplain ItemRegistry#register register} items of their own,
     * rendered client-side as the vanilla item they name.</p>
     *
     * @return the item registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    ItemRegistry items();

    /**
     * Gets the server-wide recipe registry.
     *
     * <p>Holds the vanilla crafting recipes, and where plugins
     * {@linkplain RecipeRegistry#register register} recipes of their own, including recipes
     * that take several items from one grid slot.</p>
     *
     * @return the recipe registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    RecipeRegistry recipes();

    /**
     * Gets the server-wide redstone registry.
     *
     * <p>Where each block type taking part in redstone gets its
     * {@linkplain RedstoneBehaviour behaviour}, and where plugins
     * {@linkplain RedstoneRegistry#attach attach} one to the blocks they add, or to vanilla ones.</p>
     *
     * @return the redstone registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    RedstoneRegistry redstone();

    /**
     * Gets the server-wide farming registry.
     *
     * <p>Where each block type taking part in farming gets its
     * {@linkplain FarmingBehaviour behaviour}, where plugins
     * {@linkplain FarmingRegistry#attach attach} one to the plants they add, or to the built-in
     * ones, and where they declare their seeds, fertilizers, hoes and tillable soils.</p>
     *
     * @return the farming registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    FarmingRegistry farming();

    /**
     * Gets the server-wide permission registry.
     *
     * @return the permission registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    PermissionRegistry permissions();
}
