package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventTick;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.CycleOption;
import com.tesseract.module.config.ModuleOption;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AmbienceModule extends BaseModule implements Configurable {

    public enum SkyMode {
        DAY,        // 6000  — meio-dia
        NIGHT,      // 18000 — meia-noite
        SUNSET,     // 12000 — pôr do sol
        SUNRISE,    // 23000 — amanhecer
        NETHER,     // céu/neblina do Nether
        END         // céu do End (sem sol/lua)
    }

    private final CycleOption<SkyMode>  optSkyMode;
    private final List<ModuleOption<?>> options;

    private static final long TIME_DAY     = 6000L;
    private static final long TIME_SUNSET  = 12000L;
    private static final long TIME_NIGHT   = 18000L;
    private static final long TIME_SUNRISE = 23000L;

    public AmbienceModule() {
        super("Ambience", "Controla o céu e o tempo visual do mundo.", Category.MODS);

        optSkyMode = new CycleOption<>("Modo",
                Arrays.asList(SkyMode.values()), SkyMode.DAY);

        options = Collections.singletonList(optSkyMode);

        loadConfig();
    }

    @Override
    public List<ModuleOption<?>> getOptions() { return options; }

    @Override
    public void onOptionChanged() { saveConfig(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        World world = mc.theWorld;
        SkyMode mode = optSkyMode.getValue();

        switch (mode) {
            case NETHER:
                forceDimensionRender(world, -1);
                break;

            case END:
                forceDimensionRender(world, 1);
                break;

            default:
                if (world.provider.getDimensionId() == 0) {
                    restoreOverworldRender(world);
                }
                lockTime(world, mode);
                break;
        }
    }

    private void lockTime(World world, SkyMode mode) {
        long target;
        switch (mode) {
            case DAY:     target = TIME_DAY;     break;
            case SUNSET:  target = TIME_SUNSET;  break;
            case NIGHT:   target = TIME_NIGHT;   break;
            case SUNRISE: target = TIME_SUNRISE; break;
            default:      return;
        }

        long current = world.getWorldTime();
        long dayTime = current % 24000L;
        long diff    = target - dayTime;

        world.setWorldTime(current + diff);
    }

    private void forceDimensionRender(World world, int dimensionId) {
        if (world.provider.getDimensionId() == dimensionId) return;
        try {
            net.minecraft.world.WorldProvider newProvider =
                    net.minecraft.world.WorldProvider.getProviderForDimension(dimensionId);
            newProvider.registerWorld(world);

            java.lang.reflect.Field f = World.class.getDeclaredField("provider");
            f.setAccessible(true);
            f.set(world, newProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreOverworldRender(World world) {
        if (world.provider.getDimensionId() == 0) return;
        try {
            net.minecraft.world.WorldProvider overworld =
                    net.minecraft.world.WorldProvider.getProviderForDimension(0);
            overworld.registerWorld(world);

            java.lang.reflect.Field f = World.class.getDeclaredField("provider");
            f.setAccessible(true);
            f.set(world, overworld);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (mc.theWorld != null && mc.theWorld.isRemote) {
            restoreOverworldRender(mc.theWorld);
        }
    }

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("skyMode", optSkyMode.getValue().name());
        Tesseract.instance().getConfigManager().setSection("Ambience", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("Ambience");
        if (obj.has("skyMode"))
            optSkyMode.setValue(SkyMode.valueOf(obj.get("skyMode").getAsString()));
    }
}