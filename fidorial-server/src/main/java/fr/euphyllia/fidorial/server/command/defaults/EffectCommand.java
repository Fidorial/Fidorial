package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.resource.ResourceArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.selector.EntitySelectorArgumentResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.MobEffectKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * The vanilla {@code /effect} command.
 *
 * <pre>
 * /effect give &lt;targets&gt; &lt;effect&gt; [&lt;seconds&gt;|infinite] [&lt;amplifier&gt;] [&lt;hideParticles&gt;]
 * /effect clear [&lt;targets&gt;] [&lt;effect&gt;]
 * </pre>
 *
 * <p>Each target is changed on the thread of the region that owns it; when all of them belong to the
 * region running the command, the outcome is reported right away, like vanilla, otherwise once every
 * region is done.</p>
 */
public final class EffectCommand {

    private static final String PERMISSION = "fidorial.command.effect";

    private static final int MAX_SECONDS = 1_000_000;
    private static final int DEFAULT_SECONDS = 30;
    private static final int TICKS_PER_SECOND = 20;

    /**
     * Effects applied at once, whose duration is a number of ticks rather than seconds.
     */
    private static final Set<TypedKey<MobEffect>> INSTANTANEOUS =
            Set.of(MobEffectKeys.INSTANT_HEALTH, MobEffectKeys.INSTANT_DAMAGE, MobEffectKeys.SATURATION);

    private static final String GIVE_FAILED = "commands.effect.give.failed";
    private static final String CLEAR_EVERYTHING_FAILED = "commands.effect.clear.everything.failed";
    private static final String CLEAR_SPECIFIC_FAILED = "commands.effect.clear.specific.failed";
    private static final SimpleCommandExceptionType NOT_AN_ENTITY =
            ExceptionFactory.simple("permissions.requires.entity");

    private EffectCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("effect")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("clear")
                        .executes(context -> clear(context, List.of(self(context)), null))
                        .then(argument("targets", ArgumentTypes.entities())
                                .executes(context -> clear(context, targets(context), null))
                                .then(argument("effect", effectArgument())
                                        .executes(context -> clear(context, targets(context), effect(context))))))
                .then(literal("give")
                        .then(argument("targets", ArgumentTypes.entities())
                                .then(argument("effect", effectArgument())
                                        .executes(context -> give(context, null, 0, false))
                                        .then(argument("seconds", ArgumentTypes.integer(1, MAX_SECONDS))
                                                .executes(context -> give(context, seconds(context), 0, false))
                                                .then(argument("amplifier", ArgumentTypes.integer(0, MobEffectInstance.MAX_AMPLIFIER))
                                                        .executes(context -> give(context, seconds(context),
                                                                amplifier(context), false))
                                                        .then(argument("hideParticles", ArgumentTypes.bool())
                                                                .executes(context -> give(context, seconds(context),
                                                                        amplifier(context), hideParticles(context))))))
                                        .then(literal("infinite")
                                                .executes(context -> give(context, MobEffectInstance.INFINITE_DURATION, 0, false))
                                                .then(argument("amplifier", ArgumentTypes.integer(0, MobEffectInstance.MAX_AMPLIFIER))
                                                        .executes(context -> give(context, MobEffectInstance.INFINITE_DURATION,
                                                                amplifier(context), false))
                                                        .then(argument("hideParticles", ArgumentTypes.bool())
                                                                .executes(context -> give(context,
                                                                        MobEffectInstance.INFINITE_DURATION,
                                                                        amplifier(context), hideParticles(context)))))))))
                .build();
    }

    private static int give(final CommandContext<CommandSource> context, final @Nullable Integer seconds,
                            final int amplifier, final boolean hideParticles) throws CommandSyntaxException {
        final TypedKey<MobEffect> type = effect(context);
        final boolean instantaneous = INSTANTANEOUS.contains(type);
        final int duration;
        if (seconds == null) {
            duration = instantaneous ? 1 : DEFAULT_SECONDS * TICKS_PER_SECOND;
        } else if (seconds == MobEffectInstance.INFINITE_DURATION) {
            duration = MobEffectInstance.INFINITE_DURATION;
        } else {
            duration = instantaneous ? seconds : seconds * TICKS_PER_SECOND;
        }

        final MobEffectInstance effect = context.getSource().server().effects().builder(type)
                .duration(duration)
                .amplifier(amplifier)
                .visible(!hideParticles)
                .showIcon(!hideParticles)
                .build();
        final List<Entity> targets = targets(context);

        return apply(context, targets, living -> living.addEffect(effect), GIVE_FAILED, count -> count == 1
                ? Component.translatable("commands.effect.give.success.single", name(type), targets.getFirst().displayName())
                : Component.translatable("commands.effect.give.success.multiple", name(type), Component.text(count)));
    }

    private static int clear(final CommandContext<CommandSource> context, final List<Entity> targets,
                             final @Nullable TypedKey<MobEffect> type) throws CommandSyntaxException {
        if (type == null) {
            return apply(context, targets, LivingEntity::clearEffects, CLEAR_EVERYTHING_FAILED, count -> count == 1
                    ? Component.translatable("commands.effect.clear.everything.success.single", targets.getFirst().displayName())
                    : Component.translatable("commands.effect.clear.everything.success.multiple", Component.text(count)));
        }
        return apply(context, targets, living -> living.removeEffect(type), CLEAR_SPECIFIC_FAILED, count -> count == 1
                ? Component.translatable("commands.effect.clear.specific.success.single", name(type), targets.getFirst().displayName())
                : Component.translatable("commands.effect.clear.specific.success.multiple", name(type), Component.text(count)));
    }

    /**
     * Runs {@code action} on every living target, on the thread of the region owning it, then reports
     * how many targets it changed.
     */
    private static int apply(final CommandContext<CommandSource> context, final List<Entity> targets,
                             final Predicate<LivingEntity> action, final String failureKey,
                             final Feedback feedback) throws CommandSyntaxException {
        final List<CompletableFuture<Boolean>> results = new ArrayList<>(targets.size());
        for (final Entity target : targets) {
            if (!(target instanceof final LivingEntity living)) {
                continue;
            }
            if (living.isOwnedByCurrentThread()) {
                results.add(CompletableFuture.completedFuture(action.test(living)));
                continue;
            }
            final CompletableFuture<Boolean> result = new CompletableFuture<>();
            if (!living.execute(() -> result.complete(action.test(living)))) {
                result.complete(false);
            }
            results.add(result);
        }

        final CommandSender sender = context.getSource().sender();
        final boolean done = results.stream().allMatch(CompletableFuture::isDone);
        if (done) {
            final int count = succeeded(results);
            if (count == 0) {
                throw ExceptionFactory.simple(failureKey).create();
            }
            sender.sendMessage(feedback.message(count));
            return count;
        }

        CompletableFuture.allOf(results.toArray(CompletableFuture[]::new)).thenRun(() -> {
            final int count = succeeded(results);
            sender.sendMessage(count == 0
                    ? Component.translatable(failureKey, NamedTextColor.RED)
                    : feedback.message(count));
        });
        return results.size();
    }

    private static int succeeded(final List<CompletableFuture<Boolean>> results) {
        int count = 0;
        for (final CompletableFuture<Boolean> result : results) {
            if (result.getNow(false).equals(Boolean.TRUE)) {
                count++;
            }
        }
        return count;
    }

    private static Entity self(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (context.getSource().sender() instanceof final Entity entity) {
            return entity;
        }
        throw NOT_AN_ENTITY.create();
    }

    private static List<Entity> targets(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return context.getArgument("targets", EntitySelectorArgumentResolver.class).resolve(context.getSource());
    }

    /**
     * Vanilla effects and the ones plugins registered. Sent to the client as a {@code resource_key}, which
     * it does not check against its own registry, so the server validates and suggests the effects.
     */
    private static ArgumentType<TypedKey<MobEffect>> effectArgument() {
        return ArgumentTypes.withServerSuggestions(ArgumentTypes.map(
                ArgumentTypes.resourceKey(RegistryKey.MOB_EFFECT),
                (type, reader) -> {
                    if (!FidorialServer.getInstance().effects().isEffect(type)) {
                        throw ResourceArgument.ERROR_UNKNOWN_RESOURCE.createWithContext(
                                reader, type.key().asString(), RegistryKey.MOB_EFFECT.key().asString());
                    }
                    return type;
                },
                (context, builder) -> {
                    final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                    FidorialServer.getInstance().effects().types().stream()
                            .map(type -> type.key().asString())
                            .filter(key -> key.contains(remaining))
                            .sorted()
                            .forEach(builder::suggest);
                    return builder.buildFuture();
                }));
    }

    @SuppressWarnings("unchecked")
    private static TypedKey<MobEffect> effect(final CommandContext<CommandSource> context) {
        return (TypedKey<MobEffect>) context.getArgument("effect", TypedKey.class);
    }

    private static int seconds(final CommandContext<CommandSource> context) {
        return context.getArgument("seconds", Integer.class);
    }

    private static int amplifier(final CommandContext<CommandSource> context) {
        return context.getArgument("amplifier", Integer.class);
    }

    private static boolean hideParticles(final CommandContext<CommandSource> context) {
        return context.getArgument("hideParticles", Boolean.class);
    }

    private static Component name(final TypedKey<MobEffect> type) {
        return Component.translatable("effect." + type.key().namespace() + "." + type.key().value());
    }

    @FunctionalInterface
    private interface Feedback {
        Component message(int count);
    }
}
