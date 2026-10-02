package fr.fidorial.world.dimension;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;


/**
 * A reference to a single object specified by {@link DimensionTypeDefinition#timelines()}
 * It can be either a single timeline or a tag grouping several.
 *
 * @since 0.1.0
 */
public sealed interface TimelineReference permits TimelineReference.Id, TimelineReference.Tag {

    /**
     * @param key the timeline's namespaced identifier
     * @return the reference
     */
    @Contract(value = "_ -> new", pure = true)
    static TimelineReference id(final Key key) {
        return new Id(key);
    }

    /**
     * @param key the tag's namespaced identifier, without the leading {@code #}
     * @return the reference
     */
    @Contract(value = "_ -> new", pure = true)
    static TimelineReference tag(final Key key) {
        return new Tag(key);
    }

    /**
     * @param key the timeline's namespaced identifier
     * @since 0.1.0
     */
    record Id(Key key) implements TimelineReference {
        public Id {
            Preconditions.checkArgument(key != null, "The key of a timeline reference id must not be null");
        }
    }

    /**
     * @param key the tag's namespaced identifier, without the leading {@code #}
     * @since 0.1.0
     */
    record Tag(Key key) implements TimelineReference {
        public Tag {
            Preconditions.checkArgument(key != null, "The key of a timeline reference tag must not be null");
        }
    }
}
