package fr.euphyllia.fidorial.server.tests;

import alternate.current.wire.UpdateOrder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.configuration.WorldConfiguration;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.redstone.DustImplementation;
import fr.euphyllia.fidorial.server.world.redstone.ServerRedstoneWorld;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.event.redstone.RedstoneUpdateEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.euphyllia.fidorial.server.tests.RedstoneTests.Y;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.at;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.defaults;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.isLit;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.load;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.placeOnFloor;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.power;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.pull;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.redstone;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.server;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.set;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.tookTicks;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.waitFor;

/**
 * Redstone dust driven by Alternate Current: the power it settles on is vanilla's, it breaks like
 * vanilla's, it emits fewer block updates, and the configuration of a world picks it.
 *
 * <p>Each test runs in a world of its own, switched to Alternate Current whatever its configuration
 * says, so that the other redstone tests keep testing vanilla dust.</p>
 */
@SuppressWarnings("unused")
public final class AlternateCurrentTests {

    /**
     * Half the side of the square of dust around a lever.
     */
    private static final int GRID_RADIUS = 4;

    private AlternateCurrentTests() {
    }

    /**
     * Makes the dust of a world use an implementation, whatever its configuration says.
     */
    static ServerRedstoneWorld use(final World world, final @Nullable DustImplementation implementation) {
        final ServerRedstoneWorld redstone = (ServerRedstoneWorld) redstone(world);
        redstone.overrideDustImplementation(implementation);
        return redstone;
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void lineOfDustFadesByOnePerBlock(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 2);
        helper.sequence()
                .execute(() -> {
                    use(world, DustImplementation.ALTERNATE_CURRENT);
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    for (int x = 3; x <= 19; x++) {
                        placeOnFloor(world, Position.block(x, Y, 2), BlockTypeKeys.REDSTONE_WIRE);
                    }
                    pull(world, lever);
                    for (int x = 3; x <= 19; x++) {
                        final int expected = Math.max(0, 15 - (x - 3));
                        final int actual = power(world, Position.block(x, Y, 2));
                        helper.assertTrue(actual == expected, "Dust " + (x - 2) + " blocks from the lever should carry "
                                + expected + ", carries " + actual);
                    }
                    pull(world, lever);
                    for (int x = 3; x <= 19; x++) {
                        helper.assertTrue(power(world, Position.block(x, Y, 2)) == 0,
                                "Once the lever is off, no dust should stay powered, x=" + x);
                    }
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void vanillaSquareOfDustAroundALever(final ScenarioTestHelper helper) {
        squareOfDustAroundALever(helper, DustImplementation.VANILLA);
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void alternateCurrentSquareOfDustAroundALever(final ScenarioTestHelper helper) {
        squareOfDustAroundALever(helper, DustImplementation.ALTERNATE_CURRENT);
    }

    /**
     * A lever in the middle of a square of dust powers the whole square at once, and each dust
     * settles on fifteen minus its distance to the lever, plus one: the same with both
     * implementations. Switched off, Alternate Current turns each dust off once.
     */
    private static void squareOfDustAroundALever(final ScenarioTestHelper helper, final DustImplementation implementation) {
        final World world = helper.world();
        final int center = 8;
        final BlockPosition lever = Position.block(center, Y, center);
        helper.sequence()
                .execute(() -> {
                    use(world, implementation);
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    forEachDustAround(center, (x, z) -> placeOnFloor(world, Position.block(x, Y, z), BlockTypeKeys.REDSTONE_WIRE));
                    pull(world, lever);
                    forEachDustAround(center, (x, z) -> {
                        final int distance = Math.abs(x - center) + Math.abs(z - center);
                        final int expected = Math.max(0, 16 - distance);
                        final int actual = power(world, Position.block(x, Y, z));
                        helper.assertTrue(actual == expected, implementation.configName() + " dust at " + x + "," + z
                                + " should carry " + expected + ", carries " + actual);
                    });
                    final Map<BlockPosition, AtomicInteger> changes = new ConcurrentHashMap<>();
                    final Subscription subscription = server().events().subscribe(RedstoneDustPowerEvent.class, event -> {
                        if (event.world() == world) {
                            changes.computeIfAbsent(event.position(), _ -> new AtomicInteger()).incrementAndGet();
                        }
                    });
                    pull(world, lever);
                    subscription.unsubscribe();
                    forEachDustAround(center, (x, z) -> helper.assertTrue(power(world, Position.block(x, Y, z)) == 0,
                            implementation.configName() + " dust at " + x + "," + z + " should be off with the lever"));
                    if (implementation == DustImplementation.ALTERNATE_CURRENT) {
                        // vanilla dust steps down, some of it eight times on its way to 0
                        final int mostChanges = changes.values().stream().mapToInt(AtomicInteger::get).max().orElse(0);
                        helper.assertTrue(changes.size() == dustCount() && mostChanges == 1,
                                "Alternate Current should switch each dust off at once, " + changes.size() + " dust changed, "
                                        + mostChanges + " times at most");
                    }
                })
                .build();
    }

    private static int dustCount() {
        final int side = 2 * GRID_RADIUS + 1;
        return side * side - 1;
    }

    private static void forEachDustAround(final int center, final Cell cell) {
        for (int x = center - GRID_RADIUS; x <= center + GRID_RADIUS; x++) {
            for (int z = center - GRID_RADIUS; z <= center + GRID_RADIUS; z++) {
                if (x != center || z != center) {
                    cell.accept(x, z);
                }
            }
        }
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void dustBreaksWithTheBlockUnderIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 29);
        final BlockPosition dust = Position.block(3, Y, 29);
        final BlockPosition after = Position.block(4, Y, 29);
        helper.sequence()
                .execute(() -> {
                    use(world, DustImplementation.ALTERNATE_CURRENT);
                    load(world, dust);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, dust, BlockTypeKeys.REDSTONE_WIRE);
                    placeOnFloor(world, after, BlockTypeKeys.REDSTONE_WIRE);
                    pull(world, lever);
                    helper.assertTrue(power(world, after) == 14, "The second dust should carry 14, carries " + power(world, after));
                    set(world, dust.relative(BlockFace.DOWN), defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(at(world, dust).isAir(), "Dust without a block under it should break");
                    helper.assertTrue(power(world, after) == 0, "Dust no longer reached by the signal should go off, carries "
                            + power(world, after));
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void lampAtTheEndOfALineLightsAndGoesOut(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 20);
        final BlockPosition lamp = Position.block(9, Y, 20);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    use(world, DustImplementation.ALTERNATE_CURRENT);
                    load(world, lamp);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    for (int x = 3; x <= 8; x++) {
                        placeOnFloor(world, Position.block(x, Y, 20), BlockTypeKeys.REDSTONE_WIRE);
                    }
                    placeOnFloor(world, lamp, BlockTypeKeys.REDSTONE_LAMP);
                    pull(world, lever);
                    helper.assertTrue(isLit(world, lamp), "The lamp at the end of the line should light up at once");
                    pull(world, lever);
                })
                .waitUntil(waitFor(helper, tries, () -> !isLit(world, lamp), "The lamp should go out"))
                .execute(tookTicks(helper, tries, 4, "The lamp going out"))
                .build();
    }

    /**
     * Two copies of the same circuit, a line of dust over lamps, one switched with vanilla
     * dust and the other with Alternate Current: they settle the same, but Alternate Current only
     * updates the lamps, where vanilla dust also updates every dust many times.
     */
    @ScenarioTest(timeoutTicks = 40)
    public static void alternateCurrentEmitsFewerBlockUpdates(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int vanillaZ = 2;
        final int alternateZ = 10;
        final AtomicInteger updates = new AtomicInteger();
        final Subscription[] subscription = new Subscription[1];
        helper.sequence()
                .execute(() -> {
                    load(world, Position.block(2, Y, 2));
                    for (final int z : new int[]{vanillaZ, alternateZ}) {
                        placeOnFloor(world, Position.block(2, Y, z), BlockTypeKeys.LEVER);
                        for (int x = 3; x <= 17; x++) {
                            if (x % 2 == 0) {
                                // in the floor, under the dust that powers it
                                set(world, Position.block(x, Y - 1, z), defaults(BlockTypeKeys.REDSTONE_LAMP));
                            }
                            placeOnFloor(world, Position.block(x, Y, z), BlockTypeKeys.REDSTONE_WIRE);
                        }
                    }
                    subscription[0] = server().events().subscribe(RedstoneUpdateEvent.class, event -> {
                        if (event.world() == world) {
                            updates.incrementAndGet();
                        }
                    });
                })
                .execute(() -> {
                    use(world, DustImplementation.VANILLA);
                    updates.set(0);
                    pull(world, Position.block(2, Y, vanillaZ));
                    final int vanillaUpdates = updates.get();
                    use(world, DustImplementation.ALTERNATE_CURRENT);
                    updates.set(0);
                    pull(world, Position.block(2, Y, alternateZ));
                    final int alternateUpdates = updates.get();
                    subscription[0].unsubscribe();
                    for (int x = 3; x <= 17; x++) {
                        final int vanilla = power(world, Position.block(x, Y, vanillaZ));
                        final int alternate = power(world, Position.block(x, Y, alternateZ));
                        helper.assertTrue(vanilla == alternate, "Both lines should settle the same at x=" + x
                                + ", vanilla " + vanilla + ", Alternate Current " + alternate);
                        if (x % 2 == 0) {
                            helper.assertTrue(isLit(world, Position.block(x, Y - 1, vanillaZ)) && isLit(world, Position.block(x, Y - 1, alternateZ)),
                                    "Every lamp should light up, x=" + x);
                        }
                    }
                    helper.assertTrue(alternateUpdates * 2 <= vanillaUpdates, "Alternate Current should emit at most half the block updates: "
                            + alternateUpdates + " against " + vanillaUpdates + " for vanilla");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void configurationPicksTheImplementationOfAWorld(final ScenarioTestHelper helper) {
        final World world = helper.world();
        helper.sequence()
                .execute(() -> {
                    final ServerRedstoneWorld redstone = use(world, null);
                    helper.assertTrue(((ServerWorld) world).configuration().redstone().dustImplementation() == DustImplementation.VANILLA,
                            "Dust should be vanilla by default");
                    helper.assertTrue(redstone.dustImplementation() == DustImplementation.VANILLA && redstone.wireHandler() == null,
                            "A world configured with vanilla dust should have no Alternate Current handler");

                    final JsonObject json = JsonParser.parseString("""
                            {"redstone": {"dust-implementation": "alternate-current",
                                          "alternate-current": {"update-order": "vertical-first-inward"}}}
                            """).getAsJsonObject();
                    final DataResult<WorldConfiguration> parsed = WorldConfiguration.CODEC.parse(JsonOps.INSTANCE, json);
                    final WorldConfiguration configuration = parsed.getOrThrow();
                    helper.assertTrue(configuration.redstone().dustImplementation() == DustImplementation.ALTERNATE_CURRENT,
                            "dust-implementation: alternate-current should pick Alternate Current");
                    helper.assertTrue(configuration.redstone().alternateCurrent().updateOrder() == UpdateOrder.VERTICAL_FIRST_INWARD,
                            "update-order: vertical-first-inward should be read");
                    helper.assertTrue(configuration.storage().equals(WorldConfiguration.DEFAULTS.storage()),
                            "The settings left out should keep their defaults");

                    final JsonObject unknown = JsonParser.parseString("""
                            {"redstone": {"dust-implementation": "eigencraft"}}
                            """).getAsJsonObject();
                    helper.assertTrue(WorldConfiguration.CODEC.parse(JsonOps.INSTANCE, unknown).isError(),
                            "An unknown implementation should be refused");

                    redstone.overrideDustImplementation(DustImplementation.ALTERNATE_CURRENT);
                    helper.assertTrue(redstone.wireHandler() != null, "Alternate Current should give the world a handler");
                    redstone.overrideDustImplementation(null);
                })
                .build();
    }

    @FunctionalInterface
    private interface Cell {
        void accept(int x, int z);
    }
}
