package fr.euphyllia.fidorial.server.events.region;

import fr.fidorial.event.region.RegionSplitEvent;
import fr.fidorial.scheduler.Region;
import net.kyori.adventure.key.Key;

import java.util.List;

public record RegionSplitEventImpl(Key world, Region from, List<Region> into) implements RegionSplitEvent {

    public RegionSplitEventImpl {
        into = List.copyOf(into);
    }
}
