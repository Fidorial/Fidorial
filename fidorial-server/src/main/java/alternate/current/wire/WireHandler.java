/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import fr.euphyllia.fidorial.server.world.block.redstone.RedstoneWireBlock;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.euphyllia.fidorial.server.world.redstone.ServerRedstoneWorld;
import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.AnaloguePowerable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

/**
 * This class handles power changes for redstone wire.
 *
 * <p>Alternate Current's {@code WireHandler}, ported from Minecraft's code to Fidorial's redstone
 * engine. The algorithm was designed with the following goals in mind:</p>
 * <ol>
 *     <li>Minimize the number of times a wire checks its surroundings to determine its power level.</li>
 *     <li>Minimize the number of block and shape updates emitted.</li>
 *     <li>Emit block and shape updates in a deterministic, non-locational order, fixing bug MC-11193.</li>
 * </ol>
 *
 * <p>In Vanilla redstone wire is laggy because it fails on points 1 and 2. Redstone wire updates
 * recursively and each wire calculates its power level in isolation rather than in the context of
 * the network it is a part of, so a wire in a grid can change its power level over half a dozen
 * times before settling, and each change emits 42 block updates and up to 22 shape updates, many of
 * them redundant. Here, the network is searched for the wires receiving power from outside it, the
 * roots, the network is depowered, then power spreads from the roots: each wire checks for power from
 * non-wire components at most once, sets its power level in the world once, and only updates its
 * neighbors once settled, leaving the wires of the network alone since their power was worked out
 * with it.</p>
 *
 * <p>The update order follows the direction of power flow: if the only power a wire receives comes
 * from a wire to its west, the direction of power flow through it is east, and its neighbors are
 * updated relative to that direction, in the configured {@link UpdateOrder}. This part of the
 * algorithm was inspired by theosib's {@code RedstoneWireTurbo}. Ambiguous cases keep a directional
 * element rather than introducing randomness.</p>
 *
 * <p>What changes from the original, which drives a single-threaded server:</p>
 * <ul>
 *     <li>One handler per world and per thread, since Fidorial ticks regions in parallel.</li>
 *     <li>A wire of a region the current thread does not own is {@linkplain WireNode#foreign foreign}:
 *     it gives power to the network but is never changed; it receives a block update, handed over to
 *     its region, which works its own part of the network out.</li>
 *     <li>Block updates only go to the blocks taking part in redstone, the only ones reacting to them.</li>
 *     <li>A {@link RedstoneDustPowerEvent} is fired for each wire before it
 *     transmits its power.</li>
 * </ul>
 *
 * @author Space Walker
 */
public final class WireHandler {

    /**
     * The six directions, as Alternate Current numbers them.
     *
     * <p>The cardinal directions are ordered clockwise. This allows for conversion between relative
     * and absolute directions ('left' 'right' vs 'east' 'west') with simple arithmetic: if some
     * direction index {@code iDir} is considered 'forward', then {@code (iDir + 1) & 0b11} is 'right',
     * {@code (iDir + 2) & 0b11} is 'backward', etc.</p>
     */
    static final class Directions {

        static final BlockFace[] ALL = {BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP};
        static final BlockFace[] HORIZONTAL = {BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH};

        // Indices for the arrays above.
        static final int WEST = 0b000; // 0
        static final int NORTH = 0b001; // 1
        static final int EAST = 0b010; // 2
        static final int SOUTH = 0b011; // 3
        static final int DOWN = 0b100; // 4
        static final int UP = 0b101; // 5

        private Directions() {
            throw new UnsupportedOperationException("Directions cannot be instantiated.");
        }

        static int iOpposite(final int iDir) {
            return iDir ^ (0b10 >>> (iDir >>> 2));
        }
    }

    /**
     * How the handler reaches the neighbor of a node, reading the world only for those it did not
     * look up yet.
     */
    @FunctionalInterface
    interface NodeProvider {

        /**
         * {@return the neighbor of a node in a direction}
         *
         * @param node a node
         * @param iDir the {@link Directions} index of the direction
         */
        Node getNeighbor(Node node, int iDir);
    }

    static final int POWER_MIN = 0;
    static final int POWER_MAX = 15;
    private static final int POWER_STEP = 1;

    /**
     * The direction of flow out of a dust, from the four bits telling which sides power flows in
     * from: a single direction goes through; of two directions at a right angle, the one a quarter
     * turn clockwise from the other wins; of three, the two opposite ones cancel out. Anything else
     * is ambiguous, {@code -1}.
     */
    static final int[] FLOW_IN_TO_FLOW_OUT = {
            -1, // 0b0000: -                     -> x
            Directions.WEST, // 0b0001: west                  -> west
            Directions.NORTH, // 0b0010: north                 -> north
            Directions.NORTH, // 0b0011: west/north            -> north
            Directions.EAST, // 0b0100: east                  -> east
            -1, // 0b0101: west/east             -> x
            Directions.EAST, // 0b0110: north/east            -> east
            Directions.NORTH, // 0b0111: west/north/east       -> north
            Directions.SOUTH, // 0b1000: south                 -> south
            Directions.WEST, // 0b1001: west/south            -> west
            -1, // 0b1010: north/south           -> x
            Directions.WEST, // 0b1011: west/north/south      -> west
            Directions.SOUTH, // 0b1100: east/south            -> south
            Directions.SOUTH, // 0b1101: west/east/south       -> south
            Directions.EAST, // 0b1110: north/east/south      -> east
            -1, // 0b1111: west/north/east/south -> x
    };

    /**
     * The order of shape updates, vanilla's.
     */
    private static final int[] SHAPE_UPDATE_ORDER = {
            Directions.WEST, Directions.EAST, Directions.NORTH, Directions.SOUTH, Directions.DOWN, Directions.UP
    };

    private static final BlockPosition UNSET = Position.block(0, 0, 0);
    private static final int INITIAL_NODE_CACHE = 16;

    private final ServerRedstoneWorld world;
    private final Config config;

    /**
     * The blocks of the network and around it, read once per update, by {@link #key(BlockPosition)}.
     */
    private final Long2ObjectOpenHashMap<Node> nodes = new Long2ObjectOpenHashMap<>();
    /**
     * The breadth-first search through the network.
     */
    private final SimpleQueue search = new SimpleQueue();
    /**
     * The power changes and block updates to run.
     */
    private final PriorityQueue updates = new PriorityQueue();
    private final NodeProvider neighbors = this::getNeighbor;
    private final UpdateOrder.Visitor queueNeighbor = this::queueNeighbor;

    /**
     * The nodes for blocks that are not dust, reused from one update to the next.
     */
    private Node[] nodeCache = new Node[INITIAL_NODE_CACHE];
    private int nodeCount;

    /**
     * {@code true} while working through {@link #updates}: power changes caused meanwhile join them.
     */
    private boolean updating;
    /**
     * How deep calls into the handler are nested, through the block updates it emits.
     */
    private int depth;
    private UpdateOrder updateOrder = UpdateOrder.HORIZONTAL_FIRST_OUTWARD;

    /**
     * Creates a handler for a world. A handler is only ever used by one thread at a time.
     *
     * @param world  the redstone of the world
     * @param config the settings of Alternate Current for that world
     */
    public WireHandler(final ServerRedstoneWorld world, final Config config) {
        this.world = world;
        this.config = config;
        fillNodeCache(0, nodeCache.length);
    }

    private static long key(final BlockPosition pos) {
        return ((long) (pos.blockX() & 0x3FFFFFF) << 38) | ((long) (pos.blockZ() & 0x3FFFFFF) << 12) | (pos.blockY() & 0xFFFL);
    }

    private static boolean isWire(final @Nullable BlockData state) {
        return state != null && state.key().equals(BlockTypeKeys.REDSTONE_WIRE);
    }

    /**
     * Called when a dust receives a block update, in place of vanilla's handling.
     *
     * @param pos   where the dust is
     * @param state the state of the dust
     * @return {@code false} if no dust is there, which vanilla should handle
     */
    public boolean onWireUpdated(final BlockPosition pos, final BlockData state) {
        final boolean outermost = enter();
        try {
            final Node node = getOrAddNode(pos, state);
            if (!node.isWire()) {
                return false;
            }
            final WireNode wire = node.asWire();
            invalidate();
            revalidateNode(wire);
            findRoots(wire);
            tryUpdate();
            return true;
        } catch (final RuntimeException | Error failure) {
            fail(outermost);
            throw failure;
        } finally {
            exit();
        }
    }

    /**
     * Called when a dust is placed.
     *
     * @param pos   where the dust is
     * @param state the state of the dust
     */
    public void onWireAdded(final BlockPosition pos, final BlockData state) {
        final boolean outermost = enter();
        try {
            final Node node = getOrAddNode(pos, state);
            if (!node.isWire()) {
                return;
            }
            final WireNode wire = node.asWire();
            wire.added = true;
            invalidate();
            revalidateNode(wire);
            findRoot(wire, -1);
            tryUpdate();
        } catch (final RuntimeException | Error failure) {
            fail(outermost);
            throw failure;
        } finally {
            exit();
        }
    }

    /**
     * Called when a dust is removed.
     *
     * @param pos   where the dust was
     * @param state the state of the dust that was removed
     */
    public void onWireRemoved(final BlockPosition pos, final BlockData state) {
        final boolean outermost = enter();
        try {
            final Node node = nodes.remove(key(pos));
            final WireNode wire = node == null || !node.isWire() ? new WireNode(pos, state, false) : node.asWire();
            wire.invalid = true;
            wire.removed = true;
            // removed as part of the power changes under way, which already account for it
            if (updating && wire.shouldBreak) {
                return;
            }
            invalidate();
            revalidateNode(wire);
            findRoot(wire, -1);
            tryUpdate();
        } catch (final RuntimeException | Error failure) {
            fail(outermost);
            throw failure;
        } finally {
            exit();
        }
    }

    /**
     * {@return the settings of Alternate Current this handler follows}
     */
    public Config getConfig() {
        return config;
    }

    /**
     * {@return the redstone of the world this handler works on}
     */
    public ServerRedstoneWorld world() {
        return world;
    }

    /**
     * {@return {@code true} while the handler is updating a network}, its call stack going through the
     * block updates it emits
     */
    public boolean busy() {
        return depth > 0;
    }

    /**
     * {@return {@code true} for the outermost call}, which reads the update order of the world
     */
    private boolean enter() {
        if (depth++ > 0) {
            return false;
        }
        updateOrder = config.getUpdateOrder();
        return true;
    }

    /**
     * Leaves a call; the outermost one forgets the nodes, which only describe the world while it
     * runs, even when it returned early.
     */
    private void exit() {
        if (--depth == 0 && !updating) {
            nodes.clear();
            nodeCount = 0;
        }
    }

    /**
     * Forgets everything after a failure, so that the next update starts afresh rather than from a
     * half-updated network.
     */
    private void fail(final boolean outermost) {
        if (!outermost) {
            return;
        }
        updating = false;
        search.clear();
        updates.clear();
        nodes.clear();
        nodeCount = 0;
    }

    private Node getOrAddNode(final BlockPosition pos, final @Nullable BlockData knownState) {
        final long key = key(pos);
        final Node node = nodes.get(key);
        if (node == null) {
            final Node created = getNextNode(pos, knownState != null ? knownState : world.blockAt(pos));
            nodes.put(key, created);
            return created;
        }
        if (node.invalid) {
            final Node revalidated = revalidateNode(node);
            if (revalidated != node) {
                nodes.put(key, revalidated);
            }
            return revalidated;
        }
        return node;
    }

    /**
     * {@return a node for a block}: a new {@link WireNode} for dust, a reused node otherwise
     */
    private Node getNextNode(final BlockPosition pos, final @Nullable BlockData state) {
        if (state != null && isWire(state)) {
            return new WireNode(pos, state, !world.ownsBlock(pos));
        }
        if (nodeCount == nodeCache.length) {
            final Node[] grown = new Node[nodeCache.length << 1];
            System.arraycopy(nodeCache, 0, grown, 0, nodeCache.length);
            nodeCache = grown;
            fillNodeCache(nodeCount, grown.length);
        }
        return nodeCache[nodeCount++].set(pos, state, world.behaviourOf(state), true);
    }

    private void fillNodeCache(final int start, final int end) {
        for (int index = start; index < end; index++) {
            nodeCache[index] = new Node(UNSET);
        }
    }

    /**
     * Reads the block of an invalid node again. A node that became dust, or stopped being dust,
     * needs a new node.
     */
    private Node revalidateNode(final Node node) {
        if (!node.invalid) {
            return node;
        }
        final BlockPosition pos = node.pos;
        final BlockData state = world.blockAt(pos);
        final boolean wasWire = node.isWire();
        final boolean nowWire = isWire(state);
        if (wasWire != nowWire) {
            return getNextNode(pos, state);
        }
        node.invalid = false;
        if (nowWire && state != null) {
            final WireNode wire = node.asWire();
            wire.refresh(state);
            wire.root = false;
            wire.discovered = false;
            wire.searched = false;
        } else {
            node.set(pos, state, world.behaviourOf(state), false);
        }
        return node;
    }

    /**
     * {@return the neighbour of a node on one side}, linking the two so that the next lookup is
     * immediate
     */
    private Node getNeighbor(final Node node, final int iDir) {
        final Node known = node.neighbors[iDir];
        if (known != null && !known.invalid) {
            return known;
        }
        final Node neighbor = getOrAddNode(node.pos.relative(Directions.ALL[iDir]), null);
        if (neighbor != known) {
            node.neighbors[iDir] = neighbor;
            neighbor.neighbors[Directions.iOpposite(iDir)] = node;
        }
        return neighbor;
    }

    /**
     * Marks every node invalid when the network changes in the middle of an update: the block and
     * shape updates it emitted may have changed blocks, which must be read again.
     */
    private void invalidate() {
        if (updating && !nodes.isEmpty()) {
            for (final Node node : nodes.values()) {
                node.invalid = true;
            }
        }
    }

    /**
     * Looks for dust needing a power change at and around a dust: the roots, since power only
     * spreads from those. Checking around helps when a network is powered from several points at
     * once, such as a lever with dust on its four sides, the whole network then switching at once.
     */
    private void findRoots(final WireNode wire) {
        findRoot(wire, -1);
        if (!wire.searched) {
            return;
        }
        for (final int iDir : updateOrder.directNeighbors(wire.iFlowDir)) {
            findRootsAround(wire, iDir);
        }
    }

    /**
     * Looks for dust needing a power change around the neighbour of a dust on one side.
     */
    private void findRootsAround(final WireNode wire, final int iDir) {
        final Node node = getNeighbor(wire, iDir);
        if (!node.isConductor() && !node.isSignalSource()) {
            return;
        }
        for (final int iSide : updateOrder.cardinalNeighbors(wire.iFlowDir)) {
            final Node neighbor = getNeighbor(node, iSide);
            if (neighbor.isWire() && !neighbor.asWire().foreign) {
                findRoot(neighbor.asWire(), iSide);
            }
        }
    }

    /**
     * Queues a dust for the search as a root, if it needs a power change.
     */
    private void findRoot(final WireNode wire, final int iDiscoveryDir) {
        if (wire.discovered) {
            return;
        }
        discover(wire);
        findExternalPower(wire);
        findPower(wire, false);
        if (needsUpdate(wire)) {
            searchRoot(wire, iDiscoveryDir);
        }
    }

    /**
     * Readies a dust for the search: whether it should break, its connections, and its power reset.
     * A dust that should break is not broken right away: it holds no power from now on, and breaks
     * when the network is powered.
     */
    private void discover(final WireNode wire) {
        if (wire.discovered) {
            return;
        }
        wire.discovered = true;
        wire.searched = false;
        if (!wire.removed && !wire.shouldBreak && !RedstoneWireBlock.canSurvive(world, wire.pos)) {
            wire.shouldBreak = true;
        }
        wire.virtualPower = wire.currentPower;
        wire.externalPower = POWER_MIN - 1;
        wire.connections.set(neighbors);
    }

    /**
     * Works out the power a dust receives from the blocks around it. The power of what is not dust
     * is only needed when the power from other dust dropped, to know how low the dust may fall.
     */
    private void findPower(final WireNode wire, final boolean ignoreSearched) {
        wire.virtualPower = wire.externalPower;
        wire.flowIn = 0;
        // dust that is removed or about to break gives nothing to anyone
        if (wire.removed || wire.shouldBreak) {
            return;
        }
        // dust never gives more than POWER_MAX - POWER_STEP to other dust
        if (wire.externalPower < POWER_MAX - POWER_STEP) {
            findWirePower(wire, ignoreSearched);
        }
    }

    private void findWirePower(final WireNode wire, final boolean ignoreSearched) {
        for (WireConnection connection = wire.connections.first(); connection != null; connection = connection.next) {
            if (!connection.accept) {
                continue;
            }
            final WireNode neighbor = connection.wire;
            if (!ignoreSearched || !neighbor.searched) {
                final int power = Math.max(POWER_MIN, neighbor.virtualPower - POWER_STEP);
                wire.offerPower(power, Directions.iOpposite(connection.iDir));
            }
        }
    }

    private void findExternalPower(final WireNode wire) {
        // once is enough, and never for dust that is removed or about to break
        if (wire.removed || wire.shouldBreak || wire.externalPower >= POWER_MIN) {
            return;
        }
        wire.externalPower = getExternalPower(wire);
        if (wire.externalPower > wire.virtualPower) {
            wire.virtualPower = wire.externalPower;
        }
    }

    /**
     * {@return the signal a dust receives from what is not dust}
     */
    private int getExternalPower(final WireNode wire) {
        int power = POWER_MIN;
        for (int iDir = 0; iDir < Directions.ALL.length; iDir++) {
            final Node neighbor = getNeighbor(wire, iDir);
            if (neighbor.isWire()) {
                continue; // dust gives power through the connections
            }
            // a block can be both, such as the target block
            if (neighbor.isConductor()) {
                power = Math.max(power, getDirectSignalTo(neighbor));
            }
            if (neighbor.isSignalSource()) {
                power = Math.max(power, signal(neighbor, iDir, false));
            }
            if (power >= POWER_MAX) {
                return POWER_MAX;
            }
        }
        return power;
    }

    /**
     * {@return the strong signal a conductor receives from what is around it, which it passes on}
     */
    private int getDirectSignalTo(final Node conductor) {
        int power = POWER_MIN;
        for (int iDir = 0; iDir < Directions.ALL.length; iDir++) {
            final Node neighbor = getNeighbor(conductor, iDir);
            if (neighbor.isSignalSource()) {
                power = Math.max(power, signal(neighbor, iDir, true));
                if (power >= POWER_MAX) {
                    return POWER_MAX;
                }
            }
        }
        return power;
    }

    /**
     * {@return the signal a source emits towards the block it is a neighbour of}
     *
     * @param iDir   the {@link Directions} index from that block to the source
     * @param direct {@code true} for the strong signal
     */
    private int signal(final Node source, final int iDir, final boolean direct) {
        final RedstoneBehaviour behaviour = source.behaviour;
        final BlockData state = source.state;
        if (behaviour == null || state == null) {
            return POWER_MIN;
        }
        return direct
                ? behaviour.directSignal(world, source.pos, state, Directions.ALL[iDir])
                : behaviour.signal(world, source.pos, state, Directions.ALL[iDir]);
    }

    private static boolean needsUpdate(final WireNode wire) {
        return wire.removed || wire.shouldBreak || wire.virtualPower != wire.currentPower;
    }

    /**
     * Queues a dust for the search as a root, flowing as its connections say, or as it was
     * discovered.
     */
    private void searchRoot(final WireNode wire, final int iBackupFlowDir) {
        final int iFlowDir;
        if (wire.connections.iFlowDir >= 0) {
            iFlowDir = wire.connections.iFlowDir;
        } else {
            iFlowDir = Math.max(iBackupFlowDir, 0);
        }
        search(wire, true, iFlowDir);
    }

    /**
     * Queues a dust for the search. Its flow is not known until its power is, but where a network has
     * several sources the order between them depends on which was found first: a backup flow, that of
     * the connections of a root or the direction a dust was found in, makes that less directional.
     */
    private void search(final WireNode wire, final boolean root, final int iBackupFlowDir) {
        search.offer(wire);
        wire.root = root;
        wire.searched = true;
        wire.iFlowDir = iBackupFlowDir;
    }

    private void tryUpdate() {
        if (!search.isEmpty()) {
            update();
        }
        if (!updating) {
            nodes.clear();
            nodeCount = 0;
        }
    }

    /**
     * Updates the network in three steps: search it for the dust needing a power change, depower it,
     * then power it, each dust setting its power and updating the blocks around it once settled.
     */
    private void update() {
        searchNetwork();
        depowerNetwork();
        powerNetwork();
    }

    /**
     * Walks the network from the roots, breadth first, queuing the dust whose power changes with
     * theirs. The order of the search sets the order dust changes power in.
     */
    private void searchNetwork() {
        for (WireNode wire = search.head(); wire != null; wire = wire.nextWire) {
            for (final int iDir : updateOrder.cardinalNeighbors(wire.iFlowDir)) {
                for (WireConnection connection = wire.connections.first(iDir);
                     connection != null && connection.iDir == iDir;
                     connection = connection.next) {
                    if (!connection.offer) {
                        continue;
                    }
                    final WireNode neighbor = connection.wire;
                    if (neighbor.searched || neighbor.foreign) {
                        continue;
                    }
                    discover(neighbor);
                    findPower(neighbor, false);
                    // when the power from other dust dropped, what is not dust tells how low it falls
                    if (neighbor.virtualPower < neighbor.currentPower) {
                        findExternalPower(neighbor);
                    }
                    if (needsUpdate(neighbor)) {
                        search(neighbor, false, connection.iDir);
                    }
                }
            }
        }
    }

    /**
     * Depowers the network, so that power spreads from the sources.
     */
    private void depowerNetwork() {
        WireNode wire = search.poll();
        while (wire != null) {
            findPower(wire, true);
            if (wire.root || wire.removed || wire.shouldBreak || wire.virtualPower > POWER_MIN) {
                queueWire(wire);
            } else {
                // dust receiving nothing waits for the power of its neighbours; below the minimum,
                // it takes any of it and queues its change
                wire.virtualPower--;
            }
            wire = search.poll();
        }
    }

    /**
     * Works through the queue: each dust settles its power and passes it on, then updates the blocks
     * around it.
     */
    private void powerNetwork() {
        // power changes caused by the block updates below join the queue being worked through
        if (updating) {
            return;
        }
        updating = true;
        Node node = updates.poll();
        while (node != null) {
            if (node.isWire() && !node.asWire().foreign) {
                final WireNode wire = node.asWire();
                if (needsUpdate(wire)) {
                    findPowerFlow(wire);
                    firePowerEvent(wire);
                    transmitPower(wire);
                    if (setPower(wire)) {
                        queueNeighbors(wire);
                        // dust placed or removed emitted shape updates already; vanilla updates
                        // neighbours later since 1.19, so observers expect these extra ones
                        updateNeighborShapes(wire);
                    }
                }
            } else if (node.neighborWire != null) {
                world.updateNeighborNow(node.pos, RedstoneWireBlock.KEY);
            }
            node = updates.poll();
        }
        updating = false;
    }

    /**
     * Sets the direction of flow through a dust from where it receives power, else from its
     * connections, else keeps the backup it was searched with.
     */
    private static void findPowerFlow(final WireNode wire) {
        final int flow = FLOW_IN_TO_FLOW_OUT[wire.flowIn];
        if (flow >= 0) {
            wire.iFlowDir = flow;
        } else if (wire.connections.iFlowDir >= 0) {
            wire.iFlowDir = wire.connections.iFlowDir;
        }
    }

    /**
     * Lets the listeners change the power a dust is about to hold, before it passes it on.
     */
    private void firePowerEvent(final WireNode wire) {
        final BlockData state = wire.state;
        if (wire.removed || wire.shouldBreak || state == null) {
            return;
        }
        final int newPower = Math.clamp(wire.virtualPower, POWER_MIN, POWER_MAX);
        final int resulting = RedstoneEvents.dustPower(world.world(), wire.pos, state, wire.currentPower, newPower);
        if (resulting != newPower) {
            wire.virtualPower = resulting;
        }
    }

    /**
     * Passes the power of a dust on to the dust it powers, queuing those that take it.
     */
    private void transmitPower(final WireNode wire) {
        final int power = Math.max(POWER_MIN, wire.virtualPower - POWER_STEP);
        for (final int iDir : updateOrder.cardinalNeighbors(wire.iFlowDir)) {
            for (WireConnection connection = wire.connections.first(iDir);
                 connection != null && connection.iDir == iDir;
                 connection = connection.next) {
                if (connection.offer && connection.wire.offerPower(power, iDir)) {
                    queueWire(connection.wire);
                }
            }
        }
    }

    /**
     * Sets the power of a dust in the world, or breaks it.
     *
     * @return {@code true} if the world changed, so that the blocks around should be updated
     */
    private boolean setPower(final WireNode wire) {
        if (wire.removed) {
            return true;
        }
        final BlockData state = world.blockAt(wire.pos);
        if (!(state instanceof final AnaloguePowerable dust) || !isWire(state)) {
            return false;
        }
        wire.state = state;
        if (wire.shouldBreak) {
            world.setBlock(wire.pos, world.air(), RedstoneWorld.Update.SHAPES);
            return true;
        }
        wire.currentPower = Math.clamp(wire.virtualPower, POWER_MIN, POWER_MAX);
        final BlockData powered = dust.withPower(wire.currentPower);
        wire.state = powered;
        return LevelHelper.setWireState(world, wire.pos, powered, wire.added);
    }

    /**
     * Sends shape updates to the blocks next to a dust, but dust: a power change never changes the
     * shape of dust, and nothing else would follow it.
     */
    private void updateNeighborShapes(final WireNode wire) {
        final BlockData wireState = wire.state;
        if (wireState == null) {
            return;
        }
        for (final int iDir : SHAPE_UPDATE_ORDER) {
            final Node neighbor = getNeighbor(wire, iDir);
            if (!neighbor.isWire() && neighbor.reactsToUpdates()) {
                world.updateShapeNow(neighbor.pos, Directions.ALL[Directions.iOpposite(iDir)], wireState);
            }
        }
    }

    /**
     * Queues block updates to the blocks around a dust.
     */
    private void queueNeighbors(final WireNode wire) {
        updateOrder.forEachNeighbor(neighbors, wire, wire.iFlowDir, queueNeighbor);
    }

    /**
     * Queues a block update to a node from a dust. Dust of the network is not updated, its power
     * being worked out already; neither are the blocks that would not react.
     */
    private void queueNeighbor(final Node node, final WireNode neighborWire) {
        if (node.reactsToUpdates()) {
            node.neighborWire = neighborWire;
            updates.offer(node);
        }
    }

    /**
     * Queues a dust for its power change, or, if its power is already right, passes it on.
     */
    private void queueWire(final WireNode wire) {
        if (needsUpdate(wire)) {
            updates.offer(wire);
        } else {
            findPowerFlow(wire);
            transmitPower(wire);
        }
    }
}
