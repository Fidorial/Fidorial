package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.BlockSupportDatasetGenerator;
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
 * Writes, for each block state, whether it conducts redstone power and which faces hold other
 * blocks, from PrismarineJS's collision shapes and Mojang's block tags. See
 * {@link BlockSupportDatasetGenerator}.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateBlockSupportTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineCollisionShapes();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineBlocksReport();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getBlockTags();

    @OutputFile
    public abstract RegularFileProperty getBlockSupportDataset();

    @TaskAction
    public void generate() {
        try {
            final int count = new BlockSupportDatasetGenerator().generate(
                    getPrismarineCollisionShapes().get().getAsFile().toPath(),
                    getPrismarineBlocksReport().get().getAsFile().toPath(),
                    getBlockTags().get().getAsFile().toPath(),
                    getBlockSupportDataset().get().getAsFile().toPath());
            getLogger().lifecycle("Wrote the support of {} blocks to {}", count, getBlockSupportDataset().get().getAsFile());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the block support dataset", exception);
        }
    }
}