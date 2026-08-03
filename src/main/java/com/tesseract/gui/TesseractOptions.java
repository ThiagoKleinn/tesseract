package com.tesseract.gui;

import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

/**
 * Wrapper do GuiOptions com fundo cósmico.
 */
public class TesseractOptions extends GuiOptions {

    private final TesseractCosmicScreen cosmic;

    public TesseractOptions(GuiScreen parent) {
        super(parent, net.minecraft.client.Minecraft.getMinecraft().gameSettings);
        cosmic = new TesseractCosmicScreen() {};
    }

    @Override
    public void initGui() {
        super.initGui();
        cosmic.width  = width;
        cosmic.height = height;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        cosmic.width  = width;
        cosmic.height = height;
        cosmic.drawCosmicBase();
        cosmic.drawScreenHeader("OPTIONS");
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}