package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;

import java.util.HashMap;
import java.util.Map;

/**
 * Every kind of feature the decorations know, by type name without namespace.
 */
public final class FeatureKinds {

    private FeatureKinds() {
    }

    public static Map<String, FeatureLibrary.Parser> parsers() {
        final Map<String, FeatureLibrary.Parser> parsers = new HashMap<>();
        BasicKinds.register(parsers);
        OreKinds.register(parsers);
        TerrainKinds.register(parsers);
        WaterKinds.register(parsers);
        PlantKinds.register(parsers);
        SpeleothemKinds.register(parsers);
        CaveKinds.register(parsers);
        Trees.register(parsers);
        LogKinds.register(parsers);
        TemplateKinds.register(parsers);
        SculkKinds.register(parsers);
        CrevasseKinds.register(parsers);
        NetherKinds.register(parsers);
        return parsers;
    }
}
