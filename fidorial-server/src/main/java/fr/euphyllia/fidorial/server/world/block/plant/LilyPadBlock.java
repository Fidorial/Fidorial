package fr.euphyllia.fidorial.server.world.block.plant;

import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;


@NeedsToBeRevisited("Placing from the hotbar needs ServerboundUseItemPacket and a fluid ray-trace.")
public final class LilyPadBlock implements BlockBehaviour {

    private static final Key KEY = BlockTypeKeys.LILY_PAD.key();
    private static final Sound.Type PLACE_SOUND = SoundEvents.of("block.lily_pad.place");

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(KEY));
    }

    @Override
    public boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        final BlockData below = world.blockAt(pos.offset(0, -1, 0));
        final Key key = below.key();
        if (key.equals(BlockTypeKeys.ICE.key()) || key.equals(BlockTypeKeys.FROSTED_ICE.key())) {
            return true;
        }
        if (key.equals(BlockTypeKeys.WATER.key())) {
            return "0".equals(below.get("level"));
        }
        return below.type().hasProperty("waterlogged") && "true".equals(below.get("waterlogged"));
    }

    @Override
    public List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        return List.of(ItemStack.of(ItemKeys.LILY_PAD.key()));
    }

    @Override
    public boolean breaksInstantly(final BlockData data) {
        return true;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return 0;
    }

    @Override
    public Sound.Type placeSound(final BlockData data) {
        return PLACE_SOUND;
    }
}
