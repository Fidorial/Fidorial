package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.text.Component;

import static fr.fidorial.command.Commands.literal;

public class StopCommand {
    public static LiteralCommandNode<CommandSource> create() {
        return literal("stop")
                .requires(source -> source.sender().hasPermission("fidorial.command.stop"))
                .executes(StopCommand::stop)
                .build();
    }

    private static int stop(final CommandContext<CommandSource> context) {
        FidorialServer.getInstance().sendMessage(Component.translatable("command.stop.disabling"));
        FidorialServer.getInstance().shutdown();
        return Command.SINGLE_SUCCESS;
    }
}
