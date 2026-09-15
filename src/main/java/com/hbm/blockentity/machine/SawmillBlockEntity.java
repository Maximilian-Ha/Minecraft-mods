package com.hbm.blockentity.machine;

import api.hbm.tile.IHeatSource;
import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.entity.projectile.Sawblade;
import com.hbm.items.NtmItems;
import com.hbm.network.toclient.ParticleBurst;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntitySawmill.
 *
 * Zieht Waerme aus der Quelle darunter und zersaegt damit Holz. Ab 300 TU/t laeuft
 * das Blatt zu schnell; nach 300 weiteren Ticks fliegt es als Sawblade davon und der
 * Kern explodiert. Alle Zahlenwerte sind unveraendert uebernommen.
 *
 * Die innere Klasse InventoryCraftingAuto des Originals wird nicht gebraucht: sie
 * diente nur dazu, dem CraftingManager ein 1x1-Gitter unterzuschieben. In 1.21
 * uebernimmt das CraftingInput.of(1, 1, ...).
 */
public class SawmillBlockEntity extends MachineBaseBlockEntity implements ITickable, IPersistentNBT {

    /** Gegenstueck zum Ore-Dictionary-Eintrag "stickWood". */
    private static final TagKey<Item> RODS_WOODEN = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden"));

    public int heat;
    public static final double diffusion = 0.1D;
    private int warnCooldown = 0;
    private int overspeed = 0;
    public boolean hasBlade = true;
    public int progress = 0;
    public static final int processingTime = 600;

    public float spin;
    public float lastSpin;

    private AABB renderBox = null;

    public SawmillBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SAWMILL.get(), pos, state, 3);
    }

    /** Das Original gibt hier einen leeren Namen zurueck -- das Saegewerk hat keine Oberflaeche. */
    @Override protected Component getDefaultName() { return Component.empty(); }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        BlockPos bp = this.getBlockPos();

        if(!this.level.isClientSide) {

            if(this.hasBlade) {
                this.tryPullHeat();

                if(this.warnCooldown > 0) this.warnCooldown--;

                if(this.heat >= 100) {

                    ItemStack result = this.getOutput(this.slots.get(0));

                    if(!result.isEmpty()) {
                        this.progress += this.heat / 10;

                        if(this.progress >= processingTime) {
                            this.progress = 0;
                            this.slots.set(0, ItemStack.EMPTY);
                            this.slots.set(1, result);

                            if(result.getItem() != NtmItems.POWDER_SAWDUST.get()) {
                                float chance = result.getItem() == Items.STICK ? 0.1F : 0.5F;
                                if(this.level.random.nextFloat() < chance) {
                                    this.slots.set(2, new ItemStack(NtmItems.POWDER_SAWDUST.get()));
                                }
                            }

                            this.setChanged();
                        }

                    } else {
                        this.progress = 0;
                    }

                    // Der Schlitz direkt vor dem Saegeblatt
                    AABB aabb = new AABB(-1D, 0.375D, -1D, -0.875D, 2.375D, 1D);
                    aabb = DummyableBlock.getAABBRotationOffset(aabb, bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5, this.getRot());

                    for(LivingEntity e : this.level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                        if(e.isAlive() && e.hurt(this.level.damageSources().source(NtmDamageTypes.TURBOFAN), 100)) {
                            SoundUtils.playAtVec3(this.level, new Vec3(e.getX(), e.getY(), e.getZ()),
                                    SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS,
                                    2.0F, 0.95F + this.level.random.nextFloat() * 0.2F);
                            this.spawnBloodBurst(e);
                        }
                    }

                } else {
                    this.progress = 0;
                }

                if(this.heat > 300) {

                    this.overspeed++;

                    if(this.overspeed > 60 && this.warnCooldown == 0) {
                        this.warnCooldown = 100;
                        this.level.playSound(null, bp.above(), NtmSoundEvents.WARN_OVERSPEED.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
                    }

                    if(this.overspeed > 300) {
                        this.hasBlade = false;
                        this.level.explode(null, bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, 5F, false, Level.ExplosionInteraction.NONE);

                        Direction dir = this.getDir();
                        Sawblade cog = new Sawblade(this.level, bp.getX() + 0.5 + dir.getStepX(), bp.getY() + 1, bp.getZ() + 0.5 + dir.getStepZ());
                        cog.setOrientation(dir.get3DDataValue());

                        // ForgeDirection.getRotation(DOWN) entspricht getCounterClockWise(Axis.Y)
                        Direction rot = dir.getCounterClockWise(Axis.Y);

                        cog.setDeltaMovement(rot.getStepX(), 1 + (this.heat - 100) * 0.0001D, rot.getStepZ());
                        this.level.addFreshEntity(cog);

                        this.setChanged();
                    }

                } else {
                    this.overspeed = 0;
                }
            } else {
                this.overspeed = 0;
                this.warnCooldown = 0;
            }

            this.networkPackNT(150);

            this.heat = 0;

        } else {

            float momentum = this.heat * 25F / ((float) 300);

            this.lastSpin = this.spin;
            this.spin += momentum;

            if(this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }
        }
    }

    /** Entspricht ForgeDirection.getOrientation(meta - BlockDummyable.offset) des Originals. */
    private Direction getDir() {
        return this.getBlockState().getValue(DummyableBlock.FACING);
    }

    /** Entspricht dir.getRotation(ForgeDirection.UP), also getClockWise(Axis.Y). */
    private Direction getRot() {
        return this.getDir().getClockWise(Axis.Y);
    }

    /**
     * Im Original ein AuxParticlePacketNT mit "vanillaburst"/"blockdust" aus
     * Redstoneblock-Bruchstuecken. Der Port kennt diesen Partikeltyp nicht, daher
     * wird der vorhandene ParticleBurst mit demselben Block benutzt.
     */
    private void spawnBloodBurst(LivingEntity e) {
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        double px = e.getX();
        double py = e.getY() + e.getBbHeight() * 0.5;
        double pz = e.getZ();

        PacketDistributor.sendToPlayersNear(serverLevel, null, px, py, pz, 50,
                new ParticleBurst(BlockPos.containing(px, py, pz), Blocks.REDSTONE_BLOCK));
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heat);
        buf.writeInt(this.progress);
        buf.writeBoolean(this.hasBlade);

        for(ItemStack slot : this.slots) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, slot);
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.progress = buf.readInt();
        this.hasBlade = buf.readBoolean();

        for(int i = 0; i < this.slots.size(); i++) {
            this.slots.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Maschinen ohne "hasBlade"-Eintrag starten mit Blatt, wie das Feld voreingestellt ist
        this.hasBlade = !tag.contains("hasBlade") || tag.getBoolean("hasBlade");
        this.progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("hasBlade", this.hasBlade);
        tag.putInt("progress", this.progress);
    }

    /**
     * Ersatz fuer die Schadenswerte 0 und 1 des Blockgegenstands aus 1.7.10: nur ein
     * Saegewerk OHNE Blatt bekommt Zusatzdaten mit, alles andere faellt als sauberer
     * Gegenstand.
     */
    @Override
    public void writeNBT(CompoundTag savedTag) {
        if(this.hasBlade) return;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("hasBlade", false);
        savedTag.put(NBT_PERSISTENT_KEY, tag);
    }

    @Override
    public void readNBT(CompoundTag savedTag) {
        CompoundTag tag = savedTag.getCompound(NBT_PERSISTENT_KEY);
        this.hasBlade = !tag.contains("hasBlade") || tag.getBoolean("hasBlade");
    }

    protected void tryPullHeat() {
        if(this.level == null) return;

        BlockEntity con = this.level.getBlockEntity(this.getBlockPos().below());

        if(con instanceof IHeatSource source) {
            int heatSrc = (int) (source.getHeatStored() * diffusion);

            if(heatSrc > 0) {
                source.useUpHeat(heatSrc);
                this.heat += heatSrc;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack stack) {
        return i == 0
                && this.slots.get(0).isEmpty() && this.slots.get(1).isEmpty() && this.slots.get(2).isEmpty()
                && stack.getCount() == 1
                && !this.getOutput(stack).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack stack, Direction direction) {
        return i > 0;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 0, 1, 2 };
    }

    public ItemStack getOutput(ItemStack input) {

        if(input == null || input.isEmpty()) return ItemStack.EMPTY;

        if(input.is(RODS_WOODEN)) {
            return new ItemStack(NtmItems.POWDER_SAWDUST.get());
        }

        if(input.is(ItemTags.LOGS) && this.level != null) {
            CraftingInput craftingInput = CraftingInput.of(1, 1, List.of(input));
            Optional<RecipeHolder<CraftingRecipe>> recipe = this.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, this.level);

            if(recipe.isPresent()) {
                ItemStack out = recipe.get().value().assemble(craftingInput, this.level.registryAccess());
                if(!out.isEmpty()) {
                    out = out.copy(); // sicherheitshalber
                    out.setCount(out.getCount() * 6 / 4); // aus 4 Brettern werden 6
                    return out;
                }
            }
        }

        if(input.is(ItemTags.PLANKS)) {
            return new ItemStack(Items.STICK, 6);
        }

        if(input.is(ItemTags.SAPLINGS)) {
            return new ItemStack(Items.STICK, 1);
        }

        return ItemStack.EMPTY;
    }

    /** Das Saegewerk hat im Original keine Oberflaeche, alles laeuft ueber Rechtsklick am Block. */
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return null;
    }

    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            BlockPos pos = this.worldPosition;
            this.renderBox = new AABB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 2,
                    pos.getZ() + 2);
        }

        return this.renderBox;
    }
}
