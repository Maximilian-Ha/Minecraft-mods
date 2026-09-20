package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorEnvsuit;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorEnvsuit.
 *
 * Der M1TTY-Umgebungsanzug. Ein Taucheranzug, der auch an Land etwas kann:
 *
 *   IM SPRINT SCHNELLER. Solange der Traeger sprintet, bekommt er ein Zehntel mehr Tempo.
 *   Hoert er auf, geht es wieder weg -- das Original legt den Aufschlag jeden Tick neu an
 *   und nimmt ihn jeden Tick wieder ab.
 *
 *   UNTER WASSER EIN ANTRIEB. Der Atem bleibt voll, es gibt Nachtsicht, und wer sich vorwaerts
 *   lehnt, wird in Blickrichtung geschoben.
 *
 *   UEBER WASSER wird die Nachtsicht wieder abgenommen.
 *
 * ALLES DAVON HAENGT AN DER BRUSTPLATTE und am vollstaendigen Satz, genau wie im Original.
 *
 * NICHT UEBERNOMMEN: hides(EnumPlayerPart.HAT). Ebenso die Ausnahme fuer den
 * Nachtsicht-Aufsatz am Helm -- das Original nimmt die Nachtsicht NICHT ab, wenn im Helm ein
 * ItemModNightVision steckt. Dieses Modul gibt es im Port noch nicht; solange es fehlt,
 * verhaelt sich der Anzug wie im Original ohne Aufsatz.
 */
public class ArmorEnvsuitItem extends ArmorFSBPoweredItem {

    /** Der Sprintaufschlag. Im Original ein fester UUID-Schluessel, hier ein fester Name. */
    private static final ResourceLocation SPRINT_BONUS = ResourceLocation.fromNamespaceAndPath("hbmsntm", "envsuit_sprint");

    /** Ein Zehntel mehr Tempo, absolut gerechnet -- Operation 0 im Original. */
    private static final AttributeModifier SPRINT_MOD =
            new AttributeModifier(SPRINT_BONUS, 0.1D, AttributeModifier.Operation.ADD_VALUE);

    public ArmorEnvsuitItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(!(entity instanceof Player player)) return;
        if(player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;

        /* Erst abnehmen, dann gegebenenfalls neu auflegen -- so haelt das Original den
         * Aufschlag an den Sprint gebunden, ohne ihn zu stapeln. */
        AttributeInstance tempo = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if(tempo != null) tempo.removeModifier(SPRINT_BONUS);

        if(!hasFSBArmor(player)) return;

        if(tempo != null && player.isSprinting()) tempo.addTransientModifier(SPRINT_MOD);

        if(player.isInWater()) {

            if(!level.isClientSide) {
                player.setAirSupply(300);
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 15 * 20, 0, true, false));
            }

            /* Der Antrieb: ein Zehntel der Vorwaertsneigung in Blickrichtung. */
            double schub = 0.1D * player.zza;
            Vec3 blick = player.getLookAngle();
            player.setDeltaMovement(player.getDeltaMovement().add(blick.x * schub, blick.y * schub, blick.z * schub));

        } else if(!level.isClientSide) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorEnvsuit ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorEnvsuit(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
