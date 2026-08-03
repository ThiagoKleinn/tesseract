package com.tesseract.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Random;

/**
 * Base abstrata com fundo cósmico animado.
 * Herdada por TesseractWorldSelect, TesseractMultiplayer, TesseractOptions.
 */
public abstract class TesseractCosmicScreen extends GuiScreen {

    protected float tick = 0f;

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

    protected TesseractCosmicScreen() {
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

    /** Chame no início do drawScreen da subclasse. */
    protected void drawCosmicBase() {
        tick += 0.012f;
        drawBg();
        drawAurora();
        drawStars();
        drawParticles();
    }

    private void drawBg() {
        for (int i = 0; i < height; i++) {
            float t = (float) i / height;
            int r = (int)(10 + t * 4);
            int g = (int)(17 + t * 6);
            int b = (int)(30 + t * 10);
            drawRect(0, i, width, i + 1, 0xFF000000 | (r << 16) | (g << 8) | b);
        }
    }

    private void drawAurora() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        drawWave(0xFF1A3A8A, 0.6f,  0f,    0.7f);
        drawWave(0xFF0A4A6A, 0.4f,  1.2f,  0.5f);
        drawWave(0xFF2A1A5A, 0.35f, 2.5f,  0.45f);
        GlStateManager.disableBlend();
    }

    private void drawWave(int baseColor, float amplitude, float phaseOffset, float alpha) {
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >>  8) & 0xFF;
        int b =  baseColor        & 0xFF;
        int a = (int)(alpha * 80);
        int segments = width / 2;
        int maxH     = (int)(height * 0.35f * amplitude);
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
                drawRect(x1, py, x2, py + 1, (ca << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

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

    /** Header com título da tela e linha decorativa */
    protected void drawScreenHeader(String title) {
        // Barra de topo semi-transparente
        drawRect(0, 0, width, 26, 0xCC0A111E);
        drawRect(0, 26, width, 27, 0x44378ADD);

        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2 + 1, 9, 0x22378ADD);
        mc.fontRendererObj.drawString(title, width / 2 - tw / 2,     8, 0xCC85B7EB);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}