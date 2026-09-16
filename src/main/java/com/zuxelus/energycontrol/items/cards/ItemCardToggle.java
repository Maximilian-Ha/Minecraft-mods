package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.ITouchAction;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardToggle.
 *
 * Zeigt den Zustand eines Schalters und schaltet ihn um, wenn man den Schirm anfasst.
 *
 * Umgeschaltet wird ueber {@code useWithoutItem} des Blocks, also genau so, als haette der
 * Spieler danebengestanden und geklickt: das Klacken, die Nachbarschaftsmeldungen und die
 * Rueckstellung eines Knopfes macht dann der Block selbst. Erlaubt sind nur Hebel, Knoepfe,
 * Tueren, Falltueren und Zauntore -- bei einer Kiste wuerde derselbe Aufruf eine Oberflaeche
 * oeffnen, und das ist nicht gemeint.
 */
public class ItemCardToggle extends ItemCardBase implements ITouchAction {

    public ItemCardToggle(Properties properties) {
        super(properties);
    }

    /** Ob dieser Block sich mit einer Umschaltkarte bedienen laesst. */
    public static boolean isToggleable(BlockState state) {
        return state.getBlock() instanceof LeverBlock
                || state.getBlock() instanceof ButtonBlock
                || state.getBlock() instanceof DoorBlock
                || state.getBlock() instanceof TrapDoorBlock
                || state.getBlock() instanceof FenceGateBlock;
    }

    /** Ob der Schalter gerade "an" ist -- Hebel und Knopf ueber POWERED, alles andere ueber OPEN. */
    private static boolean isOn(BlockState state) {
        if(state.hasProperty(BlockStateProperties.POWERED)) return state.getValue(BlockStateProperties.POWERED);
        if(state.hasProperty(BlockStateProperties.OPEN)) return state.getValue(BlockStateProperties.OPEN);
        return false;
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;
        if(!level.isLoaded(target)) return CardState.NO_TARGET;

        BlockState state = level.getBlockState(target);
        if(!isToggleable(state)) return CardState.NO_TARGET;

        reader.reset();
        reader.setString("block", state.getBlock().getName().getString());
        reader.setBoolean(DataHelper.ACTIVE, isOn(state));
        return CardState.OK;
    }

    @Override
    public boolean enableTouch(ItemStack stack) {
        return true;
    }

    @Override
    public boolean runTouchAction(Level level, Player player, ICardReader reader, ItemStack stack) {
        BlockPos target = reader.getTarget();
        if(target == null || !level.isLoaded(target)) return false;

        BlockState state = level.getBlockState(target);
        if(!isToggleable(state)) return false;

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
        return state.useWithoutItem(level, player, hit).consumesAction();
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();
        if((settings & 1) > 0 && reader.hasField("block"))
            result.add(PanelString.of("msg.ec.InfoPanelBlock", reader.getString("block"), showLabels));
        if(reader.hasField(DataHelper.ACTIVE)) addOnOff(result, reader.getBoolean(DataHelper.ACTIVE));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(1);
        result.add(new PanelSetting("msg.ec.cbInfoPanelBlock", 1));
        return result;
    }
}
