package com.hbm.blockentity.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.FoundryOutletBlock;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.particle.helper.FoundryCreator;
import com.hbm.util.CrucibleUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryOutlet.
 *
 * Der Ausguss sitzt am Rand eines Giessereiblocks und laesst die Schmelze nach unten fallen,
 * statt sie weiterzureichen. Er lagert selbst nichts (Fassungsvermoegen null) und nimmt nur
 * von einer Seite an: von der, an der er haengt.
 *
 * Zwei Sperren sitzen davor. Ein Materialfilter -- mit einem Schrottstueck in der Hand gesetzt,
 * mit dem Schraubendreher geloescht, mit dem Handbohrer umgekehrt -- laesst entweder nur das
 * eine Material durch oder alles ausser diesem. Und ein Riegel, den ein Redstonesignal
 * schliesst; auch der laesst sich umkehren, dann ist der Ausguss standardmaessig zu und oeffnet
 * erst auf Signal.
 */
public class FoundryOutletBlockEntity extends FoundryBaseBlockEntity {

    /** Wie weit der Strahl hinunterreicht, in Bloecken. Wert aus dem Original. */
    private static final double REICHWEITE = 4D;

    public NTMMaterial filter = null;

    /** Kehrt den Filter um: dann geht alles durch AUSSER dem eingestellten Material. */
    public boolean invertFilter = false;

    /** Kehrt das Redstoneverhalten um: dann ist der Ausguss zu, solange kein Signal anliegt. */
    public boolean invertRedstone = false;

    public FoundryOutletBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FOUNDRY_OUTLET.get(), pos, state);
    }

    /** Solange TRUE, geht gar nichts hindurch, und der Riegel wird gezeichnet. */
    public boolean isClosed() {
        if(this.level == null) return false;
        return this.invertRedstone ^ this.level.hasNeighborSignal(this.worldPosition);
    }

    @Override public int getCapacity() { return 0; }

    /* Gegossen wird in den Ausguss nicht -- er nimmt nur seitlichen Zulauf. */
    @Override public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) { return stack; }

    /** Von wo der Strahl ausgeht und wie weit er sucht. */
    private Vec3 strahlAnfang(BlockPos pos) { return new Vec3(pos.getX() + 0.5, pos.getY() - 0.125, pos.getZ() + 0.5); }
    private Vec3 strahlEnde(BlockPos pos) { return new Vec3(pos.getX() + 0.5, pos.getY() + 0.125 - REICHWEITE, pos.getZ() + 0.5); }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {

        if(this.filter != null && (this.filter != stack.material ^ this.invertFilter)) return false;
        if(this.isClosed()) return false;
        if(side != this.getBlockState().getValue(FoundryOutletBlock.FACING).getOpposite()) return false;

        BlockHitResult[] treffer = new BlockHitResult[1];
        ICrucibleAcceptor ziel = CrucibleUtil.getPouringTarget(level, this.strahlAnfang(pos), this.strahlEnde(pos), treffer);

        if(ziel == null) return false;

        Vec3 ort = treffer[0].getLocation();
        return ziel.canAcceptPartialPour(level, treffer[0].getBlockPos(), ort.x, ort.y, ort.z, Direction.UP, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {

        BlockHitResult[] treffer = new BlockHitResult[1];
        ICrucibleAcceptor ziel = CrucibleUtil.getPouringTarget(level, this.strahlAnfang(pos), this.strahlEnde(pos), treffer);

        if(ziel == null) return stack;

        Vec3 ort = treffer[0].getLocation();
        MaterialStack rest = ziel.pour(level, treffer[0].getBlockPos(), ort.x, ort.y, ort.z, Direction.UP, stack);

        if(stack != null) {

            Direction dir = side.getOpposite();
            double trefferY = treffer[0].getBlockPos().getY() + 1;

            FoundryCreator.composeEffect(level,
                    pos.getX() + 0.5D - dir.getStepX() * 0.125, pos.getY() + 0.125, pos.getZ() + 0.5D - dir.getStepZ() * 0.125,
                    stack.material.moltenColor, dir,
                    (float) Math.max(1F, pos.getY() - (Math.ceil(trefferY) - 0.875)), 0F, 0.375F);
        }

        return rest;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.invertRedstone = tag.getBoolean("invert");
        this.invertFilter = tag.getBoolean("invertFilter");
        this.filter = Mats.matById.get((int) tag.getShort("filter"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("invert", this.invertRedstone);
        tag.putBoolean("invertFilter", this.invertFilter);
        tag.putShort("filter", this.filter == null ? -1 : (short) this.filter.id);
    }

    /*
     * ABWEICHUNG: das Original haelt Filter und Riegel nur in der Blockentitaet und liest sie
     * beim Zeichnen jedes Bild neu. Im Port stehen beide im Blockzustand (siehe
     * FoundryOutletBlock), das Modell kennt sie also ohne Netzverkehr. Ueber die Leitung gehen
     * sie trotzdem, weil der Filter auch im Aufsatzhinweis steht.
     */
    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.filter == null ? -1 : this.filter.id);
        buf.writeBoolean(this.invertFilter);
        buf.writeBoolean(this.invertRedstone);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.filter = Mats.matById.get(buf.readInt());
        this.invertFilter = buf.readBoolean();
        this.invertRedstone = buf.readBoolean();
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("invert", this.invertRedstone);
        tag.putBoolean("invertFilter", this.invertFilter);
        if(this.filter != null) tag.putIntArray("matFilter", new int[] { this.filter.id });
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {

        if(tag.contains("invert")) this.invertRedstone = tag.getBoolean("invert");
        if(tag.contains("invertFilter")) this.invertFilter = tag.getBoolean("invertFilter");
        if(tag.contains("matFilter")) {
            int[] ids = tag.getIntArray("matFilter");
            if(index < ids.length) this.filter = Mats.matById.get(ids[index]);
        }

        this.setChanged();
        if(level != null) FoundryOutletBlock.uebernimmZustand(level, pos, this);
    }

    @Override
    public String[] infoForDisplay(Level level, BlockPos pos) {
        List<String> info = new ArrayList<>();
        info.add("copytool.invertRedstone");
        info.add("copytool.invertFilter");
        if(this.filter != null) info.add(this.filter.getDescriptionId());
        return info.toArray(new String[0]);
    }
}
