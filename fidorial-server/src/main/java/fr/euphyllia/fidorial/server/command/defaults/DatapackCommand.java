package fr.euphyllia.fidorial.server.command.defaults;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.fidorial.command.CommandSource;
import fr.fidorial.world.structure.Datapack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.ArrayList;
import java.util.List;

import static fr.fidorial.command.Commands.literal;

/**
 * {@code /datapack list|reload} for the packs of {@code <world>/datapacks}.
 */
public final class DatapackCommand {

    private static final String PERMISSION = "fidorial.command.datapack";

    private DatapackCommand() {
        throw new UnsupportedOperationException("DatapackCommand cannot be instantiated.");
    }

    public static LiteralCommandNode<CommandSource> create() {
        return literal("datapack")
                .requires(source -> source.sender().hasPermission(PERMISSION))
                .then(literal("list").executes(DatapackCommand::list))
                .then(literal("reload").executes(DatapackCommand::reload))
                .build();
    }

    private static int list(final CommandContext<CommandSource> context) {
        final StructureService service = StructureCommands.service();
        final List<Datapack> packs = service.datapacks();
        if (packs.isEmpty()) {
            context.getSource().sender().sendMessage(Component.translatable("command.datapack.list.none",
                    Component.text(service.folder().toString())));
            return 0;
        }
        final List<Component> names = new ArrayList<>(packs.size());
        for (final Datapack pack : packs) {
            final Component details = Component.text()
                    .append(description(pack))
                    .append(Component.newline())
                    .append(Component.translatable("command.datapack.list.details",
                            Component.text(pack.templates()), Component.text(pack.structures()), Component.text(pack.structureSets())))
                    .build();
            names.add(Component.text("[" + pack.id() + "]", NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(details)));
        }
        context.getSource().sender().sendMessage(Component.translatable("command.datapack.list",
                Component.text(packs.size()), Component.join(JoinConfiguration.commas(true), names)));
        return packs.size();
    }

    private static Component description(final Datapack pack) {
        try {
            return GsonComponentSerializer.gson().deserialize(pack.descriptionJson());
        } catch (final RuntimeException invalid) {
            return Component.text(pack.descriptionJson());
        }
    }

    private static int reload(final CommandContext<CommandSource> context) {
        final StructureService service = StructureCommands.service();
        context.getSource().sender().sendMessage(Component.translatable("command.datapack.reload.start"));
        service.reload().whenComplete((_, failure) -> {
            if (failure != null) {
                context.getSource().sender().sendMessage(Component.translatable("command.datapack.reload.failed",
                        Component.text(StructureCommands.rootMessage(failure))));
                return;
            }
            context.getSource().sender().sendMessage(Component.translatable("command.datapack.reload.success",
                    Component.text(service.datapacks().size()),
                    Component.text(service.structures().size()),
                    Component.text(service.registry().structureSets().size())));
        });
        return Command.SINGLE_SUCCESS;
    }
}
