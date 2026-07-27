package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

/**
 * Keystrokes — exibe as teclas WASD + LMB/RMB em tempo real.
 * Estilo clássico de clients PvP.
 */
public class KeystrokesModule extends BaseModule {

    private static final int KEY_SIZE   = 14;
    private static final int KEY_GAP    = 2;
    private static final int KEY_RADIUS = 2; // arredondamento (futuro)

    // Cores
    private static final int COLOR_PRESSED  = 0xCC55FF55; // verde semi-transparente
    private static final int COLOR_RELEASED = 0x88000000; // preto semi-transparente
    private static final int COLOR_TEXT     = 0xFFFFFFFF;

    public KeystrokesModule() {
        super("Keystrokes", "Exibe WASD e cliques do mouse.", Category.MODS);
        setEnabled(true);
    }

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        ScaledResolution res = event.getResolution();

        // Posição base — canto inferior direito
        int baseX = res.getScaledWidth()  - (KEY_SIZE * 3 + KEY_GAP * 2) - 4;
        int baseY = res.getScaledHeight() - (KEY_SIZE * 4 + KEY_GAP * 3) - 30;

        KeyBinding kw = mc.gameSettings.keyBindForward;
        KeyBinding ks = mc.gameSettings.keyBindBack;
        KeyBinding ka = mc.gameSettings.keyBindLeft;
        KeyBinding kd = mc.gameSettings.keyBindRight;
        KeyBinding ksp = mc.gameSettings.keyBindJump;

        // Layout:
        //    [W]
        //  [A][S][D]
        //   [SPACE]
        //  [LMB][RMB]

        drawKey("W",  baseX + KEY_SIZE + KEY_GAP,     baseY,                                  kw.isKeyDown());
        drawKey("A",  baseX,                           baseY + KEY_SIZE + KEY_GAP,             ka.isKeyDown());
        drawKey("S",  baseX + KEY_SIZE + KEY_GAP,     baseY + KEY_SIZE + KEY_GAP,             ks.isKeyDown());
        drawKey("D",  baseX + (KEY_SIZE + KEY_GAP)*2, baseY + KEY_SIZE + KEY_GAP,             kd.isKeyDown());
        drawKey("_",  baseX,                           baseY + (KEY_SIZE + KEY_GAP)*2,         ksp.isKeyDown()); // espaço

        boolean lmb = Mouse.isButtonDown(0);
        boolean rmb = Mouse.isButtonDown(1);
        drawKey("LMB", baseX,                          baseY + (KEY_SIZE + KEY_GAP)*3,        lmb);
        drawKey("RMB", baseX + KEY_SIZE + KEY_GAP,     baseY + (KEY_SIZE + KEY_GAP)*3,        rmb);
    }

    private void drawKey(String label, int x, int y, boolean pressed) {
        int bg = pressed ? COLOR_PRESSED : COLOR_RELEASED;
        RenderUtil.drawRect(x, y, x + KEY_SIZE, y + KEY_SIZE, bg);
        RenderUtil.drawStringCenteredXY(label, x + KEY_SIZE / 2.0f, y + KEY_SIZE / 2.0f, COLOR_TEXT);
    }
}