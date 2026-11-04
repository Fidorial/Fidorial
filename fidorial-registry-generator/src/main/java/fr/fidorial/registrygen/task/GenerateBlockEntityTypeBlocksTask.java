package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.RegistryGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Generates {@code BlockEntityTypeBlocks} by joining Mojang's block and block entity type
 * registries with PrismarineJS's block entities report.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateBlockEntityTypeBlocksTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getRegistriesReport();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineBlockEntitiesReport();

    @Input
    public abstract Property<String> getRegistryDataPackage();

    @Input
    public abstract Property<String> getBlockTypeKeysPackage();

    @Input
    public abstract Property<String> getBlockEntityTypeKeysPackage();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @Inject
    public GenerateBlockEntityTypeBlocksTask() {
    }

    @TaskAction
    public void generate() {
        try {
            new RegistryGenerator().generateBlockEntityTypeBlocks(
                    getRegistriesReport().get().getAsFile().toPath(),
                    getPrismarineBlockEntitiesReport().get().getAsFile().toPath(),
                    getGeneratedSourcesDirectory().get().getAsFile().toPath(),
                    getRegistryDataPackage().get(),
                    getBlockTypeKeysPackage().get(),
                    getBlockEntityTypeKeysPackage().get());
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to generate block entity type blocks", e);
        }
    }
}
