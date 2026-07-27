package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventTick;
import com.tesseract.module.BaseModule;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ZoomModule extends BaseModule {

    private float originalFOV = 70f;
    private float zoomFOV     = 30f;

    private static final float ZOOM_STEP = 2f;
    private static final float ZOOM_MIN  = 5f;
    private static final float ZOOM_MAX  = 55f;

    public ZoomModule() {
        super("Zoom", "Toggle de zoom. Scroll para ajustar o nível.", Category.MODS, Keyboard.KEY_V);
    }

    @Override
    public boolean isBindable() { return true; }

    // -------------------------------------------------------------------------

    @Override
    public void onEnable() {
        originalFOV = mc.gameSettings.fovSetting;
        mc.gameSettings.fovSetting = zoomFOV;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.fovSetting = originalFOV;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!isEnabled() || event.getPhase() != EventTick.Phase.PRE) return;

        if (mc.gameSettings.fovSetting != zoomFOV) {
            mc.gameSettings.fovSetting = zoomFOV;
        }

        int scroll = Mouse.getDWheel();
        if (scroll > 0) {
            zoomFOV = Math.max(ZOOM_MIN, zoomFOV - ZOOM_STEP);
            mc.gameSettings.fovSetting = zoomFOV;
        } else if (scroll < 0) {
            zoomFOV = Math.min(ZOOM_MAX, zoomFOV + ZOOM_STEP);
            mc.gameSettings.fovSetting = zoomFOV;
        }
    }
}