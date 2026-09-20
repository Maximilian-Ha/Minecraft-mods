package com.hbm.items.weapon.sedna.factory;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem.LambdaContext;
import com.hbm.items.weapon.sedna.GunBaseNTItem.WeaponQuality;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.GunDrillItem;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineLiquidEngine;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.util.EntityDamageUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryDrill.
 *
 * Der Bergbaubohrer. Er ist als Waffe gebaut, verschiesst aber nichts: sein "Schuss" ist ein
 * Griff ins Leere vor dem Spieler. Trifft er ein Wesen, macht er Schaden; trifft er einen
 * Block, bricht er ihn heraus -- und mit ihm die sechsundzwanzig Nachbarn, wenn man nicht
 * schleicht.
 *
 * SEIN MAGAZIN IST EIN TANK. Gefeuert wird gegen Kraftstoff, nicht gegen Patronen; zehn
 * Millibar je Zug. Nachgeladen wird an der Zapfsaeule.
 *
 * DIE VIER VERSTELLBAREN WERTE -- Reichweite, Ruestungsdurchschlag, Durchdringung, Umkreis --
 * laufen ueber XWeaponModManager.eval. Im Port gibt es keinen Aufsatz, der sie veraendert;
 * eval liefert dann den Grundwert. Die Wege stehen trotzdem hier, damit die Aufsaetze spaeter
 * nur noch angemeldet werden muessen.
 *
 * ABWEICHUNGEN, gemessen:
 * - Das Original nimmt stone_keyhole ausdruecklich vom Abbau aus. Diesen Block gibt es im
 *   Port nicht; die Bedingung entfaellt ersatzlos.
 * - Fuer die Bruchwolke schickt das Original zwei verschiedene Pakete (S28PacketEffect fuer
 *   den getroffenen Block, ParticleBurstPacket fuer die Nachbarn). In 1.21 verschickt
 *   destroyBlock das Ereignis 2001 schon an alle AUSSER den Brechenden; hier wird es dem
 *   Brechenden einzeln nachgereicht. Ein Paket statt zweier, dasselbe Bild.
 */
public class XFactoryDrill {

    public static final String D_REACH = "D_REACH";
    public static final String F_DTNEG = "F_DTNEG";
    public static final String F_PIERCE = "F_PIERCE";
    public static final String I_AOE = "I_AOE";

    /*
     * NICHT DABEI, und das ist gemessen: I_HARVEST. Das Original hebt mit dem Bohrkopf an, was
     * der Bohrer ueberhaupt abbauen darf (getModdableHarvestLevel). In 1.21 gibt es keine
     * Abbaustufe als Zahl mehr, und GunDrillItem.isCorrectToolForDrops gibt ohnehin stets wahr
     * zurueck -- der Bohrer bricht alles. Bis Runde 191 stand die Kennung hier trotzdem, ohne
     * dass sie irgendwer gelesen haette.
     */

    public static void init(DeferredRegister.Items registry) {

        NtmItems.GUN_DRILL = registry.register("gun_drill", () -> new GunDrillItem(WeaponQuality.UTILITY, new GunConfig()
                .dura(3_000).draw(10).inspect(55).hideCrosshair(false).crosshair(Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(10F).delay(20).dry(30).auto(true).jam(0)
                        .mag(new MagazineLiquidEngine(0, 4_000, Fluids.GASOLINE, Fluids.GASOLINE_LEADED, Fluids.COALGAS, Fluids.COALGAS_LEADED))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .canFire(Lego.LAMBDA_STANDARD_CAN_FIRE).fire(LAMBDA_DRILL_FIRE))
                .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                .anim(LAMBDA_DRILL_ANIMS).orchestra(Orchestras.ORCHESTRA_DRILL)
        ));
    }

    public static BiConsumer<ItemStack, LambdaContext> LAMBDA_DRILL_FIRE = (stack, ctx) -> doStandardFire(stack, ctx, GunAnimation.CYCLE, true);

    public static void doStandardFire(ItemStack stack, LambdaContext ctx, GunAnimation anim, boolean calcWear) {

        Player spieler = ctx.getPlayer();
        if(spieler == null) return;

        int index = ctx.configIndex;
        if(anim != null) GunBaseNTItem.playAnimation(spieler, stack, anim, index);

        Receiver primary = ctx.config.getReceivers(stack)[0];
        IMagazine<?> mag = primary.getMagazine(stack);

        HitResult treffer = EntityDamageUtil.getMouseOver(spieler, getModdableReach(stack, 5.0D));

        if(treffer instanceof EntityHitResult wesenTreffer) {

            float schaden = primary.getBaseDamage(stack);
            Entity ziel = wesenTreffer.getEntity();

            if(ziel instanceof LivingEntity lebendig) {
                EntityDamageUtil.hurtNT(lebendig, spieler.damageSources().playerAttack(spieler), schaden,
                        true, true, 0.1D, getModdableDTNegation(stack, 2F), getModdablePiercing(stack, 0.15F));
            } else {
                ziel.hurt(spieler.damageSources().playerAttack(spieler), schaden);
            }

        } else if(treffer instanceof BlockHitResult blockTreffer && treffer.getType() == HitResult.Type.BLOCK) {

            int umkreis = spieler.isShiftKeyDown() ? 0 : getModdableAoE(stack, 1);
            BlockPos mitte = blockTreffer.getBlockPos();

            breakExtraBlock(spieler.level, mitte, spieler, mitte);
            for(int i = -umkreis; i <= umkreis; i++) for(int j = -umkreis; j <= umkreis; j++) for(int k = -umkreis; k <= umkreis; k++) {
                if(i == 0 && j == 0 && k == 0) continue;
                breakExtraBlock(spieler.level, mitte.offset(i, j, k), spieler, mitte);
            }
        }

        mag.useUpAmmo(stack, ctx.container, 10);
        if(calcWear) GunBaseNTItem.setWear(stack, index, Math.min(GunBaseNTItem.getWear(stack, index), ctx.config.getDurability(stack)));
    }

    /**
     * Bricht einen einzelnen Block heraus. Laeuft nur auf dem Server: das Abbauen geht ueber
     * den Spielmodus des Spielers, damit Beute, Werkzeugverschleiss und Schutzbereiche
     * genauso greifen wie bei einer Spitzhacke.
     */
    public static void breakExtraBlock(Level level, BlockPos stelle, Player spieler, BlockPos mitte) {

        if(level.isEmptyBlock(stelle)) return;
        if(!(spieler instanceof ServerPlayer serverSpieler)) return;

        BlockState zustand = level.getBlockState(stelle);

        /* Grundgestein und Gleichwertiges bleiben stehen. */
        if(zustand.getDestroySpeed(level, stelle) == -1.0F && zustand.getDestroyProgress(serverSpieler, level, stelle) == 0.0F) return;

        boolean gebrochen = serverSpieler.gameMode.destroyBlock(stelle);

        /*
         * destroyBlock schickt die Bruchwolke an alle ausser den Brechenden -- ihm wird sie
         * hier nachgereicht, sonst saehe ausgerechnet er nichts.
         */
        if(gebrochen) serverSpieler.connection.send(new ClientboundLevelEventPacket(2001, stelle, Block.getId(zustand), false));
    }

    /*
     * Dieses System muss weder im GunConfig noch im Receiver stehen -- so geht es genauso, und
     * das ist im Original ausdruecklich so angemerkt.
     */
    public static double getModdableReach(ItemStack stack, double base) { return XWeaponModManager.eval(base, stack, D_REACH, NtmItems.GUN_DRILL.get(), 0); }
    public static float getModdableDTNegation(ItemStack stack, float base) { return XWeaponModManager.eval(base, stack, F_DTNEG, NtmItems.GUN_DRILL.get(), 0); }
    public static float getModdablePiercing(ItemStack stack, float base) { return XWeaponModManager.eval(base, stack, F_PIERCE, NtmItems.GUN_DRILL.get(), 0); }
    public static int getModdableAoE(ItemStack stack, int base) { return XWeaponModManager.eval(base, stack, I_AOE, NtmItems.GUN_DRILL.get(), 0); }

    /**
     * Die Bewegung des Bohrers. Drei Kanaele: DEPLOY fuehrt ihn nach vorn, SPIN dreht ihn, und
     * SPEED sagt dem Klang, wie schnell er laeuft.
     *
     * SPIN SETZT DORT AN, WO DIE VORIGE BEWEGUNG AUFGEHOERT HAT -- deshalb liest die
     * Animation ihren eigenen laufenden Wert aus, statt bei null anzufangen. Sonst zuckte der
     * Bohrer bei jedem Zug zurueck.
     */
    public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_DRILL_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimation()
                    .addBus("EQUIP", new BusAnimationSequence().setPos(-1, 0, 0).addPos(0, 0, 0, 750, IType.SIN_DOWN));
            case CYCLE: {
                float deploy = HbmAnimations.getRelevantTransformation("DEPLOY")[0];
                float speed = HbmAnimations.getRelevantTransformation("SPEED")[0];
                float spin = HbmAnimations.getRelevantTransformation("SPIN")[0] % 360F;
                return new BusAnimation()
                        .addBus("DEPLOY", new BusAnimationSequence().setPos(deploy, 0, 0).addPos(1, 0, 0, (int) (500 * (1 - deploy)), IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                        .addBus("SPIN", new BusAnimationSequence().setPos(spin, 0, 0).addPos(spin + 360 * 1.5, 0, 0, 1500).addPos(360 * 3, 0, 0, 750 + (int) (1000 * (1D - spin / 360D)), IType.SIN_DOWN))
                        .addBus("SPEED", new BusAnimationSequence().setPos(speed, 0, 0).addPos(1, 0, 0, 500).hold(1000).addPos(0, 0, 0, 750 + (int) (1000 * (1D - spin / 360D)), IType.SIN_DOWN));
            }
            case CYCLE_DRY: return new BusAnimation()
                    .addBus("DEPLOY", new BusAnimationSequence().addPos(0.25, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("SPIN", new BusAnimationSequence().addPos(360 * 1, 0, 0, 1500, IType.SIN_DOWN))
                    .addBus("SPEED", new BusAnimationSequence().addPos(0.75, 0, 0, 250).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            case INSPECT: return new BusAnimation()
                    .addBus("LIFT", new BusAnimationSequence().addPos(-45, 0, 0, 500, IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            default: return null;
        }
    };
}
