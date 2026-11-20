package fr.euphyllia.fidorial.server.entity.mob.monster;

import net.kyori.adventure.nbt.CompoundBinaryTag;

/**
 * Counts how long a mob has been in what turns it into another mob,  : after a time in it the mob
 * starts to convert, and it converts once it has stayed in it for the time of the conversion too.
 * Leaving it at any point cancels everything.
 */
public final class ConversionTimer {

    private static final int NOT_CONVERTING = -1;

    private final String afflictionTag;
    private final int afflictionTicks;
    private final String conversionTag;
    private final int conversionTicks;

    private int afflictionTime;
    private int conversionTime;
    private boolean converting;
    private boolean afflicted;

    /**
     * @param afflictionTag   the name vanilla saves the time spent in it under
     * @param afflictionTicks how long the mob stays in it before it starts to convert
     * @param conversionTag   the name vanilla saves the time left to convert under
     * @param conversionTicks how long the conversion takes
     */
    public ConversionTimer(final String afflictionTag, final int afflictionTicks,
                           final String conversionTag, final int conversionTicks) {
        this.afflictionTag = afflictionTag;
        this.afflictionTicks = afflictionTicks;
        this.conversionTag = conversionTag;
        this.conversionTicks = conversionTicks;
    }

    /**
     * Counts one tick.
     *
     * @param afflicted {@code true} if the mob is in what converts it
     * @return {@code true} if the mob converts now
     */
    public boolean tick(final boolean afflicted) {
        this.afflicted = afflicted;
        if (!afflicted) {
            afflictionTime = -1;
            converting = false;
            return false;
        }
        if (converting) {
            return --conversionTime < 0;
        }
        if (++afflictionTime >= afflictionTicks) {
            start(conversionTicks);
        }
        return false;
    }

    /**
     * Counts from nothing again, as if the mob had just left what converts it.
     */
    public void reset() {
        afflictionTime = -1;
        converting = false;
    }

    public boolean isConverting() {
        return converting;
    }

    /**
     * Starts the conversion, to end in a number of ticks.
     */
    void start(final int ticks) {
        conversionTime = ticks;
        converting = true;
    }

    /**
     * Sets for how long the mob has been in what converts it.
     */
    public void setAfflictionTime(final int ticks) {
        afflictionTime = ticks;
    }

    public void save(final CompoundBinaryTag.Builder tag) {
        tag.putInt(conversionTag, converting ? conversionTime : NOT_CONVERTING);
        tag.putInt(afflictionTag, afflicted ? afflictionTime : -1);
    }

    public void load(final CompoundBinaryTag tag) {
        afflictionTime = tag.getInt(afflictionTag, 0);
        final int saved = tag.getInt(conversionTag, NOT_CONVERTING);
        if (saved != NOT_CONVERTING) {
            start(saved);
        } else {
            converting = false;
        }
    }
}
