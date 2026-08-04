package com.tesseract.mixin.mixins;

import com.tesseract.module.modules.AmbienceModule;
import com.tesseract.Tesseract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.World; // era WorldClient

@Mixin(World.class) // era WorldClient.class
public class MixinWorld {

    @Inject(method = "getWorldTime", at = @At("RETURN"), cancellable = true)
    private void interceptWorldTime(CallbackInfoReturnable<Long> cir) {
        AmbienceModule mod = getAmbienceModule();
        if (mod == null || !mod.isEnabled()) return;

        AmbienceModule.SkyMode mode = mod.getSkyMode();
        Long fakeTime = null;

        switch (mode) {
            case DAY:     fakeTime = 6000L;  break;
            case SUNSET:  fakeTime = 12000L; break;
            case NIGHT:   fakeTime = 18000L; break;
            case SUNRISE: fakeTime = 23000L; break;
            default: break;
        }

        if (fakeTime != null) cir.setReturnValue(fakeTime);
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