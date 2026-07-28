package com.tesseract.module.config;

public class FloatOption extends ModuleOption<Float> {
    private final float min, max, step;

    public FloatOption(String label, float defaultValue, float min, float max, float step) {
        super(label, defaultValue);
        this.min  = min;
        this.max  = max;
        this.step = step;
    }

    @Override
    public void onLeftClick() {
        setValue(Math.min(max, getValue() + step));
    }

    @Override
    public void onRightClick() {
        setValue(Math.max(min, getValue() - step));
    }

    @Override
    public String getDisplayValue() {
        return String.format("%.1f", getValue());
    }
}