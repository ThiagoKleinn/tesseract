package com.tesseract.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;

/**
 * Wrapper do GuiSelectWorld com fundo cósmico.
 * Injeta o background antes de renderizar a lista vanilla.
 */
public class TesseractWorldSelect extends GuiSelectWorld {

    private final TesseractCosmicScreen cosmic;

    public TesseractWorldSelect(GuiScreen parent) {
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
        cosmic.drawScreenHeader("SINGLEPLAYER");
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}