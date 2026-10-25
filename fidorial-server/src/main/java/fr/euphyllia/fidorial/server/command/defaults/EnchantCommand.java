package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.item.enchantment.FidorialEnchantmentRegistry;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.EntitiesResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * The vanilla {@code /enchant} command, for the vanilla enchantments and those of plugins alike.
 *
 * <pre>
 * /enchant &lt;targets&gt; &lt;enchantment&gt; [&lt;level&gt;]
 * </pre>
 *
 * <p>Like vanilla, the item in the main hand of each target is enchanted when the enchantment supports
 * it and is compatible with the enchantments already there, up to the maximum level of the enchantment.
 * Each target is changed on the thread of the region owning it.</p>
 */
public final class EnchantCommand {

    private static final String PERMISSION = "fidorial.command.enchant";

    private static final Dynamic2CommandExceptionType LEVEL_TOO_HIGH =
            ExceptionFactory.dynamic2("commands.enchant.failed.level");
    private static final DynamicCommandExceptionType NOT_LIVING =
            ExceptionFactory.dynamic("commands.enchant.failed.entity");
    private static final DynamicCommandExceptionType NO_ITEM =
            ExceptionFactory.dynamic("commands.enchant.failed.itemless");
    private static final DynamicCommandExceptionType INCOMPATIBLE =
            ExceptionFactory.dynamic("commands.enchant.failed.incompatible");
    private static final SimpleCommandExceptionType FAILED = ExceptionFactory.simple("commands.enchant.failed");

    private EnchantCommand() {
        throw new UnsupportedOperationException("EnchantCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("enchant")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("targets", arguments().entities())
                        .then(argument("enchantment", arguments().resource(RegistryKey.ENCHANTMENT))
                                .executes(context -> enchant(context, 1))
                                .then(argument("level", integer(0))
                                        .executes(context -> enchant(context, context.getArgument("level", Integer.class))))))
                .build();
    }

    private static int enchant(final CommandContext<CommandSource> context, final int level) throws CommandSyntaxException {
        final Enchantment enchantment = context.getArgument("enchantment", Enchantment.class);
        final FidorialEnchantmentRegistry registry = context.getSource().server().registries().enchantments() instanceof
                final FidorialEnchantmentRegistry fidorial ? fidorial : null;
        if (registry == null) {
            throw FAILED.create();
        }
        final TypedKey<Enchantment> key = TypedKey.create(RegistryKey.ENCHANTMENT, enchantment.key());
        final EnchantmentDefinition definition = registry.definition(key).orElseThrow(FAILED::create);
        if (level > definition.maxLevel()) {
            throw LEVEL_TOO_HIGH.create(level, definition.maxLevel());
        }
        if (level < 1) {
            throw FAILED.create();
        }

        final List<Entity> targets = EntitiesResolver.entities(context, "targets");
        final List<CompletableFuture<Outcome>> outcomes = new ArrayList<>(targets.size());
        for (final Entity target : targets) {
            if (!(target instanceof final LivingEntity living)) {
                outcomes.add(CompletableFuture.completedFuture(Outcome.NOT_LIVING));
                continue;
            }
            if (living.isOwnedByCurrentThread()) {
                outcomes.add(CompletableFuture.completedFuture(apply(registry, living, key, level)));
                continue;
            }
            final CompletableFuture<Outcome> outcome = new CompletableFuture<>();
            if (!living.execute(() -> outcome.complete(apply(registry, living, key, level)))) {
                outcome.complete(Outcome.NOT_LIVING);
            }
            outcomes.add(outcome);
        }

        final Component name = registry.displayName(key, level);
        final CommandSource source = context.getSource();
        if (outcomes.stream().allMatch(CompletableFuture::isDone)) {
            final int count = succeeded(outcomes);
            if (count == 0) {
                throw failure(targets, outcomes);
            }
            source.sendMessage(feedback(name, targets, count));
            return count;
        }
        CompletableFuture.allOf(outcomes.toArray(CompletableFuture[]::new)).thenRun(() -> {
            final int count = succeeded(outcomes);
            source.sendMessage(count == 0
                    ? Component.translatable("commands.enchant.failed", NamedTextColor.RED)
                    : feedback(name, targets, count));
        });
        return outcomes.size();
    }

    private static Outcome apply(final FidorialEnchantmentRegistry registry, final LivingEntity living,
                                 final TypedKey<Enchantment> key, final int level) {
        if (!(living instanceof final Player player)) {
            return living.equipment(EquipmentSlot.MAIN_HAND).isEmpty() ? Outcome.NO_ITEM : Outcome.INCOMPATIBLE;
        }
        final ItemStack held = player.heldItem();
        if (held.isEmpty()) {
            return Outcome.NO_ITEM;
        }
        if (!registry.canEnchant(held, key)) {
            return Outcome.INCOMPATIBLE;
        }
        player.setHeldItem(registry.enchant(held, key, level));
        return Outcome.ENCHANTED;
    }

    private static int succeeded(final List<CompletableFuture<Outcome>> outcomes) {
        int count = 0;
        for (final CompletableFuture<Outcome> outcome : outcomes) {
            if (outcome.getNow(Outcome.NOT_LIVING) == Outcome.ENCHANTED) {
                count++;
            }
        }
        return count;
    }

    /**
     * Like vanilla, a single target gets a precise reason, several targets the generic one.
     */
    private static CommandSyntaxException failure(final List<Entity> targets, final List<CompletableFuture<Outcome>> outcomes) {
        if (targets.size() != 1) {
            return FAILED.create();
        }
        final Entity target = targets.getFirst();
        return switch (outcomes.getFirst().getNow(Outcome.NOT_LIVING)) {
            case NOT_LIVING -> NOT_LIVING.create(target.displayName());
            case NO_ITEM -> NO_ITEM.create(target.displayName());
            case INCOMPATIBLE -> INCOMPATIBLE.create(target instanceof final LivingEntity living
                    ? itemName(living.equipment(EquipmentSlot.MAIN_HAND))
                    : target.displayName());
            case ENCHANTED -> FAILED.create();
        };
    }

    private static Component itemName(final ItemStack stack) {
        return Component.translatable(stack).hoverEvent(stack);
    }

    private static Component feedback(final Component name, final List<Entity> targets, final int count) {
        return count == 1 && targets.size() == 1
                ? Component.translatable("commands.enchant.success.single", name, targets.getFirst().displayName())
                : Component.translatable("commands.enchant.success.multiple", name, Component.text(count));
    }

    private enum Outcome {
        ENCHANTED,
        NOT_LIVING,
        NO_ITEM,
        INCOMPATIBLE
    }
}
