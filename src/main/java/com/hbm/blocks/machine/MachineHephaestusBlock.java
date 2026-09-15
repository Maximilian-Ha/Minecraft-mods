package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineHephaestusBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.InventoryUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
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
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineHephaestus.
 *
 * Ein Turm von zwoelf Bloecken Hoehe auf einer Grundflaeche von drei mal drei. Anschluesse gibt
 * es unten und ganz oben, je vier.
 *
 * ER HAT KEINE OBERFLAECHE. Einzustellen ist nur der Stoff, und das macht die Fluidkennung in
 * der Hand; alles andere liest man aus der Anzeige ab, die beim Hinsehen erscheint.
 */
public class MachineHephaestusBlock extends DummyableBlock implements ILookOverlay {

    public static final MapCodec<MachineHephaestusBlock> CODEC = simpleCodec(MachineHephaestusBlock::new);

    public MachineHephaestusBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineHephaestusBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineHephaestusBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 11, 0, 1, 1, 1, 1 }; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        /* Die vier Kantenmitten des Dreiergevierts, unten und ganz oben. Genau dorthin zeigen
         * die acht Anschlusspunkte der Maschine.
         *
         * BERICHTIGT GEGENUEBER DEM ORIGINAL: dort werden die Anschluesse um den Punkt
         * "Setzstelle minus Blickrichtung" gesetzt, und der liegt ZWEI Bloecke hinter dem Kern
         * und damit ausserhalb des Bauwerks. makeExtra tut dort nichts, weil da kein Dummy
         * steht; von den acht Anschluessen des Originals arbeiten deshalb nur zwei. */
        BlockPos center = pos.relative(dir, offset);

        for(int y : new int[] { 0, 11 }) {
            this.makeExtra(level, center.offset(1, y, 0));
            this.makeExtra(level, center.offset(-1, y, 0));
            this.makeExtra(level, center.offset(0, y, 1));
            this.makeExtra(level, center.offset(0, y, -1));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return InteractionResult.FAIL;

        if(!(level.getBlockEntity(corePos) instanceof MachineHephaestusBlockEntity heatex)) return InteractionResult.FAIL;

        for(ItemStack stack : InventoryUtil.getItemsFromBothHands(player)) {

            if(stack.getItem() instanceof IItemFluidIdentifier identifier) {

                FluidType type = identifier.getType(level, corePos, stack);

                heatex.input.setTankType(type);
                heatex.setChanged();

                player.displayClientMessage(Component.translatable("block.hbmsntm.machine_hephaestus.changed_type_to", type.getName()).withStyle(ChatFormatting.YELLOW), false);

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof MachineHephaestusBlockEntity heatex)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal(String.format(Locale.US, "%,d", heatex.bufferedHeat) + " TU"));

        FluidTank[] tanks = heatex.getAllTanks();

        for(int i = 0; i < tanks.length; i++) {
            FluidTank tank = tanks[i];
            text.add(Component.literal(i == 0 ? "-> " : "<- ").withStyle(i == 0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(tank.getTankType().getName())
                    .append(": " + tank.getFill() + "/" + tank.getMaxFill() + "mB").withStyle(ChatFormatting.RESET));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
