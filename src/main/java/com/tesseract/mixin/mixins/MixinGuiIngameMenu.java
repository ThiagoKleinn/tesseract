package com.tesseract.mixin.mixins;

import com.tesseract.gui.TesseractIngameMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public abstract class MixinGuiIngameMenu extends GuiScreen {

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    private void onInitGui(CallbackInfo ci) {
        Minecraft.getMinecraft().displayGuiScreen(new TesseractIngameMenu());
        ci.cancel();
    }
}