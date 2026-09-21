package com.hbm.entity.mob.glyphid;

import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.entity.projectile.Rubble;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Vec3NT;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidDigger.
 *
 * Der Digger schlaegt auf den Boden. Alle sechs Sekunden reisst er einen Faecher aus
 * Bloecken vor sich heraus und wirft ihn seinem Ziel entgegen -- jeder herausgerissene
 * Block wird zu einem Truemmerstueck, das fuenfzehn Schaden macht, wenn es jemanden trifft.
 *
 * DER FAECHER, genau wie im Original: ein waagerechter Strahl von sechs Bloecken Laenge in
 * Blickrichtung auf seiner eigenen Hoehe, danach acht weitere, jeweils um ein Sechzehntel
 * Bogenmass weitergedreht, aber EINEN BLOCK TIEFER. Die Drehung staut sich auf, der Faecher
 * wandert also im Uhrzeigersinn davon -- neun Strahlen, halbes Bogenmass. Die Stellen werden
 * abgeschnitten, nicht gerundet, deshalb kommen nahe Stellen mehrfach vor; das Herausreissen
 * ist beim zweiten Mal wirkungslos, und genau so verhaelt es sich im Original.
 *
 * WAS ER HERAUSREISST: alles, dessen Sprengfestigkeit unter der von Beton liegt (84), was
 * ein voller Block ist, was nicht zu einem Mehrblockbau gehoert und was keinen Blockinhalt
 * hat. Fallen gelassen wird nichts -- der Block fliegt, er faellt nicht.
 *
 * DIE WURFBAHN ist die des Bombardiers mit anderen Zahlen: v0 1,2 und g 0,03, die Schwerkraft
 * des gewoehnlichen Geschosses. Findet die Rechnung keinen Winkel, fliegen die Truemmer
 * trotzdem -- nur eben ungerichtet. Auch das steht so im Original.
 *
 * DIE VORHERSAGE IST AUCH HIER WIRKUNGSLOS, wie beim Brawler und aus denselben zwei Gruenden:
 * lastX/lastY/lastZ werden im selben Takt gesetzt und gelesen, und lastTarget wird nie belegt.
 * Nachgemessen. Der Port baut die Felder deshalb nicht nach.
 */
public class GlyphidDigger extends Glyphid {

    /** Takte bis zum naechsten Schlag. */
    public int timer = 0;

    public GlyphidDigger(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().digger;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_DIGGER_TEX;
    }

    @Override
    public double getGlyphidScale() {
        return 1.3D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().digger;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.getTarget() == null || !this.isAlive()) return;

        if(--this.timer <= 0) {
            this.bodenschlag();
            this.timer = 120;
        }
    }

    /** Ein Schlag. Nur auf dem Server, nur auf Lebendes, nur unter dreissig Bloecken. */
    public void bodenschlag() {

        if(this.level().isClientSide) return;

        Entity ziel = this.getTarget();
        if(!(ziel instanceof LivingEntity) || this.distanceTo(ziel) >= 30) return;

        int bugX = (int) this.getX();
        int bugY = (int) this.getY();
        int bugZ = (int) this.getZ();

        Vec3NT richtung = new Vec3NT(this.getLookAngle());

        List<BlockPos> stellen = new ArrayList<>(Library.getBlockPosInPath(bugX, bugY, bugZ, 6, richtung.toVec3()));

        for(int i = 0; i < 8; i++) {
            richtung.rotateAroundYRad(-1D / 16D);
            stellen.addAll(Library.getBlockPosInPath(bugX, bugY - 1, bugZ, 6, richtung.toVec3()));
        }

        Vec3 wurf = this.wurfbahn(ziel);

        for(BlockPos stelle : stellen) {

            BlockState zustand = this.level().getBlockState(stelle);

            if(zustand.getBlock().getExplosionResistance() >= NtmBlocks.CONCRETE.get().getExplosionResistance()) continue;
            if(!zustand.isSolidRender(this.level(), stelle)) continue;
            if(zustand.getBlock() instanceof DummyableBlock) continue;
            if(this.level().getBlockEntity(stelle) != null) continue;

            Rubble truemmer = new Rubble(NtmEntityTypes.RUBBLE.get(), this.level());
            truemmer.setPos(stelle.getX() + 0.5D, stelle.getY() + 2, stelle.getZ() + 0.5D);
            truemmer.setBlock(zustand.getBlock());

            /* ProjectileNT.getMovementToShoot passt hier NICHT: es normiert die Richtung nicht
             * und streut dreieckig mit dem Pfeilwert. Das Original wirft die Truemmer als
             * Wurfkoerper, nicht als Pfeil. */
            if(wurf != null) {
                truemmer.setDeltaMovement(BobMathUtil.throwableHeading(
                        this.random, wurf.x, wurf.y, wurf.z, 1.2F, this.random.nextFloat()));
            }

            this.level().addFreshEntity(truemmer);
            this.level().removeBlock(stelle, false);
        }
    }

    /**
     * Die Wurfrichtung fuer die Truemmer, sonst null. Dieselbe Aufloesung der Wurfparabel wie
     * beim Bombardier: flache Bahn fuer nahe Ziele, steile fuer ferne ab zwanzig Bloecken.
     */
    private Vec3 wurfbahn(Entity ziel) {

        boolean hochwurf = this.distanceTo(ziel) > 20;

        Vec3NT delta = new Vec3NT(
                ziel.getX() - this.getX(),
                (ziel.getY() + ziel.getBbHeight() / 2) - (this.getY() + 1),
                ziel.getZ() - this.getZ());

        if(delta.length() < 3) return null;

        double zielGier = -Math.atan2(delta.xCoord, delta.zCoord);

        double x = Math.sqrt(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
        double y = delta.yCoord;
        double v0 = 1.2D;
        double v02 = v0 * v0;
        double g = 0.03D;
        double zweig = hochwurf ? 1 : -1;
        double zielNeigung = Math.atan((v02 + Math.sqrt(v02 * v02 - g * (g * x * x + 2 * y * v02)) * zweig) / (g * x));

        if(Double.isNaN(zielNeigung)) return null;

        Vec3NT wurf = new Vec3NT(v0, 0, 0);
        wurf.rotateAroundZRad(-zielNeigung);
        wurf.rotateAroundYRad(-(zielGier + Math.PI * 0.5));

        return wurf.toVec3();
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden * 0.25, 2), 100);
    }

    @Override
    protected boolean canDig() {
        return true;
    }
}
