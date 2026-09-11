package fr.euphyllia.fidorial.server.world.structure.template;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.BlockValidator;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.StateParser;
import fr.euphyllia.fidorial.server.world.structure.math.Direction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class TemplateParser {

    private TemplateParser() {
    }

    public static StructureTemplateImpl parse(final Key key, final byte[] data, final BlockValidator validator,
                                              final Set<String> unknownBlocks) throws IOException {
        final boolean gzip = data.length > 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B;
        final CompoundBinaryTag root = BinaryTagIO.unlimitedReader().read(
                new ByteArrayInputStream(data),
                gzip ? BinaryTagIO.Compression.GZIP : BinaryTagIO.Compression.NONE);
        return parse(key, root, validator, unknownBlocks);
    }

    public static StructureTemplateImpl parse(final Key key, final CompoundBinaryTag root, final BlockValidator validator,
                                              final Set<String> unknownBlocks) throws IOException {
        final ListBinaryTag size = root.getList("size");
        if (size.size() < 3) {
            throw new IOException("Template " + key.asString() + " has no valid 'size'");
        }
        final int sizeX = size.getInt(0);
        final int sizeY = size.getInt(1);
        final int sizeZ = size.getInt(2);

        final List<ListBinaryTag> rawPalettes = new ArrayList<>();
        if (root.contains("palettes")) {
            for (final BinaryTag tag : root.getList("palettes")) {
                if (tag instanceof final ListBinaryTag list) {
                    rawPalettes.add(list);
                }
            }
        }
        if (rawPalettes.isEmpty()) {
            rawPalettes.add(root.getList("palette"));
        }

        final Set<String> missing = new TreeSet<>();
        final BlockState[][] palettes = new BlockState[rawPalettes.size()][];
        for (int p = 0; p < rawPalettes.size(); p++) {
            final ListBinaryTag raw = rawPalettes.get(p);
            final BlockState[] palette = new BlockState[raw.size()];
            for (int i = 0; i < raw.size(); i++) {
                if (!(raw.get(i) instanceof final CompoundBinaryTag entry)) {
                    continue;
                }
                final StateParser.Parsed parsed = StateParser.parseNbt(entry);
                if (parsed == null || parsed.name().equals(Keys.STRUCTURE_VOID)) {
                    continue;
                }
                final BlockState state = validator.resolve(parsed.name(), parsed.properties());
                if (state == null) {
                    missing.add(parsed.name().asString());
                    continue;
                }
                palette[i] = state;
            }
            palettes[p] = palette;
        }
        unknownBlocks.addAll(missing);

        final ListBinaryTag blocks = root.getList("blocks");
        final int count = blocks.size();
        final int[] xs = new int[count];
        final int[] ys = new int[count];
        final int[] zs = new int[count];
        final int[] states = new int[count];
        final CompoundBinaryTag[] nbt = new CompoundBinaryTag[count];
        final List<JigsawInfo> jigsaws = new ArrayList<>();
        final BlockState[] primary = palettes[0];

        int kept = 0;
        for (final BinaryTag tag : blocks) {
            if (!(tag instanceof final CompoundBinaryTag block)) {
                continue;
            }
            final int stateIndex = block.getInt("state");
            if (stateIndex < 0 || stateIndex >= primary.length) {
                continue;
            }
            boolean placeable = false;
            for (final BlockState[] palette : palettes) {
                if (stateIndex < palette.length && palette[stateIndex] != null) {
                    placeable = true;
                    break;
                }
            }
            if (!placeable) {
                continue;
            }
            final ListBinaryTag pos = block.getList("pos");
            if (pos.size() < 3) {
                continue;
            }
            xs[kept] = pos.getInt(0);
            ys[kept] = pos.getInt(1);
            zs[kept] = pos.getInt(2);
            states[kept] = stateIndex;
            final CompoundBinaryTag data = block.contains("nbt") ? block.getCompound("nbt") : null;
            nbt[kept] = data;

            final BlockState state = primary[stateIndex];
            if (state != null && state.name().equals(Keys.JIGSAW)) {
                final JigsawInfo jigsaw = readJigsaw(xs[kept], ys[kept], zs[kept], state, data);
                if (jigsaw != null) {
                    jigsaws.add(jigsaw);
                }
            }
            kept++;
        }

        return new StructureTemplateImpl(
                key, sizeX, sizeY, sizeZ, palettes,
                trim(xs, kept), trim(ys, kept), trim(zs, kept), trim(states, kept),
                java.util.Arrays.copyOf(nbt, kept), jigsaws);
    }

    private static int[] trim(final int[] values, final int length) {
        return values.length == length ? values : java.util.Arrays.copyOf(values, length);
    }

    private static @Nullable JigsawInfo readJigsaw(final int x, final int y, final int z, final BlockState state,
                                                   final @Nullable CompoundBinaryTag data) {
        final String orientation = state.properties().getOrDefault("orientation", "north_up");
        final int split = orientation.indexOf('_');
        if (split <= 0) {
            return null;
        }
        final Direction front = Direction.byName(orientation.substring(0, split));
        final Direction top = Direction.byName(orientation.substring(split + 1));
        if (front == null || top == null) {
            return null;
        }
        final CompoundBinaryTag nbt = data == null ? CompoundBinaryTag.empty() : data;
        final Key name = Keys.parseOr(nbt.getString("name", "minecraft:empty"), Keys.EMPTY);
        final Key target = Keys.parseOr(nbt.getString("target", "minecraft:empty"), Keys.EMPTY);
        final Key pool = Keys.parseOr(nbt.getString("pool", "minecraft:empty"), Keys.EMPTY);
        final String joint = nbt.getString("joint", "");
        final boolean rollable = switch (joint) {
            case "rollable" -> true;
            case "aligned" -> false;
            default -> !front.isHorizontal();
        };
        return new JigsawInfo(x, y, z, front, top, name, target, pool, rollable,
                nbt.getInt("placement_priority", 0), nbt.getInt("selection_priority", 0));
    }
}
