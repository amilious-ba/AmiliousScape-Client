package rt4.amilious;

import rt4.ClientProt;
import rt4.Component;
import rt4.InterfaceList;
import rt4.JagString;
import rt4.client;

/**
 * Keyboard / controller highlight on "Select an Option"
 * and "Click here to continue". Does not redraw the interface.
 */
public final class DialogueController {

    public static boolean enabled = true;

    private static final int COLOR_HIGHLIGHT = 0xFFFF00;

    /** 530 option / continue interfaces. Not 137 (chat). */
    private static final int[] OPTION_IFACES = {
            140, 228, 229, 230, 231, 232, 233, 234,
            64, 241, 242, 243, 244, 210, 211
    };

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

    /**
     * Call from LoginManager.method1841() immediately before method1949
     * so the color is set for this frame's interface draw.
     */
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
            System.out.println("[dialogue] confirm missing id=" + id);
            return;
        }

        // opcode 132 — same family the server uses for dialogue buttons
        rt4.MiniMenu.method10(c.createdComponentId, id);

        System.out.println("[dialogue] confirm-132 selected=" + selected
                + " id=" + id
                + " child=" + (id & 0xFFFF)
                + " slot=" + c.createdComponentId
                + " text=" + (c.text != null ? c.text : "")
                + " continue=" + continueOnly);
        reset();
    }

    private static boolean isIfaceOpen(int iface) {
        if (InterfaceList.openInterfaces == null) {
            return false;
        }
        for (rt4.ComponentPointer p = (rt4.ComponentPointer) InterfaceList.openInterfaces.head();
             p != null;
             p = (rt4.ComponentPointer) InterfaceList.openInterfaces.next()) {
            if (p.interfaceId == iface) {
                return true;
            }
        }
        return false;
    }

    public static void reset() {
        for (int i = 0; i < optionCount; i++) {
            Component c = safeGet(OPTION_IDS[i]);
            if (c != null && OPTION_COLORS[i] != COLOR_HIGHLIGHT) {
                c.color = OPTION_COLORS[i];
            }
        }
        optionCount = 0;
        selected = 0;
        activeIface = -1;
        open = false;
        continueOnly = false;
    }

    private static void scan() {
        if (InterfaceList.components == null) {
            reset();
            return;
        }

        int foundIface = -1;
        boolean foundContinue = false;

        for (int i = 0; i < OPTION_IFACES.length; i++) {
            int iface = OPTION_IFACES[i];
            if (iface < 0 || iface >= InterfaceList.components.length) {
                continue;
            }
            if (!isIfaceOpen(iface)) {
                continue;
            }
            Component[] list = InterfaceList.components[iface];
            if (list == null) {
                continue;
            }
            if (hasVisibleText(list, "select an option")) {
                foundIface = iface;
                foundContinue = false;
                break;
            }
            if (hasVisibleText(list, "click here to continue")) {
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
        int[] foundIds = new int[OPTION_IDS.length];
        int n = 0;
        for (int i = 0; i < list.length && n < foundIds.length; i++) {
            Component c = list[i];
            if (c == null || c.hidden || c.text == null) {
                continue;
            }
            String t = c.text.toString();
            if (t.length() == 0) {
                continue;
            }
            String low = t.toLowerCase();
            if (foundContinue) {
                if (!low.contains("click here to continue")) {
                    continue;
                }
            } else {
                if (low.contains("select an option")) {
                    continue;
                }
                if (low.contains("click here to continue")) {
                    continue;
                }
                if (c.type != 4) {
                    continue;
                }
            }
            foundIds[n] = c.id;
            n++;
        }

        if (n == 0) {
            if (open) {
                reset();
            }
            return;
        }

        boolean samePage = open && foundIface == activeIface && n == optionCount;
        if (samePage) {
            for (int i = 0; i < n; i++) {
                if (foundIds[i] != OPTION_IDS[i]) {
                    samePage = false;
                    break;
                }
            }
        }

        boolean newlyOpen = !samePage;
        if (newlyOpen) {
            reset();
            selected = 0;
            for (int i = 0; i < n; i++) {
                OPTION_IDS[i] = foundIds[i];
                Component c = safeGet(foundIds[i]);
                int col = (c != null) ? c.color : 0;
                if (col == COLOR_HIGHLIGHT) {
                    col = 0x000000;
                }
                OPTION_COLORS[i] = col;
            }
            System.out.println("[dialogue] open iface=" + foundIface
                    + " options=" + n + " continue=" + foundContinue);
        }

        optionCount = n;
        for (int i = 0; i < n; i++) {
            OPTION_IDS[i] = foundIds[i];
        }
        if (selected >= n) {
            selected = n - 1;
        }
        activeIface = foundIface;
        continueOnly = foundContinue;
        open = true;
    }

    private static boolean hasVisibleText(Component[] list, String needle) {
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