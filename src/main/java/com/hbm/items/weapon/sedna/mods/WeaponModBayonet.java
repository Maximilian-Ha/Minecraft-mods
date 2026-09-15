package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.factory.XFactory44;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.mods.WeaponModCarbineBayonet und
 * WeaponModMASBayonet.
 *
 * Das Bajonett. Es macht aus der Betrachtungsbewegung einen Stoss: die rechte Maustaste loest
 * statt des Betrachtens einen Stich aus, der auf drei Block Reichweite fuenfzehn Schaden macht
 * und das Ziel zurueckwirft. Weil der Stoss keine Betrachtung mehr ist, darf er auch nicht mehr
 * abgebrochen werden (I_INSPECTCANCEL).
 *
 * ZUSAMMENGEFASST: das Original hat davon zwei Klassen, eine je Waffe. Sie unterscheiden sich in
 * nichts als dem Animationssatz, an den sie durchreichen, und dem Orchester dahinter -- beides
 * bekommt diese Klasse im Konstruktor. Die Nummern bleiben getrennt, damit gespeicherte Waffen
 * ihren Aufsatz behalten.
 */
public class WeaponModBayonet extends WeaponModBase {

    private final BiFunction<ItemStack, GunAnimation, BusAnimation> parentAnims;
    private final BiConsumer<ItemStack, LambdaContext> parentOrchestra;

    private final BiFunction<ItemStack, GunAnimation, BusAnimation> anims;
    private final BiConsumer<ItemStack, LambdaContext> orchestra;

    public WeaponModBayonet(int id,
            BiFunction<ItemStack, GunAnimation, BusAnimation> parentAnims,
            BiConsumer<ItemStack, LambdaContext> parentOrchestra) {

        super(id, "BAYONET");

        this.parentAnims = parentAnims;
        this.parentOrchestra = parentOrchestra;

        /* Nur der Stoss ist eigen; alles andere reicht der Satz der Waffe durch. */
        this.anims = (stack, type) -> {
            if(type == GunAnimation.INSPECT) return new BusAnimation()
                    .addBus("STAB", new BusAnimationSequence()
                            .addPos(0, 1, -2, 250, IType.SIN_DOWN).hold(250)
                            .addPos(0, 1, 5, 250, IType.SIN_UP).hold(250)
                            .addPos(0, 0, 0, 500, IType.SIN_FULL));
            return this.parentAnims.apply(stack, type);
        };

        this.orchestra = (stack, ctx) -> {

            LivingEntity entity = ctx.entity;
            Level level = entity.level;
            if(level.isClientSide) return;

            GunAnimation type = GunBaseNTItem.getLastAnim(stack, ctx.configIndex);
            int timer = GunBaseNTItem.getAnimTimer(stack, ctx.configIndex);

            if(type == GunAnimation.INSPECT) {

                if(timer == 15 && ctx.getPlayer() != null) {

                    HitResult hr = EntityDamageUtil.getMouseOver(ctx.getPlayer(), 3.0D);
                    if(hr == null) return;

                    if(hr.getType() == Type.ENTITY) {
                        Entity hitEntity = ((EntityHitResult) hr).getEntity();
                        hitEntity.hurt(level.damageSources().playerAttack(ctx.getPlayer()), 15F);
                        Vec3 motion = hitEntity.getDeltaMovement();
                        hitEntity.setDeltaMovement(motion.x * 2, motion.y, motion.z * 2);
                        SoundUtils.playAtVec3(level, hitEntity.position(), NtmSoundEvents.GUN_STAB.get(), entity.getSoundSource(), 1F, 0.9F + entity.random.nextFloat() * 0.2F);
                    }

                    if(hr.getType() == Type.BLOCK) {
                        BlockPos pos = ((BlockHitResult) hr).getBlockPos();
                        BlockState state = level.getBlockState(pos);
                        Block block = state.getBlock();
                        SoundUtils.playAtBlockPos(level, pos, block.getSoundType(state, level, pos, ctx.getPlayer()).getStepSound(), entity.getSoundSource(), 2F, 0.9F + entity.random.nextFloat() * 0.2F);
                    }
                }

                return;
            }

            this.parentOrchestra.accept(stack, ctx);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {

        if(key.equals(GunConfig.FUN_ANIMNATIONS)) return (T) this.anims;
        if(key.equals(GunConfig.CON_ORCHESTRA)) return (T) this.orchestra;
        if(key.equals(GunConfig.CON_ONPRESSSECONDARY)) return (T) XFactory44.SMACK_A_FUCKER;
        if(key.equals(GunConfig.I_INSPECTDURATION)) return cast(30, base);
        if(key.equals(GunConfig.I_INSPECTCANCEL)) return cast(false, base);

        return base;
    }
}
