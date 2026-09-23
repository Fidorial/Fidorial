package fr.euphyllia.fidorial.server.world.block.plant;

import fr.euphyllia.fidorial.server.plugin.BuiltInPlugin;
import fr.euphyllia.fidorial.server.world.block.FarmlandBlock;
import fr.fidorial.item.ItemStack;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.block.BlockDrop;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Set;

public final class VanillaPlants {

    private static final Plugin OWNER = BuiltInPlugin.INSTANCE;

    private VanillaPlants() {
        throw new UnsupportedOperationException("VanillaPlants cannot be instantiated.");
    }

    public static void registerAll(final BlockRegistry registry) {
        registry.register(FarmlandBlock.INSTANCE, OWNER);

        registerCrops(registry);
        registerStems(registry);
        registry.register(new CocoaBlock(), OWNER);
        registry.register(new SweetBerryBushBlock(), OWNER);
        registry.register(new LilyPadBlock(), OWNER);

        registerBlockItems(registry);
    }

    private static void registerCrops(final BlockRegistry registry) {
        registry.register(CropBlock.builder(BlockTypeKeys.WHEAT.key())
                .ripeDrops(List.of(drop(ItemKeys.WHEAT.key()), drop(ItemKeys.WHEAT_SEEDS.key(), 1, 4)))
                .immatureDrops(List.of(drop(ItemKeys.WHEAT_SEEDS.key())))
                .build(), OWNER);

        registry.register(CropBlock.builder(BlockTypeKeys.CARROTS.key())
                .ripeDrops(List.of(drop(ItemKeys.CARROT.key(), 1, 4)))
                .immatureDrops(List.of(drop(ItemKeys.CARROT.key())))
                .build(), OWNER);

        // TODO: the 2% poisonous potato wants chance-based drops.
        registry.register(CropBlock.builder(BlockTypeKeys.POTATOES.key())
                .ripeDrops(List.of(drop(ItemKeys.POTATO.key(), 1, 4)))
                .immatureDrops(List.of(drop(ItemKeys.POTATO.key())))
                .build(), OWNER);

        registry.register(CropBlock.builder(BlockTypeKeys.BEETROOTS.key())
                .growth(CropBlock.Growth.vanilla().scaled(2.0 / 3))
                .ripeDrops(List.of(drop(ItemKeys.BEETROOT.key()), drop(ItemKeys.BEETROOT_SEEDS.key(), 1, 4)))
                .immatureDrops(List.of(drop(ItemKeys.BEETROOT_SEEDS.key())))
                .build(), OWNER);

        registry.register(CropBlock.builder(BlockTypeKeys.NETHER_WART.key())
                .soils(Set.of(BlockTypeKeys.SOUL_SAND.key()))
                .minLight(0)
                .growth(CropBlock.Growth.fixed(1.0 / 10))
                .ripeDrops(List.of(drop(ItemKeys.NETHER_WART.key(), 2, 4)))
                .immatureDrops(List.of(drop(ItemKeys.NETHER_WART.key())))
                .placeSound(SoundEvents.of("item.nether_wart.plant"))
                .build(), OWNER);
    }

    private static void registerStems(final BlockRegistry registry) {
        stem(registry, BlockTypeKeys.MELON_STEM.key(), BlockTypeKeys.ATTACHED_MELON_STEM.key(),
                BlockTypeKeys.MELON.key(), ItemKeys.MELON_SEEDS.key());
        stem(registry, BlockTypeKeys.PUMPKIN_STEM.key(), BlockTypeKeys.ATTACHED_PUMPKIN_STEM.key(),
                BlockTypeKeys.PUMPKIN.key(), ItemKeys.PUMPKIN_SEEDS.key());
    }

    private static void stem(final BlockRegistry registry, final Key stem, final Key attached, final Key fruit, final Key seed) {
        registry.register(new StemBlock(stem, attached, fruit, seed), OWNER);
        registry.register(new AttachedStemBlock(attached, stem, fruit, seed), OWNER);
    }

    private static void registerBlockItems(final BlockRegistry registry) {
        registry.registerBlockItem(ItemKeys.WHEAT_SEEDS.key(), BlockTypeKeys.WHEAT.key(), OWNER);
        registry.registerBlockItem(ItemKeys.CARROT.key(), BlockTypeKeys.CARROTS.key(), OWNER);
        registry.registerBlockItem(ItemKeys.POTATO.key(), BlockTypeKeys.POTATOES.key(), OWNER);
        registry.registerBlockItem(ItemKeys.BEETROOT_SEEDS.key(), BlockTypeKeys.BEETROOTS.key(), OWNER);
        registry.registerBlockItem(ItemKeys.MELON_SEEDS.key(), BlockTypeKeys.MELON_STEM.key(), OWNER);
        registry.registerBlockItem(ItemKeys.PUMPKIN_SEEDS.key(), BlockTypeKeys.PUMPKIN_STEM.key(), OWNER);
        registry.registerBlockItem(ItemKeys.COCOA_BEANS.key(), BlockTypeKeys.COCOA.key(), OWNER);
        registry.registerBlockItem(ItemKeys.SWEET_BERRIES.key(), BlockTypeKeys.SWEET_BERRY_BUSH.key(), OWNER);
    }

    private static BlockDrop drop(final Key item) {
        return BlockDrop.of(ItemStack.of(item));
    }

    private static BlockDrop drop(final Key item, final int min, final int max) {
        return BlockDrop.of(ItemStack.of(item), min, max);
    }
}
