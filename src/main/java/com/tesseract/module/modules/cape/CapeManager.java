package com.tesseract.module.modules.cape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CapeManager {

    public static final List<Cape> AVAILABLE_CAPES = new ArrayList<>();
    private static Cape selectedCape = null;

    public static void init() {
        // Só registra os nomes, sem carregar texturas
        AVAILABLE_CAPES.add(new Cape("Migrator", "assets/customcapes/textures/capes/migrator.png", false));
        AVAILABLE_CAPES.add(new Cape("Pan",    "assets/customcapes/textures/capes/pan.png", false));
        AVAILABLE_CAPES.add(new Cape("15th Anniversary",  "assets/customcapes/textures/capes/15thanniversary.png", false));
        AVAILABLE_CAPES.add(new Cape("Common",  "assets/customcapes/textures/capes/common.png", false));
        AVAILABLE_CAPES.add(new Cape("Vanilla",  "assets/customcapes/textures/capes/vanilla.png", false));
        AVAILABLE_CAPES.add(new Cape("Cherry Blossom",  "assets/customcapes/textures/capes/cherryblossom.png", false));
        // Minecons
        AVAILABLE_CAPES.add(new Cape("2011 Minecon",  "assets/customcapes/textures/capes/2011.png", false));
        AVAILABLE_CAPES.add(new Cape("2012 Minecon",  "assets/customcapes/textures/capes/2012.png", false));
        AVAILABLE_CAPES.add(new Cape("2013 Minecon",  "assets/customcapes/textures/capes/2013.png", false));
        AVAILABLE_CAPES.add(new Cape("2015 Minecon",  "assets/customcapes/textures/capes/2015.png", false));
        AVAILABLE_CAPES.add(new Cape("2016 Minecon",  "assets/customcapes/textures/capes/2016.png", false));
        // Minecon Live 2019
        AVAILABLE_CAPES.add(new Cape("Founders",  "assets/customcapes/textures/capes/founders.png", false));
        loadConfig();
    }

    public static void loadTextures() {
        for (Cape cape : AVAILABLE_CAPES) {
            cape.loadTexture();
        }
    }

    public static ResourceLocation getCape(AbstractClientPlayer player) {
        if (isLocalPlayer(player) && selectedCape != null) {
            return selectedCape.resource;
        }
        return null;
    }

    public static boolean hasCape(AbstractClientPlayer player) {
        return getCape(player) != null;
    }

    public static void selectCape(Cape cape) {
        selectedCape = cape;
        saveConfig(cape.name);
    }

    public static void clearCape() {
        selectedCape = null;
        saveConfig("");
    }

    public static Cape getSelectedCape() {
        return selectedCape;
    }

    private static boolean isLocalPlayer(AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null &&
                player.getUniqueID().equals(mc.thePlayer.getUniqueID());
    }

    private static void saveConfig(String name) {
        try {
            File file = new File(Minecraft.getMinecraft().mcDataDir, "customcapes.txt");
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.println(name);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        try {
            File file = new File(Minecraft.getMinecraft().mcDataDir, "customcapes.txt");
            if (!file.exists()) return;

            BufferedReader br = new BufferedReader(new FileReader(file));
            String name = br.readLine();
            br.close();

            if (name != null && !name.isEmpty()) {
                for (Cape cape : AVAILABLE_CAPES) {
                    if (cape.name.equals(name)) {
                        selectedCape = cape;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}