package fr.fidorial.testing;

import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;

/**
 * Builds a mock player for use by {@link ScenarioTestHelper}.
 */
public interface ScenarioTestPlayerFactory {

    Player spawn(String name, World world, Location location, GameMode gameMode);

    void despawn(Player player);
}
