package fr.euphyllia.fidorial.server.world.block.farming;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.world.farming.FidorialFarmingRegistry;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.farming.FarmingBehaviour;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The built-in farming: the behaviours of farmland and of the plants growing on their own, the
 * items planting them, and what fertilizes and tills.
 *
 * <p>A block takes part in farming only through the behaviour attached to it, and every one
 * attached here can be replaced by a plugin through the {@link FidorialFarmingRegistry}.</p>
 */
public final class BuiltinFarming {

    private BuiltinFarming() {
        throw new UnsupportedOperationException("BuiltinFarming cannot be instantiated.");
    }

    /**
     * {@return the block behaviours of the farming blocks whose placement or use is their own, to
     * register with the block registry}
     */
    public static List<BlockBehaviour> blockBehaviours() {
        return List.of(CocoaBlock.INSTANCE, SweetBerryBushBlock.INSTANCE);
    }

    /**
     * Attaches the built-in farming behaviours to their blocks, and declares the built-in seeds
     * and fertilizers.
     *
     * @param registry where to attach and declare them
     */
    public static void attach(final FidorialFarmingRegistry registry) {
        registry.attachBuiltin(BlockTypeKeys.FARMLAND, FarmlandBlock.INSTANCE);
        for (final CropBlock crop : CropBlock.ALL) {
            registry.attachBuiltin(crop.key(), crop);
        }
        registry.attachBuiltin(PitcherCropBlock.KEY, PitcherCropBlock.INSTANCE);
        for (final StemBlock stem : List.of(StemBlock.MELON, StemBlock.PUMPKIN)) {
            registry.attachBuiltin(stem.key(), stem);
            registry.attachBuiltin(stem.fruit(), ProduceBlock.INSTANCE);
        }
        registry.attachBuiltin(AttachedStemBlock.MELON.key(), AttachedStemBlock.MELON);
        registry.attachBuiltin(AttachedStemBlock.PUMPKIN.key(), AttachedStemBlock.PUMPKIN);
        registry.attachBuiltin(BlockTypeKeys.NETHER_WART, NetherWartBlock.INSTANCE);
        registry.attachBuiltin(ColumnPlantBlock.SUGAR_CANE.key(), ColumnPlantBlock.SUGAR_CANE);
        registry.attachBuiltin(ColumnPlantBlock.CACTUS.key(), ColumnPlantBlock.CACTUS);
        registry.attachBuiltin(BlockTypeKeys.CACTUS_FLOWER, CactusFlowerBlock.INSTANCE);
        for (final BlockBehaviour behaviour : blockBehaviours()) {
            if (behaviour instanceof final FarmingBehaviour farming) {
                registry.attachBuiltin(behaviour.key(), farming);
            }
        }

        registry.seedBuiltin(ItemKeys.WHEAT_SEEDS, BlockTypeKeys.WHEAT);
        registry.seedBuiltin(ItemKeys.CARROT, BlockTypeKeys.CARROTS);
        registry.seedBuiltin(ItemKeys.POTATO, BlockTypeKeys.POTATOES);
        registry.seedBuiltin(ItemKeys.BEETROOT_SEEDS, BlockTypeKeys.BEETROOTS);
        registry.seedBuiltin(ItemKeys.TORCHFLOWER_SEEDS, BlockTypeKeys.TORCHFLOWER_CROP);
        registry.seedBuiltin(ItemKeys.PITCHER_POD, PitcherCropBlock.KEY);
        registry.seedBuiltin(ItemKeys.MELON_SEEDS, BlockTypeKeys.MELON_STEM);
        registry.seedBuiltin(ItemKeys.PUMPKIN_SEEDS, BlockTypeKeys.PUMPKIN_STEM);
        registry.seedBuiltin(ItemKeys.NETHER_WART, BlockTypeKeys.NETHER_WART);
        registry.seedBuiltin(ItemKeys.SUGAR_CANE, BlockTypeKeys.SUGAR_CANE);
        registry.seedBuiltin(ItemKeys.CACTUS, BlockTypeKeys.CACTUS);
        registry.seedBuiltin(ItemKeys.COCOA_BEANS, CocoaBlock.KEY);
        registry.seedBuiltin(ItemKeys.SWEET_BERRIES, SweetBerryBushBlock.KEY);

        registry.fertilizerBuiltin(ItemKeys.BONE_MEAL);
    }

    /**
     * Declares what hoes do to the built-in blocks, from the definition of the hoe in the data.
     *
     * @param registry   where to declare it
     * @param definition the {@code minecraft:hoe} entry of the {@code minecraft:block_transformer}
     *                   registry, {@code null} when there is none
     * @param blocks     the block registry
     * @return how many rules were declared
     */
    public static int declareTillage(final FidorialFarmingRegistry registry, final @Nullable JsonElement definition,
                                     final BlockRegistry blocks) {
        return HoeTillage.declare(registry, definition, blocks);
    }
}
