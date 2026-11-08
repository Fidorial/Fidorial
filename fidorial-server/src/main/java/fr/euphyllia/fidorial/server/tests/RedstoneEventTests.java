package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.world.redstone.DustImplementation;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.event.redstone.RedstoneEvent;
import fr.fidorial.event.redstone.RedstoneInputEvent;
import fr.fidorial.event.redstone.RedstoneOutputEvent;
import fr.fidorial.event.redstone.RedstoneUpdateEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static fr.euphyllia.fidorial.server.tests.RedstoneTests.Y;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.isLit;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.isPowered;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.load;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.place;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.placeOnFloor;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.power;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.pull;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.server;

/**
 * The redstone events: fired for what redstone does, and cancelling them stops it.
 *
 * <p>Listeners only look at the world of their own test, since the tests run side by side.</p>
 */
@SuppressWarnings("unused")
public final class RedstoneEventTests {

    private static final int LINE_LENGTH = 15;

    private RedstoneEventTests() {
    }

    /**
     * Subscribes a listener seeing only the events of one world.
     */
    private static <E extends RedstoneEvent> Subscription listen(final World world, final Class<E> type, final Consumer<E> listener) {
        return server().events().subscribe(type, event -> {
            if (event.world() == world) {
                listener.accept(event);
            }
        });
    }

    /**
     * Places a lever at x=2 and a line of dust east of it, from x=3.
     */
    private static void lineOfDust(final World world, final int z) {
        load(world, Position.block(2, Y, z));
        placeOnFloor(world, Position.block(2, Y, z), BlockTypeKeys.LEVER);
        for (int x = 3; x < 3 + LINE_LENGTH; x++) {
            placeOnFloor(world, Position.block(x, Y, z), BlockTypeKeys.REDSTONE_WIRE);
        }
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void vanillaDustFiresAPowerEventPerChange(final ScenarioTestHelper helper) {
        dustFiresAPowerEventPerChange(helper, DustImplementation.VANILLA);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void alternateCurrentDustFiresAPowerEventPerChange(final ScenarioTestHelper helper) {
        dustFiresAPowerEventPerChange(helper, DustImplementation.ALTERNATE_CURRENT);
    }

    /**
     * Every dust reached by the signal fires the event, the last one for each dust telling the
     * power it settles on; Alternate Current settles each dust at once.
     */
    private static void dustFiresAPowerEventPerChange(final ScenarioTestHelper helper, final DustImplementation implementation) {
        final World world = helper.world();
        final Map<BlockPosition, Integer> lastPower = new ConcurrentHashMap<>();
        final Map<BlockPosition, AtomicInteger> count = new ConcurrentHashMap<>();
        final Set<String> notDust = ConcurrentHashMap.newKeySet();
        helper.sequence()
                .execute(() -> {
                    AlternateCurrentTests.use(world, implementation);
                    lineOfDust(world, 2);
                    final Subscription subscription = listen(world, RedstoneDustPowerEvent.class, event -> {
                        if (!event.block().key().equals(BlockTypeKeys.REDSTONE_WIRE)) {
                            notDust.add(event.block().key().toString());
                        }
                        lastPower.put(event.position(), event.newPower());
                        count.computeIfAbsent(event.position(), _ -> new AtomicInteger()).incrementAndGet();
                    });
                    pull(world, Position.block(2, Y, 2));
                    subscription.unsubscribe();
                    helper.assertTrue(notDust.isEmpty(), "Dust power events should only be about dust, got " + notDust);
                    for (int x = 3; x < 3 + LINE_LENGTH; x++) {
                        final BlockPosition dust = Position.block(x, Y, 2);
                        final int expected = 15 - (x - 3);
                        helper.assertTrue(lastPower.getOrDefault(dust, -1) == expected, implementation.configName()
                                + ": the last event of the dust at x=" + x + " should give " + expected + ", gave " + lastPower.get(dust));
                        helper.assertTrue(power(world, dust) == expected, "The dust should carry what the event said, x=" + x);
                        final AtomicInteger events = count.get(dust);
                        if (implementation == DustImplementation.ALTERNATE_CURRENT) {
                            helper.assertTrue(events != null && events.get() == 1, "Alternate Current should set each dust once, x=" + x
                                    + " fired " + events + " events");
                        }
                    }
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void vanillaCancelledDustPowerEventStopsTheSignal(final ScenarioTestHelper helper) {
        cancelledDustPowerEventStopsTheSignal(helper, DustImplementation.VANILLA);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void alternateCurrentCancelledDustPowerEventStopsTheSignal(final ScenarioTestHelper helper) {
        cancelledDustPowerEventStopsTheSignal(helper, DustImplementation.ALTERNATE_CURRENT);
    }

    /**
     * Dust whose event is cancelled keeps its power, so the signal goes no further along the line.
     */
    private static void cancelledDustPowerEventStopsTheSignal(final ScenarioTestHelper helper, final DustImplementation implementation) {
        final World world = helper.world();
        final BlockPosition blocked = Position.block(7, Y, 2);
        helper.sequence()
                .execute(() -> {
                    AlternateCurrentTests.use(world, implementation);
                    lineOfDust(world, 2);
                    final Subscription subscription = listen(world, RedstoneDustPowerEvent.class, event -> {
                        if (event.position().equals(blocked)) {
                            event.setCancelled(true);
                        }
                    });
                    pull(world, Position.block(2, Y, 2));
                    subscription.unsubscribe();
                    for (int x = 3; x < 3 + LINE_LENGTH; x++) {
                        final int expected = x < blocked.blockX() ? 15 - (x - 3) : 0;
                        final int actual = power(world, Position.block(x, Y, 2));
                        helper.assertTrue(actual == expected, implementation.configName() + ": the dust at x=" + x
                                + " should carry " + expected + ", carries " + actual);
                    }
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void vanillaNewPowerIsPassedOn(final ScenarioTestHelper helper) {
        newPowerIsPassedOn(helper, DustImplementation.VANILLA);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void alternateCurrentNewPowerIsPassedOn(final ScenarioTestHelper helper) {
        newPowerIsPassedOn(helper, DustImplementation.ALTERNATE_CURRENT);
    }

    /**
     * Dust given another power by a listener passes that power on, one less per block.
     */
    private static void newPowerIsPassedOn(final ScenarioTestHelper helper, final DustImplementation implementation) {
        final World world = helper.world();
        final BlockPosition first = Position.block(3, Y, 2);
        helper.sequence()
                .execute(() -> {
                    AlternateCurrentTests.use(world, implementation);
                    lineOfDust(world, 2);
                    final Subscription subscription = listen(world, RedstoneDustPowerEvent.class, event -> {
                        if (event.position().equals(first) && event.newPower() == 15) {
                            event.setNewPower(6);
                        }
                    });
                    pull(world, Position.block(2, Y, 2));
                    subscription.unsubscribe();
                    for (int x = 3; x < 3 + LINE_LENGTH; x++) {
                        final int expected = Math.max(0, 6 - (x - 3));
                        final int actual = power(world, Position.block(x, Y, 2));
                        helper.assertTrue(actual == expected, implementation.configName() + ": the dust at x=" + x
                                + " should carry " + expected + ", carries " + actual);
                    }
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void cancelledInputEventKeepsTheLampOff(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(4, Y, 20);
        final BlockPosition lamp = Position.block(5, Y, 20);
        final AtomicInteger fired = new AtomicInteger();
        final AtomicInteger unpowered = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    load(world, lamp);
                    placeOnFloor(world, lamp, BlockTypeKeys.REDSTONE_LAMP);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    final Subscription subscription = listen(world, RedstoneInputEvent.class, event -> {
                        if (event.position().equals(lamp)) {
                            (event.powered() ? fired : unpowered).incrementAndGet();
                            event.setCancelled(true);
                        }
                    });
                    pull(world, lever);
                    subscription.unsubscribe();
                    helper.assertTrue(fired.get() > 0 && unpowered.get() == 0, "Powering the lamp should fire an input event telling it is powered");
                    helper.assertFalse(isLit(world, lamp), "A lamp whose input event is cancelled should stay off");
                    pull(world, lever);
                    pull(world, lever);
                    helper.assertTrue(isLit(world, lamp), "Without the listener, the lamp should light up again");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void cancelledOutputEventKeepsTheLeverWhereItWas(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 5);
        final BlockPosition dust = Position.block(3, Y, 5);
        final Set<String> seen = ConcurrentHashMap.newKeySet();
        helper.sequence()
                .execute(() -> {
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, dust, BlockTypeKeys.REDSTONE_WIRE);
                    final Subscription subscription = listen(world, RedstoneOutputEvent.class, event -> {
                        if (event.position().equals(lever)) {
                            seen.add(event.oldSignal() + "->" + event.newSignal() + (event.turningOn() ? " on" : ""));
                            event.setCancelled(true);
                        }
                    });
                    pull(world, lever);
                    subscription.unsubscribe();
                    helper.assertTrue(seen.equals(Set.of("0->15 on")), "The lever should be turning on, from 0 to 15, fired " + seen);
                    helper.assertFalse(isPowered(world, lever), "A lever whose output event is cancelled should not move");
                    helper.assertTrue(power(world, dust) == 0, "Nor should it power the dust");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void repeaterFiresAnOutputEventWhenItSwitches(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 8);
        final BlockPosition repeater = Position.block(4, Y, 8);
        final Set<String> seen = ConcurrentHashMap.newKeySet();
        final Subscription[] subscription = new Subscription[1];
        helper.sequence()
                .execute(() -> {
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, Position.block(3, Y, 8), BlockTypeKeys.REDSTONE_WIRE);
                    // looking east: the repeater takes its input from the west
                    place(world, repeater, BlockTypeKeys.REPEATER, BlockFace.UP, -90f, 30f);
                    subscription[0] = listen(world, RedstoneOutputEvent.class, event -> {
                        if (event.position().equals(repeater)) {
                            seen.add(event.oldSignal() + "->" + event.newSignal());
                        }
                    });
                    pull(world, lever);
                })
                .waitUntil(() -> helper.assertTrue(isPowered(world, repeater), "The repeater should turn on"))
                .execute(() -> pull(world, lever))
                .waitUntil(() -> helper.assertFalse(isPowered(world, repeater), "The repeater should turn off"))
                .execute(() -> {
                    subscription[0].unsubscribe();
                    helper.assertTrue(seen.contains("0->15") && seen.contains("15->0"),
                            "The repeater should fire an output event each way, fired " + seen);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void cancelledUpdateEventFreezesABlock(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(4, Y, 23);
        final BlockPosition lamp = Position.block(5, Y, 23);
        helper.sequence()
                .execute(() -> {
                    load(world, lamp);
                    placeOnFloor(world, lamp, BlockTypeKeys.REDSTONE_LAMP);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    final Subscription subscription = listen(world, RedstoneUpdateEvent.class, event -> {
                        if (event.position().equals(lamp)) {
                            event.setCancelled(true);
                        }
                    });
                    pull(world, lever);
                    subscription.unsubscribe();
                    helper.assertFalse(isLit(world, lamp), "A lamp whose updates are cancelled should not react");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void listenerOfRedstoneEventSeesEveryKind(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 26);
        final BlockPosition lamp = Position.block(5, Y, 26);
        final List<Class<? extends RedstoneEvent>> every = List.of(RedstoneDustPowerEvent.class, RedstoneInputEvent.class,
                RedstoneOutputEvent.class, RedstoneUpdateEvent.class);
        final Set<Class<? extends RedstoneEvent>> kinds = ConcurrentHashMap.newKeySet();
        helper.sequence()
                .execute(() -> {
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, Position.block(3, Y, 26), BlockTypeKeys.REDSTONE_WIRE);
                    placeOnFloor(world, Position.block(4, Y, 26), BlockTypeKeys.REDSTONE_WIRE);
                    placeOnFloor(world, lamp, BlockTypeKeys.REDSTONE_LAMP);
                    final Subscription subscription = listen(world, RedstoneEvent.class, event -> {
                        for (final Class<? extends RedstoneEvent> kind : every) {
                            if (kind.isInstance(event)) {
                                kinds.add(kind);
                            }
                        }
                    });
                    pull(world, lever);
                    subscription.unsubscribe();
                    helper.assertTrue(isLit(world, lamp), "The lamp should light up");
                    helper.assertTrue(kinds.containsAll(every), "A listener of every redstone event should see the four kinds, saw " + kinds);
                })
                .build();
    }
}
