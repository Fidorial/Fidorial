package fr.euphyllia.fidorial.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The public keys Mojang uses to sign a player's chat session key, fetched from
 * {@code https://api.minecraftservices.com/publickeys} and refreshed on the configured interval.
 * A session's signature is trusted if it verifies against any currently cached key.
 */
public final class MojangChatSigningKeys {

    private static final URI ENDPOINT = URI.create("https://api.minecraftservices.com/publickeys");
    private static final Duration REFRESH_INTERVAL = Duration.ofHours(6);

    private final HttpClient http;
    private final AtomicReference<Cached> cached = new AtomicReference<>(new Cached(List.of(), Instant.EPOCH));

    public MojangChatSigningKeys() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }

    public MojangChatSigningKeys(final HttpClient http) {
        this.http = http;
    }

    /**
     * @return the currently cached keys, refreshing them first if the cache has expired
     */
    public CompletableFuture<List<PublicKey>> keys() {
        final Cached current = cached.get();
        if (Instant.now().isBefore(current.refreshAfter())) {
            return CompletableFuture.completedFuture(current.keys());
        }
        return refresh();
    }

    private CompletableFuture<List<PublicKey>> refresh() {
        final HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200 || response.body().isBlank()) {
                        return cached.get().keys();
                    }
                    final List<PublicKey> keys = parse(response.body());
                    if (!keys.isEmpty()) {
                        cached.set(new Cached(keys, Instant.now().plus(REFRESH_INTERVAL)));
                    }
                    return keys.isEmpty() ? cached.get().keys() : keys;
                })
                .exceptionally(_ -> cached.get().keys());
    }

    private static List<PublicKey> parse(final String json) {
        final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        final JsonArray array = root.getAsJsonArray("playerCertificateKeys");
        if (array == null) {
            return List.of();
        }
        return array.asList().stream()
                .map(element -> element.getAsJsonObject().get("publicKey").getAsString())
                .map(MojangChatSigningKeys::decode)
                .filter(Objects::nonNull)
                .toList();
    }

    private static @Nullable PublicKey decode(final String pem) {
        final String stripped = pem
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        try {
            final byte[] der = Base64.getDecoder().decode(stripped);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (final IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    private record Cached(List<PublicKey> keys, Instant refreshAfter) {
    }
}
