package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.PrismarineEntityDefinition;
import fr.fidorial.registrygen.model.RegistryEntryDefinition;
import net.kyori.adventure.key.Key;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates {@code EntityProperties} — per-entity hitbox dimensions — by joining
 * Mojang's entity registry against PrismarineJS's entities report.
 *
 * <p>Mojang's {@code registries.json} lists which entity types exist and what their
 * wire IDs are, but not how big they are; Prismarine supplies the hitbox. Mojang
 * remains the authority on existence: an entity absent from the Prismarine report
 * simply falls back to the default dimensions rather than disappearing.</p>
 *
 * @since 0.1.0
 */
public final class EntityPropertiesGenerator {

    private static final String CLASS_NAME = "EntityProperties";

    private static final float DEFAULT_WIDTH = 0.6f;
    private static final float DEFAULT_HEIGHT = 1.8f;

    /**
     * Registrations per generated method, to stay clear of the JVM's 64 KB
     * per-method body limit. See {@code ItemPropertiesGenerator}.
     */
    private static final int ENTITIES_PER_METHOD = 200;

    private static final ClassName OBJECT_2_FLOAT_OPEN_HASH_MAP =
            ClassName.get("it.unimi.dsi.fastutil.objects", "Object2FloatOpenHashMap");

    /**
     * Generates the {@code EntityProperties} class.
     *
     * @param entities            Mojang's entity registry entries, which decide what exists
     * @param prismarineEntities  Prismarine metadata keyed by plain entity name
     * @param registryDataPackage package the class is written into
     * @param outputDirectory     generated Java source root
     *
     * @throws IOException if the generated file cannot be written
     */
    public void generate(final List<RegistryEntryDefinition> entities,
                         final Map<String, PrismarineEntityDefinition> prismarineEntities,
                         final String registryDataPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(prismarineEntities, "prismarineEntities");
        Objects.requireNonNull(registryDataPackage, "registryDataPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final ParameterizedTypeName floatMapType =
                ParameterizedTypeName.get(OBJECT_2_FLOAT_OPEN_HASH_MAP, ClassName.get(Key.class));

        final TypeSpec.Builder type = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Per-entity hitbox dimensions.\n\n")
                .addJavadoc("<p>Joined from Mojang's entity registry report and PrismarineJS's\n")
                .addJavadoc("{@code minecraft-data} entities report; do not edit.</p>\n\n")
                .addJavadoc("<p>These are the type's <em>defaults</em>. An individual entity may\n")
                .addJavadoc("scale them (a baby, a slime's size, a sitting pose), so read the\n")
                .addJavadoc("entity's own dimensions when you have one in hand.</p>\n")
                .addField(FieldSpec.builder(floatMapType, "WIDTH", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", OBJECT_2_FLOAT_OPEN_HASH_MAP)
                        .build())
                .addField(FieldSpec.builder(floatMapType, "HEIGHT", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", OBJECT_2_FLOAT_OPEN_HASH_MAP)
                        .build())
                .addStaticBlock(CodeBlock.builder()
                        .addStatement("WIDTH.defaultReturnValue($Lf)", DEFAULT_WIDTH)
                        .addStatement("HEIGHT.defaultReturnValue($Lf)", DEFAULT_HEIGHT)
                        .build())
                .addMethod(privateConstructor())
                .addMethod(floatAccessor("width", "WIDTH", DEFAULT_WIDTH, "hitbox width in blocks"))
                .addMethod(floatAccessor("height", "HEIGHT", DEFAULT_HEIGHT, "hitbox height in blocks"))
                .addMethod(registerHelper());

        addRegistrationMethods(type, entities, prismarineEntities);

        JavaFile.builder(registryDataPackage, type.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(outputDirectory);
    }

    private static MethodSpec privateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("throw new $T($S)", UnsupportedOperationException.class,
                        CLASS_NAME + " cannot be instantiated.")
                .build();
    }

    private static MethodSpec floatAccessor(final String methodName,
                                            final String fieldName,
                                            final float defaultValue,
                                            final String description) {

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.FLOAT)
                .addParameter(ParameterSpec.builder(ClassName.get(Key.class), "entity", Modifier.FINAL).build())
                .addJavadoc("@param entity namespaced entity type identifier\n")
                .addJavadoc("@return " + description + "; {@code " + defaultValue
                        + "} when the entity type has no recorded data\n")
                .addStatement("return $N.getFloat($N)", fieldName, "entity")
                .build();
    }

    private static MethodSpec registerHelper() {
        return MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(ClassName.get(Key.class), "entity", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(float.class, "width", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(float.class, "height", Modifier.FINAL).build())
                .addStatement("WIDTH.put($N, $N)", "entity", "width")
                .addStatement("HEIGHT.put($N, $N)", "entity", "height")
                .build();
    }

    private static void addRegistrationMethods(final TypeSpec.Builder type,
                                               final List<RegistryEntryDefinition> entities,
                                               final Map<String, PrismarineEntityDefinition> prismarineEntities) {

        final List<MethodSpec> methods = new ArrayList<>();

        MethodSpec.Builder current = null;
        int inCurrent = 0;

        for (final RegistryEntryDefinition entity : entities) {

            final String path = GenerationUtils.path(entity.identifier());
            final PrismarineEntityDefinition prismarine = prismarineEntities.get(path);

            if (prismarine == null) {
                continue; // Prismarine hasn't caught up with this Minecraft version yet
            }

            if (current == null) {
                current = MethodSpec.methodBuilder("registerEntities" + methods.size())
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC);
            }

            current.addStatement("register($T.key($S), $Lf, $Lf)",
                    Key.class,
                    path,
                    prismarine.width(),
                    prismarine.height());

            if (++inCurrent >= ENTITIES_PER_METHOD) {
                methods.add(current.build());
                current = null;
                inCurrent = 0;
            }
        }

        if (current != null) {
            methods.add(current.build());
        }

        methods.forEach(type::addMethod);

        final MethodSpec.Builder bootstrap = MethodSpec.methodBuilder("bootstrap")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("Fills the lookup tables. Idempotent, and cheap enough to call\n")
                .addJavadoc("from the static initialiser of whatever needs it first.\n");

        for (final MethodSpec method : methods) {
            bootstrap.addStatement("$N()", method.name());
        }

        type.addMethod(bootstrap.build());
    }
}
