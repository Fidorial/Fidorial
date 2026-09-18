package fr.euphyllia.fidorial.server.world.block.crop;

import fr.euphyllia.fidorial.server.world.block.FarmlandBlock;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.item.ItemStack;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.crop.CropRegistry;
import fr.fidorial.world.block.crop.CropType;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class CropPlanting {

    private static final Sound PLANT_SOUND =
            Sound.sound(SoundEvents.CROP_PLANT, Sound.Source.BLOCK, 1.0f, 1.0f);

    private final CropRegistry crops;

    public CropPlanting(final CropRegistry crops) {
        this.crops = crops;
    }

    public InteractionResult plant(final BlockInteractionContext context) {
        if (!(context instanceof final FidorialBlockInteractionContext ctx)) {
            return InteractionResult.PASS;
        }

        final ItemStack held = ctx.heldItem();
        if (held.isEmpty()) {
            return InteractionResult.PASS;
        }

        final CropType crop = crops.bySeed(held.id());
        if (crop == null) {
            return InteractionResult.PASS;
        }

        if (ctx.face() != BlockFace.UP) {
            return InteractionResult.PASS;
        }

        final BlockState soil = ctx.state();
        if (!crop.acceptsSoil(soil.name())) {
            return InteractionResult.PASS;
        }

        if (!soilIsUsable(crop, soil)) {
            return InteractionResult.CONSUME;
        }

        if (!ctx.stateAt(0, 1, 0).isAir()) {
            return InteractionResult.CONSUME;
        }

        if (!ctx.setBlockAt(0, 1, 0, seedlingState(crop))) {
            return InteractionResult.CONSUME;
        }

        ctx.consumeHeldItem();
        ctx.playSound(PLANT_SOUND);
        return InteractionResult.SUCCESS;
    }

    public static BlockState seedlingState(final CropType crop) {
        return stateAtAge(crop, 0);
    }

    public static BlockState stateAtAge(final CropType crop, final int age) {
        final int clamped = Math.clamp(age, 0, crop.maxAge());
        return BlockState.of(crop.block(), Map.of(crop.ageProperty(), Integer.toString(clamped)));
    }

    public static boolean soilIsUsable(final CropType crop, final BlockState state) {
        if (!crop.requiresMoistSoil() || !FarmlandBlock.is(state)) {
            return true;
        }
        return FarmlandBlock.moisture(state) > 0;
    }

    public static @Nullable CropType cropAt(final CropRegistry crops, final BlockState state) {
        return crops.byBlock(state.name());
    }
}
