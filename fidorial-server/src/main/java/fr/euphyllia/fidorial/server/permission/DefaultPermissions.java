package fr.euphyllia.fidorial.server.permission;

import fr.fidorial.permission.PermissionDefinition;
import fr.fidorial.permission.PermissionRegistry;

import java.util.List;

/**
 * Declares the permissions shipped with the server.
 */
public final class DefaultPermissions {

    public static final String ROOT = "fidorial";
    public static final String COMMAND_ROOT = ROOT + ".command";

    private DefaultPermissions() {
    }

    /**
     * Declares the built-in permissions.
     *
     * @param registry the server permission registry
     */
    public static void register(final PermissionRegistry registry) {
        registry.defineAll(List.of(
                PermissionDefinition.explicitOnly(ROOT + ".*", "Every Fidorial feature."),
                PermissionDefinition.explicitOnly(COMMAND_ROOT + ".*", "Every Fidorial command."),
                command("ban", "Ban a player, permanently or for a set time."),
                command("banip", "Ban an IP, permanently or for a set time."),
                command("banlist", "List the banned players."),
                command("bossbar", "Create a bossbar."),
                command("datapack", "List and reload the datapacks of the world."),
                command("fillbiome", "Repaint the biomes of a region."),
                command("gamemode", "Change the game mode."),
                command("locate", "Find the nearest datapack structure."),
                command("op", "Promote a player to operator."),
                command("deop", "Remove operator status from a player."),
                command("pardon", "Lift the ban on a player."),
                command("pardonip", "Lift the ban on an IP."),
                command("place", "Place a structure template, a jigsaw structure or a jigsaw pool."),
                command("respawn", "Respawns players stuck on the death screen, without waiting for them to click the button."),
                command("spawnpoint", "Sets where players respawn, in the world they are currently in."),
                command("stop", "Stop the server."),
                command("summon", "Summon an entity."),
                command("time", "Change the time of a world."),
                command("tps", "View per-region TPS."),
                command("weather", "Change the weather."),
                command("whitelist", "Manage the whitelist."),
                PermissionDefinition.operatorOnly("spark", "Use the built-in spark profiler."),
                PermissionDefinition.operatorOnly("spark.*", "Every spark subcommand."),
                PermissionDefinition.operatorOnly(
                        "minecraft.command.selector", "Use entity selectors (@a, @p, @e, @s).")));
    }

    private static PermissionDefinition command(final String name, final String description) {
        return PermissionDefinition.operatorOnly(COMMAND_ROOT + "." + name, description);
    }
}
