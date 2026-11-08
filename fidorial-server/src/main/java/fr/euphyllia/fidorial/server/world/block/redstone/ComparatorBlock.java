package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.block.data.type.Comparator;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;

/**
 * The comparator, a port of vanilla's {@code ComparatorBlock}: a diode reading the level of the
 * block behind it, and comparing it with, or subtracting from it, the strongest side input.
 *
 * <p>The signal it outputs is kept in its block entity, under {@code OutputSignal} so
 * that it survives a restart.</p>
 */
public final class ComparatorBlock extends DiodeBlock {

    public static final ComparatorBlock INSTANCE = new ComparatorBlock();

    private static final String OUTPUT_SIGNAL = "OutputSignal";
    private static final int DELAY = 2;

    private ComparatorBlock() {
        super(BlockTypeKeys.COMPARATOR);
    }

    private static int storedOutput(final SignalGetter world, final BlockPosition position) {
        if (!(world.world() instanceof final ServerWorld serverWorld)) {
            return 0;
        }
        final CompoundBinaryTag data = serverWorld.blockEntityData(position);
        return data == null ? 0 : data.getInt(OUTPUT_SIGNAL);
    }

    private static void storeOutput(final SignalGetter world, final BlockPosition position, final int output) {
        if (world.world() instanceof final ServerWorld serverWorld) {
            final CompoundBinaryTag data = serverWorld.blockEntityData(position);
            if (data != null) {
                serverWorld.setBlockEntityData(position, data.putInt(OUTPUT_SIGNAL, output));
            }
        }
    }

    private static Comparator.Mode mode(final BlockData state) {
        return ((Comparator) state).getMode();
    }

    @Override
    protected int delay(final BlockData state) {
        return DELAY;
    }

    @Override
    protected int outputSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        return storedOutput(world, position);
    }

    /**
     * The comparator reads the level of the block behind it, or of the block behind a
     * conductor behind it, when there is one to read.
     */
    @Override
    protected int inputSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        int signal = super.inputSignal(world, position, state);
        final BlockFace facing = facing(state);
        final BlockPosition behind = position.relative(facing);
        if (world.hasAnalogOutput(behind)) {
            signal = world.analogOutput(behind, facing);
        } else if (signal < 15 && world.isConductor(behind)) {
            final BlockPosition further = behind.relative(facing);
            if (world.hasAnalogOutput(further)) {
                signal = world.analogOutput(further, facing);
            }
        }
        return signal;
    }

    private int calculateOutputSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        final int input = inputSignal(world, position, state);
        if (input == 0) {
            return 0;
        }
        final int side = alternateSignal(world, position, state);
        if (side > input) {
            return 0;
        }
        return mode(state) == Comparator.Mode.SUBTRACT ? input - side : input;
    }

    @Override
    protected boolean shouldTurnOn(final SignalGetter world, final BlockPosition position, final BlockData state) {
        final int input = inputSignal(world, position, state);
        if (input == 0) {
            return false;
        }
        final int side = alternateSignal(world, position, state);
        return input > side || (input == side && mode(state) == Comparator.Mode.COMPARE);
    }

    @Override
    protected void checkTickOnNeighbor(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (world.willTickThisTick(position, key)) {
            return;
        }
        final int output = calculateOutputSignal(world, position, state);
        if (output != storedOutput(world, position) || isPowered(state) != shouldTurnOn(world, position, state)) {
            world.scheduleTick(position, key, DELAY, shouldPrioritize(world, position, state) ? TickPriority.HIGH : TickPriority.NORMAL);
        }
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        refreshOutputState(world, position, state);
    }

    private void refreshOutputState(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        final int output = calculateOutputSignal(world, position, state);
        final int previous = storedOutput(world, position);
        storeOutput(world, position, output);
        if (previous == output && mode(state) != Comparator.Mode.COMPARE) {
            return;
        }
        final boolean sourceOn = shouldTurnOn(world, position, state);
        final boolean on = isPowered(state);
        if (on && !sourceOn) {
            world.setBlock(position, ((Powerable) state).withPowered(false), RedstoneWorld.Update.SHAPES);
        } else if (!on && sourceOn) {
            world.setBlock(position, ((Powerable) state).withPowered(true), RedstoneWorld.Update.SHAPES);
        }
        updateNeighborsInFront(world, position, state);
    }

    /**
     * Using the comparator switches it between comparing and subtracting.
     */
    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(data instanceof final Comparator comparator) || player.gameMode() == GameMode.ADVENTURE
                || player.gameMode() == GameMode.SPECTATOR) {
            return false;
        }
        final Comparator switched = comparator.withMode(comparator.getMode() == Comparator.Mode.COMPARE
                ? Comparator.Mode.SUBTRACT
                : Comparator.Mode.COMPARE);
        final RedstoneWorld world = Redstone.world(player.world());
        // the client of the player already played the click
        Redstone.playSound(player.world(), position, SoundEventKeys.BLOCK_COMPARATOR_CLICK, Sound.Source.BLOCK, 0.3f,
                switched.getMode() == Comparator.Mode.SUBTRACT ? 0.55f : 0.5f, player);
        world.setBlock(position, switched, RedstoneWorld.Update.SHAPES);
        final BlockData current = world.blockAt(position);
        if (current != null && current.key().equals(key)) {
            refreshOutputState(world, position, current);
        }
        return true;
    }
}
