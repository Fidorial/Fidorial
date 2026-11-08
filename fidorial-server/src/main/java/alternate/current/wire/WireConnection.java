/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import org.jspecify.annotations.Nullable;

/**
 * A connection from a dust to a neighbouring dust that power can flow through, one way or both.
 *
 * <p>Alternate Current's {@code WireConnection}.</p>
 *
 * @author Space Walker
 */
final class WireConnection {

    /**
     * The connected dust.
     */
    final WireNode wire;
    /**
     * The horizontal {@link WireHandler.Directions} index from the owner of the connection to {@link #wire}.
     */
    final int iDir;
    /**
     * {@code true} if the owner gives power to {@link #wire}.
     */
    final boolean offer;
    /**
     * {@code true} if the owner takes power from {@link #wire}.
     */
    final boolean accept;

    /**
     * The next connection of the owner.
     */
    @Nullable WireConnection next;

    WireConnection(final WireNode wire, final int iDir, final boolean offer, final boolean accept) {
        this.wire = wire;
        this.iDir = iDir;
        this.offer = offer;
        this.accept = accept;
    }
}
