package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.redstone.DustImplementation;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.region.RegionMergeEvent;
import fr.fidorial.event.region.RegionSplitEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Regions owning a margin around what they tick, merging when their margins meet and splitting once
 * they drift apart.
 *
 * <p>Skipped when {@code threading.region-buffer-radius} is {@code 0}: regions never merge then.
 * The merge and split test is worked out for the default margin of one section and sections of 16
 * chunks or more, and skipped otherwise.</p>
 */
@SuppressWarnings("unused")
public final class RegionTests {

    private RegionTests() {
    }

    private static ThreadedRegionRegionizer regionizer() {
        return FidorialServer.getInstance().workers().regionizer();
    }

    /**
     * {@return the chunk in the middle of a section}
     */
    private static ChunkPos chunkOfSection(final int sectionX, final int sectionZ) {
        final int size = ThreadedRegionRegionizer.sectionSize();
        return new ChunkPos(sectionX * size + size / 2, sectionZ * size + size / 2);
    }

    /**
     * A test runs on the region of the origin of its world, so the sections around the origin are its
     * own: a block can be written past the border of the origin section, the way a piston pushes.
     */
    @ScenarioTest(timeoutTicks = 20)
    public static void marginPastASectionBorderIsOwned(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final int size = ThreadedRegionRegionizer.sectionSize();
        final boolean merging = regionizer().bufferRadius() > 0;
        helper.sequence()
                .execute(() -> {
                    if (!merging) {
                        return;
                    }
                    helper.assertTrue(world.scheduler().isOwnedByCurrentThread(world.key(), new ChunkPos(size, 0)),
                            "The section east of the origin should belong to the region of the test");
                    helper.assertTrue(world.scheduler().isOwnedByCurrentThread(world.key(), new ChunkPos(-1, -1)),
                            "The section north-west of the origin should belong to the region of the test");
                    helper.assertFalse(world.scheduler().isOwnedByCurrentThread(world.key(), chunkOfSection(40, 0)),
                            "A section far from the origin should not belong to the region of the test");

                    final BlockPosition pastTheBorder = Position.block(size * 16, RedstoneTests.Y, 0);
                    RedstoneTests.load(world, pastTheBorder);
                    RedstoneTests.set(world, pastTheBorder, RedstoneTests.defaults(BlockTypeKeys.STONE));
                    helper.assertBlockAt(pastTheBorder, BlockTypeKeys.STONE);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void vanillaDustCarriesPowerPastASectionBorder(final ScenarioTestHelper helper) {
        dustCarriesPowerPastASectionBorder(helper, DustImplementation.VANILLA);
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void alternateCurrentDustCarriesPowerPastASectionBorder(final ScenarioTestHelper helper) {
        dustCarriesPowerPastASectionBorder(helper, DustImplementation.ALTERNATE_CURRENT);
    }

    /**
     * A line of 15 dust whose middle sits on the border of the origin section: the power fades by one
     * per block all along it, with no hand over to another region.
     */
    private static void dustCarriesPowerPastASectionBorder(final ScenarioTestHelper helper, final DustImplementation implementation) {
        final World world = helper.world();
        final boolean merging = regionizer().bufferRadius() > 0;
        final int border = ThreadedRegionRegionizer.sectionSize() * 16; // first block of the next section
        final BlockPosition lever = Position.block(border - 8, RedstoneTests.Y, 2);
        helper.sequence()
                .execute(() -> {
                    if (!merging) {
                        return;
                    }
                    AlternateCurrentTests.use(world, implementation);
                    RedstoneTests.load(world, lever);
                    RedstoneTests.load(world, Position.block(border + 8, RedstoneTests.Y, 2));
                    RedstoneTests.placeOnFloor(world, lever, BlockTypeKeys.LEVER);
                    for (int x = border - 7; x <= border + 7; x++) {
                        RedstoneTests.placeOnFloor(world, Position.block(x, RedstoneTests.Y, 2), BlockTypeKeys.REDSTONE_WIRE);
                    }
                    RedstoneTests.pull(world, lever);
                    for (int x = border - 7; x <= border + 7; x++) {
                        final int expected = 15 - (x - (border - 7));
                        final int actual = RedstoneTests.power(world, Position.block(x, RedstoneTests.Y, 2));
                        helper.assertTrue(actual == expected, implementation.configName() + ": the dust at x=" + x
                                + " (border at " + border + ") should carry " + expected + ", carries " + actual);
                    }
                    RedstoneTests.pull(world, lever);
                    for (int x = border - 7; x <= border + 7; x++) {
                        helper.assertTrue(RedstoneTests.power(world, Position.block(x, RedstoneTests.Y, 2)) == 0,
                                implementation.configName() + ": once the lever is off, no dust should stay powered, x=" + x);
                    }
                })
                .build();
    }

    /**
     * A water source next to the border of the origin section flows into the next one.
     */
    @ScenarioTest(timeoutTicks = 60)
    public static void waterFlowsPastASectionBorder(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final boolean merging = regionizer().bufferRadius() > 0;
        final int border = ThreadedRegionRegionizer.sectionSize() * 16;
        final BlockPosition source = Position.block(border - 1, RedstoneTests.Y, 2);
        final BlockPosition past = Position.block(border, RedstoneTests.Y, 2);
        helper.sequence()
                .execute(() -> {
                    if (!merging) {
                        return;
                    }
                    // fluids only tick where blocks tick: make the chunks on both sides of the border tick
                    ChunkTickingTests.tickAround((ServerWorld) world, source.chunkX(), 0);
                    RedstoneTests.load(world, source);
                    RedstoneTests.load(world, past);
                    RedstoneTests.set(world, source, RedstoneTests.defaults(BlockTypeKeys.WATER));
                })
                .waitUntil(() -> !merging || RedstoneTests.at(world, past).key().equals(BlockTypeKeys.WATER),
                        "The water should flow past the border of the section")
                .execute(() -> {
                    if (merging) {
                        ((ServerWorld) world).tickingChunks().remove(source.chunkX(), 0, 0);
                    }
                })
                .build();
    }

    /**
     * Two regions apart merge once a ticket bridges their margins, and split again once it goes.
     *
     * <p>With a margin of one section: tickets on sections 40 and 45 own 39..41 and 44..46, which do
     * not touch. A ticket on 42 owns 41..43, touching both. Once it goes, 42 and 43 die: a quarter of
     * the 24 sections, enough for the region to recalculate its shape and split. Like Folia, smaller
     * sections only recalculate in regions of 2048 chunks or more, which these 24 sections are not.</p>
     */
    @ScenarioTest(timeoutTicks = 200)
    public static void regionsMergeWhenTheirMarginsMeetAndSplitOnceApart(final ScenarioTestHelper helper) {
        final Key world = helper.world().key();
        final boolean merging = regionizer().bufferRadius() == 1 && ThreadedRegionRegionizer.sectionShift() >= 4;
        final ChunkPos west = chunkOfSection(40, 0);
        final ChunkPos east = chunkOfSection(45, 0);
        final ChunkPos bridge = chunkOfSection(42, 0);
        final OwnershipProbe together = new OwnershipProbe(world, west, east);
        final List<RegionMergeEvent> merges = new CopyOnWriteArrayList<>();
        final List<RegionSplitEvent> splits = new CopyOnWriteArrayList<>();
        final Subscription[] subscriptions = new Subscription[2];

        helper.sequence()
                .execute(() -> {
                    if (!merging) {
                        return;
                    }
                    subscriptions[0] = FidorialServer.getInstance().events().subscribe(RegionMergeEvent.class, event -> {
                        if (event.world().equals(world)) {
                            merges.add(event);
                        }
                    });
                    subscriptions[1] = FidorialServer.getInstance().events().subscribe(RegionSplitEvent.class, event -> {
                        if (event.world().equals(world)) {
                            splits.add(event);
                        }
                    });
                    regionizer().addTicket(world, west);
                    regionizer().addTicket(world, east);
                })
                .waitUntil(() -> !merging || together.is(false), "Two tickets far apart should make two regions")
                .execute(() -> {
                    if (merging) {
                        regionizer().addTicket(world, bridge);
                    }
                })
                .waitUntil(() -> !merging || together.is(true), "A ticket between the two regions should merge them")
                .waitUntil(() -> !merging || merges.stream().anyMatch(e -> e.into().contains(west) && e.into().contains(east)),
                        "A merge event should tell the regions were merged")
                .execute(() -> {
                    if (merging) {
                        regionizer().removeTicket(world, bridge);
                    }
                })
                .waitUntil(() -> !merging || together.is(false), "Without the ticket between them, the region should split")
                .waitUntil(() -> !merging || splits.stream().anyMatch(e -> e.into().size() >= 2
                                && e.into().stream().noneMatch(r -> r.contains(west) && r.contains(east))),
                        "A split event should tell the region was split")
                .execute(() -> {
                    if (!merging) {
                        return;
                    }
                    regionizer().removeTicket(world, west);
                    regionizer().removeTicket(world, east);
                    for (final Subscription subscription : subscriptions) {
                        subscription.unsubscribe();
                    }
                })
                .build();
    }

    /**
     * Asks the region owning {@code from} whether it owns {@code other} too, on its own thread.
     */
    private static final class OwnershipProbe {

        private final Key world;
        private final ChunkPos from;
        private final ChunkPos other;
        private volatile @Nullable CompletableFuture<Boolean> pending;

        OwnershipProbe(final Key world, final ChunkPos from, final ChunkPos other) {
            this.world = world;
            this.from = from;
            this.other = other;
        }

        /**
         * {@return whether the last answer was {@code expected}}; asks again unless it was.
         */
        boolean is(final boolean expected) {
            final CompletableFuture<Boolean> current = pending;
            if (current != null && !current.isDone()) {
                return false;
            }
            if (current != null && current.join() == expected) {
                pending = null;
                return true;
            }
            final CompletableFuture<Boolean> next = new CompletableFuture<>();
            pending = next;
            regionizer().execute(world, from, () -> next.complete(regionizer().isOwnedByCurrentThread(world, other)));
            return false;
        }
    }
}
