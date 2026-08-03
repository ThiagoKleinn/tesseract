package com.tesseract.gui;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;

/**
 * Wrapper do GuiMultiplayer com fundo cósmico.
 */
public class TesseractMultiplayer extends GuiMultiplayer {

    private final TesseractCosmicScreen cosmic;

    public TesseractMultiplayer(GuiScreen parent) {
        super(parent);
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
        cosmic.drawScreenHeader("MULTIPLAYER");
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}