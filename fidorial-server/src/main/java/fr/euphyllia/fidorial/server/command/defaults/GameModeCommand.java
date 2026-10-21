package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PlayersResolver;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

public final class GameModeCommand {

    private static Component describe(final GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> Component.translatable("gamemode.survival");
            case CREATIVE -> Component.translatable("gamemode.creative");
            case ADVENTURE -> Component.translatable("gamemode.adventure");
            case SPECTATOR -> Component.translatable("gamemode.spectator");
        };
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("gamemode")
                .requires(src -> src.sender().hasPermission("fidorial.command.gamemode"))
                .then(argument("gamemode", arguments().gameMode())
                        .executes(GameModeCommand::executeSelf)
                        .then(argument("target", arguments().players())
                                .executes(GameModeCommand::executeTarget)))
                .build();
    }

    private static int executeSelf(final CommandContext<CommandSource> context) {
        if (!(context.getSource().executor() instanceof final Player player)) {
            context.getSource().sendMessage(Component.translatable("command.gamemode.not_player"));
            return 0;
        }
        return change(context, List.of(player));
    }

    private static int executeTarget(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return change(context, PlayersResolver.players(context, "target"));
    }

    private static int change(final CommandContext<CommandSource> context, final List<Player> targets) {
        final CommandSource source = context.getSource();
        final GameMode mode = context.getArgument("gamemode", GameMode.class);
        final Component described = describe(mode);

        for (final Player target : targets) {
            target.setGameMode(mode);

            if (source.executor() != target) {
                target.sendMessage(Component.translatable("command.gamemode.changed.self", described));
                source.sendMessage(Component.translatable(
                        "command.gamemode.changed.other", Component.text(target.name()), described));
            } else {
                source.sendMessage(Component.translatable("command.gamemode.changed.self", described));
            }
        }

        return targets.size();
    }
}
