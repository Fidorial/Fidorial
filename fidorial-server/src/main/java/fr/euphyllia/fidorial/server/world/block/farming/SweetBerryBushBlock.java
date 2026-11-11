package fr.euphyllia.fidorial.server.world.block.farming;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.farming.FarmingEffects;
import fr.euphyllia.fidorial.server.world.farming.ServerFarmingWorld;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * A sweet berry bush: it grows on soil of the {@code minecraft:supports_vegetation} tag, one stage
 * every fifth random tick on average while the block above it is lit, and bears berries from its
 * {@value #BEARING_AGE}th stage on.
 *
 * <p>A player using the bush picks the berries without breaking it: it goes back to the stage
 * right before bearing.</p>
 */
public final class SweetBerryBushBlock implements BlockBehaviour, FarmingBehaviour {

    public static final SweetBerryBushBlock INSTANCE = new SweetBerryBushBlock();

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.SWEET_BERRY_BUSH;

    /**
     * The first stage with berries to pick.
     */
    private static final int BEARING_AGE = 2;
    private static final int GROWTH_ODDS = 5;

    private SweetBerryBushBlock() {
    }

    @Override
    public TypedKey<BlockType> key() {
        return KEY;
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(KEY);
        Preconditions.checkState(type != null, "The block type %s is not registered", KEY);
        return type;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(KEY);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    /**
     * Picks the berries of a bush bearing some. A bush that is not at its last stage is left to the
     * fertilizer a player holds, so that it grows rather than being picked.
     */
    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        final int age = Farming.age(data);
        final boolean ripe = age >= Farming.maxAge(data);
        if (age < BEARING_AGE || !FidorialServer.isInitialized()) {
            return false;
        }
        final FidorialServer server = FidorialServer.getInstance();
        if (!ripe && server.registries().farming().isFertilizer(player.heldItem().id())) {
            return false;
        }
        final FarmingWorld world = server.worlds().farming().world(player.world());
        final int berries = 1 + world.random().nextInt(2) + (ripe ? 1 : 0);
        final List<ItemStack> picked = List.of(ItemStack.of(ItemKeys.SWEET_BERRIES, berries));
        if (world instanceof final ServerFarmingWorld farming) {
            farming.dropHarvest(position, data, player, player.heldItem(), picked);
        } else {
            picked.forEach(stack -> world.dropItem(position, stack));
        }
        FarmingEffects.playSound(player.world(), position, SoundEventKeys.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES,
                1.0f, 0.8f + world.random().nextFloat() * 0.4f, null);
        world.setBlock(position, Farming.withAge(data, BEARING_AGE - 1));
        FarmingEffects.gameEvent(player.world(), GameEventKeys.BLOCK_CHANGE, position);
        return true;
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (world.random().nextInt(GROWTH_ODDS) == 0
                && world.lightLevel(position.relative(BlockFace.UP)) >= Farming.GROWTH_LIGHT) {
            world.grow(position, Farming.withAge(state, Farming.age(state) + 1));
        }
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return world.isTagged(position.relative(BlockFace.DOWN), BlockTypeTagKeys.SUPPORTS_VEGETATION);
    }

    @Override
    public boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        world.grow(position, position, Farming.withAge(state, Farming.age(state) + 1), true);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
