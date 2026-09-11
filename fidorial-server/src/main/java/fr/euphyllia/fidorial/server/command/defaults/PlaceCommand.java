package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.BlockPosResolver;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.structure.StructureRotation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * {@code /place template|structure|jigsaw}, with the datapacks of {@code <world>/datapacks}.
 */
public final class PlaceCommand {

    private static final String PERMISSION = "fidorial.command.place";

    private static final SuggestionProvider<CommandSource> TEMPLATES =
            StructureCommands.keys(() -> StructureCommands.service().templates());
    private static final SuggestionProvider<CommandSource> STRUCTURES =
            StructureCommands.keys(() -> StructureCommands.service().structures());
    private static final SuggestionProvider<CommandSource> POOLS =
            StructureCommands.keys(() -> StructureCommands.service().registry().contents().templatePools().keySet());
    private static final SuggestionProvider<CommandSource> ROTATIONS = (context, builder) -> {
        for (final StructureRotation rotation : StructureRotation.values()) {
            if (rotation.serializedName().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(rotation.serializedName());
            }
        }
        return builder.buildFuture();
    };

    private PlaceCommand() {
        throw new UnsupportedOperationException("PlaceCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("place")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("template")
                        .then(argument("template", ArgumentTypes.key())
                                .suggests(TEMPLATES)
                                .executes(context -> template(context, false, false))
                                .then(argument("pos", ArgumentTypes.blockPosition())
                                        .executes(context -> template(context, true, false))
                                        .then(argument("rotation", ArgumentTypes.word())
                                                .suggests(ROTATIONS)
                                                .executes(context -> template(context, true, true))))))
                .then(literal("structure")
                        .then(argument("structure", ArgumentTypes.key())
                                .suggests(STRUCTURES)
                                .executes(context -> structure(context, false))
                                .then(argument("pos", ArgumentTypes.blockPosition())
                                        .executes(context -> structure(context, true)))))
                .then(literal("jigsaw")
                        .then(argument("pool", ArgumentTypes.key())
                                .suggests(POOLS)
                                .then(argument("target", ArgumentTypes.key())
                                        .then(argument("max_depth", ArgumentTypes.integer(1, 20))
                                                .executes(context -> jigsaw(context, false))
                                                .then(argument("pos", ArgumentTypes.blockPosition())
                                                        .executes(context -> jigsaw(context, true)))))))
                .build();
    }

    private static BlockPos position(final CommandContext<CommandSource> context, final boolean explicit)
            throws CommandSyntaxException {
        return explicit
                ? context.getArgument("pos", BlockPosResolver.class).resolve(context.getSource())
                : StructureCommands.here(context);
    }

    private static int template(final CommandContext<CommandSource> context, final boolean explicitPos,
                                final boolean explicitRotation) throws CommandSyntaxException {
        final ServerWorld world = StructureCommands.worldOf(context);
        if (world == null) {
            context.getSource().sender().sendMessage(Component.translatable("command.place.console"));
            return 0;
        }
        final Key id = context.getArgument("template", Key.class);
        StructureRotation rotation = StructureRotation.NONE;
        if (explicitRotation) {
            final String raw = context.getArgument("rotation", String.class);
            final StructureRotation parsed = StructureRotation.byName(raw);
            if (parsed == null) {
                context.getSource().sender().sendMessage(Component.translatable("command.place.rotation.invalid", Component.text(raw)));
                return 0;
            }
            rotation = parsed;
        }
        final StructureService service = StructureCommands.service();
        if (service.template(id).isEmpty()) {
            context.getSource().sender().sendMessage(Component.translatable("command.place.template.invalid", Component.text(id.asString())));
            return 0;
        }
        final BlockPos pos = position(context, explicitPos);
        report(context, service.placeTemplate(world, pos, id, rotation), id, pos,
                "command.place.template.success", "command.place.template.failed");
        return Command.SINGLE_SUCCESS;
    }

    private static int structure(final CommandContext<CommandSource> context, final boolean explicitPos)
            throws CommandSyntaxException {
        final ServerWorld world = StructureCommands.worldOf(context);
        if (world == null) {
            context.getSource().sender().sendMessage(Component.translatable("command.place.console"));
            return 0;
        }
        final Key id = context.getArgument("structure", Key.class);
        final StructureService service = StructureCommands.service();
        if (!service.structures().contains(id)) {
            context.getSource().sender().sendMessage(Component.translatable("command.place.structure.invalid", Component.text(id.asString())));
            return 0;
        }
        final BlockPos pos = position(context, explicitPos);
        report(context, service.placeStructure(world, pos, id), id, pos,
                "command.place.structure.success", "command.place.structure.failed");
        return Command.SINGLE_SUCCESS;
    }

    private static int jigsaw(final CommandContext<CommandSource> context, final boolean explicitPos)
            throws CommandSyntaxException {
        final ServerWorld world = StructureCommands.worldOf(context);
        if (world == null) {
            context.getSource().sender().sendMessage(Component.translatable("command.place.console"));
            return 0;
        }
        final Key pool = context.getArgument("pool", Key.class);
        final Key target = context.getArgument("target", Key.class);
        final int depth = context.getArgument("max_depth", Integer.class);
        final StructureService service = StructureCommands.service();
        if (service.registry().pool(pool) == null) {
            context.getSource().sender().sendMessage(Component.translatable("command.place.jigsaw.invalid", Component.text(pool.asString())));
            return 0;
        }
        final BlockPos pos = position(context, explicitPos);
        report(context, service.placeJigsaw(world, pos, pool, target, depth), pool, pos,
                "command.place.jigsaw.success", "command.place.jigsaw.failed");
        return Command.SINGLE_SUCCESS;
    }

    private static void report(final CommandContext<CommandSource> context, final CompletableFuture<Integer> future,
                               final Key id, final BlockPos pos, final String success, final String failed) {
        future.whenComplete((count, failure) -> {
            final @Nullable Integer placed = failure == null ? count : null;
            if (placed == null || placed == 0) {
                context.getSource().sender().sendMessage(Component.translatable(failed, Component.text(id.asString()),
                        Component.text(failure == null ? "-" : StructureCommands.rootMessage(failure))));
                return;
            }
            context.getSource().sender().sendMessage(Component.translatable(success,
                    Component.text(id.asString()),
                    Component.text(pos.x() + ", " + pos.y() + ", " + pos.z()),
                    Component.text(placed)));
        });
    }
}
