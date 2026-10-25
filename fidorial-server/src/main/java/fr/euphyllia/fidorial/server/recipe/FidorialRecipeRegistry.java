package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.recipe.CraftingRecipe;
import fr.fidorial.recipe.ItemTagLookup;
import fr.fidorial.recipe.Recipe;
import fr.fidorial.recipe.RecipeRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The server's {@link RecipeRegistry}: the vanilla recipes loaded at startup, plus the plugin ones.
 *
 * <p>Writes are serialised; every read goes through an immutable snapshot, so that matching a
 * grid never waits on a plugin registering a recipe.</p>
 */
public final class FidorialRecipeRegistry implements RecipeRegistry {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialRecipeRegistry.class);

    private final ItemTagLookup tags;
    private final Object lock = new Object();
    private final Map<Key, Entry> entries = new LinkedHashMap<>();
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    public FidorialRecipeRegistry(final ItemTagLookup tags) {
        this.tags = tags;
    }

    /**
     * Replaces the vanilla recipes, leaving the plugin ones alone.
     *
     * @param recipes the vanilla recipes
     */
    public void loadVanilla(final Collection<? extends Recipe> recipes) {
        synchronized (lock) {
            entries.values().removeIf(Entry::vanilla);
            for (final Recipe recipe : recipes) {
                if (entries.containsKey(recipe.key())) {
                    LOGGER.warn("The vanilla recipe {} is shadowed by a plugin recipe", recipe.key().asString());
                    continue;
                }
                entries.put(recipe.key(), new Entry(recipe, null));
            }
            rebuild();
        }
    }

    @Override
    public void register(final Recipe recipe, final Object owner) {
        Preconditions.checkArgument(recipe != null, "The recipe passed to RecipeRegistry.register() must not be null");
        Preconditions.checkArgument(owner != null, "The owner passed to RecipeRegistry.register() must not be null");
        synchronized (lock) {
            Preconditions.checkArgument(!entries.containsKey(recipe.key()),
                    "A recipe is already registered as %s", recipe.key());
            entries.put(recipe.key(), new Entry(recipe, owner));
            rebuild();
        }
        LOGGER.debug("Recipe {} registered by {}", recipe.key().asString(), owner);
    }

    @Override
    public boolean unregister(final Key key) {
        Preconditions.checkArgument(key != null, "The key passed to RecipeRegistry.unregister() must not be null");
        synchronized (lock) {
            if (entries.remove(key) == null) {
                return false;
            }
            rebuild();
        }
        LOGGER.debug("Recipe {} unregistered", key.asString());
        return true;
    }

    @Override
    public void unregisterAll(final Object owner) {
        Preconditions.checkArgument(owner != null, "The owner passed to RecipeRegistry.unregisterAll() must not be null");
        synchronized (lock) {
            if (entries.values().removeIf(entry -> entry.owner() == owner)) {
                rebuild();
            }
        }
    }

    @Override
    public Optional<Recipe> recipe(final Key key) {
        Preconditions.checkArgument(key != null, "The key passed to RecipeRegistry.recipe() must not be null");
        final Entry entry = snapshot.byKey().get(key);
        return entry == null ? Optional.empty() : Optional.of(entry.recipe());
    }

    @Override
    public Collection<Recipe> recipes() {
        return snapshot.ordered();
    }

    @Override
    public boolean isVanilla(final Key key) {
        Preconditions.checkArgument(key != null, "The key passed to RecipeRegistry.isVanilla() must not be null");
        final Entry entry = snapshot.byKey().get(key);
        return entry != null && entry.vanilla();
    }

    @Override
    public Optional<CraftingMatch> matchCrafting(final CraftingInput input) {
        Preconditions.checkArgument(input != null, "The input passed to RecipeRegistry.matchCrafting() must not be null");
        if (input.isEmpty()) {
            return Optional.empty();
        }
        for (final CraftingRecipe recipe : snapshot.crafting()) {
            final CraftingMatch match;
            try {
                match = recipe.match(input, tags);
            } catch (final RuntimeException exception) {
                LOGGER.warn("The recipe {} failed to match a grid", recipe.key().asString(), exception);
                continue;
            }
            if (match != null) {
                return Optional.of(match);
            }
        }
        return Optional.empty();
    }

    @Override
    public ItemTagLookup tags() {
        return tags;
    }

    /**
     * Plugin recipes first, in registration order, then the vanilla ones by key.
     */
    private void rebuild() {
        final List<Recipe> plugin = new ArrayList<>();
        final List<Recipe> vanilla = new ArrayList<>();
        for (final Entry entry : entries.values()) {
            (entry.vanilla() ? vanilla : plugin).add(entry.recipe());
        }
        vanilla.sort(Comparator.comparing(recipe -> recipe.key().asString()));

        final List<Recipe> ordered = new ArrayList<>(plugin.size() + vanilla.size());
        ordered.addAll(plugin);
        ordered.addAll(vanilla);

        final List<CraftingRecipe> crafting = new ArrayList<>();
        for (final Recipe recipe : ordered) {
            if (recipe instanceof final CraftingRecipe craftingRecipe) {
                crafting.add(craftingRecipe);
            }
        }
        snapshot = new Snapshot(Map.copyOf(entries), List.copyOf(ordered), List.copyOf(crafting));
    }

    private record Entry(Recipe recipe, @Nullable Object owner) {

        boolean vanilla() {
            return owner == null;
        }
    }

    private record Snapshot(Map<Key, Entry> byKey, List<Recipe> ordered, List<CraftingRecipe> crafting) {

        static final Snapshot EMPTY = new Snapshot(Map.of(), List.of(), List.of());
    }
}
