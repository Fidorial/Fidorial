package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.PaintingVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:painting_variant} registry.
 */
public final class PaintingVariantKeys {
    /**
     * Key for {@code minecraft:alban}.
     */
    public static final TypedKey<PaintingVariant> ALBAN = create("alban");

    /**
     * Key for {@code minecraft:aztec}.
     */
    public static final TypedKey<PaintingVariant> AZTEC = create("aztec");

    /**
     * Key for {@code minecraft:aztec2}.
     */
    public static final TypedKey<PaintingVariant> AZTEC2 = create("aztec2");

    /**
     * Key for {@code minecraft:backyard}.
     */
    public static final TypedKey<PaintingVariant> BACKYARD = create("backyard");

    /**
     * Key for {@code minecraft:baroque}.
     */
    public static final TypedKey<PaintingVariant> BAROQUE = create("baroque");

    /**
     * Key for {@code minecraft:bomb}.
     */
    public static final TypedKey<PaintingVariant> BOMB = create("bomb");

    /**
     * Key for {@code minecraft:bouquet}.
     */
    public static final TypedKey<PaintingVariant> BOUQUET = create("bouquet");

    /**
     * Key for {@code minecraft:burning_skull}.
     */
    public static final TypedKey<PaintingVariant> BURNING_SKULL = create("burning_skull");

    /**
     * Key for {@code minecraft:bust}.
     */
    public static final TypedKey<PaintingVariant> BUST = create("bust");

    /**
     * Key for {@code minecraft:cavebird}.
     */
    public static final TypedKey<PaintingVariant> CAVEBIRD = create("cavebird");

    /**
     * Key for {@code minecraft:changing}.
     */
    public static final TypedKey<PaintingVariant> CHANGING = create("changing");

    /**
     * Key for {@code minecraft:cotan}.
     */
    public static final TypedKey<PaintingVariant> COTAN = create("cotan");

    /**
     * Key for {@code minecraft:courbet}.
     */
    public static final TypedKey<PaintingVariant> COURBET = create("courbet");

    /**
     * Key for {@code minecraft:creebet}.
     */
    public static final TypedKey<PaintingVariant> CREEBET = create("creebet");

    /**
     * Key for {@code minecraft:dennis}.
     */
    public static final TypedKey<PaintingVariant> DENNIS = create("dennis");

    /**
     * Key for {@code minecraft:donkey_kong}.
     */
    public static final TypedKey<PaintingVariant> DONKEY_KONG = create("donkey_kong");

    /**
     * Key for {@code minecraft:earth}.
     */
    public static final TypedKey<PaintingVariant> EARTH = create("earth");

    /**
     * Key for {@code minecraft:endboss}.
     */
    public static final TypedKey<PaintingVariant> ENDBOSS = create("endboss");

    /**
     * Key for {@code minecraft:fern}.
     */
    public static final TypedKey<PaintingVariant> FERN = create("fern");

    /**
     * Key for {@code minecraft:fighters}.
     */
    public static final TypedKey<PaintingVariant> FIGHTERS = create("fighters");

    /**
     * Key for {@code minecraft:finding}.
     */
    public static final TypedKey<PaintingVariant> FINDING = create("finding");

    /**
     * Key for {@code minecraft:fire}.
     */
    public static final TypedKey<PaintingVariant> FIRE = create("fire");

    /**
     * Key for {@code minecraft:graham}.
     */
    public static final TypedKey<PaintingVariant> GRAHAM = create("graham");

    /**
     * Key for {@code minecraft:humble}.
     */
    public static final TypedKey<PaintingVariant> HUMBLE = create("humble");

    /**
     * Key for {@code minecraft:kebab}.
     */
    public static final TypedKey<PaintingVariant> KEBAB = create("kebab");

    /**
     * Key for {@code minecraft:lowmist}.
     */
    public static final TypedKey<PaintingVariant> LOWMIST = create("lowmist");

    /**
     * Key for {@code minecraft:match}.
     */
    public static final TypedKey<PaintingVariant> MATCH = create("match");

    /**
     * Key for {@code minecraft:meditative}.
     */
    public static final TypedKey<PaintingVariant> MEDITATIVE = create("meditative");

    /**
     * Key for {@code minecraft:orb}.
     */
    public static final TypedKey<PaintingVariant> ORB = create("orb");

    /**
     * Key for {@code minecraft:owlemons}.
     */
    public static final TypedKey<PaintingVariant> OWLEMONS = create("owlemons");

    /**
     * Key for {@code minecraft:passage}.
     */
    public static final TypedKey<PaintingVariant> PASSAGE = create("passage");

    /**
     * Key for {@code minecraft:pigscene}.
     */
    public static final TypedKey<PaintingVariant> PIGSCENE = create("pigscene");

    /**
     * Key for {@code minecraft:plant}.
     */
    public static final TypedKey<PaintingVariant> PLANT = create("plant");

    /**
     * Key for {@code minecraft:pointer}.
     */
    public static final TypedKey<PaintingVariant> POINTER = create("pointer");

    /**
     * Key for {@code minecraft:pond}.
     */
    public static final TypedKey<PaintingVariant> POND = create("pond");

    /**
     * Key for {@code minecraft:pool}.
     */
    public static final TypedKey<PaintingVariant> POOL = create("pool");

    /**
     * Key for {@code minecraft:prairie_ride}.
     */
    public static final TypedKey<PaintingVariant> PRAIRIE_RIDE = create("prairie_ride");

    /**
     * Key for {@code minecraft:sea}.
     */
    public static final TypedKey<PaintingVariant> SEA = create("sea");

    /**
     * Key for {@code minecraft:skeleton}.
     */
    public static final TypedKey<PaintingVariant> SKELETON = create("skeleton");

    /**
     * Key for {@code minecraft:skull_and_roses}.
     */
    public static final TypedKey<PaintingVariant> SKULL_AND_ROSES = create("skull_and_roses");

    /**
     * Key for {@code minecraft:stage}.
     */
    public static final TypedKey<PaintingVariant> STAGE = create("stage");

    /**
     * Key for {@code minecraft:sunflowers}.
     */
    public static final TypedKey<PaintingVariant> SUNFLOWERS = create("sunflowers");

    /**
     * Key for {@code minecraft:sunset}.
     */
    public static final TypedKey<PaintingVariant> SUNSET = create("sunset");

    /**
     * Key for {@code minecraft:tides}.
     */
    public static final TypedKey<PaintingVariant> TIDES = create("tides");

    /**
     * Key for {@code minecraft:unpacked}.
     */
    public static final TypedKey<PaintingVariant> UNPACKED = create("unpacked");

    /**
     * Key for {@code minecraft:void}.
     */
    public static final TypedKey<PaintingVariant> VOID = create("void");

    /**
     * Key for {@code minecraft:wanderer}.
     */
    public static final TypedKey<PaintingVariant> WANDERER = create("wanderer");

    /**
     * Key for {@code minecraft:wasteland}.
     */
    public static final TypedKey<PaintingVariant> WASTELAND = create("wasteland");

    /**
     * Key for {@code minecraft:water}.
     */
    public static final TypedKey<PaintingVariant> WATER = create("water");

    /**
     * Key for {@code minecraft:wind}.
     */
    public static final TypedKey<PaintingVariant> WIND = create("wind");

    /**
     * Key for {@code minecraft:wither}.
     */
    public static final TypedKey<PaintingVariant> WITHER = create("wither");

    private static final List<TypedKey<PaintingVariant>> VALUES = List.of(
        ALBAN,
        AZTEC,
        AZTEC2,
        BACKYARD,
        BAROQUE,
        BOMB,
        BOUQUET,
        BURNING_SKULL,
        BUST,
        CAVEBIRD,
        CHANGING,
        COTAN,
        COURBET,
        CREEBET,
        DENNIS,
        DONKEY_KONG,
        EARTH,
        ENDBOSS,
        FERN,
        FIGHTERS,
        FINDING,
        FIRE,
        GRAHAM,
        HUMBLE,
        KEBAB,
        LOWMIST,
        MATCH,
        MEDITATIVE,
        ORB,
        OWLEMONS,
        PASSAGE,
        PIGSCENE,
        PLANT,
        POINTER,
        POND,
        POOL,
        PRAIRIE_RIDE,
        SEA,
        SKELETON,
        SKULL_AND_ROSES,
        STAGE,
        SUNFLOWERS,
        SUNSET,
        TIDES,
        UNPACKED,
        VOID,
        WANDERER,
        WASTELAND,
        WATER,
        WIND,
        WITHER
    );

    private PaintingVariantKeys() {
        throw new UnsupportedOperationException("PaintingVariantKeys cannot be instantiated.");
    }

    private static TypedKey<PaintingVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.PAINTING_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<PaintingVariant>> values() {
        return VALUES.stream();
    }
}
