package com.tesseract.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.Random;

public class TesseractOptions extends GuiScreen {

    private final GuiScreen parent;
    private final net.minecraft.client.settings.GameSettings settings;

    private float tick = 0f;
    private static final int SC = 80, PC = 18;
    private final int[] sx = new int[SC], sy = new int[SC], ss = new int[SC];
    private final float[] sp = new float[SC], sa = new float[SC];
    private final float[] px = new float[PC], py = new float[PC];
    private final float[] pvx = new float[PC], pvy = new float[PC];
    private final float[] pa = new float[PC], ps = new float[PC];

    // Botões da tela de options
    private static final String[][] BUTTONS = {
            {"Skin Customization...", "Music & Sounds..."},
            {"Video Settings...",     "Controls..."},
            {"Language...",           "Chat Settings..."},
            {"Resource Packs...",     "Snooper Settings..."},
    };
    private static final int BTN_W = 150, BTN_H = 20, BTN_GAP = 4;

    // Hover animado
    private final float[] btnHover = new float[11];
    private int hoveredBtn = -1;

    public TesseractOptions(GuiScreen parent) {
        this.parent   = parent;
        this.settings = Minecraft.getMinecraft().gameSettings;
        Random r = new Random(0xC05B1CL);
        for (int i = 0; i < SC; i++) { sx[i]=r.nextInt(1000); sy[i]=r.nextInt(700); ss[i]=r.nextInt(3); sp[i]=0.1f+r.nextFloat()*0.3f; sa[i]=0.3f+r.nextFloat()*0.7f; }
        Random p = new Random(0xDEADBEEFL);
        for (int i = 0; i < PC; i++) { px[i]=p.nextFloat()*1000; py[i]=p.nextFloat()*700; pvx[i]=(p.nextFloat()-0.5f)*0.4f; pvy[i]=-0.1f-p.nextFloat()*0.2f; pa[i]=0.2f+p.nextFloat()*0.4f; ps[i]=1f+p.nextFloat()*2f; }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick += 0.012f;
        drawBg(); drawAurora(); drawStars(); drawParticles();
        drawHeader("OPTIONS");
        drawFovSlider(mouseX, mouseY);
        drawOptionButtons(mouseX, mouseY);
        drawDoneButton(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawFovSlider(int mouseX, int mouseY) {
        // FOV slider simples no topo
        int sx2 = width / 2 - BTN_W - BTN_GAP / 2;
        int sy2 = 40;
        String fovLabel = "FOV: " + (int) settings.fovSetting;
        drawStyledButton(sx2, sy2, BTN_W, BTN_H, fovLabel, 0, mouseX, mouseY);
        // Realms notifications
        String realmsLabel = "Realms: " + (settings.enableVsync ? "ON" : "OFF");
        drawStyledButton(sx2 + BTN_W + BTN_GAP, sy2, BTN_W, BTN_H, realmsLabel, 1, mouseX, mouseY);
    }

    private void drawOptionButtons(int mouseX, int mouseY) {
        int startX = width / 2 - BTN_W - BTN_GAP / 2;
        int startY = 70;
        int idx = 2;
        for (String[] row : BUTTONS) {
            for (int col = 0; col < row.length; col++) {
                int bx = startX + col * (BTN_W + BTN_GAP);
                int by = startY + (idx - 2) / 2 * (BTN_H + BTN_GAP);
                drawStyledButton(bx, by, BTN_W, BTN_H, row[col], idx, mouseX, mouseY);
                idx++;
            }
        }
    }

    private void drawDoneButton(int mouseX, int mouseY) {
        int bx = width / 2 - BTN_W / 2;
        int by = 70 + BUTTONS.length * (BTN_H + BTN_GAP) + 4;
        drawStyledButton(bx, by, BTN_W, BTN_H, "Done", 11, mouseX, mouseY);
    }

    private void drawStyledButton(int bx, int by, int bw, int bh, String label, int idx, int mouseX, int mouseY) {
        boolean hov = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        if (hov) btnHover[idx] = Math.min(1f, btnHover[idx] + 0.1f);
        else      btnHover[idx] = Math.max(0f, btnHover[idx] - 0.07f);
        float ha = btnHover[idx];

        if (ha > 0f) drawRect(bx - 2, by, bx, by + bh, ((int)(ha * 0xFF) << 24) | 0x85B7EB);
        drawRect(bx, by, bx + bw, by + bh, ((int)(0x22 + ha * 0x33) << 24) | 0x378ADD);
        drawBorder(bx, by, bx + bw, by + bh, ((int)(0x44 + ha * 0x44) << 24) | 0x85B7EB);

        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, bx + bw / 2 - lw / 2, by + bh / 2 - 3,
                ((int)(0xAA + ha * 0x55) << 24) | 0xC8D8F0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;

        // Done
        int doneX = width / 2 - BTN_W / 2;
        int doneY = 70 + BUTTONS.length * (BTN_H + BTN_GAP) + 4;
        if (mouseX >= doneX && mouseX <= doneX + BTN_W && mouseY >= doneY && mouseY <= doneY + BTN_H) {
            mc.displayGuiScreen(parent);
            return;
        }

        // Sub-telas
        int startX = width / 2 - BTN_W - BTN_GAP / 2;
        int startY = 70;
        int idx = 0;
        for (String[] row : BUTTONS) {
            for (int col = 0; col < row.length; col++) {
                int bx = startX + col * (BTN_W + BTN_GAP);
                int by = startY + idx / 2 * (BTN_H + BTN_GAP);
                if (mouseX >= bx && mouseX <= bx + BTN_W && mouseY >= by && mouseY <= by + BTN_H) {
                    openSubScreen(row[col]);
                    return;
                }
                idx++;
            }
        }
    }

    private void openSubScreen(String label) {
        switch (label) {
            case "Video Settings...":        mc.displayGuiScreen(new GuiVideoSettings(this, settings)); break;
            case "Controls...":              mc.displayGuiScreen(new GuiControls(this, settings)); break;
            case "Language...":              mc.displayGuiScreen(new GuiLanguage(this, settings, mc.getLanguageManager())); break;
            case "Music & Sounds...":  mc.displayGuiScreen(new GuiScreenOptionsSounds(this, settings)); break;
            case "Chat Settings...": mc.displayGuiScreen(new GuiChat()); break;
            case "Resource Packs...":        mc.displayGuiScreen(new GuiScreenResourcePacks(this)); break;
            case "Skin Customization...":    mc.displayGuiScreen(new GuiCustomizeSkin(this)); break;
            case "Snooper Settings...":      mc.displayGuiScreen(new GuiSnooper(this, settings)); break;
            // case "Broadcast Settings...": not available in 1.8.9
            // case "Super Secret Settings...": not available in 1.8.9
            default: break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
    }

    // Fundo cósmico
    private void drawBg() {
        for (int i = 0; i < height; i++) {
            float t = (float) i / height;
            drawRect(0, i, width, i + 1, 0xFF000000 | ((int)(10+t*4) << 16) | ((int)(17+t*6) << 8) | (int)(30+t*10));
        }
    }
    private void drawAurora() {
        GlStateManager.enableBlend(); GlStateManager.blendFunc(770, 771);
        wave(0xFF1A3A8A,0.6f,0f,0.7f); wave(0xFF0A4A6A,0.4f,1.2f,0.5f); wave(0xFF2A1A5A,0.35f,2.5f,0.45f);
        GlStateManager.disableBlend();
    }
    private void wave(int col, float amp, float phase, float alpha) {
        int r=(col>>16)&0xFF,g=(col>>8)&0xFF,b=col&0xFF,a=(int)(alpha*80);
        int seg=width/2, maxH=(int)(height*0.35f*amp);
        for (int i=0;i<seg;i++) {
            float xn=(float)i/seg;
            float w=(float)(Math.sin(xn*4+tick+phase)*0.4+Math.sin(xn*2.3+tick*0.7+phase*1.3)*0.35+Math.sin(xn*7.1+tick*1.3+phase*0.7)*0.15+Math.sin(xn*1.5+tick*0.4+phase*2.1)*0.1);
            int h=Math.max(4,(int)(maxH*(0.5f+w*0.5f)));
            for (int y=0;y<h;y++) { float f=1f-(float)y/h; f*=f; drawRect(i*2,y,i*2+2,y+1,((int)(a*f)<<24)|(r<<16)|(g<<8)|b); }
        }
    }
    private void drawStars() {
        for (int i=0;i<SC;i++) {
            int x=sx[i]*width/1000, y=sy[i]*height/700, z=(ss[i]==2)?2:1;
            float blink=(float)(Math.sin(tick*sp[i]*8+i)*0.3+0.7);
            drawRect(x,y,x+z,y+z,((int)(sa[i]*blink*200)<<24)|0x7BA7D4);
        }
    }
    private void drawParticles() {
        for (int i=0;i<PC;i++) {
            px[i]+=pvx[i]; py[i]+=pvy[i];
            if(py[i]<-4) py[i]=height+4; if(px[i]<-4) px[i]=width+4; if(px[i]>width+4) px[i]=-4;
            float pulse=(float)(Math.sin(tick*2+i*1.3)*0.3+0.7);
            int z=(int)ps[i];
            drawRect((int)px[i],(int)py[i],(int)px[i]+z,(int)py[i]+z,((int)(pa[i]*pulse*180)<<24)|0x85B7EB);
        }
    }
    private void drawHeader(String title) {
        drawRect(0, 0, width, 26, 0xCC0A111E);
        drawRect(0, 26, width, 27, 0x44378ADD);
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, width/2-tw/2+1, 9, 0x22378ADD);
        mc.fontRendererObj.drawString(title, width/2-tw/2,   8, 0xCC85B7EB);
    }
    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,y1,x2,y1+1,color); drawRect(x1,y2-1,x2,y2,color);
        drawRect(x1,y1,x1+1,y2,color); drawRect(x2-1,y1,x2,y2,color);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}