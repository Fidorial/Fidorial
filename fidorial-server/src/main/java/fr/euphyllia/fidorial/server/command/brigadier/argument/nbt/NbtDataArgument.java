package fr.euphyllia.fidorial.server.command.brigadier.argument.nbt;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.nbt.NbtPath;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class NbtDataArgument implements ArgumentType<NbtPath> {

    public static final SimpleCommandExceptionType ERROR_INVALID_NODE = ExceptionFactory.simple("arguments.nbtpath.node.invalid");
    public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = ExceptionFactory.dynamic("arguments.nbtpath.nothing_found");

    public static NbtDataArgument nbtPath() {
        return new NbtDataArgument();
    }

    public static List<BinaryTag> getOrThrow(final NbtPath path, final BinaryTag root) throws CommandSyntaxException {
        final List<BinaryTag> result = path.select(root);
        if (result.isEmpty()) {
            throw ERROR_NOTHING_FOUND.create(path.asString());
        }
        return result;
    }

    @Override
    public NbtPath parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final List<Function<List<BinaryTag>, List<BinaryTag>>> chain = new ArrayList<>();
        boolean atStart = true;

        while (reader.canRead() && reader.peek() != ' ') {
            chain.add(parseStep(reader, atStart));
            atStart = false;
            if (reader.canRead()) {
                final char next = reader.peek();
                if (next != ' ' && next != '[' && next != '{') {
                    reader.expect('.');
                }
            }
        }

        final String text = reader.getString().substring(start, reader.getCursor());
        return new ChainPath(text, List.copyOf(chain));
    }

    private static Function<List<BinaryTag>, List<BinaryTag>> parseStep(final StringReader reader, final boolean atStart) throws CommandSyntaxException {
        return switch (reader.peek()) {
            case '"', '\'' -> keyStep(reader, reader.readString());
            case '[' -> {
                reader.skip();
                if (reader.canRead() && reader.peek() == '{') {
                    final CompoundBinaryTag pattern = readPattern(reader);
                    reader.expect(']');
                    yield fanOut(tag -> matchingListElements(tag, pattern));
                } else if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    yield fanOut(NbtDataArgument::allListElements);
                } else {
                    final int index = reader.readInt();
                    reader.expect(']');
                    yield fanOut(tag -> singleListElement(tag, index));
                }
            }
            case '{' -> {
                if (!atStart) {
                    throw ERROR_INVALID_NODE.createWithContext(reader);
                }
                final CompoundBinaryTag pattern = readPattern(reader);
                yield fanOut(tag -> selfIfMatches(tag, pattern));
            }
            default -> {
                final String name = readUnquotedName(reader);
                yield keyStep(reader, name);
            }
        };
    }

    private static Function<List<BinaryTag>, List<BinaryTag>> keyStep(final StringReader reader, final String name) throws CommandSyntaxException {
        if (name.isEmpty()) {
            throw ERROR_INVALID_NODE.createWithContext(reader);
        }
        if (reader.canRead() && reader.peek() == '{') {
            final CompoundBinaryTag pattern = readPattern(reader);
            return fanOut(tag -> matchingField(tag, name, pattern));
        }
        return fanOut(tag -> field(tag, name));
    }

    private static Function<List<BinaryTag>, List<BinaryTag>> fanOut(final UnaryOperator<List<BinaryTag>> perTag) {
        return candidates -> {
            final List<BinaryTag> result = new ArrayList<>();
            for (final BinaryTag candidate : candidates) {
                result.addAll(perTag.apply(List.of(candidate)));
            }
            return result;
        };
    }

    private static List<BinaryTag> field(final List<BinaryTag> single, final String name) {
        final BinaryTag tag = single.getFirst();
        if (tag instanceof final CompoundBinaryTag compound) {
            final BinaryTag value = compound.get(name);
            return value == null ? List.of() : List.of(value);
        }
        return List.of();
    }

    private static List<BinaryTag> matchingField(final List<BinaryTag> single, final String name, final CompoundBinaryTag pattern) {
        final BinaryTag tag = single.getFirst();
        if (tag instanceof final CompoundBinaryTag compound) {
            final BinaryTag value = compound.get(name);
            if (value != null && NbtMatching.matches(pattern, value)) {
                return List.of(value);
            }
        }
        return List.of();
    }

    private static List<BinaryTag> singleListElement(final List<BinaryTag> single, final int index) {
        final BinaryTag tag = single.getFirst();
        if (tag instanceof final ListBinaryTag list) {
            final int size = list.size();
            final int actual = index < 0 ? size + index : index;
            if (actual >= 0 && actual < size) {
                return List.of(list.get(actual));
            }
        }
        return List.of();
    }

    private static List<BinaryTag> allListElements(final List<BinaryTag> single) {
        final BinaryTag tag = single.getFirst();
        if (tag instanceof final ListBinaryTag list) {
            return List.copyOf(list.stream().toList());
        }
        return List.of();
    }

    private static List<BinaryTag> matchingListElements(final List<BinaryTag> single, final CompoundBinaryTag pattern) {
        final BinaryTag tag = single.getFirst();
        if (tag instanceof final ListBinaryTag list) {
            final List<BinaryTag> out = new ArrayList<>();
            for (final BinaryTag element : list) {
                if (NbtMatching.matches(pattern, element)) {
                    out.add(element);
                }
            }
            return out;
        }
        return List.of();
    }

    private static List<BinaryTag> selfIfMatches(final List<BinaryTag> single, final CompoundBinaryTag pattern) {
        final BinaryTag tag = single.getFirst();
        if (tag instanceof CompoundBinaryTag && NbtMatching.matches(pattern, tag)) {
            return List.of(tag);
        }
        return List.of();
    }

    private static String readUnquotedName(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedUnquoted(reader.peek())) {
            reader.skip();
        }
        if (reader.getCursor() == start) {
            throw ERROR_INVALID_NODE.createWithContext(reader);
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private static boolean isAllowedUnquoted(final char c) {
        return c != ' ' && c != '"' && c != '\'' && c != '[' && c != ']' && c != '.' && c != '{' && c != '}';
    }

    private static CompoundBinaryTag readPattern(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        int depth = 0;
        char quote = 0;

        do {
            if (!reader.canRead()) {
                throw ERROR_INVALID_NODE.createWithContext(reader);
            }
            final char c = reader.read();
            if (quote != 0) {
                if (c == '\\' && reader.canRead()) {
                    reader.skip();
                } else if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            switch (c) {
                case '"', '\'' -> quote = c;
                case '{' -> depth++;
                case '}' -> depth--;
                default -> { }
            }
        } while (depth > 0);

        final String snbt = reader.getString().substring(start, reader.getCursor());
        try {
            return TagStringIO.tagStringIO().asCompound(snbt);
        } catch (final IOException e) {
            reader.setCursor(start);
            throw ERROR_INVALID_NODE.createWithContext(reader);
        }
    }

    private record ChainPath(String text, List<Function<List<BinaryTag>, List<BinaryTag>>> chain) implements NbtPath {

        @Override
        public List<BinaryTag> select(final BinaryTag root) {
            List<BinaryTag> current = List.of(root);
            for (final Function<List<BinaryTag>, List<BinaryTag>> step : chain) {
                current = step.apply(current);
                if (current.isEmpty()) {
                    return List.of();
                }
            }
            return current;
        }

        @Override
        public String asString() {
            return text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final class Info implements ArgumentTypeRegistrar<NbtDataArgument, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            return new Spec();
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
        }

        @Override
        public Spec access(final NbtDataArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<NbtDataArgument> {

            @Override
            public NbtDataArgument instantiate() {
                return NbtDataArgument.nbtPath();
            }

            @Override
            public ArgumentTypeRegistrar<NbtDataArgument, ?> type() {
                return new Info();
            }
        }
    }
}
