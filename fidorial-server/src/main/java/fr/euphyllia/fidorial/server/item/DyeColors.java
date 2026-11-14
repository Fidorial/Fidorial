package fr.euphyllia.fidorial.server.item;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.item.DyeColor;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The items of each {@link DyeColor}: its dye and its wool, and what two colours mix into.
 */
public final class DyeColors {

    private static final Map<DyeColor, TypedKey<Item>> DYES = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, TypedKey<Item>> WOOLS = new EnumMap<>(DyeColor.class);
    private static final Map<TypedKey<Item>, DyeColor> COLOR_OF_DYE = new HashMap<>();

    static {
        for (final DyeColor color : DyeColor.values()) {
            final TypedKey<Item> dye = item(color.colorName() + "_dye");
            DYES.put(color, dye);
            COLOR_OF_DYE.put(dye, color);
            WOOLS.put(color, item(color.colorName() + "_wool"));
        }
    }

    private DyeColors() {
    }

    @SuppressWarnings("PatternValidation")
    private static TypedKey<Item> item(final String name) {
        return TypedKey.withRegistry(RegistryKey.ITEM, Key.key(Key.MINECRAFT_NAMESPACE, name));
    }

    /**
     * {@return the dye of a colour}
     */
    public static TypedKey<Item> dye(final DyeColor color) {
        return DYES.get(color);
    }

    /**
     * {@return the wool of a colour}
     */
    public static TypedKey<Item> wool(final DyeColor color) {
        return WOOLS.get(color);
    }

    /**
     * {@return the colour of a dye, {@code null} if the item is no dye}
     */
    public static @Nullable DyeColor colorOfDye(final TypedKey<Item> item) {
        return COLOR_OF_DYE.get(item);
    }

    /**
     * {@return the colour two colours mix into}: as in Vanilla, the colour of the dye their two dyes
     * craft into, if any, otherwise one of the two at random
     */
    public static DyeColor mix(final DyeColor first, final DyeColor second) {
        final Optional<CraftingMatch> match = FidorialServer.getInstance().registries().recipes().matchCrafting(
                CraftingInput.of(2, 1, List.of(ItemStack.of(dye(first), 1), ItemStack.of(dye(second), 1))));
        if (match.isPresent() && !match.get().result().isEmpty()) {
            final DyeColor mixed = colorOfDye(match.get().result().id());
            if (mixed != null) {
                return mixed;
            }
        }
        return ThreadLocalRandom.current().nextBoolean() ? first : second;
    }
}
