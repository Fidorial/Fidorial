package fr.fidorial.event.redstone;

import fr.fidorial.event.Cancellable;

/**
 * Fired when redstone dust is about to change the power it carries, as a signal spreads along a
 * line of dust or fades from it: one event per dust whose power changes.
 *
 * <p>Dust takes its power from its own neighbours, one dust at a time, and may go through several
 * levels before settling: each of them fires the event.</p>
 *
 * <p>Changing {@linkplain #setNewPower(int) the new power} changes what the dust carries, and so what
 * it passes on to the dust after it. Cancelling the event keeps the power the dust carries, as if
 * {@link #oldPower()} had been set as the new power.</p>
 *
 * @since 0.1.0
 */
public interface RedstoneDustPowerEvent extends RedstoneEvent, Cancellable {

    /**
     * The lowest power redstone dust carries.
     *
     * @since 0.1.0
     */
    int MIN_POWER = 0;

    /**
     * The highest power redstone dust carries.
     *
     * @since 0.1.0
     */
    int MAX_POWER = 15;

    /**
     * {@return the power the dust carries, from {@code 0} to {@code 15}}
     *
     * @since 0.1.0
     */
    int oldPower();

    /**
     * {@return the power the dust is about to carry, from {@code 0} to {@code 15}}
     *
     * @since 0.1.0
     */
    int newPower();

    /**
     * Sets the power the dust carries once the event is over, in place of the one the signal gives
     * it. The dust passes it on to the dust it powers, one less per block.
     *
     * @param newPower the power, from {@code 0} to {@code 15}
     * @throws IllegalArgumentException if the power is not between {@link #MIN_POWER} and {@link #MAX_POWER}
     * @since 0.1.0
     */
    void setNewPower(int newPower);

    /**
     * {@return the power the dust carries once the event is over: {@link #oldPower()} if the event is
     * cancelled, {@link #newPower()} otherwise}
     *
     * @since 0.1.0
     */
    default int resultingPower() {
        return isCancelled() ? oldPower() : newPower();
    }
}