package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.CrafterMenu;
import fr.euphyllia.fidorial.server.inventory.CraftingMenu;
import fr.euphyllia.fidorial.server.inventory.EnderChestMenu;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.player.PlayerOpenInventoryEvent;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class BlockMenuTests {

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name) {
        return (ServerPlayer) helper.summonPlayer(name, Location.of(helper.world(), 0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
    }

    private static int stateId(final Key block) {
        return FidorialServer.getInstance().registries().blockStates().networkId(BlockState.of(block));
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

    @ScenarioTest(timeoutTicks = 60)
    public static void craftingTableOpensAndClosesForEveryViewerWhenBroken(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, 65, 0);
        final ServerPlayer first = player(helper, "TableUserOne");
        final ServerPlayer second = player(helper, "TableUserTwo");

        helper.sequence()
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.CRAFTING_TABLE.key())))
                .waitUntil(() -> helper.assertBlockAt(position, BlockTypeKeys.CRAFTING_TABLE.key()))
                .execute(() -> {
                    helper.assertTrue(use(first, position) && use(second, position), "Using a crafting table should be handled");
                    helper.assertTrue(first.containerMenu() instanceof CraftingMenu && second.containerMenu() instanceof CraftingMenu,
                            "Using a crafting table should open a 3x3 crafting window");
                })
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.AIR.key())))
                .waitUntil(() -> first.containerMenu() == first.inventoryMenu()
                                && second.containerMenu() == second.inventoryMenu(),
                        "Breaking the crafting table should close the window of every player using it")
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void windowClosesOnceThePlayerWalksAway(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(0, 65, -2);
        final ServerPlayer player = player(helper, "TableWalker");

        helper.sequence()
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.CRAFTING_TABLE.key())))
                .waitUntil(() -> helper.assertBlockAt(position, BlockTypeKeys.CRAFTING_TABLE.key()))
                .execute(() -> {
                    helper.assertTrue(use(player, position), "Using a crafting table should be handled");
                    helper.assertTrue(player.containerMenu() instanceof CraftingMenu && player.containerMenu().stillValid(),
                            "Next to the table, its window should be valid");
                    player.teleport(Location.of(world, 30.5, 65, 30.5, 0f, 0f));
                })
                .waitUntil(() -> player.containerMenu() == player.inventoryMenu(),
                        "Out of reach, the crafting table window should close, as in vanilla")
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void enderChestOpensThroughItsBehaviour(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(0, 65, 2);
        final ServerPlayer player = player(helper, "EnderChestUser");

        helper.sequence()
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.ENDER_CHEST.key())))
                .waitUntil(() -> helper.assertBlockAt(position, BlockTypeKeys.ENDER_CHEST.key()))
                .execute(() -> {
                    helper.assertTrue(use(player, position), "Using an ender chest should be handled");
                    helper.assertTrue(player.containerMenu() instanceof EnderChestMenu, "Using an ender chest should open it");
                    helper.assertTrue(FidorialServer.getInstance().players().blockViewers()
                                    .count(world.key(), position) == 1,
                            "The ender chest should count one viewer, for its lid");
                    player.closeContainer();
                    helper.assertTrue(FidorialServer.getInstance().players().blockViewers()
                                    .count(world.key(), position) == 0,
                            "Closing the ender chest should drop the viewer");
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void openInventoryEventDescribesTheWindowAndCanCancelIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(2, 65, 2);
        final ServerPlayer player = player(helper, "InventoryOpener");
        helper.sequence()
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.ENDER_CHEST.key())))
                .waitUntil(() -> helper.assertBlockAt(position, BlockTypeKeys.ENDER_CHEST.key()))
                .execute(() -> {
                    final List<PlayerOpenInventoryEvent> seen = new ArrayList<>();
                    final Subscription subscription = FidorialServer.getInstance().events()
                            .subscribe(PlayerOpenInventoryEvent.class, event -> {
                                if (event.player() == player) {
                                    seen.add(event);
                                    event.setCancelled(seen.size() == 1);
                                }
                            });
                    try {
                        helper.assertTrue(use(player, position), "Using an ender chest should be handled");
                        helper.assertTrue(player.containerMenu() == player.inventoryMenu(),
                                "A cancelled event should keep the ender chest closed");
                        helper.assertTrue(FidorialServer.getInstance().players().blockViewers()
                                        .count(world.key(), position) == 0,
                                "A cancelled ender chest should count no viewer");
                        helper.assertTrue(seen.size() == 1, "Expected one event, got " + seen.size());
                        final PlayerOpenInventoryEvent event = seen.getFirst();
                        helper.assertTrue(event.menuType().equals(MenuTypeKeys.GENERIC_9X3)
                                        && event.blockPosition().filter(position::equals).isPresent()
                                        && event.inventory() == player.enderChest(),
                                "The event should describe the ender chest window");

                        helper.assertTrue(use(player, position) && player.containerMenu() instanceof EnderChestMenu,
                                "Without cancelling, the ender chest should open");
                        player.closeContainer();
                    } finally {
                        subscription.unsubscribe();
                    }
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 80)
    public static void crafterKeepsItsSlotsShareThemAndClosesWhenBroken(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition position = Position.block(-2, 65, 0);
        final ServerPlayer first = player(helper, "CrafterUserOne");
        final ServerPlayer second = player(helper, "CrafterUserTwo");
        final AtomicReference<CrafterMenu> firstMenu = new AtomicReference<>();

        helper.sequence()
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.CRAFTER.key())))
                .waitUntil(() -> helper.assertBlockAt(position, BlockTypeKeys.CRAFTER.key()))
                .execute(() -> {
                    helper.assertTrue(use(first, position), "Using a crafter should be handled");
                    helper.assertTrue(first.containerMenu() instanceof CrafterMenu, "Using a crafter should open its window");
                    final CrafterMenu menu = (CrafterMenu) first.containerMenu();
                    firstMenu.set(menu);
                    first.inventory().set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.OAK_LOG.key(), 5));
                })
                .execute(() -> {
                    final CrafterMenu menu = firstMenu.get();
                    // window slot 9 is the first slot of the main inventory, right after the crafter's nine
                    click(menu, 9);
                    click(menu, 0);
                    helper.assertTrue(menu.crafter().get(0).equals(ItemStack.of(ItemKeys.OAK_LOG.key(), 5)),
                            "The logs should sit in the crafter's first slot, got " + menu.crafter().get(0));
                    helper.assertTrue(menu.preview().equals(ItemStack.of(ItemKeys.OAK_PLANKS.key(), 4)),
                            "The crafter should show the planks its grid makes, got " + menu.preview());
                    helper.assertTrue(menu.setSlotState(4, false), "An empty slot should be disabled");
                    helper.assertTrue(!menu.setSlotState(0, false), "A slot holding items cannot be disabled");

                    first.inventory().set(PlayerInventory.MAIN_START, ItemStack.of(ItemKeys.STICK.key(), 1));
                    click(menu, 9);
                    click(menu, 4);
                    helper.assertTrue(menu.crafter().get(4).isEmpty() && menu.carried().id().equals(ItemKeys.STICK.key()),
                            "A disabled slot should refuse items");
                    menu.returnCarried();

                    helper.assertTrue(use(second, position) && second.containerMenu() instanceof final CrafterMenu shared
                                    && shared.crafter() == menu.crafter(),
                            "Two players looking at one crafter should share its slots");
                })
                .execute(() -> {
                    first.closeContainer();
                    second.closeContainer();
                    helper.assertTrue(use(first, position), "The crafter should open again");
                    final CrafterMenu reopened = (CrafterMenu) first.containerMenu();
                    helper.assertTrue(reopened != null && reopened.crafter() != firstMenu.get().crafter(),
                            "With nobody looking, the crafter should be read again from its block entity");
                    helper.assertTrue(reopened.crafter().get(0).equals(ItemStack.of(ItemKeys.OAK_LOG.key(), 5))
                                    && reopened.crafter().isSlotDisabled(4),
                            "The crafter's slots should be saved in its block entity");
                    helper.assertTrue(((ServerWorld) world).blockEntityData(position) != null,
                            "The crafter should have a block entity");
                })
                .execute(() -> world.setBlockStateId(position, stateId(BlockTypeKeys.AIR.key())))
                .waitUntil(() -> first.containerMenu() == first.inventoryMenu(), "Breaking the crafter should close its window")
                .execute(() -> helper.assertTrue(((ServerWorld) world).blockEntityData(position) == null,
                        "Breaking the crafter should remove its block entity"))
                .execute(() -> {
                    int logs = 0;
                    for (final var entity : world.entities()) {
                        if (entity instanceof final ItemEntity item && !item.isRemoved()
                                && item.item().id().equals(ItemKeys.OAK_LOG.key())) {
                            logs += item.item().count();
                        }
                    }
                    helper.assertTrue(logs == 5, "Breaking the crafter should spill its 5 logs on the ground, found " + logs);
                })
                .build();
    }
}
