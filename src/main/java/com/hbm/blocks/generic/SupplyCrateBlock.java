package com.hbm.blocks.generic;

import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.SupplyCrateBlockEntity;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockSupplyCrate.
 *
 * Die Nachschubkiste, die am Fallschirm herunterkommt. Anders als die uebrigen Kisten des Mods
 * wuerfelt sie ihren Inhalt nicht aus -- sie traegt genau das, was ihr mitgegeben wurde, und
 * gibt es beim Aufbrechen wieder heraus. Geoeffnet wird sie wie alle anderen: mit der
 * Brechstange.
 *
 * WIRD SIE STATTDESSEN ABGEBAUT, wandert der Inhalt ueber IPersistentNBT in den
 * Gegenstandsstapel und beim Setzen wieder zurueck. Das Original schreibt dafuer von Hand
 * "slot0", "slot1" ... in die Gegenstandsdaten und zaehlt sie in "amount"; im Port genuegt
 * derselbe Weg, den die Lagerkisten und die Faesser schon gehen.
 *
 * SIE TEILT SICH IHR AUSSEHEN MIT DER DOSENKISTE, wie im Original: dort verweist ihr
 * Zeichnertyp auf BlockCanCrate.renderID und ihre Textur auf "hbm:crate_can". Hier zeigt ihr
 * Blockzustand auf dasselbe Modell block/crate_can.
 */
public class SupplyCrateBlock extends BaseEntityBlock {

    public static final MapCodec<SupplyCrateBlock> CODEC = simpleCodec(SupplyCrateBlock::new);

    public SupplyCrateBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<SupplyCrateBlock> codec() { return CODEC; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SupplyCrateBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if(!stack.is(NtmItems.CROWBAR.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!level.isClientSide) {
            if(level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity kiste) {
                for(ItemStack stueck : kiste.items) Block.popResource(level, pos, stueck);
                kiste.items.clear();
            }

            level.removeBlock(pos, false);
            level.playSound(null, pos, NtmSoundEvents.CRATE_BREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if(!level.isClientSide) IPersistentNBT.restoreData(level, pos, stack);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return IPersistentNBT.getDropsFromLootParams(state, params);
    }
}
