package fr.euphyllia.fidorial.server.inventory;

import fr.fidorial.inventory.PlayerInventory;

/**
 * The inventory a player carries, laid out as {@link PlayerInventory} describes.
 */
public final class ServerPlayerInventory extends SimpleContainer implements PlayerInventory {

    /**
     * Creates an empty inventory.
     */
    public ServerPlayerInventory() {
        super(SIZE);
    }

    /**
     * {@return a detached copy of {@code source}, slot for slot}
     *
     * @param source the inventory to copy
     */
    public static ServerPlayerInventory copyOf(final PlayerInventory source) {
        final ServerPlayerInventory copy = new ServerPlayerInventory();
        final int slots = Math.min(SIZE, source.size());
        for (int slot = 0; slot < slots; slot++) {
            copy.set(slot, source.get(slot));
        }
        return copy;
    }
}
