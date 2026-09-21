package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.menus.MachineFELMenu;
import com.hbm.items.machine.FelCrystalItem;
import com.hbm.items.machine.FelCrystalItem.Wellenlaenge;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFEL.
 *
 * Der Freie-Elektronen-Laser. Er tut selbst nichts Nuetzliches -- er schiesst einen Strahl
 * einer Wellenlaenge geradeaus, und was in dieser Bahn steht, hat ein Problem. Sein Zweck ist
 * die SILEX: trifft der Strahl eine, bekommt sie fuer diesen Tick seinen Modus, und nur damit
 * laufen ihre Rezepte.
 *
 * DER KRISTALL BESTIMMT ALLES. Ohne Kristall im Schacht ist der Modus NULL, dann zieht die
 * Maschine keinen Strom und der Strahl bleibt aus. Mit Kristall kostet ein Tick
 * 1250 * 3^Stufe Strom -- infrarot 3750, Digamma gut 300.000.
 *
 * DIE BAHN IST 24 BLOECKE LANG und laeuft einen Block ueber dem Kern. Durchsichtiges laesst
 * sie durch; das erste undurchsichtige haelt sie auf, und dort faengt es mit einem Fuenftel
 * Wahrscheinlichkeit zu brennen an, sofern der Block nicht sprengfester als 75 ist.
 *
 * WER IN DER BAHN STEHT: sichtbares Licht blendet und zuendet, infrarot und ultraviolett
 * zuenden, Gamma verstrahlt, Digamma tut das, was Digamma tut. Das Original schreibt diesen
 * Block mit einem absichtlichen Durchfall im switch -- sichtbares Licht blendet UND zuendet.
 *
 * ABWEICHUNG, GEMESSEN: das Original setzt missingValidSilex nur einmal auf false und nie
 * wieder auf true. Die ERR.-Anzeige im Fenster kommt damit nach dem ersten Treffer nie zurueck,
 * auch wenn die SILEX abgebaut wird. Hier wird die Marke vor jedem Durchlauf zurueckgesetzt;
 * damit zeigt das Fenster das, was sein Name sagt.
 *
 * ABWEICHUNG: das Original setzt bei Digamma und Polaroid 11 den Block digamma_matter. Den
 * gibt es im Port nicht; es bleibt beim Digammafeuer, das auch sonst dort steht.
 */
public class MachineFELBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2 {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_CRYSTAL = 1;

    public static final long MAX_POWER = 20_000_000;
    /** Grundverbrauch je Tick; die Wellenlaenge verdreifacht ihn je Stufe. */
    public static final int POWER_REQ = 1250;
    /** Wie weit der Strahl reicht. */
    public static final int RANGE = 24;

    public long power;
    public Wellenlaenge mode = Wellenlaenge.NULL;
    public boolean isOn;
    /** Ob der Strahl gerade keine brauchbare SILEX trifft -- das Fenster zeigt es als ERR. */
    public boolean missingValidSilex = true;
    /** Wie weit der Strahl kommt, bevor ihn etwas aufhaelt. */
    public int distance;

    private int audioDuration;
    private AudioWrapper audio;

    public MachineFELBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_FEL.get(), pos, state, 2);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineFEL");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.updateAudio();
            return;
        }

        Direction dir = this.getFacing();

        this.trySubscribe(this.level, this.worldPosition.relative(dir, -5).above(), dir.getOpposite());
        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);

        this.mode = this.readCrystal();

        int req = this.mode == Wellenlaenge.NULL ? 0 : (int) (POWER_REQ * Math.pow(3, this.mode.ordinal()));

        /* Reicht der Strom nicht, verpufft der Rest -- so steht es im Original. */
        if(this.isOn && this.mode != Wellenlaenge.NULL && this.power < req) this.power = 0;

        if(this.isOn && this.mode != Wellenlaenge.NULL && this.power >= req) {
            this.burnEntities(dir);
            this.power -= req;
            this.fireBeam(dir);
        }

        this.networkPackNT(250);
    }

    /** Die Wellenlaenge des Kristalls im Schacht, oder NULL. */
    private Wellenlaenge readCrystal() {

        if(!this.isOn) return Wellenlaenge.NULL;

        ItemStack crystal = this.slots.get(SLOT_CRYSTAL);
        if(crystal.getItem() instanceof FelCrystalItem item) return item.getWellenlaenge();

        return Wellenlaenge.NULL;
    }

    /** Alles Lebende in der Bahn, so weit der Strahl zuletzt gekommen ist. */
    private void burnEntities(Direction dir) {

        int reach = this.distance - 1;
        BlockPos end = this.worldPosition.relative(dir, reach).above();

        AABB box = new AABB(
                Math.min(this.worldPosition.getX(), end.getX()) + 0.2,
                Math.min(this.worldPosition.getY(), end.getY()) + 0.2,
                Math.min(this.worldPosition.getZ(), end.getZ()) + 0.2,
                Math.max(this.worldPosition.getX(), end.getX()) + 0.8,
                Math.max(this.worldPosition.getY(), end.getY()) + 0.8,
                Math.max(this.worldPosition.getZ(), end.getZ()) + 0.8);

        for(LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, box)) {
            switch(this.mode) {
                case VISIBLE -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60 * 60 * 20, 0));
                    entity.igniteForSeconds(10F);
                }
                case IR, UV -> entity.igniteForSeconds(10F);
                case GAMMA -> ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 25);
                case DRX -> ContaminationUtil.applyDigammaData(entity, 0.1F);
                default -> { }
            }
        }
    }

    /**
     * Der Strahl selbst. Er faengt drei Bloecke vor der Maschine an -- davor steht sie selbst --
     * und geht Block fuer Block weiter, bis ihn etwas aufhaelt.
     */
    private void fireBeam(Direction dir) {

        boolean silexSpacing = false;
        this.missingValidSilex = true;

        for(int i = 3; i < RANGE; i++) {

            BlockPos pos = this.worldPosition.relative(dir, i).above();
            BlockState state = this.level.getBlockState(pos);
            Block block = state.getBlock();

            if(!state.isSolidRender(this.level, pos) && block != Blocks.TNT) {
                this.distance = RANGE;
                silexSpacing = false;
                continue;
            }

            if(block == NtmBlocks.MACHINE_SILEX.get()) {

                BlockPos corePos = new BlockPos(pos.getX() + dir.getStepX(), this.worldPosition.getY(), pos.getZ() + dir.getStepZ());

                if(this.level.getBlockEntity(corePos) instanceof MachineSILEXBlockEntity silex) {

                    if(i >= 5 && !silexSpacing && this.rotationIsValid(silex, dir)) {

                        if(silex.mode != this.mode) {
                            silex.mode = this.mode;
                            this.missingValidSilex = false;
                            silexSpacing = true;
                        }

                    } else {
                        /* Falsch herum oder zu dicht: die Maschine fliegt auseinander. */
                        this.level.destroyBlock(corePos, false);
                        this.level.addFreshEntity(new ItemEntity(this.level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                new ItemStack(NtmBlocks.MACHINE_SILEX.asItem())));
                    }
                }

                continue;
            }

            this.distance = i;

            /* NICHT UEBERNOMMEN: das Original hat hier einen Zweig, der Fluessigkeiten
             * verdampfen laesst. Er ist nicht erreichbar -- eine Fluessigkeit ist nicht
             * undurchsichtig, der Strahl ist im Zweig darueber schon durch sie hindurch. */
            if(state.getBlock().getExplosionResistance() < 75F && this.level.random.nextInt(5) == 0) {
                this.level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.level.setBlockAndUpdate(pos, this.beamFire().defaultBlockState());

                if(this.mode == Wellenlaenge.DRX) {
                    this.level.setBlockAndUpdate(pos.below(), NtmBlocks.ASH_DIGAMMA.get().defaultBlockState());
                }
            }

            break;
        }
    }

    /**
     * Was der Strahl entzuendet. Digamma brennt mit eigenem Feuer; das Original setzt bei
     * Polaroid 11 stattdessen Digammamaterie, und die gibt es im Port nicht.
     */
    private Block beamFire() {
        return this.mode == Wellenlaenge.DRX ? NtmBlocks.FIRE_DIGAMMA.get() : Blocks.FIRE;
    }

    /** Die SILEX muss laengs im Strahl stehen, nicht quer. */
    private boolean rotationIsValid(MachineSILEXBlockEntity silex, Direction felDir) {
        BlockState state = silex.getBlockState();
        if(!state.hasProperty(DummyableBlock.FACING)) return false;
        Direction silexDir = state.getValue(DummyableBlock.FACING);
        return silexDir == felDir || silexDir == felDir.getOpposite();
    }

    private Direction getFacing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    /** Ob der Strahl gerade wirklich steht -- Fenster, Darsteller und Klang fragen dasselbe. */
    public boolean isBeamActive() {
        return this.isOn && this.mode != Wellenlaenge.NULL
                && this.power > POWER_REQ * Math.pow(2, this.mode.ordinal())
                && this.distance > 0;
    }

    /**
     * Der Klang laeuft an und ab, statt hart ein- und auszuschalten: zwei Schritte hinauf je
     * Tick mit Strahl, drei hinunter ohne. Ab zehn hoert man ihn, und seine Tonhoehe steigt
     * mit dem Zaehler.
     */
    private void updateAudio() {

        this.audioDuration += this.isBeamActive() && this.distance - 3 > 0 ? 2 : -3;
        this.audioDuration = Math.max(0, Math.min(60, this.audioDuration));

        if(this.audioDuration > 10) {

            if(this.audio == null) {
                this.audio = this.createAudioLoop();
                this.audio.startSound();
            } else if(!this.audio.isPlaying()) {
                this.audio = this.rebootAudio(this.audio);
            }

            this.audio.updateVolume(this.getVolume(2F));
            this.audio.updatePitch((this.audioDuration - 10) / 100F + 0.5F);

        } else if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.FEL_LOOP.get(), SoundSource.BLOCKS, this, 2.0F, 10F, 2.0F, 20);
    }

    /** Der Schalter im Fenster. */
    public void toggle() {
        this.isOn = !this.isOn;
        this.setChanged();
    }

    public long getPowerScaled(long i) {
        return this.power * i / MAX_POWER;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_CRYSTAL ? stack.getItem() instanceof FelCrystalItem : slot == SLOT_BATTERY;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[0];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.mode = readMode(tag.getString("mode"));
        this.isOn = tag.getBoolean("isOn");
        this.missingValidSilex = tag.getBoolean("valid");
        this.distance = tag.getInt("distance");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putString("mode", this.mode.name());
        tag.putBoolean("isOn", this.isOn);
        tag.putBoolean("valid", this.missingValidSilex);
        tag.putInt("distance", this.distance);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeUtf(this.mode.name());
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.missingValidSilex);
        buf.writeInt(this.distance);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.mode = readMode(buf.readUtf());
        this.isOn = buf.readBoolean();
        this.missingValidSilex = buf.readBoolean();
        this.distance = buf.readInt();
    }

    private static Wellenlaenge readMode(String name) {
        for(Wellenlaenge wellenlaenge : Wellenlaenge.values()) {
            if(wellenlaenge.name().equals(name)) return wellenlaenge;
        }
        return Wellenlaenge.NULL;
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    /**
     * Der Strahl reicht 24 Bloecke weit; ohne eigenen Sichtkasten verschwaende er, sobald der
     * Kernblock aus dem Bild faellt.
     */
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(RANGE);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineFELMenu(id, inventory, this);
    }
}
