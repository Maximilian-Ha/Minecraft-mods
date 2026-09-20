package com.hbm.items.armor;

import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorBJJetpack.
 *
 * Die gefluegelte Blackjack-Brustplatte. Sie kann alles, was die gewoehnliche kann, und
 * dazu zweierlei:
 *
 *   IONENTRIEBWERK. Ist das Rueckentriebwerk eingeschaltet (Umschalttaste) und haelt der
 *   Traeger die Sprungtaste, steigt er auf, bis er 0,4 erreicht hat. Die Fallhoehe wird
 *   dabei zurueckgesetzt, und es zischt.
 *
 *   GLEITFLUG. Ist es aus und der Traeger geht in die Hocke, wird der Sturz auf vier
 *   Zehntel gebremst und die gewonnene Bewegung in Blickrichtung umgelenkt -- aus Fallen
 *   wird Gleiten.
 *
 * Beides gilt nur bei vollstaendigem, geladenem Satz.
 *
 * NICHT UEBERNOMMEN: ArmorUtil.resetFlightTime. Diese Flugzeitrechnung gibt es im Port
 * nicht; der Ladungswerfer sagt dasselbe an seiner Stelle. Ebenso der Partikeleffekt
 * "jetpack_bj" -- der Port hat keine Entsprechung dafuer, das Zischen bleibt.
 */
public class ArmorBJJetpackItem extends ArmorBJItem {

    public ArmorBJJetpackItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain, Variante.JETPACK);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(!(entity instanceof Player player)) return;
        if(player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;
        if(!hasFSBArmor(player)) return;

        HbmPlayerAttachments props = HbmPlayerAttachments.getData(player);
        boolean triebwerkAn = props.enableBackpack && props.getKeyPressed(EnumKeybind.JETPACK);

        Vec3 bewegung = player.getDeltaMovement();

        if(triebwerkAn) {

            if(bewegung.y < 0.4D) player.setDeltaMovement(bewegung.x, bewegung.y + 0.1D, bewegung.z);
            player.fallDistance = 0F;

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    NtmSoundEvents.WEAPON_IMMOLATOR_SHOOT.get(), SoundSource.PLAYERS, 0.125F, 1.5F);

        } else if(player.isShiftKeyDown() && bewegung.y < -0.08D) {

            /* Der gebremste Anteil des Sturzes wird nach vorn umgelenkt. */
            double gewonnen = bewegung.y * -0.4D;
            Vec3 blick = player.getLookAngle();

            player.setDeltaMovement(
                    bewegung.x + blick.x * gewonnen,
                    bewegung.y + gewonnen + blick.y * gewonnen,
                    bewegung.z + blick.z * gewonnen);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("  + ").append(Component.translatable("armor.electricJetpack")).withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  + ").append(Component.translatable("armor.glider")).withStyle(ChatFormatting.GRAY));
    }
}
