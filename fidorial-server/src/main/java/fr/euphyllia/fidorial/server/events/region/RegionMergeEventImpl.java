package fr.euphyllia.fidorial.server.events.region;

import fr.fidorial.event.region.RegionMergeEvent;
import fr.fidorial.scheduler.Region;
import net.kyori.adventure.key.Key;

public record RegionMergeEventImpl(Key world, Region from, Region into) implements RegionMergeEvent {
}
