package com.tesseract.module.modules.cape;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapeSelectionGui extends GuiScreen {

    private List<Cape> capes;
    private int selected = -1;
    private GuiCapeList capeList;

    private static final int LIST_WIDTH = 180;

    @Override
    public void initGui() {
        capes = new ArrayList<>(CapeManager.AVAILABLE_CAPES);

        Cape current = CapeManager.getSelectedCape();
        if (current != null) {
            for (int i = 0; i < capes.size(); i++) {
                if (capes.get(i).name.equals(current.name)) {
                    selected = i;
                    break;
                }
            }
        }

        capeList = new GuiCapeList(this, capes, LIST_WIDTH, height);

        int buttonWidth = 100;
        int previewCenterX = LIST_WIDTH + (width - LIST_WIDTH) / 2;
        int buttonX = previewCenterX - buttonWidth - 5;

        buttonList.add(new GuiButton(0, buttonX, height - 30, buttonWidth, 20, "Selecionar"));
        buttonList.add(new GuiButton(1, buttonX + buttonWidth + 10, height - 30, buttonWidth, 20, "Remover Capa"));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        capeList.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        capeList.drawScreen(mouseX, mouseY, partialTicks);

        int previewCenterX = LIST_WIDTH + (width - LIST_WIDTH) / 2;
        int previewCenterY = height / 2;

        drawCenteredString(fontRendererObj, "Menu de Capas - Thiago Klein", previewCenterX, 10, 0xFFFFFF);

        if (selected >= 0) {
            Cape cape = capes.get(selected);
            drawCenteredString(fontRendererObj, cape.name, previewCenterX, previewCenterY - 80, 0xFFFFFF);
            drawPlayerPreview(previewCenterX, previewCenterY + 30, 40, mouseX, mouseY, cape, partialTicks);
        } else {
            drawCenteredString(fontRendererObj, "Selecione uma capa", previewCenterX, previewCenterY, 0xAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPlayerPreview(int x, int y, int scale, float mouseX, float mouseY, Cape cape, float partialTicks) {
        float autoYaw = (mc.theWorld.getTotalWorldTime() + partialTicks) * 2.0F;

        // Armazena a capa original e define a selecionada para o renderizador
        Cape originalCape = CapeManager.getSelectedCape();
        // Precisamos temporariamente mudar o que o CapeManager retorna para o mc.thePlayer
        // Mas o CapeManager.getCape(player) usa a selectedCape estática.
        CapeManager.selectCape(cape);

        // Força remover invisibilidade durante o preview
        boolean wasInvisible = mc.thePlayer.isInvisible();
        mc.thePlayer.setInvisible(false);

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        float savedYawOffset = mc.thePlayer.renderYawOffset;
        float savedYaw = mc.thePlayer.rotationYaw;
        float savedPitch = mc.thePlayer.rotationPitch;
        float savedYawHead = mc.thePlayer.rotationYawHead;
        float savedPrevYawHead = mc.thePlayer.prevRotationYawHead;

        mc.thePlayer.renderYawOffset = autoYaw;
        mc.thePlayer.rotationYaw = autoYaw;
        mc.thePlayer.rotationPitch = 0.0F;
        mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw;
        mc.thePlayer.prevRotationYawHead = mc.thePlayer.rotationYaw;

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        RenderManager rm = mc.getRenderManager();
        rm.setPlayerViewY(180.0F);
        rm.setRenderShadow(false);

        // Habilita o teste de profundidade para que as camadas não se sobreponham errado
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true); // Garante que o corpo possa escrever no Z-buffer
        GlStateManager.clear(256); // Limpa o depth buffer localmente para este preview
        rm.renderEntityWithPosYaw(mc.thePlayer, 0.0, 0.0, 0.0, 0.0F, 1.0F);
        GlStateManager.disableDepth();

        rm.setRenderShadow(true);

        mc.thePlayer.renderYawOffset = savedYawOffset;
        mc.thePlayer.rotationYaw = savedYaw;
        mc.thePlayer.rotationPitch = savedPitch;
        mc.thePlayer.rotationYawHead = savedYawHead;
        mc.thePlayer.prevRotationYawHead = savedPrevYawHead;

        mc.thePlayer.setInvisible(wasInvisible);

        // Restaura a capa original no manager
        if (originalCape != null) {
            CapeManager.selectCape(originalCape);
        } else {
            CapeManager.clearCape();
        }

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 && selected >= 0) {
            CapeManager.selectCape(capes.get(selected));
            mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            CapeManager.clearCape();
            mc.displayGuiScreen(null);
        }
    }

    public void setSelected(int index) {
        this.selected = index;
    }

    public int getSelected() {
        return selected;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}