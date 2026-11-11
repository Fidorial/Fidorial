package fr.euphyllia.fidorial.testplugin.farming;

import fr.fidorial.event.EventBus;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.farming.FarmingRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.Objects;

/**
 * What the plugin adds to farming: a plant of its own with its seed, a second fertilizer, and a
 * soil hoes could not till.
 */
public final class TestFarming {

    /**
     * Planted on end stone, it grows a {@link ChorusSprout}.
     */
    public static final TypedKey<Item> SEED = ItemKeys.POPPED_CHORUS_FRUIT;

    /**
     * Feeds plants the way bone meal does.
     */
    public static final TypedKey<Item> FERTILIZER = ItemKeys.DRAGON_BREATH;

    private TestFarming() {
    }

    public static void register(final FarmingRegistry farming, final EventBus events, final Plugin owner,
                                final ComponentLogger logger) {
        farming.attach(ChorusSprout.BLOCK, new ChorusSprout(), owner);
        farming.seed(SEED, ChorusSprout.BLOCK, owner);
        farming.fertilizer(FERTILIZER, owner);
        final BlockData farmland = Objects.requireNonNull(Blocks.data("minecraft:farmland"), "farmland is not registered");
        farming.tillable(BlockTypeKeys.MYCELIUM, () -> farmland, owner);

        events.subscribe(HarvestEvent.class, event -> {
            if (event.block().key().equals(ChorusSprout.BLOCK)) {
                logger.debug("[TestPlugin][event] HarvestEvent: a chorus sprout gives {} at {}", event.drops(), event.position());
            }
        });
        logger.info("[TestPlugin] Farming: {} plants a chorus sprout, {} fertilizes, hoes till mycelium",
                SEED.key().asString(), FERTILIZER.key().asString());
    }

    public static void unregisterAll(final FarmingRegistry farming, final Plugin owner) {
        farming.detachAll(owner);
    }
}
