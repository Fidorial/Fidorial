package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.redstone.ButtonBlock;
import fr.euphyllia.fidorial.server.world.block.redstone.LeverBlock;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.plugin.PluginContext;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.AnaloguePowerable;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.block.data.Lightable;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.block.data.type.Door;
import fr.fidorial.world.block.data.type.Repeater;
import fr.fidorial.world.block.data.type.Switch;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneRegistry;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * The redstone engine against vanilla: signal strengths, delays in game ticks, and blocks taking
 * part in redstone only through the behaviour attached to them.
 *
 * <p>Every circuit stands on the cobblestone floor of the test world, near its origin, so that the
 * thread running the test owns its blocks.</p>
 */
@SuppressWarnings("unused")
public final class RedstoneTests {

    /**
     * The first layer of air above the floor of the test worlds.
     */
    static final int Y = -48;

    private RedstoneTests() {
    }

    static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    static RedstoneWorld redstone(final World world) {
        return server().worlds().redstone().world(world);
    }

    static BlockData defaults(final TypedKey<BlockType> block) {
        return Objects.requireNonNull(Objects.requireNonNull(Blocks.type(block), "unknown block").defaultData());
    }

    /**
     * Sets a block the way the server does for a player or a command, redstone following up.
     */
    static void set(final World world, final BlockPosition position, final BlockData state) {
        final BlockStateRegistry states = server().registries().blockStates();
        server().worlds().blockEdits().set((ServerWorld) world, position, states.toBlockState(state), true);
    }

    /**
     * Places a block the way a player clicking {@code clickedFace} of its neighbour does.
     */
    static BlockData place(final World world, final BlockPosition position, final TypedKey<BlockType> block,
                                   final BlockFace clickedFace, final float yaw, final float pitch) {
        final BlockStateRegistry states = server().registries().blockStates();
        final BlockPlaceContext context = new BlockPlaceContext(position, clickedFace,
                Location.of(world, position.blockX() + 0.5, position.blockY() + 1.6, position.blockZ() + 0.5, yaw, pitch),
                states.view((ServerWorld) world), 0.5f);
        final BlockData placed = server().registries().blocks().behaviour(block).orElseThrow().placementState(context);
        Objects.requireNonNull(placed, () -> block + " cannot be placed at " + position);
        set(world, position, placed);
        return placed;
    }

    /**
     * Places a block on the floor, the player looking down at it.
     */
    static BlockData placeOnFloor(final World world, final BlockPosition position, final TypedKey<BlockType> block) {
        return place(world, position, block, BlockFace.UP, 0f, 90f);
    }

    static BlockData at(final World world, final BlockPosition position) {
        return Objects.requireNonNull(redstone(world).blockAt(position), () -> position + " is not loaded");
    }

    static int power(final World world, final BlockPosition position) {
        return at(world, position) instanceof final AnaloguePowerable dust ? dust.getPower() : -1;
    }

    static boolean isLit(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Lightable lightable && lightable.isLit();
    }

    static boolean isPowered(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Powerable powerable && powerable.isPowered();
    }

    static void pull(final World world, final BlockPosition lever) {
        LeverBlock.INSTANCE.pull(redstone(world), lever, (Switch) at(world, lever));
    }

    /**
     * Loads the chunk of a position: the engine only works on loaded chunks.
     */
    static void load(final World world, final BlockPosition position) {
        world.blockStateId(position);
    }

    /**
     * A wait counting the game ticks it took: its first try runs in the game tick of the step before.
     */
    static Runnable waitFor(final ScenarioTestHelper helper, final int[] tries, final BooleanSupplier condition,
                                    final String message) {
        return () -> {
            tries[0]++;
            helper.assertTrue(condition.getAsBoolean(), message);
        };
    }

    static Runnable tookTicks(final ScenarioTestHelper helper, final int[] tries, final int expected, final String what) {
        return () -> {
            final int ticks = tries[0] - 1;
            tries[0] = 0;
            helper.assertTrue(ticks == expected, what + " took " + ticks + " game ticks, vanilla takes " + expected);
        };
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void leverPowersALineOfDustThatFadesByOnePerBlock(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 2);
        helper.sequence()
                .execute(() -> {
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
    public static void torchTurnsOffTwoGameTicksAfterItsBlockIsPowered(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 5);
        final BlockPosition dust = Position.block(3, Y, 5);
        final BlockPosition stone = Position.block(4, Y, 5);
        final BlockPosition torch = Position.block(5, Y, 5);
        final BlockPosition output = Position.block(6, Y, 5);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, dust, BlockTypeKeys.REDSTONE_WIRE);
                    set(world, stone, defaults(BlockTypeKeys.STONE));
                    // clicked against the east face of the stone, looking west
                    final BlockData placed = place(world, torch, BlockTypeKeys.REDSTONE_TORCH, BlockFace.EAST, 90f, 0f);
                    helper.assertTrue(placed.key().equals(BlockTypeKeys.REDSTONE_WALL_TORCH),
                            "A torch placed against a wall should be a wall torch, is " + placed.key());
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    helper.assertTrue(isLit(world, torch) && power(world, output) == 15, "An unpowered torch should be lit and power the dust");
                    pull(world, lever);
                    helper.assertTrue(isLit(world, torch), "A torch should not turn off right away");
                })
                .waitUntil(waitFor(helper, tries, () -> !isLit(world, torch), "The torch should turn off"))
                .execute(tookTicks(helper, tries, 2, "Turning the torch off"))
                .execute(() -> helper.assertTrue(power(world, output) == 0, "An unlit torch should no longer power the dust"))
                .execute(() -> pull(world, lever))
                .waitUntil(waitFor(helper, tries, () -> isLit(world, torch), "The torch should light up again"))
                .execute(tookTicks(helper, tries, 2, "Lighting the torch back"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 200)
    public static void torchPoweringItsOwnBlockBurnsOut(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition support = Position.block(4, Y, 5);
        final BlockPosition torch = Position.block(5, Y, 5);
        final BlockPosition above = Position.block(5, Y + 1, 5);
        final BlockPosition loop = Position.block(4, Y + 1, 5);
        final AtomicInteger unlitFor = new AtomicInteger(0);
        final AtomicInteger waited = new AtomicInteger();
        final AtomicBoolean relitTooSoon = new AtomicBoolean(false);
        helper.sequence()
                .execute(() -> {
                    load(world, torch);
                    set(world, support, defaults(BlockTypeKeys.STONE));
                    set(world, above, defaults(BlockTypeKeys.STONE));
                    place(world, torch, BlockTypeKeys.REDSTONE_TORCH, BlockFace.EAST, 90f, 0f);
                    // the torch powers the stone above it, which powers the dust on the block holding the torch
                    place(world, loop, BlockTypeKeys.REDSTONE_WIRE, BlockFace.UP, 0f, 90f);
                })
                .waitUntil(() -> {
                    unlitFor.set(isLit(world, torch) ? 0 : unlitFor.get() + 1);
                    helper.assertTrue(unlitFor.get() >= 10, "A torch toggling every 2 game ticks should burn out");
                })
                .waitUntil(() -> {
                    if (isLit(world, torch)) {
                        relitTooSoon.set(true);
                    }
                    helper.assertTrue(waited.incrementAndGet() >= 100, "Waiting");
                })
                .execute(() -> {
                    // like vanilla: the tick that should restart the torch 160 game ticks later is dropped,
                    // the torch having one due already, so only a block update lights it up again
                    helper.assertFalse(relitTooSoon.get(), "A burnt out torch should stay off");
                    set(world, Position.block(6, Y, 5), defaults(BlockTypeKeys.STONE));
                })
                .waitUntil(() -> helper.assertTrue(isLit(world, torch),
                        "Updated once its recent toggles expired, a burnt out torch should light up again"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void dustPowersABlockOnlyForWhatIsNotDust(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 35);
        final BlockPosition stone = Position.block(4, Y, 35);
        final BlockPosition dustBehind = Position.block(5, Y, 35);
        helper.sequence()
                .execute(() -> {
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, Position.block(3, Y, 35), BlockTypeKeys.REDSTONE_WIRE);
                    set(world, stone, defaults(BlockTypeKeys.STONE));
                    placeOnFloor(world, dustBehind, BlockTypeKeys.REDSTONE_WIRE);
                    pull(world, lever);
                    helper.assertTrue(power(world, dustBehind) == 0,
                            "Dust behind a block powered by dust should stay unpowered, carries " + power(world, dustBehind));
                    helper.assertTrue(redstone(world).signal(stone, BlockFace.WEST) == 15,
                            "The block should still power what is not dust, such as a repeater or a torch");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void repeaterLockedByAnotherHoldsItsState(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition inputLever = Position.block(2, Y, 40);
        final BlockPosition repeater = Position.block(5, Y, 40);
        final BlockPosition output = Position.block(6, Y, 40);
        final BlockPosition lockLever = Position.block(5, Y, 43);
        final BlockPosition lock = Position.block(5, Y, 41);
        final int[] waited = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, repeater);
                    placeOnFloor(world, inputLever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, Position.block(3, Y, 40), BlockTypeKeys.REDSTONE_WIRE);
                    placeOnFloor(world, Position.block(4, Y, 40), BlockTypeKeys.REDSTONE_WIRE);
                    place(world, repeater, BlockTypeKeys.REPEATER, BlockFace.UP, -90f, 30f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    placeOnFloor(world, lockLever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, Position.block(5, Y, 42), BlockTypeKeys.REDSTONE_WIRE);
                    // looking north: this repeater points north, into the side of the first one
                    place(world, lock, BlockTypeKeys.REPEATER, BlockFace.UP, 180f, 30f);
                    pull(world, lockLever);
                })
                .waitUntil(() -> helper.assertTrue(((Repeater) at(world, repeater)).isLocked(),
                        "A repeater powered on its side by another one should lock"))
                .execute(() -> pull(world, inputLever))
                .waitUntil(() -> {
                    helper.assertTrue(power(world, output) == 0, "A locked repeater should not follow its input");
                    helper.assertTrue(++waited[0] >= 10, "Waiting");
                })
                .execute(() -> pull(world, lockLever))
                .waitUntil(() -> helper.assertTrue(power(world, output) == 15,
                        "Once unlocked, the repeater should follow its input"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void repeaterDelaysBy2GameTicksPerDelayStep(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(2, Y, 8);
        final BlockPosition repeater = Position.block(4, Y, 8);
        final BlockPosition output = Position.block(5, Y, 8);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, lever);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    placeOnFloor(world, Position.block(3, Y, 8), BlockTypeKeys.REDSTONE_WIRE);
                    // looking east: the repeater takes its input from the west
                    place(world, repeater, BlockTypeKeys.REPEATER, BlockFace.UP, -90f, 30f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    pull(world, lever);
                })
                .waitUntil(waitFor(helper, tries, () -> power(world, output) == 15, "The repeater should power the dust"))
                .execute(tookTicks(helper, tries, 2, "A repeater set to 1"))
                .execute(() -> {
                    pull(world, lever);
                    helper.assertTrue(isPowered(world, repeater), "The repeater should stay on for its delay");
                })
                .waitUntil(waitFor(helper, tries, () -> power(world, output) == 0, "The repeater should turn off"))
                .execute(tookTicks(helper, tries, 2, "Turning off a repeater set to 1"))
                .execute(() -> {
                    final Repeater state = (Repeater) at(world, repeater);
                    redstone(world).setBlock(repeater, state.withDelay(4), RedstoneWorld.Update.NEIGHBORS);
                    pull(world, lever);
                })
                .waitUntil(waitFor(helper, tries, () -> power(world, output) == 15, "The repeater should power the dust"))
                .execute(tookTicks(helper, tries, 8, "A repeater set to 4"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void comparatorReadsTheLevelOfAComposter(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition composter = Position.block(2, Y, 11);
        final BlockPosition comparator = Position.block(3, Y, 11);
        final BlockPosition output = Position.block(4, Y, 11);
        helper.sequence()
                .execute(() -> {
                    load(world, composter);
                    set(world, composter, defaults(BlockTypeKeys.COMPOSTER).with("level", "3"));
                    // looking east: the comparator reads the composter, west of it
                    place(world, comparator, BlockTypeKeys.COMPARATOR, BlockFace.UP, -90f, 30f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 3,
                        "A composter at level 3 should give 3 through a comparator, gives " + power(world, output)))
                .execute(() -> set(world, composter, defaults(BlockTypeKeys.COMPOSTER).with("level", "7")))
                .waitUntil(() -> helper.assertTrue(power(world, output) == 7,
                        "The comparator should follow the composter to 7, gives " + power(world, output)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void observerSendsATwoTickPulseWhenWhatItWatchesChanges(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition watched = Position.block(2, Y, 13);
        final BlockPosition observer = Position.block(2, Y, 14);
        final BlockPosition output = Position.block(2, Y, 15);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, observer);
                    // looking north: the face watches north, the back powers south
                    place(world, observer, BlockTypeKeys.OBSERVER, BlockFace.UP, 180f, 0f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    set(world, watched, defaults(BlockTypeKeys.STONE));
                })
                .waitUntil(waitFor(helper, tries, () -> power(world, output) == 15, "The observer should fire"))
                .execute(tookTicks(helper, tries, 2, "The observer firing"))
                .waitUntil(waitFor(helper, tries, () -> power(world, output) == 0, "The pulse should end"))
                .execute(tookTicks(helper, tries, 2, "The pulse of an observer"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void trappedChestPowersAsManyAsThePlayersLookingIntoIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(2, Y, 17);
        final BlockPosition output = Position.block(3, Y, 17);
        helper.sequence()
                .execute(() -> {
                    load(world, chest);
                    set(world, chest, defaults(BlockTypeKeys.TRAPPED_CHEST));
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    server().players().blockViewers().open(world.key(), chest);
                    server().players().blockViewers().open(world.key(), chest);
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 2,
                        "Two players looking into a trapped chest should give 2, gives " + power(world, output)))
                .execute(() -> {
                    server().players().blockViewers().close(world.key(), chest);
                    server().players().blockViewers().close(world.key(), chest);
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 0,
                        "A closed trapped chest should give nothing, gives " + power(world, output)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void lampLightsAtOnceAndGoesOutFourTicksLater(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(4, Y, 20);
        final BlockPosition lamp = Position.block(5, Y, 20);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, lamp);
                    placeOnFloor(world, lamp, BlockTypeKeys.REDSTONE_LAMP);
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    helper.assertTrue(!isLit(world, lamp), "An unpowered lamp should be placed off");
                    pull(world, lever);
                    helper.assertTrue(isLit(world, lamp), "A powered lamp should light up at once");
                    pull(world, lever);
                })
                .waitUntil(waitFor(helper, tries, () -> !isLit(world, lamp), "The lamp should go out"))
                .execute(tookTicks(helper, tries, 4, "The lamp going out"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void stoneButtonStaysPressedForTwentyTicks(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition button = Position.block(2, Y, 23);
        final BlockPosition output = Position.block(3, Y, 23);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, button);
                    placeOnFloor(world, button, BlockTypeKeys.STONE_BUTTON);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    final ButtonBlock stoneButton = ButtonBlock.ALL.stream()
                            .filter(candidate -> candidate.key().equals(BlockTypeKeys.STONE_BUTTON))
                            .findFirst().orElseThrow();
                    stoneButton.press(redstone(world), button, (Switch) at(world, button), null);
                    helper.assertTrue(power(world, output) == 15, "A pressed button should power the dust");
                })
                .waitUntil(waitFor(helper, tries, () -> power(world, output) == 0, "The button should pop back out"))
                .execute(tookTicks(helper, tries, 20, "A stone button popping back out"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void dustDisappearsWithTheBlockUnderIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition dust = Position.block(2, Y, 29);
        helper.sequence()
                .execute(() -> {
                    load(world, dust);
                    placeOnFloor(world, dust, BlockTypeKeys.REDSTONE_WIRE);
                    set(world, dust.relative(BlockFace.DOWN), defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(at(world, dust).isAir(), "Dust without a block under it should break");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void copperBulbTogglesOnEveryRisingEdge(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(4, Y, 31);
        final BlockPosition bulb = Position.block(5, Y, 31);
        helper.sequence()
                .execute(() -> {
                    load(world, bulb);
                    set(world, bulb, defaults(BlockTypeKeys.COPPER_BULB));
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    pull(world, lever);
                    helper.assertTrue(isLit(world, bulb), "The first pulse should light the bulb");
                    pull(world, lever);
                    helper.assertTrue(isLit(world, bulb), "Losing the signal should not switch the bulb");
                    pull(world, lever);
                    helper.assertTrue(!isLit(world, bulb), "The second pulse should switch the bulb off");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void signalOpensBothHalvesOfADoor(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition lever = Position.block(4, Y, 26);
        final BlockPosition lower = Position.block(5, Y, 26);
        final BlockPosition upper = Position.block(5, Y + 1, 26);
        helper.sequence()
                .execute(() -> {
                    load(world, lower);
                    final Door door = (Door) defaults(BlockTypeKeys.IRON_DOOR);
                    set(world, lower, door.withHalf(Bisected.Half.LOWER));
                    set(world, upper, door.withHalf(Bisected.Half.UPPER));
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    pull(world, lever);
                    helper.assertTrue(at(world, lower) instanceof final Openable open && open.isOpen(),
                            "A powered iron door should open");
                    helper.assertTrue(at(world, upper) instanceof final Openable open && open.isOpen(),
                            "The upper half should open with the lower one");
                    pull(world, lever);
                    helper.assertTrue(at(world, upper) instanceof final Openable open && !open.isOpen(),
                            "The door should close when the signal goes");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void onlyBlocksWithABehaviourTakePartInRedstone(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition source = Position.block(10, Y, 2);
        final BlockPosition output = Position.block(11, Y, 2);
        final RedstoneRegistry registry = server().registries().redstone();
        final Plugin owner = new Plugin() {
            @Override
            public void onLoad(final PluginContext context) {
                Plugin.super.onLoad(context);
            }
        };
        // what a plugin would attach: a block of diamond powering its neighbours like a block of redstone
        final RedstoneBehaviour powered = new RedstoneBehaviour() {
            @Override
            public boolean isSignalSource(final BlockData state) {
                return true;
            }

            @Override
            public int signal(final SignalGetter signals, final BlockPosition position, final BlockData state, final BlockFace direction) {
                return 15;
            }
        };
        helper.sequence()
                .execute(() -> {
                    helper.assertTrue(registry.behaviour(BlockTypeKeys.STONE).isEmpty(), "Stone should take no part in redstone");
                    helper.assertTrue(registry.behaviour(BlockTypeKeys.CHEST).isEmpty(), "A chest should take no part in redstone");
                    helper.assertTrue(registry.behaviour(BlockTypeKeys.TRAPPED_CHEST).isPresent(), "A trapped chest should");
                    load(world, source);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    set(world, source, defaults(BlockTypeKeys.DIAMOND_BLOCK));
                    helper.assertTrue(power(world, output) == 0, "A block of diamond should power nothing by default");
                    registry.attach(BlockTypeKeys.DIAMOND_BLOCK, powered, owner);
                    set(world, source, defaults(BlockTypeKeys.STONE));
                    set(world, source, defaults(BlockTypeKeys.DIAMOND_BLOCK));
                    helper.assertTrue(power(world, output) == 15, "With a behaviour attached, it should power the dust");
                    registry.detachAll(owner);
                    set(world, source, defaults(BlockTypeKeys.STONE));
                    helper.assertTrue(power(world, output) == 0, "Replaced by stone, it should no longer power the dust");
                    set(world, source, defaults(BlockTypeKeys.DIAMOND_BLOCK));
                    helper.assertTrue(power(world, output) == 0, "Detached, the behaviour should be gone");
                })
                .build();
    }
}
