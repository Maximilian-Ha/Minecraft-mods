package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineSuperComputerBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.mojang.serialization.MapCodec;
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

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineSuperComputer.
 *
 * DAS GROESSTE BAUWERK, DAS DER PORT BISHER KENNT: sechzehn Bloecke lang, sieben breit, und in
 * der Mitte reicht es sieben Bloecke nach oben und ebenso weit nach unten.
 *
 * ES BESTEHT AUS FUENF KOERPERN, nicht aus einem. Das ist der Grund, warum diese Klasse
 * checkRequirement und fillSpace selbst schreibt: die Wurzelklasse kennt nur eine einzige
 * Abmessung. Die fuenf sind
 *
 *   1. der Hauptkoerper, fuenf hoch und drei breit nach jeder Seite,
 *   2. und 3. zwei Kreuzarme, die sechs nach oben und sechs nach unten gehen,
 *   4. der Schacht in der Mitte, sieben nach oben und sieben nach unten,
 *   5. der Gang nach vorne, von drei hinter bis acht vor dem Kern.
 *
 * DER KERN LIEGT GANZ HINTEN, acht Bloecke hinter dem Block, den man setzt -- daher der Versatz
 * von acht. Die Bedienung sitzt vorne am Gang, die Anschluesse hinten am Kern.
 */
public class MachineSuperComputerBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineSuperComputerBlock> CODEC = simpleCodec(MachineSuperComputerBlock::new);

    /** Die fuenf Koerper, in der Reihenfolge des Originals. */
    private static final int[][] ALL_DIMENSIONS = {
            { 5, 0, 3, 3, 3, 3 },
            { 6, -6, 3, 3, 1, 1 },
            { 6, -6, 1, 1, 3, 3 },
            { 7, -7, 1, 1, 1, 1 },
            { 2, 0, -3, 8, 1, 1 }
    };

    public MachineSuperComputerBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineSuperComputerBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineSuperComputerBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return ALL_DIMENSIONS[0]; }
    @Override public int getOffset() { return 8; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        BlockPos core = pos.relative(dir, offset);
        for(int[] dim : ALL_DIMENSIONS) if(!MultiblockHandlerXR.checkSpace(level, core, dim, pos, dir)) return false;
        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : ALL_DIMENSIONS) MultiblockHandlerXR.fillSpace(level, core, dim, this, dir);

        Direction rot = dir.getClockWise(Axis.Y);

        /* Die fuenf Anschlussbloecke am hinteren Ende; einer davon ist zugleich der Block, den
         * der Spieler gesetzt hat. */
        this.makeExtra(level, core.relative(dir, 8));
        this.makeExtra(level, core.relative(dir, 7).relative(rot));
        this.makeExtra(level, core.relative(dir, 7).relative(rot.getOpposite()));
        this.makeExtra(level, core.relative(dir, 5).relative(rot));
        this.makeExtra(level, core.relative(dir, 5).relative(rot.getOpposite()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
