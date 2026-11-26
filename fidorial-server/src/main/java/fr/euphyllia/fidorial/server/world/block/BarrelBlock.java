package fr.euphyllia.fidorial.server.world.block;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.BarrelMenu;
import fr.euphyllia.fidorial.server.inventory.BlockBarrelInventory;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.BarrelInventory;
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
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.SignalGetter;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The barrel: the rows of slots the configuration of its world gives, kept in its block entity,
 * behind an opening that faces the player who placed it.
 *
 * <p>It opens whatever stands in front of it, shows {@code open=true} while anyone looks inside,
 * spills every item it holds when it is removed, and a comparator reads how full it is.</p>
 */
public final class BarrelBlock implements BlockBehaviour, RedstoneBehaviour {

    public static final BarrelBlock INSTANCE = new BarrelBlock();

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.BARREL;

    private BarrelBlock() {
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(KEY);
        Preconditions.checkState(type != null, "The block type %s is not registered", KEY);
        return type;
    }

    /**
     * {@return the slots of the barrel at a position, showing the rows the configuration of its world
     * gives}
     */
    public static BlockBarrelInventory inventory(final ServerWorld world, final BlockPosition position) {
        return new BlockBarrelInventory(world, position, world.configuration().blocks().barrel().rows());
    }

    /**
     * {@return the slots of the barrel at a position, empty when there is none, it has no block
     * entity or it is not loaded}
     */
    public static Optional<BarrelInventory> inventoryAt(final ServerWorld world, final BlockPosition position) {
        final BlockData state = ContainerBlocks.stateAt(world, position);
        if (state == null || !state.key().equals(KEY) || world.blockEntityData(position) == null) {
            return Optional.empty();
        }
        return Optional.of(inventory(world, position));
    }

    /**
     * Tells the comparators reading the barrel at a position that its contents changed.
     */
    public static void contentsChanged(final ServerWorld world, final BlockPosition position) {
        ContainerBlocks.contentsChanged(world, position, redstone -> redstone.analogOutputChanged(position));
    }

    /**
     * Opens or shuts the barrel at a position to match whether anyone looks inside, on the thread of
     * the region owning it: the window of a player may be closed from another region.
     */
    public static void lookedInto(final ServerWorld world, final BlockPosition position) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        world.scheduler().execute(world.key(), position.chunk(), () -> {
            final FidorialServer server = FidorialServer.getInstance();
            final boolean viewed = server.players().blockViewers().count(world.key(), position) > 0;
            if (ContainerBlocks.stateAt(world, position) instanceof final Openable barrel && barrel.key().equals(KEY)
                    && barrel.isOpen() != viewed) {
                server.worlds().blockEdits().set(world, position,
                        server.registries().blockStates().toBlockState(barrel.withOpen(viewed)), true);
            }
        });
    }

    /**
     * Like vanilla: the opening faces the player, up or down included, and the barrel is placed shut.
     */
    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData defaults = type().defaultData();
        if (!(defaults instanceof final Directional directional)) {
            return defaults;
        }
        final BlockData facing = directional.withFacing(context.lookingDirection().opposite());
        return facing instanceof final Openable openable ? openable.withOpen(false) : facing;
    }

    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(player instanceof final ServerPlayer serverPlayer) || !(player.world() instanceof final ServerWorld world)) {
            return false;
        }
        serverPlayer.openMenu(BarrelMenu.of(serverPlayer, world, position));
        return true;
    }

    /**
     * Spills every item the barrel holds, those of the rows it no longer shows included.
     */
    @Override
    public void onRemove(final World world, final BlockPosition position, final BlockData data) {
        if (world instanceof final ServerWorld serverWorld) {
            ContainerBlocks.spill(inventory(serverWorld, position));
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

    @Override
    public boolean hasAnalogOutput(final BlockData state) {
        return true;
    }

    /**
     * How full the rows the barrel shows are.
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
