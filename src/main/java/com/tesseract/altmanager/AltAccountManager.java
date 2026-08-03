package com.tesseract.altmanager;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AltAccountManager {

    private static final String CIPHER_KEY = "TesseractClient1"; // 16 bytes = AES-128
    private static final String FILE_NAME  = "tesseract/accounts.json";

    private final List<AltAccount> accounts = new ArrayList<>();
    private final File             saveFile;

    // -------------------------------------------------------------------------

    public AltAccountManager() {
        saveFile = new File(Minecraft.getMinecraft().mcDataDir, FILE_NAME);
        saveFile.getParentFile().mkdirs();
        load();
    }

    public boolean loginCracked(AltAccount account) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            Session session = new Session(
                    account.getEmail(),
                    "",
                    "offline",
                    "legacy"
            );
            setSession(mc, session);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginMicrosoft(AltAccount account) {
        try {
            String email    = account.getEmail();
            String password = decrypt(account.getPassword());

            com.mojang.authlib.Agent agent = com.mojang.authlib.Agent.MINECRAFT;
            com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService service =
                    new com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService(
                            new java.net.Proxy(java.net.Proxy.Type.DIRECT, null),
                            java.util.UUID.randomUUID().toString()
                    );
            com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication auth =
                    (com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication)
                            service.createUserAuthentication(agent);

            auth.setUsername(email);
            auth.setPassword(password);
            auth.logIn();

            String name  = auth.getSelectedProfile().getName();
            String uuid  = auth.getSelectedProfile().getId().toString();
            String token = auth.getAuthenticatedToken();

            Session session = new Session(name, uuid, token, "mojang");
            setSession(Minecraft.getMinecraft(), session);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setSession(Minecraft mc, Session session) throws Exception {
        java.lang.reflect.Field f = Minecraft.class.getDeclaredField("session");
        f.setAccessible(true);
        f.set(mc, session);
    }

    public void addAccount(AltAccount account) {
        accounts.add(account);
        save();
    }

    public void removeAccount(AltAccount account) {
        accounts.remove(account);
        save();
    }

    public List<AltAccount> getAccounts() { return accounts; }


    public void save() {
        JsonArray arr = new JsonArray();
        for (AltAccount acc : accounts) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type",     acc.getType().name());
            obj.addProperty("email",    acc.getEmail());
            obj.addProperty("password", acc.getPassword()); // já criptografado
            arr.add(obj);
        }
        try (FileWriter fw = new FileWriter(saveFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(arr, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!saveFile.exists()) return;
        try (FileReader fr = new FileReader(saveFile)) {
            JsonArray arr = new JsonParser().parse(fr).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                AltAccount.Type type = AltAccount.Type.valueOf(obj.get("type").getAsString());
                String email    = obj.get("email").getAsString();
                String password = obj.get("password").getAsString();
                accounts.add(new AltAccount(type, email, password));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String plain) {
        try {
            SecretKey key    = new SecretKeySpec(CIPHER_KEY.getBytes("UTF-8"), "AES");
            Cipher    cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes("UTF-8")));
        } catch (Exception e) {
            return plain;
        }
    }

    public static String decrypt(String encrypted) {
        try {
            SecretKey key    = new SecretKeySpec(CIPHER_KEY.getBytes("UTF-8"), "AES");
            Cipher    cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), "UTF-8");
        } catch (Exception e) {
            return encrypted;
        }
    }
}