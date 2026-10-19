package fr.euphyllia.fidorial.server.map;

import fr.fidorial.entity.player.Player;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A player as maps see them: where they are, and how to send them map data.
 */
interface MapViewer {

    Player player();

    default UUID uuid() {
        return player().uuid();
    }

    Key world();

    /**
     * Whether the world of the player has a ceiling, where map arrows spin.
     */
    boolean inCeilingWorld();

    double x();

    double z();

    float yaw();

    /**
     * Sends map data.
     *
     * @param icons the icons replacing the previous ones, or {@code null} to keep them
     * @param patch the pixels to paint, or {@code null}
     */
    void send(int mapId, int scale, boolean locked, @Nullable List<MapIcon> icons, @Nullable MapPatch patch);
}
