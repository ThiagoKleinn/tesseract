package com.tesseract.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;
import java.util.Random;

public class TesseractIngameMenu extends GuiScreen {

    private float tick = 0f;

    private static final int STAR_COUNT = 80;
    private final int[]   starX     = new int[STAR_COUNT];
    private final int[]   starY     = new int[STAR_COUNT];
    private final int[]   starSize  = new int[STAR_COUNT];
    private final float[] starSpeed = new float[STAR_COUNT];
    private final float[] starAlpha = new float[STAR_COUNT];

    private static final int PART_COUNT = 18;
    private final float[] partX      = new float[PART_COUNT];
    private final float[] partY      = new float[PART_COUNT];
    private final float[] partSpeedX = new float[PART_COUNT];
    private final float[] partSpeedY = new float[PART_COUNT];
    private final float[] partAlpha  = new float[PART_COUNT];
    private final float[] partSize   = new float[PART_COUNT];

    private static final int BTN_H   = 20;
    private static final int BTN_GAP = 6;
    private static final int FULL_W  = 200;
    private static final int HALF_W  = 98;

    // Animação hover — índices fixos por botão
    // 0=BackToGame, 1=Achievements, 2=Stats, 3=OpenToLan,
    // 4=Options, 5=ModOptions, 6=Quit
    private final float[] hov = new float[7];

    private boolean isSingleplayer;
    private boolean lanAvailable;

    @Override
    public void initGui() {
        isSingleplayer = mc.isSingleplayer();
        lanAvailable   = isSingleplayer && !mc.getIntegratedServer().getPublic();

        Random rng = new Random(0xC05B1CL);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = rng.nextInt(1000);
            starY[i]     = rng.nextInt(700);
            starSize[i]  = rng.nextInt(3);
            starSpeed[i] = 0.1f + rng.nextFloat() * 0.3f;
            starAlpha[i] = 0.3f + rng.nextFloat() * 0.7f;
        }
        Random prng = new Random(0xDEADBEEFL);
        for (int i = 0; i < PART_COUNT; i++) {
            partX[i]      = prng.nextFloat() * 1000f;
            partY[i]      = prng.nextFloat() * 700f;
            partSpeedX[i] = (prng.nextFloat() - 0.5f) * 0.4f;
            partSpeedY[i] = -0.1f - prng.nextFloat() * 0.2f;
            partAlpha[i]  = 0.2f + prng.nextFloat() * 0.4f;
            partSize[i]   = 1f + prng.nextFloat() * 2f;
        }
    }

    // -------------------------------------------------------------------------
    // Render

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick += 0.012f;

        TesseractMainMenu.drawCosmicBg(this);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        TesseractMainMenu.drawAuroraWaveStatic(this, tick, 0xFF1A3A8A, 0.6f,  0f,   0.7f);
        TesseractMainMenu.drawAuroraWaveStatic(this, tick, 0xFF0A4A6A, 0.4f,  1.2f, 0.5f);
        TesseractMainMenu.drawAuroraWaveStatic(this, tick, 0xFF2A1A5A, 0.35f, 2.5f, 0.45f);
        GlStateManager.disableBlend();

        drawStars();
        drawParticles();
        drawHeader();
        drawAllButtons(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // -------------------------------------------------------------------------
    // Botões

    /*
     * Layout (Y relativo a baseY, incrementando por BTN_H + BTN_GAP):
     *
     *  row 0 — [     BACK TO GAME     ]          full, idx=0
     *  row 1 — [ ACHIEVEMENTS ][ STATS ]          half+half, idx=1,2
     *  row 2 — [    OPEN TO LAN    ]              full, idx=3  (só singleplayer)
     *  row 3 — [ OPTIONS ][ MOD OPTIONS ]         half+half, idx=4,5
     *  row 4 — [ SAVE AND QUIT / DISCONNECT ]     full, idx=6
     */
    private void drawAllButtons(int mx, int my) {
        int cx   = width / 2;
        int baseY;

        if (isSingleplayer)
            baseY = height / 4 + 8;   // 5 linhas
        else
            baseY = height / 4 + 18;  // 4 linhas (sem LAN)

        // row 0 — Back to Game
        drawFull(mx, my, cx, baseY, FULL_W, "BACK TO GAME", 0, false);

        // row 1 — Achievements | Stats
        int r1 = baseY + (BTN_H + BTN_GAP);
        drawHalf(mx, my, cx - HALF_W - 2, r1, "ACHIEVEMENTS", 1, false);
        drawHalf(mx, my, cx + 2,          r1, "STATISTICS",   2, false);

        int nextRow = r1 + (BTN_H + BTN_GAP);

        if (isSingleplayer) {
            // row 2 — Open to LAN
            drawFull(mx, my, cx, nextRow, FULL_W, "OPEN TO LAN", 3, !lanAvailable);
            nextRow += (BTN_H + BTN_GAP);
        }

        // row 3 — Options | Mod Options
        drawHalf(mx, my, cx - HALF_W - 2, nextRow, "OPTIONS",      4, false);
        drawHalf(mx, my, cx + 2,          nextRow, "MOD OPTIONS",  5, false);
        nextRow += (BTN_H + BTN_GAP);

        // row 4 — Quit (destrutivo)
        String quitLabel = isSingleplayer ? "SAVE AND QUIT TO TITLE" : "DISCONNECT";
        drawFull(mx, my, cx, nextRow, FULL_W, quitLabel, 6, false);
    }

    private void drawFull(int mx, int my, int cx, int by, int w, String label, int idx, boolean disabled) {
        int bx = cx - w / 2;
        renderBtn(mx, my, bx, by, w, label, idx, disabled, idx == 6);
    }

    private void drawHalf(int mx, int my, int bx, int by, String label, int idx, boolean disabled) {
        renderBtn(mx, my, bx, by, HALF_W, label, idx, disabled, false);
    }

    private void renderBtn(int mx, int my, int bx, int by, int bw,
                           String label, int idx, boolean disabled, boolean danger) {
        boolean hovering = !disabled
                && mx >= bx && mx <= bx + bw
                && my >= by && my <= by + BTN_H;

        hov[idx] = hovering
                ? Math.min(1f, hov[idx] + 0.1f)
                : Math.max(0f, hov[idx] - 0.07f);
        float ha = disabled ? 0f : hov[idx];

        // Accent bar esquerda
        if (ha > 0f) {
            int accentColor = danger ? 0xC87A7A : 0x85B7EB;
            drawRect(bx - 2, by, bx, by + BTN_H, ((int)(ha * 0xFF) << 24) | accentColor);
        }

        // Fundo
        int bgBase = danger ? 0x5A1A1A : 0x378ADD;
        drawRect(bx, by, bx + bw, by + BTN_H,
                ((int)(0x22 + ha * 0x33) << 24) | bgBase);

        // Borda
        int bdBase  = danger ? 0x8B3A4A : 0x85B7EB;
        int bdAlpha = disabled ? 0x22 : (int)(0x44 + ha * 0x44);
        drawBorder(bx, by, bx + bw, by + BTN_H, (bdAlpha << 24) | bdBase);

        // Texto
        int lw      = mc.fontRendererObj.getStringWidth(label);
        int textCol = disabled ? 0x555555
                : danger      ? ((int)(0xAA + ha * 0x55) << 24) | 0xF0C0C0
                  :               ((int)(0xAA + ha * 0x55) << 24) | 0xC8D8F0;
        mc.fontRendererObj.drawString(label,
                bx + bw / 2 - lw / 2,
                by + BTN_H / 2 - 3,
                textCol);
    }

    // -------------------------------------------------------------------------
    // Clique

    @Override
    protected void mouseClicked(int mx, int my, int mouseButton) throws IOException {
        if (mouseButton != 0) { super.mouseClicked(mx, my, mouseButton); return; }

        int cx    = width / 2;
        int baseY = isSingleplayer ? height / 4 + 8 : height / 4 + 18;

        // row 0
        if (hitFull(mx, my, cx, baseY, FULL_W))           { onAction(0); return; }
        // row 1
        int r1 = baseY + (BTN_H + BTN_GAP);
        if (hitHalf(mx, my, cx - HALF_W - 2, r1))         { onAction(1); return; }
        if (hitHalf(mx, my, cx + 2,          r1))         { onAction(2); return; }

        int nextRow = r1 + (BTN_H + BTN_GAP);
        if (isSingleplayer) {
            if (hitFull(mx, my, cx, nextRow, FULL_W) && lanAvailable) { onAction(3); return; }
            nextRow += (BTN_H + BTN_GAP);
        }
        // row 3
        if (hitHalf(mx, my, cx - HALF_W - 2, nextRow))   { onAction(4); return; }
        if (hitHalf(mx, my, cx + 2,          nextRow))   { onAction(5); return; }
        nextRow += (BTN_H + BTN_GAP);
        // row 4
        if (hitFull(mx, my, cx, nextRow, FULL_W))         { onAction(6); return; }

        super.mouseClicked(mx, my, mouseButton);
    }

    private boolean hitFull(int mx, int my, int cx, int by, int w) {
        int bx = cx - w / 2;
        return mx >= bx && mx <= bx + w && my >= by && my <= by + BTN_H;
    }

    private boolean hitHalf(int mx, int my, int bx, int by) {
        return mx >= bx && mx <= bx + HALF_W && my >= by && my <= by + BTN_H;
    }

    private void onAction(int idx)  {
        switch (idx) {
            case 0: // Back to Game
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
                break;
            case 1: // Achievements
                if (mc.thePlayer != null)
                    mc.displayGuiScreen(new GuiAchievements(this, mc.thePlayer.getStatFileWriter()));
                break;
            case 2: // Statistics
                if (mc.thePlayer != null)
                    mc.displayGuiScreen(new GuiStats(this, mc.thePlayer.getStatFileWriter()));
                break;
            case 3: // Open to LAN
                mc.displayGuiScreen(new GuiShareToLan(this));
                break;
            case 4: // Options
                mc.displayGuiScreen(new TesseractOptions(this));
                break;
            case 5:
                FMLClientHandler.instance().showInGameModOptions(new GuiIngameMenu());
                break;
            case 6: // Quit
                mc.theWorld.sendQuittingDisconnectingPacket();
                mc.loadWorld((WorldClient) null);
                mc.displayGuiScreen(new TesseractMainMenu());
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Teclas

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }

    // -------------------------------------------------------------------------
    // Helpers visuais

    private void drawHeader() {
        String title = "GAME MENU";
        drawRect(0, 0, width, 32, 0xEE0A111E);
        drawRect(0, 32, width, 33, 0x66378ADD);
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2 + 1, 10, 0x22378ADD);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2,      9, 0xCC85B7EB);
    }

    private void drawStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = starX[i] * width  / 1000;
            int sy = starY[i] * height / 700;
            int sz = (starSize[i] == 2) ? 2 : 1;
            float blink = (float)(Math.sin(tick * starSpeed[i] * 8 + i) * 0.3 + 0.7);
            drawRect(sx, sy, sx + sz, sy + sz,
                    ((int)(starAlpha[i] * blink * 200) << 24) | 0x7BA7D4);
        }
    }

    private void drawParticles() {
        for (int i = 0; i < PART_COUNT; i++) {
            partX[i] += partSpeedX[i];
            partY[i] += partSpeedY[i];
            if (partY[i] < -4)        partY[i] = height + 4;
            if (partX[i] < -4)        partX[i] = width  + 4;
            if (partX[i] > width + 4) partX[i] = -4;
            float pulse = (float)(Math.sin(tick * 2 + i * 1.3) * 0.3 + 0.7);
            int   sz    = (int) partSize[i];
            drawRect((int)partX[i], (int)partY[i],
                    (int)partX[i] + sz, (int)partY[i] + sz,
                    ((int)(partAlpha[i] * pulse * 180) << 24) | 0x85B7EB);
        }
    }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,     y1,     x2,     y1 + 1, color);
        drawRect(x1,     y2 - 1, x2,     y2,     color);
        drawRect(x1,     y1,     x1 + 1, y2,     color);
        drawRect(x2 - 1, y1,     x2,     y2,     color);
    }
}