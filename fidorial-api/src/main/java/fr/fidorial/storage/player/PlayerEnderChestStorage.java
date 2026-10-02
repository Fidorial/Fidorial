package fr.fidorial.storage.player;

import fr.fidorial.inventory.EnderChestInventory;

import java.io.IOException;
import java.util.UUID;

/**
 * Persistence backend for ender chests, following the same model as
 * {@link PlayerInventoryStorage} and {@link PlayerDataStorage}.
 *
 * @since 0.1.0
 */
public interface PlayerEnderChestStorage {

    /**
     * Loads a player's ender chest.
     *
     * @param uuid the player identity
     * @return the saved ender chest, or an empty one if the player never had one
     * @throws IOException if the ender chest cannot be read
     * @since 0.1.0
     */
    EnderChestInventory load(UUID uuid) throws IOException;

    /**
     * Saves the ender chest of a player.
     *
     * @param uuid       the player identity
     * @param enderChest the ender chest to save
     * @throws IOException if the ender chest cannot be written
     * @since 0.1.0
     */
    void save(UUID uuid, EnderChestInventory enderChest) throws IOException;
}
