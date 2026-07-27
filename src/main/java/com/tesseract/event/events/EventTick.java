package com.tesseract.event.events;

/**
 * Disparado a cada tick do client (20x por segundo).
 * Use para lógica que não depende de render (timers, verificações, etc).
 */
public class EventTick {
    public enum Phase { PRE, POST }

    private final Phase phase;

    public EventTick(Phase phase) {
        this.phase = phase;
    }

    public Phase getPhase() { return phase; }
}