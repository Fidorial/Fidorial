package fr.euphyllia.fidorial.bootstrap;

import org.jspecify.annotations.Nullable;

import java.io.Serial;

public final class BootstrapException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BootstrapException(final String message) {
        super(message);
    }

    public BootstrapException(final String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
