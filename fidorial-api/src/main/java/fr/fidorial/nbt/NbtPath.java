package fr.fidorial.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A path into NBT, such as {@code Inventory[{Slot:0b}].components}.
 *
 * @see <a href="https://minecraft.wiki/w/NBT_path">NBT path</a>
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface NbtPath {

    /**
     * {@return the tags this path matches under {@code root}, empty if any segment matches nothing}
     *
     * @param root the tag to start from
     * @since 0.1.0
     */
    List<BinaryTag> select(BinaryTag root);

    /**
     * {@return this path as parseable text}
     *
     * @since 0.1.0
     */
    String asString();
}
