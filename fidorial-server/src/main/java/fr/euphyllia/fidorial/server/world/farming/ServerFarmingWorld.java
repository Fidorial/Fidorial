package fr.euphyllia.fidorial.server.world.farming;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Waterlogged;
import fr.fidorial.world.block.data.type.Farmland;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;
import fr.fidorial.world.farming.Tillage;
import fr.fidorial.world.time.DayNightCycle;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * The farming of one world, as {@link FidorialFarmingEngine} drives it.
 *
 * <p>Reads only look at loaded chunks. Writes and behaviour calls happen on the thread owning the
 * block: a change reaching a plant of another region is handed over to that region, and dropped
 * when its chunk is not loaded.</p>
 */
public final class ServerFarmingWorld implements FarmingWorld {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ServerFarmingWorld.class);

    private static final BlockFace[] FACES = BlockFace.values();

    /**
     * The blocks that are full of water without saying so in their state.
     */
    private static final Set<TypedKey<BlockType>> ALWAYS_WET = Set.of(
            BlockTypeKeys.WATER, BlockTypeKeys.BUBBLE_COLUMN, BlockTypeKeys.KELP, BlockTypeKeys.KELP_PLANT,
            BlockTypeKeys.SEAGRASS, BlockTypeKeys.TALL_SEAGRASS);

    private static final int MAX_LIGHT = 15;

    /**
     * How much light the sky loses at midnight.
     */
    private static final int NIGHT_DARKENING = 11;

    /**
     * How deep the plants around a change may break one another before the rest is put off to a
     * later task of the region, so that a long row of plants never exhausts the stack.
     */
    private static final int MAX_CASCADE_DEPTH = 256;

    private static final ThreadLocal<int[]> CASCADE_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private final FidorialFarmingEngine engine;
    private final ServerWorld world;
    private final BlockData air;
    private final BlockData water;

    ServerFarmingWorld(final FidorialFarmingEngine engine, final ServerWorld world) {
        this.engine = engine;
        this.world = world;
        this.air = Objects.requireNonNull(engine.blockStates().resolve(BlockState.withoutProperties(BlockTypeKeys.AIR)),
                "minecraft:air is not registered");
        this.water = Objects.requireNonNull(
                engine.blockStates().resolve(BlockState.withProperties(BlockTypeKeys.WATER, Map.of("level", "0"))),
                "minecraft:water is not registered");
    }

    @Override
    public World world() {
        return world;
    }

    /**
     * {@return the state of air, what a block that can no longer stand leaves behind}
     */
    public BlockData air() {
        return air;
    }

    @Override
    public RandomGenerator random() {
        return ThreadLocalRandom.current();
    }

    @Override
    public @Nullable BlockData blockAt(final BlockPosition position) {
        final int y = position.blockY();
        if (y < world.minY() || y >= world.minY() + world.height()) {
            return air;
        }
        final ChunkColumn column = world.loadedColumn(position.blockX() >> 4, position.blockZ() >> 4);
        if (column == null) {
            return null;
        }
        return engine.blockStates().resolve(column.getBlock(position.blockX() & 15, y, position.blockZ() & 15));
    }

    @Override
    public boolean isTagged(final BlockPosition position, final TagKey<BlockType> tag) {
        final BlockData state = blockAt(position);
        return state != null && engine.isTagged(state.key(), tag);
    }

    @Override
    public int lightLevel(final BlockPosition position) {
        if (!isLightKnown(position)) {
            return MAX_LIGHT;
        }
        final int x = position.blockX();
        final int y = position.blockY();
        final int z = position.blockZ();
        final int block = world.blockLightAt(x, y, z);
        if (block >= MAX_LIGHT || !world.dimensionType().hasSkylight()) {
            return block;
        }
        return Math.max(block, world.skyLightAt(x, y, z) - skyDarkening());
    }

    /**
     * {@return {@code true} once the light of the chunk of a block was worked out}: until then a
     * plant is taken to stand in full light, rather than uprooted for a darkness that is only the
     * light engine running late
     */
    private boolean isLightKnown(final BlockPosition position) {
        final ChunkColumn column = world.loadedColumn(position.chunkX(), position.chunkZ());
        return column != null && column.lightPopulated();
    }

    /**
     * {@return how much light the sky loses right now, from {@code 0} at noon under a clear sky to
     * {@value #NIGHT_DARKENING} at midnight}: the sun gives its full light for most of the day,
     * fades over dusk and dawn, and rain then thunder dim it further
     */
    private int skyDarkening() {
        final double dayFraction = world.dayNightCycle().timeOfDay() / (double) DayNightCycle.DAY_LENGTH;
        // the sun is at its highest a quarter of a day after it rises
        final double height = Math.cos((dayFraction - 0.25) * 2.0 * Math.PI);
        double brightness = 0.5 + 2.0 * Math.clamp(height, -0.25, 0.25);
        if (world.weather().isRaining()) {
            brightness *= 1.0 - 5.0 / 16.0;
        }
        if (world.weather().isThundering()) {
            brightness *= 1.0 - 5.0 / 16.0;
        }
        return (int) ((1.0 - brightness) * NIGHT_DARKENING);
    }

    @Override
    public boolean canSeeSky(final BlockPosition position) {
        return world.dimensionType().hasSkylight() && (!isLightKnown(position)
                || world.skyLightAt(position.blockX(), position.blockY(), position.blockZ()) >= MAX_LIGHT);
    }

    @Override
    public boolean isRainingAt(final BlockPosition position) {
        return world.weather().isRaining() && canSeeSky(position) && engine.hasPrecipitation(world, position);
    }

    @Override
    public boolean hasWater(final BlockPosition position) {
        final BlockData state = blockAt(position);
        if (state == null) {
            return false;
        }
        return ALWAYS_WET.contains(state.key()) || (state instanceof final Waterlogged holder && holder.isWaterlogged());
    }

    @Override
    public float growthSpeed(final BlockPosition position) {
        final BlockPosition below = position.relative(BlockFace.DOWN);
        float speed = 1.0f;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final BlockData soil = blockAt(below.offset(dx, 0, dz));
                if (soil == null || !engine.isTagged(soil.key(), BlockTypeTagKeys.GROWS_CROPS)) {
                    continue;
                }
                float share = soil instanceof final Farmland farmland && farmland.getMoisture() > 0 ? 3.0f : 1.0f;
                if (dx != 0 || dz != 0) {
                    share /= 4.0f;
                }
                speed += share;
            }
        }
        final BlockData plant = blockAt(position);
        if (plant == null || plant.isAir()) {
            return speed; // nothing planted yet: nothing to be crowded by
        }
        final TypedKey<BlockType> kind = plant.key();
        final boolean alongX = isKind(position.offset(-1, 0, 0), kind) || isKind(position.offset(1, 0, 0), kind);
        final boolean alongZ = isKind(position.offset(0, 0, -1), kind) || isKind(position.offset(0, 0, 1), kind);
        final boolean crowded = (alongX && alongZ)
                || isKind(position.offset(-1, 0, -1), kind) || isKind(position.offset(1, 0, -1), kind)
                || isKind(position.offset(1, 0, 1), kind) || isKind(position.offset(-1, 0, 1), kind);
        return crowded ? speed / 2.0f : speed;
    }

    private boolean isKind(final BlockPosition position, final TypedKey<BlockType> kind) {
        final BlockData state = blockAt(position);
        return state != null && state.key().equals(kind);
    }

    @Override
    public boolean canSurvive(final BlockPosition position, final BlockData state) {
        final FarmingBehaviour behaviour = engine.registry().behaviourOrNull(state.key());
        if (behaviour == null) {
            return true;
        }
        try {
            return behaviour.canSurvive(this, position, state);
        } catch (final RuntimeException exception) {
            fail(state, "was asked whether it can stand", position, exception);
            return true;
        }
    }

    @Override
    public boolean setBlock(final BlockPosition position, final BlockData state) {
        Preconditions.checkArgument(state != null, "The state passed to FarmingWorld.setBlock() must not be null");
        if (!owns(position)) {
            return false;
        }
        final BlockData previous = blockAt(position);
        if (previous == null || previous.equals(state)) {
            return false;
        }
        return engine.blockEdits().set(world, position, engine.blockStates().toBlockState(state), true);
    }

    @Override
    public boolean grow(final BlockPosition plant, final BlockPosition target, final BlockData grown, final boolean fertilized) {
        Preconditions.checkArgument(grown != null, "The state passed to FarmingWorld.grow() must not be null");
        final BlockData growing = blockAt(plant);
        if (growing == null || !owns(target) || blockAt(target) == null) {
            return false;
        }
        final BlockData result = FarmingEvents.grow(world, plant, growing, target, grown, fertilized);
        return result != null && setBlock(target, result);
    }

    @Override
    public boolean destroy(final BlockPosition position) {
        if (!owns(position)) {
            return false;
        }
        final BlockData state = blockAt(position);
        if (state == null || state.isAir()) {
            return false;
        }
        harvest(position, null, ItemStack.EMPTY);
        final BlockData left = state instanceof final Waterlogged holder && holder.isWaterlogged() ? water : air;
        if (!setBlock(position, left)) {
            return false;
        }
        world.sendToChunkViewers(position.chunkX(), position.chunkZ(),
                new ClientboundLevelEventPacket(ClientboundLevelEventPacket.BLOCK_BREAK, position, state.networkId(), false));
        FarmingEffects.gameEvent(world, GameEventKeys.BLOCK_DESTROY, position);
        return true;
    }

    @Override
    public boolean harvest(final BlockPosition position, final @Nullable Player player, final ItemStack tool) {
        Preconditions.checkArgument(tool != null, "The tool passed to FarmingWorld.harvest() must not be null");
        final BlockData state = blockAt(position);
        if (state == null || !owns(position)) {
            return false;
        }
        final FarmingBehaviour behaviour = engine.registry().behaviourOrNull(state.key());
        if (behaviour == null || !world.gameRules().getBoolean(GameRuleKeys.BLOCK_DROPS)) {
            return false;
        }
        final List<ItemStack> drops;
        try {
            drops = behaviour.drops(this, position, state, tool);
        } catch (final RuntimeException exception) {
            fail(state, "was harvested", position, exception);
            return false;
        }
        return dropHarvest(position, state, player, tool, drops);
    }

    /**
     * Drops a harvest once its {@link fr.fidorial.event.farming.HarvestEvent} was fired: what
     * {@link #harvest} does with the drops of a behaviour, and what a block picked without being
     * broken does with its fruits.
     *
     * @param position where the block is
     * @param state    the state giving the harvest
     * @param player   the player harvesting, {@code null} for none
     * @param tool     what the block is harvested with
     * @param drops    what the block gives
     * @return {@code true} if at least one stack was dropped
     */
    public boolean dropHarvest(final BlockPosition position, final BlockData state, final @Nullable Player player,
                               final ItemStack tool, final List<ItemStack> drops) {
        final List<ItemStack> dropped = FarmingEvents.harvest(world, position, state, player, tool, drops);
        boolean any = false;
        for (final ItemStack stack : dropped) {
            if (!stack.isEmpty()) {
                dropItem(position, stack);
                any = true;
            }
        }
        return any;
    }

    @Override
    public void dropItem(final BlockPosition position, final ItemStack stack) {
        Preconditions.checkArgument(stack != null, "The stack passed to FarmingWorld.dropItem() must not be null");
        if (stack.isEmpty() || !FidorialServer.isInitialized() || !owns(position)
                || world.loadedColumn(position.chunkX(), position.chunkZ()) == null) {
            return;
        }
        world.dropItem(Location.of(world, position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5), stack);
    }

    @Override
    public void scheduleTick(final BlockPosition position, final int delay) {
        final BlockData state = blockAt(position);
        if (state == null) {
            return;
        }
        final TypedKey<BlockType> block = state.key();
        engine.regionizer().executeDelayed(world.key(), position.chunk(),
                () -> engine.runTick(world, position, block), Math.max(1, delay));
    }

    @Override
    public void blockChanged(final BlockPosition position, final BlockData previous, final BlockData current) {
        if (previous.equals(current)) {
            return;
        }
        for (final BlockFace face : FACES) {
            // the neighbour on that side sees this block on its opposite side
            neighborChanged(position.relative(face), face.opposite(), current);
        }
    }

    /**
     * Tells one block that its neighbour on one side changed: it breaks if it can no longer stand,
     * and follows the change otherwise.
     */
    private void neighborChanged(final BlockPosition position, final BlockFace direction, final BlockData neighbor) {
        if (!owns(position)) {
            handOver(position, () -> neighborChanged(position, direction, neighbor));
            return;
        }
        final BlockData state = blockAt(position);
        if (state == null || state.isAir()) {
            return;
        }
        final FarmingBehaviour behaviour = engine.registry().behaviourOrNull(state.key());
        if (behaviour == null) {
            return;
        }
        final int[] depth = CASCADE_DEPTH.get();
        if (depth[0] >= MAX_CASCADE_DEPTH) {
            handOver(position, () -> neighborChanged(position, direction, neighbor));
            return;
        }
        depth[0]++;
        try {
            if (!behaviour.canSurvive(this, position, state)) {
                destroy(position);
                return;
            }
            final BlockData updated = behaviour.updateShape(this, position, state, direction, neighbor);
            if (updated.equals(state)) {
                return;
            }
            if (updated.isAir()) {
                destroy(position);
            } else {
                setBlock(position, updated);
            }
        } catch (final RuntimeException exception) {
            fail(state, "followed a neighbour", position, exception);
        } finally {
            depth[0]--;
        }
    }

    @Override
    public boolean useItem(final Player player, final int slot, final BlockPosition position, final BlockFace face) {
        if (!owns(position)) {
            return false;
        }
        final ItemStack held = player.inventory().get(slot);
        final BlockData state = blockAt(position);
        if (held.isEmpty() || state == null) {
            return false;
        }
        final FidorialFarmingRegistry registry = engine.registry();
        if (registry.isFertilizer(held.id())) {
            return fertilize(player, slot, held, position, state);
        }
        if (registry.isHoe(held.id())) {
            return till(player, slot, held, position, state, face);
        }
        return false;
    }

    private boolean fertilize(final Player player, final int slot, final ItemStack held, final BlockPosition position,
                              final BlockData state) {
        final FarmingBehaviour behaviour = engine.registry().behaviourOrNull(state.key());
        if (behaviour == null) {
            return false;
        }
        try {
            if (!behaviour.canFertilize(this, position, state)) {
                return false;
            }
            if (!FarmingEvents.fertilize(world, position, state, player, held)) {
                player.updateInventory(); // the client already took one out of the stack
                return true;
            }
            if (player.gameMode() != GameMode.CREATIVE) {
                player.inventory().set(slot, held.count() <= 1 ? ItemStack.EMPTY : held.withCount(held.count() - 1));
            }
            behaviour.fertilize(this, position, state);
        } catch (final RuntimeException exception) {
            fail(state, "was fertilized", position, exception);
            return true;
        }
        FarmingEffects.levelEvent(world, FarmingEffects.FERTILIZER_USE, position, FarmingEffects.FERTILIZER_PARTICLES);
        FarmingEffects.gameEvent(world, GameEventKeys.ITEM_INTERACT_FINISH, position);
        return true;
    }

    private boolean till(final Player player, final int slot, final ItemStack held, final BlockPosition position,
                         final BlockData state, final BlockFace face) {
        final Tillage tillage = engine.registry().tillage(state.key()).orElse(null);
        if (tillage == null) {
            return false;
        }
        if (tillage.needsRoom()) {
            final BlockData above = blockAt(position.relative(BlockFace.UP));
            if (face == BlockFace.DOWN || above == null || !above.isAir()) {
                return false;
            }
        }
        final BlockData tilled = FarmingEvents.till(world, position, state, player, held, tillage.tilled());
        if (tilled == null) {
            player.updateInventory();
            return true;
        }
        if (!setBlock(position, tilled)) {
            return false;
        }
        FarmingEffects.playSound(world, position, SoundEventKeys.ITEM_HOE_TILL, 1.0f, 1.0f, player);
        FarmingEffects.gameEvent(world, GameEventKeys.BLOCK_CHANGE, position);
        for (final ItemStack stack : tillage.drops(random())) {
            dropItem(position.relative(face), stack);
        }
        if (player.gameMode() != GameMode.CREATIVE) {
            player.inventory().set(slot, worn(held));
        }
        return true;
    }

    /**
     * {@return a tool after one more use: one point closer to breaking, or gone once it broke}
     */
    private static ItemStack worn(final ItemStack tool) {
        final int durability = ItemDefaults.maxDamage(tool.id(), tool);
        if (durability <= 0) {
            return tool; // a tool that never wears
        }
        final int damage = tool.getOrDefault(DataComponentTypes.DAMAGE, 0) + 1;
        return damage >= durability ? ItemStack.EMPTY : tool.with(DataComponentTypes.DAMAGE, damage);
    }

    @Override
    public void entityLanded(final Entity entity, final BlockPosition position, final double fallDistance) {
        if (!owns(position)) {
            return;
        }
        final BlockData state = blockAt(position);
        if (state == null) {
            return;
        }
        final FarmingBehaviour behaviour = engine.registry().behaviourOrNull(state.key());
        if (behaviour == null) {
            return;
        }
        try {
            behaviour.entityLanded(this, position, state, entity, fallDistance);
        } catch (final RuntimeException exception) {
            fail(state, "was landed on", position, exception);
        }
    }

    /**
     * {@return {@code true} if the current thread owns a block, and may change it}
     *
     * @param position the block
     */
    public boolean owns(final BlockPosition position) {
        return engine.regionizer().isOwnedByCurrentThread(world.key(), position.chunk());
    }

    /**
     * Hands a task over to the region owning {@code position}, unless its chunk is not loaded:
     * there is nothing to update there.
     */
    private void handOver(final BlockPosition position, final Runnable task) {
        if (world.loadedColumn(position.chunkX(), position.chunkZ()) != null) {
            engine.regionizer().execute(world.key(), position.chunk(), task);
        }
    }

    ServerWorld serverWorld() {
        return world;
    }

    private void fail(final BlockData state, final String what, final BlockPosition position, final RuntimeException exception) {
        LOGGER.error("The farming behaviour of {} failed when it {} at {} in {}", state.key(), what, position, world.key(), exception);
    }
}
