package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.entity.EntityModule;
import fr.euphyllia.fidorial.server.entity.player.PlayerList;
import fr.euphyllia.fidorial.server.entity.player.PlayerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.schedulers.WorkerModule;

/**
 * The vanilla debug renderers (F3 overlays) that clients can subscribe to.
 */
public final class DebugModule implements ServerModule {

    private final ThreadedRegionRegionizer regionizer;
    private final PlayerList online;
    private final DebugSubscribers subscribers;
    private final EntityDebugSynchronizer entities;
    private final DebugGameEvents gameEvents;

    public DebugModule(final WorkerModule scheduler, final PlayerModule players, final EntityModule entities) {
        this.regionizer = scheduler.regionizer();
        this.online = players.connections();
        this.subscribers = new DebugSubscribers(online::players);
        this.entities = new EntityDebugSynchronizer(subscribers, entities.tracker());
        this.gameEvents = new DebugGameEvents(subscribers, online::players);

        online.addListener(new PlayerList.Listener() {
            @Override
            public void onConnect(final ClientConnection connection) {
                subscribers.recompute();
            }

            @Override
            public void onDisconnect(final ClientConnection connection) {
                subscribers.recompute();
            }
        });
    }

    @Override
    public void start() {
        regionizer.addTickProfiler(new DebugSampleBroadcaster(subscribers, online::players));
    }

    public DebugSubscribers subscribers() {
        return subscribers;
    }

    public EntityDebugSynchronizer entities() {
        return entities;
    }

    public DebugGameEvents gameEvents() {
        return gameEvents;
    }
}
