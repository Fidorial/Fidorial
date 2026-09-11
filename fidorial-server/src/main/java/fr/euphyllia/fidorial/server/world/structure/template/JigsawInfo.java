package fr.euphyllia.fidorial.server.world.structure.template;

import fr.euphyllia.fidorial.server.world.structure.math.Direction;
import fr.euphyllia.fidorial.server.world.structure.math.Rotations;
import fr.fidorial.world.structure.StructureRotation;
import net.kyori.adventure.key.Key;

public record JigsawInfo(
        int x,
        int y,
        int z,
        Direction front,
        Direction top,
        Key name,
        Key target,
        Key pool,
        boolean rollable,
        int placementPriority,
        int selectionPriority
) {

    public JigsawInfo transform(final StructureRotation rotation, final int originX, final int originY, final int originZ) {
        return new JigsawInfo(
                originX + Rotations.x(rotation, x, z),
                originY + y,
                originZ + Rotations.z(rotation, x, z),
                front.rotate(rotation),
                top.rotate(rotation),
                name,
                target,
                pool,
                rollable,
                placementPriority,
                selectionPriority);
    }

    public boolean canAttach(final JigsawInfo child) {
        return front == child.front.opposite()
                && (rollable || top == child.top)
                && target.equals(child.name);
    }
}
