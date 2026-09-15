package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachinePrecAssBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPrecAss.
 *
 * Sie benutzt das Modell der Montagemaschine mit eigener Haut. Der Unterschied steckt in den
 * Armen: das Original zeichnet VIERMAL DENSELBEN ARM, je um neunzig Grad weitergedreht, alle
 * mit demselben Winkelsatz -- nur die vier Schlagbolzen laufen einzeln.
 */
public class RenderPrecAss extends BlockEntityRendererNT<MachinePrecAssBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachinePrecAssBlockEntity> create(Context context) { return new RenderPrecAss(); }

    @Override
    public void render(MachinePrecAssBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
        }

        bindTexture(ResourceManager.PRECASS_TEX);
        ResourceManager.assembly_machine.renderPart("Base");
        if(be.frame) ResourceManager.assembly_machine.renderPart("Frame");

        RenderContext.pushPose();

        float spin = BobMathUtil.interp(be.prevRing, be.ring, partialTicks);
        float[] arm = new float[] {
                BobMathUtil.interp(be.prevArmAngles[0], be.armAngles[0], partialTicks),
                BobMathUtil.interp(be.prevArmAngles[1], be.armAngles[1], partialTicks),
                BobMathUtil.interp(be.prevArmAngles[2], be.armAngles[2], partialTicks)
        };

        RenderContext.mulPose(Axis.YP.rotationDegrees(spin));
        ResourceManager.assembly_machine.renderPart("Ring");
        ResourceManager.assembly_machine.renderPart("Ring2");

        for(int i = 0; i < 4; i++) {
            renderArm(arm, BobMathUtil.interp(be.prevStrikers[i], be.strikers[i], partialTicks));
            RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));
        }

        RenderContext.popPose();

        GenericRecipe recipe = PrecAssRecipes.INSTANCE.recipeNameMap.get(be.assemblerModule.recipe);
        if(recipe != null && NuclearTechMod.proxy.me().distanceToSqr(be.getBlockPos().getBottomCenter().add(0, 1, 0)) < 35 * 35) {

            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.translate(0F, 1.0625F, 0F);

            ItemStack stack = recipe.getIcon();
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel model = renderer.getModel(stack, null, null, 0);

            if(model.isGui3d()) {
                RenderContext.translate(0F, 0.1F, 0F);
            } else {
                RenderContext.mulPose(Axis.XP.rotationDegrees(-90F));
            }
            RenderContext.scale(0.75F, 0.75F, 0.75F);

            renderer.render(stack, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer, RenderContext.light(), RenderContext.overlay(), model);
        }
    }

    /** Ein Arm samt Bolzen. Viermal aufgerufen, jedes Mal um neunzig Grad weitergedreht. */
    private static void renderArm(float[] arm, float striker) {

        RenderContext.pushPose(); {
            RenderContext.translate(0F, 1.625F, 0.9375F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(arm[0]));
            RenderContext.translate(0F, -1.625F, -0.9375F);
            ResourceManager.assembly_machine.renderPart("ArmLower1");

            RenderContext.translate(0F, 2.375F, 0.9375F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(arm[1]));
            RenderContext.translate(0F, -2.375F, -0.9375F);
            ResourceManager.assembly_machine.renderPart("ArmUpper1");

            RenderContext.translate(0F, 2.375F, 0.4375F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(arm[2]));
            RenderContext.translate(0F, -2.375F, -0.4375F);
            ResourceManager.assembly_machine.renderPart("Head1");
            RenderContext.translate(0F, striker, 0F);
            ResourceManager.assembly_machine.renderPart("Spike1");
        } RenderContext.popPose();
    }

    @Override
    public int getPacketLight(int packedLight, MachinePrecAssBlockEntity be) {
        if(be.getLevel() != null && be.getBlockState().getBlock() instanceof DummyableBlock dummy) {
            return LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above(dummy.getDimensions()[0]));
        }
        return packedLight;
    }

    // Kein Zwischenspeichern in einem Feld: BlockEntityRenderers legt pro BlockEntityType genau
    // EINEN Renderer an, den sich alle Maschinen dieses Typs teilen.
    @Override
    public AABB getRenderBoundingBox(MachinePrecAssBlockEntity be) {

        int x = be.getBlockPos().getX();
        int y = be.getBlockPos().getY();
        int z = be.getBlockPos().getZ();

        return new AABB(x - 1, y, z - 1, x + 2, y + 3, z + 2);
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_PRECASS.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2.75F, 0F);
                RenderContext.scale(4.5F, 4.5F, 4.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderContext.scale(0.75F, 0.75F, 0.75F);
                bindTexture(ResourceManager.PRECASS_TEX);
                ResourceManager.assembly_machine.renderPart("Base");
                ResourceManager.assembly_machine.renderPart("Frame");
                ResourceManager.assembly_machine.renderPart("Ring");
                ResourceManager.assembly_machine.renderPart("Ring2");
                float[] arm = new float[] { 45F, -30F, 45F };
                for(int i = 0; i < 4; i++) {
                    renderArm(arm, 0F);
                    RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                }
            }
        };
    }
}
