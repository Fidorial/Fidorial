package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.WorldgenDatasetGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

/**
 * Bundles the world generation data dumped by the data generator of the game into the dataset the noise based
 * generator of the server reads.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateWorldgenTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getGameDataDirectory();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getReportsDirectory();

    @OutputFile
    public abstract RegularFileProperty getWorldgenDataset();
    @Inject
    public GenerateWorldgenTask() {
    }

    @TaskAction
    public void generate() {
        final Path gameData = getGameDataDirectory().get().getAsFile().toPath();
        final Path reports = getReportsDirectory().get().getAsFile().toPath();
        if (!Files.isDirectory(gameData.resolve("minecraft").resolve("worldgen").resolve("noise_settings"))) {
            throw new GradleException("No noise settings in " + gameData
                    + ": add \"--server\" to dataGeneratorArguments so that the data generator dumps them.");
        }
        if (!Files.isDirectory(reports.resolve("biome_parameters"))) {
            throw new GradleException("No biome parameter report in " + reports
                    + ": add \"--reports\" to dataGeneratorArguments so that the data generator dumps them.");
        }
        try {
            final Path output = getWorldgenDataset().get().getAsFile().toPath();
            final int count = new WorldgenDatasetGenerator().generate(gameData, reports, output);
            getLogger().lifecycle("Wrote {} world generation entries to {}", count, output);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the world generation dataset", exception);
        }
    }
}