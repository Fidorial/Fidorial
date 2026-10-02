package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.PlayerProfileListResolver;
import fr.fidorial.entity.OfflinePlayer;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.moderation.BanEntry;
import fr.fidorial.moderation.BanManager;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class BanCommand {

    private static final String PERMISSION = "fidorial.command.ban";
    private static final FidorialServer server = FidorialServer.getInstance();

    private BanCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        final ArgumentType<PlayerProfileListResolver> playerArgument =
                ArgumentTypes.playerProfiles(player ->
                        !server.bans().isBanned(player.uuid()));

        return literal("ban")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("player", playerArgument)
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

        final Collection<PlayerProfile> targets =
                context.getArgument("player", PlayerProfileListResolver.class).resolve(source);

        final UUID issuer = source.sender() instanceof final Player player ? player.uuid() : null;

        int banned = 0;

        for (final PlayerProfile target : targets) {
            final BanEntry.Profile entry = duration == null
                    ? BanEntry.Profile.permanent(target.uuid(), target.name(), reason, issuer)
                    : BanEntry.Profile.lasting(target.uuid(), target.name(), reason, issuer, duration);

            final boolean added = bans.ban(entry);

            kickBanned(entry);

            source.sender()
                    .sendMessage(Component.translatable(
                            added ? "commands.ban.success" : "commands.ban.updated",
                            Component.text(entry.label()),
                            expiryOf(entry),
                            reasonOf(entry)));

            banned++;
        }

        return banned;
    }

    static Component expiryOf(final BanEntry entry) {
        return entry.permanent()
                ? Component.translatable("commands.ban.expires.never")
                : Component.text(entry.expiresLabel());
    }

    static Component reasonOf(final BanEntry entry) {
        return entry.describeReason().orElseGet(() -> Component.translatable("commands.ban.reason.none"));
    }

    static Component sourceOf(final BanEntry entry) {
        final UUID source = entry.source();
        if (source == null) {
            return Component.translatable("commands.ban.source.server");
        }
        return Component.text(server.offlinePlayers().cached(source)
                .map(OfflinePlayer::label)
                .orElseGet(source::toString));
    }

    private static int kickBanned(final BanEntry.Profile entry) {
        int kicked = 0;

        for (final ServerPlayer player : server.players()) {
            if (player.uuid().equals(entry.uuid())) {
                player.kick(server.bans().disconnectMessage(entry));
                kicked++;
            }
        }

        return kicked;
    }
}
