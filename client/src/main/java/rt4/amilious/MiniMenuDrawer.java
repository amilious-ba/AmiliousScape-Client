package rt4.amilious;

import rt4.Cs1ScriptRunner;
import rt4.Fonts;
import rt4.GlRaster;
import rt4.GlRenderer;
import rt4.InterfaceList;
import rt4.MiniMenu;
import rt4.Mouse;
import rt4.SoftwareRaster;

/**
 * Custom "Choose Option" panel.
 *
 * Drawn only when vanilla would call MiniMenu.drawA / drawB
 * (Cs1ScriptRunner.aBoolean108 == true after right-click).
 *
 * Mouse / keyboard selection wiring comes later — this only paints.
 */
public final class MiniMenuDrawer {

    /** Flip to false to fall back to vanilla drawA/drawB. */
    public static boolean enabled = false;

    private static final int COLOR_PANEL = 0x5D5447; // same family as vanilla 6116423
    private static final int COLOR_HEADER = 0x000000;
    private static final int COLOR_BORDER = 0x000000;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_HOVER = 0xFFFF00;
    private static final int COLOR_TITLE = 0xFFFFFF;

    private static final int ROW_H = 15;
    private static final int HEADER_H = 17;
    private static final int PAD_X = 3;

    /** Index into MiniMenu (0 = top of sorted list / bottom of visual stack in vanilla). */
    private static int selectedIndex = 0;
    private static int scrollOffset = 0;
    private static boolean wasOpen = false;

    private MiniMenuDrawer() {
    }

    /**
     * Called once per frame while aBoolean108 is true, before draw().
     * Resets selection the first frame the menu opens.
     */
    public static void onOpenFrame() {
        if (!wasOpen) {
            wasOpen = true;
            selectedIndex = 0;
            scrollOffset = 0;
        }
        // Clamp if option count changed while open
        if (MiniMenu.size <= 0) {
            selectedIndex = 0;
        } else if (selectedIndex >= MiniMenu.size) {
            selectedIndex = MiniMenu.size - 1;
        }
    }

    /** Called when aBoolean108 becomes false. */
    public static void onClosed() {
        wasOpen = false;
        selectedIndex = 0;
        scrollOffset = 0;
    }


    public static void draw() {
        if (!Cs1ScriptRunner.aBoolean108 || MiniMenu.size <= 0) {
            return;
        }

        // --- measure (same idea as vanilla open path) ---
        int w = Fonts.b12Full.getStringWidth(rt4.LocalizedText.CHOOSE_OPTION) + 8;
        for (int i = 0; i < MiniMenu.size; i++) {
            int ow = Fonts.b12Full.getStringWidth(MiniMenu.getOp(i)) + 8;
            if (ow > w) {
                w = ow;
            }
        }
        int h = MiniMenu.size * ROW_H + (InterfaceList.aBoolean298 ? 26 : 22);

        int canvasW = rt4.GameShell.canvasWidth;
        int canvasH = rt4.GameShell.canvasHeight;

        // Fixed slot: left-aligned, sitting just above the chat strip
        // Tweak CHAT_RESERVE if it overlaps chat in your layout
        final int CHAT_RESERVE = 165; // classic fixed chat ~142–165px
        final int MARGIN = 8;

        int x = MARGIN;
        int y = canvasH - CHAT_RESERVE - h - MARGIN;
        if (y < MARGIN) {
            y = MARGIN;
        }
        if (x + w > canvasW - MARGIN) {
            x = Math.max(MARGIN, canvasW - w - MARGIN);
        }

        // IMPORTANT: publish rect so vanilla close/click math matches what we drew
        InterfaceList.anInt4271 = x;
        InterfaceList.anInt5138 = y;
        InterfaceList.anInt761 = w;
        InterfaceList.anInt436 = h;

        // --- paint ---
        if (GlRenderer.enabled) {
            GlRaster.fillRect(x, y, w, h, COLOR_PANEL);
            GlRaster.fillRect(x + 1, y + 1, w - 2, HEADER_H - 1, COLOR_HEADER);
            GlRaster.drawRect(x + 1, y + HEADER_H + 1, w - 2, h - HEADER_H - 2, COLOR_BORDER);
        } else {
            SoftwareRaster.fillRect(x, y, w, h, COLOR_PANEL);
            SoftwareRaster.fillRect(x + 1, y + 1, w - 2, HEADER_H - 1, COLOR_HEADER);
            SoftwareRaster.drawRect(x + 1, y + HEADER_H + 1, w - 2, h - HEADER_H - 2, COLOR_BORDER);
        }

        Fonts.b12Full.renderLeft(
                rt4.LocalizedText.CHOOSE_OPTION,
                x + PAD_X,
                y + 14,
                COLOR_TITLE,
                0
        );

        int mx = Mouse.lastMouseX;
        int my = Mouse.lastMouseY;
        for (int i = 0; i < MiniMenu.size; i++) {
            int rowBaseline = (MiniMenu.size - i - 1) * ROW_H + y + 31;
            int color = COLOR_TEXT;
            boolean hovered = mx > x && mx < x + w
                    && my > rowBaseline - 13 && my < rowBaseline + 3;
            if (hovered || i == selectedIndex) {
                color = COLOR_HOVER;
            }
            Fonts.b12Full.renderLeft(MiniMenu.getOp(i), x + PAD_X, rowBaseline, color, 0);
        }

        InterfaceList.forceRedrawScreen(x, y, h, w);
    }

    /**
     * Paint using the same rect vanilla already computed:
     * InterfaceList.anInt4271 (x), anInt5138 (y), anInt761 (w), anInt436 (h)
     */
    /*public static void draw() {

        System.out.println("[mm-a] draw size=" + MiniMenu.size
                + " open=" + Cs1ScriptRunner.aBoolean108);

        if (!Cs1ScriptRunner.aBoolean108 || MiniMenu.size <= 0) {
            return;
        }

        System.out.println("[mm-b] draw size=" + MiniMenu.size
                + " open=" + Cs1ScriptRunner.aBoolean108);

        int x = InterfaceList.anInt4271;
        int y = InterfaceList.anInt5138;
        int w = InterfaceList.anInt761;
        int h = InterfaceList.anInt436;

        // Panel background + header strip + border (mirrors drawA layout)
        if (GlRenderer.enabled) {
            GlRaster.fillRect(x, y, w, h, COLOR_PANEL);
            GlRaster.fillRect(x + 1, y + 1, w - 2, HEADER_H - 1, COLOR_HEADER);
            GlRaster.drawRect(x + 1, y + HEADER_H + 1, w - 2, h - HEADER_H - 2, COLOR_BORDER);
        } else {
            SoftwareRaster.fillRect(x, y, w, h, COLOR_PANEL);
            SoftwareRaster.fillRect(x + 1, y + 1, w - 2, HEADER_H - 1, COLOR_HEADER);
            SoftwareRaster.drawRect(x + 1, y + HEADER_H + 1, w - 2, h - HEADER_H - 2, COLOR_BORDER);
        }

        // Title
        Fonts.b12Full.renderLeft(
                rt4.LocalizedText.CHOOSE_OPTION,
                x + PAD_X,
                y + 14,
                COLOR_TITLE,
                0
        );

        // Options — same visual order as vanilla drawA (index 0 at bottom of panel)
        int mx = Mouse.lastMouseX;
        int my = Mouse.lastMouseY;

        for (int i = 0; i < MiniMenu.size; i++) {
            // vanilla: (size - i - 1) * 15 + y + 31
            int rowBaseline = (MiniMenu.size - i - 1) * ROW_H + y + 31;
            int color = COLOR_TEXT;

            boolean hovered = mx > x
                    && mx < x + w
                    && my > rowBaseline - 13
                    && my < rowBaseline + 3;

            if (hovered || i == selectedIndex) {
                color = COLOR_HOVER;
            }

            Fonts.b12Full.renderLeft(
                    MiniMenu.getOp(i),
                    x + PAD_X,
                    rowBaseline,
                    color,
                    0
            );
        }

        InterfaceList.forceRedrawScreen(
                InterfaceList.anInt4271,
                InterfaceList.anInt5138,
                InterfaceList.anInt436,
                InterfaceList.anInt761
        );
    }*/

    public static boolean isOpen() {
        return wasOpen && Cs1ScriptRunner.aBoolean108;
    }

    public static int getSelectedIndex() {
        return selectedIndex;
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
    }
}