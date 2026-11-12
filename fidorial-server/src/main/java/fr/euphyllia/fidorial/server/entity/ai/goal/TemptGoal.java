package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Makes a mob follow a player who holds something it likes: the closest such player
 * within the tempt range, whom it walks up to and then watches. Once the player puts
 * the item away or leaves, the mob waits five seconds before it lets itself be tempted again.
 */
public final class TemptGoal implements Goal {

    /**
     * Vanilla's {@code tempt_range} attribute of the animals.
     */
    public static final double DEFAULT_RANGE = 10.0;
    public static final double DEFAULT_STOP_DISTANCE = 2.5;

    private static final int CALM_DOWN_TICKS = 100;
    private static final double SNEAKING_VISIBILITY = 0.8;
    private static final double MIN_VISIBLE_RANGE = 2.0;
    private static final float LOOK_YAW_SPEED = AbstractMovingMob.MAX_HEAD_YAW + 20.0f;
    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.LOOK);

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;
    private final Predicate<ItemStack> temptingItems;
    private final double range;
    private final double stopDistance;

    private @Nullable ServerPlayer tempter;
    private int calmDown;
    private boolean running;

    public TemptGoal(final AbstractPathfinderMob mob, final int priority, final double speed,
                     final Predicate<ItemStack> temptingItems) {
        this(mob, priority, speed, temptingItems, DEFAULT_RANGE, DEFAULT_STOP_DISTANCE);
    }

    /**
     * @param speed         the movement speed of the mob times vanilla's speed modifier
     * @param temptingItems the items the mob follows
     * @param range         how far the mob notices such an item, in blocks
     * @param stopDistance  how close to the player the mob stops
     */
    public TemptGoal(final AbstractPathfinderMob mob, final int priority, final double speed,
                     final Predicate<ItemStack> temptingItems, final double range, final double stopDistance) {
        this.mob = mob;
        this.priority = priority;
        this.speed = speed;
        this.temptingItems = temptingItems;
        this.range = range;
        this.stopDistance = stopDistance;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Set<Flag> flags() {
        return FLAGS;
    }

    @Override
    public boolean canStart() {
        if (calmDown > 0) {
            calmDown--;
            return false;
        }
        tempter = findTempter();
        return tempter != null;
    }

    @Override
    public boolean shouldContinue() {
        tempter = findTempter();
        return tempter != null;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        tempter = null;
        mob.navigation().stop();
        calmDown = CALM_DOWN_TICKS;
        running = false;
    }

    @Override
    public void tick() {
        final ServerPlayer player = tempter;
        if (player == null) {
            return;
        }
        mob.setLookTarget(player, LOOK_YAW_SPEED, AbstractMovingMob.MAX_HEAD_PITCH);
        if (mob.distanceSqTo(player) < stopDistance * stopDistance) {
            mob.navigation().stop();
        } else {
            mob.navigation().moveTo(mob.location(), player.location(), speed);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public @Nullable ServerPlayer tempter() {
        return tempter;
    }

    private @Nullable ServerPlayer findTempter() {
        final List<ServerPlayer> players = mob.server().players().online();
        ServerPlayer best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int i = 0, size = players.size(); i < size; i++) {
            final ServerPlayer player = players.get(i);
            if (!isTempting(player)) {
                continue;
            }
            // A sneaking player is noticed from 80% of the range, never less than 2 blocks
            final double visible = Math.max(range * (player.isSneaking() ? SNEAKING_VISIBILITY : 1.0), MIN_VISIBLE_RANGE);
            final double distSq = mob.distanceSqTo(player);
            if (distSq <= visible * visible && distSq < bestDistSq) {
                bestDistSq = distSq;
                best = player;
            }
        }
        return best;
    }

    private boolean isTempting(final ServerPlayer player) {
        if (player.isRemoved() || player.isDead()
                || player.gameMode() == GameMode.SPECTATOR
                || !player.world().equals(mob.world())) {
            return false;
        }

        return isTempting(player.inventory().get(player.selectedSlot()))
                || isTempting(player.inventory().get(PlayerInventory.OFF_HAND));
    }

    private boolean isTempting(final @Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && temptingItems.test(stack);
    }

}
