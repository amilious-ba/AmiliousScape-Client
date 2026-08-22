package rt4.amilious.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Text-entry modals that sit above CHAT (Report Abuse, future prompts, etc.).
 * Chatbox-layer amount/QC stay on ChatBoxModalRegistry.
 *
 * Keys:
 *   - packed component id (iface << 16 | child)
 *   - interface id via registerInterface / setActiveInterface
 *
 * Names: optional human labels for logs (getActiveName).
 */
public final class SpecialModalRegistry {

    /** Packed component ids known as text prompts. */
    private static final Set<Integer> TEXT_COMPONENTS = new HashSet<Integer>();

    /** Whole interface ids that can own SPECIAL_MODAL when active. */
    private static final Set<Integer> TEXT_INTERFACES = new HashSet<Integer>();

    /**
     * Optional labels.
     * Component keys = packed id.
     * Interface keys = ifaceKey(iface) so they never clash with packed ids.
     */
    private static final Map<Integer, String> NAMES = new HashMap<Integer, String>();

    private static int activeComponentId = -1;
    private static int activeInterfaceId = -1;

    private SpecialModalRegistry() {
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    public static void registerComponent(int packedComponentId) {
        TEXT_COMPONENTS.add(packedComponentId);
    }

    public static void registerComponent(int packedComponentId, String name) {
        TEXT_COMPONENTS.add(packedComponentId);
        if (name != null && !name.isEmpty()) {
            NAMES.put(packedComponentId, name);
        }
    }

    public static void registerInterface(int interfaceId) {
        TEXT_INTERFACES.add(interfaceId);
    }

    public static void registerInterface(int interfaceId, String name) {
        TEXT_INTERFACES.add(interfaceId);
        if (name != null && !name.isEmpty()) {
            NAMES.put(ifaceKey(interfaceId), name);
        }
    }

    public static void unregisterComponent(int packedComponentId) {
        TEXT_COMPONENTS.remove(packedComponentId);
        NAMES.remove(packedComponentId);
    }

    public static void unregisterInterface(int interfaceId) {
        TEXT_INTERFACES.remove(interfaceId);
        NAMES.remove(ifaceKey(interfaceId));
    }

    // -------------------------------------------------------------------------
    // Active state
    // -------------------------------------------------------------------------

    public static void setActiveComponent(int packedComponentId) {
        activeComponentId = packedComponentId;
        activeInterfaceId = -1;
    }

    public static void setActiveInterface(int interfaceId) {
        activeInterfaceId = interfaceId;
        activeComponentId = -1;
    }

    /**
     * Convenience: set by registered name (component or interface).
     * Prefer setActiveComponent / setActiveInterface when id is known.
     */
    public static void setActive(String name) {
        if (name == null) {
            return;
        }
        for (Map.Entry<Integer, String> e : NAMES.entrySet()) {
            if (name.equals(e.getValue())) {
                int key = e.getKey().intValue();
                if ((key & 0x40000000) != 0) {
                    setActiveInterface(key & ~0x40000000);
                } else {
                    setActiveComponent(key);
                }
                return;
            }
        }
        // Unregistered name: still mark active via a synthetic interface-less flag
        // by storing name under activeInterfaceId -1 is cleared — use component -2 sentinel? Skip.
        // Caller should register first.
    }

    public static void clearActive() {
        activeComponentId = -1;
        activeInterfaceId = -1;
    }

    public static void reset() {
        clearActive();
    }

    public static boolean isActive() {
        if (activeInterfaceId != -1) {
            return TEXT_INTERFACES.contains(activeInterfaceId) || NAMES.containsKey(ifaceKey(activeInterfaceId));
        }
        if (activeComponentId != -1) {
            if (TEXT_COMPONENTS.contains(activeComponentId)) {
                return true;
            }
            int iface = activeComponentId >>> 16;
            if (TEXT_INTERFACES.contains(iface)) {
                return true;
            }
            // Allow active even if only named (click path before register)
            if (NAMES.containsKey(activeComponentId)) {
                return true;
            }
            return true; // explicit setActiveComponent still counts
        }
        return false;
    }

    public static int getActiveComponentId() {
        return activeComponentId;
    }

    public static int getActiveInterfaceId() {
        return activeInterfaceId;
    }

    /** Human-readable label for logs, or null if none active. */
    public static String getActiveName() {
        if (activeInterfaceId != -1) {
            String n = NAMES.get(ifaceKey(activeInterfaceId));
            return n != null ? n : ("iface " + activeInterfaceId);
        }
        if (activeComponentId != -1) {
            String n = NAMES.get(activeComponentId);
            if (n != null) {
                return n;
            }
            int iface = activeComponentId >>> 16;
            int child = activeComponentId & 0xFFFF;
            return "component " + activeComponentId + " (iface " + iface + " child " + child + ")";
        }
        return null;
    }

    /** Distinguish interface name keys from packed component ids. */
    private static int ifaceKey(int interfaceId) {
        return interfaceId | 0x40000000;
    }
}