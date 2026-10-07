package fr.euphyllia.fidorial.server.entity.effect;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.effect.MobEffectBehaviour;
import fr.fidorial.entity.effect.MobEffectDefinition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

public record MobEffectDefinitionImpl(TypedKey<MobEffect> key, Category category,
                                      Optional<TypedKey<MobEffect>> fallback, Predicate<Player> knownBy,
                                      OptionalInt requestedNetworkId, MobEffectBehaviour behaviour)
        implements MobEffectDefinition {

    private static final MobEffectBehaviour NO_BEHAVIOUR = new MobEffectBehaviour() {
    };

    @Override
    public boolean isKnownBy(final Player player) {
        return knownBy.test(player);
    }

    static final class Builder implements MobEffectDefinition.Builder {

        private final TypedKey<MobEffect> key;
        private final Predicate<TypedKey<MobEffect>> isVanilla;
        private Category category = Category.NEUTRAL;
        private @Nullable TypedKey<MobEffect> fallback;
        private Predicate<Player> knownBy = _ -> false;
        private OptionalInt networkId = OptionalInt.empty();
        private MobEffectBehaviour behaviour = NO_BEHAVIOUR;

        Builder(final TypedKey<MobEffect> key, final Predicate<TypedKey<MobEffect>> isVanilla) {
            Preconditions.checkArgument(key != null, "The key of an effect must not be null");
            this.key = key;
            this.isVanilla = isVanilla;
        }

        @Override
        public Builder category(final Category category) {
            Preconditions.checkArgument(category != null, "The category of an effect must not be null");
            this.category = category;
            return this;
        }

        @Override
        public Builder fallback(final TypedKey<MobEffect> fallback) {
            Preconditions.checkArgument(fallback != null && isVanilla.test(fallback),
                    "The fallback of %s must be a vanilla effect, got %s", key, fallback);
            this.fallback = fallback;
            return this;
        }

        @Override
        public Builder knownBy(final Predicate<Player> knownBy) {
            Preconditions.checkArgument(knownBy != null, "The knownBy predicate must not be null");
            this.knownBy = knownBy;
            return this;
        }

        @Override
        public Builder networkId(final int networkId) {
            Preconditions.checkArgument(networkId >= 0, "Invalid network ID %s", networkId);
            this.networkId = OptionalInt.of(networkId);
            return this;
        }

        @Override
        public Builder behaviour(final MobEffectBehaviour behaviour) {
            Preconditions.checkArgument(behaviour != null, "The behaviour of an effect must not be null");
            this.behaviour = behaviour;
            return this;
        }

        @Override
        public MobEffectDefinitionImpl build() {
            return new MobEffectDefinitionImpl(key, category, Optional.ofNullable(fallback), knownBy, networkId,
                    behaviour);
        }
    }
}
