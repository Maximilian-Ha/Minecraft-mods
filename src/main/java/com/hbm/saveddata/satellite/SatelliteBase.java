package com.hbm.saveddata.satellite;

import api.hbm.redstoneoverradio.IRORInteractive;
import com.hbm.blockentity.network.RTTYSystem;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.machine.DriveItem.DriveType;
import com.hbm.util.EnumUtil;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.missile.SatellitePod;
import com.hbm.saveddata.SatelliteSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SatelliteBase {

    public static final String CHAN_SATLINK = "SAT_LINK";

    public static final String CMD_SETTARGET = "settarget";
    public static final String CMD_GETTARGET = "gettarget";
    public static final String CMD_GETTARGETX = "gettargetx";
    public static final String CMD_GETTARGETZ = "gettargetz";

    public int range = 1_000;
    public int targetX;
    public int targetZ;

    public String tx = "";

    /** Welchen leeren Datentraeger der Satellit beschreiben kann und was dabei herauskommt. */
    public DriveType driveInput = null;
    public DriveType driveOutput = null;

    /**
     * Was der Satellit zur Abholung bereitliegen hat, seit Runde 129. Die Bergbausatelliten
     * fuellen das; die Satellitenstation ruft es ab und schickt eine Kapsel.
     */
    public NonNullList<ItemStack> requestableSlots = NonNullList.create();

    public int getID() {
        return XSatelliteRegistry.satellites.indexOf(this.getClass());
    }

    public abstract String getType();

    /**
     * Was die Satellitenverbindung beim Hinsehen ueber diesen Satelliten anzeigt.
     *
     * ABWEICHUNG: im Original ist das abstrakt, und jeder der Satelliten schreibt dieselben zwei
     * Zeilen hin -- den Namen und, wo es einen Zustand gibt, "bereit" oder "laedt". Hier steht
     * der Namensteil EINMAL in der Wurzelklasse, und nur wer wirklich etwas zu melden hat,
     * ueberschreibt sie. Zehn gleichlautende Fassungen waeren zehn Gelegenheiten, eine davon zu
     * vergessen.
     */
    public List<Component> getInfo(Level level) {
        List<Component> info = new ArrayList<>();
        info.add(this.getDisplayName());
        return info;
    }

    /**
     * Der Anzeigename. Das Original nennt hier den Gegenstand, der den Satelliten hochgebracht
     * hat ("Death Ray Satellite"), nicht den rohen Typ ("ORBITAL_FUN_PLATFORM_:)"). Die Zuordnung
     * steht im Register -- nur andersherum, also wird sie hier einmal durchsucht. Das geschieht
     * bloss beim Hinsehen auf eine Bodenstation; elf Eintraege sind dafuer kein Aufwand.
     */
    public Component getDisplayName() {

        for(Map.Entry<ComparableStack, Class<? extends SatelliteBase>> entry : XSatelliteRegistry.itemToClass.entrySet()) {
            if(entry.getValue() == this.getClass()) return entry.getKey().toStack().getHoverName();
        }

        return Component.literal(this.getType());
    }

    /**
     * Ob gerade Daten zum Abholen bereitliegen. Ableitungen duerfen hier auch NEUE erzeugen --
     * der Wissenschaftssatellit tut genau das, wenn seine Wartezeit abgelaufen ist. Deshalb ist
     * das eine Frage mit Nebenwirkung und keine reine Abfrage; im Original ist es genauso.
     */
    public boolean hasData(Level level) {
        return this.driveInput != null && this.driveOutput != null;
    }

    /** Was aus dem eingelegten Datentraeger wird -- oder null, wenn der falsche eingelegt ist. */
    public DriveType getOutputData(DriveType input) {
        if(input == this.driveInput) return this.driveOutput;
        return null;
    }

    public void produceData(DriveType input, DriveType output) {
        this.driveInput = input;
        this.driveOutput = output;
    }

    public void consumeData() {
        this.driveInput = null;
        this.driveOutput = null;
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("targetX", targetX);
        tag.putInt("targetZ", targetZ);
        tag.putString("tx", tx);

        if(driveInput != null) tag.putInt("driveInput", driveInput.ordinal());
        if(driveOutput != null) tag.putInt("driveOutput", driveOutput.ordinal());

        tag.putInt("itemCount", this.requestableSlots.size());
        ListTag items = new ListTag();
        for(int i = 0; i < this.requestableSlots.size(); i++) {
            ItemStack stack = this.requestableSlots.get(i);
            if(stack.isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("slot", (byte) i);
            items.add(stack.save(registries, itemTag));
        }
        tag.put("requestableSlots", items);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.targetX = tag.getInt("targetX");
        this.targetZ = tag.getInt("targetZ");
        this.tx = tag.getString("tx");

        /* Fehlt der Schluessel, liegen keine Daten bereit -- deshalb ausdruecklich auf null
         * zuruecksetzen und nicht bloss ueberspringen. */
        this.driveInput = tag.contains("driveInput") ? EnumUtil.grabEnumSafely(DriveType.class, tag.getInt("driveInput")) : null;
        this.driveOutput = tag.contains("driveOutput") ? EnumUtil.grabEnumSafely(DriveType.class, tag.getInt("driveOutput")) : null;

        this.requestableSlots = NonNullList.withSize(tag.getInt("itemCount"), ItemStack.EMPTY);
        ListTag items = tag.getList("requestableSlots", Tag.TAG_COMPOUND);
        for(int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            int slot = itemTag.getByte("slot") & 255;
            if(slot < this.requestableSlots.size()) {
                this.requestableSlots.set(slot, ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
            }
        }
    }

    /** When a satellite is created, i.e. this frequency is occupied for the first time */
    public void onOrbit(Level level, double x, double y, double z) {
        this.setTarget((int) Math.floor(x), (int) Math.floor(z));

        RTTYSystem.broadcast(level, CHAN_SATLINK, "Established connection to " + getType() + " at " + targetX + " / " + targetZ);
    }

    /** For subsequent items sent under the same frequency as an existing satellite */
    public void onPartDelivered(Level level, ItemStack part) { }

    public void onCommand(Level level, String... cmd) {
        this.onCommandTarget(level, cmd);
        this.onCommandImpl(level, cmd);
    }

    public void onCommandTarget(Level level, String... cmd) {
        if(cmd.length <= 0) return;

        if(cmd[0].equals(CMD_SETTARGET)) {
            if(cmd.length == 3) {
                targetX = IRORInteractive.parseInt(cmd[1]);
                targetZ = IRORInteractive.parseInt(cmd[2]);
            }
            if(cmd.length == 4) {
                targetX = IRORInteractive.parseInt(cmd[1]);
                targetZ = IRORInteractive.parseInt(cmd[3]);
            }
            return;
        }

        if(cmd[0].equals(CMD_GETTARGET)) {
            this.tx = targetX + ";" + targetZ;
            return;
        }

        if(cmd[0].equals(CMD_GETTARGETX)) {
            this.tx = "" + targetX;
            return;
        }

        if(cmd[0].equals(CMD_GETTARGETZ)) {
            this.tx = "" + targetZ;
            return;
        }
    }

    public void setTarget(int x, int z) {
        this.targetX = x;
        this.targetZ = z;
    }

    public void onCommandImpl(Level level, String... cmd) { }

    /** Jeden Tick fuer jeden Satelliten in der Umlaufbahn -- siehe SatelliteSavedData.tickAll. */
    public void onUpdateTick(ServerLevel level) { }

    /**
     * Schickt die bereitliegende Ladung als Abwurfkapsel an die angegebene Stelle. Liegt nichts
     * bereit, geschieht nichts.
     *
     * DIE KAPSEL FAELLT AUS DREIHUNDERT BLOECKEN HOEHE, wie im Original -- sie braucht also
     * einen Moment, und wer darunter steht, sollte zur Seite gehen.
     */
    public boolean tryRequestItems(ServerLevel level, BlockPos pos) {

        if(this.requestableSlots.isEmpty()) return false;

        SatellitePod pod = new SatellitePod(NtmEntityTypes.SATELLITE_POD.get(), level);
        pod.setup(pos.getY(), this.requestableSlots);
        pod.setPos(pos.getX() + 0.5, 300, pos.getZ() + 0.5);

        if(!level.addFreshEntity(pod)) return false;

        this.requestableSlots = NonNullList.create();
        SatelliteSavedData.getData(level).setDirty();

        return true;
    }

    public void onCoordAction(Level level, Player player, BlockPos pos) { }
}
