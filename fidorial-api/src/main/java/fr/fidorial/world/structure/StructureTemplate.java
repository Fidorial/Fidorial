package fr.fidorial.world.structure;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

/**
 * A structure template read from a datapack {@code .nbt} file
 * ({@code data/<namespace>/structure/<path>.nbt}).
 *
 * @since 0.1.0
 */
public interface StructureTemplate extends Keyed {

    /**
     * @return the template identifier, e.g. {@code terralith:spire/layer1}
     */
    @Override
    Key key();

    int sizeX();

    int sizeY();

    int sizeZ();

    /**
     * @return the number of stored blocks, structure voids excluded
     */
    int blockCount();

    /**
     * @return the number of jigsaw blocks the template exposes
     */
    int jigsawCount();
}
