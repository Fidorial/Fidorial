package fr.euphyllia.fidorial.server.schedulers;

import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public final class RegionTickThread extends Thread {

    volatile @Nullable TickRegion currentRegion;

    RegionTickThread(final Runnable target, final String name) {
        super(target, name);
    }

    public @Nullable TickRegion currentRegion() {
        return currentRegion;
    }

    public static boolean owns(final Key world, final ChunkPos pos) {
        if (!(Thread.currentThread() instanceof final RegionTickThread self)) {
            return false;
        }
        final TickRegion region = self.currentRegion;
        return region != null && region.world().equals(world) && region.owns(pos);
    }

    public static void ensureOwned(final Key world, final ChunkPos pos) {
        if (!owns(world, pos)) {
            throw new IllegalStateException("Thread " + Thread.currentThread().getName()
                    + " does not own region for " + world.asString() + " at chunk " + pos.x() + "," + pos.z());
        }
    }
}
