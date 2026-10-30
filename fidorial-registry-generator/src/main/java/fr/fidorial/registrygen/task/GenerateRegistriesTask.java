package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.RegistryGenerator;
import fr.fidorial.registrygen.model.RegistryTypeDefinition;
import fr.fidorial.registrygen.model.SupportedRegistries;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The GenerateRegistriesTask is a Gradle task designed for generating registry source files
 * based on Minecraft version-specific reports. This task processes input reports, applies
 * provided registry mappings, and generates source files in the specified output directory.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateRegistriesTask extends DefaultTask {

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getGeneratedPackage();

    @Input
    public abstract Property<String> getRegistryDataPackage();

    @Input
    public abstract Property<String> getRegistryKeysPackage();

    @Input
    public abstract MapProperty<String, String> getRegistries();

    @Input
    public abstract Property<Boolean> getGenerateRegistryKey();

    @Input
    public abstract ListProperty<String> getProtocolIdRegistries();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getReportsDirectory();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getVanillaDataDirectory();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @Optional
    @OutputDirectory
    public abstract DirectoryProperty getRegistriesDatasetDirectory();

    @TaskAction
    public void generateRegistries() throws IOException {

        final Path registriesJson = getReportsDirectory().get().getAsFile().toPath().resolve("registries.json");
        final Path outputDirectory = getGeneratedSourcesDirectory().get().getAsFile().toPath();
        final Map<String, String> configured = getRegistries().getOrElse(Map.of());

        final Path vanillaDataDirectory = getVanillaDataDirectory().isPresent()
                ? getVanillaDataDirectory().get().getAsFile().toPath()
                : null;

        if (vanillaDataDirectory == null || !Files.isDirectory(vanillaDataDirectory)) {
            getLogger().lifecycle("No vanilla data directory (add \"--server\" to dataGeneratorArguments "
                    + "to also generate registry tags) - every registry in the dataset will have empty tags.");
        }

        final Path datasetDirectory = getRegistriesDatasetDirectory().isPresent()
                ? getRegistriesDatasetDirectory().get().getAsFile().toPath()
                : null;

        final List<RegistryTypeDefinition> registryTypes = configured.isEmpty()
                ? SupportedRegistries.ALL
                : configured.entrySet().stream()
                  .map(e -> RegistryTypeDefinition.parse(e.getKey(), e.getValue()))
                  .toList();

        new RegistryGenerator().generate(registriesJson, vanillaDataDirectory, outputDirectory, datasetDirectory,
                registryTypes, Set.copyOf(getProtocolIdRegistries().getOrElse(List.of())), getGeneratedPackage().get(),
                getRegistryDataPackage().get(), getRegistryKeysPackage().get(), getGenerateRegistryKey().get());
    }
}
