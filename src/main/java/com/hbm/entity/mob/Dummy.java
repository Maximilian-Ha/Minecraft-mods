package com.hbm.entity.mob;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityDummy.
 *
 * Die Puppe. Sie hat keine Aufgaben, sie laeuft nicht weg, und sie schlaegt nicht zurueck --
 * sie steht da und sagt an, wieviel Leben sie noch hat.
 *
 * SIE TRAEGT, WAS MAN IHR ANZIEHT: ein Rechtsklick mit einem Ruestungsteil legt es ihr an,
 * und zwar in den Platz, zu dem es gehoert. Das Original rechnet dafuer 4 minus armorType;
 * auf 1.21 nennt das Ruestungsteil seinen Platz selbst.
 *
 * IHR NAME IST IHRE LEBENSANZEIGE: das Original gibt aus getCommandSenderName
 * "Leben / Hoechstleben" zurueck, auf ein Zehntel gerundet, und laesst den Namen IMMER
 * anzeigen. Genau so steht es hier.
 *
 * SIE LAESST NICHTS FALLEN -- auch nicht die Ruestung, die sie traegt. Das Original
 * ueberschreibt dropEquipment leer, und sonst nichts; eine Beutetabelle bekommt sie nicht.
 */
public class Dummy extends Mob {

    public Dummy(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    /**
     * OEFFENTLICH, nicht geschuetzt: Mob erklaert mobInteract als protected, aber jede andere
     * Stelle des Ports schreibt sie public. Wer hier enger schreibt, faellt beim Uebersetzen
     * durch -- der Fall aus CI-Lauf 479.
     */
    @Override
    public InteractionResult mobInteract(Player spieler, InteractionHand hand) {

        ItemStack stapel = spieler.getItemInHand(hand);

        if(stapel.getItem() instanceof ArmorItem ruestung) {
            this.setItemSlot(ruestung.getEquipmentSlot(), stapel.copy());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(spieler, hand);
    }

    @Override
    public Component getName() {
        return Component.literal((int) (this.getHealth() * 10) / 10F + " / " + (int) (this.getMaxHealth() * 10) / 10F);
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    protected void dropEquipment() { }

    /** Sie steht, wo man sie hinstellt: kein Wegstossen, kein Verschwinden. */
    @Override
    public boolean removeWhenFarAway(double abstand) {
        return false;
    }
}
