package fr.fidorial.entity.mob;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;


/**
 * Everything the server needs to build a mob a plugin invented.
 *
 * @param key           the identifier the mob is summoned and saved under
 * @param networkType   the vanilla entity type the client renders
 * @param maxHealth     the health ceiling in half-hearts
 * @param movementSpeed the walking speed in blocks per tick
 * @param width         the hitbox width in blocks
 * @param height        the hitbox height in blocks
 * @param followRange   the radius in blocks the mob picks targets within, {@code 0} to never target
 * @param attackDamage  the damage one hit deals
 * @param soundSource   the sound category the mob's sounds are played in
 * @param behaviour     builds the brain of each mob of this kind
 * @param persistent    whether the mob is written to the region files
 * @since 0.1.0
 */
public record MobDefinition(
        Key key,
        Key networkType,
        float maxHealth,
        double movementSpeed,
        double width,
        double height,
        double followRange,
        float attackDamage,
        Sound.Source soundSource,
        MobBehaviour.Factory behaviour,
        boolean persistent) {

    public MobDefinition {
        Preconditions.checkNotNull(key, "The key of a mob definition must not be null");
        Preconditions.checkNotNull(networkType, "The network type of a mob definition must not be null");
        Preconditions.checkNotNull(soundSource, "The sound source of a mob definition must not be null");
        Preconditions.checkNotNull(behaviour, "The behaviour of a mob definition must not be null");
        if (maxHealth <= 0f) {
            throw new IllegalArgumentException("maxHealth must be positive, got " + maxHealth);
        }
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("The hitbox must be positive, got " + width + "x" + height);
        }
    }

    /**
     * @param key         the identifier the mob is summoned and saved under
     * @param networkType the vanilla entity type the client renders
     * @return a builder pre-filled with the defaults of a small walking animal
     * @since 0.1.0
     */
    public static Builder builder(final Key key, final Key networkType) {
        return new Builder(key, networkType);
    }

    /**
     * Assembles a {@link MobDefinition}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private final Key key;
        private final Key networkType;

        private float maxHealth = 10f;
        private double movementSpeed = 0.25;
        private double width = 0.6;
        private double height = 1.8;
        private double followRange = 16.0;
        private float attackDamage;
        private Sound.Source soundSource = Sound.Source.NEUTRAL;
        private MobBehaviour.Factory behaviour = mob -> new MobBehaviour() {
        };
        private boolean persistent = true;

        private Builder(final Key key, final Key networkType) {
            this.key = key;
            this.networkType = networkType;
        }

        /**
         * @param maxHealth the health ceiling in half-hearts
         * @return this builder
         * @since 0.1.0
         */
        public Builder maxHealth(final float maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        /**
         * @param movementSpeed the walking speed in blocks per tick
         * @return this builder
         * @since 0.1.0
         */
        public Builder movementSpeed(final double movementSpeed) {
            this.movementSpeed = movementSpeed;
            return this;
        }

        /**
         * @param width  the hitbox width in blocks
         * @param height the hitbox height in blocks
         * @return this builder
         * @since 0.1.0
         */
        public Builder size(final double width, final double height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * @param followRange the radius in blocks the mob picks targets within
         * @return this builder
         * @since 0.1.0
         */
        public Builder followRange(final double followRange) {
            this.followRange = followRange;
            return this;
        }

        /**
         * @param attackDamage the damage one hit deals
         * @return this builder
         * @since 0.1.0
         */
        public Builder attackDamage(final float attackDamage) {
            this.attackDamage = attackDamage;
            return this;
        }

        /**
         * @param soundSource the sound category the mob's sounds are played in
         * @return this builder
         * @since 0.1.0
         */
        public Builder soundSource(final Sound.Source soundSource) {
            this.soundSource = soundSource;
            return this;
        }

        /**
         * @param behaviour builds the brain of each mob of this kind
         * @return this builder
         * @since 0.1.0
         */
        public Builder behaviour(final MobBehaviour.Factory behaviour) {
            this.behaviour = behaviour;
            return this;
        }

        /**
         * @param persistent whether the mob is written to the region files
         * @return this builder
         * @since 0.1.0
         */
        public Builder persistent(final boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        /**
         * @return the assembled definition
         * @since 0.1.0
         */
        public MobDefinition build() {
            return new MobDefinition(
                    key,
                    networkType,
                    maxHealth,
                    movementSpeed,
                    width,
                    height,
                    followRange,
                    attackDamage,
                    soundSource,
                    behaviour,
                    persistent);
        }
    }
}
