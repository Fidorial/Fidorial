package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Player;

public record PlayerJoinEvent(
        @ThreadContract(value = "any", transitive = true)
        Player player
) implements PlayerEvent {
}
