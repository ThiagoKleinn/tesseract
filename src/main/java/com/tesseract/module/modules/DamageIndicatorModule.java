package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.ModuleOption;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

import java.util.Collections;
import java.util.List;

public class DamageIndicatorModule extends BaseModule implements Configurable {

    // Dimensões
    private static final int W   = 100;
    private static final int H   = 28;
    private static final int PAD = 5;

    // Cores
    private static final int COLOR_BG     = 0xAA0A111E;
    private static final int COLOR_BORDER = 0xFF5BA3DC;
    private static final int COLOR_NAME   = 0xFFFFFFFF;
    private static final int COLOR_HP_CUR = 0xFFFF5555;
    private static final int COLOR_HP_MAX = 0xFFAAAAAA;

    // Posição (drag)
    private int hudX = 10;
    private int hudY = 10;

    // Drag state
    private boolean dragging  = false;
    private int     dragOffX  = 0;
    private int     dragOffY  = 0;
    private boolean rmbWasDown = false;

    public DamageIndicatorModule() {
        super("DamageIndicator", "Exibe vida e nome do alvo.", Category.MODS);
        loadConfig();
    }

    // Configurable — sem opções visuais, só para aparecer no painel
    @Override public List<ModuleOption<?>> getOptions() { return Collections.emptyList(); }
    @Override public void onOptionChanged() {}

    // -------------------------------------------------------------------------

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        handleDrag(event.getResolution());

        EntityLivingBase target = getTarget();
        if (target == null) return;

        int x = hudX, y = hudY;

        String name   = getTargetName(target);
        int    hpCur  = (int) target.getHealth();
        int    hpMax  = (int) target.getMaxHealth();

        // Borda + fundo
        RenderUtil.drawRect(x - 1, y - 1, x + W + 1, y + H + 1, COLOR_BORDER);
        RenderUtil.drawRect(x,     y,      x + W,     y + H,     COLOR_BG);

        // Linha 1 — nome
        String nameLabel = "[" + name + "]";
        RenderUtil.drawStringWithShadow(nameLabel, x + PAD, y + PAD, COLOR_NAME);

        // Linha 2 — vida atual ♥ / vida máx ♥
        String hpCurStr = hpCur + " \u2665 ";
        String slash    = "/ ";
        String hpMaxStr = hpMax + " \u2665";

        float cx   = x + PAD;
        float lineY = y + PAD + mc.fontRendererObj.FONT_HEIGHT + 2;

        RenderUtil.drawStringWithShadow(hpCurStr, cx, lineY, COLOR_HP_CUR);
        cx += mc.fontRendererObj.getStringWidth(hpCurStr);

        RenderUtil.drawStringWithShadow(slash, cx, lineY, COLOR_NAME);
        cx += mc.fontRendererObj.getStringWidth(slash);

        RenderUtil.drawStringWithShadow(hpMaxStr, cx, lineY, COLOR_HP_MAX);
    }

    // -------------------------------------------------------------------------
    // Drag — botão direito sobre o HUD arrasta, solta salva

    private void handleDrag(ScaledResolution res) {
        // Coordenadas do mouse em scaled pixels
        int sw      = res.getScaledWidth();
        int sh      = res.getScaledHeight();
        int mouseX  = (int)(Mouse.getX() * sw  / (double) mc.displayWidth);
        int mouseY  = (int)((mc.displayHeight - Mouse.getY() - 1) * sh / (double) mc.displayHeight);

        boolean rmb = Mouse.isButtonDown(1);

        if (rmb && !rmbWasDown) {
            // Botão direito acabou de ser pressionado
            if (mouseX >= hudX && mouseX <= hudX + W
                    && mouseY >= hudY && mouseY <= hudY + H) {
                dragging = true;
                dragOffX = mouseX - hudX;
                dragOffY = mouseY - hudY;
            }
        }

        if (!rmb && rmbWasDown && dragging) {
            // Soltou — salva posição
            dragging = false;
            saveConfig();
        }

        if (dragging && rmb) {
            hudX = mouseX - dragOffX;
            hudY = mouseY - dragOffY;

            // Clamp dentro da tela
            hudX = Math.max(0, Math.min(hudX, sw - W));
            hudY = Math.max(0, Math.min(hudY, sh - H));
        }

        rmbWasDown = rmb;
    }

    // -------------------------------------------------------------------------

    private EntityLivingBase getTarget() {
        if (mc.pointedEntity instanceof EntityLivingBase) {
            return (EntityLivingBase) mc.pointedEntity;
        }
        EntityLivingBase closest = null;
        double minDist = Double.MAX_VALUE;
        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) obj;
            if (e == mc.thePlayer || e.getHealth() <= 0) continue;
            double dist = mc.thePlayer.getDistanceToEntity(e);
            if (dist < 6.0 && dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
        return closest;
    }

    private String getTargetName(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) return entity.getName();
        String raw = entity.getName();
        return raw.isEmpty() ? raw : Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    // -------------------------------------------------------------------------

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", hudX);
        obj.addProperty("y", hudY);
        Tesseract.instance().getConfigManager().setSection("TargetHUD", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("TargetHUD");
        if (obj == null) return;
        if (obj.has("x")) hudX = obj.get("x").getAsInt();
        if (obj.has("y")) hudY = obj.get("y").getAsInt();
    }
}