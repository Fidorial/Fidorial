package fr.euphyllia.fidorial.server.schedulers;

import ca.spottedleaf.concurrentutil.map.SWMRLong2ObjectHashTable;
import fr.euphyllia.fidorial.server.events.region.RegionMergeEventImpl;
import fr.euphyllia.fidorial.server.events.region.RegionSplitEventImpl;
import fr.fidorial.event.Event;
import fr.fidorial.scheduler.Region;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The sections and regions of one world {@code ThreadedRegionizer}.
 *
 * <p>Two rules keep threads apart:</p>
 * <ul>
 *   <li>every occupied section (holding a loaded chunk or a ticket) has all the sections within
 *   {@code bufferRadius} of it, so a region can always reach the neighbours of what it ticks;</li>
 *   <li>two sections less than {@code mergeRadius} apart always belong to the same region.</li>
 * </ul>
 *
 * <p>When occupying a section breaks the second rule, the regions involved are merged. A region
 * that is ticking cannot be merged: the others are kept off the scheduler and wait for it to finish
 * its tick, then it takes them over. After each tick, a region whose dead sections grew numerous
 * drops them and splits into the groups of sections that still touch.</p>
 *
 * <p>Structural changes are made under one lock per world. Adding or removing a hold from a section
 * that stays occupied, queueing a task and checking ownership need no lock.</p>
 */
final class WorldRegions {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldRegions.class);

    /**
     * A region recalculates its shape once a sixth of its sections are dead.
     */
    private static final double MAX_DEAD_SECTION_FRACTION = 1.0 / 6.0;

    final Key key;
    final ThreadedRegionRegionizer regionizer;
    private final int bufferRadius;
    private final int mergeRadius;
    private final int minSectionsForRecalculation;

    private final SWMRLong2ObjectHashTable<RegionSection> sections = new SWMRLong2ObjectHashTable<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Event> pendingEvents = new ArrayList<>(); // guarded by lock

    /**
     * Every region not dead yet. Changed under the lock, read from anywhere.
     */
    final Set<TickRegion> regions = ConcurrentHashMap.newKeySet();

    WorldRegions(final Key key, final ThreadedRegionRegionizer regionizer, final int bufferRadius) {
        this.key = key;
        this.regionizer = regionizer;
        this.bufferRadius = bufferRadius;
        this.mergeRadius = bufferRadius > 0 ? 1 : 0;
        this.minSectionsForRecalculation = Math.max(2, 2048 >> (2 * ThreadedRegionRegionizer.sectionShift()));
    }

    @Nullable RegionSection section(final int sectionX, final int sectionZ) {
        return sections.get(ChunkPos.chunkKey(sectionX, sectionZ));
    }

    @Nullable RegionSection sectionOf(final ChunkPos chunk) {
        return section(ThreadedRegionRegionizer.chunkToSection(chunk.x()), ThreadedRegionRegionizer.chunkToSection(chunk.z()));
    }

    void addHold(final int sectionX, final int sectionZ) {
        final RegionSection existing = section(sectionX, sectionZ);
        if (existing != null && existing.tryHoldWhileOccupied()) {
            return;
        }
        lock();
        try {
            occupy(sectionX, sectionZ);
        } finally {
            unlock();
        }
    }

    void removeHold(final int sectionX, final int sectionZ) {
        final RegionSection existing = section(sectionX, sectionZ);
        if (existing != null && existing.tryReleaseUnlessLast()) {
            return;
        }
        lock();
        try {
            final RegionSection section = section(sectionX, sectionZ);
            final int left = section == null ? -1 : section.release();
            if (left == 0 && section != null) {
                vacate(section);
            } else if (left < 0) {
                LOGGER.debug("Released a hold that was never taken on section {},{} of {}", sectionX, sectionZ, key.asString());
            }
        } finally {
            unlock();
        }
    }

    /**
     * Queues a task on a section. Where there is no section, it is created and held until its tasks
     * have run, so that some region ticks it.
     */
    void offer(final int sectionX, final int sectionZ, final Runnable task, final long delayTicks) {
        RegionSection found;
        while ((found = section(sectionX, sectionZ)) != null) {
            if (found.offer(task, delayTicks)) {
                regionizer.notifyTasks(found.region);
                return;
            }
            // dropped by its region meanwhile: look it up again
        }
        final RegionSection created;
        lock();
        try {
            RegionSection section = section(sectionX, sectionZ);
            if (section == null) {
                section = occupy(sectionX, sectionZ);
                section.taskHold = true;
            }
            // under the lock, the section cannot be dropped before the task is queued
            section.offer(task, delayTicks);
            created = section;
        } finally {
            unlock();
        }
        regionizer.notifyTasks(created.region);
    }

    boolean tryMarkTicking(final TickRegion region) {
        lock();
        try {
            if (region.state != TickRegion.READY) {
                return false;
            }
            region.state = TickRegion.TICKING;
            return true;
        } finally {
            unlock();
        }
    }

    /**
     * Ends a tick (or a batch of tasks): applies the merges that waited for it, drops dead sections
     * and splits the region if needed.
     *
     * @return whether the region stays scheduled
     */
    boolean markNotTicking(final TickRegion region) {
        lock();
        try {
            if (region.state != TickRegion.TICKING) {
                throw new IllegalStateException("Region " + region + " is not ticking");
            }
            release(region);
            return region.state == TickRegion.READY;
        } finally {
            unlock();
        }
    }

    /**
     * Holds a section once more. When it was not occupied, creates the missing sections around it and
     * merges the regions that now touch.
     *
     * @return the section
     */
    private RegionSection occupy(final int sectionX, final int sectionZ) {
        final long selfKey = ChunkPos.chunkKey(sectionX, sectionZ);
        final RegionSection existing = sections.get(selfKey);
        if (existing != null && existing.hold() > 0) {
            return existing; // occupied meanwhile
        }

        final ReferenceOpenHashSet<TickRegion> nearby = new ReferenceOpenHashSet<>();
        final LongArrayList missing = new LongArrayList();
        if (existing == null) {
            missing.add(selfKey);
        } else {
            nearby.add(existing.region);
            if (existing.occupiedNeighbours == 0) {
                existing.region.deadSections.remove(existing);
            }
        }

        final int search = bufferRadius + mergeRadius;
        for (int dx = -search; dx <= search; dx++) {
            for (int dz = -search; dz <= search; dz++) {
                if ((dx | dz) == 0) {
                    continue;
                }
                final boolean inBuffer = Math.max(Math.abs(dx), Math.abs(dz)) <= bufferRadius;
                final long neighbourKey = ChunkPos.chunkKey(sectionX + dx, sectionZ + dz);
                final RegionSection neighbour = sections.get(neighbourKey);
                if (neighbour != null) {
                    nearby.add(neighbour.region);
                    if (inBuffer && neighbour.occupiedNeighbours++ == 0 && neighbour.holds() == 0) {
                        neighbour.region.deadSections.remove(neighbour);
                    }
                } else if (inBuffer) {
                    missing.add(neighbourKey);
                }
            }
        }

        if (missing.isEmpty() && existing != null) {
            // every section around already existed, so they already share a region
            return existing;
        }

        final TickRegion idle = firstNotTicking(nearby);
        final boolean fresh = idle == null;
        final TickRegion target = idle != null ? idle : regionizer.newRegion(this);
        if (fresh) {
            regions.add(target);
        }

        RegionSection self = existing;
        for (int i = 0; i < missing.size(); i++) {
            final long sectionKey = missing.getLong(i);
            final RegionSection created = new RegionSection((int) (sectionKey >> 32), (int) sectionKey, target);
            if (sectionKey == selfKey) {
                created.hold();
                self = created;
            } else {
                created.occupiedNeighbours = 1;
            }
            target.addSection(created);
            sections.put(sectionKey, created);
        }

        for (final TickRegion region : nearby) {
            if (region != target && !mergeInto(region, target)) {
                mergeLater(target, region); // ticking: target waits for it
            }
        }

        target.refreshOwned();
        if (!target.mergeIntoLater.isEmpty()) {
            if (target.state == TickRegion.READY) {
                target.state = TickRegion.TRANSIENT;
                regionizer.cancel(target);
            }
        } else if (fresh) {
            target.state = TickRegion.READY;
            regionizer.schedule(target);
        } else if (target.state == TickRegion.READY) {
            regionizer.notifyTasks(target); // tasks may have come with the merged regions
        }
        if (self == null) {
            throw new IllegalStateException("Section " + sectionX + "," + sectionZ + " was not created");
        }
        return self;
    }

    private static @Nullable TickRegion firstNotTicking(final Set<TickRegion> regions) {
        for (final TickRegion region : regions) {
            if (region.state != TickRegion.TICKING) {
                return region;
            }
        }
        return null;
    }

    /**
     * Records that a section is no longer occupied: it and its neighbours may die.
     */
    private void vacate(final RegionSection section) {
        if (section.occupiedNeighbours == 0) {
            section.region.deadSections.add(section);
        }
        for (int dx = -bufferRadius; dx <= bufferRadius; dx++) {
            for (int dz = -bufferRadius; dz <= bufferRadius; dz++) {
                if ((dx | dz) == 0) {
                    continue;
                }
                final RegionSection neighbour = section(section.x + dx, section.z + dz);
                if (neighbour == null) {
                    throw new IllegalStateException("Missing buffer section around " + section);
                }
                if (--neighbour.occupiedNeighbours == 0 && neighbour.holds() == 0) {
                    neighbour.region.deadSections.add(neighbour);
                }
            }
        }
    }

    /**
     * Moves every section of {@code from} into {@code into}, unless {@code from} is ticking.
     */
    private boolean mergeInto(final TickRegion from, final TickRegion into) {
        if (from.state == TickRegion.TICKING) {
            return false;
        }
        final Region before = regionizer.listens(RegionMergeEventImpl.class) ? from.snapshot() : null;
        if (from.state == TickRegion.READY) {
            regionizer.cancel(from);
        }
        from.state = TickRegion.DEAD;
        regions.remove(from);

        for (final RegionSection section : from.sections.values()) {
            into.addSection(section);
        }
        // whatever waited on from, or from waited on, now concerns into
        for (final TickRegion ticking : from.mergeIntoLater) {
            ticking.expectingMergeFrom.remove(from);
            if (ticking != into) {
                mergeLater(into, ticking);
            }
        }
        for (final TickRegion waiting : from.expectingMergeFrom) {
            waiting.mergeIntoLater.remove(from);
            if (waiting != into) {
                mergeLater(waiting, into);
            }
        }
        from.clear();
        into.refreshOwned();
        LOGGER.debug("Region {} merged into {} in {}", from.id, into, key.asString());

        if (before != null) {
            pendingEvents.add(new RegionMergeEventImpl(key, before, into.snapshot()));
        }
        return true;
    }

    /**
     * Records that {@code waiting} merges into {@code ticking} once it finishes its tick.
     */
    private static void mergeLater(final TickRegion waiting, final TickRegion ticking) {
        waiting.mergeIntoLater.add(ticking);
        ticking.expectingMergeFrom.add(waiting);
    }

    private void release(final TickRegion region) {
        for (final RegionSection section : region.spentTaskHolds) {
            if (section.taskHold && section.region == region && !section.hasPendingTasks()) {
                section.taskHold = false;
                if (section.release() == 0) {
                    vacate(section);
                }
            }
        }
        region.spentTaskHolds.clear();

        final boolean merged = !region.expectingMergeFrom.isEmpty();
        if (merged) {
            for (final TickRegion waiting : List.copyOf(region.expectingMergeFrom)) {
                if (!mergeInto(waiting, region)) {
                    throw new IllegalStateException("Region " + waiting + " ticked while waiting to merge into " + region);
                }
            }
            if (!region.mergeIntoLater.isEmpty()) {
                // took over a region that waited on another ticking one: wait on it in turn
                region.state = TickRegion.TRANSIENT;
                return;
            }
        }

        final int total = region.sections.size();
        final int dead = region.deadSections.size();
        final boolean recalculate = merged || dead == total
                || (total >= minSectionsForRecalculation && dead >= total * MAX_DEAD_SECTION_FRACTION);
        if (!recalculate || dead == 0 || !removeDeadSections(region)) {
            region.state = TickRegion.READY;
            return;
        }
        if (region.sections.isEmpty()) {
            region.state = TickRegion.DEAD;
            regions.remove(region);
            return;
        }
        split(region);
    }

    /**
     * {@return whether a dead section was dropped} Those with tasks left stay until the tasks have run.
     */
    private boolean removeDeadSections(final TickRegion region) {
        boolean removed = false;
        for (final RegionSection section : List.copyOf(region.deadSections)) {
            if (section.tryRemove()) {
                region.removeSection(section);
                sections.remove(section.key);
                removed = true;
            }
        }
        if (removed) {
            region.refreshOwned();
        }
        return removed;
    }

    /**
     * Splits a region into the groups of its sections that touch, if there are several.
     */
    private void split(final TickRegion region) {
        final int radius = Math.max(bufferRadius, mergeRadius);
        final Long2ObjectOpenHashMap<RegionSection> left = region.sections.clone();
        final List<List<RegionSection>> parts = new ArrayList<>();
        while (!left.isEmpty()) {
            final List<RegionSection> part = new ArrayList<>();
            final Iterator<RegionSection> first = left.values().iterator();
            part.add(first.next());
            first.remove();
            for (int i = 0; i < part.size() && !left.isEmpty(); i++) {
                final RegionSection section = part.get(i);
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        final RegionSection neighbour = left.remove(ChunkPos.chunkKey(section.x + dx, section.z + dz));
                        if (neighbour != null) {
                            part.add(neighbour);
                        }
                    }
                }
            }
            parts.add(part);
        }

        if (parts.size() == 1) {
            region.state = TickRegion.READY;
            return;
        }

        final Region before = regionizer.listens(RegionSplitEventImpl.class) ? region.snapshot() : null;
        final List<Region> after = new ArrayList<>(parts.size());
        region.state = TickRegion.DEAD;
        regions.remove(region);
        for (final List<RegionSection> part : parts) {
            final TickRegion child = new TickRegion(this, region.forkTickState(), region.appliedTickChanges);
            for (final RegionSection section : part) {
                child.addSection(section);
            }
            child.refreshOwned();
            child.state = TickRegion.READY;
            regions.add(child);
            regionizer.schedule(child);
            if (before != null) {
                after.add(child.snapshot());
            }
        }
        region.clear();
        LOGGER.debug("Region {} split into {} regions in {}", region.id, parts.size(), key.asString());
        if (before != null) {
            pendingEvents.add(new RegionSplitEventImpl(key, before, after));
        }
    }

    private void lock() {
        if (lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot change the regions of " + key.asString() + " from within a change");
        }
        lock.lock();
    }

    /**
     * Unlocks, then posts the events of the change, so listeners run without the lock.
     */
    private void unlock() {
        if (pendingEvents.isEmpty()) {
            lock.unlock();
            return;
        }
        final List<Event> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        lock.unlock();
        regionizer.post(events);
    }
}
