package fr.fidorial.item.component;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * The enchantments a stack carries, under {@code minecraft:enchantments}.
 *
 * @param levels the level of each enchantment, keyed by enchantment, {@code 1}-{@value #MAX_LEVEL}
 * @since 0.1.0
 */
public record ItemEnchantments(Map<Key, Integer> levels) {

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
        for (final Map.Entry<Key, Integer> level : levels.entrySet()) {
            Preconditions.checkArgument(level.getValue() != null && level.getValue() >= 1 && level.getValue() <= MAX_LEVEL,
                    "The level of %s must be between 1 and %s, got %s", level.getKey(), MAX_LEVEL, level.getValue());
        }
        levels = Map.copyOf(levels);
    }
}