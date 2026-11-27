package fr.fidorial.world.block.data.type;

import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.block.property.NoteInstrument;

/**
 * The block state of a note block: its instrument, pitch, and whether it is powered.
 *
 * @since 0.1.0
 */
@BlockTrait(properties = {Names.INSTRUMENT, Names.NOTE})
public interface NoteBlock extends Powerable {

    @Override
    default NoteBlock withPowered(final boolean powered) {
        return (NoteBlock) Powerable.super.withPowered(powered);
    }

    /**
     * {@return the instrument played}
     *
     * @since 0.1.0
     */
    default NoteInstrument getInstrument() {
        return get(type().choice(Names.INSTRUMENT, NoteInstrument.class));
    }

    /**
     * {@return the same note block playing another instrument}
     *
     * @param instrument the instrument to play
     * @since 0.1.0
     */
    default NoteBlock withInstrument(final NoteInstrument instrument) {
        return (NoteBlock) with(type().choice(Names.INSTRUMENT, NoteInstrument.class), instrument);
    }

    /**
     * {@return the pitch, from {@code 0} to {@code 24}}
     *
     * @since 0.1.0
     */
    default int getNote() {
        return get(type().range(Names.NOTE));
    }

    /**
     * {@return the same note block tuned to another pitch}
     *
     * @param note the pitch, from {@code 0} to {@code 24}
     * @since 0.1.0
     */
    default NoteBlock withNote(final int note) {
        return (NoteBlock) with(type().range(Names.NOTE), note);
    }
}
