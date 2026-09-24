package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.ConsoleSender;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.euphyllia.fidorial.server.world.gamerule.VanillaGameRules;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.GameRuleDefinition;
import net.kyori.adventure.text.Component;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class GameRuleCommand {

    private static final String PERMISSION = "fidorial.command.gamerule";
    private static final String VALUE = "value";
    private static final FidorialServer server = FidorialServer.getInstance();

    private GameRuleCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        final LiteralArgumentBuilder<CommandSource> root = literal("gamerule")
                .requires(source -> source.sender().hasPermission(PERMISSION));
        for (final GameRuleDefinition rule : VanillaGameRules.ALL) {
            if (!rule.implemented()) {
                continue;
            }
            root.then(ruleNode(rule.id(), rule));
            root.then(ruleNode(rule.key().key().asString(), rule));
        }
        return root.build();
    }

    private static LiteralArgumentBuilder<CommandSource> ruleNode(final String name, final GameRuleDefinition rule) {
        final LiteralArgumentBuilder<CommandSource> node = literal(name)
                .executes(context -> query(context.getSource(), rule));
        if (rule.isBoolean()) {
            node.then(argument(VALUE, ArgumentTypes.bool())
                    .executes(context -> set(context, rule, context.getArgument(VALUE, Boolean.class) ? 1 : 0)));
        } else {
            node.then(argument(VALUE, ArgumentTypes.integer(rule.minValue(), rule.maxValue()))
                    .executes(context -> set(context, rule, context.getArgument(VALUE, Integer.class))));
        }
        return node;
    }

    private static int query(final CommandSource source, final GameRuleDefinition rule) {
        final int value = server.gameRules().get(rule);
        source.sender().sendMessage(Component.translatable(
                "commands.gamerule.query",
                Component.text(rule.id()),
                Component.text(rule.format(value))));
        return result(rule, value);
    }

    private static int set(final CommandContext<CommandSource> context, final GameRuleDefinition rule, final int value) {
        final CommandSender sender = context.getSource().sender();
        final FidorialGameRules rules = server.gameRules();

        final FidorialGameRules.Result outcome = rules.apply(rule, value, GameRuleChangeEvent.Cause.COMMAND, sender);
        if (outcome == FidorialGameRules.Result.CANCELLED) {
            sender.sendMessage(Component.translatable("commands.gamerule.cancelled", Component.text(rule.id())));
            return 0;
        }

        final int applied = rules.get(rule);
        sender.sendMessage(Component.translatable(
                "commands.gamerule.set",
                Component.text(rule.id()),
                Component.text(rule.format(applied))));
        if (outcome == FidorialGameRules.Result.CHANGED && !(sender instanceof ConsoleSender)) {
            FidorialServer.LOGGER.info("{} set game rule {} to {}", sender.name(), rule.id(), rule.format(applied));
        }
        return result(rule, applied);
    }

    private static int result(final GameRuleDefinition rule, final int value) {
        return rule.isBoolean() ? (value != 0 ? 1 : 0) : value;
    }
}
