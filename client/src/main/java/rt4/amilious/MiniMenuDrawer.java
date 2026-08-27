package rt4.amilious;

import rt4.Cs1ScriptRunner;
import rt4.Fonts;
import rt4.GameShell;
import rt4.GlRaster;
import rt4.GlRenderer;
import rt4.InterfaceList;
import rt4.MiniMenu;
import rt4.SoftwareRaster;
import rt4.amilious.input.InputManager;

/**
 * Larger "Choose Option" panel for mouse + controller.
 * Enable with MiniMenuDrawer.enabled = true.
 *
 * Vanilla list order: index 0 = bottom row, size-1 = top row.
 */
public final class MiniMenuDrawer {

    public static boolean enabled = true;

    private static final int COLOR_PANEL = 0x5D5447;
    private static final int COLOR_HEADER = 0x000000;
    private static final int COLOR_BORDER = 0x000000;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_HOVER = 0xFFFF00;
    private static final int COLOR_TITLE = 0xFFFFFF;
    private static final int COLOR_SEL_BG = 0x3A342C;

    private static final int ROW_H = 24;
    private static final int HEADER_H = 22;
    private static final int PAD_X = 8;
    private static final int MARGIN = 8;
    private static final int CHAT_RESERVE = 165;
    private static final int MAX_VISIBLE = 12;

    private static int selectedIndex = 0;
    private static int scrollOffset = 0;
    private static boolean wasOpen = false;

    private MiniMenuDrawer() {
    }

    public static void onOpenFrame() {
        if (!wasOpen) {
            wasOpen = true;
            // top visible row = last index
            selectedIndex = MiniMenu.size > 0 ? MiniMenu.size - 1 : 0;
            scrollOffset = 0;
            ensureSelectedVisible();
        }
        if (MiniMenu.size <= 0) {
            selectedIndex = 0;
        } else if (selectedIndex >= MiniMenu.size) {
            selectedIndex = MiniMenu.size - 1;
        }
        ensureSelectedVisible();
    }

    public static void onClosed() {
        wasOpen = false;
        selectedIndex = 0;
        scrollOffset = 0;
    }

    public static boolean isOpen() {
        return wasOpen && Cs1ScriptRunner.aBoolean108 && MiniMenu.size > 0;
    }

    public static int getSelectedIndex() {
        return selectedIndex;
    }

    /** Visual up = toward top of panel = higher index. */
    public static void moveUp() {
        if (!isOpen()) {
            return;
        }
        if (selectedIndex < MiniMenu.size - 1) {
            selectedIndex++;
            ensureSelectedVisible();
        }
    }

    /** Visual down = toward bottom of panel = lower index. */
    public static void moveDown() {
        if (!isOpen()) {
            return;
        }
        if (selectedIndex > 0) {
            selectedIndex--;
            ensureSelectedVisible();
        }
    }

    public static void confirm() {
        if (!isOpen()) {
            return;
        }
        MiniMenu.doAction(selectedIndex);
        Cs1ScriptRunner.aBoolean108 = false;
        onClosed();
    }

    public static void cancel() {
        Cs1ScriptRunner.aBoolean108 = false;
        onClosed();
    }

    public static void setSelectedIndex(int index) {
        if (MiniMenu.size <= 0) {
            selectedIndex = 0;
            return;
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= MiniMenu.size) {
            index = MiniMenu.size - 1;
        }
        selectedIndex = index;
        ensureSelectedVisible();
    }

    public static void draw() {
        if (!Cs1ScriptRunner.aBoolean108 || MiniMenu.size <= 0) {
            return;
        }

        int visible = MiniMenu.size;
        if (visible > MAX_VISIBLE) {
            visible = MAX_VISIBLE;
        }

        int w = Fonts.b12Full.getStringWidth(rt4.LocalizedText.CHOOSE_OPTION) + PAD_X * 2 + 8;
        for (int i = 0; i < MiniMenu.size; i++) {
            int ow = Fonts.b12Full.getStringWidth(MiniMenu.getOp(i)) + PAD_X * 2 + 8;
            if (ow > w) {
                w = ow;
            }
        }

        int h = HEADER_H + visible * ROW_H + 8;

        int canvasW = GameShell.canvasWidth;
        int canvasH = GameShell.canvasHeight;
        int x = MARGIN;
        int y = canvasH - CHAT_RESERVE - h - MARGIN;
        if (y < MARGIN) {
            y = MARGIN;
        }
        if (x + w > canvasW - MARGIN) {
            x = Math.max(MARGIN, canvasW - w - MARGIN);
        }

        InterfaceList.anInt4271 = x;
        InterfaceList.anInt5138 = y;
        InterfaceList.anInt761 = w;
        InterfaceList.anInt436 = h;

        if (GlRenderer.enabled) {
            GlRaster.fillRect(x, y, w, h, COLOR_PANEL);
            GlRaster.fillRect(x + 1, y + 1, w - 2, HEADER_H - 2, COLOR_HEADER);
            GlRaster.drawRect(x, y, w, h, COLOR_BORDER);
        } else {
            SoftwareRaster.fillRect(x, y, w, h, COLOR_PANEL);
            SoftwareRaster.fillRect(x + 1, y + 1, w - 2, HEADER_H - 2, COLOR_HEADER);
            SoftwareRaster.drawRect(x, y, w, h, COLOR_BORDER);
        }

        Fonts.b12Full.renderLeft(
                rt4.LocalizedText.CHOOSE_OPTION,
                x + PAD_X,
                y + 16,
                COLOR_TITLE,
                0
        );

        int mx = InputManager.getCursorX();
        int my = InputManager.getCursorY();

        int start = MiniMenu.size - 1 - scrollOffset;
        int drawn = 0;
        for (int i = start; i >= 0 && drawn < visible; i--) {
            int rowTop = y + HEADER_H + drawn * ROW_H;
            int baseline = rowTop + 17;

            boolean hovered = mx > x && mx < x + w
                    && my >= rowTop && my < rowTop + ROW_H;
            if (hovered) {
                selectedIndex = i;
            }

            if (i == selectedIndex) {
                if (GlRenderer.enabled) {
                    GlRaster.fillRect(x + 2, rowTop, w - 4, ROW_H, COLOR_SEL_BG);
                } else {
                    SoftwareRaster.fillRect(x + 2, rowTop, w - 4, ROW_H, COLOR_SEL_BG);
                }
            }

            int color = (i == selectedIndex || hovered) ? COLOR_HOVER : COLOR_TEXT;
            Fonts.b12Full.renderLeft(MiniMenu.getOp(i), x + PAD_X, baseline, color, 0);
            drawn++;
        }

        InterfaceList.forceRedrawScreen(x, y, h, w);
    }

    /** Packed index, or -1 if not on a row. */
    public static int hitTest(int px, int py) {
        if (!enabled || MiniMenu.size <= 0) {
            return -1;
        }
        int x = InterfaceList.anInt4271;
        int y = InterfaceList.anInt5138;
        int w = InterfaceList.anInt761;
        int visible = MiniMenu.size < MAX_VISIBLE ? MiniMenu.size : MAX_VISIBLE;

        if (px <= x || px >= x + w) {
            return -1;
        }

        int start = MiniMenu.size - 1 - scrollOffset;
        int drawn = 0;
        for (int i = start; i >= 0 && drawn < visible; i--) {
            int rowTop = y + HEADER_H + drawn * ROW_H;
            if (py >= rowTop && py < rowTop + ROW_H) {
                return i;
            }
            drawn++;
        }
        return -1;
    }


    public static boolean handleClick(int px, int py) {
        if (!enabled || !Cs1ScriptRunner.aBoolean108) {
            return false;
        }

        // Temp: this click belongs to the menu, not the world
        rt4.amilious.input.InputManager.consumeMouseClick();
        rt4.Mouse.clickButton = 0;

        if (MiniMenu.size > 0 && contains(px, py)) {
            int index = hitTest(px, py);
            if (index != -1) {
                MiniMenu.doAction(index);
            }
        }

        Cs1ScriptRunner.aBoolean108 = false;
        InterfaceList.redrawScreen(
                InterfaceList.anInt4271, InterfaceList.anInt761,
                InterfaceList.anInt5138, InterfaceList.anInt436);
        onClosed();
        return true;
    }

    /** True if this click belongs to the panel (consume + maybe select). */
    /*public static boolean handleClick(int px, int py) {
        if (!enabled || !Cs1ScriptRunner.aBoolean108 || MiniMenu.size <= 0) {
            return false;
        }
        if (!contains(px, py)) {
            return false;
        }

        rt4.amilious.input.InputManager.consumeMouseClick();

        int index = hitTest(px, py);
        if (index != -1) {
            MiniMenu.doAction(index);
        }

        Cs1ScriptRunner.aBoolean108 = false;
        InterfaceList.redrawScreen(
                InterfaceList.anInt4271, InterfaceList.anInt761,
                InterfaceList.anInt5138, InterfaceList.anInt436);
        onClosed();
        return true;
    }*/

    public static boolean contains(int px, int py) {
        if (!enabled) {
            return false;
        }
        int x = InterfaceList.anInt4271;
        int y = InterfaceList.anInt5138;
        int w = InterfaceList.anInt761;
        int h = InterfaceList.anInt436;
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    private static void ensureSelectedVisible() {
        int visible = MiniMenu.size < MAX_VISIBLE ? MiniMenu.size : MAX_VISIBLE;
        int topIndex = MiniMenu.size - 1 - scrollOffset;
        int bottomIndex = topIndex - visible + 1;
        if (bottomIndex < 0) {
            bottomIndex = 0;
        }
        if (selectedIndex > topIndex) {
            scrollOffset = MiniMenu.size - 1 - selectedIndex;
        } else if (selectedIndex < bottomIndex) {
            scrollOffset = MiniMenu.size - visible - selectedIndex;
        }
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
        int maxScroll = MiniMenu.size - visible;
        if (maxScroll < 0) {
            maxScroll = 0;
        }
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }
}