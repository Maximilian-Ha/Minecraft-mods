package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.blocks.machine.rbmk.RBMKRodBlock;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.NeutronNodeWorld.StreamWorld;
import com.hbm.handler.neutron.NeutronStream;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.menus.RBMKRodMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.hbm.blocks.NtmBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod.
 *
 * Die Brennstabsaeule. Nimmt Fluss aus dem Nodespace auf, laesst den Stab abbrennen und schickt
 * den erzeugten Fluss in die vier Himmelsrichtungen zurueck.
 */
public class RBMKRodBlockEntity extends RBMKSlottedBaseBlockEntity implements IRBMKFluxReceiver, IRBMKLoadable {

    /** Anteil des schnellen Flusses am eingehenden Fluss. */
    public double fluxFastRatio;
    public double fluxQuantity;
    public double lastFluxQuantity;
    public double lastFluxRatio;

    public boolean hasRod;
    public int rodColor = 0;

    /* Nur zur Anzeige, kommt fertig formatiert vom Server. */
    public String fuelYield;
    public String fuelXenon;
    public String fuelHeat;

    public RBMKRodBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.RBMK_ROD.get(), pos, state);
    }

    /** Fuer Ableitungen wie den ReaSim-Brennkanal, die einen eigenen Typ mitbringen. */
    protected RBMKRodBlockEntity(BlockEntityType<? extends LoadedBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkRod");
    }

    @Override
    public boolean isModerated() {
        return this.getBlockState().getBlock() instanceof RBMKRodBlock rod && rod.moderated;
    }

    @Override
    public int trackingRange() {
        return 25;
    }

    @Override
    public void receiveFlux(NeutronStream stream) {
        double fastFlux = this.fluxQuantity * this.fluxFastRatio;
        double fastFluxIn = stream.fluxQuantity * stream.fluxRatio;

        this.fluxQuantity += stream.fluxQuantity;
        this.fluxFastRatio = this.fluxQuantity > 0 ? (fastFlux + fastFluxIn) / this.fluxQuantity : 0;
    }

    /** Der Ladekran darf erst ab dieser Temperatur ran. */
    public boolean coldEnoughForAutoloader() {
        ItemStack stack = this.slots.get(0);
        if(stack.getItem() instanceof RBMKRodItem) return RBMKRodItem.getHullHeat(stack) <= 1_000;
        return true;
    }

    /** Von Hand darf man den Stab erst ab dieser Temperatur ziehen. */
    public boolean coldEnoughForManual() {
        ItemStack stack = this.slots.get(0);
        if(stack.getItem() instanceof RBMKRodItem) return RBMKRodItem.getHullHeat(stack) <= 200;
        return true;
    }

    @Override
    public void setRemoved() {

        // Wird der Chunk nur entladen, ist isLoaded bereits false -- dann ist das kein Abbau
        // und der Reaktor darf nicht hochgehen.
        if(this.isLoaded && this.level != null && !this.level.isClientSide) {
            ItemStack stack = this.slots.get(0);
            if(stack.getItem() instanceof RBMKRodItem && RBMKRodItem.getHullHeat(stack) >= 1500 && !RBMKDials.getMeltdownsDisabled(this.level)) {
                this.meltdown();
            }
        }

        super.setRemoved();
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        ItemStack stack = this.slots.get(0);

        if(!(stack.getItem() instanceof RBMKRodItem rod)) {

            this.lastFluxRatio = 0;
            this.lastFluxQuantity = 0;
            this.fluxQuantity = 0;
            this.fluxFastRatio = 0;
            this.hasRod = false;

            super.updateEntity();
            return;
        }

        this.rodColor = rod.colorTint;

        double fluxRatioOut = rod.rType == NType.SLOW ? 0 : 1;
        double fluxIn = fluxFromType(rod.nType);
        double fluxQuantityOut = rod.burn(this.level, stack, fluxIn);

        rod.updateHeat(this.level, stack, 1.0D);
        this.heat += rod.provideHeat(this.level, stack, this.heat, 1.0D);

        if(!this.hasLid()) {
            ChunkRadiationManager.proxy.incrementRad(this.level, this.worldPosition, (float) (this.fluxQuantity * 0.05F));
        }

        super.updateEntity();

        if(this.heat > this.maxHeat()) {

            if(!RBMKDials.getMeltdownsDisabled(this.level)) this.meltdown();

            this.lastFluxRatio = 0;
            this.lastFluxQuantity = 0;
            this.fluxQuantity = 0;
            return;
        }

        if(this.heat > 10_000) this.heat = 10_000;

        // Fuer die Ausbreitung muss der gepufferte Fluss auf null, damit sichtbar wird, wie viel
        // tatsaechlich zurueckgeworfen wird.
        this.lastFluxQuantity = this.fluxQuantity;
        this.lastFluxRatio = this.fluxFastRatio;

        this.fluxQuantity = 0;
        this.fluxFastRatio = 0;

        spreadFlux(fluxQuantityOut, fluxRatioOut);

        this.hasRod = true;
        // Der Brennstab fuehrt seinen Zustand in der Datenkomponente mit, das Fach muss also
        // jeden Tick als geaendert gelten, damit der Abbrand auch gespeichert wird.
        this.setChanged();
    }

    private double fluxFromType(NType type) {

        double fastFlux = this.fluxQuantity * this.fluxFastRatio;
        double slowFlux = this.fluxQuantity * (1 - this.fluxFastRatio);

        return switch(type) {
            case SLOW -> slowFlux + fastFlux * 0.5;
            case FAST -> fastFlux + slowFlux * 0.3;
            case ANY -> this.fluxQuantity;
        };
    }

    public static final Direction[] FLUX_DIRS = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public void spreadFlux(double flux, double ratio) {

        if(flux == 0) {
            // So faellt der Knoten aus dem Cache, sobald kein Fluss mehr hineingeht.
            NeutronNodeWorld.removeNode(this.level, this.worldPosition);
            return;
        }

        StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(this.level);
        RBMKNeutronNode node = (RBMKNeutronNode) streamWorld.getNode(this.worldPosition);

        if(node == null) {
            node = RBMKNeutronHandler.makeNode(streamWorld, this);
            streamWorld.addNode(node);
        }

        for(Direction dir : FLUX_DIRS) {
            new RBMKNeutronHandler.RBMKNeutronStream(node, new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ()), flux, ratio);
        }
    }

    @Override
    public void getLookInfo(List<Component> text) {

        if(!this.hasRod) {
            text.add(Component.translatable("trait.rbmk.look.empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        text.add(Component.translatable("trait.rbmk.look.flux",
                String.format(Locale.US, "%.1f", this.lastFluxQuantity),
                (int) (this.lastFluxRatio * 100)).withStyle(ChatFormatting.GREEN));

        if(this.fuelYield != null) text.add(Component.translatable("trait.rbmk.look.yield", this.fuelYield).withStyle(ChatFormatting.YELLOW));
        if(this.fuelXenon != null) text.add(Component.translatable("trait.rbmk.look.xenon", this.fuelXenon).withStyle(ChatFormatting.DARK_PURPLE));
        if(this.fuelHeat != null) text.add(Component.translatable("trait.rbmk.look.rodheat", this.fuelHeat).withStyle(ChatFormatting.RED));
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.FUEL;
    }

    @Override
    public CompoundTag getNBTForConsole() {

        CompoundTag data = super.getNBTForConsole();
        ItemStack stack = this.slots.get(0);

        if(stack.getItem() instanceof RBMKRodItem rod) {
            data.putDouble("enrichment", RBMKRodItem.getEnrichment(stack));
            data.putDouble("xenon", RBMKRodItem.getPoison(stack));
            data.putDouble("c_heat", RBMKRodItem.getHullHeat(stack));
            data.putDouble("c_coreHeat", RBMKRodItem.getCoreHeat(stack));
            data.putDouble("c_maxHeat", rod.meltingPoint);
        }

        return data;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.ROD;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof RBMKRodItem;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fluxQuantity = tag.getDouble("fluxQuantity");
        this.fluxFastRatio = tag.getDouble("fluxMod");
        this.hasRod = tag.getBoolean("hasRod");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("fluxQuantity", this.lastFluxQuantity);
        tag.putDouble("fluxMod", this.lastFluxRatio);
        tag.putBoolean("hasRod", this.hasRod);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.lastFluxQuantity);
        buf.writeDouble(this.lastFluxRatio);
        buf.writeBoolean(this.hasRod);
        buf.writeInt(this.rodColor);

        if(this.hasRod) {
            ItemStack stack = this.slots.get(0);
            RBMKRodItem rod = (RBMKRodItem) stack.getItem();
            buf.writeUtf(RBMKRodItem.getYield(stack) + " / " + rod.yield + " (" + (RBMKRodItem.getEnrichment(stack) * 100) + "%)");
            buf.writeUtf(RBMKRodItem.getPoison(stack) + "%");
            buf.writeUtf(String.format(java.util.Locale.US, "%.6f", RBMKRodItem.getCoreHeat(stack))
                    + " / " + String.format(java.util.Locale.US, "%.6f", RBMKRodItem.getHullHeat(stack))
                    + " / " + String.format(java.util.Locale.US, "%.2f", rod.meltingPoint));
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.fluxQuantity = buf.readDouble();
        this.fluxFastRatio = buf.readDouble();
        this.hasRod = buf.readBoolean();
        this.rodColor = buf.readInt();

        if(this.hasRod) {
            this.fuelYield = buf.readUtf();
            this.fuelXenon = buf.readUtf();
            this.fuelHeat = buf.readUtf();
        } else {
            this.fuelYield = this.fuelXenon = this.fuelHeat = null;
        }
    }

    /**
     * Ein geladener Brennkanal stuerzt bei der Kernschmelze nicht ein -- er laeuft von oben bis
     * unten mit Corium voll. Ein leerer Kanal stuerzt ein wie jede andere Saeule.
     *
     * Lag ein DRX-Stab im Kanal, setzt er dabei das Digamma-Kennzeichen der Anlage -- dann wird
     * aus dem Schutt ringsum kein strahlender, sondern Digamma-Schutt.
     */
    @Override
    public void onMelt(int reduce) {

        boolean loaded = this.slots.get(0).getItem() instanceof RBMKRodItem;
        boolean moderated = this.isModerated();

        if(this.slots.get(0).getItem() == NtmItems.RBMK_FUEL_DRX.get()) digamma = true;

        this.slots.set(0, ItemStack.EMPTY);

        if(this.level == null || this.level.isClientSide) {
            super.onMelt(reduce);
            return;
        }

        if(loaded) {

            this.pourCorium();

            int fuel = 1 + this.level.random.nextInt(RBMKDials.getColumnHeight(this.level));
            for(int i = 0; i < fuel; i++) this.spawnDebris(DebrisType.FUEL);

        } else {
            this.standardMelt(reduce);
        }

        if(moderated) {
            int graphite = 2 + this.level.random.nextInt(2);
            for(int i = 0; i < graphite; i++) this.spawnDebris(DebrisType.GRAPHITE);
        }

        this.spawnDebris(DebrisType.ELEMENT);

        if(this.hasLid()) this.spawnDebris(DebrisType.LID);
    }

    /** Giesst die ganze Saeule von unten bis oben mit Corium aus. */
    private void pourCorium() {

        BlockState corium = NtmBlocks.CORIUM.get().defaultBlockState();
        int height = RBMKDials.getColumnHeight(this.level);

        for(int i = height; i >= 0; i--) {
            this.level.setBlock(this.worldPosition.above(i), corium, 3);
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKRodMenu(id, inventory, this);
    }

    /* Der Ladekran: der Brennkanal fasst genau einen Stab. */

    @Override
    public boolean canLoad(ItemStack toLoad) {
        return !toLoad.isEmpty() && this.slots.get(0).isEmpty();
    }

    @Override
    public void load(ItemStack toLoad) {
        this.slots.set(0, toLoad.copy());
        this.setChanged();
    }

    @Override
    public boolean canUnload() {
        return !this.slots.get(0).isEmpty();
    }

    @Override
    public ItemStack provideNext() {
        return this.slots.get(0);
    }

    @Override
    public void unload() {
        this.slots.set(0, ItemStack.EMPTY);
        this.setChanged();
    }
}
