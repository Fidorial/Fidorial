package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.sound.Sound;

import java.util.List;

/**
 * A fence gate, a port of the redstone part of vanilla's {@code FenceGateBlock}: a signal opens it,
 * losing it closes it.
 */
public final class PoweredFenceGate extends PoweredOpenable {

    /**
     * Every vanilla fence gate.
     */
    public static final List<PoweredFenceGate> ALL = List.of(
            new PoweredFenceGate(BlockTypeKeys.OAK_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.SPRUCE_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.BIRCH_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.JUNGLE_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.ACACIA_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.DARK_OAK_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.PALE_OAK_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.MANGROVE_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.POPLAR_FENCE_GATE, Sounds.WOOD),
            new PoweredFenceGate(BlockTypeKeys.CHERRY_FENCE_GATE, Sounds.CHERRY),
            new PoweredFenceGate(BlockTypeKeys.BAMBOO_FENCE_GATE, Sounds.BAMBOO),
            new PoweredFenceGate(BlockTypeKeys.CRIMSON_FENCE_GATE, Sounds.NETHER_WOOD),
            new PoweredFenceGate(BlockTypeKeys.WARPED_FENCE_GATE, Sounds.NETHER_WOOD));

    private final Sounds sounds;

    private PoweredFenceGate(final TypedKey<BlockType> key, final Sounds sounds) {
        super(key);
        this.sounds = sounds;
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (!(state instanceof final Openable gate) || !(state instanceof final Powerable powerable)) {
            return;
        }
        final boolean signal = world.hasNeighborSignal(position);
        if (signal == powerable.isPowered()) {
            return;
        }
        world.setBlock(position, ((Powerable) gate.withOpen(signal)).withPowered(signal), RedstoneWorld.Update.SHAPES);
        if (gate.isOpen() != signal) {
            Redstone.playSound(world.world(), position, signal ? sounds.open : sounds.close, Sound.Source.BLOCK, 1.0f, Redstone.variedPitch(), null);
            Redstone.gameEvent(world.world(), signal ? GameEventKeys.BLOCK_OPEN : GameEventKeys.BLOCK_CLOSE, position);
        }
    }

    /**
     * The sounds of a fence gate, which vanilla's {@code WoodType} sets.
     */
    @SuppressWarnings("ImmutableEnumChecker") // typed keys are immutable records
    private enum Sounds {
        WOOD(SoundEventKeys.BLOCK_FENCE_GATE_OPEN, SoundEventKeys.BLOCK_FENCE_GATE_CLOSE),
        CHERRY(SoundEventKeys.BLOCK_CHERRY_WOOD_FENCE_GATE_OPEN, SoundEventKeys.BLOCK_CHERRY_WOOD_FENCE_GATE_CLOSE),
        BAMBOO(SoundEventKeys.BLOCK_BAMBOO_WOOD_FENCE_GATE_OPEN, SoundEventKeys.BLOCK_BAMBOO_WOOD_FENCE_GATE_CLOSE),
        NETHER_WOOD(SoundEventKeys.BLOCK_NETHER_WOOD_FENCE_GATE_OPEN, SoundEventKeys.BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE);

        private final TypedKey<SoundEvent> open;
        private final TypedKey<SoundEvent> close;

        Sounds(final TypedKey<SoundEvent> open, final TypedKey<SoundEvent> close) {
            this.open = open;
            this.close = close;
        }
    }
}
