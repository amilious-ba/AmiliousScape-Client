package rt4.amilious.menu;

import plugin.api.API;
import rt4.Fonts;
import rt4.GlRaster;
import rt4.GlRenderer;
import rt4.JagString;
import rt4.Packet;
import rt4.SoftwareRaster;
import rt4.amilious.input.InputManager;
import rt4.amilious.input.InputMode;

public final class GigosHud {

    public static final int OPCODE = 201;

    public static final class Toggle {
        public int id;
        public boolean on;
        public String label;
    }

    private static boolean visible;
    private static int hp = 1;
    private static int hpMax = 1;
    private static String title = "Gigos";
    private static Toggle[] toggles = new Toggle[0];
    private static int bananaCount;
    private static String actionName = "";

    private static final int PANEL_W = 120;
    private static final int ROW_H = 16;
    private static final int HEADER_H = 52;

    private static int panelX = 516 - PANEL_W;
    private static int panelY = 40;
    private static int panelH;
    private static boolean dragging;
    private static int dragOffX;
    private static int dragOffY;
    private static boolean placed;

    private GigosHud() {
    }

    public static boolean isInteractive() {
        if (!visible) {
            return false;
        }
        InputMode mode = InputManager.getMode();
        return mode == InputMode.WORLD || mode == InputMode.CHAT;
    }

    public static void hide() {
        visible = false;
        toggles = new Toggle[0];
        dragging = false;
    }

    public static void read(Packet buf, int payloadLen) {
        if (buf == null || payloadLen < 1) {
            return;
        }
        int show = buf.g1();
        if (show == 0) {
            hide();
            return;
        }
        hp = buf.g2();
        hpMax = buf.g2();
        JagString t = buf.gjstr();
        title = t == null ? "Gigos" : t.toString();
        bananaCount = buf.g2();
        JagString act = buf.gjstr();
        actionName = act == null ? "" : act.toString();
        int n = buf.g1();
        if (n < 0) n = 0;
        if (n > 8) n = 8;
        Toggle[] next = new Toggle[n];
        for (int i = 0; i < n; i++) {
            Toggle tw = new Toggle();
            tw.id = buf.g1();
            tw.on = buf.g1() != 0;
            JagString lab = buf.gjstr();
            tw.label = lab == null ? ("Opt " + tw.id) : lab.toString();
            next[i] = tw;
        }
        toggles = next;
        visible = true;
    }

    public static void poll(int mx, int my, boolean down, boolean pressed) {
        if (!isInteractive()) {
            dragging = false;
            return;
        }
        if (pressed) {
            mouseDown(mx, my);
        } else if (down) {
            mouseDrag(mx, my);
        } else {
            mouseUp();
        }
    }

    public static void draw() {
        if (!isInteractive()) {
            return;
        }
        if (!placed) {
            panelX = 516 - PANEL_W;
            panelY = 40;
            placed = true;
        }
        panelH = HEADER_H + toggles.length * ROW_H;
        drawPanel(panelX, panelY, PANEL_W, panelH);
        Fonts.b12Full.renderLeft(JagString.of(title), panelX + 6, panelY + 12, 0xffff00, 0);
        if (hpMax > 0) {
            int bw = PANEL_W - 12;
            int fill = hp * bw / hpMax;
            if (fill < 0) fill = 0;
            if (fill > bw) fill = bw;
            fillRect(panelX + 6, panelY + 16, bw, 5, 0x880000);
            fillRect(panelX + 6, panelY + 16, fill, 5, 0x33cc44);
        }
        Fonts.b12Full.renderLeft(JagString.of("Bananas: " + bananaCount), panelX + 6, panelY + 32, 0xffffff, 0);
        Fonts.b12Full.renderLeft(JagString.of(actionName), panelX + 6, panelY + 44, 0xcccccc, 0);
        for (int i = 0; i < toggles.length; i++) {
            int y = panelY + HEADER_H + i * ROW_H;
            String mark = toggles[i].on ? "[x] " : "[ ] ";
            Fonts.b12Full.renderLeft(JagString.of(mark + toggles[i].label), panelX + 6, y + 12, 0xffffff, 0);
        }
    }

    public static boolean mouseDown(int mx, int my) {
        if (!isInteractive() || !inside(mx, my)) {
            return false;
        }
        InputManager.consumeMouseClick();
        if (my < panelY + HEADER_H) {
            dragging = true;
            dragOffX = mx - panelX;
            dragOffY = my - panelY;
            return true;
        }
        int row = (my - panelY - HEADER_H) / ROW_H;
        if (row >= 0 && row < toggles.length) {
            API.DispatchCommand("::gigosop " + toggles[row].id);
        }
        return true;
    }

    public static void mouseDrag(int mx, int my) {
        if (!isInteractive() || !dragging) {
            return;
        }
        panelX = mx - dragOffX;
        panelY = my - dragOffY;
        if (panelX < 0) panelX = 0;
        if (panelY < 0) panelY = 0;
    }

    public static void mouseUp() {
        dragging = false;
    }

    private static boolean inside(int mx, int my) {
        return mx >= panelX && mx < panelX + PANEL_W && my >= panelY && my < panelY + panelH;
    }

    private static void drawPanel(int x, int y, int w, int h) {
        int fill = GlRenderer.enabled ? 0xC02A1A10 : 0x2A1A10;
        int edge = 0xC8A050;
        fillRect(x + 3, y, w - 6, h, fill);
        fillRect(x, y + 3, w, h - 6, fill);
        fillRect(x + 1, y + 1, w - 2, h - 2, fill);
        fillRect(x + 3, y, w - 6, 1, edge);
        fillRect(x + 3, y + h - 1, w - 6, 1, edge);
        fillRect(x, y + 3, 1, h - 6, edge);
        fillRect(x + w - 1, y + 3, 1, h - 6, edge);
    }

    private static void fillRect(int x, int y, int w, int h, int rgb) {
        if (GlRenderer.enabled) {
            GlRaster.fillRect(x, y, w, h, rgb);
        } else {
            SoftwareRaster.fillRect(x, y, w, h, rgb & 0xFFFFFF);
        }
    }
}