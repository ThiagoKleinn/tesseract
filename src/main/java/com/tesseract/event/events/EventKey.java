package com.tesseract.event.events;

/**
 * Disparado quando uma tecla é pressionada.
 * Usado pelo ModuleManager para checar keybinds dos módulos.
 */
public class EventKey {
    private final int keyCode;

    public EventKey(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() { return keyCode; }
}