package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.BarrelMenu;
import fr.euphyllia.fidorial.server.inventory.BlockBarrelInventory;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.HiddenRows;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.inventory.BarrelInventory;
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
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Openable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

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
 * Barrels against vanilla: placed with their opening toward the player, {@code open} while looked
 * into, spilled when broken, read by comparators; and the rows the configuration of their world
 * gives, the rows no longer shown being cleared or kept.
 *
 * <p>Every barrel stands in the region of the origin, so that the thread running the test owns it.</p>
 */
@SuppressWarnings("unused")
public final class BarrelTests {

    private static final int AIR_Y = 65;

    private BarrelTests() {
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name, final BlockPosition near) {
        return (ServerPlayer) helper.summonPlayer(name,
                Location.of(helper.world(), near.blockX() + 0.5, near.blockY(), near.blockZ() + 2.5, 180f, 0f),
                GameMode.SURVIVAL);
    }

    private static BlockPosition barrel(final World world, final int x, final int y, final int z) {
        final BlockPosition position = Position.block(x, y, z);
        load(world, position);
        set(world, position, defaults(BlockTypeKeys.BARREL));
        return position;
    }

    /**
     * Uses the barrel the way a right click does: through its behaviour.
     */
    private static BarrelMenu open(final ServerPlayer player, final BlockPosition position) {
        final FidorialServer server = FidorialServer.getInstance();
        final BlockData data = server.registries().blocks().fromNetworkId(player.world().blockStateId(position));
        if (data == null || !server.registries().blocks().behaviour(data.key()).orElseThrow().use(player, position, data)
                || !(player.containerMenu() instanceof final BarrelMenu menu)) {
            throw new AssertionError("Using the barrel at " + position + " should open it");
        }
        return menu;
    }

    private static boolean isOpen(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Openable barrel && barrel.isOpen();
    }

    private static BarrelInventory inventory(final World world, final BlockPosition position) {
        return world.barrelAt(position).orElseThrow(() -> new AssertionError("No barrel inventory at " + position));
    }

    private static void click(final ContainerMenu menu, final int slot) {
        menu.click(new ServerboundContainerClickPacket(menu.windowId(), menu.stateId(), (short) slot, (byte) 0,
                ServerboundContainerClickPacket.MODE_PICKUP));
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

    @ScenarioTest(timeoutTicks = 80)
    public static void barrelFacesThePlayerAndShowsOpenWhileLookedInto(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition below = Position.block(2, AIR_Y, 2);
        final BlockPosition ahead = Position.block(4, AIR_Y, 2);
        final ServerPlayer one = player(helper, "BarrelUserOne", ahead);
        final ServerPlayer two = player(helper, "BarrelUserTwo", ahead);
        helper.sequence()
                .execute(() -> {
                    load(world, below);
                    final BlockData down = place(world, below, BlockTypeKeys.BARREL, BlockFace.UP, 0f, 90f);
                    helper.assertTrue(down instanceof final Directional facing && facing.getFacing() == BlockFace.UP
                                    && down instanceof final Openable shut && !shut.isOpen(),
                            "A barrel placed looking down should open upward, shut, got " + down.asString());
                    final BlockData front = place(world, ahead, BlockTypeKeys.BARREL, BlockFace.UP, 180f, 0f);
                    helper.assertTrue(front instanceof final Directional facing && facing.getFacing() == BlockFace.SOUTH,
                            "A barrel placed looking north should open toward the player, got " + front.asString());

                    final BarrelMenu menu = open(one, ahead);
                    helper.assertTrue(menu.rows() == 3 && menu.menuType().equals(MenuTypeKeys.GENERIC_9X3)
                                    && menu.slotCount() == 27 + 36 && menu.title().equals(Component.translatable("container.barrel")),
                            "By default a barrel should open three rows, titled as a barrel");
                    helper.assertTrue(server().players().blockViewers().count(world.key(), ahead) == 1,
                            "The barrel should count one viewer");

                    one.inventory().set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.OAK_LOG, 6));
                    // window slot 27 is the first slot of the main inventory, right after the 27 of the barrel
                    click(menu, 27);
                    click(menu, 13);
                    helper.assertTrue(inventory(world, ahead).get(13).equals(ItemStack.of(ItemKeys.OAK_LOG, 6)),
                            "The barrel should keep what was put into it, got " + inventory(world, ahead).get(13));
                    helper.assertTrue(open(two, ahead).container().get(13).equals(ItemStack.of(ItemKeys.OAK_LOG, 6)),
                            "A second player should see what the first one put in the barrel");
                })
                .waitUntil(() -> isOpen(world, ahead), "A barrel looked into should show open")
                .execute(() -> {
                    one.closeContainer();
                    helper.assertTrue(server().players().blockViewers().count(world.key(), ahead) == 1,
                            "One player still looks into the barrel");
                })
                .execute(() -> helper.assertTrue(isOpen(world, ahead), "The barrel should stay open while someone looks inside"))
                .execute(two::closeContainer)
                .waitUntil(() -> !isOpen(world, ahead), "Once nobody looks inside, the barrel should shut")
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void barrelShowsTheRowsOfItsWorldAndClearsOrKeepsTheOthers(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 6);
        final ServerPlayer player = player(helper, "BarrelRowsUser", position);
        helper.sequence()
                .execute(() -> {
                    barrel(world, 2, AIR_Y, 6);
                    final BarrelInventory configured = inventory(world, position);
                    helper.assertTrue(configured.rows() == BarrelInventory.DEFAULT_ROWS && configured.size() == 27,
                            "By default a barrel should show three rows, shows " + configured.rows());

                    final BlockBarrelInventory six = new BlockBarrelInventory((ServerWorld) world, position, 6);
                    six.set(5, ItemStack.of(ItemKeys.DIAMOND, 2));
                    six.set(40, ItemStack.of(ItemKeys.EMERALD, 7));

                    final BarrelMenu large = new BarrelMenu(player, player.allocateWindowId(), (ServerWorld) world, position,
                            six, HiddenRows.CLEAR);
                    helper.assertTrue(player.openMenu(large) && large.menuType().equals(MenuTypeKeys.GENERIC_9X6),
                            "A barrel of six rows should open six rows");
                    player.closeContainer();
                    helper.assertTrue(six.get(40).equals(ItemStack.of(ItemKeys.EMERALD, 7)), "Six rows shown should clear nothing");

                    final BarrelMenu kept = new BarrelMenu(player, player.allocateWindowId(), (ServerWorld) world, position,
                            new BlockBarrelInventory((ServerWorld) world, position, 2), HiddenRows.KEEP);
                    helper.assertTrue(player.openMenu(kept) && kept.menuType().equals(MenuTypeKeys.GENERIC_9X2),
                            "A barrel of two rows should open two rows");
                    player.closeContainer();
                    helper.assertTrue(six.get(40).equals(ItemStack.of(ItemKeys.EMERALD, 7)),
                            "With hidden-rows: keep, the rows no longer shown should keep their items");

                    final BarrelMenu cleared = open(player, position);
                    player.closeContainer();
                    helper.assertTrue(cleared.rows() == 3, "Through its behaviour, the barrel should open the rows of its world");
                    helper.assertTrue(six.get(40).isEmpty(), "By default, the rows no longer shown should be cleared, got " + six.get(40));
                    helper.assertTrue(six.get(5).equals(ItemStack.of(ItemKeys.DIAMOND, 2)), "The rows still shown should keep their items");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void breakingABarrelSpillsEveryItemItHolds(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 10);
        helper.sequence()
                .execute(() -> {
                    barrel(world, 2, AIR_Y, 10);
                    final BlockBarrelInventory six = new BlockBarrelInventory((ServerWorld) world, position, 6);
                    six.set(0, ItemStack.of(ItemKeys.DIAMOND, 2));
                    six.set(50, ItemStack.of(ItemKeys.GOLD_INGOT, 3));
                    set(world, position, defaults(BlockTypeKeys.AIR));
                    helper.assertTrue(world.barrelAt(position).isEmpty(), "There should be no barrel where it was broken");
                })
                .waitUntil(() -> helper.assertTrue(onTheGround(world, ItemKeys.DIAMOND) == 2
                                && onTheGround(world, ItemKeys.GOLD_INGOT) == 3,
                        "Breaking the barrel should spill its items, those of the rows it no longer shows included"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void comparatorReadsHowFullTheBarrelIs(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, Y, 14);
        final BlockPosition comparator = Position.block(3, Y, 14);
        final BlockPosition output = Position.block(4, Y, 14);
        helper.sequence()
                .execute(() -> {
                    barrel(world, 2, Y, 14);
                    // looking east: the comparator reads the barrel west of it
                    place(world, comparator, BlockTypeKeys.COMPARATOR, BlockFace.UP, -90f, 30f);
                    placeOnFloor(world, output, BlockTypeKeys.REDSTONE_WIRE);
                    helper.assertTrue(power(world, output) == 0, "An empty barrel should give nothing");
                    inventory(world, position).set(0, ItemStack.of(ItemKeys.COBBLESTONE, 64));
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 1,
                        "One full slot out of 27 should give 1, gives " + power(world, output)))
                .execute(() -> {
                    final BarrelInventory slots = inventory(world, position);
                    for (int slot = 0; slot < slots.size(); slot++) {
                        slots.set(slot, ItemStack.of(ItemKeys.COBBLESTONE, 64));
                    }
                })
                .waitUntil(() -> helper.assertTrue(power(world, output) == 15,
                        "A full barrel should give 15, gives " + power(world, output)))
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void barrelLootAndNameOnlyTouchTheRowsItShows(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, AIR_Y, 18);
        final BlockPosition chest = Position.block(6, AIR_Y, 18);
        final ServerPlayer player = player(helper, "BarrelNameUser", position);
        final Component name = Component.text("Tonneau");
        helper.sequence()
                .execute(() -> {
                    barrel(world, 2, AIR_Y, 18);
                    load(world, chest);
                    set(world, chest, defaults(BlockTypeKeys.CHEST));
                    helper.assertTrue(world.chestAt(position).isEmpty() && world.barrelAt(chest).isEmpty(),
                            "A barrel is no chest, a chest is no barrel");

                    final BlockBarrelInventory six = new BlockBarrelInventory((ServerWorld) world, position, 6);
                    six.set(40, ItemStack.of(ItemKeys.STICK, 1));
                    final BarrelInventory shown = inventory(world, position);
                    shown.setLootTable(Key.key("minecraft", "chests/simple_dungeon"), 7L);
                    final ItemStack[] rolled = shown.contents();
                    int stacks = 0;
                    for (final ItemStack stack : rolled) {
                        stacks += stack.isEmpty() ? 0 : 1;
                    }
                    helper.assertTrue(stacks > 1 && shown.lootTable().isEmpty(), "The loot should fill the rows the barrel shows");
                    final ItemStack[] stored = six.contents();
                    for (int slot = shown.size(); slot < stored.length; slot++) {
                        helper.assertTrue(slot == 40 ? stored[slot].equals(ItemStack.of(ItemKeys.STICK, 1)) : stored[slot].isEmpty(),
                                "The loot should leave the rows the barrel no longer shows alone, slot " + slot + " holds " + stored[slot]);
                    }

                    shown.setCustomName(name);
                    helper.assertTrue(open(player, position).title().equals(name), "A named barrel should title its window");
                    player.closeContainer();
                })
                .build();
    }
}
