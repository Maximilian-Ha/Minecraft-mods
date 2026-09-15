package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.NeutronNodeWorld.StreamWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.util.Vec3NT;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKRodReaSim.
 *
 * Der ReaSim-Brennkanal. Er unterscheidet sich vom gewoehnlichen nur darin, wie er den Fluss
 * verteilt: statt in die vier Himmelsrichtungen schickt er acht Stroeme im Kreis, jeden mit drei
 * Vierteln der Menge, und dreht den ganzen Faecher bei jedem Mal ein Stueck weiter.
 */
public class RBMKRodReaSimBlockEntity extends RBMKRodBlockEntity {

    public RBMKRodReaSimBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_ROD_REASIM.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkReaSim");
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.FUEL_SIM;
    }

    @Override
    public void spreadFlux(double flux, double ratio) {

        if(flux == 0) {
            NeutronNodeWorld.removeNode(this.level, this.worldPosition);
            return;
        }

        StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(this.level);
        RBMKNeutronNode node = (RBMKNeutronNode) streamWorld.getNode(this.worldPosition);

        if(node == null) {
            node = RBMKNeutronHandler.makeNode(streamWorld, this);
            streamWorld.addNode(node);
        }

        Vec3NT vec = new Vec3NT(1, 0, 0);
        vec.rotateAroundYDeg(this.level.random.nextInt(4) * 9D);

        for(int i = 0; i < 8; i++) {
            new RBMKNeutronHandler.RBMKNeutronStream(node, new net.minecraft.world.phys.Vec3(vec.xCoord, vec.yCoord, vec.zCoord), flux * 0.75, ratio);
            vec.rotateAroundYDeg(45D);
        }
    }
}
