package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemOnPacket;
import fr.fidorial.entity.GameMode;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.block.BlockPlaceEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static fr.euphyllia.fidorial.server.tests.BlockDropTests.SETTLE_TICKS;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.TARGET;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.player;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.server;
import static fr.euphyllia.fidorial.server.tests.BlockDropTests.set;

/**
 * What placing a block takes out of the stack it comes from: one item in survival mode, the fluid
 * of a bucket, nothing in creative mode, and nothing when no block is placed.
 *
 * <p>The blocks are placed through the packet a client sends, on the top face of the floor of the
 * test world, at the position {@link BlockDropTests} breaks its blocks.</p>
 */
@SuppressWarnings("unused")
public final class BlockPlaceTests {

    /**
     * The block of the floor under {@link BlockDropTests#TARGET}, clicked to place a block there.
     */
    private static final BlockPosition FLOOR = TARGET.relative(BlockFace.DOWN);

    /**
     * The network ids of the main hand and of the top face.
     */
    private static final int MAIN_HAND = 0;
    private static final int FACE_UP = 1;

    private BlockPlaceTests() {
    }

    /**
     * Right-clicks the top face of a block with the main hand, the way a client does to place a
     * block on it.
     */
    private static void placeOnTopOf(final ServerPlayer player, final BlockPosition clicked) {
        player.connection().mockReceive(new ServerboundUseItemOnPacket(MAIN_HAND, clicked, FACE_UP, 0.5f, 1.0f, 0.5f, false, 0));
    }

    private static ItemStack held(final ServerPlayer player) {
        return player.inventory().get(player.selectedSlot());
    }

    @ScenarioTest
    public static void placingTakesOneBlockOutOfTheStack(final ScenarioTestHelper helper) {
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    player.set(player(helper, "place_mason", GameMode.SURVIVAL, ItemStack.of(ItemKeys.COBBLESTONE, 5)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.COBBLESTONE))
                .execute(() -> helper.assertTrue(held(player.get()).count() == 4,
                        "Placing a block should take one out of the stack, left " + held(player.get())))
                .build();
    }

    @ScenarioTest
    public static void placingTheLastBlockEmptiesTheHand(final ScenarioTestHelper helper) {
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    player.set(player(helper, "place_last", GameMode.SURVIVAL, ItemStack.of(ItemKeys.OAK_PLANKS, 1)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.OAK_PLANKS))
                .execute(() -> helper.assertTrue(held(player.get()).isEmpty(),
                        "Placing the last block should empty the hand, left " + held(player.get())))
                .build();
    }

    @ScenarioTest
    public static void creativePlacingKeepsTheStack(final ScenarioTestHelper helper) {
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    player.set(player(helper, "place_creative", GameMode.CREATIVE, ItemStack.of(ItemKeys.COBBLESTONE, 5)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.COBBLESTONE))
                .execute(() -> helper.assertTrue(held(player.get()).count() == 5,
                        "Placing in creative mode should take nothing, left " + held(player.get())))
                .build();
    }

    @ScenarioTest
    public static void waterBucketLeavesAnEmptyBucket(final ScenarioTestHelper helper) {
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    player.set(player(helper, "place_plumber", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WATER_BUCKET)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> helper.assertBlockAt(TARGET, BlockTypeKeys.WATER))
                .execute(() -> {
                    final ItemStack bucket = held(player.get());
                    helper.assertTrue(bucket.id().equals(ItemKeys.BUCKET) && bucket.count() == 1,
                            "Pouring the water should leave an empty bucket, left " + bucket);
                })
                .build();
    }

    @ScenarioTest
    public static void itemsPlacingNoBlockPlaceNothing(final ScenarioTestHelper helper) {
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    player.set(player(helper, "place_stick", GameMode.SURVIVAL, ItemStack.of(ItemKeys.STICK, 3)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a block to be placed")
                .execute(() -> {
                    helper.assertBlockAt(TARGET, BlockTypeKeys.AIR);
                    helper.assertTrue(held(player.get()).count() == 3, "A stick places nothing and stays, left " + held(player.get()));
                })
                .build();
    }

    @ScenarioTest
    public static void wheatPlacesNoCrop(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    set(world, FLOOR, "minecraft:farmland");
                    player.set(player(helper, "place_wheat", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WHEAT, 2)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a block to be placed")
                .execute(() -> {
                    helper.assertBlockAt(TARGET, BlockTypeKeys.AIR);
                    helper.assertTrue(held(player.get()).count() == 2, "Wheat is no seed and stays, left " + held(player.get()));
                })
                .build();
    }

    @ScenarioTest
    public static void cancelledPlacementKeepsTheStack(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final AtomicReference<ServerPlayer> player = new AtomicReference<>();
        final AtomicReference<Subscription> subscription = new AtomicReference<>();
        final AtomicInteger events = new AtomicInteger();
        final AtomicInteger settled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    subscription.set(server().events().subscribe(BlockPlaceEvent.class, event -> {
                        if (event.world() == world) {
                            events.incrementAndGet();
                            event.setCancelled(true);
                        }
                    }));
                    player.set(player(helper, "place_refused", GameMode.SURVIVAL, ItemStack.of(ItemKeys.COBBLESTONE, 5)));
                    placeOnTopOf(player.get(), FLOOR);
                })
                .waitUntil(() -> events.get() == 1, "The placement should fire its event")
                .waitUntil(() -> settled.incrementAndGet() >= SETTLE_TICKS, "Leave the time for a block to be placed")
                .execute(() -> {
                    subscription.get().unsubscribe();
                    helper.assertBlockAt(TARGET, BlockTypeKeys.AIR);
                    helper.assertTrue(held(player.get()).count() == 5, "A cancelled placement should take nothing, left " + held(player.get()));
                })
                .build();
    }
}
