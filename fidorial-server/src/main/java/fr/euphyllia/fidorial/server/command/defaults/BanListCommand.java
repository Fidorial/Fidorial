package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.moderation.BanEntry;
import fr.fidorial.moderation.BanManager;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.fidorial.command.Commands.literal;

public final class BanListCommand {

    private static final String PERMISSION = "fidorial.command.banlist";

    private BanListCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("banlist")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(context -> list(context, BanManager::bans))
                .then(literal("players").executes(context -> list(context, BanManager::profileBans)))
                .then(literal("ips").executes(context -> list(context, BanManager::ipBans)))
                .build();
    }

    private static int list(
            final CommandContext<CommandSource> context,
            final Function<BanManager, Stream<? extends BanEntry>> selector
    ) {
        final CommandSender sender = context.getSource().sender();
        final BanManager bans = context.getSource().server().bans();

        final List<? extends BanEntry> entries = selector.apply(bans).toList();


        if (entries.isEmpty()) {
            sender.sendMessage(Component.translatable("commands.banlist.none"));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Component.translatable(
                "commands.banlist.header", Component.text(entries.size())));

        for (final BanEntry entry : entries) {
            sender.sendMessage(Component.translatable(
                    "commands.banlist.entry",
                    Component.text(entry.label()),
                    BanCommand.sourceOf(entry),
                    BanCommand.expiryOf(entry),
                    BanCommand.reasonOf(entry)));
        }

        return entries.size();
    }
}
