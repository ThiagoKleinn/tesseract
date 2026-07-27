package com.tesseract.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um método como listener de evento no EventBus do KleinClient.
 * O método deve ter exatamente 1 parâmetro: o tipo do evento.
 *
 * Exemplo:
 *   @EventHandler
 *   public void onRender(EventRender2D.java event) { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {}