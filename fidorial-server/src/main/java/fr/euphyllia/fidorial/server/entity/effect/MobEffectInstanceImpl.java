package fr.euphyllia.fidorial.server.entity.effect;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;

public record MobEffectInstanceImpl(TypedKey<MobEffect> type, int duration, int amplifier, boolean ambient,
                                    boolean visible, boolean showIcon) implements MobEffectInstance {

    private static final int DEFAULT_DURATION = 20;

    public MobEffectInstanceImpl {
        Preconditions.checkArgument(type != null, "The type of an effect must not be null");
        checkDuration(duration);
        checkAmplifier(amplifier);
    }

    public static MobEffectInstanceImpl of(final MobEffectInstance effect) {
        if (effect instanceof final MobEffectInstanceImpl impl) {
            return impl;
        }
        return new MobEffectInstanceImpl(effect.type(), effect.duration(), effect.amplifier(), effect.ambient(),
                effect.visible(), effect.showIcon());
    }

    @Override
    public boolean isInfinite() {
        return duration == INFINITE_DURATION;
    }

    public boolean hasExpired() {
        return !isInfinite() && duration == 0;
    }

    public MobEffectInstanceImpl tickDown() {
        return isInfinite() || duration == 0
                ? this
                : new MobEffectInstanceImpl(type, duration - 1, amplifier, ambient, visible, showIcon);
    }

    public MobEffectInstanceImpl merge(final MobEffectInstanceImpl incoming) {
        Preconditions.checkArgument(type.equals(incoming.type), "Cannot merge %s into %s", incoming.type, type);
        if (incoming.amplifier > amplifier) {
            return incoming;
        }
        if (incoming.amplifier == amplifier && incoming.outlasts(this)) {
            return incoming;
        }
        return this;
    }

    private boolean outlasts(final MobEffectInstanceImpl other) {
        if (other.isInfinite()) {
            return false;
        }
        return isInfinite() || duration > other.duration;
    }

    @Override
    public MobEffectInstance.Builder toBuilder() {
        return new Builder(type).duration(duration).amplifier(amplifier).ambient(ambient).visible(visible)
                .showIcon(showIcon);
    }

    private static void checkDuration(final int duration) {
        Preconditions.checkArgument(duration >= 0 || duration == INFINITE_DURATION,
                "Invalid effect duration: %s", duration);
    }

    private static void checkAmplifier(final int amplifier) {
        Preconditions.checkArgument(amplifier >= 0 && amplifier <= MAX_AMPLIFIER,
                "Invalid effect amplifier: %s", amplifier);
    }

    static final class Builder implements MobEffectInstance.Builder {

        private final TypedKey<MobEffect> type;
        private int duration = DEFAULT_DURATION;
        private int amplifier;
        private boolean ambient;
        private boolean visible = true;
        private boolean showIcon = true;

        Builder(final TypedKey<MobEffect> type) {
            Preconditions.checkArgument(type != null, "The type of an effect must not be null");
            this.type = type;
        }

        @Override
        public Builder duration(final int duration) {
            checkDuration(duration);
            this.duration = duration;
            return this;
        }

        @Override
        public Builder amplifier(final int amplifier) {
            checkAmplifier(amplifier);
            this.amplifier = amplifier;
            return this;
        }

        @Override
        public Builder ambient(final boolean ambient) {
            this.ambient = ambient;
            return this;
        }

        @Override
        public Builder visible(final boolean visible) {
            this.visible = visible;
            return this;
        }

        @Override
        public Builder showIcon(final boolean showIcon) {
            this.showIcon = showIcon;
            return this;
        }

        @Override
        public MobEffectInstanceImpl build() {
            return new MobEffectInstanceImpl(type, duration, amplifier, ambient, visible, showIcon);
        }
    }
}
