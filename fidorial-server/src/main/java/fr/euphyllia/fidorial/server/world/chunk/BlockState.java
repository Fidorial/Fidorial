package fr.euphyllia.fidorial.server.world.chunk;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntityTypes;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public final class BlockState {
    private final BlockData data;
    private final boolean air;
    private final boolean waterlogged;
    private final boolean water;
    private final boolean fluid;

    private volatile @Nullable LightProperties lightProperties;
    private volatile int mapColor = -1;
    private volatile @Nullable CollisionShape collisionShape;
    private volatile @Nullable BlockEntityLookup blockEntityType;

    /**
     * The blocks full of water without a {@code waterlogged} property to say so.
     */
    private static final Set<TypedKey<BlockType>> WATER_BLOCKS = Set.of(
            BlockTypeKeys.WATER,
            BlockTypeKeys.BUBBLE_COLUMN,
            BlockTypeKeys.KELP,
            BlockTypeKeys.KELP_PLANT,
            BlockTypeKeys.SEAGRASS,
            BlockTypeKeys.TALL_SEAGRASS
    );

    // only one instance per state is allowed to exist, so we fully delegate the creation to BlockStates.
    BlockState(final BlockData data) {
        this.data = data;
        final TypedKey<BlockType> name = data.key();
        this.air = data.isAir();
        this.waterlogged = data.getOrDefault(BlockProperties.WATERLOGGED, false);
        this.water = WATER_BLOCKS.contains(name) || this.waterlogged;
        this.fluid = this.water || name.equals(BlockTypeKeys.LAVA);
    }

    public static BlockState of(final BlockData data) {
        return BlockStates.of(data);
    }

    public static BlockState defaultOf(final TypedKey<BlockType> name) {
        return BlockStates.defaultOf(name);
    }

    public static @Nullable BlockState parseLeniently(final TypedKey<BlockType> name, final Map<String, String> properties) {
        return BlockStates.parseLeniently(name, properties);
    }

    public TypedKey<BlockType> name() {
        return data.key();
    }

    public BlockType type() {
        return data.type();
    }

    public BlockData data() {
        return data;
    }

    public <T> T get(final BlockProperty<T> property) {
        return data.get(property);
    }

    public boolean has(final BlockProperty<?> property) {
        return data.has(property);
    }

    public <T> T getOrDefault(final BlockProperty<T> property, final T fallback) {
        return data.getOrDefault(property, fallback);
    }

    public <T> BlockState with(final BlockProperty<T> property, final T value) {
        return of(data.with(property, value));
    }

    /// {@return the property of this block with the name and kind of `like`, `null` if it has none}
    @SuppressWarnings("unchecked")
    public <T> @Nullable BlockProperty<T> matching(final BlockProperty<T> like) {
        final BlockProperty<?> own = type().property(like.name());
        if (own == null || own.getClass() != like.getClass()) {
            return null;
        }
        if (own instanceof final BlockProperty.Choice<?> choice && choice.type() != ((BlockProperty.Choice<?>) like).type()) {
            return null;
        }
        return (BlockProperty<T>) own;
    }

    /// {@return the value of the property [#matching(BlockProperty)] `like`, `null` if it has none}
    public <T> @Nullable T find(final BlockProperty<T> like) {
        final BlockProperty<T> own = matching(like);
        return own == null ? null : data.get(own);
    }

    /// {@return this state with the property [#matching(BlockProperty)] `like` set, unchanged if it has none or if it is not allowed}
    public <T> BlockState withIfAllowed(final BlockProperty<T> like, final T value) {
        final BlockProperty<T> own = matching(like);
        return own == null || own.indexOf(value) < 0 ? this : with(own, value);
    }

    public boolean isAir() {
        return air;
    }

    public boolean isWaterlogged() {
        return waterlogged;
    }

    /**
     * {@return {@code true} if this block is water or holds water}: water itself, a waterlogged
     * block, or a block that is always under water such as kelp, seagrass or a bubble column
     */
    public boolean isWater() {
        return water;
    }

    /**
     * {@return {@code true} if this block is lava}
     */
    public boolean isLava() {
        return fluid && !water;
    }

    /**
     * {@return {@code true} if this block {@linkplain #isWater() holds water} or is lava}
     */
    public boolean isFluid() {
        return fluid;
    }

    public boolean isTagged(final TagKey<BlockType> tag) {
        return Registries.BLOCKS.isTagged(name(), tag);
    }

    public @Nullable LightProperties lightProperties() {
        return lightProperties;
    }

    public void setLightProperties(final LightProperties props) {
        this.lightProperties = props;
    }

    public int mapColor() {
        return mapColor;
    }

    public void setMapColor(final int mapColor) {
        this.mapColor = mapColor;
    }

    /**
     * {@return the collision shape of this state, once {@link BlockCollision} worked it out}
     */
    public @Nullable CollisionShape collisionShape() {
        return collisionShape;
    }

    public void setCollisionShape(final CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
    }

    /// The block entity type of this state, `null` if it has none.
    public @Nullable TypedKey<BlockEntityType> blockEntityType() {
        BlockEntityLookup lookup = blockEntityType;
        if (lookup == null) {
            lookup = new BlockEntityLookup(BlockEntityTypes.typeIdentifier(name()));
            blockEntityType = lookup;
        }
        return lookup.type();
    }

    @Override
    public String toString() {
        return "BlockState[" + data.asString() + "]";
    }

    public record LightProperties(int opacity, int emission) {
    }

    private record BlockEntityLookup(@Nullable TypedKey<BlockEntityType> type) {
    }

    private static final class Registries {
        private static final ServerRegistry<BlockType> BLOCKS = FidorialServer.getInstance().registries().registry(RegistryKey.BLOCK);
    }
}
