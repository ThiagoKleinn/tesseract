package com.tesseract.gui;

import com.tesseract.Tesseract;
import com.tesseract.module.BaseModule;
import com.tesseract.module.modules.ClickGuiModule;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClickGuiScreen — tela principal do ClickGUI do Tesseract.
 *
 * Abre/fecha com RSHIFT (via ClickGuiModule).
 * Fundo: cosmic escuro com estrelas fixas.
 * Painéis draggáveis por categoria (um painel por categoria).
 * Botão "HUD LAYOUT" no header para abrir a tela de posicionamento de HUDs.
 */
public class ClickGuiScreen extends GuiScreen {

    private final List<GuiPanel> panels = new ArrayList<>();

    // Estrelas do fundo (posições relativas 0..999 / 0..699)
    private static final int STAR_COUNT = 60;
    private final int[] starX    = new int[STAR_COUNT];
    private final int[] starY    = new int[STAR_COUNT];
    private final int[] starSize = new int[STAR_COUNT];

    // Botão HUD LAYOUT
    private static final int BTN_W = 80;
    private static final int BTN_H = 14;
    private static final int BTN_MARGIN = 8;

    // -------------------------------------------------------------------------

    public ClickGuiScreen() {
        // Seed fixa = estrelas sempre no mesmo lugar (não dança ao reabrir)
        java.util.Random rng = new java.util.Random(0xC05B1CL);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]    = rng.nextInt(1000);
            starY[i]    = rng.nextInt(700);
            starSize[i] = rng.nextInt(3); // 0 e 1 = 1px, 2 = 2px
        }
    }

    // -------------------------------------------------------------------------

    @Override
    public void initGui() {
        panels.clear();

        // Filtra módulos por categoria, excluindo o próprio ClickGuiModule
        List<BaseModule> mods = filterOut(
                Tesseract.instance().getModuleManager().getModules(BaseModule.Category.MODS),
                ClickGuiModule.class
        );

        List<BaseModule> cosmetics =
                Tesseract.instance().getModuleManager().getModules(BaseModule.Category.COSMETICS);

        // Posicionamento inicial dos painéis
        int startY = height / 5;

        panels.add(new GuiPanel(
                "MODS",
                BaseModule.Category.MODS,
                mods,
                width / 4 - GuiPanel.WIDTH / 2,
                startY
        ));

        panels.add(new GuiPanel(
                "COSMETICS",
                BaseModule.Category.COSMETICS,
                cosmetics,
                width * 3 / 4 - GuiPanel.WIDTH / 2,
                startY
        ));
    }

    // -------------------------------------------------------------------------
    // Render

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawCosmicBackground();
        drawTitle();

        for (GuiPanel panel : panels) {
            panel.draw(mouseX, mouseY);
        }

        drawHudLayoutButton(mouseX, mouseY);
        drawHint();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // -------------------------------------------------------------------------

    private void drawCosmicBackground() {
        // Base azul escuro
        drawRect(0, 0, width, height, 0xFF0A111E);

        // Vinheta no topo
        for (int i = 0; i < 80; i++) {
            int alpha = (int) (0x18 * (1f - i / 80f));
            drawRect(0, i, width, i + 1, (alpha << 24));
        }

        // Estrelas
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = starX[i] * width  / 1000;
            int sy = starY[i] * height / 700;
            int sz = (starSize[i] == 2) ? 2 : 1;

            int brightness;
            int mod3 = i % 3;
            if      (mod3 == 0) brightness = 0xCC;
            else if (mod3 == 1) brightness = 0x88;
            else                brightness = 0x55;

            drawRect(sx, sy, sx + sz, sy + sz, (brightness << 24) | 0x7BA7D4);
        }
    }

    private void drawTitle() {
        String title = "TESSERACT";
        int tw = mc.fontRendererObj.getStringWidth(title);
        int tx = width / 2 - tw / 2;

        // Sombra
        mc.fontRendererObj.drawString(title, tx + 1, 13, 0x22378ADD);
        // Texto principal
        mc.fontRendererObj.drawString(title, tx, 12, 0xCC85B7EB);
    }

    private void drawHudLayoutButton(int mouseX, int mouseY) {
        int bx = width / 2 - BTN_W / 2;
        int by = 28; // logo abaixo do título

        boolean hovered = mouseX >= bx && mouseX <= bx + BTN_W
                && mouseY >= by && mouseY <= by + BTN_H;

        // Fundo
        drawRect(bx, by, bx + BTN_W, by + BTN_H,
                hovered ? 0x55378ADD : 0x33378ADD);
        // Borda
        drawBorder(bx, by, bx + BTN_W, by + BTN_H,
                hovered ? 0xFF85B7EB : 0x6685B7EB);

        // Texto
        String label = "HUD LAYOUT";
        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label,
                bx + BTN_W / 2 - lw / 2,
                by + BTN_H / 2 - 3,
                hovered ? 0xFFFFFFFF : 0xCC85B7EB);
    }

    private void drawHint() {
        String keyLabel  = "RSHIFT";
        String hintLabel = " para abrir / fechar";

        int keyW  = mc.fontRendererObj.getStringWidth(keyLabel) + 8;
        int textW = mc.fontRendererObj.getStringWidth(hintLabel);
        int hx    = width  - keyW - textW - 14;
        int hy    = height - 14;

        // Caixa do atalho
        drawRect(hx, hy - 2, hx + keyW, hy + 10, 0x33378ADD);
        drawBorder(hx, hy - 2, hx + keyW, hy + 10, 0x6685B7EB);
        mc.fontRendererObj.drawString(keyLabel,  hx + 4,        hy + 1, 0xFF85B7EB);
        mc.fontRendererObj.drawString(hintLabel, hx + keyW + 2, hy + 1, 0x8885B7EB);
    }

    // -------------------------------------------------------------------------
    // Mouse

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Verifica clique no botão HUD LAYOUT
        if (mouseButton == 0) {
            int bx = width / 2 - BTN_W / 2;
            int by = 28;
            if (mouseX >= bx && mouseX <= bx + BTN_W
                    && mouseY >= by && mouseY <= by + BTN_H) {
                mc.displayGuiScreen(new HudLayoutScreen());
                return;
            }
        }

        for (GuiPanel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (GuiPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (GuiPanel panel : panels) {
            panel.mouseClickMove(mouseX, mouseY);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    // -------------------------------------------------------------------------
    // Teclado

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Repassa para os painéis primeiro (bind listening tem prioridade)
        for (GuiPanel panel : panels) {
            if (panel.keyTyped(keyCode)) return; // tecla consumida
        }

        if (keyCode == Keyboard.KEY_RSHIFT || keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // -------------------------------------------------------------------------
    // Helpers

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,     y1,     x2,     y1 + 1, color); // top
        drawRect(x1,     y2 - 1, x2,     y2,     color); // bottom
        drawRect(x1,     y1,     x1 + 1, y2,     color); // left
        drawRect(x2 - 1, y1,     x2,     y2,     color); // right
    }

    /**
     * Remove da lista todos os módulos da classe especificada.
     * Usado para esconder o ClickGuiModule do painel MODS.
     */
    private List<BaseModule> filterOut(List<BaseModule> source, Class<? extends BaseModule> exclude) {
        List<BaseModule> result = new ArrayList<>();
        for (BaseModule m : source) {
            if (!exclude.isInstance(m)) result.add(m);
        }
        return result;
    }
}