package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.world.BlockPos;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class ChestBlocks {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChestBlocks.class);

    private static final float SOUND_VOLUME = 0.5f;

    private static final float SOUND_PITCH = 1.0f;

    private ChestBlocks() {
        throw new UnsupportedOperationException("ChestBlocks cannot be instantiated.");
    }

    public static boolean isBlockedAbove(final ServerWorld world, final BlockPos pos) {
        try {
            final var above = world.getBlock(pos.x(), pos.y() + 1, pos.z());
            return !above.isAir() && !above.isFluid();
        } catch (final Exception exception) {
            LOGGER.error("Failed to check if block is blocked above", exception);
            return false;
        }
    }

    public static void broadcastLid(
            final FidorialServer server, final ServerWorld world, final BlockPos pos, final int viewers) {
        server.broadcastNear(
                world,
                pos.x() + 0.5,
                pos.y() + 0.5,
                pos.z() + 0.5,
                ClientboundBlockEventPacket.chestViewers(pos, viewers));
    }

    public static void broadcastSound(
            final FidorialServer server, final ServerWorld world, final BlockPos pos, final Sound.Type type) {
        final double x = pos.x() + 0.5;
        final double y = pos.y() + 0.5;
        final double z = pos.z() + 0.5;
        server.broadcastNear(world, x, y, z, new ClientboundSoundPacket(
                Sound.sound(type, Sound.Source.BLOCK, SOUND_VOLUME, SOUND_PITCH), x, y, z));
    }
}
