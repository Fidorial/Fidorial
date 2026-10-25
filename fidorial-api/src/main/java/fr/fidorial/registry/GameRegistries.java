package fr.fidorial.registry;

import fr.fidorial.Server;
import fr.fidorial.chat.ChatTypeRegistry;
import fr.fidorial.dialog.DialogRegistry;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.effect.MobEffectRegistry;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.item.ItemRegistry;
import fr.fidorial.item.enchantment.EnchantmentRegistry;
import fr.fidorial.permission.PermissionRegistry;
import fr.fidorial.recipe.RecipeRegistry;
import fr.fidorial.world.biome.BiomeRegistry;
import fr.fidorial.world.dimension.DimensionTypeRegistry;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

/**
 * The server-wide registries plugins can extend: content sent to clients (biomes, dimension
 * types, chat types, dialogs, enchantments) and content defined in code (mobs, effects, items,
 * permissions).
 *
 * @see Server#registries()
 * @since 0.1.0
 */
public interface GameRegistries {

    /**
     * Gets the server-wide biome registry.
     *
     * <p>Biomes registered there are sent to every client, and may be referenced
     * by {@link WorldGenerator generators} through
     * {@link GeneratedChunk#setBiome(int, int, int, Key)}.</p>
     *
     * @return the biome registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    BiomeRegistry biomes();

    /**
     * Gets the server-wide dimension type registry.
     *
     * <p>Dimension types registered there are sent to every client, and may be returned by
     * {@link WorldGenerator#dimensionType() generators} to shape the worlds they drive.</p>
     *
     * @return the dimension type registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    DimensionTypeRegistry dimensionTypes();

    /**
     * Gets the server-wide chat type registry.
     *
     * @return the chat type registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    ChatTypeRegistry chatTypes();

    /**
     * Gets the server-wide dialog registry.
     *
     * @return the dialog registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    DialogRegistry dialogs();

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
     * Gets the server-wide enchantment registry.
     *
     * <p>Where plugins {@linkplain EnchantmentRegistry#register register} enchantments of their own
     * and {@linkplain EnchantmentRegistry#attach give behaviour} to vanilla enchantments.</p>
     *
     * @return the enchantment registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    EnchantmentRegistry enchantments();

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
     * Gets the server-wide permission registry.
     *
     * @return the permission registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    PermissionRegistry permissions();
}
