package com.hbm.blocks.network;

import com.hbm.blockentity.network.PipeBaseBlockEntity;
import com.hbm.blockentity.network.PipeValveBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.FluidValve.
 *
 * Ein Absperrhahn im Rohrnetz. Zugedreht entsteht an dieser Stelle kein Netzknoten, das
 * Netz ist also wirklich getrennt. Im Original sind auf und zu die Metadaten 1 und 0 mit
 * zwei Texturen; hier ist es die Blockstate-Eigenschaft OPEN.
 *
 * ABWEICHUNG: das Original spielt "hbm:block.reactorStart" mit Tonhoehe 1,0 beim Aufdrehen
 * und 0,85 beim Zudrehen. Diesen Klang gibt es im Port nicht -- wie schon beim Kabelschalter
 * steht hier der Hebelklang der Mod, mit denselben beiden Tonhoehen.
 */
public class FluidValveBlock extends FluidDuctBaseBlock implements ILookOverlay, ITooltipProvider {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public static final MapCodec<FluidValveBlock> CODEC = simpleCodec(FluidValveBlock::new);

    public FluidValveBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.FALSE));
    }

    @Override public MapCodec<? extends FluidValveBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeValveBlockEntity(pos, state);
    }

    /** Der Redstonehahn erbt alles, laesst sich aber nicht von Hand umlegen. */
    protected boolean vonHandZuDrehen() { return true; }

    /**
     * Erst der Fluidkennzeichner wie bei jedem Rohr, dann der Hahn. Genau diese Reihenfolge
     * hat das Original: wer mit dem Kennzeichner klickt, stellt das Fluid ein, statt
     * versehentlich abzusperren.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        ItemInteractionResult vorher = super.useItemOn(stack, state, level, pos, player, hand, hit);
        if(vorher == ItemInteractionResult.SUCCESS) return vorher;

        if(!this.vonHandZuDrehen()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        this.drehe(state, level, pos);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {

        if(!this.vonHandZuDrehen()) return InteractionResult.PASS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        this.drehe(state, level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void drehe(BlockState state, Level level, BlockPos pos) {

        if(level.isClientSide) return;

        boolean offen = state.getValue(OPEN);
        level.setBlock(pos, state.setValue(OPEN, !offen), Block.UPDATE_CLIENTS);
        level.playSound(null, pos, NtmSoundEvents.LEVER.get(), SoundSource.BLOCKS, 1.0F, offen ? 0.85F : 1.0F);

        if(level.getBlockEntity(pos) instanceof PipeValveBlockEntity ventil) ventil.updateState();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof PipeBaseBlockEntity rohr)) return;

        List<Component> text = new ArrayList<>();
        FluidType type = rohr.getFluidType();
        text.add(Component.translatable(type.getUnlocalizedName()).withColor(type.getColor()));

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
