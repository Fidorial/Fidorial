package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.defaults.GameRuleCommand;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.farming.SweetBerryBushBlock;
import fr.euphyllia.fidorial.server.world.gamerule.VanillaGameRules;
import fr.euphyllia.fidorial.server.world.loot.BlockLootTables;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Subscription;
import fr.fidorial.event.farming.FarmingEvent;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.event.farming.PlantFertilizeEvent;
import fr.fidorial.event.farming.PlantGrowEvent;
import fr.fidorial.event.farming.SoilMoistureEvent;
import fr.fidorial.event.farming.SoilRevertEvent;
import fr.fidorial.event.farming.SoilTillEvent;
import fr.fidorial.event.farming.SoilTrampleEvent;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.plugin.PluginContext;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.EnchantmentKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Ageable;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.type.Farmland;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingRegistry;
import fr.fidorial.world.farming.FarmingWorld;
import fr.fidorial.world.farming.Tillage;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Farming: soil being tilled, wetted, dried and trampled, plants growing on random ticks and
 * breaking when what holds them goes, fertilizer, harvests, and what plugins can change of it all.
 *
 * <p>Every field stands on the cobblestone floor of the test world, in the middle of the chunk at
 * its origin, so that the thread running the test owns its blocks.</p>
 */
@SuppressWarnings("unused")
public final class FarmingTests {

    /**
     * The top layer of the floor of the test worlds, where the soil of the fields goes: water put
     * there stays where it is, held in by the floor around it.
     */
    private static final int SOIL = -49;

    /**
     * The first layer of air above the floor, where plants grow.
     */
    private static final int Y = SOIL + 1;

    /**
     * How many random ticks a plant is given at most to do what it does at random: far more than
     * it needs, so that a test only fails when the plant never does it.
     */
    private static final int PATIENCE = 100_000;

    private FarmingTests() {
    }

    private static FidorialServer server() {
        return FidorialServer.getInstance();
    }

    private static FarmingWorld farming(final World world) {
        return server().worlds().farming().world(world);
    }

    private static FarmingRegistry registry() {
        return server().registries().farming();
    }

    private static BlockData state(final String state) {
        return Objects.requireNonNull(Blocks.data(state), () -> "unknown block " + state);
    }

    private static BlockData defaults(final TypedKey<BlockType> block) {
        return Objects.requireNonNull(Objects.requireNonNull(Blocks.type(block), "unknown block").defaultData());
    }

    /**
     * Sets a block the way the server does for a player or a command, everything following up.
     */
    private static void set(final World world, final BlockPosition position, final BlockData state) {
        final BlockStateRegistry states = server().registries().blockStates();
        server().worlds().blockEdits().set((ServerWorld) world, position, states.toBlockState(state), true);
    }

    private static void set(final World world, final BlockPosition position, final String state) {
        set(world, position, state(state));
    }

    private static BlockData at(final World world, final BlockPosition position) {
        return Objects.requireNonNull(farming(world).blockAt(position), () -> position + " is not loaded");
    }

    private static boolean is(final World world, final BlockPosition position, final TypedKey<BlockType> block) {
        return at(world, position).key().equals(block);
    }

    private static int age(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Ageable ageable ? ageable.getAge() : -1;
    }

    private static int moisture(final World world, final BlockPosition position) {
        return at(world, position) instanceof final Farmland farmland ? farmland.getMoisture() : -1;
    }

    /**
     * Loads the chunk at the origin, and brings the sun to its highest: crops need its light.
     */
    private static void prepare(final World world) {
        world.blockStateId(Position.block(8, Y, 8));
        world.dayNightCycle().setTime(6000);
    }

    /**
     * {@return {@code true} once the light of the field was worked out, and the sun reaches a block}
     */
    private static boolean sunlit(final World world, final BlockPosition position) {
        final var column = ((ServerWorld) world).loadedColumn(position.chunkX(), position.chunkZ());
        return column != null && column.lightPopulated() && world.skyLight(position) == 15;
    }

    /**
     * Gives a block one random tick, as the engine does to the blocks it picks.
     */
    private static void randomTick(final World world, final BlockPosition position) {
        server().worlds().builtinFarming().randomTick((ServerWorld) world, position);
    }

    /**
     * Gives a block random ticks until a condition holds, and fails if it never does.
     */
    private static void randomTicksUntil(final ScenarioTestHelper helper, final BlockPosition position,
                                         final BooleanSupplier condition, final String message) {
        for (int tick = 0; tick < PATIENCE && !condition.getAsBoolean(); tick++) {
            randomTick(helper.world(), position);
        }
        helper.assertTrue(condition.getAsBoolean(), message);
    }

    /**
     * {@return how many items of a kind lie on the ground of a world}
     */
    private static int dropped(final World world, final TypedKey<Item> item) {
        int count = 0;
        for (final Entity entity : world.entities()) {
            if (entity instanceof final ItemEntity dropped && dropped.item().id().equals(item)) {
                count += dropped.item().count();
            }
        }
        return count;
    }

    private static ServerPlayer player(final ScenarioTestHelper helper, final String name, final GameMode gameMode,
                                       final ItemStack held) {
        final ServerPlayer player = (ServerPlayer) helper.summonPlayer(name,
                Location.of(helper.world(), 8.5, Y, 4.5, 0f, 0f), gameMode);
        player.inventory().set(player.selectedSlot(), held);
        return player;
    }

    /**
     * Subscribes to a farming event for the world of a test only: tests run side by side, each in
     * its own world.
     */
    private static <E extends FarmingEvent> Subscription listen(final World world, final Class<E> type,
                                                                final Consumer<E> listener) {
        return server().events().subscribe(type, event -> {
            if (event.world() == world) {
                listener.accept(event);
            }
        });
    }

    private static Plugin owner() {
        return new Plugin() {
            @Override
            public void onLoad(final PluginContext context) {
                Plugin.super.onLoad(context);
            }
        };
    }

    @ScenarioTest
    public static void hoeTillsSoilWithRoomAbove(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition dirt = Position.block(8, SOIL, 8);
        final BlockPosition grass = Position.block(9, SOIL, 8);
        final BlockPosition coarse = Position.block(10, SOIL, 8);
        final BlockPosition covered = Position.block(11, SOIL, 8);
        final BlockPosition stone = Position.block(12, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    final ServerPlayer player = player(helper, "farming_tiller", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_HOE));
                    final int slot = player.selectedSlot();
                    final FarmingWorld farming = farming(world);
                    set(world, dirt, "minecraft:dirt");
                    set(world, grass, "minecraft:grass_block");
                    set(world, coarse, "minecraft:coarse_dirt");
                    set(world, covered, "minecraft:dirt");
                    set(world, covered.relative(BlockFace.UP), "minecraft:glass");
                    set(world, stone, "minecraft:stone");

                    helper.assertFalse(farming.useItem(player, slot, dirt, BlockFace.DOWN), "A hoe should not till from below");
                    helper.assertTrue(farming.useItem(player, slot, dirt, BlockFace.UP), "A hoe should till dirt");
                    helper.assertTrue(is(world, dirt, BlockTypeKeys.FARMLAND) && moisture(world, dirt) == 0,
                            "Tilled dirt should be dry farmland, is " + at(world, dirt).asString());
                    helper.assertTrue(farming.useItem(player, slot, grass, BlockFace.NORTH), "A hoe should till grass from the side");
                    helper.assertBlockAt(grass, BlockTypeKeys.FARMLAND);
                    helper.assertTrue(farming.useItem(player, slot, coarse, BlockFace.UP), "A hoe should till coarse dirt");
                    helper.assertBlockAt(coarse, BlockTypeKeys.DIRT);
                    helper.assertFalse(farming.useItem(player, slot, covered, BlockFace.UP), "A hoe should not till soil with a block above");
                    helper.assertBlockAt(covered, BlockTypeKeys.DIRT);
                    helper.assertFalse(farming.useItem(player, slot, stone, BlockFace.UP), "A hoe should do nothing to stone");

                    final ItemStack hoe = player.inventory().get(slot);
                    helper.assertTrue(hoe.getOrDefault(DataComponentTypes.DAMAGE, 0) == 3,
                            "Three blocks tilled should wear the hoe three times, wore it " + hoe.getOrDefault(DataComponentTypes.DAMAGE, 0));
                })
                .build();
    }

    @ScenarioTest
    public static void hoeWearsOutAndCreativeHoeDoesNot(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition first = Position.block(8, SOIL, 8);
        final BlockPosition second = Position.block(9, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, first, "minecraft:dirt");
                    set(world, second, "minecraft:dirt");
                    final ItemStack worn = ItemStack.of(ItemKeys.WOODEN_HOE);
                    final int lastUse = ItemDefaults.maxDamage(ItemKeys.WOODEN_HOE) - 1;
                    final ServerPlayer survivor = player(helper, "farming_worn_hoe", GameMode.SURVIVAL,
                            worn.with(DataComponentTypes.DAMAGE, lastUse));
                    helper.assertTrue(farming(world).useItem(survivor, survivor.selectedSlot(), first, BlockFace.UP), "A worn hoe still tills");
                    helper.assertTrue(survivor.inventory().get(survivor.selectedSlot()).isEmpty(), "A hoe out of durability should break");

                    final ServerPlayer creator = player(helper, "farming_creative_hoe", GameMode.CREATIVE, ItemStack.of(ItemKeys.WOODEN_HOE));
                    helper.assertTrue(farming(world).useItem(creator, creator.selectedSlot(), second, BlockFace.UP), "A creative hoe tills");
                    helper.assertTrue(creator.inventory().get(creator.selectedSlot()).getOrDefault(DataComponentTypes.DAMAGE, 0) == 0,
                            "A hoe should not wear in creative mode");
                })
                .build();
    }

    @ScenarioTest
    public static void tillingRootedDirtDropsItsRoots(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition rooted = Position.block(8, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, rooted, "minecraft:rooted_dirt");
                    set(world, rooted.relative(BlockFace.UP), "minecraft:glass");
                    final ServerPlayer player = player(helper, "farming_roots", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_HOE));
                    helper.assertTrue(farming(world).useItem(player, player.selectedSlot(), rooted, BlockFace.NORTH),
                            "Rooted dirt should be tilled even with a block above");
                    helper.assertBlockAt(rooted, BlockTypeKeys.DIRT);
                })
                .waitUntil(() -> dropped(world, ItemKeys.HANGING_ROOTS) == 1, "Tilled rooted dirt should drop its hanging roots")
                .build();
    }

    @ScenarioTest
    public static void farmlandSoaksUpNearbyWaterThenDriesOut(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition water = Position.block(12, SOIL, 8);
        final BlockPosition tooFar = Position.block(2, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, tooFar, "minecraft:water");
                    randomTick(world, soil);
                    helper.assertBlockAt(soil, BlockTypeKeys.DIRT);

                    set(world, soil, "minecraft:farmland");
                    set(world, water, "minecraft:water");
                    randomTick(world, soil);
                    helper.assertTrue(moisture(world, soil) == 7, "Farmland four blocks from water should be wet, moisture " + moisture(world, soil));

                    set(world, water, "minecraft:cobblestone");
                    for (int expected = 6; expected >= 0; expected--) {
                        randomTick(world, soil);
                        helper.assertTrue(moisture(world, soil) == expected,
                                "Farmland should dry out one step per random tick, expected " + expected + ", is " + moisture(world, soil));
                    }
                    randomTick(world, soil);
                    helper.assertBlockAt(soil, BlockTypeKeys.DIRT);
                })
                .build();
    }

    @ScenarioTest
    public static void dryFarmlandStaysTilledUnderACrop(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, soil.relative(BlockFace.UP), "minecraft:wheat");
                    for (int tick = 0; tick < 20; tick++) {
                        randomTick(world, soil);
                    }
                    helper.assertBlockAt(soil, BlockTypeKeys.FARMLAND);
                    helper.assertBlockAt(soil.relative(BlockFace.UP), BlockTypeKeys.WHEAT);
                })
                .build();
    }

    @ScenarioTest
    public static void farmlandCoveredByASolidBlockTurnsBackIntoDirt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition covered = Position.block(8, SOIL, 8);
        final BlockPosition gated = Position.block(10, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, covered, "minecraft:farmland");
                    set(world, gated, "minecraft:farmland");
                    set(world, covered.relative(BlockFace.UP), "minecraft:stone");
                    set(world, gated.relative(BlockFace.UP), "minecraft:oak_fence_gate");
                    helper.assertBlockAt(covered, BlockTypeKeys.DIRT);
                    helper.assertBlockAt(gated, BlockTypeKeys.FARMLAND);
                })
                .build();
    }

    @ScenarioTest
    public static void landingOnFarmlandTramplesItAndUprootsTheCrop(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat[age=7]");
                    final ServerPlayer player = player(helper, "farming_trampler", GameMode.SURVIVAL, ItemStack.EMPTY);
                    farming(world).entityLanded(player, soil, 0.25);
                    helper.assertBlockAt(soil, BlockTypeKeys.FARMLAND);
                    // a fall this long tramples every time
                    farming(world).entityLanded(player, soil, 3.0);
                    helper.assertBlockAt(soil, BlockTypeKeys.DIRT);
                    helper.assertBlockAt(crop, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.WHEAT) == 1, "The uprooted wheat should drop its harvest")
                .build();
    }

    @ScenarioTest
    public static void cropsOnlyStandOnFarmland(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition dirt = Position.block(10, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, dirt, "minecraft:dirt");
                })
                .waitUntil(() -> sunlit(world, soil.relative(BlockFace.UP)), "The field should see the sky")
                .execute(() -> {
                    final FarmingWorld farming = farming(world);
                    for (final String crop : List.of("minecraft:wheat", "minecraft:carrots", "minecraft:potatoes",
                            "minecraft:beetroots", "minecraft:torchflower_crop", "minecraft:pitcher_crop",
                            "minecraft:melon_stem", "minecraft:pumpkin_stem")) {
                        helper.assertTrue(farming.canSurvive(soil.relative(BlockFace.UP), state(crop)), crop + " should stand on farmland");
                        helper.assertFalse(farming.canSurvive(dirt.relative(BlockFace.UP), state(crop)), crop + " should not stand on dirt");
                    }
                    helper.assertTrue(farming.canSurvive(dirt.relative(BlockFace.UP), state("minecraft:stone")),
                            "A block taking no part in farming stands anywhere");
                })
                .build();
    }

    @ScenarioTest
    public static void wheatRipensOnRandomTicks(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        final AtomicInteger grown = new AtomicInteger();
        final Subscription[] subscription = new Subscription[1];
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat");
                    subscription[0] = listen(world, PlantGrowEvent.class, event -> {
                        helper.assertFalse(event.fertilized(), "A crop growing on a random tick is not fertilized");
                        grown.incrementAndGet();
                    });
                })
                .waitUntil(() -> sunlit(world, crop), "The field should be lit by the sun")
                .execute(() -> {
                    randomTicksUntil(helper, crop, () -> age(world, crop) == 7, "Wheat should ripen under random ticks");
                    subscription[0].unsubscribe();
                    helper.assertTrue(grown.get() == 7, "Wheat should grow one stage at a time, grew " + grown.get() + " times");
                    helper.assertFalse(registry().behaviour(BlockTypeKeys.WHEAT).orElseThrow().ticksRandomly(at(world, crop)),
                            "Ripe wheat should no longer ask for random ticks");
                })
                .build();
    }

    @ScenarioTest
    public static void cropsDoNotGrowInTheDark(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat");
                    world.dayNightCycle().setTime(18000);
                })
                .waitUntil(() -> sunlit(world, crop), "The field should see the sky")
                .execute(() -> {
                    helper.assertTrue(farming(world).lightLevel(crop) < 9, "The sky should give little light at midnight, gives "
                            + farming(world).lightLevel(crop));
                    for (int tick = 0; tick < 2_000; tick++) {
                        randomTick(world, crop);
                    }
                    helper.assertTrue(age(world, crop) == 0, "Wheat should not grow at midnight without a light, age " + age(world, crop));
                    helper.assertTrue(farming(world).canSurvive(crop, at(world, crop)), "Wheat seeing the sky should stay planted at night");
                })
                .build();
    }

    @ScenarioTest
    public static void aWellKeptFieldFeedsItsCropsBetter(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition center = Position.block(8, Y, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, center.relative(BlockFace.DOWN), "minecraft:farmland");
                    set(world, center, "minecraft:wheat");
                    final FarmingWorld farming = farming(world);
                    helper.assertTrue(farming.growthSpeed(center) == 2.0f, "A lone crop on dry farmland: 1 + 1, is " + farming.growthSpeed(center));

                    set(world, center.relative(BlockFace.DOWN), "minecraft:farmland[moisture=7]");
                    helper.assertTrue(farming.growthSpeed(center) == 4.0f, "A lone crop on wet farmland: 1 + 3, is " + farming.growthSpeed(center));

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            set(world, Position.block(8 + dx, SOIL, 8 + dz), "minecraft:farmland[moisture=7]");
                        }
                    }
                    helper.assertTrue(farming.growthSpeed(center) == 10.0f, "Wet farmland all around: 1 + 3 + 8 * 0.75, is " + farming.growthSpeed(center));

                    // a row of the same crop does not slow it down, a crop on both axes does
                    set(world, center.offset(1, 0, 0), "minecraft:wheat");
                    set(world, center.offset(-1, 0, 0), "minecraft:wheat");
                    helper.assertTrue(farming.growthSpeed(center) == 10.0f, "Crops in a row should grow at full speed, " + farming.growthSpeed(center));
                    set(world, center.offset(0, 0, 1), "minecraft:wheat");
                    helper.assertTrue(farming.growthSpeed(center) == 5.0f, "Crops crowded on both axes should grow half as fast, "
                            + farming.growthSpeed(center));
                    set(world, center.offset(0, 0, 1), "minecraft:carrots");
                    helper.assertTrue(farming.growthSpeed(center) == 10.0f, "Another crop next to it should not slow it down, "
                            + farming.growthSpeed(center));
                })
                .build();
    }

    @ScenarioTest
    public static void cropIsUprootedWhenItsFarmlandGoes(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:carrots[age=7]");
                    set(world, soil, "minecraft:cobblestone");
                    helper.assertBlockAt(crop, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.CARROT) >= 1, "An uprooted crop should drop its harvest")
                .execute(() -> helper.assertTrue(dropped(world, ItemKeys.CARROT) <= 5,
                        "Ripe carrots drop one to five carrots, dropped " + dropped(world, ItemKeys.CARROT)))
                .build();
    }

    @ScenarioTest
    public static void harvestDependsOnHowRipeTheCropIs(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition young = Position.block(6, Y, 8);
        final BlockPosition ripe = Position.block(10, Y, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, young.relative(BlockFace.DOWN), "minecraft:farmland");
                    set(world, ripe.relative(BlockFace.DOWN), "minecraft:farmland");
                    set(world, young, "minecraft:wheat[age=3]");
                    set(world, ripe, "minecraft:wheat[age=7]");
                    final ServerPlayer player = player(helper, "farming_reaper", GameMode.SURVIVAL, ItemStack.EMPTY);
                    helper.assertTrue(farming(world).harvest(young, player, ItemStack.EMPTY), "Young wheat should give its seed back");
                    helper.assertBlockAt(young, BlockTypeKeys.WHEAT);
                })
                .waitUntil(() -> dropped(world, ItemKeys.WHEAT_SEEDS) == 1 && dropped(world, ItemKeys.WHEAT) == 0,
                        "Young wheat should drop one seed and no wheat")
                .execute(() -> helper.assertTrue(farming(world).harvest(ripe, null, ItemStack.EMPTY), "Ripe wheat should give a harvest"))
                .waitUntil(() -> dropped(world, ItemKeys.WHEAT) == 1, "Ripe wheat should drop one wheat")
                .execute(() -> {
                    final int seeds = dropped(world, ItemKeys.WHEAT_SEEDS) - 1;
                    helper.assertTrue(seeds >= 1 && seeds <= 4, "Ripe wheat drops one to four seeds, dropped " + seeds);
                    world.gameRules().setBoolean(GameRuleKeys.BLOCK_DROPS, false);
                    helper.assertFalse(farming(world).harvest(ripe, null, ItemStack.EMPTY), "Nothing should drop with block drops off");
                    world.gameRules().setBoolean(GameRuleKeys.BLOCK_DROPS, true);
                    helper.assertFalse(farming(world).harvest(young.relative(BlockFace.DOWN, 2), null, ItemStack.EMPTY),
                            "A block taking no part in farming gives no harvest");
                })
                .build();
    }

    @ScenarioTest
    public static void fertilizerGrowsCropsAndIsUsedUp(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat");
                    final ServerPlayer player = player(helper, "farming_fertilizer", GameMode.SURVIVAL, ItemStack.of(ItemKeys.BONE_MEAL, 8));
                    final int slot = player.selectedSlot();
                    final FarmingWorld farming = farming(world);

                    helper.assertTrue(farming.useItem(player, slot, crop, BlockFace.UP), "Bone meal should be used on young wheat");
                    final int afterOne = age(world, crop);
                    helper.assertTrue(afterOne >= 2 && afterOne <= 5, "Bone meal adds two to five stages, added " + afterOne);
                    helper.assertTrue(player.inventory().get(slot).count() == 7, "One bone meal should be used up");

                    while (age(world, crop) < 7) {
                        helper.assertTrue(farming.useItem(player, slot, crop, BlockFace.UP), "Bone meal should be used until the wheat is ripe");
                    }
                    final int left = player.inventory().get(slot).count();
                    helper.assertFalse(farming.useItem(player, slot, crop, BlockFace.UP), "Bone meal should do nothing to ripe wheat");
                    helper.assertFalse(farming.useItem(player, slot, soil, BlockFace.UP), "Bone meal should do nothing to farmland");
                    helper.assertTrue(player.inventory().get(slot).count() == left, "Bone meal that did nothing should not be used up");
                })
                .build();
    }

    @ScenarioTest
    public static void torchflowerSeedlingRipensIntoAFlower(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:torchflower_crop");
                    final ServerPlayer player = player(helper, "farming_torchflower", GameMode.CREATIVE, ItemStack.of(ItemKeys.BONE_MEAL));
                    final FarmingWorld farming = farming(world);
                    helper.assertTrue(farming.useItem(player, player.selectedSlot(), crop, BlockFace.UP), "Bone meal on the seedling");
                    helper.assertTrue(age(world, crop) == 1, "Bone meal should add one stage to the seedling");
                    helper.assertTrue(farming.useItem(player, player.selectedSlot(), crop, BlockFace.UP), "Bone meal on the grown seedling");
                    helper.assertBlockAt(crop, BlockTypeKeys.TORCHFLOWER);
                    helper.assertTrue(player.inventory().get(player.selectedSlot()).count() == 1, "Bone meal is not used up in creative mode");
                })
                .build();
    }

    @ScenarioTest
    public static void pitcherCropGrowsTwoBlocksTall(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition lower = soil.relative(BlockFace.UP);
        final BlockPosition upper = lower.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, lower, "minecraft:pitcher_crop");
                })
                .waitUntil(() -> sunlit(world, lower), "The field should see the sky")
                .execute(() -> {
                    randomTicksUntil(helper, lower, () -> age(world, lower) == 2, "The pitcher crop should reach its third stage");
                    helper.assertBlockAt(upper, BlockTypeKeys.AIR);
                    randomTicksUntil(helper, lower, () -> age(world, lower) == 4, "The pitcher crop should ripen");
                    helper.assertTrue("upper".equals(at(world, upper).get("half")) && age(world, upper) == 4,
                            "A ripe pitcher crop should have a ripe upper half, has " + at(world, upper).asString());

                    // breaking the upper half takes the lower one with it, which holds the harvest
                    set(world, upper, "minecraft:air");
                    helper.assertBlockAt(lower, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.PITCHER_PLANT) == 1, "A ripe pitcher crop should drop a pitcher plant")
                .build();
    }

    @ScenarioTest
    public static void tallPitcherCropNeedsRoomAbove(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition lower = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, lower, "minecraft:pitcher_crop[age=2]");
                })
                .waitUntil(() -> sunlit(world, lower), "The field should see the sky")
                .execute(() -> {
                    set(world, lower.relative(BlockFace.UP), "minecraft:glass");
                    helper.assertBlockAt(lower, BlockTypeKeys.PITCHER_CROP);
                    for (int tick = 0; tick < 2_000; tick++) {
                        randomTick(world, lower);
                    }
                    helper.assertTrue(age(world, lower) == 2, "A pitcher crop should not grow tall without room, age " + age(world, lower));
                    final ServerPlayer player = player(helper, "farming_pitcher", GameMode.CREATIVE, ItemStack.of(ItemKeys.BONE_MEAL));
                    helper.assertFalse(farming(world).useItem(player, player.selectedSlot(), lower, BlockFace.NORTH),
                            "Bone meal should do nothing to a pitcher crop without room");
                })
                .build();
    }

    @ScenarioTest
    public static void waterWashesCropsAwayWithTheirHarvest(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        final BlockPosition cane = Position.block(8, Y, 10);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat[age=7]");
                    // a cane stands in a stream: sand, with water held in the floor beside it
                    set(world, cane.relative(BlockFace.DOWN), "minecraft:sand");
                    set(world, cane.relative(BlockFace.DOWN).relative(BlockFace.EAST), "minecraft:water");
                    set(world, cane, "minecraft:sugar_cane");
                })
                .waitUntil(() -> sunlit(world, crop), "The field should see the sky")
                .execute(() -> set(world, Position.block(10, Y, 9), "minecraft:water"))
                .waitUntil(() -> is(world, crop, BlockTypeKeys.WATER), "The stream should take the place of the wheat")
                .waitUntil(() -> dropped(world, ItemKeys.WHEAT) == 1, "Washed away wheat should drop its harvest")
                .execute(() -> helper.assertBlockAt(cane, BlockTypeKeys.SUGAR_CANE))
                .build();
    }

    @ScenarioTest
    public static void stemBearsAFruitThenLetsGoOfIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition stem = soil.relative(BlockFace.UP);
        final BlockPosition[] fruit = new BlockPosition[1];
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    // the fruit only finds soil to the east: cobblestone everywhere else
                    set(world, soil.relative(BlockFace.EAST), "minecraft:dirt");
                    set(world, stem, "minecraft:melon_stem");
                })
                .waitUntil(() -> sunlit(world, stem), "The field should be lit by the sun")
                .execute(() -> {
                    randomTicksUntil(helper, stem, () -> age(world, stem) == 7, "The stem should grow up");
                    randomTicksUntil(helper, stem, () -> is(world, stem, BlockTypeKeys.ATTACHED_MELON_STEM), "The stem should bear a melon");
                    final BlockFace side = ((Directional) at(world, stem)).getFacing();
                    helper.assertTrue(side == BlockFace.EAST, "The stem should face the only soil around, faces " + side);
                    fruit[0] = stem.relative(side);
                    helper.assertBlockAt(fruit[0], BlockTypeKeys.MELON);

                    final ServerPlayer player = player(helper, "farming_melon", GameMode.SURVIVAL, ItemStack.EMPTY);
                    helper.assertTrue(farming(world).harvest(fruit[0], player, ItemStack.EMPTY), "A melon should give slices");
                    set(world, fruit[0], "minecraft:air");
                    helper.assertTrue(is(world, stem, BlockTypeKeys.MELON_STEM) && age(world, stem) == 7,
                            "A stem whose melon was picked should be a grown stem again, is " + at(world, stem).asString());
                })
                .waitUntil(() -> dropped(world, ItemKeys.MELON_SLICE) >= 3, "A melon should drop at least three slices")
                .build();
    }

    @ScenarioTest
    public static void netherWartGrowsOnSoulSandInTheDark(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition wart = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    world.dayNightCycle().setTime(18000);
                    set(world, soil, "minecraft:soul_sand");
                    set(world, wart, "minecraft:nether_wart");
                    final FarmingWorld farming = farming(world);
                    helper.assertFalse(farming.canSurvive(wart.offset(2, 0, 0), state("minecraft:nether_wart")),
                            "Nether wart should not stand on cobblestone");
                    randomTicksUntil(helper, wart, () -> age(world, wart) == 3, "Nether wart should ripen whatever the light");
                    final ServerPlayer player = player(helper, "farming_wart", GameMode.CREATIVE, ItemStack.of(ItemKeys.BONE_MEAL));
                    set(world, wart, "minecraft:nether_wart");
                    helper.assertFalse(farming.useItem(player, player.selectedSlot(), wart, BlockFace.UP), "Bone meal does nothing to nether wart");
                })
                .build();
    }

    @ScenarioTest
    public static void sugarCaneGrowsThreeTallNextToWater(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition sand = Position.block(8, SOIL, 8);
        final BlockPosition water = sand.relative(BlockFace.WEST);
        final BlockPosition cane = sand.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, sand, "minecraft:sand");
                    final FarmingWorld farming = farming(world);
                    helper.assertFalse(farming.canSurvive(cane, state("minecraft:sugar_cane")), "Sugar cane needs water beside its soil");
                    set(world, water, "minecraft:water");
                    helper.assertTrue(farming.canSurvive(cane, state("minecraft:sugar_cane")), "Sugar cane stands on sand next to water");

                    set(world, cane, "minecraft:sugar_cane");
                    randomTicksUntil(helper, cane, () -> is(world, cane.relative(BlockFace.UP), BlockTypeKeys.SUGAR_CANE),
                            "Sugar cane should grow a second block");
                    helper.assertTrue(age(world, cane) <= 1, "A cane that just grew starts ageing again, age " + age(world, cane));
                    final BlockPosition second = cane.relative(BlockFace.UP);
                    final BlockPosition third = second.relative(BlockFace.UP);
                    randomTicksUntil(helper, second, () -> is(world, third, BlockTypeKeys.SUGAR_CANE), "Sugar cane should grow a third block");
                    for (int tick = 0; tick < 2_000; tick++) {
                        randomTick(world, third);
                    }
                    helper.assertBlockAt(third.relative(BlockFace.UP), BlockTypeKeys.AIR);

                    // a cane left without water breaks the next time something changes next to it
                    set(world, water, "minecraft:cobblestone");
                    helper.assertBlockAt(cane, BlockTypeKeys.SUGAR_CANE);
                    set(world, cane.relative(BlockFace.NORTH), "minecraft:glass");
                    helper.assertBlockAt(cane, BlockTypeKeys.AIR);
                    helper.assertBlockAt(second, BlockTypeKeys.AIR);
                    helper.assertBlockAt(third, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.SUGAR_CANE) == 3, "Each block of a broken cane should drop")
                .build();
    }

    @ScenarioTest
    public static void cactusBreaksWhenSomethingSolidTouchesIt(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition sand = Position.block(8, SOIL, 8);
        final BlockPosition cactus = sand.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, sand, "minecraft:sand");
                    final FarmingWorld farming = farming(world);
                    helper.assertTrue(farming.canSurvive(cactus, state("minecraft:cactus")), "A cactus stands on sand");
                    helper.assertFalse(farming.canSurvive(cactus.offset(3, 0, 0), state("minecraft:cactus")),
                            "A cactus should not stand on cobblestone");
                    set(world, cactus, "minecraft:cactus[age=15]");
                    randomTick(world, cactus);
                    helper.assertBlockAt(cactus.relative(BlockFace.UP), BlockTypeKeys.CACTUS);
                    helper.assertTrue(age(world, cactus) == 0, "A cactus that just grew starts ageing again");

                    set(world, cactus.relative(BlockFace.NORTH), "minecraft:stone");
                    helper.assertBlockAt(cactus, BlockTypeKeys.AIR);
                    helper.assertBlockAt(cactus.relative(BlockFace.UP), BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.CACTUS) == 2, "Each block of a broken cactus should drop")
                .build();
    }

    @ScenarioTest
    public static void cocoaHangsFromJungleLogsAndRipens(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition log = Position.block(8, Y, 8);
        final BlockPosition pod = log.relative(BlockFace.EAST);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, log, "minecraft:jungle_log");
                    // clicked against the east face of the log
                    final BlockPlaceContext context = new BlockPlaceContext(pod, BlockFace.EAST,
                            Location.of(world, pod.blockX() + 2.5, Y, pod.blockZ() + 0.5, 90f, 0f),
                            server().registries().blockStates().view((ServerWorld) world), 0.5f);
                    final BlockData placed = server().registries().blocks().behaviour(BlockTypeKeys.COCOA).orElseThrow().placementState(context);
                    helper.assertTrue(placed instanceof final Directional hanging && hanging.getFacing() == BlockFace.WEST,
                            "A pod placed against the east face of a log should face the log");
                    final FarmingWorld farming = farming(world);
                    helper.assertTrue(farming.canSurvive(pod, Objects.requireNonNull(placed)), "Cocoa should hang from a jungle log");
                    helper.assertFalse(farming.canSurvive(pod.offset(3, 0, 0), placed), "Cocoa should not hang from thin air");

                    set(world, pod, placed);
                    randomTicksUntil(helper, pod, () -> age(world, pod) == 2, "Cocoa should ripen");
                    set(world, log, "minecraft:air");
                    helper.assertBlockAt(pod, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.COCOA_BEANS) == 3, "A ripe pod should drop three beans")
                .build();
    }

    @ScenarioTest
    public static void sweetBerriesArePickedWithoutBreakingTheBush(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition bush = soil.relative(BlockFace.UP);
        final AtomicInteger harvests = new AtomicInteger();
        final Subscription[] subscription = new Subscription[1];
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:grass_block");
                    set(world, bush, "minecraft:sweet_berry_bush");
                })
                .waitUntil(() -> sunlit(world, bush.relative(BlockFace.UP)), "The bush should be lit by the sun")
                .execute(() -> {
                    final ServerPlayer player = player(helper, "farming_berries", GameMode.SURVIVAL, ItemStack.EMPTY);
                    helper.assertFalse(SweetBerryBushBlock.INSTANCE.use(player, bush, at(world, bush)), "A young bush has nothing to pick");
                    randomTicksUntil(helper, bush, () -> age(world, bush) == 3, "The bush should bear berries");
                    subscription[0] = listen(world, HarvestEvent.class, event -> {
                        helper.assertTrue(event.player() == player, "The harvest of a picked bush is the player's");
                        harvests.incrementAndGet();
                    });
                    helper.assertTrue(SweetBerryBushBlock.INSTANCE.use(player, bush, at(world, bush)), "A ripe bush should be picked");
                    subscription[0].unsubscribe();
                    helper.assertTrue(age(world, bush) == 1, "A picked bush goes back to its second stage, age " + age(world, bush));
                    helper.assertTrue(harvests.get() == 1, "Picking berries is a harvest");
                })
                .waitUntil(() -> dropped(world, ItemKeys.SWEET_BERRIES) >= 2 && dropped(world, ItemKeys.SWEET_BERRIES) <= 3,
                        "A ripe bush gives two or three berries")
                .build();
    }

    @ScenarioTest(timeoutTicks = 400)
    public static void forceLoadedChunksGetRandomTicks(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition wart = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:soul_sand");
                    set(world, wart, "minecraft:nether_wart");
                    // every block of a section is picked about once per game tick
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 4096);
                    world.setChunkForceLoaded(0, 0, true);
                })
                .waitUntil(() -> age(world, wart) == 3, "Nether wart in a force-loaded chunk should ripen on its own")
                .execute(() -> {
                    world.setChunkForceLoaded(0, 0, false);
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 0);
                })
                .build();
    }

    @ScenarioTest(timeoutTicks = 200)
    public static void noRandomTicksWithoutAPlayerNorAForcedChunk(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition wart = soil.relative(BlockFace.UP);
        final AtomicInteger waited = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:soul_sand");
                    set(world, wart, "minecraft:nether_wart");
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 4096);
                })
                .waitUntil(() -> waited.incrementAndGet() > 60, "Waiting three seconds")
                .execute(() -> helper.assertTrue(age(world, wart) == 0, "Nothing should grow in a chunk nobody keeps ticking"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 400)
    public static void chunksAroundAPlayerGetRandomTicks(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition wart = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:soul_sand");
                    set(world, wart, "minecraft:nether_wart");
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 4096);
                    player(helper, "farming_bystander", GameMode.SURVIVAL, ItemStack.EMPTY);
                })
                .waitUntil(() -> age(world, wart) == 3, "Nether wart next to a player should ripen on its own")
                .execute(() -> world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 0))
                .build();
    }

    @ScenarioTest(timeoutTicks = 400)
    public static void threeBlocksOfEachSectionArePickedPerTickByDefault(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final Plugin owner = owner();
        final AtomicInteger picked = new AtomicInteger();
        final AtomicInteger waited = new AtomicInteger();
        // lapis counts the random ticks it gets: no other test farms with it
        final FarmingBehaviour counting = new FarmingBehaviour() {
            @Override
            public boolean ticksRandomly(final BlockData state) {
                return true;
            }

            @Override
            public void randomTick(final FarmingWorld farming, final BlockPosition position, final BlockData state) {
                if (farming.world() == world) {
                    picked.incrementAndGet();
                }
            }
        };
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    // a quarter of the section right above the floor: four full layers of the chunk at the origin
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = Y; y < Y + 4; y++) {
                                set(world, Position.block(x, y, z), "minecraft:lapis_block");
                            }
                        }
                    }
                    world.gameRules().setInt(GameRuleKeys.RANDOM_TICK_SPEED, 3);
                    registry().attach(BlockTypeKeys.LAPIS_BLOCK, counting, owner);
                    player(helper, "farming_counter", GameMode.CREATIVE, ItemStack.EMPTY);
                })
                .waitUntil(() -> waited.incrementAndGet() > 200, "Waiting ten seconds")
                .execute(() -> {
                    registry().detachAll(owner);
                    // 200 ticks, 3 picks each, one in four landing on lapis: 150 on average
                    helper.assertTrue(picked.get() >= 90 && picked.get() <= 230,
                            "About 150 random ticks should reach a quarter of a section in 200 ticks, got " + picked.get());
                })
                .build();
    }

    @ScenarioTest
    public static void farmingGameRulesCanBeChangedByCommand(final ScenarioTestHelper helper) {
        helper.sequence()
                .execute(() -> {
                    final var command = GameRuleCommand.create();
                    for (final var rule : List.of(GameRuleKeys.RANDOM_TICK_SPEED, GameRuleKeys.BLOCK_DROPS, GameRuleKeys.MOB_GRIEFING)) {
                        helper.assertTrue(VanillaGameRules.ALL.get(VanillaGameRules.indexOf(rule)).implemented(),
                                rule.key().asString() + " drives farming: it should be marked as implemented");
                        helper.assertTrue(command.getChild(rule.key().asString()) != null,
                                "/gamerule should know " + rule.key().asString());
                    }
                })
                .build();
    }

    @ScenarioTest
    public static void growthCanBeCancelledOrChanged(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition wart = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:soul_sand");
                    set(world, wart, "minecraft:nether_wart");
                    final Subscription cancelling = listen(world, PlantGrowEvent.class, event -> {
                        helper.assertTrue(event.position().equals(wart) && event.target().equals(wart), "The wart grows in place");
                        helper.assertTrue(event.block().key().equals(BlockTypeKeys.NETHER_WART), "The event holds the plant growing");
                        event.setCancelled(true);
                    });
                    for (int tick = 0; tick < 500; tick++) {
                        randomTick(world, wart);
                    }
                    cancelling.unsubscribe();
                    helper.assertTrue(age(world, wart) == 0, "A cancelled growth should leave the plant as it is");

                    final Subscription changing = listen(world, PlantGrowEvent.class,
                            event -> event.setGrown(state("minecraft:nether_wart[age=3]")));
                    randomTicksUntil(helper, wart, () -> age(world, wart) > 0, "The wart should grow");
                    changing.unsubscribe();
                    helper.assertTrue(age(world, wart) == 3, "A listener should decide what the plant grows into, age " + age(world, wart));
                })
                .build();
    }

    @ScenarioTest
    public static void fertilizingAndTillingCanBeCancelled(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        final BlockPosition dirt = Position.block(10, SOIL, 8);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat");
                    set(world, dirt, "minecraft:dirt");
                    final FarmingWorld farming = farming(world);

                    final ServerPlayer fertilizing = player(helper, "farming_refused_meal", GameMode.SURVIVAL, ItemStack.of(ItemKeys.BONE_MEAL, 2));
                    final Subscription noFertilizer = listen(world, PlantFertilizeEvent.class, event -> {
                        helper.assertTrue(event.player() == fertilizing && event.fertilizer().id().equals(ItemKeys.BONE_MEAL),
                                "The event holds who fertilizes, and with what");
                        event.setCancelled(true);
                    });
                    helper.assertTrue(farming.useItem(fertilizing, fertilizing.selectedSlot(), crop, BlockFace.UP),
                            "A refused fertilizer is still handled");
                    noFertilizer.unsubscribe();
                    helper.assertTrue(age(world, crop) == 0, "A refused fertilizer should not grow the crop");
                    helper.assertTrue(fertilizing.inventory().get(fertilizing.selectedSlot()).count() == 2, "A refused fertilizer is not used up");

                    final ServerPlayer tilling = player(helper, "farming_refused_hoe", GameMode.SURVIVAL, ItemStack.of(ItemKeys.WOODEN_HOE));
                    final Subscription noTilling = listen(world, SoilTillEvent.class, event -> event.setCancelled(true));
                    helper.assertTrue(farming.useItem(tilling, tilling.selectedSlot(), dirt, BlockFace.UP), "A refused tilling is still handled");
                    noTilling.unsubscribe();
                    helper.assertBlockAt(dirt, BlockTypeKeys.DIRT);
                    helper.assertTrue(tilling.inventory().get(tilling.selectedSlot()).getOrDefault(DataComponentTypes.DAMAGE, 0) == 0,
                            "A refused tilling should not wear the hoe");

                    final Subscription wetTilling = listen(world, SoilTillEvent.class,
                            event -> event.setTilled(state("minecraft:farmland[moisture=7]")));
                    helper.assertTrue(farming.useItem(tilling, tilling.selectedSlot(), dirt, BlockFace.UP), "The hoe tills");
                    wetTilling.unsubscribe();
                    helper.assertTrue(moisture(world, dirt) == 7, "A listener should decide what the soil turns into");
                })
                .build();
    }

    @ScenarioTest
    public static void soilEventsCanKeepFarmlandAsItIs(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final AtomicInteger trampled = new AtomicInteger();
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland[moisture=7]");
                    final ServerPlayer player = player(helper, "farming_light_feet", GameMode.SURVIVAL, ItemStack.EMPTY);

                    final Subscription keepWet = listen(world, SoilMoistureEvent.class, event -> {
                        helper.assertTrue(event.moistened() instanceof final Farmland drier && drier.getMoisture() == 6,
                                "The event holds the soil one step drier");
                        event.setCancelled(true);
                    });
                    randomTick(world, soil);
                    keepWet.unsubscribe();
                    helper.assertTrue(moisture(world, soil) == 7, "A cancelled drying should keep the soil wet");

                    // a trample is a revert: listening to reverts sees it
                    final Subscription keepTilled = listen(world, SoilRevertEvent.class, event -> {
                        if (event instanceof final SoilTrampleEvent trample && trample.entity() == player) {
                            trampled.incrementAndGet();
                        }
                        event.setCancelled(true);
                    });
                    farming(world).entityLanded(player, soil, 5.0);
                    set(world, soil.relative(BlockFace.UP), "minecraft:stone");
                    keepTilled.unsubscribe();
                    helper.assertBlockAt(soil, BlockTypeKeys.FARMLAND);
                    helper.assertTrue(trampled.get() == 1, "The trample should have been seen as one, seen " + trampled.get());
                })
                .build();
    }

    @ScenarioTest
    public static void harvestCanBeChanged(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition crop = soil.relative(BlockFace.UP);
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    set(world, soil, "minecraft:farmland");
                    set(world, crop, "minecraft:wheat[age=7]");
                    final Subscription golden = listen(world, HarvestEvent.class, event -> {
                        helper.assertTrue(event.player() == null && event.tool().isEmpty(), "A crop breaking on its own has no harvester");
                        event.drops().clear();
                        event.drops().add(ItemStack.of(ItemKeys.GOLDEN_CARROT, 2));
                    });
                    farming(world).destroy(crop);
                    golden.unsubscribe();
                    helper.assertBlockAt(crop, BlockTypeKeys.AIR);
                })
                .waitUntil(() -> dropped(world, ItemKeys.GOLDEN_CARROT) == 2, "The harvest should be what the listener made of it")
                .execute(() -> helper.assertTrue(dropped(world, ItemKeys.WHEAT) == 0 && dropped(world, ItemKeys.WHEAT_SEEDS) == 0,
                        "What the listener removed should not drop"))
                .build();
    }

    @ScenarioTest
    public static void pluginsAddPlantsOfTheirOwn(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final BlockPosition soil = Position.block(8, SOIL, 8);
        final BlockPosition plant = soil.relative(BlockFace.UP);
        final Plugin owner = owner();
        // what a plugin would attach: emerald growing into diamond on a random tick, on gold only
        final TypedKey<BlockType> sprout = BlockTypeKeys.EMERALD_BLOCK;
        final FarmingBehaviour behaviour = new FarmingBehaviour() {
            @Override
            public boolean ticksRandomly(final BlockData state) {
                return true;
            }

            @Override
            public void randomTick(final FarmingWorld farming, final BlockPosition position, final BlockData state) {
                farming.grow(position, defaults(BlockTypeKeys.DIAMOND_BLOCK));
            }

            @Override
            public boolean canSurvive(final FarmingWorld farming, final BlockPosition position, final BlockData state) {
                return is(farming.world(), position.relative(BlockFace.DOWN), BlockTypeKeys.GOLD_BLOCK);
            }

            @Override
            public boolean canFertilize(final FarmingWorld farming, final BlockPosition position, final BlockData state) {
                return true;
            }

            @Override
            public void fertilize(final FarmingWorld farming, final BlockPosition position, final BlockData state) {
                farming.grow(position, position, defaults(BlockTypeKeys.DIAMOND_BLOCK), true);
            }

            @Override
            public List<ItemStack> drops(final FarmingWorld farming, final BlockPosition position, final BlockData state,
                                         final ItemStack tool) {
                return List.of(ItemStack.of(ItemKeys.EMERALD, 4));
            }
        };
        helper.sequence()
                .execute(() -> {
                    prepare(world);
                    final FarmingRegistry registry = registry();
                    helper.assertTrue(registry.behaviour(sprout).isEmpty(), "Emerald takes no part in farming on its own");
                    registry.attach(sprout, behaviour, owner);
                    registry.seed(ItemKeys.EMERALD, sprout, owner);
                    registry.fertilizer(ItemKeys.GLOWSTONE_DUST, owner);
                    registry.hoe(ItemKeys.STICK, owner);
                    registry.tillable(BlockTypeKeys.GOLD_BLOCK, () -> defaults(BlockTypeKeys.RAW_GOLD_BLOCK), owner);
                    try {
                        helper.assertTrue(registry.plant(ItemKeys.EMERALD).orElseThrow().equals(sprout), "The seed should plant the sprout");
                        helper.assertTrue(registry.blocks().contains(sprout), "The sprout should take part in farming");

                        final FarmingWorld farming = farming(world);
                        helper.assertFalse(farming.canSurvive(plant, defaults(sprout)), "The sprout should only stand on gold");
                        set(world, soil, "minecraft:gold_block");
                        set(world, plant, defaults(sprout));

                        final ServerPlayer player = player(helper, "farming_plugin", GameMode.SURVIVAL, ItemStack.of(ItemKeys.GLOWSTONE_DUST, 3));
                        helper.assertTrue(farming.useItem(player, player.selectedSlot(), plant, BlockFace.UP), "The plugin's fertilizer should take");
                        helper.assertBlockAt(plant, BlockTypeKeys.DIAMOND_BLOCK);
                        helper.assertTrue(player.inventory().get(player.selectedSlot()).count() == 2, "The plugin's fertilizer is used up");

                        set(world, plant, defaults(sprout));
                        randomTick(world, plant);
                        helper.assertBlockAt(plant, BlockTypeKeys.DIAMOND_BLOCK);

                        // the plugin's hoe turns the soil, which uproots the sprout
                        set(world, plant, defaults(sprout));
                        player.inventory().set(player.selectedSlot(), ItemStack.of(ItemKeys.STICK));
                        helper.assertFalse(farming.useItem(player, player.selectedSlot(), soil, BlockFace.UP), "The soil has no room above");
                        final Tillage tillage = registry.tillage(BlockTypeKeys.GOLD_BLOCK).orElseThrow();
                        helper.assertTrue(tillage.needsRoom() && tillage.tilled().key().equals(BlockTypeKeys.RAW_GOLD_BLOCK),
                                "The tillage should be the one declared");
                        set(world, soil, "minecraft:raw_gold_block");
                        helper.assertBlockAt(plant, BlockTypeKeys.AIR);
                    } finally {
                        registry.detachAll(owner);
                    }
                    helper.assertTrue(registry.behaviour(sprout).isEmpty() && registry.plant(ItemKeys.EMERALD).isEmpty()
                                    && !registry.isFertilizer(ItemKeys.GLOWSTONE_DUST) && !registry.isHoe(ItemKeys.STICK)
                                    && registry.tillage(BlockTypeKeys.GOLD_BLOCK).isEmpty(),
                            "Everything the plugin declared should be taken back");
                })
                .waitUntil(() -> dropped(world, ItemKeys.EMERALD) == 4, "The uprooted sprout should drop what its behaviour gives")
                .build();
    }

    @ScenarioTest
    public static void pluginsOverrideBuiltinPlantsAndGiveThemBack(final ScenarioTestHelper helper) {
        final Plugin first = owner();
        final Plugin second = owner();
        final FarmingBehaviour inert = new FarmingBehaviour() {
        };
        final FarmingBehaviour other = new FarmingBehaviour() {
        };
        helper.sequence()
                .execute(() -> {
                    final FarmingRegistry registry = registry();
                    // potatoes are grown by no other test: tests run side by side
                    final TypedKey<BlockType> potatoes = BlockTypeKeys.POTATOES;
                    final FarmingBehaviour builtin = registry.behaviour(potatoes).orElseThrow();
                    try {
                        registry.attach(potatoes, inert, first);
                        registry.attach(potatoes, other, second);
                        helper.assertTrue(registry.behaviour(potatoes).orElseThrow() == other, "The latest behaviour attached is in effect");
                        helper.assertTrue(registry.detach(potatoes, second), "The second plugin detaches its behaviour");
                        helper.assertTrue(registry.behaviour(potatoes).orElseThrow() == inert, "The previous behaviour is in effect again");
                        helper.assertFalse(registry.detach(potatoes, second), "Nothing is left to detach for the second plugin");
                    } finally {
                        registry.detachAll(first);
                        registry.detachAll(second);
                    }
                    helper.assertTrue(registry.behaviour(potatoes).orElseThrow() == builtin, "The built-in behaviour is in effect again");
                })
                .build();
    }

    @ScenarioTest
    public static void builtinSeedsHoesAndTillageAreDeclared(final ScenarioTestHelper helper) {
        helper.sequence()
                .execute(() -> {
                    final FarmingRegistry registry = registry();
                    helper.assertTrue(registry.plant(ItemKeys.WHEAT_SEEDS).orElseThrow().equals(BlockTypeKeys.WHEAT), "Wheat seeds plant wheat");
                    helper.assertTrue(registry.plant(ItemKeys.POTATO).orElseThrow().equals(BlockTypeKeys.POTATOES), "A potato plants potatoes");
                    helper.assertTrue(registry.plant(ItemKeys.MELON_SEEDS).orElseThrow().equals(BlockTypeKeys.MELON_STEM), "Melon seeds plant a stem");
                    helper.assertTrue(registry.plant(ItemKeys.STICK).isEmpty(), "A stick plants nothing");
                    helper.assertTrue(registry.isFertilizer(ItemKeys.BONE_MEAL) && !registry.isFertilizer(ItemKeys.BONE), "Bone meal fertilizes");
                    helper.assertTrue(registry.isHoe(ItemKeys.NETHERITE_HOE) && !registry.isHoe(ItemKeys.NETHERITE_SHOVEL), "Hoes come from their tag");

                    final Tillage grass = registry.tillage(BlockTypeKeys.GRASS_BLOCK).orElseThrow();
                    helper.assertTrue(grass.needsRoom() && grass.tilled().key().equals(BlockTypeKeys.FARMLAND), "Grass is tilled into farmland");
                    helper.assertTrue(registry.tillage(BlockTypeKeys.DIRT_PATH).isPresent(), "A path is tilled into farmland");
                    final Tillage rooted = registry.tillage(BlockTypeKeys.ROOTED_DIRT).orElseThrow();
                    helper.assertTrue(!rooted.needsRoom() && rooted.tilled().key().equals(BlockTypeKeys.DIRT), "Rooted dirt is tilled into dirt");
                    final List<ItemStack> roots = rooted.drops(ThreadLocalRandom.current());
                    helper.assertTrue(roots.size() == 1 && roots.getFirst().id().equals(ItemKeys.HANGING_ROOTS), "Rooted dirt gives its roots");
                    helper.assertTrue(registry.tillage(BlockTypeKeys.STONE).isEmpty(), "Stone cannot be tilled");
                })
                .build();
    }

    @ScenarioTest
    public static void lootTablesFollowTheStateAndTheTool(final ScenarioTestHelper helper) {
        helper.sequence()
                .execute(() -> {
                    final BlockLootTables loot = Objects.requireNonNull(BlockLootTables.instance(), "loot tables are not loaded");
                    final ThreadLocalRandom random = ThreadLocalRandom.current();
                    for (int roll = 0; roll < 200; roll++) {
                        final List<ItemStack> young = loot.drops(state("minecraft:beetroots[age=1]"), ItemStack.EMPTY, random);
                        helper.assertTrue(young.size() == 1 && young.getFirst().id().equals(ItemKeys.BEETROOT_SEEDS) && young.getFirst().count() == 1,
                                "Young beetroots give one seed, gave " + young);
                        final List<ItemStack> ripe = loot.drops(state("minecraft:beetroots[age=3]"), ItemStack.EMPTY, random);
                        helper.assertTrue(ripe.stream().anyMatch(stack -> stack.id().equals(ItemKeys.BEETROOT)), "Ripe beetroots give a beetroot");
                        final int slices = loot.drops(state("minecraft:melon"), ItemStack.EMPTY, random).stream()
                                .filter(stack -> stack.id().equals(ItemKeys.MELON_SLICE)).mapToInt(ItemStack::count).sum();
                        helper.assertTrue(slices >= 3 && slices <= 7, "A melon gives three to seven slices, gave " + slices);
                        final List<ItemStack> upper = loot.drops(state("minecraft:pitcher_crop[age=4,half=upper]"), ItemStack.EMPTY, random);
                        helper.assertTrue(upper.isEmpty(), "The upper half of a pitcher crop gives nothing, gave " + upper);
                        final List<ItemStack> pod = loot.drops(state("minecraft:cocoa[age=0]"), ItemStack.EMPTY, random);
                        helper.assertTrue(pod.size() == 1 && pod.getFirst().count() == 1, "An unripe pod gives one bean, gave " + pod);
                    }
                    final ItemStack silkTouch = ItemStack.of(ItemKeys.DIAMOND_PICKAXE).with(DataComponentTypes.ENCHANTMENTS,
                            ItemEnchantments.EMPTY.with(EnchantmentKeys.SILK_TOUCH, 1));
                    final List<ItemStack> whole = loot.drops(state("minecraft:melon"), silkTouch, random);
                    helper.assertTrue(whole.size() == 1 && whole.getFirst().id().equals(ItemKeys.MELON), "Silk touch gives the whole melon, gave " + whole);
                    helper.assertTrue(loot.drops(state("minecraft:air"), ItemStack.EMPTY, random).isEmpty(), "Air gives nothing");
                })
                .build();
    }
}
