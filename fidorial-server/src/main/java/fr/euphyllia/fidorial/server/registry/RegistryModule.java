package fr.euphyllia.fidorial.server.registry;

import fr.euphyllia.fidorial.server.bootstrap.GameBootstrap;
import fr.euphyllia.fidorial.server.entity.effect.FidorialMobEffectRegistry;
import fr.euphyllia.fidorial.server.entity.mob.FidorialMobRegistry;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.item.enchantment.FidorialEnchantmentRegistry;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.permission.FidorialPermissionRegistry;
import fr.euphyllia.fidorial.server.recipe.FidorialRecipeRegistry;
import fr.euphyllia.fidorial.server.recipe.VanillaRecipeLoader;
import fr.euphyllia.fidorial.server.registry.biome.FidorialBiomeRegistry;
import fr.euphyllia.fidorial.server.registry.chat.FidorialChatTypeRegistry;
import fr.euphyllia.fidorial.server.registry.dialog.FidorialDialogRegistry;
import fr.euphyllia.fidorial.server.registry.dimension.FidorialDimensionTypeRegistry;
import fr.euphyllia.fidorial.server.translation.BuiltInTranslationStore;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.block.FidorialBlockRegistry;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.permission.PermissionRegistry;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.translation.TranslationStore;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Game data: blocks, items, mobs, effects, permissions, translations and the data-driven
 * registries synchronised with clients (biomes, dimension types, chat types, dialogs, enchantments...).
 *
 * <p>This is the server's implementation of the {@link GameRegistries} API.</p>
 */
public final class RegistryModule implements ServerModule, GameRegistries {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(RegistryModule.class);
    private static final Key ITEM_REGISTRY = Key.key("item");

    private final Registries registries = Registries.load();
    private final FidorialBlockRegistry blocks = GameBootstrap.blockRegistry();
    private final BlockStateRegistry blockStates = new BlockStateRegistry(blocks);
    private final FidorialItemRegistry items = new FidorialItemRegistry();
    private final FidorialMobRegistry mobs = new FidorialMobRegistry();
    private final FidorialMobEffectRegistry effects = new FidorialMobEffectRegistry();
    private final FidorialPermissionRegistry permissions = new FidorialPermissionRegistry();
    private final BuiltInTranslationStore translations = new BuiltInTranslationStore();
    private final FidorialRecipeRegistry recipes =
            new FidorialRecipeRegistry((tag, item) -> registries.frozen().isTagged(ITEM_REGISTRY, tag, item));

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(PermissionRegistry.class, permissions);
        services.provide(MobRegistry.class, mobs);
    }

    @Override
    public void load() {
        TranslationStore.setStore(translations);

        final VanillaRecipeLoader loader =
                new VanillaRecipeLoader(item -> registries.frozen().networkId(ITEM_REGISTRY, item) >= 0);
        recipes.loadVanilla(loader.load());
        LOGGER.info("Loaded {} vanilla crafting recipes, {} other vanilla recipes are not supported yet",
                recipes.recipes().size(), loader.skipped().values().stream().mapToInt(Integer::intValue).sum());
    }

    /**
     * Once started, changing a client-synchronised registry sends the players back to the
     * configuration phase to resync them.
     *
     * @param started whether players may be connected
     */
    public void setStarted(final boolean started) {
        registries.biomes().started.set(started);
        registries.dimensionTypes().started.set(started);
        registries.chatTypes().started.set(started);
        registries.enchantments().started.set(started);
    }

    public <T> Registry<T> registry(final RegistryKey<T> key) {
        return registries.registry(key);
    }

    public RegistryHolder frozen() {
        return registries.frozen();
    }

    public RegistryHolder network() {
        return registries.network();
    }

    public RegistryHolder dynamic() {
        return registries.dynamic();
    }

    @Override
    public FidorialBiomeRegistry biomes() {
        return registries.biomes();
    }

    @Override
    public FidorialDialogRegistry dialogs() {
        return registries.dialogs();
    }

    @Override
    public FidorialDimensionTypeRegistry dimensionTypes() {
        return registries.dimensionTypes();
    }

    @Override
    public FidorialChatTypeRegistry chatTypes() {
        return registries.chatTypes();
    }

    @Override
    public FidorialEnchantmentRegistry enchantments() {
        return registries.enchantments();
    }

    public FidorialBlockRegistry blocks() {
        return blocks;
    }

    public BlockStateRegistry blockStates() {
        return blockStates;
    }

    @Override
    public FidorialItemRegistry items() {
        return items;
    }

    @Override
    public FidorialRecipeRegistry recipes() {
        return recipes;
    }

    @Override
    public FidorialMobRegistry mobs() {
        return mobs;
    }

    @Override
    public FidorialMobEffectRegistry effects() {
        return effects;
    }

    @Override
    public FidorialPermissionRegistry permissions() {
        return permissions;
    }

    public BuiltInTranslationStore translations() {
        return translations;
    }
}
