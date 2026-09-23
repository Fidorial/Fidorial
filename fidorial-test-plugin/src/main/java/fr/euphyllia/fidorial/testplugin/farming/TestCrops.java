package fr.euphyllia.fidorial.testplugin.farming;

import fr.fidorial.Server;
import fr.fidorial.item.ItemDefinition;
import fr.fidorial.item.ItemRegistry;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockDrop;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Crops that do not exist in vanilla, each showing a different way of farming
 * through the block behaviour API:
 *
 * <ul>
 *   <li><b>Rice</b> — a plain {@link CropBlock} with its own growth rule: it only
 *       grows while its soil touches water (a paddy field).</li>
 *   <li><b>Tomatoes</b> — {@link RegrowingCropBlock}: right-click to pick, the plant
 *       stays and bears again.</li>
 *   <li><b>Hemp</b> — {@link ClimbingCropBlock}: grows upwards, three blocks tall.</li>
 *   <li><b>Truffles</b> — a {@link CropBlock} that only grows in the dark, on
 *       forest soil.</li>
 *   <li><b>Mint</b> — {@link SpreadingCropBlock}: invades the soil around it once
 *       ripe.</li>
 * </ul>
 *
 * <p>The client only knows vanilla blocks and items, so each crop borrows the look
 * of a vanilla one ({@link #lookalike}), and each item is rendered as a vanilla
 * item with its own name.</p>
 */
public final class TestCrops {

    private static final String NAMESPACE = "testplugin";

    public static final Key RICE_SEEDS = key("rice_seeds");
    public static final Key RICE = key("rice");
    public static final Key TOMATO_SEEDS = key("tomato_seeds");
    public static final Key TOMATO = key("tomato");
    public static final Key HEMP_CUTTING = key("hemp_cutting");
    public static final Key HEMP_FIBER = key("hemp_fiber");
    public static final Key TRUFFLE_SPORES = key("truffle_spores");
    public static final Key TRUFFLE = key("truffle");
    public static final Key MINT = key("mint");

    public static final Key RICE_PLANT = key("rice_plant");
    public static final Key TOMATO_PLANT = key("tomato_plant");
    public static final Key HEMP_PLANT = key("hemp_plant");
    public static final Key TRUFFLE_PLANT = key("truffle_plant");
    public static final Key MINT_PLANT = key("mint_plant");

    /**
     * What {@code /farm} hands out: whatever plants each crop, sixteen of each.
     */
    public static final List<Key> STARTER_KIT = List.of(RICE_SEEDS, TOMATO_SEEDS, HEMP_CUTTING, TRUFFLE_SPORES, MINT);

    private static final Key WATER = BlockTypeKeys.WATER.key();
    private static final Key FARMLAND = BlockTypeKeys.FARMLAND.key();

    private TestCrops() {
        throw new UnsupportedOperationException("TestCrops cannot be instantiated.");
    }

    /**
     * Must run in {@code onLoad}, before the worlds open, so chunks that already
     * hold these crops find their blocks when they load.
     */
    public static void registerAll(final Server server, final Plugin plugin, final ComponentLogger logger) {
        registerItems(server.items(), plugin);

        final BlockRegistry blocks = Blocks.registry();
        lookalike(blocks, RICE_PLANT, BlockTypeKeys.WHEAT.key(), 8);
        lookalike(blocks, TOMATO_PLANT, BlockTypeKeys.BEETROOTS.key(), 4);
        lookalike(blocks, HEMP_PLANT, BlockTypeKeys.SUGAR_CANE.key(), 4);
        lookalike(blocks, TRUFFLE_PLANT, BlockTypeKeys.NETHER_WART.key(), 4);
        lookalike(blocks, MINT_PLANT, BlockTypeKeys.CARROTS.key(), 8);

        blocks.register(rice(blocks), plugin);
        blocks.register(tomatoes(blocks), plugin);
        blocks.register(hemp(blocks), plugin);
        blocks.register(truffles(blocks), plugin);
        blocks.register(mint(blocks), plugin);

        blocks.registerBlockItem(RICE_SEEDS, RICE_PLANT, plugin);
        blocks.registerBlockItem(TOMATO_SEEDS, TOMATO_PLANT, plugin);
        blocks.registerBlockItem(HEMP_CUTTING, HEMP_PLANT, plugin);
        blocks.registerBlockItem(TRUFFLE_SPORES, TRUFFLE_PLANT, plugin);
        blocks.registerBlockItem(MINT, MINT_PLANT, plugin);

        logger.info("[TestPlugin] Custom crops registered: rice, tomatoes, hemp, truffles, mint. Type /farm to get seeds.");
    }

    /**
     * Block behaviours and block items are dropped by the server when the plugin
     * is disabled; items are not.
     */
    public static void unregisterAll(final Server server, final Plugin plugin) {
        server.items().unregisterAll(plugin);
    }

    /**
     * Grows only while the soil under it touches water — plant it in a paddy.
     */
    private static CropBlock rice(final BlockRegistry blocks) {
        return blocks.crop(RICE_PLANT)
                .soils(Set.of(FARMLAND, BlockTypeKeys.MUD.key()))
                .growsWhen((data, world, pos) -> isFlooded(world, pos.offset(0, -1, 0)))
                .ripeDrops(List.of(drop(RICE, 1, 3), drop(RICE_SEEDS, 1, 2)))
                .immatureDrops(List.of(drop(RICE_SEEDS, 1, 1)))
                .build();
    }

    /**
     * Right-click when red to pick 2–4 tomatoes; the plant goes back to stage 2.
     */
    private static CropBlock tomatoes(final BlockRegistry blocks) {
        return new RegrowingCropBlock(
                blocks.crop(TOMATO_PLANT)
                        .ripeDrops(List.of(drop(TOMATO, 1, 2), drop(TOMATO_SEEDS, 1, 1)))
                        .immatureDrops(List.of(drop(TOMATO_SEEDS, 1, 1)))
                        .build(),
                2,
                List.of(drop(TOMATO, 2, 4)));
    }

    /**
     * Up to three blocks tall; break the bottom to bring the whole stalk down.
     */
    private static CropBlock hemp(final BlockRegistry blocks) {
        return new ClimbingCropBlock(
                blocks.crop(HEMP_PLANT)
                        .growth(CropBlock.Growth.fixed(1.0 / 6))
                        .ripeDrops(List.of(drop(HEMP_FIBER, 1, 2), drop(HEMP_CUTTING, 0, 1)))
                        .immatureDrops(List.of(drop(HEMP_CUTTING, 1, 1)))
                        .build(),
                3,
                1.0 / 4);
    }

    /**
     * On podzol, mycelium or rooted dirt, and only where light stays at 7 or below.
     */
    private static CropBlock truffles(final BlockRegistry blocks) {
        return blocks.crop(TRUFFLE_PLANT)
                .soils(Set.of(BlockTypeKeys.PODZOL.key(), BlockTypeKeys.MYCELIUM.key(), BlockTypeKeys.ROOTED_DIRT.key()))
                .minLight(0)
                .growsWhen((data, world, pos) -> world.lightLevel(pos) <= 7)
                .growth(CropBlock.Growth.fixed(1.0 / 8))
                .ripeDrops(List.of(drop(TRUFFLE, 1, 2), drop(TRUFFLE_SPORES, 1, 2)))
                .immatureDrops(List.of(drop(TRUFFLE_SPORES, 1, 1)))
                .placeSound(SoundEvents.of("item.nether_wart.plant"))
                .build();
    }

    /**
     * Once ripe, spreads to any free farmland, dirt or grass next to it.
     */
    private static CropBlock mint(final BlockRegistry blocks) {
        return new SpreadingCropBlock(
                blocks.crop(MINT_PLANT)
                        .soils(Set.of(FARMLAND, BlockTypeKeys.DIRT.key(), BlockTypeKeys.GRASS_BLOCK.key()))
                        .ripeDrops(List.of(drop(MINT, 2, 4)))
                        .immatureDrops(List.of(drop(MINT, 1, 1)))
                        .build(),
                1.0 / 3);
    }

    private static boolean isFlooded(final BlockAccess world, final BlockPos soil) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if ((dx != 0 || dz != 0) && world.blockAt(soil.offset(dx, 0, dz)).key().equals(WATER)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void registerItems(final ItemRegistry items, final Plugin plugin) {
        item(items, plugin, RICE_SEEDS, ItemKeys.WHEAT_SEEDS.key(), "Rice seeds", NamedTextColor.WHITE);
        item(items, plugin, RICE, ItemKeys.WHEAT.key(), "Rice", NamedTextColor.WHITE);
        item(items, plugin, TOMATO_SEEDS, ItemKeys.BEETROOT_SEEDS.key(), "Tomato seeds", NamedTextColor.RED);
        item(items, plugin, TOMATO, ItemKeys.APPLE.key(), "Tomato", NamedTextColor.RED);
        item(items, plugin, HEMP_CUTTING, ItemKeys.SUGAR_CANE.key(), "Hemp cutting", NamedTextColor.GREEN);
        item(items, plugin, HEMP_FIBER, ItemKeys.STRING.key(), "Hemp fiber", NamedTextColor.GREEN);
        item(items, plugin, TRUFFLE_SPORES, ItemKeys.NETHER_WART.key(), "Truffle spores", NamedTextColor.GOLD);
        item(items, plugin, TRUFFLE, ItemKeys.BROWN_DYE.key(), "Truffle", NamedTextColor.GOLD);
        item(items, plugin, MINT, ItemKeys.CARROT.key(), "Mint", NamedTextColor.AQUA);
    }

    private static void item(final ItemRegistry items,
                             final Plugin plugin,
                             final Key key,
                             final Key lookalike,
                             final String name,
                             final NamedTextColor color) {
        if (items.isCustom(key)) {
            return;
        }
        items.register(ItemDefinition.builder(key, lookalike)
                .set(DataComponentTypes.MAX_STACK_SIZE, 64)
                .set(DataComponentTypes.ITEM_NAME, Component.text(name, color))
                .build(), plugin);
    }

    /**
     * Declares a plugin block with {@code stages} growth stages, shown to players as
     * the stages of a vanilla crop spread over the same range.
     */
    private static void lookalike(final BlockRegistry blocks, final Key key, final Key vanilla, final int stages) {
        if (blocks.type(key).isPresent()) {
            return;
        }
        final BlockType base = blocks.type(vanilla).orElseThrow();
        final BlockProperty baseAge = Objects.requireNonNull(base.property(CropBlock.AGE),
                () -> vanilla.asString() + " has no age property");
        final int baseMax = baseAge.values().size() - 1;

        final String[] values = new String[stages];
        final int[] stateIds = new int[stages];
        for (int stage = 0; stage < stages; stage++) {
            values[stage] = Integer.toString(stage);
            final int mapped = stages == 1 ? 0 : Math.round((float) stage * baseMax / (stages - 1));
            final BlockData look = Objects.requireNonNull(
                    base.data(Map.of(CropBlock.AGE, baseAge.values().get(mapped))));
            stateIds[stage] = look.networkId();
        }
        blocks.register(BlockType.builder(key)
                .property(CropBlock.AGE, values)
                .stateIds(stateIds)
                .build());
    }

    private static BlockDrop drop(final Key item, final int min, final int max) {
        return BlockDrop.of(ItemStack.of(item), min, max);
    }

    private static Key key(final String value) {
        return Key.key(NAMESPACE, value);
    }
}
