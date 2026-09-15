package fr.euphyllia.fidorial.server.testing;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.plugin.PluginMeta;
import fr.fidorial.testing.ScenarioTestInfo;
import fr.fidorial.testing.ScenarioTestInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

final class ScenarioTestMain {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ScenarioTestMain.class);

    private ScenarioTestMain() {
    }

    static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            LOGGER.error("Usage: ScenarioTestMain <package.to.scan> [more.packages...]");
            System.exit(2);
            return;
        }

        final FidorialServer server = new FidorialServer(true);
        server.start();

        final ScenarioTestRunner runner = new ScenarioTestRunner();
        final List<ScenarioTestInstance> tests =
                ScenarioTestScanner.scanPackages(collectClassLoaders(server), args);

        final CountDownLatch done = new CountDownLatch(1);
        runner.onComplete(done::countDown);

        runner.start(tests, server);
        if (runner.isDone()) {
            done.countDown();
        }

        done.await();

        logResults(runner);

        server.shutdown();
        System.exit(runner.failedRequiredCount());
    }

    private static List<ClassLoader> collectClassLoaders(final FidorialServer server) {
        final List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(ScenarioTestMain.class.getClassLoader());
        for (final PluginMeta meta : server.plugins().loaded()) {
            server.plugins().plugin(meta.id())
                    .map(p -> p.getClass().getClassLoader())
                    .ifPresent(loaders::add);
        }
        return loaders;
    }

    private static void logResults(final ScenarioTestRunner runner) {
        for (final ScenarioTestInfo info : runner.finished()) {
            if (info.state() == ScenarioTestInfo.State.FAILED) {
                logFailure(info);
            } else {
                logSuccess(info);
            }
        }

        final int failed = runner.failedRequiredCount();
        if (failed != 0) {
            LOGGER.error(Component.text(failed + " required test(s) failed", NamedTextColor.RED, TextDecoration.BOLD));
        }
    }

    private static void logFailure(final ScenarioTestInfo info) {
        final Throwable error = info.error();
        final String message = error != null ? error.getMessage() : "unknown";
        final Throwable cause = error != null ? error.getCause() : null;
        final String suffix = cause != null ? " (last failed assertion: " + cause.getMessage() + ")" : "";

        LOGGER.error(Component.text("FAIL: ", Style.style(TextDecoration.BOLD))
                .append(Component.text(info.instance().id(), Style.style(TextDecoration.BOLD)))
                .append(Component.text(" - ", Style.style(TextDecoration.BOLD)))
                .append(Component.text(message + suffix, Style.style(TextDecoration.BOLD)))
                .append(groupSuffix(info)));
    }

    private static void logSuccess(final ScenarioTestInfo info) {
        LOGGER.info(Component.text("PASS: ", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(info.instance().id(), Style.style(NamedTextColor.GRAY, TextDecoration.BOLD)))
                .append(groupSuffix(info)));
    }

    private static Component groupSuffix(final ScenarioTestInfo info) {
        return Component.text(" [" + info.instance().group() + "]", NamedTextColor.DARK_GRAY);
    }
}
