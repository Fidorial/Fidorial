package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BarrelBlock;
import fr.fidorial.inventory.BarrelInventory;
import fr.fidorial.math.BlockPosition;

/**
 * The slots of a barrel, kept in its block entity as {@link BlockEntityInventory} tells: the rows
 * it shows first, then up to {@value #MAX_ROWS} rows in all, those it no longer shows kept hidden.
 */
public final class BlockBarrelInventory extends BlockEntityInventory implements BarrelInventory {

    private final int rows;

    /**
     * @param rows the rows the barrel shows, from {@code 1} to {@value #MAX_ROWS}
     */
    public BlockBarrelInventory(final ServerWorld world, final BlockPosition position, final int rows) {
        super(world, position, "barrel", checkedRows(rows, position) * ROW_SIZE, MAX_ROWS * ROW_SIZE);
        this.rows = rows;
    }

    private static int checkedRows(final int rows, final BlockPosition position) {
        Preconditions.checkArgument(rows >= 1 && rows <= MAX_ROWS,
                "The barrel at %s shows from 1 to %s rows, not %s", position, MAX_ROWS, rows);
        return rows;
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    protected void contentsChanged() {
        BarrelBlock.contentsChanged(world(), position());
    }
}
