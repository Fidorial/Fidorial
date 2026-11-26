package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Whether a block can stay where a decoration wants to put it: a plant needs ground it grows on, a hanging block
 * needs a ceiling, a crystal needs a face to cling to. Blocks without a rule stay anywhere.
 *
 * <p>The ground a plant needs is a tag of the game, such as {@code minecraft:supports_vegetation}; which plants
 * need it is a tag of the data pack bundled with the server, which other data packs can change.</p>
 *
 * <p>The rule of each state number is looked up once. Thread-safe.</p>
 */
public final class Survival {

    @FunctionalInterface
    private interface Rule {
        boolean test(DecorationRegion region, char id, int x, int y, int z);
    }

    /**
     * The plants that stand on a block of {@code minecraft:supports_vegetation}: grasses, ferns, bushes, flowers and
     * saplings. A plant with a rule of its own, such as the wither rose or the azaleas, follows that rule instead.
     */
    public static final TagKey<BlockType> VEGETATION = TagKey.withRegistry(RegistryKey.BLOCK, Key.key("fidorial", "vegetation"));

    /**
     * The plants that stand on a block of {@code minecraft:supports_dry_vegetation}.
     */
    public static final TagKey<BlockType> DRY_VEGETATION = TagKey.withRegistry(RegistryKey.BLOCK, Key.key("fidorial", "dry_vegetation"));

    /**
     * The crystals that grow on a face, and only stay while the block behind them holds them on that face.
     */
    public static final TagKey<BlockType> CRYSTALS = TagKey.withRegistry(RegistryKey.BLOCK, Key.key("fidorial", "crystals"));

    private static final Rule ANYWHERE = (region, id, x, y, z) -> true;

    private final StateFlags states;
    private final StateFlags.Membership vegetation;
    private final StateFlags.Membership dryVegetation;
    private final StateFlags.Membership crystals;
    private final Rule onVegetationGround;
    private final Rule onDryGround;
    private final Map<TypedKey<BlockType>, Rule> named = new HashMap<>();
    private volatile Rule[] rules = new Rule[256];

    public Survival(final StateFlags states) {
        this.states = states;
        this.vegetation = states.membership(VEGETATION);
        this.dryVegetation = states.membership(DRY_VEGETATION);
        this.crystals = states.membership(CRYSTALS);
        this.onVegetationGround = groundTag(BlockTypeTagKeys.SUPPORTS_VEGETATION);
        this.onDryGround = groundTag(BlockTypeTagKeys.SUPPORTS_DRY_VEGETATION);

        named.put(BlockTypeKeys.WITHER_ROSE, groundTag(BlockTypeTagKeys.SUPPORTS_WITHER_ROSE));
        named.put(BlockTypeKeys.AZALEA, groundTag(BlockTypeTagKeys.SUPPORTS_AZALEA));
        named.put(BlockTypeKeys.FLOWERING_AZALEA, groundTag(BlockTypeTagKeys.SUPPORTS_AZALEA));
        named.put(BlockTypeKeys.BAMBOO, groundTag(BlockTypeTagKeys.SUPPORTS_BAMBOO));
        named.put(BlockTypeKeys.BAMBOO_SAPLING, groundTag(BlockTypeTagKeys.SUPPORTS_BAMBOO));
        named.put(BlockTypeKeys.BROWN_MUSHROOM, (region, id, x, y, z) -> states.sturdyCenter(region.id(x, y - 1, z), BlockFace.UP));
        named.put(BlockTypeKeys.RED_MUSHROOM, named.get(BlockTypeKeys.BROWN_MUSHROOM));
        named.put(BlockTypeKeys.LEAF_LITTER, (region, id, x, y, z) -> states.sturdy(region.id(x, y - 1, z), BlockFace.UP));
        named.put(BlockTypeKeys.HANGING_ROOTS, (region, id, x, y, z) -> states.sturdy(region.id(x, y + 1, z), BlockFace.DOWN));
        named.put(BlockTypeKeys.MOSS_CARPET, (region, id, x, y, z) -> !region.isAir(x, y - 1, z));
        named.put(BlockTypeKeys.PALE_MOSS_CARPET, this::paleMossCarpet);
        named.put(BlockTypeKeys.SNOW, this::snowLayer);
        named.put(BlockTypeKeys.CACTUS, this::cactus);
        named.put(BlockTypeKeys.SUGAR_CANE, this::sugarCane);
        named.put(BlockTypeKeys.LILY_PAD, this::lilyPad);
        named.put(BlockTypeKeys.SEAGRASS, (region, id, x, y, z) -> seagrassGround(region.id(x, y - 1, z)));
        named.put(BlockTypeKeys.SEA_PICKLE, (region, id, x, y, z) -> {
            final char below = region.id(x, y - 1, z);
            return states.has(below, StateFlags.TOP_SURFACE) || states.sturdy(below, BlockFace.UP);
        });
        named.put(BlockTypeKeys.SPORE_BLOSSOM, (region, id, x, y, z) ->
                states.sturdyCenter(region.id(x, y + 1, z), BlockFace.DOWN) && !states.has(region.id(x, y, z), StateFlags.WATER));
        named.put(BlockTypeKeys.MANGROVE_PROPAGULE, this::propagule);
        named.put(BlockTypeKeys.CRIMSON_FUNGUS, groundTag(BlockTypeTagKeys.SUPPORTS_CRIMSON_FUNGUS));
        named.put(BlockTypeKeys.WARPED_FUNGUS, groundTag(BlockTypeTagKeys.SUPPORTS_WARPED_FUNGUS));
        named.put(BlockTypeKeys.CRIMSON_ROOTS, groundTag(BlockTypeTagKeys.SUPPORTS_CRIMSON_ROOTS));
        named.put(BlockTypeKeys.WARPED_ROOTS, groundTag(BlockTypeTagKeys.SUPPORTS_WARPED_ROOTS));
        named.put(BlockTypeKeys.NETHER_SPROUTS, groundTag(BlockTypeTagKeys.SUPPORTS_NETHER_SPROUTS));
        named.put(BlockTypeKeys.SOUL_FIRE, groundTag(BlockTypeTagKeys.SOUL_FIRE_BASE_BLOCKS));
        // a fire next to a block that burns stays too, but the server does not know which blocks burn: decorations
        // only set fire on solid ground anyway
        named.put(BlockTypeKeys.FIRE, (region, id, x, y, z) -> states.sturdy(region.id(x, y - 1, z), BlockFace.UP));
    }

    /**
     * {@return {@code true} if a state can stay at a block}
     */
    public boolean canSurvive(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        return rule(id).test(region, id, x, y, z);
    }

    private Rule rule(final char id) {
        final Rule[] known = rules;
        if (id < known.length && known[id] != null) {
            return known[id];
        }
        final Rule rule = resolve(id);
        synchronized (this) {
            Rule[] current = rules;
            if (id >= current.length) {
                current = Arrays.copyOf(current, Math.max(current.length * 2, id + 1));
            }
            current[id] = rule;
            rules = current;
        }
        return rule;
    }

    private Rule resolve(final char id) {
        final TypedKey<BlockType> name = states.palette().state(id).name();
        Rule rule = named.get(name);
        if (rule == null) {
            if (name.equals(BlockTypeKeys.SMALL_DRIPLEAF)) {
                rule = this::smallDripleaf;
            } else if (name.equals(BlockTypeKeys.TALL_SEAGRASS)) {
                rule = this::tallSeagrass;
            } else if (crystals.test(id)) {
                rule = Survival::crystal;
            } else if (dryVegetation.test(id)) {
                rule = onDryGround;
            } else if (vegetation.test(id)) {
                rule = onVegetationGround;
            } else {
                rule = ANYWHERE;
            }
        }
        // the upper half of a tall plant stands on its lower half; small dripleaf and tall seagrass check it themselves
        if (states.has(id, StateFlags.TWO_BLOCKS_TALL) && !name.equals(BlockTypeKeys.SMALL_DRIPLEAF)
                && !name.equals(BlockTypeKeys.TALL_SEAGRASS)) {
            final Rule lower = rule;
            rule = (region, state, x, y, z) -> "upper".equals(states.property(state, "half"))
                    ? upperHalf(region, state, x, y, z)
                    : lower.test(region, state, x, y, z);
        }
        return rule;
    }

    private Rule groundTag(final TagKey<BlockType> tag) {
        final StateFlags.Membership ground = states.membership(tag);
        return (region, id, x, y, z) -> ground.test(region.id(x, y - 1, z));
    }

    private boolean upperHalf(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        final char below = region.id(x, y - 1, z);
        return states.palette().state(below).name().equals(states.palette().state(id).name())
                && "lower".equals(states.property(below, "half"));
    }

    private boolean seagrassGround(final char below) {
        return states.sturdy(below, BlockFace.UP) && !states.isTagged(below, BlockTypeTagKeys.CANNOT_SUPPORT_SEAGRASS);
    }

    private boolean tallSeagrass(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        if ("upper".equals(states.property(id, "half"))) {
            return upperHalf(region, id, x, y, z);
        }
        return seagrassGround(region.id(x, y - 1, z)) && states.has(region.id(x, y, z), StateFlags.WATER_SOURCE);
    }

    private boolean smallDripleaf(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        if ("upper".equals(states.property(id, "half"))) {
            return upperHalf(region, id, x, y, z);
        }
        final char below = region.id(x, y - 1, z);
        if (states.isTagged(below, BlockTypeTagKeys.SUPPORTS_SMALL_DRIPLEAF)) {
            return true;
        }
        return states.has(region.id(x, y, z), StateFlags.WATER_SOURCE) && states.isTagged(below, BlockTypeTagKeys.SUPPORTS_VEGETATION);
    }

    private boolean paleMossCarpet(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        final char below = region.id(x, y - 1, z);
        if ("true".equals(states.property(id, "bottom"))) {
            return !states.isAir(below);
        }
        return states.is(below, BlockTypeKeys.PALE_MOSS_CARPET) && "true".equals(states.property(below, "bottom"));
    }

    private boolean snowLayer(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        final char below = region.id(x, y - 1, z);
        if (states.isTagged(below, BlockTypeTagKeys.CANNOT_SUPPORT_SNOW_LAYER)) {
            return false;
        }
        if (states.isTagged(below, BlockTypeTagKeys.SUPPORT_OVERRIDE_SNOW_LAYER)) {
            return true;
        }
        return states.sturdy(below, BlockFace.UP)
                || states.is(below, BlockTypeKeys.SNOW) && "8".equals(states.property(below, "layers"));
    }

    private boolean cactus(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        if (blockedSide(region, x + 1, y, z) || blockedSide(region, x - 1, y, z)
                || blockedSide(region, x, y, z + 1) || blockedSide(region, x, y, z - 1)) {
            return false;
        }
        final char below = region.id(x, y - 1, z);
        return (states.is(below, BlockTypeKeys.CACTUS) || states.isTagged(below, BlockTypeTagKeys.SUPPORTS_CACTUS))
                && !states.has(region.id(x, y + 1, z), StateFlags.LIQUID_BLOCK);
    }

    private boolean blockedSide(final DecorationRegion region, final int x, final int y, final int z) {
        final char side = region.id(x, y, z);
        return states.has(side, StateFlags.SOLID) || states.has(side, StateFlags.LAVA);
    }

    private boolean sugarCane(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        final char below = region.id(x, y - 1, z);
        if (states.is(below, BlockTypeKeys.SUGAR_CANE)) {
            return true;
        }
        if (!states.isTagged(below, BlockTypeTagKeys.SUPPORTS_SUGAR_CANE)) {
            return false;
        }
        return wetSide(region, x + 1, y - 1, z) || wetSide(region, x - 1, y - 1, z)
                || wetSide(region, x, y - 1, z + 1) || wetSide(region, x, y - 1, z - 1);
    }

    private boolean wetSide(final DecorationRegion region, final int x, final int y, final int z) {
        final char side = region.id(x, y, z);
        return states.has(side, StateFlags.WATER) || states.isTagged(side, BlockTypeTagKeys.SUPPORTS_SUGAR_CANE_ADJACENTLY);
    }

    private boolean lilyPad(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        final char below = region.id(x, y - 1, z);
        final boolean ground = states.has(below, StateFlags.WATER_SOURCE) || states.isTagged(below, BlockTypeTagKeys.SUPPORTS_LILY_PAD);
        return ground && !states.hasFluid(region.id(x, y, z));
    }

    private boolean propagule(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        if ("true".equals(states.property(id, "hanging"))) {
            return states.isTagged(region.id(x, y + 1, z), BlockTypeTagKeys.SUPPORTS_HANGING_MANGROVE_PROPAGULE);
        }
        return states.isTagged(region.id(x, y - 1, z), BlockTypeTagKeys.SUPPORTS_MANGROVE_PROPAGULE);
    }

    private static boolean crystal(final DecorationRegion region, final char id, final int x, final int y, final int z) {
        final StateFlags states = region.states();
        final String facing = states.property(id, "facing");
        if (facing == null) {
            return true;
        }
        final BlockFace face = BlockFace.valueOf(facing.toUpperCase(Locale.ROOT));
        return states.sturdy(region.id(x - face.dx(), y - face.dy(), z - face.dz()), face);
    }
}
