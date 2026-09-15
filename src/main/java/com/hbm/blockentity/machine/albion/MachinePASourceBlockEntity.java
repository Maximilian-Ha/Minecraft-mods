package com.hbm.blockentity.machine.albion;

import com.hbm.blockentity.CooledBaseBlockEntity;
import com.hbm.blockentity.IConditionalInvAccess;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.MachinePASourceMenu;
import com.hbm.lib.Library;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityPASource.
 *
 * Die Quelle des Beschleunigers -- und zugleich sein Rechenwerk. Sie setzt aus zwei Gegenstaenden
 * ein Teilchen zusammen, schickt es auf die Reise und fuehrt dabei Buch ueber alles, was ihm
 * unterwegs zustoesst.
 *
 * DER STRAHL IST KEINE ENTITAET. Es gibt nichts, das durch die Welt fliegt: die Quelle fragt
 * jeden Tick das Bauteil ab, auf dem das Teilchen gerade steht, laesst es dort seine Wirkung tun
 * und uebernimmt die Stelle, an der es wieder herauskommt. Der ganze Ring wird also von hier aus
 * abgeschritten, Block fuer Block.
 *
 * UND ZWAR SCHNELLER, JE SCHNELLER DAS TEILCHEN IST: ein Schritt je Tick am Anfang, bis zu zehn
 * bei hohem Impuls. Das ist kein Beiwerk -- ein Ring von hundert Bloecken Umfang braucht sonst
 * Minuten je Runde.
 *
 * WER NICHT MITSPIELT, BRINGT IHN ZUM ABSTURZ. Steht im Weg etwas, das keine Strahlfuehrung ist,
 * oder wird es aus der falschen Richtung getroffen, endet die Fahrt -- und die Anzeige sagt,
 * woran es lag. Dreizehn Zustaende, und zehn davon sind Fehler.
 *
 * SIE HAELT AN, WENN DER RING NICHT GELADEN IST, statt das Teilchen ins Leere laufen zu lassen.
 * Kommt der Abschnitt zurueck, laeuft die Fahrt weiter.
 *
 * NICHT UEBERNOMMEN: die Anbindung an OpenComputers und an Redstone-ueber-Funk. Beides gibt es
 * im Port nicht.
 */
public class MachinePASourceBlockEntity extends CooledBaseBlockEntity implements IControlReceiver, IConditionalInvAccess {

    public static final long usage = 100_000;

    /** Fach null laedt die Maschine, eins und zwei nehmen die Ausgangsstoffe, drei und vier geben die Behaelter zurueck. */
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_CONTAINER_1 = 3;
    public static final int SLOT_CONTAINER_2 = 4;

    public Particle particle;
    public PAState state = PAState.IDLE;

    /** Der zuletzt gemessene Impuls -- die Anzeige zeigt ihn auch nach dem Absturz noch. */
    public int lastSpeed;
    public int debugSpeed;

    /**
     * Was mit dem Strahl los ist. Die Farbe steht dabei, weil die Anzeige nichts weiter braucht
     * als sie: blau heisst leer, gelb faehrt, gruen hat getroffen, grau wartet, rot ist hin.
     */
    public enum PAState {
        IDLE(0x8080ff),                 // kein Teilchen unterwegs
        RUNNING(0xffff00),              // faehrt ohne Beanstandung
        SUCCESS(0x00ff00),              // Rezept getroffen
        PAUSE_UNLOADED(0x808080),       // angehalten, weil der Abschnitt nicht geladen ist
        CRASH_DEFOCUS(0xff0000),        // zu weit aufgefaechert
        CRASH_DERAIL(0xff0000),         // aus der Strahlfuehrung gelaufen
        CRASH_CANNOT_ENTER(0xff0000),   // Bauteil von der falschen Seite getroffen
        CRASH_NOCOOL(0xff0000),         // nicht kalt genug
        CRASH_NOPOWER(0xff0000),        // Strom aus
        CRASH_NOCOIL(0xff0000),         // keine Spule eingesetzt (Quadrupol, Dipol)
        CRASH_OVERSPEED(0xff0000),      // schneller, als die Spule traegt
        CRASH_UNDERSPEED(0xff0000),     // langsamer, als das Rezept verlangt
        CRASH_NORECIPE(0xff0000);       // kein Rezept zu den beiden Ausgangsstoffen

        public final int color;

        PAState(int color) {
            this.color = color;
        }
    }

    public void updateState(PAState state) { this.state = state; }

    public MachinePASourceBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PA_SOURCE.get(), pos, state, 5);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.paSource");
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.getMaxPower());

            /* Je schneller das Teilchen, desto mehr Bloecke legt es je Tick zurueck. Ohne das
             * braeuchte eine Runde durch einen grossen Ring mehrere Minuten. */
            int steps = 1;
            if(this.particle != null) steps = 1 + Mth.clamp(this.particle.momentum / 1_000, 0, 9);

            for(int i = 0; i < steps; i++) {
                if(this.particle != null) {
                    this.state = PAState.RUNNING;
                    this.steppy();
                    this.debugSpeed = this.particle.momentum;
                    if(this.particle.invalid) this.particle = null;
                } else if(this.power >= usage && !this.slots.get(SLOT_INPUT_1).isEmpty() && !this.slots.get(SLOT_INPUT_2).isEmpty()) {
                    this.tryRun();
                    break;
                }
            }
        }

        super.updateEntity();
    }

    /** Ein Schritt: das Bauteil unter dem Teilchen fragen, es wirken lassen, weiterruecken. */
    public void steppy() {

        /* Nicht ins Ungeladene laufen lassen -- dort steht kein Blockeintrag, den man fragen
         * koennte, und das Teilchen waere bei der Rueckkehr spurlos weg. */
        if(!this.level.isLoaded(this.particle.pos)) { this.state = PAState.PAUSE_UNLOADED; return; }

        BlockState state = this.level.getBlockState(this.particle.pos);
        Block block = state.getBlock();

        if(block instanceof DummyableBlock dummyable) {
            BlockPos core = dummyable.findCore(this.level, this.particle.pos);
            if(core == null) { this.particle.crash(PAState.CRASH_DERAIL); return; }

            if(!(this.level.getBlockEntity(core) instanceof IParticleUser user)) { this.particle.crash(PAState.CRASH_DERAIL); return; }

            if(user.canParticleEnter(this.particle, this.particle.dir, this.particle.pos)) {
                user.onEnter(this.particle, this.particle.dir);
                BlockPos exit = user.getExitPos(this.particle);
                if(exit != null) this.particle.move(exit);
            } else {
                this.particle.crash(PAState.CRASH_CANNOT_ENTER);
            }
        } else {
            this.particle.crash(PAState.CRASH_DERAIL);
        }
    }

    /**
     * Setzt ein neues Teilchen zusammen. Es startet fuenf Bloecke seitlich der Quelle, also am
     * ersten Stueck Strahlfuehrung.
     *
     * HAT EIN AUSGANGSSTOFF EINEN BEHAELTER -- eine leere Teilchenhuelle etwa --, wird der
     * sofort zurueckgelegt. Ist das Rueckgabefach schon belegt, faengt der Lauf gar nicht erst an.
     */
    public void tryRun() {

        if(!this.isCool()) return;

        ItemStack in1 = this.slots.get(SLOT_INPUT_1);
        ItemStack in2 = this.slots.get(SLOT_INPUT_2);

        if(in1.hasCraftingRemainingItem() && !this.slots.get(SLOT_CONTAINER_1).isEmpty()) return;
        if(in2.hasCraftingRemainingItem() && !this.slots.get(SLOT_CONTAINER_2).isEmpty()) return;

        if(in1.hasCraftingRemainingItem()) this.slots.set(SLOT_CONTAINER_1, in1.getCraftingRemainingItem().copy());
        if(in2.hasCraftingRemainingItem()) this.slots.set(SLOT_CONTAINER_2, in2.getCraftingRemainingItem().copy());

        this.power -= usage;

        Direction rot = this.getDir().getCounterClockWise(Axis.Y);
        this.particle = new Particle(this, this.worldPosition.relative(rot, 5), rot, in1.copy(), in2.copy());

        this.slots.set(SLOT_INPUT_1, ItemStack.EMPTY);
        this.slots.set(SLOT_INPUT_2, ItemStack.EMPTY);
        this.setChanged();
    }

    public Direction getDir() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.debugSpeed);
        buf.writeByte((byte) this.state.ordinal());
        buf.writeInt(this.lastSpeed);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.debugSpeed = buf.readInt();
        this.state = EnumUtil.grabEnumSafely(PAState.class, buf.readByte());
        this.lastSpeed = buf.readInt();
    }

    @Override
    public DirPos[] getConPos() {

        Direction dir = this.getDir();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.relative(dir, 2), dir),
                new DirPos(p.relative(dir, 2).relative(rot, 2), dir),
                new DirPos(p.relative(dir, 2).relative(rot, -2), dir),
                new DirPos(p.relative(dir, -2), dir.getOpposite()),
                new DirPos(p.relative(dir, -2).relative(rot, 2), dir.getOpposite()),
                new DirPos(p.relative(dir, -2).relative(rot, -2), dir.getOpposite()),
                new DirPos(p.relative(rot, 5), rot),
                new DirPos(p.below(2), dir),
                new DirPos(p.below(2).relative(rot, 2), dir),
                new DirPos(p.below(2).relative(rot, -2), dir)
        };
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return slot == SLOT_CONTAINER_1 || slot == SLOT_CONTAINER_2; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { SLOT_CONTAINER_1, SLOT_CONTAINER_2 }; }

    @Override public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) { return this.canPlaceItem(slot, stack); }
    @Override public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction direction) { return this.canTakeItemThroughFace(slot, stack, direction); }

    /**
     * Die beiden Ausgangsstoffe haben je eine eigene Ecke: an der einen nimmt die Maschine den
     * ersten an, an der anderen den zweiten. Sonst koennte ein Trichter nicht gezielt befuellen.
     */
    public static final int[] slotsRed = new int[] { SLOT_INPUT_1, SLOT_CONTAINER_1, SLOT_CONTAINER_2 };
    public static final int[] slotsYellow = new int[] { SLOT_INPUT_2, SLOT_CONTAINER_1, SLOT_CONTAINER_2 };

    @Override
    public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction direction) {

        Direction dir = this.getDir();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        if(pos.equals(p.relative(dir).relative(rot, -2)) || pos.equals(p.relative(dir, -1).relative(rot, 2))) return slotsYellow;
        if(pos.equals(p.relative(dir, -1).relative(rot, -2)) || pos.equals(p.relative(dir).relative(rot, 2))) return slotsRed;

        return this.getSlotsForFace(direction);
    }

    @Override
    public long getMaxPower() {
        return 10_000_000;
    }

    private AABB renderBox;

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 4, p.getY() - 1, p.getZ() - 4, p.getX() + 5, p.getY() + 2, p.getZ() + 6);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePASourceMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("cancel")) {
            this.particle = null;
            this.state = PAState.IDLE;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if(this.particle != null) {
            CompoundTag particleTag = new CompoundTag();
            particleTag.putInt("x", this.particle.pos.getX());
            particleTag.putInt("y", this.particle.pos.getY());
            particleTag.putInt("z", this.particle.pos.getZ());
            particleTag.putByte("dir", (byte) this.particle.dir.ordinal());
            particleTag.putInt("momentum", this.particle.momentum);
            particleTag.putInt("defocus", this.particle.defocus);
            particleTag.putInt("dist", this.particle.distanceTraveled);
            particleTag.put("input1", this.particle.input1.save(registries, new CompoundTag()));
            particleTag.put("input2", this.particle.input2.save(registries, new CompoundTag()));
            tag.put("particle", particleTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.particle = null;
        if(!tag.contains("particle")) return;

        CompoundTag particleTag = tag.getCompound("particle");
        BlockPos pos = new BlockPos(particleTag.getInt("x"), particleTag.getInt("y"), particleTag.getInt("z"));
        Direction dir = EnumUtil.grabEnumSafely(Direction.class, particleTag.getByte("dir"));
        ItemStack input1 = ItemStack.parse(registries, particleTag.getCompound("input1")).orElse(ItemStack.EMPTY);
        ItemStack input2 = ItemStack.parse(registries, particleTag.getCompound("input2")).orElse(ItemStack.EMPTY);

        this.particle = new Particle(this, pos, dir, input1, input2);
        this.particle.momentum = particleTag.getInt("momentum");
        this.particle.defocus = particleTag.getInt("defocus");
        this.particle.distanceTraveled = particleTag.getInt("dist");
    }
}
