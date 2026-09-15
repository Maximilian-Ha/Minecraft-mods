package com.hbm.blocks.generic;

import com.hbm.blockentity.BedrockOreBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockBedrockOreTE.
 *
 * Das Grundgesteinserz. Es liegt in der untersten Lage der Welt und ist mit der Hand NICHT
 * abzubauen -- unzerstoerbar wie das Grundgestein, in dem es steckt. Herankommt nur der Bagger,
 * und auch der nur mit einem Bohrer der richtigen Stufe und, weiter unten in der Ergiebigkeit,
 * mit der passenden Saeure im Tank.
 *
 * WER ES ANSIEHT, ERFAEHRT WAS DRIN IST: Inhalt, Stufe und Saeurebedarf stehen in der
 * Einblendung -- sonst muesste man raten, welchen Bohrer man braucht.
 *
 * ABWEICHUNG: das Original zeichnet ueber die Grundgesteinstextur eine von zehn Erzformen in der
 * Farbe des Inhalts, in einem zweiten Durchgang. Der Port zeichnet vorerst nur das Grundgestein;
 * die Formen und der Farbgeber gehoeren in eine eigene Runde zum Blockrenderer.
 */
public class BedrockOreBlock extends BaseEntityBlock implements ILookOverlay {

    public static final MapCodec<BedrockOreBlock> CODEC = simpleCodec(BedrockOreBlock::new);

    public BedrockOreBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<BedrockOreBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BedrockOreBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof BedrockOreBlockEntity ore)) return;

        List<Component> text = new ArrayList<>();

        if(!ore.resource.isEmpty()) text.add(ore.resource.getHoverName());

        text.add(Component.translatable("tile.bedrock_ore.tier", ore.tier).withStyle(ChatFormatting.YELLOW));

        if(ore.acidRequirement != null) {
            text.add(Component.translatable("tile.bedrock_ore.requires", ore.acidRequirement.fill,
                    Component.translatable(ore.acidRequirement.type.getUnlocalizedName())).withStyle(ChatFormatting.AQUA));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
