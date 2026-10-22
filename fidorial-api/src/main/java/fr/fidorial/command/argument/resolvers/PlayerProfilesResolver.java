package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.player.profile.PlayerProfile;

import java.util.List;

/**
 * Resolves the profiles of the players matched by a selector or name.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public non-sealed interface PlayerProfilesResolver extends ArgumentResolver<List<PlayerProfile>> {

    /**
     * {@return the player profiles argument {@code name} matches for the source of {@code context}}
     *
     * @param context the command context
     * @param name    the argument name
     * @throws CommandSyntaxException if it can't be resolved for the source
     * @since 0.1.0
     */
    static List<PlayerProfile> playerProfiles(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        return context.getArgument(name, PlayerProfilesResolver.class).resolve(context.getSource());
    }
}
