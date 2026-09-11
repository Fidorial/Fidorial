package fr.euphyllia.fidorial.server.world.structure.worldgen;

import net.kyori.adventure.key.Key;

import java.util.List;

public record StructureSet(Key id, List<Entry> structures, StructurePlacement placement) {

    public StructureSet {
        structures = List.copyOf(structures);
    }

    public record Entry(Key structure, int weight) {
    }
}
