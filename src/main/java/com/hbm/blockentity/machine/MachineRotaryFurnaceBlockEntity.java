package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.IConditionalInvAccess;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.MachinePollutingBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.menus.MachineRotaryFurnaceMenu;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.module.ModuleBurnTime;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.helper.FoundryCreator;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace.
 *
 * Der Drehrohrofen. Er braucht dreierlei: Zutaten, Dampf und ein Feuer darunter. Das Feuer
 * bestimmt dabei nicht die Temperatur, sondern das Tempo -- ein besserer Brennstoff laesst die
 * Trommel schneller laufen und frisst dafuer ueberproportional mehr Dampf. Heraus kommt kein
 * Gegenstand, sondern fluessiges Material, das vorn aus der Trommel laeuft.
 */
public class MachineRotaryFurnaceBlockEntity extends MachinePollutingBlockEntity implements IFluidStandardTransceiverMK2, IFluidCopiable, IConditionalInvAccess {

    public final FluidTank[] tanks = new FluidTank[3];
    public boolean isProgressing;
    public float progress;
    public int burnTime;
    public double burnHeat = 1D;
    public int maxBurnTime;
    public int steamUsed = 0;
    public boolean isVenting;
    public MaterialStack output;

    public static final int maxOutput = MaterialShapes.BLOCK.q(16);

    public int anim;
    public int lastAnim;

    /**
     * Der Ofen hat keine eigene Temperatur -- der Hitzefaktor des Brennstoffs wirkt hier auf
     * den Fortschritt je Feuer statt auf eine Waermezahl.
     */
    public static final ModuleBurnTime burnModule = new ModuleBurnTime()
            .setCokeTimeMod(1.25)
            .setRocketTimeMod(1.5)
            .setSolidTimeMod(1.5)
            .setBalefireTimeMod(1.5)
            .setSolidHeatMod(1.5)
            .setRocketHeatMod(3)
            .setBalefireHeatMod(10);

    private AABB renderBox;

    public MachineRotaryFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ROTARY_FURNACE.get(), pos, state, 5, 50);

        this.tanks[0] = new FluidTank(Fluids.NONE, 16_000);
        this.tanks[1] = new FluidTank(Fluids.STEAM, 12_000);
        this.tanks[2] = new FluidTank(Fluids.SPENTSTEAM, 120);
    }

    public static void readConfig(JsonObject obj) {
        if(obj.has("M:burnModule")) burnModule.readIfPresent(obj.get("M:burnModule").getAsJsonObject());
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("M:burnModule").beginObject();
        burnModule.writeConfig(writer);
        writer.endObject();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineRotaryFurnace");
    }

    /** In welche Richtung der Ofen zeigt. */
    public Direction getPointing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    /** Die Achse quer dazu -- dort sitzen Ausguss, Abgas und die Anschluesse. */
    public Direction getRot() {
        return this.getPointing().getCounterClockWise();
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        Direction dir = this.getPointing();
        Direction rot = this.getRot();

        if(this.level.isClientSide) {
            this.clientUpdate(dir, rot);
            return;
        }

        this.tanks[0].setType(3, this.slots);

        for(DirPos pos : this.getSteamPos()) {
            this.trySubscribe(this.tanks[1].getTankType(), this.level, pos);
            if(this.tanks[2].getFill() > 0) this.tryProvide(this.tanks[2], this.level, pos);
        }

        if(this.tanks[0].getTankType() != Fluids.NONE) {
            for(DirPos pos : this.getFluidPos()) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
        }

        this.sendSmoke(new DirPos[] { new DirPos(
                this.worldPosition.getX() + rot.getStepX(), this.worldPosition.getY() + 5, this.worldPosition.getZ() + rot.getStepZ(), Direction.UP) });

        this.pourOutput(rot);

        RotaryFurnaceRecipe recipe = RotaryFurnaceRecipes.getRecipe(this.slots.get(0), this.slots.get(1), this.slots.get(2));
        this.isProgressing = false;

        if(recipe != null) {
            this.process(recipe);
        } else {
            this.progress = 0;
        }

        this.isVenting = false;
        this.networkPackNT(50);
    }

    private void process(RotaryFurnaceRecipe recipe) {

        if(this.burnTime <= 0 && burnModule.getBurnTime(this.slots.get(4)) > 0) {
            this.burnHeat = burnModule.getMod(this.slots.get(4), burnModule.getModHeat());
            this.maxBurnTime = this.burnTime = burnModule.getBurnTime(this.slots.get(4)) / 2;
            this.removeItem(4, 1);
            this.setChanged();
        }

        float processSpeed = Math.max((float) this.burnHeat, 1F);
        float steamUseMult = (float) (10 * Math.log10(processSpeed) + 1);

        if(this.canProcess(recipe, steamUseMult)) {

            this.progress += processSpeed / recipe.duration;
            this.tanks[1].setFill((int) (this.tanks[1].getFill() - recipe.steam * steamUseMult));
            this.steamUsed += (int) (recipe.steam * steamUseMult);
            this.isProgressing = true;

            if(this.progress >= 1F) {
                this.progress -= 1F;
                this.consumeItems(recipe);

                if(this.output == null) {
                    this.output = recipe.output.copy();
                } else {
                    this.output.amount += recipe.output.amount;
                }

                this.setChanged();
            }

            if(this.burnTime > 0) {
                this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 10F);
                this.burnTime--;
            }

        } else {
            this.progress = 0;
        }

        // ein Hundertstel des verbrauchten Dampfes kommt als Abdampf zurueck
        if(this.steamUsed >= 100) {
            int steamReturn = this.steamUsed / 100;
            int canReturn = this.tanks[2].getMaxFill() - this.tanks[2].getFill();
            int doesReturn = Math.min(steamReturn, canReturn);
            this.steamUsed -= doesReturn * 100;
            this.tanks[2].setFill(this.tanks[2].getFill() + doesReturn);
        }
    }

    private void pourOutput(Direction rot) {

        if(this.output == null) return;

        int prev = this.output.amount;
        double spoutX = this.worldPosition.getX() + 0.5D + rot.getStepX() * 2.875D;
        double spoutZ = this.worldPosition.getZ() + 0.5D + rot.getStepZ() * 2.875D;

        Vec3[] impact = new Vec3[1];
        this.output = CrucibleUtil.pourSingleStack(this.level, spoutX, this.worldPosition.getY() + 1.25D, spoutZ,
                6, true, this.output, MaterialShapes.INGOT.q(1), impact);

        if(this.output != null && prev != this.output.amount) {

            float length = 1F;
            if(impact[0] != null) length = (float) Math.max(1D, this.worldPosition.getY() + 1 - (Math.ceil(impact[0].y) - 1.125D));

            FoundryCreator.composeEffect(this.level, spoutX, this.worldPosition.getY() + 0.75D, spoutZ,
                    this.output.material.moltenColor, rot, length, 0.625F, 0.625F);
        }

        if(this.output != null && this.output.amount <= 0) this.output = null;
    }

    private void clientUpdate(Direction dir, Direction rot) {

        if(this.burnTime > 0) {
            this.level.addParticle(ParticleTypes.FLAME,
                    this.worldPosition.getX() + 0.5 + dir.getStepX() * 0.5 + rot.getStepX() + this.level.random.nextGaussian() * 0.25,
                    this.worldPosition.getY() + 0.375,
                    this.worldPosition.getZ() + 0.5 + dir.getStepZ() * 0.5 + rot.getStepZ() + this.level.random.nextGaussian() * 0.25,
                    0, 0, 0);
        }

        if(this.isVenting && this.level.getGameTime() % 2 == 0) {

            CompoundTag fx = new CompoundTag();
            fx.putFloat("lift", 10F);
            fx.putFloat("base", 0.25F);
            fx.putFloat("max", 2.5F);
            fx.putInt("life", 100 + this.level.random.nextInt(20));
            fx.putInt("color", 0x202020);

            ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                    this.worldPosition.getX() + 0.5 + rot.getStepX(), this.worldPosition.getY() + 5, this.worldPosition.getZ() + 0.5 + rot.getStepZ());
        }

        this.lastAnim = this.anim;
        if(this.isProgressing) {
            this.anim += (int) Math.max(burnModule.getMod(this.slots.get(4), burnModule.getModHeat()), 1);
        }
    }

    public boolean canProcess(RotaryFurnaceRecipe recipe, float steamUseMult) {

        if(this.burnTime <= 0) return false;

        if(recipe.fluid != null) {
            if(this.tanks[0].getTankType() != recipe.fluid.type) return false;
            if(this.tanks[0].getFill() < recipe.fluid.fill) return false;
        }

        if(this.tanks[1].getFill() < recipe.steam * steamUseMult) return false;
        if(this.tanks[2].getMaxFill() - this.tanks[2].getFill() < recipe.steam * steamUseMult / 100) return false;
        if(this.steamUsed > 100) return false;

        if(this.output != null) {
            if(this.output.material != recipe.output.material) return false;
            if(this.output.amount + recipe.output.amount > maxOutput) return false;
        }

        return true;
    }

    public void consumeItems(RotaryFurnaceRecipe recipe) {

        for(AStack aStack : recipe.ingredients) {
            for(int i = 0; i < 3; i++) {
                ItemStack stack = this.slots.get(i);
                if(aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
                    this.removeItem(i, aStack.stacksize);
                    break;
                }
            }
        }

        if(recipe.fluid != null) {
            this.tanks[0].setFill(this.tanks[0].getFill() - recipe.fluid.fill);
        }
    }

    /**
     * Wie in der Wurzelklasse, aber der Ofen merkt sich, wenn der Rauchpuffer ueberlaeuft --
     * dann qualmt der Schornstein sichtbar.
     */
    @Override
    public void pollute(PollutionType type, float amount) {

        FluidTank tank = switch(type) {
            case SOOT -> this.smoke;
            case HEAVYMETAL -> this.smokeLeaded;
            default -> this.smokePoison;
        };

        if(tank.getFill() + (int) Math.ceil(amount * 100F) > tank.getMaxFill()) this.isVenting = true;

        super.pollute(type, amount);
    }

    public DirPos[] getSteamPos() {

        Direction dir = this.getPointing();
        Direction rot = this.getRot();
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x - dir.getStepX() * 2 - rot.getStepX() * 2, y, z - dir.getStepZ() * 2 - rot.getStepZ() * 2, dir.getOpposite()),
                new DirPos(x - dir.getStepX() * 2 - rot.getStepX(), y, z - dir.getStepZ() * 2 - rot.getStepZ(), dir.getOpposite())
        };
    }

    public DirPos[] getFluidPos() {

        Direction dir = this.getPointing();
        Direction rot = this.getRot();
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() + rot.getStepX() * 3, y, z + dir.getStepZ() + rot.getStepZ() * 3, rot),
                new DirPos(x - dir.getStepX() + rot.getStepX() * 3, y, z - dir.getStepZ() + rot.getStepZ() * 3, rot)
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
        buf.writeBoolean(this.isVenting);
        buf.writeBoolean(this.isProgressing);
        buf.writeFloat(this.progress);
        buf.writeInt(this.burnTime);
        buf.writeInt(this.maxBurnTime);

        buf.writeBoolean(this.output != null);
        if(this.output != null) {
            buf.writeInt(this.output.material.id);
            buf.writeInt(this.output.amount);
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
        this.isVenting = buf.readBoolean();
        this.isProgressing = buf.readBoolean();
        this.progress = buf.readFloat();
        this.burnTime = buf.readInt();
        this.maxBurnTime = buf.readInt();

        if(buf.readBoolean()) {
            NTMMaterial mat = Mats.matById.get(buf.readInt());
            int amount = buf.readInt();
            this.output = mat == null ? null : new MaterialStack(mat, amount);
        } else {
            this.output = null;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        for(int i = 0; i < 3; i++) this.tanks[i].readFromNBT(tag, "t" + i);

        this.progress = tag.getFloat("prog");
        this.burnTime = tag.getInt("burn");
        this.burnHeat = tag.getDouble("heat");
        this.maxBurnTime = tag.getInt("maxBurn");

        if(tag.contains("outType")) {
            NTMMaterial mat = Mats.matById.get(tag.getInt("outType"));
            this.output = mat == null ? null : new MaterialStack(mat, tag.getInt("outAmount"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        for(int i = 0; i < 3; i++) this.tanks[i].writeToNBT(tag, "t" + i);

        tag.putFloat("prog", this.progress);
        tag.putInt("burn", this.burnTime);
        tag.putDouble("heat", this.burnHeat);
        tag.putInt("maxBurn", this.maxBurnTime);

        if(this.output != null) {
            tag.putInt("outType", this.output.material.id);
            tag.putInt("outAmount", this.output.amount);
        }
    }

    @Override public int[] getSlotsForFace(Direction direction) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot < 3 || slot == 4; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }

    /* --- Nur die richtigen Huellenbloecke geben Zugriff auf das jeweilige Fach --- */

    @Override public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) { return slot < 3 || slot == 4; }
    @Override public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side) { return false; }

    @Override
    public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side) {

        Direction dir = this.getPointing();
        Direction rot = dir.getClockWise();
        BlockPos core = this.worldPosition;

        if(side == dir.getOpposite() && pos.equals(core.relative(dir, -1).relative(rot, -2))) return new int[] { 0 };
        if(side == dir.getOpposite() && pos.equals(core.relative(dir, -1).relative(rot, -1))) return new int[] { 1 };
        if(side == dir.getOpposite() && pos.equals(core.relative(dir, -1))) return new int[] { 2 };
        if(side == dir && pos.equals(core.relative(dir, 1).relative(rot, -1))) return new int[] { 4 };

        return new int[0];
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tanks[0], this.tanks[1], this.tanks[2], this.smoke }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[2], this.smoke }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0], this.tanks[1] }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineRotaryFurnaceMenu(id, inventory, this);
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 5, z + 3);
        }
        return this.renderBox;
    }

    public @Nullable MaterialStack getOutput() { return this.output; }
}
