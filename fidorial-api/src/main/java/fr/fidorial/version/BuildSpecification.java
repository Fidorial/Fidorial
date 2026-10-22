package fr.fidorial.version;

import java.util.OptionalInt;

/**
 * The build of the running server.
 *
 * @param minecraftVersion the Minecraft version implemented
 * @param protocolVersion  the protocol version clients must use to join
 * @param apiVersion       the Fidorial API version, for instance {@code 0.1.0}
 * @param version          the full server version, for instance {@code 26.3-build.42+0.1.0}
 * @param buildNumber      the CI build number, empty for a local build
 * @since 0.1.0
 */
public record BuildSpecification(
        MinecraftVersion minecraftVersion,
        int protocolVersion,
        String apiVersion,
        String version,
        OptionalInt buildNumber
) {
}
