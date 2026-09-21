package fr.euphyllia.fidorial.server.command.brigadier.argument.entity;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.DoubleRange;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.EntitySelectorParser;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class EntitySelector {

    public enum SortType {
        ARBITRARY, NEAREST, FURTHEST, RANDOM
    }

    private final int maxResults;
    private boolean includesEntities;
    private final boolean selfSelector;
    private final boolean usesSelector;

    private final List<Predicate<Entity>> predicates;

    private final @Nullable Double x;
    private final @Nullable Double y;
    private final @Nullable Double z;

    private final @Nullable DoubleRange distance;

    private final @Nullable Double dx;
    private final @Nullable Double dy;
    private final @Nullable Double dz;

    private final SortType sort;

    private final @Nullable String targetName;
    private final @Nullable UUID targetUuid;

    public EntitySelector(
            final int maxResults,
            final boolean includesEntities,
            final boolean selfSelector,
            final boolean usesSelector,
            final List<Predicate<Entity>> predicates,
            final @Nullable Double x,
            final @Nullable Double y,
            final @Nullable Double z,
            final @Nullable DoubleRange distance,
            final @Nullable Double dx,
            final @Nullable Double dy,
            final @Nullable Double dz,
            final SortType sort,
            final @Nullable String targetName,
            final @Nullable UUID targetUuid
    ) {
        this.maxResults = maxResults;
        this.includesEntities = includesEntities;
        this.selfSelector = selfSelector;
        this.usesSelector = usesSelector;
        this.predicates = predicates;
        this.x = x;
        this.y = y;
        this.z = z;
        this.distance = distance;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.sort = sort;
        this.targetName = targetName;
        this.targetUuid = targetUuid;
    }

    public int maxResults() {
        return maxResults;
    }

    public boolean includesEntities() {
        return includesEntities;
    }

    public boolean selfSelector() {
        return selfSelector;
    }

    public boolean usesSelector() {
        return usesSelector;
    }

    public static EntitySelector parse(final String input) throws CommandSyntaxException {
        return new EntitySelectorParser(new StringReader(input)).parse();
    }

    public Entity findSingleEntity(final CommandSource source) throws CommandSyntaxException {
        checkPermissions(source);

        final Collection<? extends Entity> entities = findEntities(source);

        if (entities.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }
        if (entities.size() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
        }

        return entities.iterator().next();
    }

    public Player findSinglePlayer(final CommandSource source) throws CommandSyntaxException {
        checkPermissions(source);

        final List<Player> players = findPlayers(source);

        if (players.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        if (players.size() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        }

        return players.getFirst();
    }

    public Collection<? extends Entity> findEntities(final CommandSource source) throws CommandSyntaxException {
        checkPermissions(source);

        final FidorialServer server = (FidorialServer) source.server();
        final Collection<? extends Entity> entities;
        final Entity executor = source.executor();
        final ServerWorld defaultWorld = server.worldManager().defaultWorld().orElse(null);

        final Collection<? extends Entity> worldEntities = executor != null
                ? ((ServerWorld) executor.world()).entityManager().all()
                : defaultWorld != null ? defaultWorld.entityManager().all() : Collections.emptyList();

        if (targetUuid != null) {
            final Optional<? extends Entity> entity = worldEntities.stream()
                    .filter(e -> e.uuid().equals(targetUuid))
                    .findFirst();

            if (entity.isPresent()) {
                final Entity found = entity.get();
                includesEntities = !(found instanceof Player);
                entities = includesEntities ? List.of(found) : server.onlinePlayers();
            } else {
                includesEntities = false;
                entities = server.onlinePlayers();
            }

        } else if (targetName != null) {
            includesEntities = false;
            entities = server.onlinePlayers();

        } else if (selfSelector && source.sender() instanceof final Player player) {
            includesEntities = false;
            entities = List.of(player);

        } else if (includesEntities) {
            entities = worldEntities;
        } else {
            entities = server.onlinePlayers();
        }

        final List<Entity> result = entities.stream()
                .filter(it -> matches(it, source))
                .map(Entity.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));

        sort(result, source);

        return result.stream().limit(maxResults).toList();
    }

    public List<Player> findPlayers(final CommandSource source) throws CommandSyntaxException {
        final Collection<? extends Entity> entities = findEntities(source);
        final List<Player> players = new ArrayList<>();

        for (final Entity entity : entities) {
            if (!(entity instanceof final Player player)) {
                throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create();
            }
            players.add(player);
        }

        if (players.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }

        return players;
    }

    private boolean matches(final Entity entity, final CommandSource source) {
        if (targetUuid != null && !entity.uuid().equals(targetUuid)) {
            return false;
        }

        if (targetName != null) {
            if (!(entity instanceof final Player player)) return false;
            if (!player.name().equalsIgnoreCase(targetName)) return false;
        }

        for (final Predicate<Entity> predicate : predicates) {
            if (!predicate.test(entity)) return false;
        }

        if (distance != null) {
            final double distSqr = entity.location().distanceSquared(source.location());
            if (!distance.matchesSqr(distSqr)) return false;
        }

        if (dx != null || dy != null || dz != null) {
            final double originX = x != null ? x : source.location().x();
            final double originY = y != null ? y : source.location().y();
            final double originZ = z != null ? z : source.location().z();

            final double ex = entity.location().x();
            final double ey = entity.location().y();
            final double ez = entity.location().z();

            if (dx != null) {
                final double lo = Math.min(originX, originX + dx);
                final double hi = Math.max(originX, originX + dx);
                if (ex < lo || ex > hi) return false;
            }
            if (dy != null) {
                final double lo = Math.min(originY, originY + dy);
                final double hi = Math.max(originY, originY + dy);
                if (ey < lo || ey > hi) return false;
            }
            if (dz != null) {
                final double lo = Math.min(originZ, originZ + dz);
                final double hi = Math.max(originZ, originZ + dz);
                if (ez < lo || ez > hi) return false;
            }
        }

        return true;
    }

    private void sort(final List<? extends Entity> entities, final CommandSource source) {
        switch (sort) {
            case NEAREST -> entities.sort(Comparator.comparingDouble(e -> e.location().distanceSquared(source.location())));
            case FURTHEST -> entities.sort(
                    Comparator.comparingDouble((Entity e) -> e.location().distanceSquared(source.location())).reversed());
            case RANDOM -> Collections.shuffle(entities);
            case ARBITRARY -> { }
        }
    }

    private void checkPermissions(final CommandSource source) throws CommandSyntaxException {
        if (!usesSelector) return;
        if (!source.sender().hasPermission("minecraft.command.selector")) {
            throw EntityArgument.SELECTORS_NOT_PERMITTED.create();
        }
    }

    public EntitySelector withPredicate(final Predicate<Entity> extra) {
        final List<Predicate<Entity>> combined = new ArrayList<>(predicates.size() + 1);
        combined.addAll(predicates);
        combined.add(extra);
        return new EntitySelector(maxResults, includesEntities, selfSelector, usesSelector, combined,
                x, y, z, distance, dx, dy, dz, sort, targetName, targetUuid);
    }
}
