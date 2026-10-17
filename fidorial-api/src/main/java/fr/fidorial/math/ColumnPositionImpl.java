package fr.fidorial.math;

record ColumnPositionImpl(int x, int z) implements ColumnPosition {
    @Override
    public ColumnPosition offsetX(final int x) {
        return withX(this.x + x);
    }

    @Override
    public ColumnPosition offsetZ(final int z) {
        return withZ(this.z + z);
    }

    @Override
    public ColumnPosition offset(final int x, final int z) {
        return with(this.x + x, this.z + z);
    }

    @Override
    public ColumnPosition withX(final int x) {
        return with(x, z);
    }

    @Override
    public ColumnPosition withZ(final int z) {
        return with(x, z);
    }

    @Override
    public ColumnPosition with(final int x, final int z) {
        return new ColumnPositionImpl(x, z);
    }
}
