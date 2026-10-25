package fr.euphyllia.fidorial.testplugin.enchantment;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.item.enchantment.EnchantedItem;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;

/**
 * Heals the attacker by 10% of the damage dealt per level.
 */
final class LifestealBehaviour implements EnchantmentBehaviour {

    private static final float SHARE_PER_LEVEL = 0.1f;

    @Override
    public void onAttack(final EnchantedItem item, final LivingEntity victim, final DamageSource source, final float damage) {
        item.owner().heal(damage * SHARE_PER_LEVEL * item.level());
    }
}
