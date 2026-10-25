package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentEditor;
import fr.fidorial.item.data.DataComponentMap;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.recipe.CraftingCategory;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.ItemTagLookup;
import fr.fidorial.recipe.ShapelessRecipe;
import fr.fidorial.recipe.TransmuteRecipe;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * The server's {@link TransmuteRecipe}.
 */
public final class TransmuteRecipeImpl implements TransmuteRecipe {

    private final Key key;
    private final Ingredient input;
    private final Ingredient material;
    private final int minMaterials;
    private final int maxMaterials;
    private final @Nullable Key resultItem;
    private final int resultCount;
    private final DataComponentMap resultComponents;
    private final boolean addMaterialCountToResult;
    private final String group;
    private final CraftingCategory category;

    private TransmuteRecipeImpl(final BuilderImpl builder, final Ingredient input, final Ingredient material) {
        this.key = builder.key;
        this.input = input;
        this.material = material;
        this.minMaterials = builder.minMaterials;
        this.maxMaterials = builder.maxMaterials;
        this.resultItem = builder.resultItem;
        this.resultCount = builder.resultCount;
        this.resultComponents = builder.resultComponents;
        this.addMaterialCountToResult = builder.addMaterialCountToResult;
        this.group = builder.group;
        this.category = builder.category;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public Ingredient input() {
        return input;
    }

    @Override
    public Ingredient material() {
        return material;
    }

    @Override
    public int minMaterials() {
        return minMaterials;
    }

    @Override
    public int maxMaterials() {
        return maxMaterials;
    }

    @Override
    public @Nullable Key resultItem() {
        return resultItem;
    }

    @Override
    public int resultCount() {
        return resultCount;
    }

    @Override
    public DataComponentMap resultComponents() {
        return resultComponents;
    }

    @Override
    public boolean addMaterialCountToResult() {
        return addMaterialCountToResult;
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
    public @Nullable CraftingMatch match(final CraftingInput grid, final ItemTagLookup tags) {
        final int filled = grid.nonEmptyCount();
        if (filled < minMaterials + 1 || filled > maxMaterials + 1) {
            return null;
        }
        final int[] consumed = new int[grid.size()];
        int inputSlot = -1;
        int materials = 0;
        for (int slot = 0; slot < grid.size(); slot++) {
            final ItemStack stack = grid.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (input.test(stack, tags)) {
                if (inputSlot >= 0 || stack.count() < input.count()) {
                    return null;
                }
                inputSlot = slot;
                consumed[slot] = input.count();
            } else if (material.accepts(stack, tags)) {
                materials++;
                consumed[slot] = material.count();
            } else {
                return null;
            }
        }
        if (inputSlot < 0 || materials < minMaterials || materials > maxMaterials) {
            return null;
        }

        final ItemStack source = grid.get(inputSlot);
        final int count = resultCount + (addMaterialCountToResult ? materials : 0);
        final DataComponentEditor components = DataComponentEditor.of(source.components());
        for (final Map.Entry<DataComponentType<?>, Object> entry : resultComponents.entries()) {
            copy(components, entry.getKey(), entry.getValue());
        }
        for (final DataComponentType<?> removed : resultComponents.removedTypes()) {
            components.remove(removed);
        }
        final ItemStack result = new ItemStack(resultItem != null ? resultItem : source.id(), count, components.components());
        return new CraftingMatchImpl(this, result, consumed);
    }

    @SuppressWarnings("unchecked")
    private static <T> void copy(final DataComponentEditor editor, final DataComponentType<T> type, final Object value) {
        editor.set(type, (T) value);
    }

    @Override
    public String toString() {
        return "TransmuteRecipe{" + key.asString() + ", " + input + " + " + material + " -> "
                + (resultItem == null ? "<input>" : resultItem.asString()) + "}";
    }

    /**
     * The server's {@link TransmuteRecipe.Builder}.
     */
    public static final class BuilderImpl implements TransmuteRecipe.Builder {

        private final Key key;
        private @Nullable Ingredient input;
        private @Nullable Ingredient material;
        private int minMaterials = 1;
        private int maxMaterials = 1;
        private @Nullable Key resultItem;
        private int resultCount = 1;
        private DataComponentMap resultComponents = DataComponentMap.EMPTY;
        private boolean addMaterialCountToResult;
        private String group = "";
        private CraftingCategory category = CraftingCategory.MISC;

        BuilderImpl(final Key key) {
            Preconditions.checkArgument(key != null, "The key of a transmute recipe must not be null");
            this.key = key;
        }

        @Override
        public TransmuteRecipe.Builder input(final Ingredient input) {
            Preconditions.checkArgument(input != null, "The input of a transmute recipe must not be null");
            this.input = input;
            return this;
        }

        @Override
        public TransmuteRecipe.Builder material(final Ingredient material) {
            Preconditions.checkArgument(material != null, "The material of a transmute recipe must not be null");
            this.material = material;
            return this;
        }

        @Override
        public TransmuteRecipe.Builder materialCount(final int min, final int max) {
            Preconditions.checkArgument(min >= 1 && max >= min && max <= ShapelessRecipe.MAX_INGREDIENTS - 1,
                    "A transmute recipe takes between 1 and %s materials, got %s..%s",
                    ShapelessRecipe.MAX_INGREDIENTS - 1, min, max);
            this.minMaterials = min;
            this.maxMaterials = max;
            return this;
        }

        @Override
        public TransmuteRecipe.Builder result(final ItemStack result) {
            Preconditions.checkArgument(result != null && !result.isEmpty(), "The result of a transmute recipe must not be empty");
            this.resultItem = result.id();
            this.resultCount = result.count();
            this.resultComponents = result.components();
            return this;
        }

        @Override
        public TransmuteRecipe.Builder keepInputItem(final int count) {
            Preconditions.checkArgument(count >= 1, "A transmute recipe gives at least one item, got %s", count);
            this.resultItem = null;
            this.resultCount = count;
            this.resultComponents = DataComponentMap.EMPTY;
            return this;
        }

        @Override
        public TransmuteRecipe.Builder addMaterialCountToResult(final boolean add) {
            this.addMaterialCountToResult = add;
            return this;
        }

        @Override
        public TransmuteRecipe.Builder group(final String group) {
            Preconditions.checkArgument(group != null, "The group of a transmute recipe must not be null");
            this.group = group;
            return this;
        }

        @Override
        public TransmuteRecipe.Builder category(final CraftingCategory category) {
            Preconditions.checkArgument(category != null, "The category of a transmute recipe must not be null");
            this.category = category;
            return this;
        }

        @Override
        public TransmuteRecipe build() {
            final Ingredient builtInput = input;
            final Ingredient builtMaterial = material;
            Preconditions.checkArgument(builtInput != null, "The transmute recipe %s has no input", key);
            Preconditions.checkArgument(builtMaterial != null, "The transmute recipe %s has no material", key);
            return new TransmuteRecipeImpl(this, builtInput, builtMaterial);
        }
    }
}
