package fr.euphyllia.fidorial.server.network.session;

import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundForgetLevelChunkPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelChunkWithLightPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetChunkCacheCenterPacket;
import fr.euphyllia.fidorial.server.schedulers.ThreadedChunkWorker;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.ChunkViewSource;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

public final class ChunkViewTracker implements ChunkViewSource {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChunkViewTracker.class);

    private final ClientConnection connection;
    private final ThreadedChunkWorker chunkWorker;
    private final ServerWorld world;
    private final ChunkNetworkSerializer serializer;
    private volatile int radius;
    private volatile int forgetRadius;
    private volatile boolean closed;

    private final Object lock = new Object();
    private final LongSet sent = new LongOpenHashSet();
    private final LongSet pending = new LongOpenHashSet();

    private int centerX;
    private int centerZ;

    public ChunkViewTracker(
            final ClientConnection connection,
            final ThreadedChunkWorker chunkWorker,
            final ServerWorld world,
            final ChunkNetworkSerializer serializer,
            final int radius
    ) {
        this.connection = connection;
        this.chunkWorker = chunkWorker;
        this.world = world;
        this.serializer = serializer;
        this.radius = radius;
        this.forgetRadius = radius;
    }

    public void init(final ChunkPos center) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            centerX = center.x();
            centerZ = center.z();
        }
        connection.send(new ClientboundSetChunkCacheCenterPacket(center.x(), center.z()));
        stream(center.x(), center.z());
    }

    public void resend(final ChunkPos center) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            sent.clear();
            centerX = center.x();
            centerZ = center.z();
        }
        connection.send(new ClientboundSetChunkCacheCenterPacket(center.x(), center.z()));
        requestInRange(center.x(), center.z());
    }

    public boolean moveTo(final int chunkX, final int chunkZ) {
        synchronized (lock) {
            if (closed) {
                return false;
            }
            if (chunkX == centerX && chunkZ == centerZ) {
                return false;
            }
            centerX = chunkX;
            centerZ = chunkZ;
        }
        connection.send(new ClientboundSetChunkCacheCenterPacket(chunkX, chunkZ));
        stream(chunkX, chunkZ);
        return true;
    }

    private void stream(final int centerX, final int centerZ) {
        synchronized (lock) {
            if (closed) {
                return;
            }
        }
        forgetOutOfRange(centerX, centerZ);
        requestInRange(centerX, centerZ);
    }

    private void forgetOutOfRange(final int centerX, final int centerZ) {
        LongList toForget = null;
        synchronized (lock) {
            final Iterator<Long> it = sent.iterator();
            while (it.hasNext()) {
                final long key = it.next();
                final int cx = (int) (key >> 32);
                final int cz = (int) key;
                if (!inRange(cx, cz, centerX, centerZ, forgetRadius)) {
                    if (toForget == null) toForget = new LongArrayList();
                    toForget.add(key);
                    it.remove();
                }
            }
        }
        if (toForget != null) {
            for (final long key : toForget) {
                connection.send(new ClientboundForgetLevelChunkPacket((int) (key >> 32), (int) key));
            }
        }
    }

    private void requestInRange(final int centerX, final int centerZ) {
        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    requestChunk(centerX + dx, centerZ + dz);
                }
            }
        }
    }

    private void requestChunk(final int cx, final int cz) {
        final long key = ChunkPos.chunkKey(cx, cz);
        synchronized (lock) {
            if (sent.contains(key) || !pending.add(key)) {
                return;
            }
        }
        chunkWorker.loadAsync(world, cx, cz).whenComplete((column, error) -> onLoaded(cx, cz, column, error));
    }

    private void onLoaded(final int cx, final int cz, final ChunkColumn column, @Nullable final Throwable error) {
        final long key = ChunkPos.chunkKey(cx, cz);
        synchronized (lock) {
            pending.remove(key);

            if (closed) {
                return;
            }

            if (error != null) {
                LOGGER.error("Unable to load chunk {},{} for {}", cx, cz, connection.username(), error);
                return;
            }

            if (!inRange(cx, cz, centerX, centerZ, forgetRadius) || !sent.add(key)) {
                return;
            }
        }

        if (closed) {
            return;
        }

        final ClientboundLevelChunkWithLightPacket packet;
        synchronized (world.lightManager()) {
            packet = new ClientboundLevelChunkWithLightPacket(serializer, column, world.generator.dimensionType().hasSkylight());
        }
        connection.send(packet);
    }

    private boolean inRange(final int cx, final int cz, final int centerX, final int centerZ, final int range) {
        return Math.abs(cx - centerX) <= range && Math.abs(cz - centerZ) <= range;
    }

    public ChunkPos center() {
        synchronized (lock) {
            return new ChunkPos(centerX, centerZ);
        }
    }

    public ServerWorld world() {
        return world;
    }

    @Override
    public void collectViewedChunks(final LongSet target) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            target.addAll(sent);
            target.addAll(pending);
        }
    }

    public int sentCount() {
        synchronized (lock) {
            return sent.size();
        }
    }

    public void updateViewDistance(final int newRadius) {
        final int clamped = Math.max(2, newRadius);
        final int cx, cz;
        synchronized (lock) {
            if (closed || clamped == this.radius) {
                return;
            }
            this.radius = clamped;
            this.forgetRadius = clamped;
            cx = centerX;
            cz = centerZ;
        }
        stream(cx, cz);
    }

    public void close() {
        synchronized (lock) {
            if (closed) {
                return;
            }
            closed = true;
            pending.clear();
            sent.clear();
        }
    }
}
