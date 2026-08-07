package com.tesseract.module;

public interface HudComponent {
    int getHudX();
    int getHudY();
    int getHudWidth();
    int getHudHeight();
    void setHudPos(int x, int y);
    void saveConfig();
    String getHudLabel();
}