package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class HarvestEventImpl extends AbstractFarmingEvent implements HarvestEvent {

    private final @Nullable Player player;
    private final ItemStack tool;
    private final List<ItemStack> drops;

    public HarvestEventImpl(final World world, final BlockPosition position, final BlockData block,
                            final @Nullable Player player, final ItemStack tool, final List<ItemStack> drops) {
        super(world, position, block);
        Preconditions.checkArgument(tool != null, "The tool of a harvest event must not be null");
        Preconditions.checkArgument(drops != null, "The drops of a harvest event must not be null");
        this.player = player;
        this.tool = tool;
        this.drops = new ArrayList<>(drops);
    }

    @Override
    public @Nullable Player player() {
        return player;
    }

    @Override
    public ItemStack tool() {
        return tool;
    }

    @Override
    public List<ItemStack> drops() {
        return drops;
    }
}
