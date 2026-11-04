package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.Potion;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:potion} registry.
 */
public final class PotionTagKeys {
    /**
     * Tag {@code #minecraft:douses_fire}.
     */
    public static final TagKey<Potion> DOUSES_FIRE = create("douses_fire");

    /**
     * Tag {@code #minecraft:extinguishes_entities}.
     */
    public static final TagKey<Potion> EXTINGUISHES_ENTITIES = create("extinguishes_entities");

    /**
     * Tag {@code #minecraft:hurts_water_sensitive_entities}.
     */
    public static final TagKey<Potion> HURTS_WATER_SENSITIVE_ENTITIES = create("hurts_water_sensitive_entities");

    /**
     * Tag {@code #minecraft:rehydrates_axolotls}.
     */
    public static final TagKey<Potion> REHYDRATES_AXOLOTLS = create("rehydrates_axolotls");

    /**
     * Tag {@code #minecraft:tradeable}.
     */
    public static final TagKey<Potion> TRADEABLE = create("tradeable");

    private PotionTagKeys() {
        throw new UnsupportedOperationException("PotionTagKeys cannot be instantiated.");
    }

    private static TagKey<Potion> create(@KeyPattern final String value) {
        return TagKey.withRegistry(RegistryKey.POTION, value);
    }
}
