package com.hbm.items.tools;

import com.hbm.blockentity.network.PylonBaseBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.util.TagsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemWiring.
 *
 * Verbindet zwei Strommasten: der erste Rechtsklick merkt sich den Mast, der zweite
 * spannt die Leitung. Ohne dieses Werkzeug laesst sich kein Mastennetz aufbauen.
 *
 * Die Position liegt in den Custom-Data des Stapels; geprueft wird auf den Schluessel
 * "wireX", nicht auf hasCustomData -- so stoert es nicht, wenn andere Daten am Stapel
 * haengen.
 */
public class WiringToolItem extends Item {

    private static final String KEY_X = "wireX";
    private static final String KEY_Y = "wireY";
    private static final String KEY_Z = "wireZ";

    public WiringToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null || player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        // Bei Multibloecken auf den Kern umlenken, sonst trifft man einen Huellblock.
        if(level.getBlockState(pos).getBlock() instanceof DummyableBlock dummyable) {
            BlockPos core = dummyable.findCore(level, pos);
            if(core != null) pos = core;
        }

        if(!(level.getBlockEntity(pos) instanceof PylonBaseBlockEntity second)) return InteractionResult.PASS;

        if(level.isClientSide) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = TagsUtil.getCustomData(stack);

        if(!tag.contains(KEY_X)) {
            tag.putInt(KEY_X, pos.getX());
            tag.putInt(KEY_Y, pos.getY());
            tag.putInt(KEY_Z, pos.getZ());
            TagsUtil.putCustomData(stack, tag);
            player.displayClientMessage(Component.translatable("item.hbmsntm.wiring_tool.start").withStyle(ChatFormatting.YELLOW), false);
            player.swing(context.getHand());
            return InteractionResult.CONSUME;
        }

        BlockPos firstPos = new BlockPos(tag.getInt(KEY_X), tag.getInt(KEY_Y), tag.getInt(KEY_Z));

        if(level.getBlockEntity(firstPos) instanceof PylonBaseBlockEntity first) {
            // canConnect: 0 = ok, 1 = andere Bauart, 2 = derselbe Mast, 3 = zu weit weg
            switch(PylonBaseBlockEntity.canConnect(first, second)) {
                case 0 -> {
                    first.addConnection(pos);
                    second.addConnection(firstPos);
                    player.displayClientMessage(Component.translatable("item.hbmsntm.wiring_tool.end").withStyle(ChatFormatting.GREEN), false);
                }
                case 1 -> player.displayClientMessage(Component.translatable("item.hbmsntm.wiring_tool.error.type").withStyle(ChatFormatting.RED), false);
                case 2 -> player.displayClientMessage(Component.translatable("item.hbmsntm.wiring_tool.error.same").withStyle(ChatFormatting.RED), false);
                default -> player.displayClientMessage(Component.translatable("item.hbmsntm.wiring_tool.error.distance").withStyle(ChatFormatting.RED), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("item.hbmsntm.wiring_tool.error.gone").withStyle(ChatFormatting.RED), false);
        }

        clearStart(stack, tag);
        player.swing(context.getHand());
        return InteractionResult.CONSUME;
    }

    private static void clearStart(ItemStack stack, CompoundTag tag) {
        tag.remove(KEY_X);
        tag.remove(KEY_Y);
        tag.remove(KEY_Z);
        TagsUtil.putCustomData(stack, tag);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = TagsUtil.getCustomData(stack);

        if(tag.contains(KEY_X)) {
            tooltip.add(Component.translatable("item.hbmsntm.wiring_tool.desc.pending",
                    tag.getInt(KEY_X), tag.getInt(KEY_Y), tag.getInt(KEY_Z)).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("item.hbmsntm.wiring_tool.desc").withStyle(ChatFormatting.GRAY));
        }
    }
}
