package fr.euphyllia.fidorial.server.world.generation.density;

/**
 * A cubic spline of another function: between two points the curve follows a cubic Hermite segment whose slopes
 * are given at the points, outside the points it goes on along a straight line. A point may hold a spline of its
 * own instead of a value, which makes the curve depend on several functions.
 */
final class SplineDensity extends Density {

    private final Curve curve;

    SplineDensity(final Curve curve) {
        super(curve.low(), curve.high());
        this.curve = curve;
    }

    @Override
    public float compute(final DensityContext context, final int x, final int y, final int z) {
        return curve.evaluate(context, x, y, z);
    }

    @Override
    public int axes() {
        return curve.axes();
    }

    /**
     * A spline or a constant value.
     */
    sealed interface Curve permits Flat, Multipoint {

        float evaluate(DensityContext context, int x, int y, int z);

        float low();

        float high();

        int axes();
    }

    record Flat(float value) implements Curve {
        @Override
        public float evaluate(final DensityContext context, final int x, final int y, final int z) {
            return value;
        }

        @Override
        public float low() {
            return value;
        }

        @Override
        public float high() {
            return value;
        }

        @Override
        public int axes() {
            return 0;
        }
    }

    static final class Multipoint implements Curve {
        private final Density coordinate;
        private final float[] locations;
        private final Curve[] values;
        private final float[] slopes;
        private final float low;
        private final float high;

        Multipoint(final Density coordinate, final float[] locations, final Curve[] values, final float[] slopes) {
            if (locations.length == 0 || locations.length != values.length || locations.length != slopes.length) {
                throw new IllegalArgumentException("A spline needs as many locations, values and derivatives, and at least one point");
            }
            this.coordinate = coordinate;
            this.locations = locations;
            this.values = values;
            this.slopes = slopes;
            final float[] bounds = bounds();
            this.low = bounds[0];
            this.high = bounds[1];
        }

        /**
         * {@return a range sure to hold the curve}: the values of the points, the straight extensions at both ends
         * over the range of the coordinate, and around each segment the most a cubic term can bulge.
         */
        private float[] bounds() {
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            for (final Curve value : values) {
                min = Math.min(min, value.low());
                max = Math.max(max, value.high());
            }
            final int last = locations.length - 1;
            final float inputMin = coordinate.min();
            final float inputMax = coordinate.max();
            if (inputMin < locations[0]) {
                for (final float edge : new float[]{values[0].low(), values[0].high()}) {
                    final float extended = edge + slopes[0] * (inputMin - locations[0]);
                    min = Math.min(min, extended);
                    max = Math.max(max, extended);
                }
            }
            if (inputMax > locations[last]) {
                for (final float edge : new float[]{values[last].low(), values[last].high()}) {
                    final float extended = edge + slopes[last] * (inputMax - locations[last]);
                    min = Math.min(min, extended);
                    max = Math.max(max, extended);
                }
            }
            for (int i = 0; i < last; i++) {
                final float dx = locations[i + 1] - locations[i];
                final float spread = Math.max(Math.abs(values[i + 1].high() - values[i].low()),
                        Math.abs(values[i].high() - values[i + 1].low()));
                final float bulge = 0.25F * (Math.max(Math.abs(slopes[i]), Math.abs(slopes[i + 1])) * dx + spread);
                min = Math.min(min, Math.min(values[i].low(), values[i + 1].low()) - bulge);
                max = Math.max(max, Math.max(values[i].high(), values[i + 1].high()) + bulge);
            }
            if (Float.isNaN(min) || Float.isNaN(max)) {
                return new float[]{Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY};
            }
            return new float[]{min, max};
        }

        @Override
        public float evaluate(final DensityContext context, final int x, final int y, final int z) {
            final float input = coordinate.compute(context, x, y, z);
            if (Float.isNaN(input)) {
                return Float.NaN;
            }
            final int last = locations.length - 1;
            if (input <= locations[0]) {
                return extend(input, values[0].evaluate(context, x, y, z), 0);
            }
            if (input >= locations[last]) {
                return extend(input, values[last].evaluate(context, x, y, z), last);
            }
            int upper = 1;
            while (upper < last && !(input < locations[upper])) {
                upper++;
            }
            final int lower = upper - 1;
            final float x1 = locations[lower];
            final float x2 = locations[upper];
            final float y1 = values[lower].evaluate(context, x, y, z);
            final float y2 = values[upper].evaluate(context, x, y, z);
            final float dx = x2 - x1;
            final float dy = y2 - y1;
            final float t = (input - x1) / dx;
            final float a = slopes[lower] * dx - dy;
            final float b = -slopes[upper] * dx + dy;
            return (y1 + t * (y2 - y1)) + t * (1.0F - t) * (a + t * (b - a));
        }

        private float extend(final float input, final float value, final int index) {
            final float slope = slopes[index];
            return slope == 0.0F ? value : value + slope * (input - locations[index]);
        }

        @Override
        public float low() {
            return low;
        }

        @Override
        public float high() {
            return high;
        }

        @Override
        public int axes() {
            int axes = coordinate.axes();
            for (final Curve value : values) {
                axes |= value.axes();
            }
            return axes;
        }
    }
}
