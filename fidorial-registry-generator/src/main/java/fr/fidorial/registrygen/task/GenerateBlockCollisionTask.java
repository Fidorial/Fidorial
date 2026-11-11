package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.BlockCollisionDatasetGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Writes the collision shape of every block state, from PrismarineJS's collision shapes. See
 * {@link BlockCollisionDatasetGenerator}.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateBlockCollisionTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineCollisionShapes();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineBlocksReport();

    @OutputFile
    public abstract RegularFileProperty getBlockCollisionDataset();

    @TaskAction
    public void generate() {
        try {
            final int count = new BlockCollisionDatasetGenerator().generate(
                    getPrismarineCollisionShapes().get().getAsFile().toPath(),
                    getPrismarineBlocksReport().get().getAsFile().toPath(),
                    getBlockCollisionDataset().get().getAsFile().toPath());
            getLogger().lifecycle("Wrote the collision shapes of {} blocks to {}", count, getBlockCollisionDataset().get().getAsFile());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the block collision dataset", exception);
        }
    }
}