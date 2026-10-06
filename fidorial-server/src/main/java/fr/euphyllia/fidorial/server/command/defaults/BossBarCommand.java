package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.world.BossBarRegistry;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.Player;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class BossBarCommand {

    private static final Map<BossBar.Color, NamedTextColor> COLOR_FORMATTING = new EnumMap<>(Map.of(
            BossBar.Color.PINK, NamedTextColor.LIGHT_PURPLE,
            BossBar.Color.BLUE, NamedTextColor.BLUE,
            BossBar.Color.RED, NamedTextColor.RED,
            BossBar.Color.GREEN, NamedTextColor.GREEN,
            BossBar.Color.YELLOW, NamedTextColor.YELLOW,
            BossBar.Color.PURPLE, NamedTextColor.DARK_PURPLE,
            BossBar.Color.WHITE, NamedTextColor.WHITE));

    private static final DynamicCommandExceptionType ERROR_BOSSBAR_EXISTS =
            ExceptionFactory.dynamic("commands.bossbar.create.failed");
    private static final DynamicCommandExceptionType ERROR_BOSSBAR_UNKNOWN =
            ExceptionFactory.dynamic("commands.bossbar.unknown");
    private static final SimpleCommandExceptionType ERROR_PLAYERS_UNCHANGED =
            ExceptionFactory.simple("commands.bossbar.set.players.unchanged");
    private static final SimpleCommandExceptionType ERROR_NAME_UNCHANGED =
            ExceptionFactory.simple("commands.bossbar.set.name.unchanged");
    private static final SimpleCommandExceptionType ERROR_COLOR_UNCHANGED =
            ExceptionFactory.simple("commands.bossbar.set.color.unchanged");
    private static final SimpleCommandExceptionType ERROR_OVERLAY_UNCHANGED =
            ExceptionFactory.simple("commands.bossbar.set.style.unchanged");
    private static final SimpleCommandExceptionType ERROR_VALUE_UNCHANGED =
            ExceptionFactory.simple("commands.bossbar.set.value.unchanged");
    private static final SimpleCommandExceptionType ERROR_MAX_UNCHANGED =
            ExceptionFactory.simple("commands.bossbar.set.max.unchanged");
    private static final SimpleCommandExceptionType ERROR_VISIBILITY_UNCHANGED_HIDDEN =
            ExceptionFactory.simple("commands.bossbar.set.visibility.unchanged.hidden");
    private static final SimpleCommandExceptionType ERROR_VISIBILITY_UNCHANGED_VISIBLE =
            ExceptionFactory.simple("commands.bossbar.set.visibility.unchanged.visible");

    public static final SuggestionProvider<CommandSource> BOSSBAR_ID_SUGGESTIONS = (ctx, builder) -> {
        bossBars().entries().forEach(entry -> builder.suggest(entry.id().asString()));
        return builder.buildFuture();
    };

    public static LiteralCommandNode<CommandSource> create() {
        final ArgumentType<PlayerSelectorArgumentResolver> targetsArgument = ArgumentTypes.players();

        return literal("bossbar")
                .requires(source -> source.sender().hasPermission("fidorial.command.bossbar"))
                .then(literal("add")
                        .then(argument("id", ArgumentTypes.key())
                                .executes(ctx -> createBossBar(ctx, bossBarId(ctx), Component.text(bossBarId(ctx).asString())))
                                .then(argument("name", ArgumentTypes.component())
                                        .executes(ctx -> createBossBar(ctx, bossBarId(ctx), ctx.getArgument("name", Component.class))))))
                .then(literal("remove")
                        .then(argument("id", ArgumentTypes.key())
                                .suggests(BOSSBAR_ID_SUGGESTIONS)
                                .executes(ctx -> deleteBossBar(ctx, requireEntry(ctx)))))
                .then(literal("list")
                        .executes(BossBarCommand::listBossBars))
                .then(literal("set")
                        .then(argument("id", ArgumentTypes.key())
                                .suggests(BOSSBAR_ID_SUGGESTIONS)
                                .then(literal("name")
                                        .then(argument("name", ArgumentTypes.component())
                                                .executes(ctx -> updateName(ctx, requireEntry(ctx), ctx.getArgument("name", Component.class)))))
                                .then(literal("color")
                                        .then(argument("color", ArgumentTypes.bossBarColor())
                                                .executes(ctx -> updateColor(ctx, requireEntry(ctx), ctx.getArgument("color", BossBar.Color.class)))))
                                .then(literal("style")
                                        .then(argument("style", ArgumentTypes.bossBarOverlay())
                                                .executes(ctx -> updateOverlay(ctx, requireEntry(ctx), ctx.getArgument("style", BossBar.Overlay.class)))))
                                .then(literal("value")
                                        .then(argument("value", ArgumentTypes.integer(0, Integer.MAX_VALUE))
                                                .executes(ctx -> updateValue(ctx, requireEntry(ctx), ctx.getArgument("value", Integer.class)))))
                                .then(literal("max")
                                        .then(argument("max", ArgumentTypes.integer(1, Integer.MAX_VALUE))
                                                .executes(ctx -> updateMax(ctx, requireEntry(ctx), ctx.getArgument("max", Integer.class)))))
                                .then(literal("visible")
                                        .then(argument("visible", ArgumentTypes.bool())
                                                .executes(ctx -> updateVisibility(ctx, requireEntry(ctx), ctx.getArgument("visible", Boolean.class)))))
                                .then(literal("players")
                                        .executes(ctx -> updateTargets(ctx, requireEntry(ctx), Set.of()))
                                        .then(argument("targets", targetsArgument)
                                                .executes(ctx -> updateTargets(ctx, requireEntry(ctx), resolveTargetIds(ctx)))))))
                .then(literal("get")
                        .then(argument("id", ArgumentTypes.key())
                                .suggests(BOSSBAR_ID_SUGGESTIONS)
                                .then(literal("value").executes(ctx -> reportValue(ctx, requireEntry(ctx))))
                                .then(literal("max").executes(ctx -> reportMax(ctx, requireEntry(ctx))))
                                .then(literal("visible").executes(ctx -> reportVisibility(ctx, requireEntry(ctx))))
                                .then(literal("players").executes(ctx -> reportTargets(ctx, requireEntry(ctx))))))
                .build();
    }

    private static BossBarRegistry bossBars() {
        return FidorialServer.getInstance().bossBarRegistry();
    }

    private static Key bossBarId(final CommandContext<CommandSource> ctx) {
        return ctx.getArgument("id", Key.class);
    }

    private static BossBarRegistry.BossBarEntry requireEntry(final CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        final Key id = bossBarId(ctx);
        return bossBars().getEntry(id).orElseThrow(() -> ERROR_BOSSBAR_UNKNOWN.create(id.asString()));
    }

    private static int applyUpdate(
            final CommandContext<CommandSource> ctx,
            final BossBarRegistry.BossBarEntry entry,
            final Predicate<BossBarRegistry.BossBarEntry> unless,
            final SimpleCommandExceptionType unchangedError,
            final Runnable mutate,
            final String successKey
    ) throws CommandSyntaxException {
        if (unless.test(entry)) {
            throw unchangedError.create();
        }
        mutate.run();
        final BossBarRegistry.BossBarEntry updated = bossBars().getEntry(entry.id()).orElse(entry);
        ctx.getSource().sendMessage(Component.translatable(successKey, displayNameOf(updated)));
        return Command.SINGLE_SUCCESS;
    }

    private static Component displayNameOf(final BossBarRegistry.BossBarEntry entry) {
        final String idString = entry.id().asString();
        final NamedTextColor color = COLOR_FORMATTING.getOrDefault(entry.bar().color(), NamedTextColor.WHITE);

        return Component.text("[")
                .append(entry.bar().name())
                .append(Component.text("]"))
                .color(color)
                .hoverEvent(HoverEvent.showText(Component.text(idString)))
                .insertion(idString);
    }

    private static int createBossBar(final CommandContext<CommandSource> ctx, final Key id, final Component name) throws CommandSyntaxException {
        if (!bossBars().create(id, name)) {
            throw ERROR_BOSSBAR_EXISTS.create(id.asString());
        }

        final BossBarRegistry.BossBarEntry created = bossBars().getEntry(id).orElseThrow();
        ctx.getSource().sendMessage(Component.translatable("commands.bossbar.create.success", displayNameOf(created)));
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteBossBar(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry) {
        final Component displayName = displayNameOf(entry);
        bossBars().unregister(entry.id());
        ctx.getSource().sendMessage(Component.translatable("commands.bossbar.remove.success", displayName));
        return bossBars().entries().size();
    }

    private static int listBossBars(final CommandContext<CommandSource> ctx) {
        final var entries = bossBars().entries();
        if (entries.isEmpty()) {
            ctx.getSource().sendMessage(Component.translatable("commands.bossbar.list.bars.none"));
        } else {
            final Component joined = Component.join(JoinConfiguration.commas(true),
                    entries.stream().map(BossBarCommand::displayNameOf).toList());
            ctx.getSource().sendMessage(Component.translatable(
                    "commands.bossbar.list.bars.some", Component.text(entries.size()), joined));
        }

        return entries.size();
    }

    private static int updateName(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final Component name) throws CommandSyntaxException {
        return applyUpdate(ctx, entry,
                e -> e.bar().name().equals(name), ERROR_NAME_UNCHANGED,
                () -> entry.bar().name(name),
                "commands.bossbar.set.name.success");
    }

    private static int updateColor(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final BossBar.Color color) throws CommandSyntaxException {
        return applyUpdate(ctx, entry,
                e -> e.bar().color() == color, ERROR_COLOR_UNCHANGED,
                () -> entry.bar().color(color),
                "commands.bossbar.set.color.success");
    }

    private static int updateOverlay(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final BossBar.Overlay overlay) throws CommandSyntaxException {
        return applyUpdate(ctx, entry,
                e -> e.bar().overlay() == overlay, ERROR_OVERLAY_UNCHANGED,
                () -> entry.bar().overlay(overlay),
                "commands.bossbar.set.style.success");
    }

    private static int updateValue(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final int value) throws CommandSyntaxException {
        if (entry.value() == value) {
            throw ERROR_VALUE_UNCHANGED.create();
        }

        bossBars().setValue(entry.id(), value);
        ctx.getSource().sendMessage(
                Component.translatable("commands.bossbar.set.value.success", displayNameOf(entry), Component.text(value)));
        return value;
    }

    private static int updateMax(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final int max) throws CommandSyntaxException {
        if (entry.max() == max) {
            throw ERROR_MAX_UNCHANGED.create();
        }

        bossBars().setMax(entry.id(), max);
        ctx.getSource().sendMessage(
                Component.translatable("commands.bossbar.set.max.success", displayNameOf(entry), Component.text(max)));
        return max;
    }

    private static int updateVisibility(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final boolean visible) throws CommandSyntaxException {
        if (entry.visible() == visible) {
            throw visible ? ERROR_VISIBILITY_UNCHANGED_VISIBLE.create() : ERROR_VISIBILITY_UNCHANGED_HIDDEN.create();
        }

        bossBars().setVisible(entry.id(), visible);
        ctx.getSource().sendMessage(Component.translatable(
                visible ? "commands.bossbar.set.visible.success.visible" : "commands.bossbar.set.visible.success.hidden",
                displayNameOf(entry)));
        return Command.SINGLE_SUCCESS;
    }

    private static int updateTargets(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry, final Set<UUID> targetIds) throws CommandSyntaxException {
        if (entry.players().equals(targetIds)) {
            throw ERROR_PLAYERS_UNCHANGED.create();
        }

        bossBars().setPlayers(entry.id(), targetIds);

        if (targetIds.isEmpty()) {
            ctx.getSource().sendMessage(Component.translatable("commands.bossbar.set.players.success.none", displayNameOf(entry)));
        } else {
            ctx.getSource().sendMessage(Component.translatable(
                    "commands.bossbar.set.players.success.some",
                    displayNameOf(entry), Component.text(targetIds.size()), joinPlayerNames(targetIds)));
        }

        return targetIds.size();
    }

    private static Set<UUID> resolveTargetIds(final CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class);
        final List<Player> resolved = resolver.resolve(ctx.getSource());

        final Set<UUID> ids = new HashSet<>();
        for (final Player player : resolved) {
            ids.add(player.uuid());
        }
        return ids;
    }

    private static int reportValue(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry) {
        ctx.getSource().sendMessage(Component.translatable(
                "commands.bossbar.get.value", displayNameOf(entry), Component.text(entry.value())));
        return entry.value();
    }

    private static int reportMax(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry) {
        ctx.getSource().sendMessage(Component.translatable(
                "commands.bossbar.get.max", displayNameOf(entry), Component.text(entry.max())));
        return entry.max();
    }

    private static int reportVisibility(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry) {
        ctx.getSource().sendMessage(Component.translatable(
                entry.visible() ? "commands.bossbar.get.visible.visible" : "commands.bossbar.get.visible.hidden",
                displayNameOf(entry)));
        return entry.visible() ? 1 : 0;
    }

    private static int reportTargets(final CommandContext<CommandSource> ctx, final BossBarRegistry.BossBarEntry entry) {
        if (entry.players().isEmpty()) {
            ctx.getSource().sendMessage(Component.translatable("commands.bossbar.get.players.none", displayNameOf(entry)));
        } else {
            ctx.getSource().sendMessage(Component.translatable(
                    "commands.bossbar.get.players.some",
                    displayNameOf(entry), Component.text(entry.players().size()), joinPlayerNames(entry.players())));
        }

        return entry.players().size();
    }

    private static Component joinPlayerNames(final Set<UUID> ids) {
        final List<TextComponent> names = ids.stream()
                .map(id -> FidorialServer.getInstance().onlinePlayers().stream()
                        .filter(p -> p.uuid().equals(id))
                        .map(Player::name)
                        .findFirst()
                        .orElse(id.toString()))
                .map(Component::text)
                .toList();
        return Component.join(JoinConfiguration.commas(true), names);
    }
}
