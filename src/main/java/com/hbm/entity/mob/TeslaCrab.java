package com.hbm.entity.mob;

import com.hbm.blockentity.machine.TeslaBlockEntity;
import com.hbm.items.NtmItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityTeslaCrab.
 *
 * Die Teslakrabbe. Eine Kybernetische Krabbe mit einer Spule auf dem Ruecken: zehn
 * Lebenspunkte, etwas langsamer, und sie schlaegt bestaendig Blitze auf alles im Umkreis
 * von drei Bloecken. Der meteor_spawner stellt jede fuenfte Krabbe als diese auf.
 *
 * SIE SCHIESST TROTZDEM: die Fernkampfaufgabe der Grundkrabbe bleibt unveraendert, das
 * Original ueberschreibt sie nicht. Die Blitze kommen zusaetzlich.
 *
 * DIE BLITZE WERDEN AUF BEIDEN SEITEN GERECHNET, nicht uebertragen. Das Original macht das
 * genauso -- onLivingUpdate laeuft dort auf Client und Server, und der Renderer liest die
 * Liste, die der Client sich selbst gefuellt hat. Damit auf dem Client niemand zu Schaden
 * kommt, laeuft der Schlag dort im zeichnenden Modus.
 */
public class TeslaCrab extends CyberCrab {

    /** Die Endpunkte der Blitze, nur zum Zeichnen. */
    public List<double[]> targets = new ArrayList<>();

    public TeslaCrab(EntityType<? extends TeslaCrab> type, Level level) {
        super(type, level);

        /* ignoreFrustumCheck des Originals: ihre Blitze reichen weiter als ihr Koerper und
         * sollen nicht verschwinden, sobald sie selbst aus dem Blickfeld rutscht. */
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    public void aiStep() {

        this.targets = TeslaBlockEntity.zap(this.level(),
                this.getX(), this.getY() + 1, this.getZ(), 3, this, !this.level().isClientSide);

        super.aiStep();
    }

    /**
     * Die Kupferspule. Im Original ist sie ein dropRareDrop: der Zufall dort ist
     * rand.nextInt(200) minus Pluenderung, und faellt er unter fuenf, gibt es sie --
     * also etwa jede vierzigste erschlagene Krabbe. Die Pluenderungsverzauberung geht in
     * 1.21 nicht mehr durch diesen Aufruf; sie bleibt hier darum ohne Wirkung.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        if(recentlyHit && this.random.nextInt(200) < 5) {
            this.spawnAtLocation(NtmItems.COIL_COPPER.get());
        }
    }
}
