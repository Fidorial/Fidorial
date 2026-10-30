package fr.euphyllia.fidorial.server.registry;

import fr.euphyllia.fidorial.server.bootstrap.GameBootstrap;
import fr.euphyllia.fidorial.server.datapack.DatapackContents;
import fr.euphyllia.fidorial.server.datapack.DatapackLoader;
import fr.euphyllia.fidorial.server.entity.effect.FidorialMobEffectRegistry;
import fr.euphyllia.fidorial.server.entity.mob.FidorialMobRegistry;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.item.enchantment.FidorialEnchantments;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.permission.FidorialPermissionRegistry;
import fr.euphyllia.fidorial.server.recipe.FidorialRecipeRegistry;
import fr.euphyllia.fidorial.server.recipe.VanillaRecipeLoader;
import fr.euphyllia.fidorial.server.translation.BuiltInTranslationStore;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.block.FidorialBlockRegistry;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.permission.PermissionRegistry;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.translation.TranslationStore;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Game data: the registries, filled with the vanilla data as soon as the module is built so that
 * commands can use them, then with the data packs on load; and the content defined in code: blocks,
 * items, mobs, effects, permissions, recipes and translations.
 *
 * <p>This is the server's implementation of the {@link GameRegistries} API.</p>
 */
public final class RegistryModule implements ServerModule, GameRegistries {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(RegistryModule.class);

    private final Path datapacksFolder;
    private final ServerRegistries registries = ServerRegistries.load();
    private final FidorialEnchantments enchantments = new FidorialEnchantments(registries);
    private final FidorialBlockRegistry blocks = GameBootstrap.blockRegistry();
    private final BlockStateRegistry blockStates = new BlockStateRegistry(blocks);
    private final FidorialItemRegistry items = new FidorialItemRegistry();
    private final FidorialMobRegistry mobs = new FidorialMobRegistry();
    private final FidorialMobEffectRegistry effects = new FidorialMobEffectRegistry();
    private final FidorialPermissionRegistry permissions = new FidorialPermissionRegistry();
    private final BuiltInTranslationStore translations = new BuiltInTranslationStore();
    private final FidorialRecipeRegistry recipes =
            new FidorialRecipeRegistry((tag, item) -> registries.registry(RegistryKey.ITEM).isTagged(item, tag));
    private volatile DatapackContents datapacks = DatapackContents.empty();

    /**
     * @param datapacksFolder the folder of the data packs
     */
    public RegistryModule(final Path datapacksFolder) {
        this.datapacksFolder = datapacksFolder;
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(PermissionRegistry.class, permissions);
        services.provide(MobRegistry.class, mobs);
    }

    @Override
    public void load() {
        TranslationStore.setStore(translations);

        try {
            datapacks = DatapackLoader.load(datapacksFolder);
        } catch (final IOException failure) {
            LOGGER.error("[datapacks] Unable to read {}", datapacksFolder, failure);
        }
        registries.applyDatapackTags(datapacks);

        final VanillaRecipeLoader loader = new VanillaRecipeLoader(registries.registry(RegistryKey.ITEM)::contains);
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
        registries.setStarted(started);
    }

    /**
     * Removes what a plugin added to the registries.
     */
    public void unregisterAll(final Plugin plugin) {
        registries.unregisterAll(plugin);
        enchantments.detachAll(plugin);
        recipes.unregisterAll(plugin);
    }

    @Override
    public <T> ServerRegistry<T> registry(final RegistryKey<T> key) {
        return registries.registry(key);
    }

    @Override
    public <T> WritableServerRegistry<T> writable(final RegistryKey<T> key) {
        return registries.writable(key);
    }

    /**
     * {@return the registry named {@code name}, {@code null} if there is none}
     */
    public @Nullable ServerRegistry<?> registry(final Key name) {
        return registries.registry(name);
    }

    public ServerRegistries serverRegistries() {
        return registries;
    }

    /**
     * {@return the data packs, read on load}
     */
    public DatapackContents datapacks() {
        return datapacks;
    }

    @Override
    public FidorialEnchantments enchantments() {
        return enchantments;
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
