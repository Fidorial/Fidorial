package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.dialog.TestDialogs;
import fr.fidorial.command.CommandSource;
import fr.fidorial.dialog.Dialog;
import fr.fidorial.dialog.DialogAction;
import fr.fidorial.dialog.DialogActionButton;
import fr.fidorial.dialog.DialogBase;
import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.dialog.DialogRegistry;
import fr.fidorial.dialog.NoticeDialog;
import fr.fidorial.entity.Player;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;

/**
 * Drives the {@linkplain TestDialogs sample dialogs} from the chat bar.
 *
 * <pre>
 * /testdialog show &lt;name&gt;   open a sample, sent inline
 * /testdialog ref &lt;name&gt;    open the registered copy, sent as a reference
 * /testdialog close         close whatever dialog is on screen
 * /testdialog list          list the registry, with network ids
 * /testdialog chain         open a dialog whose button opens another one
 * </pre>
 *
 * <p>{@code show} and {@code ref} are worth comparing: the first ships the whole definition down
 * the wire and works even for a dialog nobody registered, the second sends a single identifier and
 * only works because the entry was registered before the player joined.</p>
 */
public final class DialogCommand {

    private static final SuggestionProvider<CommandSource> NAMES = (ctx, builder) -> {
        for (final String name : TestDialogs.names()) {
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    };

    private static TestPlugin plugin;

    /**
     * @param plugin the owning plugin
     */
    public DialogCommand(final TestPlugin plugin) {
        DialogCommand.plugin = plugin;
    }

    private static int show(final CommandContext<CommandSource> ctx, final String name) {
        final Player player = requirePlayer(ctx);
        if (player == null) {
            return 0;
        }

        final CommandSource source = ctx.getSource();
        final DialogDefinition dialog = TestDialogs.byName(name);
        if (dialog == null) {
            plugin.msg(source, "<red>Dialogue unknown : " + name);
            return 0;
        }

        // Sent in full: no registration needed, works at any time.
        player.showDialog(dialog);
        plugin.msg(source, "<green>Dialogue <white>" + name + "<green> sent inline.");
        return Command.SINGLE_SUCCESS;
    }

    private static int reference(final CommandContext<CommandSource> ctx, final String name) {
        final Player player = requirePlayer(ctx);
        if (player == null) {
            return 0;
        }

        final CommandSource source = ctx.getSource();
        final Key key = Key.key(TestDialogs.NAMESPACE, name);
        final DialogRegistry dialogs = plugin.server().dialogs();
        if (!dialogs.contains(key)) {
            plugin.msg(source, "<red>Nothing is recorded under" + key.asString());
            return 0;
        }

        // Sent as a single id: only works because the entry reached the client at configuration time.
        player.showDialog(Dialog.reference(key));
        plugin.msg(source, "<green>Reference <white>%s<green> sent (network id <aqua>%d<green>)."
                .formatted(key.asString(), dialogs.networkId(key)));
        return Command.SINGLE_SUCCESS;
    }

    private static int close(final CommandContext<CommandSource> ctx) {
        final Player player = requirePlayer(ctx);
        if (player == null) {
            return 0;
        }
        player.closeDialog();
        return Command.SINGLE_SUCCESS;
    }

    private static int list(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        final DialogRegistry dialogs = plugin.server().dialogs();

        plugin.msg(source, "<gold>%d dialogue(s) in the log:".formatted(dialogs.keys().size()));
        for (final Key key : dialogs.keys()) {
            final String origin = dialogs.definition(key).isPresent() ? "server" : "vanilla";
            plugin.msg(source, "<gray> - <white>%s <gray>(id <aqua>%d<gray>, %s)"
                    .formatted(key.asString(), dialogs.networkId(key), origin));
        }

        plugin.msg(source, "<gray>Break menu: <white>" + dialogs.pauseScreenAdditions());
        plugin.msg(source, "<gray>Quick actions:<white>" + dialogs.quickActions());
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Opens a dialog built on the spot whose button opens another one, to check that nested
     * definitions survive the round trip.
     */
    private static int chain(final CommandContext<CommandSource> ctx) {
        final Player player = requirePlayer(ctx);
        if (player == null) {
            return 0;
        }

        final NoticeDialog second = NoticeDialog.of(
                DialogBase.builder(Component.text("Second screen", NamedTextColor.AQUA))
                        .body(Component.text("Opened by the first one's button."))
                        .build());

        final NoticeDialog first = new NoticeDialog(
                DialogBase.builder(Component.text("First screen", NamedTextColor.GOLD))
                        .body(Component.text("The button below opens a nested dialog."))
                        .build(),
                DialogActionButton.of(Component.text("Continue"), DialogAction.showDialog(second)));

        player.showDialog(first);
        return Command.SINGLE_SUCCESS;
    }

    private static @Nullable Player requirePlayer(final CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        if (source.executor() instanceof final Player player) {
            return player;
        }
        plugin.msg(source, "<red>This command must be executed by a player.");
        return null;
    }

    /**
     * {@return the command tree}
     */
    public LiteralCommandNode<CommandSource> create() {
        return literal("testdialog")
                .then(literal("show")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(NAMES)
                                .executes(ctx -> show(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(literal("ref")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(NAMES)
                                .executes(ctx -> reference(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(literal("close").executes(DialogCommand::close))
                .then(literal("list").executes(DialogCommand::list))
                .then(literal("chain").executes(DialogCommand::chain))
                .build();
    }
}
