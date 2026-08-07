package com.tesseract.gui;

import com.tesseract.Tesseract;
import com.tesseract.module.BaseModule;
import com.tesseract.module.HudComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HudLayoutScreen extends GuiScreen {

    private static class HudHandle {
        HudComponent module;
        boolean dragging;
        int dragOffX, dragOffY;

        HudHandle(HudComponent module) {
            this.module = module;
        }
    }

    private final List<HudHandle> handles = new ArrayList<>();

    private static final int COLOR_BG_OVERLAY  = 0xAA0A111E;
    private static final int COLOR_HANDLE_BG   = 0xCC0D1A28;
    private static final int COLOR_HANDLE_BDR  = 0xFF378ADD;
    private static final int COLOR_HANDLE_TEXT = 0xFFC8D8F0;
    private static final int COLOR_TITLE       = 0xCC85B7EB;
    private static final int COLOR_HINT        = 0x8885B7EB;
    private static final int COLOR_HOVER       = 0x33378ADD;

    @Override
    public void initGui() {
        handles.clear();

        for (BaseModule m : Tesseract.instance().getModuleManager().getModules(BaseModule.Category.MODS)) {
            if (m instanceof HudComponent && m.isEnabled()) {
                handles.add(new HudHandle((HudComponent) m));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, COLOR_BG_OVERLAY);

        // Título
        String title = "HUD LAYOUT";
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2 + 1, 13, 0x22378ADD);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2,     12, COLOR_TITLE);

        if (handles.isEmpty()) {
            String msg = "Nenhum HUD ativo para posicionar.";
            int mw = mc.fontRendererObj.getStringWidth(msg);
            mc.fontRendererObj.drawString(msg, width / 2 - mw / 2, height / 2 - 4, COLOR_HINT);
        } else {
            for (HudHandle h : handles) drawHandle(h, mouseX, mouseY);
        }

        String hint = "ESC para fechar e salvar";
        mc.fontRendererObj.drawString(hint, 8, height - 12, COLOR_HINT);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHandle(HudHandle h, int mouseX, int mouseY) {
        HudComponent m  = h.module;
        int x  = m.getHudX();
        int y  = m.getHudY();
        int w  = m.getHudWidth();
        int hh = m.getHudHeight();

        boolean hovered = mouseX >= x && mouseX <= x + w
                && mouseY >= y && mouseY <= y + hh;

        Gui.drawRect(x, y, x + w, y + hh,
                hovered || h.dragging ? COLOR_HOVER : COLOR_HANDLE_BG);
        drawBorder(x, y, x + w, y + hh, COLOR_HANDLE_BDR);

        String label = m.getHudLabel();
        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(
                label,
                x + w / 2 - lw / 2,
                y + hh / 2 - 4,
                COLOR_HANDLE_TEXT
        );
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (HudHandle h : handles) {
                HudComponent m = h.module;
                if (mouseX >= m.getHudX() && mouseX <= m.getHudX() + m.getHudWidth()
                        && mouseY >= m.getHudY() && mouseY <= m.getHudY() + m.getHudHeight()) {
                    h.dragging = true;
                    h.dragOffX = mouseX - m.getHudX();
                    h.dragOffY = mouseY - m.getHudY();
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
                h.module.saveConfig();
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (HudHandle h : handles) {
            if (h.dragging) {
                HudComponent m = h.module;
                int nx = Math.max(0, Math.min(mouseX - h.dragOffX, width  - m.getHudWidth()));
                int ny = Math.max(0, Math.min(mouseY - h.dragOffY, height - m.getHudHeight()));
                m.setHudPos(nx, ny);
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RSHIFT) {
            saveAll();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    private void saveAll() {
        for (HudHandle h : handles) h.module.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1,     y1,     x2,     y1 + 1, color);
        Gui.drawRect(x1,     y2 - 1, x2,     y2,     color);
        Gui.drawRect(x1,     y1,     x1 + 1, y2,     color);
        Gui.drawRect(x2 - 1, y1,     x2,     y2,     color);
    }
}