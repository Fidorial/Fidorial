/**
 * Arguments resolved against the source running the command, such as selectors and relative or local positions.
 *
 * <p>Each resolver reads its argument with a static method named after its factory in
 * {@link Arguments}, like {@link PlayersResolver#players}.</p>
 *
 * @since 0.1.0
 */
package fr.fidorial.command.argument.resolvers;

import fr.fidorial.command.argument.Arguments;
