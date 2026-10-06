package fr.euphyllia.fidorial.testplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.euphyllia.fidorial.testplugin.TestPlugin;
import fr.euphyllia.fidorial.testplugin.mob.BullMobs;
import fr.euphyllia.fidorial.testplugin.mob.CompanionMobs;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.math.Location;
import net.kyori.adventure.key.Key;

import java.util.Optional;

import static fr.fidorial.command.Commands.literal;

public final class CustomMobCommand {

    private final TestPlugin plugin;

    public CustomMobCommand(final TestPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSource> create() {
        return literal("testmob")
                .then(literal("bull").executes(this::bull))
                .then(literal("companion").executes(this::companion))
                .build();
    }

    private int bull(final CommandContext<CommandSource> ctx) {
        return summon(ctx, BullMobs.BULL);
    }

    private int companion(final CommandContext<CommandSource> ctx) {
        return summon(ctx, CompanionMobs.COMPANION);
    }

    private int summon(final CommandContext<CommandSource> ctx, final Key key) {
        final CommandSource source = ctx.getSource();
        final Location location = source.location();
        if (location == null) {
            plugin.msg(source, "<red>[TestPlugin] Aucun monde chargé.</red>");
            return 0;
        }

        final Optional<Mob> mob = plugin.server().mobs().spawn(key, location);
        if (mob.isEmpty()) {
            plugin.msg(source, "<red>[TestPlugin] Impossible d'invoquer <white>" + key.asString() + "<red>.");
            return 0;
        }

        plugin.msg(source, "<yellow>[TestPlugin] Invocation de <white>" + key.asString() + "<yellow>...");
        return Command.SINGLE_SUCCESS;
    }
}
