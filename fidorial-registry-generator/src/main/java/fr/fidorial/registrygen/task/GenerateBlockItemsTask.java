package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.BlockItemsDatasetGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Writes which block each item places, from the outputs of Mojang's data generator only. See
 * {@link BlockItemsDatasetGenerator}.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateBlockItemsTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getBlocksReport();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getItemComponentsReports();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getBlockLootTables();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getBlockTags();

    @OutputFile
    public abstract RegularFileProperty getBlockItemsDataset();

    @TaskAction
    public void generate() {
        try {
            final int count = new BlockItemsDatasetGenerator().generate(
                    getBlocksReport().get().getAsFile().toPath(),
                    getItemComponentsReports().get().getAsFile().toPath(),
                    getBlockLootTables().get().getAsFile().toPath(),
                    getBlockTags().get().getAsFile().toPath(),
                    getBlockItemsDataset().get().getAsFile().toPath());
            getLogger().lifecycle("Wrote the blocks of {} items to {}", count, getBlockItemsDataset().get().getAsFile());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the block items dataset", exception);
        }
    }
}