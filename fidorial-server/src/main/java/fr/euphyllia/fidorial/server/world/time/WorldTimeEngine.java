package fr.euphyllia.fidorial.server.world.time;

import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetTimePacket;
import fr.fidorial.world.time.DayNightCycle;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public final class WorldTimeEngine implements DayNightCycle {

    public static final float DEFAULT_RATE = 1.0f;

    private final Key clock;

    private long worldAge;
    private long time;
    private float fractionalTime;
    private float rate = DEFAULT_RATE;
    private boolean doDaylightCycle = true;

    private volatile @Nullable Consumer<WorldTimeEngine> broadcaster;

    public WorldTimeEngine(final Key clock) {
        this.clock = clock;
    }

    @Override
    public Key clock() {
        return clock;
    }

    public void setBroadcaster(@Nullable final Consumer<WorldTimeEngine> broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void tick() {
        tick(true);
    }

    public synchronized void tick(final boolean advanceTime) {
        worldAge++;
        if (!advanceTime || !doDaylightCycle || rate <= 0f) {
            return;
        }
        final float accumulated = fractionalTime + rate;
        final long whole = (long) Math.floor(accumulated);
        fractionalTime = (float) (accumulated - whole);
        time += whole;
    }

    @Override
    public synchronized long worldAge() {
        return worldAge;
    }

    @Override
    public synchronized long time() {
        return time;
    }

    @Override
    public void setTime(final long time) {
        synchronized (this) {
            this.time = time;
            this.fractionalTime = 0f;
        }
        broadcast();
    }

    @Override
    public void addTime(final long ticks) {
        synchronized (this) {
            this.time += ticks;
        }
        broadcast();
    }

    @Override
    public synchronized float fractionalTime() {
        return fractionalTime;
    }

    @Override
    public synchronized float rate() {
        return rate;
    }

    @Override
    public void setRate(final float rate) {
        synchronized (this) {
            this.rate = Math.max(0f, rate);
        }
        broadcast();
    }

    @Override
    public synchronized boolean doDaylightCycle() {
        return doDaylightCycle;
    }

    @Override
    public void setDoDaylightCycle(final boolean enabled) {
        synchronized (this) {
            this.doDaylightCycle = enabled;
        }
        broadcast();
    }

    public synchronized void restore(final long worldAge, final long time, final boolean doDaylightCycle) {
        this.worldAge = Math.max(0L, worldAge);
        this.time = time;
        this.fractionalTime = 0f;
        this.doDaylightCycle = doDaylightCycle;
    }

    public ClientboundSetTimePacket.Clock snapshot(final int networkId) {
        return snapshot(networkId, true);
    }

    public synchronized ClientboundSetTimePacket.Clock snapshot(final int networkId, final boolean advanceTime) {
        return new ClientboundSetTimePacket.Clock(
                networkId, time, fractionalTime, advanceTime && doDaylightCycle ? rate : 0f);
    }

    private void broadcast() {
        final Consumer<WorldTimeEngine> target = broadcaster;
        if (target != null) {
            target.accept(this);
        }
    }


}
