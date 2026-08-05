package com.tesseract.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

public class TesseractWorldSelect extends GuiSelectWorld {

    public GuiScreen parentScreen;
    private float tick = 0f;

    private static final int SC = 80, PC = 18;
    private final int[]   sx = new int[SC], sy = new int[SC], ss = new int[SC];
    private final float[] sp = new float[SC], sa = new float[SC];
    private final float[] px = new float[PC], py = new float[PC];
    private final float[] pvx = new float[PC], pvy = new float[PC];
    private final float[] pa = new float[PC], ps = new float[PC];

    private static final String[] BTN_LABELS = {
            "Play Selected World", "Create New World", "Rename", "Delete", "Re-Create", "Cancel"
    };
    private static final int[] BTN_IDS = { 1, 3, 6, 2, 7, 0 };
    private static final int BTN_H = 20;
    private final float[] btnHoverAnim = new float[BTN_LABELS.length];

    public TesseractWorldSelect(GuiScreen parent) {
        super(parent);
        this.parentScreen = parent;
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

    @Override public void drawWorldBackground(int tint) {}
    @Override public void drawDefaultBackground() {}

    private GuiSlot getWorldList() {
        try {
            for (Field f : GuiSelectWorld.class.getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(this);
                if (val instanceof GuiSlot) return (GuiSlot) val;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private GuiSlot wrapSlotNoBg(GuiSlot real) {
        return new GuiSlot(mc, real.width, real.height, real.top, real.bottom, real.slotHeight) {
            @Override
            protected int getSize() {
                try {
                    Method m = real.getClass().getDeclaredMethod("getSize");
                    m.setAccessible(true);
                    return (int) m.invoke(real);
                } catch (Exception e) { return 0; }
            }

            @Override
            protected void elementClicked(int idx, boolean isDoubleClick, int mx, int my) {
                try {
                    Method m = real.getClass().getDeclaredMethod(
                            "elementClicked", int.class, boolean.class, int.class, int.class);
                    m.setAccessible(true);
                    m.invoke(real, idx, isDoubleClick, mx, my);
                } catch (Exception e) { /* ignore */ }
            }

            @Override
            protected boolean isSelected(int idx) {
                try {
                    Method m = real.getClass().getDeclaredMethod("isSelected", int.class);
                    m.setAccessible(true);
                    return (boolean) m.invoke(real, idx);
                } catch (Exception e) { return false; }
            }

            @Override protected void drawBackground() {}
            @Override protected void drawContainerBackground(Tessellator t) {}

            @Override
            protected void drawSlot(int id, int x, int y, int h, int mx, int my) {
                try {
                    Method m = real.getClass().getDeclaredMethod(
                            "drawSlot", int.class, int.class, int.class, int.class, int.class, int.class);
                    m.setAccessible(true);
                    m.invoke(real, id, x, y, h, mx, my);
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tick += 0.012f;
        updateParticles();

        drawSpaceBg(0, height);
        drawAurora();
        drawStars();
        drawParticles();

        GuiSlot real = getWorldList();
        if (real != null) real.drawScreen(mouseX, mouseY, partialTicks);

        drawSpaceBg(0, 36);
        drawSpaceBg(height - 68, height);
        drawAurora();
        drawStars();
        drawParticles();

        drawCustomButtons(mouseX, mouseY);
        drawHeader("SINGLEPLAYER");
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        GuiSlot list = getWorldList();
        if (list != null) {
            try {
                Method m = null;
                for (String name : new String[]{"mouseClicked", "func_148179_a"}) {
                    try {
                        m = GuiSlot.class.getDeclaredMethod(name, int.class, int.class, int.class);
                        break;
                    } catch (NoSuchMethodException ignored) {}
                }
                if (m != null) { m.setAccessible(true); m.invoke(list, mouseX, mouseY, mouseButton); }
            } catch (Exception ignored) {}
        }

        if (mouseButton != 0) return;

        int row1Y = height - 52;
        int row2Y = height - 28;
        int[][] layout = {
                { width/2 - 154, row1Y, 150, 0 },
                { width/2 + 4,   row1Y, 150, 1 },
                { width/2 - 154, row2Y, 72,  2 },
                { width/2 - 76,  row2Y, 72,  3 },
                { width/2 + 4,   row2Y, 72,  4 },
                { width/2 + 82,  row2Y, 72,  5 },
        };

        for (int[] btn : layout) {
            int bx = btn[0], by = btn[1], bw = btn[2], idx = btn[3];
            if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + BTN_H) {
                for (GuiButton b : this.buttonList) {
                    if (b.id == BTN_IDS[idx] && b.enabled) {
                        actionPerformed(b);
                        return;
                    }
                }
            }
        }
    }

    private void drawCustomButtons(int mouseX, int mouseY) {
        int row1Y = height - 52;
        int row2Y = height - 28;
        int[][] layout = {
                { width/2 - 154, row1Y, 150, 0 },
                { width/2 + 4,   row1Y, 150, 1 },
                { width/2 - 154, row2Y, 72,  2 },
                { width/2 - 76,  row2Y, 72,  3 },
                { width/2 + 4,   row2Y, 72,  4 },
                { width/2 + 82,  row2Y, 72,  5 },
        };
        for (int[] btn : layout) {
            int bx = btn[0], by = btn[1], bw = btn[2], idx = btn[3];
            boolean hov = mouseX >= bx && mouseX <= bx + bw
                    && mouseY >= by && mouseY <= by + BTN_H;

            btnHoverAnim[idx] = hov
                    ? Math.min(1f, btnHoverAnim[idx] + 0.1f)
                    : Math.max(0f, btnHoverAnim[idx] - 0.07f);
            float ha = btnHoverAnim[idx];

            if (ha > 0f)
                drawRect(bx - 2, by, bx, by + BTN_H, ((int)(ha*255) << 24) | 0x85B7EB);

            drawRect(bx, by, bx + bw, by + BTN_H, ((int)(0x22 + ha*0x33) << 24) | 0x378ADD);
            drawBorder(bx, by, bx + bw, by + BTN_H, ((int)(0x44 + ha*0x44) << 24) | 0x85B7EB);

            String label = BTN_LABELS[idx];
            int lw = mc.fontRendererObj.getStringWidth(label);
            mc.fontRendererObj.drawString(label,
                    bx + bw / 2 - lw / 2,
                    by + BTN_H / 2 - 3,
                    ((int)(0xAA + ha*0x55) << 24) | 0xC8D8F0);
        }
    }

    private void drawSpaceBg(int yStart, int yEnd) {
        for (int i = Math.max(0, yStart); i < Math.min(height, yEnd); i++) {
            float t = (float) i / height;
            drawRect(0, i, width, i + 1,
                    0xFF000000 | ((int)(10+t*4)<<16) | ((int)(17+t*6)<<8) | (int)(30+t*10));
        }
    }

    private void drawAurora() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        wave(0xFF1A3A8A, 0.6f, 0f,  0.7f);
        wave(0xFF0A4A6A, 0.4f, 1.2f, 0.5f);
        wave(0xFF2A1A5A, 0.35f,2.5f, 0.45f);
        GlStateManager.disableBlend();
    }

    private void wave(int col, float amp, float phase, float alpha) {
        int r=(col>>16)&0xFF, g=(col>>8)&0xFF, b=col&0xFF, a=(int)(alpha*80);
        int seg = width/2, maxH = (int)(height*0.35f*amp);
        for (int i = 0; i < seg; i++) {
            float xn = (float)i/seg;
            float w = (float)(
                    Math.sin(xn*4+tick+phase)*0.4 +
                            Math.sin(xn*2.3+tick*0.7+phase*1.3)*0.35 +
                            Math.sin(xn*7.1+tick*1.3+phase*0.7)*0.15 +
                            Math.sin(xn*1.5+tick*0.4+phase*2.1)*0.1);
            int h = Math.max(4, (int)(maxH*(0.5f+w*0.5f)));
            for (int y = 0; y < h; y++) {
                float f = 1f-(float)y/h; f*=f;
                drawRect(i*2, y, i*2+2, y+1, ((int)(a*f)<<24)|(r<<16)|(g<<8)|b);
            }
        }
    }

    private void drawStars() {
        for (int i = 0; i < SC; i++) {
            int x = sx[i]*width/1000, y = sy[i]*height/700, z = (ss[i]==2)?2:1;
            float blink = (float)(Math.sin(tick*sp[i]*8+i)*0.3+0.7);
            drawRect(x, y, x+z, y+z, ((int)(sa[i]*blink*200)<<24)|0x7BA7D4);
        }
    }

    private void updateParticles() {
        for (int i = 0; i < PC; i++) {
            px[i]+=pvx[i]; py[i]+=pvy[i];
            if(py[i]<-4) py[i]=height+4;
            if(px[i]<-4) px[i]=width+4;
            if(px[i]>width+4) px[i]=-4;
        }
    }

    private void drawParticles() {
        for (int i = 0; i < PC; i++) {
            float pulse = (float)(Math.sin(tick*2+i*1.3)*0.3+0.7);
            int z = (int)ps[i];
            drawRect((int)px[i], (int)py[i], (int)px[i]+z, (int)py[i]+z,
                    ((int)(pa[i]*pulse*180)<<24)|0x85B7EB);
        }
    }

    private void drawHeader(String title) {
        drawRect(0, 0, width, 32, 0xEE0A111E);
        drawRect(0, 32, width, 33, 0x66378ADD);
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, width/2-tw/2+1, 10, 0x22378ADD);
        mc.fontRendererObj.drawString(title, width/2-tw/2,    9, 0xCC85B7EB);
    }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,   y1,   x2,   y1+1, color);
        drawRect(x1,   y2-1, x2,   y2,   color);
        drawRect(x1,   y1,   x1+1, y2,   color);
        drawRect(x2-1, y1,   x2,   y2,   color);
    }
}