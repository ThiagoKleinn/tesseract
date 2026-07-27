package com.tesseract.gui;

import com.tesseract.module.BaseModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Painel draggável do ClickGUI.
 * Cada categoria tem seu próprio GuiPanel.
 *
 * Cores:
 *   MODS      — azul  (#378ADD / #185FA5)
 *   COSMETICS — roxo  (#8A5FCC / #5C3A9E)
 */
public class GuiPanel {

    // Layout
    public static final int WIDTH        = 160;
    public static final int HEADER_H     = 20;
    public static final int MODULE_H     = 18;
    public static final int PADDING      = 8;

    // Cores MODS (azul cósmico)
    private static final int COLOR_MOD_HEADER     = 0xCC185FA5;
    private static final int COLOR_MOD_ACCENT     = 0xFF378ADD;
    private static final int COLOR_MOD_DOT        = 0xFF85B7EB;

    // Cores COSMETICS (roxo)
    private static final int COLOR_COS_HEADER     = 0xCC5C3A9E;
    private static final int COLOR_COS_ACCENT     = 0xFF8A5FCC;
    private static final int COLOR_COS_DOT        = 0xFFC4A0F0;

    // Cores comuns
    private static final int COLOR_BG             = 0xD90A141E;  // fundo escuro quase opaco
    private static final int COLOR_BORDER         = 0x2E78B4FF;  // borda sutil azul
    private static final int COLOR_MODULE_HOVER   = 0x1A378ADD;
    private static final int COLOR_MODULE_TEXT    = 0xFFC8D8F0;
    private static final int COLOR_TOGGLE_OFF     = 0x441E3A5A;
    private static final int COLOR_TOGGLE_BORDER  = 0x663478B4;
    private static final int COLOR_TEXT_TITLE     = 0xFF85B7EB;
    private static final int COLOR_TEXT_TITLE_COS = 0xFFC4A0F0;

    // Estado
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
    // Dimensões

    public int getWidth()  { return WIDTH; }
    public int getHeight() { return HEADER_H + modules.size() * MODULE_H; }

    // -------------------------------------------------------------------------
    // Render

    public void draw(int mouseX, int mouseY) {
        int panelH = getHeight();
        boolean isCosmetics = (category == BaseModule.Category.COSMETICS);

        // Fundo principal
        drawRect(x, y, x + WIDTH, y + panelH, COLOR_BG);

        // Borda fina
        drawBorder(x, y, x + WIDTH, y + panelH, COLOR_BORDER);

        // Header
        int headerColor = isCosmetics ? COLOR_COS_HEADER : COLOR_MOD_HEADER;
        drawRect(x, y, x + WIDTH, y + HEADER_H, headerColor);

        // Dot no header
        int dotColor = isCosmetics ? COLOR_COS_DOT : COLOR_MOD_DOT;
        drawDot(x + PADDING, y + HEADER_H / 2, 4, dotColor);

        // Título
        int titleColor = isCosmetics ? COLOR_TEXT_TITLE_COS : COLOR_TEXT_TITLE;
        Minecraft.getMinecraft().fontRendererObj.drawString(
                title.toUpperCase(),
                x + PADDING + 10,
                y + HEADER_H / 2 - 3,
                titleColor
        );

        // Linha separadora sob o header
        int accentColor = isCosmetics ? COLOR_COS_ACCENT : COLOR_MOD_ACCENT;
        drawRect(x, y + HEADER_H - 1, x + WIDTH, y + HEADER_H, accentColor & 0x44FFFFFF | 0x44000000);

        // Módulos
        for (int i = 0; i < modules.size(); i++) {
            drawModule(i, mouseX, mouseY, isCosmetics);
        }
    }

    private void drawModule(int index, int mouseX, int mouseY, boolean isCosmetics) {
        BaseModule module = modules.get(index);
        int my = y + HEADER_H + index * MODULE_H;

        boolean hovered = mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= my && mouseY <= my + MODULE_H;

        // Hover highlight
        if (hovered) {
            drawRect(x, my, x + WIDTH, my + MODULE_H, COLOR_MODULE_HOVER);
        }

        // Separador entre módulos
        if (index > 0) {
            drawRect(x, my, x + WIDTH, my + 1, 0x0F78B4FF);
        }

        // Nome do módulo
        Minecraft.getMinecraft().fontRendererObj.drawString(
                module.getName(),
                x + PADDING,
                my + MODULE_H / 2 - 3,
                COLOR_MODULE_TEXT
        );

        // Toggle
        drawToggle(x + WIDTH - PADDING - 24, my + MODULE_H / 2 - 5, module.isEnabled(), isCosmetics);
    }

    /**
     * Desenha o toggle switch (ligado/desligado).
     * tw=24, th=10
     */
    private void drawToggle(int tx, int ty, boolean on, boolean isCosmetics) {
        int tw = 24, th = 10;

        if (on) {
            int onColor  = isCosmetics ? COLOR_COS_ACCENT : COLOR_MOD_ACCENT;
            int onBorder = isCosmetics ? 0x80C4A0F0 : 0x8085B7EB;
            drawRect(tx, ty, tx + tw, ty + th, (onColor & 0x00FFFFFF) | 0x55000000);
            drawBorder(tx, ty, tx + tw, ty + th, onBorder);
            // Knob direita
            int knobColor = isCosmetics ? COLOR_COS_DOT : COLOR_MOD_DOT;
            drawDot(tx + tw - 6, ty + th / 2, 4, knobColor | 0xFF000000);
        } else {
            drawRect(tx, ty, tx + tw, ty + th, COLOR_TOGGLE_OFF);
            drawBorder(tx, ty, tx + tw, ty + th, COLOR_TOGGLE_BORDER);
            // Knob esquerda
            drawDot(tx + 6, ty + th / 2, 4, 0xFF3A5A7A);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse

    public void mouseClicked(int mouseX, int mouseY, int button) {
        // Clique no header → começa drag
        if (button == 0 && isOverHeader(mouseX, mouseY)) {
            dragging  = true;
            dragOffX  = mouseX - x;
            dragOffY  = mouseY - y;
            return;
        }

        // Clique em módulo → toggle
        if (button == 0) {
            for (int i = 0; i < modules.size(); i++) {
                int my = y + HEADER_H + i * MODULE_H;
                if (mouseX >= x && mouseX <= x + WIDTH && mouseY >= my && mouseY <= my + MODULE_H) {
                    modules.get(i).toggle();
                    return;
                }
            }
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

    private boolean isOverHeader(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= y && mouseY <= y + HEADER_H;
    }

    public boolean isOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= y && mouseY <= y + getHeight();
    }

    // -------------------------------------------------------------------------
    // Helpers de desenho (sem dependência de RenderHelper extra)

    private void drawRect(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y2, color);
    }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1,     y1,     x2, y1 + 1, color); // top
        Gui.drawRect(x1,     y2 - 1, x2, y2,     color); // bottom
        Gui.drawRect(x1,     y1,     x1 + 1, y2, color); // left
        Gui.drawRect(x2 - 1, y1,     x2, y2,     color); // right
    }

    /** Círculo aproximado via quadrado com bordas arredondadas (GL quads). */
    private void drawDot(int cx, int cy, int r, int color) {
        // Quad simples — funciona bem em 1.8
        Gui.drawRect(cx - r, cy - r, cx + r, cy + r, color);
    }
}