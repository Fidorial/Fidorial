package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Player;

public record PlayerQuitEvent(
        @ThreadContract("get -> any; modify -> owner")
        Player player
) implements PlayerEvent {
}
