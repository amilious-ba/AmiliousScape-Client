package rt4.amilious;

import rt4.Component;
import rt4.ComponentPointer;
import rt4.DisplayMode;
import rt4.GameShell;
import rt4.InterfaceList;
import rt4.client;

/**
 * Tutorial progress (iface 371).
 *
 * Fixed:     parent 746:5  (48889861), chat 746:70
 * Resizable: parent 548:4  (35913732)
 *
 * Mode changes: immediate pin + deferred pins while layout settles.
 */
public final class TutorialPatch {

    public static final int PARENT_FIXED = (746 << 16) | 5;
    public static final int PARENT_RESIZABLE = (548 << 16) | 4;
    public static final int CHAT_FIXED = (746 << 16) | 70;

    private static int pendingRepins = 0;
    private static int lastTopLevel = -1;

    private TutorialPatch() {
    }

    /** After InterfaceList.method1626(arg0) in Protocol.method1148. */
    public static void onInterfaceOpen(int interfaceId, int parentId) {
        if (interfaceId != 371) {
            return;
        }
        if (InterfaceList.components == null || InterfaceList.components.length <= 371) {
            return;
        }
        Component[] list = InterfaceList.components[371];
        if (list == null) {
            return;
        }
        position371AboveChat(list);
        pendingRepins = 3;
    }

    /** After InterfaceList.method3712(true) in DisplayMode.setWindowMode. */
    public static void onWindowModeChanged() {
        pendingRepins = 5;
        repinNow();
    }

    /** Call every frame from AmiliousClient.update() while in-game. */
    public static void tick() {
        if (client.gameState != 30) {
            lastTopLevel = InterfaceList.topLevelInterface;
            return;
        }

        int top = InterfaceList.topLevelInterface;
        if (lastTopLevel != -1 && top != lastTopLevel && (top == 548 || top == 746)) {
            pendingRepins = 5;
        }
        lastTopLevel = top;

        if (pendingRepins <= 0) {
            return;
        }
        pendingRepins--;
        repinNow();
    }

    private static void repinNow() {
        if (client.gameState != 30) {
            return;
        }
        if (InterfaceList.components == null || InterfaceList.components.length <= 371) {
            return;
        }
        Component[] list = InterfaceList.components[371];
        if (list == null) {
            return;
        }

        int newParent = isResizableLayout() ? PARENT_RESIZABLE : PARENT_FIXED;
        Component parent = InterfaceList.getComponent(newParent);
        if (parent == null || parent.hidden || parent.height <= 0) {
            return;
        }

        reparentOpenInterface(371, newParent);
        try {
            InterfaceList.method531(parent, false);
        } catch (Exception ignored) {
        }
        position371AboveChat(list);
    }

    private static void position371AboveChat(Component[] list) {
        if (list == null || list.length == 0) {
            return;
        }

        try {
            Component chat = findChatAnchor();
            if (chat == null) {
                return;
            }

            Component root = null;
            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c != null && c.overlayer == -1) {
                    root = c;
                    break;
                }
            }
            if (root == null) {
                return;
            }
            final int rootId = root.id;

            int minY = Integer.MAX_VALUE;
            int maxBottom = Integer.MIN_VALUE;
            int count = 0;

            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null || c.hidden) {
                    continue;
                }
                if (c.overlayer != rootId) {
                    continue;
                }
                if (c.height > 80) {
                    continue;
                }
                count++;
                if (c.y < minY) {
                    minY = c.y;
                }
                int bottom = c.y + c.height;
                if (bottom > maxBottom) {
                    maxBottom = bottom;
                }
            }

            if (count == 0 || minY == Integer.MAX_VALUE) {
                return;
            }

            int blockH = Math.max(8, maxBottom - minY);
            final int GAP = 12;

            int targetScreenTop = chat.y - GAP - blockH;
            if (targetScreenTop < 4) {
                targetScreenTop = 4;
            }
            int maxTop = GameShell.canvasHeight - blockH - 4;
            if (maxTop < 4) {
                maxTop = 4;
            }
            if (targetScreenTop > maxTop) {
                targetScreenTop = maxTop;
            }

            if (root.y < -50 || root.y > GameShell.canvasHeight) {
                int rootDelta = targetScreenTop - minY - root.y;
                root.y += rootDelta;
                root.baseY += rootDelta;
            }

            int targetLocalY = targetScreenTop - root.y;
            if (targetLocalY < 0) {
                targetLocalY = 0;
            }
            if (root.height > 0 && targetLocalY + blockH > root.height) {
                targetLocalY = Math.max(0, root.height - blockH);
            }

            int delta = targetLocalY - minY;
            if (delta == 0) {
                return;
            }

            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null) {
                    continue;
                }
                if (c.overlayer != rootId) {
                    continue;
                }
                if (c.height > 80) {
                    continue;
                }
                c.y += delta;
                c.baseY += delta;
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean isResizableLayout() {
        int top = InterfaceList.topLevelInterface;
        if (top == 548) {
            return true;
        }
        if (top == 746) {
            return false;
        }
        return DisplayMode.hdModeActive;
    }

    private static Component findChatAnchor() {
        Component c = InterfaceList.getComponent(CHAT_FIXED);
        if (c != null && c.height > 0 && !c.hidden) {
            return c;
        }

        int iface = InterfaceList.topLevelInterface;
        if (iface < 0) {
            iface = isResizableLayout() ? 548 : 746;
        }
        if (InterfaceList.components == null || iface >= InterfaceList.components.length) {
            return null;
        }
        Component[] g = InterfaceList.components[iface];
        if (g == null) {
            return null;
        }

        Component best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < g.length; i++) {
            Component x = g[i];
            if (x == null || x.hidden) {
                continue;
            }
            if (x.height < 50 || x.height > 220) {
                continue;
            }
            if (x.width < 200) {
                continue;
            }
            if (x.y < 150) {
                continue;
            }
            int score = x.y + x.width;
            if (score > bestScore) {
                bestScore = score;
                best = x;
            }
        }
        return best;
    }

    private static boolean reparentOpenInterface(int ifaceId, int newParentId) {
        if (InterfaceList.openInterfaces == null) {
            return false;
        }
        try {
            ComponentPointer found = null;
            for (ComponentPointer p = (ComponentPointer) InterfaceList.openInterfaces.head();
                 p != null;
                 p = (ComponentPointer) InterfaceList.openInterfaces.next()) {
                if (p.interfaceId == ifaceId) {
                    found = p;
                    break;
                }
            }
            if (found == null) {
                return false;
            }

            long key = found.key;
            if (key == (newParentId & 0xFFFFFFFFL) || key == newParentId) {
                return true;
            }

            found.unlink();
            InterfaceList.openInterfaces.put(found, newParentId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}