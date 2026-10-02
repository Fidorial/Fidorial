package fr.euphyllia.fidorial.testplugin.pregen;

import fr.fidorial.world.Chunk;
import fr.fidorial.world.World;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PregenTask {

    private static final int MAX_IN_FLIGHT = 64;

    private static final long REPORT_PERIOD_MS = 5_000;

    private final World world;
    private final ComponentLogger logger;
    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final int total;
    private final Consumer<String> progressListener;

    private final Semaphore inFlight = new Semaphore(MAX_IN_FLIGHT);
    private final AtomicInteger done = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final long startedAt = System.currentTimeMillis();
    private volatile boolean cancelled;
    private volatile boolean finished;
    private final Runnable onStart;
    private final Runnable onFinish;
    private final Thread thread = Thread.ofPlatform().name("fidorial-pregen").unstarted(() -> {
        try {
            run();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    });

    public PregenTask(
            final World world,
            final ComponentLogger logger,
            final int centerX,
            final int centerZ,
            final int radius,
            final Consumer<String> progressListener,
            final Runnable onStart,
            final Runnable onFinish
    ) {
        this.world = world;
        this.logger = logger;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.total = (2 * radius + 1) * (2 * radius + 1);
        this.progressListener = progressListener;
        this.onStart = onStart;
        this.onFinish = onFinish;
    }

    public void start() {
        onStart.run();
        thread.start();
    }

    public void cancel() {
        cancelled = true;
        onFinish.run();
        thread.interrupt();
    }

    public boolean isRunning() {
        return !finished && !cancelled;
    }

    public String status() {
        final int completed = done.get();
        final long elapsedMs = Math.max(1, System.currentTimeMillis() - startedAt);
        final double perSecond = completed * 1000.0 / elapsedMs;
        final long remaining = total - completed;
        final long etaSeconds = perSecond > 0 ? (long) (remaining / perSecond) : -1;
        return String.format(
                "%d/%d chunks (%.1f%%), %.0f chunks/s, ETA %s",
                completed, total, completed * 100.0 / total, perSecond, etaSeconds < 0 ? "?" : etaSeconds + "s");
    }

    private void run() throws ExecutionException, InterruptedException {
        logger.info("Pre-generation started: radius {} around {},{} ({} chunks)", radius, centerX, centerZ, total);
        long nextReport = System.currentTimeMillis() + REPORT_PERIOD_MS;

        outer:
        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    if (cancelled) {
                        break outer;
                    }
                    submit(this.world, centerX + dx, centerZ + dz);

                    if (System.currentTimeMillis() >= nextReport) {
                        nextReport = System.currentTimeMillis() + REPORT_PERIOD_MS;
                        progressListener.accept(status());
                    }
                }
            }
        }

        // attendre la fin des chargements en vol
        try {
            inFlight.acquire(MAX_IN_FLIGHT);
        } catch (final InterruptedException e) {
            thread.interrupt();
        }
        finished = true;

        try {
            onFinish.run();
        } catch (final Exception e) {
            logger.warn("Failed to refresh the commands", e);
        }

        if (cancelled) {
            progressListener.accept("Pre-generation cancelled after " + done.get() + " chunks.");
        } else {
            final long seconds = (System.currentTimeMillis() - startedAt) / 1000;
            progressListener.accept("Pre-generation finished: " + done.get() + " chunks in " + seconds + "s"
                    + (failed.get() > 0 ? " (" + failed.get() + " failures, see the logs)" : "") + ".");
        }
    }

    private CompletableFuture<Chunk> submit(final World world, final int chunkX, final int chunkZ) {
        try {
            inFlight.acquire();
        } catch (final InterruptedException e) {
            thread.interrupt();
            cancelled = true;
            return new CompletableFuture<>();
        }

        return world.chunkAsync(chunkX, chunkZ)
                .thenCompose(c -> world.unloadChunkAsync(chunkX, chunkZ).thenApply(_ -> c))
                .whenComplete((_, error) -> {
                    inFlight.release();
                    if (error != null) {
                        failed.incrementAndGet();
                        logger.warn("Failed to pre-generate chunk {},{}", chunkX, chunkZ, error);
                    }
                    done.incrementAndGet();
                });
    }
}
