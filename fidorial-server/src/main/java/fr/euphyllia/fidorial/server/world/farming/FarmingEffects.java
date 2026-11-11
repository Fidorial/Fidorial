package fr.euphyllia.fidorial.server.world.farming;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.world.World;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

/**
 * What players see and hear of farming: sounds, the particles of a fertilizer, a block breaking.
 */
public final class FarmingEffects {

    /**
     * The level event of a fertilizer taking: green particles and its sound, the data being how
     * many particles.
     */
    public static final int FERTILIZER_USE = 1505;

    /**
     * How many particles a fertilizer raises.
     */
    public static final int FERTILIZER_PARTICLES = 15;

    private FarmingEffects() {
        throw new UnsupportedOperationException("FarmingEffects cannot be instantiated.");
    }

    /**
     * Sends a level event to the players that can see the block.
     */
    public static void levelEvent(final World world, final int event, final BlockPosition position, final int data) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        FidorialServer.getInstance().players().broadcastNear(world,
                position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5,
                new ClientboundLevelEventPacket(event, position, data, false), null);
    }

    /**
     * Plays a sound at the center of a block to the players that can hear it.
     *
     * @param except the player left out, whose client already played the sound of the item it
     *               used; {@code null} for none
     */
    public static void playSound(final World world, final BlockPosition position, final TypedKey<SoundEvent> sound,
                                 final float volume, final float pitch, final @Nullable Player except) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final double x = position.blockX() + 0.5;
        final double y = position.blockY() + 0.5;
        final double z = position.blockZ() + 0.5;
        FidorialServer.getInstance().players().broadcastNear(world, x, y, z,
                new ClientboundSoundPacket(Sound.sound(sound.key(), Sound.Source.BLOCK, volume, pitch), x, y, z), except);
    }

    /**
     * Emits a game event at the center of a block, for the debug renderers following them.
     */
    public static void gameEvent(final World world, final TypedKey<GameEvent> event, final BlockPosition position) {
        if (FidorialServer.isInitialized()) {
            FidorialServer.getInstance().debug().gameEvents().emit(world, event, position);
        }
    }
}
