package fr.fidorial.registrygen.task;

import fr.fidorial.registrygen.generate.ItemCombatDatasetGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Writes the dataset the server fights with: the default combat components of every item, from
 * Mojang's item components report.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateItemCombatTask extends DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getItemComponentsReports();

    @OutputFile
    public abstract RegularFileProperty getItemCombatDataset();

    @Inject
    public GenerateItemCombatTask() {
    }

    @TaskAction
    public void generate() {
        final Path itemComponents = getItemComponentsReports().getFiles().stream()
                .filter(File::exists)
                .map(File::toPath)
                .findFirst()
                .orElseThrow(() -> new GradleException("No item components report: run the data generator with \"--reports\"."));
        try {
            final int count = new ItemCombatDatasetGenerator().generate(
                    itemComponents, getItemCombatDataset().get().getAsFile().toPath());
            getLogger().lifecycle("Wrote the combat data of {} items to {}", count, getItemCombatDataset().get().getAsFile());
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to generate the item combat dataset", exception);
        }
    }
}
