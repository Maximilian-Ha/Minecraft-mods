package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineIndustrialTurbineBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.BobMathUtil;
import com.hbm.util.SoundUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Pre;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineIndustrialTurbine.
 *
 * Fuenf Bloecke lang, drei hoch, drei breit. Der Hebel sitzt vorn oben; ein Klick darauf schaltet
 * den Verdichter eine Dampfstufe weiter, solange die Turbine steht.
 *
 * NICHT UEBERNOMMEN: die Viertelbloecke, mit denen das Original die Drehzahl als kleines
 * Laufbild in die Anzeige schreibt. Der Port nennt die Drehzahl in Prozent -- dieselbe Auskunft
 * ohne den Trick mit den Sonderzeichen.
 */
public class MachineIndustrialTurbineBlock extends DummyableBlock implements ITooltipProvider, ILookOverlay {

    public static final MapCodec<MachineIndustrialTurbineBlock> CODEC = simpleCodec(MachineIndustrialTurbineBlock::new);

    public MachineIndustrialTurbineBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineIndustrialTurbineBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineIndustrialTurbineBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 3, 3, 1, 1 }; }
    @Override public int getOffset() { return 3; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return InteractionResult.PASS;

        if(!(level.getBlockEntity(corePos) instanceof MachineIndustrialTurbineBlockEntity be)) return InteractionResult.PASS;

        Direction dir = be.getBlockState().getValue(FACING);
        BlockPos lever = corePos.offset(dir.getStepX() * 3, 1, dir.getStepZ() * 3);

        if(!pos.equals(lever)) return InteractionResult.PASS;

        if(!level.isClientSide) {

            if(be.operational) {
                player.sendSystemMessage(Component.translatable("turbine.operational").withStyle(ChatFormatting.RED));
            } else {
                SoundUtils.playAtVec3(level, Vec3.atCenterOf(pos), NtmSoundEvents.TURBINE_LEVER.get(), SoundSource.BLOCKS, 1.5F, 1F);
                be.onLeverPull();
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos center = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        /* Vier Anschluesse an den Laengsseiten, zwei oben, einer hinten fuer den Strom. */
        this.makeExtra(level, center.offset(dir.getStepX() * 3 + rot.getStepX(), 0, dir.getStepZ() * 3 + rot.getStepZ()));
        this.makeExtra(level, center.offset(dir.getStepX() * 3 - rot.getStepX(), 0, dir.getStepZ() * 3 - rot.getStepZ()));
        this.makeExtra(level, center.offset(-dir.getStepX() + rot.getStepX(), 0, -dir.getStepZ() + rot.getStepZ()));
        this.makeExtra(level, center.offset(-dir.getStepX() - rot.getStepX(), 0, -dir.getStepZ() - rot.getStepZ()));
        this.makeExtra(level, center.offset(dir.getStepX() * 3, 2, dir.getStepZ() * 3));
        this.makeExtra(level, center.offset(-dir.getStepX(), 2, -dir.getStepZ()));
        this.makeExtra(level, center.offset(-dir.getStepX() * 3, 1, -dir.getStepZ() * 3));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        if(!(level.getBlockEntity(corePos) instanceof MachineIndustrialTurbineBlockEntity be)) return;

        FluidTank tankInput = be.tanks[0];
        FluidTank tankOutput = be.tanks[1];

        FluidType inputType = tankInput.getTankType();
        FluidType outputType = Fluids.NONE;

        if(inputType.hasTrait(FT_Coolable.class)) outputType = inputType.getTrait(FT_Coolable.class).coolsTo;

        List<Component> text = new ArrayList<>();

        text.add(Component.literal("-> ").withStyle(ChatFormatting.DARK_GREEN).append(inputType.getName())
                .append(": " + String.format(Locale.US, "%,d", tankInput.getFill()) + "/" + String.format(Locale.US, "%,d", tankInput.getMaxFill()) + "mB").withStyle(ChatFormatting.RESET));
        text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(outputType.getName())
                .append(": " + String.format(Locale.US, "%,d", tankOutput.getFill()) + "/" + String.format(Locale.US, "%,d", tankOutput.getMaxFill()) + "mB").withStyle(ChatFormatting.RESET));
        text.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(BobMathUtil.getShortNumber(be.powerBuffer) + "HE (" + Math.round(be.spin * 100) + "%)").withStyle(ChatFormatting.RESET));

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
