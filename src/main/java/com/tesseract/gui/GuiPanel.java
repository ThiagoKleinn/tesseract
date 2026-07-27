package com.tesseract.gui;

import com.tesseract.module.BaseModule;
import com.tesseract.module.modules.CapeModule;
import com.tesseract.module.modules.cape.CapeSelectionGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class GuiPanel {

    // Layout base
    public static final int WIDTH    = 160;
    public static final int HEADER_H = 20;
    public static final int MODULE_H = 18;
    public static final int PADDING  = 8;

    // Sub-painel de bind
    private static final int BIND_PANEL_H   = 36;
    private static final int BIND_BTN_W     = 60;
    private static final int BIND_BTN_H     = 12;

    // Cores MODS
    private static final int COLOR_MOD_HEADER    = 0xCC185FA5;
    private static final int COLOR_MOD_ACCENT    = 0xFF378ADD;
    private static final int COLOR_MOD_DOT       = 0xFF85B7EB;

    // Cores COSMETICS
    private static final int COLOR_COS_HEADER    = 0xCC5C3A9E;
    private static final int COLOR_COS_ACCENT    = 0xFF8A5FCC;
    private static final int COLOR_COS_DOT       = 0xFFC4A0F0;

    // Cores comuns
    private static final int COLOR_BG            = 0xD90A141E;
    private static final int COLOR_BORDER        = 0x2E78B4FF;
    private static final int COLOR_MODULE_HOVER  = 0x1A378ADD;
    private static final int COLOR_MODULE_TEXT   = 0xFFC8D8F0;
    private static final int COLOR_TOGGLE_OFF    = 0x441E3A5A;
    private static final int COLOR_TOGGLE_BORDER = 0x663478B4;
    private static final int COLOR_TEXT_TITLE    = 0xFF85B7EB;
    private static final int COLOR_TEXT_TITLE_COS= 0xFFC4A0F0;

    // Bind panel cores
    private static final int COLOR_BIND_BG       = 0xEE0D1A28;
    private static final int COLOR_BIND_BORDER   = 0xFF378ADD;
    private static final int COLOR_BIND_BTN      = 0xFF1A3A5C;
    private static final int COLOR_BIND_BTN_ACT  = 0xFF378ADD;  // listening
    private static final int COLOR_BIND_TEXT     = 0xFFC8D8F0;
    private static final int COLOR_BIND_KEY      = 0xFF85B7EB;

    // -------------------------------------------------------------------------

    private final String title;
    private final BaseModule.Category category;
    private final List<BaseModule> modules;

    public int x, y;
    private boolean dragging = false;
    private int dragOffX, dragOffY;

    // -------------------------------------------------------------------------

    public GuiPanel(String title, BaseModule.Category category, List<BaseModule> modules, int x, int y) {
        this.title    = title;
        this.category = category;
        this.modules  = modules;
        this.x        = x;
        this.y        = y;
    }

    // -------------------------------------------------------------------------
    // Dimensões — height cresce quando sub-painéis estão abertos

    public int getWidth() { return WIDTH; }

    public int getHeight() {
        int h = HEADER_H;
        for (BaseModule m : modules) {
            h += MODULE_H;
            if (m.isBindable() && m.isBindPanelOpen()) h += BIND_PANEL_H;
        }
        return h;
    }

    // -------------------------------------------------------------------------
    // Render

    public void draw(int mouseX, int mouseY) {
        boolean isCosmetics = (category == BaseModule.Category.COSMETICS);
        int panelH = getHeight();

        // Fundo + borda
        drawRect(x, y, x + WIDTH, y + panelH, COLOR_BG);
        drawBorder(x, y, x + WIDTH, y + panelH, COLOR_BORDER);

        // Header
        int headerColor = isCosmetics ? COLOR_COS_HEADER : COLOR_MOD_HEADER;
        drawRect(x, y, x + WIDTH, y + HEADER_H, headerColor);

        int dotColor = isCosmetics ? COLOR_COS_DOT : COLOR_MOD_DOT;
        drawDot(x + PADDING, y + HEADER_H / 2, 4, dotColor);

        int titleColor = isCosmetics ? COLOR_TEXT_TITLE_COS : COLOR_TEXT_TITLE;
        Minecraft.getMinecraft().fontRendererObj.drawString(
                title.toUpperCase(),
                x + PADDING + 10,
                y + HEADER_H / 2 - 3,
                titleColor
        );

        int accentColor = isCosmetics ? COLOR_COS_ACCENT : COLOR_MOD_ACCENT;
        drawRect(x, y + HEADER_H - 1, x + WIDTH, y + HEADER_H, accentColor & 0x44FFFFFF | 0x44000000);

        // Módulos
        int curY = y + HEADER_H;
        for (int i = 0; i < modules.size(); i++) {
            curY = drawModule(i, curY, mouseX, mouseY, isCosmetics);
        }
    }

    /**
     * Desenha um módulo na posição curY e retorna o próximo Y disponível.
     */
    private int drawModule(int index, int curY, int mouseX, int mouseY, boolean isCosmetics) {
        BaseModule module = modules.get(index);

        boolean hovered = mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= curY && mouseY <= curY + MODULE_H;

        if (hovered) drawRect(x, curY, x + WIDTH, curY + MODULE_H, COLOR_MODULE_HOVER);
        if (index > 0) drawRect(x, curY, x + WIDTH, curY + 1, 0x0F78B4FF);

        Minecraft.getMinecraft().fontRendererObj.drawString(
                module.getName(),
                x + PADDING,
                curY + MODULE_H / 2 - 3,
                COLOR_MODULE_TEXT
        );

        if (!(module instanceof CapeModule)) {
            drawToggle(x + WIDTH - PADDING - 24, curY + MODULE_H / 2 - 5, module.isEnabled(), isCosmetics);
        }

        curY += MODULE_H;

        // Sub-painel de bind
        if (module.isBindable() && module.isBindPanelOpen()) {
            curY = drawBindPanel(module, curY, mouseX, mouseY, isCosmetics);
        }

        return curY;
    }

    /**
     * Desenha o sub-painel de configuração de keybind.
     * Retorna o próximo Y disponível.
     */
    private int drawBindPanel(BaseModule module, int curY, int mouseX, int mouseY, boolean isCosmetics) {
        int accentColor = isCosmetics ? COLOR_COS_ACCENT : COLOR_MOD_ACCENT;
        int panelY = curY;

        // Fundo do sub-painel
        drawRect(x + 2, panelY, x + WIDTH - 2, panelY + BIND_PANEL_H, COLOR_BIND_BG);
        drawBorder(x + 2, panelY, x + WIDTH - 2, panelY + BIND_PANEL_H, accentColor & 0x66FFFFFF | 0x66000000);

        // Label "BIND:"
        Minecraft.getMinecraft().fontRendererObj.drawString(
                "BIND:",
                x + PADDING + 2,
                panelY + 5,
                COLOR_BIND_TEXT
        );

        // Tecla atual
        String keyName = module.getKeybind() == -1
                ? "NONE"
                : Keyboard.getKeyName(module.getKeybind());
        Minecraft.getMinecraft().fontRendererObj.drawString(
                keyName,
                x + PADDING + 34,
                panelY + 5,
                COLOR_BIND_KEY
        );

        // Botão "SET KEY" ou "LISTENING..."
        int btnX = x + PADDING + 2;
        int btnY = panelY + 18;
        boolean listening = module.isListeningForKey();
        int btnColor = listening ? COLOR_BIND_BTN_ACT : COLOR_BIND_BTN;
        String btnLabel = listening ? "LISTENING..." : "SET KEY";

        drawRect(btnX, btnY, btnX + BIND_BTN_W, btnY + BIND_BTN_H, btnColor);
        drawBorder(btnX, btnY, btnX + BIND_BTN_W, btnY + BIND_BTN_H,
                accentColor & 0x99FFFFFF | 0x99000000);
        int labelX = btnX + BIND_BTN_W / 2
                - Minecraft.getMinecraft().fontRendererObj.getStringWidth(btnLabel) / 2;
        Minecraft.getMinecraft().fontRendererObj.drawString(btnLabel, labelX, btnY + 2, COLOR_BIND_TEXT);

        // Botão "CLEAR"
        int clearX = btnX + BIND_BTN_W + 6;
        drawRect(clearX, btnY, clearX + 30, btnY + BIND_BTN_H, COLOR_BIND_BTN);
        drawBorder(clearX, btnY, clearX + 30, btnY + BIND_BTN_H,
                accentColor & 0x99FFFFFF | 0x99000000);
        int clearLabelX = clearX + 15
                - Minecraft.getMinecraft().fontRendererObj.getStringWidth("CLEAR") / 2;
        Minecraft.getMinecraft().fontRendererObj.drawString("CLEAR", clearLabelX, btnY + 2, COLOR_BIND_TEXT);

        return panelY + BIND_PANEL_H;
    }

    private void drawToggle(int tx, int ty, boolean on, boolean isCosmetics) {
        int tw = 24, th = 10;
        if (on) {
            int onColor  = isCosmetics ? COLOR_COS_ACCENT : COLOR_MOD_ACCENT;
            int onBorder = isCosmetics ? 0x80C4A0F0 : 0x8085B7EB;
            drawRect(tx, ty, tx + tw, ty + th, (onColor & 0x00FFFFFF) | 0x55000000);
            drawBorder(tx, ty, tx + tw, ty + th, onBorder);
            int knobColor = isCosmetics ? COLOR_COS_DOT : COLOR_MOD_DOT;
            drawDot(tx + tw - 6, ty + th / 2, 4, knobColor | 0xFF000000);
        } else {
            drawRect(tx, ty, tx + tw, ty + th, COLOR_TOGGLE_OFF);
            drawBorder(tx, ty, tx + tw, ty + th, COLOR_TOGGLE_BORDER);
            drawDot(tx + 6, ty + th / 2, 4, 0xFF3A5A7A);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse

    public void mouseClicked(int mouseX, int mouseY, int button) {
        // Header → drag
        if (button == 0 && isOverHeader(mouseX, mouseY)) {
            dragging = true;
            dragOffX = mouseX - x;
            dragOffY = mouseY - y;
            return;
        }

        int curY = y + HEADER_H;
        for (BaseModule module : modules) {
            int moduleY = curY;

            // Clique na linha do módulo
            if (mouseX >= x && mouseX <= x + WIDTH
                    && mouseY >= moduleY && mouseY <= moduleY + MODULE_H) {

                if (module instanceof CapeModule) {
                    Minecraft.getMinecraft().displayGuiScreen(new CapeSelectionGui());
                } else if (button == 0) {
                    // Clique esquerdo: toggle
                    // Fecha bind panel se estava aberto
                    if (module.isBindable() && module.isBindPanelOpen()) {
                        module.closeBindPanel();
                    } else {
                        module.toggle();
                    }
                } else if (button == 1 && module.isBindable()) {
                    // Clique direito: abre/fecha sub-painel de bind
                    if (module.isBindPanelOpen()) {
                        module.closeBindPanel();
                    } else {
                        // Fecha outros bind panels abertos
                        closeAllBindPanels();
                        module.openBindPanel();
                    }
                }
                return;
            }

            curY += MODULE_H;

            // Clique dentro do sub-painel de bind
            if (module.isBindable() && module.isBindPanelOpen()) {
                int bindPanelY = curY;
                if (mouseX >= x && mouseX <= x + WIDTH
                        && mouseY >= bindPanelY && mouseY <= bindPanelY + BIND_PANEL_H) {
                    handleBindPanelClick(module, mouseX, mouseY, bindPanelY);
                    return;
                }
                curY += BIND_PANEL_H;
            }
        }
    }

    private void handleBindPanelClick(BaseModule module, int mouseX, int mouseY, int panelY) {
        int btnX  = x + PADDING + 2;
        int btnY  = panelY + 18;

        // Botão SET KEY
        if (mouseX >= btnX && mouseX <= btnX + BIND_BTN_W
                && mouseY >= btnY && mouseY <= btnY + BIND_BTN_H) {
            module.startListening();
            return;
        }

        // Botão CLEAR
        int clearX = btnX + BIND_BTN_W + 6;
        if (mouseX >= clearX && mouseX <= clearX + 30
                && mouseY >= btnY && mouseY <= btnY + BIND_BTN_H) {
            module.setKeybind(-1);
            module.closeBindPanel();
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) dragging = false;
    }

    public void mouseClickMove(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - dragOffX;
            y = mouseY - dragOffY;
        }
    }

    // -------------------------------------------------------------------------
    // Teclado — chamado pelo ClickGuiScreen

    /**
     * Processa tecla pressionada. Retorna true se algum módulo estava em modo listening
     * e consumiu a tecla (para evitar que o GuiScreen feche).
     */
    public boolean keyTyped(int keyCode) {
        for (BaseModule module : modules) {
            if (module.isBindable() && module.isListeningForKey()) {
                module.onKeyReceived(keyCode);
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------

    private void closeAllBindPanels() {
        for (BaseModule m : modules) {
            if (m.isBindable() && m.isBindPanelOpen()) {
                m.closeBindPanel();
            }
        }
    }

    private boolean isOverHeader(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= y && mouseY <= y + HEADER_H;
    }

    public boolean isOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= y && mouseY <= y + getHeight();
    }

    // -------------------------------------------------------------------------

    private void drawRect(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y2, color);
    }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1,     y1,     x2,     y1 + 1, color);
        Gui.drawRect(x1,     y2 - 1, x2,     y2,     color);
        Gui.drawRect(x1,     y1,     x1 + 1, y2,     color);
        Gui.drawRect(x2 - 1, y1,     x2,     y2,     color);
    }

    private void drawDot(int cx, int cy, int r, int color) {
        Gui.drawRect(cx - r, cy - r, cx + r, cy + r, color);
    }
}