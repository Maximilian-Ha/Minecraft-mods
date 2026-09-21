package com.hbm.items.tools;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ModEffect;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import com.hbm.entity.projectile.Rubble;
import com.hbm.entity.NtmEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.function.BiConsumer;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmCriteria;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.function.Consumer;

public class SpecialSwordItem extends SwordItem {

    public static final Consumer<LivingEntity> LAMBDA_OPENER_HURT_ENEMY = (target) -> {
        Level level = target.level;

        if(!level.isClientSide) {
            int i = level.random.nextInt(7);
            if(i == 0) target.addEffect(new MobEffectInstance(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 60 * 20, 0)));
            if(i == 1) target.addEffect(new MobEffectInstance(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 60 * 20, 2)));
            if(i == 2) target.addEffect(new MobEffectInstance(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5 * 60 * 20, 2)));
            if(i == 3) target.addEffect(new MobEffectInstance(new MobEffectInstance(MobEffects.CONFUSION, 1 * 60 * 20, 0)));
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_LAND, SoundSource.AMBIENT, 3.0F, 1.0F);
        }
    };

    /** Der Holzhammer kann nichts ausser Krach machen -- "Thunk!". */
    public static final Consumer<LivingEntity> LAMBDA_GAVEL_WOOD_HURT_ENEMY = (target) -> {
        Level level = target.level;

        if(!level.isClientSide) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    NtmSoundEvents.WEAPON_WHACK.get(), SoundSource.PLAYERS, 3.0F, 1.0F);
        }
    };

    /**
     * Der Bleihammer spricht das Urteil: fuenfzehn Sekunden Bleivergiftung der fuenften Stufe.
     * Das Original schreibt dafuer HbmPotion.lead mit Amplifier 4.
     */
    public static final Consumer<LivingEntity> LAMBDA_GAVEL_LEAD_HURT_ENEMY = (target) -> {
        Level level = target.level;

        if(!level.isClientSide) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    NtmSoundEvents.WEAPON_WHACK.get(), SoundSource.PLAYERS, 3.0F, 1.0F);
            target.addEffect(new MobEffectInstance(ModEffect.LEAD, 15 * 20, 4));
        }
    };

    /**
     * Der Diamanthammer aus dem Original: er nimmt dem Ziel ein Drittel seiner HOECHSTEN
     * Lebenspunkte ab, unabhaengig von Ruestung und Schadensberechnung. Daher der Spruch im
     * Original -- "Deals as much damage as it needs to".
     */
    public static final Consumer<LivingEntity> LAMBDA_GAVEL_HURT_ENEMY = (target) -> {
        Level level = target.level;

        if(!level.isClientSide) {
            target.setHealth(target.getHealth() - target.getMaxHealth() / 3F);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    NtmSoundEvents.WEAPON_WHACK.get(), SoundSource.PLAYERS, 3.0F, 1.0F);
        }
    };

    /**
     * Der Schimmerhammer schleudert sein Ziel in Blickrichtung des Angreifers davon -- fuenffach
     * genommen, wie im Original (WeaponSpecial.java:91-101). Ohne hurtMarked sieht der Client
     * den Stoss nicht; in 1.7.10 uebernahm das die Bewegungssynchronisierung von selbst.
     */
    public static final BiConsumer<LivingEntity, LivingEntity> LAMBDA_SHIMMER_SLEDGE_HURT_ENEMY = (target, attacker) -> {
        Level level = target.level();
        if(level.isClientSide) return;

        Vec3 blick = attacker.getLookAngle();
        target.setDeltaMovement(target.getDeltaMovement().add(blick.scale(5D)));
        target.hurtMarked = true;

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                NtmSoundEvents.WEAPON_BANG.get(), SoundSource.PLAYERS, 3.0F, 1.0F);
    };

    /** Die Schimmeraxt halbiert die AKTUELLE Lebensenergie des Ziels, nicht die hoechste. */
    public static final Consumer<LivingEntity> LAMBDA_SHIMMER_AXE_HURT_ENEMY = (target) -> {
        Level level = target.level();
        if(level.isClientSide) return;

        target.setHealth(target.getHealth() / 2F);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                NtmSoundEvents.WEAPON_SLICE.get(), SoundSource.PLAYERS, 3.0F, 1.0F);
    };

    /**
     * Der Hammer schlaegt einen Block aus der Wand und schleudert ihn als Truemmerstueck davon.
     *
     * DIE GRENZE 6000 steht so im Original: alles mit dieser Sprengfestigkeit oder mehr bleibt
     * stehen -- Grundgestein und was ihm gleicht.
     */
    public static final Consumer<UseOnContext> LAMBDA_SHIMMER_SLEDGE_USE_ON = (context) -> {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player spieler = context.getPlayer();

        if(state.isAir() || spieler == null) return;
        if(state.getBlock().getExplosionResistance() >= 6000F) return;

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                NtmSoundEvents.WEAPON_BANG.get(), SoundSource.PLAYERS, 3.0F, 1.0F);

        if(level.isClientSide) return;

        Rubble truemmer = new Rubble(NtmEntityTypes.RUBBLE.get(), level);
        truemmer.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        truemmer.setBlock(state.getBlock());
        truemmer.setDeltaMovement(truemmer.getDeltaMovement().add(spieler.getLookAngle().scale(5D)));

        level.addFreshEntity(truemmer);
        level.destroyBlock(pos, false);
    };

    /**
     * Die Axt schlaegt eine Spalte aus drei Bloecken heraus -- den angeklickten und je einen
     * darueber und darunter. Auch hier gilt die Grenze 6000, und auch hier faellt nichts.
     */
    public static final Consumer<UseOnContext> LAMBDA_SHIMMER_AXE_USE_ON = (context) -> {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                NtmSoundEvents.WEAPON_KAPENG.get(), SoundSource.PLAYERS, 3.0F, 1.0F);

        if(level.isClientSide) return;

        for(BlockPos ziel : new BlockPos[] { pos, pos.above(), pos.below() }) {
            BlockState state = level.getBlockState(ziel);
            if(state.isAir() || state.getBlock().getExplosionResistance() >= 6000F) continue;
            level.destroyBlock(ziel, false);
        }
    };

    @Nullable private Consumer<LivingEntity> hurtEnemy;
    /** Fuer Waffen, die auch den Angreifer brauchen -- der Hammer stoesst in dessen Blickrichtung. */
    @Nullable private BiConsumer<LivingEntity, LivingEntity> hurtEnemyMitAngreifer;
    @Nullable private Consumer<UseOnContext> useOn;

    public SpecialSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public SpecialSwordItem setHurtEnemy(Consumer<LivingEntity> lambda) {
        this.hurtEnemy = lambda;
        return this;
    }

    public SpecialSwordItem setHurtEnemy(BiConsumer<LivingEntity, LivingEntity> lambda) {
        this.hurtEnemyMitAngreifer = lambda;
        return this;
    }

    public SpecialSwordItem setUseOn(Consumer<UseOnContext> lambda) {
        this.useOn = lambda;
        return this;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if(hurtEnemy != null) this.hurtEnemy.accept(target);
        if(hurtEnemyMitAngreifer != null) this.hurtEnemyMitAngreifer.accept(target, attacker);

        return true;
    }

    /**
     * Die beiden Fiend-Erfolge. Im Original steht das in WeaponSpecial.onUpdate, also bei
     * JEDER Sonderwaffe -- die Pruefung selbst grenzt es ein (ArmorUtil.checkForFiend):
     * die Jacke am Leib UND die passende Waffe in der Hand. Eine Waffe allein reicht nicht,
     * und eine Jacke allein auch nicht.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {

        if(level.isClientSide || !selected) return;
        if(!(entity instanceof ServerPlayer spieler)) return;

        ItemStack brust = spieler.getItemBySlot(EquipmentSlot.CHEST);

        if(stack.is(NtmItems.SHIMMER_SLEDGE.get()) && brust.is(NtmItems.JACKT.get())) {
            NtmCriteria.marke(spieler, "fiend");
        } else if(stack.is(NtmItems.SHIMMER_AXE.get()) && brust.is(NtmItems.JACKT2.get())) {
            NtmCriteria.marke(spieler, "fiend2");
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        if(this.useOn == null) return super.useOn(context);

        this.useOn.accept(context);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        for(String s : ITooltipProvider.getDescription(stack)) components.add(Component.translatable(s).withStyle(ChatFormatting.GRAY));
    }
}
