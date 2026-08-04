package com.tesseract.module.modules;

import com.google.gson.JsonObject;
import com.tesseract.Tesseract;
import com.tesseract.module.BaseModule;
import com.tesseract.module.config.Configurable;
import com.tesseract.module.config.CycleOption;
import com.tesseract.module.config.ModuleOption;

import java.util.Arrays;
import java.util.List;

public class AmbienceModule extends BaseModule implements Configurable {

    public enum SkyMode {
        DAY,
        NIGHT,
        SUNSET,
        SUNRISE,
        NETHER,
        END
    }

    private final CycleOption<SkyMode>  optSkyMode;
    private final List<ModuleOption<?>> options;

    public AmbienceModule() {
        super("Ambience", "Controla o céu e o tempo visual do mundo.", Category.MODS);

        optSkyMode = new CycleOption<>("Modo",
                Arrays.asList(SkyMode.values()), SkyMode.DAY);

        options = Arrays.asList(optSkyMode);

        loadConfig();
    }

    @Override
    public List<ModuleOption<?>> getOptions() { return options; }

    @Override
    public void onOptionChanged() { saveConfig(); }

    public SkyMode getSkyMode() { return optSkyMode.getValue(); }

    public void saveConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("skyMode", optSkyMode.getValue().name());
        Tesseract.instance().getConfigManager().setSection("Ambience", obj);
        Tesseract.instance().getConfigManager().save();
    }

    private void loadConfig() {
        JsonObject obj = Tesseract.instance().getConfigManager().getSection("Ambience");
        if (obj != null && obj.has("skyMode"))
            optSkyMode.setValue(SkyMode.valueOf(obj.get("skyMode").getAsString()));
    }
}