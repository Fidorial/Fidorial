package fr.fidorial.world.dimension;

import com.google.common.base.Preconditions;
import fr.fidorial.world.environment.EnvironmentAttributes;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link DimensionTypeDefinition}.
 *
 * <p>Defaults match the vanilla overworld.</p>
 *
 * @since 0.1.0
 */
public final class DimensionTypeBuilder {

    private Key key;
    private double coordinateScale = 1.0D;
    private boolean hasSkylight = true;
    private boolean hasCeiling = false;
    private boolean hasEnderDragonFight = false;
    private float ambientLight = 0.0F;
    private boolean hasFixedTime = false;
    private int monsterSpawnBlockLightLimit = 0;
    private IntProvider monsterSpawnLightLevel = IntProvider.uniform(0, 7);
    private int logicalHeight = 384;
    private int minY = -64;
    private int height = 384;
    private Key infiniburn = Key.key("infiniburn_overworld");
    private Skybox skybox = Skybox.OVERWORLD;
    private CardinalLight cardinalLight = CardinalLight.DEFAULT;
    private EnvironmentAttributes.Builder attributes = EnvironmentAttributes.builder();
    private @Nullable Key defaultClock;
    private final List<TimelineReference> timelines = new ArrayList<>();

    DimensionTypeBuilder(final Key key) {
        Preconditions.checkArgument(key != null, "The key of a dimension type builder must not be null");
        this.key = key;
    }

    DimensionTypeBuilder(final DimensionTypeDefinition definition) {
        this.key = definition.key();
        this.coordinateScale = definition.coordinateScale();
        this.hasSkylight = definition.hasSkylight();
        this.hasCeiling = definition.hasCeiling();
        this.hasEnderDragonFight = definition.hasEnderDragonFight();
        this.ambientLight = definition.ambientLight();
        this.hasFixedTime = definition.hasFixedTime();
        this.monsterSpawnBlockLightLimit = definition.monsterSpawnBlockLightLimit();
        this.monsterSpawnLightLevel = definition.monsterSpawnLightLevel();
        this.logicalHeight = definition.logicalHeight();
        this.minY = definition.minY();
        this.height = definition.height();
        this.infiniburn = definition.infiniburn();
        this.skybox = definition.skybox();
        this.cardinalLight = definition.cardinalLight();
        this.attributes = EnvironmentAttributes.builder(definition.attributes());
        this.defaultClock = definition.defaultClock();
        this.timelines.addAll(definition.timelines());
    }

    /**
     * @param key the namespaced identifier of the dimension type
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder key(final Key key) {
        Preconditions.checkArgument(key != null, "The key passed to DimensionTypeBuilder.key() must not be null");
        this.key = key;
        return this;
    }

    /**
     * @param coordinateScale multiplier applied to coordinates when leaving the dimension,
     *                        within {@code [0.00001, 30000000.0]}
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder coordinateScale(final double coordinateScale) {
        this.coordinateScale = coordinateScale;
        return this;
    }

    /**
     * @param hasSkylight whether the dimension has skylight; disables weather when {@code false}
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder hasSkylight(final boolean hasSkylight) {
        this.hasSkylight = hasSkylight;
        return this;
    }

    /**
     * @param hasCeiling whether the dimension has a logical bedrock ceiling
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder hasCeiling(final boolean hasCeiling) {
        this.hasCeiling = hasCeiling;
        return this;
    }

    /**
     * @param hasEnderDragonFight whether this dimension can host an ender dragon fight
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder hasEnderDragonFight(final boolean hasEnderDragonFight) {
        this.hasEnderDragonFight = hasEnderDragonFight;
        return this;
    }

    /**
     * @param ambientLight how much light the dimension has regardless of the light level
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder ambientLight(final float ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * @param hasFixedTime whether this dimension has fixed time
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder hasFixedTime(final boolean hasFixedTime) {
        this.hasFixedTime = hasFixedTime;
        return this;
    }

    /**
     * @param monsterSpawnBlockLightLimit block light must be at or below this, within {@code [0, 15]},
     *                                    for monsters to spawn
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder monsterSpawnBlockLightLimit(final int monsterSpawnBlockLightLimit) {
        this.monsterSpawnBlockLightLimit = monsterSpawnBlockLightLimit;
        return this;
    }

    /**
     * @param monsterSpawnLightLevel the computed light level must be at or below this for monsters
     *                               to spawn
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder monsterSpawnLightLevel(final IntProvider monsterSpawnLightLevel) {
        Preconditions.checkArgument(monsterSpawnLightLevel != null, "The monster spawn light level passed to DimensionTypeBuilder.monsterSpawnLightLevel() must not be null");
        this.monsterSpawnLightLevel = monsterSpawnLightLevel;
        return this;
    }

    /**
     * Shorthand for {@link #monsterSpawnLightLevel(IntProvider)} with a constant value.
     *
     * @param constant the light level, within {@code [0, 15]}
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder monsterSpawnLightLevel(final int constant) {
        this.monsterSpawnLightLevel = IntProvider.constant(constant);
        return this;
    }

    /**
     * @param logicalHeight the maximum height chorus fruits and Nether portals bring
     *                      players to; must not exceed {@link #height(int)}
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder logicalHeight(final int logicalHeight) {
        this.logicalHeight = logicalHeight;
        return this;
    }

    /**
     * @param minY lowest buildable height, a multiple of 16 within {@code [-2032, 2031]}
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder minY(final int minY) {
        this.minY = minY;
        return this;
    }

    /**
     * @param height total buildable height, a multiple of 16 within {@code [16, 4064]};
     *               {@code minY + height - 1} must not exceed {@code 2031}
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder height(final int height) {
        this.height = height;
        return this;
    }

    /**
     * Sets {@link #minY(int)} and {@link #height(int)} together, leaving
     * {@link #logicalHeight(int)} untouched.
     *
     * @param minY   lowest buildable height
     * @param height total buildable height
     * @return this builder
     */
    @Contract("_, _ -> this")
    public DimensionTypeBuilder bounds(final int minY, final int height) {
        this.minY = minY;
        this.height = height;
        return this;
    }

    /**
     * @param infiniburn the block tag whose blocks burn forever
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder infiniburn(final Key infiniburn) {
        Preconditions.checkArgument(infiniburn != null, "The infiniburn passed to DimensionTypeBuilder.infiniburn() must not be null");
        this.infiniburn = infiniburn;
        return this;
    }

    /**
     * @param skybox which skybox the client renders
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder skybox(final Skybox skybox) {
        Preconditions.checkArgument(skybox != null, "The skybox passed to DimensionTypeBuilder.skybox() must not be null");
        this.skybox = skybox;
        return this;
    }

    /**
     * @param cardinalLight which direction cardinal light comes from
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder cardinalLight(final CardinalLight cardinalLight) {
        Preconditions.checkArgument(cardinalLight != null, "The cardinal light passed to DimensionTypeBuilder.cardinalLight() must not be null");
        this.cardinalLight = cardinalLight;
        return this;
    }

    /**
     * Replaces the whole environment attribute map.
     *
     * @param attributes the environment attributes this dimension sets
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder attributes(final EnvironmentAttributes attributes) {
        this.attributes = EnvironmentAttributes.builder(attributes);
        return this;
    }

    /**
     * Configures the environment attributes in place, starting from what is already set.
     *
     * @param configurer callback receiving the attribute builder
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder attributes(final Consumer<EnvironmentAttributes.Builder> configurer) {
        configurer.accept(this.attributes);
        return this;
    }

    /**
     * @param defaultClock the world clock used as default for this dimension, or
     *                     {@code null} if it has none
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder defaultClock(final @Nullable Key defaultClock) {
        this.defaultClock = defaultClock;
        return this;
    }

    /**
     * Adds one timeline to the list of timelines active in this dimension.
     *
     * @param timeline the timeline to add
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder addTimeline(final TimelineReference timeline) {
        Preconditions.checkArgument(timeline != null, "The timeline passed to DimensionTypeBuilder.addTimeline() must not be null");
        this.timelines.add(timeline);
        return this;
    }

    /**
     * Replaces the whole list of timelines active in this dimension.
     *
     * @param timelines the timelines
     * @return this builder
     */
    @Contract("_ -> this")
    public DimensionTypeBuilder timelines(final List<TimelineReference> timelines) {
        this.timelines.clear();
        this.timelines.addAll(timelines);
        return this;
    }

    /**
     * {@return the immutable definition described by this builder}
     */
    @Contract("-> new")
    public DimensionTypeDefinition build() {
        return new DimensionTypeDefinition(
                key,
                coordinateScale,
                hasSkylight,
                hasCeiling,
                hasEnderDragonFight,
                ambientLight,
                hasFixedTime,
                monsterSpawnBlockLightLimit,
                monsterSpawnLightLevel,
                logicalHeight,
                minY,
                height,
                infiniburn,
                skybox,
                cardinalLight,
                attributes.build(),
                defaultClock,
                List.copyOf(timelines));
    }
}
