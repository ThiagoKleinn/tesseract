package com.tesseract.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventBus simples baseado em anotações.
 * Módulos se registram aqui e recebem eventos via @EventHandler.
 */
public class EventBus {

    // Mapa: tipo do evento → lista de (objeto, método)
    private final Map<Class<?>, List<EventListener>> listeners = new HashMap<>();

    /**
     * Registra todos os métodos anotados com @EventHandler no objeto dado.
     */
    public void register(Object obj) {
        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> eventType = method.getParameterTypes()[0];
            listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(new EventListener(obj, method));
        }
    }

    /**
     * Remove todos os listeners registrados para o objeto dado.
     */
    public void unregister(Object obj) {
        listeners.values().forEach(list ->
                list.removeIf(listener -> listener.getTarget() == obj)
        );
    }

    /**
     * Dispara um evento para todos os listeners registrados para aquele tipo.
     */
    public void post(Object event) {
        List<EventListener> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) return;

        // Cria uma cópia da lista para evitar ConcurrentModificationException
        // caso um listener chame unregister() durante a iteração.
        List<EventListener> safeList = new ArrayList<>(list);

        for (EventListener listener : safeList) {
            try {
                listener.invoke(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------------------------------------------------------------

    private static class EventListener {
        private final Object target;
        private final Method method;

        EventListener(Object target, Method method) {
            this.target = target;
            this.method = method;
            method.setAccessible(true);
        }

        Object getTarget() { return target; }

        void invoke(Object event) throws Exception {
            method.invoke(target, event);
        }
    }
}