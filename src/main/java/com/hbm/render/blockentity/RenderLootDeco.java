package com.hbm.render.blockentity;

import com.hbm.blockentity.LootDecoBlockEntity;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Tuple.Quartet;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderLoot.
 *
 * Zeichnet, was auf einem Beutesockel liegt: jeden Stapel an seinem eigenen Versatz, flach auf
 * dem Boden. Der Normalfall ist das Gegenstandsbild, um neunzig Grad gekippt und halbiert --
 * dieselbe Lage wie im Original, dort noch als rohes Zweiecksbild gezeichnet.
 *
 * ZWEI AUSNAHMEN hat das Original, und beide hat der Port:
 *   * eine Minikernwaffe (die fuenf Patronen von NUKE_STANDARD bis NUKE_HIVE) liegt als
 *     koerperliches Modell da, nicht als Bild;
 *   * die Mare's Leg ebenso, gedreht und angewinkelt, damit sie wie hingelegt aussieht.
 *
 * ZWEI WEITERE AUSNAHMEN sind NICHT uebernommen: das Original zeichnet die Trenchmaster- und
 * die NCR-Ruestung als getragene Ruestungsteile. Beide Ruestungen hat der Port nicht; sie
 * wuerden hier als gewoehnliches Gegenstandsbild liegen, sobald sie nachkommen.
 */
public class RenderLootDeco extends BlockEntityRendererNT<LootDecoBlockEntity> {

    @Override public BlockEntityRenderer<LootDecoBlockEntity> create(Context context) { return new RenderLootDeco(); }

    @Override
    public void render(LootDecoBlockEntity sockel, MultiBufferSource buffer, float partialTicks) {

        for(Quartet<ItemStack, Double, Double, Double> eintrag : sockel.items) {

            ItemStack stapel = eintrag.getW();
            if(stapel == null || stapel.isEmpty()) continue;

            RenderContext.pushPose(); {

                RenderContext.translate(eintrag.getX().floatValue(), eintrag.getY().floatValue(), eintrag.getZ().floatValue());

                if(istMinikernwaffe(stapel)) {
                    zeichneMinikernwaffe();
                } else if(stapel.is(NtmItems.GUN_MARESLEG.get())) {
                    zeichneMaresleg();
                } else {
                    zeichneGegenstand(stapel, buffer);
                }

            } RenderContext.popPose();
        }
    }

    /** Die fuenf Minikernwaffen liegen in EnumAmmo zusammen; das Original prueft ihre Spanne. */
    private static boolean istMinikernwaffe(ItemStack stapel) {
        if(!stapel.is(NtmItems.AMMO_STANDARD.get())) return false;
        int meta = MetaHelper.getMeta(stapel);
        return meta >= Ammo.NUKE_STANDARD.ordinal() && meta <= Ammo.NUKE_HIVE.ordinal();
    }

    private void zeichneMinikernwaffe() {
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        RenderContext.translate(1F, 0.5F, 1F);
        this.bindTexture(ResourceManager.FATMAN_MININUKE_TEX);
        ResourceManager.fatman.renderPart("MiniNuke");
    }

    private void zeichneMaresleg() {
        RenderContext.scale(0.125F, 0.125F, 0.125F);
        RenderContext.translate(3F, 0F, 0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        this.bindTexture(ResourceManager.MARESLEG_TEX);
        ResourceManager.maresleg.renderAll();
    }

    private void zeichneGegenstand(ItemStack stapel, MultiBufferSource buffer) {

        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel modell = renderer.getModel(stapel, null, null, 0);

        RenderContext.translate(0.25F, 0F, 0.25F);
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F));

        renderer.render(stapel, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer,
                RenderContext.light(), RenderContext.overlay(), modell);
    }
}
