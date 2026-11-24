package fr.euphyllia.fidorial.server.command.fidorial;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.fidorial.utils.TpsBarManager;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PlayersResolver;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * {@code /tpsbar [targets]}: turns on or off a boss bar showing, live, the TPS, MSPT and thread
 * load of the region the player stands in, their ping and the memory used by the server.
 */
public final class TpsBarCommand {

    private static final String PERMISSION = "fidorial.command.tpsbar";
    private static final String PERMISSION_OTHERS = "fidorial.command.tpsbar.others";
    private static final String TARGETS = "targets";

    private TpsBarCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("tpsbar")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(TpsBarCommand::toggleSelf)
                .then(argument(TARGETS, arguments().players())
                        .requires(source -> source.sender().hasPermission(PERMISSION_OTHERS))
                        .executes(TpsBarCommand::toggleTargets))
                .build();
    }

    private static TpsBarManager bars() {
        return FidorialServer.getInstance().commands().tpsBars();
    }

    private static int toggleSelf(final CommandContext<CommandSource> context) {
        if (!(context.getSource().executor() instanceof final Player player)) {
            context.getSource().sendMessage(Component.translatable("command.tpsbar.not_player"));
            return 0;
        }
        return toggle(context.getSource(), List.of(player));
    }

    private static int toggleTargets(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return toggle(context.getSource(), PlayersResolver.players(context, TARGETS));
    }

    private static int toggle(final CommandSource source, final List<Player> targets) {
        final TpsBarManager bars = bars();
        for (final Player target : targets) {
            final String state = bars.toggle(target) ? "enabled" : "disabled";
            if (source.executor() != target) {
                target.sendMessage(Component.translatable("command.tpsbar." + state));
                source.sendMessage(Component.translatable(
                        "command.tpsbar." + state + ".other", Component.text(target.name())));
            } else {
                source.sendMessage(Component.translatable("command.tpsbar." + state));
            }
        }
        return targets.size();
    }
}
