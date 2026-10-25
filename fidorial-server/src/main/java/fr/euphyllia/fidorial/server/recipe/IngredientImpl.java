package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.ItemTagLookup;
import net.kyori.adventure.key.Key;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The server's {@link Ingredient}: what it accepts, and how many of it one craft takes.
 *
 * @param choice what the ingredient accepts
 * @param count  how many items one craft takes from the slot, at least {@code 1}
 */
public record IngredientImpl(Choice choice, int count) implements Ingredient {

    public IngredientImpl {
        Preconditions.checkArgument(choice != null, "The choice of an ingredient must not be null");
        Preconditions.checkArgument(count >= 1, "An ingredient must take at least one item, got %s", count);
    }

    static IngredientImpl items(final Set<Key> items) {
        return new IngredientImpl(new AnyOf(items), 1);
    }

    static IngredientImpl tag(final Key tag) {
        return new IngredientImpl(new Tag(tag), 1);
    }

    static IngredientImpl exact(final ItemStack stack) {
        Preconditions.checkArgument(stack != null, "The stack passed to Ingredient.exact() must not be null");
        Preconditions.checkArgument(!stack.isEmpty(), "The stack passed to Ingredient.exact() must not be empty");
        return new IngredientImpl(new Exact(stack.withCount(1)), stack.count());
    }

    static IngredientImpl matching(final Predicate<ItemStack> predicate) {
        return new IngredientImpl(new Matching(predicate), 1);
    }

    @Override
    public Ingredient withCount(final int newCount) {
        return newCount == count ? this : new IngredientImpl(choice, newCount);
    }

    @Override
    public boolean test(final ItemStack stack, final ItemTagLookup tags) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (choice) {
            case AnyOf(final Set<Key> items) -> items.contains(stack.id());
            case Tag(final Key tag) -> tags.isTagged(tag, stack.id());
            case Exact(final ItemStack expected) -> expected.isSimilar(stack);
            case Matching(final Predicate<ItemStack> predicate) -> predicate.test(stack);
        };
    }

    @Override
    public boolean accepts(final ItemStack stack, final ItemTagLookup tags) {
        return stack.count() >= count && test(stack, tags);
    }

    @Override
    public String toString() {
        final String accepted = switch (choice) {
            case AnyOf(final Set<Key> items) -> items.toString();
            case Tag(final Key tag) -> "#" + tag.asString();
            case Exact(final ItemStack stack) -> stack.toString();
            case Matching _ -> "<predicate>";
        };
        return count == 1 ? accepted : accepted + " x" + count;
    }

    /**
     * What an ingredient accepts.
     */
    public sealed interface Choice permits AnyOf, Tag, Exact, Matching {
    }

    /**
     * Any of a set of items.
     */
    public record AnyOf(Set<Key> items) implements Choice {

        public AnyOf {
            Preconditions.checkArgument(items != null, "The items of an ingredient must not be null");
            Preconditions.checkArgument(!items.isEmpty(), "An ingredient must accept at least one item");
            items = Collections.unmodifiableSet(new LinkedHashSet<>(items));
        }
    }

    /**
     * Any item of an item tag.
     */
    public record Tag(Key tag) implements Choice {

        public Tag {
            Preconditions.checkArgument(tag != null, "The tag of an ingredient must not be null");
        }
    }

    /**
     * Stacks {@linkplain ItemStack#isSimilar similar} to a given one, of one item.
     */
    public record Exact(ItemStack stack) implements Choice {

        public Exact {
            Preconditions.checkArgument(stack != null, "The stack of an ingredient must not be null");
        }
    }

    /**
     * Whatever a predicate accepts.
     */
    public record Matching(Predicate<ItemStack> predicate) implements Choice {

        public Matching {
            Preconditions.checkArgument(predicate != null, "The predicate of an ingredient must not be null");
        }
    }
}
