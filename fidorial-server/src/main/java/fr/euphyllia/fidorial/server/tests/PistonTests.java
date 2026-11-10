package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.redstone.MovingPistons;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.redstone.PistonEvent;
import fr.fidorial.event.redstone.PistonExtendEvent;
import fr.fidorial.event.redstone.PistonRetractEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.Piston;
import fr.fidorial.world.block.data.type.PistonHead;
import fr.fidorial.world.block.data.type.TechnicalPiston;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static fr.euphyllia.fidorial.server.tests.RedstoneTests.Y;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.at;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.defaults;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.load;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.placeOnFloor;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.pull;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.server;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.set;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.tookTicks;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.waitFor;

/**
 * Pistons against vanilla: what they push, pull, break and leave alone, how long they take in game
 * ticks, and the quirks contraptions rely on.
 *
 * <p>Every piston faces east, with what powers it west of it, and stands near the origin of the
 * test world so that the thread running the test owns its blocks.</p>
 */
@SuppressWarnings("unused")
public final class PistonTests {

    /**
     * Where the pistons of the tests are, on the x axis: what powers them is one block west, what
     * they push one block east.
     */
    private static final int X = 4;

    private PistonTests() {
    }

    private static BlockPosition east(final int blocks, final int z) {
        return Position.block(X + blocks, Y, z);
    }

    private static BlockData piston(final TypedKey<BlockType> type) {
        return ((Piston) defaults(type)).withFacing(BlockFace.EAST);
    }

    private static boolean is(final World world, final BlockPosition position, final TypedKey<BlockType> block) {
        return at(world, position).key().equals(block);
    }

    private static boolean isExtended(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Piston piston && piston.isExtended();
    }

    /**
     * Powers a piston with a block of redstone next to it; air there takes the power away.
     */
    private static void power(final World world, final BlockPosition source, final boolean powered) {
        set(world, source, defaults(powered ? BlockTypeKeys.REDSTONE_BLOCK : BlockTypeKeys.AIR));
    }

    /**
     * A wait letting the given count of game ticks go by.
     */
    private static Runnable ticksLater(final ScenarioTestHelper helper, final int ticks) {
        final int[] seen = {0};
        return () -> helper.assertTrue(seen[0]++ >= ticks, "Waiting for " + ticks + " game ticks");
    }

    private static <E extends PistonEvent> Subscription listen(final World world, final Class<E> type, final Consumer<E> listener) {
        return server().events().subscribe(type, event -> {
            if (event.world() == world) {
                listener.accept(event);
            }
        });
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void pistonPushesItsBlockThenLeavesItThere(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition lever = east(-1, z);
        final BlockPosition base = east(0, z);
        final int[] tries = {0};
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.STONE));
                    placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    pull(world, lever);
                    helper.assertFalse(isExtended(world, base), "A piston moves on a block event, not on the update powering it");
                })
                .waitUntil(waitFor(helper, tries, () -> is(world, east(1, z), BlockTypeKeys.PISTON_HEAD) && is(world, east(2, z), BlockTypeKeys.STONE),
                        "The head should come out and push the stone one block"))
                // one game tick for the block event of an input coming from outside the tick, two for the move
                .execute(tookTicks(helper, tries, 3, "Extending the piston"))
                .execute(() -> {
                    helper.assertTrue(isExtended(world, base), "The base of an extended piston should be extended");
                    final BlockData head = at(world, east(1, z));
                    helper.assertTrue(head instanceof final PistonHead pistonHead && pistonHead.getFacing() == BlockFace.EAST
                            && !pistonHead.isSticky() && !pistonHead.isShort(), "The head should face east and be a normal one, is " + head);
                    pull(world, lever);
                    helper.assertTrue(isExtended(world, base), "A piston does not retract on the update either");
                })
                .waitUntil(waitFor(helper, tries, () -> is(world, base, BlockTypeKeys.PISTON) && !isExtended(world, base),
                        "The piston should retract"))
                .execute(tookTicks(helper, tries, 3, "Retracting the piston"))
                .execute(() -> {
                    helper.assertTrue(at(world, east(1, z)).isAir(), "The head of a retracted piston should be gone");
                    helper.assertBlockAt(east(2, z), BlockTypeKeys.STONE);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void whatAPistonMovesIsAMovingBlockForTwoGameTicks(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final BlockPosition head = east(1, z);
        final BlockPosition destination = east(2, z);
        final MovingPistons moving = server().worlds().movingPistons();
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.STICKY_PISTON));
                    set(world, head, defaults(BlockTypeKeys.STONE));
                    power(world, east(-1, z), true);
                })
                .waitUntil(ticksLater(helper, 1))
                .execute(() -> {
                    helper.assertTrue(isExtended(world, base), "The base is extended as soon as the piston starts moving");
                    helper.assertBlockAt(head, BlockTypeKeys.MOVING_PISTON);
                    helper.assertBlockAt(destination, BlockTypeKeys.MOVING_PISTON);
                    helper.assertTrue(at(world, head) instanceof final TechnicalPiston part && part.isSticky()
                            && part.getFacing() == BlockFace.EAST, "The moving head should be the one of a sticky piston facing east");
                    final MovingPistons.MovingBlock stone = moving.at(world, destination);
                    helper.assertTrue(stone != null && stone.moved().key().equals(BlockTypeKeys.STONE) && stone.extending()
                            && !stone.sourcePiston() && stone.progress() == 0.5f, "The stone should be half way to its destination");
                    final MovingPistons.MovingBlock movingHead = moving.at(world, head);
                    helper.assertTrue(movingHead != null && movingHead.moved().key().equals(BlockTypeKeys.PISTON_HEAD)
                            && movingHead.sourcePiston(), "The head should be moving as a part of the piston");
                    // what clients loading the chunk draw, and what a save keeps
                    final CompoundBinaryTag saved = ((ServerWorld) world).blockEntityData(destination);
                    helper.assertTrue(saved != null && "minecraft:stone".equals(saved.getCompound("blockState").getString("Name"))
                            && saved.getInt("facing") == BlockFace.EAST.ordinal() && saved.getBoolean("extending")
                            && !saved.getBoolean("source"), "The block entity of a moving block should hold what moves, holds " + saved);
                })
                .waitUntil(ticksLater(helper, 2))
                .execute(() -> {
                    helper.assertBlockAt(head, BlockTypeKeys.PISTON_HEAD);
                    helper.assertBlockAt(destination, BlockTypeKeys.STONE);
                    helper.assertTrue(moving.at(world, destination) == null && moving.at(world, head) == null,
                            "Nothing should be moving once the blocks landed");
                    helper.assertTrue(((ServerWorld) world).blockEntityData(destination) == null,
                            "A landed block should leave no block entity behind");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void stickyPistonPullsItsBlockBack(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final BlockPosition source = east(-1, z);
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.STICKY_PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.STONE));
                    power(world, source, true);
                })
                .waitUntil(() -> is(world, east(2, z), BlockTypeKeys.STONE), "The sticky piston should push the stone")
                .execute(() -> {
                    helper.assertTrue(at(world, east(1, z)) instanceof final PistonHead head && head.isSticky(),
                            "The head of a sticky piston should be sticky");
                    power(world, source, false);
                })
                .waitUntil(() -> is(world, base, BlockTypeKeys.STICKY_PISTON) && !isExtended(world, base), "The sticky piston should retract")
                .execute(() -> {
                    helper.assertBlockAt(east(1, z), BlockTypeKeys.STONE);
                    helper.assertTrue(at(world, east(2, z)).isAir(), "The pulled stone should have left where it was pushed to");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void stickyPistonLetsGoOfItsBlockOnAOneTickPulse(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final BlockPosition source = east(-1, z);
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.STICKY_PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.STONE));
                    power(world, source, true);
                })
                .waitUntil(ticksLater(helper, 1))
                .execute(() -> {
                    helper.assertBlockAt(east(2, z), BlockTypeKeys.MOVING_PISTON);
                    power(world, source, false);
                })
                .waitUntil(() -> is(world, base, BlockTypeKeys.STICKY_PISTON) && !isExtended(world, base), "The sticky piston should retract")
                .execute(() -> {
                    helper.assertBlockAt(east(2, z), BlockTypeKeys.STONE);
                    helper.assertTrue(at(world, east(1, z)).isAir(), "A block still being pushed when the piston retracts should be left behind");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void pistonPushesTwelveBlocksButNotThirteen(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int twelve = 2;
        final int thirteen = 6;
        helper.sequence()
                .execute(() -> {
                    load(world, east(0, twelve));
                    load(world, east(16, thirteen));
                    set(world, east(0, twelve), piston(BlockTypeKeys.PISTON));
                    set(world, east(0, thirteen), piston(BlockTypeKeys.PISTON));
                    for (int block = 1; block <= 12; block++) {
                        set(world, east(block, twelve), defaults(BlockTypeKeys.STONE));
                    }
                    for (int block = 1; block <= 13; block++) {
                        set(world, east(block, thirteen), defaults(BlockTypeKeys.STONE));
                    }
                    power(world, east(-1, twelve), true);
                    power(world, east(-1, thirteen), true);
                })
                .waitUntil(() -> is(world, east(1, twelve), BlockTypeKeys.PISTON_HEAD), "A piston should push twelve blocks")
                .execute(() -> {
                    for (int block = 2; block <= 13; block++) {
                        helper.assertBlockAt(east(block, twelve), BlockTypeKeys.STONE);
                    }
                    helper.assertFalse(isExtended(world, east(0, thirteen)), "A piston should not push thirteen blocks");
                    for (int block = 1; block <= 13; block++) {
                        helper.assertBlockAt(east(block, thirteen), BlockTypeKeys.STONE);
                    }
                    helper.assertTrue(at(world, east(14, thirteen)).isAir(), "Thirteen blocks should not have moved");
                    // one block less, and the piston moves on the next update
                    set(world, east(13, thirteen), defaults(BlockTypeKeys.AIR));
                    set(world, east(0, thirteen).relative(BlockFace.UP), defaults(BlockTypeKeys.STONE));
                })
                .waitUntil(() -> is(world, east(1, thirteen), BlockTypeKeys.PISTON_HEAD) && is(world, east(13, thirteen), BlockTypeKeys.STONE),
                        "With one block less, the piston should push the twelve left")
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void pistonLeavesWhatDoesNotMoveAlone(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int obsidian = 2;
        final int furnace = 5;
        final int extended = 8;
        helper.sequence()
                .execute(() -> {
                    load(world, east(0, obsidian));
                    set(world, east(0, obsidian), piston(BlockTypeKeys.PISTON));
                    set(world, east(1, obsidian), defaults(BlockTypeKeys.OBSIDIAN));
                    set(world, east(0, furnace), piston(BlockTypeKeys.PISTON));
                    set(world, east(1, furnace), defaults(BlockTypeKeys.STONE));
                    set(world, east(2, furnace), defaults(BlockTypeKeys.FURNACE));
                    // a piston facing up, extended: neither its base nor its head moves
                    set(world, east(0, extended), piston(BlockTypeKeys.PISTON));
                    set(world, east(1, extended), ((Piston) defaults(BlockTypeKeys.PISTON)).withFacing(BlockFace.UP));
                    power(world, east(1, extended).relative(BlockFace.SOUTH), true);
                })
                .waitUntil(() -> isExtended(world, east(1, extended)), "The piston facing up should extend")
                .execute(() -> {
                    power(world, east(-1, obsidian), true);
                    power(world, east(-1, furnace), true);
                    power(world, east(-1, extended), true);
                })
                .waitUntil(ticksLater(helper, 6))
                .execute(() -> {
                    helper.assertFalse(isExtended(world, east(0, obsidian)), "A piston should not push obsidian");
                    helper.assertBlockAt(east(1, obsidian), BlockTypeKeys.OBSIDIAN);
                    helper.assertFalse(isExtended(world, east(0, furnace)), "A piston should not push a block holding a block entity");
                    helper.assertBlockAt(east(1, furnace), BlockTypeKeys.STONE);
                    helper.assertBlockAt(east(2, furnace), BlockTypeKeys.FURNACE);
                    helper.assertFalse(isExtended(world, east(0, extended)), "A piston should not push an extended piston");
                    helper.assertTrue(isExtended(world, east(1, extended)), "The extended piston should be where it was");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void pistonBreaksWhatDoesNotWithstandIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int torch = 2;
        final int flower = 5;
        helper.sequence()
                .execute(() -> {
                    load(world, east(0, torch));
                    set(world, east(0, torch), piston(BlockTypeKeys.PISTON));
                    set(world, east(1, torch), defaults(BlockTypeKeys.TORCH));
                    set(world, east(0, flower), piston(BlockTypeKeys.PISTON));
                    set(world, east(1, flower), defaults(BlockTypeKeys.STONE));
                    set(world, east(2, flower), defaults(BlockTypeKeys.DANDELION));
                    power(world, east(-1, torch), true);
                    power(world, east(-1, flower), true);
                })
                .waitUntil(() -> is(world, east(1, torch), BlockTypeKeys.PISTON_HEAD) && is(world, east(1, flower), BlockTypeKeys.PISTON_HEAD),
                        "Both pistons should extend")
                .execute(() -> {
                    helper.assertTrue(at(world, east(2, torch)).isAir(), "A torch should break rather than be pushed");
                    helper.assertBlockAt(east(2, flower), BlockTypeKeys.STONE);
                    helper.assertTrue(at(world, east(3, flower)).isAir(), "A flower should break rather than be pushed");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void pistonIsPoweredThroughTheBlockAboveItOnceUpdated(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.STONE));
                    // powers the block above the piston, which is air: no update reaches the piston
                    set(world, Position.block(X, Y + 2, z), defaults(BlockTypeKeys.REDSTONE_BLOCK));
                })
                .waitUntil(ticksLater(helper, 5))
                .execute(() -> {
                    helper.assertFalse(isExtended(world, base), "A piston powered by quasi-connectivity should wait for an update");
                    set(world, base.relative(BlockFace.SOUTH), defaults(BlockTypeKeys.STONE));
                })
                .waitUntil(() -> is(world, east(2, z), BlockTypeKeys.STONE) && isExtended(world, base),
                        "Once updated, a piston powered by quasi-connectivity should extend")
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void slimeDragsItsNeighboursAlongButNotHoney(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 4;
        // above the floor, which slime would otherwise stick to
        final int y = Y + 2;
        final BlockPosition base = Position.block(X, y, z);
        final BlockPosition source = Position.block(X - 1, y, z);
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.STICKY_PISTON));
                    set(world, Position.block(X + 1, y, z), defaults(BlockTypeKeys.SLIME_BLOCK));
                    set(world, Position.block(X + 1, y + 1, z), defaults(BlockTypeKeys.STONE));
                    set(world, Position.block(X + 1, y, z + 1), defaults(BlockTypeKeys.HONEY_BLOCK));
                    power(world, source, true);
                })
                .waitUntil(() -> is(world, Position.block(X + 2, y, z), BlockTypeKeys.SLIME_BLOCK), "The piston should push the slime")
                .execute(() -> {
                    helper.assertBlockAt(Position.block(X + 2, y + 1, z), BlockTypeKeys.STONE);
                    helper.assertTrue(at(world, Position.block(X + 1, y + 1, z)).isAir(), "The stone on the slime should have moved with it");
                    helper.assertBlockAt(Position.block(X + 1, y, z + 1), BlockTypeKeys.HONEY_BLOCK);
                    power(world, source, false);
                })
                .waitUntil(() -> is(world, Position.block(X + 1, y, z), BlockTypeKeys.SLIME_BLOCK) && !isExtended(world, base),
                        "The sticky piston should pull the slime back")
                .execute(() -> {
                    helper.assertBlockAt(Position.block(X + 1, y + 1, z), BlockTypeKeys.STONE);
                    helper.assertTrue(at(world, Position.block(X + 2, y + 1, z)).isAir(), "The stone on the slime should have come back with it");
                    helper.assertBlockAt(Position.block(X + 1, y, z + 1), BlockTypeKeys.HONEY_BLOCK);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void glazedTerracottaIsPushedButNotPulled(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final BlockPosition source = east(-1, z);
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.STICKY_PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.WHITE_GLAZED_TERRACOTTA));
                    power(world, source, true);
                })
                .waitUntil(() -> is(world, east(2, z), BlockTypeKeys.WHITE_GLAZED_TERRACOTTA), "The piston should push glazed terracotta")
                .execute(() -> power(world, source, false))
                .waitUntil(() -> is(world, base, BlockTypeKeys.STICKY_PISTON) && !isExtended(world, base), "The sticky piston should retract")
                .execute(() -> {
                    helper.assertBlockAt(east(2, z), BlockTypeKeys.WHITE_GLAZED_TERRACOTTA);
                    helper.assertTrue(at(world, east(1, z)).isAir(), "A sticky piston should not pull glazed terracotta");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void headAndBaseBreakTogether(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int baseBroken = 2;
        final int headBroken = 5;
        helper.sequence()
                .execute(() -> {
                    load(world, east(0, baseBroken));
                    set(world, east(0, baseBroken), piston(BlockTypeKeys.PISTON));
                    set(world, east(0, headBroken), piston(BlockTypeKeys.STICKY_PISTON));
                    power(world, east(-1, baseBroken), true);
                    power(world, east(-1, headBroken), true);
                })
                .waitUntil(() -> is(world, east(1, baseBroken), BlockTypeKeys.PISTON_HEAD) && is(world, east(1, headBroken), BlockTypeKeys.PISTON_HEAD),
                        "Both pistons should extend")
                .execute(() -> {
                    set(world, east(0, baseBroken), defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(at(world, east(1, baseBroken)).isAir(), "The head should break with its base");
                    set(world, east(1, headBroken), defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(at(world, east(0, headBroken)).isAir(), "The base should break with its head");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void extendEventTellsWhatMovesAndCancelsIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final AtomicInteger fired = new AtomicInteger();
        final AtomicReference<String> wrong = new AtomicReference<>();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        final boolean[] cancel = {true};
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.STONE));
                    set(world, east(2, z), defaults(BlockTypeKeys.STONE));
                    set(world, east(3, z), defaults(BlockTypeKeys.TORCH));
                    subscription.set(listen(world, PistonExtendEvent.class, event -> {
                        fired.incrementAndGet();
                        if (!event.position().equals(base) || event.direction() != BlockFace.EAST || event.movement() != BlockFace.EAST
                                || event.sticky() || !event.movedBlocks().equals(List.of(east(1, z), east(2, z)))
                                || !event.brokenBlocks().equals(List.of(east(3, z)))) {
                            wrong.set("moved " + event.movedBlocks() + ", broken " + event.brokenBlocks() + ", towards " + event.movement());
                        }
                        event.setCancelled(cancel[0]);
                    }));
                    power(world, east(-1, z), true);
                })
                .waitUntil(ticksLater(helper, 5))
                .execute(() -> {
                    helper.assertTrue(fired.get() == 1, "Extending should fire one event, fired " + fired.get());
                    helper.assertTrue(wrong.get() == null, "The extend event should tell what the piston moves and breaks: " + wrong.get());
                    helper.assertFalse(isExtended(world, base), "A piston whose extend event is cancelled should stay retracted");
                    helper.assertBlockAt(east(1, z), BlockTypeKeys.STONE);
                    helper.assertBlockAt(east(3, z), BlockTypeKeys.TORCH);
                    // the next update tries again
                    cancel[0] = false;
                    set(world, base.relative(BlockFace.SOUTH), defaults(BlockTypeKeys.STONE));
                })
                .waitUntil(() -> is(world, east(1, z), BlockTypeKeys.PISTON_HEAD) && is(world, east(3, z), BlockTypeKeys.STONE),
                        "Once the event is let through, the piston should extend")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertTrue(fired.get() == 2, "The second try should fire the event again, fired " + fired.get());
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void retractEventTellsWhatAStickyPistonPulls(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final BlockPosition source = east(-1, z);
        final List<List<BlockPosition>> pulled = new CopyOnWriteArrayList<>();
        final AtomicReference<String> wrong = new AtomicReference<>();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.STICKY_PISTON));
                    set(world, east(1, z), defaults(BlockTypeKeys.STONE));
                    subscription.set(listen(world, PistonRetractEvent.class, event -> {
                        pulled.add(event.movedBlocks());
                        if (!event.position().equals(base) || !event.sticky() || event.movement() != BlockFace.WEST
                                || !(event.block() instanceof final Piston piston) || !piston.isExtended()) {
                            wrong.set(event.block() + " towards " + event.movement());
                        }
                    }));
                    power(world, source, true);
                })
                .waitUntil(() -> is(world, east(2, z), BlockTypeKeys.STONE), "The sticky piston should push the stone")
                .execute(() -> power(world, source, false))
                .waitUntil(() -> is(world, east(1, z), BlockTypeKeys.STONE) && !isExtended(world, base), "The sticky piston should pull the stone back")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertTrue(wrong.get() == null, "The retract event should be about the extended sticky piston: " + wrong.get());
                    helper.assertTrue(pulled.equals(List.of(List.of(east(2, z)))),
                            "Retracting should fire one event telling the stone is pulled, told " + pulled);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void pistonPushesTheEntitiesInItsWay(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int z = 2;
        final BlockPosition base = east(0, z);
        final AtomicReference<ServerItemEntity> item = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    load(world, base);
                    set(world, base, piston(BlockTypeKeys.PISTON));
                    // lying on the floor, in the middle of the block the head comes out into
                    item.set(server().entities().dropItem((ServerWorld) world, X + 1.5, Y, z + 0.5,
                            ItemStack.of(ItemKeys.STICK, 1), 0.0, 0.0, 0.0, 0, null));
                })
                .waitUntil(() -> item.get().onGround(), "The item should rest on the floor")
                .execute(() -> power(world, east(-1, z), true))
                .waitUntil(() -> is(world, east(1, z), BlockTypeKeys.PISTON_HEAD), "The piston should extend")
                .execute(() -> {
                    final double x = item.get().location().x();
                    helper.assertFalse(item.get().isRemoved(), "The item should still be there");
                    helper.assertTrue(x > X + 2, "The head should have pushed the item out of its block, the item is at x=" + x);
                    helper.assertTrue(x < X + 3, "The item should not be pushed further than the head went, the item is at x=" + x);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void blockSavedWhileMovingMovesOnOnceItsChunkIsRead(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = east(2, 2);
        final MovingPistons moving = server().worlds().movingPistons();
        helper.sequence()
                .execute(() -> {
                    load(world, position);
                    final ServerWorld serverWorld = (ServerWorld) world;
                    // what a chunk saved in the middle of a move holds, in the vanilla format
                    set(world, position, ((TechnicalPiston) defaults(BlockTypeKeys.MOVING_PISTON)).withFacing(BlockFace.EAST));
                    serverWorld.setBlockEntityData(position, CompoundBinaryTag.builder()
                            .put("blockState", CompoundBinaryTag.builder()
                                    .putString("Name", "minecraft:oak_log")
                                    .put("Properties", CompoundBinaryTag.builder().putString("axis", "x").build())
                                    .build())
                            .putInt("facing", BlockFace.EAST.ordinal())
                            .putFloat("progress", 0.5f)
                            .putBoolean("extending", true)
                            .putBoolean("source", false)
                            .build());
                    helper.assertTrue(moving.at(world, position) == null, "Nothing moves the block yet");
                    moving.resume(serverWorld, serverWorld.loadedColumn(position.chunkX(), position.chunkZ()));
                })
                .waitUntil(() -> is(world, position, BlockTypeKeys.OAK_LOG), "The block should land as what it was saved moving")
                .execute(() -> {
                    helper.assertTrue("x".equals(at(world, position).get("axis")), "The block should land in the state it was saved in");
                    helper.assertTrue(moving.at(world, position) == null, "Nothing should be moving once the block landed");
                })
                .build();
    }
}
