package com.tesseract.module;

import com.tesseract.Tesseract;
import net.minecraft.client.Minecraft;

/**
 * Classe base para todos os módulos do Tesseract.
 * Todo módulo DEVE extender esta classe.
 *
 * Exemplo mínimo:
 *   public class ZoomModule extends BaseModule {
 *       public ZoomModule() { super("Zoom", "Permite dar zoom", Category.UTIL, Keyboard.KEY_Z); }
 *
 *       @Override public void onEnable() { ... }
 *       @Override public void onDisable() { ... }
 *   }
 */
public abstract class BaseModule {

    public enum Category {
        MODS,       // Todos os módulos utilitários e visuais (legit)
        COSMETICS,  // Capas, cosméticos...
    }

    // -------------------------------------------------------------------------

    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final Category category;

    private int keybind;       // código da tecla (Keyboard.KEY_*), -1 = sem bind
    private boolean enabled;
    private boolean registered; // está registrado no EventBus?

    // -------------------------------------------------------------------------

    public BaseModule(String name, String description, Category category, int keybind) {
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.keybind     = keybind;
        this.enabled     = false;
        this.registered  = false;
    }

    public BaseModule(String name, String description, Category category) {
        this(name, description, category, -1);
    }

    // -------------------------------------------------------------------------
    // Métodos que os módulos filhos podem (ou devem) sobrescrever

    /** Chamado quando o módulo é ativado. */
    public void onEnable() {}

    /** Chamado quando o módulo é desativado. */
    public void onDisable() {}

    // -------------------------------------------------------------------------
    // Toggle

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            if (!registered) {
                Tesseract.instance().getEventBus().register(this);
                registered = true;
            }
            onEnable();
        } else {
            if (registered) {
                Tesseract.instance().getEventBus().unregister(this);
                registered = false;
            }
            onDisable();
        }
    }

    // -------------------------------------------------------------------------
    // Getters / Setters

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }
    public int getKeybind()        { return keybind; }
    public boolean isEnabled()     { return enabled; }

    public void setKeybind(int keybind) { this.keybind = keybind; }
}