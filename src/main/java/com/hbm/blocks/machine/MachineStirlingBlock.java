package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineStirlingBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

public class MachineStirlingBlock extends DummyableBlock implements ILookOverlay, ITooltipProvider {

    public MachineStirlingBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineStirlingBlock> CODEC = simpleCodec(MachineStirlingBlock::new);
    @Override public MapCodec<MachineStirlingBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineStirlingBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {1, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        // Nur die vier direkt angrenzenden Dummys werden zu Stromanschluessen, die Diagonalen nicht
        BlockPos core = pos.relative(dir, offset);

        this.makeExtra(level, core.east());
        this.makeExtra(level, core.west());
        this.makeExtra(level, core.south());
        this.makeExtra(level, core.north());
    }

    /**
     * Wiedereinsetzen des Zahnrads, aus MachineStirling.onBlockActivated portiert.
     * Das Original kennt nur diesen einen Fall: passender Untertyp in der Hand,
     * Maschine ohne Zahnrad, nicht geduckt. Sonst passiert nichts.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!(level.getBlockEntity(corePos) instanceof MachineStirlingBlockEntity stirling)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int meta = stirling.getGearMeta();

        if(!stirling.hasCog && !stack.isEmpty() && stack.is(NtmItems.GEAR_LARGE.get()) && MetaHelper.getMeta(stack) == meta) {
            stack.shrink(1);
            stirling.hasCog = true;
            stirling.setChanged();
            // Ton am angeklickten Block, nicht am Kern -- wie im Original
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.5F, 0.75F);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {
        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        BlockEntity te = level.getBlockEntity(corePos);
        if(!(te instanceof MachineStirlingBlockEntity stirling)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal(stirling.heat + "TU/t"));
        text.add(Component.literal((stirling.hasCog ? stirling.powerBuffer : 0) + "HE/t"));

        int maxHeat = stirling.maxHeat();
        double percent = (double) stirling.heat / (double) maxHeat;
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

        if(percent > 1D) color = 0xff0000;

        text.add(Component.literal(((stirling.heat * 1000 / maxHeat) / 10D) + "%").withColor(color));

        if(stirling.heat > maxHeat) {
            text.add(Component.literal("! ! ! OVERSPEED ! ! !").withColor(BobMathUtil.getBlink() ? 0xff0000 : 0xffff00));
        }

        if(!stirling.hasCog) {
            text.add(Component.literal("Gear missing!").withColor(0xff0000));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
