package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntityAnchorArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.AngleArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.RotationArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.EntitiesResolver;
import fr.fidorial.command.argument.resolvers.EntityResolver;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * The vanilla {@code /teleport} command, also registered as {@code /tp}.
 *
 * <pre>
 * /teleport &lt;location&gt;
 * /teleport &lt;destination&gt;
 * /teleport &lt;targets&gt; &lt;location&gt; [&lt;rotation&gt;]
 * /teleport &lt;targets&gt; &lt;location&gt; facing &lt;facingLocation&gt;
 * /teleport &lt;targets&gt; &lt;location&gt; facing entity &lt;facingEntity&gt; [eyes|feet]
 * /teleport &lt;targets&gt; &lt;destination&gt;
 * </pre>
 *
 * <p>As in vanilla, a location is taken in the world of the command source, while teleporting to an entity follows
 * it into its world: {@code /execute in <dimension> run teleport ...} moves to coordinates in another world.</p>
 */
public final class TeleportCommand {

    private static final String PERMISSION = "fidorial.command.teleport";

    /**
     * The vanilla limits of the positions entities may be teleported to.
     */
    private static final int HORIZONTAL_LIMIT = 30_000_000;
    private static final int VERTICAL_LIMIT = 20_000_000;

    /**
     * How high the eyes of an entity are, relative to its height, unless the entity says otherwise.
     */
    private static final float DEFAULT_EYE_HEIGHT = 0.85F;

    private static final SimpleCommandExceptionType INVALID_POSITION =
            ExceptionFactory.simple("commands.teleport.invalidPosition");
    private static final SimpleCommandExceptionType NOT_AN_ENTITY =
            ExceptionFactory.simple("permissions.requires.entity");

    private TeleportCommand() {
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("teleport")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(argument("location", arguments().position())
                        .executes(context -> toLocation(context, List.of(self(context)), null, null)))
                .then(argument("destination", arguments().entity())
                        .executes(context -> toEntity(context, List.of(self(context)),
                                EntityResolver.entity(context, "destination"))))
                .then(argument("targets", arguments().entities())
                        .then(argument("location", arguments().position())
                                .executes(context -> toLocation(context, targets(context), null, null))
                                .then(argument("rotation", RotationArgument.rotation())
                                        .executes(context -> toLocation(context, targets(context),
                                                context.getArgument("rotation", RotationArgument.Rotation.class), null)))
                                .then(literal("facing")
                                        .then(literal("entity")
                                                .then(argument("facingEntity", arguments().entity())
                                                        .executes(context -> toLocation(context, targets(context), null,
                                                                facingEntity(context, EntityAnchorArgument.Anchor.FEET)))
                                                        .then(argument("facingAnchor", EntityAnchorArgument.anchor())
                                                                .executes(context -> toLocation(context, targets(context),
                                                                        null, facingEntity(context, context.getArgument(
                                                                                "facingAnchor",
                                                                                EntityAnchorArgument.Anchor.class)))))))
                                        .then(argument("facingLocation", arguments().position())
                                                .executes(context -> toLocation(context, targets(context), null,
                                                        PositionResolver.position(context, "facingLocation"))))))
                        .then(argument("destination", arguments().entity())
                                .executes(context -> toEntity(context, targets(context),
                                        EntityResolver.entity(context, "destination")))))
                .build();
    }

    private static Entity self(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final Entity executor = context.getSource().executor();
        if (executor == null) {
            throw NOT_AN_ENTITY.create();
        }
        return executor;
    }

    private static List<Entity> targets(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return EntitiesResolver.entities(context, "targets");
    }

    private static Location facingEntity(final CommandContext<CommandSource> context, final EntityAnchorArgument.Anchor anchor)
            throws CommandSyntaxException {
        final Entity entity = EntityResolver.entity(context, "facingEntity");
        final Location at = entity.location();
        return anchor == EntityAnchorArgument.Anchor.EYES ? at.offsetY(eyeHeight(entity)) : at;
    }

    private static double eyeHeight(final Entity entity) {
        if (entity instanceof final ServerPlayer player) {
            return player.eyeHeight();
        }
        return entity.type().height() * DEFAULT_EYE_HEIGHT;
    }

    private static int toEntity(final CommandContext<CommandSource> context, final List<Entity> targets, final Entity destination) {
        for (final Entity target : targets) {
            target.teleport(destination);
        }

        final CommandSource source = context.getSource();
        if (targets.size() == 1) {
            source.sendMessage(Component.translatable("commands.teleport.success.entity.single",
                    targets.getFirst().displayName(), destination.displayName()));
        } else {
            source.sendMessage(Component.translatable("commands.teleport.success.entity.multiple",
                    Component.text(targets.size()), destination.displayName()));
        }
        return targets.size();
    }

    private static int toLocation(
            final CommandContext<CommandSource> context,
            final List<Entity> targets,
            final RotationArgument.@Nullable Rotation rotation,
            final @Nullable Location facing) throws CommandSyntaxException {
        final CommandSource source = context.getSource();
        final Location destination = PositionResolver.position(context, "location");
        if (!isInSpawnableBounds(destination)) {
            throw INVALID_POSITION.create();
        }
        final float yaw = rotation == null ? 0.0F : AngleArgument.wrapDegrees(rotation.yaw(source));
        final float pitch = rotation == null ? 0.0F : AngleArgument.wrapDegrees(rotation.pitch(source));

        for (final Entity target : targets) {
            final Location current = target.location();
            Location arrival;
            if (rotation != null) {
                arrival = destination.with(destination.x(), destination.y(), destination.z(), yaw, pitch);
            } else {
                arrival = destination.with(destination.x(), destination.y(), destination.z(), current.yaw(), current.pitch());
            }
            if (facing != null) {
                arrival = lookingAt(arrival, facing);
            }
            target.teleport(arrival);
        }

        final Component x = coordinate(destination.x());
        final Component y = coordinate(destination.y());
        final Component z = coordinate(destination.z());
        if (targets.size() == 1) {
            source.sendMessage(Component.translatable("commands.teleport.success.location.single",
                    targets.getFirst().displayName(), x, y, z));
        } else {
            source.sendMessage(Component.translatable("commands.teleport.success.location.multiple",
                    Component.text(targets.size()), x, y, z));
        }
        return targets.size();
    }

    /**
     * {@return the location turned so that its feet look at a point}
     */
    private static Location lookingAt(final Location from, final Location point) {
        final double dx = point.x() - from.x();
        final double dy = point.y() - from.y();
        final double dz = point.z() - from.z();
        final double horizontal = Math.sqrt(dx * dx + dz * dz);
        final float pitch = AngleArgument.wrapDegrees((float) -Math.toDegrees(Math.atan2(dy, horizontal)));
        final float yaw = AngleArgument.wrapDegrees((float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
        return from.with(from.x(), from.y(), from.z(), yaw, pitch);
    }

    private static boolean isInSpawnableBounds(final Location location) {
        final int x = (int) Math.floor(location.x());
        final int y = (int) Math.floor(location.y());
        final int z = (int) Math.floor(location.z());
        return y >= -VERTICAL_LIMIT && y < VERTICAL_LIMIT
                && x >= -HORIZONTAL_LIMIT && z >= -HORIZONTAL_LIMIT && x < HORIZONTAL_LIMIT && z < HORIZONTAL_LIMIT;
    }

    private static Component coordinate(final double value) {
        return Component.text(String.format(Locale.ROOT, "%f", value));
    }
}
