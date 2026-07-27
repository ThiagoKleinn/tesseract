package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.Minecraft;

/**
 * FPS Counter — exibe o FPS atual no canto da tela.
 * Posição padrão: canto superior esquerdo.
 */
public class FPSModule extends BaseModule {

    // Posição na tela (futuramente movível pelo HUD Editor)
    private int x = 2;
    private int y = 2;

    public FPSModule() {
        super("FPS", "Exibe os frames por segundo na tela.", Category.MODS);
        setEnabled(true); // ativo por padrão
    }

    @EventHandler
    public void onRender(EventRender2D event) {
        int fps = Minecraft.getDebugFPS();

        // Cor muda conforme o FPS
        int color = getFPSColor(fps);

        String text = "FPS: " + fps;
        RenderUtil.drawStringWithShadow(text, x, y, color);
    }

    private int getFPSColor(int fps) {
        if (fps >= 100) return 0xFF55FF55; // verde
        if (fps >= 60)  return 0xFFFFFF55; // amarelo
        return 0xFFFF5555;                  // vermelho
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}