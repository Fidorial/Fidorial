package fr.euphyllia.fidorial.server.world.structure.worldgen;

public enum TerrainAdaptation {
    NONE,
    BURY,
    BEARD_THIN,
    BEARD_BOX,
    ENCAPSULATE;

    public static TerrainAdaptation byName(final String name) {
        return switch (name) {
            case "bury" -> BURY;
            case "beard_thin" -> BEARD_THIN;
            case "beard_box" -> BEARD_BOX;
            case "encapsulate" -> ENCAPSULATE;
            default -> NONE;
        };
    }

    public boolean beard() {
        return this == BEARD_THIN || this == BEARD_BOX;
    }
}
