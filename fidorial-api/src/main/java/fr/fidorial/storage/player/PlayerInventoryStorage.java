package fr.fidorial.storage.player;

import fr.fidorial.inventory.PlayerInventory;

import java.io.IOException;
import java.util.UUID;

/**
 * Persistence backend for player inventories, following the same model as {@link PlayerDataStorage}.
 *
 * @since 0.1.0
 */
public interface PlayerInventoryStorage {

    /**
     * Loads the inventory of a player.
     *
     * @param uuid the player identity
     * @return the saved inventory, or an empty one if the player never had one
     * @throws IOException if the inventory cannot be read
     * @since 0.1.0
     */
    PlayerInventory load(UUID uuid) throws IOException;

    /**
     * Saves the inventory of a player.
     *
     * @param uuid      the player identity
     * @param inventory the inventory to save
     * @throws IOException if the inventory cannot be written
     * @since 0.1.0
     */
    void save(UUID uuid, PlayerInventory inventory) throws IOException;
}
