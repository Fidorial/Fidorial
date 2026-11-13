package fr.fidorial.item.component;

import com.google.common.base.Preconditions;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * The effects a suspicious stew gives whoever eats it, under
 * {@code minecraft:suspicious_stew_effects}.
 *
 * @param effects the effects, in the order they are given
 * @since 0.1.0
 */
public record SuspiciousStewEffects(List<Entry> effects) {

    /**
     * How long an effect lasts when its duration is not given, in ticks.
     *
     * @since 0.1.0
     */
    public static final int DEFAULT_DURATION = 160;

    /**
     * No effect at all.
     *
     * @since 0.1.0
     */
    public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());

    /**
     * Creates the effects of a suspicious stew.
     *
     * @param effects the effects, in the order they are given
     * @since 0.1.0
     */
    public SuspiciousStewEffects {
        Preconditions.checkArgument(effects != null, "The effects of a suspicious stew must not be null");
        effects = List.copyOf(effects);
    }

    /**
     * {@return the effects of a stew that gives a single effect}
     *
     * @param effect   the effect
     * @param duration how long it lasts, in ticks
     * @since 0.1.0
     */
    public static SuspiciousStewEffects of(final TypedKey<MobEffect> effect, final int duration) {
        return new SuspiciousStewEffects(List.of(new Entry(effect, duration)));
    }

    /**
     * {@return these effects followed by one more}
     *
     * @param entry the effect to append
     * @since 0.1.0
     */
    public SuspiciousStewEffects plus(final Entry entry) {
        Preconditions.checkArgument(entry != null, "The effect passed to SuspiciousStewEffects.plus() must not be null");
        final List<Entry> copy = new ArrayList<>(effects);
        copy.add(entry);
        return new SuspiciousStewEffects(copy);
    }

    /**
     * One effect of a suspicious stew.
     *
     * @param effect   the effect
     * @param duration how long it lasts, in ticks
     * @since 0.1.0
     */
    public record Entry(TypedKey<MobEffect> effect, int duration) {

        /**
         * Creates one effect of a suspicious stew.
         *
         * @param effect   the effect
         * @param duration how long it lasts, in ticks
         * @since 0.1.0
         */
        public Entry {
            Preconditions.checkArgument(effect != null, "The effect of a suspicious stew must not be null");
        }
    }
}
