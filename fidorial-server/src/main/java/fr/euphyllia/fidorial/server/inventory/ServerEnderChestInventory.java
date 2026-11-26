package fr.euphyllia.fidorial.server.inventory;

import fr.fidorial.inventory.EnderChestInventory;

/**
 * The 27 slots of a player's ender chest.
 */
public final class ServerEnderChestInventory extends SimpleContainer implements EnderChestInventory {

    /**
     * Creates an empty ender chest.
     */
    public ServerEnderChestInventory() {
        super(SIZE);
    }

    /**
     * {@return a detached copy of {@code source}, slot for slot}
     *
     * @param source the ender chest to copy
     */
    public static ServerEnderChestInventory copyOf(final EnderChestInventory source) {
        final ServerEnderChestInventory copy = new ServerEnderChestInventory();
        final int slots = Math.min(SIZE, source.size());
        for (int slot = 0; slot < slots; slot++) {
            copy.set(slot, source.get(slot));
        }
        return copy;
    }
}
