package fr.euphyllia.fidorial.server.world.generation.density;

import fr.euphyllia.fidorial.server.world.generation.noise.BlendedTerrainNoise;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseField;

/**
 * The building blocks of compiled density functions. Each node is small: the compiler picks the most specific
 * one for what the data pack wrote, folding constants where it can.
 */
final class DensityNodes {

    private static final float INFINITY = Float.POSITIVE_INFINITY;

    private DensityNodes() {
    }

    static float lowest(final float... values) {
        float result = INFINITY;
        for (final float value : values) {
            result = Math.min(result, value);
        }
        return result;
    }

    static float highest(final float... values) {
        float result = -INFINITY;
        for (final float value : values) {
            result = Math.max(result, value);
        }
        return result;
    }

    /**
     * {@return the product range, or the whole line when one bound is infinite and another zero}
     */
    static float[] productRange(final float aMin, final float aMax, final float bMin, final float bMax) {
        final float p1 = aMin * bMin;
        final float p2 = aMin * bMax;
        final float p3 = aMax * bMin;
        final float p4 = aMax * bMax;
        if (Float.isNaN(p1) || Float.isNaN(p2) || Float.isNaN(p3) || Float.isNaN(p4)) {
            return new float[]{-INFINITY, INFINITY};
        }
        return new float[]{lowest(p1, p2, p3, p4), highest(p1, p2, p3, p4)};
    }

    static final class Constant extends Density {
        final float value;

        Constant(final float value) {
            super(value, value);
            this.value = value;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return value;
        }

        @Override
        public int axes() {
            return 0;
        }

        @Override
        public boolean isConstant() {
            return true;
        }
    }

    abstract static class Binary extends Density {
        final Density left;
        final Density right;

        Binary(final Density left, final Density right, final float min, final float max) {
            super(min, max);
            this.left = left;
            this.right = right;
        }

        @Override
        public int axes() {
            return left.axes() | right.axes();
        }
    }

    static final class Add extends Binary {
        Add(final Density left, final Density right) {
            super(left, right, left.min() + right.min(), left.max() + right.max());
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return left.compute(context, x, y, z) + right.compute(context, x, y, z);
        }
    }

    static final class AddConstant extends Density {
        final Density input;
        final float value;

        AddConstant(final Density input, final float value) {
            super(input.min() + value, input.max() + value);
            this.input = input;
            this.value = value;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return input.compute(context, x, y, z) + value;
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    static final class Sub extends Binary {
        Sub(final Density left, final Density right) {
            super(left, right, left.min() - right.max(), left.max() - right.min());
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return left.compute(context, x, y, z) - right.compute(context, x, y, z);
        }
    }

    /**
     * A constant minus a function.
     */
    static final class ConstantMinus extends Density {
        final float value;
        final Density input;

        ConstantMinus(final float value, final Density input) {
            super(value - input.max(), value - input.min());
            this.value = value;
            this.input = input;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return value - input.compute(context, x, y, z);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    static final class Mul extends Binary {
        Mul(final Density left, final Density right, final float[] range) {
            super(left, right, range[0], range[1]);
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return left.compute(context, x, y, z) * right.compute(context, x, y, z);
        }
    }

    static final class MulConstant extends Density {
        final Density input;
        final float value;

        MulConstant(final Density input, final float value) {
            super(Math.min(input.min() * value, input.max() * value), Math.max(input.min() * value, input.max() * value));
            this.input = input;
            this.value = value;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return input.compute(context, x, y, z) * value;
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    static final class Div extends Binary {
        Div(final Density left, final Density right, final float min, final float max) {
            super(left, right, min, max);
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return left.compute(context, x, y, z) / right.compute(context, x, y, z);
        }
    }

    /**
     * A constant divided by a function.
     */
    static final class ConstantOver extends Density {
        final float value;
        final Density input;

        ConstantOver(final float value, final Density input, final float min, final float max) {
            super(min, max);
            this.value = value;
            this.input = input;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return value / input.compute(context, x, y, z);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    /**
     * The smaller side; the right side is not computed when the left one is already at or below anything it
     * could return.
     */
    static final class Min extends Binary {
        Min(final Density left, final Density right) {
            super(left, right, Math.min(left.min(), right.min()), Math.min(left.max(), right.max()));
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float first = left.compute(context, x, y, z);
            return first <= right.min() ? first : Math.min(first, right.compute(context, x, y, z));
        }
    }

    static final class MinConstant extends Density {
        final Density input;
        final float value;

        MinConstant(final Density input, final float value) {
            super(Math.min(input.min(), value), Math.min(input.max(), value));
            this.input = input;
            this.value = value;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return Math.min(input.compute(context, x, y, z), value);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    static final class Max extends Binary {
        Max(final Density left, final Density right) {
            super(left, right, Math.max(left.min(), right.min()), Math.max(left.max(), right.max()));
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float first = left.compute(context, x, y, z);
            return first >= right.max() ? first : Math.max(first, right.compute(context, x, y, z));
        }
    }

    static final class MaxConstant extends Density {
        final Density input;
        final float value;

        MaxConstant(final Density input, final float value) {
            super(Math.max(input.min(), value), Math.max(input.max(), value));
            this.input = input;
            this.value = value;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return Math.max(input.compute(context, x, y, z), value);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    /**
     * A function applied to the value of another, element by element.
     */
    enum UnaryKind {
        ABS, SQUARE, CUBE, SQRT, HALF_NEGATIVE, QUARTER_NEGATIVE, RECIPROCAL, NEGATE, SQUEEZE, LOG, SIGN;

        float apply(final float value) {
            return switch (this) {
                case ABS -> Math.abs(value);
                case SQUARE -> value * value;
                case CUBE -> value * value * value;
                case SQRT -> (float) Math.sqrt(value);
                case HALF_NEGATIVE -> value > 0.0F ? value : value * 0.5F;
                case QUARTER_NEGATIVE -> value > 0.0F ? value : value * 0.25F;
                case RECIPROCAL -> 1.0F / value;
                case NEGATE -> -value;
                case SQUEEZE -> {
                    final float clamped = Math.clamp(value, -1.0F, 1.0F);
                    yield clamped / 2.0F - clamped * clamped * clamped / 24.0F;
                }
                case LOG -> (float) Math.log(value);
                case SIGN -> Math.signum(value);
            };
        }

        float[] range(final float min, final float max) {
            return switch (this) {
                case ABS -> min >= 0.0F ? new float[]{min, max}
                        : max <= 0.0F ? new float[]{-max, -min} : new float[]{0.0F, Math.max(-min, max)};
                case SQUARE -> min >= 0.0F ? new float[]{min * min, max * max}
                        : max <= 0.0F ? new float[]{max * max, min * min} : new float[]{0.0F, Math.max(min * min, max * max)};
                case RECIPROCAL -> min > 0.0F || max < 0.0F
                        ? new float[]{1.0F / max, 1.0F / min} : new float[]{-INFINITY, INFINITY};
                case NEGATE -> new float[]{-max, -min};
                case SIGN -> new float[]{Math.signum(min), Math.signum(max)};
                case SQRT -> new float[]{min <= 0.0F ? 0.0F : apply(min), max < 0.0F ? 0.0F : apply(max)};
                case LOG -> new float[]{min <= 0.0F ? -INFINITY : apply(min), max <= 0.0F ? -INFINITY : apply(max)};
                // the others never decrease
                default -> new float[]{apply(min), apply(max)};
            };
        }
    }

    static final class Unary extends Density {
        final UnaryKind kind;
        final Density input;

        Unary(final UnaryKind kind, final Density input, final float[] range) {
            super(range[0], range[1]);
            this.kind = kind;
            this.input = input;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return kind.apply(input.compute(context, x, y, z));
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    /**
     * Rounds the value to a multiple of an integer.
     */
    enum RoundKind {
        FLOOR, ROUND, CEIL, TRUNCATE;

        float apply(final float value, final int multiple) {
            final float steps = value / multiple;
            final float rounded = switch (this) {
                case FLOOR -> (float) Math.floor(steps);
                case ROUND -> (float) Math.rint(steps);
                case CEIL -> (float) Math.ceil(steps);
                case TRUNCATE -> steps < 0.0F ? (float) Math.ceil(steps) : (float) Math.floor(steps);
            };
            return rounded * multiple;
        }
    }

    static final class Round extends Density {
        final RoundKind kind;
        final int multiple;
        final Density input;

        Round(final RoundKind kind, final int multiple, final Density input) {
            super(Math.min(kind.apply(input.min(), multiple), kind.apply(input.max(), multiple)),
                    Math.max(kind.apply(input.min(), multiple), kind.apply(input.max(), multiple)));
            this.kind = kind;
            this.multiple = multiple;
            this.input = input;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return kind.apply(input.compute(context, x, y, z), multiple);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    static final class Pow extends Binary {
        Pow(final Density base, final Density exponent) {
            super(base, exponent, -INFINITY, INFINITY);
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return (float) Math.pow(left.compute(context, x, y, z), right.compute(context, x, y, z));
        }
    }

    static final class Clamp extends Density {
        final Density input;
        final float low;
        final float high;

        Clamp(final Density input, final float low, final float high) {
            super(Math.clamp(input.min(), low, high), Math.clamp(input.max(), low, high));
            this.input = input;
            this.low = low;
            this.high = high;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return Math.clamp(input.compute(context, x, y, z), low, high);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    /**
     * Blends two functions; at the ends of the blend only one of them is computed.
     */
    static final class Lerp extends Density {
        final Density alpha;
        final Density first;
        final Density second;

        Lerp(final Density alpha, final Density first, final Density second) {
            super(lerpRange(alpha, first, second)[0], lerpRange(alpha, first, second)[1]);
            this.alpha = alpha;
            this.first = first;
            this.second = second;
        }

        static float[] lerpRange(final Density alpha, final Density first, final Density second) {
            float low = INFINITY;
            float high = -INFINITY;
            for (final float t : new float[]{alpha.min(), alpha.max()}) {
                for (final float a : new float[]{first.min(), first.max()}) {
                    for (final float b : new float[]{second.min(), second.max()}) {
                        final float value = a + t * (b - a);
                        if (Float.isNaN(value)) {
                            return new float[]{-INFINITY, INFINITY};
                        }
                        low = Math.min(low, value);
                        high = Math.max(high, value);
                    }
                }
            }
            return new float[]{low, high};
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float t = alpha.compute(context, x, y, z);
            if (t == 0.0F) {
                return first.compute(context, x, y, z);
            }
            if (t == 1.0F) {
                return second.compute(context, x, y, z);
            }
            final float from = first.compute(context, x, y, z);
            return from + t * (second.compute(context, x, y, z) - from);
        }

        @Override
        public int axes() {
            return alpha.axes() | first.axes() | second.axes();
        }
    }

    /**
     * Picks one of two functions depending on whether a third one falls in a range.
     */
    static final class RangeChoice extends Density {
        final Density input;
        final float minInclusive;
        final float maxExclusive;
        final Density inRange;
        final Density outOfRange;

        RangeChoice(final Density input, final float minInclusive, final float maxExclusive,
                    final Density inRange, final Density outOfRange) {
            super(Math.min(inRange.min(), outOfRange.min()), Math.max(inRange.max(), outOfRange.max()));
            this.input = input;
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
            this.inRange = inRange;
            this.outOfRange = outOfRange;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float value = input.compute(context, x, y, z);
            return value >= minInclusive && value < maxExclusive
                    ? inRange.compute(context, x, y, z)
                    : outOfRange.compute(context, x, y, z);
        }

        @Override
        public int axes() {
            return input.axes() | inRange.axes() | outOfRange.axes();
        }
    }

    /**
     * Picks a function by the interval the input falls in: the first function below the first threshold, the
     * last one above the last threshold.
     */
    static final class IntervalSelect extends Density {
        final Density input;
        final float[] thresholds;
        final Density[] functions;

        IntervalSelect(final Density input, final float[] thresholds, final Density[] functions) {
            super(rangeOf(functions)[0], rangeOf(functions)[1]);
            this.input = input;
            this.thresholds = thresholds;
            this.functions = functions;
        }

        private static float[] rangeOf(final Density[] functions) {
            float low = INFINITY;
            float high = -INFINITY;
            for (final Density function : functions) {
                low = Math.min(low, function.min());
                high = Math.max(high, function.max());
            }
            return new float[]{low, high};
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float value = input.compute(context, x, y, z);
            for (int i = 0; i < thresholds.length; i++) {
                if (value < thresholds[i]) {
                    return functions[i].compute(context, x, y, z);
                }
            }
            return functions[functions.length - 1].compute(context, x, y, z);
        }

        @Override
        public int axes() {
            int axes = input.axes();
            for (final Density function : functions) {
                axes |= function.axes();
            }
            return axes;
        }
    }

    /**
     * A linear ramp along one axis, between two coordinates.
     */
    static final class Gradient extends Density {
        enum Tiling {CLAMP, REPEAT, MIRROR}

        final int axis;
        final Tiling tiling;
        final int from;
        final int lowest;
        final int highest;
        final int span;
        final float fromValue;
        final float step;

        Gradient(final int axis, final Tiling tiling, final int from, final int to, final float fromValue, final float toValue) {
            super(Math.min(fromValue, toValue), Math.max(fromValue, toValue));
            final int resolvedTo = to == from ? from + 1 : to;
            this.axis = axis;
            this.tiling = tiling;
            this.from = from;
            this.lowest = Math.min(from, resolvedTo);
            this.highest = Math.max(from, resolvedTo);
            this.span = resolvedTo - from;
            this.fromValue = fromValue;
            this.step = (toValue - fromValue) / span;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final int coordinate = axis == X ? x : axis == Y ? y : z;
            return switch (tiling) {
                case CLAMP -> fromValue + (Math.clamp(coordinate, lowest, highest) - from) * step;
                case REPEAT -> fromValue + Math.floorMod(coordinate - from, span) * step;
                case MIRROR -> {
                    final int relative = coordinate - from;
                    final int tile = Math.floorDiv(relative, span);
                    final int local = relative - tile * span;
                    yield (tile & 1) == 0 ? fromValue + local * step : fromValue + (span - local) * step;
                }
            };
        }

        @Override
        public int axes() {
            return axis;
        }
    }

    /**
     * Reads another function with one coordinate fixed.
     */
    static final class Slice extends Density {
        final Density input;
        final int axis;
        final int coordinate;

        Slice(final Density input, final int axis, final int coordinate) {
            super(input.min(), input.max());
            this.input = input;
            this.axis = axis;
            this.coordinate = coordinate;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return switch (axis) {
                case X -> input.compute(context, coordinate, y, z);
                case Y -> input.compute(context, x, coordinate, z);
                default -> input.compute(context, x, y, coordinate);
            };
        }

        @Override
        public int axes() {
            return input.axes() & ~axis;
        }
    }

    /**
     * The distance from a block to a fixed point.
     */
    static final class DistanceToPoint extends Density {
        enum Metric {EUCLIDEAN, EUCLIDEAN_SQUARED, MANHATTAN, CHEBYSHEV}

        final Metric metric;
        final int px;
        final int py;
        final int pz;

        DistanceToPoint(final Metric metric, final int px, final int py, final int pz) {
            super(0.0F, INFINITY);
            this.metric = metric;
            this.px = px;
            this.py = py;
            this.pz = pz;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float dx = px - x;
            final float dy = py - y;
            final float dz = pz - z;
            return switch (metric) {
                case EUCLIDEAN -> (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                case EUCLIDEAN_SQUARED -> dx * dx + dy * dy + dz * dz;
                case MANHATTAN -> Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                case CHEBYSHEV -> Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
            };
        }
    }

    /**
     * Reads another function at a position moved by three offset functions, rounded down to blocks.
     */
    static final class DomainWarp extends Density {
        final Density input;
        final Density offsetX;
        final Density offsetY;
        final Density offsetZ;

        DomainWarp(final Density input, final Density offsetX, final Density offsetY, final Density offsetZ) {
            super(input.min(), input.max());
            this.input = input;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final int wx = x + (int) Math.floor(offsetX.compute(context, x, y, z));
            final int wy = (input.axes() & Y) == 0 ? y : y + (int) Math.floor(offsetY.compute(context, x, y, z));
            final int wz = z + (int) Math.floor(offsetZ.compute(context, x, y, z));
            return input.compute(context, wx, wy, wz);
        }

        @Override
        public int axes() {
            return input.axes() | offsetX.axes() | offsetY.axes() | offsetZ.axes();
        }
    }

    /**
     * A named noise read at scaled block coordinates, optionally moved by shift functions.
     */
    static final class Noise extends Density {
        final NoiseField noise;
        final double xzScale;
        final double yScale;
        final Density shiftX;
        final Density shiftY;
        final Density shiftZ;
        final boolean shifted;

        Noise(final NoiseField noise, final double xzScale, final double yScale,
              final Density shiftX, final Density shiftY, final Density shiftZ) {
            super(-noise.bound(), noise.bound());
            this.noise = noise;
            this.xzScale = xzScale;
            this.yScale = yScale;
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.shiftZ = shiftZ;
            this.shifted = !(isZero(shiftX) && isZero(shiftY) && isZero(shiftZ));
        }

        private static boolean isZero(final Density function) {
            return function instanceof final Constant constant && constant.value == 0.0F;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            if (!shifted) {
                return noise.sample(x * xzScale, y * yScale, z * xzScale);
            }
            final double sx = x * xzScale + shiftX.compute(context, x, y, z);
            final double sy = y * yScale + shiftY.compute(context, x, y, z);
            final double sz = z * xzScale + shiftZ.compute(context, x, y, z);
            return noise.sample(sx, sy, sz);
        }

        @Override
        public int axes() {
            int axes = XYZ;
            if (yScale == 0.0) {
                axes &= ~Y;
            }
            if (xzScale == 0.0) {
                axes &= ~(X | Z);
            }
            return axes | shiftX.axes() | shiftY.axes() | shiftZ.axes();
        }
    }

    /**
     * The domain warps of the climate noises: a noise read at a quarter of the block coordinates, scaled by
     * four.
     */
    static final class Shift extends Density {
        enum Kind {A, B, ALL}

        final NoiseField noise;
        final Kind kind;

        Shift(final NoiseField noise, final Kind kind) {
            super(-noise.bound() * 4.0F, noise.bound() * 4.0F);
            this.noise = noise;
            this.kind = kind;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return switch (kind) {
                case A -> noise.sample(x * 0.25, y * 0.0, z * 0.25) * 4.0F;
                case B -> noise.sample(z * 0.25, x * 0.25, 0.0) * 4.0F;
                case ALL -> noise.sample(x * 0.25, y * 0.25, z * 0.25) * 4.0F;
            };
        }

        @Override
        public int axes() {
            return kind == Kind.ALL ? XYZ : X | Z;
        }
    }

    static final class Blended extends Density {
        final BlendedTerrainNoise noise;

        Blended(final BlendedTerrainNoise noise) {
            super(-noise.bound(), noise.bound());
            this.noise = noise;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            return noise.sample(x, y, z);
        }
    }

    /**
     * Remembers the last value computed for the same block, or the same column when the input does not depend on
     * the height.
     */
    static final class Cache extends Density {
        final Density input;
        final int slot;
        final boolean column;

        Cache(final Density input, final int slot) {
            super(input.min(), input.max());
            this.input = input;
            this.slot = slot;
            this.column = (input.axes() & Y) == 0;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final long key = column ? DensityContext.columnKey(x, z) : DensityContext.blockKey(x, y, z);
            return context.cached(slot, key, input, x, y, z);
        }

        @Override
        public int axes() {
            return input.axes();
        }
    }

    /**
     * Steps down a column from an upper bound, a cell at a time, and returns the first height where a density is
     * positive. The answer does not depend on the height it is asked at.
     */
    static final class FindTopSurface extends Density {
        final Density density;
        final Density upperBound;
        final int lowerBound;
        final int cellHeight;

        FindTopSurface(final Density density, final Density upperBound, final int lowerBound, final int cellHeight) {
            super(lowerBound, Math.max(lowerBound, upperBound.max()));
            this.density = density;
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
            this.cellHeight = cellHeight;
        }

        @Override
        public float compute(final DensityContext context, final int x, final int y, final int z) {
            final float upper = upperBound.compute(context, x, 0, z);
            final int top = (int) Math.floor(upper / cellHeight) * cellHeight;
            if (top <= lowerBound) {
                return lowerBound;
            }
            for (int probe = top; probe >= lowerBound; probe -= cellHeight) {
                if (density.compute(context, x, probe, z) > 0.0F) {
                    return probe;
                }
            }
            return lowerBound;
        }

        @Override
        public int axes() {
            return (density.axes() | upperBound.axes()) & ~Y;
        }
    }
}
