package fr.euphyllia.fidorial.server.codecs.adventure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.euphyllia.fidorial.server.codecs.CommonCodecs;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Optional;

import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.KEY_CODEC;
import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.UUID_CODEC;
import static fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs.COMPONENT_CODEC;
import static fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs.COMPOUND_BINARY_TAG_HOLDER_CODEC;

public final class StyleCodecs {

    private StyleCodecs() {
    }

    public static final Codec<TextColor> COLOR_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                final TextColor color = s.startsWith("#") ? TextColor.fromHexString(s) : NamedTextColor.NAMES.value(s);
                return color != null
                        ? DataResult.success(color)
                        : DataResult.error(() -> "Unknown color: " + s);
            },
            color -> color instanceof final NamedTextColor named ? named.toString() : color.asHexString()
    );

    public static final Codec<ClickEvent<?>> CLICK_EVENT_CODEC = Codec.STRING.dispatch(
            "action",
            StyleCodecs::clickActionName,
            StyleCodecs::clickMapCodecFor
    );

    public static String clickActionName(final ClickEvent<?> event) {
        return switch (event.action()) {
            case ClickEvent.Action.OpenUrl _ -> "open_url";
            case ClickEvent.Action.OpenFile _ -> "open_file";
            case ClickEvent.Action.RunCommand _ -> "run_command";
            case ClickEvent.Action.SuggestCommand _ -> "suggest_command";
            case ClickEvent.Action.ChangePage _ -> "change_page";
            case ClickEvent.Action.CopyToClipboard _ -> "copy_to_clipboard";
            case ClickEvent.Action.Custom _ -> "custom";
            default -> throw new UnsupportedOperationException("Unsupported click action: " + event.action());
        };
    }

    public static MapCodec<? extends ClickEvent<?>> clickMapCodecFor(final String action) {
        return switch (action) {
            case "open_url" -> Codec.STRING.fieldOf("url")
                    .xmap(ClickEvent::openUrl, e -> ((ClickEvent.Payload.Text) e.payload()).value());
            case "open_file" -> Codec.STRING.fieldOf("path")
                    .xmap(ClickEvent::openFile, e -> ((ClickEvent.Payload.Text) e.payload()).value());
            case "run_command" -> Codec.STRING.fieldOf("command")
                    .xmap(ClickEvent::runCommand, e -> ((ClickEvent.Payload.Text) e.payload()).value());
            case "suggest_command" -> Codec.STRING.fieldOf("command")
                    .xmap(ClickEvent::suggestCommand, e -> ((ClickEvent.Payload.Text) e.payload()).value());
            case "change_page" -> CommonCodecs.POSITIVE_INT
                    .fieldOf("page")
                    .xmap(ClickEvent::changePage, e -> ((ClickEvent.Payload.Int) e.payload()).integer());
            case "copy_to_clipboard" -> Codec.STRING.fieldOf("value")
                    .xmap(ClickEvent::copyToClipboard, e -> ((ClickEvent.Payload.Text) e.payload()).value());
            case "custom" -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                    KEY_CODEC.fieldOf("id").forGetter(
                            e -> ((ClickEvent.Payload.Custom) e.payload()).key()),
                    COMPOUND_BINARY_TAG_HOLDER_CODEC.optionalFieldOf("payload").forGetter(e -> {
                        final BinaryTagHolder nbt = ((ClickEvent.Payload.Custom) e.payload()).nbt();
                        return Optional.ofNullable(nbt);
                    })
            ).apply(instance, (id, payload) -> ClickEvent.custom(id, payload.orElse(null))));
            default -> throw new IllegalArgumentException("Unsupported click action: " + action);
        };
    }

    public static final Codec<HoverEvent<?>> HOVER_EVENT_CODEC = Codec.STRING.dispatch(
            "action",
            StyleCodecs::hoverActionName,
            StyleCodecs::hoverMapCodecFor
    );

    private static String hoverActionName(final HoverEvent<?> event) {
        if (event.action() == HoverEvent.Action.SHOW_TEXT) return "show_text";
        if (event.action() == HoverEvent.Action.SHOW_ENTITY) return "show_entity";
        if (event.action() == HoverEvent.Action.SHOW_ITEM) return "show_item";
        throw new UnsupportedOperationException("Unsupported hover action: " + event.action());
    }

    private static MapCodec<? extends HoverEvent<?>> hoverMapCodecFor(final String action) {
        return switch (action) {
            case "show_text" -> COMPONENT_CODEC.fieldOf("value")
                    .xmap(HoverEvent::showText, HoverEvent::value);

            case "show_entity" -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                    KEY_CODEC.fieldOf("id").forGetter(
                            e -> ((HoverEvent.ShowEntity) e.value()).type()),
                    UUID_CODEC.fieldOf("uuid").forGetter(
                            e -> ((HoverEvent.ShowEntity) e.value()).id()),
                    COMPONENT_CODEC.optionalFieldOf("name").forGetter(
                            e -> Optional.ofNullable(((HoverEvent.ShowEntity) e.value()).name()))
            ).apply(instance, (id, uuid, name) ->
                    HoverEvent.showEntity(id, uuid, name.orElse(null))));

            case "show_item" -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                    KEY_CODEC.fieldOf("id").forGetter(
                            e -> ((HoverEvent.ShowItem) e.value()).item()),
                    Codec.INT.fieldOf("count").forGetter(
                            e -> ((HoverEvent.ShowItem) e.value()).count())
                    // TODO: data components.
            ).apply(instance, HoverEvent::showItem));

            default -> throw new IllegalArgumentException("Unsupported hover action: " + action);
        };
    }

    public static final MapCodec<Style> STYLE_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            COLOR_CODEC.optionalFieldOf("color").forGetter(s -> Optional.ofNullable(s.color())),
            Codec.INT.optionalFieldOf("shadow_color").forGetter(s ->
                    Optional.ofNullable(s.shadowColor()).map(ShadowColor::value)),
            Codec.BOOL.optionalFieldOf("bold").forGetter(s -> decorationOf(s, TextDecoration.BOLD)),
            Codec.BOOL.optionalFieldOf("italic").forGetter(s -> decorationOf(s, TextDecoration.ITALIC)),
            Codec.BOOL.optionalFieldOf("underlined").forGetter(s -> decorationOf(s, TextDecoration.UNDERLINED)),
            Codec.BOOL.optionalFieldOf("strikethrough").forGetter(s -> decorationOf(s, TextDecoration.STRIKETHROUGH)),
            Codec.BOOL.optionalFieldOf("obfuscated").forGetter(s -> decorationOf(s, TextDecoration.OBFUSCATED)),
            CLICK_EVENT_CODEC.optionalFieldOf("click_event").forGetter(s -> Optional.ofNullable(s.clickEvent())),
            HOVER_EVENT_CODEC.optionalFieldOf("hover_event").forGetter(s -> Optional.ofNullable(s.hoverEvent())),
            Codec.STRING.optionalFieldOf("insertion").forGetter(s -> Optional.ofNullable(s.insertion())),
            KEY_CODEC.optionalFieldOf("font").forGetter(s -> Optional.ofNullable(s.font()))
    ).apply(instance, StyleCodecs::buildStyle));

    private static Optional<Boolean> decorationOf(final Style style, final TextDecoration decoration) {
        final TextDecoration.State state = style.decoration(decoration);
        return state == TextDecoration.State.NOT_SET
                ? Optional.empty()
                : Optional.of(state == TextDecoration.State.TRUE);
    }

    private static Style buildStyle(
            final Optional<TextColor> color,
            final Optional<Integer> shadowColor,
            final Optional<Boolean> bold,
            final Optional<Boolean> italic,
            final Optional<Boolean> underlined,
            final Optional<Boolean> strikethrough,
            final Optional<Boolean> obfuscated,
            final Optional<ClickEvent<?>> clickEvent,
            final Optional<HoverEvent<?>> hoverEvent,
            final Optional<String> insertion,
            final Optional<Key> font
    ) {
        final Style.Builder builder = Style.style();
        color.ifPresent(builder::color);
        shadowColor.ifPresent(v -> builder.shadowColor(ShadowColor.shadowColor(v)));
        bold.ifPresent(v -> builder.decoration(TextDecoration.BOLD, v));
        italic.ifPresent(v -> builder.decoration(TextDecoration.ITALIC, v));
        underlined.ifPresent(v -> builder.decoration(TextDecoration.UNDERLINED, v));
        strikethrough.ifPresent(v -> builder.decoration(TextDecoration.STRIKETHROUGH, v));
        obfuscated.ifPresent(v -> builder.decoration(TextDecoration.OBFUSCATED, v));
        clickEvent.ifPresent(builder::clickEvent);
        hoverEvent.ifPresent(builder::hoverEvent);
        insertion.ifPresent(builder::insertion);
        font.ifPresent(builder::font);
        return builder.build();
    }
}
