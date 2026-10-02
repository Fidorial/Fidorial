package fr.fidorial.world.block.data.type;

import fr.fidorial.world.block.data.Powerable;

/**
 * The block state of a note block: its instrument, pitch, and whether it is powered.
 *
 * @since 0.1.0
 */
public interface NoteBlock extends Powerable {

    @Override
    default NoteBlock withPowered(final boolean powered) {
        return (NoteBlock) Powerable.super.withPowered(powered);
    }

    /**
     * {@return the instrument played, for instance {@code harp}}
     *
     * @since 0.1.0
     */
    default String getInstrument() {
        return get("instrument");
    }

    /**
     * {@return the same note block playing another instrument}
     *
     * @param instrument the serialized instrument name, for instance {@code harp}
     * @since 0.1.0
     */
    default NoteBlock withInstrument(final String instrument) {
        return (NoteBlock) with("instrument", instrument);
    }

    /**
     * {@return the pitch, from {@code 0} to {@code 24}}
     *
     * @since 0.1.0
     */
    default int getNote() {
        return Integer.parseInt(get("note"));
    }

    /**
     * {@return the same note block tuned to another pitch}
     *
     * @param note the pitch, from {@code 0} to {@code 24}
     * @since 0.1.0
     */
    default NoteBlock withNote(final int note) {
        return (NoteBlock) with("note", String.valueOf(note));
    }
}
