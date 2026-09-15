package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachinePumpBase.
 *
 * Die Wasserpumpe holt Grundwasser aus dem Boden. Sie ist NTMs Antwort auf die Frage, woher das
 * Kuehlwasser eines Reaktors kommen soll, ohne dass man einen Teich danebenbauen muss.
 *
 * SIE BRAUCHT ECHTEN BODEN, und das ist der ganze Witz. Unter ihr muessen in vier Lagen
 * ueberwiegend Erde, Gras, Sand oder Mycel liegen -- Stein zaehlt nicht, und die oberste Lage
 * muss vollstaendig fest sein. Wer sie auf eine Betonplattform stellt, bekommt kein Wasser.
 *
 * SIE BRAUCHT TIEFE. Ueber Hoehe siebzig laeuft sie nicht; das Grundwasser steht nun einmal
 * unten. Beides ist einstellbar, beides ist voreingestellt wie im Original.
 *
 * In der Oberwelt gilt das alles; in einer Welt ohne Himmel -- dem Nether etwa -- laeuft sie
 * grundsaetzlich nicht.
 *
 * ABWEICHUNG: der Port prueft die Bodenarten ueber Block-Tags statt ueber eine feste Liste. Das
 * Original zaehlt neun Bloecke namentlich auf; hier stehen die Vanilla-Tags fuer Erde und Sand,
 * und die verseuchten Boeden des Mods kommen namentlich dazu. Damit zaehlt auch der Boden
 * anderer Mods, was dem Sinn der Pruefung entspricht -- gefragt ist, ob da Erdreich liegt.
 *
 * Die beiden schmutzigen Saende des Originals fehlen dabei: sand_dirty und sand_dirty_red gibt
 * es im Port nicht. Sie kommen mit ihnen nach.
 *
 * NICHT UEBERNOMMEN: das sich drehende Pumpenrad, das Platschen und das Zischen. Der Port
 * zeichnet die Pumpe als Kasten.
 */
public abstract class MachinePumpBaseBlockEntity extends TickingBaseBlockEntity implements IFluidStandardSenderMK2, IFluidCopiable {

    /** Bloecke, die als Erdreich zaehlen und die das Vanilla-Tagwerk nicht kennt. */
    private static final Set<net.minecraft.world.level.block.Block> EXTRA_GROUND = new HashSet<>();

    /** Hoechste Hoehe, in der noch Grundwasser steht. */
    public static final int GROUND_HEIGHT = 70;

    /** Wie viele Lagen unter der Pumpe geprueft werden. */
    public static final int GROUND_DEPTH = 4;

    public static final int STEAM_SPEED = 1_000;
    public static final int ELECTRIC_SPEED = 10_000;

    public FluidTank water;

    public boolean isOn = false;
    public boolean onGround = false;

    public MachinePumpBaseBlockEntity(BlockEntityType<? extends MachinePumpBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        for(DirPos pos : this.getConPos()) {
            if(this.water.getFill() > 0) this.tryProvide(this.water, this.level, pos);
        }

        /* Der Boden wird nur alle zwei Sekunden nachgesehen -- er aendert sich selten, und die
         * Pruefung sieht sechsunddreissig Bloecke an. */
        if(this.level.getGameTime() % 40 == 0) this.onGround = this.checkGround();

        this.isOn = false;

        if(this.canOperate() && this.worldPosition.getY() <= GROUND_HEIGHT && this.onGround) {
            this.isOn = true;
            this.operate();
        }

        this.networkPackNT(150);
    }

    /**
     * Drei mal drei Bloecke, vier Lagen tief. Die oberste Lage muss ganz fest sein, und ueber
     * alle Lagen hinweg muss mindestens die Haelfte Erdreich sein.
     */
    protected boolean checkGround() {

        if(!this.level.dimensionType().hasSkyLight()) return false;

        int valid = 0;
        int invalid = 0;

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y >= -GROUND_DEPTH; y--) {
                for(int z = -1; z <= 1; z++) {

                    BlockPos pos = this.worldPosition.offset(x, y, z);
                    BlockState state = this.level.getBlockState(pos);

                    if(y == -1 && !state.isSolidRender(this.level, pos)) return false;

                    if(isGround(state)) valid++;
                    else invalid++;
                }
            }
        }

        return valid >= invalid;
    }

    private static boolean isGround(BlockState state) {

        if(state.is(net.minecraft.tags.BlockTags.DIRT)) return true;
        if(state.is(net.minecraft.tags.BlockTags.SAND)) return true;
        if(state.is(Blocks.MYCELIUM)) return true;

        if(EXTRA_GROUND.isEmpty()) {
            EXTRA_GROUND.add(NtmBlocks.WASTE_EARTH.get());
            EXTRA_GROUND.add(NtmBlocks.DIRT_DEAD.get());
            EXTRA_GROUND.add(NtmBlocks.DIRT_OILY.get());
        }

        return EXTRA_GROUND.contains(state.getBlock());
    }

    /** Vier Anschluesse, je zwei Bloecke vom Kern entfernt -- die Pumpe ist drei Bloecke breit. */
    protected DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(this.worldPosition.east(2), Direction.EAST),
                new DirPos(this.worldPosition.west(2), Direction.WEST),
                new DirPos(this.worldPosition.south(2), Direction.SOUTH),
                new DirPos(this.worldPosition.north(2), Direction.NORTH)
        };
    }

    protected abstract boolean canOperate();
    protected abstract void operate();

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.water }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.water }; }

    /* getTankToPaste bleibt bei der Vorgabe: der Wassertank ist fest auf Wasser, und das
     * Abschreibwerkzeug soll ihn nicht umstellen koennen. Bei der Dampfpumpe greift dadurch die
     * Vorgabe fuer Sende-Empfaenger und trifft den Dampftank -- genau richtig. */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.onGround);
        this.water.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.isOn = buf.readBoolean();
        this.onGround = buf.readBoolean();
        this.water.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.water.readFromNBT(tag, "water");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.water.writeToNBT(tag, "water");
    }
}
