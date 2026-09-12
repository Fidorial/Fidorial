package fr.fidorial.registrygen.model;

/**
 * Per-entity metadata sourced from PrismarineJS's {@code minecraft-data} entities report.
 *
 * <p>Mojang's registry report carries the wire ID but no hitbox, so the dimensions come
 * from here. The {@code id} field is Prismarine's own numbering and is deliberately
 * ignored: the only authority for a network ID is {@code registries.json}.</p>
 *
 * @param name        unnamespaced entity identifier, e.g. {@code "zombie"}
 * @param displayName the English name, e.g. {@code "Zombie"}
 * @param width       hitbox width in blocks
 * @param height      hitbox height in blocks
 * @param type        Prismarine's loose classification: {@code animal}, {@code hostile},
 *                    {@code water_creature}, {@code ambient}, {@code projectile},
 *                    {@code mob}, {@code passive}, {@code living}, {@code player} or
 *                    {@code other}
 *
 * @since 0.1.0
 */
public record PrismarineEntityDefinition(
        String name,
        String displayName,
        float width,
        float height,
        String type) {
}
