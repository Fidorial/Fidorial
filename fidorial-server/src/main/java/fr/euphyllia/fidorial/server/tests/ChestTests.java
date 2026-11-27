package fr.euphyllia.fidorial.server.tests;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.BlockEntityInventory;
import fr.euphyllia.fidorial.server.inventory.ChestMenu;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.ServerBlockPlaceContext;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.player.PlayerOpenInventoryEvent;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.DoubleChestInventory;
import fr.fidorial.inventory.PlayerInventory;
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
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.Chest;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static fr.euphyllia.fidorial.server.tests.RedstoneTests.Y;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.at;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.defaults;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.load;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.place;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.placeOnFloor;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.power;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.server;
import static fr.euphyllia.fidorial.server.tests.RedstoneTests.set;

/**
 * Chests against vanilla: two chests side by side joining into a large chest, the way placing and
 * sneaking choose which, the window and what it keeps, the lid a solid block keeps shut, comparators,
 * loot tables, names, and the copper chests joining whatever their oxidation.
 *
 * <p>Every chest stands in the region of the origin, so that the thread running the test owns it.</p>
 */
@SuppressWarnings("unused")
public final class ChestTests {

    /**
     * The yaw of a player looking north: the chests they place face south, toward them.
     */
    private static final float LOOKING_NORTH = 180f;

    private static final int AIR_Y = 65;

    private ChestTests() {
    }

    /**
     * Places a chest the way a player looking north does, clicking {@code clickedFace} of the block
     * it is placed against.
     */
    private static Chest placeChest(final World world, final BlockPosition position, final TypedKey<BlockType> block,
                                    final BlockFace clickedFace, final boolean sneaking) {
        final BlockStateRegistry states = server().registries().blockStates();
        final BlockPlaceContext context = new ServerBlockPlaceContext(position, clickedFace,
                Location.of(world, position.blockX() + 0.5, position.blockY() + 1.6, position.blockZ() + 3.5, LOOKING_NORTH, 30f),
                states.view((ServerWorld) world), 0.5f, sneaking);
        final BlockData placed = server().registries().blocks().behaviour(block).orElseThrow().placementState(context);
        Preconditions.checkArgument(placed != null, "%s refused to be placed at %s", block, position);
        set(world, position, placed);
        return (Chest) placed;
    }

    private static Chest placeChest(final World world, final BlockPosition position, final TypedKey<BlockType> block) {
        return placeChest(world, position, block, BlockFace.UP, false);
    }

    private static Chest chest(final World world, final BlockPosition position) {
        final BlockData state = at(world, position);
        if (!(state instanceof final Chest chest)) {
            throw new AssertionError("No chest at " + position + " but " + state.asString());
        }
        return chest;
    }

    private static Chest.Type type(final World world, final BlockPosition position) {
        return chest(world, position).getType();
    }

    private static ChestInventory inventory(final World world, final BlockPosition position) {
        return world.chestAt(position).orElseThrow(() -> new AssertionError("No chest inventory at " + position));
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name, final BlockPosition near) {
        return (ServerPlayer) helper.summonPlayer(name,
                Location.of(helper.world(), near.blockX() + 0.5, near.blockY(), near.blockZ() + 2.5, LOOKING_NORTH, 0f),
                GameMode.SURVIVAL);
    }

    /**
     * Uses the block the way a right click does: through its behaviour.
     */
    private static boolean use(final ServerPlayer player, final BlockPosition position) {
        final FidorialServer server = FidorialServer.getInstance();
        final BlockData data = server.registries().blocks().fromNetworkId(player.world().blockStateId(position));
        if (data == null) {
            return false;
        }
        return server.registries().blocks().behaviour(data.key()).orElseThrow().use(player, position, data);
    }

    private static void click(final ContainerMenu menu, final int slot) {
        menu.click(new ServerboundContainerClickPacket(menu.windowId(), menu.stateId(), (short) slot, (byte) 0,
                ServerboundContainerClickPacket.MODE_PICKUP));
    }

    private static int viewers(final World world, final BlockPosition position) {
        return server().players().blockViewers().count(world.key(), position);
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

    @ScenarioTest(timeoutTicks = 60)
    public static void chestsPlacedSideBySideJoinIntoALargeChest(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition left = Position.block(2, AIR_Y, 4);
        final BlockPosition right = Position.block(3, AIR_Y, 4);
        final BlockPosition third = Position.block(4, AIR_Y, 4);
        helper.sequence()
                .execute(() -> {
                    load(world, left);
                    final Chest first = placeChest(world, left, BlockTypeKeys.CHEST);
                    helper.assertTrue(first.getFacing() == BlockFace.SOUTH && first.getType() == Chest.Type.SINGLE,
                            "A lone chest should face the player and stand alone, got " + first.asString());
                    final Chest second = placeChest(world, right, BlockTypeKeys.CHEST);
                    helper.assertTrue(second.getType() == Chest.Type.LEFT,
                            "A chest placed on the right of another should join it as its left half, got " + second.asString());
                })
                .waitUntil(() -> helper.assertTrue(type(world, left) == Chest.Type.RIGHT,
                        "The chest joined should become the right half, is " + type(world, left)))
                .execute(() -> {
                    final DoubleChestInventory large = inventory(world, right).doubleChest()
                            .orElseThrow(() -> new AssertionError("The two chests should form a large chest"));
                    helper.assertTrue(large.first().position().equals(left) && large.second().position().equals(right),
                            "The first half of the window should be the one on the left of the player");
                    helper.assertTrue(large.size() == 54, "A large chest should have 54 slots");

                    final Chest alone = placeChest(world, third, BlockTypeKeys.CHEST);
                    helper.assertTrue(alone.getType() == Chest.Type.SINGLE,
                            "A chest cannot join a chest already half of a large chest, got " + alone.asString());
                })
                .waitUntil(() -> helper.assertTrue(type(world, right) == Chest.Type.LEFT && type(world, third) == Chest.Type.SINGLE,
                        "A third chest should leave the large chest alone"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void sneakingKeepsAChestAloneUnlessPlacedAgainstTheSideOfAnother(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(5, AIR_Y, 8);
        final BlockPosition beside = Position.block(6, AIR_Y, 8);
        final BlockPosition against = Position.block(4, AIR_Y, 8);
        final BlockPosition inFront = Position.block(5, AIR_Y, 9);
        helper.sequence()
                .execute(() -> {
                    load(world, first);
                    placeChest(world, first, BlockTypeKeys.CHEST);
                    final Chest sneaked = placeChest(world, beside, BlockTypeKeys.CHEST, BlockFace.UP, true);
                    helper.assertTrue(sneaked.getType() == Chest.Type.SINGLE,
                            "Sneaking, a chest placed on the floor should stay alone, got " + sneaked.asString());
                })
                .waitUntil(() -> helper.assertTrue(type(world, first) == Chest.Type.SINGLE,
                        "A sneaked chest should not join its neighbour"))
                .execute(() -> {
                    // clicking the west side of the first chest
                    final Chest joined = placeChest(world, against, BlockTypeKeys.CHEST, BlockFace.WEST, true);
                    helper.assertTrue(joined.getType() == Chest.Type.RIGHT && joined.getFacing() == BlockFace.SOUTH,
                            "Sneaking against the side of a chest should join it, got " + joined.asString());
                })
                .waitUntil(() -> helper.assertTrue(type(world, first) == Chest.Type.LEFT,
                        "The chest clicked should become the other half, is " + type(world, first)))
                .execute(() -> {
                    // clicking the front of the first chest
                    final Chest front = placeChest(world, inFront, BlockTypeKeys.CHEST, BlockFace.SOUTH, true);
                    helper.assertTrue(front.getType() == Chest.Type.SINGLE,
                            "Sneaking against the front of a chest should not join it, got " + front.asString());
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void breakingOneHalfSplitsTheLargeChestAndSpillsItsSlots(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition kept = Position.block(2, AIR_Y, 12);
        final BlockPosition broken = Position.block(3, AIR_Y, 12);
        helper.sequence()
                .execute(() -> {
                    load(world, kept);
                    placeChest(world, kept, BlockTypeKeys.CHEST);
                    placeChest(world, broken, BlockTypeKeys.CHEST);
                })
                .waitUntil(() -> helper.assertTrue(type(world, kept) == Chest.Type.RIGHT, "The chests should join"))
                .execute(() -> {
                    inventory(world, kept).set(0, ItemStack.of(ItemKeys.OAK_LOG, 5));
                    inventory(world, broken).set(0, ItemStack.of(ItemKeys.STICK, 3));
                    final DoubleChestInventory large = inventory(world, kept).doubleChest().orElseThrow();
                    helper.assertTrue(large.get(0).equals(ItemStack.of(ItemKeys.OAK_LOG, 5))
                                    && large.get(27).equals(ItemStack.of(ItemKeys.STICK, 3)),
                            "The large chest should show the slots of its first half then those of its second");
                    set(world, broken, defaults(BlockTypeKeys.AIR));
                })
                .waitUntil(() -> helper.assertTrue(type(world, kept) == Chest.Type.SINGLE,
                        "The chest left should stand alone again, is " + type(world, kept)))
                .execute(() -> {
                    helper.assertTrue(onTheGround(world, ItemKeys.STICK) == 3,
                            "The broken half should spill its 3 sticks, found " + onTheGround(world, ItemKeys.STICK));
                    helper.assertTrue(onTheGround(world, ItemKeys.OAK_LOG) == 0, "The chest left should keep its logs");
                    helper.assertTrue(inventory(world, kept).get(0).equals(ItemStack.of(ItemKeys.OAK_LOG, 5)),
                            "The chest left should still hold its logs");
                    helper.assertTrue(world.chestAt(broken).isEmpty(), "There should be no chest where it was broken");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void largeChestWindowIsSharedAndSavedInTheChests(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(2, AIR_Y, 16);
        final BlockPosition second = Position.block(3, AIR_Y, 16);
        final ServerPlayer one = player(helper, "LargeChestOne", first);
        final ServerPlayer two = player(helper, "LargeChestTwo", second);
        final List<PlayerOpenInventoryEvent> seen = new ArrayList<>();
        helper.sequence()
                .execute(() -> {
                    load(world, first);
                    placeChest(world, first, BlockTypeKeys.CHEST);
                    placeChest(world, second, BlockTypeKeys.CHEST);
                })
                .waitUntil(() -> helper.assertTrue(type(world, first) == Chest.Type.RIGHT, "The chests should join"))
                .execute(() -> {
                    final Subscription subscription = server().events().subscribe(PlayerOpenInventoryEvent.class, event -> {
                        if (event.player() == one) {
                            seen.add(event);
                        }
                    });
                    try {
                        helper.assertTrue(use(one, second), "Using a chest should be handled");
                    } finally {
                        subscription.unsubscribe();
                    }
                    helper.assertTrue(one.containerMenu() instanceof ChestMenu, "Using a chest should open its window");
                    final ChestMenu menu = (ChestMenu) one.containerMenu();
                    helper.assertTrue(menu.menuType().equals(MenuTypeKeys.GENERIC_9X6) && menu.halves().equals(List.of(first, second)),
                            "Either half should open the six rows of the large chest, its first half on top");
                    helper.assertTrue(menu.title().equals(Component.translatable("container.chestDouble")),
                            "The window should be titled as a large chest, got " + menu.title());
                    helper.assertTrue(seen.size() == 1 && seen.getFirst().inventory() instanceof DoubleChestInventory
                                    && seen.getFirst().blockPosition().filter(second::equals).isPresent(),
                            "The open event should describe the large chest");
                    helper.assertTrue(viewers(world, first) == 1 && viewers(world, second) == 1,
                            "Both lids should open for one viewer");

                    one.inventory().set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.OAK_LOG, 7));
                    // window slot 54 is the first slot of the main inventory, right after the 54 of the chest
                    click(menu, 54);
                    click(menu, 30);
                    helper.assertTrue(inventory(world, second).get(3).equals(ItemStack.of(ItemKeys.OAK_LOG, 7)),
                            "Slot 30 of the window should be slot 3 of the second half, got " + inventory(world, second).get(3));

                    helper.assertTrue(use(two, first) && two.containerMenu() instanceof final ChestMenu shared
                                    && shared.container().get(30).equals(ItemStack.of(ItemKeys.OAK_LOG, 7)),
                            "A second player should see what the first one put in the large chest");
                    helper.assertTrue(viewers(world, first) == 2 && viewers(world, second) == 2,
                            "Both lids should count both viewers");
                    two.closeContainer();
                    one.closeContainer();
                    helper.assertTrue(viewers(world, first) == 0 && viewers(world, second) == 0,
                            "Once both windows are closed, nobody should look into the chest");
                })
                .execute(() -> {
                    helper.assertTrue(use(one, first) && one.containerMenu() instanceof final ChestMenu reopened
                                    && reopened.container().get(30).equals(ItemStack.of(ItemKeys.OAK_LOG, 7)),
                            "The large chest should keep what was put into it");
                    set(world, second, defaults(BlockTypeKeys.AIR));
                })
                .waitUntil(() -> one.containerMenu() == one.inventoryMenu(),
                        "Breaking one half should close the window of the large chest")
                .execute(() -> helper.assertTrue(viewers(world, first) == 0 && viewers(world, second) == 0,
                        "A window closed by a broken half should give back both lids"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void solidBlockOnALidKeepsTheChestShut(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(2, AIR_Y, 20);
        final BlockPosition second = Position.block(3, AIR_Y, 20);
        final ServerPlayer player = player(helper, "ShutChestUser", first);
        helper.sequence()
                .execute(() -> {
                    load(world, first);
                    placeChest(world, first, BlockTypeKeys.CHEST);
                    set(world, first.relative(BlockFace.UP), defaults(BlockTypeKeys.STONE));
                    helper.assertTrue(use(player, first), "Using a shut chest should still be handled");
                    helper.assertTrue(player.containerMenu() == player.inventoryMenu(), "A stone on the lid should keep the chest shut");
                    set(world, first.relative(BlockFace.UP), defaults(BlockTypeKeys.GLASS));
                    helper.assertTrue(use(player, first) && player.containerMenu() instanceof ChestMenu,
                            "Glass on the lid should not keep the chest shut");
                    player.closeContainer();
                    set(world, first.relative(BlockFace.UP), defaults(BlockTypeKeys.AIR));
                    placeChest(world, second, BlockTypeKeys.CHEST);
                })
                .waitUntil(() -> helper.assertTrue(type(world, first) == Chest.Type.RIGHT, "The chests should join"))
                .execute(() -> {
                    set(world, second.relative(BlockFace.UP), defaults(BlockTypeKeys.STONE));
                    helper.assertTrue(use(player, first) && player.containerMenu() == player.inventoryMenu(),
                            "A stone on either lid should keep the large chest shut");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void comparatorReadsHowFullTheChestIs(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition chest = Position.block(2, Y, 24);
        final BlockPosition comparator = Position.block(3, Y, 24);
        final BlockPosition output = Position.block(4, Y, 24);
        final BlockPosition other = Position.block(2, Y, 25);
        helper.sequence()
                .execute(() -> {
                    load(world, chest);
                    // looking east: the chest faces west, the comparator reads the chest west of it
                    final Chest placed = (Chest) place(world, chest, BlockTypeKeys.CHEST, BlockFace.UP, -90f, 30f);
                    helper.assertTrue(placed.getFacing() == BlockFace.WEST, "The chest should face west, got " + placed.asString());
                    place(world, comparator, BlockTypeKeys.COMPARATOR, BlockFace.UP, -90f, 30f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    helper.assertTrue(power(world, output) == 0, "An empty chest should give nothing");
                    inventory(world, chest).set(0, ItemStack.of(ItemKeys.COBBLESTONE, 64));
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 1,
                        "One full slot out of 27 should give 1, gives " + power(world, output)))
                .execute(() -> {
                    final ChestInventory slots = inventory(world, chest);
                    for (int slot = 0; slot < ChestInventory.SIZE; slot++) {
                        slots.set(slot, ItemStack.of(ItemKeys.COBBLESTONE, 64));
                    }
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 15,
                        "A full chest should give 15, gives " + power(world, output)))
                .execute(() -> {
                    // looking east too, south of the first chest: the first one is on the left of the player
                    final Chest joined = (Chest) place(world, other, BlockTypeKeys.CHEST, BlockFace.UP, -90f, 30f);
                    helper.assertTrue(joined.getType() == Chest.Type.LEFT, "The second chest should join, got " + joined.asString());
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 8,
                        "Half a large chest full should give 8, gives " + power(world, output)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void copperChestsJoinWhateverTheirOxidation(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition oxidized = Position.block(2, AIR_Y, 28);
        final BlockPosition fresh = Position.block(3, AIR_Y, 28);
        final BlockPosition copper = Position.block(2, AIR_Y, 30);
        final BlockPosition waxed = Position.block(3, AIR_Y, 30);
        final BlockPosition wooden = Position.block(2, AIR_Y, 32);
        final BlockPosition trapped = Position.block(3, AIR_Y, 32);
        helper.sequence()
                .execute(() -> {
                    load(world, oxidized);
                    placeChest(world, oxidized, BlockTypeKeys.OXIDIZED_COPPER_CHEST);
                    inventory(world, oxidized).set(0, ItemStack.of(ItemKeys.COPPER_INGOT, 9));
                    placeChest(world, fresh, BlockTypeKeys.COPPER_CHEST);

                    placeChest(world, copper, BlockTypeKeys.COPPER_CHEST);
                    final Chest placed = placeChest(world, waxed, BlockTypeKeys.WAXED_WEATHERED_COPPER_CHEST);
                    helper.assertTrue(placed.key().equals(BlockTypeKeys.WAXED_COPPER_CHEST) && placed.getType() == Chest.Type.LEFT,
                            "A waxed weathered chest joining a copper chest should lose its oxidation, keep its wax, got "
                                    + placed.asString());

                    placeChest(world, wooden, BlockTypeKeys.CHEST);
                    final Chest lone = placeChest(world, trapped, BlockTypeKeys.TRAPPED_CHEST);
                    helper.assertTrue(lone.getType() == Chest.Type.SINGLE, "A trapped chest should not join a chest");
                })
                .waitUntil(() -> helper.assertTrue(chest(world, oxidized).key().equals(BlockTypeKeys.COPPER_CHEST)
                                && type(world, oxidized) == Chest.Type.RIGHT,
                        "The oxidized chest should join and take the oxidation of the fresh one, is "
                                + chest(world, oxidized).asString()))
                .execute(() -> {
                    helper.assertTrue(inventory(world, oxidized).get(0).equals(ItemStack.of(ItemKeys.COPPER_INGOT, 9)),
                            "A copper chest taking another oxidation should keep what it holds");
                    helper.assertTrue(onTheGround(world, ItemKeys.COPPER_INGOT) == 0, "Nothing should spill");
                    helper.assertTrue(chest(world, copper).key().equals(BlockTypeKeys.COPPER_CHEST)
                                    && type(world, copper) == Chest.Type.RIGHT,
                            "The copper chest should join the waxed one and stay as it was");
                    helper.assertTrue(type(world, wooden) == Chest.Type.SINGLE, "A chest should not join a trapped chest");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void lootTableIsRolledOnceTheFirstTimeTheChestIsRead(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(2, AIR_Y, 36);
        final BlockPosition twin = Position.block(6, AIR_Y, 36);
        final Key dungeon = Key.key("minecraft", "chests/simple_dungeon");
        helper.sequence()
                .execute(() -> {
                    load(world, first);
                    placeChest(world, first, BlockTypeKeys.CHEST);
                    placeChest(world, twin, BlockTypeKeys.CHEST);
                    final ChestInventory chest = inventory(world, first);
                    chest.set(26, ItemStack.of(ItemKeys.STICK, 1));
                    chest.setLootTable(dungeon, 42L);
                    inventory(world, twin).set(26, ItemStack.of(ItemKeys.STICK, 1));
                    inventory(world, twin).setLootTable(dungeon, 42L);
                    helper.assertTrue(chest.lootTable().filter(dungeon::equals).isPresent(), "The chest should hold its loot table");

                    final ItemStack[] rolled = chest.contents();
                    helper.assertTrue(chest.lootTable().isEmpty(), "Reading the chest should roll its loot table");
                    int stacks = 0;
                    for (final ItemStack stack : rolled) {
                        stacks += stack.isEmpty() ? 0 : 1;
                    }
                    helper.assertTrue(stacks > 1, "The dungeon loot should fill the chest, found " + stacks + " stacks");
                    helper.assertTrue(rolled[26].equals(ItemStack.of(ItemKeys.STICK, 1)), "The loot should only fill empty slots");
                    helper.assertTrue(Arrays.equals(rolled, chest.contents()), "The loot should be rolled once");
                    helper.assertTrue(Arrays.equals(rolled, inventory(world, twin).contents()),
                            "The same seed should roll the same loot");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void namedChestTitlesItsWindowAndItsLargeChest(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(2, AIR_Y, 40);
        final BlockPosition second = Position.block(3, AIR_Y, 40);
        final ServerPlayer player = player(helper, "NamedChestUser", first);
        final Component name = Component.text("Trésor");
        helper.sequence()
                .execute(() -> {
                    load(world, first);
                    placeChest(world, first, BlockTypeKeys.CHEST);
                    helper.assertTrue(use(player, first) && player.containerMenu() instanceof final ChestMenu plain
                                    && plain.title().equals(Component.translatable("container.chest"))
                                    && plain.menuType().equals(MenuTypeKeys.GENERIC_9X3),
                            "A lone chest should open three rows, titled as a chest");
                    player.closeContainer();
                    placeChest(world, second, BlockTypeKeys.CHEST);
                })
                .waitUntil(() -> helper.assertTrue(type(world, first) == Chest.Type.RIGHT, "The chests should join"))
                .execute(() -> {
                    inventory(world, second).setCustomName(name);
                    helper.assertTrue(inventory(world, second).customName().filter(name::equals).isPresent(),
                            "The chest should keep its name");
                    helper.assertTrue(use(player, first) && player.containerMenu() instanceof final ChestMenu named
                                    && named.title().equals(name),
                            "The name of one half should title the large chest, got " + player.containerMenu().title());
                    player.closeContainer();
                    inventory(world, second).setCustomName(null);
                    helper.assertTrue(inventory(world, second).customName().isEmpty(), "The name should go");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void trappedLargeChestPowersFromBothHalves(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(2, Y, 44);
        final BlockPosition second = Position.block(3, Y, 44);
        final BlockPosition firstOutput = Position.block(2, Y, 45);
        final BlockPosition secondOutput = Position.block(3, Y, 45);
        final ServerPlayer player = player(helper, "TrappedChestUser", Position.block(2, Y + 1, 44));
        helper.sequence()
                .execute(() -> {
                    load(world, first);
                    placeChest(world, first, BlockTypeKeys.TRAPPED_CHEST);
                    placeChest(world, second, BlockTypeKeys.TRAPPED_CHEST);
                    placeOnFloor(world, firstOutput, BlockTypeKeys.REDSTONE_WIRE);
                    placeOnFloor(world, secondOutput, BlockTypeKeys.REDSTONE_WIRE);
                })
                .waitUntil(() -> helper.assertTrue(type(world, first) == Chest.Type.RIGHT, "The trapped chests should join"))
                .execute(() -> helper.assertTrue(use(player, second) && player.containerMenu() instanceof ChestMenu,
                        "Using a trapped chest should open the large chest"))
                .waitUntil(() -> helper.assertTrue(power(world, firstOutput) == 1 && power(world, secondOutput) == 1,
                        "Both halves should power the dust beside them, give " + power(world, firstOutput)
                                + " and " + power(world, secondOutput)))
                .execute(player::closeContainer)
                .waitUntil(() -> helper.assertTrue(power(world, firstOutput) == 0 && power(world, secondOutput) == 0,
                        "A closed trapped chest should power nothing"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void apiInventoryReadsAndWritesTheBlockEntity(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 48);
        helper.sequence()
                .execute(() -> {
                    load(world, position);
                    helper.assertTrue(world.chestAt(position).isEmpty(), "There should be no chest in the air");
                    placeChest(world, position, BlockTypeKeys.CHEST);
                    final ChestInventory one = inventory(world, position);
                    final ChestInventory two = inventory(world, position);
                    one.set(4, ItemStack.of(ItemKeys.DIAMOND, 2));
                    helper.assertTrue(two.get(4).equals(ItemStack.of(ItemKeys.DIAMOND, 2)),
                            "Two views of one chest should agree, got " + two.get(4));
                    two.clear();
                    helper.assertTrue(one.isEmpty(), "Clearing a view should clear the chest");
                    final Container stored = one;
                    stored.setContents(new ItemStack[] {ItemStack.of(ItemKeys.APPLE, 3)});
                    helper.assertTrue(two.get(0).equals(ItemStack.of(ItemKeys.APPLE, 3)) && two.firstEmpty() == 1,
                            "Replacing the contents should be seen by every view");
                    final CompoundBinaryTag saved = ((ServerWorld) world).blockEntityData(position);
                    helper.assertTrue(saved != null && saved.get("Items") != null, "The slots should be saved in the block entity");
                    set(world, position, defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(one.isEmpty() && one.size() == ChestInventory.SIZE,
                            "Once the chest is gone, its inventory should read as empty");
                    one.set(0, ItemStack.of(ItemKeys.APPLE, 1));
                    helper.assertTrue(one.isEmpty(), "Once the chest is gone, writing should do nothing");
                })
                .waitUntil(() -> helper.assertTrue(onTheGround(world, ItemKeys.APPLE) == 3,
                        "Breaking the chest should spill its 3 apples, found " + onTheGround(world, ItemKeys.APPLE)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void windowOnABrokenChestHandsOutNothing(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 4);
        helper.sequence()
                .execute(() -> {
                    load(world, position);
                    placeChest(world, position, BlockTypeKeys.CHEST);
                    inventory(world, position).set(0, ItemStack.of(ItemKeys.DIAMOND, 64));
                    final ServerPlayer viewer = player(helper, "broken_chest_viewer", position);
                    helper.assertTrue(use(viewer, position) && viewer.containerMenu() instanceof ChestMenu, "The chest should open");
                    final ContainerMenu window = viewer.containerMenu();
                    set(world, position, defaults(BlockTypeKeys.AIR));
                    // a click let through just before the chest broke
                    click(window, 0);
                    helper.assertTrue(window.carried().isEmpty(),
                            "The window of a broken chest should hand out nothing, gave " + window.carried());
                })
                .waitUntil(() -> helper.assertTrue(onTheGround(world, ItemKeys.DIAMOND) == 64,
                        "The broken chest should spill its 64 diamonds once, found " + onTheGround(world, ItemKeys.DIAMOND)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void windowOnABrokenChestTakesNothingIn(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 4);
        // the first slot of the player inventory, just after the 27 of the chest
        final int inventorySlot = 27;
        helper.sequence()
                .execute(() -> {
                    load(world, position);
                    placeChest(world, position, BlockTypeKeys.CHEST);
                    final ServerPlayer viewer = player(helper, "broken_chest_depositor", position);
                    viewer.inventory().set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.DIAMOND, 64));
                    helper.assertTrue(use(viewer, position) && viewer.containerMenu() instanceof ChestMenu, "The chest should open");
                    final ContainerMenu window = viewer.containerMenu();
                    click(window, inventorySlot);
                    helper.assertTrue(window.carried().count() == 64, "The diamonds should be on the cursor, carried " + window.carried());
                    set(world, position, defaults(BlockTypeKeys.AIR));
                    // a click let through just before the chest broke, putting the diamonds down in it
                    click(window, 0);
                    helper.assertTrue(window.carried().count() == 64,
                            "The window of a broken chest should take nothing in, left on the cursor " + window.carried());
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void slotsOfAChestWaitForAnAtomicStepOnAnotherThread(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 4);
        helper.sequence()
                .execute(() -> {
                    load(world, position);
                    placeChest(world, position, BlockTypeKeys.CHEST);
                    final ChestInventory chest = inventory(world, position);
                    chest.set(0, ItemStack.of(ItemKeys.DIAMOND, 64));
                    final CountDownLatch inside = new CountDownLatch(1);
                    final CountDownLatch release = new CountDownLatch(1);
                    final CountDownLatch read = new CountDownLatch(1);
                    final AtomicReference<ItemStack> seen = new AtomicReference<>();
                    // a player of one region taking the stack, another reading it from the next region
                    final Thread taker = new Thread(() -> BlockEntityInventory.atomically(List.of(chest), () -> {
                        inside.countDown();
                        awaitQuietly(release);
                        chest.set(0, ItemStack.EMPTY);
                    }), "chest-test-taker");
                    final Thread reader = new Thread(() -> {
                        awaitQuietly(inside);
                        seen.set(chest.get(0));
                        read.countDown();
                    }, "chest-test-reader");
                    taker.start();
                    reader.start();
                    helper.assertTrue(awaitQuietly(inside), "The step should start");
                    final boolean readDuringTheStep = awaitQuietly(read, 200);
                    release.countDown();
                    helper.assertFalse(readDuringTheStep, "Another thread should not read the slots in the middle of the step");
                    helper.assertTrue(awaitQuietly(read) && seen.get().isEmpty(),
                            "Once the step is over, the other thread should read what it left, read " + seen.get());
                })
                .build();
    }

    private static boolean awaitQuietly(final CountDownLatch latch) {
        return awaitQuietly(latch, 5_000);
    }

    private static boolean awaitQuietly(final CountDownLatch latch, final long millis) {
        try {
            return latch.await(millis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
