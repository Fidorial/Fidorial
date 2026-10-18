package fr.fidorial.entity.player;

import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.inventory.PlayerInventory;

import java.util.UUID;

/**
 * The state of a player at the moment it was read.
 *
 * @param uuid       the identity this state belongs to
 * @param gameMode   the game mode held at the time of reading
 * @param inventory  a detached copy of the player inventory
 * @param enderChest a detached copy of the ender chest
 * @param live       {@code true} if the state was read from a connected player, {@code false} if it
 *                   was read from storage
 * @since 0.1.0
 */
public record OfflinePlayerSnapshot(
        UUID uuid,
        GameMode gameMode,
        PlayerInventory inventory,
        EnderChestInventory enderChest,
        boolean live
) {
}
