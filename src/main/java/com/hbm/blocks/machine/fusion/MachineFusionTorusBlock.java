package com.hbm.blocks.machine.fusion;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.fusion.FusionTorusBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
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

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.fusion.MachineFusionTorus.
 *
 * Fuenfzehn mal fuenfzehn Bloecke Grundflaeche, fuenf hoch, und kein Quader: der Torus ist ein
 * Achteck mit einem Loch in der Mitte. Der Grundriss steht deshalb als Maske da, drei Schichten
 * fuer fuenf Ebenen -- die oberen beiden spiegeln die unteren.
 *
 * 0 = bleibt frei, 1..3 = wird belegt. Welche Zahl genau, ist im Original nur eine Notiz fuer den
 * Zeichner; fuer den Bau zaehlt nur "groesser als null".
 */
public class MachineFusionTorusBlock extends DummyableBlock implements ITooltipProvider {

    public static final int[][][] LAYOUT = new int[][][] {

            new int[][] {
                    {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
                    {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0}
            },
            new int[][] {
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0}
            },
            new int[][] {
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0}
            }
    };

    public MachineFusionTorusBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineFusionTorusBlock> CODEC = simpleCodec(MachineFusionTorusBlock::new);
    @Override public MapCodec<MachineFusionTorusBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new FusionTorusBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override public int[] getDimensions() { return new int[] { 4, 0, 7, 7, 7, 7 }; }
    @Override public int getOffset() { return 7; }

    /** Der Grundriss der Ebene: die oberen beiden spiegeln die unteren. */
    private static int[][] layerFor(int y) {
        return LAYOUT[y > 2 ? 4 - y : y];
    }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        BlockPos core = pos.relative(dir, offset);

        for(int iy = 0; iy < 5; iy++) {

            int[][] layer = layerFor(iy);

            for(int ix = 0; ix < layer.length; ix++) {
                for(int iz = 0; iz < layer[ix].length; iz++) {

                    if(layer[ix][iz] <= 0) continue;

                    BlockPos target = core.offset(ix - layer.length / 2, iy, iz - layer[ix].length / 2);
                    if(!level.getBlockState(target).canBeReplaced()) return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        BlockPos core = pos.relative(dir, offset);

        for(int iy = 0; iy < 5; iy++) {

            int[][] layer = layerFor(iy);

            for(int ix = 0; ix < layer.length; ix++) {
                for(int iz = 0; iz < layer[ix].length; iz++) {

                    if(layer[ix][iz] <= 0) continue;

                    int ex = ix - layer.length / 2;
                    int ez = iz - layer[ix].length / 2;

                    /* Der Kern selbst bleibt, wie er ist -- er steht schon. */
                    if(iy == 0 && ex == 0 && ez == 0) continue;

                    /*
                     * Die Blickrichtung des Stellvertreters zeigt zum Kern zurueck, damit der
                     * Kernsucher ihn findet: nach unten, sobald man eine Ebene hoeher ist, sonst
                     * waagerecht zur Mitte.
                     */
                    Direction facing;
                    if(iy > 0) facing = Direction.UP;
                    else if(ex < 0) facing = Direction.WEST;
                    else if(ex > 0) facing = Direction.EAST;
                    else if(ez < 0) facing = Direction.NORTH;
                    else facing = Direction.SOUTH;

                    level.setBlock(core.offset(ex, iy, ez), this.createDummyState(facing), 3);
                }
            }
        }

        /* Die Anschlussstellen: eine oben und je sechs an den vier Armen. */
        this.makeExtra(level, core.above(4));

        for(int side = 0; side < 4; side++) {

            Direction arm = Direction.from2DDataValue(side);
            Direction rot = arm.getClockWise();

            for(int off = -2; off <= 2; off += 2) {
                BlockPos base = core.offset(arm.getStepX() * 6 + rot.getStepX() * off, 0, arm.getStepZ() * 6 + rot.getStepZ() * off);
                this.makeExtra(level, base);
                this.makeExtra(level, base.above(4));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
