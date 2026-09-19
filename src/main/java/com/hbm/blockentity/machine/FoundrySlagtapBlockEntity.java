package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.SlagBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.SlagBlock;
import com.hbm.blocks.machine.FoundryOutletBlock;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.particle.helper.FoundryCreator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundrySlagtap.
 *
 * Der Schlackenabstich. Er sieht aus wie der Ausguss und haengt genauso am Rand einer Rinne,
 * nur nimmt er keinen Abnehmer an: was durch ihn hindurchlaeuft, faellt bis zu fuenfzehn Bloecke
 * tief und bleibt dort als Schlackenpfuetze liegen. So wird man los, was kein Rezept mehr will.
 *
 * ABWEICHUNG, der Strahl: das Original sucht mit returnLastUncollidedBlock, bekommt also auch
 * dann eine Blockstelle zurueck, wenn der Strahl ins Leere laeuft -- die letzte durchquerte.
 * Auf 1.21 tut level.clip genau das: ein Fehlschlag traegt die Stelle am Strahlende. Deshalb
 * wird hier NICHT auf HitResult.Type.BLOCK geprueft, sondern beides benutzt.
 *
 * ABWEICHUNG, Umriss statt Kollision: wie beim Giessen fragt der Strahl den Umriss ab. Auf
 * 1.7.10 sind Blockgrenzen und Kollisionskoerper dasselbe, auf 1.21 nicht -- mit dem
 * Kollisionskoerper fiele die Schlacke durch hohle Becken und Formen hindurch.
 */
public class FoundrySlagtapBlockEntity extends FoundryOutletBlockEntity {

    /** Wie weit die Schlacke faellt, in Bloecken. Wert aus dem Original. */
    private static final double REICHWEITE = 15D;

    public FoundrySlagtapBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FOUNDRY_SLAGTAP.get(), pos, state);
    }

    /** Sucht die Stelle, an der die Schlacke aufkommt. Das kann auch blosse Luft sein. */
    private BlockHitResult strahl(Level level, BlockPos pos) {

        Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() - 0.125, pos.getZ() + 0.5);
        Vec3 ende = new Vec3(pos.getX() + 0.5, pos.getY() + 0.125 - REICHWEITE, pos.getZ() + 0.5);

        return level.clip(new ClipContext(start, ende, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {

        if(this.filter != null && (this.filter != stack.material ^ this.invertFilter)) return false;
        if(this.isClosed()) return false;
        if(side != this.getBlockState().getValue(FoundryOutletBlock.FACING).getOpposite()) return false;

        return this.strahl(level, pos) != null;
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {

        if(stack == null || stack.material == null || stack.amount <= 0) return null;

        BlockHitResult treffer = this.strahl(level, pos);
        if(treffer == null) return stack;

        BlockPos ziel = treffer.getBlockPos();
        BlockPos darueber = ziel.above();

        boolean geflossen = false;

        if(level.getBlockState(ziel).getBlock() == NtmBlocks.SLAG.get()) {

            // Eine Pfuetze desselben Materials wird aufgefuellt; eine fremde bleibt unberuehrt.
            if(level.getBlockEntity(ziel) instanceof SlagBlockEntity tile && tile.mat == stack.material) {
                int transfer = Math.min(SlagBlockEntity.MAX_AMOUNT - tile.amount, stack.amount);
                tile.amount += transfer;
                stack.amount -= transfer;
                geflossen = transfer > 0;
                SlagBlock.aktualisiere(level, ziel, tile);
                level.scheduleTick(ziel, NtmBlocks.SLAG.get(), 1);
            }

        } else if(level.getBlockState(ziel).canBeReplaced()) {

            int transfer = Math.min(SlagBlockEntity.MAX_AMOUNT, stack.amount);
            SlagBlock.setze(level, ziel, stack.material, transfer);
            stack.amount -= transfer;
            geflossen = transfer > 0;
        }

        // Was nicht mehr hineinpasst, stapelt sich einen Block hoeher.
        if(stack.amount > 0 && level.getBlockState(darueber).canBeReplaced()) {
            int transfer = Math.min(SlagBlockEntity.MAX_AMOUNT, stack.amount);
            SlagBlock.setze(level, darueber, stack.material, transfer);
            stack.amount -= transfer;
            geflossen = true;
        }

        if(geflossen) {
            Direction dir = side.getOpposite();

            FoundryCreator.composeEffect(level,
                    pos.getX() + 0.5D - dir.getStepX() * 0.125, pos.getY() + 0.125, pos.getZ() + 0.5D - dir.getStepZ() * 0.125,
                    stack.material.moltenColor, dir,
                    Math.max(1F, pos.getY() - ziel.getY()), 0F, 0.375F);
        }

        return stack.amount <= 0 ? null : stack;
    }
}
