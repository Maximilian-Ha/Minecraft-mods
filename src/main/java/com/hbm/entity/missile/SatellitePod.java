package com.hbm.entity.missile;

import com.hbm.blockentity.machine.MachineSatDockBlockEntity;
import com.hbm.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.missile.EntitySatellitePod.
 *
 * Die Abwurfkapsel. Ein Bergbausatellit hat etwas bereitliegen, die Satellitenstation ruft es ab
 * -- und dann faellt das hier aus dreihundert Bloecken Hoehe herunter, faehrt kurz vorher die
 * Beine aus, setzt auf und laedt nach fuenf Sekunden ab.
 *
 * SIE BREMST RECHTZEITIG: unterhalb von fuenfundzwanzig Bloecken ueber der rufenden Station
 * nimmt sie Fahrt heraus, bis auf ein Vierzigstel. Ohne das schluege sie mit voller Fallgeschwindigkeit auf.
 *
 * WAS NICHT IN DIE STATION PASST, FAELLT HERAUS. Die Kapsel haelt nichts zurueck.
 *
 * ABWEICHUNG: das Original leitet die Kapsel von seiner Wurfgeschoss-Basis ab, vor allem wegen
 * der Positionsglaettung auf der Client-Seite. Auf 1.21 kann das jede Entitaet von Haus aus, und
 * eine Kapsel, die weder trifft noch abprallt, braucht den Rest der Wurfgeschoss-Maschinerie
 * nicht. Sie ist deshalb eine gewoehnliche Entitaet.
 *
 * ABWEICHUNG: das Original zieht beim Aufsetzen eine Explosion und Gasflammen hinter sich her.
 * Die Kapsel liefert hier ab, ohne Feuerwerk.
 */
public class SatellitePod extends Entity {

    private static final EntityDataAccessor<Boolean> LEGS =
            SynchedEntityData.defineId(SatellitePod.class, EntityDataSerializers.BOOLEAN);

    public static final float LEG_SPEED = 1F / 20F;

    public NonNullList<ItemStack> slots = NonNullList.create();

    /** Wartezeit nach dem Aufsetzen, bis abgeladen wird. */
    public int timer = 0;

    /** Die Hoehe der Station, die gerufen hat -- danach richtet sich das Bremsen. */
    public int callerY;

    public double speed = 0.75D;

    public float legs = 0F;
    public float prevLegs = 0F;

    public SatellitePod(EntityType<? extends SatellitePod> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public void setup(int callerY, NonNullList<ItemStack> cargo) {
        this.callerY = callerY;
        this.slots = cargo;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LEGS, false);
    }

    public boolean doesDeployLegs() { return this.entityData.get(LEGS); }
    public void setDeployLegs(boolean deploy) { this.entityData.set(LEGS, deploy); }

    /** Solange etwas an Bord ist, sinkt sie; danach steigt sie wieder auf und verschwindet. */
    public boolean isLanding() { return !this.slots.isEmpty(); }

    @Override
    public void tick() {

        super.tick();

        if(!this.level.isClientSide) {

            if(this.isLanding()) {

                if(this.timer > 0) {
                    this.timer++;
                    /* Auf ganzer Hoehe stehen bleiben, damit sie nicht in den Boden sackt. */
                    this.setPos(this.getX(), Math.ceil(this.getY()), this.getZ());
                    if(this.timer >= 100) this.unloadItems();

                } else if(this.onGround()) {
                    this.speed = 0D;
                    this.timer = 1;
                    this.setDeployLegs(true);

                } else {
                    if(this.getY() < this.callerY + 17 && !this.doesDeployLegs()) this.setDeployLegs(true);
                    if(this.getY() < this.callerY + 25) this.speed -= 0.01;
                    this.speed = Mth.clamp(this.speed, 0.025D, 0.75D);
                }

                this.setDeltaMovement(0, -this.speed, 0);

            } else {

                this.speed += 0.01;
                if(this.speed >= 0.2) this.setDeployLegs(false);
                this.speed = Mth.clamp(this.speed, 0D, 2D);
                this.setDeltaMovement(0, this.speed, 0);

                if(this.getY() > 300) this.discard();
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

        } else {

            this.prevLegs = this.legs;
            this.legs = Mth.clamp(this.legs + (this.doesDeployLegs() ? LEG_SPEED : -LEG_SPEED), 0F, 1F);
        }
    }

    /**
     * Laedt in die Station ab, auf der die Kapsel steht. Was nicht hineinpasst, faellt daneben --
     * die Kapsel behaelt nichts.
     */
    public void unloadItems() {

        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.5, this.getZ());

        if(this.level.getBlockEntity(below) instanceof MachineSatDockBlockEntity dock) {
            for(int i = 0; i < this.slots.size(); i++) {
                ItemStack stack = this.slots.get(i);
                if(stack.isEmpty()) continue;
                this.slots.set(i, InventoryUtil.tryAddItemToInventory(dock.slots, 0, 14, stack));
            }
        }

        for(ItemStack stack : this.slots) {
            if(!stack.isEmpty()) this.spawnAtLocation(stack, 0.25F);
        }

        this.slots = NonNullList.create();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        this.timer = tag.getInt("timer");
        this.callerY = tag.getInt("callerY");
        this.speed = tag.getDouble("speed");

        this.slots = NonNullList.withSize(tag.getInt("itemCount"), ItemStack.EMPTY);
        ListTag items = tag.getList("items", Tag.TAG_COMPOUND);
        HolderLookup.Provider registries = this.registryAccess();

        for(int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            int slot = itemTag.getByte("slot") & 255;
            if(slot < this.slots.size()) this.slots.set(slot, ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

        tag.putInt("timer", this.timer);
        tag.putInt("callerY", this.callerY);
        tag.putDouble("speed", this.speed);

        tag.putInt("itemCount", this.slots.size());
        ListTag items = new ListTag();
        HolderLookup.Provider registries = this.registryAccess();

        for(int i = 0; i < this.slots.size(); i++) {
            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("slot", (byte) i);
            items.add(stack.save(registries, itemTag));
        }
        tag.put("items", items);
    }

    /** Sie faellt aus dreihundert Bloecken -- sie muss von weit her sichtbar sein. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 65536.0D;
    }

    @Override public boolean isPickable() { return true; }
    @Override public boolean fireImmune() { return true; }
}
