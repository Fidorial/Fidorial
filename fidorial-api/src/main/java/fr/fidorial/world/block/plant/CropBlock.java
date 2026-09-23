package fr.fidorial.world.block.plant;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockDrop;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * A plant that sits on a soil block and climbs through growth stages carried in an
 * {@code age} property: wheat, carrots, potatoes, beetroots, nether wart — or a
 * plugin's own rice.
 *
 * @since 0.1.0
 */
public class CropBlock implements BlockBehaviour {

    /**
     * The block property crops carry their growth stage in, unless they say otherwise.
     *
     * @since 0.1.0
     */
    public static final String DEFAULT_AGE_PROPERTY = "age";

    /**
     * The light level vanilla crops need to keep growing.
     *
     * @since 0.1.0
     */
    public static final int DEFAULT_MIN_LIGHT = 9;

    private final Key key;
    private final String ageProperty;
    private final int maxAge;
    private final Set<Key> soils;
    private final int minLight;
    private final Growth growth;
    private final List<BlockDrop> ripeDrops;
    private final List<BlockDrop> immatureDrops;
    private final Sound.@Nullable Type placeSound;

    protected CropBlock(final Builder builder) {
        this.key = builder.key;
        this.ageProperty = builder.ageProperty;
        this.maxAge = builder.resolveMaxAge();
        this.soils = Set.copyOf(builder.soils);
        this.minLight = builder.minLight;
        this.growth = builder.growth;
        this.ripeDrops = List.copyOf(builder.ripeDrops);
        this.immatureDrops = List.copyOf(builder.immatureDrops);
        this.placeSound = builder.placeSound;
    }

    /**
     * @param block the block the crop grows as
     * @return a builder defaulting to farmland soil, the vanilla growth speed, a
     * light requirement of {@value #DEFAULT_MIN_LIGHT}, and the {@code age} range
     * the block type declares
     * @since 0.1.0
     */
    public static Builder builder(final Key block) {
        return new Builder(block);
    }

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(key), () -> "Unknown block type " + key.asString());
    }

    @Override
    public Key key() {
        return key;
    }

    /**
     * @return the block property carrying the growth stage
     * @since 0.1.0
     */
    public String ageProperty() {
        return ageProperty;
    }

    /**
     * @return the growth stage at which the crop is ripe
     * @since 0.1.0
     */
    public int maxAge() {
        return maxAge;
    }

    /**
     * @return the blocks this crop can sit on
     * @since 0.1.0
     */
    public Set<Key> soils() {
        return soils;
    }

    /**
     * @param data a state of this crop
     * @return its growth stage, {@code 0} when unreadable
     * @since 0.1.0
     */
    public int age(final BlockData data) {
        final String raw = data.get(ageProperty);
        if (raw == null) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /**
     * @param data a state of this crop
     * @param age  the growth stage wanted, clamped to the valid range
     * @return that state at that growth stage
     * @since 0.1.0
     */
    public BlockData withAge(final BlockData data, final int age) {
        return data.with(ageProperty, Integer.toString(Math.clamp(age, 0, maxAge)));
    }

    /**
     * @param data a state of this crop
     * @return {@code true} when it is fully grown
     * @since 0.1.0
     */
    public boolean isRipe(final BlockData data) {
        return age(data) >= maxAge;
    }

    @Override
    public boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        return soils.contains(world.blockAt(pos.offset(0, -1, 0)).key());
    }

    @Override
    public boolean isRandomlyTicking(final BlockData data) {
        return !isRipe(data);
    }

    @Override
    public void randomTick(final BlockData data, final BlockAccess world, final BlockPos pos, final RandomGenerator random) {
        if (isRipe(data)) {
            return;
        }
        if (minLight > 0 && world.lightLevel(pos) < minLight) {
            return;
        }
        if (random.nextDouble() >= growth.chance(this, data, world, pos)) {
            return;
        }
        world.setBlock(pos, withAge(data, age(data) + 1));
    }

    @Override
    public List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        final List<BlockDrop> table = isRipe(data) ? ripeDrops : immatureDrops;
        final List<ItemStack> result = new ArrayList<>(table.size());
        for (final BlockDrop drop : table) {
            final ItemStack rolled = drop.roll(random);
            if (!rolled.isEmpty()) {
                result.add(rolled);
            }
        }
        return result;
    }

    @Override
    public boolean breaksInstantly(final BlockData data) {
        return true;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return 0;
    }

    @Override
    public Sound.@Nullable Type placeSound(final BlockData data) {
        return placeSound;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + key.asString() + "]";
    }

    /**
     * How likely a crop is to move up one stage on a random tick.
     *
     * @since 0.1.0
     */
    @FunctionalInterface
    public interface Growth {

        /**
         * @param crop  the crop being ticked
         * @param data  its current state
         * @param world where it is
         * @param pos   its position
         * @return a probability between {@code 0} and {@code 1}
         * @since 0.1.0
         */
        double chance(CropBlock crop, BlockData data, BlockAccess world, BlockPos pos);

        /**
         * The same odds on every tick.
         *
         * @param chance a probability between {@code 0} and {@code 1}
         * @return a growth rule
         * @since 0.1.0
         */
        static Growth fixed(final double chance) {
            Preconditions.checkArgument(chance >= 0 && chance <= 1, "chance must be within [0, 1], got %s", chance);
            return (_, _, _, _) -> chance;
        }

        /**
         * Vanilla's farmland rule: faster on wet soil, faster with soil all around,
         * slower when the same crop is planted in tight rows.
         *
         * @return a growth rule
         * @since 0.1.0
         */
        static Growth vanilla() {
            return (crop, data, world, pos) -> 1.0 / ((int) (25.0f / vanillaSpeed(crop, data, world, pos)) + 1);
        }

        /**
         * @param factor what to multiply the odds by
         * @return this rule, made faster or slower
         * @since 0.1.0
         */
        default Growth scaled(final double factor) {
            return (crop, data, world, pos) -> Math.min(1.0, chance(crop, data, world, pos) * factor);
        }

        private static float vanillaSpeed(final CropBlock crop, final BlockData data, final BlockAccess world, final BlockPos pos) {
            float speed = 1.0f;
            final BlockPos below = pos.offset(0, -1, 0);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    final BlockData soil = world.blockAt(below.offset(dx, 0, dz));
                    float bonus = 0.0f;
                    if (crop.soils().contains(soil.key())) {
                        bonus = isMoist(soil) ? 3.0f : 1.0f;
                    }
                    if (dx != 0 || dz != 0) {
                        bonus /= 4.0f;
                    }
                    speed += bonus;
                }
            }

            final Key self = data.key();
            final boolean westEast = same(world, pos.offset(-1, 0, 0), self) || same(world, pos.offset(1, 0, 0), self);
            final boolean northSouth = same(world, pos.offset(0, 0, -1), self) || same(world, pos.offset(0, 0, 1), self);
            if (westEast && northSouth) {
                speed /= 2.0f;
            } else if (same(world, pos.offset(-1, 0, -1), self) || same(world, pos.offset(1, 0, -1), self)
                    || same(world, pos.offset(1, 0, 1), self) || same(world, pos.offset(-1, 0, 1), self)) {
                speed /= 2.0f;
            }
            return speed;
        }

        private static boolean isMoist(final BlockData soil) {
            if (!soil.type().hasProperty("moisture")) {
                return true;
            }
            return !"0".equals(soil.get("moisture"));
        }

        private static boolean same(final BlockAccess world, final BlockPos pos, final Key block) {
            return world.blockAt(pos).key().equals(block);
        }
    }

    /**
     * Assembles a {@link CropBlock}.
     *
     * @since 0.1.0
     */
    public static class Builder {

        private final Key key;
        private String ageProperty = DEFAULT_AGE_PROPERTY;
        private int maxAge = -1;
        private Set<Key> soils = Set.of(BlockTypeKeys.FARMLAND.key());
        private int minLight = DEFAULT_MIN_LIGHT;
        private Growth growth = Growth.vanilla();
        private List<BlockDrop> ripeDrops = List.of();
        private List<BlockDrop> immatureDrops = List.of();
        private Sound.@Nullable Type placeSound = SoundEvents.CROP_PLANT;

        protected Builder(final Key key) {
            this.key = Objects.requireNonNull(key, "key");
        }

        /**
         * @param ageProperty the block property carrying the growth stage
         * @return this builder
         * @since 0.1.0
         */
        public Builder ageProperty(final String ageProperty) {
            this.ageProperty = Objects.requireNonNull(ageProperty, "ageProperty");
            return this;
        }

        /**
         * Only needed when the block type does not list its stages itself.
         *
         * @param maxAge the growth stage at which the crop is ripe
         * @return this builder
         * @since 0.1.0
         */
        public Builder maxAge(final int maxAge) {
            Preconditions.checkArgument(maxAge >= 1, "maxAge must be at least 1, got %s", maxAge);
            this.maxAge = maxAge;
            return this;
        }

        /**
         * @param soils the blocks the crop can sit on; defaults to farmland alone
         * @return this builder
         * @since 0.1.0
         */
        public Builder soils(final Set<Key> soils) {
            Preconditions.checkArgument(!soils.isEmpty(), "A crop needs at least one soil");
            this.soils = Set.copyOf(soils);
            return this;
        }

        /**
         * @param minLight the light level the crop needs to grow; {@code 0} to let it grow in the dark
         * @return this builder
         * @since 0.1.0
         */
        public Builder minLight(final int minLight) {
            this.minLight = Math.clamp(minLight, 0, 15);
            return this;
        }

        /**
         * @param growth how likely the crop is to grow on each random tick
         * @return this builder
         * @since 0.1.0
         */
        public Builder growth(final Growth growth) {
            this.growth = Objects.requireNonNull(growth, "growth");
            return this;
        }

        /**
         * @param ripeDrops what breaking the ripe crop gives back
         * @return this builder
         * @since 0.1.0
         */
        public Builder ripeDrops(final List<BlockDrop> ripeDrops) {
            this.ripeDrops = List.copyOf(ripeDrops);
            return this;
        }

        /**
         * @param immatureDrops what breaking the crop early gives back
         * @return this builder
         * @since 0.1.0
         */
        public Builder immatureDrops(final List<BlockDrop> immatureDrops) {
            this.immatureDrops = List.copyOf(immatureDrops);
            return this;
        }

        /**
         * @param drops what breaking the crop gives back, ripe or not
         * @return this builder
         * @since 0.1.0
         */
        public Builder drops(final List<BlockDrop> drops) {
            return ripeDrops(drops).immatureDrops(drops);
        }

        /**
         * @param placeSound the sound played when the crop is planted, {@code null} for none
         * @return this builder
         * @since 0.1.0
         */
        public Builder placeSound(final Sound.@Nullable Type placeSound) {
            this.placeSound = placeSound;
            return this;
        }

        /**
         * @return the crop
         * @since 0.1.0
         */
        public CropBlock build() {
            return new CropBlock(this);
        }

        private int resolveMaxAge() {
            if (maxAge > 0) {
                return maxAge;
            }
            final BlockType type = Blocks.type(key);
            final BlockProperty age = type == null ? null : type.property(ageProperty);
            if (age == null) {
                throw new IllegalStateException("Block " + key.asString() + " has no '" + ageProperty
                        + "' property; call maxAge(...) to say how many stages the crop has");
            }
            return age.values().size() - 1;
        }
    }
}
