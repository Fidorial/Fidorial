package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.debug.DebugValues;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.ai.Goals;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Schedules the goals of a mob as vanilla does: each running goal holds the
 * {@linkplain Goal#flags() controls} it needs, and a goal starts only if every control it needs is
 * free or held by an interruptible goal of lower priority.
 */
public class GoalSelector implements Goals {

    private final List<Goal> goals = new CopyOnWriteArrayList<>();

    private final Set<Goal> running = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Goal.Flag, Goal> holders = new EnumMap<>(Goal.Flag.class);

    @Override
    public void add(final Goal goal) {
        goals.add(goal);
        goals.sort(Comparator.comparingInt(Goal::priority));
    }

    @Override
    public boolean remove(final Goal goal) {
        stop(goal);
        return goals.remove(goal);
    }

    @Override
    public boolean removeIf(final Predicate<Goal> filter) {
        for (final Goal goal : goals) {
            if (filter.test(goal)) {
                stop(goal);
            }
        }
        return goals.removeIf(filter);
    }

    @Override
    public void clear() {
        stopAll();
        goals.clear();
    }

    @Override
    public List<Goal> all() {
        return List.copyOf(goals);
    }

    public void tick() {
        for (final Goal goal : goals) {
            if (running.contains(goal) && !goal.shouldContinue()) {
                stop(goal);
            }
        }

        for (final Goal goal : goals) {
            if (running.contains(goal) || !canTakeControls(goal) || !goal.canStart()) {
                continue;
            }
            for (final Goal.Flag flag : goal.flags()) {
                final Goal holder = holders.get(flag);
                if (holder != null) {
                    stop(holder);
                }
                holders.put(flag, goal);
            }
            running.add(goal);
            goal.start();
        }

        for (final Goal goal : goals) {
            if (running.contains(goal)) {
                goal.tick();
            }
        }
    }

    private boolean canTakeControls(final Goal goal) {
        for (final Goal.Flag flag : goal.flags()) {
            final Goal holder = holders.get(flag);
            if (holder != null && !(holder.isInterruptible() && goal.priority() < holder.priority())) {
                return false;
            }
        }
        return true;
    }

    private void stop(final Goal goal) {
        if (!running.remove(goal)) {
            return;
        }
        holders.values().removeIf(holder -> holder == goal);
        goal.stop();
    }

    @Override
    public @Nullable Goal active() {
        for (final Goal goal : goals) {
            if (running.contains(goal)) {
                return goal;
            }
        }
        return null;
    }

    @Override
    public List<Goal> running() {
        final List<Goal> result = new ArrayList<>(running.size());
        for (final Goal goal : goals) {
            if (running.contains(goal)) {
                result.add(goal);
            }
        }
        return List.copyOf(result);
    }

    public boolean isRunning(final Goal goal) {
        return running.contains(goal);
    }

    public DebugValues.@Nullable GoalSelectorInfo debugSnapshot() {
        if (goals.isEmpty()) {
            return null;
        }
        final List<DebugValues.GoalSelectorInfo.Entry> entries = new ArrayList<>(goals.size());
        for (final Goal goal : goals) {
            entries.add(new DebugValues.GoalSelectorInfo.Entry(goal.priority(), running.contains(goal), debugName(goal)));
        }
        return new DebugValues.GoalSelectorInfo(entries);
    }

    public static String debugName(final Goal goal) {
        final String simple = goal.getClass().getSimpleName();
        return simple.isEmpty() ? goal.getClass().getName() : simple;
    }

    @Override
    public void stopAll() {
        for (final Goal goal : goals) {
            stop(goal);
        }
        running.clear();
        holders.clear();
    }
}
