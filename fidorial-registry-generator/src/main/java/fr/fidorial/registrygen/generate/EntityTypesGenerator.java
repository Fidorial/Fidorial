package fr.fidorial.registrygen.generate;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import fr.fidorial.registrygen.GenerationUtils;
import fr.fidorial.registrygen.model.EntityCategories;
import fr.fidorial.registrygen.model.PrismarineEntityDefinition;
import fr.fidorial.registrygen.model.RegistryEntryDefinition;
import net.kyori.adventure.key.Key;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates the whole {@code EntityTypes} class: one constant per entry of
 * {@code minecraft:entity_type}, each carrying the network ID from Mojang's registry
 * report and the hitbox from PrismarineJS, plus the registry facade the server calls.
 *
 * <p>Only the spawn category resists generation — see {@link EntityCategories}.</p>
 *
 * @since 0.1.0
 */
public final class EntityTypesGenerator {

    private static final String CLASS_NAME = "EntityTypes";

    private static final float DEFAULT_WIDTH = 0.6f;
    private static final float DEFAULT_HEIGHT = 1.8f;

    private static final ClassName ENTITY_TYPE = ClassName.get("fr.fidorial.entity", "EntityType");
    private static final ClassName CATEGORY = ENTITY_TYPE.nestedClass("Category");
    private static final ClassName NULLABLE = ClassName.get("org.jspecify.annotations", "Nullable");

    /**
     * Generates the {@code EntityTypes} class.
     *
     * @param entities           Mojang's entity registry entries, which decide what exists
     *                           and what its network ID is
     * @param prismarineEntities Prismarine metadata keyed by plain entity name, for hitboxes
     * @param entityPackage      package the class is written into, e.g.
     *                           {@code fr.euphyllia.fidorial.server.entity}
     * @param outputDirectory    generated Java source root
     *
     * @throws IOException if the generated file cannot be written
     */
    public void generate(final List<RegistryEntryDefinition> entities,
                         final Map<String, PrismarineEntityDefinition> prismarineEntities,
                         final String entityPackage,
                         final Path outputDirectory) throws IOException {

        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(prismarineEntities, "prismarineEntities");
        Objects.requireNonNull(entityPackage, "entityPackage");
        Objects.requireNonNull(outputDirectory, "outputDirectory");

        final ParameterizedTypeName typeMap = ParameterizedTypeName.get(
                ClassName.get(Map.class), ClassName.get(Key.class), ENTITY_TYPE);

        final ParameterizedTypeName idMap = ParameterizedTypeName.get(
                ClassName.get(Map.class), ClassName.get(Key.class), ClassName.get(Integer.class));

        final TypeSpec.Builder type = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Every entity type of the targeted Minecraft version.\n\n")
                .addJavadoc("<p>Network IDs come from Mojang's registry report and hitboxes from\n")
                .addJavadoc("PrismarineJS's {@code minecraft-data}; do not edit. To change a spawn\n")
                .addJavadoc("category, edit {@code EntityCategories} in the registry generator and\n")
                .addJavadoc("regenerate.</p>\n")
                .addField(FieldSpec.builder(typeMap, "BY_KEY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", ConcurrentHashMap.class)
                        .build())
                .addField(FieldSpec.builder(idMap, "NETWORK_IDS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", ConcurrentHashMap.class)
                        .build());

        addConstants(type, entities, prismarineEntities);
        addFacade(type);

        JavaFile.builder(entityPackage, type.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(outputDirectory);
    }

    private static void addConstants(final TypeSpec.Builder type,
                                     final List<RegistryEntryDefinition> entities,
                                     final Map<String, PrismarineEntityDefinition> prismarineEntities) {

        for (final RegistryEntryDefinition entity : entities) {

            final String path = GenerationUtils.path(entity.identifier());
            final PrismarineEntityDefinition prismarine = prismarineEntities.get(path);

            final float width = (prismarine != null) ? prismarine.width() : DEFAULT_WIDTH;
            final float height = (prismarine != null) ? prismarine.height() : DEFAULT_HEIGHT;
            final String prismarineType = (prismarine != null) ? prismarine.type() : null;

            final String category = EntityCategories.categoryOf(path, prismarineType);

            if (EntityCategories.isDerived(path, prismarineType)) {
                System.out.println("Entity type " + entity.identifier() + ": spawn category derived as "
                        + category + "; add an EntityCategories override if that is wrong.");
            }

            if (prismarine == null) {
                System.out.println("Entity type " + entity.identifier()
                        + " is absent from Prismarine's entities report; using default dimensions.");
            }

            final FieldSpec.Builder field = FieldSpec
                    .builder(ENTITY_TYPE, GenerationUtils.constantName(entity.identifier()),
                            Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("{@code $L}, network ID {@code $L}.\n", entity.identifier(), entity.protocolId())
                    .initializer("vanilla($S, $T.$L, $L, $Lf, $Lf)",
                            path, CATEGORY, category, entity.protocolId(), width, height);

            type.addField(field.build());
        }
    }

    private static void addFacade(final TypeSpec.Builder type) {

        final ParameterSpec key = ParameterSpec.builder(ClassName.get(Key.class), "key", Modifier.FINAL).build();
        final ParameterSpec entityType = ParameterSpec.builder(ENTITY_TYPE, "type", Modifier.FINAL).build();

        type.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build());

        type.addMethod(MethodSpec.methodBuilder("vanilla")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(ENTITY_TYPE)
                .addParameter(ParameterSpec.builder(String.class, "name", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(CATEGORY, "category", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(int.class, "networkId", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(float.class, "width", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(float.class, "height", Modifier.FINAL).build())
                .addStatement("final $T key = $T.key(name)", Key.class, Key.class)
                .addStatement("final $T type = new $T(key, category, width, height)", ENTITY_TYPE, ENTITY_TYPE)
                .addStatement("register(type)")
                .addStatement("NETWORK_IDS.put(key, networkId)")
                .addStatement("return type")
                .build());

        type.addMethod(MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ENTITY_TYPE)
                .addParameter(entityType)
                .addJavadoc("Registers a type that no vanilla constant already claims.\n\n")
                .addJavadoc("@param type the type to register\n")
                .addJavadoc("@return the registered type\n")
                .addJavadoc("@throws IllegalStateException if the key is already taken\n")
                .addStatement("final $T previous = BY_KEY.putIfAbsent(type.key(), type)", ENTITY_TYPE)
                .beginControlFlow("if (previous != null)")
                .addStatement("throw new $T($S + type.key())", IllegalStateException.class,
                        "Entity type already registered : ")
                .endControlFlow()
                .addStatement("return type")
                .build());

        type.addMethod(MethodSpec.methodBuilder("registerCustom")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ENTITY_TYPE)
                .addParameter(entityType)
                .addParameter(ParameterSpec.builder(int.class, "networkId", Modifier.FINAL).build())
                .addJavadoc("Registers a plugin-defined type, rendered client-side as some vanilla type.\n\n")
                .addJavadoc("@param type      the synthetic type\n")
                .addJavadoc("@param networkId the network ID of the vanilla type it is rendered as\n")
                .addJavadoc("@return the registered type\n")
                .addStatement("BY_KEY.put(type.key(), type)")
                .addStatement("NETWORK_IDS.put(type.key(), networkId)")
                .addStatement("return type")
                .build());

        type.addMethod(MethodSpec.methodBuilder("unregister")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(key)
                .addStatement("NETWORK_IDS.remove(key)")
                .addStatement("return BY_KEY.remove(key) != null")
                .build());

        type.addMethod(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ENTITY_TYPE.annotated(AnnotationSpec.builder(NULLABLE).build()))
                .addParameter(key)
                .addStatement("return BY_KEY.get(key)")
                .build());

        type.addMethod(MethodSpec.methodBuilder("values")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Iterable.class), ENTITY_TYPE))
                .addStatement("return BY_KEY.values()")
                .build());

        type.addMethod(MethodSpec.methodBuilder("networkId")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.INT)
                .addParameter(entityType)
                .addJavadoc("@param type the entity type\n")
                .addJavadoc("@return the ID written by {@code add_entity}\n")
                .addJavadoc("@throws IllegalStateException if the type was never registered\n")
                .addStatement("final $T id = NETWORK_IDS.get(type.key())", Integer.class)
                .beginControlFlow("if (id == null)")
                .addStatement("throw new $T($S + type.key())", IllegalStateException.class,
                        "No network ID for the entity type ")
                .endControlFlow()
                .addStatement("return id")
                .build());

        type.addMethod(MethodSpec.methodBuilder("hasNetworkId")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(entityType)
                .addStatement("return NETWORK_IDS.containsKey(type.key())")
                .build());
    }
}
