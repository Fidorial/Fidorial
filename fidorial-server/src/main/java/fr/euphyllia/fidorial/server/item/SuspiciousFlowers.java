package fr.euphyllia.fidorial.server.item;

import fr.fidorial.item.component.SuspiciousStewEffects;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * The effect each flower gives a suspicious stew: a brown mooshroom that eats one
 * gives that stew.
 */
public final class SuspiciousFlowers {

    private static final Map<TypedKey<Item>, SuspiciousStewEffects> EFFECTS = Map.ofEntries(
            flower(ItemKeys.DANDELION, MobEffectKeys.SATURATION, 0.35f),
            flower(ItemKeys.GOLDEN_DANDELION, MobEffectKeys.SATURATION, 0.35f),
            flower(ItemKeys.TORCHFLOWER, MobEffectKeys.NIGHT_VISION, 5.0f),
            flower(ItemKeys.POPPY, MobEffectKeys.NIGHT_VISION, 5.0f),
            flower(ItemKeys.BLUE_ORCHID, MobEffectKeys.SATURATION, 0.35f),
            flower(ItemKeys.ALLIUM, MobEffectKeys.FIRE_RESISTANCE, 3.0f),
            flower(ItemKeys.AZURE_BLUET, MobEffectKeys.BLINDNESS, 11.0f),
            flower(ItemKeys.RED_TULIP, MobEffectKeys.WEAKNESS, 7.0f),
            flower(ItemKeys.ORANGE_TULIP, MobEffectKeys.WEAKNESS, 7.0f),
            flower(ItemKeys.WHITE_TULIP, MobEffectKeys.WEAKNESS, 7.0f),
            flower(ItemKeys.PINK_TULIP, MobEffectKeys.WEAKNESS, 7.0f),
            flower(ItemKeys.OXEYE_DAISY, MobEffectKeys.REGENERATION, 7.0f),
            flower(ItemKeys.CORNFLOWER, MobEffectKeys.JUMP_BOOST, 5.0f),
            flower(ItemKeys.WITHER_ROSE, MobEffectKeys.WITHER, 7.0f),
            flower(ItemKeys.LILY_OF_THE_VALLEY, MobEffectKeys.POISON, 11.0f),
            flower(ItemKeys.OPEN_EYEBLOSSOM, MobEffectKeys.BLINDNESS, 11.0f),
            flower(ItemKeys.CLOSED_EYEBLOSSOM, MobEffectKeys.NAUSEA, 7.0f));

    private SuspiciousFlowers() {
    }

    private static Map.Entry<TypedKey<Item>, SuspiciousStewEffects> flower(final TypedKey<Item> item,
                                                                           final TypedKey<MobEffect> effect,
                                                                           final float seconds) {
        return Map.entry(item, SuspiciousStewEffects.of(effect, (int) Math.floor(seconds * 20.0f)));
    }

    /**
     * {@return the effects a flower gives a suspicious stew, {@code null} if the item is no such
     * flower}
     */
    public static @Nullable SuspiciousStewEffects effectsOf(final TypedKey<Item> item) {
        return EFFECTS.get(item);
    }
}
