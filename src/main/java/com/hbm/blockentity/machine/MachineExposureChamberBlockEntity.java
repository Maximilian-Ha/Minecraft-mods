package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.menus.MachineExposureChamberMenu;
import com.hbm.inventory.recipes.ExposureChamberRecipes;
import com.hbm.inventory.recipes.ExposureChamberRecipes.ExposureChamberRecipe;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineExposureChamber.
 *
 * Die Bestrahlungskammer ist der Abnehmer des Teilchenbeschleunigers -- und der einzige Weg zu
 * den vier Endstoffen des Mods: Schraranium, Schrabidium, Euphemium und Dineutronium.
 *
 * EINE KAPSEL REICHT FUER ACHT DURCHGAENGE. Das ist der eigentliche Kniff: der Beschleuniger
 * liefert einzelne Teilchen, und ein einzelnes Teilchen je Barren waere ein Missverhaeltnis.
 * Die Kammer zieht die Kapsel ein, merkt sich acht Ladungen und gibt die leere Huelle sofort
 * zurueck.
 *
 * SIE ZIEHT ERST NACH, WENN NICHTS MEHR DA IST. Das verhindert, dass zwei verschiedene Kapseln
 * gleichzeitig hineingeraten und die Kammer mit einer Ladung dasteht, die zum eingelegten
 * Barren nicht passt.
 *
 * DIE FACHPRUEFUNG ARBEITET UEBER KREUZ: was hinein darf, haengt davon ab, was schon drin liegt.
 * Liegt ein Barren bereit, sind nur Teilchen erlaubt, die MIT DIESEM Barren ein Rezept ergeben
 * -- und umgekehrt. Steht die Kammer leer, ist alles erlaubt, was irgendwo in einem Rezept
 * vorkommt. So kann ein Trichter sie nicht verstopfen.
 *
 * NICHT UEBERNOMMEN: das Modell des Originals. Die Abmessungen stimmen -- der Bau ist neun
 * Bloecke lang und fuenf hoch, mit zwei Fluegeln und einem Kopfstueck am Ende.
 */
public class MachineExposureChamberBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IUpgradeInfoProvider {

    /** Fach null nimmt die Kapsel, eins haelt sie fest, zwei gibt die Huelle zurueck. */
    public static final int SLOT_PARTICLE = 0;
    public static final int SLOT_PARTICLE_INTERNAL = 1;
    public static final int SLOT_PARTICLE_CONTAINER = 2;
    public static final int SLOT_INGREDIENT = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_UPGRADE_START = 6;
    public static final int SLOT_UPGRADE_END = 7;
    public static final int SLOTS = 8;

    public static final long maxPower = 1_000_000;
    public static final int processTimeBase = 200;
    public static final int consumptionBase = 10_000;

    /** Soviele Durchgaenge steckt eine einzelne Kapsel. */
    public static final int maxParticles = 8;

    public long power;
    public int progress;
    public int processTime = processTimeBase;
    public int consumption = consumptionBase;
    public int savedParticles;
    public boolean isOn = false;

    public float rotation;
    public float prevRotation;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private static final int[] ACCESS = { SLOT_PARTICLE, SLOT_PARTICLE_CONTAINER, SLOT_INGREDIENT, SLOT_OUTPUT };

    private AABB renderBox;

    public MachineExposureChamberBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_EXPOSURE_CHAMBER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.exposureChamber");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.isOn = false;
            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);

            /* Ersatz fuer autoPort() aus 1.7.10. */
            for(DirPos pos : this.getConPos()) this.trySubscribe(this.level, pos);

            this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
            int speedLevel = this.upgradeManager.getLevel(UpgradeType.SPEED);
            int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);
            int overdriveLevel = this.upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            this.consumption = consumptionBase;
            this.processTime = processTimeBase - processTimeBase / 4 * speedLevel;
            this.consumption *= (speedLevel / 2 + 1);
            this.processTime *= (powerLevel / 2 + 1);
            this.consumption /= (powerLevel + 1);
            this.processTime /= (overdriveLevel + 1);
            this.consumption *= (overdriveLevel * 2 + 1);

            this.tryLoadParticle();
            this.tryProcess();

            /* Ist die Ladung aufgebraucht, verschwindet auch der gemerkte Typ -- sonst bliebe
             * die Kammer auf einer Sorte stehen, die sie gar nicht mehr hat. */
            if(this.savedParticles <= 0) this.slots.set(SLOT_PARTICLE_INTERNAL, ItemStack.EMPTY);

            this.networkPackNT(50);

        } else {

            this.prevRotation = this.rotation;

            if(this.isOn) {
                this.rotation += 10F;
                if(this.rotation >= 720F) {
                    this.rotation -= 720F;
                    this.prevRotation -= 720F;
                }
            }
        }
    }

    /**
     * Zieht eine neue Kapsel ein -- aber nur, wenn keine Ladung mehr da ist und die Huelle
     * wirklich zurueckgelegt werden kann. Passt die Huelle nicht ins Rueckgabefach, bleibt die
     * Kapsel liegen, statt spurlos zu verschwinden.
     */
    private void tryLoadParticle() {

        if(!this.slots.get(SLOT_PARTICLE_INTERNAL).isEmpty()) return;
        if(this.savedParticles > 0) return;

        ItemStack particle = this.slots.get(SLOT_PARTICLE);
        ItemStack ingredient = this.slots.get(SLOT_INGREDIENT);
        if(particle.isEmpty() || ingredient.isEmpty()) return;

        if(ExposureChamberRecipes.getRecipe(particle, ingredient) == null) return;

        ItemStack container = particle.hasCraftingRemainingItem() ? particle.getCraftingRemainingItem() : ItemStack.EMPTY;
        ItemStack held = this.slots.get(SLOT_PARTICLE_CONTAINER);

        boolean canStore;

        if(container.isEmpty()) {
            canStore = true;
        } else if(held.isEmpty()) {
            this.slots.set(SLOT_PARTICLE_CONTAINER, container.copy());
            canStore = true;
        } else if(ItemStack.isSameItemSameComponents(held, container) && held.getCount() < held.getMaxStackSize()) {
            held.grow(1);
            canStore = true;
        } else {
            canStore = false;
        }

        if(canStore) {
            /* Die gemerkte Sorte traegt absichtlich die Anzahl null: sie ist nur ein Merkzettel,
             * kein Bestand -- der steckt in savedParticles. */
            ItemStack marker = particle.copy();
            marker.setCount(0);
            this.slots.set(SLOT_PARTICLE_INTERNAL, marker);
            this.removeItem(SLOT_PARTICLE, 1);
            this.savedParticles = maxParticles;
        }
    }

    private void tryProcess() {

        ItemStack marker = this.slots.get(SLOT_PARTICLE_INTERNAL);

        if(marker.isEmpty() || this.savedParticles <= 0 || this.power < this.consumption) {
            this.progress = 0;
            return;
        }

        ExposureChamberRecipe recipe = ExposureChamberRecipes.getRecipe(marker, this.slots.get(SLOT_INGREDIENT));

        if(recipe == null || !this.canOutput(recipe)) {
            this.progress = 0;
            return;
        }

        this.progress++;
        this.power -= this.consumption;
        this.isOn = true;

        if(this.progress >= this.processTime) {

            this.progress = 0;
            this.savedParticles--;
            this.removeItem(SLOT_INGREDIENT, 1);

            ItemStack out = this.slots.get(SLOT_OUTPUT);
            if(out.isEmpty()) this.slots.set(SLOT_OUTPUT, recipe.output.copy());
            else out.grow(recipe.output.getCount());

            this.setChanged();
        }
    }

    private boolean canOutput(ExposureChamberRecipe recipe) {

        ItemStack out = this.slots.get(SLOT_OUTPUT);
        if(out.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(out, recipe.output)
                && out.getCount() + recipe.output.getCount() <= out.getMaxStackSize();
    }

    public Direction getDir() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    public DirPos[] getConPos() {

        Direction dir = this.getDir();
        Direction rot = dir.getClockWise(Axis.Y).getOpposite();
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.relative(rot, 7).relative(dir, 2), dir),
                new DirPos(p.relative(rot, 7).relative(dir, -2), dir.getOpposite()),
                new DirPos(p.relative(rot, 8).relative(dir, 2), dir),
                new DirPos(p.relative(rot, 8).relative(dir, -2), dir.getOpposite()),
                new DirPos(p.relative(rot, 9), rot)
        };
    }

    /**
     * Ueber Kreuz: was schon drin liegt, entscheidet, was noch hinein darf. Ohne das koennte
     * ein Trichter eine Kapsel und einen Barren einlegen, die nicht zusammenpassen -- und die
     * Kammer stuende, ohne dass etwas herauskaeme.
     */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) return stack.getItem() instanceof MachineUpgradeItem;

        ItemStack ingredient = this.slots.get(SLOT_INGREDIENT);

        /* Was schon angefangen ist, darf aufgefuellt werden. */
        if(slot == SLOT_PARTICLE && !this.slots.get(SLOT_PARTICLE).isEmpty()) return true;
        if(slot == SLOT_INGREDIENT && !ingredient.isEmpty()) return true;

        /* Steht keine Ladung an, gilt die noch nicht verbrauchte Kapsel als Bezug. */
        ItemStack particle = !this.slots.get(SLOT_PARTICLE_INTERNAL).isEmpty()
                ? this.slots.get(SLOT_PARTICLE_INTERNAL)
                : this.slots.get(SLOT_PARTICLE);

        if(slot == SLOT_PARTICLE && particle.isEmpty() && !ingredient.isEmpty()) {
            return ExposureChamberRecipes.getRecipe(stack, ingredient) != null;
        }

        if(slot == SLOT_INGREDIENT && !particle.isEmpty() && ingredient.isEmpty()) {
            return ExposureChamberRecipes.getRecipe(particle, stack) != null;
        }

        if(particle.isEmpty() && ingredient.isEmpty()) {
            for(ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes) {
                if(slot == SLOT_PARTICLE && recipe.particle.matchesRecipe(stack, true)) return true;
                if(slot == SLOT_INGREDIENT && recipe.ingredient.matchesRecipe(stack, true)) return true;
            }
        }

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == SLOT_PARTICLE_CONTAINER || slot == SLOT_OUTPUT;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESS;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.isOn);
        buf.writeInt(this.progress);
        buf.writeInt(this.processTime);
        buf.writeInt(this.consumption);
        buf.writeLong(this.power);
        buf.writeByte((byte) this.savedParticles);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.isOn = buf.readBoolean();
        this.progress = buf.readInt();
        this.processTime = buf.readInt();
        this.consumption = buf.readInt();
        this.power = buf.readLong();
        this.savedParticles = buf.readByte();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("progress");
        this.power = tag.getLong("power");
        this.savedParticles = tag.getInt("savedParticles");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", this.progress);
        tag.putLong("power", this.power);
        tag.putInt("savedParticles", this.savedParticles);
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 8, p.getY(), p.getZ() - 8, p.getX() + 9, p.getY() + 5, p.getZ() + 9);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineExposureChamberMenu(id, inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_EXPOSURE_CHAMBER.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 50) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (100 - 100 / (lvl + 1)) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_DELAY, "+" + (lvl * 50) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.OVERDRIVE) {
            components.add(Component.literal("YES").withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }
}
