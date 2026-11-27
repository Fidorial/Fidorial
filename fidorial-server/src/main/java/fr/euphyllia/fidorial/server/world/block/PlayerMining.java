package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.AttributeKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * How fast a player mines a block, for the server to tell whether the block a client says it has
 * mined could have been mined in the time it took.
 *
 * <p>The estimate counts everything that speeds a player up: the speed of the tool, the mining
 * efficiency its enchantments give, Haste and Conduit Power, and the block break speed attribute.
 * It leaves out what slows a player down, mining underwater, in the air or under Mining Fatigue,
 * so that it never asks a player for more time than their client took: a player slowed down only
 * gets the block later than the estimate would allow.</p>
 */
public final class PlayerMining {

    /**
     * How many times its hardness, in ticks, a block takes to mine at speed {@code 1} with a tool
     * that gets its drops.
     */
    private static final float HARVESTABLE_DIVISOR = 30.0f;

    /**
     * The same, with a tool that does not get its drops: stone with a bare hand.
     */
    private static final float UNHARVESTABLE_DIVISOR = 100.0f;

    /**
     * How much faster each level of Haste or Conduit Power mines.
     */
    private static final float HASTE_PER_LEVEL = 0.2f;

    private static final List<TypedKey<MobEffect>> HASTE_EFFECTS = List.of(MobEffectKeys.HASTE, MobEffectKeys.CONDUIT_POWER);

    private PlayerMining() {
        throw new UnsupportedOperationException("PlayerMining cannot be instantiated.");
    }

    /**
     * {@return how much of a block a player mines each tick with what they hold, at most: {@code 1}
     * or more for a block they break as soon as they start, {@code 0} for air and for a block no
     * player in survival mode breaks, such as bedrock}
     *
     * @param player the player mining
     * @param block  the block mined
     */
    public static float progressPerTick(final ServerPlayer player, final BlockData block) {
        if (block.isAir()) {
            return 0.0f;
        }
        final TypedKey<BlockType> type = block.key();
        final float hardness = BlockMining.hardness(type);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        if (hardness == 0.0f) {
            return Float.POSITIVE_INFINITY;
        }
        final ItemStack tool = player.heldItem();
        final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags = blockTags();
        float speed = BlockMining.toolSpeed(type, tool, blockTags);
        if (speed > 1.0f) {
            speed += (float) player.attributeValue(AttributeKeys.MINING_EFFICIENCY);
        }
        final int haste = hasteLevel(player);
        if (haste > 0) {
            speed *= 1.0f + HASTE_PER_LEVEL * haste;
        }
        speed *= (float) player.attributeValue(AttributeKeys.BLOCK_BREAK_SPEED);
        if (speed <= 0.0f) {
            return 0.0f;
        }
        final boolean harvestable = BlockMining.canHarvest(type, tool, blockTags);
        return speed / hardness / (harvestable ? HARVESTABLE_DIVISOR : UNHARVESTABLE_DIVISOR);
    }

    /**
     * {@return how many ticks of mining a player needs at {@code progressPerTick}, at least {@code 1};
     * {@link Long#MAX_VALUE} when they never get through}
     *
     * @param progressPerTick what {@link #progressPerTick} gave
     */
    public static long ticksToMine(final float progressPerTick) {
        if (progressPerTick <= 0.0f || Float.isNaN(progressPerTick)) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, (long) Math.ceil(1.0 / progressPerTick));
    }

    private static int hasteLevel(final ServerPlayer player) {
        int level = 0;
        for (final TypedKey<MobEffect> effect : HASTE_EFFECTS) {
            final MobEffectInstance instance = player.effect(effect).orElse(null);
            if (instance != null) {
                level = Math.max(level, instance.amplifier() + 1);
            }
        }
        return level;
    }

    private static BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags() {
        if (!FidorialServer.isInitialized()) {
            return (block, tag) -> false;
        }
        return FidorialServer.getInstance().registries().registry(RegistryKey.BLOCK)::isTagged;
    }
}
