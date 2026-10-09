package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.mob.MobFactories;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.ArgumentTypes;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

public final class SummonCommand {
    public static LiteralCommandNode<CommandSource> create() {
        return literal("summon")
                .requires(source -> source.sender().hasPermission("fidorial.command.summon"))
                .then(argument("entity", ArgumentTypes.serverResource(RegistryKey.ENTITY_TYPE))
                        .executes(SummonCommand::executeSelf)
                        .then(argument("position", ArgumentTypes.position())
                                .executes(SummonCommand::executeCoordinates))).build();
    }

    private static int executeSelf(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        final Location location = source.location();
        if (location == null) {
            source.sendMessage(Component.translatable("command.summon.no_world"));
            return 0;
        }
        return summon(
                context,
                worldOf(context),
                location);
    }

    private static int executeCoordinates(final CommandContext<CommandSource> context) {
        final Location location = context.getArgument("position", PositionResolver.class).resolve(context.getSource());

        final ServerWorld world = worldOf(context);

        return summon(context, world, location);
    }

    private static int summon(final CommandContext<CommandSource> context, final @Nullable ServerWorld world, final Location location) {
        final EntityType entity = context.getArgument("entity", EntityType.class);

        if (!MobFactories.isMob(entity)) {
            context.getSource()
                    .sendMessage(Component.translatable(
                            "command.summon.notmob", Component.text(entity.key().asString())));
            return 0;
        }

        final FidorialServer server = FidorialServer.getInstance();
        final CommandSource source = context.getSource();
        if (world == null) {
            source.sendMessage(Component.translatable("command.summon.no_world"));
            return 0;
        }
        world.scheduler().execute(world.key(), location.chunk(), () -> {
            final AbstractMob mob = MobFactories.create(entity, server.entityIds().allocate(), location);
            server.spawnEntity(mob);

            source.sendMessage(Component.translatable(
                    "command.summon.done", Component.text(entity.key().value()), Component.text(mob.entityId())));
        });

        return Command.SINGLE_SUCCESS;
    }

    private static @Nullable ServerWorld worldOf(final CommandContext<CommandSource> context) {
        final CommandSource source = context.getSource();
        return source.location() instanceof final Location location
                && location.world() instanceof final ServerWorld world ? world : null;
    }
}
