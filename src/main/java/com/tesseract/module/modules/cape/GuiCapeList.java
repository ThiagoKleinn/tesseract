package com.tesseract.module.modules.cape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.List;

public class GuiCapeList extends GuiSlot {

    private final CapeSelectionGui parent;
    private final List<Cape> capes;
    private final int listWidth;

    public GuiCapeList(CapeSelectionGui parent, List<Cape> capes, int listWidth, int screenHeight) {
        super(Minecraft.getMinecraft(), listWidth, screenHeight, 20, screenHeight - 60, 36);
        this.parent = parent;
        this.capes = capes;
        this.listWidth = listWidth;
        this.left = 0;
        this.right = listWidth;
        this.width = listWidth;
    }

    @Override
    protected int getSize() {
        return capes.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        parent.setSelected(slotIndex);
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return parent.getSelected() == slotIndex;
    }

    @Override
    protected void drawBackground() {}

    @Override
    public int getListWidth() {
        return listWidth - 10;
    }

    @Override
    protected int getScrollBarX() {
        return listWidth - 6;
    }

    @Override
    protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseX, int mouseY) {
        Cape cape = capes.get(entryID);

        mc.getTextureManager().bindTexture(cape.resource);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);

        float u1 = 0.0F / 64.0F;
        float v1 = 0.0F / 32.0F;
        float u2 = 10.0F / 64.0F;
        float v2 = 16.0F / 32.0F;

        int px = x + 4;
        int py = y + 2;
        int pw = 16;
        int ph = 32;

        wr.pos(px,      py + ph, 0.0D).tex(u1, v2).endVertex();
        wr.pos(px + pw, py + ph, 0.0D).tex(u2, v2).endVertex();
        wr.pos(px + pw, py,      0.0D).tex(u2, v1).endVertex();
        wr.pos(px,      py,      0.0D).tex(u1, v1).endVertex();
        tess.draw();

        String name = cape.name;
        if (mc.fontRendererObj.getStringWidth(name) > listWidth - 40) {
            name = mc.fontRendererObj.trimStringToWidth(name, listWidth - 50) + "...";
        }
        mc.fontRendererObj.drawStringWithShadow(name, x + 28, y + 13, 0xFFFFFF);
    }

    @Override
    protected void drawContainerBackground(Tessellator tess) {
        WorldRenderer wr = tess.getWorldRenderer();
        mc.getTextureManager().bindTexture(net.minecraft.client.gui.Gui.optionsBackground);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        wr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        wr.pos(this.left,  this.bottom, 0.0D).tex(this.left  / f, (this.bottom + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        wr.pos(this.right, this.bottom, 0.0D).tex(this.right / f, (this.bottom + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        wr.pos(this.right, this.top,    0.0D).tex(this.right / f, (this.top    + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        wr.pos(this.left,  this.top,    0.0D).tex(this.left  / f, (this.top    + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        tess.draw();
    }

}