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
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

public class DamageIndicatorModule extends BaseModule implements Configurable {

    private static final int W   = 100;
    private static final int H   = 28;
    private static final int PAD = 5;

    private static final int COLOR_NAME   = 0xFFFFFFFF;
    private static final int COLOR_HP_CUR = 0xFFAA0000;

    private int hudX = 10;
    private int hudY = 10;

    private boolean dragging   = false;
    private int     dragOffX   = 0;
    private int     dragOffY   = 0;
    private boolean rmbWasDown = false;

    public DamageIndicatorModule() {
        super("DamageIndicator", "Exibe vida e nome do alvo.", Category.MODS);
        loadConfig();
    }

    @Override public List<ModuleOption<?>> getOptions() { return Collections.emptyList(); }
    @Override public void onOptionChanged() {}

    // -------------------------------------------------------------------------
    // Getters/Setters para o HudLayoutScreen

    public int getHudX() { return hudX; }
    public int getHudY() { return hudY; }

    public void setHudPos(int x, int y) {
        this.hudX = x;
        this.hudY = y;
    }

    // -------------------------------------------------------------------------

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        handleDrag(event.getResolution());

        EntityLivingBase target = getTarget();
        if (target == null) return;

        int x = hudX, y = hudY;

        String name  = getTargetName(target);
        int    hpCur = (int) target.getHealth();
        int    hpMax = (int) target.getMaxHealth();

        // Linha 1 — nome
        RenderUtil.drawStringWithShadow("[" + name + "]", x + PAD, y + PAD, COLOR_NAME);

        // Linha 2 — vida
        String hpCurNum = String.valueOf(hpCur);
        String heart    = "\u2665";
        String slash    = "/";
        String hpMaxNum = String.valueOf(hpMax);

        float cx           = x + PAD;
        float lineY        = y + PAD + mc.fontRendererObj.FONT_HEIGHT + 2;
        float scaleX       = 1.8f;
        float scaleY       = 1.5f;
        float heartOffsetY = lineY - (mc.fontRendererObj.FONT_HEIGHT / 2f) * (scaleY - 1f) - 1.2f;
        float heartWidth   = mc.fontRendererObj.getStringWidth(heart) * scaleX;

        // número atual
        RenderUtil.drawStringWithShadow(hpCurNum, cx, lineY, COLOR_NAME);
        cx += mc.fontRendererObj.getStringWidth(hpCurNum) + 1;

        // coração 1
        GL11.glPushMatrix();
        GL11.glTranslatef(cx, heartOffsetY, 0);
        GL11.glScalef(scaleX, scaleY, 1f);
        RenderUtil.drawStringWithShadow(heart, 0, 0, COLOR_HP_CUR);
        GL11.glPopMatrix();
        cx += heartWidth + 1;

        // slash
        RenderUtil.drawStringWithShadow(slash, cx, lineY, COLOR_NAME);
        cx += mc.fontRendererObj.getStringWidth(slash) + 1;

        // número máximo
        RenderUtil.drawStringWithShadow(hpMaxNum, cx, lineY, COLOR_NAME);
        cx += mc.fontRendererObj.getStringWidth(hpMaxNum) + 1;

        // coração 2
        GL11.glPushMatrix();
        GL11.glTranslatef(cx, heartOffsetY, 0);
        GL11.glScalef(scaleX, scaleY, 1f);
        RenderUtil.drawStringWithShadow(heart, 0, 0, COLOR_HP_CUR);
        GL11.glPopMatrix();
    }

    private void handleDrag(ScaledResolution res) {
        int sw     = res.getScaledWidth();
        int sh     = res.getScaledHeight();
        int mouseX = (int)(Mouse.getX() * sw  / (double) mc.displayWidth);
        int mouseY = (int)((mc.displayHeight - Mouse.getY() - 1) * sh / (double) mc.displayHeight);

        boolean rmb = Mouse.isButtonDown(1);

        if (rmb && !rmbWasDown) {
            if (mouseX >= hudX && mouseX <= hudX + W
                    && mouseY >= hudY && mouseY <= hudY + H) {
                dragging = true;
                dragOffX = mouseX - hudX;
                dragOffY = mouseY - hudY;
            }
        }

        if (!rmb && rmbWasDown && dragging) {
            dragging = false;
            saveConfig();
        }

        if (dragging && rmb) {
            hudX = mouseX - dragOffX;
            hudY = mouseY - dragOffY;
            hudX = Math.max(0, Math.min(hudX, sw - W));
            hudY = Math.max(0, Math.min(hudY, sh - H));
        }

        rmbWasDown = rmb;
    }

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

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", hudX);
        obj.addProperty("y", hudY);
        Tesseract.instance().getConfigManager().setSection("DamageIndicator", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("DamageIndicator");
        if (obj == null) return;
        if (obj.has("x")) hudX = obj.get("x").getAsInt();
        if (obj.has("y")) hudY = obj.get("y").getAsInt();
    }
}