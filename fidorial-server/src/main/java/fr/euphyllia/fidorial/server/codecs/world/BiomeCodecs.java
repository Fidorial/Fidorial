package fr.euphyllia.fidorial.server.codecs.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.biome.BiomeEffects;
import fr.fidorial.world.biome.GrassColorModifier;
import fr.fidorial.world.biome.TemperatureModifier;
import fr.fidorial.world.environment.EnvironmentAttributes;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Arrays;
import java.util.function.Function;

public class BiomeCodecs {

    public static final Codec<TemperatureModifier> TEMPERATURE_MODIFIER =
            byId(TemperatureModifier.values(), TemperatureModifier::id, "temperature modifier");

    public static final Codec<GrassColorModifier> GRASS_COLOR_MODIFIER =
            byId(GrassColorModifier.values(), GrassColorModifier::id, "grass color modifier");

    public static final Codec<Key> PARTICLE_OPTIONS = CommonCodecs.KEY_CODEC.fieldOf("type").codec();

    public static final Codec<BiomeEffects> EFFECTS = RecordCodec.builder(BiomeEffects.class)
            .required("water_color", BiomeEffects::waterColor, CommonCodecs.RGB_COLOR)
            .nullable("foliage_color", BiomeEffects::foliageColor, CommonCodecs.RGB_COLOR)
            .nullable("grass_color", BiomeEffects::grassColor, CommonCodecs.RGB_COLOR)
            .nullable("dry_foliage_color", BiomeEffects::dryFoliageColor, CommonCodecs.RGB_COLOR)
            .field("grass_color_modifier", BiomeEffects::grassColorModifier, GRASS_COLOR_MODIFIER, GrassColorModifier.NONE)
            .build();

    private BiomeCodecs() {
        throw new UnsupportedOperationException("BiomeCodecs cannot be instantiated.");
    }

    public static Codec<BiomeDefinition> codec(final Key key) {
        return RecordCodec.builder(BiomeDefinition.class)
                .given(key)
                .required("has_precipitation", BiomeDefinition::hasPrecipitation, Codec.BOOL)
                .required("temperature", BiomeDefinition::temperature, Codec.FLOAT)
                .field("temperature_modifier", BiomeDefinition::temperatureModifier, TEMPERATURE_MODIFIER, TemperatureModifier.NONE)
                .required("downfall", BiomeDefinition::downfall, Codec.FLOAT)
                .required("effects", BiomeDefinition::effects, EFFECTS)
                .field("attributes", BiomeDefinition::attributes, EnvironmentAttributeCodecs.ATTRIBUTES, EnvironmentAttributes.EMPTY)
                .build();
    }

    public static CompoundBinaryTag encodeNbt(final BiomeDefinition biome) {
        final BinaryTag tag = codec(biome.key())
                .encodeStart(BinaryTagOps.binaryTagOps(), biome)
                .getOrThrow(message -> new IllegalStateException(
                        "Failed to encode biome " + biome.key().asString() + ": " + message));

        if (!(tag instanceof final CompoundBinaryTag compound)) {
            throw new IllegalStateException("Biome " + biome.key().asString() + " did not encode to a compound tag");
        }

        return compound;
    }

    public static BiomeDefinition fromJson(final Key key, final String json) {
        final JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (final RuntimeException exception) {
            throw new IllegalArgumentException("Biome " + key.asString() + " is not valid JSON", exception);
        }

        return codec(key)
                .parse(JsonOps.INSTANCE, element)
                .getOrThrow(message -> new IllegalArgumentException(
                        "Failed to read biome " + key.asString() + ": " + message));
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
                id::apply);
    }
}
