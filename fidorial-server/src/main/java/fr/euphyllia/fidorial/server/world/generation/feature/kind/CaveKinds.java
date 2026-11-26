package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseDefinition;
import fr.euphyllia.fidorial.server.world.generation.noise.OctaveStack;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.fidorial.entity.EntityType;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.EntityTypeKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The features hidden underground: dungeons with their spawner and chests, and geodes.
 */
final class CaveKinds {

    private static final List<TypedKey<EntityType>> DUNGEON_MOBS = List.of(EntityTypeKeys.SKELETON, EntityTypeKeys.ZOMBIE,
            EntityTypeKeys.ZOMBIE, EntityTypeKeys.SPIDER);
    private static final Key DUNGEON_LOOT = Key.key("chests/simple_dungeon");

    private CaveKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("monster_room", (library, data) -> CaveKinds::dungeon);
        parsers.put("geode", (library, data) -> new Geode(library, data));
    }

    private static boolean dungeon(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
        final StateFlags states = region.states();
        final int xr = random.nextInt(2) + 2;
        final int minX = -xr - 1;
        final int maxX = xr + 1;
        final int zr = random.nextInt(2) + 2;
        final int minZ = -zr - 1;
        final int maxZ = zr + 1;
        int holes = 0;
        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = -1; dy <= 4; dy++) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    final boolean solid = region.has(x + dx, y + dy, z + dz, StateFlags.SOLID);
                    if ((dy == -1 || dy == 4) && !solid) {
                        return false;
                    }
                    if ((dx == minX || dx == maxX || dz == minZ || dz == maxZ) && dy == 0
                            && region.isAir(x + dx, y, z + dz) && region.isAir(x + dx, y + 1, z + dz)) {
                        holes++;
                    }
                }
            }
        }
        if (holes < 1 || holes > 5) {
            return false;
        }
        final char caveAir = region.id(region.blocks().caveAir);
        final char cobble = region.id(region.blocks().state(BlockTypeKeys.COBBLESTONE));
        final char mossy = region.id(region.blocks().state(BlockTypeKeys.MOSSY_COBBLESTONE));
        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = 3; dy >= -1; dy--) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    final int px = x + dx;
                    final int py = y + dy;
                    final int pz = z + dz;
                    final char wall = region.id(px, py, pz);
                    if (dx == minX || dy == -1 || dz == minZ || dx == maxX || dy == 4 || dz == maxZ) {
                        if (py >= region.minY() && !region.has(px, py - 1, pz, StateFlags.SOLID)) {
                            region.set(px, py, pz, caveAir);
                        } else if (states.has(wall, StateFlags.SOLID) && !states.is(wall, BlockTypeKeys.CHEST)) {
                            if (dy == -1 && random.nextInt(4) != 0) {
                                setUnlessProtected(region, px, py, pz, mossy);
                            } else {
                                setUnlessProtected(region, px, py, pz, cobble);
                            }
                        }
                    } else if (!states.is(wall, BlockTypeKeys.CHEST) && !states.is(wall, BlockTypeKeys.SPAWNER)) {
                        setUnlessProtected(region, px, py, pz, caveAir);
                    }
                }
            }
        }
        final char chest = region.id(region.blocks().state(BlockTypeKeys.CHEST));
        for (int attempt = 0; attempt < 2; attempt++) {
            for (int i = 0; i < 3; i++) {
                final int cx = x + random.nextInt(xr * 2 + 1) - xr;
                final int cz = z + random.nextInt(zr * 2 + 1) - zr;
                if (!region.isAir(cx, y, cz)) {
                    continue;
                }
                int walls = 0;
                for (final int side : Facing.HORIZONTAL) {
                    if (region.has(cx + Facing.DX[side], y, cz + Facing.DZ[side], StateFlags.SOLID)) {
                        walls++;
                    }
                }
                if (walls == 1) {
                    if (setUnlessProtected(region, cx, y, cz, facingAway(region, cx, y, cz, chest))) {
                        region.putBlockEntity(cx, y, cz, CompoundBinaryTag.builder()
                                .putString("LootTable", DUNGEON_LOOT.asString())
                                .putLong("LootTableSeed", random.nextLong())
                                .build());
                    }
                    break;
                }
            }
        }
        final char spawner = region.id(region.blocks().state(BlockTypeKeys.SPAWNER));
        if (setUnlessProtected(region, x, y, z, spawner)) {
            final String mob = DUNGEON_MOBS.get(random.nextInt(DUNGEON_MOBS.size())).key().asString();
            region.putBlockEntity(x, y, z, CompoundBinaryTag.builder()
                    .put("SpawnData", CompoundBinaryTag.builder()
                            .put("entity", CompoundBinaryTag.builder().putString("id", mob).build())
                            .build())
                    .putShort("Delay", (short) 20)
                    .putShort("MinSpawnDelay", (short) 200)
                    .putShort("MaxSpawnDelay", (short) 800)
                    .putShort("SpawnCount", (short) 4)
                    .putShort("MaxNearbyEntities", (short) 6)
                    .putShort("RequiredPlayerRange", (short) 16)
                    .putShort("SpawnRange", (short) 4)
                    .build());
        }
        return true;
    }

    private static boolean setUnlessProtected(final DecorationRegion region, final int x, final int y, final int z, final char state) {
        if (region.has(x, y, z, StateFlags.PROTECTED)) {
            return false;
        }
        return region.set(x, y, z, state);
    }

    /**
     * {@return a chest turned away from the one wall it stands against, or toward the most open side}
     */
    static char facingAway(final DecorationRegion region, final int x, final int y, final int z, final char state) {
        final StateFlags states = region.states();
        int wall = -1;
        for (final int side : Facing.HORIZONTAL) {
            final char neighbour = region.id(x + Facing.DX[side], y, z + Facing.DZ[side]);
            if (states.is(neighbour, BlockTypeKeys.CHEST)) {
                return state;
            }
            if (states.has(neighbour, StateFlags.FULL_COLLISION)) {
                if (wall != -1) {
                    wall = -1;
                    break;
                }
                wall = side;
            }
        }
        if (wall != -1) {
            return states.with(state, "facing", Facing.NAMES[Facing.OPPOSITE[wall]]);
        }
        int facing = Facing.byName(states.property(state, "facing"));
        if (blocked(region, x, y, z, facing)) {
            facing = Facing.OPPOSITE[facing];
        }
        if (blocked(region, x, y, z, facing)) {
            facing = Facing.clockwise(facing);
        }
        if (blocked(region, x, y, z, facing)) {
            facing = Facing.OPPOSITE[facing];
        }
        return states.with(state, "facing", Facing.NAMES[facing]);
    }

    private static boolean blocked(final DecorationRegion region, final int x, final int y, final int z, final int side) {
        return region.has(x + Facing.DX[side], y, z + Facing.DZ[side], StateFlags.FULL_COLLISION);
    }

    /**
     * A hollow ball of nested shells around a few random points, its innermost shell growing crystals, often
     * cracked open on one side.
     */
    private static final class Geode implements Feature {

        private static final Map<Long, OctaveStack> NOISES = new ConcurrentHashMap<>();

        private final StateSource filling;
        private final StateSource inner;
        private final StateSource alternateInner;
        private final StateSource middle;
        private final StateSource outer;
        private final char[] crystals;
        private final StateFlags.Membership cannotReplace;
        private final StateFlags.Membership invalid;
        private final double fillingLayer;
        private final double innerLayer;
        private final double middleLayer;
        private final double outerLayer;
        private final double crackChance;
        private final double baseCrackSize;
        private final int crackPointOffset;
        private final double crystalChance;
        private final double alternateChance;
        private final boolean crystalsNeedAlternate;
        private final IntSource wallDistance;
        private final IntSource points;
        private final IntSource pointOffset;
        private final int minOffset;
        private final int maxOffset;
        private final double noiseMultiplier;
        private final int invalidThreshold;

        Geode(final FeatureLibrary library, final JsonObject data) {
            final JsonObject blocks = data.getAsJsonObject("blocks");
            this.filling = library.source(blocks.get("filling_provider"));
            this.inner = library.source(blocks.get("inner_layer_provider"));
            this.alternateInner = library.source(blocks.get("alternate_inner_layer_provider"));
            this.middle = library.source(blocks.get("middle_layer_provider"));
            this.outer = library.source(blocks.get("outer_layer_provider"));
            final List<Character> crystalList = new ArrayList<>();
            for (final JsonElement crystal : blocks.getAsJsonArray("inner_placements")) {
                crystalList.add(library.state(crystal));
            }
            this.crystals = new char[crystalList.size()];
            for (int i = 0; i < crystals.length; i++) {
                crystals[i] = crystalList.get(i);
            }
            this.cannotReplace = library.blockSet(blocks.get("cannot_replace"));
            this.invalid = library.blockSet(blocks.get("invalid_blocks"));
            final JsonObject layers = data.getAsJsonObject("layers");
            this.fillingLayer = number(layers, "filling", 1.7);
            this.innerLayer = number(layers, "inner_layer", 2.2);
            this.middleLayer = number(layers, "middle_layer", 3.2);
            this.outerLayer = number(layers, "outer_layer", 4.2);
            final JsonObject crack = data.getAsJsonObject("crack");
            this.crackChance = number(crack, "generate_crack_chance", 1.0);
            this.baseCrackSize = number(crack, "base_crack_size", 2.0);
            this.crackPointOffset = (int) number(crack, "crack_point_offset", 2.0);
            this.crystalChance = number(data, "use_potential_placements_chance", 0.35);
            this.alternateChance = number(data, "use_alternate_layer0_chance", 0.0);
            this.crystalsNeedAlternate = !data.has("placements_require_layer0_alternate")
                    || data.get("placements_require_layer0_alternate").getAsBoolean();
            this.wallDistance = data.has("outer_wall_distance") ? IntSource.parse(data.get("outer_wall_distance")) : uniform(4, 5);
            this.points = data.has("distribution_points") ? IntSource.parse(data.get("distribution_points")) : uniform(3, 4);
            this.pointOffset = data.has("point_offset") ? IntSource.parse(data.get("point_offset")) : uniform(1, 2);
            this.minOffset = (int) number(data, "min_gen_offset", -16);
            this.maxOffset = (int) number(data, "max_gen_offset", 16);
            this.noiseMultiplier = number(data, "noise_multiplier", 0.05);
            this.invalidThreshold = data.get("invalid_blocks_threshold").getAsInt();
        }

        private static double number(final JsonObject object, final String key, final double fallback) {
            return object != null && object.has(key) ? object.get(key).getAsDouble() : fallback;
        }

        private static IntSource uniform(final int min, final int max) {
            // a range without a type is a uniform draw
            final JsonObject object = new JsonObject();
            object.addProperty("min_inclusive", min);
            object.addProperty("max_inclusive", max);
            return IntSource.parse(object);
        }

        /**
         * {@return the noise that roughens the shells: one octave, scaled as the older single-octave noises were,
         * seeded by the world}
         */
        private static OctaveStack noise(final long seed) {
            return NOISES.computeIfAbsent(seed, key -> {
                // one octave at 1/16, with the weight that gives it the spread of the older noises
                final double octaveDeviation = 0.2702247831245211;
                final double scaled = 1.0 / 3.0 / (octaveDeviation * Math.sqrt(2.0));
                final double older = 0.5 / 3.0 / (0.1 * 2.0);
                final NoiseDefinition definition = new NoiseDefinition(older / scaled, -4, 1, NoiseDefinition.Scaling.NORMALIZED,
                        new double[0]);
                return definition.create(new LegacyRandom(key));
            });
        }

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            final StateFlags states = region.states();
            final int pointCount = points.sample(random);
            final OctaveStack noise = noise(region.seed());
            final double crackAdjust = (double) pointCount / wallDistance.max();
            final double airLimit = 1.0 / Math.sqrt(fillingLayer);
            final double innerLimit = 1.0 / Math.sqrt(innerLayer + crackAdjust);
            final double middleLimit = 1.0 / Math.sqrt(middleLayer + crackAdjust);
            final double outerLimit = 1.0 / Math.sqrt(outerLayer + crackAdjust);
            final double crackSize = 1.0 / Math.sqrt(baseCrackSize + random.nextDouble() / 2.0 + (pointCount > 3 ? crackAdjust : 0.0));
            final boolean cracked = random.nextFloat() < crackChance;
            int invalidPoints = 0;
            final List<int[]> centres = new ArrayList<>();
            for (int i = 0; i < pointCount; i++) {
                final int px = x + wallDistance.sample(random);
                final int py = y + wallDistance.sample(random);
                final int pz = z + wallDistance.sample(random);
                final char state = region.id(px, py, pz);
                if (states.isAir(state) || invalid.test(state)) {
                    if (++invalidPoints > invalidThreshold) {
                        return false;
                    }
                }
                centres.add(new int[] {px, py, pz, pointOffset.sample(random)});
            }
            final List<int[]> crackPoints = new ArrayList<>();
            if (cracked) {
                final int side = random.nextInt(4);
                final int offset = pointCount * 2 + 1;
                final int cx = side == 0 || side == 2 ? offset : 0;
                final int cz = side == 1 || side == 2 ? offset : 0;
                crackPoints.add(new int[] {x + cx, y + 7, z + cz});
                crackPoints.add(new int[] {x + cx, y + 5, z + cz});
                crackPoints.add(new int[] {x + cx, y + 1, z + cz});
            }
            final List<int[]> crystalSpots = new ArrayList<>();
            for (int pz = z + minOffset; pz <= z + maxOffset; pz++) {
                for (int py = y + minOffset; py <= y + maxOffset; py++) {
                    for (int px = x + minOffset; px <= x + maxOffset; px++) {
                        final double noiseOffset = noise.sample(px, py, pz) * noiseMultiplier;
                        double shell = 0.0;
                        for (final int[] centre : centres) {
                            shell += 1.0 / Math.sqrt(distanceSquared(px, py, pz, centre) + centre[3]) + noiseOffset;
                        }
                        if (shell < outerLimit) {
                            continue;
                        }
                        if (shell >= airLimit) {
                            setReplaceable(region, px, py, pz, filling.pickOrKeep(region, random, px, py, pz));
                            continue;
                        }
                        double crack = 0.0;
                        for (final int[] point : crackPoints) {
                            crack += 1.0 / Math.sqrt(distanceSquared(px, py, pz, point) + crackPointOffset) + noiseOffset;
                        }
                        if (cracked && crack >= crackSize) {
                            setReplaceable(region, px, py, pz, region.id(region.blocks().air));
                            for (final int direction : Facing.ALL) {
                                region.scheduleFluid(px + Facing.DX[direction], py + Facing.DY[direction], pz + Facing.DZ[direction]);
                            }
                        } else if (shell >= innerLimit) {
                            final boolean alternate = random.nextFloat() < alternateChance;
                            final StateSource layer = alternate ? alternateInner : inner;
                            setReplaceable(region, px, py, pz, layer.pickOrKeep(region, random, px, py, pz));
                            if ((!crystalsNeedAlternate || alternate) && random.nextFloat() < crystalChance) {
                                crystalSpots.add(new int[] {px, py, pz});
                            }
                        } else if (shell >= middleLimit) {
                            setReplaceable(region, px, py, pz, middle.pickOrKeep(region, random, px, py, pz));
                        } else {
                            setReplaceable(region, px, py, pz, outer.pickOrKeep(region, random, px, py, pz));
                        }
                    }
                }
            }
            for (final int[] spot : crystalSpots) {
                char crystal = crystals[random.nextInt(crystals.length)];
                for (final int direction : Facing.ALL) {
                    crystal = states.withIfAllowed(crystal, "facing", Facing.NAMES[direction]);
                    final int px = spot[0] + Facing.DX[direction];
                    final int py = spot[1] + Facing.DY[direction];
                    final int pz = spot[2] + Facing.DZ[direction];
                    final char there = region.id(px, py, pz);
                    crystal = states.withIfAllowed(crystal, "waterlogged", Boolean.toString(states.has(there, StateFlags.WATER_SOURCE)));
                    if (states.isAir(there) || states.isWaterBlock(there) && states.has(there, StateFlags.WATER_SOURCE)) {
                        setReplaceable(region, px, py, pz, crystal);
                        break;
                    }
                }
            }
            return true;
        }

        private void setReplaceable(final DecorationRegion region, final int x, final int y, final int z, final char state) {
            if (!cannotReplace.test(region.id(x, y, z))) {
                region.set(x, y, z, state);
            }
        }

        private static double distanceSquared(final int x, final int y, final int z, final int[] point) {
            final double dx = x - point[0];
            final double dy = y - point[1];
            final double dz = z - point[2];
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
