package com.tesseract.gui;

import com.tesseract.altmanager.AltManagerScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.Random;

/**
 * TesseractMainMenu — substitui o GuiMainMenu vanilla.
 * Aurora cósmica animada + botões estilizados.
 * Registrado via ForgeEventListener no evento GuiOpenEvent.
 */
public class TesseractMainMenu extends GuiScreen {

    // -------------------------------------------------------------------------
    // Animação — aurora

    private float  tick       = 0f;
    private static final int   STAR_COUNT = 80;
    private final  int[]       starX      = new int[STAR_COUNT];
    private final  int[]       starY      = new int[STAR_COUNT];
    private final  int[]       starSize   = new int[STAR_COUNT];
    private final  float[]     starSpeed  = new float[STAR_COUNT];
    private final  float[]     starAlpha  = new float[STAR_COUNT];

    // Partículas flutuantes
    private static final int   PART_COUNT = 18;
    private final  float[]     partX      = new float[PART_COUNT];
    private final  float[]     partY      = new float[PART_COUNT];
    private final  float[]     partSpeedX = new float[PART_COUNT];
    private final  float[]     partSpeedY = new float[PART_COUNT];
    private final  float[]     partAlpha  = new float[PART_COUNT];
    private final  float[]     partSize   = new float[PART_COUNT];

    // -------------------------------------------------------------------------
    // Botões

    // [label, largura]
    private static final String[] BTN_LABELS = {
            "SINGLEPLAYER", "MULTIPLAYER", "OPTIONS", "ALT MANAGER", "QUIT"
    };
    private static final int BTN_W  = 200;
    private static final int BTN_H  = 20;
    private static final int BTN_GAP = 6;

    // Hover state
    private int hoveredBtn = -1;
    // Animação de hover (alpha 0..1 por botão)
    private final float[] btnHoverAnim = new float[BTN_LABELS.length];

    // -------------------------------------------------------------------------
    // AltManager
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
    // Fundo cósmico base

    private void drawCosmicBackground() {
        // Gradiente vertical azul escuro → quase preto
        int rows = height;
        for (int i = 0; i < rows; i++) {
            float t     = (float) i / rows;
            int   r     = (int)(10  + t * 4);
            int   g     = (int)(17  + t * 6);
            int   b     = (int)(30  + t * 10);
            int   color = 0xFF000000 | (r << 16) | (g << 8) | b;
            drawRect(0, i, width, i + 1, color);
        }
    }

    // -------------------------------------------------------------------------
    // Aurora animada — ondas de luz cósmica

    private void drawAurora() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA

        // Três camadas de aurora com cores e fases diferentes
        drawAuroraWave(0xFF1A3A8A, 0.6f,  0f,    0.7f);  // azul profundo
        drawAuroraWave(0xFF0A4A6A, 0.4f,  1.2f,  0.5f);  // ciano escuro
        drawAuroraWave(0xFF2A1A5A, 0.35f, 2.5f,  0.45f); // roxo

        GlStateManager.disableBlend();
    }

    /**
     * Desenha uma onda de aurora no topo da tela.
     * baseColor: cor ARGB base.
     * amplitude: altura da onda (0..1 relativo a height/3).
     * phaseOffset: deslocamento de fase para variação entre camadas.
     * alpha: transparência da camada.
     */
    private void drawAuroraWave(int baseColor, float amplitude, float phaseOffset, float alpha) {
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >>  8) & 0xFF;
        int b =  baseColor        & 0xFF;
        int a = (int)(alpha * 80);

        int segments = width / 2;
        int maxH     = (int)(height * 0.35f * amplitude);

        for (int sx = 0; sx < segments; sx++) {
            float xNorm = (float) sx / segments;

            // Onda composta de senos para parecer orgânica
            float wave = (float)(
                    Math.sin(xNorm * 4.0 + tick + phaseOffset) * 0.4 +
                            Math.sin(xNorm * 2.3 + tick * 0.7 + phaseOffset * 1.3) * 0.35 +
                            Math.sin(xNorm * 7.1 + tick * 1.3 + phaseOffset * 0.7) * 0.15 +
                            Math.sin(xNorm * 1.5 + tick * 0.4 + phaseOffset * 2.1) * 0.1
            );

            // Altura da coluna neste ponto
            int colH = (int)(maxH * (0.5f + wave * 0.5f));
            colH = Math.max(4, colH);

            int x1 = sx * 2;
            int x2 = x1 + 2;

            // Gradiente vertical dentro da coluna (mais forte no topo, fade para baixo)
            for (int py = 0; py < colH; py++) {
                float fade = 1f - (float) py / colH;
                fade = fade * fade; // quadrático para fade mais suave
                int ca = (int)(a * fade);
                int color = (ca << 24) | (r << 16) | (g << 8) | b;
                drawRect(x1, py, x2, py + 1, color);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Estrelas

    private void drawStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = starX[i] * width  / 1000;
            int sy = starY[i] * height / 700;
            int sz = (starSize[i] == 2) ? 2 : 1;

            // Pisca suavemente
            float blink = (float)(Math.sin(tick * starSpeed[i] * 8 + i) * 0.3 + 0.7);
            int   a     = (int)(starAlpha[i] * blink * 200);
            drawRect(sx, sy, sx + sz, sy + sz, (a << 24) | 0x7BA7D4);
        }
    }

    // -------------------------------------------------------------------------
    // Partículas flutuantes

    private void drawParticles() {
        for (int i = 0; i < PART_COUNT; i++) {
            // Move partícula
            partX[i] += partSpeedX[i];
            partY[i] += partSpeedY[i];

            // Wrap
            if (partY[i] < -4)     partY[i] = height + 4;
            if (partX[i] < -4)     partX[i] = width + 4;
            if (partX[i] > width + 4) partX[i] = -4;

            // Pulsa
            float pulse = (float)(Math.sin(tick * 2 + i * 1.3) * 0.3 + 0.7);
            int   a     = (int)(partAlpha[i] * pulse * 180);
            int   sz    = (int)(partSize[i]);

            drawRect((int)partX[i], (int)partY[i],
                    (int)partX[i] + sz, (int)partY[i] + sz,
                    (a << 24) | 0x85B7EB);
        }
    }

    // -------------------------------------------------------------------------
    // Título TESSERACT

    private void drawTitle() {
        String title    = "TESSERACT";
        String subtitle = "Client";

        // Espaçamento estilo pixel art — desenha letra por letra com gap
        int letterGap = 3;
        int charW     = mc.fontRendererObj.getStringWidth("A");
        int totalW    = mc.fontRendererObj.getStringWidth(title)
                + (title.length() - 1) * letterGap;
        int tx = width / 2 - totalW / 2;
        int ty = height / 4 - 16;

        // Glow animado atrás do título
        float glowPulse = (float)(Math.sin(tick * 2) * 0.15 + 0.85);
        int   glowAlpha = (int)(glowPulse * 0x33);
        int   glowW     = totalW + 40;
        for (int gi = 0; gi < 10; gi++) {
            int ga = glowAlpha - gi * 3;
            if (ga <= 0) break;
            drawRect(tx - 20 - gi, ty - 4 - gi,
                    tx - 20 - gi + glowW + gi * 2, ty - 4 - gi + 20 + gi * 2,
                    (ga << 24) | 0x378ADD);
        }

        // Sombra do título
        int cx = tx;
        for (int ci = 0; ci < title.length(); ci++) {
            String ch = String.valueOf(title.charAt(ci));
            mc.fontRendererObj.drawString(ch, cx + 1, ty + 1, 0x11378ADD);
            cx += mc.fontRendererObj.getStringWidth(ch) + letterGap;
        }

        // Título principal com gradiente simulado (duas passagens)
        cx = tx;
        for (int ci = 0; ci < title.length(); ci++) {
            String ch = String.valueOf(title.charAt(ci));
            float t   = (float) ci / (title.length() - 1);
            // Vai de 0x85B7EB (azul claro) → 0xC4A0F0 (roxo) nos extremos
            int r = (int)(0x85 + t * (0xC4 - 0x85));
            int g = (int)(0xB7 + t * (0xA0 - 0xB7));
            int b = (int)(0xEB + t * (0xF0 - 0xEB));
            mc.fontRendererObj.drawString(ch, cx, ty, 0xFF000000 | (r << 16) | (g << 8) | b);
            cx += mc.fontRendererObj.getStringWidth(ch) + letterGap;
        }

        // Sublinha decorativa
        int lineY = ty + mc.fontRendererObj.FONT_HEIGHT + 3;
        for (int lx = tx; lx < tx + totalW; lx++) {
            float t     = (float)(lx - tx) / totalW;
            float alpha = (float)(Math.sin(t * Math.PI) * glowPulse);
            int   la    = (int)(alpha * 0xAA);
            drawRect(lx, lineY, lx + 1, lineY + 1, (la << 24) | 0x85B7EB);
        }

        // Subtítulo
        int sw = mc.fontRendererObj.getStringWidth(subtitle);
        mc.fontRendererObj.drawString(subtitle,
                width / 2 - sw / 2, lineY + 4, 0x558899BB);
    }

    // -------------------------------------------------------------------------
    // Botões

    private void drawButtons(int mouseX, int mouseY) {
        int totalH = BTN_LABELS.length * (BTN_H + BTN_GAP) - BTN_GAP;
        int startY = height / 2 - totalH / 2 + 20;
        int bx     = width / 2 - BTN_W / 2;

        for (int i = 0; i < BTN_LABELS.length; i++) {
            int by = startY + i * (BTN_H + BTN_GAP);
            boolean hov = mouseX >= bx && mouseX <= bx + BTN_W
                    && mouseY >= by && mouseY <= by + BTN_H;

            // Anima hover
            if (hov) btnHoverAnim[i] = Math.min(1f, btnHoverAnim[i] + 0.1f);
            else      btnHoverAnim[i] = Math.max(0f, btnHoverAnim[i] - 0.07f);

            float ha = btnHoverAnim[i];

            // Borda lateral esquerda colorida (aparece no hover)
            if (ha > 0f) {
                int accentA = (int)(ha * 0xFF);
                drawRect(bx - 2, by, bx, by + BTN_H, (accentA << 24) | 0x85B7EB);
            }

            // Fundo do botão
            int bgAlpha = (int)(0x22 + ha * 0x33);
            drawRect(bx, by, bx + BTN_W, by + BTN_H, (bgAlpha << 24) | 0x378ADD);

            // Borda
            int bdAlpha = (int)(0x44 + ha * 0x44);
            drawBorder(bx, by, bx + BTN_W, by + BTN_H, (bdAlpha << 24) | 0x85B7EB);

            // Texto
            String label = BTN_LABELS[i];
            int    lw    = mc.fontRendererObj.getStringWidth(label);
            int    textA = (int)(0xAA + ha * 0x55);
            int    textC = (textA << 24) | 0xC8D8F0;
            mc.fontRendererObj.drawString(label,
                    bx + BTN_W / 2 - lw / 2,
                    by + BTN_H / 2 - 3, textC);

            // Indicador hoveredBtn para mouseClicked
            if (hov) hoveredBtn = i;
        }
    }

    private void drawVersion() {
        String ver = "Tesseract v1.0.0 | MC 1.8.9";
        mc.fontRendererObj.drawString(ver, 4, height - 10, 0x33AAAAAA);

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
        int startY = height / 2 - totalH / 2 + 20;
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
            case 0: mc.displayGuiScreen(new GuiSelectWorld(this));   break;
            case 1: mc.displayGuiScreen(new GuiMultiplayer(this));   break;
            case 2: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
            case 3: mc.displayGuiScreen(new AltManagerScreen(this, altManager)); break;
            case 4: mc.shutdown();                                    break;
        }
    }

    // -------------------------------------------------------------------------
    // Teclado

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // ESC não faz nada no main menu
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    // -------------------------------------------------------------------------

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,     y1,     x2,     y1 + 1, color);
        drawRect(x1,     y2 - 1, x2,     y2,     color);
        drawRect(x1,     y1,     x1 + 1, y2,     color);
        drawRect(x2 - 1, y1,     x2,     y2,     color);
    }
}