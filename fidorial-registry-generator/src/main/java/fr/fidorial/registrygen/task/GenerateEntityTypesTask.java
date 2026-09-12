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
 * Generates the {@code EntityTypes} class — every entity type of the targeted version,
 * with its network ID and hitbox — from Mojang's entity registry and PrismarineJS's
 * entities report.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateEntityTypesTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getRegistriesReport();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getPrismarineEntitiesReport();

    @Input
    public abstract Property<String> getEntityPackage();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @Inject
    public GenerateEntityTypesTask() {
    }

    @TaskAction
    public void generate() {
        try {
            new RegistryGenerator().generateEntityTypes(
                    getRegistriesReport().get().getAsFile().toPath(),
                    getPrismarineEntitiesReport().get().getAsFile().toPath(),
                    getGeneratedSourcesDirectory().get().getAsFile().toPath(),
                    getEntityPackage().get());
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to generate entity types", e);
        }
    }
}
