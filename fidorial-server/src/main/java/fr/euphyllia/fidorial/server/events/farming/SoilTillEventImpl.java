package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.farming.SoilTillEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class SoilTillEventImpl extends AbstractFarmingEvent implements SoilTillEvent {

    private final Player player;
    private final ItemStack tool;
    private BlockData tilled;

    public SoilTillEventImpl(final World world, final BlockPosition position, final BlockData block,
                             final Player player, final ItemStack tool, final BlockData tilled) {
        super(world, position, block);
        Preconditions.checkArgument(player != null, "The player of a soil till event must not be null");
        Preconditions.checkArgument(tool != null, "The tool of a soil till event must not be null");
        Preconditions.checkArgument(tilled != null, "The tilled state of a soil till event must not be null");
        this.player = player;
        this.tool = tool;
        this.tilled = tilled;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public ItemStack tool() {
        return tool;
    }

    @Override
    public BlockData tilled() {
        return tilled;
    }

    @Override
    public void setTilled(final BlockData tilled) {
        Preconditions.checkArgument(tilled != null, "The tilled state of a soil till event must not be null");
        this.tilled = tilled;
    }
}
