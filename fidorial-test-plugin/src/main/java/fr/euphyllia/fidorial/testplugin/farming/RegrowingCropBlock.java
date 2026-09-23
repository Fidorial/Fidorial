package fr.euphyllia.fidorial.testplugin.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockDrop;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.sound.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A crop you do not replant: once ripe, right-clicking it picks the harvest and
 * the plant drops back to an earlier stage to bear again (tomatoes).
 */
public final class RegrowingCropBlock extends CropBlock {

    private static final Sound.Type PICK_SOUND = SoundEvents.of("block.sweet_berry_bush.pick_berries");

    private final int regrowAge;
    private final List<BlockDrop> harvest;

    /**
     * @param builder   the crop settings
     * @param regrowAge the stage the plant falls back to after a harvest
     * @param harvest   what one right-click harvest gives
     */
    public RegrowingCropBlock(final CropBlock.Builder builder, final int regrowAge, final List<BlockDrop> harvest) {
        super(builder);
        this.regrowAge = regrowAge;
        this.harvest = List.copyOf(harvest);
    }

    @Override
    public InteractionResult use(final BlockData data, final BlockInteractionContext context) {
        if (!isRipe(data)) {
            return InteractionResult.PASS;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final List<ItemStack> picked = new ArrayList<>(harvest.size());
        for (final BlockDrop drop : harvest) {
            final ItemStack rolled = drop.roll(random);
            if (!rolled.isEmpty()) {
                picked.add(rolled);
            }
        }
        context.blocks().dropItems(context.pos(), picked);
        context.playSound(Sound.sound(PICK_SOUND, Sound.Source.BLOCK, 1.0f, 0.8f + random.nextFloat() * 0.4f));
        context.blocks().setBlock(context.pos(), withAge(data, regrowAge));
        return InteractionResult.SUCCESS;
    }
}
