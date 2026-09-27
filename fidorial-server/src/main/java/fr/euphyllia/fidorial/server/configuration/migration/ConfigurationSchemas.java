package fr.euphyllia.fidorial.server.configuration.migration;

import fr.euphyllia.fidorial.server.configuration.migration.schemas.LegacyToV1Schema;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;

import java.util.List;
import java.util.Objects;

/**
 * The migration schema history.
 */
public final class ConfigurationSchemas {

    public static final ConfigurationSchemas SERVER = new ConfigurationSchemas(
            "config-version",
            LegacyToV1Schema.VERSION,
            List.of(
                    // V2 and later go here
            ));

    private final String versionKey;
    private final int firstVersion;
    private final List<ConfigurationSchema> schemas;

    public ConfigurationSchemas(final String versionKey, final int firstVersion, final List<ConfigurationSchema> schemas) {
        this.versionKey = Objects.requireNonNull(versionKey, "versionKey");
        this.firstVersion = firstVersion;
        this.schemas = List.copyOf(schemas);
        for (int i = 0; i < this.schemas.size(); i++) {
            final ConfigurationSchema schema = this.schemas.get(i);
            final int expected = firstVersion + 1 + i;
            if (schema.version() != expected) {
                throw new IllegalArgumentException("Expected schema version " + expected + " at position " + i
                        + ", got " + schema.version() + " (" + schema.description() + ")");
            }
        }
    }

    public String versionKey() {
        return versionKey;
    }

    public int latestVersion() {
        return schemas.isEmpty() ? firstVersion : schemas.getLast().version();
    }

    /**
     * Brings {@code root} up to {@link #latestVersion()}. An empty node is a new file and is left as is;
     * it is written at the latest version when saved.
     *
     * @throws ConfigurateException if the file has no version key, or a version newer than this build knows
     */
    public void upgrade(final CommentedConfigurationNode root) throws ConfigurateException {
        if (root.empty()) {
            return;
        }
        final CommentedConfigurationNode versionNode = root.node(versionKey);
        if (versionNode.virtual()) {
            throw new ConfigurateException(root, "Missing '" + versionKey + "': this file was not written by a known "
                    + "Fidorial version. Delete it to regenerate the defaults.");
        }
        final int current = versionNode.getInt();
        if (current > latestVersion()) {
            throw new ConfigurateException(root, "File version " + current + " is newer than this build supports ("
                    + latestVersion() + "); it was written by a newer version of Fidorial.");
        }
        for (final ConfigurationSchema schema : schemas) {
            if (schema.version() > current) {
                schema.transformation().apply(root);
            }
        }
        versionNode.raw(latestVersion());
    }

    /**
     * Marks {@code root} as being at the latest version.
     */
    public void stamp(final CommentedConfigurationNode root) {
        final CommentedConfigurationNode version = root.node(versionKey);
        version.raw(latestVersion());
        version.comment("Used to upgrade this file between Fidorial versions. Do not edit.");
    }
}
