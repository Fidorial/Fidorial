package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.ContainerMenu;
import fr.euphyllia.fidorial.server.inventory.CraftingMenu;
import fr.euphyllia.fidorial.server.inventory.InventoryMenu;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.fidorial.entity.GameMode;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.player.PlayerCraftItemEvent;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.RecipeRegistry;
import fr.fidorial.recipe.ShapedRecipe;
import fr.fidorial.recipe.ShapelessRecipe;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import net.kyori.adventure.key.Key;

@SuppressWarnings("unused")
public final class CraftingTests {

    private static RecipeRegistry recipes() {
        return FidorialServer.getInstance().registries().recipes();
    }

    private static ServerPlayer survivalPlayer(final ScenarioTestHelper helper, final String name) {
        return (ServerPlayer) helper.summonPlayer(name, Location.of(helper.world(), 0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
    }

    private static void click(final ContainerMenu menu, final int slot, final int button, final int mode) {
        menu.click(new ServerboundContainerClickPacket(menu.windowId(), menu.stateId(), (short) slot, (byte) button, mode));
    }

    private static ItemStack stack(final Key item, final int count) {
        return ItemStack.of(item, count);
    }

    private static int count(final PlayerInventory inventory, final Key item) {
        int total = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            final ItemStack stack = inventory.get(slot);
            if (stack.id().equals(item)) {
                total += stack.count();
            }
        }
        return total;
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void vanillaRecipesAreLoaded(final ScenarioTestHelper helper) {
        helper.assertTrue(recipes().recipes().size() > 1000,
                "Expected the vanilla crafting recipes to be loaded, got " + recipes().recipes().size());
        helper.assertTrue(recipes().isVanilla(Key.key("crafting_table")), "minecraft:crafting_table is not a vanilla recipe");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void inventoryGridCraftsPlanks(final ScenarioTestHelper helper) {
        final ServerPlayer player = survivalPlayer(helper, "InventoryCrafter");
        final InventoryMenu menu = player.inventoryMenu();

        menu.grid().set(0, stack(ItemKeys.OAK_LOG.key(), 2));
        menu.refreshResult();
        helper.assertTrue(menu.result().equals(stack(ItemKeys.OAK_PLANKS.key(), 4)),
                "Expected 4 oak planks in the result slot, got " + menu.result());

        click(menu, InventoryMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_PICKUP);
        helper.assertTrue(menu.carried().equals(stack(ItemKeys.OAK_PLANKS.key(), 4)),
                "Expected 4 planks on the cursor, got " + menu.carried());
        helper.assertTrue(menu.grid().get(0).count() == 1, "Expected one log left, got " + menu.grid().get(0));

        click(menu, InventoryMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_PICKUP);
        helper.assertTrue(menu.carried().count() == 8, "Expected the second craft to stack on the cursor, got " + menu.carried());
        helper.assertTrue(menu.grid().isEmpty() && menu.result().isEmpty(), "Expected an empty grid and no result");

        player.doCloseContainer();
        helper.assertTrue(menu.carried().isEmpty() && count(player.inventory(), ItemKeys.OAK_PLANKS.key()) == 8,
                "Closing the inventory should put the cursor back into it");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void shiftClickCraftsAsManyAsPossible(final ScenarioTestHelper helper) {
        final ServerPlayer player = survivalPlayer(helper, "ShiftCrafter");
        final CraftingMenu menu = new CraftingMenu(player, player.allocateWindowId(), Position.block(0, 64, 0));

        menu.grid().set(4, stack(ItemKeys.OAK_LOG.key(), 10));
        menu.refreshResult();
        click(menu, CraftingMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_QUICK_MOVE);

        helper.assertTrue(menu.grid().isEmpty(), "Expected every log to be crafted, left " + menu.grid().get(4));
        helper.assertTrue(count(player.inventory(), ItemKeys.OAK_PLANKS.key()) == 40,
                "Expected 40 planks in the inventory, got " + count(player.inventory(), ItemKeys.OAK_PLANKS.key()));
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void pluginRecipeTakesSeveralItemsPerSlot(final ScenarioTestHelper helper) {
        final Object owner = new Object();
        recipes().register(ShapedRecipe.builder(Key.key("scenario_test", "compressed_pickaxe"))
                .pattern("DDD", " S ", " S ")
                .define('D', Ingredient.of(ItemKeys.DIAMOND_BLOCK.key()).withCount(16))
                .define('S', Ingredient.of(ItemKeys.STICK.key()))
                .result(ItemStack.of(ItemKeys.NETHERITE_PICKAXE.key()))
                .build(), owner);
        try {
            final ServerPlayer player = survivalPlayer(helper, "MultiStackCrafter");
            final CraftingMenu menu = new CraftingMenu(player, player.allocateWindowId(), Position.block(0, 64, 0));
            menu.grid().set(0, stack(ItemKeys.DIAMOND_BLOCK.key(), 20));
            menu.grid().set(1, stack(ItemKeys.DIAMOND_BLOCK.key(), 16));
            menu.grid().set(2, stack(ItemKeys.DIAMOND_BLOCK.key(), 15));
            menu.grid().set(4, stack(ItemKeys.STICK.key(), 1));
            menu.grid().set(7, stack(ItemKeys.STICK.key(), 1));
            menu.refreshResult();
            helper.assertTrue(menu.result().isEmpty(), "15 diamond blocks in a slot asking for 16 must not craft");

            menu.grid().set(2, stack(ItemKeys.DIAMOND_BLOCK.key(), 64));
            menu.refreshResult();
            helper.assertTrue(menu.result().id().equals(ItemKeys.NETHERITE_PICKAXE.key()),
                    "Expected the plugin recipe to match, got " + menu.result());

            click(menu, CraftingMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_PICKUP);
            helper.assertTrue(menu.carried().id().equals(ItemKeys.NETHERITE_PICKAXE.key()), "The pickaxe is not on the cursor");
            helper.assertTrue(menu.grid().get(0).count() == 4 && menu.grid().get(1).isEmpty()
                            && menu.grid().get(2).count() == 48 && menu.grid().get(4).isEmpty(),
                    "Expected 16 blocks taken per slot and one stick per stick slot");
        } finally {
            recipes().unregisterAll(owner);
        }
        helper.assertTrue(recipes().recipe(Key.key("scenario_test", "compressed_pickaxe")).isEmpty(),
                "unregisterAll did not drop the plugin recipe");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void shapelessPluginRecipeWithCounts(final ScenarioTestHelper helper) {
        final Object owner = new Object();
        recipes().register(ShapelessRecipe.builder(Key.key("scenario_test", "gold_pack"))
                .ingredient(Ingredient.of(ItemKeys.GOLD_INGOT.key()).withCount(32))
                .ingredient(Ingredient.of(ItemKeys.PAPER.key()))
                .result(ItemStack.of(ItemKeys.GOLD_BLOCK.key(), 3))
                .build(), owner);
        try {
            final ServerPlayer player = survivalPlayer(helper, "ShapelessCrafter");
            final InventoryMenu menu = player.inventoryMenu();
            menu.grid().set(3, stack(ItemKeys.GOLD_INGOT.key(), 64));
            menu.grid().set(0, stack(ItemKeys.PAPER.key(), 2));
            menu.refreshResult();
            click(menu, InventoryMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_QUICK_MOVE);
            helper.assertTrue(count(player.inventory(), ItemKeys.GOLD_BLOCK.key()) == 6,
                    "Expected two crafts of 3 gold blocks, got " + count(player.inventory(), ItemKeys.GOLD_BLOCK.key()));
            helper.assertTrue(menu.grid().isEmpty(), "Expected 64 ingots and 2 papers to be used up");
        } finally {
            recipes().unregisterAll(owner);
        }
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void bucketsStayInTheGrid(final ScenarioTestHelper helper) {
        final ServerPlayer player = survivalPlayer(helper, "CakeCrafter");
        final CraftingMenu menu = new CraftingMenu(player, player.allocateWindowId(), Position.block(0, 64, 0));
        final Key[] cake = {
                ItemKeys.MILK_BUCKET.key(), ItemKeys.MILK_BUCKET.key(), ItemKeys.MILK_BUCKET.key(),
                ItemKeys.SUGAR.key(), ItemKeys.EGG.key(), ItemKeys.SUGAR.key(),
                ItemKeys.WHEAT.key(), ItemKeys.WHEAT.key(), ItemKeys.WHEAT.key()
        };
        for (int slot = 0; slot < cake.length; slot++) {
            menu.grid().set(slot, stack(cake[slot], 1));
        }
        menu.refreshResult();
        click(menu, CraftingMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_PICKUP);
        helper.assertTrue(menu.carried().id().equals(ItemKeys.CAKE.key()), "Expected a cake, got " + menu.carried());
        for (int slot = 0; slot < 3; slot++) {
            helper.assertTrue(menu.grid().get(slot).equals(stack(ItemKeys.BUCKET.key(), 1)),
                    "Expected an empty bucket left in slot " + slot + ", got " + menu.grid().get(slot));
        }

        menu.removed();
        helper.assertTrue(menu.grid().isEmpty() && count(player.inventory(), ItemKeys.BUCKET.key()) == 3,
                "Closing the crafting table should give the buckets back");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void armorSlotsFollowTheGeneratedEquipmentSlot(final ScenarioTestHelper helper) {
        final ServerPlayer player = survivalPlayer(helper, "ArmorWearer");
        final InventoryMenu menu = player.inventoryMenu();

        player.inventory().set(PlayerInventory.MAIN_START, stack(ItemKeys.CARVED_PUMPKIN.key(), 2));
        click(menu, PlayerInventory.MAIN_START, 0, ServerboundContainerClickPacket.MODE_QUICK_MOVE);
        helper.assertTrue(player.inventory().get(PlayerInventory.HEAD).equals(stack(ItemKeys.CARVED_PUMPKIN.key(), 1)),
                "A shift-clicked carved pumpkin should be worn, one at a time, got " + player.inventory().get(PlayerInventory.HEAD));

        player.inventory().set(PlayerInventory.MAIN_START + 1, stack(ItemKeys.ELYTRA.key(), 1));
        click(menu, PlayerInventory.MAIN_START + 1, 0, ServerboundContainerClickPacket.MODE_PICKUP);
        click(menu, InventoryMenu.ARMOR_FROM + 3, 0, ServerboundContainerClickPacket.MODE_PICKUP);
        helper.assertTrue(player.inventory().get(PlayerInventory.FEET).isEmpty(), "An elytra cannot be worn as boots");
        click(menu, InventoryMenu.ARMOR_FROM + 1, 0, ServerboundContainerClickPacket.MODE_PICKUP);
        helper.assertTrue(player.inventory().get(PlayerInventory.CHEST).id().equals(ItemKeys.ELYTRA.key()),
                "An elytra is worn on the chest");
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void cancelledCraftTakesNothing(final ScenarioTestHelper helper) {
        final ServerPlayer player = survivalPlayer(helper, "CancelledCrafter");
        final InventoryMenu menu = player.inventoryMenu();
        try (final Subscription _ = FidorialServer.getInstance().events()
                .subscribe(PlayerCraftItemEvent.class, event -> {
                    if (event.player() == player) {
                        event.setCancelled(true);
                    }
                })) {
            menu.grid().set(0, stack(ItemKeys.OAK_LOG.key(), 1));
            menu.refreshResult();
            click(menu, InventoryMenu.RESULT_SLOT, 0, ServerboundContainerClickPacket.MODE_PICKUP);
            helper.assertTrue(menu.carried().isEmpty(), "A cancelled craft must give nothing");
            helper.assertTrue(menu.grid().get(0).count() == 1, "A cancelled craft must take nothing");
        }
    }
}
