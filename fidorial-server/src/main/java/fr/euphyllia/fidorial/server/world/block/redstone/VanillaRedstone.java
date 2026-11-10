package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.redstone.FidorialRedstoneRegistry;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.redstone.RedstoneBehaviour;

import java.util.ArrayList;
import java.util.List;

/**
 * The vanilla redstone blocks: the block behaviours of those whose placement, use or light depend on
 * redstone, and the redstone behaviours of every block taking part in it.
 *
 * <p>A block takes part in redstone only through the behaviour attached to it: stone has none.</p>
 */
public final class VanillaRedstone {

    private VanillaRedstone() {
        throw new UnsupportedOperationException("VanillaRedstone cannot be instantiated.");
    }

    /**
     * {@return the block behaviours of the vanilla redstone blocks, to register with the block
     * registry}
     */
    public static List<BlockBehaviour> blockBehaviours() {
        final List<BlockBehaviour> behaviours = new ArrayList<>();
        behaviours.add(RedstoneWireBlock.INSTANCE);
        behaviours.add(RedstoneTorchBlock.STANDING);
        behaviours.add(RedstoneTorchBlock.WALL);
        behaviours.add(RepeaterBlock.INSTANCE);
        behaviours.add(ComparatorBlock.INSTANCE);
        behaviours.add(LeverBlock.INSTANCE);
        behaviours.addAll(ButtonBlock.ALL);
        behaviours.add(ObserverBlock.INSTANCE);
        behaviours.add(RedstoneLampBlock.INSTANCE);
        behaviours.addAll(CopperBulbBlock.ALL);
        behaviours.addAll(PoweredDoor.ALL);
        behaviours.addAll(PoweredTrapdoor.ALL);
        behaviours.addAll(PoweredFenceGate.ALL);
        return List.copyOf(behaviours);
    }

    /**
     * Attaches the vanilla redstone behaviours to their blocks.
     *
     * @param registry where to attach them
     */
    public static void attach(final FidorialRedstoneRegistry registry) {
        for (final BlockBehaviour behaviour : blockBehaviours()) {
            if (behaviour instanceof final RedstoneBehaviour redstone) {
                registry.attachVanilla(behaviour.key(), redstone);
            }
        }
        registry.attachVanilla(BlockTypeKeys.REDSTONE_BLOCK, RedstoneBlock.INSTANCE);
        registry.attachVanilla(TrappedChestBlock.KEY, TrappedChestBlock.INSTANCE);
        registry.attachVanilla(PoweredNoteBlock.KEY, PoweredNoteBlock.INSTANCE);
        registry.attachVanilla(BlockTypeKeys.PISTON, PistonBaseBlock.NORMAL);
        registry.attachVanilla(BlockTypeKeys.STICKY_PISTON, PistonBaseBlock.STICKY);
        registry.attachVanilla(PistonHeadBlock.KEY, PistonHeadBlock.INSTANCE);
        registry.attachVanilla(MovingPistonBlock.KEY, MovingPistonBlock.INSTANCE);
        AnalogOutputs.vanilla().forEach(registry::attachVanilla);
    }
}
