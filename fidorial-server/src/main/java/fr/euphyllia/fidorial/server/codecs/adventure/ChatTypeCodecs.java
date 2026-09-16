package fr.euphyllia.fidorial.server.codecs.adventure;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.fidorial.chat.ChatTypeDecoration;
import fr.fidorial.chat.ChatTypeDefinition;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Optional;

import static fr.euphyllia.fidorial.server.codecs.adventure.StyleCodecs.STYLE_MAP_CODEC;

public final class ChatTypeCodecs {

    private static final Codec<ChatTypeDecoration> DECORATION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey),
            Codec.STRING.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters),
            STYLE_MAP_CODEC.codec().optionalFieldOf("style")
                    .forGetter(d -> Optional.ofNullable(d.style()))
    ).apply(instance, (translationKey, parameters, style) ->
            new ChatTypeDecoration(translationKey, parameters, style.orElse(null))));

    private ChatTypeCodecs() {
        throw new UnsupportedOperationException("ChatTypeCodecs cannot be instantiated.");
    }

    public static Codec<ChatTypeDefinition> codec(final Key key) {
        return RecordCodecBuilder.create(instance -> instance.group(
                DECORATION_CODEC.fieldOf("chat").forGetter(ChatTypeDefinition::chat),
                DECORATION_CODEC.fieldOf("narration").forGetter(ChatTypeDefinition::narration)
        ).apply(instance, (chat, narration) -> new ChatTypeDefinition(key, chat, narration)));
    }

    public static CompoundBinaryTag encodeNbt(final ChatTypeDefinition chatType) {
        final BinaryTag tag = codec(chatType.key())
                .encodeStart(BinaryTagOps.binaryTagOps(), chatType)
                .getOrThrow(message -> new IllegalStateException(
                        "Failed to encode chat type " + chatType.key().asString() + ": " + message));

        if (!(tag instanceof final CompoundBinaryTag compound)) {
            throw new IllegalStateException(
                    "Chat type " + chatType.key().asString() + " did not encode to a compound tag");
        }

        return compound;
    }

    public static ChatTypeDefinition fromJson(final Key key, final String json) {
        final JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (final RuntimeException exception) {
            throw new IllegalArgumentException("Chat type " + key.asString() + " is not valid JSON", exception);
        }

        return codec(key)
                .parse(JsonOps.INSTANCE, element)
                .getOrThrow(message -> new IllegalArgumentException(
                        "Failed to read chat type " + key.asString() + ": " + message));
    }
}
