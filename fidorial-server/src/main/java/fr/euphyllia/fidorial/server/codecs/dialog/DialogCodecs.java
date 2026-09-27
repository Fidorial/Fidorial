package fr.euphyllia.fidorial.server.codecs.dialog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import fr.euphyllia.fidorial.server.codecs.DispatchCodecs;
import fr.euphyllia.fidorial.server.codecs.RecordCodec;
import fr.euphyllia.fidorial.server.codecs.adventure.StyleCodecs;
import fr.fidorial.dialog.ConfirmationDialog;
import fr.fidorial.dialog.Dialog;
import fr.fidorial.dialog.DialogAction;
import fr.fidorial.dialog.DialogActionButton;
import fr.fidorial.dialog.DialogAfterAction;
import fr.fidorial.dialog.DialogBase;
import fr.fidorial.dialog.DialogBody;
import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.dialog.DialogInput;
import fr.fidorial.dialog.DialogListDialog;
import fr.fidorial.dialog.DialogReference;
import fr.fidorial.dialog.MultiActionDialog;
import fr.fidorial.dialog.NoticeDialog;
import fr.fidorial.dialog.ServerLinksDialog;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;
import java.util.Objects;

import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.KEY_CODEC;
import static fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs.COMPONENT_CODEC;
import static fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs.COMPOUND_BINARY_TAG_CODEC;


public final class DialogCodecs {

    public static final Codec<DialogAction> ACTION_CODEC = Codec.STRING.dispatch(
            "type",
            DialogCodecs::actionName,
            DialogCodecs::actionMapCodecFor);

    public static final Codec<DialogBody> BODY_CODEC;
    public static final Codec<DialogInput> INPUT_CODEC;
    public static final Codec<DialogDefinition> DEFINITION_CODEC;
    public static final Codec<Dialog> DIALOG_CODEC;

    private static final Codec<DialogAfterAction> AFTER_ACTION_CODEC =
            Codec.STRING.comapFlatMap(DialogCodecs::afterActionById, DialogAfterAction::id);

    private static final Codec<DialogActionButton> BUTTON_CODEC = RecordCodec.builder(DialogActionButton.class)
            .required("label", DialogActionButton::label, COMPONENT_CODEC)
            .nullable("tooltip", DialogActionButton::tooltip, COMPONENT_CODEC)
            .field("width", DialogActionButton::width, Codec.intRange(1, DialogActionButton.MAX_WIDTH), DialogActionButton.DEFAULT_WIDTH)
            .nullable("action", DialogActionButton::action, ACTION_CODEC)
            .build();

    private DialogCodecs() {
        throw new UnsupportedOperationException("DialogCodecs cannot be instantiated.");
    }

    private static final MapCodec<DialogBody.PlainMessage> PLAIN_MESSAGE_CODEC = RecordCodec.builder(DialogBody.PlainMessage.class)
            .required("contents", DialogBody.PlainMessage::contents, COMPONENT_CODEC)
            .field("width", DialogBody.PlainMessage::width, Codec.intRange(1, DialogBody.MAX_WIDTH), DialogBody.DEFAULT_WIDTH)
            .buildMap();

    private static final MapCodec<DialogBody.Item> ITEM_BODY_CODEC = RecordCodec.builder(DialogBody.Item.class)
            .required("item", DialogBody.Item::item, DialogItemCodecs.ITEM_STACK_CODEC)
            .nullable("description", DialogBody.Item::description, PLAIN_MESSAGE_CODEC.codec())
            .field("show_decoration", DialogBody.Item::showDecoration, Codec.BOOL, true)
            .field("show_tooltip", DialogBody.Item::showTooltip, Codec.BOOL, true)
            .field("width", DialogBody.Item::width, Codec.intRange(1, DialogBody.Item.MAX_ITEM_SIZE), DialogBody.Item.DEFAULT_ITEM_SIZE)
            .field("height", DialogBody.Item::height, Codec.intRange(1, DialogBody.Item.MAX_ITEM_SIZE), DialogBody.Item.DEFAULT_ITEM_SIZE)
            .buildMap();

    private static final Codec<DialogInput.Text.Multiline> MULTILINE_CODEC = RecordCodec.builder(DialogInput.Text.Multiline.class)
            .nullable("max_lines", DialogInput.Text.Multiline::maxLines, Codec.INT)
            .nullable("height", DialogInput.Text.Multiline::height, Codec.intRange(1, DialogInput.Text.Multiline.MAX_HEIGHT))
            .build();

    private static final MapCodec<DialogInput.Text> TEXT_INPUT_CODEC = RecordCodec.builder(DialogInput.Text.class)
            .required("key", DialogInput.Text::key, Codec.STRING)
            .required("label", DialogInput.Text::label, COMPONENT_CODEC)
            .field("width", DialogInput.Text::width, Codec.intRange(1, DialogInput.MAX_WIDTH), DialogInput.DEFAULT_WIDTH)
            .field("label_visible", DialogInput.Text::labelVisible, Codec.BOOL, true)
            .field("initial", DialogInput.Text::initial, Codec.STRING, "")
            .field("max_length", DialogInput.Text::maxLength, Codec.INT, DialogInput.Text.DEFAULT_MAX_LENGTH)
            .nullable("multiline", DialogInput.Text::multiline, MULTILINE_CODEC)
            .buildMap();

    private static final MapCodec<DialogInput.Bool> BOOL_INPUT_CODEC = RecordCodec.builder(DialogInput.Bool.class)
            .required("key", DialogInput.Bool::key, Codec.STRING)
            .required("label", DialogInput.Bool::label, COMPONENT_CODEC)
            .field("initial", DialogInput.Bool::initial, Codec.BOOL, false)
            .field("on_true", DialogInput.Bool::onTrue, Codec.STRING, "true")
            .field("on_false", DialogInput.Bool::onFalse, Codec.STRING, "false")
            .buildMap();

    private static final Codec<DialogInput.SingleOption.Entry> OPTION_ENTRY_CODEC = RecordCodec.builder(DialogInput.SingleOption.Entry.class)
            .required("id", DialogInput.SingleOption.Entry::id, Codec.STRING)
            .nullable("display", DialogInput.SingleOption.Entry::display, COMPONENT_CODEC)
            .field("initial", DialogInput.SingleOption.Entry::initial, Codec.BOOL, false)
            .build();

    private static final MapCodec<DialogInput.SingleOption> SINGLE_OPTION_INPUT_CODEC = RecordCodec.builder(DialogInput.SingleOption.class)
            .required("key", DialogInput.SingleOption::key, Codec.STRING)
            .required("label", DialogInput.SingleOption::label, COMPONENT_CODEC)
            .required("options", DialogInput.SingleOption::options, OPTION_ENTRY_CODEC.listOf())
            .field("label_visible", DialogInput.SingleOption::labelVisible, Codec.BOOL, true)
            .field("width", DialogInput.SingleOption::width, Codec.intRange(1, DialogInput.MAX_WIDTH), DialogInput.DEFAULT_WIDTH)
            .buildMap();

    private static final MapCodec<DialogInput.NumberRange> NUMBER_RANGE_INPUT_CODEC = RecordCodec.builder(DialogInput.NumberRange.class)
            .required("key", DialogInput.NumberRange::key, Codec.STRING)
            .required("label", DialogInput.NumberRange::label, COMPONENT_CODEC)
            .field("label_format", DialogInput.NumberRange::labelFormat, Codec.STRING, DialogInput.NumberRange.DEFAULT_LABEL_FORMAT)
            .field("width", DialogInput.NumberRange::width, Codec.intRange(1, DialogInput.MAX_WIDTH), DialogInput.DEFAULT_WIDTH)
            .required("start", DialogInput.NumberRange::start, Codec.FLOAT)
            .required("end", DialogInput.NumberRange::end, Codec.FLOAT)
            .nullable("step", DialogInput.NumberRange::step, Codec.FLOAT)
            .nullable("initial", DialogInput.NumberRange::initial, Codec.FLOAT)
            .buildMap();

    static {
        BODY_CODEC = DispatchCodecs.<DialogBody>matcher("type", List.of(
                DispatchCodecs.Variant.of(
                        "plain_message", null, true, DialogBody.PlainMessage.class, PLAIN_MESSAGE_CODEC),
                DispatchCodecs.Variant.of(
                        "item", null, true, DialogBody.Item.class, ITEM_BODY_CODEC)
        )).codec();

        INPUT_CODEC = DispatchCodecs.<DialogInput>matcher("type", List.of(
                DispatchCodecs.Variant.of(
                        "text", null, true, DialogInput.Text.class, TEXT_INPUT_CODEC),
                DispatchCodecs.Variant.of(
                        "boolean", null, true, DialogInput.Bool.class, BOOL_INPUT_CODEC),
                DispatchCodecs.Variant.of(
                        "single_option", null, true,
                        DialogInput.SingleOption.class, SINGLE_OPTION_INPUT_CODEC),
                DispatchCodecs.Variant.of(
                        "number_range", null, true,
                        DialogInput.NumberRange.class, NUMBER_RANGE_INPUT_CODEC)
        )).codec();

        DEFINITION_CODEC = Codec.recursive("fidorial:dialog", self -> {
            final Codec<Dialog> dialog = eitherReferenceOr(self);
            return DispatchCodecs.<DialogDefinition>matcher("type", List.of(
                    DispatchCodecs.Variant.of(
                            "notice", null, true, NoticeDialog.class, noticeCodec()),
                    DispatchCodecs.Variant.of(
                            "confirmation", null, true,
                            ConfirmationDialog.class, confirmationCodec()),
                    DispatchCodecs.Variant.of(
                            "multi_action", null, true,
                            MultiActionDialog.class, multiActionCodec()),
                    DispatchCodecs.Variant.of(
                            "server_links", null, true,
                            ServerLinksDialog.class, serverLinksCodec()),
                    DispatchCodecs.Variant.of(
                            "dialog_list", null, true,
                            DialogListDialog.class, dialogListCodec(dialog))
            )).codec();
        });

        DIALOG_CODEC = eitherReferenceOr(DEFINITION_CODEC);
    }

    private static MapCodec<DialogBase> baseCodec() {
        return RecordCodec.builder(DialogBase.class)
                .required("title", DialogBase::title, COMPONENT_CODEC)
                .nullable("external_title", DialogBase::externalTitle, COMPONENT_CODEC)
                .field("body", DialogBase::body, bodyListCodec(), List.of())
                .field("inputs", DialogBase::inputs, INPUT_CODEC.listOf(), List.of())
                .field("can_close_with_escape", DialogBase::canCloseWithEscape, Codec.BOOL, true)
                .field("pause", DialogBase::pause, Codec.BOOL, true)
                .field("after_action", DialogBase::afterAction, AFTER_ACTION_CODEC, DialogAfterAction.CLOSE)
                .buildMap();
    }

    private static MapCodec<NoticeDialog> noticeCodec() {
        return RecordCodec.builder(NoticeDialog.class)
                .inline(NoticeDialog::base, baseCodec())
                .field("action", NoticeDialog::action, BUTTON_CODEC, NoticeDialog.DEFAULT_ACTION)
                .buildMap();
    }

    private static MapCodec<ConfirmationDialog> confirmationCodec() {
        return RecordCodec.builder(ConfirmationDialog.class)
                .inline(ConfirmationDialog::base, baseCodec())
                .required("yes", ConfirmationDialog::yes, BUTTON_CODEC)
                .required("no", ConfirmationDialog::no, BUTTON_CODEC)
                .buildMap();
    }

    private static MapCodec<MultiActionDialog> multiActionCodec() {
        return RecordCodec.builder(MultiActionDialog.class)
                .inline(MultiActionDialog::base, baseCodec())
                .required("actions", MultiActionDialog::actions, BUTTON_CODEC.listOf())
                .field("columns", MultiActionDialog::columns, Codec.INT, MultiActionDialog.DEFAULT_COLUMNS)
                .nullable("exit_action", MultiActionDialog::exitAction, BUTTON_CODEC)
                .buildMap();
    }

    private static MapCodec<ServerLinksDialog> serverLinksCodec() {
        return RecordCodec.builder(ServerLinksDialog.class)
                .inline(ServerLinksDialog::base, baseCodec())
                .nullable("exit_action", ServerLinksDialog::exitAction, BUTTON_CODEC)
                .field("columns", ServerLinksDialog::columns, Codec.INT, ServerLinksDialog.DEFAULT_COLUMNS)
                .field("button_width", ServerLinksDialog::buttonWidth,
                        Codec.intRange(1, DialogActionButton.MAX_WIDTH), DialogActionButton.DEFAULT_WIDTH)
                .buildMap();
    }

    private static MapCodec<DialogListDialog> dialogListCodec(final Codec<Dialog> nested) {
        return RecordCodec.builder(DialogListDialog.class)
                .inline(DialogListDialog::base, baseCodec())
                .required("dialogs", DialogListDialog::dialogs, nested.listOf())
                .nullable("exit_action", DialogListDialog::exitAction, BUTTON_CODEC)
                .field("columns", DialogListDialog::columns, Codec.INT, DialogListDialog.DEFAULT_COLUMNS)
                .field("button_width", DialogListDialog::buttonWidth,
                        Codec.intRange(1, DialogActionButton.MAX_WIDTH), DialogActionButton.DEFAULT_WIDTH)
                .buildMap();
    }

    private static Codec<List<DialogBody>> bodyListCodec() {
        return Codec.either(BODY_CODEC.listOf(), BODY_CODEC).xmap(
                either -> either.<List<DialogBody>>map(list -> list, List::of),
                Either::left);
    }

    private static Codec<Dialog> eitherReferenceOr(final Codec<DialogDefinition> definition) {
        return Codec.either(KEY_CODEC, definition).xmap(
                either -> either.<Dialog>map(Dialog::reference, dialog -> dialog),
                dialog -> {
                    if (dialog instanceof final DialogReference reference) {
                        return Either.left(reference.key());
                    }
                    return Either.right((DialogDefinition) dialog);
                });
    }

    private static String actionName(final DialogAction action) {
        return switch (action) {
            case final DialogAction.Static value -> StyleCodecs.clickActionName(value.event());
            case DialogAction.ShowDialog _ -> "show_dialog";
            case DialogAction.DynamicRunCommand _ -> "dynamic/run_command";
            case DialogAction.DynamicCustom _ -> "dynamic/custom";
        };
    }

    private static MapCodec<? extends DialogAction> actionMapCodecFor(final String type) {
        return switch (type) {
            case "show_dialog" -> showDialogCodec();
            case "dynamic/run_command" -> dynamicRunCommandCodec();
            case "dynamic/custom" -> dynamicCustomCodec();
            default -> staticActionCodec(type);
        };
    }

    private static MapCodec<DialogAction.ShowDialog> showDialogCodec() {
        return RecordCodec.builder(DialogAction.ShowDialog.class)
                .required("dialog", DialogAction.ShowDialog::dialog, DIALOG_CODEC)
                .buildMap();
    }

    private static MapCodec<DialogAction.DynamicRunCommand> dynamicRunCommandCodec() {
        return RecordCodec.builder(DialogAction.DynamicRunCommand.class)
                .required("template", DialogAction.DynamicRunCommand::template, Codec.STRING)
                .buildMap();
    }

    private static MapCodec<DialogAction.DynamicCustom> dynamicCustomCodec() {
        return RecordCodec.builder(DialogAction.DynamicCustom.class)
                .required("id", DialogAction.DynamicCustom::id, KEY_CODEC)
                .nullable("additions", DialogAction.DynamicCustom::additions, COMPOUND_BINARY_TAG_CODEC)
                .buildMap();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MapCodec<DialogAction> staticActionCodec(final String type) {
        final MapCodec raw = StyleCodecs.clickMapCodecFor(type);
        final MapCodec<ClickEvent<?>> codec = (MapCodec<ClickEvent<?>>) raw;
        return codec.xmap(DialogAction::of, action -> ((DialogAction.Static) action).event());
    }

    private static DataResult<DialogAfterAction> afterActionById(final String id) {
        for (final DialogAfterAction value : DialogAfterAction.values()) {
            if (value.id().equals(id)) {
                return DataResult.success(value);
            }
        }
        return DataResult.error(() -> "Unknown after_action: " + id);
    }

    public static CompoundBinaryTag toNbt(final DialogDefinition dialog) {
        Objects.requireNonNull(dialog, "dialog");
        final BinaryTag tag = DEFINITION_CODEC
                .encodeStart(BinaryTagOps.binaryTagOps(), dialog)
                .getOrThrow(message -> new IllegalArgumentException("Failed to encode dialog: " + message));
        if (!(tag instanceof final CompoundBinaryTag compound)) {
            throw new IllegalArgumentException("A dialog must encode to a compound tag, got " + tag.type());
        }
        return compound;
    }

    public static DialogDefinition fromJson(final Key key, final String json) {
        final JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (final RuntimeException exception) {
            throw new IllegalArgumentException("Dialog " + key.asString() + " is not valid JSON", exception);
        }

        return DEFINITION_CODEC
                .parse(JsonOps.INSTANCE, element)
                .getOrThrow(message -> new IllegalArgumentException(
                        "Failed to read dialog " + key.asString() + ": " + message));
    }
}
