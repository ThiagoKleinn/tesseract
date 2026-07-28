package com.tesseract.gui;

import com.tesseract.Tesseract;
import com.tesseract.module.BaseModule;
import com.tesseract.module.modules.DamageIndicatorModule;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HudLayoutScreen — permite arrastar os HUDs ativos pela tela.
 * Acessível via botão no ClickGuiScreen.
 */
public class HudLayoutScreen extends GuiScreen {

    // Representa um HUD arrastável nessa tela
    private static class HudHandle {
        String label;
        int x, y, w, h;
        boolean dragging;
        int dragOffX, dragOffY;
        Runnable onMove; // callback chamado quando a posição muda

        HudHandle(String label, int x, int y, int w, int h, Runnable onMove) {
            this.label  = label;
            this.x      = x;
            this.y      = y;
            this.w      = w;
            this.h      = h;
            this.onMove = onMove;
        }
    }

    private final List<HudHandle> handles = new ArrayList<>();

    // Referências aos módulos com HUD
    private DamageIndicatorModule damageIndicator;

    // Cores
    private static final int COLOR_BG_OVERLAY  = 0xAA0A111E;
    private static final int COLOR_HANDLE_BG   = 0xCC0D1A28;
    private static final int COLOR_HANDLE_BDR  = 0xFF378ADD;
    private static final int COLOR_HANDLE_TEXT = 0xFFC8D8F0;
    private static final int COLOR_TITLE       = 0xCC85B7EB;
    private static final int COLOR_HINT        = 0x8885B7EB;
    private static final int COLOR_HOVER       = 0x33378ADD;

    // -------------------------------------------------------------------------

    @Override
    public void initGui() {
        handles.clear();

        // Coleta DamageIndicatorModule se estiver ativo
        for (BaseModule m : Tesseract.instance().getModuleManager().getModules(BaseModule.Category.MODS)) {
            if (m instanceof DamageIndicatorModule && m.isEnabled()) {
                damageIndicator = (DamageIndicatorModule) m;
            }
        }

        if (damageIndicator != null) {
            final int W = 100, H = 28;
            handles.add(new HudHandle(
                    "Damage Indicator",
                    damageIndicator.getHudX(),
                    damageIndicator.getHudY(),
                    W, H,
                    () -> {
                        HudHandle h = handleByLabel("Damage Indicator");
                        if (h != null) {
                            damageIndicator.setHudPos(h.x, h.y);
                        }
                    }
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Render

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Fundo semi-transparente (mostra o jogo atrás)
        drawRect(0, 0, width, height, COLOR_BG_OVERLAY);

        // Título
        String title = "HUD LAYOUT";
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2 + 1, 13, 0x22378ADD);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2,     12, COLOR_TITLE);

        // Handles
        if (handles.isEmpty()) {
            String msg = "Nenhum HUD ativo para posicionar.";
            int mw = mc.fontRendererObj.getStringWidth(msg);
            mc.fontRendererObj.drawString(msg, width / 2 - mw / 2, height / 2 - 4, COLOR_HINT);
        } else {
            for (HudHandle h : handles) {
                drawHandle(h, mouseX, mouseY);
            }
        }

        // Dica de fechar
        String hint = "ESC para fechar e salvar";
        mc.fontRendererObj.drawString(hint, 8, height - 12, COLOR_HINT);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHandle(HudHandle h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= h.x && mouseX <= h.x + h.w
                && mouseY >= h.y && mouseY <= h.y + h.h;

        // Fundo
        Gui.drawRect(h.x, h.y, h.x + h.w, h.y + h.h,
                hovered || h.dragging ? COLOR_HOVER : COLOR_HANDLE_BG);

        // Borda
        drawBorder(h.x, h.y, h.x + h.w, h.y + h.h, COLOR_HANDLE_BDR);

        // Label centrado
        int lw = mc.fontRendererObj.getStringWidth(h.label);
        mc.fontRendererObj.drawString(
                h.label,
                h.x + h.w / 2 - lw / 2,
                h.y + h.h / 2 - 4,
                COLOR_HANDLE_TEXT
        );
    }

    // -------------------------------------------------------------------------
    // Mouse

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (HudHandle h : handles) {
                if (mouseX >= h.x && mouseX <= h.x + h.w
                        && mouseY >= h.y && mouseY <= h.y + h.h) {
                    h.dragging = true;
                    h.dragOffX = mouseX - h.x;
                    h.dragOffY = mouseY - h.y;
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (HudHandle h : handles) {
            if (h.dragging) {
                h.dragging = false;
                h.onMove.run(); // persiste a posição
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (HudHandle h : handles) {
            if (h.dragging) {
                h.x = Math.max(0, Math.min(mouseX - h.dragOffX, width  - h.w));
                h.y = Math.max(0, Math.min(mouseY - h.dragOffY, height - h.h));
                h.onMove.run();
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    // -------------------------------------------------------------------------
    // Teclado

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RSHIFT) {
            saveAll();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    // -------------------------------------------------------------------------

    private void saveAll() {
        if (damageIndicator != null) {
            damageIndicator.saveConfig();
        }
    }

    private HudHandle handleByLabel(String label) {
        for (HudHandle h : handles) {
            if (h.label.equals(label)) return h;
        }
        return null;
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    // -------------------------------------------------------------------------

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1,     y1,     x2,     y1 + 1, color);
        Gui.drawRect(x1,     y2 - 1, x2,     y2,     color);
        Gui.drawRect(x1,     y1,     x1 + 1, y2,     color);
        Gui.drawRect(x2 - 1, y1,     x2,     y2,     color);
    }
}