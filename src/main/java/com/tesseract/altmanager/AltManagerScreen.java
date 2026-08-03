package com.tesseract.altmanager;

import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/**
 * Tela do AltManager — lista contas, permite adicionar/remover/logar.
 * Acessível via botão no TesseractMainMenu.
 */
public class AltManagerScreen extends GuiScreen {

    // -------------------------------------------------------------------------
    // Layout

    private static final int PANEL_W    = 320;
    private static final int PANEL_H    = 240;
    private static final int ENTRY_H    = 26;
    private static final int BTN_W      = 90;
    private static final int BTN_H      = 16;
    private static final int FIELD_H    = 18;

    // Cores
    private static final int C_BG           = 0xFF0A111E;
    private static final int C_PANEL        = 0xDD0D1A28;
    private static final int C_BORDER       = 0x66378ADD;
    private static final int C_ACCENT       = 0xFF378ADD;
    private static final int C_TEXT         = 0xFFC8D8F0;
    private static final int C_DIM          = 0x8885B7EB;
    private static final int C_HOVER        = 0x22378ADD;
    private static final int C_SELECTED     = 0x44378ADD;
    private static final int C_BTN          = 0x33378ADD;
    private static final int C_BTN_HOVER    = 0x66378ADD;
    private static final int C_CRACKED      = 0xFF55DD55;
    private static final int C_MICROSOFT    = 0xFF378ADD;
    private static final int C_ERROR        = 0xFFFF5555;
    private static final int C_SUCCESS      = 0xFF55FF55;

    // -------------------------------------------------------------------------

    private final AltAccountManager manager;
    private final GuiScreen         parent;

    // Estado da UI
    private int    selectedIndex = -1;
    private int    scrollOffset  = 0;
    private String statusMsg     = "";
    private int    statusColor   = C_DIM;
    private int    statusTimer   = 0;

    // Painel de adição
    private boolean addPanelOpen    = false;
    private boolean addingMicrosoft = false; // false = cracked

    // Campos de texto
    private GuiTextField fieldName;     // nome (cracked) ou email (microsoft)
    private GuiTextField fieldPassword; // só para microsoft

    // -------------------------------------------------------------------------

    public AltManagerScreen(GuiScreen parent, AltAccountManager manager) {
        this.parent  = parent;
        this.manager = manager;
    }

    @Override
    public void initGui() {
        int px = width  / 2 - PANEL_W / 2;
        int py = height / 2 - PANEL_H / 2;

        // Campo nome/email
        fieldName = new GuiTextField(0, mc.fontRendererObj,
                px + 10, py + PANEL_H - 80, PANEL_W - 20, FIELD_H);
        fieldName.setMaxStringLength(100);
        fieldName.setFocused(false);
        fieldName.setVisible(false);

        // Campo senha
        fieldPassword = new GuiTextField(1, mc.fontRendererObj,
                px + 10, py + PANEL_H - 56, PANEL_W - 20, FIELD_H);
        fieldPassword.setMaxStringLength(100);
        fieldPassword.setFocused(false);
        fieldPassword.setVisible(false);
    }

    // -------------------------------------------------------------------------
    // Render

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Fundo
        drawRect(0, 0, width, height, C_BG);
        drawCosmicOverlay();

        int px = width  / 2 - PANEL_W / 2;
        int py = height / 2 - PANEL_H / 2;

        // Painel principal
        drawRect(px, py, px + PANEL_W, py + PANEL_H, C_PANEL);
        drawBorder(px, py, px + PANEL_W, py + PANEL_H, C_BORDER);

        // Header
        drawRect(px, py, px + PANEL_W, py + 22, 0xCC185FA5);
        String title = "ALT MANAGER";
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawString(title, px + PANEL_W / 2 - tw / 2, py + 7, 0xFF85B7EB);

        // Conta atual
        String current = "Logged in as: §b" + mc.getSession().getUsername();
        mc.fontRendererObj.drawString(current, px + 8, py + 28, C_DIM);

        // Lista de contas
        int listY    = py + 42;
        int listH    = addPanelOpen ? PANEL_H - 150 : PANEL_H - 60;
        drawAccountList(px, listY, listH, mouseX, mouseY);

        // Painel de adição
        if (addPanelOpen) {
            drawAddPanel(px, py, mouseX, mouseY);
        }

        // Botões da direita (só quando não está no painel de adição)
        if (!addPanelOpen) {
            drawSideButtons(px + PANEL_W + 6, py + 42, mouseX, mouseY);
        }

        // Status
        if (statusTimer > 0) {
            statusTimer--;
            int sw = mc.fontRendererObj.getStringWidth(statusMsg);
            mc.fontRendererObj.drawStringWithShadow(statusMsg,
                    px + PANEL_W / 2 - sw / 2, py + PANEL_H - 12, statusColor);
        }

        if (fieldName.getVisible())     fieldName.drawTextBox();
        if (fieldPassword.getVisible()) fieldPassword.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawAccountList(int px, int listY, int listH, int mouseX, int mouseY) {
        List<AltAccount> accounts = manager.getAccounts();

        // Fundo da lista
        drawRect(px + 4, listY, px + PANEL_W - 4, listY + listH, 0x220D1A28);
        drawBorder(px + 4, listY, px + PANEL_W - 4, listY + listH, 0x22378ADD);

        if (accounts.isEmpty()) {
            String msg = "Nenhuma conta salva";
            int mw = mc.fontRendererObj.getStringWidth(msg);
            mc.fontRendererObj.drawString(msg,
                    px + PANEL_W / 2 - mw / 2,
                    listY + listH / 2 - 4, C_DIM);
            return;
        }

        int maxVisible = listH / ENTRY_H;
        int startIdx   = Math.max(0, Math.min(scrollOffset, accounts.size() - maxVisible));

        for (int i = startIdx; i < Math.min(startIdx + maxVisible, accounts.size()); i++) {
            AltAccount acc = accounts.get(i);
            int ey = listY + (i - startIdx) * ENTRY_H;

            boolean hovered  = mouseX >= px + 4 && mouseX <= px + PANEL_W - 4
                    && mouseY >= ey && mouseY <= ey + ENTRY_H;
            boolean selected = (i == selectedIndex);

            drawRect(px + 4, ey, px + PANEL_W - 4, ey + ENTRY_H,
                    selected ? C_SELECTED : (hovered ? C_HOVER : 0));

            if (i > startIdx)
                drawRect(px + 4, ey, px + PANEL_W - 4, ey + 1, 0x11378ADD);

            // Tipo badge
            boolean isMicrosoft = acc.getType() == AltAccount.Type.MICROSOFT;
            String  badge       = isMicrosoft ? "MSA" : "CRACK";
            int     badgeColor  = isMicrosoft ? C_MICROSOFT : C_CRACKED;
            mc.fontRendererObj.drawString(badge, px + 10, ey + ENTRY_H / 2 - 3, badgeColor);

            // Nome
            mc.fontRendererObj.drawString(acc.getDisplayName(),
                    px + 48, ey + ENTRY_H / 2 - 3, C_TEXT);

            // Botão LOGIN rápido
            int btnX = px + PANEL_W - 52;
            int btnY = ey + ENTRY_H / 2 - BTN_H / 2;
            boolean btnHov = mouseX >= btnX && mouseX <= btnX + 44
                    && mouseY >= btnY && mouseY <= btnY + BTN_H;
            drawRect(btnX, btnY, btnX + 44, btnY + BTN_H,
                    btnHov ? C_BTN_HOVER : C_BTN);
            drawBorder(btnX, btnY, btnX + 44, btnY + BTN_H, C_BORDER);
            String loginLabel = "LOGIN";
            int lw = mc.fontRendererObj.getStringWidth(loginLabel);
            mc.fontRendererObj.drawString(loginLabel,
                    btnX + 22 - lw / 2, btnY + BTN_H / 2 - 3, 0xFF85B7EB);
        }
    }

    private void drawAddPanel(int px, int py, int mouseX, int mouseY) {
        int apY = py + PANEL_H - 100;

        drawRect(px + 4, apY, px + PANEL_W - 4, py + PANEL_H - 4, 0xEE0D1A28);
        drawBorder(px + 4, apY, px + PANEL_W - 4, py + PANEL_H - 4, C_ACCENT);

        // Toggle Cracked / Microsoft
        int tglX  = px + 10;
        int tglY  = apY + 5;
        int tglW  = (PANEL_W - 24) / 2;

        boolean crackedSel   = !addingMicrosoft;
        boolean microsoftSel =  addingMicrosoft;

        drawRect(tglX,          tglY, tglX + tglW,      tglY + 14,
                crackedSel   ? 0x55378ADD : 0x220A111E);
        drawRect(tglX + tglW,   tglY, tglX + tglW * 2,  tglY + 14,
                microsoftSel ? 0x55378ADD : 0x220A111E);
        drawBorder(tglX, tglY, tglX + tglW * 2, tglY + 14, C_BORDER);

        String lCracked = "Cracked";
        String lMSA     = "Microsoft";
        mc.fontRendererObj.drawString(lCracked,
                tglX + tglW / 2 - mc.fontRendererObj.getStringWidth(lCracked) / 2,
                tglY + 3, crackedSel ? 0xFFFFFFFF : C_DIM);
        mc.fontRendererObj.drawString(lMSA,
                tglX + tglW + tglW / 2 - mc.fontRendererObj.getStringWidth(lMSA) / 2,
                tglY + 3, microsoftSel ? 0xFFFFFFFF : C_DIM);

        // Labels dos campos
        String label1 = addingMicrosoft ? "Email:" : "Nome:";
        mc.fontRendererObj.drawString(label1, px + 10, apY + 23, C_DIM);
        if (addingMicrosoft)
            mc.fontRendererObj.drawString("Senha:", px + 10, apY + 47, C_DIM);

        // Campos (reposicionados)
        fieldName.yPosition     = apY + 32;
        fieldPassword.yPosition = apY + 56;
        fieldName.setVisible(true);
        fieldPassword.setVisible(addingMicrosoft);

        // Botões Salvar / Cancelar
        int savX = px + PANEL_W - 10 - BTN_W;
        int savY = py + PANEL_H - 22;
        boolean savHov = mouseX >= savX && mouseX <= savX + BTN_W
                && mouseY >= savY && mouseY <= savY + BTN_H;
        drawRect(savX, savY, savX + BTN_W, savY + BTN_H,
                savHov ? C_BTN_HOVER : C_BTN);
        drawBorder(savX, savY, savX + BTN_W, savY + BTN_H, C_BORDER);
        String savLabel = "SALVAR";
        mc.fontRendererObj.drawString(savLabel,
                savX + BTN_W / 2 - mc.fontRendererObj.getStringWidth(savLabel) / 2,
                savY + BTN_H / 2 - 3, 0xFF85B7EB);

        int canX = savX - BTN_W - 6;
        boolean canHov = mouseX >= canX && mouseX <= canX + BTN_W
                && mouseY >= savY && mouseY <= savY + BTN_H;
        drawRect(canX, savY, canX + BTN_W, savY + BTN_H,
                canHov ? 0x44FF5555 : 0x22FF5555);
        drawBorder(canX, savY, canX + BTN_W, savY + BTN_H, 0x66FF5555);
        String canLabel = "CANCELAR";
        mc.fontRendererObj.drawString(canLabel,
                canX + BTN_W / 2 - mc.fontRendererObj.getStringWidth(canLabel) / 2,
                savY + BTN_H / 2 - 3, 0xFFFF7777);
    }

    private void drawSideButtons(int bx, int by, int mouseX, int mouseY) {
        String[] labels = { "ADICIONAR", "REMOVER", "VOLTAR" };
        for (int i = 0; i < labels.length; i++) {
            int btnY   = by + i * (BTN_H + 4);
            boolean hov = mouseX >= bx && mouseX <= bx + BTN_W
                    && mouseY >= btnY && mouseY <= btnY + BTN_H;
            boolean isDel  = labels[i].equals("REMOVER");
            boolean isBack = labels[i].equals("VOLTAR");
            int bg = isDel  ? (hov ? 0x44FF5555 : 0x22FF5555)
                    : isBack ? (hov ? 0x33AAAAAA : 0x22AAAAAA)
                      : (hov ? C_BTN_HOVER : C_BTN);
            int bd = isDel  ? 0x66FF5555
                    : isBack ? 0x44AAAAAA
                      : C_BORDER;
            int tc = isDel  ? 0xFFFF7777
                    : isBack ? 0xFFAAAAAA
                      : 0xFF85B7EB;
            drawRect(bx, btnY, bx + BTN_W, btnY + BTN_H, bg);
            drawBorder(bx, btnY, bx + BTN_W, btnY + BTN_H, bd);
            int lw = mc.fontRendererObj.getStringWidth(labels[i]);
            mc.fontRendererObj.drawString(labels[i],
                    bx + BTN_W / 2 - lw / 2, btnY + BTN_H / 2 - 3, tc);
        }
    }

    private void drawCosmicOverlay() {
        // Vinheta suave nas bordas
        for (int i = 0; i < 40; i++) {
            int alpha = (int)(0x18 * (1f - i / 40f));
            drawRect(0, i, width, i + 1, alpha << 24);
            drawRect(0, height - i - 1, width, height - i, alpha << 24);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) { super.mouseClicked(mouseX, mouseY, mouseButton); return; }

        int px = width  / 2 - PANEL_W / 2;
        int py = height / 2 - PANEL_H / 2;

        // Clique nos campos de texto
        if (fieldName.getVisible())     fieldName.mouseClicked(mouseX, mouseY, mouseButton);
        if (fieldPassword.getVisible()) fieldPassword.mouseClicked(mouseX, mouseY, mouseButton);

        // ---- Painel de adição aberto ----
        if (addPanelOpen) {
            int apY  = py + PANEL_H - 100;
            int tglX = px + 10;
            int tglY = apY + 5;
            int tglW = (PANEL_W - 24) / 2;

            // Toggle Cracked
            if (mouseX >= tglX && mouseX <= tglX + tglW
                    && mouseY >= tglY && mouseY <= tglY + 14) {
                addingMicrosoft = false;
                fieldPassword.setVisible(false);
                return;
            }
            // Toggle Microsoft
            if (mouseX >= tglX + tglW && mouseX <= tglX + tglW * 2
                    && mouseY >= tglY && mouseY <= tglY + 14) {
                addingMicrosoft = true;
                return;
            }

            int savY = py + PANEL_H - 22;
            int savX = px + PANEL_W - 10 - BTN_W;
            int canX = savX - BTN_W - 6;

            // Salvar
            if (mouseX >= savX && mouseX <= savX + BTN_W
                    && mouseY >= savY && mouseY <= savY + BTN_H) {
                saveNewAccount();
                return;
            }
            // Cancelar
            if (mouseX >= canX && mouseX <= canX + BTN_W
                    && mouseY >= savY && mouseY <= savY + BTN_H) {
                closeAddPanel();
                return;
            }
            return;
        }

        // ---- Botões laterais ----
        int bx = px + PANEL_W + 6;
        int by = py + 42;
        for (int i = 0; i < 3; i++) {
            int btnY = by + i * (BTN_H + 4);
            if (mouseX >= bx && mouseX <= bx + BTN_W
                    && mouseY >= btnY && mouseY <= btnY + BTN_H) {
                if (i == 0) openAddPanel();
                if (i == 1) removeSelected();
                if (i == 2) mc.displayGuiScreen(parent);
                return;
            }
        }

        // ---- Lista de contas ----
        int listY = py + 42;
        int listH = PANEL_H - 60;
        List<AltAccount> accounts = manager.getAccounts();
        int maxVisible = listH / ENTRY_H;
        int startIdx   = Math.max(0, Math.min(scrollOffset, accounts.size() - maxVisible));

        for (int i = startIdx; i < Math.min(startIdx + maxVisible, accounts.size()); i++) {
            int ey   = listY + (i - startIdx) * ENTRY_H;
            int btnX = px + PANEL_W - 52;
            int btnY = ey + ENTRY_H / 2 - BTN_H / 2;

            // Botão LOGIN
            if (mouseX >= btnX && mouseX <= btnX + 44
                    && mouseY >= btnY && mouseY <= btnY + BTN_H) {
                loginAccount(accounts.get(i));
                return;
            }

            // Selecionar
            if (mouseX >= px + 4 && mouseX <= px + PANEL_W - 4
                    && mouseY >= ey && mouseY <= ey + ENTRY_H) {
                selectedIndex = i;
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll < 0) scrollOffset++;
        if (scroll > 0) scrollOffset = Math.max(0, scrollOffset - 1);
    }

    // -------------------------------------------------------------------------
    // Teclado

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (fieldName.isFocused())     fieldName.textboxKeyTyped(typedChar, keyCode);
        if (fieldPassword.isFocused()) fieldPassword.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_TAB && addPanelOpen && addingMicrosoft) {
            fieldName.setFocused(!fieldName.isFocused());
            fieldPassword.setFocused(!fieldPassword.isFocused());
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (addPanelOpen) closeAddPanel();
            else mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void updateScreen() {
        if (fieldName.getVisible())     fieldName.updateCursorCounter();
        if (fieldPassword.getVisible()) fieldPassword.updateCursorCounter();
    }

    // -------------------------------------------------------------------------
    // Ações

    private void loginAccount(AltAccount acc) {
        boolean ok = acc.getType() == AltAccount.Type.CRACKED
                ? manager.loginCracked(acc)
                : manager.loginMicrosoft(acc);

        if (ok) {
            showStatus("Logado como " + acc.getDisplayName(), C_SUCCESS);
        } else {
            showStatus("Falha ao logar. Verifique as credenciais.", C_ERROR);
        }
    }

    private void saveNewAccount() {
        String name = fieldName.getText().trim();
        if (name.isEmpty()) { showStatus("Nome/Email não pode ser vazio.", C_ERROR); return; }

        AltAccount acc;
        if (addingMicrosoft) {
            String pass = fieldPassword.getText().trim();
            if (pass.isEmpty()) { showStatus("Senha não pode ser vazia.", C_ERROR); return; }
            acc = new AltAccount(AltAccount.Type.MICROSOFT, name,
                    AltAccountManager.encrypt(pass));
        } else {
            acc = new AltAccount(AltAccount.Type.CRACKED, name, "");
        }

        manager.addAccount(acc);
        showStatus("Conta adicionada!", C_SUCCESS);
        closeAddPanel();
    }

    private void removeSelected() {
        List<AltAccount> accounts = manager.getAccounts();
        if (selectedIndex < 0 || selectedIndex >= accounts.size()) {
            showStatus("Selecione uma conta primeiro.", C_ERROR);
            return;
        }
        manager.removeAccount(accounts.get(selectedIndex));
        selectedIndex = -1;
        showStatus("Conta removida.", C_DIM);
    }

    private void openAddPanel() {
        addPanelOpen    = true;
        addingMicrosoft = false;
        fieldName.setText("");
        fieldPassword.setText("");
        fieldName.setFocused(true);
    }

    private void closeAddPanel() {
        addPanelOpen = false;
        fieldName.setVisible(false);
        fieldPassword.setVisible(false);
        fieldName.setFocused(false);
        fieldPassword.setFocused(false);
    }

    private void showStatus(String msg, int color) {
        statusMsg   = msg;
        statusColor = color;
        statusTimer = 120; // ~6 segundos
    }

    // -------------------------------------------------------------------------

    @Override public boolean doesGuiPauseGame() { return false; }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1,     y1,     x2,     y1 + 1, color);
        drawRect(x1,     y2 - 1, x2,     y2,     color);
        drawRect(x1,     y1,     x1 + 1, y2,     color);
        drawRect(x2 - 1, y1,     x2,     y2,     color);
    }
}