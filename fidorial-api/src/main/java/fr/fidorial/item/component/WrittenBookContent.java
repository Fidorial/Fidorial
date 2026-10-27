package fr.fidorial.item.component;

import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.inventory.BookLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;

/**
 * The contents of a signed book, under {@code minecraft:written_book_content}.
 *
 * <p>As a {@link BookLike}, it can be opened directly with
 * {@link Audience#openBook(BookLike)}.</p>
 *
 * @param title      the title, at most {@value #MAX_TITLE_LENGTH} characters
 * @param author     the name of the author
 * @param generation how many times the book was copied, from {@code 0} (original) to {@value #MAX_GENERATION}
 * @param pages      the pages, at most {@value #MAX_PAGES}
 * @param resolved   whether the selectors, scores and NBT the pages hold were already resolved
 * @since 0.1.0
 */
public record WrittenBookContent(String title, String author, int generation, List<Component> pages,
                                 boolean resolved) implements BookLike {

    /**
     * The longest title the client accepts.
     */
    public static final int MAX_TITLE_LENGTH = 32;

    /**
     * The most pages a written book holds.
     */
    public static final int MAX_PAGES = 100;

    /**
     * The highest generation, a copy of a copy.
     */
    public static final int MAX_GENERATION = 3;

    public WrittenBookContent {
        Preconditions.checkArgument(title != null, "The title of a written book must not be null");
        Preconditions.checkArgument(author != null, "The author of a written book must not be null");
        Preconditions.checkArgument(pages != null, "The pages of a written book must not be null");
        Preconditions.checkArgument(title.length() <= MAX_TITLE_LENGTH,
                "The title of a written book is limited to %s characters, got %s", MAX_TITLE_LENGTH, title.length());
        Preconditions.checkArgument(generation >= 0 && generation <= MAX_GENERATION,
                "The generation of a written book must be between 0 and %s, got %s", MAX_GENERATION, generation);
        Preconditions.checkArgument(pages.size() <= MAX_PAGES,
                "A written book is limited to %s pages, got %s", MAX_PAGES, pages.size());
        pages = List.copyOf(pages);
    }

    /**
     * Converts an Adventure book. Its title and author are flattened to plain text, the only form
     * written books keep them in; the title is cut to {@value #MAX_TITLE_LENGTH} characters and the
     * pages to the first {@value #MAX_PAGES}.
     *
     * @param book the book
     * @return the contents of an original, already resolved written book
     */
    public static WrittenBookContent of(final BookLike book) {
        Preconditions.checkArgument(book != null, "The book passed to WrittenBookContent.of() must not be null");
        final Book adventure = book.asBook();
        final String title = PlainTextComponentSerializer.plainText().serialize(adventure.title());
        final List<Component> pages = adventure.pages();
        return new WrittenBookContent(
                title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH) : title,
                PlainTextComponentSerializer.plainText().serialize(adventure.author()),
                0,
                pages.size() > MAX_PAGES ? pages.subList(0, MAX_PAGES) : pages,
                true);
    }

    @Override
    public Book asBook() {
        return Book.book(Component.text(title), Component.text(author), pages);
    }
}
