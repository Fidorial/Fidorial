package fr.euphyllia.fidorial.server.configuration.exception;

import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class InvalidConfigurationException extends IOException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Path file;
    private final List<String> problems;

    public InvalidConfigurationException(final Path file, final List<String> problems) {
        super("Failed to load " + file + ":" + problems.stream().map(problem -> "\n - " + problem).collect(Collectors.joining()));
        this.file = file;
        this.problems = List.copyOf(problems);
    }

    public Path file() {
        return file;
    }

    public List<String> problems() {
        return problems;
    }
}
