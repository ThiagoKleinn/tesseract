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
        register("Migrator",         "migrator");
        register("Pan",              "pan");
        register("15th Anniversary", "15thanniversary");
        register("Common",           "common");
        register("Vanilla",          "vanilla");
        register("Cherry Blossom",   "cherryblossom");
        register("2011 Minecon",     "2011");
        register("2012 Minecon",     "2012");
        register("2013 Minecon",     "2013");
        register("2015 Minecon",     "2015");
        register("2016 Minecon",     "2016");
        register("Founders",         "founders");
        loadConfig();
    }

    /**
     * Registra uma cape usando o filename do PNG (sem extensão).
     * O texturePath fica: assets/customcapes/textures/capes/<filename>.png
     */
    private static void register(String displayName, String filename) {
        String texturePath = "assets/customcapes/textures/capes/" + filename + ".png";
        AVAILABLE_CAPES.add(new Cape(displayName, texturePath, false));
    }

    /** Deve ser chamado após o TextureManager estar pronto (ex: pós-login). */
    public static void loadTextures() {
        for (Cape cape : AVAILABLE_CAPES) {
            cape.loadTexture();
        }
    }

    public static ResourceLocation getCape(AbstractClientPlayer player) {
        if (selectedCape != null && isLocalPlayer(player)) {
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
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        try {
            File file = new File(Minecraft.getMinecraft().mcDataDir, "customcapes.txt");
            if (!file.exists()) return;

            String name;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                name = br.readLine();
            }

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