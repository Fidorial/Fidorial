package fr.euphyllia.fidorial.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

final class BundledJars {

    static final String DIRECTORY = "META-INF/fidorial/";
    static final String LIST = DIRECTORY + "bundled.list";

    private BundledJars() {
    }

    static List<Path> extract(final JarFile self, final Path librariesDir) throws IOException {
        final ZipEntry listEntry = self.getEntry(LIST);
        if (listEntry == null) {
            throw new BootstrapException("this jar has no " + LIST
                    + "; it was probably built with the plain `jar` task instead of `bootstrapJar`");
        }

        final List<Artifact> artifacts;
        final InputStream list = self.getInputStream(listEntry);
        if (list == null) {
            throw new BootstrapException("cannot read " + LIST);
        }
        try (list) {
            artifacts = Artifact.read(list, false);
        }

        final List<Path> paths = new ArrayList<>(artifacts.size());
        for (final Artifact artifact : artifacts) {
            final Path target = librariesDir.resolve(artifact.path());
            final Path directory = target.getParent();
            if (directory == null) {
                throw new BootstrapException("bundled jar " + artifact.fileName() + " has no parent directory");
            }
            Files.createDirectories(directory);

            if (!upToDate(target, artifact)) {
                final ZipEntry entry = self.getEntry(DIRECTORY + artifact.fileName());
                if (entry == null) {
                    throw new BootstrapException("bundled jar " + artifact.fileName() + " is listed but missing from the launcher");
                }
                final Path temporary = Files.createTempFile(directory, artifact.fileName(), ".part");
                final InputStream in = self.getInputStream(entry);
                if (in == null) {
                    throw new BootstrapException("cannot read bundled jar " + artifact.fileName());
                }
                try (in) {
                    Files.copy(in, temporary, StandardCopyOption.REPLACE_EXISTING);
                    final String actual = Sha256.of(temporary);
                    if (!actual.equalsIgnoreCase(artifact.sha256())) {
                        throw new BootstrapException("bundled jar " + artifact.fileName() + " is corrupt (checksum mismatch)");
                    }
                    Files.move(temporary, target,
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
            paths.add(target);
        }
        return paths;
    }

    private static boolean upToDate(final Path target, final Artifact artifact) throws IOException {
        return Files.isRegularFile(target)
                && Files.size(target) == artifact.size()
                && Sha256.of(target).equalsIgnoreCase(artifact.sha256());
    }
}
