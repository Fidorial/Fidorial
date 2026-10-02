package fr.fidorial.permission;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A permission node, such as {@code fidorial.command.gamemode}, normalized to lower case.
 *
 * <p>A {@value #WILDCARD} segment matches any segment at its position.</p>
 *
 * @param path the dotted path of the node
 * @since 0.1.0
 */
public record PermissionNode(String path) implements Comparable<PermissionNode> {

    /**
     * The node matching every permission.
     */
    public static final PermissionNode ROOT = new PermissionNode("*");

    /**
     * Wildcard segment.
     */
    public static final String WILDCARD = "*";

    /**
     * Normalizes the path to lower case and validates it.
     *
     * @throws IllegalArgumentException if the path is empty or malformed
     */
    public PermissionNode {
        Preconditions.checkArgument(path != null, "The path of a permission node must not be null");
        path = path.trim().toLowerCase(Locale.ROOT);
        if (path.isEmpty()) {
            throw new IllegalArgumentException("A permission node cannot be empty");
        }
        if (path.startsWith(".") || path.endsWith(".") || path.contains("..")) {
            throw new IllegalArgumentException("Malformed permission node: " + path);
        }
        for (int i = 0; i < path.length(); i++) {
            final char c = path.charAt(i);
            final boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-'
                    || c == '*';
            if (!ok) {
                throw new IllegalArgumentException("Illegal character '" + c + "' in permission node: " + path);
            }
        }
    }

    /**
     * Creates a node from its textual path.
     *
     * @param path dot-separated path
     * @return the node
     * @throws IllegalArgumentException if the path is malformed
     */
    public static PermissionNode of(final String path) {
        return new PermissionNode(path);
    }

    /**
     * @return the individual segments of this node
     */
    public List<String> segments() {
        return List.of(path.split("\\."));
    }

    /**
     * @return {@code true} when the last segment is a wildcard
     */
    public boolean isWildcard() {
        return path.equals(WILDCARD) || path.endsWith("." + WILDCARD);
    }

    /**
     * Builds the lookup chain for this node, ordered from most specific to least specific.
     *
     * <p>{@code a.b.c} yields {@code a.b.c}, {@code a.b.*}, {@code a.*} and finally {@code *}. The
     * resolver walks this list and stops on the first decided state, which is what gives more
     * specific rules priority over broader ones.
     *
     * @return the ordered lookup chain, always at least one element long
     */
    public List<PermissionNode> lookupChain() {
        final List<String> segments = segments();
        final List<PermissionNode> chain = new ArrayList<>(segments.size() + 1);
        chain.add(this);
        for (int cut = segments.size() - 1; cut >= 1; cut--) {
            chain.add(new PermissionNode(String.join(".", segments.subList(0, cut)) + "." + WILDCARD));
        }
        chain.add(ROOT);
        return List.copyOf(chain);
    }

    /**
     * Tests whether this node covers {@code other}, taking wildcards into account.
     *
     * @param other node being tested
     * @return {@code true} if a rule on this node applies to {@code other}
     */
    public boolean covers(final PermissionNode other) {
        Preconditions.checkArgument(other != null, "The node to compare passed to PermissionNode.covers() must not be null");
        if (this.equals(other)) {
            return true;
        }
        if (!isWildcard()) {
            return false;
        }
        if (this.equals(ROOT)) {
            return true;
        }
        final String prefix = path.substring(0, path.length() - WILDCARD.length());
        return other.path.startsWith(prefix);
    }

    /**
     * Appends a child segment.
     *
     * @param segment segment to append
     * @return the child node
     */
    public PermissionNode child(final String segment) {
        return new PermissionNode(path + "." + segment);
    }

    @Override
    public int compareTo(final PermissionNode other) {
        return path.compareTo(other.path);
    }

    @Override
    public String toString() {
        return path;
    }
}
