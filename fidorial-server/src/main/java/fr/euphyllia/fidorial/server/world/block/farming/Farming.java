package fr.euphyllia.fidorial.server.world.block.farming;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.loot.BlockLootTables;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Ageable;
import fr.fidorial.world.farming.FarmingWorld;
import fr.fidorial.world.redstone.SupportType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * What the built-in farming blocks share: the light plants need, the chance crops grow with, and
 * what blocks drop.
 */
final class Farming {

    /**
     * The light a crop needs to grow.
     */
    static final int GROWTH_LIGHT = 9;

    /**
     * The light a crop that cannot see the sky needs to stay planted.
     */
    static final int SURVIVAL_LIGHT = 8;

    static final BlockFace[] HORIZONTAL = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private static final BlockFace[] ALL_FACES = BlockFace.values();

    private Farming() {
        throw new UnsupportedOperationException("Farming cannot be instantiated.");
    }

    /**
     * {@return the default state of a block type}
     *
     * @param key the block type
     * @throws IllegalStateException if the block type is not registered
     */
    static BlockData defaultState(final TypedKey<BlockType> key) {
        final BlockType type = Blocks.type(key);
        final BlockData data = type == null ? null : type.defaultData();
        Preconditions.checkState(data != null, "The block type %s is not registered", key);
        return data;
    }

    /**
     * {@return the growth stage of a state, {@code 0} for a state without one}
     */
    static int age(final BlockData state) {
        return state instanceof final Ageable ageable ? ageable.getAge() : 0;
    }

    /**
     * {@return the last growth stage of a state, {@code 0} for a state without one}
     */
    static int maxAge(final BlockData state) {
        return state instanceof final Ageable ageable ? ageable.getMaximumAge() : 0;
    }

    /**
     * {@return a state at another growth stage}
     */
    static BlockData withAge(final BlockData state, final int age) {
        return state instanceof final Ageable ageable ? ageable.withAge(age) : state;
    }

    /**
     * {@return {@code true} if a state is of a block type}
     *
     * @param state the state, {@code null} for a block that is not loaded
     */
    static boolean is(final @Nullable BlockData state, final TypedKey<BlockType> block) {
        return state != null && state.key().equals(block);
    }

    /**
     * {@return {@code true} if a plant gets enough light to stay planted: it sees the sky, or what
     * lights it is bright enough}
     */
    static boolean hasSurvivalLight(final FarmingWorld world, final BlockPosition position) {
        return world.canSeeSky(position) || world.lightLevel(position) >= SURVIVAL_LIGHT;
    }

    /**
     * Rolls whether a crop grows on a random tick: one chance in {@code floor(25 / speed) + 1},
     * the speed being what its field {@linkplain FarmingWorld#growthSpeed gives} it.
     */
    static boolean rollsGrowth(final FarmingWorld world, final BlockPosition position) {
        final float speed = world.growthSpeed(position);
        return world.random().nextInt((int) (25.0f / speed) + 1) == 0;
    }

    /**
     * {@return how many stages a fertilizer adds to a crop: two to five}
     */
    static int fertilizedStages(final RandomGenerator random) {
        return 2 + random.nextInt(4);
    }

    /**
     * {@return {@code true} if a state stands in the way like a solid block does}: entities collide
     * with it as with a full block, or one of its faces can hold something in its middle
     *
     * @param state the state, {@code null} for a block that is not loaded
     */
    static boolean isSolid(final @Nullable BlockData state) {
        if (state == null || state.isAir() || state.isFluid()) {
            return false;
        }
        if (BlockSupport.isCollisionShapeFullBlock(state)) {
            return true;
        }
        for (final BlockFace face : ALL_FACES) {
            if (BlockSupport.isFaceSturdy(state, face, SupportType.CENTER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rolls what a block drops when it is broken, from its loot table.
     */
    static List<ItemStack> loot(final FarmingWorld world, final BlockData state, final ItemStack tool) {
        final BlockLootTables tables = BlockLootTables.instance();
        return tables == null ? List.of() : tables.drops(state, tool, world.random());
    }
}
