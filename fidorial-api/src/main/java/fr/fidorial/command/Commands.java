package fr.fidorial.command;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

/**
 * @since 0.1.0
 */
public interface Commands {

    /**
     * Creates a new {@link LiteralArgumentBuilder} of the required name.
     *
     * @param name the literal name
     * @return a new literal argument builder
     * @since 0.1.0
     */
    static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkArgument(name.indexOf(' ') == -1, "the argument name cannot contain spaces");

        return LiteralArgumentBuilder.literal(name);
    }

    /**
     * Creates a new {@link RequiredArgumentBuilder} of the required name and type.
     *
     * @param name         the argument name
     * @param argumentType the argument type required
     * @param <T>          the argument type
     * @return a new required argument builder
     * @since 0.1.0
     */
    static <T> RequiredArgumentBuilder<CommandSource, T> argument(
            final String name,
            final ArgumentType<T> argumentType
    ) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(argumentType, "argumentType");

        return RequiredArgumentBuilder.argument(name, argumentType);
    }
}
