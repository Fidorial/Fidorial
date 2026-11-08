package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A trapdoor, a port of the redstone part of vanilla's {@code TrapDoorBlock}: a signal opens it,
 * losing it closes it.
 */
public final class PoweredTrapdoor extends PoweredOpenable {

    /**
     * Every vanilla trapdoor.
     */
    public static final List<PoweredTrapdoor> ALL = List.of(
            new PoweredTrapdoor(BlockTypeKeys.OAK_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.SPRUCE_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.BIRCH_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.JUNGLE_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.ACACIA_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.DARK_OAK_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.PALE_OAK_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.MANGROVE_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.POPLAR_TRAPDOOR, RedstoneBlockSet.WOOD),
            new PoweredTrapdoor(BlockTypeKeys.CHERRY_TRAPDOOR, RedstoneBlockSet.CHERRY),
            new PoweredTrapdoor(BlockTypeKeys.BAMBOO_TRAPDOOR, RedstoneBlockSet.BAMBOO),
            new PoweredTrapdoor(BlockTypeKeys.CRIMSON_TRAPDOOR, RedstoneBlockSet.NETHER_WOOD),
            new PoweredTrapdoor(BlockTypeKeys.WARPED_TRAPDOOR, RedstoneBlockSet.NETHER_WOOD),
            new PoweredTrapdoor(BlockTypeKeys.IRON_TRAPDOOR, RedstoneBlockSet.IRON),
            new PoweredTrapdoor(BlockTypeKeys.COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.EXPOSED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.WEATHERED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.OXIDIZED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.WAXED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.WAXED_EXPOSED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.WAXED_WEATHERED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER),
            new PoweredTrapdoor(BlockTypeKeys.WAXED_OXIDIZED_COPPER_TRAPDOOR, RedstoneBlockSet.COPPER));

    private static final Map<TypedKey<BlockType>, PoweredTrapdoor> BY_KEY = ALL.stream()
            .collect(Collectors.toUnmodifiableMap(PoweredTrapdoor::key, Function.identity()));

    private final RedstoneBlockSet set;

    private PoweredTrapdoor(final TypedKey<BlockType> key, final RedstoneBlockSet set) {
        super(key);
        this.set = set;
    }

    /**
     * {@return {@code true} if the state is a trapdoor, which dust climbs onto from below when it
     * is open}
     *
     * @param state the state, {@code null} for an unloaded block
     */
    public static boolean isTrapdoor(final @Nullable BlockData state) {
        return state != null && BY_KEY.containsKey(state.key());
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (!(state instanceof final Openable trapdoor) || !(state instanceof final Powerable powerable)) {
            return;
        }
        final boolean signal = world.hasNeighborSignal(position);
        if (signal == powerable.isPowered()) {
            return;
        }
        BlockData updated = state;
        if (trapdoor.isOpen() != signal) {
            updated = trapdoor.withOpen(signal);
            Redstone.playSound(world.world(), position, set.trapdoor(signal), Sound.Source.BLOCK, 1.0f, Redstone.variedPitch(), null);
            Redstone.gameEvent(world.world(), signal ? GameEventKeys.BLOCK_OPEN : GameEventKeys.BLOCK_CLOSE, position);
        }
        world.setBlock(position, ((Powerable) updated).withPowered(signal), RedstoneWorld.Update.SHAPES);
    }
}
