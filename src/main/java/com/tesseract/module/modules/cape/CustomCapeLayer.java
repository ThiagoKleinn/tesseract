package com.tesseract.module.modules.cape;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CustomCapeLayer implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer playerRenderer;

    public CustomCapeLayer(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (!player.hasPlayerInfo()) return;
        
        if (!player.isWearing(EnumPlayerModelParts.CAPE)) {
             // Força a exibição da capa se for o jogador local
             if (player.getUniqueID().equals(net.minecraft.client.Minecraft.getMinecraft().thePlayer.getUniqueID())) {
                 // Continuar
             } else {
                 return;
             }
        }
        
        if (!CapeManager.hasCape(player)) return;

        ResourceLocation capeTexture = CapeManager.getCape(player);
        if (capeTexture == null) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.playerRenderer.bindTexture(capeTexture);

        GlStateManager.pushMatrix();
        this.playerRenderer.getMainModel().bipedBody.postRender(0.0625F);

        // Habilita blend e alpha test para garantir transparência correta se houver
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();

        GlStateManager.translate(0.0F, 0.0F, 0.125F);

        double dx = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks
                - (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
        double dy = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks
                - (player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
        double dz = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks
                - (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);

        float yaw = player.prevRenderYawOffset
                + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;

        double sinYaw = Math.sin(yaw * Math.PI / 180.0D);
        double cosYaw = -Math.cos(yaw * Math.PI / 180.0D);

        float swingY = (float)(dy * 10.0D);
        swingY = MathHelper.clamp_float(swingY, -6.0F, 32.0F);

        float swingX = (float)(dx * sinYaw + dz * cosYaw) * 100.0F;
        swingX = MathHelper.clamp_float(swingX, 0.0F, 150.0F);

        float swingZ = (float)(dx * cosYaw - dz * sinYaw) * 100.0F;
        swingZ = MathHelper.clamp_float(swingZ, -20.0F, 20.0F);

        float pitch = player.prevCameraYaw
                + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        float walked = player.prevDistanceWalkedModified
                + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks;
        swingY += MathHelper.sin(walked * 6.0F) * 32.0F * pitch;

        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }

        GlStateManager.rotate(6.0F + swingX / 2.0F + swingY, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(swingZ / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-swingZ / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        ModelBiped model = (ModelBiped) this.playerRenderer.getMainModel();
        GlStateManager.scale(-1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);

        net.minecraft.client.model.ModelRenderer capeModel =
                new net.minecraft.client.model.ModelRenderer(model, 0, 0);
        capeModel.textureWidth = 64;
        capeModel.textureHeight = 32;
        capeModel.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1);
        capeModel.rotationPointY = 0.0F;
        capeModel.render(0.0625F);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}