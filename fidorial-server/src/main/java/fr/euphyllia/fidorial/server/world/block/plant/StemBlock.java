package fr.euphyllia.fidorial.server.world.block.plant;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class StemBlock extends FidorialCropBlock {

    static final BlockFace[] HORIZONTAL = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

    /**
     * What the fruit may land on (vanilla's {@code dirt} tag, plus farmland).
     */
    private static final Set<Key> FRUIT_SOILS = Set.of(
            BlockTypeKeys.DIRT.key(),
            BlockTypeKeys.GRASS_BLOCK.key(),
            BlockTypeKeys.FARMLAND.key(),
            BlockTypeKeys.COARSE_DIRT.key(),
            BlockTypeKeys.PODZOL.key(),
            BlockTypeKeys.ROOTED_DIRT.key(),
            BlockTypeKeys.MYCELIUM.key(),
            BlockTypeKeys.MOSS_BLOCK.key(),
            BlockTypeKeys.PALE_MOSS_BLOCK.key(),
            BlockTypeKeys.MUD.key());

    private final Key attachedStem;
    private final Key fruit;
    private final Key seed;

    public StemBlock(final Key stem, final Key attachedStem, final Key fruit, final Key seed) {
        super(FidorialCropBlock.builder(stem));
        this.attachedStem = attachedStem;
        this.fruit = fruit;
        this.seed = seed;
    }

    @Override
    public boolean isRandomlyTicking(final BlockData data) {
        return true;
    }

    @Override
    public void randomTick(final BlockData data, final BlockAccess world, final BlockPos pos, final RandomGenerator random) {
        if (!canGrow(data, world, pos) || !rollGrowth(data, world, pos, random)) {
            return;
        }
        if (!isRipe(data)) {
            world.setBlock(pos, withAge(data, age(data) + 1));
            return;
        }

        final BlockFace direction = HORIZONTAL[random.nextInt(HORIZONTAL.length)];
        final BlockPos target = pos.relative(direction);
        if (!world.blockAt(target).isAir()
                || !FRUIT_SOILS.contains(world.blockAt(target.offset(0, -1, 0)).key())) {
            return;
        }
        world.setBlock(target, defaultData(fruit));
        world.setBlock(pos, defaultData(attachedStem).with("facing", direction.name().toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        return seeds(seed, 3, (age(data) + 1) / 15.0, random);
    }

    static List<ItemStack> seeds(final Key seed, final int tries, final double chance, final RandomGenerator random) {
        int count = 0;
        for (int i = 0; i < tries; i++) {
            if (random.nextDouble() < chance) {
                count++;
            }
        }
        return count == 0 ? List.of() : List.of(ItemStack.of(seed, count));
    }

    static BlockData defaultData(final Key block) {
        final BlockType type = Objects.requireNonNull(Blocks.type(block), () -> "Unknown block type " + block.asString());
        return Objects.requireNonNull(type.defaultData(), () -> "No default state for " + block.asString());
    }
}
