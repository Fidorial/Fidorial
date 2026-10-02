package fr.fidorial.world.environment;

import net.kyori.adventure.key.Key;

/**
 * One track of the {@code minecraft:audio/background_music} environment attribute.
 *
 * @param sound               the {@code minecraft:sound_event} key
 * @param minDelay            minimum delay, in ticks, between two plays
 * @param maxDelay            maximum delay, in ticks, between two plays
 * @param replaceCurrentMusic whether this track cuts the music already playing
 * @since 0.1.0
 */
public record MusicTrack(Key sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {

    public MusicTrack {
        if (minDelay < 0 || maxDelay < minDelay) {
            throw new IllegalArgumentException("The delays of a music track must satisfy 0 <= minDelay <= maxDelay, got " + minDelay + " and " + maxDelay);
        }
    }

    /**
     * Creates a track that waits for the current music to end.
     *
     * @param sound    the sound event key
     * @param minDelay minimum delay in ticks
     * @param maxDelay maximum delay in ticks
     * @return the track
     */
    public static MusicTrack of(final Key sound, final int minDelay, final int maxDelay) {
        return new MusicTrack(sound, minDelay, maxDelay, false);
    }
}
