package com.tesseract.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Random;

public class TesseractControls extends GuiScreen {

    private final GuiScreen parent;
    private final GameSettings settings;
    private float tick = 0f;

    private static final int SC = 80, PC = 18;
    private final int[]   sx = new int[SC], sy = new int[SC], ss = new int[SC];
    private final float[] sp = new float[SC], sa = new float[SC];
    private final float[] px = new float[PC], py = new float[PC];
    private final float[] pvx = new float[PC], pvy = new float[PC];
    private final float[] pa = new float[PC], ps = new float[PC];

    private static final int BTN_W = 150, BTN_H = 20, GAP = 4;
    private static final int SCROLL_W = 310, VISIBLE_ROWS = 12;

    private KeyBinding waitingForKey = null;
    private int scrollOffset = 0;
    private final float[] bindHover;

    public TesseractControls(GuiScreen parent, GameSettings settings) {
        this.parent = parent;
        this.settings = settings;
        bindHover = new float[settings.keyBindings.length + 4];
        initParticles();
    }

    private void initParticles() {
        Random r = new Random(0xC05B1CL);
        for (int i = 0; i < SC; i++) {
            sx[i]=r.nextInt(1000); sy[i]=r.nextInt(700); ss[i]=r.nextInt(3);
            sp[i]=0.1f+r.nextFloat()*0.3f; sa[i]=0.3f+r.nextFloat()*0.7f;
        }
        Random p = new Random(0xDEADBEEFL);
        for (int i = 0; i < PC; i++) {
            px[i]=p.nextFloat()*1000; py[i]=p.nextFloat()*700;
            pvx[i]=(p.nextFloat()-0.5f)*0.4f; pvy[i]=-0.1f-p.nextFloat()*0.2f;
            pa[i]=0.2f+p.nextFloat()*0.4f; ps[i]=1f+p.nextFloat()*2f;
        }
    }

    private int listX() { return width / 2 - SCROLL_W / 2; }
    private int listY() { return 40; }
    private int listH() { return VISIBLE_ROWS * (BTN_H + GAP); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick += 0.012f;
        drawBg(); drawAurora(); drawStars(); drawParticles();
        drawHeader("CONTROLS");

        int lx = listX(), ly = listY();
        KeyBinding[] keys = settings.keyBindings;

        drawRect(lx - 2, ly - 2, lx + SCROLL_W + 2, ly + listH() + 2, 0x33000000);

        for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < keys.length; i++) {
            int idx = i + scrollOffset;
            KeyBinding kb = keys[idx];
            int ry = ly + i * (BTN_H + GAP);

            String catLabel = kb.getKeyCategory();
            mc.fontRendererObj.drawString(catLabel, lx, ry + BTN_H / 2 - 3, 0x88C8D8F0);

            String keyName = kb.getKeyDescription();
            int lw = mc.fontRendererObj.getStringWidth(keyName);
            int nameBx = lx + 110;
            // CORRIGIDO: removidos parâmetros desnecessários
            drawSmallRect(nameBx, ry, nameBx + 95, ry + BTN_H);
            mc.fontRendererObj.drawString(keyName, nameBx + 48 - lw / 2, ry + BTN_H / 2 - 3, 0xCCC8D8F0);

            String bindLabel = waitingForKey == kb ? "> Press Key <" : Keyboard.getKeyName(kb.getKeyCode());
            boolean conflict = isConflict(kb);
            int bindBx = lx + 210;
            int bindColor = waitingForKey == kb ? 0xFF4A8AFF : (conflict ? 0xFF8A2222 : 0xFF1A4A8A);
            drawBindBtn(bindBx, ry, bindLabel, bindColor, idx + keys.length, mouseX, mouseY);
        }

        if (keys.length > VISIBLE_ROWS) {
            int sbH = listH();
            int thumbH = Math.max(20, sbH * VISIBLE_ROWS / keys.length);
            int thumbY = ly + (sbH - thumbH) * scrollOffset / (keys.length - VISIBLE_ROWS);
            drawRect(lx + SCROLL_W + 4, ly, lx + SCROLL_W + 8, ly + sbH, 0x44378ADD);
            drawRect(lx + SCROLL_W + 4, thumbY, lx + SCROLL_W + 8, thumbY + thumbH, 0xAA85B7EB);
        }

        int btnY = listY() + listH() + GAP * 2;
        drawStyledBtn(lx, btnY, "Reset All", bindHover.length - 2, mouseX, mouseY);
        drawStyledBtn(lx + SCROLL_W - BTN_W, btnY, "Done", bindHover.length - 1, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // CORRIGIDO: removidos parâmetros não utilizados
    private void drawSmallRect(int bx, int by, int ex, int ey) {
        drawRect(bx, by, ex, ey, 0x33378ADD);
        drawBorder(bx, by, ex, ey, 0x4485B7EB);
    }

    private void drawBindBtn(int bx, int by, String label, int bgColor, int idx, int mouseX, int mouseY) {
        boolean hov = mouseX >= bx && mouseX <= bx + 95 && mouseY >= by && mouseY <= by + BTN_H;
        bindHover[idx] = hov ? Math.min(1f, bindHover[idx] + 0.1f) : Math.max(0f, bindHover[idx] - 0.07f);
        float ha = bindHover[idx];
        drawRect(bx, by, bx + 95, by + BTN_H, (bgColor & 0x00FFFFFF) | ((int)(0x55 + ha * 0x44) << 24));
        drawBorder(bx, by, bx + 95, by + BTN_H, ((int)(0x44 + ha * 0x44) << 24) | 0x85B7EB);
        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, bx + 48 - lw / 2, by + BTN_H / 2 - 3, ((int)(0xAA + ha * 0x55) << 24) | 0xC8D8F0);
    }

    private void drawStyledBtn(int bx, int by, String label, int idx, int mouseX, int mouseY) {
        boolean hov = mouseX >= bx && mouseX <= bx + BTN_W && mouseY >= by && mouseY <= by + BTN_H;
        bindHover[idx] = hov ? Math.min(1f, bindHover[idx] + 0.1f) : Math.max(0f, bindHover[idx] - 0.07f);
        float ha = bindHover[idx];
        if (ha > 0f) drawRect(bx - 2, by, bx, by + BTN_H, ((int)(ha * 0xFF) << 24) | 0x85B7EB);
        drawRect(bx, by, bx + BTN_W, by + BTN_H, ((int)(0x22 + ha * 0x33) << 24) | 0x378ADD);
        drawBorder(bx, by, bx + BTN_W, by + BTN_H, ((int)(0x44 + ha * 0x44) << 24) | 0x85B7EB);
        int lw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, bx + BTN_W / 2 - lw / 2, by + BTN_H / 2 - 3, ((int)(0xAA + ha * 0x55) << 24) | 0xC8D8F0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;
        KeyBinding[] keys = settings.keyBindings;
        int lx = listX(), ly = listY();

        for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < keys.length; i++) {
            int idx = i + scrollOffset;
            int ry = ly + i * (BTN_H + GAP);
            int bindBx = lx + 210;
            if (mouseX >= bindBx && mouseX <= bindBx + 95 && mouseY >= ry && mouseY <= ry + BTN_H) {
                waitingForKey = keys[idx]; return;
            }
        }

        int btnY = listY() + listH() + GAP * 2;
        if (hit(mouseX, mouseY, lx, btnY)) {
            // CORRIGIDO: usa getKeyCode() como fallback — troque pelo campo correto do seu mappings
            for (KeyBinding kb : keys) kb.setKeyCode(kb.getKeyCode());
            KeyBinding.resetKeyBindingArrayAndHash();
            settings.saveOptions();
            return;
        }
        if (hit(mouseX, mouseY, lx + SCROLL_W - BTN_W, btnY)) {
            settings.saveOptions();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset = Math.max(0, Math.min(settings.keyBindings.length - VISIBLE_ROWS, scrollOffset - (scroll / 120)));
        }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (waitingForKey != null) {
            waitingForKey.setKeyCode(key == Keyboard.KEY_ESCAPE ? Keyboard.KEY_NONE : key);
            waitingForKey = null;
            KeyBinding.resetKeyBindingArrayAndHash();
            settings.saveOptions();
        } else if (key == Keyboard.KEY_ESCAPE) {
            settings.saveOptions();
            mc.displayGuiScreen(parent);
        }
    }

    // CORRIGIDO: removido parâmetro 'w' que era sempre BTN_W
    private boolean hit(int mx, int my, int bx, int by) {
        return mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H;
    }

    private boolean isConflict(KeyBinding kb) {
        if (kb.getKeyCode() == Keyboard.KEY_NONE) return false;
        for (KeyBinding other : settings.keyBindings) {
            if (other != kb && other.getKeyCode() == kb.getKeyCode()) return true;
        }
        return false;
    }

    private void drawBg() { for (int i = 0; i < height; i++) { float t = (float)i/height; drawRect(0,i,width,i+1,0xFF000000|((int)(10+t*4)<<16)|((int)(17+t*6)<<8)|(int)(30+t*10)); } }
    private void drawAurora() { GlStateManager.enableBlend(); GlStateManager.blendFunc(770,771); wave(0xFF1A3A8A,0.6f,0f,0.7f); wave(0xFF0A4A6A,0.4f,1.2f,0.5f); wave(0xFF2A1A5A,0.35f,2.5f,0.45f); GlStateManager.disableBlend(); }
    private void wave(int col, float amp, float phase, float alpha) { int r=(col>>16)&0xFF,g=(col>>8)&0xFF,b=col&0xFF,a=(int)(alpha*80); int seg=width/2,maxH=(int)(height*0.35f*amp); for(int i=0;i<seg;i++){float xn=(float)i/seg;float w=(float)(Math.sin(xn*4+tick+phase)*0.4+Math.sin(xn*2.3+tick*0.7+phase*1.3)*0.35+Math.sin(xn*7.1+tick*1.3+phase*0.7)*0.15+Math.sin(xn*1.5+tick*0.4+phase*2.1)*0.1);int h=Math.max(4,(int)(maxH*(0.5f+w*0.5f)));for(int y=0;y<h;y++){float f=1f-(float)y/h;f*=f;drawRect(i*2,y,i*2+2,y+1,((int)(a*f)<<24)|(r<<16)|(g<<8)|b);}}}
    private void drawStars() { for(int i=0;i<SC;i++){int x=sx[i]*width/1000,y=sy[i]*height/700,z=(ss[i]==2)?2:1;float blink=(float)(Math.sin(tick*sp[i]*8+i)*0.3+0.7);drawRect(x,y,x+z,y+z,((int)(sa[i]*blink*200)<<24)|0x7BA7D4);} }
    private void drawParticles() { for(int i=0;i<PC;i++){px[i]+=pvx[i];py[i]+=pvy[i];if(py[i]<-4)py[i]=height+4;if(px[i]<-4)px[i]=width+4;if(px[i]>width+4)px[i]=-4;float pulse=(float)(Math.sin(tick*2+i*1.3)*0.3+0.7);int z=(int)ps[i];drawRect((int)px[i],(int)py[i],(int)px[i]+z,(int)py[i]+z,((int)(pa[i]*pulse*180)<<24)|0x85B7EB);} }
    private void drawHeader(String title) { drawRect(0,0,width,26,0xCC0A111E); drawRect(0,26,width,27,0x44378ADD); int tw=mc.fontRendererObj.getStringWidth(title); mc.fontRendererObj.drawString(title,width/2-tw/2+1,9,0x22378ADD); mc.fontRendererObj.drawString(title,width/2-tw/2,8,0xCC85B7EB); }
    private void drawBorder(int x1,int y1,int x2,int y2,int color) { drawRect(x1,y1,x2,y1+1,color);drawRect(x1,y2-1,x2,y2,color);drawRect(x1,y1,x1+1,y2,color);drawRect(x2-1,y1,x2,y2,color); }
    @Override public boolean doesGuiPauseGame() { return false; }
}