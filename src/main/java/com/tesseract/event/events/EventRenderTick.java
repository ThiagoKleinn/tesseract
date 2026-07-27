package com.tesseract.event.events;

public class EventRenderTick {
    private final float partialTicks;

    public EventRenderTick(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() { return partialTicks; }
}