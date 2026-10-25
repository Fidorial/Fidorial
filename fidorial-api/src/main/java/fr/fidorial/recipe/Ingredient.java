package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * What one grid slot of a recipe accepts, and how many of it the craft takes from that slot.
 *
 * <p>Vanilla always takes one item per slot. Here the {@linkplain #count() count} can be raised:
 * {@code Ingredient.of(DIAMOND).withCount(16)} only matches a slot holding at least sixteen
 * diamonds, and crafting takes sixteen of them.</p>
 *
 * <p>Instances are immutable and safe to share between threads.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface Ingredient {

    /**
     * @param items the items accepted, any of them will do
     * @return an ingredient taking one of any of {@code items}
     * @throws IllegalArgumentException if {@code items} is empty
     * @since 0.1.0
     */
    static Ingredient of(final Key... items) {
        return of(Arrays.asList(items));
    }

    /**
     * @param items the items accepted, any of them will do
     * @return an ingredient taking one of any of {@code items}
     * @throws IllegalArgumentException if {@code items} is empty
     * @since 0.1.0
     */
    static Ingredient of(final Collection<Key> items) {
        return RecipeFactory.factory().items(items);
    }

    /**
     * @param tag an item tag, e.g. {@code minecraft:planks}, without the leading {@code #}
     * @return an ingredient taking one of any item of the tag
     * @since 0.1.0
     */
    static Ingredient tag(final Key tag) {
        return RecipeFactory.factory().tag(tag);
    }

    /**
     * Accepts only stacks of the same item carrying the same components, which is how a plugin
     * recognises its own custom items. The count of {@code stack} becomes the count of the
     * ingredient: {@code Ingredient.exact(ruby.withCount(4))} takes four rubies.
     *
     * @param stack the stack to compare against
     * @return an ingredient taking {@code stack.count()} items similar to {@code stack}
     * @throws IllegalArgumentException if {@code stack} is empty
     * @since 0.1.0
     */
    static Ingredient exact(final ItemStack stack) {
        return RecipeFactory.factory().exact(stack);
    }

    /**
     * Accepts whatever {@code predicate} accepts. The predicate sees the whole stack but must not
     * look at its count: the {@linkplain #count() count} of the ingredient takes care of that.
     *
     * @param predicate tells whether a stack is accepted, never called with an empty stack
     * @return an ingredient taking one item accepted by {@code predicate}
     * @since 0.1.0
     */
    static Ingredient matching(final Predicate<ItemStack> predicate) {
        return RecipeFactory.factory().matching(predicate);
    }

    /**
     * @return how many items one craft takes from the slot, at least {@code 1}
     * @since 0.1.0
     */
    int count();

    /**
     * @param newCount how many items one craft takes from the slot
     * @return an ingredient accepting the same items, in that quantity
     * @throws IllegalArgumentException if {@code newCount} is lower than {@code 1}
     * @since 0.1.0
     */
    Ingredient withCount(int newCount);

    /**
     * Whether the item of {@code stack} is accepted, regardless of how many there are.
     *
     * @param stack the stack to test
     * @param tags  resolves {@linkplain #tag(Key) tag} ingredients
     * @return {@code true} when {@code stack} is not empty and its item is accepted
     * @since 0.1.0
     */
    boolean test(ItemStack stack, ItemTagLookup tags);

    /**
     * Whether {@code stack} can feed one craft: its item is {@linkplain #test accepted} and there
     * are at least {@link #count()} of it.
     *
     * @param stack the stack to test
     * @param tags  resolves {@linkplain #tag(Key) tag} ingredients
     * @return {@code true} when one craft can take its share from {@code stack}
     * @since 0.1.0
     */
    boolean accepts(ItemStack stack, ItemTagLookup tags);
}
