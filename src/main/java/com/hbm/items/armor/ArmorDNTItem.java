package com.hbm.items.armor;

import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.render.model.armor.ModelArmorDNT;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorDNT.
 *
 * Der DNT-Nanoanzug, der staerkste Satz des Mods. Eine Milliarde Ladung, und wer ihn
 * vollstaendig traegt, ist praktisch unverwundbar:
 *
 *   JEDER ANGRIFF AUSSER EINER SPRENGUNG WIRD ABGEBROCHEN, mit einem Klirren.
 *   SPRENGUNGEN richten ein Tausendstel ihres Schadens an, alles andere null.
 *
 * Dazu fliegt er. Drei Zustaende, alle nur bei vollstaendigem Satz:
 *
 *   TRIEBWERK (Rueckentriebwerk an, Sprungtaste): steigt um 0,2 je Tick bis 0,6.
 *   SCHWEBEFLUG (Rueckentriebwerk an, in der Luft, nicht in der Hocke): der Sturz wird
 *     aufgefangen, die Waagerechte um ein Zwanzigstel je Tick beschleunigt, und wer sich
 *     vorwaerts lehnt, wird in Blickrichtung gezogen.
 *   SINKEN (in der Hocke, in der Luft): zieht mit 0,1 je Tick nach unten.
 *
 * Und im Sprint ein Viertel mehr Tempo.
 *
 * NICHT UEBERNOMMEN: enableVATS, enableThermalSight, setHasHardLanding,
 * setStep/setJump/setFall, hides(HAT) sowie ArmorUtil.resetFlightTime und der
 * Partikeleffekt "jetpack_dns" -- dieselben fehlenden Teilsysteme wie beim Blackjack-Anzug.
 */
public class ArmorDNTItem extends ArmorFSBPoweredItem {

    private static final ResourceLocation SPRINT_BONUS = ResourceLocation.fromNamespaceAndPath("hbmsntm", "dnt_sprint");

    /** Ein Viertel mehr Tempo, absolut gerechnet -- Operation 0 im Original. */
    private static final AttributeModifier SPRINT_MOD =
            new AttributeModifier(SPRINT_BONUS, 0.25D, AttributeModifier.Operation.ADD_VALUE);

    public ArmorDNTItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(!(entity instanceof Player player)) return;
        if(player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;

        /* Erst abnehmen, dann bei Sprint neu auflegen -- wie beim Umgebungsanzug. */
        AttributeInstance tempo = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if(tempo != null) tempo.removeModifier(SPRINT_BONUS);
        if(tempo != null && player.isSprinting()) tempo.addTransientModifier(SPRINT_MOD);

        if(!hasFSBArmor(player)) return;

        HbmPlayerAttachments props = HbmPlayerAttachments.getData(player);
        boolean triebwerkAn = props.enableBackpack && props.getKeyPressed(EnumKeybind.JETPACK);

        Vec3 bewegung = player.getDeltaMovement();

        if(triebwerkAn) {

            if(bewegung.y < 0.6D) player.setDeltaMovement(bewegung.x, bewegung.y + 0.2D, bewegung.z);
            player.fallDistance = 0F;
            zischen(level, player);

        } else if(!player.isShiftKeyDown() && !player.onGround() && props.enableBackpack) {

            player.fallDistance = 0F;

            /* Der Sturz wird in Stufen aufgefangen, je schneller desto kraeftiger. */
            double y = bewegung.y;
            if(y < -1D) y += 0.4D;
            else if(y < -0.1D) y += 0.2D;
            else if(y < 0D) y = 0D;

            double x = bewegung.x * 1.05D;
            double z = bewegung.z * 1.05D;

            if(player.zza != 0F) {
                Vec3 blick = player.getLookAngle();
                x += blick.x * 0.25D * player.zza;
                z += blick.z * 0.25D * player.zza;
            }

            player.setDeltaMovement(x, y, z);
            zischen(level, player);
        }

        if(player.isShiftKeyDown() && !player.onGround()) {
            Vec3 jetzt = player.getDeltaMovement();
            player.setDeltaMovement(jetzt.x, jetzt.y - 0.1D, jetzt.z);
        }
    }

    private void zischen(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                NtmSoundEvents.WEAPON_IMMOLATOR_SHOOT.get(), SoundSource.PLAYERS, 0.125F, 1.5F);
    }

    @Override
    public void handleAttack(LivingIncomingDamageEvent event) {

        if(!(event.getEntity() instanceof Player player)) return;
        if(!hasFSBArmor(player)) return;

        /* Sprengungen laufen weiter und werden erst in handleHurt gedaempft. */
        if(event.getSource().is(DamageTypeTags.IS_EXPLOSION)) return;

        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                0.5F, 1.0F + player.getRandom().nextFloat() * 0.5F);
        event.setCanceled(true);
    }

    @Override
    public void handleHurt(LivingDamageEvent.Pre event) {

        if(!(event.getEntity() instanceof Player player)) return;
        if(!hasFSBArmor(player)) return;

        if(event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            event.setNewDamage(event.getNewDamage() * 0.001F);
            return;
        }

        event.setNewDamage(0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorDNT ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorDNT(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
