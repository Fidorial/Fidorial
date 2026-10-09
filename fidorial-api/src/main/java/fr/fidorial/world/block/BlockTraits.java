package fr.fidorial.world.block;

import fr.fidorial.world.block.data.Ageable;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Levelled;
import fr.fidorial.world.block.data.Lightable;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.block.data.Orientable;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.block.data.Rotatable;
import fr.fidorial.world.block.data.Snowable;
import fr.fidorial.world.block.data.Waterlogged;
import fr.fidorial.world.block.data.type.Door;
import fr.fidorial.world.block.data.type.Icicle;
import fr.fidorial.world.block.data.type.NoteBlock;
import fr.fidorial.world.block.data.type.Slab;
import fr.fidorial.world.block.data.type.Speleothem;
import fr.fidorial.world.block.data.type.Stairs;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects the trait interfaces a block type implements from its properties.
 */
final class BlockTraits {

    private static final Set<String> FACES = Set.of("north", "south", "east", "west", "up", "down");
    private static final Set<String> AXES = Set.of("x", "y", "z");
    private static final Set<String> HALVES = Set.of("top", "bottom", "upper", "lower");
    private static final Set<String> SLAB_TYPES = Set.of("top", "bottom", "double");
    private static final Set<String> SPELEOTHEM_THICKNESSES = Set.of("tip_merge", "tip", "frustum", "middle", "base");
    private static final Set<String> VERTICAL_DIRECTIONS = Set.of("up", "down");
    private static final Key NOTE_BLOCK = Key.key("note_block");

    private BlockTraits() {
    }

    static List<Class<? extends BlockData>> detect(final Key key, final List<BlockProperty> properties) {
        final List<Class<? extends BlockData>> traits = new ArrayList<>();

        final BlockProperty facing = find(properties, "facing");
        final BlockProperty half = find(properties, "half");
        final BlockProperty type = find(properties, "type");

        if (facing != null && FACES.containsAll(facing.values())) {
            traits.add(Directional.class);
        }
        final BlockProperty axis = find(properties, "axis");
        if (axis != null && AXES.containsAll(axis.values())) {
            traits.add(Orientable.class);
        }
        if (half != null && HALVES.containsAll(half.values())) {
            traits.add(Bisected.class);
        }
        if (find(properties, "waterlogged") != null) {
            traits.add(Waterlogged.class);
        }
        if (find(properties, "powered") != null) {
            traits.add(Powerable.class);
        }
        if (find(properties, "open") != null) {
            traits.add(Openable.class);
        }
        if (find(properties, "lit") != null) {
            traits.add(Lightable.class);
        }
        if (find(properties, "snowy") != null) {
            traits.add(Snowable.class);
        }
        if (find(properties, "age") != null) {
            traits.add(Ageable.class);
        }
        if (find(properties, "level") != null) {
            traits.add(Levelled.class);
        }
        if (find(properties, "rotation") != null) {
            traits.add(Rotatable.class);
        }

        if (key.equals(NOTE_BLOCK)) {
            traits.add(NoteBlock.class);
        }
        if (facing != null && half != null && find(properties, "shape") != null && traits.contains(Bisected.class)) {
            traits.add(Stairs.class);
        }
        if (type != null
                && SLAB_TYPES.containsAll(type.values())
                && type.values().size() == 3) {
            traits.add(Slab.class);
        }
        if (facing != null && half != null && find(properties, "hinge") != null && find(properties, "open") != null) {
            traits.add(Door.class);
        }
        final BlockProperty thickness = find(properties, "thickness");
        final BlockProperty verticalDirection = find(properties, "vertical_direction");
        if (thickness != null
                && SPELEOTHEM_THICKNESSES.containsAll(thickness.values())
                && verticalDirection != null
                && VERTICAL_DIRECTIONS.containsAll(verticalDirection.values())
                && find(properties, "waterlogged") != null) {
            traits.add(find(properties, "attached") != null ? Icicle.class : Speleothem.class);
        }

        return traits;
    }

    private static @Nullable BlockProperty find(final List<BlockProperty> properties, final String name) {
        for (final BlockProperty property : properties) {
            if (property.name().equals(name)) {
                return property;
            }
        }
        return null;
    }
}
