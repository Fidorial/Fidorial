package fr.fidorial.dialog;

import fr.fidorial.annotation.SinceMinecraft;

/**
 * A complete dialog: its shared {@linkplain #base() base} plus the buttons specific to its type.
 *
 * @since 0.1.0
 */
@SinceMinecraft("1.21.6")
public sealed interface DialogDefinition extends Dialog
        permits NoticeDialog, ConfirmationDialog, MultiActionDialog, ServerLinksDialog, DialogListDialog {

    /**
     * {@return the title, contents and behaviour shared by every dialog type}
     *
     * @since 0.1.0
     */
    DialogBase base();
}
