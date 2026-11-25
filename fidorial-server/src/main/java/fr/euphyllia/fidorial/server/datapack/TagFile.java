package fr.euphyllia.fidorial.server.datapack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;

import java.util.List;

public record TagFile(boolean replace, List<Entry> values) {
    public static final Codec<TagFile> CODEC = RecordCodec.builder(TagFile.class)
            .field("replace", TagFile::replace, Codec.BOOL, false)
            .required("values", TagFile::values, Entry.CODEC.listOf())
            .build();

    public record Entry(String reference, boolean required) {
        private static final Codec<Entry> OBJECT_CODEC = RecordCodec.builder(Entry.class)
                .keys(RecordCodec.KeyStyle.UNCHECKED)
                .required("id", Entry::reference, Codec.STRING)
                .field("required", Entry::required, Codec.BOOL, true)
                .build();

        /// A reference alone, or an object with an `id` and whether it is `required`.
        public static final Codec<Entry> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC).xmap(
                either -> either.map(reference -> new Entry(reference, true), entry -> entry),
                entry -> entry.required() ? Either.left(entry.reference()) : Either.right(entry));

        public boolean isTag() {
            return reference.startsWith("#");
        }
    }
}
