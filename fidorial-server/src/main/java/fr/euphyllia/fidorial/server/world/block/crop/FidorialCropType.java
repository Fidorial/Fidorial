package fr.euphyllia.fidorial.server.world.block.crop;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.crop.CropDrop;
import fr.fidorial.world.block.crop.CropType;
import net.kyori.adventure.key.Key;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class FidorialCropType implements CropType {

    private final Key seed;
    private final Key block;
    private final String ageProperty;
    private final int maxAge;
    private final Set<Key> soils;
    private final boolean requiresMoistSoil;
    private final int averageTicksPerStage;
    private final int minLight;
    private final List<CropDrop> ripeDrops;
    private final List<CropDrop> immatureDrops;

    private FidorialCropType(final BuilderImpl builder) {
        this.seed = builder.seed;
        this.block = builder.block;
        this.ageProperty = builder.ageProperty;
        this.maxAge = builder.maxAge;
        this.soils = Set.copyOf(builder.soils);
        this.requiresMoistSoil = builder.requiresMoistSoil;
        this.averageTicksPerStage = builder.averageTicksPerStage;
        this.minLight = builder.minLight;
        this.ripeDrops = List.copyOf(builder.ripeDrops);
        this.immatureDrops = List.copyOf(builder.immatureDrops);
    }

    /**
     * @param seed  the item that plants this crop
     * @param block the block the crop grows as
     * @return a builder, defaulting to farmland soil and a seven-stage {@code age}
     */
    public static CropType.Builder builder(final Key seed, final Key block) {
        return new BuilderImpl(seed, block);
    }

    @Override
    public Key seed() {
        return seed;
    }

    @Override
    public Key block() {
        return block;
    }

    @Override
    public String ageProperty() {
        return ageProperty;
    }

    @Override
    public int maxAge() {
        return maxAge;
    }

    @Override
    public Set<Key> soils() {
        return soils;
    }

    @Override
    public boolean requiresMoistSoil() {
        return requiresMoistSoil;
    }

    @Override
    public int averageTicksPerStage() {
        return averageTicksPerStage;
    }

    @Override
    public int minLight() {
        return minLight;
    }

    @Override
    public List<CropDrop> ripeDrops() {
        return ripeDrops;
    }

    @Override
    public List<CropDrop> immatureDrops() {
        return immatureDrops;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final CropType other
                && seed.equals(other.seed())
                && block.equals(other.block());
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed, block);
    }

    @Override
    public String toString() {
        return "CropType[seed=" + seed.asString() + ", block=" + block.asString() + "]";
    }

    private static final class BuilderImpl implements CropType.Builder {

        private final Key seed;
        private final Key block;
        private String ageProperty = DEFAULT_AGE_PROPERTY;
        private int maxAge = 7;
        private final Set<Key> soils = new LinkedHashSet<>(Set.of(BlockTypeKeys.FARMLAND.key()));
        private boolean requiresMoistSoil = true;
        private int averageTicksPerStage = 600;
        private int minLight = 0;
        private List<CropDrop> ripeDrops;
        private List<CropDrop> immatureDrops;

        private BuilderImpl(final Key seed, final Key block) {
            this.seed = Objects.requireNonNull(seed, "seed");
            this.block = Objects.requireNonNull(block, "block");
        }

        @Override
        public CropType.Builder ageProperty(final String ageProperty) {
            this.ageProperty = Objects.requireNonNull(ageProperty, "ageProperty");
            return this;
        }

        @Override
        public CropType.Builder maxAge(final int maxAge) {
            Preconditions.checkArgument(maxAge >= 1, "maxAge must be at least 1, got %s", maxAge);
            this.maxAge = maxAge;
            return this;
        }

        @Override
        public CropType.Builder soils(final Set<Key> soils) {
            Preconditions.checkArgument(!soils.isEmpty(), "A crop needs at least one soil");
            this.soils.clear();
            this.soils.addAll(soils);
            return this;
        }

        @Override
        public CropType.Builder requiresMoistSoil(final boolean requiresMoistSoil) {
            this.requiresMoistSoil = requiresMoistSoil;
            return this;
        }

        @Override
        public CropType.Builder averageTicksPerStage(final int averageTicksPerStage) {
            Preconditions.checkArgument(averageTicksPerStage >= 1,
                    "averageTicksPerStage must be at least 1, got %s", averageTicksPerStage);
            this.averageTicksPerStage = averageTicksPerStage;
            return this;
        }

        @Override
        public CropType.Builder minLight(final int minLight) {
            this.minLight = Math.clamp(minLight, 0, 15);
            return this;
        }

        @Override
        public CropType.Builder ripeDrops(final List<CropDrop> ripeDrops) {
            this.ripeDrops = List.copyOf(ripeDrops);
            return this;
        }

        @Override
        public CropType.Builder immatureDrops(final List<CropDrop> immatureDrops) {
            this.immatureDrops = List.copyOf(immatureDrops);
            return this;
        }

        @Override
        public CropType build() {
            if (ripeDrops == null) {
                ripeDrops = List.of(CropDrop.of(ItemStack.of(seed)));
            }
            if (immatureDrops == null) {
                immatureDrops = List.of(CropDrop.of(ItemStack.of(seed)));
            }
            return new FidorialCropType(this);
        }
    }
}
