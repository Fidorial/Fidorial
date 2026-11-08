package fr.euphyllia.fidorial.bootstrap;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class Main {

    private static final int MINIMUM_JAVA_FEATURE = 25;
    private static final String SERVER_MAIN_CLASS = "fr.euphyllia.fidorial.server.Main";
    private static final String LIBRARIES_LIST = BundledJars.DIRECTORY + "libraries.list";
    private static final String REPOSITORIES_LIST = BundledJars.DIRECTORY + "repositories.list";

    private Main() {
    }

    public static void main(final String[] args) {
        if (Runtime.version().feature() < MINIMUM_JAVA_FEATURE) {
            System.err.println("Fidorial requires Java " + MINIMUM_JAVA_FEATURE
                    + " or newer, but this JVM is Java " + Runtime.version().feature()
                    + " (" + System.getProperty("java.home") + ").");
            System.exit(1);
        }
        try {
            run(args);
        } catch (final BootstrapException e) {
            Log.warn(String.valueOf(e.getMessage()));
            if (e.getCause() != null) {
                e.getCause().printStackTrace(System.err);
            }
            System.exit(1);
        } catch (final Throwable t) {
            Log.warn("the launcher failed before the server could start");
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run(final String[] args) throws Exception {
        final Path self = SelfJar.locate();
        if (self == null) {
            throw new BootstrapException("the launcher must run from a jar; "
                    + "use `./gradlew :fidorial-server:run` for a classpath dev session");
        }

        final Path librariesDir = Paths
                .get(System.getProperty("fidorial.libraries.dir", "libraries"))
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(librariesDir);

        final List<Path> classpath = new ArrayList<>();

        try (final JarFile jar = new JarFile(self.toFile())) {
            classpath.addAll(BundledJars.extract(jar, librariesDir));

            final List<Artifact> external;
            final ZipEntry entry = jar.getEntry(LIBRARIES_LIST);
            if (entry == null) {
                throw new BootstrapException("this jar has no " + LIBRARIES_LIST);
            }
            final InputStream in = jar.getInputStream(entry);
            if (in == null) {
                throw new BootstrapException("cannot read " + LIBRARIES_LIST);
            }
            try (in) {
                external = Artifact.read(in, true);
            }

            final List<String> repositories;
            final ZipEntry repositoriesEntry = jar.getEntry(REPOSITORIES_LIST);
            try (final InputStream repositoriesIn = repositoriesEntry == null ? null : jar.getInputStream(repositoriesEntry)) {
                repositories = Repositories.resolve(librariesDir, repositoriesIn);
            }

            final LibraryStore store = new LibraryStore(librariesDir, repositories);
            classpath.addAll(store.resolve(external, "librar" + (external.size() == 1 ? "y" : "ies")));

            System.setProperty("fidorial.libraries.dir", librariesDir.toString());
            System.setProperty("fidorial.libraries.repositories", String.join(",", repositories));
        }

        Launcher.launch(classpath, SERVER_MAIN_CLASS, args);
    }
}
