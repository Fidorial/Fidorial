package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Lightable;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import net.kyori.adventure.sound.Sound;

import java.util.List;

/**
 * A copper bulb, a port of vanilla's {@code CopperBulbBlock}: every time it starts being powered,
 * it switches its light on or off. The more oxidized the copper, the dimmer the light.
 */
public final class CopperBulbBlock implements BlockBehaviour, RedstoneBehaviour {

    /**
     * Every vanilla copper bulb, waxed or not, with the light it gives when lit.
     */
    public static final List<CopperBulbBlock> ALL = List.of(
            new CopperBulbBlock(BlockTypeKeys.COPPER_BULB, 15),
            new CopperBulbBlock(BlockTypeKeys.EXPOSED_COPPER_BULB, 12),
            new CopperBulbBlock(BlockTypeKeys.WEATHERED_COPPER_BULB, 8),
            new CopperBulbBlock(BlockTypeKeys.OXIDIZED_COPPER_BULB, 4),
            new CopperBulbBlock(BlockTypeKeys.WAXED_COPPER_BULB, 15),
            new CopperBulbBlock(BlockTypeKeys.WAXED_EXPOSED_COPPER_BULB, 12),
            new CopperBulbBlock(BlockTypeKeys.WAXED_WEATHERED_COPPER_BULB, 8),
            new CopperBulbBlock(BlockTypeKeys.WAXED_OXIDIZED_COPPER_BULB, 4));

    private final TypedKey<BlockType> key;
    private final int litEmission;

    private CopperBulbBlock(final TypedKey<BlockType> key, final int litEmission) {
        this.key = key;
        this.litEmission = litEmission;
    }

    private static boolean isLit(final BlockData state) {
        return state instanceof final Lightable bulb && bulb.isLit();
    }

    /**
     * On a rising edge of the signal, the bulb toggles its light.
     */
    private static void checkAndFlip(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Powerable bulb) || !(state instanceof final Lightable light)) {
            return;
        }
        final boolean signal = world.hasNeighborSignal(position);
        if (signal == bulb.isPowered() || !RedstoneEvents.input(world.world(), position, state, signal)) {
            return;
        }
        BlockData updated = state;
        if (!bulb.isPowered()) {
            updated = light.withLit(!light.isLit());
            Redstone.playSound(world.world(), position, light.isLit()
                    ? SoundEventKeys.BLOCK_COPPER_BULB_TURN_OFF
                    : SoundEventKeys.BLOCK_COPPER_BULB_TURN_ON, Sound.Source.BLOCK, 1.0f, 1.0f, null);
        }
        world.setBlock(position, ((Powerable) updated).withPowered(signal), RedstoneWorld.Update.NEIGHBORS);
    }

    @Override
    public TypedKey<BlockType> key() {
        return key;
    }

    @Override
    public BlockType type() {
        return Redstone.type(key);
    }

    @Override
    public int lightEmission(final BlockData data) {
        return isLit(data) ? litEmission : 0;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }

    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        if (!previous.key().equals(state.key())) {
            checkAndFlip(world, position, state);
        }
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        checkAndFlip(world, position, state);
    }

    @Override
    public boolean hasAnalogOutput(final BlockData state) {
        return true;
    }

    /**
     * {@code 15} while lit, {@code 0} otherwise.
     */
    @Override
    public int analogOutput(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return isLit(state) ? 15 : 0;
    }
}
