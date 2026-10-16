package fr.euphyllia.fidorial.server.command.brigadier.argument.entity;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.range.RangeBounds;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.EntitySelectorParser;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A parsed selector, name or UUID. Immutable: one instance is resolved once per source.
 *
 * <p>{@code x}, {@code y} and {@code z} override the origin's coordinates; {@code dx}, {@code dy} and
 * {@code dz} span the volume from it.</p>
 */
public record EntitySelector(
        int maxResults,
        boolean includesEntities,
        boolean selfSelector,
        boolean usesSelector,
        List<Predicate<Entity>> predicates,
        @Nullable Double x,
        @Nullable Double y,
        @Nullable Double z,
        RangeBounds.@Nullable Doubles distance,
        @Nullable Double dx,
        @Nullable Double dy,
        @Nullable Double dz,
        SortType sort,
        @Nullable String targetName,
        @Nullable UUID targetUuid
) {

    public enum SortType {
        ARBITRARY, NEAREST, FURTHEST, RANDOM
    }

    public EntitySelector {
        predicates = List.copyOf(predicates);
    }

    public static EntitySelector parse(final String input) throws CommandSyntaxException {
        return new EntitySelectorParser(new StringReader(input)).parse();
    }

    public EntitySelector withPredicate(final Predicate<Entity> extra) {
        final List<Predicate<Entity>> combined = new ArrayList<>(predicates);
        combined.add(extra);
        return new EntitySelector(maxResults, includesEntities, selfSelector, usesSelector, combined,
                x, y, z, distance, dx, dy, dz, sort, targetName, targetUuid);
    }

    /**
     * {@return the matched entities, possibly none}
     */
    public List<Entity> findEntities(final CommandSource source) throws CommandSyntaxException {
        if (usesSelector && !source.sender().hasPermission("minecraft.command.selector")) {
            throw EntityArgument.SELECTORS_NOT_PERMITTED.create();
        }

        final Location origin = origin(source);
        final List<Entity> found = new ArrayList<>();
        for (final Entity entity : candidates(source)) {
            if (matches(entity, origin)) {
                found.add(entity);
            }
        }
        sort(found, origin);
        return List.copyOf(found.subList(0, Math.min(maxResults, found.size())));
    }

    /**
     * {@return the matched players, possibly none}
     */
    public List<Player> findPlayers(final CommandSource source) throws CommandSyntaxException {
        return findEntities(source).stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .toList();
    }

    public Entity findSingleEntity(final CommandSource source) throws CommandSyntaxException {
        final List<Entity> entities = findEntities(source);
        if (entities.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }
        if (entities.size() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
        }
        return entities.getFirst();
    }

    public Player findSinglePlayer(final CommandSource source) throws CommandSyntaxException {
        final List<Player> players = findPlayers(source);
        if (players.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        if (players.size() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        }
        return players.getFirst();
    }

    private Collection<? extends Entity> candidates(final CommandSource source) {
        final FidorialServer server = (FidorialServer) source.server();

        if (selfSelector) {
            final Entity executor = source.executor();
            return executor == null ? List.of() : List.of(executor);
        }
        if (targetUuid != null) {
            return Stream.concat(server.players().online().stream(), worldEntities(source).stream())
                    .filter(entity -> entity.uuid().equals(targetUuid))
                    .limit(1)
                    .toList();
        }
        return includesEntities ? worldEntities(source) : server.players().online();
    }

    private static Collection<? extends Entity> worldEntities(final CommandSource source) {
        return source.location() instanceof final Location location && location.world() instanceof final ServerWorld world
                ? world.entityManager().all()
                : List.of();
    }

    private @Nullable Location origin(final CommandSource source) {
        final Location location = source.location();
        if (location == null || (x == null && y == null && z == null)) {
            return location;
        }
        return location.with(x != null ? x : location.x(), y != null ? y : location.y(), z != null ? z : location.z());
    }

    private boolean matches(final Entity entity, final @Nullable Location origin) {
        if (targetName != null && !(entity instanceof final Player player && player.name().equalsIgnoreCase(targetName))) {
            return false;
        }
        for (final Predicate<Entity> predicate : predicates) {
            if (!predicate.test(entity)) {
                return false;
            }
        }
        if (distance == null && dx == null && dy == null && dz == null) {
            return true;
        }

        if (origin == null || !entity.world().equals(origin.world())) {
            return false;
        }
        final Location at = entity.location();
        if (distance != null && !distance.matchesSqr(squaredDistance(at, origin))) {
            return false;
        }
        return within(at.x(), origin.x(), dx) && within(at.y(), origin.y(), dy) && within(at.z(), origin.z(), dz);
    }

    private static boolean within(final double value, final double origin, final @Nullable Double delta) {
        if (delta == null) {
            return true;
        }
        return value >= Math.min(origin, origin + delta) && value <= Math.max(origin, origin + delta);
    }

    private void sort(final List<Entity> entities, final @Nullable Location origin) {
        switch (sort) {
            case ARBITRARY -> { }
            case RANDOM -> Collections.shuffle(entities);
            case NEAREST, FURTHEST -> {
                if (origin == null) {
                    return;
                }
                final Comparator<Entity> nearest = Comparator.comparingDouble(entity -> squaredDistance(entity.location(), origin));
                entities.sort(sort == SortType.NEAREST ? nearest : nearest.reversed());
            }
        }
    }

    // ignores worlds unlike Location#distanceSquared
    private static double squaredDistance(final Position a, final Position b) {
        return a.distanceSquared(b);
    }
}
