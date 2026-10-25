package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON helpers for enchantment effects, accepting {@code 0} and {@code 1} as booleans since NBT has no
 * boolean type.
 */
public final class EffectJson {

    private EffectJson() {
        throw new UnsupportedOperationException("EffectJson cannot be instantiated.");
    }

    public static JsonObject object(final @Nullable JsonElement element, final String what) {
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException(what + " must be a JSON object, got " + element);
        }
        return element.getAsJsonObject();
    }

    public static JsonObject object(final JsonObject parent, final String field) {
        return object(parent.get(field), "'" + field + "'");
    }

    public static @Nullable JsonObject optionalObject(final JsonObject parent, final String field) {
        final JsonElement element = parent.get(field);
        return element == null || element.isJsonNull() ? null : object(element, "'" + field + "'");
    }

    public static List<JsonElement> list(final @Nullable JsonElement element, final String what) {
        if (element == null || !element.isJsonArray()) {
            throw new IllegalArgumentException(what + " must be a JSON array, got " + element);
        }
        final JsonArray array = element.getAsJsonArray();
        final List<JsonElement> values = new ArrayList<>(array.size());
        for (final JsonElement value : array) {
            values.add(value);
        }
        return values;
    }

    public static List<JsonElement> list(final JsonObject parent, final String field) {
        return list(parent.get(field), "'" + field + "'");
    }

    public static String string(final JsonObject parent, final String field) {
        final JsonElement element = parent.get(field);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("'" + field + "' must be a string, got " + element);
        }
        return element.getAsString();
    }

    public static int integer(final JsonObject parent, final String field) {
        final JsonElement element = parent.get(field);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("'" + field + "' must be a number, got " + element);
        }
        final double value = element.getAsDouble();
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("'" + field + "' must be a whole number, got " + element);
        }
        return (int) value;
    }

    public static float number(final JsonObject parent, final String field) {
        final JsonElement element = parent.get(field);
        if (element == null || !isNumber(element)) {
            throw new IllegalArgumentException("'" + field + "' must be a number, got " + element);
        }
        return element.getAsFloat();
    }

    public static float number(final JsonObject parent, final String field, final float fallback) {
        return parent.has(field) ? number(parent, field) : fallback;
    }

    public static boolean isNumber(final JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    /**
     * {@return the boolean of {@code field}, {@code null} when absent}
     */
    public static @Nullable Boolean optionalBoolean(final JsonObject parent, final String field) {
        final JsonElement element = parent.get(field);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                return primitive.getAsDouble() != 0.0;
            }
            if (primitive.isString() && ("true".equals(primitive.getAsString()) || "false".equals(primitive.getAsString()))) {
                return Boolean.parseBoolean(primitive.getAsString());
            }
        }
        throw new IllegalArgumentException("'" + field + "' must be a boolean, got " + element);
    }

    public static boolean bool(final JsonObject parent, final String field) {
        final Boolean value = optionalBoolean(parent, field);
        if (value == null) {
            throw new IllegalArgumentException("'" + field + "' is missing");
        }
        return value;
    }

    public static Key key(final String value) {
        try {
            return Key.key(value);
        } catch (final InvalidKeyException exception) {
            throw new IllegalArgumentException("'" + value + "' is not a valid identifier", exception);
        }
    }

    public static Key key(final JsonObject parent, final String field) {
        return key(string(parent, field));
    }

    /**
     * {@return the {@code type} of a dispatched object, such as {@code minecraft:linear}}
     */
    public static Key type(final JsonObject object) {
        return key(object, "type");
    }
}
