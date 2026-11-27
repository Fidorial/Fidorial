package fr.euphyllia.fidorial.server.world.block.hopper;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.BlockHopperInventory;
import fr.euphyllia.fidorial.server.inventory.HopperMenu;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.ContainerBlocks;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.type.Hopper;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The hopper: five slots kept in its block entity, a spout pointing down or to a side, and the
 * items it moves on every game tick, as {@link Hoppers} tells.
 *
 * <p>Like vanilla, its spout points to the block it was placed against, down when it was placed
 * against the top or the bottom of a block; a redstone signal locks it ({@code enabled=false}),
 * though it still takes the items other hoppers push into it. It spills every item it holds when it
 * is removed, and a comparator reads how full it is.</p>
 */
public final class HopperBlock implements BlockBehaviour, RedstoneBehaviour {

    public static final HopperBlock INSTANCE = new HopperBlock();

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.HOPPER;

    private HopperBlock() {
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(KEY);
        Preconditions.checkState(type != null, "The block type %s is not registered", KEY);
        return type;
    }

    /**
     * {@return the slots of the hopper at a position}
     */
    public static BlockHopperInventory inventory(final ServerWorld world, final BlockPosition position) {
        return new BlockHopperInventory(world, position);
    }

    /**
     * {@return the slots of the hopper at a position, empty when there is none, it has no block
     * entity or it is not loaded}
     */
    public static Optional<HopperInventory> inventoryAt(final ServerWorld world, final BlockPosition position) {
        final BlockData state = ContainerBlocks.stateAt(world, position);
        if (state == null || !state.key().equals(KEY) || world.blockEntityData(position) == null) {
            return Optional.empty();
        }
        return Optional.of(inventory(world, position));
    }

    /**
     * Tells the comparators reading the hopper at a position that its contents changed.
     */
    public static void contentsChanged(final ServerWorld world, final BlockPosition position) {
        ContainerBlocks.contentsChanged(world, position, redstone -> redstone.analogOutputChanged(position));
    }

    /**
     * Like vanilla: the spout points to the block clicked, down when its top or its bottom was, and
     * the hopper is placed enabled; the redstone around it may lock it at once.
     */
    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData defaults = type().defaultData();
        if (!(defaults instanceof final Hopper hopper)) {
            return defaults;
        }
        final BlockFace spout = context.clickedFace().opposite();
        return hopper.withFacing(spout == BlockFace.UP || spout == BlockFace.DOWN ? BlockFace.DOWN : spout)
                .withEnabled(true);
    }

    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(player instanceof final ServerPlayer serverPlayer) || !(player.world() instanceof final ServerWorld world)) {
            return false;
        }
        serverPlayer.openMenu(HopperMenu.of(serverPlayer, world, position));
        return true;
    }

    /**
     * Spills every item the hopper holds, and forgets its cooldown.
     */
    @Override
    public void onRemove(final World world, final BlockPosition position, final BlockData data) {
        if (world instanceof final ServerWorld serverWorld) {
            ContainerBlocks.spill(inventory(serverWorld, position));
            Hoppers.INSTANCE.forget(serverWorld, position);
        }
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(KEY);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    /**
     * Locks or unlocks the hopper to match the redstone signal it receives.
     */
    private static void checkPowered(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Hopper hopper)) {
            return;
        }
        final boolean enabled = !world.hasNeighborSignal(position);
        if (enabled == hopper.isEnabled() || !RedstoneEvents.input(world.world(), position, state, !enabled)) {
            return;
        }
        world.setBlock(position, hopper.withEnabled(enabled), RedstoneWorld.Update.SHAPES);
    }

    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        if (!previous.key().equals(state.key())) {
            checkPowered(world, position, state);
        }
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        checkPowered(world, position, state);
    }

    @Override
    public boolean hasAnalogOutput(final BlockData state) {
        return true;
    }

    /**
     * How full the five slots of the hopper are.
     */
    @Override
    public int analogOutput(final SignalGetter world, final BlockPosition position, final BlockData state,
                            final BlockFace direction) {
        if (!(world.world() instanceof final ServerWorld serverWorld)) {
            return 0;
        }
        return ContainerBlocks.signal(inventory(serverWorld, position));
    }
}
