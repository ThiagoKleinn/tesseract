package com.tesseract.event.events;

import net.minecraft.client.gui.ScaledResolution;

/**
 * Disparado a cada frame do HUD (somente em jogo).
 * Use para desenhar elementos 2D na tela (FPS, keystrokes, armor, etc).
 */
public class EventRender2D {
    private final ScaledResolution resolution;
    private final float partialTicks;

    public EventRender2D(ScaledResolution resolution, float partialTicks) {
        this.resolution = resolution;
        this.partialTicks = partialTicks;
    }

    public ScaledResolution getResolution() { return resolution; }
    public float getPartialTicks() { return partialTicks; }
}