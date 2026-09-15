package com.hbm.items.tools;

import com.hbm.util.TagsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemCoordinateBase.
 *
 * Ein Gegenstand, der sich eine Stelle in der Welt merkt. Was eine gueltige Stelle ist und was
 * beim Merken geschehen soll, entscheidet die Ableitung.
 *
 * ABWEICHUNG: das Original schreibt die drei Zahlen einzeln ins Gegenstands-NBT. Auf 1.21 gibt
 * es dafuer die Datenbestandteile; die Stelle liegt hier unter "pos" im eigenen Datenblock, wie
 * bei allen anderen portierten Gegenstaenden mit Gedaechtnis.
 */
public abstract class CoordinateItem extends Item {

    public CoordinateItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockPos getPosition(ItemStack stack) {

        if(!TagsUtil.hasCustomData(stack)) return null;

        CompoundTag tag = TagsUtil.getCustomData(stack);
        if(!tag.contains("posX")) return null;

        return new BlockPos(tag.getInt("posX"), tag.getInt("posY"), tag.getInt("posZ"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if(!this.canGrabCoordinateHere(level, pos)) return InteractionResult.PASS;

        if(!level.isClientSide) {

            BlockPos target = this.getCoordinates(level, pos);

            CompoundTag tag = TagsUtil.getCustomData(context.getItemInHand());
            tag.putInt("posX", target.getX());
            /* Nicht jeder Verbinder braucht die Hoehe -- wer sie nicht braucht, merkt sich Null,
             * damit der Eintrag nicht von einer frueheren Stelle stehen bleibt. */
            tag.putInt("posY", this.includeY() ? target.getY() : 0);
            tag.putInt("posZ", target.getZ());
            TagsUtil.putCustomData(context.getItemInHand(), tag);

            this.onTargetSet(level, target, context.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /** Ob an dieser Stelle etwas steht, das sich merken laesst. */
    public abstract boolean canGrabCoordinateHere(Level level, BlockPos pos);

    /** Welche Stelle gemerkt wird -- bei Mehrblock-Bauten die des Kerns, nicht die des Klicks. */
    public abstract BlockPos getCoordinates(Level level, BlockPos pos);

    public abstract void onTargetSet(Level level, BlockPos pos, @Nullable Player player);

    public boolean includeY() { return true; }
}
