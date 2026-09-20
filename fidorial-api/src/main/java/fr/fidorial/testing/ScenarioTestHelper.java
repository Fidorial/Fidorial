package fr.fidorial.testing;

import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public final class ScenarioTestHelper {

    private final World world;
    private final ScenarioTestPlayerFactory playerFactory;
    private final ScenarioTestInfo info;
    private final List<Player> summonedPlayers = new ArrayList<>();

    ScenarioTestHelper(final World world, final ScenarioTestPlayerFactory playerFactory, final ScenarioTestInfo info) {
        this.world = world;
        this.playerFactory = playerFactory;
        this.info = info;
    }

    public World world() {
        return world;
    }

    /**
     * Summons a mock player into {@link #world()}.
     * @apiNote the player is automatically despawned upon test completion, regardless of its result.
     */
    public Player summonPlayer(final String name, final Location location, final GameMode gameMode) {
        final Player player = playerFactory.spawn(name, location, gameMode);
        summonedPlayers.add(player);
        return player;
    }

    public void fail(final String reason) {
        throw new ScenarioAssertionException(reason, info.tick());
    }

    public void assertTrue(final boolean condition, final String messageIfFalse) {
        if (!condition) {
            fail(messageIfFalse);
        }
    }

    public void assertBlockAt(final BlockPos pos, final Key expected) {
        final Key actual = world.blockKeyAt(pos).orElse(Key.key("air"));
        assertTrue(expected.equals(actual),
                "Expected " + expected + " at " + pos + " but found " + actual);
    }

    /**
     * Builds a sequence for this test.
     */
    public ScenarioTestSequence.Builder sequence() {
        info.sequenceBuilderCreated();
        return new ScenarioTestSequence.Builder(info);
    }

    void despawnPlayers() {
        for (final Player player : summonedPlayers) {
            playerFactory.despawn(player);
        }
        summonedPlayers.clear();
    }
}
