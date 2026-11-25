package fr.fidorial.world.structure;

import net.kyori.adventure.text.Component;

import java.nio.file.Path;

/**
 * A datapack: bundled with the server, or loaded from the {@code datapacks} folder of the main world.
 *
 * @param id              the identifier of the pack, e.g. {@code fidorial} or {@code file/my_pack}
 * @param source          the folder, zip or server jar the pack was read from
 * @param description     the description of its {@code pack.mcmeta}
 * @param templates       the number of structure templates ({@code .nbt}) it provides
 * @param structures      the number of worldgen structures it provides
 * @param structureSets   the number of structure sets it provides
 * @since 0.1.0
 */
public record Datapack(
        String id,
        Path source,
        Component description,
        int templates,
        int structures,
        int structureSets
) {
}
