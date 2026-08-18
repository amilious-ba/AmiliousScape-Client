package rt4.amilious;

import rt4.ClientProt;
import rt4.JagString;
import rt4.Keyboard;
import rt4.WorldMap;
import rt4.client;

public final class MapController {

    public static final int OPEN_MAP = 48889966;
    public static final int CLOSE_MAP = 49479683;

    /** null = unknown, true = open, false = closed */
    private static Boolean open = null;

    /** Base pan speed at zoom == 8 (tweak to taste) */
    private static final float BASE_STEP = 12.0f;

    private MapController() {
    }

    public static void onComponent(int componentId) {
        if (componentId == OPEN_MAP) {
            open = true;
        } else if (componentId == CLOSE_MAP) {
            open = false;
        }
    }

    public static boolean isOpen() {
        return Boolean.TRUE.equals(open);
    }

    public static void open() {
        if (client.gameState != 30) {
            return;
        }
        ClientProt.method4512(JagString.EMPTY, -1, 1, OPEN_MAP);
    }

    public static void close() {
        if (client.gameState != 30) {
            return;
        }
        ClientProt.method4512(JagString.EMPTY, -1, 1, CLOSE_MAP);
    }

    public static void toggle() {
        if (Boolean.TRUE.equals(open)) {
            close();
        } else {
            open();
        }
    }

    /** Larger zoom = more map visible = larger step so feel stays consistent */
    private static int step() {
        float z = WorldMap.zoom;
        if (z < 0.5f) {
            z = 0.5f;
        }
        // zoomed out (z~3) → larger step; zoomed in (z~16) → smaller step
        return Math.max(1, Math.round(16.0f * (8.0f / z)));
    }

    /** Stock zoom presets (getTargetZoom / setTargetZoom) */
    private static final int[] ZOOM_LEVELS = { 37, 50, 75, 100, 200 };

    public static void zoomIn() {
        if (!isOpen() || WorldMap.loadPercentage < 100) {
            return;
        }
        int current = WorldMap.getTargetZoom();
        for (int i = 0; i < ZOOM_LEVELS.length; i++) {
            if (ZOOM_LEVELS[i] == current && i < ZOOM_LEVELS.length - 1) {
                WorldMap.setTargetZoom(ZOOM_LEVELS[i + 1]);
                return;
            }
        }
        // if unknown, step toward more zoomed-in
        WorldMap.setTargetZoom(200);
    }

    public static void zoomOut() {
        if (!isOpen() || WorldMap.loadPercentage < 100) {
            return;
        }
        int current = WorldMap.getTargetZoom();
        for (int i = 0; i < ZOOM_LEVELS.length; i++) {
            if (ZOOM_LEVELS[i] == current && i > 0) {
                WorldMap.setTargetZoom(ZOOM_LEVELS[i - 1]);
                return;
            }
        }
        WorldMap.setTargetZoom(37);
    }

    public static void pan(int dx, int dz) {
        if (!isOpen() || client.gameState != 30 || WorldMap.loadPercentage < 100) {
            return;
        }

        // cancel scripted/mouse lerp toward a target
        WorldMap.anInt4901 = -1;
        WorldMap.anInt3482 = -1;

        WorldMap.anInt435 += dx;
        WorldMap.anInt919 += dz;
        WorldMap.method965();
    }

    /** Call every client tick from AmiliousClient.update() */
    public static void tickInput() {
        if (!isOpen() || client.gameState != 30 || WorldMap.loadPercentage < 100) {
            return;
        }

        int s = step();
        int dx = 0;
        int dz = 0;

        if (Keyboard.pressedKeys[Keyboard.KEY_LEFT])  dx -= s;
        if (Keyboard.pressedKeys[Keyboard.KEY_RIGHT]) dx += s;
        if (Keyboard.pressedKeys[Keyboard.KEY_UP])    dz -= s;
        if (Keyboard.pressedKeys[Keyboard.KEY_DOWN])  dz += s;

        if (dx != 0 || dz != 0) {
            pan(dx, dz);
        }

    }
}