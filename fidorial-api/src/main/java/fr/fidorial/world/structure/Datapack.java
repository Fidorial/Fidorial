package fr.fidorial.world.structure;

import java.nio.file.Path;

/**
 * A datapack loaded from the {@code datapacks} folder of the main world.
 *
 * @param id              the identifier of the pack, e.g. {@code fidorial:my_pack}
 * @param source          the folder or zip the pack was read from
 * @param descriptionJson the raw JSON description found in {@code pack.mcmeta}
 * @param templates       the number of structure templates ({@code .nbt}) it provides
 * @param structures      the number of worldgen structures it provides
 * @param structureSets   the number of structure sets it provides
 * @since 0.1.0
 */
public record Datapack(
        String id,
        Path source,
        String descriptionJson,
        int templates,
        int structures,
        int structureSets
) {
}
