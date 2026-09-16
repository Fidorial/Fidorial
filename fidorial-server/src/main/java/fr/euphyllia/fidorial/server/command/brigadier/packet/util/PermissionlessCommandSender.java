package fr.euphyllia.fidorial.server.command.brigadier.packet.util;

import fr.fidorial.command.CommandSender;
import fr.fidorial.permission.PermissionGrant;
import fr.fidorial.permission.PermissionNode;
import fr.fidorial.plugin.Plugin;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.util.TriState;

import java.util.Map;

/**
 * A sender that holds nothing, used when building the command tree sent to a client before its
 * permissions are known.
 * <p>
 * Every node resolves to {@link TriState#FALSE} rather than {@link TriState#NOT_SET}: the point is
 * to produce a definitive "no" that no resolver can override, not to defer the question.
 */
public final class PermissionlessCommandSender implements CommandSender {

    static final PermissionlessCommandSender INSTANCE = new PermissionlessCommandSender();
    private static final PointersSupplier<PermissionlessCommandSender> pointers = PointersSupplier.<PermissionlessCommandSender>builder()
            .resolving(Identity.NAME, PermissionlessCommandSender::name)
            .resolving(PermissionChecker.POINTER, sender -> sender::permissionState)
            .build();

    private PermissionlessCommandSender() {
    }

    @Override
    public TriState permissionState(final PermissionNode node) {
        return TriState.FALSE;
    }

    @Override
    public PermissionGrant newGrant(final Plugin owner) {
        throw new UnsupportedOperationException("Cannot grant permissions to the permissionless sender");
    }

    @Override
    public Map<PermissionNode, TriState> activeOverrides() {
        return Map.of();
    }

    @Override
    public void invalidatePermissions() {
        // nothing is cached
    }

    @Override
    public String name() {
        return "Permissionless";
    }

    @Override
    public Pointers pointers() {
        return pointers.view(this);
    }

    @Override
    public boolean isOperator() {
        return false;
    }

    @Override
    public void setOperator(final boolean operator) {
        throw new UnsupportedOperationException("Cannot op the permissionless sender");
    }
}
