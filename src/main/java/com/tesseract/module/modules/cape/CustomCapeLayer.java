package com.tesseract.module.modules.cape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CustomCapeLayer implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer playerRenderer;
    private final ModelRenderer capeModel; // <- campo

    public CustomCapeLayer(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;

        ModelBiped dummy = new ModelBiped();
        capeModel = new ModelRenderer(dummy, 0, 0);
        capeModel.textureWidth  = 64;
        capeModel.textureHeight = 32;
        capeModel.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1);
        capeModel.rotationPointY = 0.0F;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        // Só renderiza para o jogador local
        if (!CapeManager.hasCape(player)) return;

        ResourceLocation capeTexture = CapeManager.getCape(player);
        if (capeTexture == null) return;

        // Confirma que a textura está registrada no TextureManager
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getTextureManager().getTexture(capeTexture) == null) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.playerRenderer.bindTexture(capeTexture);

        GlStateManager.pushMatrix();

        // Âncora no body — igual ao LayerCape vanilla
        this.playerRenderer.getMainModel().bipedBody.postRender(0.0625F);

        GlStateManager.translate(0.0F, 0.0F, 0.125F);

        double dx = player.prevChasingPosX
                + (player.chasingPosX - player.prevChasingPosX) * partialTicks
                - (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
        double dy = player.prevChasingPosY
                + (player.chasingPosY - player.prevChasingPosY) * partialTicks
                - (player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
        double dz = player.prevChasingPosZ
                + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks
                - (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);

        float yaw = player.prevRenderYawOffset
                + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double sinYaw = Math.sin(yaw * Math.PI / 180.0D);
        double cosYaw = -Math.cos(yaw * Math.PI / 180.0D);

        float swingY = (float) dy * 10.0F;
        swingY = MathHelper.clamp_float(swingY, -6.0F, 32.0F);

        float swingX = (float) (dx * sinYaw + dz * cosYaw) * 100.0F;
        swingX = MathHelper.clamp_float(swingX, 0.0F, 150.0F);

        float swingZ = (float) (dx * cosYaw - dz * sinYaw) * 100.0F;
        swingZ = MathHelper.clamp_float(swingZ, -20.0F, 20.0F);

        float pitch = player.prevCameraYaw
                + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        float walked = player.prevDistanceWalkedModified
                + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks;
        swingY += MathHelper.sin(walked * 6.0F) * 32.0F * pitch;

        if (player.isSneaking()) {
            swingY += 25.0F;
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }

        GlStateManager.rotate(6.0F + swingX / 2.0F + swingY, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(swingZ / 2.0F,  0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-swingZ / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        // Render da capa usando o modelo do playerRenderer direto
        this.playerRenderer.getMainModel().renderCape(0.0625F);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}