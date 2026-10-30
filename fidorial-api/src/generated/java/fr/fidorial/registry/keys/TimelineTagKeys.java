package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.Timeline;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:timeline} registry.
 */
public final class TimelineTagKeys {
    /**
     * Tag {@code #minecraft:in_end}.
     */
    public static final TagKey<Timeline> IN_END = create("in_end");

    /**
     * Tag {@code #minecraft:in_nether}.
     */
    public static final TagKey<Timeline> IN_NETHER = create("in_nether");

    /**
     * Tag {@code #minecraft:in_overworld}.
     */
    public static final TagKey<Timeline> IN_OVERWORLD = create("in_overworld");

    /**
     * Tag {@code #minecraft:universal}.
     */
    public static final TagKey<Timeline> UNIVERSAL = create("universal");

    private TimelineTagKeys() {
        throw new UnsupportedOperationException("TimelineTagKeys cannot be instantiated.");
    }

    private static TagKey<Timeline> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.TIMELINE, value);
    }
}
