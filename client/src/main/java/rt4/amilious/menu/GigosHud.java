package rt4.amilious.menu;

import plugin.api.API;
import rt4.Fonts;
import rt4.GameShell;
import rt4.GlRaster;
import rt4.GlRenderer;
import rt4.JagString;
import rt4.Packet;
import rt4.SoftwareRaster;
import rt4.amilious.input.InputManager;
import rt4.amilious.input.InputMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public final class GigosHud {

    public static final int OPCODE = 201;

    public static final class Toggle {
        public int id;
        public boolean on;
        public String label;
    }

    private static final int COLOR_PANEL = 0x5D5447;
    private static final int COLOR_HEADER = 0x000000;
    private static final int COLOR_BORDER = 0x000000;
    private static final int COLOR_TITLE = 0xFFFF00;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_STATUS = 0xC8C8C8;
    private static final int COLOR_BAR_BG = 0x4B0000;
    private static final int COLOR_BAR_FG = 0x00C000;
    private static final int COLOR_BTN = 0x3A342C;

    private static final int PANEL_W = 130;
    private static final int ROW_H = 16;
    private static final int HEADER_H = 70;
    private static final int TITLE_H = 16;
    private static final int BTN_W = 14;
    private static final int MARGIN = 4;
    private static final int BAG_W = 14;
    private static final int BAG_H = 12;
    /** Server command that opens Gigos pack. Change if yours differs. */
    private static final String BAG_COMMAND = "::monkeybag";

    private static boolean visible;
    private static int hp = 1;
    private static int hpMax = 1;
    private static String title = "Gigos";
    private static Toggle[] toggles = new Toggle[0];
    private static int bananaCount;
    private static String actionName = "";
    private static String phaseName = "";
    private static int phaseIndex;
    private static int phaseCount = 1;
    private static boolean busy;
    private static boolean drunk;
    private static int drunkTicks;

    private static int panelX = -1;
    private static int panelY = -1;
    private static int panelH;
    private static boolean dragging;
    private static int dragOffX;
    private static int dragOffY;
    private static boolean loaded;
    private static boolean collapsed = true;

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
        JagString ph = buf.gjstr();
        phaseName = ph == null ? "" : ph.toString();
        phaseIndex = buf.g1();
        phaseCount = buf.g1();
        if (phaseCount < 1) {
            phaseCount = 1;
        }
        busy = buf.g1() != 0;
        drunk = buf.g1() != 0;
        drunkTicks = buf.g2();
        int n = buf.g1();
        if (n < 0) {
            n = 0;
        }
        if (n > 8) {
            n = 8;
        }
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
        ensureLoaded();
        panelH = collapsed ? HEADER_H : HEADER_H + toggles.length * ROW_H;
        clampToScreen();
        rt4.amilious.OverlayClickBlocker.add(panelX, panelY, PANEL_W, panelH);

        if (pressed) {
            mouseDown(mx, my);
        } else if (down) {
            mouseDrag(mx, my);
        } else if (dragging) {
            dragging = false;
            clampToScreen();
            saveLayout();
        }
    }

    public static void draw() {
        if (!isInteractive() || Fonts.b12Full == null) {
            return;
        }
        ensureLoaded();
        panelH = collapsed ? HEADER_H : HEADER_H + toggles.length * ROW_H;
        clampToScreen();
        rt4.amilious.OverlayClickBlocker.add(panelX, panelY, PANEL_W, panelH);

        fillRect(panelX, panelY, PANEL_W, panelH, COLOR_PANEL);
        fillRect(panelX + 1, panelY + 1, PANEL_W - 2, TITLE_H, COLOR_HEADER);
        drawRect(panelX, panelY, PANEL_W, panelH, COLOR_BORDER);

        Fonts.b12Full.renderLeft(JagString.of(title), panelX + 6, panelY + 13, COLOR_TITLE, 0);
        drawCollapseBtn();
        drawBagBtn();

        if (hpMax > 0) {
            int bw = PANEL_W - 12;
            int fill = hp * bw / hpMax;
            if (fill < 0) {
                fill = 0;
            }
            if (fill > bw) {
                fill = bw;
            }
            fillRect(panelX + 6, panelY + 20, bw, 6, COLOR_BAR_BG);
            fillRect(panelX + 6, panelY + 20, fill, 6, COLOR_BAR_FG);
            drawRect(panelX + 6, panelY + 20, bw, 6, COLOR_BORDER);
        }

        Fonts.b12Full.renderLeft(JagString.of("Bananas: " + bananaCount), panelX + 6, panelY + 40, COLOR_TEXT, 0);

        String status = actionName == null || actionName.length() == 0 ? "idle" : actionName;
        if (drunk) {
            status = status + " *";
        }
        Fonts.b12Full.renderLeft(JagString.of(status), panelX + 6, panelY + 52, COLOR_STATUS, 0);

        int shown = phaseIndex + 1;
        if (shown < 1) {
            shown = 1;
        }
        if (shown > phaseCount) { shown = phaseCount; }

        String phaseLine = shown + "/" + phaseCount;
        if (phaseName != null && phaseName.length() > 0) {
            phaseLine = phaseLine + " " + phaseLineSafe(phaseName);
        }
        if (Fonts.p11Full != null) {
            Fonts.p11Full.renderLeft(JagString.of(phaseLine), panelX + 12, panelY + 64, COLOR_STATUS, 0);
        } else {
            Fonts.b12Full.renderLeft(JagString.of(phaseLine), panelX + 12, panelY + 64, COLOR_STATUS, 0);
        }

        if (collapsed) {return;}
        for (int i = 0; i < toggles.length; i++) {
            int y = panelY + HEADER_H + i * ROW_H;
            drawCheckbox(panelX + 6, y + 2, toggles[i].on);
            Fonts.b12Full.renderLeft(JagString.of(toggles[i].label), panelX + 22, y + 12, COLOR_TEXT, 0);
        }
    }

    private static String phaseLineSafe(String s) {
        return s.length() > 12 ? s.substring(0, 12) : s;
    }

    public static boolean mouseDown(int mx, int my) {
        if (!isInteractive() || !inside(mx, my)) {
            return false;
        }
        InputManager.consumeMouseClick();
        if (hitCollapseBtn(mx, my)) {
            collapsed = !collapsed;
            clampToScreen();
            saveLayout();
            dragging = false;
            return true;
        }
        if (hitBagBtn(mx, my)) {
            API.DispatchCommand(BAG_COMMAND);
            dragging = false;
            return true;
        }
        if (collapsed || my < panelY + HEADER_H) {
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
        clampToScreen();
    }

    public static void mouseUp() {
        if (dragging) {
            dragging = false;
            clampToScreen();
            saveLayout();
        }
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadLayout();
        if (panelX < 0 || panelY < 0) {
            panelX = GameShell.canvasWidth - PANEL_W - 8;
            panelY = 40;
        }
        clampToScreen();
    }

    private static int bagX() {
        return btnX() - BAG_W - 2;
    }

    private static int bagY() {
        return panelY + 2;
    }

    private static boolean hitBagBtn(int mx, int my) {
        int x = bagX();
        int y = bagY();
        return mx >= x && mx < x + BAG_W && my >= y && my < y + BAG_H;
    }

    private static void drawBagBtn() {
        int x = bagX();
        int y = bagY();
        fillRect(x, y, BAG_W, BAG_H, COLOR_BTN);
        drawRect(x, y, BAG_W, BAG_H, COLOR_BORDER);
        // flap
        fillRect(x + 2, y + 1, BAG_W - 4, 3, COLOR_TITLE);
        drawRect(x + 2, y + 1, BAG_W - 4, 3, COLOR_BORDER);
        // body
        fillRect(x + 3, y + 5, BAG_W - 6, 5, 0xC8B070);
        drawRect(x + 3, y + 5, BAG_W - 6, 5, COLOR_BORDER);
    }

    private static void clampToScreen() {
        int cw = GameShell.canvasWidth;
        int ch = GameShell.canvasHeight;
        if (cw < 1) {
            cw = 765;
        }
        if (ch < 1) {
            ch = 503;
        }
        panelH = collapsed ? HEADER_H : HEADER_H + toggles.length * ROW_H;
        if (panelX + PANEL_W > cw - MARGIN) {
            panelX = cw - PANEL_W - MARGIN;
        }
        if (panelY + panelH > ch - MARGIN) {
            panelY = ch - panelH - MARGIN;
        }
        if (panelX < MARGIN) {
            panelX = MARGIN;
        }
        if (panelY < MARGIN) {
            panelY = MARGIN;
        }
    }

    private static File layoutFile() {
        File dir = new File(System.getProperty("user.home"), ".amilious");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "gigos-hud.txt");
    }

    private static void loadLayout() {
        File f = layoutFile();
        if (!f.isFile()) {
            return;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line;
            while ((line = in.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                if ("x".equals(k)) {
                    panelX = Integer.parseInt(v);
                } else if ("y".equals(k)) {
                    panelY = Integer.parseInt(v);
                } else if ("collapsed".equals(k)) {
                    collapsed = "1".equals(v) || "true".equalsIgnoreCase(v);
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void saveLayout() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(layoutFile()));
            out.println("x=" + panelX);
            out.println("y=" + panelY);
            out.println("collapsed=" + (collapsed ? "1" : "0"));
        } catch (Exception ignored) {
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static int btnX() {
        return panelX + PANEL_W - BTN_W - 3;
    }

    private static int btnY() {
        return panelY + 2;
    }

    private static boolean hitCollapseBtn(int mx, int my) {
        int x = btnX();
        int y = btnY();
        return mx >= x && mx < x + BTN_W && my >= y && my < y + 12;
    }

    private static void drawCollapseBtn() {
        int x = btnX();
        int y = btnY();
        fillRect(x, y, BTN_W, 12, COLOR_BTN);
        drawRect(x, y, BTN_W, 12, COLOR_BORDER);
        Fonts.b12Full.renderLeft(JagString.of(collapsed ? "+" : "-"), x + 4, y + 11, COLOR_TITLE, 0);
    }

    private static void drawCheckbox(int x, int y, boolean on) {
        int s = 11;
        fillRect(x, y, s, s, COLOR_BTN);
        drawRect(x, y, s, s, COLOR_BORDER);
        if (on) {
            fillRect(x + 3, y + 3, s - 6, s - 6, COLOR_TITLE);
            drawRect(x + 2, y + 2, s - 4, s - 4, COLOR_BORDER);
        }
    }

    private static boolean inside(int mx, int my) {
        return mx >= panelX && mx < panelX + PANEL_W && my >= panelY && my < panelY + panelH;
    }

    private static void fillRect(int x, int y, int w, int h, int rgb) {
        if (GlRenderer.enabled) {
            GlRaster.fillRect(x, y, w, h, rgb);
        } else {
            SoftwareRaster.fillRect(x, y, w, h, rgb & 0xFFFFFF);
        }
    }

    private static void drawRect(int x, int y, int w, int h, int rgb) {
        if (GlRenderer.enabled) {
            GlRaster.drawRect(x, y, w, h, rgb);
        } else {
            SoftwareRaster.drawRect(x, y, w, h, rgb & 0xFFFFFF);
        }
    }
}