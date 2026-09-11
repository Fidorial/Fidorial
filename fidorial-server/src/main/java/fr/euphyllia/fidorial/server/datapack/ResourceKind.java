package fr.euphyllia.fidorial.server.datapack;

import org.jspecify.annotations.Nullable;

public enum ResourceKind {
    TEMPLATE(".nbt", "structure/", "structures/"),
    STRUCTURE(".json", "worldgen/structure/"),
    STRUCTURE_SET(".json", "worldgen/structure_set/"),
    TEMPLATE_POOL(".json", "worldgen/template_pool/"),
    PROCESSOR_LIST(".json", "worldgen/processor_list/"),
    BIOME_TAG(".json", "tags/worldgen/biome/"),
    BLOCK_TAG(".json", "tags/block/", "tags/blocks/");

    private final String extension;
    private final String[] prefixes;

    ResourceKind(final String extension, final String... prefixes) {
        this.extension = extension;
        this.prefixes = prefixes;
    }

    public @Nullable String match(final String namespaced) {
        if (!namespaced.endsWith(extension)) {
            return null;
        }
        for (final String prefix : prefixes) {
            if (namespaced.startsWith(prefix)) {
                return namespaced.substring(prefix.length(), namespaced.length() - extension.length());
            }
        }
        return null;
    }

    public record Match(ResourceKind kind, String path) {
    }

    public static @Nullable Match classify(final String namespaced) {
        for (final ResourceKind kind : values()) {
            final String path = kind.match(namespaced);
            if (path != null && !path.isEmpty()) {
                return new Match(kind, path);
            }
        }
        return null;
    }
}
