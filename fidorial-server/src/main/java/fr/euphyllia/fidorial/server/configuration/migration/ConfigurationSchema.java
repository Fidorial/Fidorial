package fr.euphyllia.fidorial.server.configuration.migration;

import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.Objects;

/**
 * One step in the schema history of a configuration file: the transformation that upgrades a file
 * to {@link #version()} from the version before it.
 *
 * @param version        the version a file has after this schema is applied
 * @param description    what this schema changes
 * @param transformation the node transformation performing the upgrade
 */
public record ConfigurationSchema(int version, String description, ConfigurationTransformation transformation) {

    public ConfigurationSchema {
        if (version < 1) {
            throw new IllegalArgumentException("Schema versions start at 1, got " + version);
        }
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(transformation, "transformation");
    }

    public static Builder builder(final int version, final String description) {
        return new Builder(version, description);
    }

    public static final class Builder {

        private final int version;
        private final String description;
        private final ConfigurationTransformation.Builder transformation = ConfigurationTransformation.builder();

        private Builder(final int version, final String description) {
            this.version = version;
            this.description = description;
        }

        /**
         * Moves the node at {@code from}, with its children and comments, to {@code to}.
         */
        public Builder move(final NodePath from, final NodePath to) {
            final Object[] target = to.array();
            transformation.addAction(from, (path, value) -> target);
            return this;
        }

        /**
         * Renames the last element of {@code path}, keeping the node under the same parent.
         */
        public Builder rename(final NodePath path, final String newName) {
            transformation.addAction(path, TransformAction.rename(newName));
            return this;
        }

        /**
         * Removes the node at {@code path}.
         */
        public Builder remove(final NodePath path) {
            transformation.addAction(path, TransformAction.remove());
            return this;
        }

        /**
         * Escape hatch for changes the helpers above do not cover, such as rewriting a value.
         */
        public Builder action(final NodePath path, final TransformAction action) {
            transformation.addAction(path, action);
            return this;
        }

        public ConfigurationSchema build() {
            return new ConfigurationSchema(version, description, transformation.build());
        }
    }
}
