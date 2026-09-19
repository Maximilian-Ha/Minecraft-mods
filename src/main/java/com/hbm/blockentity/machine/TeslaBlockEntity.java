package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.lib.Library;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.ArmorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityTesla.
 *
 * Die Teslaspule schlaegt jeden Tick, fuer den sie Strom hat, nach allem Lebendigen im
 * Umkreis von zehn Bloecken -- ohne Zielwahl, ohne Freund und Feind zu unterscheiden.
 * Wer eine geschlossene leitende Ruestung traegt, bleibt verschont: der faradaysche
 * Kaefig steht in ArmorUtil.checkForFaraday.
 *
 * Der Schaden haengt an der Lebenskraft des Getroffenen (die Haelfte, begrenzt auf drei
 * bis zwanzig) und wird auf alle Ziele im Kasten aufgeteilt -- auch auf die, die der
 * Blitz gar nicht erreicht. Das ist im Original so und bleibt hier so: je voller der
 * Raum, desto schwaecher der einzelne Schlag.
 *
 * Steht der Sternmetall-Generator direkt darunter, ist die Spule immer voll geladen.
 *
 * NICHT UEBERNOMMEN: die drei Sonderfaelle fuer Krabben (Taint-, Tesla- und Cyberkrabbe).
 * Im Original heilen die ersten beiden am Blitz und die dritte bleibt unbehelligt; die
 * drei Wesen gibt es im Port noch nicht. Kommen sie dazu, gehoeren die Faelle hierher
 * zurueck.
 */
public class TeslaBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2 {

    public static final long MAX_POWER = 100_000L;

    /** Verbrauch je Schlag, also je Tick. */
    private static final long VERBRAUCH = 5_000L;

    public static final double REICHWEITE = 10D;

    /** Hoehe der Kugel ueber der Blockunterkante -- Ausgangspunkt jedes Blitzes. */
    public static final double HOEHE = 1.75D;

    public long power;

    /** Die Endpunkte der Blitze, nur zum Zeichnen. Der Server fuellt sie, der Client malt sie. */
    public List<double[]> targets = new ArrayList<>();

    public TeslaBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.TESLA.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        for(Direction dir : Direction.values()) {
            this.trySubscribe(this.level, this.worldPosition.relative(dir), dir);
        }

        this.targets.clear();

        if(this.level.getBlockState(this.worldPosition.below()).is(NtmBlocks.METEOR_BATTERY.get())) {
            this.power = MAX_POWER;
        }

        if(this.power >= VERBRAUCH) {
            this.power -= VERBRAUCH;
            this.targets = zap(this.level,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + HOEHE,
                    this.worldPosition.getZ() + 0.5D,
                    REICHWEITE, null);
        }

        this.networkPackNT(100);
    }

    /**
     * Ein Schlag von (x, y, z) aus. Gibt die Punkte zurueck, an denen ein Blitz endet.
     *
     * @param quelle wird uebersprungen -- so trifft eine tragbare Quelle nicht den Traeger.
     */
    public static List<double[]> zap(Level level, double x, double y, double z, double radius, Entity quelle) {

        List<double[]> enden = new ArrayList<>();

        List<LivingEntity> ziele = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));

        for(LivingEntity e : ziele) {

            if(e instanceof Ocelot || e == quelle) continue;

            Vec3 richtung = new Vec3(e.getX() - x, e.getY() + e.getBbHeight() / 2 - y, e.getZ() - z);
            if(richtung.length() > radius) continue;

            if(Library.isObstructed(level, x, y, z, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ())) continue;

            // Der Strom zuendet den Creeper, statt ihn zu verletzen.
            if(e instanceof Creeper creeper) {
                creeper.ignite();
                enden.add(new double[] { e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ() });
                continue;
            }

            if(!(e instanceof Player spieler && ArmorUtil.checkForFaraday(spieler))) {

                DamageSource quell = level.damageSources().source(NtmDamageTypes.ELECTRICITY);
                float schaden = Mth.clamp(e.getMaxHealth() * 0.5F, 3F, 20F) / ziele.size();

                if(e.hurt(quell, schaden)) {
                    level.playSound(null, e.getX(), e.getY(), e.getZ(),
                            NtmSoundEvents.WEAPON_TESLA.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }

            enden.add(new double[] { e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ() });
        }

        return enden;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeShort(this.targets.size());
        for(double[] ende : this.targets) {
            buf.writeDouble(ende[0]);
            buf.writeDouble(ende[1]);
            buf.writeDouble(ende[2]);
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        int anzahl = buf.readShort();

        this.targets.clear();
        for(int i = 0; i < anzahl; i++) {
            this.targets.add(new double[] { buf.readDouble(), buf.readDouble(), buf.readDouble() });
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    /* Ohne @Override: getRenderBoundingBox kommt aus der NeoForge-Erweiterung von
     * BlockEntity und gilt dem Uebersetzer nicht als ueberschrieben. Die Blitze reichen
     * bis zehn Bloecke weit, der Kasten muss also mitwachsen. */
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(REICHWEITE + HOEHE);
    }
}
