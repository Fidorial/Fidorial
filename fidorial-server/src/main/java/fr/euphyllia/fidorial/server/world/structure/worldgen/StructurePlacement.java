package fr.euphyllia.fidorial.server.world.structure.worldgen;

import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public sealed interface StructurePlacement {

    record RandomSpread(
            int spacing,
            int separation,
            boolean triangular,
            int salt,
            float frequency,
            @Nullable Key exclusionSet,
            int exclusionChunks
    ) implements StructurePlacement {

        public int[] potentialChunk(final long seed, final int chunkX, final int chunkZ) {
            final int cellX = Math.floorDiv(chunkX, spacing);
            final int cellZ = Math.floorDiv(chunkZ, spacing);
            return cellCandidate(seed, cellX, cellZ);
        }

        public int[] cellCandidate(final long seed, final int cellX, final int cellZ) {
            final LegacyRandom random = new LegacyRandom(0L);
            random.setLargeFeatureWithSalt(seed, cellX, cellZ, salt);
            final int range = Math.max(1, spacing - separation);
            final int offsetX = triangular ? (random.nextInt(range) + random.nextInt(range)) / 2 : random.nextInt(range);
            final int offsetZ = triangular ? (random.nextInt(range) + random.nextInt(range)) / 2 : random.nextInt(range);
            return new int[]{cellX * spacing + offsetX, cellZ * spacing + offsetZ};
        }

        public boolean passesFrequency(final long seed, final int chunkX, final int chunkZ) {
            if (frequency >= 1.0F) {
                return true;
            }
            final LegacyRandom random = new LegacyRandom(0L);
            random.setLargeFeatureWithSalt(seed, salt, chunkX, chunkZ);
            return random.nextFloat() < frequency;
        }
    }

    record Unsupported(String type) implements StructurePlacement {
    }
}
