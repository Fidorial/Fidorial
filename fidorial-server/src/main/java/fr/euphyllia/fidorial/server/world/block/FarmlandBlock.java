package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockGetter;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class FarmlandBlock implements BlockBehaviour {

    public static final FarmlandBlock INSTANCE = new FarmlandBlock();

    public static final Key KEY = BlockTypeKeys.FARMLAND.key();
    public static final String MOISTURE = "moisture";
    public static final int MAX_MOISTURE = 7;
    public static final int WATER_RANGE = 4;

    private static final Set<Key> MAINTAINS_FARMLAND = Set.of(
            BlockTypeKeys.ATTACHED_MELON_STEM.key(),
            BlockTypeKeys.ATTACHED_PUMPKIN_STEM.key(),
            BlockTypeKeys.TORCHFLOWER_CROP.key(),
            BlockTypeKeys.PITCHER_CROP.key());

    private FarmlandBlock() {
    }

    public static boolean is(final BlockState state) {
        return KEY.equals(state.name());
    }

    public static BlockState withMoisture(final int moisture) {
        final int clamped = Math.clamp(moisture, 0, MAX_MOISTURE);
        return BlockState.of(KEY, Map.of(MOISTURE, Integer.toString(clamped)));
    }

    public static boolean isHydrated(final BlockGetter world, final BlockPos pos) {
        for (int dx = -WATER_RANGE; dx <= WATER_RANGE; dx++) {
            for (int dz = -WATER_RANGE; dz <= WATER_RANGE; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (isWater(world.blockAt(pos.offset(dx, dy, dz)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isWater(final @Nullable BlockData data) {
        if (data == null) {
            return false;
        }
        return BlockTypeKeys.WATER.key().equals(data.key())
                || (data.type().hasProperty("waterlogged") && "true".equals(data.get("waterlogged")));
    }

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(KEY));
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    @Override
    public boolean isRandomlyTicking(final BlockData data) {
        return true;
    }

    @Override
    @NeedsToBeRevisited("Rain falling on the block should keep it wet too.")
    public void randomTick(final BlockData data, final BlockAccess world, final BlockPos pos, final RandomGenerator random) {
        final int moisture = moisture(data);
        if (isHydrated(world, pos)) {
            if (moisture < MAX_MOISTURE) {
                world.setBlock(pos, data.with(MOISTURE, Integer.toString(MAX_MOISTURE)));
            }
            return;
        }
        if (moisture > 0) {
            world.setBlock(pos, data.with(MOISTURE, Integer.toString(moisture - 1)));
        } else if (!maintainedBy(world.blockAt(pos.offset(0, 1, 0)))) {
            final BlockType dirt = Blocks.type(BlockTypeKeys.DIRT.key());
            if (dirt != null && dirt.defaultData() != null) {
                world.setBlock(pos, dirt.defaultData());
            }
        }
    }

    private static int moisture(final BlockData data) {
        try {
            return Integer.parseInt(data.get(MOISTURE));
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private static boolean maintainedBy(final BlockData above) {
        if (MAINTAINS_FARMLAND.contains(above.key())) {
            return true;
        }
        return Blocks.registry().behaviour(above.key()).orElse(null) instanceof CropBlock;
    }
}
