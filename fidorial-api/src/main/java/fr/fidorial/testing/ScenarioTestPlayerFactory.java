package fr.fidorial.testing;

import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;

/**
 * Builds a mock player for use by {@link ScenarioTestHelper}.
 */
public interface ScenarioTestPlayerFactory {

    Player spawn(String name, Location location, GameMode gameMode);

    void despawn(Player player);
}
