package com.hbm.blockentity.turret;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.items.weapon.sedna.factory.XFactory556mm;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretFriendly.
 *
 * Derselbe Turm wie die Gatling, nur in 5,56 mm und mit der halben Kadenz -- der kleine Bruder,
 * den man frueh bauen kann. Er wirft seine Huelsen entsprechend sanfter aus.
 */
public class TurretFriendlyBlockEntity extends TurretChekhovBlockEntity {

    private static final List<Integer> CONFIGS = new ArrayList<>();

    public static void initAmmo() {
        if(!CONFIGS.isEmpty()) return;
        CONFIGS.add(XFactory556mm.r556_sp.id);
        CONFIGS.add(XFactory556mm.r556_fmj.id);
        CONFIGS.add(XFactory556mm.r556_jhp.id);
        CONFIGS.add(XFactory556mm.r556_ap.id);
    }

    public TurretFriendlyBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.TURRET_FRIENDLY.get(), pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.turretFriendly"); }
    @Override protected List<Integer> getAmmoList() { return CONFIGS; }

    @Override public ResourceLocation getGuiTexture() { return NuclearTechMod.withDefaultNamespace("textures/gui/weapon/gui_turret_friendly.png"); }

    @Override public int getDelay() { return 5; }
    @Override protected SoundEvent getFireSound() { return NtmSoundEvents.TURRET_CHEKHOV_FIRE.get(); }

    @Override
    protected Vec3 getCasingMotion() { return new Vec3(-0.3, 0.6, 0); }
}
