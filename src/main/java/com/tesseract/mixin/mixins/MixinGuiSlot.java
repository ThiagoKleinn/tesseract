package com.tesseract.mixin.mixins;

// mixin/MixinGuiSlot.java

import com.tesseract.gui.TesseractMultiplayer;
import com.tesseract.gui.TesseractWorldSelect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public class MixinGuiSlot {

    @Inject(method = "drawContainerBackground", at = @At("HEAD"), cancellable = true)
    private void cancelDirtBg(Tessellator tessellator, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof TesseractWorldSelect ||
                mc.currentScreen instanceof TesseractMultiplayer) {
            ci.cancel();
        }
    }

}
