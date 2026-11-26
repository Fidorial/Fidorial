package fr.euphyllia.fidorial.server.world.block.chest;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.BlockChestInventory;
import fr.euphyllia.fidorial.server.inventory.ChestMenu;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.ContainerBlocks;
import fr.euphyllia.fidorial.server.world.redstone.Faces;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.type.Chest;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A chest: 27 slots kept in its block entity, joined with a chest of its {@linkplain ChestFamily
 * family} standing beside it and facing the same way into a large chest of 54.
 *
 * <p>Placed without sneaking, a chest joins the lone chest on the left of the player placing it,
 * else the one on their right. Sneaking, it stays alone, unless it is placed against the side of a
 * lone chest: it then turns the way that chest faces and joins it. The chest next to it follows
 * through its shape update, which also splits a large chest when one half goes.</p>
 *
 * <p>A chest opens unless a solid block sits on the lid of one of its halves. Its contents spill on
 * the ground when it is removed, and a comparator reads how full the whole large chest is.</p>
 *
 * <p>The copper chests join each other whatever their oxidation: the more oxidized half takes the
 * oxidation of the other one, keeping its wax and its contents.</p>
 */
public class ChestBlock implements BlockBehaviour, RedstoneBehaviour {

    private final TypedKey<BlockType> key;
    private final ChestFamily family;
    private final int oxidation;
    private final boolean waxed;
    private final TypedKey<SoundEvent> openSound;
    private final TypedKey<SoundEvent> closeSound;

    /**
     * @param key        the block type
     * @param family     the chests it joins
     * @param oxidation  how oxidized a copper chest is, from {@code 0} (none) to {@code 3}; {@code 0}
     *                   for the other chests
     * @param waxed      whether a copper chest is waxed
     * @param openSound  what plays when the first player opens it
     * @param closeSound what plays when the last player closes it
     */
    ChestBlock(final TypedKey<BlockType> key, final ChestFamily family, final int oxidation, final boolean waxed,
               final TypedKey<SoundEvent> openSound, final TypedKey<SoundEvent> closeSound) {
        Preconditions.checkArgument(key != null, "A chest needs the block type it drives");
        Preconditions.checkArgument(family != null, "The chest %s needs the family of chests it joins", key);
        Preconditions.checkArgument(oxidation >= 0 && oxidation <= 3,
                "The oxidation of the chest %s goes from 0 to 3, not %s", key, oxidation);
        Preconditions.checkArgument(family == ChestFamily.COPPER || (oxidation == 0 && !waxed),
                "Only a copper chest is oxidized or waxed, not %s", key);
        Preconditions.checkArgument(openSound != null, "The chest %s needs the sound it opens with", key);
        Preconditions.checkArgument(closeSound != null, "The chest %s needs the sound it closes with", key);
        this.key = key;
        this.family = family;
        this.oxidation = oxidation;
        this.waxed = waxed;
        this.openSound = openSound;
        this.closeSound = closeSound;
    }

    @Override
    public final BlockType type() {
        final BlockType type = Blocks.type(key);
        Preconditions.checkState(type != null, "The block type %s is not registered", key);
        return type;
    }

    @Override
    public final TypedKey<BlockType> key() {
        return key;
    }

    public final ChestFamily family() {
        return family;
    }

    /**
     * {@return how oxidized the copper chest is, from {@code 0} to {@code 3}, {@code 0} for the
     * other chests}
     */
    public final int oxidation() {
        return oxidation;
    }

    public final boolean waxed() {
        return waxed;
    }

    public final TypedKey<SoundEvent> openSound() {
        return openSound;
    }

    public final TypedKey<SoundEvent> closeSound() {
        return closeSound;
    }

    /**
     * {@return {@code true} if {@code other} is a chest of the family of this one, which it may form
     * a large chest with}
     *
     * @param other any block state, {@code null} for an unloaded one
     */
    public final boolean joins(final @Nullable BlockData other) {
        if (!(other instanceof Chest)) {
            return false;
        }
        final ChestBlock behaviour = ChestBlocks.behaviour(other.key());
        return behaviour != null && behaviour.family == family;
    }

    /**
     * Turns the front toward the player, and joins a lone chest beside it, as the class tells.
     */
    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData defaults = type().defaultData();
        if (!(defaults instanceof final Chest chest)) {
            return defaults;
        }
        BlockFace facing = context.horizontalFacing().opposite();
        Chest.Type type = Chest.Type.SINGLE;
        Chest partner = null;
        if (context.sneaking()) {
            final BlockFace clicked = context.clickedFace();
            final BlockFace toward = clicked.opposite();
            if (Faces.isHorizontal(clicked) && context.relative(toward) instanceof final Chest against && joinsAlone(against)) {
                final BlockFace front = against.getFacing();
                // the chest clicked must stand on the left or on the right of the new one, not behind it
                if (front != clicked && front != toward) {
                    facing = front;
                    type = Faces.clockWise(front) == toward ? Chest.Type.LEFT : Chest.Type.RIGHT;
                    partner = against;
                }
            }
        } else {
            // first the chest on the left of the player, which is on the right of the new chest
            for (final Chest.Type candidate : List.of(Chest.Type.LEFT, Chest.Type.RIGHT)) {
                final BlockFace side = Chest.partnerDirection(facing, candidate).orElseThrow();
                if (context.relative(side) instanceof final Chest neighbour && joinsAlone(neighbour)
                        && neighbour.getFacing() == facing) {
                    type = candidate;
                    partner = neighbour;
                    break;
                }
            }
        }
        final Chest placed = chest.withFacing(facing).withType(type).withWaterlogged(context.intoWater());
        return partner == null ? placed : alignedWith(placed, partner);
    }

    private boolean joinsAlone(final Chest other) {
        return joins(other) && other.getType() == Chest.Type.SINGLE;
    }

    /**
     * Follows the chest beside it: joins it when it points at this lone chest, and stands alone again
     * when the chest this one pointed at no longer points back.
     */
    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (!(state instanceof final Chest chest) || !Faces.isHorizontal(direction)) {
            return state;
        }
        final boolean pointsThere = chest.partnerDirection().filter(direction::equals).isPresent();
        if (neighbor instanceof final Chest other && joins(other) && other.getFacing() == chest.getFacing()
                && other.partnerDirection().filter(direction.opposite()::equals).isPresent()) {
            if (chest.getType() == Chest.Type.SINGLE) {
                return alignedWith(chest.withType(other.getType().opposite()), other);
            }
            return pointsThere ? alignedWith(chest, other) : state;
        }
        return pointsThere ? chest.withType(Chest.Type.SINGLE) : state;
    }

    /**
     * {@return {@code state}, as the block of the oxidation of {@code partner} when this is a copper
     * chest more oxidized than its partner}
     *
     * @param state   a state of this block
     * @param partner the other half of the large chest
     */
    private Chest alignedWith(final Chest state, final Chest partner) {
        if (family != ChestFamily.COPPER) {
            return state;
        }
        final ChestBlock other = ChestBlocks.behaviour(partner.key());
        if (other == null || other.oxidation >= oxidation) {
            return state;
        }
        return ChestBlocks.copper(other.oxidation, waxed).stateLike(state);
    }

    /**
     * {@return the state of this block that faces, holds water and joins like {@code source}}
     *
     * @param source a state of any chest
     */
    final Chest stateLike(final Chest source) {
        final BlockData defaults = type().defaultData();
        Preconditions.checkArgument(defaults != null, "The block type %s has no default state", key);
        Preconditions.checkArgument(defaults instanceof Chest, "The default state of %s has none of the properties of a chest", key);
        return ((Chest) defaults).withFacing(source.getFacing()).withType(source.getType())
                .withWaterlogged(source.isWaterlogged());
    }

    /**
     * A chest turning into another chest of its family keeps what it holds.
     */
    @Override
    public boolean keepsBlockEntity(final BlockData state, final BlockData replacement) {
        return joins(replacement);
    }

    /**
     * Opens the chest, or the large chest it is half of, unless a solid block sits on a lid.
     */
    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(player instanceof final ServerPlayer serverPlayer) || !(player.world() instanceof final ServerWorld world)
                || !(data instanceof final Chest chest)) {
            return false;
        }
        final List<BlockPosition> halves = ChestBlocks.halves(world, position, chest);
        for (final BlockPosition half : halves) {
            if (ChestBlocks.isBlockedAbove(world, half)) {
                return true;
            }
        }
        final Container contents = ChestBlocks.contents(world, halves);
        serverPlayer.openMenu(new ChestMenu(serverPlayer, serverPlayer.allocateWindowId(), world, position, this,
                halves, contents, ChestBlocks.title(contents)));
        return true;
    }

    /**
     * Like vanilla: the 27 slots spill on the ground where the chest stood, its loot rolled first
     * when it still had a table.
     */
    @Override
    public void onRemove(final World world, final BlockPosition position, final BlockData data) {
        if (!(world instanceof final ServerWorld serverWorld) || !FidorialServer.isInitialized()) {
            return;
        }
        ContainerBlocks.spill(new BlockChestInventory(serverWorld, position));
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(key);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }

    @Override
    public boolean hasAnalogOutput(final BlockData state) {
        return true;
    }

    /**
     * How full the chest is, or the whole large chest it is half of.
     */
    @Override
    public int analogOutput(final SignalGetter world, final BlockPosition position, final BlockData state,
                            final BlockFace direction) {
        if (!(world.world() instanceof final ServerWorld serverWorld) || !(state instanceof final Chest chest)) {
            return 0;
        }
        return ContainerBlocks.signal(ChestBlocks.contents(serverWorld, ChestBlocks.halves(serverWorld, position, chest)));
    }

    /**
     * A chest joining or leaving a large chest, or taking another oxidation, changes what the
     * comparators beside it read: the shape updates making these changes tell nobody else.
     */
    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                        final BlockData previous) {
        if (state instanceof final Chest now && previous instanceof final Chest before && joins(before)
                && (!before.key().equals(now.key()) || before.getType() != now.getType())) {
            world.analogOutputChanged(position);
        }
    }
}
