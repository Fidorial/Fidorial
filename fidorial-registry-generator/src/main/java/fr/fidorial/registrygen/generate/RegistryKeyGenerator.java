package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.RegistryTypeDefinition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Generates the central {@code RegistryKey} record.
 *
 * @since 0.1.0
 */
public final class RegistryKeyGenerator {

    private static final ClassName KEY = ClassName.get(Key.class);
    private static final ClassName KEY_PATTERN = ClassName.get(KeyPattern.class);

    /**
     * Generates {@code RegistryKey.java}.
     *
     * @param registryTypes   configured supported registries
     * @param registryPackage package {@code RegistryKey} itself is written to
     * @param dataPackage     base package for marker interfaces (each registry type's own
     *                        subpackage is resolved against this, if it has one); bound
     *                        registries use their own type instead
     * @param outputDirectory generated source root
     * @throws IOException if the generated file cannot be written
     */
    public void generate(final List<RegistryTypeDefinition> registryTypes,
                         final String registryPackage,
                         final String dataPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(registryTypes, "registryTypes");
        Objects.requireNonNull(registryPackage, "registryPackage");
        Objects.requireNonNull(dataPackage, "dataPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final ClassName registryKey = ClassName.get(registryPackage, "RegistryKey");

        final MethodSpec constructor = MethodSpec.compactConstructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Key.class, "key")
                .addStatement("$T.requireNonNull(key, $S)", Objects.class, "key")
                .build();
        final TypeVariableName typeVariable = TypeVariableName.get("T");
        final TypeSpec.Builder registryKeyType = TypeSpec
                .recordBuilder("RegistryKey")
                .recordConstructor(constructor)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariable)
                .addJavadoc("Identifies a Minecraft registry.\n\n")
                .addJavadoc("@param <T> marker type for entries contained by the registry\n")
                .addJavadoc("@param key namespaced registry identifier\n");

        addRegistryFields(registryKeyType, registryTypes, registryKey, dataPackage);
        registryKeyType.addMethod(createFactoryMethod(registryKey));
        registryKeyType.addMethod(createFactoryKeyMethod(registryKey));
        registryKeyType.addMethod(createToStringMethod());

        JavaFile.builder(registryPackage, registryKeyType.build()).indent("    ").skipJavaLangImports(true).build().writeTo(outputDirectory);
    }

    private static void addRegistryFields(final TypeSpec.Builder registryKeyType,
                                          final List<RegistryTypeDefinition> registryTypes,
                                          final ClassName registryKey,
                                          final String dataPackage) {

        for (final RegistryTypeDefinition registryType : registryTypes) {

            final ClassName markerType = ClassName.bestGuess(registryType.valueType(dataPackage));
            final ParameterizedTypeName fieldType = ParameterizedTypeName.get(registryKey, markerType);


            final String fieldName = GenerationUtils.constantName(registryType.path(), true);
            registryKeyType.addField(FieldSpec.builder(fieldType, fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("of($S)", registryType.path())
                    .addJavadoc("Registry key for {@code $L}.\n", registryType.identifier())
                    .build());
        }
    }

    /**
     * Generates:
     *
     * <pre>{@code
     * private static <T> RegistryKey<T> of(
     *         @KeyPattern final String path
     * ) {
     *     return new RegistryKey<>(Key.key(path));
     * }
     * }</pre>
     */
    private static MethodSpec createFactoryMethod(final ClassName registryKey) {

        final TypeVariableName typeVariable = TypeVariableName.get("T");
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(registryKey, typeVariable);
        final ParameterSpec pathParameter = ParameterSpec.builder(String.class, "path", Modifier.FINAL).addAnnotation(KEY_PATTERN).build();

        return MethodSpec.methodBuilder("of")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addTypeVariable(typeVariable)
                .returns(returnType)
                .addParameter(pathParameter)
                .addStatement("return new $T<>($T.key($N))", registryKey, KEY, "path")
                .build();
    }

    private static MethodSpec createFactoryKeyMethod(final ClassName registryKey) {

        final TypeVariableName typeVariable = TypeVariableName.get("T");
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(registryKey, typeVariable);
        final ParameterSpec pathParameter = ParameterSpec.builder(Key.class, "key", Modifier.FINAL).build();

        return MethodSpec.methodBuilder("of")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(typeVariable)
                .returns(returnType)
                .addParameter(pathParameter)
                .addStatement("return new $T<>($N)", registryKey, "key")
                .build();
    }

    private static MethodSpec createToStringMethod() {

        return MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S + $N + $S", "RegistryKey[", "key", "]")
                .build();
    }
}
