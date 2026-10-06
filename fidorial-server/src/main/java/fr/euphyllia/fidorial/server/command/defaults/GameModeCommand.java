package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

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
                .then(argument("gamemode", ArgumentTypes.gameMode())
                        .executes(GameModeCommand::executeSelf)
                        .then(argument("target", ArgumentTypes.players())
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
        final var resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = resolver.resolve(context.getSource());
        return change(context, targets);
    }

    private static int change(final CommandContext<CommandSource> context, final List<Player> targets) {
        final GameMode mode = context.getArgument("gamemode", GameMode.class);

        for (final Player target : targets) {
            target.setGameMode(mode);
            target.sendMessage(Component.translatable("command.gamemode.changed.self", describe(mode)));

            if (context.getSource().sender() != target) {
                context.getSource()
                        .sendMessage(Component.translatable(
                                "command.gamemode.changed.other", Component.text(target.name()), describe(mode)));
            }
        }

        return targets.size();
    }
}
