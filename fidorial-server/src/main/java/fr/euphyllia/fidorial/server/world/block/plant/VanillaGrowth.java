package fr.euphyllia.fidorial.server.world.block.plant;

import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;

import java.util.Set;

public final class VanillaGrowth {

    private VanillaGrowth() {
        throw new UnsupportedOperationException("VanillaGrowth cannot be instantiated.");
    }

    public static CropBlock.Growth on(final Set<Key> soils) {
        return (data, world, pos) -> 1.0 / ((int) (25.0f / speed(soils, data, world, pos)) + 1);
    }

    public static CropBlock.Growth scaled(final CropBlock.Growth growth, final double factor) {
        return (data, world, pos) -> Math.min(1.0, growth.chance(data, world, pos) * factor);
    }

    private static float speed(final Set<Key> soils, final BlockData data, final BlockAccess world, final BlockPos pos) {
        float speed = 1.0f;
        final BlockPos below = pos.offset(0, -1, 0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final BlockData soil = world.blockAt(below.offset(dx, 0, dz));
                float bonus = 0.0f;
                if (soils.contains(soil.key())) {
                    bonus = isMoist(soil) ? 3.0f : 1.0f;
                }
                if (dx != 0 || dz != 0) {
                    bonus /= 4.0f;
                }
                speed += bonus;
            }
        }

        final Key self = data.key();
        final boolean westEast = same(world, pos.offset(-1, 0, 0), self) || same(world, pos.offset(1, 0, 0), self);
        final boolean northSouth = same(world, pos.offset(0, 0, -1), self) || same(world, pos.offset(0, 0, 1), self);
        if (westEast && northSouth) {
            speed /= 2.0f;
        } else if (same(world, pos.offset(-1, 0, -1), self) || same(world, pos.offset(1, 0, -1), self)
                || same(world, pos.offset(1, 0, 1), self) || same(world, pos.offset(-1, 0, 1), self)) {
            speed /= 2.0f;
        }
        return speed;
    }

    private static boolean isMoist(final BlockData soil) {
        if (!soil.type().hasProperty("moisture")) {
            return true;
        }
        return !"0".equals(soil.get("moisture"));
    }

    private static boolean same(final BlockAccess world, final BlockPos pos, final Key block) {
        return world.blockAt(pos).key().equals(block);
    }
}
