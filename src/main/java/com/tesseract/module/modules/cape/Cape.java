package com.tesseract.module.modules.cape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class Cape {

    public final String name;
    public final ResourceLocation resource;
    private final String texturePath;

    public Cape(String name, String texturePath, boolean loadNow) {
        this.name = name;
        this.texturePath = texturePath;
        // domínio "customcapes", path "textures/capes/<name>" — alinha com assets/customcapes/...
        this.resource = new ResourceLocation("customcapes",
                "textures/capes/" + name.toLowerCase().replace(" ", "_") + ".png");
        if (loadNow) loadTexture();
    }

    public void loadTexture() {
        try {
            // texturePath já é relativo ao classpath: "assets/customcapes/textures/capes/xxx.png"
            InputStream is = Cape.class.getClassLoader().getResourceAsStream(texturePath);
            if (is == null) {
                System.err.println("[Cape] Texture not found on classpath: " + texturePath);
                return;
            }
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                System.err.println("[Cape] Failed to read image: " + texturePath);
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() ->
                    mc.getTextureManager().loadTexture(resource, new DynamicTexture(image))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}