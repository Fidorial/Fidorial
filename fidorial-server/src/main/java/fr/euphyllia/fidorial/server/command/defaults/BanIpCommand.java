package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.Player;
import fr.fidorial.moderation.BanEntry;
import fr.fidorial.moderation.BanManager;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class BanIpCommand {

    private static final String PERMISSION = "fidorial.command.banip";
    private static final FidorialServer server = FidorialServer.getInstance();

    private BanIpCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("ban-ip")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("player", ArgumentTypes.players())
                        .executes(context -> ban(context, null, null))
                        .then(literal("duration")
                                .then(argument("duration", ArgumentTypes.duration())
                                        .executes(context -> ban(context, duration(context), null))
                                        .then(argument("reason", ArgumentTypes.component())
                                                .executes(context -> ban(context, duration(context), reason(context))))))
                        .then(literal("reason")
                                .then(argument("reason", ArgumentTypes.component())
                                        .executes(context -> ban(context, null, reason(context))))))
                .build();
    }

    private static Duration duration(final CommandContext<CommandSource> context) {
        return context.getArgument("duration", Duration.class);
    }

    private static Component reason(final CommandContext<CommandSource> context) {
        return context.getArgument("reason", Component.class);
    }

    private static int ban(
            final CommandContext<CommandSource> context,
            @Nullable final Duration duration,
            @Nullable final Component reason
    ) throws CommandSyntaxException {

        final CommandSource source = context.getSource();
        final BanManager bans = server.bans();

        final List<Player> targets =
                context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source);

        final UUID issuer = source.sender() instanceof final Player player ? player.uuid() : null;
        final Set<InetAddress> seen = new LinkedHashSet<>();

        int banned = 0;

        for (final Player target : targets) {
            final InetAddress address = target.address();

            if (!seen.add(address)) {
                continue;
            }

            final BanEntry.Address entry = duration == null
                    ? BanEntry.Address.permanent(address, target.name(), reason, issuer)
                    : BanEntry.Address.lasting(address, target.name(), reason, issuer, duration);

            final boolean added = bans.ban(entry);
            final int kicked = kickBanned(entry);

            source.sender()
                    .sendMessage(Component.translatable(
                            added ? "commands.banip.success" : "commands.banip.updated",
                            Component.text(entry.label()),
                            Component.text(kicked),
                            BanCommand.expiryOf(entry),
                            BanCommand.reasonOf(entry)));

            banned++;
        }

        return banned;
    }

    private static int kickBanned(final BanEntry.Address entry) {
        int kicked = 0;

        for (final ServerPlayer player : server.players()) {
            if (player.address().equals(entry.address())) {
                player.kick(server.bans().disconnectMessage(entry));
                kicked++;
            }
        }

        return kicked;
    }
}
