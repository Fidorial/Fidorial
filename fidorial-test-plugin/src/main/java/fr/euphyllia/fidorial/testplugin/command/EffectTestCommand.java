package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.effect.TestEffects;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.effect.MobEffectRegistry;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.MobEffectKeys;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * {@code /testeffect bleed [level] | regen | list | clear}, on the player running it.
 */
public final class EffectTestCommand {

    private static final int DURATION_TICKS = 20 * 10;

    private final TestPlugin plugin;

    public EffectTestCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("testeffect")
                .then(literal("bleed")
                        .executes(ctx -> give(ctx, TestEffects.BLEEDING, 0))
                        .then(argument("level", ArgumentTypes.integer(1, MobEffectInstance.MAX_AMPLIFIER + 1))
                                .executes(ctx -> give(ctx, TestEffects.BLEEDING,
                                        ctx.getArgument("level", Integer.class) - 1))))
                .then(literal("regen").executes(ctx -> give(ctx, MobEffectKeys.REGENERATION, 1)))
                .then(literal("list").executes(this::list))
                .then(literal("clear").executes(this::clear))
                .build();
    }

    private int give(final CommandContext<CommandSource> ctx, final TypedKey<MobEffect> type, final int amplifier) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        final MobEffectRegistry effects = plugin.server().effects();
        final MobEffectInstance effect = effects.instance(type, DURATION_TICKS, amplifier);
        player.execute(() -> {
            final boolean applied = player.addEffect(effect);
            plugin.msg(player, applied
                    ? "<green>[TestPlugin] " + type.key().asString() + " " + (amplifier + 1) + " for 10 s"
                    : "<red>[TestPlugin] " + type.key().asString() + " not applied (stronger effect running?)");
        });
        return Command.SINGLE_SUCCESS;
    }

    private int list(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        if (player.activeEffects().isEmpty()) {
            plugin.msg(player, "<gray>[TestPlugin] No active effect");
            return 0;
        }
        for (final MobEffectInstance effect : player.activeEffects()) {
            final String remaining = effect.isInfinite() ? "∞" : effect.duration() / 20 + " s";
            plugin.msg(player, "<yellow>[TestPlugin] <white>" + effect.type().key().asString()
                    + "</white> level " + (effect.amplifier() + 1) + ", " + remaining);
        }
        return player.activeEffects().size();
    }

    private int clear(final CommandContext<CommandSource> ctx) {
        final Player player = player(ctx);
        if (player == null) {
            return 0;
        }
        player.execute(() -> plugin.msg(player, player.clearEffects()
                ? "<green>[TestPlugin] Effects cleared"
                : "<gray>[TestPlugin] No effect to clear"));
        return Command.SINGLE_SUCCESS;
    }

    private @Nullable Player player(final CommandContext<CommandSource> ctx) {
        if (ctx.getSource().sender() instanceof final Player player) {
            return player;
        }
        plugin.msg(ctx.getSource().sender(), "<red>[TestPlugin] Run this command in game.");
        return null;
    }
}
