package com.tesseract.module;

import com.tesseract.Tesseract;
import net.minecraft.client.Minecraft;

public abstract class BaseModule {

    public enum Category {
        MODS,
        COSMETICS,
    }

    // -------------------------------------------------------------------------

    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final Category category;

    private int keybind;
    private boolean enabled;
    private boolean registered;

    // Sub-painel de bind aberto neste módulo?
    private boolean bindPanelOpen = false;
    // Aguardando tecla para registrar?
    private boolean listeningForKey = false;

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

    public void onEnable()  {}
    public void onDisable() {}

    // -------------------------------------------------------------------------

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
    // Bind panel

    /** Sobrescreva e retorne true nos módulos que aceitam keybind configurável. */
    public boolean isBindable() { return false; }

    public boolean isBindPanelOpen()    { return bindPanelOpen; }
    public boolean isListeningForKey()  { return listeningForKey; }

    public void openBindPanel() {
        bindPanelOpen   = true;
        listeningForKey = false;
    }

    public void closeBindPanel() {
        bindPanelOpen   = false;
        listeningForKey = false;
    }

    public void startListening() {
        listeningForKey = true;
    }

    /** Chamado pelo GuiPanel quando uma tecla é pressionada no modo listening. */
    public void onKeyReceived(int keyCode) {
        if (!listeningForKey) return;
        setKeybind(keyCode);
        listeningForKey = false;
        bindPanelOpen   = false;
    }

    // -------------------------------------------------------------------------

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }
    public int getKeybind()        { return keybind; }
    public boolean isEnabled()     { return enabled; }

    public void setKeybind(int keybind) { this.keybind = keybind; }
}