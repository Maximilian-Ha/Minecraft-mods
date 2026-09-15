package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineSatLinkBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ISatChip;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineSatLink.
 *
 * Ein Mast von sieben Bloecken Hoehe auf einer Grundflaeche von zwei mal zwei. Der Kern steht
 * unten vorne links; die drei uebrigen Bodenbloecke sind Anschluesse.
 *
 * EINGESTELLT WIRD MIT DEM SATELLITENCHIP: wer mit einem in der Hand daraufklickt, uebertraegt
 * dessen Frequenz auf die Station. Eine Oberflaeche gibt es nicht -- gelesen wird beim Hinsehen.
 */
public class MachineSatLinkBlock extends DummyableBlock implements ILookOverlay, ITooltipProvider {

    public static final MapCodec<MachineSatLinkBlock> CODEC = simpleCodec(MachineSatLinkBlock::new);

    public MachineSatLinkBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineSatLinkBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineSatLinkBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state);
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 6, 0, 1, 0, 1, 0 }; }
    @Override public int getOffset() { return 0; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        this.makeExtra(level, core.relative(dir.getOpposite()));
        this.makeExtra(level, core.relative(rot));
        this.makeExtra(level, core.relative(dir.getOpposite()).relative(rot));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(stack.getItem() instanceof ISatChip)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return ItemInteractionResult.FAIL;
        if(!(level.getBlockEntity(corePos) instanceof MachineSatLinkBlockEntity link)) return ItemInteractionResult.FAIL;

        link.freq = ISatChip.getFreqS(stack);
        link.setChanged();

        player.displayClientMessage(Component.literal("Set frequency to " + link.freq).withStyle(ChatFormatting.YELLOW), false);
        level.playSound(null, corePos, NtmSoundEvents.TECH_BLEEP.get(), SoundSource.BLOCKS, 1F, 1F);

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof MachineSatLinkBlockEntity link)) return;

        List<Component> text = new ArrayList<>();

        text.add(Component.translatable("tile.machine_satlink.freq").append(Component.literal(": " + link.freq)));
        text.add(Component.translatable("tile.machine_satlink.connected").append(Component.literal(": "))
                .append(Component.translatable(link.connected ? "tile.machine_satlink.yes" : "tile.machine_satlink.no")
                        .withStyle(link.connected ? ChatFormatting.GREEN : ChatFormatting.RED)));

        text.addAll(link.info);

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
