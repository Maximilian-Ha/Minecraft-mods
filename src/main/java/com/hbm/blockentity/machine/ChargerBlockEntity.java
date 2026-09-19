package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.ChargerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityCharger.
 *
 * Eine Ladestation, die keinen eigenen Speicher hat: sie meldet als Fassungsvermoegen genau
 * so viel, wie die Batterien der davorstehenden Spieler in diesem Tick aufnehmen koennen,
 * und reicht den ankommenden Strom unmittelbar weiter.
 *
 * Der Arm faehrt in zwanzig Ticks aus und ebenso lange wieder ein; geladen wird erst, wenn
 * er ganz draussen ist.
 */
public class ChargerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2 {

    /** Wie lange der Arm zum Ausfahren braucht. */
    public static final int DAUER = 20;

    public int ausfahrt;
    public int letzteAusfahrt;

    private final List<Player> spieler = new ArrayList<>();
    private long aufnahme = 0;
    private int nachlauf = 0;
    private boolean teilchen = false;

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CHARGER.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        Direction vorn = this.getBlockState().getValue(ChargerBlock.FACING).getOpposite();

        if(!this.level.isClientSide) {

            // Das Kabel steckt in der Wand hinter dem Geraet.
            Direction rueck = vorn.getOpposite();
            this.trySubscribe(this.level, this.worldPosition.relative(rueck), rueck);

            this.spieler.clear();
            this.spieler.addAll(this.level.getEntitiesOfClass(Player.class, new AABB(
                    this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5,
                    this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5)
                    .inflate(0.5, 0.0, 0.5)));

            this.aufnahme = 0;
            for(Player p : this.spieler) {
                for(ItemStack stack : ausruestung(p)) {
                    if(stack.getItem() instanceof IBatteryItem batterie) {
                        this.aufnahme += Math.min(batterie.getMaxCharge(stack) - batterie.getCharge(stack),
                                batterie.getChargeRate(stack));
                    }
                }
            }

            this.teilchen = this.nachlauf > 0;

            if(this.teilchen) {
                this.nachlauf--;
                if(this.level.getGameTime() % 20 == 0) {
                    this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 0.5F);
                }
            }

            this.networkPackNT(50);
        }

        this.letzteAusfahrt = this.ausfahrt;

        // Die Bewegung laeuft auf beiden Seiten mit, die Toene nur auf der Serverseite:
        // das Original ruft playSoundEffect auch auf dem Client und spielt sie doppelt.
        if((this.aufnahme > 0 || this.teilchen) && this.ausfahrt < DAUER) {
            this.ausfahrt++;
            if(this.ausfahrt == 2 && !this.level.isClientSide) {
                this.level.playSound(null, this.worldPosition, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 0.5F);
            }
        }

        if((this.aufnahme <= 0 && !this.teilchen) && this.ausfahrt > 0) {
            this.ausfahrt--;
            if(this.ausfahrt == 4 && !this.level.isClientSide) {
                this.level.playSound(null, this.worldPosition, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 0.5F);
            }
        }

        if(this.teilchen && this.level.isClientSide) {
            RandomSource zufall = this.level.getRandom();
            this.level.addParticle(ParticleTypes.ENCHANTED_HIT,
                    this.worldPosition.getX() + 0.5 + zufall.nextDouble() * 0.0625 + vorn.getStepX() * 0.75,
                    this.worldPosition.getY() + 0.1,
                    this.worldPosition.getZ() + 0.5 + zufall.nextDouble() * 0.0625 + vorn.getStepZ() * 0.75,
                    -vorn.getStepX() + zufall.nextGaussian() * 0.1,
                    0,
                    -vorn.getStepZ() + zufall.nextGaussian() * 0.1);
        }
    }

    /** Hand und Ruestung -- im Original die fuenf Ausruestungsplaetze 0 bis 4. */
    private static Iterable<ItemStack> ausruestung(Player spieler) {
        List<ItemStack> stapel = new ArrayList<>();
        stapel.add(spieler.getMainHandItem());
        for(EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD }) {
            stapel.add(spieler.getItemBySlot(slot));
        }
        return stapel;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.aufnahme);
        buf.writeBoolean(this.teilchen);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.aufnahme = buf.readLong();
        this.teilchen = buf.readBoolean();
    }

    /* Kein eigener Speicher: das Geraet meldet als Fassungsvermoegen, was die Batterien
     * gerade aufnehmen koennen, und behaelt selbst nichts. */
    @Override public long getPower() { return 0; }
    @Override public void setPower(long power) { }
    @Override public long getMaxPower() { return this.aufnahme; }

    @Override
    public long transferPower(long power) {

        if(this.ausfahrt < DAUER || power == 0) return power;

        for(Player p : this.spieler) {
            for(ItemStack stack : ausruestung(p)) {

                if(!(stack.getItem() instanceof IBatteryItem batterie)) continue;

                long menge = Math.min(batterie.getMaxCharge(stack) - batterie.getCharge(stack),
                        batterie.getChargeRate(stack));
                menge = Math.min(menge, Math.max(power / 5, 1));
                batterie.chargeBattery(stack, menge);
                power -= menge;

                this.nachlauf = 4;
            }
        }

        return power;
    }
}
