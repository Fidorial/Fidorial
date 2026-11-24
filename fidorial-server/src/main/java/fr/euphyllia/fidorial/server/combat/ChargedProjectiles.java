package fr.euphyllia.fidorial.server.combat;

import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.registry.keys.DataComponentTypeKeys;

import java.util.List;

/**
 * The projectiles a crossbow is loaded with: the {@code minecraft:charged_projectiles} component,
 * known to the server only.
 *
 * @param items the projectiles, one stack of one item each
 */
public record ChargedProjectiles(List<ItemStack> items) {

    /**
     * The component, sent to clients so that they draw the crossbow loaded, and saved with the stack.
     */
    public static final DataComponentType<ChargedProjectiles> TYPE =
            DataComponentType.of(DataComponentTypeKeys.CHARGED_PROJECTILES, ChargedProjectiles.class);

    public ChargedProjectiles {
        items = List.copyOf(items);
    }

    /**
     * {@return {@code true} when the crossbow holds no projectile}
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
