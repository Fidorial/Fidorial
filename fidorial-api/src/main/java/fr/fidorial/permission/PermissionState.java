package fr.fidorial.permission;

import com.google.common.base.Preconditions;
import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.util.TriState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * The permission engine of one holder: its grants, resolvers, defaults and cache.
 *
 * @since 0.1.0
 */
public final class PermissionState {

    private final PermissionHolder owner;
    private final PermissionRegistry registry;
    private final Supplier<List<PermissionResolver>> resolvers;

    private final List<ActiveGrant> grants = new CopyOnWriteArrayList<>();
    private final Map<PermissionNode, TriState> cache = new ConcurrentHashMap<>();
    private final AtomicLong seenRevision = new AtomicLong(Long.MIN_VALUE);

    /**
     * Creates an engine with no external resolvers.
     *
     * @param owner    the holder this engine answers for
     * @param registry the permission registry
     */
    public PermissionState(final PermissionHolder owner, final PermissionRegistry registry) {
        this(owner, registry, List::of);
    }

    /**
     * Creates an engine.
     *
     * @param owner     the holder this engine answers for
     * @param registry  the permission registry
     * @param resolvers supplier of the currently registered resolvers; called on every uncached
     *                  lookup so plugins registering late are picked up without a restart
     */
    public PermissionState(
            final PermissionHolder owner,
            final PermissionRegistry registry,
            final Supplier<List<PermissionResolver>> resolvers
    ) {
        Preconditions.checkArgument(owner != null, "The owner of a permission state must not be null");
        Preconditions.checkArgument(registry != null, "The registry of a permission state must not be null");
        Preconditions.checkArgument(resolvers != null, "The resolvers of a permission state must not be null");
        this.owner = owner;
        this.registry = registry;
        this.resolvers = resolvers;
    }

    /**
     * Resolves a node.
     *
     * @param node the node
     * @return the resolved state
     */
    public TriState resolve(final PermissionNode node) {
        Preconditions.checkArgument(node != null, "The node passed to PermissionState.resolve() must not be null");
        final long revision = registry.revision();
        if (seenRevision.getAndSet(revision) != revision) {
            cache.clear();
        }
        final TriState cached = cache.get(node);
        if (cached != null) {
            return cached;
        }
        final Resolution resolution = compute(node);
        if (resolution.cacheable()) {
            cache.put(node, resolution.state());
        }
        return resolution.state();
    }

    private Resolution compute(final PermissionNode node) {
        final List<PermissionResolver> chain = new ArrayList<>(resolvers.get());
        chain.sort(Comparator.comparingInt(PermissionResolver::weight).reversed());
        for (final PermissionResolver resolver : chain) {
            final TriState state = resolver.resolve(owner, node);
            if (state != TriState.NOT_SET) {
                return new Resolution(state, resolver.cacheable());
            }
        }

        final boolean operator = owner.isOperator();
        for (final PermissionNode candidate : node.lookupChain()) {
            final TriState granted = fromGrants(candidate);
            if (granted != TriState.NOT_SET) {
                return new Resolution(granted, true);
            }
            final TriState declared = registry.definition(candidate)
                    .map(definition -> definition.defaultFor(operator))
                    .orElse(TriState.NOT_SET);
            if (declared != TriState.NOT_SET) {
                return new Resolution(declared, true);
            }
        }
        return new Resolution(TriState.NOT_SET, true);
    }

    private TriState fromGrants(final PermissionNode node) {
        TriState result = TriState.NOT_SET;
        for (final ActiveGrant grant : grants) {
            final TriState state = grant.overrides.get(node);
            if (state != null && state != TriState.NOT_SET) {
                result = state; // later grants win over earlier ones
            }
        }
        return result;
    }

    /**
     * Opens a new grant on this engine.
     *
     * @param plugin the plugin requesting it
     * @return the grant
     */
    public PermissionGrant newGrant(final Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "The plugin passed to PermissionState.newGrant() must not be null");
        final ActiveGrant grant = new ActiveGrant(plugin);
        grants.add(grant);
        invalidate();
        return grant;
    }

    /**
     * @return every override applied by the active grants, later grants overriding earlier ones
     */
    public Map<PermissionNode, TriState> activeOverrides() {
        final Map<PermissionNode, TriState> merged = new LinkedHashMap<>();
        for (final ActiveGrant grant : grants) {
            merged.putAll(grant.overrides);
        }
        return Map.copyOf(merged);
    }

    /**
     * Revokes every grant opened by a plugin, typically when it is disabled.
     */
    public void revokeGrantsOf(final Plugin plugin) {
        for (final ActiveGrant grant : grants) {
            if (grant.owner.equals(plugin)) {
                grant.revoke();
            }
        }
    }

    /**
     * Revokes every grant on this engine.
     */
    public void revokeAll() {
        for (final ActiveGrant grant : grants) {
            grant.revoke();
        }
    }

    /**
     * Drops the memoised results.
     */
    public void invalidate() {
        cache.clear();
    }

    private record Resolution(TriState state, boolean cacheable) {
    }

    private final class ActiveGrant implements PermissionGrant {

        private final Plugin owner;
        private final Map<PermissionNode, TriState> overrides = new ConcurrentHashMap<>();
        private volatile boolean revoked;

        private ActiveGrant(final Plugin owner) {
            this.owner = owner;
        }

        @Override
        public Plugin owner() {
            return owner;
        }

        @Override
        public PermissionHolder holder() {
            return PermissionState.this.owner;
        }

        @Override
        public PermissionGrant set(final PermissionNode node, final TriState state) {
            Preconditions.checkArgument(node != null, "The node passed to PermissionState.ActiveGrant.set() must not be null");
            Preconditions.checkArgument(state != null, "The state passed to PermissionState.ActiveGrant.set() must not be null");
            if (revoked) {
                throw new IllegalStateException("This grant has been revoked");
            }
            if (state != TriState.NOT_SET) {
                overrides.put(node, state);
            } else {
                overrides.remove(node);
            }
            invalidate();
            return this;
        }

        @Override
        public Map<PermissionNode, TriState> overrides() {
            return Map.copyOf(overrides);
        }

        @Override
        public boolean revoked() {
            return revoked;
        }

        @Override
        public void revoke() {
            if (revoked) {
                return;
            }
            revoked = true;
            overrides.clear();
            grants.remove(this);
            invalidate();
        }
    }
}
