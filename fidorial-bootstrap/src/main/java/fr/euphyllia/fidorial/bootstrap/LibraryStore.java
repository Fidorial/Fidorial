package fr.euphyllia.fidorial.bootstrap;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The shared library cache, laid out like a local Maven repository under {@code libraries/}.
 *
 * <p>The same instance backs the server's own dependencies and every plugin's declared
 * libraries, so two plugins asking for the same jar download it once and share the file.
 *
 * <p>Every entry carries a SHA-256 computed at build time. A file that does not match is
 * re-downloaded; a download that does not match is a hard failure, never a warning.
 */
public final class LibraryStore {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(5);
    private static final int ATTEMPTS_PER_REPOSITORY = 3;
    private static final String USER_AGENT = "Fidorial-Bootstrap";

    private final Path root;
    private final List<String> repositories;
    private final boolean offline;
    private final boolean verifyCached;
    private final boolean allowExtraRepositories;
    private final HttpClient http;
    private final Map<String, List<String>> servedByFallback = new ConcurrentHashMap<>();

    public LibraryStore(final Path root, final List<String> repositories) {
        this.root = root;
        this.repositories = List.copyOf(repositories);
        this.offline = Boolean.getBoolean("fidorial.libraries.offline");
        this.verifyCached = !"false".equalsIgnoreCase(System.getProperty("fidorial.libraries.verify"));
        this.allowExtraRepositories =
                !"false".equalsIgnoreCase(System.getProperty("fidorial.libraries.pluginRepositories"));
        this.http = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Path root() {
        return root;
    }

    public List<Path> resolve(final List<Artifact> artifacts, final String what) throws IOException {
        return resolve(artifacts, what, List.of());
    }

    /**
     * Same, with repositories appended after the server's own — used by plugins that pull a
     * library from somewhere the server does not mirror.
     *
     * <p>Extras come last on purpose: the mirror always gets first refusal, so a plugin
     * cannot quietly reroute an artifact the server already trusts. Integrity does not
     * depend on where a jar comes from anyway, since every entry is pinned to a SHA-256 at
     * build time; what an extra repository buys an attacker is a request to a host of their
     * choosing, which is why {@code -Dfidorial.libraries.pluginRepositories=false} turns the
     * whole mechanism off for administrators who need a closed network.
     */
    public List<Path> resolve(
            final List<Artifact> artifacts,
            final String what,
            final List<String> extraRepositories
    ) throws IOException {
        final List<Path> resolved = new ArrayList<>(artifacts.size());
        final Map<Artifact, Path> missing = new LinkedHashMap<>();

        for (final Artifact artifact : artifacts) {
            final Path target = root.resolve(artifact.path());
            resolved.add(target);
            if (!isUpToDate(target, artifact)) {
                missing.put(artifact, target);
            }
        }

        if (missing.isEmpty()) {
            return resolved;
        }
        if (offline) {
            throw new BootstrapException(missing.size() + " librar" + (missing.size() == 1 ? "y is" : "ies are")
                    + " missing from " + root + " and offline mode is on:"
                    + System.lineSeparator() + "  " + String.join(System.lineSeparator() + "  ",
                    missing.keySet().stream().map(Artifact::id).toList()));
        }

        final long bytes = missing.keySet().stream().mapToLong(Artifact::size).sum();
        String repositoryList = String.join(", ", sources(extraRepositories));
        if (sources(extraRepositories).size() > 1) {
            repositoryList = "[" + repositoryList + "]";
        }
        Log.info("Downloading " + missing.size() + " " + what + " (" + mib(bytes) + " MiB) from " + repositoryList);
        downloadAll(missing, sources(extraRepositories));
        return resolved;
    }

    private List<String> sources(final List<String> extraRepositories) {
        if (extraRepositories.isEmpty()) {
            return repositories;
        }
        if (!allowExtraRepositories) {
            Log.warn("Ignoring " + extraRepositories.size() + " extra repositor(y/ies): "
                    + "fidorial.libraries.pluginRepositories is off.");
            return repositories;
        }
        final List<String> all = new ArrayList<>(repositories);
        for (final String extra : extraRepositories) {
            final String normalised = extra.endsWith("/") ? extra : extra + "/";
            if (!all.contains(normalised)) {
                all.add(normalised);
            }
        }
        return all;
    }

    private void downloadAll(final Map<Artifact, Path> missing, final List<String> sources) throws IOException {
        Files.createDirectories(root);
        final int total = missing.size();
        final AtomicInteger completed = new AtomicInteger();
        final int threads = Math.min(8, missing.size());
        final ExecutorService pool = Executors.newFixedThreadPool(threads, runnable -> {
            final Thread thread = new Thread(runnable, "fidorial-library-download");
            thread.setDaemon(true);
            return thread;
        });

        try (final Closeable ignored = lock()) {
            final Map<Artifact, Future<?>> tasks = new LinkedHashMap<>(missing.size());
            for (final Map.Entry<Artifact, Path> entry : missing.entrySet()) {
                tasks.put(entry.getKey(), pool.submit(() -> {
                    try {
                        download(entry.getKey(), entry.getValue(), sources, completed, total);
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    return null;
                }));
            }

            final List<String> failures = new ArrayList<>();
            for (final Map.Entry<Artifact, Future<?>> task : tasks.entrySet()) {
                try {
                    task.getValue().get();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BootstrapException("interrupted while downloading libraries", e);
                } catch (final ExecutionException e) {
                    final String reason = switch (e.getCause()) {
                        case final UncheckedIOException unchecked -> String.valueOf(unchecked.getCause());
                        case null -> String.valueOf(e.getMessage());
                        case final Throwable cause -> String.valueOf(cause.getMessage());
                    };
                    failures.add(task.getKey().id() + System.lineSeparator() + "      " + reason);
                }
            }

            reportFallbacks();

            if (!failures.isEmpty()) {
                throw new BootstrapException("could not download " + failures.size() + " of "
                        + missing.size() + " librar" + (missing.size() == 1 ? "y" : "ies") + ":"
                        + System.lineSeparator() + "  - "
                        + String.join(System.lineSeparator() + "  - ", failures));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private void download(final Artifact artifact, final Path target, final List<String> sources, final AtomicInteger completed, final int total) throws IOException {
        final Path directory = target.getParent();
        if (directory == null) {
            throw new BootstrapException(artifact.fileName() + " has no parent directory");
        }
        Files.createDirectories(directory);
        final List<String> failures = new ArrayList<>();

        for (final String repository : sources) {
            final URI uri = URI.create(repository + artifact.path());
            for (int attempt = 1; attempt <= ATTEMPTS_PER_REPOSITORY; attempt++) {
                Path temporary = null;
                try {
                    temporary = Files.createTempFile(directory, artifact.fileName(), ".part");
                    final HttpRequest request = HttpRequest.newBuilder(uri)
                            .header("User-Agent", USER_AGENT)
                            .timeout(REQUEST_TIMEOUT)
                            .GET()
                            .build();
                    final HttpResponse<Path> response = http.send(request,
                            HttpResponse.BodyHandlers.ofFile(temporary,
                                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));

                    // 4xx is an answer, not a hiccup: this mirror does not have the artifact.
                    // Only server errors and network failures deserve a second attempt.
                    if (response.statusCode() >= 400 && response.statusCode() < 500) {
                        failures.add(uri + " -> HTTP " + response.statusCode());
                        break;
                    }
                    if (response.statusCode() != 200) {
                        failures.add(uri + " -> HTTP " + response.statusCode());
                        continue;
                    }

                    final String actual = Sha256.of(temporary);
                    if (!actual.equalsIgnoreCase(artifact.sha256())) {
                        throw new BootstrapException("checksum mismatch for " + artifact.id()
                                + System.lineSeparator() + "  from     " + uri
                                + System.lineSeparator() + "  expected " + artifact.sha256()
                                + System.lineSeparator() + "  actual   " + actual
                                + System.lineSeparator()
                                + "Refusing to run. Wipe " + root + " and retry; if it persists the mirror is serving a different artifact.");
                    }

                    Files.move(temporary, target,
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    Log.info(String.format(Locale.ROOT, "  [%d/%d] %s", completed.incrementAndGet(), total, artifact.id()));
                    if (!repository.equals(repositories.getFirst())) {
                        servedByFallback
                                .computeIfAbsent(repository, key -> Collections.synchronizedList(new ArrayList<>()))
                                .add(artifact.id());
                    }
                    return;
                } catch (final IOException e) {
                    failures.add(uri + " -> " + e);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BootstrapException("interrupted while downloading " + artifact.id(), e);
                } finally {
                    if (temporary != null) {
                        Files.deleteIfExists(temporary);
                    }
                }
            }
        }

        throw new BootstrapException(String.join(System.lineSeparator() + "      ", failures));
    }

    /**
     * A mirror that has to be bypassed is a mirror that is missing a proxy. Saying so out
     * loud is the difference between "it works" and "it works, and someone else is paying
     * for the bandwidth".
     */
    private void reportFallbacks() {
        if (servedByFallback.isEmpty()) {
            return;
        }
        Log.warn(repositories.getFirst() + " could not serve every artifact; these came from elsewhere.");
        Log.warn("Proxy them on the mirror to keep traffic off third-party repositories:");
        servedByFallback.forEach((repository, artifacts) -> {
            final List<String> sorted = new ArrayList<>(artifacts);
            Collections.sort(sorted);
            Log.warn("  " + repository);
            sorted.forEach(id -> Log.warn("      " + id));
        });
        servedByFallback.clear();
    }

    private boolean isUpToDate(final Path target, final Artifact artifact) throws IOException {
        if (!Files.isRegularFile(target)) {
            return false;
        }
        if (Files.size(target) != artifact.size()) {
            return false;
        }
        if (!verifyCached) {
            return true;
        }
        return Sha256.of(target).equalsIgnoreCase(artifact.sha256());
    }

    private Closeable lock() throws IOException {
        final Path lockFile = root.resolve(".lock");
        final FileChannel channel = FileChannel.open(lockFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        final FileLock fileLock;
        try {
            fileLock = channel.lock();
        } catch (final IOException e) {
            channel.close();
            throw e;
        }
        return () -> {
            try {
                fileLock.release();
            } finally {
                channel.close();
            }
        };
    }

    private static String mib(final long bytes) {
        return String.format(Locale.ROOT, "%.1f", bytes / (1024.0 * 1024.0));
    }
}
