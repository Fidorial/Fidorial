package fr.fidorial.world.environment;

import net.kyori.adventure.key.Key;

/**
 * Sound randomly played while a player is inside the biome.
 *
 * @param sound      the {@code minecraft:sound_event} key
 * @param tickChance chance, per tick, that the sound plays; must be in {@code [0, 1]}
 * @since 0.1.0
 */
public record AdditionsSound(Key sound, float tickChance) {

    public AdditionsSound {
        if (tickChance < 0F || tickChance > 1F) {
            throw new IllegalArgumentException("The tick chance of an additions sound must be within [0, 1], got " + tickChance);
        }
    }

    /**
     * Creates an additions sound.
     *
     * @param sound      the sound event key
     * @param tickChance chance per tick
     * @return the additions sound
     */
    public static AdditionsSound of(final Key sound, final float tickChance) {
        return new AdditionsSound(sound, tickChance);
    }
}
