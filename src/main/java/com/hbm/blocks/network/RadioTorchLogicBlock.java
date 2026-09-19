package com.hbm.blocks.network;

import com.hbm.blockentity.network.RadioTorchLogicBlockEntity;
import com.hbm.inventory.screens.RadioTorchLogicScreen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.RadioTorchLogic.
 *
 * Wie der Empfaenger eine Redstone-Quelle, nur dass die Staerke nicht aus der Nachricht
 * selbst kommt, sondern aus der Nummer der ersten Bedingung, die auf sie zutrifft.
 *
 * ABWEICHUNG: das Original hat zwei Bilder, eines fuer an und eines fuer aus. Der Block des
 * Ports fuehrt nur FACING und keinen Leuchtzustand, deshalb steht -- wie bei Sender und
 * Empfaenger -- nur das Bild fuer "aus" im Baum.
 */
public class RadioTorchLogicBlock extends RadioTorchRWBaseBlock {

    public RadioTorchLogicBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioTorchLogicBlockEntity(pos, state);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if(level.getBlockEntity(pos) instanceof RadioTorchLogicBlockEntity logik) return logik.lastState;
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object provideScreen(Player player, BlockPos pos) {
        if(player.level().getBlockEntity(pos) instanceof RadioTorchLogicBlockEntity logik) return new RadioTorchLogicScreen(logik);
        return null;
    }
}
