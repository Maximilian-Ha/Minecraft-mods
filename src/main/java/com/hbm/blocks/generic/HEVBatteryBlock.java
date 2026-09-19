package com.hbm.blocks.generic;

import com.hbm.items.armor.ArmorFSBItem;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.items.armor.ArmorFSBPoweredItem;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.HEVBattery.
 *
 * Der Akku an der Wand. Er haengt in den Bauwerken des Originals herum und laedt mit einem
 * Rechtsklick die ganze getragene Ruestung auf -- danach ist er fort. Wer keinen vollstaendigen
 * bestromten Satz traegt, bekommt nichts und der Akku bleibt haengen.
 *
 * ABWEICHUNG: das Original prueft den HELM auf ArmorFSBPowered, der gleichnamige Gegenstand
 * dagegen die BRUSTPLATTE. Bei einem einheitlichen Satz ist das dasselbe; der Port prueft an
 * beiden Stellen die Brustplatte, weil sie ohnehin ueber den Satz entscheidet.
 */
public class HEVBatteryBlock extends Block {

    /** Was ein Akku hergibt, in HE. Wert aus dem Original. */
    public static final long LADUNG = 150_000L;

    public static final MapCodec<HEVBatteryBlock> CODEC = simpleCodec(HEVBatteryBlock::new);

    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 6, 10);

    public HEVBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<HEVBatteryBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        if(!ladeRuestung(player)) return InteractionResult.CONSUME;

        level.playSound(null, player.getX(), player.getY(), player.getZ(), NtmSoundEvents.SUIT_BATTERY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.removeBlock(pos, false);

        return InteractionResult.CONSUME;
    }

    /**
     * Laedt jedes getragene Teil um LADUNG auf. Zurueck kommt, ob ueberhaupt geladen wurde --
     * nur dann verschwindet der Akku.
     */
    public static boolean ladeRuestung(Player player) {

        if(!ArmorFSBItem.hasFSBArmorIgnoreCharge(player)) return false;
        if(!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorFSBPoweredItem)) return false;

        for(ItemStack teil : player.getArmorSlots()) {
            if(teil.getItem() instanceof ArmorFSBPoweredItem akku) {
                akku.setCharge(teil, Math.min(akku.getCharge(teil) + LADUNG, akku.getMaxCharge(teil)));
            }
        }

        return true;
    }
}
