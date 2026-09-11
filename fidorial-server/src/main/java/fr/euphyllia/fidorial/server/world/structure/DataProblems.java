package fr.euphyllia.fidorial.server.world.structure;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DataProblems {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DataProblems.class);
    private static final int MAX_REPORTS = 200;

    private final Set<String> reported = ConcurrentHashMap.newKeySet();

    public void warn(final String message) {
        if (reported.size() < MAX_REPORTS && reported.add(message)) {
            LOGGER.warn("[datapacks] {}", message);
        }
    }

    public int count() {
        return reported.size();
    }
}
