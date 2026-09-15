package com.hbm.blockentity.machine.pile;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.pile.PileBlock;
import com.hbm.blocks.states.PileBlockType;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.machine.PileRodItem;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.particle.helper.MarkerCreator;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.pile.TileEntityPileCore.
 *
 * Der Chicago Pile MK2 -- ein Quader aus Graphitziegeln, in den man mit der Handbohrmaschine
 * Kanaele treibt. Welche Art Kanal dabei herauskommt, entscheidet allein die Richtung:
 *
 * - senkrecht          -> Steuerstabkanal
 * - laengs zur Blickrichtung des Kerns -> Brennstoffkanal
 * - quer dazu          -> Lueftungskanal
 *
 * Die Blickrichtung ist die Seite, von der aus zusammengebaut wurde. Damit steht sie fest,
 * sobald der Quader gebaut ist, und mit ihr, welche Bohrung was ergibt.
 *
 * WIE ER RECHNET
 *
 * Jeder Brennstoffkanal bekommt seinen einlaufenden Fluss auf seine Staebe verteilt, laesst
 * jeden Stab reagieren und zaehlt zusammen, was herauskommt. Dann wird verteilt, in zwei
 * Schritten:
 *
 * 1. Innerhalb einer senkrechten Scheibe geben alle Kanaele ihren Ausstoss an alle Kanaele
 *    derselben Scheibe weiter -- auch an sich selbst.
 * 2. Von Scheibe zu Scheibe, nach links und nach rechts, wobei jede durchquerte Scheibe den
 *    Fluss mit ihrem Faktor multipliziert. Eine Scheibe mit Steuerstaeben hat einen Faktor
 *    unter eins; je weiter die Staebe drin stecken, desto kleiner.
 *
 * So wirkt ein Steuerstab nicht nur dort, wo er steckt, sondern schirmt alles dahinter mit ab.
 * Das ist der ganze Trick dieser Anlage.
 *
 * Gekuehlt wird ueber Luft: ein Lueftungskanal auf gleicher Hoehe (plus/minus einen Block) zieht
 * bis zu fuenf Prozent der Waerme aus einem Brennstoffkanal. Ueber 800 Grad fliegt der Quader
 * auseinander.
 *
 * ABWEICHUNGEN:
 * - Die Rauchfahne ueber dem Luftauslass benutzt im Original die Partikelsorte "tower", die im
 *   Port noch fehlt -- dieselbe Stelle wie beim RBMK-Nachlader. Sie bleibt weg.
 * - Statt des Metadatenwerts traegt der Block eine Zustands-Eigenschaft.
 */
public class PileCoreBlockEntity extends PileBaseBlockEntity {

    /** Ab hier fliegt die Anlage auseinander. */
    public static final int MAX_HEAT = 800;
    /** Untergrenze, auf die ein Kanal von selbst zurueckfaellt. */
    private static final double AMBIENT_HEAT = 20D;

    /**
     * Gesetzt, solange die Kernschmelze laeuft. Die Bloecke der Anlage sehen daran, dass sie
     * nicht einzeln zu Graphit zurueckfallen sollen -- sie fliegen ohnehin gerade weg.
     */
    public static boolean meltingDown = false;

    /** Die Splitter, die bei der Kernschmelze hochgehen. */
    public static final BulletConfig PILE_DEBRIS = new BulletConfig()
            .setLife(200).setVel(1F).setGrav(0.1D)
            .setOnImpact((bullet, hit) -> {
                bullet.level().explode(bullet, bullet.getX(), bullet.getY(), bullet.getZ(), 5F, ExplosionInteraction.BLOCK);
                bullet.discard();
            });

    public PileOrientation orientation = PileOrientation.NEITHER;

    public int height;
    public int width;
    public int depth;

    public int left;
    public int right;
    public int up;

    public final List<PileChannel> fuelChannels = new ArrayList<>();
    public final List<PileChannel> ventilationChannels = new ArrayList<>();
    public final List<PileChannel> controlChannels = new ArrayList<>();

    /** Die senkrechten Scheiben, von links nach rechts. Sie machen die Rechnung erst handhabbar. */
    public PileSegment[] segments = new PileSegment[0];

    public double highestHeat;

    public PileCoreBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PILE_CORE.get(), pos, state);
    }

    public PileCoreBlockEntity setupSize(int up, int down, int left, int right, int depth) {
        this.height = up + 1 + down;
        this.width = left + 1 + right;
        this.depth = depth;
        this.left = left;
        this.right = right;
        this.up = up;
        this.segments = new PileSegment[this.width];
        return this;
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.runSimulation();
        this.handleVentilation();
        this.handleMeltdown();
    }

    @Override
    public void setRemoved() {
        for(PileChannel chan : this.fuelChannels) chan.ejectAll();
        super.setRemoved();
    }

    /* --- Kanaele bohren --- */

    /** Der Kanal, der an dieser Stelle anfaengt, oder null. */
    @Nullable
    public PileChannel getChannel(BlockPos pos, List<PileChannel> list) {
        for(PileChannel chan : list) if(chan.entryPos().equals(pos)) return chan;
        return null;
    }

    public List<PileChannel> getChannelList(PileChannelType type) {
        if(type == PileChannelType.FUEL) return this.fuelChannels;
        if(type == PileChannelType.VENTILATION) return this.ventilationChannels;
        return this.controlChannels;
    }

    /**
     * Bohrt einen Kanal, oder macht ihn wieder zu, wenn an dieser Stelle schon einer anfaengt.
     * Gibt zurueck, ob die Bohrmaschine etwas getan hat.
     */
    public boolean drillChannel(BlockPos pos, Direction dir, @Nullable Player player) {

        if(this.level == null) return false;

        PileBlockType startType = this.level.getBlockState(pos).getValue(PileBlock.TYPE);
        PileChannelType type = PileChannelType.getChannelType(dir, this.orientation);

        int size = switch(type) {
            case CONTROL -> this.height;
            case FUEL -> this.depth;
            case VENTILATION -> this.width;
        };

        List<PileChannel> list = this.getChannelList(type);

        /* Steht hier schon der Anfang eines Kanals, macht die Bohrmaschine ihn wieder zu. */
        if(startType == PileBlockType.FUEL_IN || startType == PileBlockType.AIR_IN || startType == PileBlockType.CONTROL) {
            for(int i = 0; i < list.size(); i++) {

                PileChannel chan = list.get(i);
                if(!chan.entryPos().equals(pos) || chan.entry.getDir() != dir) continue;

                if(chan.type == PileChannelType.FUEL) chan.ejectAll();
                list.remove(i);

                for(int j = 0; j < size; j++) this.setType(pos.relative(dir, j), PileBlockType.DUMMY);

                this.plink(pos, 0.75F);
                this.recalculateSegments();
                this.setChanged();
                return true;
            }
        }

        /* Sonst: erst pruefen, ob die ganze Strecke frei ist. */
        boolean error = false;

        for(int i = 0; i < size; i++) {

            BlockPos iPos = pos.relative(dir, i);
            BlockState state = this.level.getBlockState(iPos);

            if(!state.is(NtmBlocks.PILE_BLOCK.get())) {
                this.error(player, iPos, "Foreign block in reactor");
                error = true;
                continue;
            }

            PileBlockType at = state.getValue(PileBlock.TYPE);

            if(at == PileBlockType.EDGE) { this.error(player, iPos, "Cannot drill along edge"); error = true; }
            else if(at == PileBlockType.CORE) { this.error(player, iPos, "Cannot intersect core"); error = true; }
            else if(at == PileBlockType.CHANNEL) { this.error(player, iPos, "Cannot intersect channel"); error = true; }
            else if(at != PileBlockType.DUMMY) { this.error(player, iPos, "Cannot intersect channel IO"); error = true; }
        }

        if(error) return false;

        /* Dann bohren: Anfang, Mittelstueck, Ende. */
        for(int i = 0; i < size; i++) {

            BlockPos iPos = pos.relative(dir, i);

            if(i == 0) {
                this.setType(iPos, switch(type) {
                    case FUEL -> PileBlockType.FUEL_IN;
                    case VENTILATION -> PileBlockType.AIR_IN;
                    case CONTROL -> PileBlockType.CONTROL;
                });
            } else if(i == size - 1) {
                this.setType(iPos, switch(type) {
                    case FUEL -> PileBlockType.FUEL_OUT;
                    case VENTILATION -> PileBlockType.AIR_OUT;
                    case CONTROL -> PileBlockType.CONTROL;
                });
            } else {
                this.setType(iPos, PileBlockType.CHANNEL);
            }
        }

        list.add(new PileChannel(pos, dir, size, type));

        this.plink(pos, 1.25F);
        this.recalculateSegments();
        this.setChanged();

        return true;
    }

    private void setType(BlockPos pos, PileBlockType type) {
        BlockState state = this.level.getBlockState(pos);
        if(state.is(NtmBlocks.PILE_BLOCK.get())) this.level.setBlock(pos, state.setValue(PileBlock.TYPE, type), 3);
    }

    /**
     * Der Klick der Bohrmaschine. Das Original nimmt dafuer seinen eigenen Klang-Eintrag
     * VANILLA_PLINK; im Port steht dafuer der Erfahrungskugel-Klang, der derselbe ist.
     */
    private void plink(BlockPos pos, float pitch) {
        this.level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1F, pitch);
    }

    private void error(@Nullable Player player, BlockPos pos, String message) {
        if(player instanceof ServerPlayer serverPlayer) MarkerCreator.sendError(serverPlayer, pos, Component.literal(message));
    }

    /* --- Die Rechnung --- */

    protected void runSimulation() {

        /* Erst reagieren die Staebe in jedem Kanal fuer sich. */
        for(PileChannel chan : this.fuelChannels) {

            if(chan.length <= 0) continue;
            double producedNeutrons = 0D;

            for(int i = 0; i < chan.rods.size(); i++) {

                ItemStack stack = chan.rods.get(i);
                if(!(stack.getItem() instanceof PileRodItem)) continue;

                double neutrons = PileRodItem.getReactivity(stack, chan.incomingNeutrons / chan.length);
                producedNeutrons += neutrons;
                chan.heat += neutrons * PileRodItem.getHeatPerNeutron(stack);
                chan.rods.set(i, PileRodItem.react(stack, neutrons));
            }

            chan.outgoingNeutrons = producedNeutrons;
            chan.incomingNeutrons = 0D;
        }

        /* Dann innerhalb jeder Scheibe. */
        for(PileSegment seg : this.segments) {

            if(seg == null || seg.segType != PileChannelType.FUEL) continue;

            double outgoing = 0D;
            for(PileChannel chan : seg.channels) outgoing += chan.outgoingNeutrons;
            for(PileChannel chan : seg.channels) chan.incomingNeutrons += outgoing;
        }

        /*
         * Und zuletzt von Scheibe zu Scheibe. Die Grenzen lassen die aeusserste Scheibe je Seite
         * aus -- dort ist die Kante, und an der Kante kann kein Kanal liegen.
         */
        for(int i = 1; i < this.segments.length - 1; i++) {

            PileSegment seg = this.segments[i];
            if(seg == null || seg.segType != PileChannelType.FUEL) continue;

            double outgoing = 0D;
            for(PileChannel chan : seg.channels) outgoing += chan.outgoingNeutrons;

            this.spread(i, -1, outgoing);
            this.spread(i, 1, outgoing);
        }
    }

    /** Traegt den Ausstoss einer Scheibe in eine Richtung weiter, gedaempft von allem dazwischen. */
    private void spread(int from, int step, double outgoing) {

        double mult = 1D;

        for(int j = from + step; j >= 1 && j < this.segments.length - 1; j += step) {

            PileSegment neighbor = this.segments[j];
            if(neighbor == null) continue;

            mult *= neighbor.getNeutronMult(this);

            if(neighbor.segType == PileChannelType.FUEL) {
                for(PileChannel chan : neighbor.channels) chan.incomingNeutrons += outgoing * mult;
            }
        }
    }

    protected void handleVentilation() {

        for(PileChannel chan : this.ventilationChannels) {

            if(chan.air <= 0) continue;

            double airCap = (double) chan.air / (double) PileChannel.MAX_AIR;

            /* Ein voller Luftkanal nimmt fuenf Prozent der Waerme, und nur auf gleicher Hoehe. */
            for(PileChannel fuel : this.fuelChannels) {
                if(Math.abs(fuel.entry.getY() - chan.entry.getY()) <= 1) fuel.heat *= (1D - airCap * 0.05D);
            }

            chan.air -= (int) Math.ceil(airCap * 5D);
        }

        for(PileChannel chan : this.fuelChannels) {
            chan.heat *= 0.999D;
            if(chan.heat < AMBIENT_HEAT) chan.heat = AMBIENT_HEAT;
        }
    }

    protected void handleMeltdown() {

        this.highestHeat = 0D;
        for(PileChannel chan : this.fuelChannels) if(chan.heat > this.highestHeat) this.highestHeat = chan.heat;

        if(this.highestHeat <= MAX_HEAT) return;
        if(this.fuelChannels.isEmpty()) return;

        this.destroy();

        /* Es knallt in der Mitte aller Brennstoffkanaele, nicht am Kern. */
        double avgX = 0D;
        double avgZ = 0D;

        for(PileChannel chan : this.fuelChannels) {
            avgX += chan.entry.getX() + 0.5D + chan.entry.getDir().getStepX() * (chan.length - 1) / 2D;
            avgZ += chan.entry.getZ() + 0.5D + chan.entry.getDir().getStepZ() * (chan.length - 1) / 2D;
        }

        avgX /= this.fuelChannels.size();
        avgZ /= this.fuelChannels.size();

        double y = this.worldPosition.getY() + this.up;

        meltingDown = true;
        this.level.explode(null, avgX, y, avgZ, 15F, ExplosionInteraction.BLOCK);
        meltingDown = false;

        for(int i = 0; i < 15; i++) {
            double motionY = this.level.random.nextDouble() * 0.5D + 1D;
            BulletBaseMK4 fragment = new BulletBaseMK4(this.level, null, PILE_DEBRIS, 100F, 0.35F,
                    new Vec3(avgX, y + 1, avgZ), new Vec3(0, motionY, 0));
            this.level.addFreshEntity(fragment);
        }
    }

    /** Macht aus dem Kern wieder einen Graphitziegel -- der Rest der Anlage folgt von selbst. */
    public void destroy() {
        this.level.setBlock(this.worldPosition, NtmBlocks.PILE_BRICK.get().defaultBlockState(), 3);
    }

    /* --- Scheiben --- */

    protected void recalculateSegments() {

        this.segments = new PileSegment[this.width];

        this.sortIntoSegments(this.fuelChannels, PileChannelType.FUEL);
        this.sortIntoSegments(this.controlChannels, PileChannelType.CONTROL);
    }

    private void sortIntoSegments(List<PileChannel> channels, PileChannelType type) {

        for(PileChannel chan : channels) {

            int index = this.getChannelVerticalIndex(chan);
            if(index < 0 || index >= this.segments.length) continue;

            if(this.segments[index] == null) {
                this.segments[index] = new PileSegment(type).addChan(chan);
            } else if(this.segments[index].segType == type) {
                this.segments[index].addChan(chan);
            }
        }
    }

    /**
     * Zerlegt die Anlage in senkrechte Scheiben von links nach rechts und sagt, in welche ein
     * Kanal faellt. Weil das von der Vorderseite aus gedacht ist, gilt es nur fuer Brennstoff-
     * und Steuerkanaele.
     */
    protected int getChannelVerticalIndex(PileChannel chan) {

        DirPos pos = chan.entry;
        Direction right = pos.getDir().getClockWise();

        int deltaX = (pos.getX() - this.worldPosition.getX()) * right.getStepX();
        int deltaZ = (pos.getZ() - this.worldPosition.getZ()) * right.getStepZ();

        return (deltaX == 0 ? deltaZ : deltaX) + this.left;
    }

    /* --- Speichern --- */

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);

        this.height = tag.getInt("height");
        this.width = tag.getInt("width");
        this.depth = tag.getInt("depth");
        this.left = tag.getInt("left");
        this.right = tag.getInt("right");
        this.up = tag.getInt("up");

        this.orientation = EnumUtil.grabEnumSafely(PileOrientation.class, tag.getInt("orientation"));

        this.segments = new PileSegment[this.width];

        this.fuelChannels.clear();
        this.ventilationChannels.clear();
        this.controlChannels.clear();

        int fuelCount = tag.getByte("fc");
        int ventCount = tag.getByte("vc");
        int contCount = tag.getByte("cc");

        for(int i = 0; i < fuelCount; i++) this.fuelChannels.add(this.readChannel(tag, "f" + i, registries));
        for(int i = 0; i < ventCount; i++) this.ventilationChannels.add(this.readChannel(tag, "v" + i, registries));
        for(int i = 0; i < contCount; i++) this.controlChannels.add(this.readChannel(tag, "c" + i, registries));

        this.recalculateSegments();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("height", this.height);
        tag.putInt("width", this.width);
        tag.putInt("depth", this.depth);
        tag.putInt("left", this.left);
        tag.putInt("right", this.right);
        tag.putInt("up", this.up);

        tag.putInt("orientation", this.orientation.ordinal());

        tag.putByte("fc", (byte) this.fuelChannels.size());
        tag.putByte("vc", (byte) this.ventilationChannels.size());
        tag.putByte("cc", (byte) this.controlChannels.size());

        for(int i = 0; i < this.fuelChannels.size(); i++) this.fuelChannels.get(i).write(tag, "f" + i, registries);
        for(int i = 0; i < this.ventilationChannels.size(); i++) this.ventilationChannels.get(i).write(tag, "v" + i, registries);
        for(int i = 0; i < this.controlChannels.size(); i++) this.controlChannels.get(i).write(tag, "c" + i, registries);
    }

    public PileChannel readChannel(CompoundTag tag, String name, Provider registries) {

        BlockPos pos = new BlockPos(tag.getInt(name + "_x"), tag.getInt(name + "_y"), tag.getInt(name + "_z"));
        Direction dir = Direction.from3DDataValue(tag.getByte(name + "_d"));

        PileChannel chan = new PileChannel(pos, dir);

        if(chan.type == PileChannelType.FUEL) {

            ListTag list = tag.getList(name + "items", 10);

            for(int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                byte slot = entry.getByte("slot");
                if(slot >= 0 && slot < chan.rods.size()) {
                    ItemStack.parse(registries, entry.getCompound("stack")).ifPresent(stack -> chan.rods.set(slot, stack));
                }
            }

            chan.heat = tag.getDouble(name + "heat");
            chan.incomingNeutrons = tag.getDouble(name + "neutrons");
        }

        if(chan.type == PileChannelType.VENTILATION) chan.air = tag.getInt(name + "air");
        if(chan.type == PileChannelType.CONTROL) chan.control = tag.getDouble(name + "control");

        return chan;
    }

    /* --- Bestandteile --- */

    /**
     * Ein Kanal. Er traegt die Daten aller drei Arten -- das Original haelt es ebenso und
     * begruendet es damit, dass drei Unterklassen die Sache nur aufblaehen wuerden.
     */
    public class PileChannel {

        public static final int MAX_AIR = 1_000;

        /** Der erste Block des Kanals und die Richtung, in die er hineinfuehrt. */
        public final DirPos entry;
        /** Laenge des Kanals; sie entspricht immer der Ausdehnung der Anlage in dieser Achse. */
        public final int length;
        public final PileChannelType type;

        public final List<ItemStack> rods;

        public double heat = 0D;
        public double outgoingNeutrons = 0D;
        public double incomingNeutrons = 0D;
        public int air;
        /** Wie weit der Steuerstab draussen ist. Eins heisst ganz heraus. */
        public double control = 1D;

        public PileChannel(BlockPos pos, Direction dir) {
            this(pos, dir, 0, PileChannelType.getChannelType(dir, PileCoreBlockEntity.this.orientation));
        }

        public PileChannel(BlockPos pos, Direction dir, int length, PileChannelType type) {

            this.entry = new DirPos(pos, dir);
            this.type = type;

            /* Bei der Kurzform steht die Laenge nicht da -- sie folgt aus der Art des Kanals. */
            this.length = length > 0 ? length : switch(type) {
                case CONTROL -> PileCoreBlockEntity.this.height;
                case FUEL -> PileCoreBlockEntity.this.depth;
                case VENTILATION -> PileCoreBlockEntity.this.width;
            };

            this.rods = new ArrayList<>(this.length);
            for(int i = 0; i < this.length; i++) this.rods.add(ItemStack.EMPTY);
        }

        public BlockPos entryPos() {
            return new BlockPos(this.entry.getX(), this.entry.getY(), this.entry.getZ());
        }

        public void write(CompoundTag tag, String name, Provider registries) {

            tag.putInt(name + "_x", this.entry.getX());
            tag.putInt(name + "_y", this.entry.getY());
            tag.putInt(name + "_z", this.entry.getZ());
            tag.putByte(name + "_d", (byte) this.entry.getDir().get3DDataValue());

            if(this.type == PileChannelType.FUEL) {

                ListTag list = new ListTag();

                for(int i = 0; i < this.rods.size(); i++) {
                    ItemStack stack = this.rods.get(i);
                    if(stack.isEmpty()) continue;
                    CompoundTag entry = new CompoundTag();
                    entry.putByte("slot", (byte) i);
                    entry.put("stack", stack.save(registries));
                    list.add(entry);
                }

                tag.put(name + "items", list);
                tag.putDouble(name + "heat", this.heat);
                tag.putDouble(name + "neutrons", this.incomingNeutrons);
            }

            if(this.type == PileChannelType.VENTILATION) tag.putInt(name + "air", this.air);
            if(this.type == PileChannelType.CONTROL) tag.putDouble(name + "control", this.control);
        }

        /**
         * Schiebt einen Stab vorne hinein. Was schon drinliegt, rutscht eine Stelle weiter; was
         * hinten herausfaellt, faellt heraus.
         */
        public void loadItem(ItemStack stack) {

            if(stack.isEmpty()) return;
            if(this.rods.isEmpty()) { this.dropItem(stack, -1); return; }

            for(int i = 0; i < this.rods.size(); i++) {
                ItemStack prev = this.rods.get(i);
                this.rods.set(i, stack);
                stack = prev;
                if(stack.isEmpty()) return;
            }

            this.dropItem(stack, this.length);
        }

        public void ejectAll() {
            for(int i = 0; i < this.rods.size(); i++) {
                this.dropItem(this.rods.get(i), this.length);
                this.rods.set(i, ItemStack.EMPTY);
            }
        }

        /**
         * Wirft einen Stab am angegebenen Punkt des Kanals aus. Der Abbrand wird dabei
         * abgestreift, damit gleiche Staebe im Inventar wieder zusammengehen.
         */
        public void dropItem(ItemStack stack, int depth) {

            if(stack.isEmpty() || PileCoreBlockEntity.this.level == null) return;

            int x = this.entry.getX() + this.entry.getDir().getStepX() * depth;
            int y = this.entry.getY();
            int z = this.entry.getZ() + this.entry.getDir().getStepZ() * depth;

            CompoundTag data = com.hbm.util.TagsUtil.getCustomData(stack);
            if(data.contains(PileRodItem.KEY_NBT_DEPLETION)) {
                data.remove(PileRodItem.KEY_NBT_DEPLETION);
                com.hbm.util.TagsUtil.putCustomData(stack, data);
            }

            PileCoreBlockEntity.this.level.addFreshEntity(
                    new ItemEntity(PileCoreBlockEntity.this.level, x + 0.5D, y + 0.5D, z + 0.5D, stack));
        }
    }

    /**
     * Die Blickrichtung der Anlage, bestimmt durch die Seite, von der aus zusammengebaut wurde.
     * Kanaele laengs dazu sind Brennstoffkanaele, quer dazu Lueftungskanaele.
     */
    public enum PileOrientation {

        NORTH_SOUTH,
        EAST_WEST,
        NEITHER;

        public static PileOrientation getOrientation(Direction dir) {
            if(dir == Direction.NORTH || dir == Direction.SOUTH) return NORTH_SOUTH;
            if(dir == Direction.EAST || dir == Direction.WEST) return EAST_WEST;
            return NEITHER;
        }
    }

    public enum PileChannelType {

        FUEL, VENTILATION, CONTROL;

        public static PileChannelType getChannelType(Direction channelDir, PileOrientation orientation) {
            if(channelDir == Direction.UP || channelDir == Direction.DOWN) return CONTROL;
            if(PileOrientation.getOrientation(channelDir) == orientation) return FUEL;
            return VENTILATION;
        }
    }

    /**
     * Eine senkrechte Scheibe der Vorderseite. Sie kann mehrere Kanaele fassen; wegen der
     * Bauform sind die dann alle von derselben Art.
     */
    public static class PileSegment {

        public final List<PileChannel> channels = new ArrayList<>();
        public final PileChannelType segType;

        public PileSegment(PileChannelType segType) {
            this.segType = segType;
        }

        public PileSegment addChan(PileChannel chan) {
            this.channels.add(chan);
            return this;
        }

        /**
         * Womit diese Scheibe den durchlaufenden Fluss multipliziert. Nur Steuerscheiben daempfen;
         * hoechstens auf die Haelfte, und nur, wenn die Anlage ueberhaupt gross genug fuer
         * Steuerstaebe ist.
         */
        public double getNeutronMult(PileCoreBlockEntity core) {

            if(this.segType != PileChannelType.CONTROL) return 1D;

            /*
             * Minus eins, wie im Original: ein Muster aus abwechselnd Stab und Luecke deckt damit
             * rechnerisch die Haelfte ab.
             */
            int size = core.depth - 1;
            if(size < 3) return 0D;

            double total = 0D;
            for(PileChannel chan : this.channels) total += chan.control;

            return Mth.clamp(total / size, 0D, 0.5D);
        }
    }
}
