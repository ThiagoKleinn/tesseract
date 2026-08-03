package com.tesseract.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Random;

public class TesseractWorldSelect extends GuiSelectWorld {

    public GuiScreen parentScreen;

    private float tick = 0f;
    private static final int SC = 80, PC = 18;
    private final int[] sx = new int[SC], sy = new int[SC], ss = new int[SC];
    private final float[] sp = new float[SC], sa = new float[SC];
    private final float[] px = new float[PC], py = new float[PC];
    private final float[] pvx = new float[PC], pvy = new float[PC];
    private final float[] pa = new float[PC], ps = new float[PC];

    public TesseractWorldSelect(GuiScreen parent) {
        super(parent);
        this.parentScreen = parent;
        Random r = new Random(0xC05B1CL);
        for (int i = 0; i < SC; i++) { sx[i]=r.nextInt(1000); sy[i]=r.nextInt(700); ss[i]=r.nextInt(3); sp[i]=0.1f+r.nextFloat()*0.3f; sa[i]=0.3f+r.nextFloat()*0.7f; }
        Random p = new Random(0xDEADBEEFL);
        for (int i = 0; i < PC; i++) { px[i]=p.nextFloat()*1000; py[i]=p.nextFloat()*700; pvx[i]=(p.nextFloat()-0.5f)*0.4f; pvy[i]=-0.1f-p.nextFloat()*0.2f; pa[i]=0.2f+p.nextFloat()*0.4f; ps[i]=1f+p.nextFloat()*2f; }
    }

    @Override public void drawDefaultBackground() { /* remove textura vanilla */ }
    @Override public void drawWorldBackground(int tint) { /* remove textura vanilla */ }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick += 0.012f;
        drawBg(); drawAurora(); drawStars(); drawParticles();
        drawHeader("SINGLEPLAYER");
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawBg() {
        for (int i = 0; i < height; i++) {
            float t=(float)i/height;
            drawRect(0,i,width,i+1, 0xFF000000|((int)(10+t*4)<<16)|((int)(17+t*6)<<8)|(int)(30+t*10));
        }
    }

    private void drawAurora() {
        GlStateManager.enableBlend(); GlStateManager.blendFunc(770,771);
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
        drawRect(0,0,width,26,0xCC0A111E);
        drawRect(0,26,width,27,0x44378ADD);
        int tw=mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title,width/2-tw/2+1,9,0x22378ADD);
        mc.fontRendererObj.drawString(title,width/2-tw/2,8,0xCC85B7EB);
    }
}