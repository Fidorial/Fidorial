package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingCategory;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.ItemTagLookup;
import fr.fidorial.recipe.ShapelessRecipe;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The server's {@link ShapelessRecipe}: pairs each non-empty slot with an ingredient accepting it.
 */
public final class ShapelessRecipeImpl implements ShapelessRecipe {

    private final Key key;
    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final String group;
    private final CraftingCategory category;

    private ShapelessRecipeImpl(final BuilderImpl builder) {
        this.key = builder.key;
        this.ingredients = List.copyOf(builder.ingredients);
        this.result = builder.result;
        this.group = builder.group;
        this.category = builder.category;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public List<Ingredient> ingredients() {
        return ingredients;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public CraftingCategory category() {
        return category;
    }

    @Override
    public @Nullable CraftingMatch match(final CraftingInput input, final ItemTagLookup tags) {
        if (input.nonEmptyCount() != ingredients.size()) {
            return null;
        }
        final int[] slots = new int[ingredients.size()];
        int filled = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            if (!input.get(slot).isEmpty()) {
                slots[filled++] = slot;
            }
        }

        // accepts[i][j]: ingredient i can be fed by the j-th non-empty slot
        final boolean[][] accepts = new boolean[ingredients.size()][slots.length];
        for (int i = 0; i < ingredients.size(); i++) {
            boolean any = false;
            for (int j = 0; j < slots.length; j++) {
                accepts[i][j] = ingredients.get(i).accepts(input.get(slots[j]), tags);
                any |= accepts[i][j];
            }
            if (!any) {
                return null;
            }
        }

        final int[] assignment = new int[ingredients.size()];
        if (!assign(accepts, 0, new boolean[slots.length], assignment)) {
            return null;
        }
        final int[] consumed = new int[input.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            consumed[slots[assignment[i]]] = ingredients.get(i).count();
        }
        return new CraftingMatchImpl(this, result, consumed);
    }

    private static boolean assign(final boolean[][] accepts, final int ingredient, final boolean[] taken,
                                  final int[] assignment) {
        if (ingredient == accepts.length) {
            return true;
        }
        for (int slot = 0; slot < taken.length; slot++) {
            if (taken[slot] || !accepts[ingredient][slot]) {
                continue;
            }
            taken[slot] = true;
            assignment[ingredient] = slot;
            if (assign(accepts, ingredient + 1, taken, assignment)) {
                return true;
            }
            taken[slot] = false;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ShapelessRecipe{" + key.asString() + ", " + ingredients + " -> " + result + "}";
    }

    /**
     * The server's {@link ShapelessRecipe.Builder}.
     */
    public static final class BuilderImpl implements ShapelessRecipe.Builder {

        private final Key key;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private ItemStack result = ItemStack.EMPTY;
        private String group = "";
        private CraftingCategory category = CraftingCategory.MISC;

        BuilderImpl(final Key key) {
            Preconditions.checkArgument(key != null, "The key of a shapeless recipe must not be null");
            this.key = key;
        }

        @Override
        public ShapelessRecipe.Builder ingredient(final Ingredient ingredient) {
            Preconditions.checkArgument(ingredient != null, "The ingredient of a shapeless recipe must not be null");
            ingredients.add(ingredient);
            return this;
        }

        @Override
        public ShapelessRecipe.Builder ingredient(final Ingredient ingredient, final int slots) {
            Preconditions.checkArgument(slots >= 1, "An ingredient fills at least one slot, got %s", slots);
            for (int i = 0; i < slots; i++) {
                ingredient(ingredient);
            }
            return this;
        }

        @Override
        public ShapelessRecipe.Builder result(final ItemStack result) {
            Preconditions.checkArgument(result != null, "The result of a shapeless recipe must not be null");
            this.result = result;
            return this;
        }

        @Override
        public ShapelessRecipe.Builder group(final String group) {
            Preconditions.checkArgument(group != null, "The group of a shapeless recipe must not be null");
            this.group = group;
            return this;
        }

        @Override
        public ShapelessRecipe.Builder category(final CraftingCategory category) {
            Preconditions.checkArgument(category != null, "The category of a shapeless recipe must not be null");
            this.category = category;
            return this;
        }

        @Override
        public ShapelessRecipe build() {
            Preconditions.checkArgument(!result.isEmpty(), "The shapeless recipe %s has no result", key);
            Preconditions.checkArgument(!ingredients.isEmpty() && ingredients.size() <= MAX_INGREDIENTS,
                    "The shapeless recipe %s needs between 1 and %s ingredients, got %s",
                    key, MAX_INGREDIENTS, ingredients.size());
            return new ShapelessRecipeImpl(this);
        }
    }
}
