package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import org.lwjgl.input.Keyboard;

public class ArmorHUDModule extends BaseModule {

    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum Layout { HORIZONTAL, VERTICAL }

    public enum DurabilityMode { PERCENTAGE, HITS, NONE }

    // --- Configurações ---
    private Position      position       = Position.BOTTOM_LEFT;
    private Layout        layout         = Layout.VERTICAL;
    private DurabilityMode durabilityMode = DurabilityMode.PERCENTAGE;
    private float         scale          = 1.0f;

    private static final int   SLOT_SIZE  = 16;
    private static final int   PADDING    = 2;
    private static final int   TEXT_COLOR = 0xFFFFFF;

    public ArmorHUDModule() {
        super("ArmorHUD", "Exibe os itens de armadura equipados.", Category.MODS);
        loadConfig();
        setEnabled(true);
    }

    // -------------------------------------------------------------------------

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        ScaledResolution res = event.getResolution();
        ItemStack[] armor    = mc.thePlayer.inventory.armorInventory;

        int slotCount  = 4;
        int scaledSlot = (int)(SLOT_SIZE * scale);
        int scaledPad  = (int)(PADDING  * scale);

        // Tamanho total do bloco
        int totalW = layout == Layout.HORIZONTAL
                ? slotCount * (scaledSlot + scaledPad)
                : scaledSlot;
        int totalH = layout == Layout.VERTICAL
                ? slotCount * (scaledSlot + scaledPad)
                : scaledSlot;

        int[] origin = getOrigin(res, totalW, totalH);
        int ox = origin[0];
        int oy = origin[1];

        for (int i = 3; i >= 0; i--) {
            ItemStack stack = armor[i];
            int index = 3 - i; // 0 = capacete, 3 = botas

            int x = layout == Layout.HORIZONTAL
                    ? ox + index * (scaledSlot + scaledPad)
                    : ox;
            int y = layout == Layout.VERTICAL
                    ? oy + index * (scaledSlot + scaledPad)
                    : oy;

            // Escala do item
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
            net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
            RenderUtil.drawItem(stack != null ? stack : new ItemStack(Item.getItemById(0)), 0, 0);
            net.minecraft.client.renderer.GlStateManager.popMatrix();

            // Durabilidade
            if (stack != null && durabilityMode != DurabilityMode.NONE) {
                String label = getDurabilityLabel(stack);
                if (label != null) {
                    int color = getDurabilityColor(stack);
                    if (layout == Layout.VERTICAL) {
                        RenderUtil.drawStringWithShadow(label,
                                x + scaledSlot + 2,
                                y + scaledSlot / 2f - 4,
                                color);
                    } else {
                        RenderUtil.drawStringWithShadow(label,
                                x + scaledSlot / 2f - mc.fontRendererObj.getStringWidth(label) / 2f,
                                y + scaledSlot + 1,
                                color);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------

    private int[] getOrigin(ScaledResolution res, int totalW, int totalH) {
        int sw = res.getScaledWidth();
        int sh = res.getScaledHeight();
        int margin = 4;
        int hotbarOffset = 22;

        switch (position) {
            case TOP_LEFT:     return new int[]{ margin, margin };
            case TOP_CENTER:   return new int[]{ sw / 2 - totalW / 2, margin };
            case TOP_RIGHT:    return new int[]{ sw - totalW - margin, margin };
            case MIDDLE_LEFT:  return new int[]{ margin, sh / 2 - totalH / 2 };
            case MIDDLE_RIGHT: return new int[]{ sw - totalW - margin, sh / 2 - totalH / 2 };
            case BOTTOM_LEFT:  return new int[]{ margin, sh - hotbarOffset - totalH - margin };
            case BOTTOM_RIGHT: return new int[]{ sw - totalW - margin, sh - hotbarOffset - totalH - margin };
            default:           return new int[]{ margin, margin };
        }
    }

    private String getDurabilityLabel(ItemStack stack) {
        if (!stack.isItemStackDamageable()) return null;
        int max     = stack.getMaxDamage();
        int current = max - stack.getItemDamage();
        switch (durabilityMode) {
            case PERCENTAGE: return (int)((current / (float) max) * 100) + "%";
            case HITS:       return String.valueOf(current);
            default:         return null;
        }
    }

    private int getDurabilityColor(ItemStack stack) {
        float pct = 1f - (float) stack.getItemDamage() / stack.getMaxDamage();
        if (pct > 0.6f) return 0x55FF55; // verde
        if (pct > 0.3f) return 0xFFFF55; // amarelo
        return 0xFF5555;                  // vermelho
    }

    // -------------------------------------------------------------------------
    // Config

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("position",       position.name());
        obj.addProperty("layout",         layout.name());
        obj.addProperty("durabilityMode", durabilityMode.name());
        obj.addProperty("scale",          scale);
        Tesseract.instance().getConfigManager().setSection("ArmorHUD", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("ArmorHUD");
        if (obj.has("position"))
            position = Position.valueOf(obj.get("position").getAsString());
        if (obj.has("layout"))
            layout = Layout.valueOf(obj.get("layout").getAsString());
        if (obj.has("durabilityMode"))
            durabilityMode = DurabilityMode.valueOf(obj.get("durabilityMode").getAsString());
        if (obj.has("scale"))
            scale = obj.get("scale").getAsFloat();
    }

    // -------------------------------------------------------------------------
    // Getters / Setters (para a GUI de configuração futura)

    public Position getPosition()             { return position; }
    public Layout getLayout()                 { return layout; }
    public DurabilityMode getDurabilityMode() { return durabilityMode; }
    public float getScale()                   { return scale; }

    public void setPosition(Position p)          { position = p;       saveConfig(); }
    public void setLayout(Layout l)              { layout = l;         saveConfig(); }
    public void setDurabilityMode(DurabilityMode d) { durabilityMode = d; saveConfig(); }
    public void setScale(float s)                { scale = Math.max(0.5f, Math.min(3f, s)); saveConfig(); }
}