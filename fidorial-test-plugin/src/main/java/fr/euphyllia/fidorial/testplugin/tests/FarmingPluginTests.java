package fr.euphyllia.fidorial.testplugin.tests;

import fr.euphyllia.fidorial.testplugin.farming.ChorusSprout;
import fr.euphyllia.fidorial.testplugin.farming.TestFarming;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.farming.FarmingEngine;
import fr.fidorial.world.farming.FarmingRegistry;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.Objects;

/**
 * The plant the plugin adds, driven through the API alone: declared in the registry, planted where
 * it can stand, grown by the engine around a player, fertilized and harvested.
 */
public final class FarmingPluginTests {

    /**
     * The top layer of the floor of the test worlds.
     */
    private static final int SOIL = -49;

    private static BlockData state(final String state) {
        return Objects.requireNonNull(Blocks.data(state), () -> "unknown block " + state);
    }

    private static FarmingWorld farming(final Player player, final World world) {
        return player.server().services().find(FarmingEngine.class).orElseThrow().world(world);
    }

    private static int dropped(final World world, final TypedKey<Item> item) {
        int count = 0;
        for (final Entity entity : world.entities()) {
            if (entity instanceof final ItemEntity dropped && dropped.item().id().equals(item)) {
                count += dropped.item().count();
            }
        }
        return count;
    }

    @ScenarioTest(group = "testplugin.farming", timeoutTicks = 20)
    public static void pluginFarmingIsDeclared(final ScenarioTestHelper helper) {
        final Player probe = helper.summonPlayer("Botanist", Location.of(helper.world(), 8.5, SOIL + 1, 4.5, 0f, 0f), GameMode.CREATIVE);
        final FarmingRegistry registry = probe.server().registries().farming();
        helper.assertTrue(registry.behaviour(ChorusSprout.BLOCK).orElseThrow() instanceof ChorusSprout,
                "The chorus sprout should have the behaviour of the plugin");
        helper.assertTrue(registry.plant(TestFarming.SEED).orElseThrow().equals(ChorusSprout.BLOCK), "The seed should plant the sprout");
        helper.assertTrue(registry.isFertilizer(TestFarming.FERTILIZER), "The plugin's fertilizer should be one");
        helper.assertTrue(registry.isFertilizer(ItemKeys.BONE_MEAL), "Bone meal should still be a fertilizer");
        helper.assertTrue(registry.tillage(BlockTypeKeys.MYCELIUM).orElseThrow().tilled().key().equals(BlockTypeKeys.FARMLAND),
                "Mycelium should be tilled into farmland");
    }

    @ScenarioTest(group = "testplugin.farming", timeoutTicks = 400)
    public static void pluginPlantGrowsAroundAPlayerAndGivesItsHarvest(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition sprout = soil.relative(BlockFace.UP);
        final Player[] grower = new Player[1];
        helper.sequence()
                .execute(() -> {
                    world.blockStateId(soil); // loads the chunk of the field
                    grower[0] = helper.summonPlayer("Grower", Location.of(world, 8.5, SOIL + 1, 4.5, 0f, 0f), GameMode.SURVIVAL);
                    final FarmingWorld farming = farming(grower[0], world);
                    helper.assertFalse(farming.canSurvive(sprout, state("minecraft:chorus_flower")), "The sprout should not stand on cobblestone");
                    helper.assertTrue(farming.setBlock(soil, state("minecraft:end_stone")), "The soil should be laid");
                    helper.assertTrue(farming.canSurvive(sprout, state("minecraft:chorus_flower")), "The sprout should stand on end stone");
                    helper.assertTrue(farming.setBlock(sprout, state("minecraft:chorus_flower")), "The sprout should be planted");

                    grower[0].setHeldItem(ItemStack.of(TestFarming.FERTILIZER, 2));
                    helper.assertTrue(farming.useItem(grower[0], grower[0].selectedSlot(), sprout, BlockFace.UP), "The plugin's fertilizer should take");
                    helper.assertTrue(ChorusSprout.stage(Objects.requireNonNull(farming.blockAt(sprout))) == 1, "The fertilizer adds one stage");
                    helper.assertTrue(grower[0].heldItem().count() == 1, "The fertilizer should be used up");

                    // every block around the player is picked about once per game tick
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 4096);
                })
                .waitUntil(() -> ChorusSprout.stage(Objects.requireNonNull(farming(grower[0], world).blockAt(sprout))) == 5,
                        "The sprout should ripen on random ticks")
                .execute(() -> {
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 0);
                    // without its soil the sprout breaks, and gives what its behaviour says
                    farming(grower[0], world).setBlock(soil, state("minecraft:cobblestone"));
                    helper.assertBlockAt(sprout, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.CHORUS_FRUIT) >= 1 && dropped(world, TestFarming.SEED) == 1,
                        "A ripe sprout should give chorus fruits and its seed back")
                .build();
    }
}
