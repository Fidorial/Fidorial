package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.ConsoleSender;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.euphyllia.fidorial.server.world.gamerule.VanillaGameRules;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.entity.Player;
import fr.fidorial.event.server.GameRuleChangeEvent;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.world.World;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class GameRuleCommand {

    private static final String PERMISSION = "fidorial.command.gamerule";
    private static final String VALUE = "value";
    private static final String WORLD = "world";
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
                .executes(context -> query(context, rule));

        final RequiredArgumentBuilder<CommandSource, ?> value = rule.isBoolean()
                ? argument(VALUE, ArgumentTypes.bool())
                : argument(VALUE, ArgumentTypes.integer(rule.minValue(), rule.maxValue()));
        value.executes(context -> set(context, rule, typedValue(context, rule), null))
                .then(argument(WORLD, ArgumentTypes.world())
                        .executes(context -> set(context, rule, typedValue(context, rule), targetWorld(context))));
        return node
                .then(value)
                .then(literal("inherit")
                        .then(argument(WORLD, ArgumentTypes.world())
                                .executes(context -> inherit(context, rule, targetWorld(context)))));
    }

    private static int typedValue(final CommandContext<CommandSource> context, final GameRuleDefinition rule) {
        return rule.isBoolean()
                ? (context.getArgument(VALUE, Boolean.class) ? 1 : 0)
                : context.getArgument(VALUE, Integer.class);
    }

    private static ServerWorld targetWorld(final CommandContext<CommandSource> context) {
        return (ServerWorld) context.getArgument(WORLD, World.class);
    }

    private static int query(final CommandContext<CommandSource> context, final GameRuleDefinition rule) {
        final CommandSender sender = context.getSource().sender();
        final ServerWorld world = sender instanceof final Player player && player.world() instanceof final ServerWorld playerWorld
                ? playerWorld
                : null;
        final Integer override = world == null ? null : world.gameRuleValues().override(rule);
        final int base = server.gameRules().get(rule);

        if (override == null) {
            sender.sendMessage(Component.translatable(
                    "commands.gamerule.query",
                    Component.text(rule.id()),
                    Component.text(rule.format(base))));
            return result(rule, base);
        }
        sender.sendMessage(Component.translatable(
                "commands.gamerule.query.world",
                Component.text(rule.id()),
                Component.text(rule.format(override)),
                Component.text(world.key().asString()),
                Component.text(rule.format(base))));
        return result(rule, override);
    }

    private static int set(
            final CommandContext<CommandSource> context,
            final GameRuleDefinition rule,
            final int value,
            final @Nullable ServerWorld world) {
        final CommandSender sender = context.getSource().sender();
        final FidorialGameRules rules = server.gameRules();
        final boolean override = world != null && !FidorialGameRules.holdsBaseValues(world);

        final FidorialGameRules.Result outcome = rules.apply(world, rule, value, GameRuleChangeEvent.Cause.COMMAND, sender);
        if (outcome == FidorialGameRules.Result.CANCELLED) {
            sender.sendMessage(Component.translatable("commands.gamerule.cancelled", Component.text(rule.id())));
            return 0;
        }

        final int applied = override ? world.gameRuleValues().get(rule) : rules.get(rule);
        if (override) {
            sender.sendMessage(Component.translatable(
                    "commands.gamerule.set.world",
                    Component.text(rule.id()),
                    Component.text(rule.format(applied)),
                    Component.text(world.key().asString())));
        } else {
            sender.sendMessage(Component.translatable(
                    "commands.gamerule.set",
                    Component.text(rule.id()),
                    Component.text(rule.format(applied))));
        }
        if (outcome == FidorialGameRules.Result.CHANGED && !(sender instanceof ConsoleSender)) {
            FidorialServer.LOGGER.info("{} set game rule {} to {} in {}", sender.name(), rule.id(), rule.format(applied),
                    override ? world.key().asString() : "every world without an override");
        }
        return result(rule, applied);
    }

    private static int inherit(
            final CommandContext<CommandSource> context,
            final GameRuleDefinition rule,
            final ServerWorld world) {
        final CommandSender sender = context.getSource().sender();
        final Component id = Component.text(rule.id());
        final Component worldName = Component.text(world.key().asString());

        switch (server.gameRules().removeOverride(world, rule, GameRuleChangeEvent.Cause.COMMAND, sender)) {
            case CANCELLED -> {
                sender.sendMessage(Component.translatable("commands.gamerule.cancelled", id));
                return 0;
            }
            case UNCHANGED -> {
                sender.sendMessage(Component.translatable("commands.gamerule.inherit.none", id, worldName));
                return 0;
            }
            case CHANGED -> {
                final int value = world.gameRuleValues().get(rule);
                sender.sendMessage(Component.translatable(
                        "commands.gamerule.inherit", id, worldName, Component.text(rule.format(value))));
                return result(rule, value);
            }
        }
        return 0;
    }

    private static int result(final GameRuleDefinition rule, final int value) {
        return rule.isBoolean() ? (value != 0 ? 1 : 0) : value;
    }
}
