package fr.euphyllia.fidorial.server.chat;

import net.kyori.adventure.chat.SignedMessage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

/**
 * The last 20 signed messages sent to one specific client, in send order, so an incoming
 * {@code acknowledged} bitset and checksum can be resolved back into the actual signatures the
 * client claims to have last seen.
 */
public final class LastSeenMessages {

    public static final int SIZE = 20;

    private final Deque<SignedMessage.Signature> sent = new ArrayDeque<>(SIZE);

    public synchronized void push(final SignedMessage.Signature signature) {
        if (sent.size() == SIZE) {
            sent.removeFirst();
        }
        sent.addLast(signature);
    }

    /**
     * @param acknowledged 20-bit mask; bit 19 (highest) is the most recently sent of the window
     * @param checksum     the checksum the client computed over the resolved signatures
     * @return the acknowledged signatures, oldest first, or {@code null} if the checksum doesn't
     * match (the client acknowledged something inconsistent with what we actually sent)
     */
    public synchronized @Nullable List<SignedMessage.Signature> resolve(final BitSet acknowledged, final byte checksum) {
        final List<SignedMessage.Signature> window = new ArrayList<>(sent);
        final List<SignedMessage.Signature> resolved = new ArrayList<>();

        final int offset = SIZE - window.size();
        for (int bit = 0; bit < SIZE; bit++) {
            if (!acknowledged.get(bit)) {
                continue;
            }
            final int windowIndex = bit - offset;
            if (windowIndex < 0 || windowIndex >= window.size()) {
                return null;
            }
            resolved.add(window.get(windowIndex));
        }

        return checksum == computeChecksum(resolved) ? resolved : null;
    }

    public synchronized void clear() {
        sent.clear();
    }

    private static byte computeChecksum(final List<SignedMessage.Signature> signatures) {
        int hash = 1;
        for (final SignedMessage.Signature signature : signatures) {
            hash = 31 * hash + Arrays.hashCode(signature.bytes());
        }
        final byte b = (byte) hash;
        return b == 0 ? (byte) 1 : b;
    }
}
