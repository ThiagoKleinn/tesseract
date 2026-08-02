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
import org.lwjgl.opengl.GL11;

import java.util.*;

public class StatusEffectModule extends BaseModule implements Configurable {

    // Layout

    private static final int PAD    = 5;
    private static final int LINE_H = 20;
    private static final int ICON_S = 14; // tamanho do ícone (quadrado)

    // Cores
    private static final int COLOR_NAME      = 0xFFFFFFFF;
    private static final int COLOR_TIME      = 0xFFAAAAAA;
    private static final int COLOR_AMPLIFIER = 0xFFFFDD55;

    // Posição do HUD (configurável via HudLayoutScreen)
    private int hudX = 10;
    private int hudY = 60;

    // Filtro de efeitos — mapa de potionId -> visível
    // Populado dinamicamente conforme efeitos aparecem

    private final Map<Integer, Boolean> effectFilter = new LinkedHashMap<>();

    // Lista de opções exposta para o GuiPanel (atualizada a cada render)
    private final List<ModuleOption<?>> options = new ArrayList<>();

    public StatusEffectModule() {
        super("StatusEffect", "Exibe efeitos de poções ativos.", Category.MODS);
        loadConfig();
    }

    // Configurable

    @Override
    public List<ModuleOption<?>> getOptions() {
        return options;
    }

    @Override
    public void onOptionChanged() {
        saveConfig();
    }

    // Render

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (effects == null || effects.isEmpty()) return;

        // Sincroniza o mapa de filtros com os efeitos ativos atuais
        syncFilterMap(effects);

        // Rebuild das opções para o GuiPanel
        rebuildOptions();

        int curY = hudY;

        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();

            // Se o efeito está filtrado (desligado), pula
            Boolean visible = effectFilter.get(id);
            if (visible != null && !visible) continue;

            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;

            String name      = buildName(effect, potion);
            String timeStr   = formatDuration(effect.getDuration());
            int    nameColor = potion.getLiquidColor() | 0xFF000000;

            // Nome + amplificador
            mc.fontRendererObj.drawStringWithShadow(name, hudX + ICON_S + PAD, curY, nameColor);

            // Tempo restante
            mc.fontRendererObj.drawStringWithShadow(
                    timeStr,
                    hudX + ICON_S + PAD,
                    curY + mc.fontRendererObj.FONT_HEIGHT + 1,
                    COLOR_TIME
            );

            curY += LINE_H * 2 + 2;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers

    /**
     * Garante que todo efeito ativo tenha entrada no mapa de filtros.
     * Novos efeitos entram como visíveis (true) por padrão.
     */
    private void syncFilterMap(Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            effectFilter.putIfAbsent(effect.getPotionID(), true);
        }
    }

    /**
     * Reconstrói a lista de ModuleOption a partir do effectFilter atual,
     * apenas para efeitos que o player tem ativos no momento.
     */
    private void rebuildOptions() {
        if (mc.thePlayer == null) return;

        Collection<PotionEffect> active = mc.thePlayer.getActivePotionEffects();
        Set<Integer> activeIds = new HashSet<>();
        for (PotionEffect e : active) activeIds.add(e.getPotionID());

        // Só reconstrói se o conjunto de efeitos mudou
        // (compara com os ids já nas options)
        Set<Integer> optionIds = new HashSet<>();
        for (ModuleOption<?> opt : options) {
            if (opt instanceof EffectToggleOption) {
                optionIds.add(((EffectToggleOption) opt).potionId);
            }
        }

        if (optionIds.equals(activeIds)) return; // nada mudou

        options.clear();
        for (PotionEffect effect : active) {
            int id = effect.getPotionID();
            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;

            boolean current = effectFilter.getOrDefault(id, true);
            options.add(new EffectToggleOption(potion.getName(), id, current));
        }
    }

    private String buildName(PotionEffect effect, Potion potion) {
        String name = potion.getName();
        // Capitaliza primeira letra
        if (!name.isEmpty()) name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        int amp = effect.getAmplifier();
        if (amp > 0) {
            name += " " + toRoman(amp + 1);
        }
        return name;
    }

    private String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String toRoman(int n) {
        switch (n) {
            case 2:  return "II";
            case 3:  return "III";
            case 4:  return "IV";
            case 5:  return "V";
            case 6:  return "VI";
            case 7:  return "VII";
            case 8:  return "VIII";
            case 9:  return "IX";
            case 10: return "X";
            default: return String.valueOf(n);
        }
    }

    // Getters/setters para HudLayoutScreen

    public int getHudX() { return hudX; }
    public int getHudY() { return hudY; }

    public void setHudPos(int x, int y) {
        this.hudX = x;
        this.hudY = y;
    }

    // Config

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", hudX);
        obj.addProperty("y", hudY);

        JsonObject filters = new JsonObject();
        for (Map.Entry<Integer, Boolean> entry : effectFilter.entrySet()) {
            filters.addProperty(String.valueOf(entry.getKey()), entry.getValue());
        }
        obj.add("filters", filters);

        Tesseract.instance().getConfigManager().setSection("StatusEffect", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("StatusEffect");
        if (obj == null) return;
        if (obj.has("x")) hudX = obj.get("x").getAsInt();
        if (obj.has("y")) hudY = obj.get("y").getAsInt();

        if (obj.has("filters")) {
            JsonObject filters = obj.getAsJsonObject("filters");
            for (Map.Entry<String, com.google.gson.JsonElement> entry : filters.entrySet()) {
                try {
                    int id = Integer.parseInt(entry.getKey());
                    boolean visible = entry.getValue().getAsBoolean();
                    effectFilter.put(id, visible);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    // EffectToggleOption — opção que liga/desliga um efeito específico

    private class EffectToggleOption extends ModuleOption<Boolean> {

        final int potionId;

        EffectToggleOption(String label, int potionId, boolean initialValue) {
            super(label, initialValue);
            this.potionId = potionId;
        }

        @Override
        public void onLeftClick() {
            toggle();
        }

        @Override
        public void onRightClick() {
            toggle();
        }

        private void toggle() {
            boolean next = !getValue();
            setValue(next);
            effectFilter.put(potionId, next);
            onOptionChanged();
        }

        @Override
        public String getDisplayValue() {
            return getValue() ? "ON" : "OFF";
        }
    }
}