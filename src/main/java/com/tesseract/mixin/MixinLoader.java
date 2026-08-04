package com.tesseract.mixin;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("TesseractMixinLoader")
@IFMLLoadingPlugin.SortingIndex(1001)
public class MixinLoader implements IFMLLoadingPlugin {

    static {
        try {
            // Adiciona o jar do mixin ao URLClassLoader pai
            File mixinJar = new File(
                    Launch.minecraftHome,
                    "mods/mixin-0.7.11.jar"
            );
            if (mixinJar.exists()) {
                Method addURL = java.net.URLClassLoader.class
                        .getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                ClassLoader parent = Launch.classLoader.getClass()
                        .getClassLoader();
                addURL.invoke(parent, mixinJar.toURI().toURL());
                addURL.invoke(Launch.classLoader, mixinJar.toURI().toURL());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.tesseract.json");
        MixinEnvironment.getDefaultEnvironment()
                .setObfuscationContext("searge");
    }

    @Override public String[] getASMTransformerClass()   { return new String[0]; }
    @Override public String   getModContainerClass()      { return null; }
    @Override public String   getSetupClass()             { return null; }
    @Override public void     injectData(Map<String, Object> data) {}
    @Override public String   getAccessTransformerClass() { return null; }
}