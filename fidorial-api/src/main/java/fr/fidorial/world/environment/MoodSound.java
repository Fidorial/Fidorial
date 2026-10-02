package fr.fidorial.world.environment;

import net.kyori.adventure.key.Key;

/**
 * Sound played when a player stays too long in the dark, the vanilla "cave ambience".
 *
 * <p>Part of the {@code minecraft:audio/ambient_sounds} environment attribute.</p>
 *
 * @param sound             the {@code minecraft:sound_event} key
 * @param tickDelay         ticks the mood value needs to fill before the sound may play
 * @param blockSearchExtent radius, in blocks, of the darkness search cube
 * @param offset            distance from the player the sound is emitted at
 * @since 0.1.0
 */
public record MoodSound(Key sound, int tickDelay, int blockSearchExtent, double offset) {

    /**
     * The vanilla overworld cave mood sound.
     */
    public static final MoodSound CAVE =
            new MoodSound(Key.key("ambient.cave"), 6000, 8, 2.0D);

    public MoodSound {
        if (tickDelay < 0) {
            throw new IllegalArgumentException("The tick delay of a mood sound must not be negative, got " + tickDelay);
        }
        if (blockSearchExtent < 0) {
            throw new IllegalArgumentException("The block search extent of a mood sound must not be negative, got " + blockSearchExtent);
        }
    }

    /**
     * Creates a mood sound.
     *
     * @param sound             the sound event key
     * @param tickDelay         ticks before the sound may play
     * @param blockSearchExtent radius of the darkness search cube
     * @param offset            emission distance
     * @return the mood sound
     */
    public static MoodSound of(final Key sound, final int tickDelay, final int blockSearchExtent, final double offset) {
        return new MoodSound(sound, tickDelay, blockSearchExtent, offset);
    }
}
