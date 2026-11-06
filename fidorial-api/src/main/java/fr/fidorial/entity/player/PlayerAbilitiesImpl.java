package fr.fidorial.entity.player;

record PlayerAbilitiesImpl(boolean flightAllowed, boolean flying, float flySpeed, float walkSpeed) implements PlayerAbilities {

    PlayerAbilitiesImpl {
        if (!Float.isFinite(flySpeed)) {
            throw new IllegalArgumentException("Fly speed must be finite: " + flySpeed);
        }
        if (!Float.isFinite(walkSpeed)) {
            throw new IllegalArgumentException("Walk speed must be finite: " + walkSpeed);
        }
        flying = flying && flightAllowed;
    }

    @Override
    public PlayerAbilities withFlightAllowed(final boolean flightAllowed) {
        return new PlayerAbilitiesImpl(flightAllowed, flying, flySpeed, walkSpeed);
    }

    @Override
    public PlayerAbilities withFlying(final boolean flying) {
        return new PlayerAbilitiesImpl(flightAllowed, flying, flySpeed, walkSpeed);
    }

    @Override
    public PlayerAbilities withFlySpeed(final float flySpeed) {
        return new PlayerAbilitiesImpl(flightAllowed, flying, flySpeed, walkSpeed);
    }

    @Override
    public PlayerAbilities withWalkSpeed(final float walkSpeed) {
        return new PlayerAbilitiesImpl(flightAllowed, flying, flySpeed, walkSpeed);
    }
}
