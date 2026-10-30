package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.RegistryTagDefinition;
import fr.fidorial.registrygen.model.RegistryTypeDefinition;
import net.kyori.adventure.key.KeyPattern;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Generates the tag key classes of the vanilla tags, such as {@code BiomeTagKeys}.
 *
 * @since 0.1.0
 */
public final class RegistryTagKeysGenerator {

    private static final ClassName KEY_PATTERN = ClassName.get(KeyPattern.class);
    private static final String MINECRAFT_PREFIX = "minecraft:";

    /**
     * Generates a tag key class.
     *
     * @param registryType    configured Java type information
     * @param tags            the vanilla tags of the registry
     * @param registryPackage package holding {@code RegistryKey}/{@code TagKey}
     * @param valueType       fully qualified type of the registry values
     * @param keysPackage     package this class is written to
     * @param outputDirectory generated Java source root
     * @throws IOException if the source file cannot be written
     */
    public void generate(final RegistryTypeDefinition registryType,
                         final List<RegistryTagDefinition> tags,
                         final String registryPackage,
                         final String valueType,
                         final String keysPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(registryType, "registryType");
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(registryPackage, "registryPackage");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(keysPackage, "keysPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final ClassName registryKey = ClassName.get(registryPackage, "RegistryKey");
        final ClassName tagKey = ClassName.get(registryPackage, "TagKey");
        final ParameterizedTypeName tagKeyType = ParameterizedTypeName.get(tagKey, ClassName.bestGuess(valueType));

        final TypeSpec.Builder type = TypeSpec.classBuilder(registryType.tagKeysClassName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Keys of the vanilla tags of the {@code $L} registry.\n", registryType.identifier());

        final Set<String> fieldNames = new HashSet<>();
        for (final RegistryTagDefinition tag : tags) {
            final String fieldName = GenerationUtils.constantName(tag.identifier());
            if (!fieldNames.add(fieldName)) {
                throw new IllegalStateException("Multiple tags in '" + registryType.identifier()
                        + "' produce the Java field name '" + fieldName + "'.");
            }
            type.addField(FieldSpec.builder(tagKeyType, fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("create($S)", value(tag.identifier()))
                    .addJavadoc("Tag {@code #$L}.\n", tag.identifier())
                    .build());
        }

        type.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("throw new $T($S)", UnsupportedOperationException.class,
                        registryType.tagKeysClassName() + " cannot be instantiated.")
                .build());

        type.addMethod(MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(tagKeyType)
                .addParameter(ParameterSpec.builder(String.class, "value", Modifier.FINAL).addAnnotation(KEY_PATTERN).build())
                .addStatement("return $T.create($T.$N, $N)", tagKey, registryKey,
                        GenerationUtils.constantName(registryType.path(), true), "value")
                .build());

        JavaFile.builder(keysPackage, type.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(outputDirectory);
    }

    private static String value(final String identifier) {
        return identifier.startsWith(MINECRAFT_PREFIX) ? identifier.substring(MINECRAFT_PREFIX.length()) : identifier;
    }
}
