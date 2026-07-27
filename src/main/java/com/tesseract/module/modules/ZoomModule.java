package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventTick;
import com.tesseract.module.BaseModule;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Zoom — segure a tecla configurada para dar zoom (padrão: V).
 * Scroll do mouse ajusta o nível do zoom enquanto está ativo.
 *
 * Como funciona: manipulamos mc.gameSettings.fovSetting temporariamente.
 */
public class ZoomModule extends BaseModule {

    private float originalFOV;
    private float currentFOV   = 30f;  // FOV com zoom ativo
    private float zoomFOV      = 30f;  // ajustável pelo scroll
    private static final float ZOOM_STEP   = 2f;
    private static final float ZOOM_MIN    = 5f;
    private static final float ZOOM_MAX    = 55f;

    public ZoomModule() {
        super("Zoom", "Diminui o FOV para dar zoom. Scroll para ajustar.", Category.MODS, Keyboard.KEY_V);
    }

    // -------------------------------------------------------------------------

    @Override
    public void onEnable() {
        originalFOV = mc.gameSettings.fovSetting;
        currentFOV  = zoomFOV;
        mc.gameSettings.fovSetting = currentFOV;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.fovSetting = originalFOV;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (event.getPhase() != EventTick.Phase.PRE) return;

        // Verifica se a tecla ainda está pressionada (modo "segurar")
        if (!Keyboard.isKeyDown(getKeybind())) {
            if (isEnabled()) setEnabled(false);
            return;
        }

        // Ajuste de FOV pelo scroll
        int scroll = Mouse.getDWheel();
        if (scroll > 0) {
            zoomFOV = Math.max(ZOOM_MIN, zoomFOV - ZOOM_STEP);
        } else if (scroll < 0) {
            zoomFOV = Math.min(ZOOM_MAX, zoomFOV + ZOOM_STEP);
        }

        mc.gameSettings.fovSetting = zoomFOV;
    }

    // Zoom ativa ao segurar, não ao toggle
    @Override
    public void toggle() {
        // Não faz toggle — é controlado pelo onTick verificando se a tecla está pressionada
        if (!isEnabled()) setEnabled(true);
    }
}