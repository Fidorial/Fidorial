package fr.fidorial.registrygen.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * GenerateReportsTask is responsible for running the Minecraft data generator
 * to create the necessary data files for a specified Minecraft version.
 * The task executes the data generation process using a given {@link JavaLauncher} (Java executable),
 * a server JAR file, and a set of provided arguments.
 * <p>
 * The generated data is stored in a designated output directory. The task ensures
 * that the output directory is created if it does not already exist. If the process
 * fails, it throws an exception with the generated process exit code.
 *
 * @since 0.1.0
 */
@CacheableTask
public abstract class GenerateReportsTask extends DefaultTask {

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Nested
    public abstract Property<JavaLauncher> getJavaLauncher();

    @Input
    public abstract ListProperty<String> getDataGeneratorArguments();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getServerJar();

    @OutputDirectory
    public abstract DirectoryProperty getDataDirectory();

    @TaskAction
    public void generate() throws IOException, InterruptedException {
        final Path dataDirectory = getDataDirectory().get().getAsFile().toPath();
        final Path serverJar = getServerJar().get().getAsFile().toPath();

        Files.createDirectories(dataDirectory);

        final String javaExecutable = getJavaLauncher().get()
                .getExecutablePath()
                .getAsFile()
                .getAbsolutePath();

        final List<String> command = new ArrayList<>();
        command.add(javaExecutable);
        command.add("-DbundlerMainClass=net.minecraft.data.Main");
        command.add("-jar");
        command.add(serverJar.toAbsolutePath().toString());
        command.addAll(getDataGeneratorArguments().get());

        getLogger().lifecycle("Running Minecraft data generator for Minecraft {}", getMinecraftVersion().get());
        getLogger().lifecycle("Java executable: {}", javaExecutable);
        getLogger().lifecycle("Working directory: {}", dataDirectory.toAbsolutePath());
        getLogger().lifecycle("Command: {}", command);

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(dataDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        final Process process = processBuilder.start();

        try (final BufferedReader reader = process.inputReader(StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                getLogger().lifecycle("[Minecraft Data Generator] {}", line);
            }
        }

        final int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Minecraft data generator exited with code " + exitCode
            );
        }

        final Path registriesFile = dataDirectory.resolve("generated").resolve("reports").resolve("registries.json");

        if (!Files.isRegularFile(registriesFile)) {
            throw new IOException(
                    "Minecraft data generator exited successfully, but did not generate "
                            + registriesFile
            );
        }
    }
}
