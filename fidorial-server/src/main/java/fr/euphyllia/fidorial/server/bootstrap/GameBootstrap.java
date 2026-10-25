package fr.euphyllia.fidorial.server.bootstrap;

import fr.euphyllia.fidorial.server.debug.DebugChannels;
import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.registry.data.ItemProperties;
import fr.euphyllia.fidorial.server.world.block.ClusterBlock;
import fr.euphyllia.fidorial.server.world.block.CrafterBlock;
import fr.euphyllia.fidorial.server.world.block.CraftingTableBlock;
import fr.euphyllia.fidorial.server.world.block.EnderChestBlock;
import fr.euphyllia.fidorial.server.world.block.FidorialBlockRegistry;
import fr.euphyllia.fidorial.server.world.block.IcicleBlock;
import fr.euphyllia.fidorial.server.world.block.SpeleothemBlock;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

public final class GameBootstrap {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(GameBootstrap.class);

    private static volatile @Nullable FidorialBlockRegistry blockRegistry;
    private static @Nullable Throwable failure; // guarded by GameBootstrap.class

    private GameBootstrap() {
        throw new UnsupportedOperationException("GameBootstrap cannot be instantiated.");
    }

    /**
     * Initialises the static game data if it has not been done yet.
     */
    public static void bootstrap() {
        if (blockRegistry != null) {
            return;
        }
        synchronized (GameBootstrap.class) {
            if (blockRegistry != null) {
                return;
            }
            if (failure != null) {
                // The static holders may be half-filled: retrying would register things twice.
                throw new IllegalStateException("The game bootstrap already failed, restart the JVM", failure);
            }
            try {
                blockRegistry = runOnce();
            } catch (final RuntimeException | Error e) {
                failure = e;
                throw e;
            }
        }
    }

    /**
     * {@return the block registry built by {@link #bootstrap()}}
     *
     * @throws IllegalStateException if {@link #bootstrap()} has not run yet
     */
    public static FidorialBlockRegistry blockRegistry() {
        final FidorialBlockRegistry registry = blockRegistry;
        if (registry == null) {
            throw new IllegalStateException("GameBootstrap.bootstrap() must run before the block registry is used");
        }
        return registry;
    }

    public static boolean isBootstrapped() {
        return blockRegistry != null;
    }

    private static FidorialBlockRegistry runOnce() {
        // Data fixers are built asynchronously: start them first so they are ready by the time a world loads.
        DataFixersRegistry.bootstrap();
        bootstrapItems();
        final FidorialBlockRegistry blocks = bootstrapBlocks();
        // Relies on the frozen registries only, but kept last so that every block/item table exists.
        DebugChannels.bootstrap();
        return blocks;
    }

    private static void bootstrapItems() {
        ItemProperties.bootstrap();
        ItemDefaults.install(new ItemDefaults.Source() {

            @Override
            public int maxStackSize(final Key item) {
                return ItemProperties.maxStackSize(item);
            }

            @Override
            public int maxDamage(final Key item) {
                return ItemProperties.maxDamage(item);
            }

            @Override
            public @Nullable EquipmentSlot equipmentSlot(final Key item) {
                return EquipmentSlot.byName(ItemProperties.equipmentSlot(item));
            }
        });
    }

    private static FidorialBlockRegistry bootstrapBlocks() {
        final FidorialBlockRegistry registry = new FidorialBlockRegistry();
        BlockStateIds.registerAll(registry);
        BlockStates.bootstrap(registry);
        BlockStateLightProperties.bootstrap();
        Blocks.bootstrap(registry);
        registerBlockBehaviours(registry);
        LOGGER.debug("{} blocks defined in code", registry.definedCount());
        return registry;
    }

    private static void registerBlockBehaviours(final FidorialBlockRegistry registry) {
        registry.register(CraftingTableBlock.INSTANCE);
        registry.register(CrafterBlock.INSTANCE);
        registry.register(EnderChestBlock.INSTANCE);
        registry.register(ClusterBlock.AMETHYST_CLUSTER);
        registry.register(ClusterBlock.LARGE_AMETHYST_BUD);
        registry.register(ClusterBlock.MEDIUM_AMETHYST_BUD);
        registry.register(ClusterBlock.SMALL_AMETHYST_BUD);
        registry.register(ClusterBlock.ICE_CRYSTAL);
        registry.register(SpeleothemBlock.POINTED_DRIPSTONE);
        registry.register(SpeleothemBlock.SULFUR_SPIKE);
        registry.register(IcicleBlock.INSTANCE);
    }
}
