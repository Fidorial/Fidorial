package fr.fidorial.world.block.property;

/// The progress of a trial spawner.
///
/// @since 0.1.0
public enum TrialSpawnerPhase {
    INACTIVE,
    WAITING_FOR_PLAYERS,
    ACTIVE,
    WAITING_FOR_REWARD_EJECTION,
    EJECTING_REWARD,
    COOLDOWN
}
