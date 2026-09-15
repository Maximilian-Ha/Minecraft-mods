package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineMiningLaserBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineMiningLaser.
 *
 * Drei mal drei mal drei, und der Hoehenversatz ist der Kniff: gesetzt wird AN DIE DECKE, der
 * Kern sitzt einen Block darunter. Der Laser braucht Luft unter sich, nicht ueber sich.
 *
 * FUENF ANSCHLUSSBLOECKE: die vier Seiten fuer Gueter und Oel, der obere fuer Strom. An jedem
 * von ihnen haelt ein Rotsteinsignal die Maschine an.
 *
 * ABWEICHUNG: das Original gibt dem oberen Anschluss NUR Strom und den vier seitlichen NUR
 * Gueter und Fluid. Hier koennen alle fuenf alles. Am Blockzustand laesst sich nicht ablesen,
 * welcher der obere ist, und der Unterschied waere klein: der Kern holt seinen Strom ohnehin
 * nur von oben, und Gueter nimmt er an allen vier Seiten ab.
 */
public class MachineMiningLaserBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineMiningLaserBlock> CODEC = simpleCodec(MachineMiningLaserBlock::new);

    public MachineMiningLaserBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineMiningLaserBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineMiningLaserBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 1, 1, 1, 1, 1, 1 }; }
    @Override public int getOffset() { return 0; }
    @Override public int getHeightOffset() { return -1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        this.makeExtra(level, core.east());
        this.makeExtra(level, core.west());
        this.makeExtra(level, core.south());
        this.makeExtra(level, core.north());
        this.makeExtra(level, core.above());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("desc.miningLaser.multiblock"));
        components.add(Component.translatable("desc.miningLaser.ceiling"));
        this.addStandardInfo(components);
    }
}
