package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.BlockMiningDatasetGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Writes the dataset the server mines blocks with: block hardness from PrismarineJS's blocks
 * report, tool rules from Mojang's item components report.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateBlockMiningTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineBlocksReport();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getItemComponentsReports();

    @OutputFile
    public abstract RegularFileProperty getBlockMiningDataset();

    @Inject
    public GenerateBlockMiningTask() {
    }

    @TaskAction
    public void generate() {
        final Path itemComponents = getItemComponentsReports().getFiles().stream()
                .filter(File::exists)
                .map(File::toPath)
                .findFirst()
                .orElseThrow(() -> new GradleException("No item components report: run the data generator with \"--reports\"."));
        try {
            final int count = new BlockMiningDatasetGenerator().generate(
                    getPrismarineBlocksReport().get().getAsFile().toPath(),
                    itemComponents,
                    getBlockMiningDataset().get().getAsFile().toPath());
            getLogger().lifecycle("Wrote the mining data of {} blocks to {}", count, getBlockMiningDataset().get().getAsFile());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the block mining dataset", exception);
        }
    }
}