package rt4.amilious;

import rt4.ClientProt;
import rt4.Component;
import rt4.InterfaceList;
import rt4.JagString;
import rt4.client;

/**
 * Keyboard / controller highlight on the parchment "Select an Option"
 * (and "Click here to continue"). Does not redraw the interface.
 */
public final class DialogueController {

    public static boolean enabled = true;

    private static final int COLOR_HIGHLIGHT = 0xFFFF00;

    private static final int[] OPTION_IDS = new int[8];
    private static final int[] OPTION_COLORS = new int[8];
    private static int optionCount = 0;
    private static int selected = 0;
    private static int activeIface = -1;
    private static boolean open = false;
    private static boolean continueOnly = false;

    private DialogueController() {
    }

    public static void init() {
        reset();
    }

    /** Call from AmiliousClient.update() before InputManager.tick(), in-game only. */
    public static void tick() {
        if (!enabled || client.gameState != 30) {
            if (open) {
                reset();
            }
            return;
        }
        scan();
    }

    /** Call from AmiliousClient.onDrawOverlay() so CS2 color writes do not win. */
    public static void applyHighlight() {
        if (!open) {
            return;
        }
        for (int i = 0; i < optionCount; i++) {
            Component c = safeGet(OPTION_IDS[i]);
            if (c == null) {
                continue;
            }
            c.color = (i == selected) ? COLOR_HIGHLIGHT : OPTION_COLORS[i];
        }
    }

    public static boolean isOpen() {
        return enabled && open && optionCount > 0;
    }

    public static void moveUp() {
        if (!isOpen()) {
            return;
        }
        if (selected > 0) {
            selected--;
        }
    }

    public static void moveDown() {
        if (!isOpen()) {
            return;
        }
        if (selected < optionCount - 1) {
            selected++;
        }
    }

    public static void confirm() {
        if (!isOpen()) {
            return;
        }
        int id = OPTION_IDS[selected];
        Component c = safeGet(id);
        if (c == null) {
            return;
        }
        JagString text = c.text != null ? c.text : JagString.parse("");
        int child = id & 0xFFFF;
        ClientProt.method4512(text, child, 1, id);
        System.out.println("[dialogue] confirm iface=" + activeIface
                + " child=" + child + " continue=" + continueOnly);
    }

    public static void reset() {
        optionCount = 0;
        selected = 0;
        activeIface = -1;
        open = false;
        continueOnly = false;
    }

    private static void scan() {
        if (InterfaceList.components == null || InterfaceList.openInterfaces == null) {
            reset();
            return;
        }

        int foundIface = -1;
        boolean foundContinue = false;

        for (rt4.ComponentPointer p = (rt4.ComponentPointer) InterfaceList.openInterfaces.head();
             p != null;
             p = (rt4.ComponentPointer) InterfaceList.openInterfaces.next()) {
            int iface = p.interfaceId;
            if (iface < 0 || iface >= InterfaceList.components.length) {
                continue;
            }
            Component[] list = InterfaceList.components[iface];
            if (list == null) {
                continue;
            }
            if (hasTitle(list, "select an option")) {
                foundIface = iface;
                foundContinue = false;
                break;
            }
            if (hasTitle(list, "click here to continue")) {
                foundIface = iface;
                foundContinue = true;
                break;
            }
        }

        if (foundIface < 0) {
            if (open) {
                reset();
            }
            return;
        }

        Component[] list = InterfaceList.components[foundIface];
        int n = 0;
        for (int i = 0; i < list.length && n < OPTION_IDS.length; i++) {
            Component c = list[i];
            if (c == null || c.hidden || c.text == null) {
                continue;
            }
            String t = c.text.toString();
            if (t.length() == 0) {
                continue;
            }
            String low = t.toLowerCase();
            if (low.contains("select an option")) {
                continue;
            }
            if (!foundContinue && low.contains("click here to continue")) {
                continue;
            }
            // text labels only (type 4). Some option rows are type 0 with a child — still catch type 4.
            if (c.type != 4 && !foundContinue) {
                continue;
            }
            OPTION_IDS[n] = c.id;
            OPTION_COLORS[n] = c.color;
            n++;
        }

        if (n == 0) {
            reset();
            return;
        }

        if (foundIface != activeIface) {
            selected = 0;
        }
        if (selected >= n) {
            selected = n - 1;
        }
        optionCount = n;
        activeIface = foundIface;
        continueOnly = foundContinue;
        open = true;
    }

    private static boolean hasTitle(Component[] list, String needle) {
        for (int i = 0; i < list.length; i++) {
            Component c = list[i];
            if (c == null || c.hidden || c.text == null) {
                continue;
            }
            if (c.text.toString().toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static Component safeGet(int packedId) {
        try {
            if (InterfaceList.components == null) {
                return null;
            }
            int iface = packedId >>> 16;
            if (iface < 0 || iface >= InterfaceList.components.length) {
                return null;
            }
            if (InterfaceList.components[iface] == null) {
                return null;
            }
            return InterfaceList.method1418(packedId, -1);
        } catch (Exception e) {
            return null;
        }
    }
}