package fr.fidorial.command;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

record CommandSourceImpl(CommandSender sender, @Nullable Entity executor, @Nullable Location location) implements CommandSource {

    @Override
    public CommandSource as(final Entity executor) {
        Preconditions.checkArgument(executor != null, "executor cannot be null");
        return new CommandSourceImpl(sender, executor, location);
    }

    @Override
    public CommandSource at(final Location location) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        return new CommandSourceImpl(sender, executor, location);
    }
}
