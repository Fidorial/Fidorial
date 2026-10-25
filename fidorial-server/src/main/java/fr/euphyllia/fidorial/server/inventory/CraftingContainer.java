package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.recipe.CraftingInputImpl;
import fr.fidorial.inventory.SimpleContainer;
import fr.fidorial.recipe.CraftingInput;

import java.util.List;

/**
 * The slots of a crafting grid, row by row.
 */
public final class CraftingContainer extends SimpleContainer {

    private final int width;
    private final int height;

    public CraftingContainer(final int width, final int height) {
        super(width * height);
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * @return a snapshot of the grid, for recipes to match against
     */
    public CraftingInput asInput() {
        return new CraftingInputImpl(width, height, List.of(contents()));
    }
}
