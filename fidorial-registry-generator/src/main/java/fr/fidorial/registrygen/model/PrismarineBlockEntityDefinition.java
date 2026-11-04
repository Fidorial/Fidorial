package fr.fidorial.registrygen.model;

import java.util.List;

/**
 * One entry of PrismarineJS's {@code minecraft-data} block entities report.
 *
 * @param name   unnamespaced block entity type identifier, e.g. {@code "chest"}
 * @param blocks unnamespaced identifiers of the blocks carrying this block entity
 *
 * @since 0.1.0
 */
public record PrismarineBlockEntityDefinition(String name, List<String> blocks) {

    public PrismarineBlockEntityDefinition {
        blocks = List.copyOf(blocks);
    }
}
