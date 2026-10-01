package fr.euphyllia.fidorial.server.console.command;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.CommandManager;
import fr.euphyllia.fidorial.server.console.command.brigadier.FidorialCommandCompleter;
import fr.euphyllia.fidorial.server.console.command.brigadier.FidorialCommandHighlighter;
import fr.euphyllia.fidorial.server.console.command.brigadier.FidorialConsoleParser;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ConsoleCommandReader {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ConsoleCommandReader.class);

    private final CommandManager commandManager;
    private final BooleanSupplier serverRunning;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public ConsoleCommandReader(final CommandManager commandManager, final BooleanSupplier serverRunning) {
        this.commandManager = commandManager;
        this.serverRunning = serverRunning;
    }

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        Thread thread = new Thread(this::run, "fidorial-console");
        thread.setDaemon(true);
        thread.start();
        LOGGER.debug("Interactive console ready");
    }

    private void run() {
        Supplier<CommandSource> consoleSource = () -> FidorialServer.getInstance().getConsole();

        try (final Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            final LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .appName("Fidorial")
                    .parser(new FidorialConsoleParser(commandManager, consoleSource))
                    .completer(new FidorialCommandCompleter(commandManager, consoleSource))
                    .highlighter(new FidorialCommandHighlighter(commandManager, consoleSource))
                    .build();

            final String prompt = "> ";

            while (serverRunning.getAsBoolean()) {
                String line;
                try {
                    line = lineReader.readLine(prompt);
                } catch (UserInterruptException | EndOfFileException e) {
                    FidorialServer.getInstance().shutdown();
                    break;
                }

                if (line == null) continue;

                line = line.stripTrailing();
                if (line.isBlank()) continue;

                commandManager.dispatchAsync(FidorialServer.getInstance().getConsole(), line);
            }
        } catch (final IOException e) {
            LOGGER.warn("Console reading interrupted: {}", e.getMessage());
        }
    }
}
