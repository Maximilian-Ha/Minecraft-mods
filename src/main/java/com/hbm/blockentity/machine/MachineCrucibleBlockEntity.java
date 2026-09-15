package com.hbm.blockentity.machine;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.tile.IHeatSource;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.config.IConfigurableMachine;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.menus.MachineCrucibleMenu;
import com.hbm.inventory.recipes.CrucibleRecipe;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.helper.FoundryCreator;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityCrucible.
 *
 * Der Tiegel. Er zieht Hitze aus dem Block unter sich, schmilzt ein, was hineingeworfen oder
 * eingelegt wird, und haelt das Ergebnis in zwei getrennten Baendern: was zum eingestellten
 * Rezept gehoert, und der Rest. Beides laeuft aus je einem Ausguss ab -- das Rezeptband nach
 * vorn, der Abraum nach hinten.
 *
 * ABWEICHUNG: das Original nimmt fuer den Schaden an Lebewesen DamageSource.lava. Auf 1.21 ist
 * das ein ResourceKey, der erst zur Laufzeit aufgeloest wird.
 */
public class MachineCrucibleBlockEntity extends MachineBaseBlockEntity implements ICrucibleAcceptor, IControlReceiver {

    public int heat;
    public int progress;

    public String recipe = "null";

    public List<MaterialStack> recipeStack = new ArrayList<>();
    public List<MaterialStack> wasteStack = new ArrayList<>();

    /* einstellbare Groessen, siehe hbmMachines.json */
    public static int recipeCapacity = MaterialShapes.BLOCK.q(16);
    public static int wasteCapacity = MaterialShapes.BLOCK.q(16);
    public static int processTime = 20_000;
    public static double diffusion = 0.25D;
    public static int maxHeat = 100_000;

    private AABB renderBox;

    public MachineCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_CRUCIBLE.get(), pos, state, 10);
    }

    public static void readConfig(JsonObject obj) {
        recipeCapacity = IConfigurableMachine.grab(obj, "I:recipeCapacity", recipeCapacity);
        wasteCapacity = IConfigurableMachine.grab(obj, "I:wasteCapacity", wasteCapacity);
        processTime = IConfigurableMachine.grab(obj, "I:processHeat", processTime);
        diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
        maxHeat = IConfigurableMachine.grab(obj, "I:heatCap", maxHeat);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:recipeCapacity").value(recipeCapacity);
        writer.name("I:wasteCapacity").value(wasteCapacity);
        writer.name("I:processHeat").value(processTime);
        writer.name("D:diffusion").value(diffusion);
        writer.name("I:heatCap").value(maxHeat);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineCrucible");
    }

    /** Ein Gegenstand je Fach -- sonst verstopft der Tiegel. */
    @Override public int getMaxStackSize() { return 1; }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.clientUpdate();
            return;
        }

        this.tryPullHeat();
        this.collectItems();
        this.burnEntities();

        if(!this.trySmelt()) this.progress = 0;
        this.tryRecipe();

        this.pourWaste();
        this.pourRecipe();

        this.recipeStack.removeIf(o -> o.amount <= 0);
        this.wasteStack.removeIf(o -> o.amount <= 0);

        this.networkPackNT(25);
    }

    private void clientUpdate() {

        if(this.recipeStack.isEmpty() && this.wasteStack.isEmpty()) return;
        if(this.level.getGameTime() % 10 != 0) return;

        CompoundTag fx = new CompoundTag();
        fx.putFloat("lift", 10F);
        fx.putFloat("base", 0.75F);
        fx.putFloat("max", 3.5F);
        fx.putInt("life", 100 + this.level.random.nextInt(20));
        fx.putInt("color", 0x202020);

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 0.5);
    }

    /** Was oben hineingeworfen wird, landet in den Faechern. */
    private void collectItems() {

        if(this.level.getGameTime() % 5 != 0) return;

        AABB box = new AABB(this.worldPosition.getX() - 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() - 0.5,
                this.worldPosition.getX() + 1.5, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 1.5);

        for(ItemEntity item : this.level.getEntitiesOfClass(ItemEntity.class, box)) {

            if(item.isRemoved()) continue;

            ItemStack stack = item.getItem();
            if(!this.isItemSmeltable(stack)) continue;

            for(int i = 1; i < 10; i++) {

                if(!this.slots.get(i).isEmpty()) continue;

                if(stack.getCount() == 1) {
                    this.slots.set(i, stack.copy());
                    item.discard();
                    this.setChanged();
                    break;
                }

                ItemStack single = stack.copy();
                single.setCount(1);
                this.slots.set(i, single);
                stack.shrink(1);
                this.setChanged();

                // Das Original laesst hier einen leeren Stapel zurueck; auf 1.21 raeumen wir ihn
                // gleich weg, statt auf den Aufraeumtakt der Entitaet zu warten.
                if(stack.isEmpty()) { item.discard(); break; }
            }
        }
    }

    /** Wer in die Schmelze steigt, verbrennt. */
    private void burnEntities() {

        int totalMass = getQuantaFromType(this.recipeStack, null) + getQuantaFromType(this.wasteStack, null);
        double fill = ((double) totalMass / (double) (recipeCapacity + wasteCapacity)) * 0.875D;

        AABB box = new AABB(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5 + fill, this.worldPosition.getZ() + 0.5).inflate(1, 0, 1);

        for(LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, box)) {
            entity.hurt(entity.damageSources().source(DamageTypes.LAVA), 5F);
            entity.setRemainingFireTicks(100);
        }
    }

    /** Der Ausguss hinten: alles, was das Rezept nicht braucht. */
    private void pourWaste() {

        if(this.wasteStack.isEmpty()) return;

        this.pour(this.getPointing().getOpposite(), this.wasteStack);
        PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 20F);
    }

    /** Der Ausguss vorn: was das Rezept herstellt. Ohne Rezept laeuft alles ab. */
    private void pourRecipe() {

        if(this.recipeStack.isEmpty()) return;

        CrucibleRecipe recipe = this.getLoadedRecipe();
        List<MaterialStack> toCast = new ArrayList<>();

        if(recipe == null) {
            toCast.addAll(this.recipeStack);
        } else {
            for(MaterialStack stack : this.recipeStack) {
                for(MaterialStack output : recipe.output) {
                    if(stack.material == output.material) { toCast.add(stack); break; }
                }
            }
        }

        this.pour(this.getPointing(), toCast);
        PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 20F);
    }

    private void pour(Direction dir, List<MaterialStack> stacks) {

        if(stacks.isEmpty()) return;

        double spoutX = this.worldPosition.getX() + 0.5D + dir.getStepX() * 1.875D;
        double spoutZ = this.worldPosition.getZ() + 0.5D + dir.getStepZ() * 1.875D;

        Vec3[] impact = new Vec3[1];
        MaterialStack didPour = CrucibleUtil.pourFullStack(this.level, spoutX, this.worldPosition.getY() + 0.25D, spoutZ,
                6, true, stacks, MaterialShapes.NUGGET.q(3), impact);

        if(didPour == null) return;

        float length = 1F;
        if(impact[0] != null) length = (float) Math.max(1D, this.worldPosition.getY() - (Math.ceil(impact[0].y) - 0.875D));

        FoundryCreator.composeEffect(this.level, spoutX, this.worldPosition.getY(), spoutZ,
                didPour.material.moltenColor, dir, length, 0.625F, 0.625F);
    }

    /** In welche Richtung der Tiegel zeigt -- dorthin sitzt der Rezeptausguss. */
    public Direction getPointing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    protected void tryPullHeat() {

        if(this.heat >= maxHeat) return;

        BlockEntity below = this.level.getBlockEntity(this.worldPosition.below());

        if(below instanceof IHeatSource source) {

            int diff = source.getHeatStored() - this.heat;

            if(diff == 0) return;

            diff = Math.min(diff, maxHeat - this.heat);

            if(diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                source.useUpHeat(diff);
                this.heat = Math.min(this.heat + diff, maxHeat);
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    protected boolean trySmelt() {

        if(this.heat < maxHeat / 2) return false;

        int slot = this.getFirstSmeltableSlot();
        if(slot == -1) return false;

        int delta = (int) ((this.heat - (maxHeat / 2)) * 0.05);

        this.progress += delta;
        this.heat -= delta;

        if(this.progress >= processTime) {

            this.progress = 0;

            List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(this.slots.get(slot));
            CrucibleRecipe recipe = this.getLoadedRecipe();

            for(MaterialStack material : materials) {

                boolean recipeMaterial = recipe != null
                        && (getQuantaFromType(recipe.input, material.material) > 0 || getQuantaFromType(recipe.output, material.material) > 0);

                this.addToStack(recipeMaterial ? this.recipeStack : this.wasteStack, material);
            }

            this.removeItem(slot, 1);
        }

        return true;
    }

    protected void tryRecipe() {

        CrucibleRecipe recipe = this.getLoadedRecipe();

        if(recipe == null) return;
        if(this.level.getGameTime() % Math.max(1, recipe.frequency) > 0) return;

        for(MaterialStack stack : recipe.input) {
            if(getQuantaFromType(this.recipeStack, stack.material) < stack.amount) return;
        }

        for(MaterialStack stack : this.recipeStack) {
            stack.amount -= getQuantaFromType(recipe.input, stack.material);
        }

        outer:
        for(MaterialStack out : recipe.output) {

            for(MaterialStack stack : this.recipeStack) {
                if(stack.material == out.material) {
                    stack.amount += out.amount;
                    continue outer;
                }
            }

            this.recipeStack.add(out.copy());
        }
    }

    protected int getFirstSmeltableSlot() {

        for(int i = 1; i < 10; i++) {
            if(!this.slots.get(i).isEmpty() && this.isItemSmeltable(this.slots.get(i))) return i;
        }

        return -1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.isItemSmeltable(stack);
    }

    public boolean isItemSmeltable(ItemStack stack) {

        List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(stack);

        if(materials.isEmpty()) return false;

        CrucibleRecipe recipe = this.getLoadedRecipe();

        // ohne Rezept stimmt immer alles
        boolean matchesRecipe = recipe == null;

        int recipeContent = recipe != null ? recipe.getInputAmount() : 0;
        int recipeAmount = getQuantaFromType(this.recipeStack, null);
        int wasteAmount = getQuantaFromType(this.wasteStack, null);

        for(MaterialStack mat : materials) {

            int recipeInputRequired = recipe != null ? getQuantaFromType(recipe.input, mat.material) : 0;

            // so kann man das Ergebnis wieder in den Tiegel zurueckgiessen
            if(recipe != null && getQuantaFromType(recipe.output, mat.material) > 0) {
                recipeAmount += mat.amount;
                matchesRecipe = true;
                continue;
            }

            if(recipeInputRequired == 0) {
                wasteAmount += mat.amount;
            } else {

                // das Hoechstmass ist das Verhaeltnis des Rezepts, hochgerechnet auf die Kapazitaet
                int matMaximum = recipeInputRequired * recipeCapacity / recipeContent;
                int amountStored = getQuantaFromType(this.recipeStack, mat.material);

                matchesRecipe = true;
                recipeAmount += mat.amount;

                if(amountStored + mat.amount > matMaximum) return false;
            }
        }

        return recipeAmount <= recipeCapacity && wasteAmount <= wasteCapacity && matchesRecipe;
    }

    public void addToStack(List<MaterialStack> stack, MaterialStack matStack) {

        for(MaterialStack mat : stack) {
            if(mat.material == matStack.material) {
                mat.amount += matStack.amount;
                return;
            }
        }

        stack.add(matStack.copy());
    }

    public @Nullable CrucibleRecipe getLoadedRecipe() {
        return CrucibleRecipes.INSTANCE.recipeNameMap.get(this.recipe);
    }

    public static int getQuantaFromType(MaterialStack[] stacks, @Nullable NTMMaterial mat) {
        for(MaterialStack stack : stacks) {
            if(mat == null || stack.material == mat) return stack.amount;
        }
        return 0;
    }

    public static int getQuantaFromType(List<MaterialStack> stacks, @Nullable NTMMaterial mat) {
        int sum = 0;
        for(MaterialStack stack : stacks) {
            if(stack.material == mat) return stack.amount;
            if(mat == null) sum += stack.amount;
        }
        return sum;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot > 0 && this.isItemSmeltable(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.progress);
        buf.writeInt(this.heat);
        buf.writeUtf(this.recipe);
        writeStack(buf, this.recipeStack);
        writeStack(buf, this.wasteStack);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readInt();
        this.heat = buf.readInt();
        this.recipe = buf.readUtf();
        readStack(buf, this.recipeStack);
        readStack(buf, this.wasteStack);
    }

    private static void writeStack(RegistryFriendlyByteBuf buf, List<MaterialStack> stacks) {
        buf.writeShort(stacks.size());
        for(MaterialStack stack : stacks) {
            buf.writeInt(stack.material == null ? -1 : stack.material.id);
            buf.writeInt(stack.amount);
        }
    }

    private static void readStack(RegistryFriendlyByteBuf buf, List<MaterialStack> stacks) {

        stacks.clear();
        int count = buf.readShort();

        for(int i = 0; i < count; i++) {
            NTMMaterial mat = Mats.matById.get(buf.readInt());
            int amount = buf.readInt();
            if(mat != null) stacks.add(new MaterialStack(mat, amount));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.recipe = tag.getString("recipe");
        this.progress = tag.getInt("progress");
        this.heat = tag.getInt("heat");

        this.recipeStack.clear();
        this.wasteStack.clear();
        loadStack(tag.getIntArray("rec"), this.recipeStack);
        loadStack(tag.getIntArray("was"), this.wasteStack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putString("recipe", this.recipe);
        tag.putInt("progress", this.progress);
        tag.putInt("heat", this.heat);
        tag.putIntArray("rec", saveStack(this.recipeStack));
        tag.putIntArray("was", saveStack(this.wasteStack));
    }

    private static void loadStack(int[] data, List<MaterialStack> stacks) {
        for(int i = 0; i < data.length / 2; i++) {
            NTMMaterial mat = Mats.matById.get(data[i * 2]);
            if(mat != null) stacks.add(new MaterialStack(mat, data[i * 2 + 1]));
        }
    }

    private static int[] saveStack(List<MaterialStack> stacks) {
        int[] data = new int[stacks.size() * 2];
        for(int i = 0; i < stacks.size(); i++) {
            MaterialStack stack = stacks.get(i);
            data[i * 2] = stack.material.id;
            data[i * 2 + 1] = stack.amount;
        }
        return data;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCrucibleMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("index") && data.contains("selection") && data.getInt("index") == 0) {
            this.recipe = data.getString("selection");
            this.setChanged();
        }
    }

    /* --- Der Tiegel nimmt selbst Guss an: ohne Rezept in den Abraum, sonst in das Rezeptband --- */

    @Override
    public boolean canAcceptPartialPour(net.minecraft.world.level.Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {

        CrucibleRecipe recipe = this.getLoadedRecipe();

        if(recipe == null) return getQuantaFromType(this.wasteStack, null) < wasteCapacity;

        int recipeContent = recipe.getInputAmount();
        if(recipeContent <= 0) return false;

        int matMaximum = getQuantaFromType(recipe.input, stack.material) * recipeCapacity / recipeContent;

        return getQuantaFromType(this.recipeStack, stack.material) < matMaximum
                && getQuantaFromType(this.recipeStack, null) < recipeCapacity;
    }

    @Override
    public MaterialStack pour(net.minecraft.world.level.Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {

        CrucibleRecipe recipe = this.getLoadedRecipe();

        if(recipe == null) {

            int amount = getQuantaFromType(this.wasteStack, null);

            if(amount + stack.amount <= wasteCapacity) {
                this.addToStack(this.wasteStack, stack.copy());
                return null;
            }

            int toAdd = wasteCapacity - amount;
            this.addToStack(this.wasteStack, new MaterialStack(stack.material, toAdd));
            return new MaterialStack(stack.material, stack.amount - toAdd);
        }

        int recipeContent = recipe.getInputAmount();
        if(recipeContent <= 0) return stack;

        int recipeInputRequired = getQuantaFromType(recipe.input, stack.material);
        int matMaximum = recipeInputRequired * recipeCapacity / recipeContent;

        if(recipeInputRequired + stack.amount <= matMaximum) {
            this.addToStack(this.recipeStack, stack.copy());
            return null;
        }

        int toAdd = Math.min(matMaximum - stack.amount, recipeCapacity - getQuantaFromType(this.recipeStack, null));
        if(toAdd <= 0) return stack;

        this.addToStack(this.recipeStack, new MaterialStack(stack.material, toAdd));
        return new MaterialStack(stack.material, stack.amount - toAdd);
    }

    @Override public boolean canAcceptPartialFlow(net.minecraft.world.level.Level level, BlockPos pos, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack flow(net.minecraft.world.level.Level level, BlockPos pos, Direction side, MaterialStack stack) { return stack; }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 2, z + 2);
        }
        return this.renderBox;
    }
}
