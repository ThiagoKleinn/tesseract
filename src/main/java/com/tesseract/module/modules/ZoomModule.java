package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRenderTick;
import com.tesseract.event.events.EventTick;
import com.tesseract.module.BaseModule;
import org.lwjgl.input.Keyboard;

public class ZoomModule extends BaseModule {

    private float originalFOV  = 70f;
    private float originalSens = 100f;
    private float currentFOV   = 70f;

    private static final float ZOOM_FOV     = 15f;
    private static final float SMOOTH_SPEED = 0.15f;

    private boolean zooming = false;
    private boolean skipFirst   = true;

    public ZoomModule() {
        super("Zoom", "Zoom fixo. Segure a bind para dar zoom.", Category.MODS, Keyboard.KEY_NONE);
    }

    @Override
    public boolean isBindable() { return true; }

    @Override
    public boolean isToggleByKey() { return false; }

    @Override
    public void onEnable() {
        originalFOV  = mc.gameSettings.fovSetting;
        originalSens = mc.gameSettings.mouseSensitivity;
        currentFOV   = originalFOV;
        skipFirst    = true;
    }

    @Override
    public void onDisable() {
        zooming = false;
        mc.gameSettings.mouseSensitivity = originalSens;
        currentFOV = originalFOV;
        mc.gameSettings.fovSetting = originalFOV;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (event.getPhase() != EventTick.Phase.PRE) return;
        if (!isEnabled()) return;

        // Verifica se a bind está sendo segurada
        int key = getKeybind();
        boolean holding = key != Keyboard.KEY_NONE && Keyboard.isKeyDown(key);

        // Ignora o primeiro tick (a tecla que ativou o módulo ainda está pressionada)
        if (skipFirst) {
            if (!holding) skipFirst = false; // espera soltar a tecla antes de começar
            return;
        }

        if (holding && !zooming) {
            // Começou a segurar
            zooming = true;
            originalFOV  = mc.gameSettings.fovSetting;
            originalSens = mc.gameSettings.mouseSensitivity;

            // Sensibilidade proporcional ao zoom
            float zoomFactor = originalFOV / ZOOM_FOV;
            mc.gameSettings.mouseSensitivity = originalSens / zoomFactor;

        } else if (!holding && zooming) {
            // Soltou
            zooming = false;
            mc.gameSettings.mouseSensitivity = originalSens;
        }
    }

    @EventHandler
    public void onRenderTick(EventRenderTick event) {
        if (!isEnabled()) return;

        float targetFOV = zooming ? ZOOM_FOV : originalFOV;
        currentFOV += (targetFOV - currentFOV) * SMOOTH_SPEED;

        if (Math.abs(currentFOV - targetFOV) < 0.05f) {
            currentFOV = targetFOV;
        }

        mc.gameSettings.fovSetting = currentFOV;
    }
}