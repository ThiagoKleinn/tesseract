package com.tesseract.gui;

import com.tesseract.altmanager.AltAccountManager;
import com.tesseract.altmanager.AltManagerScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.Random;

public class TesseractMainMenu extends GuiScreen {

    // -------------------------------------------------------------------------
    // Animação

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

    // -------------------------------------------------------------------------
    // Botões

    private static final String[] BTN_LABELS = {
            "SINGLEPLAYER", "MULTIPLAYER", "OPTIONS", "ALT MANAGER", "QUIT"
    };
    private static final int BTN_W   = 200;
    private static final int BTN_H   = 20;
    private static final int BTN_GAP = 6;

    private final float[] btnHoverAnim = new float[BTN_LABELS.length];

    // -------------------------------------------------------------------------

    private AltAccountManager altManager;

    // -------------------------------------------------------------------------

    public TesseractMainMenu() {
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
        altManager = new AltAccountManager();
    }

    // -------------------------------------------------------------------------
    // Render

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick += 0.012f;
        drawCosmicBackground();
        drawAurora();
        drawStars();
        drawParticles();
        drawTitle();
        drawButtons(mouseX, mouseY);
        drawVersion();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // -------------------------------------------------------------------------
    // Fundo

    static void drawCosmicBg(GuiScreen gui) {
        int rows = gui.height;
        for (int i = 0; i < rows; i++) {
            float t = (float) i / rows;
            int r = (int)(10 + t * 4);
            int g = (int)(17 + t * 6);
            int b = (int)(30 + t * 10);
            gui.drawRect(0, i, gui.width, i + 1, 0xFF000000 | (r << 16) | (g << 8) | b);
        }
    }

    private void drawCosmicBackground() {
        drawCosmicBg(this);
    }

    // -------------------------------------------------------------------------
    // Aurora

    private void drawAurora() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        drawAuroraWave(0xFF1A3A8A, 0.6f, 0f,   0.7f);
        drawAuroraWave(0xFF0A4A6A, 0.4f, 1.2f, 0.5f);
        drawAuroraWave(0xFF2A1A5A, 0.35f,2.5f, 0.45f);
        GlStateManager.disableBlend();
    }

    static void drawAuroraWaveStatic(GuiScreen gui, float tick, int baseColor,
                                     float amplitude, float phaseOffset, float alpha) {
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >>  8) & 0xFF;
        int b =  baseColor        & 0xFF;
        int a = (int)(alpha * 80);
        int segments = gui.width / 2;
        int maxH     = (int)(gui.height * 0.35f * amplitude);
        for (int sx = 0; sx < segments; sx++) {
            float xNorm = (float) sx / segments;
            float wave  = (float)(
                    Math.sin(xNorm * 4.0 + tick + phaseOffset) * 0.4 +
                            Math.sin(xNorm * 2.3 + tick * 0.7 + phaseOffset * 1.3) * 0.35 +
                            Math.sin(xNorm * 7.1 + tick * 1.3 + phaseOffset * 0.7) * 0.15 +
                            Math.sin(xNorm * 1.5 + tick * 0.4 + phaseOffset * 2.1) * 0.1
            );
            int colH = Math.max(4, (int)(maxH * (0.5f + wave * 0.5f)));
            int x1 = sx * 2, x2 = x1 + 2;
            for (int py = 0; py < colH; py++) {
                float fade = 1f - (float) py / colH;
                fade = fade * fade;
                int ca = (int)(a * fade);
                gui.drawRect(x1, py, x2, py + 1, (ca << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    private void drawAuroraWave(int baseColor, float amplitude, float phaseOffset, float alpha) {
        drawAuroraWaveStatic(this, tick, baseColor, amplitude, phaseOffset, alpha);
    }

    // -------------------------------------------------------------------------
    // Estrelas

    private void drawStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = starX[i] * width  / 1000;
            int sy = starY[i] * height / 700;
            int sz = (starSize[i] == 2) ? 2 : 1;
            float blink = (float)(Math.sin(tick * starSpeed[i] * 8 + i) * 0.3 + 0.7);
            int   a     = (int)(starAlpha[i] * blink * 200);
            drawRect(sx, sy, sx + sz, sy + sz, (a << 24) | 0x7BA7D4);
        }
    }

    // -------------------------------------------------------------------------
    // Partículas

    private void drawParticles() {
        for (int i = 0; i < PART_COUNT; i++) {
            partX[i] += partSpeedX[i];
            partY[i] += partSpeedY[i];
            if (partY[i] < -4)        partY[i] = height + 4;
            if (partX[i] < -4)        partX[i] = width + 4;
            if (partX[i] > width + 4) partX[i] = -4;
            float pulse = (float)(Math.sin(tick * 2 + i * 1.3) * 0.3 + 0.7);
            int   a     = (int)(partAlpha[i] * pulse * 180);
            int   sz    = (int)(partSize[i]);
            drawRect((int)partX[i], (int)partY[i],
                    (int)partX[i] + sz, (int)partY[i] + sz,
                    (a << 24) | 0x85B7EB);
        }
    }

    // -------------------------------------------------------------------------
    // Título — letras grandes estilo Minecraft via GL scale, sem glow box

    private void drawTitle() {
        String line1 = "TESSERACT";
        String line2 = "Client";
        float  scale = 3.0f; // letras grandes

        float glowPulse = (float)(Math.sin(tick * 2) * 0.15 + 0.85);

        // Mede largura escalada
        int rawW1 = mc.fontRendererObj.getStringWidth(line1);
        int rawW2 = mc.fontRendererObj.getStringWidth(line2);
        int scaledW1 = (int)(rawW1 * scale);

        int ty1 = height / 4 - 16;
        int ty2 = ty1 + (int)(mc.fontRendererObj.FONT_HEIGHT * scale) + 4;

        // --- TESSERACT ---
        // Sombra
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f - scaledW1 / 2f + 2, ty1 + 2, 0);
        GlStateManager.scale(scale, scale, 1f);
        mc.fontRendererObj.drawString(line1, 0, 0, 0x22378ADD);
        GlStateManager.popMatrix();

        // Texto principal com gradiente letra por letra
        float cx = width / 2f - scaledW1 / 2f;
        for (int ci = 0; ci < line1.length(); ci++) {
            String ch = String.valueOf(line1.charAt(ci));
            float  t  = (float) ci / (line1.length() - 1);
            int    r  = (int)(0x85 + t * (0xC4 - 0x85));
            int    g  = (int)(0xB7 + t * (0xA0 - 0xB7));
            int    b  = (int)(0xEB + t * (0xF0 - 0xEB));
            int color = 0xFF000000 | (r << 16) | (g << 8) | b;

            GlStateManager.pushMatrix();
            GlStateManager.translate(cx, ty1, 0);
            GlStateManager.scale(scale, scale, 1f);
            mc.fontRendererObj.drawString(ch, 0, 0, color);
            GlStateManager.popMatrix();

            cx += mc.fontRendererObj.getStringWidth(ch) * scale;
        }

        // Sublinha animada
        int lineY  = ty1 + (int)(mc.fontRendererObj.FONT_HEIGHT * scale) + 2;
        int lineX0 = width / 2 - scaledW1 / 2;
        for (int lx = lineX0; lx < lineX0 + scaledW1; lx++) {
            float t     = (float)(lx - lineX0) / scaledW1;
            float alpha = (float)(Math.sin(t * Math.PI) * glowPulse);
            int   la    = (int)(alpha * 0xBB);
            drawRect(lx, lineY, lx + 1, lineY + 1, (la << 24) | 0x85B7EB);
        }

        // --- Client (menor, sutil) ---
        float scale2   = 1.5f;
        int   scaledW2 = (int)(rawW2 * scale2);
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f - scaledW2 / 2f, lineY + 4, 0);
        GlStateManager.scale(scale2, scale2, 1f);
        mc.fontRendererObj.drawString(line2, 0, 0, 0x778899BB);
        GlStateManager.popMatrix();
    }

    // -------------------------------------------------------------------------
    // Botões

    private void drawButtons(int mouseX, int mouseY) {
        int totalH = BTN_LABELS.length * (BTN_H + BTN_GAP) - BTN_GAP;
        int startY = height / 2 - totalH / 2 + 24;
        int bx     = width / 2 - BTN_W / 2;

        for (int i = 0; i < BTN_LABELS.length; i++) {
            int by = startY + i * (BTN_H + BTN_GAP);
            boolean hov = mouseX >= bx && mouseX <= bx + BTN_W
                    && mouseY >= by && mouseY <= by + BTN_H;

            if (hov) btnHoverAnim[i] = Math.min(1f, btnHoverAnim[i] + 0.1f);
            else      btnHoverAnim[i] = Math.max(0f, btnHoverAnim[i] - 0.07f);

            float ha = btnHoverAnim[i];

            if (ha > 0f) {
                int accentA = (int)(ha * 0xFF);
                drawRect(bx - 2, by, bx, by + BTN_H, (accentA << 24) | 0x85B7EB);
            }

            int bgAlpha = (int)(0x22 + ha * 0x33);
            drawRect(bx, by, bx + BTN_W, by + BTN_H, (bgAlpha << 24) | 0x378ADD);

            int bdAlpha = (int)(0x44 + ha * 0x44);
            drawBorder(bx, by, bx + BTN_W, by + BTN_H, (bdAlpha << 24) | 0x85B7EB);

            String label = BTN_LABELS[i];
            int    lw    = mc.fontRendererObj.getStringWidth(label);
            int    textA = (int)(0xAA + ha * 0x55);
            mc.fontRendererObj.drawString(label,
                    bx + BTN_W / 2 - lw / 2,
                    by + BTN_H / 2 - 3,
                    (textA << 24) | 0xC8D8F0);
        }
    }

    private void drawVersion() {
        mc.fontRendererObj.drawString("Tesseract v1.0.0 | MC 1.8.9",
                4, height - 10, 0x33AAAAAA);
        String user = "Playing as: §b" + mc.getSession().getUsername();
        int uw = mc.fontRendererObj.getStringWidth(user.replaceAll("§.", ""));
        mc.fontRendererObj.drawString(user, width - uw - 20, height - 10, 0x4485B7EB);
    }

    // -------------------------------------------------------------------------
    // Mouse

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) { super.mouseClicked(mouseX, mouseY, mouseButton); return; }

        int totalH = BTN_LABELS.length * (BTN_H + BTN_GAP) - BTN_GAP;
        int startY = height / 2 - totalH / 2 + 24;
        int bx     = width / 2 - BTN_W / 2;

        for (int i = 0; i < BTN_LABELS.length; i++) {
            int by = startY + i * (BTN_H + BTN_GAP);
            if (mouseX >= bx && mouseX <= bx + BTN_W
                    && mouseY >= by && mouseY <= by + BTN_H) {
                onButtonClick(i);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void onButtonClick(int index) {
        switch (index) {
            case 0: mc.displayGuiScreen(new TesseractWorldSelect(this));      break;
            case 1: mc.displayGuiScreen(new TesseractMultiplayer(this));      break;
            case 2: mc.displayGuiScreen(new TesseractOptions(this));          break;
            case 3: mc.displayGuiScreen(new AltManagerScreen(this, altManager)); break;
            case 4: mc.shutdown();                                              break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {}

    @Override public boolean doesGuiPauseGame() { return false; }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,     y1,     x2,     y1 + 1, color);
        drawRect(x1,     y2 - 1, x2,     y2,     color);
        drawRect(x1,     y1,     x1 + 1, y2,     color);
        drawRect(x2 - 1, y1,     x2,     y2,     color);
    }
}