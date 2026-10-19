package fr.euphyllia.fidorial.server.map;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.player.Player;
import fr.fidorial.map.MapFrame;
import fr.fidorial.map.MapView;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An item frame entity that only exists on the client of its viewer, see {@link MapModule#hang}.
 */
final class ServerMapFrame implements MapFrame {

    private final MapModule module;
    private final ServerMapView map;
    private final ServerPlayer viewer;
    private final Key world;
    private final BlockPosition block;
    private final BlockFace facing;
    private final boolean invisible;
    private final int entityId;
    private final AtomicBoolean removed = new AtomicBoolean();

    ServerMapFrame(final MapModule module, final ServerMapView map, final ServerPlayer viewer, final Key world,
                   final BlockPosition block, final BlockFace facing, final boolean invisible, final int entityId) {
        this.module = module;
        this.map = map;
        this.viewer = viewer;
        this.world = world;
        this.block = block;
        this.facing = facing;
        this.invisible = invisible;
        this.entityId = entityId;
    }

    @Override
    public MapView map() {
        return map;
    }

    ServerMapView serverMap() {
        return map;
    }

    @Override
    public Player viewer() {
        return viewer;
    }

    ServerPlayer serverViewer() {
        return viewer;
    }

    @Override
    public Key world() {
        return world;
    }

    @Override
    public BlockPosition block() {
        return block;
    }

    @Override
    public BlockFace facing() {
        return facing;
    }

    @Override
    public boolean invisible() {
        return invisible;
    }

    /**
     * {@return the id of the entity on the client of the viewer}
     */
    int entityId() {
        return entityId;
    }

    @Override
    public boolean isRemoved() {
        return removed.get();
    }

    /**
     * Marks the frame as taken down.
     *
     * @return whether it was hanging until now
     */
    boolean markRemoved() {
        return removed.compareAndSet(false, true);
    }

    @Override
    public void remove() {
        module.takeDown(this, true);
    }

    @Override
    public String toString() {
        return "MapFrame[map=" + map.id() + ", viewer=" + viewer.name() + ", block=" + block.blockX() + ","
                + block.blockY() + "," + block.blockZ() + ", facing=" + facing + "]";
    }
}
