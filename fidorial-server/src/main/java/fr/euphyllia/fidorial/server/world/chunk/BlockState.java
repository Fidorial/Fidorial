package fr.euphyllia.fidorial.server.world.chunk;

import fr.fidorial.registry.keys.BlockTypeKeys;
import net.kyori.adventure.key.Key;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockState {
    private final Key name;
    private final Map<String, String> properties;
    private final int hashCode;
    private final boolean air;
    private final boolean fluid;

    private volatile LightProperties lightProperties;
    private static final ConcurrentHashMap<BlockState, BlockState> INTERN = new ConcurrentHashMap<>(65_536);

    private BlockState(final Key name, final Map<String, String> properties) {
        this.name = name;
        this.properties = properties.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new TreeMap<>(properties));
        this.hashCode = 31 * name.hashCode() + this.properties.hashCode();
        this.air = name.equals(BlockTypeKeys.AIR.key()) || name.equals(BlockTypeKeys.CAVE_AIR.key()) || name.equals(BlockTypeKeys.VOID_AIR.key());
        this.fluid = name.equals(BlockTypeKeys.WATER.key())
                || name.equals(BlockTypeKeys.LAVA.key())
                || "true".equals(this.properties.get("waterlogged"));
    }

    private BlockState(final Key name, final Map<String, String> properties, final int hashCode, final boolean air, final boolean fluid) {
        this.name = name;
        this.properties = properties;
        this.hashCode = hashCode;
        this.air = air;
        this.fluid = fluid;
    }

    public static BlockState of(final Key name) {
        return of(name, Collections.emptyMap(), true);
    }

    public static BlockState of(final Key name, final Map<String, String> properties) {
        return of(name, properties, true);
    }

    static BlockState of(final Key name, final Map<String, String> properties, final boolean checkIntern) {
        if (checkIntern) {
            final int hash = 31 * name.hashCode() + properties.hashCode();
            final BlockState existing = INTERN.get(new BlockState(name, properties, hash, false, false)); // safe to pass whatever value for air and fluid as they're discarded in this context
            if (existing != null) {
                return existing;
            }
        }
        final BlockState created = new BlockState(name, properties);
        final BlockState previous = INTERN.putIfAbsent(created, created);
        return previous != null ? previous : created;
    }

    public Key name() {
        return name;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public boolean isAir() {
        return air;
    }

    public boolean isFluid() {
        return fluid;
    }

    public LightProperties lightProperties() {
        return lightProperties;
    }

    public void setLightProperties(final LightProperties props) {
        this.lightProperties = props;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockState other)) return false;
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
}
