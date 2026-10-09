package fr.fidorial.testing;

import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * The toolbox handed to a {@link ScenarioTest} method: its world,
 * mock players, assertions and {@linkplain #sequence() sequences}.
 *
 * @since 0.1.0
 */
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

    /**
     * {@return the world the test runs in}
     *
     * @since 0.1.0
     */
    public World world() {
        return world;
    }

    /**
     * Summons a mock player into {@link #world()}.
     *
     * @param name     the player name
     * @param location where to spawn the player
     * @param gameMode the game mode of the player
     * @return the summoned player
     * @apiNote the player is automatically despawned upon test completion, regardless of its result.
     * @since 0.1.0
     */
    public Player summonPlayer(final String name, final Location location, final GameMode gameMode) {
        return summonPlayer(name, world, location, gameMode);
    }

    /**
     * Summons a mock player into the specified world.
     *
     * @param name     the player name
     * @param world    the world to spawn the player in
     * @param location where to spawn the player
     * @param gameMode the game mode of the player
     * @return the summoned player
     * @apiNote the player is automatically despawned upon test completion, regardless of its result.
     * @since 0.1.0
     */
    public Player summonPlayer(final String name, final World world, final Location location, final GameMode gameMode) {
        final Player player = playerFactory.spawn(name, location, gameMode);
        summonedPlayers.add(player);
        return player;
    }

    /**
     * Fails the test right away.
     *
     * @param reason the failure message
     * @throws RuntimeException always, to abort the test
     * @since 0.1.0
     */
    public void fail(final String reason) {
        throw new ScenarioAssertionException(reason, info.tick());
    }

    /**
     * Fails the test unless a condition holds.
     *
     * @param condition      the condition to check
     * @param messageIfFalse the failure message
     * @since 0.1.0
     */
    public void assertTrue(final boolean condition, final String messageIfFalse) {
        if (!condition) {
            fail(messageIfFalse);
        }
    }

    /**
     * Fails the test unless a block of the expected type sits at a position.
     *
     * @param position the block position
     * @param expected the expected block key; {@code minecraft:air} matches any kind of air
     * @since 0.1.0
     */
    public void assertBlockAt(final BlockPosition position, final Key expected) {
        final Key actual = world.blockKeyAt(position).orElse(Key.key("air"));
        assertTrue(expected.equals(actual),
                "Expected " + expected + " at " + position + " but found " + actual);
    }

    /**
     * Starts building the sequence of steps of this test; the test passes once the built sequence completes.
     *
     * @return a sequence builder; call {@code build()} on it before the method returns
     * @since 0.1.0
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
