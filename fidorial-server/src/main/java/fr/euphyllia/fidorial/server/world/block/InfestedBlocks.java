package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.mob.monster.silverfish.Silverfish;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.EnchantmentTagKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The blocks a silverfish hides in: each looks like the block it stands for, its host, and lets the
 * silverfish out when it is broken.
 */
@NeedsToBeRevisited("It will need to be replaced with the tags")
public final class InfestedBlocks {

    static final Map<TypedKey<BlockType>, TypedKey<BlockType>> INFESTED_BY_HOST = Map.of(
            BlockTypeKeys.STONE, BlockTypeKeys.INFESTED_STONE,
            BlockTypeKeys.COBBLESTONE, BlockTypeKeys.INFESTED_COBBLESTONE,
            BlockTypeKeys.STONE_BRICKS, BlockTypeKeys.INFESTED_STONE_BRICKS,
            BlockTypeKeys.MOSSY_STONE_BRICKS, BlockTypeKeys.INFESTED_MOSSY_STONE_BRICKS,
            BlockTypeKeys.CRACKED_STONE_BRICKS, BlockTypeKeys.INFESTED_CRACKED_STONE_BRICKS,
            BlockTypeKeys.CHISELED_STONE_BRICKS, BlockTypeKeys.INFESTED_CHISELED_STONE_BRICKS,
            BlockTypeKeys.DEEPSLATE, BlockTypeKeys.INFESTED_DEEPSLATE
    );
    private static final Map<TypedKey<BlockType>, TypedKey<BlockType>> HOST_BY_INFESTED = new HashMap<>();

    static {
        INFESTED_BY_HOST.forEach((host, infested) -> HOST_BY_INFESTED.put(infested, host));
    }

    private InfestedBlocks() {
        throw new UnsupportedOperationException("InfestedBlocks cannot be instantiated.");
    }

    /**
     * {@return {@code true} if a silverfish can hide in a block}
     */
    public static boolean isHost(final BlockState state) {
        return INFESTED_BY_HOST.containsKey(state.name());
    }

    /**
     * {@return {@code true} if a silverfish hides in a block}
     */
    public static boolean isInfested(final BlockState state) {
        return HOST_BY_INFESTED.containsKey(state.name());
    }

    /**
     * {@return the block a silverfish hiding in a host turns it into, {@code null} if it cannot hide
     * there}; the infested deepslate keeps the way the deepslate lies
     */
    public static @Nullable BlockState infested(final BlockState host) {
        final TypedKey<BlockType> infested = INFESTED_BY_HOST.get(host.name());
        return infested == null ? null : BlockState.withProperties(infested, host.properties());
    }

    /**
     * {@return the block an infested block stands for, {@code null} if it is not infested}
     */
    public static @Nullable BlockState host(final BlockState infested) {
        final TypedKey<BlockType> host = HOST_BY_INFESTED.get(infested.name());
        return host == null ? null : BlockState.withProperties(host, infested.properties());
    }

    /**
     * Lets the silverfish out of a block that was just broken, if it hid one: it appears in the
     * middle of the block, unless blocks drop nothing in this world or the tool keeps it in.
     *
     * @param world    the world of the block
     * @param position where the block was, owned by the calling thread
     * @param broken   the block that was broken
     * @param tool     what broke it, {@link ItemStack#EMPTY} for no tool
     */
    public static void afterBreak(final ServerWorld world, final BlockPosition position, final BlockState broken,
                                  final ItemStack tool) {
        if (!isInfested(broken) || !world.gameRules().getBoolean(GameRuleKeys.BLOCK_DROPS) || keepsSilverfishIn(tool)) {
            return;
        }
        final FidorialServer server = FidorialServer.getInstance();
        final Silverfish silverfish = Silverfish.leavingBlock(server.entities().ids().allocate(),
                Location.of(world, position.blockX() + 0.5, position.blockY(), position.blockZ() + 0.5, 0f, 0f));
        server.entities().spawn(silverfish);
        silverfish.spawnAnimation();
    }

    private static boolean keepsSilverfishIn(final ItemStack tool) {
        if (tool.isEmpty()) {
            return false;
        }
        final var enchantments = FidorialServer.getInstance().registries().enchantments().registry();
        for (final TypedKey<EnchantmentDefinition> enchantment
                : tool.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY).levels().keySet()) {
            if (enchantments.isTagged(enchantment, EnchantmentTagKeys.PREVENTS_INFESTED_SPAWNS)) {
                return true;
            }
        }
        return false;
    }
}
