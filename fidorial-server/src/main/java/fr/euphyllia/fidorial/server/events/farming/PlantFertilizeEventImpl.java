package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.farming.PlantFertilizeEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import org.jspecify.annotations.Nullable;

public final class PlantFertilizeEventImpl extends AbstractFarmingEvent implements PlantFertilizeEvent {

    private final @Nullable Player player;
    private final ItemStack fertilizer;

    public PlantFertilizeEventImpl(final World world, final BlockPosition position, final BlockData block,
                                   final @Nullable Player player, final ItemStack fertilizer) {
        super(world, position, block);
        Preconditions.checkArgument(fertilizer != null, "The fertilizer of a plant fertilize event must not be null");
        this.player = player;
        this.fertilizer = fertilizer;
    }

    @Override
    public @Nullable Player player() {
        return player;
    }

    @Override
    public ItemStack fertilizer() {
        return fertilizer;
    }
}
