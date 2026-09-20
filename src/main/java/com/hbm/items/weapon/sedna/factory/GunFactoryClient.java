package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.weapon.sedna.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import static com.hbm.items.weapon.sedna.factory.GunFactory.*;
import static com.hbm.items.weapon.sedna.factory.XFactory12ga.*;
import static com.hbm.items.weapon.sedna.factory.XFactory44.*;
import static com.hbm.items.weapon.sedna.factory.XFactory9mm.*;
import static com.hbm.items.weapon.sedna.factory.XFactoryCatapult.*;

public class GunFactoryClient {

    public static void init(RegisterClientExtensionsEvent event) {
        //GUNS
        registerGunItemRenderer(event, new ItemRenderDebug(), NtmItems.GUN_DEBUG.get());
        registerGunItemRenderer(event, new ItemRenderMaresleg(ResourceManager.MARESLEG_TEX), NtmItems.GUN_MARESLEG.get());
        registerGunItemRenderer(event, new ItemRenderSPAS12(), NtmItems.GUN_SPAS12.get());
        registerGunItemRenderer(event, new ItemRenderHangman(), NtmItems.GUN_HANGMAN.get());
        registerGunItemRenderer(event, new ItemRenderNI4NI(), NtmItems.GUN_N_I_4_N_I.get());
        registerGunItemRenderer(event, new ItemRenderHenry(ResourceManager.HENRY_TEX), NtmItems.GUN_HENRY.get());
        registerGunItemRenderer(event, new ItemRenderDrill(), NtmItems.GUN_DRILL.get());
        registerGunItemRenderer(event, new ItemRenderTeslaCannon(), NtmItems.GUN_TESLA_CANNON.get());
        registerGunItemRenderer(event, new ItemRenderFolly(), NtmItems.GUN_FOLLY.get());
        registerGunItemRenderer(event, new ItemRenderChargeThrower(), NtmItems.GUN_CHARGE_THROWER.get());
        registerGunItemRenderer(event, new ItemRenderFireExt(), NtmItems.GUN_FIREEXT.get());
        registerGunItemRenderer(event, new ItemRenderPAMelee(), NtmItems.GUN_PA_MELEE.get());
        registerGunItemRenderer(event, new ItemRenderChemthrower(), NtmItems.GUN_CHEMTHROWER.get());
        registerGunItemRenderer(event, new ItemRenderLaserPistol(ResourceManager.LASER_PISTOL_TEX), NtmItems.GUN_LASER_PISTOL.get());
        registerGunItemRenderer(event, new ItemRenderLaserPistol(ResourceManager.LASER_PISTOL_PEW_PEW_TEX), NtmItems.GUN_LASER_PISTOL_PEW_PEW.get());
        registerGunItemRenderer(event, new ItemRenderLaserPistol(ResourceManager.LASER_PISTOL_MORNING_GLORY_TEX), NtmItems.GUN_LASER_PISTOL_MORNING_GLORY.get());
        registerGunItemRenderer(event, new ItemRenderLasrifle(), NtmItems.GUN_LASRIFLE.get());
        registerGunItemRenderer(event, new ItemRenderFlamer(ResourceManager.FLAMETHROWER_TEX), NtmItems.GUN_FLAMER.get());
        registerGunItemRenderer(event, new ItemRenderFlamer(ResourceManager.FLAMETHROWER_TOPAZ_TEX), NtmItems.GUN_FLAMER_TOPAZ.get());
        registerGunItemRenderer(event, new ItemRenderFlamer(ResourceManager.FLAMETHROWER_DAYBREAKER_TEX), NtmItems.GUN_FLAMER_DAYBREAKER.get());
        registerGunItemRenderer(event, new ItemRenderHenry(ResourceManager.HENRY_LINCOLN_TEX), NtmItems.GUN_HENRY_LINCOLN.get());
        registerGunItemRenderer(event, new ItemRenderHeavyRevolver(ResourceManager.HEAVY_REVOLVER_TEX), NtmItems.GUN_HEAVY_REVOLVER.get());
        registerGunItemRenderer(event, new ItemRenderLiberator(), NtmItems.GUN_LIBERATOR.get());
        registerGunItemRenderer(event, new ItemRenderHeavyRevolver(ResourceManager.LILMAC_TEX), NtmItems.GUN_HEAVY_REVOLVER_LILMAC.get());
        registerGunItemRenderer(event, new ItemRenderHeavyRevolver(ResourceManager.PROTEGE_TEX), NtmItems.GUN_HEAVY_REVOLVER_PROTEGE.get());
        registerGunItemRenderer(event, new ItemRenderGreasegun(), NtmItems.GUN_GREASEGUN.get());
        registerGunItemRenderer(event, new ItemRenderPepperbox(), NtmItems.GUN_PEPPERBOX.get());
        registerGunItemRenderer(event, new ItemRenderAtlas(ResourceManager.BIO_REVOLVER_TEX), NtmItems.GUN_LIGHT_REVOLVER.get());
        registerGunItemRenderer(event, new ItemRenderAtlas(ResourceManager.BIO_REVOLVER_ATLAS_TEX), NtmItems.GUN_LIGHT_REVOLVER_ATLAS.get());
        registerGunItemRenderer(event, new ItemRenderAm180(), NtmItems.GUN_AM180.get());
        registerGunItemRenderer(event, new ItemRenderStarF(ResourceManager.STAR_F_TEX), NtmItems.GUN_STAR_F.get());
        registerGunItemRenderer(event, new ItemRenderStarFAkimbo(), NtmItems.GUN_STAR_F_AKIMBO.get());
        registerGunItemRenderer(event, new ItemRenderMaresleg(ResourceManager.MARESLEG_BROKEN_TEX), NtmItems.GUN_MARESLEG_BROKEN.get());
        registerGunItemRenderer(event, new ItemRenderMareslegAkimbo(), NtmItems.GUN_MARESLEG_AKIMBO.get());
        registerGunItemRenderer(event, new ItemRenderDANI(), NtmItems.GUN_LIGHT_REVOLVER_DANI.get());
        registerGunItemRenderer(event, new ItemRenderUzi(), NtmItems.GUN_UZI.get());
        registerGunItemRenderer(event, new ItemRenderUziAkimbo(), NtmItems.GUN_UZI_AKIMBO.get());
        registerGunItemRenderer(event, new ItemRenderAmat(ResourceManager.AMAT_TEX), NtmItems.GUN_AMAT.get());
        registerGunItemRenderer(event, new ItemRenderAmat(ResourceManager.AMAT_SUBTLETY_TEX), NtmItems.GUN_AMAT_SUBTLETY.get());
        registerGunItemRenderer(event, new ItemRenderAmat(ResourceManager.AMAT_PENANCE_TEX), NtmItems.GUN_AMAT_PENANCE.get());
        registerGunItemRenderer(event, new ItemRenderM2(), NtmItems.GUN_M2.get());
        registerGunItemRenderer(event, new ItemRenderG3(ResourceManager.G3_TEX), NtmItems.GUN_G3.get());
        registerGunItemRenderer(event, new ItemRenderG3(ResourceManager.G3_ZEBRA_TEX), NtmItems.GUN_G3_ZEBRA.get());
        registerGunItemRenderer(event, new ItemRenderSTG77(), NtmItems.GUN_STG77.get());
        registerGunItemRenderer(event, new ItemRenderCarbine(), NtmItems.GUN_CARBINE.get());
        registerGunItemRenderer(event, new ItemRenderMAS36(), NtmItems.GUN_MAS36.get());
        registerGunItemRenderer(event, new ItemRenderMinigun(ResourceManager.MINIGUN_TEX), NtmItems.GUN_MINIGUN.get());
        registerGunItemRenderer(event, new ItemRenderMinigunDual(), NtmItems.GUN_MINIGUN_DUAL.get());
        registerGunItemRenderer(event, new ItemRenderDoubleBarrel(ResourceManager.DOUBLE_BARREL_TEX), NtmItems.GUN_DOUBLE_BARREL.get());
        registerGunItemRenderer(event, new ItemRenderDoubleBarrel(ResourceManager.DOUBLE_BARREL_SACRED_DRAGON_TEX), NtmItems.GUN_DOUBLE_BARREL_SACRED_DRAGON.get());
        registerGunItemRenderer(event, new ItemRenderBolter(), NtmItems.GUN_BOLTER.get());
        registerGunItemRenderer(event, new ItemRenderAberrator(), NtmItems.GUN_ABERRATOR.get());
        registerGunItemRenderer(event, new ItemRenderAberrator(), NtmItems.GUN_ABERRATOR_EOTT.get());
        registerGunItemRenderer(event, new ItemRenderFlaregun(), NtmItems.GUN_FLAREGUN.get());
        registerGunItemRenderer(event, new ItemRenderCongoLake(), NtmItems.GUN_CONGOLAKE.get());
        registerGunItemRenderer(event, new ItemRenderPanzerschreck(), NtmItems.GUN_PANZERSCHRECK.get());
        registerGunItemRenderer(event, new ItemRenderMK108(), NtmItems.GUN_MK108.get());
        registerGunItemRenderer(event, new ItemRenderShredder(ResourceManager.SHREDDER_TEX), NtmItems.GUN_AUTOSHOTGUN.get());
        registerGunItemRenderer(event, new ItemRenderShredder(ResourceManager.SHREDDER_TEX), NtmItems.GUN_AUTOSHOTGUN_SHREDDER.get());
        registerGunItemRenderer(event, new ItemRenderShredder(ResourceManager.SHREDDER_TEX), NtmItems.GUN_AUTOSHOTGUN_SEXY.get());

        //PROJECTILES
        ammo_debug.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        ammo_debug_shot.setRenderer(LegoClient.RENDER_STANDARD_BULLET);

        m44_bp.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        m44_sp.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        m44_fmj.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        m44_jhp.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        m44_ap.setRenderer(LegoClient.RENDER_AP_BULLET);
        m44_express.setRenderer(LegoClient.RENDER_EXPRESS_BULLET);

        g12_bp.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        g12_bp_magnum.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        g12_bp_slug.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        g12.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        g12_slug.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        g12_flechette.setRenderer(LegoClient.RENDER_FLECHETTE_BULLET);
        g12_magnum.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        g12_explosive.setRenderer(LegoClient.RENDER_EXPRESS_BULLET);
        g12_phosphorus.setRenderer(LegoClient.RENDER_AP_BULLET);

        p9_sp.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        p9_fmj.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        p9_jhp.setRenderer(LegoClient.RENDER_STANDARD_BULLET);
        p9_ap.setRenderer(LegoClient.RENDER_AP_BULLET);

        cluster_submunition.setRenderer(LegoClient.RENDER_BOMB);

        /* DIE STRAHLEN. Bis Runde 188 hatte keiner von ihnen einen Zeichner -- was nicht
         * auffiel, weil bis dahin auch gar kein Strahl erzeugt wurde. Jetzt wirken sie, und
         * damit gehoert sichtbar gemacht, was sie tun. */
        XFactory35800.p35800.setRendererBeam(LegoClient.RENDER_CRACKLE);
        XFactory35800.p35800_bl.setRendererBeam(LegoClient.RENDER_BLACK_LIGHTNING);

        for(BulletConfig schredder : XFactory12ga.SCHREDDER_STRAHLEN) schredder.setRendererBeam(LegoClient.RENDER_SHREDDER);
        XFactoryAccelerator.ni4ni_arc.setRendererBeam(LegoClient.RENDER_NI4NI_BOLT);
        XFactoryEnergy.energy_tesla.setRendererBeam(LegoClient.RENDER_LIGHTNING);
        XFactoryEnergy.energy_tesla_overcharge.setRendererBeam(LegoClient.RENDER_LIGHTNING);
        XFactoryEnergy.energy_tesla_ir.setRendererBeam(LegoClient.RENDER_LIGHTNING);
        XFactoryEnergy.energy_tesla_ir_sub.setRendererBeam(LegoClient.RENDER_LIGHTNING_SUB);
        XFactoryEnergy.energy_las.setRendererBeam(LegoClient.RENDER_LASER_RED);
        XFactoryEnergy.energy_las_overcharge.setRendererBeam(LegoClient.RENDER_LASER_RED);
        XFactoryEnergy.energy_las_ir.setRendererBeam(LegoClient.RENDER_LASER_RED);
        XFactoryEnergy.energy_emerald.setRendererBeam(LegoClient.RENDER_LASER_EMERALD);
        XFactoryEnergy.energy_emerald_overcharge.setRendererBeam(LegoClient.RENDER_LASER_EMERALD);
        XFactoryEnergy.energy_emerald_ir.setRendererBeam(LegoClient.RENDER_LASER_EMERALD);

        //HUDS
        ((GunBaseNTItem) NtmItems.GUN_DEBUG.get())						.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO, LegoClient.HUD_COMPONENT_AMMO_SECOND);
        ((GunBaseNTItem) NtmItems.GUN_MARESLEG.get())					.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_HENRY.get())						.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_DRILL.get())						.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_TESLA_CANNON.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_FOLLY.get())						.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_CHARGE_THROWER.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_CHEMTHROWER.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_LASER_PISTOL.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_LASER_PISTOL_PEW_PEW.get())		.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_LASER_PISTOL_MORNING_GLORY.get()).getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_LASRIFLE.get())					.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_FLAMER.get())						.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO_NOCOUNTER);
        ((GunBaseNTItem) NtmItems.GUN_FLAMER_TOPAZ.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO_NOCOUNTER);
        ((GunBaseNTItem) NtmItems.GUN_FLAMER_DAYBREAKER.get())			.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO_NOCOUNTER);
        ((GunBaseNTItem) NtmItems.GUN_N_I_4_N_I.get())					.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_HENRY_LINCOLN.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_SPAS12.get())						.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_HANGMAN.get())					.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_HEAVY_REVOLVER.get())			.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_LIBERATOR.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_HEAVY_REVOLVER_LILMAC.get())	.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_HEAVY_REVOLVER_PROTEGE.get())	.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_PANZERSCHRECK.get())			.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
        ((GunBaseNTItem) NtmItems.GUN_GREASEGUN.get())				.getConfig(null, 0).hud(LegoClient.HUD_COMPONENT_DURABILITY, LegoClient.HUD_COMPONENT_AMMO);
    }

    public static void registerGunItemRenderer(RegisterClientExtensionsEvent event, ItemRenderWeaponBase weaponRenderer, Item item) {
        event.registerItem(new IClientItemExtensions() {

            private ItemRenderWeaponBase renderer;

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                if(renderer == null) this.renderer = weaponRenderer;
                renderer.setup(itemInHand, poseStack, partialTick);
                return true;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(renderer == null) this.renderer = weaponRenderer;
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity living, InteractionHand hand, ItemStack itemStack) {
                if(renderer == null) this.renderer = weaponRenderer;
                renderer.setEntity(living);

                return IClientItemExtensions.super.getArmPose(living, hand, itemStack);
            }
        }, item);
    }

}
