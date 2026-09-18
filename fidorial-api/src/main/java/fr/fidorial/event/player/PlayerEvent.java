package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Event;

public interface PlayerEvent extends Event {

    @ThreadContract("get -> any")
    Player player();
}
