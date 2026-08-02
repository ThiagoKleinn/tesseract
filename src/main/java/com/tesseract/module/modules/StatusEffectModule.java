package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.ModuleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class StatusEffectModule extends BaseModule implements Configurable {

    // -------------------------------------------------------------------------
    // Layout

    private static final int ICON_SIZE = 16;
    private static final int LINE_H    = 10;
    private static final int GAP       = 2;
    private static final int TEXT_PAD  = 4;

    // Cores
    private static final int COLOR_TIME = 0xFFAAAAAA;

    // Posição HUD
    private int hudX = 10;
    private int hudY = 60;

    // -------------------------------------------------------------------------
    // Nomes hardcoded em inglês por potionId

    private static final Map<Integer, String> POTION_NAMES = new HashMap<>();
    static {
        POTION_NAMES.put(1,  "Speed");
        POTION_NAMES.put(2,  "Slowness");
        POTION_NAMES.put(3,  "Haste");
        POTION_NAMES.put(4,  "Mining Fatigue");
        POTION_NAMES.put(5,  "Strength");
        POTION_NAMES.put(6,  "Instant Health");
        POTION_NAMES.put(7,  "Instant Damage");
        POTION_NAMES.put(8,  "Jump Boost");
        POTION_NAMES.put(9,  "Nausea");
        POTION_NAMES.put(10, "Regeneration");
        POTION_NAMES.put(11, "Resistance");
        POTION_NAMES.put(12, "Fire Resistance");
        POTION_NAMES.put(13, "Water Breathing");
        POTION_NAMES.put(14, "Invisibility");
        POTION_NAMES.put(15, "Blindness");
        POTION_NAMES.put(16, "Night Vision");
        POTION_NAMES.put(17, "Hunger");
        POTION_NAMES.put(18, "Weakness");
        POTION_NAMES.put(19, "Poison");
        POTION_NAMES.put(20, "Wither");
        POTION_NAMES.put(21, "Health Boost");
        POTION_NAMES.put(22, "Absorption");
        POTION_NAMES.put(23, "Saturation");
    }

    // -------------------------------------------------------------------------
    // Filtro de efeitos

    private final Map<Integer, Boolean> effectFilter = new LinkedHashMap<>();
    private final List<ModuleOption<?>>  options      = new ArrayList<>();

    // -------------------------------------------------------------------------

    public StatusEffectModule() {
        super("StatusEffect", "Exibe efeitos de poções ativos.", Category.MODS);
        loadConfig();
    }

    // -------------------------------------------------------------------------
    // Configurable

    @Override public List<ModuleOption<?>> getOptions() { return options; }
    @Override public void onOptionChanged() { saveConfig(); }

    // -------------------------------------------------------------------------
    // Render

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (effects == null || effects.isEmpty()) return;

        syncFilterMap(effects);
        rebuildOptions(effects);

        int curY = hudY;

        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();
            if (!effectFilter.getOrDefault(id, true)) continue;

            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;

            String name    = getPotionName(effect, id);
            String timeStr = formatDuration(effect.getDuration());

            int nameW = mc.fontRendererObj.getStringWidth(name);
            int timeW = mc.fontRendererObj.getStringWidth(timeStr);
            int textW = Math.max(nameW, timeW);

            // texto à esquerda, ícone à direita
            int textX = hudX;
            int iconX = hudX + textW + TEXT_PAD;

            // nome centralizado horizontalmente dentro do textW
            int nameX = textX + (textW - nameW) / 2;
            int nameY = curY + (ICON_SIZE / 2) - LINE_H;

            // tempo centralizado
            int timeX = textX + (textW - timeW) / 2;
            int timeY = nameY + LINE_H + 1;

            int color = potion.getLiquidColor() | 0xFF000000;

            mc.fontRendererObj.drawStringWithShadow(name,    nameX, nameY, color);
            mc.fontRendererObj.drawStringWithShadow(timeStr, timeX, timeY, COLOR_TIME);

            drawPotionIcon(potion, iconX, curY);

            curY += ICON_SIZE + GAP;
        }
    }

    // -------------------------------------------------------------------------
    // Ícone real da poção

    private void drawPotionIcon(Potion potion, int x, int y) {
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1f, 1f, 1f, 1f);

            mc.getTextureManager().bindTexture(
                    new ResourceLocation("textures/gui/container/inventory.png"));

            int iconIndex = potion.getStatusIconIndex();
            int iconU = (iconIndex % 8) * 18;
            int iconV = 198 + (iconIndex / 8) * 18;

            float s  = 256f;
            float u0 = iconU / s;
            float v0 = iconV / s;
            float u1 = (iconU + 18) / s;
            float v1 = (iconV + 18) / s;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x,             y);
            GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x + ICON_SIZE, y);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x + ICON_SIZE, y + ICON_SIZE);
            GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x,             y + ICON_SIZE);
            GL11.glEnd();

            GL11.glDisable(GL11.GL_BLEND);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------------------------
    // Helpers

    private String getPotionName(PotionEffect effect, int id) {
        String base = POTION_NAMES.getOrDefault(id, "Effect " + id);
        int amp = effect.getAmplifier();
        if (amp > 0) base += " " + toRoman(amp + 1);
        return base;
    }

    private void syncFilterMap(Collection<PotionEffect> effects) {
        for (PotionEffect e : effects) effectFilter.putIfAbsent(e.getPotionID(), true);
    }

    private void rebuildOptions(Collection<PotionEffect> effects) {
        Set<Integer> activeIds = new HashSet<>();
        for (PotionEffect e : effects) activeIds.add(e.getPotionID());

        Set<Integer> optionIds = new HashSet<>();
        for (ModuleOption<?> opt : options)
            if (opt instanceof EffectToggleOption)
                optionIds.add(((EffectToggleOption) opt).potionId);

        if (optionIds.equals(activeIds)) return;

        options.clear();
        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();
            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;
            String name = POTION_NAMES.getOrDefault(id, "Effect " + id);
            options.add(new EffectToggleOption(name, id, effectFilter.getOrDefault(id, true)));
        }
    }

    private String formatDuration(int ticks) {
        int total = ticks / 20;
        return String.format("%d:%02d", total / 60, total % 60);
    }

    private String toRoman(int n) {
        String[] r = {"","I","II","III","IV","V","VI","VII","VIII","IX","X"};
        return (n >= 1 && n <= 10) ? r[n] : String.valueOf(n);
    }

    // -------------------------------------------------------------------------
    // Getters/Setters para HudLayoutScreen

    public int getHudX() { return hudX; }
    public int getHudY() { return hudY; }
    public void setHudPos(int x, int y) { this.hudX = x; this.hudY = y; }

    // -------------------------------------------------------------------------
    // Config

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", hudX);
        obj.addProperty("y", hudY);
        JsonObject filters = new JsonObject();
        for (Map.Entry<Integer, Boolean> e : effectFilter.entrySet())
            filters.addProperty(String.valueOf(e.getKey()), e.getValue());
        obj.add("filters", filters);
        Tesseract.instance().getConfigManager().setSection("StatusEffect", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("StatusEffect");
        if (obj == null) return;
        if (obj.has("x")) hudX = obj.get("x").getAsInt();
        if (obj.has("y")) hudY = obj.get("y").getAsInt();
        if (obj.has("filters"))
            for (Map.Entry<String, com.google.gson.JsonElement> e :
                    obj.getAsJsonObject("filters").entrySet())
                try { effectFilter.put(Integer.parseInt(e.getKey()), e.getValue().getAsBoolean()); }
                catch (NumberFormatException ignored) {}
    }

    // -------------------------------------------------------------------------
    // EffectToggleOption

    private class EffectToggleOption extends ModuleOption<Boolean> {
        final int potionId;

        EffectToggleOption(String label, int potionId, boolean initial) {
            super(label, initial);
            this.potionId = potionId;
        }

        @Override public void onLeftClick()  { toggle(); }
        @Override public void onRightClick() { toggle(); }

        private void toggle() {
            boolean next = !getValue();
            setValue(next);
            effectFilter.put(potionId, next);
            onOptionChanged();
        }

        @Override public String getDisplayValue() { return getValue() ? "ON" : "OFF"; }
    }
}