package fr.fidorial.registrygen.model;

import java.util.List;
import java.util.Optional;

/**
 * Catalogue of registries generated as raw protocol ID constants.
 *
 * @since 0.1.0
 */
public final class ProtocolIdRegistries {

    private static final String SERVER_DATA_PACKAGE = "fr.euphyllia.fidorial.server.registry.data";

    /**
     * {@code minecraft:command_argument_type} &rarr; {@code ArgumentTypeIds}.
     */
    public static final ProtocolIdTarget ARGUMENT_TYPE = new ProtocolIdTarget(
            "minecraft:command_argument_type",
            SERVER_DATA_PACKAGE,
            "ArgumentTypeIds",
            "_ARGUMENT_ID",
            ProtocolIdValueKind.PROTOCOL_ID,
            "Network IDs for entries in the {@code minecraft:command_argument_type} registry.\n",
            "Generated from Mojang's registry report; do not edit.");

    /**
     * {@code minecraft:block_entity_type} &rarr; {@code BlockEntityTypeIds}.
     *
     * <p>Used by the {@code level_chunk_with_light} and {@code block_entity_data}
     * packets, which both encode the block entity type as a {@code VarInt}.</p>
     */
    public static final ProtocolIdTarget BLOCK_ENTITY_TYPE = new ProtocolIdTarget(
            "minecraft:block_entity_type",
            SERVER_DATA_PACKAGE,
            "BlockEntityTypeIds",
            "_BLOCK_ENTITY_ID",
            ProtocolIdValueKind.PROTOCOL_ID,
            "Network IDs for entries in the {@code minecraft:block_entity_type} registry.\n",
            "Generated from Mojang's registry report; do not edit.");

    /**
     * {@code minecraft:entity_type} &rarr; {@code EntityTypeIds}.
     */
    public static final ProtocolIdTarget ENTITY_TYPE = new ProtocolIdTarget(
            "minecraft:entity_type",
            SERVER_DATA_PACKAGE,
            "EntityTypeIds",
            "_ENTITY_ID",
            ProtocolIdValueKind.PROTOCOL_ID,
            "Network IDs for entries in the {@code minecraft:entity_type} registry.\n",
            "Generated from Mojang's registry report; do not edit.");

    public static final List<ProtocolIdTarget> ALL = List.of(ARGUMENT_TYPE, BLOCK_ENTITY_TYPE, ENTITY_TYPE);

    private ProtocolIdRegistries() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the protocol ID target configured for the given registry, if any.
     *
     * @param registryIdentifier namespaced registry identifier
     * @return the matching target, or {@link Optional#empty()}
     */
    public static Optional<ProtocolIdTarget> byIdentifier(final String registryIdentifier) {

        return ALL.stream()
                .filter(target -> target.registryIdentifier().equals(registryIdentifier))
                .findFirst();
    }
}
