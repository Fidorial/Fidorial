package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.NoteBlock;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * The note block, a port of the redstone part of vanilla's {@code NoteBlock}: it plays its note
 * when it starts being powered.
 */
public final class PoweredNoteBlock implements RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.NOTE_BLOCK;

    public static final PoweredNoteBlock INSTANCE = new PoweredNoteBlock();

    /**
     * The instruments of the mob heads, which play whatever covers the note block.
     */
    private static final Set<String> MOB_HEADS =
            Set.of("zombie", "skeleton", "creeper", "dragon", "wither_skeleton", "piglin", "custom_head");

    private static final String CUSTOM_HEAD = "custom_head";
    private static final String NOTE_BLOCK_SOUND = "note_block_sound";
    private static final float VOLUME = 3.0f;

    private PoweredNoteBlock() {
    }

    private static @Nullable Key sound(final RedstoneWorld world, final BlockPosition position, final String instrument,
                                       final boolean mobHead) {
        if (!mobHead) {
            return Key.key(Key.MINECRAFT_NAMESPACE, "block.note_block." + instrument);
        }
        if (!instrument.equals(CUSTOM_HEAD)) {
            final String mob = instrument.equals("dragon") ? "ender_dragon" : instrument;
            return Key.key(Key.MINECRAFT_NAMESPACE, "block.note_block.imitate." + mob);
        }
        if (!(world.world() instanceof final ServerWorld serverWorld)) {
            return null;
        }
        final CompoundBinaryTag head = serverWorld.blockEntityData(position.relative(BlockFace.UP));
        final String custom = head == null ? "" : head.getString(NOTE_BLOCK_SOUND);
        if (custom.isEmpty()) {
            return null;
        }
        try {
            return Key.key(custom);
        } catch (final InvalidKeyException ignored) {
            return null;
        }
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (!(state instanceof final NoteBlock note)) {
            return;
        }
        final boolean signal = world.hasNeighborSignal(position);
        if (signal == note.isPowered()) {
            return;
        }
        if (signal) {
            play(world, position, note);
        }
        world.setBlock(position, note.withPowered(signal), RedstoneWorld.Update.NEIGHBORS);
    }

    /**
     * Plays the note of a note block, as a signal does: a note block only plays a
     * mob head, or with nothing but air above it.
     *
     * @param world    the redstone of the world of the note block
     * @param position where the note block is
     * @param note     its state
     */
    public void play(final RedstoneWorld world, final BlockPosition position, final NoteBlock note) {
        final String instrument = note.getInstrument();
        final boolean mobHead = MOB_HEADS.contains(instrument);
        if (!mobHead) {
            final BlockData above = world.blockAt(position.relative(BlockFace.UP));
            if (above == null || !above.isAir()) {
                return;
            }
        }
        final Key sound = sound(world, position, instrument, mobHead);
        if (sound == null) {
            // a custom head without a sound plays nothing, not even the particle
            return;
        }
        // the note particle the clients show, which the event carries
        Redstone.blockEvent(world.world(), position, 0, 0);
        final float pitch = mobHead ? 1.0f : (float) Math.pow(2.0, (note.getNote() - 12) / 12.0);
        Redstone.playSound(world.world(), position, TypedKey.withRegistry(RegistryKey.SOUND_EVENT, sound), Sound.Source.RECORD, VOLUME, pitch, null);
        Redstone.gameEvent(world.world(), GameEventKeys.NOTE_BLOCK_PLAY, position);
    }
}
