package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.RecipeDatasetGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Bundles the vanilla recipes dumped by Mojang's data generator into the dataset the server loads.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateRecipesTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getVanillaDataDirectory();

    @OutputFile
    public abstract RegularFileProperty getRecipesDataset();

    @Inject
    public GenerateRecipesTask() {
    }

    @TaskAction
    public void generate() {
        final Path vanillaData = getVanillaDataDirectory().get().getAsFile().toPath();
        if (!Files.isDirectory(vanillaData.resolve("minecraft").resolve("recipe"))) {
            throw new GradleException("No vanilla recipe in " + vanillaData
                    + ": add \"--server\" to dataGeneratorArguments so that Mojang dumps them.");
        }
        try {
            final int count = new RecipeDatasetGenerator()
                    .generate(vanillaData, getRecipesDataset().get().getAsFile().toPath());
            getLogger().lifecycle("Wrote {} vanilla recipes to {}", count, getRecipesDataset().get().getAsFile());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the recipe dataset", exception);
        }
    }
}
