package fr.euphyllia.fidorial.server.entity.player.profile;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Command line reader for a profile journal.
 *
 * <p>The journal is a binary file and cannot be read with a text editor. This tool prints its
 * contents, in a table by default and in JSON or tab separated form on request, so that it can be
 * inspected directly or piped into other tools.</p>
 *
 * <p>The file is opened for reading only. Unlike {@link ProfileJournal#open(ProfileJournal.Replay)},
 * a damaged trailing record is reported rather than repaired, so the tool is safe to run against
 * the journal of a running server.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 * java -cp fidorial-server.jar fr.euphyllia.fidorial.server.entity.player.profile.ProfileDump \
 *     &lt;file&gt; [options]
 *
 *   --json          emit JSON instead of a table
 *   --tsv           emit tab separated values instead of a table
 *   --history       list every record in file order, superseded ones and removals included,
 *                   instead of the resulting entries
 *   --verify        report integrity only, and exit non-zero if the file is damaged
 *   --name &lt;text&gt;   keep entries whose name contains the text, ignoring case
 *   --uuid &lt;text&gt;   keep entries whose identity starts with the text
 * </pre>
 */
public final class ProfileDump {

    private static final DateTimeFormatter TIMESTAMPS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).withZone(ZoneId.systemDefault());

    private ProfileDump() {
    }

    /**
     * Runs the tool.
     *
     * @param args the file to read, followed by any options
     * @throws IOException if the file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final PrintStream out = System.out;

        if (args.length == 0 || "--help".equals(args[0]) || "-h".equals(args[0])) {
            usage(out);
            return;
        }

        final Path file = Path.of(args[0]);
        if (!Files.isRegularFile(file)) {
            System.err.println("No such file: " + file);
            System.exit(2);
            return;
        }

        Format format = Format.TABLE;
        boolean history = false;
        boolean verifyOnly = false;
        String nameFilter = null;
        String uuidFilter = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--json" -> format = Format.JSON;
                case "--tsv" -> format = Format.TSV;
                case "--history" -> history = true;
                case "--verify" -> verifyOnly = true;
                case "--name" -> nameFilter = argument(args, ++i, "--name");
                case "--uuid" -> uuidFilter = argument(args, ++i, "--uuid");
                default -> {
                    System.err.println("Unknown option: " + args[i]);
                    usage(System.err);
                    System.exit(2);
                    return;
                }
            }
        }

        final List<Record> records = new ArrayList<>();
        final ProfileJournal.Scan scan =
                ProfileJournal.scan(file, (entry, removed) -> records.add(new Record(entry, removed)));

        if (verifyOnly) {
            out.printf("file      %s%n", file);
            out.printf(Locale.ROOT, "size      %d bytes%n", scan.fileBytes());
            out.printf(Locale.ROOT, "records   %d%n", scan.records());
            out.printf("state     %s%n", scan.intact()
                    ? "intact"
                    : "damaged, " + scan.danglingBytes() + " unreadable trailing bytes");
            System.exit(scan.intact() ? 0 : 1);
            return;
        }

        final List<Record> rows = history ? records : resolve(records);
        final List<Record> filtered = filter(rows, nameFilter, uuidFilter);

        switch (format) {
            case JSON -> printJson(out, filtered, history);
            case TSV -> printTsv(out, filtered, history);
            case TABLE -> printTable(out, filtered, history, scan, records.size());
        }

        if (!scan.intact()) {
            System.err.printf("warning: %d unreadable trailing bytes; the last record is incomplete%n",
                    scan.danglingBytes());
        }
    }

    private static String argument(final String[] args, final int index, final String option) {
        if (index >= args.length) {
            System.err.println("Missing value for " + option);
            System.exit(2);
        }
        return args[index];
    }

    /** Applies last write wins, yielding the entries the server would hold in memory. */
    private static List<Record> resolve(final List<Record> records) {
        final Map<UUID, Record> live = new LinkedHashMap<>();
        for (final Record record : records) {
            if (record.removed()) {
                live.remove(record.entry().uuid());
            } else {
                live.put(record.entry().uuid(), record);
            }
        }
        final List<Record> out = new ArrayList<>(live.values());
        out.sort((a, b) -> Long.compare(b.entry().lastSeen(), a.entry().lastSeen()));
        return out;
    }

    private static List<Record> filter(
            final List<Record> records,
            final @Nullable String nameFilter,
            final @Nullable String uuidFilter
    ) {
        final List<Record> out = new ArrayList<>();
        for (final Record record : records) {
            final String name = record.entry().name();
            if (nameFilter != null && (name == null
                    || !name.toLowerCase(Locale.ROOT).contains(nameFilter.toLowerCase(Locale.ROOT)))) {
                continue;
            }
            if (uuidFilter != null && !record.entry().uuid().toString().startsWith(uuidFilter)) {
                continue;
            }
            out.add(record);
        }
        return out;
    }

    private static void printTable(
            final PrintStream out,
            final List<Record> rows,
            final boolean history,
            final ProfileJournal.Scan scan,
            final int totalRecords
    ) {
        out.printf("%-36s  %-16s  %-19s  %-19s%s%n",
                "uuid", "name", "first seen", "last seen", history ? "  record" : "");
        out.println("-".repeat(history ? 105 : 96));

        for (final Record row : rows) {
            final ProfileEntry entry = row.entry();
            out.printf("%-36s  %-16s  %-19s  %-19s%s%n",
                    entry.uuid(),
                    entry.name() == null ? "-" : entry.name(),
                    time(entry.firstSeen()),
                    time(entry.lastSeen()),
                    history ? (row.removed() ? "  removal" : "  upsert") : "");
        }

        out.println();
        if (history) {
            out.printf(Locale.ROOT, "%d records in file order, %d bytes%n", rows.size(), scan.fileBytes());
        } else {
            out.printf(Locale.ROOT, "%d entries from %d records, %d bytes (%.0f%% superseded)%n",
                    rows.size(), totalRecords, scan.fileBytes(),
                    totalRecords == 0 ? 0.0 : 100.0 * (totalRecords - rows.size()) / totalRecords);
        }
    }

    private static void printTsv(final PrintStream out, final List<Record> rows, final boolean history) {
        out.println(history
                ? "uuid\tname\tfirst_seen\tlast_seen\trecord"
                : "uuid\tname\tfirst_seen\tlast_seen");
        for (final Record row : rows) {
            final ProfileEntry entry = row.entry();
            out.print(entry.uuid());
            out.print('\t');
            out.print(entry.name() == null ? "" : entry.name());
            out.print('\t');
            out.print(entry.firstSeen());
            out.print('\t');
            out.print(entry.lastSeen());
            if (history) {
                out.print('\t');
                out.print(row.removed() ? "removal" : "upsert");
            }
            out.println();
        }
    }

    private static void printJson(final PrintStream out, final List<Record> rows, final boolean history) {
        out.println("[");
        for (int i = 0; i < rows.size(); i++) {
            final ProfileEntry entry = rows.get(i).entry();
            out.print("  {\"uuid\": \"" + entry.uuid() + "\"");
            out.print(", \"name\": " + (entry.name() == null ? "null" : '"' + escape(entry.name()) + '"'));
            out.print(", \"firstSeen\": " + entry.firstSeen());
            out.print(", \"lastSeen\": " + entry.lastSeen());
            if (history) {
                out.print(", \"record\": \"" + (rows.get(i).removed() ? "removal" : "upsert") + '"');
            }
            out.println(i + 1 < rows.size() ? "}," : "}");
        }
        out.println("]");
    }

    private static String escape(final String text) {
        final StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String time(final long epochMillis) {
        return epochMillis == 0L ? "-" : TIMESTAMPS.format(Instant.ofEpochMilli(epochMillis));
    }

    private static void usage(final PrintStream out) {
        out.println("""
                Reads a Fidorial profile journal.

                  java -cp fidorial-server.jar \\
                      fr.euphyllia.fidorial.server.entity.player.profile.ProfileDump <file> [options]

                  --json          emit JSON instead of a table
                  --tsv           emit tab separated values instead of a table
                  --history       list every record in file order, superseded ones and removals
                                  included, instead of the resulting entries
                  --verify        report integrity only, and exit non-zero if the file is damaged
                  --name <text>   keep entries whose name contains the text, ignoring case
                  --uuid <text>   keep entries whose identity starts with the text

                The file is never modified.""");
    }

    private enum Format {
        TABLE,
        JSON,
        TSV
    }

    private record Record(ProfileEntry entry, boolean removed) {
    }
}
