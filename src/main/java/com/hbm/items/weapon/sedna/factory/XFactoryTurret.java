package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.items.NtmItems;
import com.hbm.items.special.PolaroidItem;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.SpentCasing.SpentCasingType;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo240Shell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.BiConsumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.sedna.factory.XFactoryTurret.
 *
 * Die Munition, die es nur in Geschuetztuermen gibt: die 240-mm-Granaten des Kanonenturms und die
 * Patronen des Nahbereichsgeschuetzes.
 *
 * Die fuenf Granaten unterscheiden sich darin, was beim Aufschlag geschieht: die gewoehnliche
 * sprengt einfach, die Sprenggranate zusaetzlich mit Wucht gegen Bloecke, die beiden Wuchtgeschosse
 * durchschlagen und sprengen gar nicht -- und die W9 ist eine Atomgranate.
 *
 * ABWEICHUNG: der PlayerProcessorStandard des Originals, der Spieler beim Sprengen gesondert
 * behandelt, hat im Port keine Entsprechung -- der Port kennt nur den Entitaetenverarbeiter.
 */
public class XFactoryTurret {

    public static BulletConfig dgk_normal;

    public static SpentCasing CASING240MM = new SpentCasing(SpentCasingType.BOTTLENECK).setScale(7.5F).setBounceMotion(0.02F, 0.05F).setColor(SpentCasing.COLOR_CASE_BRASS).setupSmoke(1F, 0.5D, 60, 20);

    public static BulletConfig shell_normal;
    public static BulletConfig shell_explosive;
    public static BulletConfig shell_ap;
    public static BulletConfig shell_du;
    public static BulletConfig shell_w9;

    public static void init() {

        dgk_normal = new BulletConfig().setItem(NtmItems.AMMO_DGK);

        shell_normal = new BulletConfig().setItem(Ammo240Shell.STOCK).setDamage(1F)
                .setCasing(CASING240MM.clone().register("240standard"))
                .setOnImpact((bullet, hr) -> { Lego.standardExplode(bullet, hr, 10F); bullet.discard(); });

        shell_explosive = new BulletConfig().setItem(Ammo240Shell.EXPLOSIVE).setDamage(1.5F)
                .setCasing(CASING240MM.clone().register("240ext"))
                .setOnImpact((bullet, hr) -> {
                    Vec3 position = hr.getLocation();
                    ExplosionVNT vnt = new ExplosionVNT(bullet.level, position.x, position.y, position.z, 10F, bullet.getOwner());
                    vnt.setBlockAllocator(new BlockAllocatorStandard());
                    vnt.setBlockProcessor(new BlockProcessorStandard());
                    vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage));
                    vnt.setPlayerProcessor(new PlayerProcessorStandard());
                    vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
                    vnt.explode();
                    bullet.discard();
                });

        shell_ap = new BulletConfig().setItem(Ammo240Shell.APFSDS_T).setDamage(2F).setDoesPenetrate(true)
                .setCasing(CASING240MM.clone().register("240w"));

        shell_du = new BulletConfig().setItem(Ammo240Shell.APFSDS_DU).setDamage(2.5F).setDoesPenetrate(true).setDamageFalloffByPen(false)
                .setCasing(CASING240MM.clone().register("240u"));

        shell_w9 = new BulletConfig().setItem(Ammo240Shell.W9).setDamage(2.5F)
                .setCasing(CASING240MM.clone().register("240n"))
                .setOnImpact(LAMBDA_NUKE_SHELL);
    }

    /**
     * Die Atomgranate. Portiert aus XFactoryCatapult.LAMBDA_NUKE_STANDARD des Originals -- der
     * Katapultzweig steht im Port noch nicht, die W9 braucht denselben Einschlag aber schon jetzt.
     * Der Sprengradius ist klein; was sie ausmacht, ist die Strahlung ringsum und die Wolke.
     */
    public static BiConsumer<BulletBaseMK4, HitResult> LAMBDA_NUKE_SHELL = (bullet, hr) -> {

        if(!bullet.isAlive()) return;
        bullet.discard();

        Vec3 position = hr.getLocation();

        ExplosionVNT vnt = new ExplosionVNT(bullet.level, position.x, position.y, position.z, 10F, bullet.getOwner());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, bullet.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        ExplosionNukeGeneric.incrementRad(bullet.level, position.x, position.y, position.z, 1F);

        SoundUtils.playAtVec3(bullet.level, position, NtmSoundEvents.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

        if(bullet.level instanceof ServerLevel serverLevel) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", "muke");
            tag.putBoolean("balefire", PolaroidItem.polaroidID == 11 || serverLevel.random.nextInt(100) == 0);
            PacketDistributor.sendToPlayersNear(serverLevel, null, position.x, position.y + 0.5, position.z, 250,
                    new AuxParticle(tag, position.x, position.y + 0.5, position.z));
        }
    };
}
