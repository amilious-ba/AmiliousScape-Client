package rt4.amilious;

import rt4.Mouse;
import rt4.amilious.input.InputManager;

/**
 * Regions that eat world / minimenu clicks (HUD, trade popup, …).
 * Register every frame from draw/tick. Clear at start of update.
 */
public final class OverlayClickBlocker {

    private static final int MAX = 16;
    private static final int[] xs = new int[MAX];
    private static final int[] ys = new int[MAX];
    private static final int[] ws = new int[MAX];
    private static final int[] hs = new int[MAX];
    private static int count;

    private OverlayClickBlocker() {
    }

    public static void beginFrame() {
        count = 0;
    }

    public static void add(int x, int y, int w, int h) {
        if (count >= MAX || w <= 0 || h <= 0) {
            return;
        }
        xs[count] = x;
        ys[count] = y;
        ws[count] = w;
        hs[count] = h;
        count++;
    }

    public static boolean contains(int mx, int my) {
        for (int i = 0; i < count; i++) {
            if (mx >= xs[i] && my >= ys[i] && mx < xs[i] + ws[i] && my < ys[i] + hs[i]) {
                return true;
            }
        }
        return false;
    }

    /** Call after overlays have handled the click. */
    public static void consumeIfBlocked() {
        if (Mouse.clickButton == 0) {
            return;
        }
        if (!contains(Mouse.clickX, Mouse.clickY)) {
            return;
        }
        Mouse.clickButton = 0;
        InputManager.consumeMouseClick();
    }
}