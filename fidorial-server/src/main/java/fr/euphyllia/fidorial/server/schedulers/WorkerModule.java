package fr.euphyllia.fidorial.server.schedulers;

import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.lifecycle.Quietly;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;

/**
 * The thread pools that tick and load the worlds: regions, chunks and AI.
 *
 * <p>Added before the modules that use them, so that they are stopped after them, and before any
 * module is closed (saved).</p>
 */
public final class WorkerModule implements ServerModule {

    private final ThreadedChunkWorker chunkWorker;
    private final ThreadedRegionRegionizer regionizer;
    private final AiWorker aiWorker;

    public WorkerModule(final ServerConfiguration config) {
        final ServerConfiguration.Threading threading = config.threading();
        this.chunkWorker = new ThreadedChunkWorker(threading.chunkWorkers());
        this.regionizer = new ThreadedRegionRegionizer(threading.regionWorkers(), threading.regionSectionShift());
        this.aiWorker = new AiWorker(threading.aiWorkers());
    }

    @Override
    public void stop() {
        Quietly.run("ai workers", aiWorker::shutdown);
        Quietly.run("region workers", regionizer::shutdown);
        Quietly.run("chunk workers", chunkWorker::shutdown);
    }

    public ThreadedRegionRegionizer regionizer() {
        return regionizer;
    }

    public ThreadedChunkWorker chunkWorker() {
        return chunkWorker;
    }

    public AiWorker aiWorker() {
        return aiWorker;
    }
}
