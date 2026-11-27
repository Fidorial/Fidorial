package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.configuration.WorldConfiguration;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.CrafterContainer;
import fr.euphyllia.fidorial.server.inventory.HopperMenu;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.block.HopperEvent;
import fr.fidorial.event.block.HopperMoveItemEvent;
import fr.fidorial.event.block.HopperPickupItemEvent;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.DoubleChestInventory;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.type.Hopper;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

import static fr.euphyllia.fidorial.server.tests.RedstoneTests.Y;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.at;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.defaults;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.load;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.place;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.placeOnFloor;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.power;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.server;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.set;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.tookTicks;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.waitFor;

/**
 * Hoppers against vanilla: where their spout points, pushing into the container they face and
 * pulling from the one above at the pace of their cooldown, picking up the items above them,
 * locked by redstone, read by comparators, spreading items over a crafter; and the cooldown and
 * the items moved at once the configuration of their world gives.
 *
 * <p>Every hopper stands in the region of the origin, so that the thread running the test owns it,
 * and ticks before the steps of the test in each game tick.</p>
 */
@SuppressWarnings("unused")
public final class HopperTests {

    private static final float LOOKING_NORTH = 180f;

    private HopperTests() {
    }

    /**
     * Places a hopper the way a player clicking {@code clickedFace} of the block it is placed
     * against does.
     */
    private static BlockPosition hopper(final World world, final int x, final int y, final int z, final BlockFace clickedFace) {
        final BlockPosition position = Position.block(x, y, z);
        load(world, position);
        place(world, position, BlockTypeKeys.HOPPER, clickedFace, 0f, 90f);
        return position;
    }

    /**
     * Places a hopper on top of the block below it, its spout pointing down.
     */
    private static BlockPosition hopper(final World world, final int x, final int y, final int z) {
        return hopper(world, x, y, z, BlockFace.UP);
    }

    private static BlockPosition chest(final World world, final int x, final int y, final int z) {
        final BlockPosition position = Position.block(x, y, z);
        load(world, position);
        place(world, position, BlockTypeKeys.CHEST, BlockFace.UP, LOOKING_NORTH, 30f);
        return position;
    }

    private static HopperInventory inventory(final World world, final BlockPosition position) {
        return world.hopperAt(position).orElseThrow(() -> new AssertionError("No hopper inventory at " + position));
    }

    private static ChestInventory chestInventory(final World world, final BlockPosition position) {
        return world.chestAt(position).orElseThrow(() -> new AssertionError("No chest inventory at " + position));
    }

    private static int count(final Container container, final TypedKey<Item> item) {
        int count = 0;
        for (final ItemStack stack : container.contents()) {
            if (!stack.isEmpty() && stack.id().equals(item)) {
                count += stack.count();
            }
        }
        return count;
    }

    private static boolean isEnabled(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Hopper hopper && hopper.isEnabled();
    }

    /**
     * Drops a stack at an exact spot, without the random push of {@link World#dropItem}.
     */
    private static ServerItemEntity dropStill(final World world, final double x, final double y, final double z,
                                              final ItemStack stack) {
        return server().entities().dropItem((ServerWorld) world, x, y, z, stack, 0.0, 0.0, 0.0, 40, null);
    }

    private static int onTheGround(final World world, final TypedKey<Item> item) {
        int count = 0;
        for (final var entity : world.entities()) {
            if (entity instanceof final ItemEntity dropped && !dropped.isRemoved() && dropped.item().id().equals(item)) {
                count += dropped.item().count();
            }
        }
        return count;
    }

    /**
     * A wait that passes once {@code ticks} game ticks went by since the step before.
     */
    private static Runnable waitTicks(final ScenarioTestHelper helper, final int ticks) {
        final int[] waited = new int[1];
        return () -> {
            waited[0]++;
            helper.assertTrue(waited[0] > ticks, "Waiting " + ticks + " game ticks");
        };
    }

    /**
     * Gives the hoppers of a world another cooldown and another number of items moved at once.
     */
    private static void configure(final World world, final int transferCooldown, final int itemsPerTransfer) {
        final ServerWorld serverWorld = (ServerWorld) world;
        final WorldConfiguration configuration = serverWorld.configuration();
        serverWorld.setConfiguration(new WorldConfiguration(configuration.gameplay(), configuration.storage(),
                configuration.redstone(), configuration.mobs(), new WorldConfiguration.Blocks(configuration.blocks().barrel(),
                new WorldConfiguration.Hopper(transferCooldown, itemsPerTransfer))));
    }

    /**
     * Subscribes a listener seeing only the events of one world.
     */
    private static <E extends HopperEvent> Subscription listen(final World world, final Class<E> type, final Consumer<E> listener) {
        return server().events().subscribe(type, event -> {
            if (event.world() == world) {
                listener.accept(event);
            }
        });
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void hopperPointsToTheBlockItWasPlacedAgainstAndOpensItsFiveSlots(final ScenarioTestHelper helper) {
        final World world = helper.world();
        helper.sequence()
                .execute(() -> {
                    final BlockPosition onTop = hopper(world, 4, Y, 4, BlockFace.UP);
                    final BlockPosition sideways = hopper(world, 6, Y, 4, BlockFace.NORTH);
                    final BlockPosition below = hopper(world, 8, Y + 2, 4, BlockFace.DOWN);
                    helper.assertTrue(at(world, onTop) instanceof final Hopper hopper && hopper.getFacing() == BlockFace.DOWN
                                    && hopper.isEnabled(),
                            "A hopper placed on top of a block should point down, enabled, got " + at(world, onTop).asString());
                    helper.assertTrue(at(world, sideways) instanceof final Hopper hopper && hopper.getFacing() == BlockFace.SOUTH,
                            "A hopper placed against the north face of a block should point south into it, got "
                                    + at(world, sideways).asString());
                    helper.assertTrue(at(world, below) instanceof final Hopper hopper && hopper.getFacing() == BlockFace.DOWN,
                            "A hopper placed under a block should point down, got " + at(world, below).asString());

                    final HopperInventory slots = inventory(world, onTop);
                    helper.assertTrue(slots.size() == HopperInventory.SIZE && slots.isEmpty() && slots.transferCooldown() == 0,
                            "A new hopper should have five empty slots and be ready");
                    helper.assertTrue(world.chestAt(onTop).isEmpty() && world.barrelAt(onTop).isEmpty(),
                            "A hopper is neither a chest nor a barrel");
                    try {
                        slots.setTransferCooldown(-1);
                        helper.fail("A negative cooldown should be refused");
                    } catch (final IllegalArgumentException expected) {
                        // refused
                    }

                    final ServerPlayer player = (ServerPlayer) helper.summonPlayer("HopperUser",
                            Location.of(world, 4.5, Y, 6.5, LOOKING_NORTH, 30f), GameMode.SURVIVAL);
                    final BlockData data = at(world, onTop);
                    helper.assertTrue(server().registries().blocks().behaviour(data.key()).orElseThrow().use(player, onTop, data)
                                    && player.containerMenu() instanceof final HopperMenu menu
                                    && menu.menuType().equals(MenuTypeKeys.HOPPER) && menu.slotCount() == 5 + 36
                                    && menu.title().equals(Component.translatable("container.hopper")),
                            "Using a hopper should open its five slots, titled as a hopper");
                    player.closeContainer();
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void hopperPushesOneItemEveryEightTicksIntoTheChestBelow(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(4, Y, 4);
        final BlockPosition hopper = Position.block(4, Y + 1, 4);
        final int[] tries = new int[1];
        helper.sequence()
                .execute(() -> {
                    chest(world, 4, Y, 4);
                    hopper(world, 4, Y + 1, 4);
                    inventory(world, hopper).set(0, ItemStack.of(ItemKeys.DIAMOND, 3));
                })
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 1,
                        "The hopper should push a first diamond into the chest"))
                .execute(tookTicks(helper, tries, 1, "A ready hopper pushing"))
                .execute(() -> helper.assertTrue(count(inventory(world, hopper), ItemKeys.DIAMOND) == 2
                                && inventory(world, hopper).transferCooldown() == 8,
                        "The hopper should push one item at a time, then wait 8 game ticks"))
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 2,
                        "The hopper should push a second diamond"))
                .execute(tookTicks(helper, tries, 8, "The cooldown of a hopper"))
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 3,
                        "The hopper should push the last diamond"))
                .execute(tookTicks(helper, tries, 8, "The cooldown of a hopper"))
                .execute(() -> helper.assertTrue(inventory(world, hopper).isEmpty(), "The hopper should be empty"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void hopperPullsFromTheBarrelAboveIntoTheLargeChestBelow(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition left = Position.block(4, Y, 4);
        final BlockPosition right = Position.block(5, Y, 4);
        final BlockPosition hopper = Position.block(5, Y + 1, 4);
        final BlockPosition barrel = Position.block(5, Y + 2, 4);
        helper.sequence()
                .execute(() -> {
                    chest(world, 4, Y, 4);
                    chest(world, 5, Y, 4);
                    final DoubleChestInventory large = chestInventory(world, right).doubleChest()
                            .orElseThrow(() -> new AssertionError("The two chests should form a large chest"));
                    // the first 27 slots are full: the items go to the other half
                    for (int slot = 0; slot < 27; slot++) {
                        large.set(slot, ItemStack.of(ItemKeys.COBBLESTONE, 64));
                    }
                    load(world, barrel);
                    set(world, barrel, defaults(BlockTypeKeys.BARREL));
                    world.barrelAt(barrel).orElseThrow().set(3, ItemStack.of(ItemKeys.EMERALD, 2));
                    hopper(world, 5, Y + 1, 4);
                })
                .waitUntil(() -> helper.assertTrue(world.barrelAt(barrel).orElseThrow().isEmpty(),
                        "The hopper should pull every emerald out of the barrel above it"))
                .waitUntil(() -> {
                    final DoubleChestInventory large = chestInventory(world, left).doubleChest().orElseThrow();
                    helper.assertTrue(large.get(27).equals(ItemStack.of(ItemKeys.EMERALD, 2)),
                            "The emeralds should go into the first slot of the large chest with room, got " + large.get(27));
                })
                .execute(() -> helper.assertTrue(inventory(world, hopper).isEmpty(), "The hopper should pass every emerald on"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void itemsGoDownALineOfHoppersAtThePaceOfTheCooldown(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition firstFirst = Position.block(4, Y + 1, 4);
        final BlockPosition firstSecond = Position.block(5, Y + 1, 4);
        final BlockPosition firstChest = Position.block(5, Y, 4);
        final BlockPosition secondFirst = Position.block(4, Y + 1, 8);
        final BlockPosition secondSecond = Position.block(5, Y + 1, 8);
        final BlockPosition secondChest = Position.block(5, Y, 8);
        final int[] tries = new int[1];
        helper.sequence()
                .execute(() -> {
                    chest(world, 5, Y, 4);
                    chest(world, 5, Y, 8);
                    // the feeding hopper ticks first in the first line, last in the second
                    hopper(world, 4, Y + 1, 4, BlockFace.WEST);
                    hopper(world, 5, Y + 1, 4);
                    hopper(world, 5, Y + 1, 8);
                    hopper(world, 4, Y + 1, 8, BlockFace.WEST);
                    helper.assertTrue(at(world, firstFirst) instanceof final Hopper hopper && hopper.getFacing() == BlockFace.EAST,
                            "A hopper placed against the west face of a block should point east into it");
                    inventory(world, firstFirst).set(0, ItemStack.of(ItemKeys.GOLD_INGOT, 1));
                    inventory(world, secondFirst).set(0, ItemStack.of(ItemKeys.GOLD_INGOT, 1));
                })
                .waitUntil(waitFor(helper, tries, () -> count(inventory(world, firstSecond), ItemKeys.GOLD_INGOT) == 1
                                && count(inventory(world, secondSecond), ItemKeys.GOLD_INGOT) == 1,
                        "The first hoppers should feed the second ones"))
                .execute(tookTicks(helper, tries, 1, "A ready hopper pushing"))
                .execute(() -> helper.assertTrue(count(chestInventory(world, firstChest), ItemKeys.GOLD_INGOT) == 0
                                && count(chestInventory(world, secondChest), ItemKeys.GOLD_INGOT) == 0,
                        "A hopper just fed should not pass the item on in the same game tick"))
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, firstChest), ItemKeys.GOLD_INGOT) == 1
                                && count(chestInventory(world, secondChest), ItemKeys.GOLD_INGOT) == 1,
                        "The second hoppers should pass the items on to the chests"))
                .execute(tookTicks(helper, tries, 7, "A hopper fed by another one, whichever ticks first,"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void hopperPicksUpTheItemsAboveItUnlessAFullBlockCoversIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition open = Position.block(4, Y, 4);
        final BlockPosition covered = Position.block(8, Y, 4);
        final BlockPosition rail = Position.block(12, Y, 4);
        helper.sequence()
                .execute(() -> {
                    hopper(world, 4, Y, 4);
                    hopper(world, 8, Y, 4);
                    set(world, covered.relative(BlockFace.UP), defaults(BlockTypeKeys.STONE));
                    hopper(world, 12, Y, 4);
                    set(world, rail.relative(BlockFace.UP), defaults(BlockTypeKeys.RAIL));
                    inventory(world, open).setTransferCooldown(30);
                    dropStill(world, 4.5, Y + 1.2, 4.5, ItemStack.of(ItemKeys.DIAMOND, 5));
                    dropStill(world, 8.5, Y + 1.5, 4.5, ItemStack.of(ItemKeys.EMERALD, 5));
                    dropStill(world, 12.5, Y + 1.5, 4.5, ItemStack.of(ItemKeys.GOLD_INGOT, 5));
                })
                .waitUntil(() -> helper.assertTrue(count(inventory(world, open), ItemKeys.DIAMOND) == 5
                                && onTheGround(world, ItemKeys.DIAMOND) == 0,
                        "A hopper should pick up the whole stack above it, even waiting its cooldown"))
                .waitUntil(() -> helper.assertTrue(count(inventory(world, rail), ItemKeys.GOLD_INGOT) == 5,
                        "A rail above a hopper should not keep it from picking up the items falling through it"))
                .waitUntil(waitTicks(helper, 10))
                .execute(() -> helper.assertTrue(inventory(world, covered).isEmpty() && onTheGround(world, ItemKeys.EMERALD) == 5,
                        "A full block above a hopper should keep it from picking up items"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void aPoweredHopperIsLockedButStillReceives(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(4, Y, 4);
        final BlockPosition locked = Position.block(4, Y + 1, 4);
        final BlockPosition feeder = Position.block(3, Y + 1, 4);
        final BlockPosition power = Position.block(5, Y + 1, 4);
        helper.sequence()
                .execute(() -> {
                    chest(world, 4, Y, 4);
                    hopper(world, 4, Y + 1, 4);
                    set(world, power, defaults(BlockTypeKeys.REDSTONE_BLOCK));
                    hopper(world, 3, Y + 1, 4, BlockFace.WEST);
                    inventory(world, locked).set(0, ItemStack.of(ItemKeys.DIAMOND, 1));
                    inventory(world, feeder).set(0, ItemStack.of(ItemKeys.EMERALD, 1));
                })
                .waitUntil(() -> helper.assertFalse(isEnabled(world, locked), "A powered hopper should be locked"))
                .waitUntil(() -> helper.assertTrue(count(inventory(world, locked), ItemKeys.EMERALD) == 1,
                        "A locked hopper should still take the items another hopper pushes into it"))
                .waitUntil(waitTicks(helper, 12))
                .execute(() -> {
                    helper.assertTrue(chestInventory(world, chest).isEmpty() && count(inventory(world, locked), ItemKeys.DIAMOND) == 1,
                            "A locked hopper should push nothing");
                    set(world, power, defaults(BlockTypeKeys.AIR));
                })
                .waitUntil(() -> helper.assertTrue(isEnabled(world, locked), "Once unpowered, the hopper should be enabled again"))
                .waitUntil(() -> helper.assertTrue(count(chestInventory(world, chest), ItemKeys.DIAMOND) == 1
                                && count(chestInventory(world, chest), ItemKeys.EMERALD) == 1,
                        "Once unlocked, the hopper should push its items into the chest"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void comparatorReadsHowFullTheHopperIs(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, Y, 14);
        final BlockPosition comparator = Position.block(3, Y, 14);
        final BlockPosition output = Position.block(4, Y, 14);
        helper.sequence()
                .execute(() -> {
                    hopper(world, 2, Y, 14);
                    // looking east: the comparator reads the hopper west of it
                    place(world, comparator, BlockTypeKeys.COMPARATOR, BlockFace.UP, -90f, 30f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    helper.assertTrue(power(world, output) == 0, "An empty hopper should give nothing");
                    inventory(world, position).set(0, ItemStack.of(ItemKeys.COBBLESTONE, 1));
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 1,
                        "A single item should give 1, gives " + power(world, output)))
                .execute(() -> {
                    final HopperInventory slots = inventory(world, position);
                    for (int slot = 0; slot < slots.size(); slot++) {
                        slots.set(slot, ItemStack.of(ItemKeys.COBBLESTONE, 64));
                    }
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 15,
                        "A full hopper should give 15, gives " + power(world, output)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void hopperSpreadsItemsOverTheEnabledSlotsOfACrafter(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition crafter = Position.block(4, Y, 4);
        final BlockPosition hopper = Position.block(4, Y + 1, 4);
        helper.sequence()
                .execute(() -> {
                    configure(world, 1, 64);
                    load(world, crafter);
                    place(world, crafter, BlockTypeKeys.CRAFTER, BlockFace.UP, LOOKING_NORTH, 30f);
                    final ServerWorld serverWorld = (ServerWorld) world;
                    try {
                        final CompoundBinaryTag data = serverWorld.blockEntityData(crafter);
                        final CrafterContainer grid = CrafterContainer.load(data);
                        grid.setSlotDisabled(1, true);
                        serverWorld.setBlockEntityData(crafter, grid.save(data));
                    } catch (final IOException exception) {
                        throw new AssertionError("Unable to disable a slot of the crafter", exception);
                    }
                    hopper(world, 4, Y + 1, 4);
                    inventory(world, hopper).set(0, ItemStack.of(ItemKeys.STICK, 10));
                })
                .waitUntil(() -> helper.assertTrue(inventory(world, hopper).isEmpty(),
                        "Moving up to 64 items at once, the hopper should empty itself into the crafter"))
                .execute(() -> {
                    final CrafterContainer grid;
                    try {
                        grid = CrafterContainer.load(((ServerWorld) world).blockEntityData(crafter));
                    } catch (final IOException exception) {
                        throw new AssertionError("Unable to read the crafter", exception);
                    }
                    final int[] expected = {2, 0, 2, 1, 1, 1, 1, 1, 1};
                    for (int slot = 0; slot < expected.length; slot++) {
                        final ItemStack stack = grid.get(slot);
                        helper.assertTrue(stack.isEmpty() ? expected[slot] == 0 : stack.id().equals(ItemKeys.STICK) && stack.count() == expected[slot],
                                "Slot " + slot + " of the crafter should hold " + expected[slot] + " sticks, holds " + stack);
                    }
                    helper.assertTrue(grid.isSlotDisabled(1), "The disabled slot of the crafter should stay disabled");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void configurationSetsTheCooldownAndHowManyItemsMoveAtOnce(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(4, Y, 4);
        final BlockPosition hopper = Position.block(4, Y + 1, 4);
        final int[] tries = new int[1];
        helper.sequence()
                .execute(() -> {
                    configure(world, 2, 4);
                    chest(world, 4, Y, 4);
                    hopper(world, 4, Y + 1, 4);
                    inventory(world, hopper).set(0, ItemStack.of(ItemKeys.DIAMOND, 10));
                })
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 4,
                        "The hopper should push four diamonds at once"))
                .execute(tookTicks(helper, tries, 1, "A ready hopper pushing"))
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 8,
                        "The hopper should push four more diamonds"))
                .execute(tookTicks(helper, tries, 2, "A cooldown of 2"))
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 10,
                        "The hopper should push the last two diamonds"))
                .execute(tookTicks(helper, tries, 2, "A cooldown of 2"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void theTransferCooldownCanBeChanged(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(4, Y, 4);
        final BlockPosition hopper = Position.block(4, Y + 1, 4);
        final int[] tries = new int[1];
        helper.sequence()
                .execute(() -> {
                    chest(world, 4, Y, 4);
                    hopper(world, 4, Y + 1, 4);
                    final HopperInventory slots = inventory(world, hopper);
                    slots.set(0, ItemStack.of(ItemKeys.DIAMOND, 1));
                    slots.setTransferCooldown(20);
                    helper.assertTrue(slots.transferCooldown() == 20, "The hopper should wait the cooldown it was given");
                })
                .waitUntil(waitFor(helper, tries, () -> count(chestInventory(world, chest), ItemKeys.DIAMOND) == 1,
                        "Once its cooldown is over, the hopper should push the diamond"))
                .execute(tookTicks(helper, tries, 20, "A cooldown of 20"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void listenersRefuseWhatHoppersMoveAndPickUp(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(4, Y, 4);
        final BlockPosition pusher = Position.block(4, Y + 1, 4);
        final BlockPosition picker = Position.block(8, Y, 4);
        final List<HopperMoveItemEvent> moves = new ArrayList<>();
        final List<HopperPickupItemEvent> pickups = new ArrayList<>();
        final Subscription[] subscriptions = new Subscription[2];
        helper.sequence()
                .execute(() -> {
                    subscriptions[0] = listen(world, HopperMoveItemEvent.class, event -> {
                        moves.add(event);
                        event.setCancelled(true);
                    });
                    subscriptions[1] = listen(world, HopperPickupItemEvent.class, event -> {
                        pickups.add(event);
                        event.setCancelled(true);
                    });
                    chest(world, 4, Y, 4);
                    hopper(world, 4, Y + 1, 4);
                    inventory(world, pusher).set(0, ItemStack.of(ItemKeys.DIAMOND, 1));
                    hopper(world, 8, Y, 4);
                    dropStill(world, 8.5, Y + 1.2, 4.5, ItemStack.of(ItemKeys.EMERALD, 3));
                })
                .waitUntil(waitTicks(helper, 12))
                .execute(() -> {
                    subscriptions[0].unsubscribe();
                    subscriptions[1].unsubscribe();
                    helper.assertTrue(chestInventory(world, chest).isEmpty() && inventory(world, picker).isEmpty(),
                            "Refused, the hoppers should move nothing");
                    helper.assertTrue(moves.size() == 2,
                            "A refused move should still make the hopper wait its cooldown: 2 tries in 12 game ticks, got "
                                    + moves.size());
                    final HopperMoveItemEvent move = moves.getFirst();
                    helper.assertTrue(move.isPush() && move.position().equals(pusher) && move.sourcePosition().equals(pusher)
                                    && move.destinationPosition().equals(chest) && move.destination() instanceof ChestInventory
                                    && move.item().equals(ItemStack.of(ItemKeys.DIAMOND, 1)),
                            "The event should tell the hopper pushes one diamond into the chest below it");
                    helper.assertTrue(pickups.size() > 1 && pickups.getFirst().taken().equals(ItemStack.of(ItemKeys.EMERALD, 3)),
                            "A refused pickup should be tried again on every game tick");
                })
                .waitUntil(() -> helper.assertTrue(count(chestInventory(world, chest), ItemKeys.DIAMOND) == 1
                                && count(inventory(world, picker), ItemKeys.EMERALD) == 3,
                        "Without the listeners, the hoppers should move the items"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void hopperMovesNothingOutOfASlotEmptiedMeanwhile(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition puller = Position.block(4, Y, 8);
        final BlockPosition chest = Position.block(4, Y + 1, 8);
        final AtomicReferenceArray<Subscription> subscription = new AtomicReferenceArray<>(new Subscription[1]);
        final AtomicIntegerArray tries = new AtomicIntegerArray(new int[]{0});
        helper.sequence()
                .execute(() -> {
                    hopper(world, 4, Y, 8);
                    chest(world, 4, Y + 1, 8);
                    chestInventory(world, chest).set(0, ItemStack.of(ItemKeys.DIAMOND, 64));
                    // the stack is taken while the hopper is about to pull from it, as a player looking
                    // into the chest from another region, on another thread, can
                    subscription.set(0, listen(world, HopperMoveItemEvent.class, event -> {
                        tries.getAndIncrement(0);
                        event.source().set(0, ItemStack.EMPTY);
                    }));
                })
                .waitUntil(() -> tries.get(0) > 0, "The hopper should try to pull the diamonds")
                .execute(() -> {
                    subscription.get(0).unsubscribe();
                    final int inChest = count(chestInventory(world, chest), ItemKeys.DIAMOND);
                    final int inHopper = count(inventory(world, puller), ItemKeys.DIAMOND);
                    helper.assertTrue(inChest == 0 && inHopper == 0,
                            "The hopper should not bring back what was taken from the slot meanwhile: the chest holds "
                                    + inChest + " diamonds, the hopper " + inHopper);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void breakingAHopperSpillsItsItems(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(4, Y + 3, 4);
        helper.sequence()
                .execute(() -> {
                    // in the air, nothing to push into
                    hopper(world, 4, Y + 3, 4);
                    final HopperInventory slots = inventory(world, position);
                    slots.set(0, ItemStack.of(ItemKeys.DIAMOND, 2));
                    slots.set(4, ItemStack.of(ItemKeys.GOLD_INGOT, 3));
                    set(world, position, defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(world.hopperAt(position).isEmpty(), "There should be no hopper where it was broken");
                })
                .waitUntil(() -> helper.assertTrue(onTheGround(world, ItemKeys.DIAMOND) == 2
                                && onTheGround(world, ItemKeys.GOLD_INGOT) == 3,
                        "Breaking the hopper should spill its items"))
                .build();
    }
}
