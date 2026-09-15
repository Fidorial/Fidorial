package fr.euphyllia.fidorial.server.chat;

import java.util.BitSet;

// used for censoring text
// see the table under the packet description: https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Chat_Message
public sealed interface FilterType {

    FilterType PASS_THROUGH = new PassThrough();
    FilterType FULLY_FILTERED = new FullyFiltered();

    record PassThrough() implements FilterType {
    }

    record FullyFiltered() implements FilterType {
    }

    record PartiallyFiltered(BitSet mask) implements FilterType {
    }
}
