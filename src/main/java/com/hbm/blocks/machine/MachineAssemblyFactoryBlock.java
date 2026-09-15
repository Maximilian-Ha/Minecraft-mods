package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyDynBlockEntity;
import com.hbm.blockentity.machine.MachineAssemblyFactoryBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineAssemblyFactory.
 *
 * Gleiche Abmessungen und gleiche Anschlussordnung wie die Chemiefabrik aus Runde 113.
 *
 * Fuenf mal fuenf Bloecke, drei hoch. Die Anschlussbloecke sind DYNAMISCH: an den vier
 * Kuehlstellen sieht ein Rohr nur Wasser und Abdampf, ueberall sonst die
 * Rezeptfluessigkeiten. Deshalb steht hier der dynamische Stellvertreter und nicht der
 * gewoehnliche.
 *
 * DIE ANZEIGE SAGT, WORAN MAN STEHT. An einem Kuehlanschluss nennt sie die beiden Kuehlstoffe,
 * an einem Gueteranschluss die Nummer des Rezeptfeldes, das dort bedient wird -- ohne das
 * waeren vier gleich aussehende Anschluesse nicht auseinanderzuhalten.
 */
public class MachineAssemblyFactoryBlock extends DummyableBlock implements ITooltipProvider, ILookOverlay {

    public static final MapCodec<MachineAssemblyFactoryBlock> CODEC = simpleCodec(MachineAssemblyFactoryBlock::new);

    public MachineAssemblyFactoryBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineAssemblyFactoryBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineAssemblyFactoryBlockEntity(pos, state);
            case EXTRA -> new ProxyDynBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 2, 2, 2, 2 }; }
    @Override public int getOffset() { return 2; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos center = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        /* Der ganze Rand des Fuenfergevierts ist Anschluss. */
        for(int i = -2; i <= 2; i++) {
            for(int j = -2; j <= 2; j++) {
                if(Math.abs(i) == 2 || Math.abs(j) == 2) this.makeExtra(level, center.offset(i, 0, j));
            }
        }

        /* Dazu die beiden oberen Laengsreihen. */
        for(int i = -2; i <= 2; i++) {
            this.makeExtra(level, center.offset(dir.getStepX() * i + rot.getStepX() * 2, 2, dir.getStepZ() * i + rot.getStepZ() * 2));
            this.makeExtra(level, center.offset(dir.getStepX() * i - rot.getStepX() * 2, 2, dir.getStepZ() * i - rot.getStepZ() * 2));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof MachineAssemblyFactoryBlockEntity factory)) return;

        for(DirPos cool : factory.getCoolPos()) {

            if(!cool.compare(pos.getX() + cool.getStepX(), pos.getY(), pos.getZ() + cool.getStepZ())) continue;

            List<Component> text = new ArrayList<>();
            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(factory.water.getTankType().getName()).withStyle(ChatFormatting.RESET));
            text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(factory.lps.getTankType().getName()).withStyle(ChatFormatting.RESET));

            ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
            return;
        }

        DirPos[] io = factory.getIOPos();

        for(int i = 0; i < io.length; i++) {

            if(!io[i].compare(pos.getX() + io[i].getStepX(), pos.getY(), pos.getZ() + io[i].getStepZ())) continue;

            List<Component> text = new ArrayList<>();
            text.add(Component.literal("-> ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable("assemblyFactory.field", i + 1)).withStyle(ChatFormatting.RESET));

            ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
            return;
        }
    }
}
