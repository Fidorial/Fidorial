package fr.fidorial.world.structure;

import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Entry point to the datapack structures of the server.
 *
 * @since 0.1.0
 */
public interface StructureManager {

    /**
     * @return the loaded packs, in load order (later packs override earlier ones)
     */
    List<Datapack> datapacks();

    /**
     * @return every template identifier available
     */
    Set<Key> templates();

    /**
     * Loads (or returns the cached) template.
     *
     * @param key the template identifier
     * @return the template, empty when unknown or unreadable
     */
    Optional<StructureTemplate> template(Key key);

    /**
     * @return every jigsaw structure identifier available ({@code worldgen/structure})
     */
    Set<Key> structures();

    /**
     * Pastes a template into a world.
     *
     * @param world    the target world
     * @param origin   the lowest corner before rotation
     * @param template the template identifier
     * @param rotation the rotation to apply around {@code origin}
     * @return the number of blocks written, completed once every chunk has been updated
     */
    CompletableFuture<Integer> placeTemplate(World world, BlockPos origin, Key template, StructureRotation rotation);

    /**
     * Assembles and places a jigsaw structure. The biome filter of the
     * structure is ignored.
     *
     * @param world     the target world
     * @param position  the block whose chunk hosts the structure start
     * @param structure the structure identifier
     * @return the number of pieces placed, {@code 0} when the structure could not be assembled
     */
    CompletableFuture<Integer> placeStructure(World world, BlockPos position, Key structure);

    /**
     * Searches the nearest start of a structure that world generation would place.
     *
     * @param world       the world to search
     * @param origin      the search centre
     * @param structure   the structure identifier
     * @param radiusCells how many placement cells to scan around the origin
     * @return the start position, or empty when none was found in range
     */
    CompletableFuture<Optional<BlockPos>> locate(World world, BlockPos origin, Key structure, int radiusCells);

    /**
     * Reads the datapacks again. Chunks generated from now on use the new content; chunks already
     * generated keep what they have.
     *
     * @return completed once the new content is live
     */
    CompletableFuture<Void> reload();
}
