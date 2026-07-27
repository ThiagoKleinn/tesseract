package com.tesseract.module.modules;

import com.tesseract.event.EventHandler;
import com.tesseract.event.events.EventRender2D;
import com.tesseract.module.BaseModule;
import com.tesseract.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;

/**
 * Armor HUD — exibe os 4 slots de armadura com durabilidade.
 * Posição padrão: canto inferior esquerdo, acima da hotbar.
 */
public class ArmorHUDModule extends BaseModule {

    public ArmorHUDModule() {
        super("ArmorHUD", "Exibe os itens de armadura equipados.", Category.MODS);
        setEnabled(true);
    }

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.thePlayer == null) return;

        ScaledResolution res = event.getResolution();
        int startX = 2;
        int startY = res.getScaledHeight() - 55; // acima da hotbar

        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;

        // Renderiza do capacete (índice 3) até as botas (índice 0) de cima pra baixo
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = armor[i];
            if (stack == null) continue;

            int slotY = startY + (3 - i) * 18;
            RenderUtil.drawItem(stack, startX, slotY);
        }
    }
}