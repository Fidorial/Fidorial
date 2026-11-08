/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.AnaloguePowerable;
import org.jspecify.annotations.Nullable;

/**
 * A {@link Node} holding redstone dust: everything {@link WireHandler} needs to work out the power of
 * the dust, along with the network it belongs to.
 *
 * <p>Alternate Current's {@code WireNode}. Unlike the original, a node may be
 * {@linkplain #foreign foreign}: dust of a region another thread owns, which this thread reads but
 * never changes. Setting its power is {@code WireHandler}'s job, which reaches the world.</p>
 *
 * @author Space Walker
 */
final class WireNode extends Node {

    final WireConnectionManager connections;

    /**
     * {@code true} for dust of a region the current thread does not own: it gives power to the
     * network, but is neither searched nor changed, only told when the dust next to it changes.
     */
    final boolean foreign;

    /**
     * The power the dust holds in the world.
     */
    int currentPower;
    /**
     * The power the dust should hold, while the power of the network is worked out.
     */
    int virtualPower;
    /**
     * The power the dust receives from what is not dust.
     */
    int externalPower;
    /**
     * Four bits, one per horizontal {@link WireHandler.Directions} index, telling from where the dust receives
     * the power it holds.
     */
    int flowIn;
    /**
     * The direction power flows through the dust, a horizontal {@link WireHandler.Directions} index.
     */
    int iFlowDir;
    boolean added;
    boolean removed;
    boolean shouldBreak;
    boolean root;
    boolean discovered;
    boolean searched;

    /**
     * The next dust in the {@link SimpleQueue}.
     */
    @Nullable WireNode nextWire;

    WireNode(final BlockPosition pos, final BlockData state, final boolean foreign) {
        super(pos);
        this.state = state;
        this.foreign = foreign;
        this.connections = new WireConnectionManager(this);
        this.currentPower = powerOf(state);
        this.virtualPower = currentPower;
        this.priority = priority();
    }

    /**
     * {@return the power dust in that state carries}
     */
    static int powerOf(final @Nullable BlockData state) {
        return state instanceof final AnaloguePowerable dust ? dust.getPower() : 0;
    }

    /**
     * Reads the dust again, after the blocks may have changed.
     */
    void refresh(final BlockData state) {
        this.state = state;
        this.currentPower = powerOf(state);
        if (foreign) {
            // never searched, so never reset by WireHandler.discover: what it gives is what it holds
            this.virtualPower = currentPower;
        }
    }

    /**
     * {@return the priority to queue this dust with: the power it should hold}, or, for foreign
     * dust only queued for a block update, that of the dust updating it
     */
    @Override
    int priority() {
        if (foreign) {
            return super.priority();
        }
        return Math.clamp(virtualPower, WireHandler.POWER_MIN, WireHandler.POWER_MAX);
    }

    @Override
    boolean isWire() {
        return true;
    }

    /**
     * Foreign dust is told of the changes next to it, the other region working its power out;
     * dust of this network learns of them as the power spreads.
     */
    @Override
    boolean reactsToUpdates() {
        return foreign;
    }

    @Override
    WireNode asWire() {
        return this;
    }

    /**
     * Offers power to the dust, from its neighbour in the opposite of {@code iDir}.
     *
     * @param power the power offered
     * @param iDir  the {@link WireHandler.Directions} index the power flows in
     * @return {@code true} if the dust takes it, being more than it had
     */
    boolean offerPower(final int power, final int iDir) {
        if (removed || shouldBreak || foreign) {
            return false;
        }
        if (power == virtualPower) {
            flowIn |= 1 << iDir;
            return false;
        }
        if (power > virtualPower) {
            virtualPower = power;
            flowIn = 1 << iDir;
            return true;
        }
        return false;
    }
}
