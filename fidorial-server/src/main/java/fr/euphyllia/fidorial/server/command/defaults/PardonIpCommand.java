package fr.euphyllia.fidorial.server.command.defaults;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.moderation.BanEntry;
import net.kyori.adventure.text.Component;

import java.util.Locale;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class PardonIpCommand {

    private static final String PERMISSION = "fidorial.command.pardonip";
    private static final FidorialServer server = FidorialServer.getInstance();

    private static final SuggestionProvider<CommandSource> BANNED = (_, builder) -> {
        final String remaining = builder.getRemainingLowerCase();

        server.bans().ipBans()
                .map(BanEntry.Address::label)
                .filter(label -> label.toLowerCase(Locale.ROOT).startsWith(remaining))
                .forEach(builder::suggest);

        return builder.buildFuture();
    };

    private PardonIpCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("pardon-ip")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("address", ArgumentTypes.word())
                        .suggests(BANNED)
                        .executes(PardonIpCommand::pardon))
                .build();
    }

    private static int pardon(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        final String address = context.getArgument("address", String.class);

        if (!InetAddresses.isInetAddress(address)) {
            source
                    .sendMessage(Component.translatable("commands.pardonip.invalid", Component.text(address)));
            return 0;
        }

        if (!server.bans().pardon(InetAddresses.forString(address))) {
            source
                    .sendMessage(Component.translatable("commands.pardonip.failed", Component.text(address)));
            return 0;
        }

        source
                .sendMessage(Component.translatable("commands.pardonip.success", Component.text(address)));

        return Command.SINGLE_SUCCESS;
    }
}
