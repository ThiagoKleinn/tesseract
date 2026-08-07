package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.HudComponent;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.Minecraft;

public class FPSModule extends BaseModule implements HudComponent {

    private static final int W = 60;
    private static final int H = 10;

    private int hudX = 2;
    private int hudY = 2;

    public FPSModule() {
        super("FPS", "Exibe os frames por segundo na tela.", Category.MODS);
        setEnabled(true);
        loadConfig();
    }

    // HudComponent

    @Override public int    getHudX()      { return hudX; }
    @Override public int    getHudY()      { return hudY; }
    @Override public int    getHudWidth()  { return W; }
    @Override public int    getHudHeight() { return H; }
    @Override public String getHudLabel()  { return "FPS"; }

    @Override
    public void setHudPos(int x, int y) {
        this.hudX = x;
        this.hudY = y;
    }

    @EventHandler
    public void onRender(EventRender2D event) {
        handleHudDrag(this, event.getResolution());

        int    fps  = Minecraft.getDebugFPS();
        String text = "FPS: " + fps;
        RenderUtil.drawStringWithShadow(text, hudX, hudY, getFPSColor(fps));
    }

    private int getFPSColor(int fps) {
        if (fps >= 100) return 0xFF55FF55;
        if (fps >= 60)  return 0xFFFFFF55;
        return 0xFFFF5555;
    }

    @Override
    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", hudX);
        obj.addProperty("y", hudY);
        Tesseract.instance().getConfigManager().setSection("FPS", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("FPS");
        if (obj == null) return;
        if (obj.has("x")) hudX = obj.get("x").getAsInt();
        if (obj.has("y")) hudY = obj.get("y").getAsInt();
    }
}