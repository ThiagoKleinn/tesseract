package com.tesseract;

import com.tesseract.core.ForgeEventListener;
import com.tesseract.core.ModuleManager;
import com.tesseract.event.EventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid   = Tesseract.MODID,
        name    = Tesseract.NAME,
        version = Tesseract.VERSION
)
public class Tesseract {

    public static final String MODID   = "tesseract";
    public static final String NAME    = "Tesseract";
    public static final String VERSION = "1.0.0";

    // Singleton — acesse de qualquer lugar com Tesseract.instance()
    @Mod.Instance(MODID)
    public static Tesseract INSTANCE;

    public static Tesseract instance() { return INSTANCE; }

    // -------------------------------------------------------------------------

    private EventBus     eventBus;
    private ModuleManager moduleManager;

    // -------------------------------------------------------------------------

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        eventBus      = new EventBus();
        moduleManager = new ModuleManager();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Registra a ponte Forge → nosso EventBus
        MinecraftForge.EVENT_BUS.register(new ForgeEventListener());

        // Carrega todos os módulos
        moduleManager.init();

        System.out.println("{Tesseract} inicializado com sucesso!");
    }

    // -------------------------------------------------------------------------

    public EventBus      getEventBus()      { return eventBus; }
    public ModuleManager getModuleManager() { return moduleManager; }
}