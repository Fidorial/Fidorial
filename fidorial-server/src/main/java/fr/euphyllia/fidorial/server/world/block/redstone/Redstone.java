package fr.euphyllia.fidorial.server.world.block.redstone;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.redstone.MovingPistons;
import fr.euphyllia.fidorial.server.world.redstone.ServerRedstoneWorld;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneEngine;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * What the vanilla redstone blocks need from the server: the engine in effect, the behaviours of
 * other blocks and block states.
 */
final class Redstone {

    private Redstone() {
        throw new UnsupportedOperationException("Redstone cannot be instantiated.");
    }

    /**
     * {@return the redstone of a world, as the {@link RedstoneEngine} in effect drives it, which may
     * be a plugin's}
     *
     * @param world the world
     */
    static RedstoneWorld world(final World world) {
        return FidorialServer.getInstance().worlds().redstone().world(world);
    }

    /**
     * {@return the blocks pistons are moving}
     */
    static MovingPistons movingPistons() {
        return FidorialServer.getInstance().worlds().movingPistons();
    }

    /**
     * {@return {@code true} if the calling thread owns a block, and may therefore change it}
     *
     * @param world    the redstone of the world
     * @param position where the block is
     */
    static boolean owns(final RedstoneWorld world, final BlockPosition position) {
        return world.world().scheduler().isOwnedByCurrentThread(world.world().key(), position.chunk());
    }

    /**
     * Replaces a block without telling the players that see it, when the engine in effect is the
     * server's: what vanilla does for the blocks a piston is moving, which clients work out from the
     * block event of the piston. Another engine has no such write, and players are then told.
     *
     * @param world    the redstone of the world
     * @param position where the block is
     * @param data     the new state
     * @param update   how far the change spreads
     */
    static void setBlockUnseen(final RedstoneWorld world, final BlockPosition position, final BlockData data,
                               final RedstoneWorld.Update update) {
        if (world instanceof final ServerRedstoneWorld server) {
            server.setBlockUnseen(position, data, update);
        } else {
            world.setBlock(position, data, update);
        }
    }

    /**
     * {@return the redstone behaviour in effect for a state, {@code null} for a block taking no part in redstone}
     *
     * @param state the state
     */
    static @Nullable RedstoneBehaviour behaviour(final BlockData state) {
        return FidorialServer.getInstance().registries().redstone().behaviourOrNull(state.key());
    }

    /**
     * {@return the default state of a block type}
     *
     * @param key the block type
     * @throws IllegalStateException if the block type is not registered
     */
    static BlockData defaultState(final TypedKey<BlockType> key) {
        final BlockType type = Blocks.type(key);
        final BlockData data = type == null ? null : type.defaultData();
        Preconditions.checkState(data != null, "The block type %s is not registered", key);
        return data;
    }

    /**
     * {@return the state of air, what a block that can no longer stand turns into}
     */
    static BlockData air() {
        return defaultState(BlockTypeKeys.AIR);
    }

    /**
     * {@return the block type registered under a key}
     *
     * @param key the block type
     * @throws IllegalStateException if the block type is not registered
     */
    static BlockType type(final TypedKey<BlockType> key) {
        final BlockType type = Blocks.type(key);
        Preconditions.checkState(type != null, "The block type %s is not registered", key);
        return type;
    }

    /**
     * Emits a game event at the center of a block, such as a lever switched on, for the debug
     * renderers following them.
     */
    static void gameEvent(final World world, final TypedKey<GameEvent> event, final BlockPosition position) {
        if (FidorialServer.isInitialized()) {
            FidorialServer.getInstance().debug().gameEvents().emit(world, event, position);
        }
    }

    /**
     * {@return a pitch between {@code 0.9} and {@code 1.0}, the one doors, trapdoors and fence gates
     * open and close at}
     */
    static float variedPitch() {
        return ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f;
    }

    /**
     * Sends a level event, such as the smoke of a burnt out torch, to the players that can see the block.
     */
    static void levelEvent(final World world, final int event, final BlockPosition position, final int data) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        FidorialServer.getInstance().players().broadcastNear(world,
                position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5,
                new ClientboundLevelEventPacket(event, position, data, false), null);
    }

    /**
     * Sends a block event, such as the note a note block plays, to the players that can see the block.
     */
    static void blockEvent(final World world, final BlockPosition position, final int action, final int parameter) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        FidorialServer.getInstance().players().broadcastNear(world,
                position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5,
                new ClientboundBlockEventPacket(position, action, parameter, 0), null);
    }


    /**
     * Plays a sound at the center of a block to the players that can see it.
     *
     * @param except the player left out, whose client already played the sound of the block it
     *               used, does for buttons and comparators; {@code null} for none
     */
    static void playSound(final World world, final BlockPosition position, final TypedKey<SoundEvent> sound, final Sound.Source source,
                          final float volume, final float pitch, final @Nullable Player except) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final double x = position.blockX() + 0.5;
        final double y = position.blockY() + 0.5;
        final double z = position.blockZ() + 0.5;
        FidorialServer.getInstance().players().broadcastNear(world, x, y, z,
                new ClientboundSoundPacket(Sound.sound(sound.key(), source, volume, pitch), x, y, z), except);
    }
}
