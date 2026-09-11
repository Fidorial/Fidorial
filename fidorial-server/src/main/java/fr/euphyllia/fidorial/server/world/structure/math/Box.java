package fr.euphyllia.fidorial.server.world.structure.math;


public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public static Box of(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        return new Box(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public Box moved(final int dx, final int dy, final int dz) {
        return new Box(minX + dx, minY + dy, minZ + dz, maxX + dx, maxY + dy, maxZ + dz);
    }

    public Box encapsulate(final int x, final int y, final int z) {
        return new Box(Math.min(minX, x), Math.min(minY, y), Math.min(minZ, z),
                Math.max(maxX, x), Math.max(maxY, y), Math.max(maxZ, z));
    }

    public Box union(final Box other) {
        return new Box(Math.min(minX, other.minX), Math.min(minY, other.minY), Math.min(minZ, other.minZ),
                Math.max(maxX, other.maxX), Math.max(maxY, other.maxY), Math.max(maxZ, other.maxZ));
    }

    public boolean isInside(final int x, final int y, final int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean intersects(final Box other) {
        return maxX >= other.minX && minX <= other.maxX
                && maxY >= other.minY && minY <= other.maxY
                && maxZ >= other.minZ && minZ <= other.maxZ;
    }

    public boolean intersectsXZ(final int otherMinX, final int otherMinZ, final int otherMaxX, final int otherMaxZ) {
        return maxX >= otherMinX && minX <= otherMaxX && maxZ >= otherMinZ && minZ <= otherMaxZ;
    }

    public boolean contains(final Box other) {
        return other.minX >= minX && other.maxX <= maxX
                && other.minY >= minY && other.maxY <= maxY
                && other.minZ >= minZ && other.maxZ <= maxZ;
    }

    public int ySpan() {
        return maxY - minY + 1;
    }

    public int centerX() {
        return (minX + maxX) / 2;
    }

    public int centerZ() {
        return (minZ + maxZ) / 2;
    }
}
