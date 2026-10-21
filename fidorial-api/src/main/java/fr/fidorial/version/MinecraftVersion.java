package fr.fidorial.version;

import org.jetbrains.annotations.Contract;

/**
 * A Minecraft version, such as {@code 26.4}, {@code 26.4.1} or {@code 26.4-snapshot-5}.
 *
 * <p>Versions are ordered by number, then by stage: snapshots, pre-releases, release candidates, then the release.</p>
 *
 * @since 0.1.0
 */
public sealed interface MinecraftVersion extends Comparable<MinecraftVersion> permits MinecraftVersionImpl {

    /**
     * Creates a version.
     *
     * @param major     the first number of the version
     * @param minor     the second number of the version
     * @param patch     the third number of the version, {@code 0} if absent
     * @param stage     the development stage
     * @param iteration which snapshot, pre-release or release candidate this is, {@code 0} for a release
     * @return the version
     * @throws IllegalArgumentException if a number is negative, or the iteration does not match the stage
     * @since 0.1.0
     */
    @Contract("_, _, _, _, _ -> new")
    static MinecraftVersion of(final int major, final int minor, final int patch, final Stage stage, final int iteration) {
        return new MinecraftVersionImpl(major, minor, patch, stage, iteration);
    }

    /**
     * Parses a version id.
     *
     * @param id the version id, for instance {@code 26.2-rc-10}
     * @return the version
     * @throws IllegalArgumentException if the id is malformed
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static MinecraftVersion parse(final String id) {
        return MinecraftVersionImpl.parse(id);
    }

    /**
     * {@return the first number of the version, {@code 26} in {@code 26.4.1-rc-2}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    int major();

    /**
     * {@return the second number of the version, {@code 4} in {@code 26.4.1-rc-2}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    int minor();

    /**
     * {@return the third number of the version, {@code 1} in {@code 26.4.1-rc-2}, {@code 0} when omitted as in {@code 26.4}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    int patch();

    /**
     * {@return the development stage, {@link Stage#RELEASE_CANDIDATE} in {@code 26.4.1-rc-2}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Stage stage();

    /**
     * {@return which snapshot, pre-release or release candidate this is, {@code 2} in {@code 26.4.1-rc-2},
     * {@code 0} for a release}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    int iteration();

    /**
     * {@return the version id, for instance {@code 26.4-snapshot-5}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    String id();

    /**
     * {@return the display name, for instance {@code 26.4 Snapshot 5}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    String name();

    /**
     * A development stage, in release order.
     *
     * @since 0.1.0
     */
    enum Stage {
        /**
         * A snapshot, such as {@code 26.4-snapshot-5}.
         */
        SNAPSHOT("snapshot", "Snapshot"),
        /**
         * A pre-release, such as {@code 26.4-pre-2}.
         */
        PRE_RELEASE("pre", "Pre-Release"),
        /**
         * A release candidate, such as {@code 26.4-rc-1}.
         */
        RELEASE_CANDIDATE("rc", "Release Candidate"),
        /**
         * A release, such as {@code 26.4}.
         */
        RELEASE("", "");

        final String id;
        final String displayName;

        Stage(final String id, final String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }
}
