package com.hbm.entity.logic;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.ParachuteCrate;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsC130;
import com.hbm.util.Vec3NT;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.EnumUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.logic.EntityC130.
 *
 * Das Transportflugzeug. Eine Leuchtpatrone ruft es; es fliegt einmal ueber die Stelle hinweg
 * und wirft auf halber Strecke eine Kiste am Fallschirm ab. Was in der Kiste liegt, haengt an
 * der Farbe der Patrone: blau bringt Nachschub, gruen Waffen und Munition.
 *
 * ES FLIEGT VON WEIT HER: hundert Bloecke vor dem Ziel wird es eingesetzt, hundert Bloecke
 * ueber dem Boden, und es haelt seine Richtung bis zum Ende seiner Lebensdauer. Die Kiste
 * faellt sieben Tickschritte HINTER ihm, zehn Bloecke tiefer -- so sieht es aus, als waere sie
 * aus der Heckklappe gerutscht.
 *
 * DIE DRITTE NUTZLAST, A_FUCKING_FUEL_TRUCK, steht auch im Original nur in der Aufzaehlung;
 * abgeworfen wird sie dort nirgends. Sie bleibt deshalb auch hier leer.
 */
public class C130 extends PlaneBase {

    public Nutzlast nutzlast = Nutzlast.SUPPLIES;

    protected AudioWrapper audio;

    public C130(EntityType<? extends C130> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level.isClientSide) {
            if(this.getHealth() > 0) {
                if(this.audio == null || !this.audio.isPlaying()) {
                    this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.BOMBER_LOOP.get(), SoundSource.HOSTILE,
                            (float) this.getX(), (float) this.getY(), (float) this.getZ(), 2F, 250F, 1F, 20);
                    this.audio.startSound();
                }
                this.audio.keepAlive();
                this.audio.updatePosition((float) this.getX(), (float) this.getY(), (float) this.getZ());
            } else if(this.audio != null && this.audio.isPlaying()) {
                this.audio.stopSound();
                this.audio = null;
            }
            return;
        }

        if(this.tickCount != this.getLifetime() / 2) return;
        if(this.getHealth() <= 0) return;

        this.wirfAb();
    }

    private void wirfAb() {

        ParachuteCrate kiste = new ParachuteCrate(NtmEntityTypes.PARACHUTE_CRATE.get(), this.level);
        kiste.setPos(this.getX() - this.getDeltaMovement().x * 7,
                this.getY() - 10,
                this.getZ() - this.getDeltaMovement().z * 7);

        if(this.nutzlast == Nutzlast.SUPPLIES) {
            for(int i = 0; i < 5; i++) kiste.items.add(ItemPool.get(ItemPoolsC130.POOL_SUPPLIES).draw(this.random));
        }

        if(this.nutzlast == Nutzlast.WEAPONS) {
            int waffen = 1 + this.random.nextInt(2);
            for(int i = 0; i < waffen; i++) kiste.items.add(ItemPool.get(ItemPoolsC130.POOL_WEAPONS).draw(this.random));
            for(int i = 0; i < 6; i++) kiste.items.add(ItemPool.get(ItemPoolsC130.POOL_AMMO).draw(this.random));
        }

        this.level.addFreshEntity(kiste);
    }

    /**
     * Setzt das Flugzeug so ein, dass es ueber die angegebene Stelle hinwegfliegt: hundert
     * Bloecke davor und hundert darueber, mit einer zufaelligen Richtung.
     */
    public void fac(Level level, double x, double y, double z, Nutzlast nutzlast) {

        Vec3NT richtung = new Vec3NT(level.random.nextDouble() - 0.5, 0, level.random.nextDouble() - 0.5);
        richtung = richtung.normalizeSelf();
        richtung.xCoord *= 2;
        richtung.zCoord *= 2;

        this.nutzlast = nutzlast;

        this.moveTo(x - richtung.xCoord * 100, y + 100, z - richtung.zCoord * 100, 0.0F, 0.0F);
        this.setDeltaMovement(richtung.xCoord, 0, richtung.zCoord);

        this.rotation();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nutzlast = EnumUtil.grabEnumSafely(Nutzlast.class, tag.getInt("payload"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("payload", this.nutzlast.ordinal());
    }

    /** Was das Flugzeug geladen hat. Die Namen sind die des Originals. */
    public enum Nutzlast {
        SUPPLIES,
        WEAPONS,
        A_FUCKING_FUEL_TRUCK
    }
}
