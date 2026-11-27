package fr.euphyllia.fidorial.server.events.mob;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.mob.MobChangeBlockEvent;
import fr.fidorial.math.BlockPosition;

/**
 * The blocks mobs change, each one once the plugins agreed through a {@link MobChangeBlockEvent}.
 */
public final class MobBlockChanges {

    private MobBlockChanges() {
    }

    /**
     * {@return {@code true} if the plugins let a mob change a block}
     *
     * @param mob      the mob changing it
     * @param position where the block lies
     * @param from     the block there now
     * @param to       the block the mob puts there
     */
    public static boolean allowed(final Mob mob, final BlockPosition position, final BlockState from, final BlockState to) {
        final FidorialServer server = FidorialServer.getInstance();
        return !server.events().post(new MobChangeBlockEventImpl(mob, position, from.data(), to.data())).isCancelled();
    }

    /**
     * Changes a block for a mob, once the plugins agreed.
     *
     * @param mob      the mob changing it
     * @param world    the world of the mob
     * @param position where the block lies
     * @param from     the block there now
     * @param to       the block the mob puts there
     * @return {@code true} if the block changed
     */
    public static boolean change(final Mob mob, final ServerWorld world, final BlockPosition position,
                                 final BlockState from, final BlockState to) {
        return allowed(mob, position, from, to)
                && FidorialServer.getInstance().worlds().blockEdits().set(world, position, to, true);
    }
}
