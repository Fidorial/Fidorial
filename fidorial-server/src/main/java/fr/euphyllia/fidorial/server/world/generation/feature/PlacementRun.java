package fr.euphyllia.fidorial.server.world.generation.feature;

import org.jspecify.annotations.Nullable;

/**
 * What the steps of a placement can look at: the region, and the feature checked against the biomes when the
 * placement belongs to a feature listed by the biomes.
 *
 * @param region     the region being decorated
 * @param biomeCheck the feature whose biomes are checked, {@code null} for a feature nested in another one
 */
public record PlacementRun(DecorationRegion region, @Nullable PlacedFeature biomeCheck) {
}
