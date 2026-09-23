package fr.euphyllia.fidorial.server.world.block.plant;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class AttachedStemBlock implements BlockBehaviour {

    private final Key key;
    private final Key stem;
    private final Key fruit;
    private final Key seed;

    public AttachedStemBlock(final Key key, final Key stem, final Key fruit, final Key seed) {
        this.key = key;
        this.stem = stem;
        this.fruit = fruit;
        this.seed = seed;
    }

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(key), () -> "Unknown block type " + key.asString());
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        return world.blockAt(pos.offset(0, -1, 0)).key().equals(BlockTypeKeys.FARMLAND.key());
    }

    @Override
    public BlockData updateShape(final BlockData data,
                                 final BlockFace direction,
                                 final BlockData neighbour,
                                 final BlockAccess world,
                                 final BlockPos pos) {
        final boolean towardsFruit = direction.name().toLowerCase(Locale.ROOT).equals(data.get("facing"));
        if (towardsFruit && !neighbour.key().equals(fruit)) {
            return StemBlock.defaultData(stem).with(CropBlock.AGE, "7");
        }
        return data;
    }

    @Override
    public List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        return StemBlock.seeds(seed, 3, 8 / 15.0, random);
    }

    @Override
    public boolean breaksInstantly(final BlockData data) {
        return true;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return 0;
    }
}
