package com.hbm.items.tools;

import com.hbm.blockentity.machine.SolarMirrorBlockEntity;
import com.hbm.blocks.machine.MachineSolarBoilerBlock;
import com.hbm.blocks.machine.SolarMirrorBlock;
import com.hbm.util.TagsUtil;
import com.hbm.util.i18n.I18nUtil;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Spiegelwerkzeug: merkt sich per Rechtsklick den Solarkessel und richtet danach
 * angeklickte Heliostatspiegel auf ihn aus.
 */
public class MirrorToolItem extends Item {

    public MirrorToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if(player == null) return InteractionResult.PASS;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if(block instanceof MachineSolarBoilerBlock boilerBlock) {

            BlockPos corePos = boilerBlock.findCore(level, pos);

            if(corePos != null && !level.isClientSide) {

                CompoundTag tag = TagsUtil.getCustomData(stack);
                tag.putInt("posX", corePos.getX());
                tag.putInt("posY", corePos.getY() + 1);
                tag.putInt("posZ", corePos.getZ());
                TagsUtil.putCustomData(stack, tag);

                player.displayClientMessage(Component.translatable("item.hbmsntm.mirror_tool.linked").withStyle(ChatFormatting.YELLOW), false);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if(block instanceof SolarMirrorBlock && TagsUtil.hasCustomData(stack)) {

            if(!level.isClientSide) {

                BlockEntity be = level.getBlockEntity(pos);

                if(be instanceof SolarMirrorBlockEntity mirror) {

                    CompoundTag tag = TagsUtil.getCustomData(stack);
                    int tx = tag.getInt("posX");
                    int ty = tag.getInt("posY");
                    int tz = tag.getInt("posZ");

                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();

                    double dx = x - tx;
                    double dy = y - ty;
                    double dz = z - tz;

                    boolean withinReach = Math.sqrt(dx * dx + dy * dy + dz * dz) <= 100;
                    boolean withinAngle = (x - tx) * (x - tx) + (z - tz) * (z - tz) <= (y - ty) * (y - ty);

                    if(!withinReach) {
                        player.displayClientMessage(Component.translatable("item.hbmsntm.mirror_tool.reach").withStyle(ChatFormatting.RED), false);
                    } else if(!withinAngle) {
                        player.displayClientMessage(Component.translatable("item.hbmsntm.mirror_tool.angle").withStyle(ChatFormatting.RED), false);
                    } else {
                        mirror.setTarget(tx, ty, tz);
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        // Das Original trennt die Beschreibung am $; Component.translatable tut das nicht,
        // deshalb der Umweg ueber resolveKeyArray (Muster: StarterKitItem).
        for(String s : I18nUtil.resolveKeyArray("item.hbmsntm.mirror_tool.desc")) {
            components.add(Component.translatable(s).withStyle(ChatFormatting.YELLOW));
        }
    }
}
