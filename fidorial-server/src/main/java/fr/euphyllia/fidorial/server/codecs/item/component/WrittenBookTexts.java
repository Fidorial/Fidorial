package fr.euphyllia.fidorial.server.codecs.item.component;

import fr.fidorial.item.component.WrittenBookContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flattens the title and author of a {@link WrittenBookContent} to the plain strings the client
 * expects, both on the network and in saved items.
 */
public final class WrittenBookTexts {

    private static final Logger log = LoggerFactory.getLogger(WrittenBookTexts.class);

    private WrittenBookTexts() {
        throw new UnsupportedOperationException("WrittenBookTexts cannot be instantiated.");
    }

    /**
     * The title as plain text, cut to {@value WrittenBookContent#MAX_TITLE_LENGTH} characters
     * without splitting a surrogate pair.
     */
    public static String title(final Component title) {
        final String plain = PlainTextComponentSerializer.plainText().serialize(title);
        if (plain.length() <= WrittenBookContent.MAX_TITLE_LENGTH) {
            return plain;
        }
        int end = WrittenBookContent.MAX_TITLE_LENGTH;
        if (Character.isHighSurrogate(plain.charAt(end - 1))) {
            end--;
        }
        return plain.substring(0, end);
    }

    /**
     * The author as plain text.
     */
    public static String author(final Component author) {
        return PlainTextComponentSerializer.plainText().serialize(author);
    }
}
