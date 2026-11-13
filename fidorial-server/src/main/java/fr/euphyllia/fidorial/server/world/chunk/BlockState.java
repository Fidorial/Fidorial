package fr.euphyllia.fidorial.server.world.chunk;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntityTypes;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockState {
    private final TypedKey<BlockType> name;
    private final Map<String, String> properties;
    private final int hashCode;
    private final boolean air;
    private final boolean waterlogged;
    private final boolean fluid;

    private volatile @Nullable LightProperties lightProperties;
    private volatile @Nullable BlockData data;
    private volatile int mapColor = -1;
    private volatile @Nullable CollisionShape collisionShape;
    private volatile @Nullable BlockEntityLookup blockEntityType;
    private static final ConcurrentHashMap<BlockState, BlockState> INTERN = new ConcurrentHashMap<>(BlockStateIds.STATE_COUNT);

    private static final Set<TypedKey<BlockType>> FLUID_BLOCKS = Set.of(
            BlockTypeKeys.WATER,
            BlockTypeKeys.LAVA,
            BlockTypeKeys.BUBBLE_COLUMN,
            BlockTypeKeys.KELP,
            BlockTypeKeys.KELP_PLANT,
            BlockTypeKeys.SEAGRASS,
            BlockTypeKeys.TALL_SEAGRASS
    );

    private BlockState(final TypedKey<BlockType> name, final Map<String, String> properties) {
        this.name = name;
        this.properties = properties.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new TreeMap<>(properties));
        this.hashCode = 31 * name.hashCode() + this.properties.hashCode();
        this.air = name.equals(BlockTypeKeys.AIR) || name.equals(BlockTypeKeys.CAVE_AIR) || name.equals(BlockTypeKeys.VOID_AIR);
        this.waterlogged = "true".equals(this.properties.get("waterlogged"));
        this.fluid = (FLUID_BLOCKS.contains(name) || this.waterlogged);
    }

    private BlockState(final TypedKey<BlockType> name, final Map<String, String> properties, final int hashCode, final boolean air, final boolean waterlogged, final boolean fluid) {
        this.name = name;
        this.properties = properties;
        this.hashCode = hashCode;
        this.air = air;
        this.waterlogged = waterlogged;
        this.fluid = fluid;
    }

    public static BlockState withoutProperties(final TypedKey<BlockType> name) {
        return withProperties(name, Collections.emptyMap(), true);
    }

    public static BlockState withProperties(final TypedKey<BlockType> name, final Map<String, String> properties) {
        return withProperties(name, properties, true);
    }

    static BlockState withProperties(final TypedKey<BlockType> name, final Map<String, String> properties, final boolean checkIntern) {
        if (checkIntern) {
            final int hash = 31 * name.hashCode() + properties.hashCode();
            final BlockState existing = INTERN.get(new BlockState(name, properties, hash, false, false, false)); // safe to pass whatever value for air, waterlogged and fluid as they're discarded in this context
            if (existing != null) {
                return existing;
            }
        }
        final BlockState created = new BlockState(name, properties);
        final BlockState previous = INTERN.putIfAbsent(created, created);
        return previous != null ? previous : created;
    }

    public TypedKey<BlockType> name() {
        return name;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public boolean isAir() {
        return air;
    }

    public boolean isWaterlogged() {
        return waterlogged;
    }

    public boolean isFluid() {
        return fluid;
    }

    public boolean isTagged(final TagKey<BlockType> tag) {
        return Registries.BLOCKS.isTagged(this.name, tag);
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

    public @Nullable BlockData data() {
        return data;
    }

    public void setData(final BlockData data) {
        this.data = data;
    }

    /// The block entity type of this state, `null` if it has none.
    public @Nullable TypedKey<BlockEntityType> blockEntityType() {
        BlockEntityLookup lookup = blockEntityType;
        if (lookup == null) {
            lookup = new BlockEntityLookup(BlockEntityTypes.typeIdentifier(name));
            blockEntityType = lookup;
        }
        return lookup.type();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof final BlockState other)) return false;
        return hashCode == other.hashCode && name.equals(other.name) && properties.equals(other.properties);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "BlockState[name=" + name + ", properties=" + properties + "]";
    }

    public record LightProperties(int opacity, int emission) {
    }

    private record BlockEntityLookup(@Nullable TypedKey<BlockEntityType> type) {
    }

    private static final class Registries {
        private static final ServerRegistry<BlockType> BLOCKS = FidorialServer.getInstance().registries().registry(RegistryKey.BLOCK);
    }
}
