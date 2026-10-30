package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;
import com.palantir.javapoet.WildcardTypeName;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.RegistryTypeDefinition;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Generates {@code RegistryValues}: one record per registry without a bound API type, implementing
 * its marker interface, and the table mapping each registry to the constructor of its values.
 *
 * <p>This keeps the server's registries in step with {@code RegistryKey}: a registry added to
 * {@code SupportedRegistries} gets its values without any hand-written mapping.</p>
 *
 * @since 0.1.0
 */
public final class RegistryValuesGenerator {

    private static final String CLASS_NAME = "RegistryValues";
    private static final String VALUE_SUFFIX = "Value";
    private static final ClassName KEY = ClassName.get(Key.class);
    private static final ClassName NULLABLE = ClassName.get(Nullable.class);

    /**
     * Generates the {@code RegistryValues} class.
     *
     * @param registryTypes      the registries to emit, all without a bound type
     * @param valuesPackage      package the class is written into
     * @param apiRegistryPackage package of the API's {@code RegistryKey}
     * @param outputDirectory    generated Java source root
     * @throws IOException if the generated file cannot be written
     */
    public void generate(final List<RegistryTypeDefinition> registryTypes,
                         final String valuesPackage,
                         final String apiRegistryPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(registryTypes, "registryTypes");
        Objects.requireNonNull(valuesPackage, "valuesPackage");
        Objects.requireNonNull(apiRegistryPackage, "apiRegistryPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final ClassName registryKey = ClassName.get(apiRegistryPackage, "RegistryKey");
        final ClassName self = ClassName.get(valuesPackage, CLASS_NAME);
        final String apiDataPackage = apiRegistryPackage + ".data";

        final ParameterizedTypeName factoryType = ParameterizedTypeName.get(
                ClassName.get(Function.class), KEY, WildcardTypeName.subtypeOf(Object.class));
        final ParameterizedTypeName factoriesType = ParameterizedTypeName.get(
                ClassName.get(Map.class), KEY, factoryType);

        final TypeSpec.Builder type = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Values of the registries without a bound API type, each carrying only its key.\n\n")
                .addJavadoc("<p>Generated from {@code SupportedRegistries}; do not edit.</p>\n")
                .addField(FieldSpec.builder(factoriesType, "FACTORIES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("factories()")
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("throw new $T($S)", UnsupportedOperationException.class,
                                CLASS_NAME + " cannot be instantiated.")
                        .build());

        final TypeVariableName t = TypeVariableName.get("T");
        final TypeName nullableFactory = ParameterizedTypeName.get(ClassName.get(Function.class), KEY, t)
                .annotated(AnnotationSpec.builder(NULLABLE).build());

        type.addMethod(MethodSpec.methodBuilder("factory")
                .addJavadoc("{@return the constructor of the values of {@code registry}, or {@code null} when it has a bound API type}\n\n")
                .addJavadoc("@param registry the registry\n")
                .addJavadoc("@param <T>      the value type\n")
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(t)
                .returns(nullableFactory)
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(registryKey, t), "registry", Modifier.FINAL).build())
                .addStatement("return ($T) FACTORIES.get(registry.key())", ParameterizedTypeName.get(ClassName.get(Function.class), KEY, t))
                .build());

        final CodeBlock.Builder factories = CodeBlock.builder()
                .addStatement("final $T factories = new $T<>()", factoriesType, HashMap.class);

        for (final RegistryTypeDefinition registryType : registryTypes) {
            if (registryType.isBound()) {
                throw new IllegalArgumentException(registryType.identifier() + " is bound to " + registryType.boundType());
            }
            final ClassName marker = ClassName.bestGuess(registryType.valueType(apiDataPackage));
            final ClassName value = self.nestedClass(registryType.typeName() + VALUE_SUFFIX);

            type.addType(TypeSpec.recordBuilder(value.simpleName())
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("A {@code $L} entry.\n", registryType.identifier())
                    .recordConstructor(MethodSpec.compactConstructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(KEY, "key")
                            .addStatement("$T.requireNonNull(key, $S)", Objects.class, "key")
                            .build())
                    .addSuperinterface(marker)
                    .build());

            factories.addStatement("factories.put($T.$N.key(), $T::new)",
                    registryKey, GenerationUtils.constantName(registryType.path(), true), value);
        }

        type.addMethod(MethodSpec.methodBuilder("factories")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(factoriesType)
                .addCode(factories.build())
                .addStatement("return $T.copyOf(factories)", Map.class)
                .build());

        JavaFile.builder(valuesPackage, type.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(outputDirectory);
    }
}
