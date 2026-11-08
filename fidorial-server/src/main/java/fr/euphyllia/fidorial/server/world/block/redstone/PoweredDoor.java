package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Bisected.Half;
import fr.fidorial.world.block.data.type.Door;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import net.kyori.adventure.sound.Sound;

import java.util.List;

/**
 * A door, a port of the redstone part of vanilla's {@code DoorBlock}: a signal reaching either half
 * opens it, losing it closes it, and each half follows the other.
 */
public final class PoweredDoor extends PoweredOpenable {

    /**
     * Every vanilla door.
     */
    public static final List<PoweredDoor> ALL = List.of(
            new PoweredDoor(BlockTypeKeys.OAK_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.SPRUCE_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.BIRCH_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.JUNGLE_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.ACACIA_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.DARK_OAK_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.PALE_OAK_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.MANGROVE_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.POPLAR_DOOR, RedstoneBlockSet.WOOD),
            new PoweredDoor(BlockTypeKeys.CHERRY_DOOR, RedstoneBlockSet.CHERRY),
            new PoweredDoor(BlockTypeKeys.BAMBOO_DOOR, RedstoneBlockSet.BAMBOO),
            new PoweredDoor(BlockTypeKeys.CRIMSON_DOOR, RedstoneBlockSet.NETHER_WOOD),
            new PoweredDoor(BlockTypeKeys.WARPED_DOOR, RedstoneBlockSet.NETHER_WOOD),
            new PoweredDoor(BlockTypeKeys.IRON_DOOR, RedstoneBlockSet.IRON),
            new PoweredDoor(BlockTypeKeys.COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.EXPOSED_COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.WEATHERED_COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.OXIDIZED_COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.WAXED_COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.WAXED_EXPOSED_COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.WAXED_WEATHERED_COPPER_DOOR, RedstoneBlockSet.COPPER),
            new PoweredDoor(BlockTypeKeys.WAXED_OXIDIZED_COPPER_DOOR, RedstoneBlockSet.COPPER));

    private final RedstoneBlockSet set;

    private PoweredDoor(final TypedKey<BlockType> key, final RedstoneBlockSet set) {
        super(key);
        this.set = set;
    }

    private static BlockFace otherHalf(final Door door) {
        return door.getHalf() == Half.LOWER ? BlockFace.UP : BlockFace.DOWN;
    }

    /**
     * A door, placed as its lower half, starts open when either half would be powered.
     */
    @Override
    protected boolean placementSignal(final SignalGetter world, final BlockPosition position) {
        return world.hasNeighborSignal(position) || world.hasNeighborSignal(position.relative(BlockFace.UP));
    }

    /**
     * A change from anything but a door of this kind, the other half included, makes
     * the door follow the signal of both its halves.
     */
    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (source.equals(key) || !(state instanceof final Door door)) {
            return;
        }
        final boolean signal = world.hasNeighborSignal(position) || world.hasNeighborSignal(position.relative(otherHalf(door)));
        if (signal == door.isPowered() || !RedstoneEvents.input(world.world(), position, state, signal)) {
            return;
        }
        if (signal != door.isOpen()) {
            Redstone.playSound(world.world(), position, set.door(signal), Sound.Source.BLOCK, 1.0f, Redstone.variedPitch(), null);
            Redstone.gameEvent(world.world(), signal ? GameEventKeys.BLOCK_OPEN : GameEventKeys.BLOCK_CLOSE, position);
        }
        world.setBlock(position, door.withPowered(signal).withOpen(signal), RedstoneWorld.Update.SHAPES);
    }

    /**
     * A half takes the state of the other half when that one changes. A half missing
     * its other half stays, where vanilla would break it.
     */
    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (state instanceof final Door door && direction == otherHalf(door)
                && neighbor instanceof final Door other && other.getHalf() != door.getHalf()) {
            return other.withHalf(door.getHalf());
        }
        return state;
    }
}
