package com.tesseract.module.modules;

import com.tesseract.module.BaseModule;
import com.tesseract.module.modules.cape.CapeManager;
import com.tesseract.module.modules.cape.CapeSelectionGui;
import com.tesseract.module.modules.cape.CustomCapeLayer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Map;

/**
 * CapeModule — gerencia capas customizadas dentro do Tesseract.
 *
 * Responsabilidades (antes espalhadas em CapesMod + CapeRenderer + KeyHandler):
 *  - Injetar o CustomCapeLayer nos renderers ao primeiro tick
 *  - Carregar texturas das capas ao primeiro tick
 *  - Abrir a CapeSelectionGui via keybind (padrão: K)
 *  - Chamar CapeManager.init() no construtor
 *
 * Uso:
 *   moduleManager.register(new CapeModule());
 *   // toggle() ou setEnabled(true) ativa o módulo
 */
public class CapeModule extends BaseModule {

    private boolean layerInjected  = false;
    private boolean capesLoaded    = false;

    public CapeModule() {
        super(
                "Capas",
                "Capas customizadas para o seu personagem",
                Category.COSMETICS,
                Keyboard.KEY_K   // tecla para abrir o menu — troque se quiser
        );

        // Inicializa a lista de capas disponíveis (sem carregar texturas ainda)
        CapeManager.init();
    }

    // -------------------------------------------------------------------------
    // Ciclo de vida

    @Override
    public void onEnable() {
        // Nada extra: o @SubscribeEvent só roda enquanto o módulo está registrado
        // no EventBus, o que BaseModule já garante ao ativar.
    }

    @Override
    public void onDisable() {
        // Ao desativar, o layer já foi injetado e permanece — remover exigiria
        // reiniciar o RenderManager, que não vale a pena. O layer checa
        // CapeManager.hasCape() internamente e não renderiza nada se não houver capa.
    }

    // -------------------------------------------------------------------------
    // Eventos (só executam quando o módulo está enabled e registrado no EventBus)

    /**
     * Tick de render: injeta o layer e carrega texturas uma única vez,
     * e escuta a keybind para abrir o menu.
     */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        // Carrega texturas na primeira oportunidade (contexto OpenGL disponível)
        if (!capesLoaded) {
            CapeManager.loadTextures();
            capesLoaded = true;
        }

        // Injeta o CustomCapeLayer nos dois skin types (default / slim)
        if (!layerInjected) {
            injectCapeLayer(mc.getRenderManager());
            layerInjected = true;
        }
    }

    /**
     * Tick do cliente: abre a GUI quando a keybind é pressionada.
     * Usamos ClientTickEvent para checar input fora do render.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return; // não abre sobre outra GUI

        if (Keyboard.isKeyDown(getKeybind())) {
            // Abre a GUI no próximo tick para evitar conflito com o estado do teclado
            mc.addScheduledTask(() -> mc.displayGuiScreen(new CapeSelectionGui()));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers

    /**
     * Remove o LayerCape vanilla e adiciona o CustomCapeLayer em ambos os
     * renderers (skin padrão e slim/alex), exatamente como o CapeRenderer original.
     */
    private void injectCapeLayer(RenderManager rm) {
        for (Map.Entry<String, RenderPlayer> entry : rm.getSkinMap().entrySet()) {
            RenderPlayer renderer = entry.getValue();

            // Remove a capa vanilla para evitar sobreposição
            try {
                java.lang.reflect.Field field =
                        net.minecraft.client.renderer.entity.RendererLivingEntity.class
                                .getDeclaredField("layerRenderers");
                field.setAccessible(true);

                @SuppressWarnings("unchecked")
                java.util.List<LayerRenderer<?>> layers =
                        (java.util.List<LayerRenderer<?>>) field.get(renderer);

                layers.removeIf(
                        layer -> layer instanceof net.minecraft.client.renderer.entity.layers.LayerCape
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            renderer.addLayer(new CustomCapeLayer(renderer));
        }
    }
}