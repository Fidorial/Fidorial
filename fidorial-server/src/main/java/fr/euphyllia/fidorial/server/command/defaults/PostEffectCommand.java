package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.Player;
import fr.fidorial.event.player.PlayerPostEffectsModifyEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class PostEffectCommand {

    private static final String PERMISSION = "fidorial.command.posteffect";

    private static final Messages ADDED = new Messages(
            "command.posteffect.added", "command.posteffect.added.many", "command.posteffect.add.unchanged");
    private static final Messages REMOVED = new Messages(
            "command.posteffect.removed", "command.posteffect.removed.many", "command.posteffect.remove.unchanged");
    private static final Messages CLEARED = new Messages(
            "command.posteffect.cleared", "command.posteffect.cleared.many", "command.posteffect.clear.unchanged");

    private PostEffectCommand() {
        throw new UnsupportedOperationException("PostEffectCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("posteffect")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("add")
                        .then(argument("targets", ArgumentTypes.players())
                                .then(argument("effect", ArgumentTypes.postEffect())
                                        .executes(PostEffectCommand::add))))
                .then(literal("remove")
                        .then(argument("targets", ArgumentTypes.players())
                                .then(argument("effect", ArgumentTypes.postEffect())
                                        .executes(PostEffectCommand::remove))))
                .then(literal("clear")
                        .then(argument("targets", ArgumentTypes.players())
                                .executes(PostEffectCommand::clear)))
                .then(literal("list")
                        .then(argument("target", ArgumentTypes.player())
                                .executes(PostEffectCommand::list)))
                .build();
    }

    private static int add(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final Key effect = context.getArgument("effect", Key.class);
        return modify(context, ADDED, effect, current -> Stream.concat(current.stream(), Stream.of(effect)).toList());
    }

    private static int remove(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final Key effect = context.getArgument("effect", Key.class);
        return modify(context, REMOVED, effect, current -> current.stream().filter(active -> !active.equals(effect)).toList());
    }

    private static int clear(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return modify(context, CLEARED, null, _ -> List.of());
    }

    private static int modify(final CommandContext<CommandSource> context, final Messages messages, final @Nullable Key effect, final UnaryOperator<List<Key>> change) throws CommandSyntaxException {
        final List<ServerPlayer> players = context.getArgument("targets", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource())
                .stream()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .toList();
        final List<CompletableFuture<Boolean>> results = players.stream()
                .map(player -> player.modifyPostEffects(PlayerPostEffectsModifyEvent.Cause.COMMAND, change))
                .toList();
        final CommandSender sender = context.getSource().sender();
        final List<Component> effectArg = effect == null ? List.of() : List.of(Component.text(effect.asString()));

        CompletableFuture.allOf(results.toArray(CompletableFuture[]::new)).whenComplete((_, _) -> {
            final List<Player> changed = new ArrayList<>(players.size());
            for (int i = 0; i < players.size(); i++) {
                final CompletableFuture<Boolean> result = results.get(i);
                if (!result.isCompletedExceptionally() && result.getNow(false)) {
                    changed.add(players.get(i));
                }
            }
            if (changed.isEmpty()) {
                sender.sendMessage(Component.translatable(messages.unchanged(), effectArg));
                return;
            }
            final boolean single = changed.size() == 1;
            final Component subject = single
                    ? Component.text(changed.getFirst().name())
                    : Component.text(changed.size());
            final List<Component> args = Stream.concat(effectArg.stream(), Stream.of(subject)).toList();
            sender.sendMessage(Component.translatable(single ? messages.single() : messages.many(), args));
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int list(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource())
                .getFirst();
        final List<Key> effects = target.activePostEffects();
        final CommandSender sender = context.getSource().sender();

        if (effects.isEmpty()) {
            sender.sendMessage(Component.translatable("command.posteffect.list.none", Component.text(target.name())));
        } else {
            final Component joined = Component.join(JoinConfiguration.commas(true),
                    effects.stream().map(effect -> Component.text(effect.asString())).toList());
            sender.sendMessage(Component.translatable("command.posteffect.list",
                    Component.text(target.name()), Component.text(effects.size()), joined));
        }
        return effects.size();
    }

    private record Messages(String single, String many, String unchanged) {
    }
}
