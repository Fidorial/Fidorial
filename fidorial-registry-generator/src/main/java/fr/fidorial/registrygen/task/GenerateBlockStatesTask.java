package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.RegistryGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

@CacheableTask
public abstract class GenerateBlockStatesTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getBlocksReport();

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineBlocksReport();

    @Input
    public abstract Property<String> getRegistryDataPackage();

    @Input
    public abstract Property<String> getBlockTypeKeysPackage();

    @Input
    public abstract Property<String> getBlockPackage();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @Inject
    public GenerateBlockStatesTask() {
    }

    @TaskAction
    public void generate() {
        try {
            final Path prismarineBlocksReport = getPrismarineBlocksReport().isPresent()
                    ? getPrismarineBlocksReport().get().getAsFile().toPath()
                    : null;

            new RegistryGenerator().generateBlockStates(
                    getBlocksReport().get().getAsFile().toPath(),
                    prismarineBlocksReport,
                    getGeneratedSourcesDirectory().get().getAsFile().toPath(),
                    getBlockPackage().get(),
                    getRegistryDataPackage().get(),
                    getBlockTypeKeysPackage().get());
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to generate block states", e);
        }
    }
}
