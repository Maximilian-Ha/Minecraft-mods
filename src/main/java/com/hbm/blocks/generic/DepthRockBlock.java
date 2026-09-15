package com.hbm.blocks.generic;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.IDepthRockTool;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockDepth.
 *
 * Tiefengestein. Es umgibt jedes Grundgesteinserz und ist mit gewoehnlichem Werkzeug nicht zu
 * brechen -- die Haerte steht auf unzerstoerbar. Nur Werkzeug, das sich als IDepthRockTool
 * ausweist, kommt hindurch, und auch das langsam.
 *
 * ES IST DIE MAUER UM DIE ERZE. Ohne sie koennte man das Grundgesteinserz einfach freilegen und
 * der Bagger waere ueberfluessig.
 */
public class DepthRockBlock extends Block implements ITooltipProvider {

    public static final MapCodec<DepthRockBlock> CODEC = simpleCodec(DepthRockBlock::new);

    public DepthRockBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<DepthRockBlock> codec() { return CODEC; }

    /**
     * Unzerstoerbar (Haerte -1), ausser fuer das richtige Werkzeug. Der Wert 1/50 ist der des
     * Originals: auch das richtige Werkzeug braucht fuenfzig Schlaege.
     */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {

        ItemStack held = player.getMainHandItem();

        if(held.getItem() instanceof IDepthRockTool tool && tool.canBreakRock(level, player, held, state, pos)) {
            return 1F / 50F;
        }

        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("trait.tile.depth"));
        this.addStandardInfo(components);
    }
}
