package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.RegistryGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
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

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates {@code FrozenRegistries} — the entries and tags of the registries whose
 * network IDs the client hard-codes, ordered by Mojang's {@code protocol_id}.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateFrozenRegistriesTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getRegistriesReport();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getVanillaDataDirectory();

    @Input
    public abstract ListProperty<String> getFrozenRegistries();

    @Input
    public abstract Property<String> getRegistryDataPackage();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @Inject
    public GenerateFrozenRegistriesTask() {
    }

    @TaskAction
    public void generate() {

        final Path vanillaDataDirectory = getVanillaDataDirectory().isPresent()
                ? getVanillaDataDirectory().get().getAsFile().toPath()
                : null;

        if (vanillaDataDirectory == null || !Files.isDirectory(vanillaDataDirectory)) {
            getLogger().lifecycle("No vanilla data directory (add \"--server\" to dataGeneratorArguments "
                    + "to also generate registry tags) - every frozen registry will have empty tags.");
        }

        try {
            new RegistryGenerator().generateFrozenRegistries(
                    getRegistriesReport().get().getAsFile().toPath(),
                    vanillaDataDirectory,
                    getGeneratedSourcesDirectory().get().getAsFile().toPath(),
                    getRegistryDataPackage().get(),
                    getFrozenRegistries().get());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate frozen registries", exception);
        }
    }
}
