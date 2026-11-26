package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SoundEvent;
import net.kyori.adventure.sound.Sound;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The lids of the blocks a window is opened on, a chest, the two halves of a large chest or an
 * ender chest: they stay open while anyone looks inside, and creak when the first player opens
 * them and when the last one closes them.
 *
 * <p>The players looking into each block are counted by the server's {@link BlockViewerTracker};
 * the players near the blocks are told of every change.</p>
 */
final class BlockLid {

    private static final float VOLUME = 0.5f;

    private BlockLid() {
        throw new UnsupportedOperationException("BlockLid cannot be instantiated.");
    }

    /**
     * Counts one more player looking into each block.
     *
     * @param sound what plays, once for all the blocks, when nobody looked into them before
     */
    static void open(final FidorialServer server, final ServerWorld world, final List<BlockPosition> blocks,
                     final TypedKey<SoundEvent> sound) {
        boolean first = false;
        for (final BlockPosition block : blocks) {
            final int viewers = server.players().blockViewers().open(world.key(), block);
            lid(server, world, block, viewers);
            first |= viewers == 1;
        }
        if (first) {
            sound(server, world, blocks, sound);
        }
    }

    /**
     * Counts one player less looking into each block.
     *
     * @param sound what plays, once for all the blocks, when nobody looks into them any more
     */
    static void close(final FidorialServer server, final ServerWorld world, final List<BlockPosition> blocks,
                      final TypedKey<SoundEvent> sound) {
        boolean last = false;
        for (final BlockPosition block : blocks) {
            final int viewers = server.players().blockViewers().close(world.key(), block);
            lid(server, world, block, viewers);
            last |= viewers == 0;
        }
        if (last) {
            sound(server, world, blocks, sound);
        }
    }

    private static void lid(final FidorialServer server, final ServerWorld world, final BlockPosition block, final int viewers) {
        server.players().broadcastNear(world, block.blockX() + 0.5, block.blockY() + 0.5, block.blockZ() + 0.5,
                ClientboundBlockEventPacket.chestViewers(block, viewers), null);
    }

    /**
     * Plays a sound at the middle of the blocks, the middle of a large chest.
     */
    private static void sound(final FidorialServer server, final ServerWorld world, final List<BlockPosition> blocks,
                              final TypedKey<SoundEvent> sound) {
        double x = 0;
        double y = 0;
        double z = 0;
        for (final BlockPosition block : blocks) {
            x += block.blockX() + 0.5;
            y += block.blockY() + 0.5;
            z += block.blockZ() + 0.5;
        }
        x /= blocks.size();
        y /= blocks.size();
        z /= blocks.size();
        final float pitch = ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f;
        server.players().broadcastNear(world, x, y, z,
                new ClientboundSoundPacket(Sound.sound(sound.key(), Sound.Source.BLOCK, VOLUME, pitch), x, y, z), null);
    }
}
