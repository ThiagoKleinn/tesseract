package com.tesseract.core;

import com.tesseract.Tesseract;
import com.tesseract.event.events.EventKey;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.event.events.EventRenderTick;
import com.tesseract.event.events.EventTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Listener registrado no Forge Event Bus.
 * Traduz eventos do Forge para o nosso EventBus interno.
 *
 * Registrar em Tesseract com:
 *   MinecraftForge.EVENT_BUS.register(new ForgeEventListener());
 */
public class ForgeEventListener {

    private final Minecraft mc = Minecraft.getMinecraft();

    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null) return;

        EventTick.Phase phase = event.phase == TickEvent.Phase.START
                ? EventTick.Phase.PRE
                : EventTick.Phase.POST;

        Tesseract.instance().getEventBus().post(new EventTick(phase));
    }

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Post event) {
        // Só dispara uma vez por frame (no tipo TEXT, que vem por último)
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.theWorld == null) return;

        ScaledResolution res = new ScaledResolution(mc);
        Tesseract.instance().getEventBus().post(new EventRender2D(res, event.partialTicks));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Tesseract.instance().getEventBus().post(
                new EventRenderTick(event.renderTickTime)
        );
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // Verifica keybinds de todos os módulos
        org.lwjgl.input.Keyboard.getEventKey();
        int key = org.lwjgl.input.Keyboard.getEventKey();

        if (key != 0 && org.lwjgl.input.Keyboard.getEventKeyState()) {
            Tesseract.instance().getModuleManager().onKeyPress(key);
            Tesseract.instance().getEventBus().post(new EventKey(key));
        }
    }
}