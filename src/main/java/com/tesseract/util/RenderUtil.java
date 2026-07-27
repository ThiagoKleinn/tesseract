package com.tesseract.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class RenderUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    /**
     * Desenha um item (ItemStack) na tela.
     */
    public static void drawItem(ItemStack stack, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null);
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
    }
    /**
     * Desenha um retângulo preenchido na tela.
     */
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    /**
     * Desenha uma string com sombra.
     */
    public static void drawStringWithShadow(String text, float x, float y, int color) {
        mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
    }
    /**
     * Desenha uma string sem sombra.
     */
    public static void drawString(String text, float x, float y, int color) {
        mc.fontRendererObj.drawString(text, (int)x, (int)y, color);
    }
    /**
     * Desenha uma string centralizada horizontalmente.
     */
    public static void drawStringCentered(String text, float x, float y, int color) {
        mc.fontRendererObj.drawString(text, (int)(x - mc.fontRendererObj.getStringWidth(text) / 2.0f), (int)y, color);
    }
    /**
     * Desenha uma string centralizada horizontal e verticalmente (opcional, útil para o Keystrokes).
     */
    public static void drawStringCenteredXY(String text, float x, float y, int color) {
        mc.fontRendererObj.drawString(text, (int)(x - mc.fontRendererObj.getStringWidth(text) / 2.0f), (int)(y - mc.fontRendererObj.FONT_HEIGHT / 2.0f), color);
    }
}
