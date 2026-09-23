package fr.euphyllia.fidorial.server.world.block.plant;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockDrop;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

public class FidorialCropBlock implements CropBlock {

    public static final int DEFAULT_MIN_LIGHT = 9;

    private static final Condition ALWAYS = (_, _, _) -> true;

    private final Key key;
    private final int maxAge;
    private final Set<Key> soils;
    private final int minLight;
    private final Condition condition;
    private final Growth growth;
    private final List<BlockDrop> ripeDrops;
    private final List<BlockDrop> immatureDrops;
    private final Sound.@Nullable Type placeSound;

    protected FidorialCropBlock(final Builder builder) {
        this.key = builder.key;
        this.maxAge = builder.resolveMaxAge();
        this.soils = builder.soils;
        this.minLight = builder.minLight;
        this.condition = builder.condition;
        this.growth = builder.growth != null ? builder.growth : VanillaGrowth.on(soils);
        this.ripeDrops = builder.ripeDrops;
        this.immatureDrops = builder.immatureDrops;
        this.placeSound = builder.placeSound;
    }

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

    @Override
    public int maxAge() {
        return maxAge;
    }

    @Override
    public Set<Key> soils() {
        return soils;
    }

    @Override
    public int age(final BlockData data) {
        try {
            return Integer.parseInt(data.get(AGE));
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public BlockData withAge(final BlockData data, final int age) {
        return data.with(AGE, Integer.toString(Math.clamp(age, 0, maxAge)));
    }

    protected boolean canGrow(final BlockData data, final BlockAccess world, final BlockPos pos) {
        return (minLight == 0 || world.lightLevel(pos) >= minLight) && condition.test(data, world, pos);
    }

    protected boolean rollGrowth(final BlockData data, final BlockAccess world, final BlockPos pos,
                                 final RandomGenerator random) {
        return random.nextDouble() < growth.chance(data, world, pos);
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
        if (!isRipe(data) && canGrow(data, world, pos) && rollGrowth(data, world, pos, random)) {
            world.setBlock(pos, withAge(data, age(data) + 1));
        }
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

    public static class Builder implements CropBlock.Builder {

        private final Key key;
        private Set<Key> soils = Set.of(BlockTypeKeys.FARMLAND.key());
        private int minLight = DEFAULT_MIN_LIGHT;
        private Condition condition = ALWAYS;
        private @Nullable Growth growth;
        private List<BlockDrop> ripeDrops = List.of();
        private List<BlockDrop> immatureDrops = List.of();
        private Sound.@Nullable Type placeSound = SoundEvents.CROP_PLANT;

        protected Builder(final Key key) {
            this.key = Objects.requireNonNull(key, "key");
        }

        @Override
        public Builder soils(final Set<Key> soils) {
            Preconditions.checkArgument(!soils.isEmpty(), "A crop needs at least one soil");
            this.soils = Set.copyOf(soils);
            return this;
        }

        @Override
        public Builder minLight(final int minLight) {
            this.minLight = Math.clamp(minLight, 0, 15);
            return this;
        }

        @Override
        public Builder growsWhen(final Condition condition) {
            this.condition = Objects.requireNonNull(condition, "condition");
            return this;
        }

        @Override
        public Builder growth(final Growth growth) {
            this.growth = Objects.requireNonNull(growth, "growth");
            return this;
        }

        @Override
        public Builder ripeDrops(final List<BlockDrop> drops) {
            this.ripeDrops = List.copyOf(drops);
            return this;
        }

        @Override
        public Builder immatureDrops(final List<BlockDrop> drops) {
            this.immatureDrops = List.copyOf(drops);
            return this;
        }

        @Override
        public Builder placeSound(final Sound.@Nullable Type sound) {
            this.placeSound = sound;
            return this;
        }

        @Override
        public CropBlock build() {
            return new FidorialCropBlock(this);
        }

        private int resolveMaxAge() {
            final BlockType type = Blocks.type(key);
            final BlockProperty age = type == null ? null : type.property(AGE);
            if (age == null) {
                throw new IllegalStateException("Block " + key.asString() + " must be registered, with an '"
                        + AGE + "' property, before a crop can grow as it");
            }
            return age.values().size() - 1;
        }
    }
}
