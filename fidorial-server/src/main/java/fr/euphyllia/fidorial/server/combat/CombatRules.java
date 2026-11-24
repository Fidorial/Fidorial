package fr.euphyllia.fidorial.server.combat;

/**
 * The formulas of combat, free of any entity: what armor, Resistance and protection take away, how
 * the attack cooldown scales a hit, what a smash attack adds.
 */
public final class CombatRules {

    private static final float MAX_ARMOR = 20.0f;

    private static final float ARMOR_REDUCTION_PER_POINT = 25.0f;

    private static final float TOUGHNESS_DIVISOR = 4.0f;

    private static final float MIN_ARMOR_RATIO = 0.2f;

    private static final float MAX_PROTECTION = 20.0f;

    private static final float PROTECTION_REDUCTION_PER_POINT = 25.0f;

    private static final int RESISTANCE_PER_LEVEL = 5;

    private static final float RESISTANCE_SCALE = 25.0f;

    /**
     * The charge a hit needs to be strong: critical, sweeping or sprinting.
     */
    public static final float STRONG_HIT_THRESHOLD = 0.9f;

    /**
     * How far one must fall for a mace hit to be a smash attack.
     */
    public static final double SMASH_MIN_FALL = 1.5;

    /**
     * The fall above which a smash attack is heavy: louder, and pushing twice as hard.
     */
    public static final double HEAVY_SMASH_FALL = 5.0;

    private static final double SMASH_FIRST_BLOCKS = 3.0;
    private static final double SMASH_NEXT_BLOCKS = 8.0;
    private static final double SMASH_FIRST_DAMAGE = 4.0;
    private static final double SMASH_NEXT_DAMAGE = 2.0;
    private static final double SMASH_LAST_DAMAGE = 1.0;

    private CombatRules() {
        throw new UnsupportedOperationException("CombatRules cannot be instantiated.");
    }

    /**
     * {@return the damage left once armor took its share}: each point of armor takes 4%, but heavy
     * hits wear it down, less so the tougher it is, to no less than a fifth of it
     *
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

    /**
     * {@return the damage left once protection enchantments took their share}: 4% per point, up to
     * 20 points
     */
    public static float damageAfterProtection(final float damage, final float protection) {
        if (protection <= 0f) {
            return damage;
        }
        return damage * (1.0f - Math.clamp(protection, 0.0f, MAX_PROTECTION) / PROTECTION_REDUCTION_PER_POINT);
    }

    /**
     * {@return the share of a hit left by Resistance}: 20% less per level, nothing from level V
     *
     * @param amplifier the amplifier of the effect, its level minus one; negative for no effect
     */
    public static float resistanceMultiplier(final int amplifier) {
        if (amplifier < 0) {
            return 1.0f;
        }
        final int level = amplifier + 1;
        return Math.max(0.0f, (RESISTANCE_SCALE - level * RESISTANCE_PER_LEVEL) / RESISTANCE_SCALE);
    }

    /**
     * {@return how much of its damage a hit keeps at a given charge}: a fifth when spammed, all of it
     * when fully charged, along a curve
     */
    public static float attackDamageScale(final float strengthScale) {
        final float scale = Math.clamp(strengthScale, 0.0f, 1.0f);
        return 0.2f + scale * scale * 0.8f;
    }

    /**
     * {@return the charge of a swing}: how far into its cooldown the swing comes, half a tick
     * counted in favor of the swinger
     *
     * @param ticksSinceLastSwing the ticks since the last swing
     * @param cooldownTicks       the ticks a swing takes to recharge, from the attack speed
     */
    public static float attackStrength(final int ticksSinceLastSwing, final float cooldownTicks) {
        if (cooldownTicks <= 0f) {
            return 1.0f;
        }
        return Math.clamp((ticksSinceLastSwing + 0.5f) / cooldownTicks, 0.0f, 1.0f);
    }

    /**
     * {@return the ticks a swing takes to recharge at an attack speed, in swings per second}
     */
    public static float attackCooldown(final double attackSpeed) {
        return attackSpeed <= 0.0 ? Float.MAX_VALUE : (float) (20.0 / attackSpeed);
    }

    /**
     * {@return the damage a smash attack adds for a fall}: four per block over the first three
     * blocks, two per block over the next five, one per block beyond
     *
     * @param fallDistance how far the attacker fell
     */
    public static double smashBonus(final double fallDistance) {
        if (fallDistance <= SMASH_FIRST_BLOCKS) {
            return SMASH_FIRST_DAMAGE * fallDistance;
        }
        if (fallDistance <= SMASH_NEXT_BLOCKS) {
            return SMASH_FIRST_DAMAGE * SMASH_FIRST_BLOCKS + SMASH_NEXT_DAMAGE * (fallDistance - SMASH_FIRST_BLOCKS);
        }
        return SMASH_FIRST_DAMAGE * SMASH_FIRST_BLOCKS
                + SMASH_NEXT_DAMAGE * (SMASH_NEXT_BLOCKS - SMASH_FIRST_BLOCKS)
                + SMASH_LAST_DAMAGE * (fallDistance - SMASH_NEXT_BLOCKS);
    }

    /**
     * {@return the durability a blocked hit costs a shield}: nothing under the threshold, otherwise
     * {@code base + factor × damage}, rounded down
     */
    public static int blockingItemDamage(final ItemCombatData.ItemDamage wear, final float damage) {
        if (damage < wear.threshold()) {
            return 0;
        }
        return (int) Math.floor(wear.base() + wear.factor() * damage);
    }
}
