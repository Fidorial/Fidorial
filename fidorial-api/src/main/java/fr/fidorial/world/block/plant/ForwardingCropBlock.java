package fr.fidorial.world.block.plant;

import fr.fidorial.item.ItemStack;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * A crop that hands every call to another one. Extend it and override only what
 * your crop does differently; call {@code super} to keep the regular behaviour.
 *
 * @since 0.1.0
 */
public abstract class ForwardingCropBlock implements CropBlock {

    private final CropBlock delegate;

    protected ForwardingCropBlock(final CropBlock delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * @return the crop calls are handed to
     * @since 0.1.0
     */
    protected final CropBlock delegate() {
        return delegate;
    }

    @Override
    public BlockType type() {
        return delegate.type();
    }

    @Override
    public Key key() {
        return delegate.key();
    }

    @Override
    public int maxAge() {
        return delegate.maxAge();
    }

    @Override
    public Set<Key> soils() {
        return delegate.soils();
    }

    @Override
    public int age(final BlockData data) {
        return delegate.age(data);
    }

    @Override
    public BlockData withAge(final BlockData data, final int age) {
        return delegate.withAge(data, age);
    }

    @Override
    public boolean isRipe(final BlockData data) {
        return delegate.isRipe(data);
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        return delegate.placementState(context);
    }

    @Override
    public int lightEmission(final BlockData data) {
        return delegate.lightEmission(data);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return delegate.lightOpacity(data);
    }

    @Override
    public boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        return delegate.canSurvive(data, world, pos);
    }

    @Override
    public BlockData updateShape(final BlockData data,
                                 final BlockFace direction,
                                 final BlockData neighbour,
                                 final BlockAccess world,
                                 final BlockPos pos) {
        return delegate.updateShape(data, direction, neighbour, world, pos);
    }

    @Override
    public boolean isRandomlyTicking(final BlockData data) {
        return delegate.isRandomlyTicking(data);
    }

    @Override
    public void randomTick(final BlockData data, final BlockAccess world, final BlockPos pos, final RandomGenerator random) {
        delegate.randomTick(data, world, pos, random);
    }

    @Override
    public InteractionResult use(final BlockData data, final BlockInteractionContext context) {
        return delegate.use(data, context);
    }

    @Override
    public List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        return delegate.drops(data, random);
    }

    @Override
    public boolean breaksInstantly(final BlockData data) {
        return delegate.breaksInstantly(data);
    }

    @Override
    public Sound.@Nullable Type placeSound(final BlockData data) {
        return delegate.placeSound(data);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + delegate + "]";
    }
}
