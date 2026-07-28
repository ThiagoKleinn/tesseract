package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.CycleOption;
import com.tesseract.module.config.FloatOption;
import com.tesseract.module.config.ModuleOption;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ArmorHUDModule extends BaseModule implements Configurable {

    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_RIGHT
    }
    public enum Layout         { HORIZONTAL, VERTICAL }
    public enum DurabilityMode { PERCENTAGE, HITS, NONE }

    private final CycleOption<Position>       optPosition;
    private final CycleOption<Layout>         optLayout;
    private final CycleOption<DurabilityMode> optDurability;
    private final FloatOption                 optScale;
    private final List<ModuleOption<?>>       options;

    private static final int SLOT_SIZE = 16;
    private static final int PADDING   = 2;

    public ArmorHUDModule() {
        super("ArmorHUD", "Exibe os itens de armadura equipados.", Category.MODS);

        optPosition   = new CycleOption<>("Posicao",
                Arrays.asList(Position.values()), Position.BOTTOM_LEFT);
        optLayout     = new CycleOption<>("Layout",
                Arrays.asList(Layout.values()), Layout.VERTICAL);
        optDurability = new CycleOption<>("Durabilidade",
                Arrays.asList(DurabilityMode.values()), DurabilityMode.PERCENTAGE);
        optScale      = new FloatOption("Escala", 1.0f, 0.5f, 3.0f, 0.25f);

        options = Arrays.asList(optPosition, optLayout, optDurability, optScale);

        loadConfig();
        setEnabled(true);
    }

    @Override public List<ModuleOption<?>> getOptions() { return options; }

    @Override
    public void onOptionChanged() { saveConfig(); }

    // -------------------------------------------------------------------------

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        ScaledResolution res   = event.getResolution();
        ItemStack[]      armor = mc.thePlayer.inventory.armorInventory;

        float scale      = optScale.getValue();
        int scaledSlot   = (int)(SLOT_SIZE * scale);
        int scaledPad    = (int)(PADDING   * scale);
        Layout layout    = optLayout.getValue();

        int totalW = layout == Layout.HORIZONTAL ? 4 * (scaledSlot + scaledPad) : scaledSlot;
        int totalH = layout == Layout.VERTICAL   ? 4 * (scaledSlot + scaledPad) : scaledSlot;

        int[] origin = getOrigin(res, totalW, totalH);
        int ox = origin[0], oy = origin[1];

        for (int i = 3; i >= 0; i--) {
            ItemStack stack = armor[i];
            int index = 3 - i;

            int x = layout == Layout.HORIZONTAL ? ox + index * (scaledSlot + scaledPad) : ox;
            int y = layout == Layout.VERTICAL   ? oy + index * (scaledSlot + scaledPad) : oy;

            if (stack != null) {
                net.minecraft.client.renderer.GlStateManager.pushMatrix();
                net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
                net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
                RenderUtil.drawItem(stack, 0, 0);
                net.minecraft.client.renderer.GlStateManager.popMatrix();

                DurabilityMode mode = optDurability.getValue();
                if (mode != DurabilityMode.NONE) {
                    String label = getDurabilityLabel(stack, mode);
                    if (label != null) {
                        int color = getDurabilityColor(stack);
                        if (layout == Layout.VERTICAL) {
                            RenderUtil.drawStringWithShadow(label,
                                    x + scaledSlot + 2, y + scaledSlot / 2f - 4, color);
                        } else {
                            RenderUtil.drawStringWithShadow(label,
                                    x + scaledSlot / 2f - mc.fontRendererObj.getStringWidth(label) / 2f,
                                    y + scaledSlot + 1, color);
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------

    private int[] getOrigin(ScaledResolution res, int totalW, int totalH) {
        int sw = res.getScaledWidth(), sh = res.getScaledHeight();
        int margin = 4, hotbar = 22;
        switch (optPosition.getValue()) {
            case TOP_LEFT:     return new int[]{ margin, margin };
            case TOP_CENTER:   return new int[]{ sw / 2 - totalW / 2, margin };
            case TOP_RIGHT:    return new int[]{ sw - totalW - margin, margin };
            case MIDDLE_LEFT:  return new int[]{ margin, sh / 2 - totalH / 2 };
            case MIDDLE_RIGHT: return new int[]{ sw - totalW - margin, sh / 2 - totalH / 2 };
            case BOTTOM_LEFT:  return new int[]{ margin, sh - hotbar - totalH - margin };
            case BOTTOM_RIGHT: return new int[]{ sw - totalW - margin, sh - hotbar - totalH - margin };
            default:           return new int[]{ margin, margin };
        }
    }

    private String getDurabilityLabel(ItemStack stack, DurabilityMode mode) {
        if (!stack.isItemStackDamageable()) return null;
        int max = stack.getMaxDamage(), current = max - stack.getItemDamage();
        switch (mode) {
            case PERCENTAGE: return (int)((current / (float) max) * 100) + "%";
            case HITS:       return String.valueOf(current);
            default:         return null;
        }
    }

    private int getDurabilityColor(ItemStack stack) {
        float pct = 1f - (float) stack.getItemDamage() / stack.getMaxDamage();
        if (pct > 0.6f) return 0x55FF55;
        if (pct > 0.3f) return 0xFFFF55;
        return 0xFF5555;
    }

    // -------------------------------------------------------------------------

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("position",    optPosition.getValue().name());
        obj.addProperty("layout",      optLayout.getValue().name());
        obj.addProperty("durability",  optDurability.getValue().name());
        obj.addProperty("scale",       optScale.getValue());
        Tesseract.instance().getConfigManager().setSection("ArmorHUD", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("ArmorHUD");
        if (obj.has("position"))
            optPosition.setValue(Position.valueOf(obj.get("position").getAsString()));
        if (obj.has("layout"))
            optLayout.setValue(Layout.valueOf(obj.get("layout").getAsString()));
        if (obj.has("durability"))
            optDurability.setValue(DurabilityMode.valueOf(obj.get("durability").getAsString()));
        if (obj.has("scale"))
            optScale.setValue(obj.get("scale").getAsFloat());
    }
}