package com.hbm.blockentity.machine.albion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityPABeamline.
 *
 * Das Rohr dazwischen. Es tut nichts mit dem Teilchen, ausser es durchzulassen -- und genau
 * darum geht es: der Beschleuniger ist ueberwiegend Strahlfuehrung, und was sie kostet,
 * entscheidet, wie gross ein Ring werden kann.
 *
 * DREI BLOECKE STRECKE JE STUECK. Das ist die Waehrung, in der die Dipole rechnen: sie verlangen
 * eine Mindestkantenlaenge, und die kommt aus diesen Dreien.
 *
 * EIN FENSTER LAESST SICH AUFSCHRAUBEN. Es aendert nichts an der Physik, nur am Modell -- dafuer
 * sieht man den Strahl. Der Schraubenzieher schaltet es um.
 *
 * DAS LEUCHTEN IST EINE EINBAHNSTRASSE: die Serverseite setzt beim Durchflug eine Fahne, schickt
 * sie einmal mit und loescht sie sofort wieder. Der Klient macht daraus ein Aufleuchten, das ueber
 * acht Ticks verglimmt. Ohne diesen Kniff muesste jeder Block jeden Tick senden.
 */
public class MachinePABeamlineBlockEntity extends LoadedBaseBlockEntity implements IParticleUser, ITickable {

    public boolean window = false;
    public boolean didPass = false;

    public float light;
    public float prevLight;

    public MachinePABeamlineBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PA_BEAMLINE.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            this.prevLight = this.light;
            if(this.light > 0) this.light -= 0.25F;

            if(this.light > this.prevLight) this.prevLight = this.light;

        } else {
            this.networkPackNT(150);
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.window);
        buf.writeBoolean(this.didPass);
        this.didPass = false;
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.window = buf.readBoolean();
        this.didPass = buf.readBoolean();
        if(this.didPass) this.light = 2F;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.window = tag.getBoolean("window");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("window", this.window);
    }

    /** Die Richtung, in die das Rohr zeigt -- quer zur Blickrichtung des gesetzten Blocks. */
    public Direction getBeamlineDir() {
        BlockState state = this.getBlockState();
        Direction dir = state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
        return dir.getCounterClockWise(Axis.Y);
    }

    @Override
    public boolean canParticleEnter(Particle particle, Direction dir, BlockPos pos) {
        Direction beamlineDir = this.getBeamlineDir();
        return this.worldPosition.relative(beamlineDir, -1).equals(pos) && beamlineDir == dir;
    }

    @Override
    public void onEnter(Particle particle, Direction dir) {
        particle.addDistance(3);
        this.didPass = true;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        return this.worldPosition.relative(this.getBeamlineDir(), 2);
    }

    private AABB renderBox;

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 1, p.getZ() + 2);
        }
        return this.renderBox;
    }
}
