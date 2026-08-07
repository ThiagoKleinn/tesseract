package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.HudComponent;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.CycleOption;
import com.tesseract.module.config.FloatOption;
import com.tesseract.module.config.ModuleOption;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;

public class KeystrokesModule extends BaseModule implements Configurable, HudComponent {

    public enum KeyColor {
        AZUL_COSMICO, VERDE, VERMELHO, ROXO, AMARELO, BRANCO
    }

    private static final int KEY_SIZE = 14;
    private static final int KEY_GAP  = 2;

    private static final int COLOR_RELEASED = 0x88000000;
    private static final int COLOR_TEXT     = 0xFFFFFFFF;

    private final FloatOption           optScale;
    private final CycleOption<KeyColor> optColor;
    private final List<ModuleOption<?>> options;

    private int hudX = 0;
    private int hudY = 0;
    private boolean hudPosInitialized = false;

    public KeystrokesModule() {
        super("Keystrokes", "Exibe WASD e cliques do mouse.", Category.MODS);

        optScale = new FloatOption("Escala", 1.0f, 0.5f, 3.0f, 0.25f);
        optColor = new CycleOption<>("Cor", Arrays.asList(KeyColor.values()), KeyColor.AZUL_COSMICO);
        options  = Arrays.asList(optScale, optColor);

        loadConfig();
        setEnabled(true);
    }

    @Override public List<ModuleOption<?>> getOptions() { return options; }
    @Override public void onOptionChanged() { saveConfig(); }

    // -------------------------------------------------------------------------
    // HudComponent

    @Override public int    getHudX()      { return hudX; }
    @Override public int    getHudY()      { return hudY; }
    @Override public String getHudLabel()  { return "Keystrokes"; }

    @Override
    public int getHudWidth() {
        float scale = optScale.getValue();
        int ks = (int)(KEY_SIZE * scale);
        int kg = (int)(KEY_GAP  * scale);
        return ks * 3 + kg * 2;
    }

    @Override
    public int getHudHeight() {
        float scale = optScale.getValue();
        int ks = (int)(KEY_SIZE * scale);
        int kg = (int)(KEY_GAP  * scale);
        return ks * 4 + kg * 3;
    }

    @Override
    public void setHudPos(int x, int y) {
        this.hudX = x;
        this.hudY = y;
    }

    // -------------------------------------------------------------------------
    // Render

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        // Inicializa posição padrão (canto inferior direito) na primeira vez
        if (!hudPosInitialized) {
            ScaledResolution res = event.getResolution();
            hudX = res.getScaledWidth()  - getHudWidth()  - 4;
            hudY = res.getScaledHeight() - getHudHeight() - 30;
            hudPosInitialized = true;
        }

        handleHudDrag(this, event.getResolution());

        float scale = optScale.getValue();
        int ks = (int)(KEY_SIZE * scale);
        int kg = (int)(KEY_GAP  * scale);
        int totalW = getHudWidth();

        KeyBinding kw  = mc.gameSettings.keyBindForward;
        KeyBinding ka  = mc.gameSettings.keyBindLeft;
        KeyBinding kss = mc.gameSettings.keyBindBack;
        KeyBinding kd  = mc.gameSettings.keyBindRight;
        KeyBinding ksp = mc.gameSettings.keyBindJump;

        // Linha 1 — W
        drawKey("W", hudX + ks + kg, hudY, ks, kw.isKeyDown(), scale);

        // Linha 2 — A S D
        drawKey("A", hudX,               hudY + ks + kg, ks, ka.isKeyDown(),  scale);
        drawKey("S", hudX + ks + kg,     hudY + ks + kg, ks, kss.isKeyDown(), scale);
        drawKey("D", hudX + ks*2 + kg*2, hudY + ks + kg, ks, kd.isKeyDown(),  scale);

        // Linha 3 — SPACE
        drawKeyW("_", hudX, hudY + (ks + kg)*2, totalW, ks, ksp.isKeyDown(), scale);

        // Linha 4 — LMB | RMB
        int half = (totalW - kg) / 2;
        drawKeyW("LMB", hudX,             hudY + (ks + kg)*3, half, ks, Mouse.isButtonDown(0), scale);
        drawKeyW("RMB", hudX + half + kg, hudY + (ks + kg)*3, half, ks, Mouse.isButtonDown(1), scale);
    }

    // -------------------------------------------------------------------------

    private void drawKey(String label, int x, int y, int ks, boolean pressed, float scale) {
        drawKeyW(label, x, y, ks, ks, pressed, scale);
    }

    private void drawKeyW(String label, int x, int y, int w, int h, boolean pressed, float scale) {
        int bg = pressed ? getPressedColor() : COLOR_RELEASED;
        RenderUtil.drawRect(x, y, x + w, y + h, bg);
        RenderUtil.drawStringCenteredXY(label, x + w / 2.0f, y + h / 2.0f, COLOR_TEXT);
    }

    private int getPressedColor() {
        switch (optColor.getValue()) {
            case AZUL_COSMICO: return 0xCC5BA3DC;
            case VERDE:        return 0xCC55FF55;
            case VERMELHO:     return 0xCCFF5555;
            case ROXO:         return 0xCCAA55FF;
            case AMARELO:      return 0xCCFFFF55;
            case BRANCO:       return 0xCCFFFFFF;
            default:           return 0xCC5BA3DC;
        }
    }

    // -------------------------------------------------------------------------
    // Config

    @Override
    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x",     hudX);
        obj.addProperty("y",     hudY);
        obj.addProperty("scale", optScale.getValue());
        obj.addProperty("color", optColor.getValue().name());
        Tesseract.instance().getConfigManager().setSection("Keystrokes", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("Keystrokes");
        if (obj == null) return;
        if (obj.has("x"))     { hudX = obj.get("x").getAsInt(); hudPosInitialized = true; }
        if (obj.has("y"))     { hudY = obj.get("y").getAsInt(); }
        if (obj.has("scale")) optScale.setValue(obj.get("scale").getAsFloat());
        if (obj.has("color")) optColor.setValue(KeyColor.valueOf(obj.get("color").getAsString()));
    }
}