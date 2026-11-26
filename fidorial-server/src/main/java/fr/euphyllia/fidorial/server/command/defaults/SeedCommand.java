package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.Nullable;

import static fr.fidorial.command.Commands.argument;
import static fr.fidorial.command.Commands.literal;
import static fr.fidorial.command.argument.Arguments.arguments;

/**
 * {@code /seed [world]}: the seed of the world of the sender, or of the given world.
 *
 * <p>As in vanilla, the seed is shown between brackets, in green, and is copied to the
 * clipboard when clicked.</p>
 */
public final class SeedCommand {

    private static final String PERMISSION = "fidorial.command.seed";
    private static final String WORLD = "world";

    private SeedCommand() {
        throw new UnsupportedOperationException("SeedCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("seed")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .executes(context -> seed(context.getSource(), worldOf(context.getSource())))
                .then(argument(WORLD, arguments().world())
                        .executes(context -> seed(context.getSource(),
                                (ServerWorld) context.getArgument(WORLD, World.class))))
                .build();
    }

    private static @Nullable ServerWorld worldOf(final CommandSource source) {
        final Location location = source.location();
        if (location != null && location.world() instanceof final ServerWorld world) {
            return world;
        }
        return FidorialServer.getInstance().worlds().manager().defaultWorld().orElse(null);
    }

    private static int seed(final CommandSource source, final @Nullable ServerWorld world) {
        if (world == null) {
            source.sendMessage(Component.translatable("commands.seed.no_world"));
            return 0;
        }
        source.sendMessage(Component.translatable("commands.seed.success", copyOnClick(String.valueOf(world.seed()))));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * {@return the text between brackets, green and copied to the clipboard on click}, like the
     * {@code ComponentUtils.copyOnClickText} of vanilla
     */
    private static Component copyOnClick(final String text) {
        return Component.text()
                .append(Component.text("["))
                .append(Component.text(text, NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.copyToClipboard(text))
                        .hoverEvent(HoverEvent.showText(Component.translatable("chat.copy.click")))
                        .insertion(text))
                .append(Component.text("]"))
                .build();
    }
}
