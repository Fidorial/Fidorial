package fr.euphyllia.fidorial.server.registry.data;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.data.Attribute;
import fr.fidorial.registry.data.BannerPattern;
import fr.fidorial.registry.data.BlockSoundSet;
import fr.fidorial.registry.data.BlockTransformer;
import fr.fidorial.registry.data.BlockType;
import fr.fidorial.registry.data.CatSoundVariant;
import fr.fidorial.registry.data.CatVariant;
import fr.fidorial.registry.data.ChickenSoundVariant;
import fr.fidorial.registry.data.ChickenVariant;
import fr.fidorial.registry.data.CowSoundVariant;
import fr.fidorial.registry.data.CowVariant;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.data.DataComponentType;
import fr.fidorial.registry.data.DebugSubscription;
import fr.fidorial.registry.data.DecoratedPotPattern;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.data.FrogVariant;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.registry.data.GameRule;
import fr.fidorial.registry.data.Instrument;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.data.JukeboxSong;
import fr.fidorial.registry.data.MapDecorationType;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.data.PaintingVariant;
import fr.fidorial.registry.data.PigSoundVariant;
import fr.fidorial.registry.data.PigVariant;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.registry.data.SulfurCubeArchetype;
import fr.fidorial.registry.data.TestEnvironment;
import fr.fidorial.registry.data.TestInstance;
import fr.fidorial.registry.data.Timeline;
import fr.fidorial.registry.data.TrimMaterial;
import fr.fidorial.registry.data.TrimPattern;
import fr.fidorial.registry.data.VillagerProfession;
import fr.fidorial.registry.data.VillagerType;
import fr.fidorial.registry.data.WolfSoundVariant;
import fr.fidorial.registry.data.WolfVariant;
import fr.fidorial.registry.data.WorldClock;
import fr.fidorial.registry.data.ZombieNautilusVariant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * Values of the registries without a bound API type, each carrying only its key.
 *
 * <p>Generated from {@code SupportedRegistries}; do not edit.</p>
 */
public final class RegistryValues {
    private static final Map<Key, Function<Key, ?>> FACTORIES = factories();

    private RegistryValues() {
        throw new UnsupportedOperationException("RegistryValues cannot be instantiated.");
    }

    /**
     * {@return the constructor of the values of {@code registry}, or {@code null} when it has a bound API type}
     *
     * @param registry the registry
     * @param <T>      the value type
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable Function<Key, T> factory(final RegistryKey<T> registry) {
        return (Function<Key, T>) FACTORIES.get(registry.key());
    }

    private static Map<Key, Function<Key, ?>> factories() {
        final Map<Key, Function<Key, ?>> factories = new HashMap<>();
        factories.put(RegistryKey.ATTRIBUTE.key(), AttributeValue::new);
        factories.put(RegistryKey.BANNER_PATTERN.key(), BannerPatternValue::new);
        factories.put(RegistryKey.BLOCK_SOUND_SET.key(), BlockSoundSetValue::new);
        factories.put(RegistryKey.BLOCK_TRANSFORMER.key(), BlockTransformerValue::new);
        factories.put(RegistryKey.BLOCK.key(), BlockTypeValue::new);
        factories.put(RegistryKey.CAT_SOUND_VARIANT.key(), CatSoundVariantValue::new);
        factories.put(RegistryKey.CAT_VARIANT.key(), CatVariantValue::new);
        factories.put(RegistryKey.CHICKEN_SOUND_VARIANT.key(), ChickenSoundVariantValue::new);
        factories.put(RegistryKey.CHICKEN_VARIANT.key(), ChickenVariantValue::new);
        factories.put(RegistryKey.COW_SOUND_VARIANT.key(), CowSoundVariantValue::new);
        factories.put(RegistryKey.COW_VARIANT.key(), CowVariantValue::new);
        factories.put(RegistryKey.DAMAGE_TYPE.key(), DamageTypeValue::new);
        factories.put(RegistryKey.DATA_COMPONENT_TYPE.key(), DataComponentTypeValue::new);
        factories.put(RegistryKey.DECORATED_POT_PATTERN.key(), DecoratedPotPatternValue::new);
        factories.put(RegistryKey.DEBUG_SUBSCRIPTION.key(), DebugSubscriptionValue::new);
        factories.put(RegistryKey.FROG_VARIANT.key(), FrogVariantValue::new);
        factories.put(RegistryKey.FLUID.key(), FluidValue::new);
        factories.put(RegistryKey.GAME_EVENT.key(), GameEventValue::new);
        factories.put(RegistryKey.GAME_RULE.key(), GameRuleValue::new);
        factories.put(RegistryKey.INSTRUMENT.key(), InstrumentValue::new);
        factories.put(RegistryKey.ITEM.key(), ItemValue::new);
        factories.put(RegistryKey.JUKEBOX_SONG.key(), JukeboxSongValue::new);
        factories.put(RegistryKey.MAP_DECORATION_TYPE.key(), MapDecorationTypeValue::new);
        factories.put(RegistryKey.MENU.key(), MenuTypeValue::new);
        factories.put(RegistryKey.MOB_EFFECT.key(), MobEffectValue::new);
        factories.put(RegistryKey.PAINTING_VARIANT.key(), PaintingVariantValue::new);
        factories.put(RegistryKey.PIG_SOUND_VARIANT.key(), PigSoundVariantValue::new);
        factories.put(RegistryKey.PIG_VARIANT.key(), PigVariantValue::new);
        factories.put(RegistryKey.SOUND_EVENT.key(), SoundEventValue::new);
        factories.put(RegistryKey.SULFUR_CUBE_ARCHETYPE.key(), SulfurCubeArchetypeValue::new);
        factories.put(RegistryKey.TEST_ENVIRONMENT.key(), TestEnvironmentValue::new);
        factories.put(RegistryKey.TEST_INSTANCE.key(), TestInstanceValue::new);
        factories.put(RegistryKey.TIMELINE.key(), TimelineValue::new);
        factories.put(RegistryKey.TRIM_MATERIAL.key(), TrimMaterialValue::new);
        factories.put(RegistryKey.TRIM_PATTERN.key(), TrimPatternValue::new);
        factories.put(RegistryKey.VILLAGER_PROFESSION.key(), VillagerProfessionValue::new);
        factories.put(RegistryKey.VILLAGER_TYPE.key(), VillagerTypeValue::new);
        factories.put(RegistryKey.WOLF_SOUND_VARIANT.key(), WolfSoundVariantValue::new);
        factories.put(RegistryKey.WOLF_VARIANT.key(), WolfVariantValue::new);
        factories.put(RegistryKey.WORLD_CLOCK.key(), WorldClockValue::new);
        factories.put(RegistryKey.ZOMBIE_NAUTILUS_VARIANT.key(), ZombieNautilusVariantValue::new);
        return Map.copyOf(factories);
    }

    /**
     * A {@code minecraft:attribute} entry.
     */
    public record AttributeValue(Key key) implements Attribute {
        public AttributeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:banner_pattern} entry.
     */
    public record BannerPatternValue(Key key) implements BannerPattern {
        public BannerPatternValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:block_sound_set} entry.
     */
    public record BlockSoundSetValue(Key key) implements BlockSoundSet {
        public BlockSoundSetValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:block_transformer} entry.
     */
    public record BlockTransformerValue(Key key) implements BlockTransformer {
        public BlockTransformerValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:block} entry.
     */
    public record BlockTypeValue(Key key) implements BlockType {
        public BlockTypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:cat_sound_variant} entry.
     */
    public record CatSoundVariantValue(Key key) implements CatSoundVariant {
        public CatSoundVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:cat_variant} entry.
     */
    public record CatVariantValue(Key key) implements CatVariant {
        public CatVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:chicken_sound_variant} entry.
     */
    public record ChickenSoundVariantValue(Key key) implements ChickenSoundVariant {
        public ChickenSoundVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:chicken_variant} entry.
     */
    public record ChickenVariantValue(Key key) implements ChickenVariant {
        public ChickenVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:cow_sound_variant} entry.
     */
    public record CowSoundVariantValue(Key key) implements CowSoundVariant {
        public CowSoundVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:cow_variant} entry.
     */
    public record CowVariantValue(Key key) implements CowVariant {
        public CowVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:damage_type} entry.
     */
    public record DamageTypeValue(Key key) implements DamageType {
        public DamageTypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:data_component_type} entry.
     */
    public record DataComponentTypeValue(Key key) implements DataComponentType {
        public DataComponentTypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:decorated_pot_pattern} entry.
     */
    public record DecoratedPotPatternValue(Key key) implements DecoratedPotPattern {
        public DecoratedPotPatternValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:debug_subscription} entry.
     */
    public record DebugSubscriptionValue(Key key) implements DebugSubscription {
        public DebugSubscriptionValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:frog_variant} entry.
     */
    public record FrogVariantValue(Key key) implements FrogVariant {
        public FrogVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:fluid} entry.
     */
    public record FluidValue(Key key) implements Fluid {
        public FluidValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:game_event} entry.
     */
    public record GameEventValue(Key key) implements GameEvent {
        public GameEventValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:game_rule} entry.
     */
    public record GameRuleValue(Key key) implements GameRule {
        public GameRuleValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:instrument} entry.
     */
    public record InstrumentValue(Key key) implements Instrument {
        public InstrumentValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:item} entry.
     */
    public record ItemValue(Key key) implements Item {
        public ItemValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:jukebox_song} entry.
     */
    public record JukeboxSongValue(Key key) implements JukeboxSong {
        public JukeboxSongValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:map_decoration_type} entry.
     */
    public record MapDecorationTypeValue(Key key) implements MapDecorationType {
        public MapDecorationTypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:menu} entry.
     */
    public record MenuTypeValue(Key key) implements MenuType {
        public MenuTypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:mob_effect} entry.
     */
    public record MobEffectValue(Key key) implements MobEffect {
        public MobEffectValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:painting_variant} entry.
     */
    public record PaintingVariantValue(Key key) implements PaintingVariant {
        public PaintingVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:pig_sound_variant} entry.
     */
    public record PigSoundVariantValue(Key key) implements PigSoundVariant {
        public PigSoundVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:pig_variant} entry.
     */
    public record PigVariantValue(Key key) implements PigVariant {
        public PigVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:sound_event} entry.
     */
    public record SoundEventValue(Key key) implements SoundEvent {
        public SoundEventValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:sulfur_cube_archetype} entry.
     */
    public record SulfurCubeArchetypeValue(Key key) implements SulfurCubeArchetype {
        public SulfurCubeArchetypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:test_environment} entry.
     */
    public record TestEnvironmentValue(Key key) implements TestEnvironment {
        public TestEnvironmentValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:test_instance} entry.
     */
    public record TestInstanceValue(Key key) implements TestInstance {
        public TestInstanceValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:timeline} entry.
     */
    public record TimelineValue(Key key) implements Timeline {
        public TimelineValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:trim_material} entry.
     */
    public record TrimMaterialValue(Key key) implements TrimMaterial {
        public TrimMaterialValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:trim_pattern} entry.
     */
    public record TrimPatternValue(Key key) implements TrimPattern {
        public TrimPatternValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:villager_profession} entry.
     */
    public record VillagerProfessionValue(Key key) implements VillagerProfession {
        public VillagerProfessionValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:villager_type} entry.
     */
    public record VillagerTypeValue(Key key) implements VillagerType {
        public VillagerTypeValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:wolf_sound_variant} entry.
     */
    public record WolfSoundVariantValue(Key key) implements WolfSoundVariant {
        public WolfSoundVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:wolf_variant} entry.
     */
    public record WolfVariantValue(Key key) implements WolfVariant {
        public WolfVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:world_clock} entry.
     */
    public record WorldClockValue(Key key) implements WorldClock {
        public WorldClockValue {
            Objects.requireNonNull(key, "key");
        }
    }

    /**
     * A {@code minecraft:zombie_nautilus_variant} entry.
     */
    public record ZombieNautilusVariantValue(Key key) implements ZombieNautilusVariant {
        public ZombieNautilusVariantValue {
            Objects.requireNonNull(key, "key");
        }
    }
}
