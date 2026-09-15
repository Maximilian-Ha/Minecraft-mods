package com.hbm.entity.item;

import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.item.EntityMovingPackage.
 *
 * Ein Paket faehrt wie ein einzelner Gegenstand ueber das Band, traegt aber bis zu einundzwanzig
 * Stapel auf einmal. Das ist sein ganzer Zweck: eine Strecke, die Stapel einzeln faehrt, braucht
 * fuer eine Kistenladung einundzwanzig Fahrten und ebenso viele Entitaeten; als Paket ist es eine.
 *
 * ABWEICHUNG: der Inhalt wird NICHT ueber das Netz geschickt. Im Original ebensowenig -- die
 * Entitaet hat dort ein leeres entityInit --, und es macht auch keinen Unterschied: der
 * Zuschauer sieht eine Kiste, nicht ihren Inhalt. Wer wissen will, was drin ist, muss sie
 * anfassen, und das entscheidet ohnehin der Server.
 */
public class MovingPackage extends MovingConveyorObject implements IConveyorPackage {

    protected ItemStack[] contents = new ItemStack[0];

    public MovingPackage(EntityType<? extends MovingPackage> entityType, Level level) {
        super(entityType, level);
    }

    public void setItemStacks(ItemStack[] stacks) {

        List<ItemStack> copy = new ArrayList<>();
        for(ItemStack stack : stacks) {
            if(stack != null && !stack.isEmpty()) copy.add(stack.copy());
        }

        this.contents = copy.toArray(new ItemStack[0]);
    }

    @Override
    public ItemStack[] getItemStacks() {
        return this.contents;
    }

    /** Rechtsklick packt das Paket aus: was in den Rucksack passt, kommt hinein, der Rest faellt. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {

        if(this.level.isClientSide || this.isRemoved()) return InteractionResult.PASS;

        for(ItemStack stack : this.contents) {
            if(!player.getInventory().add(stack.copy())) {
                this.level.addFreshEntity(new ItemEntity(this.level, this.getX(), this.getY() + 0.125, this.getZ(), stack.copy()));
            }
        }

        this.discard();
        return InteractionResult.SUCCESS;
    }

    /** Ein Schlag reisst es auf -- der ganze Inhalt faellt zu Boden. */
    @Override
    public boolean hurt(DamageSource source, float amount) {

        if(this.level.isClientSide || this.isRemoved()) return false;

        this.discard();

        for(ItemStack stack : this.contents) {
            this.level.addFreshEntity(new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), stack.copy()));
        }

        return true;
    }

    @Override
    public void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction dir) {

        if(this.isRemoved()) return;

        if(enterable.canPackageEnter(this.level, pos, dir, this)) {
            enterable.onPackageEnter(this.level, pos, dir, this);
            this.discard();
        }
    }

    @Override
    public boolean onLeaveConveyor() {

        if(this.isRemoved()) return true;

        this.discard();

        for(ItemStack stack : this.contents) {

            ItemEntity item = new ItemEntity(this.level,
                    this.getX() + this.getDeltaMovement().x * 2,
                    this.getY() + this.getDeltaMovement().y * 2,
                    this.getZ() + this.getDeltaMovement().z * 2,
                    stack.copy());

            item.setDeltaMovement(this.getDeltaMovement().x * 2, 0.1, this.getDeltaMovement().z * 2);
            item.hasImpulse = true;
            this.level.addFreshEntity(item);
        }

        return true;
    }

    @Override
    protected void defineSynchedData(Builder builder) { }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

        ListTag list = tag.getList("contents", Tag.TAG_COMPOUND);
        List<ItemStack> stacks = new ArrayList<>();

        for(int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(this.registryAccess(), list.getCompound(i));
            if(!stack.isEmpty()) stacks.add(stack);
        }

        this.contents = stacks.toArray(new ItemStack[0]);

        /* Ein leeres Paket hat keinen Grund zu fahren. */
        if(this.contents.length == 0) this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

        ListTag list = new ListTag();

        for(ItemStack stack : this.contents) {
            if(!stack.isEmpty()) list.add(stack.save(this.registryAccess()));
        }

        tag.put("contents", list);
    }
}
