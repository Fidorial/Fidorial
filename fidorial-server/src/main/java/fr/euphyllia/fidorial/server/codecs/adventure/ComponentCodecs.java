package fr.euphyllia.fidorial.server.codecs.adventure;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.euphyllia.fidorial.server.codecs.DispatchCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.KEY_CODEC;
import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.UUID_CODEC;
import static fr.euphyllia.fidorial.server.codecs.adventure.StyleCodecs.STYLE_MAP_CODEC;

public final class ComponentCodecs {

    private ComponentCodecs() {
    }

    public static final Codec<Component> COMPONENT_CODEC =
            Codec.recursive("adventure:component", ComponentCodecs::createCodec);

    private static final MapCodec<TextComponent> TEXT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(TextComponent::content)
    ).apply(instance, Component::text));

    private static final Codec<TranslationArgument> ARG_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<TranslationArgument, T>> decode(final DynamicOps<T> ops, final T input) {
            final Optional<Number> number = ops.getNumberValue(input).result();
            if (number.isPresent()) {
                return DataResult.success(Pair.of(TranslationArgument.numeric(number.get()), input));
            }
            final Optional<String> string = ops.getStringValue(input).result();
            return string.map(s -> DataResult.success(Pair.of(TranslationArgument.component(Component.text(s)), input)))
                    .orElseGet(() -> COMPONENT_CODEC.decode(ops, input).map(pair -> pair.mapFirst(TranslationArgument::component)));
        }

        @Override
        public <T> DataResult<T> encode(final TranslationArgument input, final DynamicOps<T> ops, final T prefix) {
            final Object value = input.value();
            return switch (value) {
                case final Boolean b -> ops.mergeToPrimitive(prefix, ops.createBoolean(b));
                case final Number n -> ops.mergeToPrimitive(prefix, ops.createNumeric(n));
                default -> {
                    final Component component = input.asComponent();
                    final String collapsed = collapseToString(component);
                    yield collapsed != null
                            ? ops.mergeToPrimitive(prefix, ops.createString(collapsed))
                            : COMPONENT_CODEC.encode(component, ops, prefix);
                }
            };
        }
    };

    private static final MapCodec<TranslatableComponent> TRANSLATABLE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("translate").forGetter(TranslatableComponent::key),
            Codec.STRING.optionalFieldOf("fallback").forGetter(t -> Optional.ofNullable(t.fallback())),
            lenientListOf(ARG_CODEC).optionalFieldOf("with", List.of()).forGetter(TranslatableComponent::arguments)
    ).apply(instance, (key, fallback, with) ->
            Component.translatable(key).arguments(with).fallback(fallback.orElse(null))));

    private static final MapCodec<KeybindComponent> KEYBIND_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("keybind").forGetter(KeybindComponent::keybind)
    ).apply(instance, Component::keybind));

    private record ScoreData(String name, String objective) {
    }

    private static final Codec<ScoreData> SCORE_DATA_CODEC = RecordCodec.builder(ScoreData.class)
            .required("name", ScoreData::name, Codec.STRING)
            .required("objective", ScoreData::objective, Codec.STRING)
            .build();

    private static final MapCodec<ScoreComponent> SCORE_CODEC = SCORE_DATA_CODEC.fieldOf("score").xmap(
            d -> Component.score(d.name(), d.objective()),
            s -> new ScoreData(s.name(), s.objective()));

    private static final MapCodec<SelectorComponent> SELECTOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("selector").forGetter(SelectorComponent::pattern),
            COMPONENT_CODEC.optionalFieldOf("separator").forGetter(s -> Optional.ofNullable(s.separator()))
    ).apply(instance, (selector, separator) -> Component.selector(selector, separator.orElse(null))));

    private static final MapCodec<ObjectComponent> SPRITE_OBJECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KEY_CODEC.optionalFieldOf("atlas", SpriteObjectContents.DEFAULT_ATLAS)
                    .forGetter(o -> ((SpriteObjectContents) o.contents()).atlas()),
            KEY_CODEC.fieldOf("sprite").forGetter(o -> ((SpriteObjectContents) o.contents()).sprite())
    ).apply(instance, (atlas, sprite) -> Component.object(ObjectContents.sprite(atlas, sprite))));

    private static final Codec<PlayerHeadObjectContents.ProfileProperty> PROFILE_PROPERTY_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(PlayerHeadObjectContents.ProfileProperty::name),
                    Codec.STRING.fieldOf("value").forGetter(PlayerHeadObjectContents.ProfileProperty::value),
                    Codec.STRING.optionalFieldOf("signature").forGetter(p -> Optional.ofNullable(p.signature()))
            ).apply(instance, (name, value, signature) ->
                    PlayerHeadObjectContents.property(name, value, signature.orElse(null))));

    private static final Codec<PlayerHeadObjectContents> PLAYER_HEAD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(c -> Optional.ofNullable(c.name())),
            UUID_CODEC.optionalFieldOf("id").forGetter(c -> Optional.ofNullable(c.id())),
            PROFILE_PROPERTY_CODEC.listOf().optionalFieldOf("properties", List.of()).forGetter(PlayerHeadObjectContents::profileProperties),
            KEY_CODEC.optionalFieldOf("texture").forGetter(c -> Optional.ofNullable(c.texture()))
    ).apply(instance, (name, id, properties, texture) -> ObjectContents.playerHead()
            .name(name.orElse(null))
            .id(id.orElse(null))
            .profileProperties(properties)
            .texture(texture.orElse(null))
            .build()));

    private static final MapCodec<ObjectComponent> PLAYER_OBJECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PLAYER_HEAD_CODEC.fieldOf("player").forGetter(o -> (PlayerHeadObjectContents) o.contents()),
            Codec.BOOL.optionalFieldOf("hat", true).forGetter(o -> ((PlayerHeadObjectContents) o.contents()).hat())
    ).apply(instance, (contents, hat) -> Component.object(contents.toBuilder().hat(hat).build())));

    private static final MapCodec<ObjectComponent> OBJECT_CONTENTS_CODEC = DispatchCodecs.matcher("object", List.of(
            DispatchCodecs.Variant.of("atlas", null, true,
                    o -> o.contents() instanceof SpriteObjectContents, SPRITE_OBJECT_CODEC),
            DispatchCodecs.Variant.of("player", null, true,
                    o -> o.contents() instanceof PlayerHeadObjectContents, PLAYER_OBJECT_CODEC)));

    private static final MapCodec<ObjectComponent> OBJECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OBJECT_CONTENTS_CODEC.forGetter(Function.identity()),
            COMPONENT_CODEC.optionalFieldOf("fallback").forGetter(o -> Optional.ofNullable(o.fallback()))
    ).apply(instance, (object, fallback) -> fallback.map(object::fallback).orElse(object)));

    private static final MapCodec<BlockNBTComponent> BLOCK_NBT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("nbt").forGetter(BlockNBTComponent::nbtPath),
            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(BlockNBTComponent::interpret),
            COMPONENT_CODEC.optionalFieldOf("separator").forGetter(c -> Optional.ofNullable(c.separator())),
            Codec.STRING.fieldOf("block").forGetter(c -> c.pos().asString())
    ).apply(instance, (nbt, interpret, separator, block) -> Component.blockNBT()
            .nbtPath(nbt)
            .interpret(interpret)
            .separator(separator.orElse(null))
            .pos(BlockNBTComponent.Pos.fromString(block))
            .build()));

    private static final MapCodec<EntityNBTComponent> ENTITY_NBT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("nbt").forGetter(EntityNBTComponent::nbtPath),
            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(EntityNBTComponent::interpret),
            COMPONENT_CODEC.optionalFieldOf("separator").forGetter(c -> Optional.ofNullable(c.separator())),
            Codec.STRING.fieldOf("entity").forGetter(EntityNBTComponent::selector)
    ).apply(instance, (nbt, interpret, separator, entity) -> Component.entityNBT()
            .nbtPath(nbt)
            .interpret(interpret)
            .separator(separator.orElse(null))
            .selector(entity)
            .build()));

    private static final MapCodec<StorageNBTComponent> STORAGE_NBT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("nbt").forGetter(StorageNBTComponent::nbtPath),
            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(StorageNBTComponent::interpret),
            COMPONENT_CODEC.optionalFieldOf("separator").forGetter(c -> Optional.ofNullable(c.separator())),
            KEY_CODEC.fieldOf("storage").forGetter(StorageNBTComponent::storage)
    ).apply(instance, (nbt, interpret, separator, storage) -> Component.storageNBT()
            .nbtPath(nbt)
            .interpret(interpret)
            .separator(separator.orElse(null))
            .storage(storage)
            .build()));

    private static final MapCodec<NBTComponent<?>> NBT_CODEC = DispatchCodecs.matcher("source", List.of(
            DispatchCodecs.Variant.of("block", "block", false, BlockNBTComponent.class, BLOCK_NBT_CODEC),
            DispatchCodecs.Variant.of("entity", "entity", false, EntityNBTComponent.class, ENTITY_NBT_CODEC),
            DispatchCodecs.Variant.of("storage", "storage", false, StorageNBTComponent.class, STORAGE_NBT_CODEC)));

    private static final MapCodec<Component> CONTENT_CODEC = DispatchCodecs.matcher("type", List.of(
            DispatchCodecs.Variant.of("text", "text", false, TextComponent.class, TEXT_CODEC),
            DispatchCodecs.Variant.of("translatable", "translate", false, TranslatableComponent.class, TRANSLATABLE_CODEC),
            DispatchCodecs.Variant.of("keybind", "keybind", false, KeybindComponent.class, KEYBIND_CODEC),
            DispatchCodecs.Variant.of("score", "score", false, ScoreComponent.class, SCORE_CODEC),
            DispatchCodecs.Variant.of("selector", "selector", false, SelectorComponent.class, SELECTOR_CODEC),
            DispatchCodecs.Variant.of("nbt", "nbt", false, c -> c instanceof NBTComponent<?>, NBT_CODEC),
            DispatchCodecs.Variant.of("object", null, true, ObjectComponent.class, OBJECT_CODEC)));

    private static Codec<Component> createCodec(final Codec<Component> self) {
        final Codec<Component> direct = RecordCodecBuilder.create(instance -> instance.group(
                CONTENT_CODEC.forGetter(Function.identity()),
                lenientListOf(self).optionalFieldOf("extra", List.of()).forGetter(Component::children),
                STYLE_MAP_CODEC.forGetter(Component::style)
        ).apply(instance, (content, children, style) -> content.children(children).style(style)));

        return Codec.either(Codec.either(Codec.STRING, lenientListOf(self)), direct).xmap(
                either -> either.map(
                        stringOrList -> stringOrList.map(Component::text, ComponentCodecs::fromList),
                        Function.identity()),
                component -> {
                    final String collapsed = collapseToString(component);
                    return collapsed != null
                            ? Either.left(Either.left(collapsed))
                            : Either.right(component);
                });
    }

    private static Component fromList(final List<Component> list) {
        if (list.isEmpty()) return Component.empty();
        Component result = list.getFirst();
        for (int i = 1; i < list.size(); i++) {
            result = result.append(list.get(i));
        }
        return result;
    }

    private static @Nullable String collapseToString(final Component component) {
        if (component instanceof final TextComponent text
                && component.children().isEmpty()
                && component.style().isEmpty()) {
            return text.content();
        }
        return null;
    }

    private static <A> Codec<List<A>> lenientListOf(final Codec<A> elementCodec) {
        return Codec.of(
                new Encoder<>() {
                    @Override
                    public <T> DataResult<T> encode(final List<A> input, final DynamicOps<T> ops, final T prefix) {
                        final ListBuilder<T> builder = new ListBuilder.Builder<>(ops);
                        for (final A a : input) {
                            builder.add(elementCodec.encodeStart(ops, a));
                        }
                        return builder.build(prefix);
                    }
                },
                elementCodec.listOf()
        );
    }
}
