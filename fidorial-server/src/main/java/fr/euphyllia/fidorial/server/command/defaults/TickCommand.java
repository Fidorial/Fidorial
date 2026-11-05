package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer.RegionTpsSnapshot;
import fr.euphyllia.fidorial.server.schedulers.tick.ServerTickRates;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.Locale;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * /tick query
 * /tick rate &lt;rate&gt;
 * /tick freeze|unfreeze
 * /tick step [&lt;time&gt;]|stop
 */
public final class TickCommand {

    private static final String PERMISSION = "fidorial.command.tick";

    private static final float MIN_RATE = 1.0F;
    private static final float MAX_RATE = 10_000.0F;

    private static final SimpleCommandExceptionType STEP_NOT_FROZEN = ExceptionFactory.simple("commands.tick.step.fail");
    private static final SimpleCommandExceptionType NOT_STEPPING = ExceptionFactory.simple("commands.tick.step.stop.fail");

    private TickCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("tick")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("query").executes(TickCommand::query))
                .then(literal("rate")
                        .then(argument("rate", floatArg(MIN_RATE, MAX_RATE))
                                .suggests((_, builder) -> builder.suggest("20").buildFuture())
                                .executes(context -> rate(context, context.getArgument("rate", Float.class)))))
                .then(literal("step")
                        .executes(context -> step(context, 1))
                        .then(literal("stop").executes(TickCommand::stopStepping))
                        .then(argument("time", arguments().time(1))
                                .suggests((_, builder) -> builder.suggest("1t").suggest("1s").buildFuture())
                                .executes(context -> step(context, context.getArgument("time", Integer.class)))))
                .then(literal("unfreeze").executes(context -> freeze(context, false)))
                .then(literal("freeze").executes(context -> freeze(context, true)))
                .build();
    }

    private static int query(final CommandContext<CommandSource> context) {
        final ServerTickRates rates = rates();
        final double[] mspt = FidorialServer.getInstance().workers().regionizer().tpsSnapshots().stream()
                .mapToDouble(RegionTpsSnapshot::msptAvg)
                .sorted()
                .toArray();
        final double busy = Arrays.stream(mspt).average().orElse(0.0);
        final double target = rates.nanosPerTick() / 1_000_000.0;

        final String status = rates.isFrozen() ? "commands.tick.status.frozen"
                : target < busy ? "commands.tick.status.lagging"
                : "commands.tick.status.running";
        final CommandSource source = context.getSource();
        source.sendMessage(Component.translatable(status));
        source.sendMessage(Component.translatable("commands.tick.query.rate.running",
                Component.text(oneDecimal(rates.rate())), Component.text(oneDecimal(busy)), Component.text(oneDecimal(target))));
        // Regions tick independently, so the percentiles are taken across regions.
        source.sendMessage(Component.translatable("commands.tick.query.percentiles",
                Component.text(oneDecimal(percentile(mspt, 0.5))),
                Component.text(oneDecimal(percentile(mspt, 0.95))),
                Component.text(oneDecimal(percentile(mspt, 0.99))),
                Component.text(mspt.length)));
        return (int) rates.rate();
    }

    private static int rate(final CommandContext<CommandSource> context, final float rate) {
        rates().setRate(rate);
        context.getSource().sendMessage(Component.translatable("commands.tick.rate.success", Component.text(oneDecimal(rate))));
        return (int) rate;
    }

    private static int freeze(final CommandContext<CommandSource> context, final boolean freeze) {
        final ServerTickRates rates = rates();
        if (freeze && rates.isStepping()) {
            rates.stopStepping();
        }
        rates.setFrozen(freeze);
        context.getSource().sendMessage(Component.translatable(
                freeze ? "commands.tick.status.frozen" : "commands.tick.status.running"));
        return freeze ? 1 : 0;
    }

    private static int step(final CommandContext<CommandSource> context, final int ticks) throws CommandSyntaxException {
        if (!rates().step(ticks)) {
            throw STEP_NOT_FROZEN.create();
        }
        context.getSource().sendMessage(Component.translatable("commands.tick.step.success", Component.text(ticks)));
        return Command.SINGLE_SUCCESS;
    }

    private static int stopStepping(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (!rates().stopStepping()) {
            throw NOT_STEPPING.create();
        }
        context.getSource().sendMessage(Component.translatable("commands.tick.step.stop.success"));
        return Command.SINGLE_SUCCESS;
    }

    private static double percentile(final double[] sorted, final double fraction) {
        return sorted.length == 0 ? 0.0 : sorted[(int) (sorted.length * fraction)];
    }

    private static String oneDecimal(final double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static ServerTickRates rates() {
        return FidorialServer.getInstance().worlds().tickRates();
    }
}
