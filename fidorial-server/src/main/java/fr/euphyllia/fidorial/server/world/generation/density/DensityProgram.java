package fr.euphyllia.fidorial.server.world.generation.density;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.noise.BlendedTerrainNoise;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseDefinition;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseField;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Compiles the density functions of a world: every function of the data, a named one or one written inline, is
 * turned into a tree of {@link Density} nodes, with the noises drawn from the seed of the world.
 *
 * <p>A named function is compiled once, so that all the places using it share its caches. Where a function that
 * depends on the height uses one that does not, the latter is cached by column: it is computed once for the whole
 * column instead of once per block.</p>
 *
 * <p>Compiling is not thread-safe; once everything is compiled, the program and its functions are immutable and
 * every thread evaluates them with its own {@linkplain #context() context}.</p>
 */
public final class DensityProgram {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DensityProgram.class);
    private static final Key TERRAIN_SEED = Key.key( "terrain");

    private final WorldgenData data;
    private final HeightContext heights;
    private final PositionalRandom random;
    private final Map<String, Density> named = new HashMap<>();
    private final Set<String> compiling = new HashSet<>();
    private final Map<Key, NoiseField> noises = new HashMap<>();
    private final Map<Density, Density> columnCaches = new IdentityHashMap<>();
    private final Set<String> warned = new HashSet<>();
    private final ThreadLocal<DensityContext> contexts = ThreadLocal.withInitial(this::newContext);
    private int cacheSlots;
    private int gridSlots;

    /**
     * @param data    where the named functions and noises are read
     * @param heights the vertical extent heights are resolved against
     * @param random  the positional factory of the world seed
     */
    public DensityProgram(final WorldgenData data, final HeightContext heights, final PositionalRandom random) {
        this.data = data;
        this.heights = heights;
        this.random = random;
    }

    public HeightContext heights() {
        return heights;
    }

    /**
     * {@return the context of the calling thread}
     */
    public DensityContext context() {
        return contexts.get();
    }

    /**
     * {@return a new, empty context, for a caller that manages its own}
     */
    public synchronized DensityContext newContext() {
        return new DensityContext(cacheSlots, gridSlots);
    }

    /**
     * {@return the noise of an identifier, built once from the world seed}
     */
    public synchronized NoiseField noise(final Key id) {
        final NoiseField known = noises.get(id);
        if (known != null) {
            return known;
        }
        final NoiseField created = NoiseDefinition.parse(data.get(WorldgenData.Section.NOISE, id.asString())).create(random.named(id));
        noises.put(id, created);
        return created;
    }

    /**
     * {@return a function: a number, the identifier of a named function, or an inline definition}
     */
    public synchronized Density compile(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return constant(element.getAsFloat());
            }
            return named(element.getAsString());
        }
        return inline(element.getAsJsonObject());
    }

    /**
     * {@return a named function, compiled on first use}
     */
    public synchronized Density named(final String id) {
        final String key = WorldgenData.normalize(id);
        final Density known = named.get(key);
        if (known != null) {
            return known;
        }
        if (!compiling.add(key)) {
            throw new IllegalStateException("The density function " + key + " refers to itself");
        }
        try {
            final Density compiled = compile(data.get(WorldgenData.Section.DENSITY_FUNCTION, key));
            named.put(key, compiled);
            return compiled;
        } finally {
            compiling.remove(key);
        }
    }

    private static Density constant(final float value) {
        return new DensityNodes.Constant(value);
    }

    private static boolean isConstant(final Density function, final float value) {
        return function instanceof final DensityNodes.Constant constant && constant.value == value;
    }

    private static float valueOf(final Density function) {
        return ((DensityNodes.Constant) function).value;
    }

    private void warnOnce(final String what) {
        if (warned.add(what)) {
            LOGGER.warn("[worldgen] {}", what);
        }
    }

    /**
     * {@return the child, cached by column when it is worth it: it does not depend on the height but what uses it
     * does, and it does more than read a constant}
     */
    private Density adopt(final Density child, final int parentAxes) {
        if ((parentAxes & Density.Y) == 0 || (child.axes() & Density.Y) != 0 || child.isConstant()
                || child instanceof DensityNodes.Cache || child instanceof DensityNodes.Gradient) {
            return child;
        }
        return columnCaches.computeIfAbsent(child, value -> new DensityNodes.Cache(value, cacheSlots++));
    }

    private Density field(final JsonObject object, final String name) {
        return compile(object.get(name));
    }

    private Density optional(final JsonObject object, final String name) {
        return object.has(name) ? compile(object.get(name)) : constant(0.0F);
    }

    private Density inline(final JsonObject object) {
        final String type = Values.type(object);
        return switch (type) {
            case "constant" -> constant(object.get("value").getAsFloat());
            case "add", "sub", "mul", "div", "min", "max" ->
                    binary(type, field(object, "left"), field(object, "right"));
            case "abs", "square", "cube", "sqrt", "half_negative", "quarter_negative", "reciprocal", "negate",
                 "squeeze", "log", "sign" ->
                    unary(DensityNodes.UnaryKind.valueOf(type.toUpperCase(Locale.ROOT)), field(object, "input"));
            case "floor", "round", "ceil", "truncate" -> {
                final int multiple = object.has("multiple") ? object.get("multiple").getAsInt() : 1;
                final Density input = field(object, "input");
                final DensityNodes.RoundKind kind = DensityNodes.RoundKind.valueOf(type.toUpperCase(Locale.ROOT));
                yield input.isConstant() ? constant(kind.apply(valueOf(input), multiple)) : new DensityNodes.Round(kind, multiple, input);
            }
            case "pow" -> {
                final Density base = field(object, "base");
                final Density exponent = field(object, "exponent");
                if (exponent.isConstant()) {
                    final float power = valueOf(exponent);
                    if (power == 0.5F) {
                        yield unary(DensityNodes.UnaryKind.SQRT, base);
                    }
                    if (power == 2.0F) {
                        yield unary(DensityNodes.UnaryKind.SQUARE, base);
                    }
                    if (power == 3.0F) {
                        yield unary(DensityNodes.UnaryKind.CUBE, base);
                    }
                }
                final int axes = base.axes() | exponent.axes();
                yield new DensityNodes.Pow(adopt(base, axes), adopt(exponent, axes));
            }
            case "clamp" -> {
                final Density input = field(object, "input");
                final float low = object.get("min").getAsFloat();
                final float high = object.get("max").getAsFloat();
                yield input.isConstant() ? constant(Math.clamp(valueOf(input), low, high)) : new DensityNodes.Clamp(input, low, high);
            }
            case "lerp" -> lerp(field(object, "alpha"), field(object, "first"), field(object, "second"));
            case "range_choice" -> {
                final Density input = field(object, "input");
                final Density inRange = field(object, "when_in_range");
                final Density outOfRange = field(object, "when_out_of_range");
                final int axes = input.axes() | inRange.axes() | outOfRange.axes();
                yield new DensityNodes.RangeChoice(adopt(input, axes), object.get("min_inclusive").getAsFloat(),
                        object.get("max_exclusive").getAsFloat(), adopt(inRange, axes), adopt(outOfRange, axes));
            }
            case "interval_select" -> intervalSelect(object);
            case "spline" -> {
                final SplineDensity.Curve curve = curve(object.get("spline"));
                yield curve instanceof SplineDensity.Flat(final float value) ? constant(value) : new SplineDensity(curve);
            }
            case "gradient" -> gradient(object);
            case "slice" -> {
                final int axis = axis(object.get("axis").getAsString());
                yield new DensityNodes.Slice(field(object, "input"), axis, object.get("coordinate").getAsInt());
            }
            case "distance_to_point" -> {
                final JsonArray point = object.getAsJsonArray("point");
                final String metric = object.has("metric") ? object.get("metric").getAsString() : "euclidean";
                yield new DensityNodes.DistanceToPoint(DensityNodes.DistanceToPoint.Metric.valueOf(metric.toUpperCase(Locale.ROOT)),
                        point.get(0).getAsInt(), point.get(1).getAsInt(), point.get(2).getAsInt());
            }
            case "domain_warp" -> {
                final Density input = field(object, "input");
                yield new DensityNodes.DomainWarp(input, optional(object, "offset_x"), optional(object, "offset_y"), optional(object, "offset_z"));
            }
            case "noise" -> {
                final Density shiftX = optional(object, "shift_x");
                final Density shiftY = optional(object, "shift_y");
                final Density shiftZ = optional(object, "shift_z");
                final double xzScale = object.get("xz_scale").getAsDouble();
                final double yScale = object.get("y_scale").getAsDouble();
                final DensityNodes.Noise noise = new DensityNodes.Noise(noise(Key.key(object.get("noise").getAsString())), xzScale,
                        yScale, shiftX, shiftY, shiftZ);
                final int axes = noise.axes();
                yield new DensityNodes.Noise(noise.noise, xzScale, yScale, adopt(shiftX, axes), adopt(shiftY, axes), adopt(shiftZ, axes));
            }
            case "shift_a" ->
                    new DensityNodes.Shift(noise(Key.key(object.get("noise").getAsString())), DensityNodes.Shift.Kind.A);
            case "shift_b" ->
                    new DensityNodes.Shift(noise(Key.key(object.get("noise").getAsString())), DensityNodes.Shift.Kind.B);
            case "shift" ->
                    new DensityNodes.Shift(noise(Key.key(object.get("noise").getAsString())), DensityNodes.Shift.Kind.ALL);
            case "old_blended_noise" -> new DensityNodes.Blended(new BlendedTerrainNoise(random.named(TERRAIN_SEED),
                    object.get("xz_scale").getAsDouble(), object.get("y_scale").getAsDouble(),
                    object.get("xz_factor").getAsDouble(), object.get("y_factor").getAsDouble(),
                    object.get("smear_scale_multiplier").getAsDouble()));
            case "cache", "cache_once", "flat_cache", "cache_2d", "cache_all_in_cell", "interpolated" -> {
                final Density input = field(object, "input");
                if ("interpolated".equals(type)) {
                    final int xz = object.has("cell_size_xz") ? object.get("cell_size_xz").getAsInt() : 4;
                    final int y = object.has("cell_size_y") ? object.get("cell_size_y").getAsInt() : 8;
                    yield upscale(input, new UpscaleDensity.Axis(UpscaleDensity.Mode.INTERPOLATE_LINEAR, xz),
                            new UpscaleDensity.Axis(UpscaleDensity.Mode.INTERPOLATE_LINEAR, y),
                            new UpscaleDensity.Axis(UpscaleDensity.Mode.INTERPOLATE_LINEAR, xz));
                }
                yield input.isConstant() || input instanceof DensityNodes.Cache ? input : new DensityNodes.Cache(input, cacheSlots++);
            }
            case "upscale" -> upscale(field(object, "input"), upscaleAxis(object.getAsJsonObject("x")),
                    upscaleAxis(object.getAsJsonObject("y")), upscaleAxis(object.getAsJsonObject("z")));
            case "find_top_surface" -> {
                final Density density = field(object, "density");
                final Density upper = field(object, "upper_bound");
                yield new DensityNodes.FindTopSurface(density, adopt(upper, Density.XYZ),
                        object.get("lower_bound").getAsInt(), object.get("cell_height").getAsInt());
            }
            // nothing is blended with older terrain and no structure adapts it: these keep their neutral value
            case "blend_alpha" -> constant(1.0F);
            case "blend_offset", "beardifier" -> constant(0.0F);
            case "blend_density" -> field(object, "input");
            default -> {
                warnOnce("The density function type " + type + " is not supported, it is read as zero");
                yield constant(0.0F);
            }
        };
    }

    private Density upscale(final Density input, final UpscaleDensity.Axis x, final UpscaleDensity.Axis y, final UpscaleDensity.Axis z) {
        if (input.isConstant()) {
            return input;
        }
        return new UpscaleDensity(input, gridSlots++, x, y, z);
    }

    private static UpscaleDensity.Axis upscaleAxis(final JsonObject axis) {
        final UpscaleDensity.Mode mode = UpscaleDensity.Mode.valueOf(axis.get("mode").getAsString().toUpperCase(Locale.ROOT));
        return new UpscaleDensity.Axis(mode, axis.get("cell_size").getAsInt());
    }

    private static int axis(final String name) {
        return switch (name) {
            case "x" -> Density.X;
            case "y" -> Density.Y;
            case "z" -> Density.Z;
            default -> throw new IllegalArgumentException("Not an axis: " + name);
        };
    }

    private Density gradient(final JsonObject object) {
        final int axis = axis(object.has("axis") ? object.get("axis").getAsString() : "y");
        final String tilingName = object.has("tiling") ? object.get("tiling").getAsString() : "clamp_to_edge";
        final DensityNodes.Gradient.Tiling tiling = switch (tilingName) {
            case "repeat" -> DensityNodes.Gradient.Tiling.REPEAT;
            case "mirrored_repeat" -> DensityNodes.Gradient.Tiling.MIRROR;
            default -> DensityNodes.Gradient.Tiling.CLAMP;
        };
        final int from = heights.resolve(object.get("from_coordinate"));
        final int to = heights.resolve(object.get("to_coordinate"));
        return new DensityNodes.Gradient(axis, tiling, from, to, object.get("from_value").getAsFloat(), object.get("to_value").getAsFloat());
    }

    private Density lerp(final Density alpha, final Density first, final Density second) {
        if (alpha.isConstant()) {
            final float t = valueOf(alpha);
            if (t == 0.0F) {
                return first;
            }
            if (t == 1.0F) {
                return second;
            }
        }
        final int axes = alpha.axes() | first.axes() | second.axes();
        return new DensityNodes.Lerp(adopt(alpha, axes), adopt(first, axes), adopt(second, axes));
    }

    private Density intervalSelect(final JsonObject object) {
        final Density input = field(object, "input");
        final JsonArray thresholdArray = object.getAsJsonArray("thresholds");
        final JsonArray functionArray = object.getAsJsonArray("functions");
        if (functionArray.size() != thresholdArray.size() + 1) {
            throw new IllegalArgumentException("An interval_select needs one threshold fewer than functions");
        }
        final float[] thresholds = new float[thresholdArray.size()];
        for (int i = 0; i < thresholds.length; i++) {
            thresholds[i] = thresholdArray.get(i).getAsFloat();
        }
        final Density[] functions = new Density[functionArray.size()];
        int axes = input.axes();
        for (int i = 0; i < functions.length; i++) {
            functions[i] = compile(functionArray.get(i));
            axes |= functions[i].axes();
        }
        for (int i = 0; i < functions.length; i++) {
            functions[i] = adopt(functions[i], axes);
        }
        return new DensityNodes.IntervalSelect(adopt(input, axes), thresholds, functions);
    }

    private SplineDensity.Curve curve(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return new SplineDensity.Flat(element.getAsFloat());
        }
        final JsonObject object = element.getAsJsonObject();
        final Density coordinate = compile(object.get("coordinate"));
        final JsonArray points = object.getAsJsonArray("points");
        final float[] locations = new float[points.size()];
        final float[] slopes = new float[points.size()];
        final SplineDensity.Curve[] values = new SplineDensity.Curve[points.size()];
        for (int i = 0; i < points.size(); i++) {
            final JsonObject point = points.get(i).getAsJsonObject();
            locations[i] = point.get("location").getAsFloat();
            slopes[i] = point.has("derivative") ? point.get("derivative").getAsFloat() : 0.0F;
            values[i] = curve(point.get("value"));
        }
        return new SplineDensity.Multipoint(coordinate, locations, values, slopes);
    }

    private static Density unary(final DensityNodes.UnaryKind kind, final Density input) {
        if (input.isConstant()) {
            return constant(kind.apply(valueOf(input)));
        }
        return new DensityNodes.Unary(kind, input, kind.range(input.min(), input.max()));
    }

    private Density binary(final String type, final Density left, final Density right) {
        if (left.isConstant() && right.isConstant()) {
            final float a = valueOf(left);
            final float b = valueOf(right);
            return constant(switch (type) {
                case "add" -> a + b;
                case "sub" -> a - b;
                case "mul" -> a * b;
                // dividing by a constant multiplies by its reciprocal
                case "div" -> a * (1.0F / b);
                case "min" -> Math.min(a, b);
                default -> Math.max(a, b);
            });
        }
        final int axes = left.axes() | right.axes();
        final Density l = adopt(left, axes);
        final Density r = adopt(right, axes);
        return switch (type) {
            case "add" -> {
                if (left.isConstant()) {
                    yield isConstant(left, 0.0F) ? right : new DensityNodes.AddConstant(r, valueOf(left));
                }
                if (right.isConstant()) {
                    yield isConstant(right, 0.0F) ? left : new DensityNodes.AddConstant(l, valueOf(right));
                }
                yield new DensityNodes.Add(l, r);
            }
            case "sub" -> {
                if (left.isConstant()) {
                    yield new DensityNodes.ConstantMinus(valueOf(left), r);
                }
                if (right.isConstant()) {
                    yield new DensityNodes.AddConstant(l, -valueOf(right));
                }
                yield new DensityNodes.Sub(l, r);
            }
            case "mul" -> {
                if (left.isConstant()) {
                    yield new DensityNodes.MulConstant(r, valueOf(left));
                }
                if (right.isConstant()) {
                    yield new DensityNodes.MulConstant(l, valueOf(right));
                }
                yield new DensityNodes.Mul(l, r, DensityNodes.productRange(left.min(), left.max(), right.min(), right.max()));
            }
            case "div" -> {
                if (right.isConstant()) {
                    yield new DensityNodes.MulConstant(l, 1.0F / valueOf(right));
                }
                final float[] reciprocal = DensityNodes.UnaryKind.RECIPROCAL.range(right.min(), right.max());
                final float[] range = DensityNodes.productRange(left.min(), left.max(), reciprocal[0], reciprocal[1]);
                if (left.isConstant()) {
                    yield new DensityNodes.ConstantOver(valueOf(left), r, range[0], range[1]);
                }
                yield new DensityNodes.Div(l, r, range[0], range[1]);
            }
            case "min" -> {
                if (left.max() < right.min()) {
                    yield left;
                }
                if (right.max() < left.min()) {
                    yield right;
                }
                if (left.isConstant()) {
                    yield new DensityNodes.MinConstant(r, valueOf(left));
                }
                if (right.isConstant()) {
                    yield new DensityNodes.MinConstant(l, valueOf(right));
                }
                yield new DensityNodes.Min(l, r);
            }
            default -> {
                if (left.min() > right.max()) {
                    yield left;
                }
                if (right.min() > left.max()) {
                    yield right;
                }
                if (left.isConstant()) {
                    yield new DensityNodes.MaxConstant(r, valueOf(left));
                }
                if (right.isConstant()) {
                    yield new DensityNodes.MaxConstant(l, valueOf(right));
                }
                yield new DensityNodes.Max(l, r);
            }
        };
    }
}
