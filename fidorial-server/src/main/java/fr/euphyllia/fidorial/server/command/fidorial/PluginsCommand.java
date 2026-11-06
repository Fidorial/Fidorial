package fr.euphyllia.fidorial.server.command.fidorial;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.plugin.PluginFailure;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * {@code /plugins [plugin]}: lists the plugins, green when enabled and red otherwise, and shows
 * why a plugin failed to start.
 */
public final class PluginsCommand {

    private static final String PERMISSION = "fidorial.command.plugins";
    private static final String ARGUMENT = "plugin";
    /** Stack trace lines shown in chat; the full trace is in the server log. */
    private static final int SHOWN_DETAIL_LINES = 12;

    private static final SuggestionProvider<CommandSource> PLUGINS = (context, builder) -> {
        final String remaining = builder.getRemainingLowerCase();
        for (final PluginFailure.Status status : statuses()) {
            if (status.key().toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(status.key());
            }
        }
        return builder.buildFuture();
    };

    private PluginsCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("plugins")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(PluginsCommand::list)
                .then(argument(ARGUMENT, greedyString())
                        .suggests(PLUGINS)
                        .executes(PluginsCommand::info))
                .build();
    }

    private static List<PluginFailure.Status> statuses() {
        return FidorialServer.getInstance().plugins().manager().statuses();
    }

    private static int list(final CommandContext<CommandSource> context) {
        final List<PluginFailure.Status> statuses = statuses();
        if (statuses.isEmpty()) {
            context.getSource().sendMessage(Component.translatable("command.plugins.none"));
            return 0;
        }

        final List<Component> names = new ArrayList<>(statuses.size());
        int enabled = 0;
        for (final PluginFailure.Status status : statuses) {
            if (status.enabled()) {
                enabled++;
            }
            names.add(entry(status));
        }

        context.getSource().sendMessage(Component.translatable(
                "command.plugins.list",
                Component.text(statuses.size()),
                Component.text(enabled),
                Component.text(statuses.size() - enabled),
                Component.join(JoinConfiguration.commas(true), names)));
        return statuses.size();
    }

    private static Component entry(final PluginFailure.Status status) {
        final Component hover;
        if (status.enabled()) {
            hover = Component.translatable("command.plugins.hover.enabled", version(status));
        } else if (status.failure() != null) {
            hover = Component.translatable(
                    "command.plugins.hover.failed",
                    Component.translatable(status.failure().phase().translationKey()),
                    Component.text(status.failure().summary()));
        } else {
            hover = Component.translatable("command.plugins.hover.disabled", version(status));
        }
        return Component.text(status.name(), status.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand("/plugins " + status.key()));
    }

    private static int info(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        final String query = StringArgumentType.getString(context, ARGUMENT).trim();
        final Optional<PluginFailure.Status> found = find(query);
        if (found.isEmpty()) {
            source.sendMessage(Component.translatable("command.plugins.unknown", Component.text(query)));
            return 0;
        }

        final PluginFailure.Status status = found.get();
        source.sendMessage(Component.translatable(
                "command.plugins.info.header",
                Component.text(status.name(), status.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED),
                version(status),
                Component.text(status.key())));
        if (!status.authors().isEmpty()) {
            source.sendMessage(Component.translatable(
                    "command.plugins.info.authors", Component.text(String.join(", ", status.authors()))));
        }

        final PluginFailure failure = status.failure();
        if (status.enabled()) {
            source.sendMessage(Component.translatable("command.plugins.info.enabled"));
            return Command.SINGLE_SUCCESS;
        }
        if (failure == null) {
            source.sendMessage(Component.translatable("command.plugins.info.disabled"));
            return Command.SINGLE_SUCCESS;
        }

        source.sendMessage(Component.translatable(
                "command.plugins.info.failed",
                Component.translatable(failure.phase().translationKey()),
                Component.text(failure.summary())));
        final List<String> details = failure.details();
        // the first line of the trace repeats the summary
        final int from = details.isEmpty() ? 0 : 1;
        final int to = Math.min(details.size(), from + SHOWN_DETAIL_LINES);
        for (int i = from; i < to; i++) {
            source.sendMessage(Component.text(details.get(i).replace("\t", "  "), NamedTextColor.GRAY));
        }
        if (details.size() > to) {
            source.sendMessage(Component.translatable(
                    "command.plugins.info.more", Component.text(details.size() - to)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Optional<PluginFailure.Status> find(final String query) {
        final List<PluginFailure.Status> statuses = statuses();
        for (final PluginFailure.Status status : statuses) {
            if (status.key().equalsIgnoreCase(query)) {
                return Optional.of(status);
            }
        }
        for (final PluginFailure.Status status : statuses) {
            if (status.name().equalsIgnoreCase(query)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }

    private static Component version(final PluginFailure.Status status) {
        return status.version() == null
                ? Component.translatable("command.plugins.version.unknown")
                : Component.text(status.version());
    }
}
