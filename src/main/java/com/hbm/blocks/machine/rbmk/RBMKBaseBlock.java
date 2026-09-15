package com.hbm.blocks.machine.rbmk;

import api.hbm.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.rbmk.RBMKBaseBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKDials;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.items.NtmItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKBase.
 *
 * Eine RBMK-Saeule ist ein schmaler Turm: unten der Kernblock mit der Block-Entitaet, darueber
 * so viele Fuellbloecke, wie der Dial dialColumnHeight vorgibt.
 */
public abstract class RBMKBaseBlock extends DummyableBlock implements IToolable, ILookOverlay {

    /** Der Deckel der Saeule. Liegt auf allen Bloecken der Saeule, damit die Textur durchgehend passt. */
    public static final EnumProperty<RBMKLid> LID = EnumProperty.create("lid", RBMKLid.class);

    protected RBMKBaseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, DummyBlockType.CORE)
                .setValue(LID, RBMKLid.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, LID);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isCollisionShapeFullBlock(level, pos) ? 0.2F : 1.0F;
    }

    @Override public int[] getDimensions() { return new int[] {3, 0, 0, 0, 0, 0}; }
    @Override public int getOffset() { return 0; }

    /** Die Hoehe der Saeule haengt am Dial, deshalb wird sie nicht aus getDimensions() genommen. */
    public int[] getDimensions(Level level) {
        return new int[] {RBMKDials.getColumnHeight(level), 0, 0, 0, 0, 0};
    }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        return MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), this.getDimensions(level), pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        MultiblockHandlerXR.fillSpace(level, pos.relative(dir, offset), this.getDimensions(level), this, dir);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        RBMKLid lid = null;
        if(stack.is(NtmItems.RBMK_LID.get())) lid = RBMKLid.CONCRETE;
        if(stack.is(NtmItems.RBMK_LID_GLASS.get())) lid = RBMKLid.GLASS;

        if(lid == null) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return ItemInteractionResult.FAIL;
        if(!(level.getBlockEntity(corePos) instanceof RBMKBaseBlockEntity rbmk)) return ItemInteractionResult.FAIL;

        // Saeulen mit festem Deckel, zum Beispiel Steuerstaebe, nehmen keinen an.
        if(!rbmk.isLidRemovable()) return ItemInteractionResult.FAIL;
        if(rbmk.hasLid()) return ItemInteractionResult.FAIL;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        this.setLid(level, corePos, lid);
        RBMKNeutronNode node = (RBMKNeutronNode) NeutronNodeWorld.getNode(level, corePos);
        if(node != null) node.addLid();

        if(!player.hasInfiniteMaterials()) stack.shrink(1);
        level.playSound(null, corePos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        return ItemInteractionResult.SUCCESS;
    }

    /** Der Schraubenzieher nimmt den Deckel ab und wirft ihn aus. */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return false;
        if(!(level.getBlockEntity(corePos) instanceof RBMKBaseBlockEntity rbmk)) return false;
        if(!rbmk.hasLid() || !rbmk.isLidRemovable()) return false;

        RBMKLid lid = rbmk.getLid();

        if(level.isClientSide) return true;

        RBMKNeutronNode node = (RBMKNeutronNode) NeutronNodeWorld.getNode(level, corePos);
        if(node != null) node.removeLid();

        this.setLid(level, corePos, RBMKLid.NONE);

        ItemStack drop = new ItemStack(lid == RBMKLid.GLASS ? NtmItems.RBMK_LID_GLASS.get() : NtmItems.RBMK_LID.get());
        Block.popResource(level, corePos.above(RBMKDials.getColumnHeight(level)), drop);

        return true;
    }

    /**
     * Portiert aus 1.7.10: TileEntityRBMKBase.diagnosticPrintHook.
     *
     * Zeigt beim Hinsehen die Werte der Saeule an. Das Original liest dafuer das NBT der
     * Block-Entitaet aus und listet jeden Schluessel auf ("Dump of Ordered Data Diagnostic").
     * Das ist in 1.21 unnoetig umstaendlich -- die Block-Entitaeten liefern ihre Werte hier
     * direkt, und die Saeulen mit eigenen Werten reichen sie ueber getLookInfo nach.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof RBMKBaseBlockEntity rbmk)) return;

        List<Component> text = new ArrayList<>();

        text.add(Component.literal(String.format(Locale.US, "%.1f", rbmk.heat) + " / " + (int) rbmk.maxHeat() + " C")
                .withStyle(rbmk.heat > rbmk.maxHeat() * 0.75 ? ChatFormatting.RED : ChatFormatting.YELLOW));

        rbmk.getLookInfo(text);

        ILookOverlay.printGeneric(event, this.getName(), 0xFFFF00, 0x404000, text);
    }

    /**
     * Loest eine beliebige Stelle einer RBMK-Saeule zu deren Kern-Block-Entitaet auf.
     * Liefert null, wenn dort keine Saeule steht.
     */
    public static RBMKBaseBlockEntity findColumn(Level level, BlockPos pos) {

        if(!(level.getBlockState(pos).getBlock() instanceof RBMKBaseBlock block)) return null;

        BlockPos corePos = block.findCore(level, pos);
        if(corePos == null) return null;

        return level.getBlockEntity(corePos) instanceof RBMKBaseBlockEntity rbmk ? rbmk : null;
    }

    /**
     * Wie viele Bloecke die Saeule ueber ihrem Kernblock noch weitergeht.
     *
     * Die Renderer zeichnen vom Kernblock aus nach oben und brauchen die Hoehe der konkreten
     * Saeule, nicht die eingestellte Hoechsthoehe: eine abgebaute Saeule ist kuerzer.
     */
    public static int columnHeight(Level level, BlockPos corePos, Block block) {

        int height = 1;

        for(int i = 1; i < 16; i++) {
            if(level.getBlockState(corePos.above(i)).getBlock() != block) break;
            height = i;
        }

        return height;
    }

    /** Setzt den Deckelzustand auf der ganzen Saeule, damit die Textur durchgehend bleibt. */
    private void setLid(Level level, BlockPos corePos, RBMKLid lid) {

        int height = RBMKDials.getColumnHeight(level);

        safeRem = true;
        for(int i = 0; i <= height; i++) {
            BlockPos pos = corePos.above(i);
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() != this) continue;
            level.setBlock(pos, state.setValue(LID, lid), 3);
        }
        safeRem = false;
    }
}
