package fr.euphyllia.fidorial.server.world.block.plant;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class SweetBerryBushBlock extends FidorialCropBlock {

    private static final Key BERRIES = ItemKeys.SWEET_BERRIES.key();
    private static final Sound.Type PICK_SOUND = SoundEvents.of("block.sweet_berry_bush.pick_berries");

    private static final Set<Key> SOILS = Set.of(
            BlockTypeKeys.DIRT.key(),
            BlockTypeKeys.GRASS_BLOCK.key(),
            BlockTypeKeys.FARMLAND.key(),
            BlockTypeKeys.COARSE_DIRT.key(),
            BlockTypeKeys.PODZOL.key(),
            BlockTypeKeys.ROOTED_DIRT.key(),
            BlockTypeKeys.MYCELIUM.key(),
            BlockTypeKeys.MOSS_BLOCK.key(),
            BlockTypeKeys.PALE_MOSS_BLOCK.key(),
            BlockTypeKeys.MUD.key());

    public SweetBerryBushBlock() {
        super(FidorialCropBlock.builder(BlockTypeKeys.SWEET_BERRY_BUSH.key())
                .soils(SOILS)
                .growth(Growth.fixed(1.0 / 5))
                .placeSound(SoundEvents.of("block.sweet_berry_bush.place")));
    }

    @Override
    public InteractionResult use(final BlockData data, final BlockInteractionContext context) {
        final int age = age(data);
        if (age <= 1) {
            return InteractionResult.PASS;
        }
        final boolean ripe = age == maxAge();
        if (!ripe && context.heldItem().id().equals(ItemKeys.BONE_MEAL.key())) {
            return InteractionResult.PASS;
        }

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int count = 1 + random.nextInt(2) + (ripe ? 1 : 0);
        context.blocks().dropItems(context.pos(), List.of(ItemStack.of(BERRIES, count)));
        context.playSound(Sound.sound(PICK_SOUND, Sound.Source.BLOCK, 1.0f, 0.8f + random.nextFloat() * 0.4f));
        context.blocks().setBlock(context.pos(), withAge(data, 1));
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        return switch (age(data)) {
            case 3 -> List.of(ItemStack.of(BERRIES, random.nextInt(2, 4)));
            case 2 -> List.of(ItemStack.of(BERRIES, random.nextInt(1, 3)));
            default -> List.of();
        };
    }
}
