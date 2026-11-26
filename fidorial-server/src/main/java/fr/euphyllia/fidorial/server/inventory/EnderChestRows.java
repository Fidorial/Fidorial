package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.permission.DefaultPermissions;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.permission.PermissionDefinition;
import fr.fidorial.permission.PermissionHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * How many rows of their ender chest a player sees, from the
 * {@code fidorial.enderchest.rows.<one|two|three|four|five|six>} permissions, and what becomes of
 * the rows they no longer see.
 *
 * <p>A player sees the most rows a permission granted to them gives, even fewer than the default:
 * granting {@code fidorial.enderchest.rows.one} alone shrinks an ender chest to one row. A player
 * granted none of them sees the default rows of the configuration. The permissions are granted to
 * nobody by default, operators included, so that losing the operator status never shrinks an ender
 * chest.</p>
 */
public final class EnderChestRows {

    /**
     * The root of the permissions, followed by the name of a number of rows.
     */
    public static final String PERMISSION_ROOT = DefaultPermissions.ROOT + ".enderchest.rows";

    private static final List<String> NAMES = List.of("one", "two", "three", "four", "five", "six");

    private EnderChestRows() {
        throw new UnsupportedOperationException("EnderChestRows cannot be instantiated.");
    }

    /**
     * {@return the permission giving a number of rows, such as {@code fidorial.enderchest.rows.six}}
     *
     * @param rows from {@code 1} to {@value EnderChestInventory#MAX_ROWS}
     */
    public static String permission(final int rows) {
        checkRows(rows);
        return PERMISSION_ROOT + "." + NAMES.get(rows - 1);
    }

    /**
     * {@return the permissions to declare: one per number of rows, and the one granting them all}
     */
    public static List<PermissionDefinition> permissions() {
        final List<PermissionDefinition> permissions = new ArrayList<>();
        permissions.add(PermissionDefinition.explicitOnly(PERMISSION_ROOT + ".*",
                "See the " + EnderChestInventory.MAX_ROWS + " rows of one's ender chest."));
        for (int rows = 1; rows <= EnderChestInventory.MAX_ROWS; rows++) {
            permissions.add(PermissionDefinition.explicitOnly(permission(rows),
                    "See " + rows + (rows == 1 ? " row" : " rows") + " of one's ender chest."));
        }
        return List.copyOf(permissions);
    }

    /**
     * {@return how many rows of their ender chest a holder sees}
     *
     * @param holder      the player
     * @param defaultRows the rows of a player granted none of the permissions
     */
    public static int of(final PermissionHolder holder, final int defaultRows) {
        Preconditions.checkArgument(holder != null, "The rows of an ender chest are worked out for a player, not null");
        checkRows(defaultRows);
        for (int rows = EnderChestInventory.MAX_ROWS; rows >= 1; rows--) {
            if (holder.hasPermission(permission(rows))) {
                return rows;
            }
        }
        return defaultRows;
    }

    /**
     * Empties the slots of the rows beyond {@code rows}, the ones a player does not see.
     *
     * @param enderChest the ender chest
     * @param rows       the rows the player sees
     * @return the stacks taken out, in slot order, empty when the hidden rows held nothing
     */
    public static List<ItemStack> clearHidden(final EnderChestInventory enderChest, final int rows) {
        Preconditions.checkArgument(enderChest != null, "Only an ender chest has hidden rows to clear, not null");
        checkRows(rows);
        final List<ItemStack> cleared = new ArrayList<>();
        for (int slot = rows * EnderChestInventory.ROW_SIZE; slot < enderChest.size(); slot++) {
            final ItemStack stack = enderChest.get(slot);
            if (!stack.isEmpty()) {
                cleared.add(stack);
                enderChest.set(slot, ItemStack.EMPTY);
            }
        }
        return List.copyOf(cleared);
    }

    private static void checkRows(final int rows) {
        Preconditions.checkArgument(rows >= 1 && rows <= EnderChestInventory.MAX_ROWS,
                "An ender chest shows from 1 to %s rows, not %s", EnderChestInventory.MAX_ROWS, rows);
    }
}
