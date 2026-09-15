package com.hbm.items.tools;

import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemTooling.
 *
 * Ein Werkzeug, das Maschinen einstellt statt etwas zu bauen: Schraubenzieher, Handbohrer,
 * Entschaerfer. Es fragt den angeklickten Block, ob er sich einstellen laesst, und ueberlaesst
 * ihm die Arbeit.
 *
 * DER GRIFF HEISST onItemUseFirst, UND DAS IST DER GANZE PUNKT. Wer nur useOn ueberschreibt,
 * kommt zu spaet: Minecraft fragt erst den Block und dann den Gegenstand in der Hand, und ein
 * Block, der ein Fenster oeffnet, hat den Klick dann schon verbraucht. Genau das ist im Port
 * bis Runde 99 geschehen -- der Schraubenzieher oeffnete die Oberflaeche, statt zu schrauben,
 * bei jeder Maschine mit Fenster. Das Original stellt sich aus demselben Grund vor den Block,
 * dort per onItemUse und einer Ausnahme in jedem betroffenen Block; auf 1.21 gibt es dafuer
 * einen Griff, der ausdruecklich VOR der Blockabfrage laeuft.
 *
 * Abgenutzt wird nur bei Erfolg, und nur wenn das Werkzeug ueberhaupt Haltbarkeit hat -- der
 * Schraubenzieher aus Desh hat keine. Beides steht so im Original.
 */
public class ToolingItem extends Item {

    protected ToolType type;

    public ToolingItem(ToolType type, Properties properties) {
        super(properties.stacksTo(1));

        this.type = type;

        type.register(new ItemStack(this));
    }

    public ToolType getToolType() {
        return this.type;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos usedPos = context.getClickedPos();
        Direction usedDirection = context.getClickedFace();

        if(player == null) return InteractionResult.PASS;

        if(!(level.getBlockState(usedPos).getBlock() instanceof IToolable toolable)) return InteractionResult.PASS;

        if(!toolable.onScrew(level, player, usedPos, usedDirection, this.type)) return InteractionResult.PASS;

        if(stack.isDamageableItem()) {
            stack.hurtAndBreak(1, player, context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }

        return InteractionResult.SUCCESS;
    }
}
