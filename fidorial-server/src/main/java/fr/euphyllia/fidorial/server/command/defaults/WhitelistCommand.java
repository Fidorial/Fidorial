package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.PlayerProfileListResolver;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.moderation.WhitelistManager;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.List;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class WhitelistCommand {

    private static final String PERMISSION = "fidorial.command.whitelist";
    private static final FidorialServer server = FidorialServer.getInstance();

    private WhitelistCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        final ArgumentType<PlayerProfileListResolver> unlistedArgument =
                ArgumentTypes.playerProfiles(player -> !server.whitelist().contains(player.uuid()));

        final ArgumentType<PlayerProfileListResolver> listedArgument =
                ArgumentTypes.playerProfiles(player -> server.whitelist().contains(player.uuid()));

        return literal("whitelist")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("on").executes(context -> enforce(context, true)))
                .then(literal("off").executes(context -> enforce(context, false)))
                .then(literal("add")
                        .then(argument("player", unlistedArgument)
                                .executes(WhitelistCommand::add)))
                .then(literal("remove")
                        .then(argument("player", listedArgument)
                                .executes(WhitelistCommand::remove)))
                .then(literal("list").executes(WhitelistCommand::list))
                .then(literal("reload").executes(WhitelistCommand::reload))
                .build();
    }

    private static int enforce(final CommandContext<CommandSource> context, final boolean enabled) {
        final CommandSource source = context.getSource();
        final WhitelistManager whitelist = server.whitelist();

        if (!whitelist.enabled(enabled)) {
            source.sendMessage(Component.translatable(
                    enabled ? "commands.whitelist.alreadyOn" : "commands.whitelist.alreadyOff"));
            return 0;
        }

        source.sendMessage(Component.translatable(
                enabled ? "commands.whitelist.enabled" : "commands.whitelist.disabled"));

        kickDisallowed(source);

        return Command.SINGLE_SUCCESS;
    }

    private static int add(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final CommandSource source = context.getSource();
        final WhitelistManager whitelist = server.whitelist();

        final Collection<PlayerProfile> targets =
                context.getArgument("player", PlayerProfileListResolver.class).resolve(source);

        int added = 0;

        for (final PlayerProfile target : targets) {
            final Component name = Component.text(target.name());

            if (!whitelist.add(target)) {
                source.sendMessage(Component.translatable("commands.whitelist.add.failed", name));
                continue;
            }

            source.sendMessage(Component.translatable("commands.whitelist.add.success", name));
            added++;
        }

        return added;
    }

    private static int remove(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final CommandSource source = context.getSource();
        final WhitelistManager whitelist = server.whitelist();

        final Collection<PlayerProfile> targets =
                context.getArgument("player", PlayerProfileListResolver.class).resolve(source);

        int removed = 0;

        for (final PlayerProfile target : targets) {
            final Component name = Component.text(target.name());

            if (!whitelist.remove(target.uuid())) {
                source.sendMessage(Component.translatable("commands.whitelist.remove.failed", name));
                continue;
            }

            source.sendMessage(Component.translatable("commands.whitelist.remove.success", name));
            removed++;
        }

        if (removed > 0) {
            kickDisallowed(source);
        }

        return removed;
    }

    private static int list(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        final WhitelistManager whitelist = server.whitelist();

        final List<PlayerProfile> entries = whitelist.entries().toList();

        if (entries.isEmpty()) {
            source.sendMessage(Component.translatable("commands.whitelist.none"));
            return Command.SINGLE_SUCCESS;
        }

        source.sendMessage(Component.translatable(
                "commands.whitelist.list",
                Component.text(entries.size()),
                Component.text(entries.stream().map(PlayerProfile::name).reduce((a, b) -> a + ", " + b).orElse(""))));

        return Command.SINGLE_SUCCESS;
    }

    private static int reload(final CommandContext<CommandSource> context) {
        server.whitelist().load();

        context.getSource().sendMessage(Component.translatable("commands.whitelist.reloaded"));

        kickDisallowed(context.getSource());

        return Command.SINGLE_SUCCESS;
    }

    private static void kickDisallowed(final CommandSource source) {
        final int kicked = enforceWhitelist();

        if (kicked > 0) {
            source.sendMessage(Component.translatable(
                    "commands.whitelist.kicked", Component.text(kicked)));
        }
    }

    private static int enforceWhitelist() {
        if (!server.whitelist().enabled()) {
            return 0;
        }

        int kicked = 0;

        for (final ServerPlayer player : server.players()) {
            if (server.whitelist().contains(player.uuid())) {
                continue;
            }

            player.kick(Component.translatable("multiplayer.disconnect.not_whitelisted"));
            kicked++;
        }

        return kicked;
    }
}
