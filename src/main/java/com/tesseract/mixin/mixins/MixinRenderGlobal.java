package com.tesseract.mixin.mixins;

import com.tesseract.Tesseract;
import com.tesseract.module.modules.AmbienceModule;
import net.minecraft.client.renderer.RenderGlobal;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(
            method = "renderSky(FI)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interceptRenderSky(float partialTicks, int pass, CallbackInfo ci) {
        AmbienceModule mod = getAmbienceModule();
        if (mod == null || !mod.isEnabled()) return;

        AmbienceModule.SkyMode mode = mod.getSkyMode();
        if (mode == AmbienceModule.SkyMode.NETHER) {
            renderNetherSky();
            ci.cancel();
        } else if (mode == AmbienceModule.SkyMode.END) {
            renderEndSky();
            ci.cancel();
        }
    }

    private void renderNetherSky() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glClearColor(0.20f, 0.03f, 0.03f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderEndSky() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glClearColor(0.07f, 0.02f, 0.10f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private AmbienceModule getAmbienceModule() {
        try {
            return (AmbienceModule) Tesseract.instance()
                    .getModuleManager()
                    .getModule(AmbienceModule.class);
        } catch (Exception e) {
            return null;
        }
    }
}