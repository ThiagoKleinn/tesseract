package com.tesseract.module.config;

import java.util.List;

public class CycleOption<T> extends ModuleOption<T> {
    private final List<T> values;
    private int index;

    public CycleOption(String label, List<T> values, T defaultValue) {
        super(label, defaultValue);
        this.values = values;
        this.index  = values.indexOf(defaultValue);
        if (this.index < 0) this.index = 0;
    }

    @Override
    public void onLeftClick() {
        index = (index + 1) % values.size();
        setValue(values.get(index));
    }

    @Override
    public void onRightClick() {
        index = (index - 1 + values.size()) % values.size();
        setValue(values.get(index));
    }

    @Override
    public String getDisplayValue() {
        return getValue().toString();
    }
}