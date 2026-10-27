package fr.fidorial.item.component;

import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.inventory.BookLike;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * The contents of a signed book, under {@code minecraft:written_book_content}.
 *
 * <p>As a {@link BookLike}, it can be opened directly with
 * {@link Audience#openBook(BookLike)}.</p>
 *
 * <p>The client only knows the title and author as plain text: they are flattened when the book
 * is sent or saved, and the title is cut to {@value #MAX_TITLE_LENGTH} characters.</p>
 *
 * @param title      the title
 * @param author     the author
 * @param generation how many times the book was copied, from {@code 0} (original) to {@value #MAX_GENERATION}
 * @param pages      the pages
 * @param resolved   whether the selectors, scores and NBT the pages hold were already resolved
 * @since 0.1.0
 */
public record WrittenBookContent(Component title, Component author, int generation, List<Component> pages,
                                 boolean resolved) implements BookLike {

    /**
     * The longest title, in plain text characters, the client accepts.
     */
    public static final int MAX_TITLE_LENGTH = 32;

    /**
     * The most pages a written book holds.
     */
    public static final int MAX_PAGES = Integer.MAX_VALUE;

    /**
     * The highest generation, a copy of a copy.
     */
    public static final int MAX_GENERATION = 3;

    public WrittenBookContent {
        Preconditions.checkArgument(title != null, "The title of a written book must not be null");
        Preconditions.checkArgument(author != null, "The author of a written book must not be null");
        Preconditions.checkArgument(pages != null, "The pages of a written book must not be null");
        Preconditions.checkArgument(generation >= 0 && generation <= MAX_GENERATION,
                "The generation of a written book must be between 0 and %s, got %s", MAX_GENERATION, generation);
        pages = List.copyOf(pages);
    }

    /**
     * Converts an Adventure book, keeping its title, author and pages as they are.
     *
     * @param book the book
     * @return the contents of an original, already resolved written book
     */
    public static WrittenBookContent of(final BookLike book) {
        Preconditions.checkArgument(book != null, "The book passed to WrittenBookContent.of() must not be null");
        final Book adventure = book.asBook();
        return new WrittenBookContent(adventure.title(), adventure.author(), 0, adventure.pages(), true);
    }

    @Override
    public Book asBook() {
        return Book.book(title, author, pages);
    }
}
