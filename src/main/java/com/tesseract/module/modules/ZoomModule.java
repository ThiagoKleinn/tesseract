package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRenderTick;
import com.tesseract.event.events.EventTick;
import com.tesseract.module.BaseModule;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ZoomModule extends BaseModule {

    private float originalFOV     = 70f;
    private float zoomFOV         = 15f;
    private float currentFOV      = 70f;
    private float originalSens    = 100f;

    private static final float ZOOM_STEP    = 2f;
    private static final float ZOOM_MIN     = 5f;
    private static final float ZOOM_MAX     = 55f;
    private static final float SMOOTH_SPEED = 0.15f;

    public ZoomModule() {
        super("Zoom", "Zoom suave igual OptiFine. Scroll para ajustar.", Category.MODS, Keyboard.KEY_V);
    }

    @Override
    public boolean isBindable() { return true; }

    @Override
    public void onEnable() {
        originalFOV  = mc.gameSettings.fovSetting;
        originalSens = mc.gameSettings.mouseSensitivity;
        currentFOV   = originalFOV;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.mouseSensitivity = originalSens;
    }

    // Scroll e sensibilidade no game tick (20x/s é suficiente)
    @EventHandler
    public void onTick(EventTick event) {
        if (event.getPhase() != EventTick.Phase.PRE) return;

        if (isEnabled() && mc.currentScreen == null) {
            // Scroll para ajustar zoom
            int scroll = Mouse.getDWheel();
            if (scroll > 0) zoomFOV = Math.max(ZOOM_MIN, zoomFOV - ZOOM_STEP);
            else if (scroll < 0) zoomFOV = Math.min(ZOOM_MAX, zoomFOV + ZOOM_STEP);

            // Sensibilidade proporcional ao zoom (igual OptiFine)
            float zoomFactor = originalFOV / zoomFOV;
            mc.gameSettings.mouseSensitivity = originalSens / zoomFactor;
        } else if (!isEnabled()) {
            mc.gameSettings.mouseSensitivity = originalSens;
        }
    }

    // Suavização do FOV no render tick (60fps+)
    @EventHandler
    public void onRenderTick(EventRenderTick event) {
        float targetFOV = isEnabled() ? zoomFOV : originalFOV;

        // Lerp suave
        currentFOV += (targetFOV - currentFOV) * SMOOTH_SPEED;

        // Snap final para evitar drift infinito
        if (Math.abs(currentFOV - targetFOV) < 0.05f) {
            currentFOV = targetFOV;
        }

        mc.gameSettings.fovSetting = currentFOV;
    }
}