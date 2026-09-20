package fr.euphyllia.fidorial.server.network.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntitySelector;
import fr.euphyllia.fidorial.server.command.brigadier.argument.nbt.NbtDataArgument;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.world.CoordMath;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.entity.AnvilEntitySerializer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.NbtPathResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import fr.fidorial.world.BlockPos;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ComponentResolver {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ComponentResolver.class);
    private static final int MAX_COMPONENT_DEPTH = 64;
    private static final AnvilEntitySerializer ENTITY_SERIALIZER = new AnvilEntitySerializer();

    public static Component resolve(final Component component, final CommandSource source) {
        return resolve(component, source, 0);
    }

    private static Component resolve(final Component component, final CommandSource source, final int depth) {
        if (depth >= MAX_COMPONENT_DEPTH) {
            return component;
        }

        Component resolvedContent = switch (component) {
            case final SelectorComponent sel -> resolveSelector(sel, source);
            //case ScoreComponent score -> resolveScore(score, source); TBD
            case final NBTComponent<?> nbt -> resolveNbt(nbt, source);
            default -> component;
        };

        if (!resolvedContent.children().isEmpty()) {
            final List<Component> resolvedChildren = new ArrayList<>();
            for (final Component child : resolvedContent.children()) {
                resolvedChildren.add(resolve(child, source, depth + 1));
            }
            resolvedContent = resolvedContent.children(resolvedChildren);
        }
        return resolvedContent;
    }

    private static Component resolveSelector(final SelectorComponent sel, final CommandSource source) {
        final EntitySelector selector;
        try {
            selector = EntitySelector.parse(sel.pattern());
        } catch (final CommandSyntaxException e) {
            return Component.empty();
        }

        final List<Entity> matches;
        try {
            matches = new ArrayList<>(selector.findEntities(source));
        } catch (final CommandSyntaxException e) {
            return Component.empty();
        }

        if (matches.isEmpty()) {
            return Component.empty();
        }

        final Component separator = sel.separator() != null
                ? sel.separator()
                : Component.text(", ").color(NamedTextColor.GRAY);

        Component result = Component.empty();
        for (int i = 0; i < matches.size(); i++) {
            final Entity entity = matches.get(i);

            final Component name = entity.displayName().hoverEvent(entity);

            if (i != 0) {
                result = result.append(separator);
            }
            result = result.append(name);
        }

        return result;
    }

    private static Component resolveNbt(final NBTComponent<?> nbt, final CommandSource source) {
        final CompoundBinaryTag root = switch (nbt) {
            case final BlockNBTComponent block -> resolveBlockRoot(block, source);
            case final EntityNBTComponent entity -> resolveEntityRoot(entity, source);
            case final StorageNBTComponent storage -> resolveStorageRoot(storage);
        };
        if (root == null) {
            return Component.empty();
        }

        final NbtPathResolver path;
        try {
            path = NbtDataArgument.nbtPath().parse(new StringReader(nbt.nbtPath()));
        } catch (final CommandSyntaxException e) {
            return Component.empty();
        }

        final List<BinaryTag> matches = path.resolve(root);
        if (matches.isEmpty()) return Component.empty();

        final Component separator = nbt.separator() != null
                ? nbt.separator()
                : Component.text(", ").color(NamedTextColor.GRAY);

        final boolean shouldInterpret = nbt.interpret() && !nbt.nbtPath().isEmpty();
        final Component interpreted = shouldInterpret ? resolveInterpreting(matches, separator) : Component.empty();

        return interpreted.equals(Component.empty()) ? resolveFormatting(matches, separator, nbt.plain()) : interpreted;
    }

    private static Component resolveInterpreting(final List<BinaryTag> matches, final Component separator) {
        Component result = null;
        for (final BinaryTag tag : matches) {
            final Component parsed;
            try {
                parsed = ComponentCodecs.COMPONENT_CODEC.parse(BinaryTagOps.binaryTagOps(), tag).getOrThrow();
            } catch (final Exception e) {
                // might not be a component so render raw
                continue;
            }
            result = (result == null) ? parsed : result.append(separator).append(parsed);
        }
        return result == null ? Component.empty() : result;
    }

    private static Component resolveFormatting(final List<BinaryTag> matches, final Component separator, final boolean plain) {
        Component result = Component.empty();
        for (int i = 0; i < matches.size(); i++) {
            if (i != 0) {
                result = result.append(separator);
            }
            result = result.append(NbtTextDecorator.render(matches.get(i), plain));
        }
        return result;
    }

    private static @Nullable CompoundBinaryTag resolveEntityRoot(final EntityNBTComponent nbt, final CommandSource source) {
        final EntitySelector selector;
        try {
            selector = EntitySelector.parse(nbt.selector());
        } catch (final CommandSyntaxException e) {
            return null;
        }

        final List<Entity> matches;
        try {
            matches = new ArrayList<>(selector.findEntities(source));
        } catch (final CommandSyntaxException e) {
            return null;
        }

        if (matches.size() != 1 || !(matches.getFirst() instanceof final AbstractEntity entity)) {
            return null;
        }

        return ENTITY_SERIALIZER.toNbt(entity);
    }

    private static @Nullable CompoundBinaryTag resolveBlockRoot(final BlockNBTComponent nbt, final CommandSource source) {
        final Entity executor = source.executor();
        if (executor == null || !(executor.world() instanceof final ServerWorld world)) {
            return null;
        }

        final BlockPos pos = resolveBlockPos(nbt.pos(), executor.location());
        if (pos == null) {
            return null;
        }

        final ChunkColumn chunk = world.loadedColumn(pos.chunkX(), pos.chunkZ());
        if (chunk == null) {
            return null;
        }

        final BlockEntity blockEntity = chunk.blockEntity(pos.localX(), pos.y(), pos.localZ());
        if (blockEntity == null) {
            return null;
        }

        final CompoundBinaryTag.Builder root = CompoundBinaryTag.builder();
        final CompoundBinaryTag extra = blockEntity.data();

        root.putString("id", blockEntity.type().asString());
        root.putInt("x", pos.x());
        root.putInt("y", pos.y());
        root.putInt("z", pos.z());

        root.put("components",
                (extra != null && extra.keySet().contains("components"))
                        ? extra.get("components")
                        : CompoundBinaryTag.empty());

        if (extra != null) {
            for (final String key : extra.keySet()) {
                if (!key.equals("components")) {
                    root.put(key, extra.get(key));
                }
            }
        }

        return root.build();
    }

    private static @Nullable BlockPos resolveBlockPos(final BlockNBTComponent.Pos pos, final Location origin) {
        return switch (pos) {
            case final BlockNBTComponent.LocalPos local -> {
                final Location resolved = CoordMath.applyLocalCoords(
                        origin, local.left(), local.up(), local.forwards());
                yield new BlockPos(
                        (int) Math.floor(resolved.x()),
                        (int) Math.floor(resolved.y()),
                        (int) Math.floor(resolved.z()));
            }
            case final BlockNBTComponent.WorldPos world -> new BlockPos(
                    resolveCoordinate(world.x(), (int) Math.floor(origin.x())),
                    resolveCoordinate(world.y(), (int) Math.floor(origin.y())),
                    resolveCoordinate(world.z(), (int) Math.floor(origin.z())));
            default -> null;
        };
    }

    private static int resolveCoordinate(final BlockNBTComponent.WorldPos.Coordinate coordinate, final int originCoord) {
        return coordinate.type() == BlockNBTComponent.WorldPos.Coordinate.Type.RELATIVE
                ? originCoord + coordinate.value()
                : coordinate.value();
    }

    // TODO
    private static @Nullable CompoundBinaryTag resolveStorageRoot(final StorageNBTComponent nbt) {
        return null;
    }
}
