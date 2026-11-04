package fr.fidorial.item.component;

import com.google.common.base.Preconditions;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.registry.TypedKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The enchantments a stack carries, under {@code minecraft:enchantments}, or stores, under
 * {@code minecraft:stored_enchantments}.
 *
 * @param levels the level of each enchantment, keyed by enchantment, {@code 1}-{@value #MAX_LEVEL}
 * @see fr.fidorial.item.enchantment.Enchantments#enchant
 * @since 0.1.0
 */
public record ItemEnchantments(Map<TypedKey<EnchantmentDefinition>, Integer> levels) {

    /**
     * The highest level an enchantment may have on a stack.
     */
    public static final int MAX_LEVEL = 255;

    /**
     * No enchantment at all.
     */
    public static final ItemEnchantments EMPTY = new ItemEnchantments(Map.of());

    public ItemEnchantments {
        Preconditions.checkArgument(levels != null, "The levels of item enchantments must not be null");
        for (final Map.Entry<TypedKey<EnchantmentDefinition>, Integer> level : levels.entrySet()) {
            Preconditions.checkArgument(level.getKey() != null, "An enchantment of item enchantments must not be null");
            Preconditions.checkArgument(level.getValue() != null && level.getValue() >= 1 && level.getValue() <= MAX_LEVEL,
                    "The level of %s must be between 1 and %s, got %s", level.getKey(), MAX_LEVEL, level.getValue());
        }
        // Keeps the insertion order, used by the client for enchantments outside minecraft:tooltip_order.
        levels = Collections.unmodifiableMap(new LinkedHashMap<>(levels));
    }

    /**
     * {@return the level of {@code enchantment}, {@code 0} when absent}
     *
     * @param enchantment the enchantment
     * @since 0.1.0
     */
    public int level(final TypedKey<EnchantmentDefinition> enchantment) {
        final Integer level = levels.get(enchantment);
        return level == null ? 0 : level;
    }

    /**
     * {@return whether {@code enchantment} is present}
     *
     * @param enchantment the enchantment
     * @since 0.1.0
     */
    public boolean has(final TypedKey<EnchantmentDefinition> enchantment) {
        return levels.containsKey(enchantment);
    }

    /**
     * {@return whether no enchantment is present}
     *
     * @since 0.1.0
     */
    public boolean isEmpty() {
        return levels.isEmpty();
    }

    /**
     * {@return a copy with {@code enchantment} set to {@code level}}
     *
     * @param enchantment the enchantment
     * @param level       the level, from {@code 1} to {@value #MAX_LEVEL}
     * @throws IllegalArgumentException if the level is out of range
     * @since 0.1.0
     */
    public ItemEnchantments with(final TypedKey<EnchantmentDefinition> enchantment, final int level) {
        Preconditions.checkArgument(enchantment != null, "The enchantment must not be null");
        final Map<TypedKey<EnchantmentDefinition>, Integer> next = new LinkedHashMap<>(levels);
        next.put(enchantment, level);
        return new ItemEnchantments(next);
    }

    /**
     * {@return a copy without {@code enchantment}, or this instance if it is absent}
     *
     * @param enchantment the enchantment
     * @since 0.1.0
     */
    public ItemEnchantments without(final TypedKey<EnchantmentDefinition> enchantment) {
        if (!levels.containsKey(enchantment)) {
            return this;
        }
        final Map<TypedKey<EnchantmentDefinition>, Integer> next = new LinkedHashMap<>(levels);
        next.remove(enchantment);
        return next.isEmpty() ? EMPTY : new ItemEnchantments(next);
    }
}
