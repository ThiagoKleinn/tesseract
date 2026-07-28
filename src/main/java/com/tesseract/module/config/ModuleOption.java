package com.tesseract.module.config;

public abstract class ModuleOption<T> {
    private final String label;
    private T value;

    public ModuleOption(String label, T defaultValue) {
        this.label = label;
        this.value = defaultValue;
    }

    public String getLabel() { return label; }
    public T getValue()      { return value; }
    public void setValue(T v){ this.value = v; }

    /** Clique esquerdo — avança para o próximo valor */
    public abstract void onLeftClick();
    /** Clique direito — volta para o valor anterior */
    public abstract void onRightClick();
    /** Texto exibido na GUI ao lado do label */
    public abstract String getDisplayValue();
}