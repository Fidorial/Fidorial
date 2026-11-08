package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.FaceAttachable;
import fr.fidorial.world.block.data.Powerable;

/**
 * The block state of a lever or a button: the surface it hangs on, its facing, and whether it is
 * on.
 *
 * @since 0.1.0
 */
@BlockTrait
public interface Switch extends FaceAttachable, Powerable {

    @Override
    default Switch withFacing(final BlockFace facing) {
        return (Switch) FaceAttachable.super.withFacing(facing);
    }

    @Override
    default Switch withAttachedFace(final AttachedFace face) {
        return (Switch) FaceAttachable.super.withAttachedFace(face);
    }

    @Override
    default Switch withPowered(final boolean powered) {
        return (Switch) Powerable.super.withPowered(powered);
    }
}
