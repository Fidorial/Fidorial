package fr.euphyllia.fidorial.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Artifact(String id, String path, String sha256, long size) {

    private static final String BUNDLED_PREFIX = ".fidorial/";

    public Artifact {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(sha256, "sha256");
        if (sha256.length() != 64) {
            throw new IllegalArgumentException("not a sha-256 digest: " + sha256);
        }
    }

    public static List<Artifact> read(final InputStream in, final boolean maven) throws IOException {
        final List<Artifact> artifacts = new ArrayList<>();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            int number = 0;
            while ((line = reader.readLine()) != null) {
                number++;
                final String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
                    continue;
                }
                final String[] fields = trimmed.split("\\s+", -1);
                if (fields.length != 3) {
                    throw new BootstrapException("malformed library list at line " + number + ": " + trimmed);
                }
                final long size;
                try {
                    size = Long.parseLong(fields[2]);
                } catch (final NumberFormatException e) {
                    throw new BootstrapException("malformed size at line " + number + ": " + fields[2]);
                }
                artifacts.add(maven
                        ? external(fields[0], fields[1], size)
                        : bundled(fields[0], fields[1], size));
            }
        }
        return Collections.unmodifiableList(artifacts);
    }

    /**
     * A unique snapshot version: {@code 1.0-20260208.124123-2}. Maven keeps these in the
     * {@code 1.0-SNAPSHOT} directory while the file name carries the timestamp, so one
     * cannot be derived from the other naively.
     */
    private static final Pattern UNIQUE_SNAPSHOT = Pattern.compile("^(.*)-\\d{8}\\.\\d{6}-\\d+$");

    public static Artifact external(final String coordinates, final String sha256, final long size) {
        final String[] parts = coordinates.split(":", -1);
        if (parts.length < 3 || parts.length > 4) {
            throw new BootstrapException("invalid coordinates: " + coordinates);
        }
        final String group = parts[0];
        final String artifact = parts[1];
        final String version = parts[2];
        final String classifier = parts.length == 4 ? parts[3] : null;
        final String fileName = classifier == null
                ? artifact + '-' + version + ".jar"
                : artifact + '-' + version + '-' + classifier + ".jar";
        final Matcher unique = UNIQUE_SNAPSHOT.matcher(version);
        final String directory = unique.matches() ? unique.group(1) + "-SNAPSHOT" : version;

        final String path = group.replace('.', '/') + '/' + artifact + '/' + directory + '/' + fileName;
        return new Artifact(coordinates, path, sha256, size);
    }

    public static Artifact bundled(final String fileName, final String sha256, final long size) {
        return new Artifact(fileName, BUNDLED_PREFIX + fileName, sha256, size);
    }

    public String fileName() {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
