package fr.fidorial.testing;

import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;

/**
 * Builds a mock player for use by {@link ScenarioTestHelper}.
 *
 * @since 0.1.0
 */
public interface ScenarioTestPlayerFactory {

    /**
     * Spawns a mock player.
     *
     * @param name     the player name
     * @param world    the world to spawn in
     * @param location where to spawn
     * @param gameMode the game mode of the player
     * @return the mock player
     * @since 0.1.0
     */
    Player spawn(String name, World world, Location location, GameMode gameMode);

    /**
     * Removes a mock player spawned by this factory.
     *
     * @param player the player to remove
     * @since 0.1.0
     */
    void despawn(Player player);
}
