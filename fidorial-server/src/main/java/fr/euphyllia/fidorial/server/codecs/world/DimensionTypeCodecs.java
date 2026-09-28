package fr.euphyllia.fidorial.server.codecs.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.fidorial.world.dimension.CardinalLight;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.Skybox;
import fr.fidorial.world.dimension.TimelineReference;
import fr.fidorial.world.environment.EnvironmentAttributes;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class DimensionTypeCodecs {

    public static final Codec<Skybox> SKYBOX = byId(Skybox.values(), Skybox::id, "skybox");

    public static final Codec<CardinalLight> CARDINAL_LIGHT =
            byId(CardinalLight.values(), CardinalLight::id, "cardinal light");

    public static final Codec<Key> TAG_KEY = Codec.STRING.comapFlatMap(
            s -> s.startsWith("#") && Key.parseable(s.substring(1))
                    ? DataResult.success(Key.key(s.substring(1)))
                    : DataResult.error(() -> "Expected a #tag, got: " + s),
            key -> "#" + key.asString());

    private static final Codec<Double> COORDINATE_SCALE = Codec.DOUBLE.validate(CommonCodecs.check(
            scale -> scale >= 0.00001D && scale <= 30_000_000.0D,
            scale -> "coordinate_scale out of range: " + scale
    ));

    private static final Codec<TimelineReference> SINGLE_TIMELINE = Codec.STRING.comapFlatMap(
            s -> {
                final String raw = s.startsWith("#") ? s.substring(1) : s;
                if (!Key.parseable(raw)) {
                    return DataResult.error(() -> "Not a valid key: " + s);
                }
                final Key key = Key.key(raw);
                return DataResult.success(s.startsWith("#") ? TimelineReference.tag(key) : TimelineReference.id(key));
            },
            timeline -> switch (timeline) {
                case final TimelineReference.Id id -> id.key().asString();
                case final TimelineReference.Tag tag -> "#" + tag.key().asString();
            });

    private static final Codec<List<TimelineReference>> TIMELINES = Codec.either(
            SINGLE_TIMELINE.listOf(), SINGLE_TIMELINE
    ).xmap(
            either -> either.map(list -> list, List::of),
            list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));

    private DimensionTypeCodecs() {
        throw new UnsupportedOperationException("DimensionTypeCodecs cannot be instantiated.");
    }

    public static Codec<DimensionTypeDefinition> codec(final Key key) {
        return RecordCodec.builder(DimensionTypeDefinition.class)
                .given(key)
                .required("coordinate_scale", DimensionTypeDefinition::coordinateScale, COORDINATE_SCALE)
                .required("has_skylight", DimensionTypeDefinition::hasSkylight, Codec.BOOL)
                .required("has_ceiling", DimensionTypeDefinition::hasCeiling, Codec.BOOL)
                .required("has_ender_dragon_fight", DimensionTypeDefinition::hasEnderDragonFight, Codec.BOOL)
                .required("ambient_light", DimensionTypeDefinition::ambientLight, Codec.FLOAT)
                .field("has_fixed_time", DimensionTypeDefinition::hasFixedTime, Codec.BOOL, false)
                .required("monster_spawn_block_light_limit", DimensionTypeDefinition::monsterSpawnBlockLightLimit, Codec.intRange(0, 15))
                .required("monster_spawn_light_level", DimensionTypeDefinition::monsterSpawnLightLevel, IntProviderCodecs.INT_PROVIDER)
                .required("logical_height", DimensionTypeDefinition::logicalHeight, Codec.INT)
                .required("min_y", DimensionTypeDefinition::minY, Codec.INT)
                .required("height", DimensionTypeDefinition::height, Codec.INT)
                .required("infiniburn", DimensionTypeDefinition::infiniburn, TAG_KEY)
                .field("skybox", DimensionTypeDefinition::skybox, SKYBOX, Skybox.OVERWORLD)
                .field("cardinal_light", DimensionTypeDefinition::cardinalLight, CARDINAL_LIGHT, CardinalLight.DEFAULT)
                .field("attributes", DimensionTypeDefinition::attributes, EnvironmentAttributeCodecs.ATTRIBUTES, EnvironmentAttributes.EMPTY)
                .nullable("default_clock", DimensionTypeDefinition::defaultClock, CommonCodecs.KEY_CODEC)
                .field("timelines", DimensionTypeDefinition::timelines, TIMELINES, List.of())
                .build();
    }

    public static CompoundBinaryTag encodeNbt(final DimensionTypeDefinition dimensionType) {
        final BinaryTag tag = codec(dimensionType.key())
                .encodeStart(BinaryTagOps.binaryTagOps(), dimensionType)
                .getOrThrow(message -> new IllegalStateException(
                        "Failed to encode dimension type " + dimensionType.key().asString() + ": " + message));

        if (!(tag instanceof final CompoundBinaryTag compound)) {
            throw new IllegalStateException(
                    "Dimension type " + dimensionType.key().asString() + " did not encode to a compound tag");
        }

        return compound;
    }

    public static DimensionTypeDefinition fromJson(final Key key, final String json) {
        final JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (final RuntimeException exception) {
            throw new IllegalArgumentException("Dimension type " + key.asString() + " is not valid JSON", exception);
        }

        return codec(key)
                .parse(JsonOps.INSTANCE, element)
                .getOrThrow(message -> new IllegalArgumentException(
                        "Failed to read dimension type " + key.asString() + ": " + message));
    }

    private static <E extends Enum<E>> Codec<E> byId(
            final E[] values,
            final Function<E, String> id,
            final String what
    ) {
        return Codec.STRING.comapFlatMap(
                name -> Arrays.stream(values)
                        .filter(value -> id.apply(value).equals(name))
                        .findFirst()
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown " + what + ": " + name)),
                id);
    }
}
