package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.ModuleOption;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class StatusEffectModule extends BaseModule implements Configurable {

    // -------------------------------------------------------------------------
    // Layout

    private static final int ICON_SIZE = 18;
    private static final int GAP       = 3;
    private static final int TEXT_PAD  = 3;

    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation("textures/gui/container/inventory.png");

    private static final int COLOR_TIME = 0xFFAAAAAA;

    // Posição HUD
    private int hudX = 10;
    private int hudY = 60;

    // -------------------------------------------------------------------------
    // Opção de alinhamento: false = ícone esquerda (padrão), true = ícone direita

    private boolean iconRight = false;

    private final AlignmentOption alignOption = new AlignmentOption();

    // -------------------------------------------------------------------------
    // Nomes hardcoded

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
    private final List<ModuleOption<?>> options      = new ArrayList<>();

    // -------------------------------------------------------------------------

    public StatusEffectModule() {
        super("StatusEffect", "Exibe efeitos de poções ativos.", Category.MODS);
        loadConfig();
        rebuildStaticOptions();
    }

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
        rebuildDynamicOptions(effects);

        int curY = hudY;

        // Pré-calcula largura máxima dos textos para fixar coluna do ícone no modo RIGHT
        int maxTextW = 0;
        if (iconRight) {
            for (PotionEffect e2 : effects) {
                int id2 = e2.getPotionID();
                if (!effectFilter.getOrDefault(id2, true)) continue;
                int w = Math.max(mc.fontRendererObj.getStringWidth(getPotionName(e2, id2)),
                        mc.fontRendererObj.getStringWidth(formatDuration(e2.getDuration())));
                if (w > maxTextW) maxTextW = w;
            }
        }

        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();
            if (!effectFilter.getOrDefault(id, true)) continue;

            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;

            String name    = getPotionName(effect, id);
            String timeStr = formatDuration(effect.getDuration());
            int color      = potion.getLiquidColor() | 0xFF000000;

            int iconX, textX;
            if (iconRight) {
                textX = hudX;
                iconX = hudX + maxTextW + TEXT_PAD;
            } else {
                iconX = hudX;
                textX = hudX + ICON_SIZE + TEXT_PAD;
            }

            int nameY = curY + (ICON_SIZE / 2) - mc.fontRendererObj.FONT_HEIGHT;
            int timeY = nameY + mc.fontRendererObj.FONT_HEIGHT + 1;

            mc.fontRendererObj.drawStringWithShadow(name,    textX, nameY, color);
            mc.fontRendererObj.drawStringWithShadow(timeStr, textX, timeY, COLOR_TIME);

            if (potion.hasStatusIcon()) {
                drawPotionIcon(potion, iconX, curY);
            }

            curY += ICON_SIZE + GAP;
        }
    }

    // -------------------------------------------------------------------------
    // Ícone

    private void drawPotionIcon(Potion potion, int x, int y) {
        int iconIndex = potion.getStatusIconIndex();
        int col = iconIndex % 8;
        int row = iconIndex / 8;
        int u = col * 18;
        int v = 198 + row * 18;

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(INVENTORY_TEXTURE);
        new Gui().drawTexturedModalRect(x, y, u, v, ICON_SIZE, ICON_SIZE);
        GlStateManager.disableBlend();
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

    /**
     * Opções estáticas (alinhamento) ficam sempre no topo.
     * Opções dinâmicas (toggle por efeito) ficam abaixo.
     */
    private void rebuildStaticOptions() {
        options.clear();
        options.add(alignOption);
    }

    private void rebuildDynamicOptions(Collection<PotionEffect> effects) {
        Set<Integer> activeIds = new HashSet<>();
        for (PotionEffect e : effects) activeIds.add(e.getPotionID());

        // Coleta ids já presentes nas opções dinâmicas
        Set<Integer> optionIds = new HashSet<>();
        for (ModuleOption<?> opt : options)
            if (opt instanceof EffectToggleOption)
                optionIds.add(((EffectToggleOption) opt).potionId);

        if (optionIds.equals(activeIds)) return;

        // Remove antigas opções dinâmicas, mantém estáticas
        options.removeIf(o -> o instanceof EffectToggleOption);

        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();
            if (Potion.potionTypes[id] == null) continue;
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
        obj.addProperty("iconRight", iconRight);
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
        if (obj.has("x"))         hudX      = obj.get("x").getAsInt();
        if (obj.has("y"))         hudY      = obj.get("y").getAsInt();
        if (obj.has("iconRight")) iconRight  = obj.get("iconRight").getAsBoolean();
        if (obj.has("filters"))
            for (Map.Entry<String, com.google.gson.JsonElement> e :
                    obj.getAsJsonObject("filters").entrySet())
                try { effectFilter.put(Integer.parseInt(e.getKey()), e.getValue().getAsBoolean()); }
                catch (NumberFormatException ignored) {}
    }

    // -------------------------------------------------------------------------
    // AlignmentOption — aparece no ClickGUI como "Icon Side: LEFT / RIGHT"

    private class AlignmentOption extends ModuleOption<Boolean> {
        AlignmentOption() { super("Icon Side", iconRight); }

        @Override public void onLeftClick()  { toggle(); }
        @Override public void onRightClick() { toggle(); }

        private void toggle() {
            iconRight = !iconRight;
            setValue(iconRight);
            onOptionChanged();
        }

        @Override public String getDisplayValue() { return iconRight ? "RIGHT" : "LEFT"; }
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