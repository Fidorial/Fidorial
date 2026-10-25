package fr.euphyllia.fidorial.server.combat;

public final class CombatRules {

    private static final float MAX_ARMOR = 20.0f;

    private static final float ARMOR_REDUCTION_PER_POINT = 25.0f;

    private static final float TOUGHNESS_DIVISOR = 4.0f;

    private static final float MIN_ARMOR_RATIO = 0.2f;

    private static final float MAX_PROTECTION = 20.0f;

    private static final float PROTECTION_REDUCTION_PER_POINT = 25.0f;

    private CombatRules() {
    }

    /**
     * @param effectiveness changes the share of the damage the armor absorbs, as Breach lowers it
     */
    public static float damageAfterArmor(final float damage, final float armor, final float toughness,
                                         final ArmorEffectiveness effectiveness) {
        if (armor <= 0f) {
            return damage;
        }
        final float falloff = 2.0f + toughness / TOUGHNESS_DIVISOR;
        final float effective = Math.clamp(armor - damage / falloff, armor * MIN_ARMOR_RATIO, MAX_ARMOR);
        final float share = Math.clamp(effectiveness.apply(effective / ARMOR_REDUCTION_PER_POINT), 0.0f, 1.0f);
        return damage * (1.0f - share);
    }

    /**
     * Changes the share of a hit armor absorbs, from {@code 0} to {@code 1}.
     */
    @FunctionalInterface
    public interface ArmorEffectiveness {
        float apply(float share);
    }

    public static float damageAfterProtection(final float damage, final float protection) {
        if (protection <= 0f) {
            return damage;
        }
        return damage * (1.0f - Math.clamp(protection, 0.0f, MAX_PROTECTION) / PROTECTION_REDUCTION_PER_POINT);
    }

    public static float resistanceMultiplier(final int amplifier) {
        if (amplifier < 0) {
            return 1.0f;
        }
        final int level = amplifier + 1;
        return level >= 5 ? 0.0f : Math.max(0.0f, (25.0f - level * 5.0f) / 25.0f);
    }

    public static float attackDamageScale(final float strengthScale) {
        final float scale = Math.clamp(strengthScale, 0.0f, 1.0f);
        return 0.2f + scale * scale * 0.8f;
    }
}
