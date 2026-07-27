package com.tesseract.module.modules;

import com.tesseract.gui.ClickGuiScreen;
import com.tesseract.module.BaseModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * ClickGuiModule — abre/fecha o ClickGUI ao pressionar RSHIFT.
 *
 * Registrado no ModuleManager como qualquer outro módulo.
 * Não aparece nos painéis do ClickGUI (filtrado pelo GuiPanel via instanceof).
 *
 * Keybind padrão: RSHIFT (Keyboard.KEY_RSHIFT)
 */
public class ClickGuiModule extends BaseModule {

    public ClickGuiModule() {
        super("ClickGUI", "Abre o menu de módulos", Category.MODS, Keyboard.KEY_RSHIFT);
    }

    // -------------------------------------------------------------------------

    /**
     * toggle() sobrescrito: em vez de ligar/desligar, abre ou fecha a tela.
     * Assim o ModuleManager.onKeyPress() funciona normalmente sem adaptação.
     */
    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClickGuiScreen) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else {
            mc.displayGuiScreen(new ClickGuiScreen());
        }
    }

    // -------------------------------------------------------------------------
    // ClickGUI não tem estado ligado/desligado — hooks vazios

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}