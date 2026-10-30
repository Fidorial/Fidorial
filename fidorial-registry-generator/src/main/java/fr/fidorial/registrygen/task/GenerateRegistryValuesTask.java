package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.RegistryGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates {@code RegistryValues}, the server-side values of every supported registry without a
 * bound API type.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateRegistryValuesTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getRegistriesReport();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getVanillaDataDirectory();

    @Input
    public abstract Property<String> getRegistryValuesPackage();

    @Input
    public abstract Property<String> getApiRegistryPackage();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @TaskAction
    public void generate() throws IOException {
        final Path vanillaDataDirectory = getVanillaDataDirectory().isPresent()
                ? getVanillaDataDirectory().get().getAsFile().toPath()
                : null;

        new RegistryGenerator().generateRegistryValues(
                getRegistriesReport().get().getAsFile().toPath(),
                vanillaDataDirectory,
                getGeneratedSourcesDirectory().get().getAsFile().toPath(),
                getRegistryValuesPackage().get(),
                getApiRegistryPackage().get());
    }
}
