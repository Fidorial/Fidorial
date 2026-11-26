package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * The window of an ender chest: the rows of the player's own ender chest they see, from one to six,
 * opened on a block whose lid opens while anyone looks inside.
 *
 * <p>The rows beyond are not in the window. When the configuration says so, they are emptied as the
 * window opens, what they held being logged; otherwise they keep it out of reach.</p>
 *
 * @see EnderChestRows
 */
public final class EnderChestMenu extends ContainerMenu {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EnderChestMenu.class);

    private final ServerWorld world;
    private final List<BlockPosition> block;
    private final int rows;
    private final HiddenRows hiddenRows;

    /**
     * @param rows       the rows of the ender chest shown, from {@code 1} to {@value EnderChestInventory#MAX_ROWS}
     * @param hiddenRows what becomes of the rows beyond
     */
    public EnderChestMenu(final ServerPlayer player, final int windowId, final ServerWorld world, final BlockPosition position,
                          final int rows, final HiddenRows hiddenRows) {
        super(player, windowId, position, BlockTypeKeys.ENDER_CHEST);
        Preconditions.checkArgument(rows >= 1 && rows <= EnderChestInventory.MAX_ROWS,
                "The window of an ender chest shows from 1 to %s rows, not %s", EnderChestInventory.MAX_ROWS, rows);
        Preconditions.checkArgument(hiddenRows != null,
                "The window of the ender chest of %s needs to know what becomes of the rows it does not show", player.name());
        this.world = world;
        this.block = List.of(position);
        this.rows = rows;
        this.hiddenRows = hiddenRows;
    }

    /**
     * {@return the window of the ender chest at {@code position}, showing the rows the player sees,
     * the rows beyond following the server configuration}
     */
    public static EnderChestMenu of(final ServerPlayer player, final ServerWorld world, final BlockPosition position) {
        return new EnderChestMenu(player, player.allocateWindowId(), world, position, player.enderChestRows(),
                player.connection().server().config().enderChest().hiddenRows());
    }

    /**
     * {@return the rows of the ender chest the window shows}
     */
    public int rows() {
        return rows;
    }

    @Override
    public Container container() {
        return player.enderChest();
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        final Container enderChest = player.enderChest();
        for (int i = 0; i < rows * EnderChestInventory.ROW_SIZE; i++) {
            slots.add(new Slot(enderChest, i));
        }
        addPlayerInventory(slots);
        return slots;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return genericRows(rows);
    }

    @Override
    public Component title() {
        return Component.translatable("container.enderchest");
    }

    @Override
    public void onOpened() {
        if (hiddenRows == HiddenRows.CLEAR) {
            final List<ItemStack> cleared = EnderChestRows.clearHidden(player.enderChest(), rows);
            if (!cleared.isEmpty()) {
                LOGGER.info("Cleared the rows {} sees no more in their ender chest, beyond row {}: {}",
                        player.name(), rows, HiddenRows.describe(cleared));
            }
        }
        BlockLid.open(server(), world, block, SoundEventKeys.BLOCK_ENDER_CHEST_OPEN);
    }

    @Override
    protected void onClosed() {
        BlockLid.close(server(), world, block, SoundEventKeys.BLOCK_ENDER_CHEST_CLOSE);
    }

    private FidorialServer server() {
        return player.connection().server();
    }
}
