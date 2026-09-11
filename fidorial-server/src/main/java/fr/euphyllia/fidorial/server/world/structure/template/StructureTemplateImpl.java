package fr.euphyllia.fidorial.server.world.structure.template;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.world.structure.StructureTemplate;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class StructureTemplateImpl implements StructureTemplate {

    private final Key key;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final @Nullable BlockState[][] palettes;
    private final int[] xs;
    private final int[] ys;
    private final int[] zs;
    private final int[] states;
    private final @Nullable CompoundBinaryTag[] nbt;
    private final List<JigsawInfo> jigsaws;

    StructureTemplateImpl(
            final Key key,
            final int sizeX,
            final int sizeY,
            final int sizeZ,
            final @Nullable BlockState[][] palettes,
            final int[] xs,
            final int[] ys,
            final int[] zs,
            final int[] states,
            final @Nullable CompoundBinaryTag[] nbt,
            final List<JigsawInfo> jigsaws
    ) {
        this.key = key;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palettes = palettes;
        this.xs = xs;
        this.ys = ys;
        this.zs = zs;
        this.states = states;
        this.nbt = nbt;
        this.jigsaws = List.copyOf(jigsaws);
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public int sizeX() {
        return sizeX;
    }

    @Override
    public int sizeY() {
        return sizeY;
    }

    @Override
    public int sizeZ() {
        return sizeZ;
    }

    @Override
    public int blockCount() {
        return xs.length;
    }

    @Override
    public int jigsawCount() {
        return jigsaws.size();
    }

    public int paletteCount() {
        return palettes.length;
    }

    public @Nullable BlockState[] palette(final int index) {
        return palettes[Math.floorMod(index, palettes.length)];
    }

    public int x(final int i) {
        return xs[i];
    }

    public int y(final int i) {
        return ys[i];
    }

    public int z(final int i) {
        return zs[i];
    }

    public int stateIndex(final int i) {
        return states[i];
    }

    public @Nullable CompoundBinaryTag nbt(final int i) {
        return nbt[i];
    }

    public List<JigsawInfo> jigsaws() {
        return jigsaws;
    }

    @Override
    public String toString() {
        return "StructureTemplate[" + key.asString() + ", " + sizeX + "x" + sizeY + "x" + sizeZ + ", blocks=" + xs.length + "]";
    }
}
