package rt4.amilious.guide;

import rt4.Fonts;
import rt4.GameShell;
import rt4.GlRaster;
import rt4.GlRenderer;
import rt4.JagString;
import rt4.SoftwareRaster;
import rt4.amilious.input.InputManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public final class GuideHud {

    private static final int COLOR_PANEL = 0x5D5447;
    private static final int COLOR_BORDER = 0x000000;
    private static final int COLOR_TITLE = 0xFFFF00;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_STATUS = 0xC8C8C8;
    private static final int COLOR_BAR_BG = 0x4B0000;
    private static final int COLOR_BAR_FG = 0x00C000;
    private static final int COLOR_BTN = 0x3A342C;
    private static final int COLOR_MISS = 0xFF6666;

    private static final int PANEL_W = 220;
    private static final int TITLE_H = 16;
    private static final int HEADER_H = 48;
    private static final int LINE_H = 14;
    private static final int BTN_W = 14;
    private static final int MARGIN = 4;

    private static int panelX = -1;
    private static int panelY = -1;
    private static int panelH;
    private static boolean dragging;
    private static int dragOffX;
    private static int dragOffY;
    private static boolean loaded;
    private static boolean collapsed;
    private static boolean visible = false;

    private GuideHud() {
    }

    public static void toggle() {
        setVisible(!visible);
    }

    public static void setVisible(boolean v) {
        visible = v;
        saveLayout();
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void draw() {
        if (!visible || rt4.client.gameState != 30) {
            return;
        }
        if (Fonts.b12Full == null || Fonts.p11Full == null) {
            return;
        }
        ensureLoaded();

        String[] body = collapsed ? new String[0] : ProgressGuide.overlayBody();
        panelH = collapsed
                ? HEADER_H
                : (HEADER_H + 4 + body.length * LINE_H + 6);

        rt4.amilious.OverlayClickBlocker.add(panelX, panelY, PANEL_W, panelH);

        fillRect(panelX, panelY, PANEL_W, panelH, COLOR_PANEL);
        fillRect(panelX + 1, panelY + 1, PANEL_W - 2, TITLE_H, 0x000000);
        drawRect(panelX, panelY, PANEL_W, panelH, COLOR_BORDER);

        int done = ProgressGuide.getDoneCount();
        int total = ProgressGuide.getTotalCount();
        int pct = ProgressGuide.currentProgressPct();

        Fonts.b12Full.renderLeft(
                JagString.of("Guide - Task " + done + "/" + total),
                panelX + 6,
                panelY + 13,
                COLOR_TITLE,
                0);
        drawCollapseBtn();

        String taskName = ProgressGuide.currentTaskTitle();
        if (taskName == null || taskName.length() == 0) {
            taskName = "—";
        }
        Fonts.p11Full.renderLeft(
                JagString.of(taskName),
                panelX + 6,
                panelY + TITLE_H + 12,
                COLOR_TEXT,
                0);
        Fonts.p11Full.renderRight(
                JagString.of(pct + "%"),
                panelX + PANEL_W - 6,
                panelY + TITLE_H + 12,
                COLOR_STATUS,
                0);

        int barX = panelX + 6;
        int barY = panelY + TITLE_H + 16;
        int barW = PANEL_W - 12;
        int barH = 8;
        fillRect(barX, barY, barW, barH, COLOR_BAR_BG);
        int fill = 0;
        if (pct > 0) {
            fill = barW * pct / 100;
            if (fill < 1) {
                fill = 1;
            }
            if (fill > barW) {
                fill = barW;
            }
            fillRect(barX, barY, fill, barH, COLOR_BAR_FG);
        }
        drawRect(barX, barY, barW, barH, COLOR_BORDER);

        if (collapsed) {
            return;
        }

        int y = panelY + HEADER_H + 12;
        for (int i = 0; i < body.length; i++) {
            if (body[i] == null) {
                continue;
            }
            int col = body[i].startsWith("Need") || body[i].startsWith("  ")
                    ? COLOR_MISS : COLOR_STATUS;
            Fonts.p11Full.renderLeft(JagString.of(body[i]), panelX + 6, y, col, 0);
            y += LINE_H;
        }
    }

    public static boolean mouseDown(int mx, int my) {
        if (!visible || rt4.client.gameState != 30) {
            return false;
        }
        ensureLoaded();
        panelH = collapsed ? HEADER_H : panelH;
        if (panelH < HEADER_H) {
            panelH = HEADER_H;
        }
        if (!inside(mx, my)) {
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
        dragging = true;
        dragOffX = mx - panelX;
        dragOffY = my - panelY;
        return true;
    }

    public static void mouseDrag(int mx, int my) {
        if (!dragging) {
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
            panelX = 8;
            panelY = 40;
        }
        clampToScreen();
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
        if (panelX < MARGIN) {
            panelX = MARGIN;
        }
        if (panelY < MARGIN) {
            panelY = MARGIN;
        }
        if (panelX + PANEL_W > cw - MARGIN) {
            panelX = cw - PANEL_W - MARGIN;
        }
        if (panelY + HEADER_H > ch - MARGIN) {
            panelY = ch - HEADER_H - MARGIN;
        }
    }

    private static int btnX() {
        return panelX + PANEL_W - BTN_W - 3;
    }

    private static int btnY() {
        return panelY + 2;
    }

    private static boolean hitCollapseBtn(int mx, int my) {
        return mx >= btnX() && mx < btnX() + BTN_W && my >= btnY() && my < btnY() + 12;
    }

    private static void drawCollapseBtn() {
        fillRect(btnX(), btnY(), BTN_W, 12, COLOR_BTN);
        drawRect(btnX(), btnY(), BTN_W, 12, COLOR_BORDER);
        Fonts.b12Full.renderLeft(JagString.of(collapsed ? "+" : "-"), btnX() + 4, btnY() + 11, COLOR_TITLE, 0);
    }

    private static boolean inside(int mx, int my) {
        return mx >= panelX && mx < panelX + PANEL_W && my >= panelY && my < panelY + panelH;
    }

    private static File layoutFile() {
        return new File(System.getProperty("user.home"), ".amilious-guide-hud.txt");
    }

    private static void loadLayout() {
        BufferedReader in = null;
        try {
            File f = layoutFile();
            if (!f.exists()) {
                return;
            }
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
                } else if ("visible".equals(k)) {
                    visible = "1".equals(v) || "true".equalsIgnoreCase(v);
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
            out.println("visible=" + (visible ? "1" : "0"));
        } catch (Exception ignored) {
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void poll(int mx, int my, boolean down, boolean pressed) {
        if (!visible || rt4.client.gameState != 30) {
            dragging = false;
            return;
        }
        ensureLoaded();
        if (panelH < HEADER_H) {
            panelH = HEADER_H;
        }
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