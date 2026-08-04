package com.tesseract.mixin.mixins;

import com.tesseract.Tesseract;
import com.tesseract.module.modules.AmbienceModule;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    private void interceptWorldTime(CallbackInfoReturnable<Long> cir) {
        AmbienceModule mod = getAmbienceModule();
        if (mod == null || !mod.isEnabled()) return;

        AmbienceModule.SkyMode mode = mod.getSkyMode();
        switch (mode) {
            case DAY:     cir.setReturnValue(6000L);  return;
            case SUNSET:  cir.setReturnValue(12000L); return;
            case NIGHT:   cir.setReturnValue(18000L); return;
            case SUNRISE: cir.setReturnValue(23000L); return;
            default: break;
        }
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