package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.FloatOption;
import com.tesseract.module.config.ModuleOption;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;

public class KeystrokesModule extends BaseModule implements Configurable {

    private static final int KEY_SIZE = 14;
    private static final int KEY_GAP  = 2;

    private static final int COLOR_PRESSED  = 0xCC55FF55;
    private static final int COLOR_RELEASED = 0x88000000;
    private static final int COLOR_TEXT     = 0xFFFFFFFF;

    private final FloatOption         optScale;
    private final List<ModuleOption<?>> options;

    public KeystrokesModule() {
        super("Keystrokes", "Exibe WASD e cliques do mouse.", Category.MODS);

        optScale = new FloatOption("Escala", 1.0f, 0.5f, 3.0f, 0.25f);
        options  = Arrays.asList(optScale);

        loadConfig();
        setEnabled(true);
    }

    @Override public List<ModuleOption<?>> getOptions() { return options; }
    @Override public void onOptionChanged() { saveConfig(); }

    // -------------------------------------------------------------------------

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        ScaledResolution res   = event.getResolution();
        float            scale = optScale.getValue();

        int ks  = (int)(KEY_SIZE * scale); // tamanho de cada tecla
        int kg  = (int)(KEY_GAP  * scale); // gap entre teclas

        // Largura total = 3 teclas (A S D) + 2 gaps
        int totalW = ks * 3 + kg * 2;
        // Altura total = 4 linhas (W / ASD / SPACE / LMB+RMB) + 3 gaps
        int totalH = ks * 4 + kg * 3;

        // Posição base — canto inferior direito
        int baseX = res.getScaledWidth()  - totalW - 4;
        int baseY = res.getScaledHeight() - totalH - 30;

        KeyBinding kw  = mc.gameSettings.keyBindForward;
        KeyBinding ka  = mc.gameSettings.keyBindLeft;
        KeyBinding ks_ = mc.gameSettings.keyBindBack;
        KeyBinding kd  = mc.gameSettings.keyBindRight;
        KeyBinding ksp = mc.gameSettings.keyBindJump;

        // Linha 1 — W (centralizado)
        drawKey("W", baseX + ks + kg, baseY, ks, kw.isKeyDown(), scale);

        // Linha 2 — A S D
        drawKey("A", baseX,          baseY + ks + kg, ks, ka.isKeyDown(),  scale);
        drawKey("S", baseX + ks + kg, baseY + ks + kg, ks, ks_.isKeyDown(), scale);
        drawKey("D", baseX + ks*2 + kg*2, baseY + ks + kg, ks, kd.isKeyDown(), scale);

        // Linha 3 — SPACE (largura total)
        drawKeyW("_", baseX, baseY + (ks + kg)*2, totalW, ks, ksp.isKeyDown(), scale);

        // Linha 4 — LMB | RMB (cada um = metade do totalW com gap no meio)
        int half = (totalW - kg) / 2;
        drawKeyW("LMB", baseX,          baseY + (ks + kg)*3, half, ks, Mouse.isButtonDown(0), scale);
        drawKeyW("RMB", baseX + half + kg, baseY + (ks + kg)*3, half, ks, Mouse.isButtonDown(1), scale);
    }

    // -------------------------------------------------------------------------

    /** Tecla quadrada (largura = ks). */
    private void drawKey(String label, int x, int y, int ks, boolean pressed, float scale) {
        drawKeyW(label, x, y, ks, ks, pressed, scale);
    }

    /** Tecla com largura customizada. */
    private void drawKeyW(String label, int x, int y, int w, int h, boolean pressed, float scale) {
        int bg = pressed ? COLOR_PRESSED : COLOR_RELEASED;
        RenderUtil.drawRect(x, y, x + w, y + h, bg);
        RenderUtil.drawStringCenteredXY(label, x + w / 2.0f, y + h / 2.0f, COLOR_TEXT);
    }

    // -------------------------------------------------------------------------

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("scale", optScale.getValue());
        Tesseract.instance().getConfigManager().setSection("Keystrokes", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("Keystrokes");
        if (obj != null && obj.has("scale"))
            optScale.setValue(obj.get("scale").getAsFloat());
    }
}