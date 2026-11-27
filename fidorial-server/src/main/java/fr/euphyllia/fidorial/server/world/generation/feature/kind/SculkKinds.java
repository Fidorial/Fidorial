package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The sculk patches of the deep dark: charges leave a starting point, crawl along the sculk they create, turn the
 * stone they touch into sculk, cover it with veins, and spend what remains on sensors and shriekers.
 */
final class SculkKinds {

    /**
     * Charge above which a cursor is split in several.
     */
    private static final int CHARGE_CAP = 1000;
    /**
     * Number of cursors a spreader follows at most.
     */
    private static final int CURSOR_CAP = 32;
    /**
     * Square of the horizontal distance from the start a charge may travel.
     */
    private static final int REACH_SQUARED = 12 * 12;
    /**
     * Distance at which the charge decay is at its strongest.
     */
    private static final int DECAY_REACH = 24;
    /**
     * Charge spent on a growth, and one chance in this number per update that a charge is used.
     */
    private static final int GROWTH_COST = 50;
    private static final int CHARGE_USE_CHANCE = 5;
    private static final int EXTRA_DECAY_CHANCE = 10;
    private static final int SHRIEKER_CHANCE = 11;
    /**
     * Radius around the start where nothing grows.
     */
    private static final int QUIET_RADIUS = 1;

    /**
     * The 18 offsets to the blocks sharing a face or an edge with a block, in scan order.
     */
    private static final int[][] NEIGHBOURS;

    static {
        final List<int[]> offsets = new ArrayList<>();
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    final boolean flat = dx == 0 || dy == 0 || dz == 0;
                    if (flat && (dx != 0 || dy != 0 || dz != 0)) {
                        offsets.add(new int[] {dx, dy, dz});
                    }
                }
            }
        }
        NEIGHBOURS = offsets.toArray(int[][]::new);
    }

    private SculkKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("sculk_patch", SculkKinds::patch);
    }

    private static Feature patch(final FeatureLibrary library, final JsonObject data) {
        final int chargeCount = data.get("charge_count").getAsInt();
        final int amountPerCharge = data.get("amount_per_charge").getAsInt();
        final int attempts = data.get("spread_attempts").getAsInt();
        final int growthRounds = data.get("growth_rounds").getAsInt();
        final int spreadRounds = data.get("spread_rounds").getAsInt();
        final Blocks blocks = new Blocks(library.context().states());
        return (region, random, x, y, z) -> {
            final Growth growth = new Growth(region, blocks, x, y, z);
            if (!growth.canStart()) {
                return false;
            }
            for (int round = 0; round < spreadRounds + growthRounds; round++) {
                for (int i = 0; i < chargeCount; i++) {
                    growth.addCharge(amountPerCharge);
                }
                final boolean veins = round < spreadRounds;
                for (int i = 0; i < attempts; i++) {
                    growth.step(random, veins);
                }
                growth.cursors.clear();
            }
            return true;
        };
    }

    /**
     * The states and block sets the growth works with.
     */
    private static final class Blocks {

        final StateFlags states;
        final char sculk;
        final char vein;
        final char sensor;
        final char shrieker;
        final char air;
        final char water;
        final StateFlags.Membership isSculk;
        final StateFlags.Membership isVein;
        final StateFlags.Membership stopsVeins;
        final StateFlags.Membership growable;
        final StateFlags.Membership substrate;
        final StateFlags.Membership inhibitors;
        final StateFlags.Membership fire;

        Blocks(final StateFlags states) {
            this.states = states;
            this.sculk = state(states, BlockTypeKeys.SCULK);
            this.vein = state(states, BlockTypeKeys.SCULK_VEIN);
            this.sensor = state(states, BlockTypeKeys.SCULK_SENSOR);
            this.shrieker = states.with(state(states, BlockTypeKeys.SCULK_SHRIEKER), BlockProperties.CAN_SUMMON, true);
            this.air = states.palette().id(states.blocks().air);
            this.water = states.palette().id(states.blocks().water);
            this.isSculk = states.membership(Set.of(BlockTypeKeys.SCULK));
            this.isVein = states.membership(Set.of(BlockTypeKeys.SCULK_VEIN));
            this.stopsVeins = states.membership(Set.of(BlockTypeKeys.SCULK, BlockTypeKeys.SCULK_CATALYST, BlockTypeKeys.MOVING_PISTON));
            this.growable = states.membership(BlockTypeTagKeys.SCULK_REPLACEABLE_WORLD_GEN);
            this.substrate = states.membership(BlockTypeTagKeys.SCULK_REPLACEABLE);
            this.inhibitors = states.membership(BlockTypeTagKeys.SCULK_GROWTH_INHIBITORS);
            this.fire = states.membership(BlockTypeTagKeys.FIRE);
        }

        private static char state(final StateFlags states, final TypedKey<BlockType> block) {
            return states.palette().id(states.blocks().state(block));
        }

        /**
         * {@return {@code true} for sculk and veins, the blocks a charge can stand on}
         */
        boolean carriesCharge(final char state) {
            return isSculk.test(state) || isVein.test(state);
        }

        /**
         * {@return the faces of a vein as a bit set by direction, nothing for any other block}
         */
        int faces(final char state) {
            if (!isVein.test(state)) {
                return 0;
            }
            int faces = 0;
            for (int direction = 0; direction < 6; direction++) {
                if (Boolean.TRUE.equals(states.get(state, Facing.FLAGS[direction]))) {
                    faces |= 1 << direction;
                }
            }
            return faces;
        }

        boolean waterSource(final char state) {
            return states.isWaterBlock(state) && states.has(state, StateFlags.WATER_SOURCE);
        }
    }

    /**
     * A charge moving through the sculk.
     */
    private static final class Cursor {

        int x;
        int y;
        int z;
        int charge;
        int waitUpdates;
        int decayGrace = 1;
        /**
         * The faces of the vein the charge stands on, as a bit set; {@code -1} while unknown.
         */
        int faces = -1;

        Cursor(final int x, final int y, final int z, final int charge) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.charge = charge;
        }
    }

    /**
     * How a block reacts to a charge standing on it.
     */
    private enum Behaviour {
        /**
         * A block of sculk.
         */
        SCULK,
        /**
         * A vein.
         */
        VEIN,
        /**
         * Any other block: the charge only fades there.
         */
        OTHER;

        boolean rereadsAfterVeins() {
            return this != SCULK;
        }
    }

    /**
     * One patch being grown.
     */
    private static final class Growth {

        final DecorationRegion region;
        final Blocks blocks;
        final StateFlags states;
        final int originX;
        final int originY;
        final int originZ;
        List<Cursor> cursors = new ArrayList<>();

        Growth(final DecorationRegion region, final Blocks blocks, final int x, final int y, final int z) {
            this.region = region;
            this.blocks = blocks;
            this.states = blocks.states;
            this.originX = x;
            this.originY = y;
            this.originZ = z;
        }

        boolean canStart() {
            final char start = region.id(originX, originY, originZ);
            if (blocks.carriesCharge(start)) {
                return true;
            }
            if (!states.isAir(start) && !blocks.waterSource(start)) {
                return false;
            }
            for (int direction = 0; direction < 6; direction++) {
                final char neighbour = region.id(originX + Facing.DX[direction], originY + Facing.DY[direction], originZ + Facing.DZ[direction]);
                if (states.has(neighbour, StateFlags.FULL_COLLISION)) {
                    return true;
                }
            }
            return false;
        }

        void addCharge(final int amount) {
            int left = amount;
            while (left > 0) {
                final int part = Math.min(left, CHARGE_CAP);
                if (cursors.size() < CURSOR_CAP) {
                    cursors.add(new Cursor(originX, originY, originZ, part));
                }
                left -= part;
            }
        }

        void step(final BitRandom random, final boolean veins) {
            if (cursors.isEmpty()) {
                return;
            }
            final List<Cursor> kept = new ArrayList<>(cursors.size());
            for (final Cursor cursor : cursors) {
                update(cursor, random, veins);
                if (cursor.charge > 0) {
                    kept.add(cursor);
                }
            }
            cursors = kept;
        }

        private Behaviour behaviour(final char state) {
            if (blocks.isSculk.test(state)) {
                return Behaviour.SCULK;
            }
            return blocks.isVein.test(state) ? Behaviour.VEIN : Behaviour.OTHER;
        }

        private void update(final Cursor cursor, final BitRandom random, final boolean veins) {
            if (cursor.charge <= 0) {
                return;
            }
            if (cursor.waitUpdates > 0) {
                cursor.waitUpdates--;
                return;
            }
            char state = region.id(cursor.x, cursor.y, cursor.z);
            Behaviour behaviour = behaviour(state);
            if (veins && spreadVeins(behaviour, cursor, state) && behaviour.rereadsAfterVeins()) {
                state = region.id(cursor.x, cursor.y, cursor.z);
                behaviour = behaviour(state);
            }
            cursor.charge = useCharge(behaviour, cursor, random, veins);
            if (cursor.charge <= 0) {
                discharge(behaviour, state, cursor.x, cursor.y, cursor.z);
                return;
            }
            final int[] target = nextPosition(cursor.x, cursor.y, cursor.z, random);
            if (target == null) {
                discharge(behaviour, state, cursor.x, cursor.y, cursor.z);
                cursor.charge = 0;
                return;
            }
            discharge(behaviour, state, cursor.x, cursor.y, cursor.z);
            cursor.x = target[0];
            cursor.y = target[1];
            cursor.z = target[2];
            final char landing = region.id(cursor.x, cursor.y, cursor.z);
            if (blocks.carriesCharge(landing)) {
                cursor.faces = blocks.faces(landing);
            }
            cursor.decayGrace = behaviour == Behaviour.OTHER ? Math.max(cursor.decayGrace - 1, 0) : 1;
            cursor.waitUpdates = 1;
        }

        private boolean spreadVeins(final Behaviour behaviour, final Cursor cursor, final char state) {
            if (behaviour == Behaviour.OTHER) {
                if (cursor.faces == -1) {
                    return spreadAll(state, cursor.x, cursor.y, cursor.z, true) > 0;
                }
                if (cursor.faces != 0) {
                    if (!states.isAir(state) && !(states.hasFluid(state) && states.has(state, StateFlags.WATER_SOURCE))) {
                        return false;
                    }
                    return regrow(cursor.x, cursor.y, cursor.z, state, cursor.faces);
                }
            }
            return spreadAll(state, cursor.x, cursor.y, cursor.z, false) > 0;
        }

        /**
         * Puts a vein back with the faces it had, where it still can hold.
         */
        private boolean regrow(final int x, final int y, final int z, final char existing, final int faces) {
            char vein = blocks.vein;
            boolean any = false;
            for (int direction = 0; direction < 6; direction++) {
                if ((faces & 1 << direction) != 0 && canAttach(x, y, z, direction)) {
                    vein = states.with(vein, Facing.FLAGS[direction], true);
                    any = true;
                }
            }
            if (!any) {
                return false;
            }
            if (states.hasFluid(existing)) {
                vein = states.with(vein, BlockProperties.WATERLOGGED, true);
            }
            region.set(x, y, z, vein);
            return true;
        }

        private boolean canAttach(final int x, final int y, final int z, final int direction) {
            final char neighbour = region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]);
            return states.sturdy(neighbour, PlantKinds.face(Facing.OPPOSITE[direction]));
        }

        /**
         * Spreads veins from every face of a block toward every other direction.
         *
         * @param sameSpot {@code true} to only add faces on the block itself
         * @return the number of faces added
         */
        private int spreadAll(final char source, final int x, final int y, final int z, final boolean sameSpot) {
            final int sourceFaces = blocks.faces(source);
            final boolean anySource = !blocks.isVein.test(source);
            int added = 0;
            for (int from = 0; from < 6; from++) {
                if (!anySource && (sourceFaces & 1 << from) == 0) {
                    continue;
                }
                for (int toward = 0; toward < 6; toward++) {
                    if (Facing.axis(toward) == Facing.axis(from)) {
                        continue;
                    }
                    if (!anySource && (sourceFaces & 1 << toward) != 0) {
                        continue;
                    }
                    if (spreadOnce(x, y, z, from, toward, sameSpot)) {
                        added++;
                    }
                }
            }
            return added;
        }

        private boolean spreadOnce(final int x, final int y, final int z, final int from, final int toward, final boolean sameSpot) {
            final int kinds = sameSpot ? 1 : 3;
            for (int kind = 0; kind < kinds; kind++) {
                final int px;
                final int py;
                final int pz;
                final int face;
                switch (kind) {
                    case 0 -> {
                        px = x;
                        py = y;
                        pz = z;
                        face = toward;
                    }
                    case 1 -> {
                        px = x + Facing.DX[toward];
                        py = y + Facing.DY[toward];
                        pz = z + Facing.DZ[toward];
                        face = from;
                    }
                    default -> {
                        px = x + Facing.DX[toward] + Facing.DX[from];
                        py = y + Facing.DY[toward] + Facing.DY[from];
                        pz = z + Facing.DZ[toward] + Facing.DZ[from];
                        face = Facing.OPPOSITE[toward];
                    }
                }
                final char existing = region.id(px, py, pz);
                if (canTake(x, y, z, px, py, pz, face, existing) && canHold(existing, px, py, pz, face)) {
                    final char placed = withFace(region.id(px, py, pz), px, py, pz, face);
                    return placed != StateSource.NONE && region.set(px, py, pz, placed);
                }
            }
            return false;
        }

        private boolean canTake(final int sx, final int sy, final int sz, final int px, final int py, final int pz, final int face,
                                final char existing) {
            final char against = region.id(px + Facing.DX[face], py + Facing.DY[face], pz + Facing.DZ[face]);
            if (blocks.stopsVeins.test(against)) {
                return false;
            }
            if (Math.abs(sx - px) + Math.abs(sy - py) + Math.abs(sz - pz) == 2) {
                final int back = Facing.OPPOSITE[face];
                final char behind = region.id(sx + Facing.DX[back], sy + Facing.DY[back], sz + Facing.DZ[back]);
                if (states.sturdy(behind, PlantKinds.face(face))) {
                    return false;
                }
            }
            if (states.hasFluid(existing) && !(states.has(existing, StateFlags.WATER_SOURCE) && !states.has(existing, StateFlags.LAVA))) {
                return false;
            }
            if (blocks.fire.test(existing)) {
                return false;
            }
            return states.has(existing, StateFlags.REPLACEABLE) || states.isAir(existing) || blocks.isVein.test(existing)
                    || blocks.waterSource(existing);
        }

        private boolean canHold(final char existing, final int x, final int y, final int z, final int face) {
            if (blocks.isVein.test(existing) && (blocks.faces(existing) & 1 << face) != 0) {
                return false;
            }
            return canAttach(x, y, z, face);
        }

        /**
         * {@return the state with a vein face added, {@link StateSource#NONE} when it cannot hold there}
         */
        private char withFace(final char existing, final int x, final int y, final int z, final int face) {
            if (!canHold(existing, x, y, z, face)) {
                return StateSource.NONE;
            }
            final char state;
            if (blocks.isVein.test(existing)) {
                state = existing;
            } else if (states.has(existing, StateFlags.WATER_SOURCE) && !states.has(existing, StateFlags.LAVA)) {
                state = states.with(blocks.vein, BlockProperties.WATERLOGGED, true);
            } else {
                state = blocks.vein;
            }
            return states.with(state, Facing.FLAGS[face], true);
        }

        private int useCharge(final Behaviour behaviour, final Cursor cursor, final BitRandom random, final boolean veins) {
            return switch (behaviour) {
                case OTHER -> cursor.decayGrace > 0 ? cursor.charge : 0;
                case VEIN -> {
                    if (veins && placeSculk(cursor.x, cursor.y, cursor.z, random)) {
                        yield cursor.charge - 1;
                    }
                    yield random.nextInt(CHARGE_USE_CHANCE) == 0 ? (int) Math.floor(cursor.charge * 0.5F) : cursor.charge;
                }
                case SCULK -> sculkCharge(cursor, random);
            };
        }

        private int sculkCharge(final Cursor cursor, final BitRandom random) {
            final int charge = cursor.charge;
            if (charge == 0 || random.nextInt(CHARGE_USE_CHANCE) != 0) {
                return charge;
            }
            final int dx = cursor.x - originX;
            final int dy = cursor.y - originY;
            final int dz = cursor.z - originZ;
            final int distanceSquared = dx * dx + dy * dy + dz * dz;
            final boolean nearStart = distanceSquared < QUIET_RADIUS * QUIET_RADIUS;
            if (!nearStart && roomForGrowth(cursor.x, cursor.y, cursor.z)) {
                if (random.nextInt(GROWTH_COST) < charge) {
                    char growth = random.nextInt(SHRIEKER_CHANCE) == 0 ? blocks.shrieker : blocks.sensor;
                    if (states.hasFluid(region.id(cursor.x, cursor.y + 1, cursor.z))) {
                        growth = states.with(growth, BlockProperties.WATERLOGGED, true);
                    }
                    region.set(cursor.x, cursor.y + 1, cursor.z, growth);
                }
                return Math.max(0, charge - GROWTH_COST);
            }
            if (random.nextInt(EXTRA_DECAY_CHANCE) != 0) {
                return charge;
            }
            if (nearStart) {
                return charge - 1;
            }
            final float beyond = (float) Math.sqrt(distanceSquared) - QUIET_RADIUS;
            final float factor = Math.min(1.0F, beyond * beyond / (float) ((DECAY_REACH - QUIET_RADIUS) * (DECAY_REACH - QUIET_RADIUS)));
            return charge - Math.max(1, (int) (charge * factor * 0.5F));
        }

        private boolean roomForGrowth(final int x, final int y, final int z) {
            final char above = region.id(x, y + 1, z);
            if (!states.isAir(above) && !blocks.waterSource(above)) {
                return false;
            }
            int found = 0;
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    for (int dx = -4; dx <= 4; dx++) {
                        if (blocks.inhibitors.test(region.id(x + dx, y + dy, z + dz)) && ++found > 2) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        /**
         * Turns a block a vein clings to into sculk, and lets veins grow over the new sculk.
         */
        private boolean placeSculk(final int x, final int y, final int z, final BitRandom random) {
            final char state = region.id(x, y, z);
            final int faces = blocks.faces(state);
            for (final int support : Facing.shuffled(Facing.ALL, random)) {
                if ((faces & 1 << support) == 0) {
                    continue;
                }
                final int sx = x + Facing.DX[support];
                final int sy = y + Facing.DY[support];
                final int sz = z + Facing.DZ[support];
                if (!blocks.growable.test(region.id(sx, sy, sz))) {
                    continue;
                }
                region.set(sx, sy, sz, blocks.sculk);
                spreadAll(blocks.sculk, sx, sy, sz, false);
                final int skip = Facing.OPPOSITE[support];
                for (int direction = 0; direction < 6; direction++) {
                    if (direction == skip) {
                        continue;
                    }
                    final int vx = sx + Facing.DX[direction];
                    final int vy = sy + Facing.DY[direction];
                    final int vz = sz + Facing.DZ[direction];
                    final char nearby = region.id(vx, vy, vz);
                    if (blocks.isVein.test(nearby)) {
                        discharge(Behaviour.VEIN, nearby, vx, vy, vz);
                    }
                }
                return true;
            }
            return false;
        }

        /**
         * Removes from a vein the faces lying against sculk, and the vein itself when no face is left.
         */
        private void discharge(final Behaviour behaviour, final char state, final int x, final int y, final int z) {
            if (behaviour != Behaviour.VEIN || !blocks.isVein.test(state)) {
                return;
            }
            char result = state;
            for (int direction = 0; direction < 6; direction++) {
                if (Boolean.TRUE.equals(states.get(result, Facing.FLAGS[direction]))
                        && blocks.isSculk.test(region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]))) {
                    result = states.with(result, Facing.FLAGS[direction], false);
                }
            }
            if (blocks.faces(result) == 0) {
                result = states.hasFluid(region.id(x, y, z)) ? blocks.water : blocks.air;
            }
            region.set(x, y, z, result);
        }

        private int @Nullable [] nextPosition(final int x, final int y, final int z, final BitRandom random) {
            final int[][] order = NEIGHBOURS.clone();
            for (int i = order.length; i > 1; i--) {
                final int swap = random.nextInt(i);
                final int[] kept = order[i - 1];
                order[i - 1] = order[swap];
                order[swap] = kept;
            }
            int[] found = null;
            for (final int[] offset : order) {
                final int nx = x + offset[0];
                final int ny = y + offset[1];
                final int nz = z + offset[2];
                final int fx = originX - nx;
                final int fz = originZ - nz;
                if (fx * fx + fz * fz > REACH_SQUARED) {
                    continue;
                }
                final char target = region.id(nx, ny, nz);
                if (!blocks.carriesCharge(target) || !reachable(x, y, z, offset)) {
                    continue;
                }
                found = new int[] {nx, ny, nz};
                if (touchesSubstrate(target, nx, ny, nz)) {
                    break;
                }
            }
            return found;
        }

        private boolean reachable(final int x, final int y, final int z, final int[] offset) {
            if (Math.abs(offset[0]) + Math.abs(offset[1]) + Math.abs(offset[2]) == 1) {
                return true;
            }
            final int alongX = offset[0] < 0 ? Facing.WEST : Facing.EAST;
            final int alongY = offset[1] < 0 ? Facing.DOWN : Facing.UP;
            final int alongZ = offset[2] < 0 ? Facing.NORTH : Facing.SOUTH;
            if (offset[0] == 0) {
                return open(x, y, z, alongY) || open(x, y, z, alongZ);
            }
            if (offset[1] == 0) {
                return open(x, y, z, alongX) || open(x, y, z, alongZ);
            }
            return open(x, y, z, alongX) || open(x, y, z, alongY);
        }

        private boolean open(final int x, final int y, final int z, final int direction) {
            final char next = region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]);
            return !states.sturdy(next, PlantKinds.face(Facing.OPPOSITE[direction]));
        }

        private boolean touchesSubstrate(final char state, final int x, final int y, final int z) {
            if (!blocks.isVein.test(state)) {
                return false;
            }
            final int faces = blocks.faces(state);
            for (int direction = 0; direction < 6; direction++) {
                if ((faces & 1 << direction) != 0
                        && blocks.substrate.test(region.id(x + Facing.DX[direction], y + Facing.DY[direction], z + Facing.DZ[direction]))) {
                    return true;
                }
            }
            return false;
        }
    }
}
