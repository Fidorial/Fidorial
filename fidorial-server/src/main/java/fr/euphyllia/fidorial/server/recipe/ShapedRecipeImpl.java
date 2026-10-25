package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingCategory;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.ItemTagLookup;
import fr.fidorial.recipe.ShapedRecipe;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The server's {@link ShapedRecipe}: the pattern, trimmed of its empty rows and columns.
 */
public final class ShapedRecipeImpl implements ShapedRecipe {

    private final Key key;
    private final int width;
    private final int height;
    private final @Nullable Ingredient[] ingredients;
    private final ItemStack result;
    private final String group;
    private final CraftingCategory category;
    private final boolean mirrored;

    private ShapedRecipeImpl(final BuilderImpl builder, final int width, final int height,
                             final @Nullable Ingredient[] ingredients) {
        this.key = builder.key;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = builder.result;
        this.group = builder.group;
        this.category = builder.category;
        this.mirrored = builder.mirrored;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public @Nullable Ingredient ingredient(final int x, final int y) {
        Preconditions.checkElementIndex(x, width, "x");
        Preconditions.checkElementIndex(y, height, "y");
        return ingredients[y * width + x];
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public boolean mirrored() {
        return mirrored;
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
        if (width > input.width() || height > input.height()) {
            return null;
        }
        for (int offsetY = 0; offsetY <= input.height() - height; offsetY++) {
            for (int offsetX = 0; offsetX <= input.width() - width; offsetX++) {
                int[] consumed = matchAt(input, tags, offsetX, offsetY, false);
                if (consumed == null && mirrored) {
                    consumed = matchAt(input, tags, offsetX, offsetY, true);
                }
                if (consumed != null) {
                    return new CraftingMatchImpl(this, result, consumed);
                }
            }
        }
        return null;
    }

    private int @Nullable [] matchAt(final CraftingInput input, final ItemTagLookup tags,
                                     final int offsetX, final int offsetY, final boolean flip) {
        final int[] consumed = new int[input.size()];
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                final ItemStack stack = input.get(x, y);
                final int patternX = x - offsetX;
                final int patternY = y - offsetY;
                final boolean inside = patternX >= 0 && patternX < width && patternY >= 0 && patternY < height;
                final Ingredient ingredient =
                        inside ? ingredients[patternY * width + (flip ? width - 1 - patternX : patternX)] : null;
                if (ingredient == null) {
                    if (!stack.isEmpty()) {
                        return null;
                    }
                    continue;
                }
                if (!ingredient.accepts(stack, tags)) {
                    return null;
                }
                consumed[y * input.width() + x] = ingredient.count();
            }
        }
        return consumed;
    }

    @Override
    public String toString() {
        return "ShapedRecipe{" + key.asString() + ", " + width + "x" + height + " -> " + result + "}";
    }

    /**
     * The server's {@link ShapedRecipe.Builder}.
     */
    public static final class BuilderImpl implements ShapedRecipe.Builder {

        private final Key key;
        private final Map<Character, Ingredient> definitions = new LinkedHashMap<>();
        private List<String> pattern = List.of();
        private ItemStack result = ItemStack.EMPTY;
        private String group = "";
        private CraftingCategory category = CraftingCategory.MISC;
        private boolean mirrored = true;

        BuilderImpl(final Key key) {
            Preconditions.checkArgument(key != null, "The key of a shaped recipe must not be null");
            this.key = key;
        }

        @Override
        public ShapedRecipe.Builder pattern(final String... rows) {
            Preconditions.checkArgument(rows != null, "The pattern of a shaped recipe must not be null");
            this.pattern = List.of(rows);
            return this;
        }

        @Override
        public ShapedRecipe.Builder define(final char symbol, final Ingredient ingredient) {
            Preconditions.checkArgument(symbol != ' ', "A space always stands for an empty cell");
            Preconditions.checkArgument(ingredient != null, "The ingredient of a shaped recipe must not be null");
            definitions.put(symbol, ingredient);
            return this;
        }

        @Override
        public ShapedRecipe.Builder result(final ItemStack result) {
            Preconditions.checkArgument(result != null, "The result of a shaped recipe must not be null");
            this.result = result;
            return this;
        }

        @Override
        public ShapedRecipe.Builder group(final String group) {
            Preconditions.checkArgument(group != null, "The group of a shaped recipe must not be null");
            this.group = group;
            return this;
        }

        @Override
        public ShapedRecipe.Builder category(final CraftingCategory category) {
            Preconditions.checkArgument(category != null, "The category of a shaped recipe must not be null");
            this.category = category;
            return this;
        }

        @Override
        public ShapedRecipe.Builder mirrored(final boolean mirrored) {
            this.mirrored = mirrored;
            return this;
        }

        @Override
        public ShapedRecipe build() {
            Preconditions.checkArgument(!result.isEmpty(), "The shaped recipe %s has no result", key);
            Preconditions.checkArgument(!pattern.isEmpty() && pattern.size() <= MAX_SIZE,
                    "The shaped recipe %s needs between 1 and %s rows, got %s", key, MAX_SIZE, pattern.size());
            final int rawWidth = pattern.getFirst().length();
            for (final String row : pattern) {
                Preconditions.checkArgument(row.length() == rawWidth,
                        "Every row of the shaped recipe %s must be %s wide: %s", key, rawWidth, pattern);
            }
            Preconditions.checkArgument(rawWidth >= 1 && rawWidth <= MAX_SIZE,
                    "The shaped recipe %s needs between 1 and %s columns, got %s", key, MAX_SIZE, rawWidth);

            final List<Character> unused = new ArrayList<>(definitions.keySet());
            int minX = rawWidth;
            int maxX = -1;
            int minY = pattern.size();
            int maxY = -1;
            for (int y = 0; y < pattern.size(); y++) {
                for (int x = 0; x < rawWidth; x++) {
                    final char symbol = pattern.get(y).charAt(x);
                    if (symbol == ' ') {
                        continue;
                    }
                    Preconditions.checkArgument(definitions.containsKey(symbol),
                            "The shaped recipe %s uses the undefined symbol '%s'", key, symbol);
                    unused.remove((Character) symbol);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
            Preconditions.checkArgument(maxX >= 0, "The pattern of the shaped recipe %s is empty", key);
            Preconditions.checkArgument(unused.isEmpty(),
                    "The shaped recipe %s defines symbols its pattern never uses: %s", key, unused);

            final int width = maxX - minX + 1;
            final int height = maxY - minY + 1;
            final @Nullable Ingredient[] cells = new Ingredient[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final char symbol = pattern.get(minY + y).charAt(minX + x);
                    cells[y * width + x] = symbol == ' ' ? null : definitions.get(symbol);
                }
            }
            return new ShapedRecipeImpl(this, width, height, cells);
        }
    }
}
