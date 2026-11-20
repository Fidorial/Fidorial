package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.mob.MobChangeBlockEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;

public final class MobChangeBlockEventImpl implements MobChangeBlockEvent {

    private final Mob mob;
    private final BlockPosition position;
    private final BlockData from;
    private final BlockData to;
    private boolean cancelled;

    public MobChangeBlockEventImpl(final Mob mob, final BlockPosition position, final BlockData from, final BlockData to) {
        Preconditions.checkArgument(mob != null, "The mob must not be null");
        Preconditions.checkArgument(position != null, "The position must not be null");
        Preconditions.checkArgument(from != null && to != null, "The blocks must not be null");
        this.mob = mob;
        this.position = position;
        this.from = from;
        this.to = to;
    }

    @Override
    public Mob mob() {
        return mob;
    }

    @Override
    public BlockPosition position() {
        return position;
    }

    @Override
    public BlockData from() {
        return from;
    }

    @Override
    public BlockData to() {
        return to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
