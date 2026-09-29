package fr.euphyllia.fidorial.server.datapack.known;

import java.util.List;
import java.util.Set;

/**
 * The known packs the server offers during configuration, and how a client's response is negotiated.
 */
public record KnownPackNegotiation(List<KnownPack> packs) {

    /**
     * @param packs the packs.
     * @return the negotiation for them
     */
    public static KnownPackNegotiation packs(final KnownPack... packs) {
        return new KnownPackNegotiation(List.of(packs));
    }

    /**
     * The packs to send to the client.
     */
    @Override
    public List<KnownPack> packs() {
        return packs;
    }

    public Result negotiate(final List<KnownPack> selected) {
        if (!selected.equals(packs)) {
            return new Result.Rejected(Set.of());
        }
        return new Result.Accepted(Set.copyOf(packs));
    }

    public sealed

    interface Result {

        /**
         * Used when the server and client agree on all their known packs.
         * @param negotiated the packs both sides know
         */
        record Accepted(Set<KnownPack> negotiated) implements Result {
        }

        /**
         * Used when the server and client disagree on at least one known pack.
         * @param empty an empty pack set
         */
        record Rejected(Set<KnownPack> empty) implements Result {
        }
    }
}
