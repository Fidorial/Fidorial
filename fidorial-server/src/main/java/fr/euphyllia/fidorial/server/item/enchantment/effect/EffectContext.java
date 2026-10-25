package fr.euphyllia.fidorial.server.item.enchantment.effect;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.random.RandomGenerator;

/**
 * The context an enchantment effect is evaluated in.
 *
 * @param level  the level of the enchantment
 * @param self   the entity {@code this} refers to
 * @param source the hit, {@code null} outside of a hit
 * @param tool   the enchanted stack
 * @param random the random source
 */
public record EffectContext(
        int level,
        Entity self,
        @Nullable DamageSource source,
        ItemStack tool,
        RandomGenerator random
) {

    /**
     * {@return the entity credited with the hit, {@code attacker} in vanilla data}
     */
    public @Nullable Entity attacker() {
        return source == null ? null : source.causingEntity();
    }

    /**
     * {@return the entity that touched the victim, {@code direct_attacker} in vanilla data}
     */
    public @Nullable Entity directAttacker() {
        return source == null ? null : source.directEntity();
    }
}
