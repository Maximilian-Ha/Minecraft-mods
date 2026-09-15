package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineStrandCasterBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.MoldItem;
import com.hbm.items.machine.MoldItem.Mold;
import com.hbm.items.machine.ScrapsItem;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineStrandCaster.
 *
 * Sieben Bloecke lang, zwei breit, an einem Ende drei hoch. Wie bei Giessform und Giessbecken
 * ist der BLOCK der Abnehmer des Giessstrahls und nicht sein BlockEntity; er sucht den Kern und
 * reicht alles dorthin weiter.
 *
 * GEGOSSEN WIRD NUR AUF DIE VIER OBEREN FELDER, und der Block prueft das doppelt: er laesst nur
 * Stellen zu, an denen ein Anschlussblock mit Metalldurchlass sitzt, und der Kern prueft danach
 * noch einmal die Koordinaten. Ohne die erste Pruefung koennte man den Strang irgendwo auf
 * seiner Laenge fuellen.
 *
 * DIE BEDIENUNG IST DIE DER GIESSEREI: eine Form in der Hand legt die Form ein, eine Schaufel
 * schoepft erstarrtes Metall als Schrott heraus, alles andere oeffnet die Oberflaeche.
 *
 * NICHT UEBERNOMMEN: das Modell strand_caster.obj; hier stehen Feuerfestziegel-Kaesten.
 */
public class MachineStrandCasterBlock extends DummyableBlock implements ICrucibleAcceptor, ILookOverlay, IToolable {

    public static final MapCodec<MachineStrandCasterBlock> CODEC = simpleCodec(MachineStrandCasterBlock::new);

    /** Der Aufbau ueber dem hinteren Ende, zusaetzlich zur Grundflaeche. */
    private static final int[] TOWER = { 2, 0, 1, 0, 1, 0 };

    public MachineStrandCasterBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineStrandCasterBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineStrandCasterBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid().moltenMetal();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 0, 0, 6, 0, 1, 0 }; }
    @Override public int getOffset() { return 0; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos center = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        MultiblockHandlerXR.fillSpace(level, center, TOWER, this, dir);

        /* Rohranschluesse an beiden Enden ... */
        this.makeExtra(level, center.offset(rot.getStepX() - dir.getStepX(), 0, rot.getStepZ() - dir.getStepZ()));
        this.makeExtra(level, center.offset(-dir.getStepX(), 0, -dir.getStepZ()));
        this.makeExtra(level, center.offset(-dir.getStepX() * 5, 0, -dir.getStepZ() * 5));
        this.makeExtra(level, center.offset(rot.getStepX() - dir.getStepX() * 5, 0, rot.getStepZ() - dir.getStepZ() * 5));

        /* ... und die vier Giesspunkte oben. */
        this.makeExtra(level, center.offset(rot.getStepX() - dir.getStepX(), 2, rot.getStepZ() - dir.getStepZ()));
        this.makeExtra(level, center.offset(-dir.getStepX(), 2, -dir.getStepZ()));
        this.makeExtra(level, center.offset(rot.getStepX(), 2, rot.getStepZ()));
        this.makeExtra(level, center.offset(0, 2, 0));
    }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), this.getDimensions(), pos, dir)) return false;

        return MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), TOWER, pos, dir);
    }

    /**
     * Der Kern hinter einem Giesspunkt -- oder null, wenn hier gar nicht gegossen werden darf.
     * Die Pruefung auf moltenMetal ist die erste der beiden: nur Anschlussbloecke mit
     * Metalldurchlass zaehlen, und das sind genau die vier oben.
     */
    private MachineStrandCasterBlockEntity pourTarget(Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof ProxyComboBlockEntity proxy) || !proxy.moltenMetal) return null;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return null;

        return level.getBlockEntity(corePos) instanceof MachineStrandCasterBlockEntity caster ? caster : null;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        MachineStrandCasterBlockEntity caster = this.pourTarget(level, pos);
        return caster != null && caster.canAcceptPartialPour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        MachineStrandCasterBlockEntity caster = this.pourTarget(level, pos);
        return caster == null ? stack : caster.pour(level, pos, dX, dY, dZ, side, stack);
    }

    /** Fliessen laesst er nicht -- gegossen wird von oben, und nur dort. */
    @Override public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return stack; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!(level.getBlockEntity(corePos) instanceof MachineStrandCasterBlockEntity caster)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(stack.getItem() instanceof MoldItem && caster.getItem(MachineStrandCasterBlockEntity.SLOT_MOLD).isEmpty()) {

            if(!level.isClientSide) {
                caster.setItem(MachineStrandCasterBlockEntity.SLOT_MOLD, stack.copyWithCount(1));
                stack.shrink(1);
                level.playSound(null, pos, NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if(stack.is(ItemTags.SHOVELS)) {

            if(!level.isClientSide && caster.amount > 0 && caster.type != null) {

                ItemStack scrap = ScrapsItem.create(new MaterialStack(caster.type, caster.amount));

                if(!player.getInventory().add(scrap)) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, scrap);
                } else {
                    player.inventoryMenu.broadcastChanges();
                }

                caster.amount = 0;
                caster.type = null;
                caster.setChanged();
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Der Schraubenzieher holt die Form zurueck, solange kein Metall mehr darin steht. */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return false;
        if(!(level.getBlockEntity(corePos) instanceof MachineStrandCasterBlockEntity caster)) return false;

        ItemStack mold = caster.getItem(MachineStrandCasterBlockEntity.SLOT_MOLD);
        if(mold.isEmpty() || caster.amount > 0) return false;

        if(!player.getInventory().add(mold.copy())) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, mold.copy());
        }

        caster.setItem(MachineStrandCasterBlockEntity.SLOT_MOLD, ItemStack.EMPTY);
        caster.setChanged();

        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineStrandCasterBlockEntity caster) {

            if(caster.amount > 0 && caster.type != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                        ScrapsItem.create(new MaterialStack(caster.type, caster.amount)));
                caster.amount = 0;
            }

            Containers.dropContents(level, pos, caster);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof MachineStrandCasterBlockEntity caster)) return;

        List<Component> text = new ArrayList<>();
        Mold mold = caster.getInstalledMold();

        if(mold == null) {
            text.add(Component.translatable("foundry.noCast").withStyle(ChatFormatting.RED));
        } else {
            text.add(mold.getTitle().copy().withStyle(ChatFormatting.BLUE));
        }

        if(caster.type != null && caster.amount > 0) {
            text.add(caster.type.getName().append(": " + caster.amount + " / " + caster.getCapacity()).withStyle(ChatFormatting.YELLOW));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xFF4000, 0x401000, text);
    }
}
