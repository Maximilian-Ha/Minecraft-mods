package com.hbm.blockentity.machine.storage;

import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.storage.TileEntitySoyuzCapsule.
 *
 * Neunzehn Faecher: achtzehn in einem Raster von sechs mal drei -- die Nutzlast, die oben
 * mitgeflogen ist -- und eines links daneben. In das legt die landende Kapsel die Rakete,
 * mit der sie gekommen ist.
 */
public class SoyuzCapsuleBlockEntity extends CrateBaseBlockEntity {

    /** Das Fach der Rakete. Es liegt hinter dem Raster, damit die Nutzlast bei 0 beginnt. */
    public static final int FACH_RAKETE = 18;

    public SoyuzCapsuleBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SOYUZ_CAPSULE.get(), pos, state, 19, "gui_soyuz_capsule", 6, 3, 62, 18, 8, 104, 176, 186, 8, 0x7daf71, 4210752);
    }

    @Override
    public int[] zusatzFaecher() {
        return new int[] { FACH_RAKETE, 17, 36 };
    }

    @Override public Component getName() { return Component.translatable("container.soyuzCapsule"); }
}
