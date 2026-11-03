package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.JukeboxSong;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:jukebox_song} registry.
 */
public final class JukeboxSongKeys {
    /**
     * Key for {@code minecraft:11}.
     */
    public static final TypedKey<JukeboxSong> _11 = create("11");

    /**
     * Key for {@code minecraft:13}.
     */
    public static final TypedKey<JukeboxSong> _13 = create("13");

    /**
     * Key for {@code minecraft:5}.
     */
    public static final TypedKey<JukeboxSong> _5 = create("5");

    /**
     * Key for {@code minecraft:blocks}.
     */
    public static final TypedKey<JukeboxSong> BLOCKS = create("blocks");

    /**
     * Key for {@code minecraft:bounce}.
     */
    public static final TypedKey<JukeboxSong> BOUNCE = create("bounce");

    /**
     * Key for {@code minecraft:cat}.
     */
    public static final TypedKey<JukeboxSong> CAT = create("cat");

    /**
     * Key for {@code minecraft:chirp}.
     */
    public static final TypedKey<JukeboxSong> CHIRP = create("chirp");

    /**
     * Key for {@code minecraft:creator}.
     */
    public static final TypedKey<JukeboxSong> CREATOR = create("creator");

    /**
     * Key for {@code minecraft:creator_music_box}.
     */
    public static final TypedKey<JukeboxSong> CREATOR_MUSIC_BOX = create("creator_music_box");

    /**
     * Key for {@code minecraft:far}.
     */
    public static final TypedKey<JukeboxSong> FAR = create("far");

    /**
     * Key for {@code minecraft:flurry}.
     */
    public static final TypedKey<JukeboxSong> FLURRY = create("flurry");

    /**
     * Key for {@code minecraft:lava_chicken}.
     */
    public static final TypedKey<JukeboxSong> LAVA_CHICKEN = create("lava_chicken");

    /**
     * Key for {@code minecraft:mall}.
     */
    public static final TypedKey<JukeboxSong> MALL = create("mall");

    /**
     * Key for {@code minecraft:mellohi}.
     */
    public static final TypedKey<JukeboxSong> MELLOHI = create("mellohi");

    /**
     * Key for {@code minecraft:otherside}.
     */
    public static final TypedKey<JukeboxSong> OTHERSIDE = create("otherside");

    /**
     * Key for {@code minecraft:pigstep}.
     */
    public static final TypedKey<JukeboxSong> PIGSTEP = create("pigstep");

    /**
     * Key for {@code minecraft:precipice}.
     */
    public static final TypedKey<JukeboxSong> PRECIPICE = create("precipice");

    /**
     * Key for {@code minecraft:relic}.
     */
    public static final TypedKey<JukeboxSong> RELIC = create("relic");

    /**
     * Key for {@code minecraft:stal}.
     */
    public static final TypedKey<JukeboxSong> STAL = create("stal");

    /**
     * Key for {@code minecraft:strad}.
     */
    public static final TypedKey<JukeboxSong> STRAD = create("strad");

    /**
     * Key for {@code minecraft:tears}.
     */
    public static final TypedKey<JukeboxSong> TEARS = create("tears");

    /**
     * Key for {@code minecraft:wait}.
     */
    public static final TypedKey<JukeboxSong> WAIT = create("wait");

    /**
     * Key for {@code minecraft:ward}.
     */
    public static final TypedKey<JukeboxSong> WARD = create("ward");

    private static final List<TypedKey<JukeboxSong>> VALUES = List.of(
        _11,
        _13,
        _5,
        BLOCKS,
        BOUNCE,
        CAT,
        CHIRP,
        CREATOR,
        CREATOR_MUSIC_BOX,
        FAR,
        FLURRY,
        LAVA_CHICKEN,
        MALL,
        MELLOHI,
        OTHERSIDE,
        PIGSTEP,
        PRECIPICE,
        RELIC,
        STAL,
        STRAD,
        TEARS,
        WAIT,
        WARD
    );

    private JukeboxSongKeys() {
        throw new UnsupportedOperationException("JukeboxSongKeys cannot be instantiated.");
    }

    private static TypedKey<JukeboxSong> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.JUKEBOX_SONG, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<JukeboxSong>> values() {
        return VALUES.stream();
    }
}
