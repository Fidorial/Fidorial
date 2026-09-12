package fr.fidorial.registrygen.model;

import java.util.Map;

/**
 * Maps an entity type onto its spawn category.
 *
 * @since 0.1.0
 */
public final class EntityCategories {

    /** Category name used when nothing better can be derived. */
    public static final String DEFAULT = "MISC";

    /**
     * Prismarine's {@code type} field, mapped to the category names of
     * {@code fr.fidorial.entity.EntityType.Category}.
     */
    private static final Map<String, String> BY_PRISMARINE_TYPE = Map.of(
            "animal", "CREATURE",
            "passive", "CREATURE",
            "hostile", "MONSTER",
            "water_creature", "WATER_CREATURE",
            "ambient", "AMBIENT",
            "player", "PLAYER",
            "projectile", "MISC",
            "living", "MISC",
            "mob", "MISC",
            "other", "MISC");

    /**
     * Entity types whose vanilla {@code MobCategory} disagrees with Prismarine's
     * {@code type}, keyed by unnamespaced identifier.
     */
    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry("allay", "CREATURE"),
            Map.entry("axolotl", "WATER_CREATURE"),
            Map.entry("camel_husk", "MONSTER"),
            Map.entry("dolphin", "WATER_CREATURE"),
            Map.entry("ender_dragon", "MONSTER"),
            Map.entry("ghast", "MONSTER"),
            Map.entry("glow_squid", "WATER_CREATURE"),
            Map.entry("hoglin", "MONSTER"),
            Map.entry("magma_cube", "MONSTER"),
            Map.entry("nautilus", "WATER_CREATURE"),
            Map.entry("phantom", "MONSTER"),
            Map.entry("shulker", "MONSTER"),
            Map.entry("slime", "MONSTER"),
            Map.entry("squid", "WATER_CREATURE"),
            Map.entry("sulfur_cube", "MONSTER"),
            Map.entry("zombie_nautilus", "MONSTER"));

    private EntityCategories() {
        throw new UnsupportedOperationException();
    }

    /**
     * Resolves the category name for an entity type.
     *
     * @param name           unnamespaced entity identifier, e.g. {@code "zombie"}
     * @param prismarineType Prismarine's {@code type} field, or {@code null} when the
     *                       entity is absent from Prismarine's report
     * @return the category constant name, never {@code null}
     */
    public static String categoryOf(final String name, final String prismarineType) {

        final String override = OVERRIDES.get(name);

        if (override != null) {
            return override;
        }

        if (prismarineType == null) {
            return DEFAULT;
        }

        return BY_PRISMARINE_TYPE.getOrDefault(prismarineType, DEFAULT);
    }

    /**
     * Whether the category was derived rather than stated, i.e. worth a line in the
     * build log so that a newly added mob does not quietly spawn in the wrong bucket.
     *
     * @param name           unnamespaced entity identifier
     * @param prismarineType Prismarine's {@code type} field, or {@code null}
     * @return {@code true} when the result rests on a guess
     */
    public static boolean isDerived(final String name, final String prismarineType) {

        if (OVERRIDES.containsKey(name)) {
            return false;
        }

        return prismarineType == null
                || "mob".equals(prismarineType)
                || "living".equals(prismarineType);
    }
}
