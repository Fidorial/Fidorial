package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.world.BlockPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * {@code /locate structure <structure>}: nearest start of a datapack structure.
 */
public final class LocateCommand {

    private static final String PERMISSION = "fidorial.command.locate";
    private static final int DEFAULT_RADIUS_CELLS = 64;
    private static final int MAX_RADIUS_CELLS = 128;

    private static final SuggestionProvider<CommandSource> STRUCTURES =
            StructureCommands.keys(() -> StructureCommands.service().structures());

    private LocateCommand() {
        throw new UnsupportedOperationException("LocateCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("locate")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("structure")
                        .then(argument("structure", ArgumentTypes.key())
                                .suggests(STRUCTURES)
                                .executes(context -> locate(context, DEFAULT_RADIUS_CELLS))
                                .then(argument("radius", ArgumentTypes.integer(1, MAX_RADIUS_CELLS))
                                        .executes(context -> locate(context, context.getArgument("radius", Integer.class))))))
                .build();
    }

    private static int locate(final CommandContext<CommandSource> context, final int radiusCells) {
        final ServerWorld world = StructureCommands.worldOf(context);
        if (world == null) {
            context.getSource().sender().sendMessage(Component.translatable("command.locate.console"));
            return 0;
        }
        final Key id = context.getArgument("structure", Key.class);
        final StructureService service = StructureCommands.service();
        if (!service.structures().contains(id)) {
            context.getSource().sender().sendMessage(Component.translatable("command.locate.structure.invalid", Component.text(id.asString())));
            return 0;
        }
        final BlockPos origin = StructureCommands.here(context);
        context.getSource().sender().sendMessage(Component.translatable("command.locate.structure.searching", Component.text(id.asString())));
        service.locate(world, origin, id, radiusCells).whenComplete((found, failure) -> {
            if (failure != null || found == null || found.isEmpty()) {
                context.getSource().sender().sendMessage(Component.translatable("command.locate.structure.notfound", Component.text(id.asString())));
                return;
            }
            final BlockPos pos = found.get();
            final long dx = pos.x() - origin.x();
            final long dz = pos.z() - origin.z();
            final int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
            final Component coordinates = Component.text("[" + pos.x() + ", ~, " + pos.z() + "]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.suggestCommand("/tp @s " + pos.x() + " ~ " + pos.z()))
                    .hoverEvent(HoverEvent.showText(Component.translatable("command.locate.structure.teleport")));
            context.getSource().sender().sendMessage(Component.translatable("command.locate.structure.success",
                    Component.text(id.asString()), coordinates, Component.text(distance)));
        }).exceptionally(failure -> {
            context.getSource().sender().sendMessage(Component.translatable("command.locate.structure.error", Component.text(id.asString()), Component.text(failure.getMessage())));
            return Optional.empty();
        });
        return Command.SINGLE_SUCCESS;
    }
}
