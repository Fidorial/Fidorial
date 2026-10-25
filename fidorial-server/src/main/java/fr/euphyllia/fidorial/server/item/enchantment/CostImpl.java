package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EffectJson;
import fr.fidorial.item.enchantment.EnchantmentDefinition;

/**
 * Server implementation of {@link EnchantmentDefinition.Cost}.
 *
 * @param base               the cost at level {@code 1}
 * @param perLevelAboveFirst the cost added by each level above the first
 */
public record CostImpl(int base, int perLevelAboveFirst) implements EnchantmentDefinition.Cost {

    public static CostImpl parse(final JsonObject object) {
        return new CostImpl(EffectJson.integer(object, "base"), EffectJson.integer(object, "per_level_above_first"));
    }

    public JsonObject toJson() {
        final JsonObject object = new JsonObject();
        object.addProperty("base", base);
        object.addProperty("per_level_above_first", perLevelAboveFirst);
        return object;
    }
}
