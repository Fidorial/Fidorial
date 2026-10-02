package fr.fidorial.permission;

import com.google.common.base.Preconditions;
import net.kyori.adventure.util.TriState;


/**
 * The declaration of a permission node and the values it takes by default.
 *
 * @param node            the node
 * @param description     what the permission allows
 * @param regularDefault  the default value for regular players
 * @param operatorDefault the default value for operators
 * @since 0.1.0
 */
public record PermissionDefinition(
        PermissionNode node,
        String description,
        TriState regularDefault,
        TriState operatorDefault
) {

    /**
     * Validates the components.
     */
    public PermissionDefinition {
        Preconditions.checkArgument(node != null, "The node of a permission definition must not be null");
        Preconditions.checkArgument(regularDefault != null, "The regular default of a permission definition must not be null");
        Preconditions.checkArgument(operatorDefault != null, "The operator default of a permission definition must not be null");
    }

    /**
     * Declares a permission only operators hold by default.
     *
     * @param node        node path
     * @param description description
     * @return the definition
     */
    public static PermissionDefinition operatorOnly(final String node, final String description) {
        return new PermissionDefinition(PermissionNode.of(node), description, TriState.NOT_SET, TriState.TRUE);
    }

    /**
     * Declares a permission everyone holds by default.
     *
     * @param node        node path
     * @param description description
     * @return the definition
     */
    public static PermissionDefinition everyone(final String node, final String description) {
        return new PermissionDefinition(PermissionNode.of(node), description, TriState.TRUE, TriState.TRUE);
    }

    /**
     * Declares a permission nobody holds by default, operators included. It has to be granted
     * explicitly through a {@link PermissionGrant} or an external {@link PermissionResolver}.
     *
     * @param node        node path
     * @param description description
     * @return the definition
     */
    public static PermissionDefinition explicitOnly(final String node, final String description) {
        return new PermissionDefinition(PermissionNode.of(node), description, TriState.NOT_SET, TriState.NOT_SET);
    }

    /**
     * Declares a permission that is actively refused unless a grant overrides it.
     *
     * @param node        node path
     * @param description description
     * @return the definition
     */
    public static PermissionDefinition refused(final String node, final String description) {
        return new PermissionDefinition(PermissionNode.of(node), description, TriState.FALSE, TriState.FALSE);
    }

    /**
     * Returns the default state for a holder.
     *
     * @param operator whether the holder is an operator
     * @return the applicable default
     */
    public TriState defaultFor(final boolean operator) {
        return operator ? operatorDefault : regularDefault;
    }
}
