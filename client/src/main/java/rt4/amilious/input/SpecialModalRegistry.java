package rt4.amilious.input;

import java.util.HashSet;
import java.util.Set;

/**
 * Interfaces / components that must own text input (above CHAT).
 * Bank and most overlays are NOT listed — they stay WORLD.
 *
 * Add packed component ids or interface ids as you discover text prompts.
 */
public final class SpecialModalRegistry {

    /** Packed component ids known to be text-entry prompts. */
    private static final Set<Integer> TEXT_COMPONENTS = new HashSet<Integer>();

    /** Whole interface ids (top 16 bits) that are always special when open. */
    private static final Set<Integer> TEXT_INTERFACES = new HashSet<Integer>();

    /** Optional: last component that requested special mode (click path). */
    private static int activeComponentId = -1;

    private SpecialModalRegistry() {
    }

    public static void registerComponent(int packedComponentId) {
        TEXT_COMPONENTS.add(packedComponentId);
    }

    public static void registerInterface(int interfaceId) {
        TEXT_INTERFACES.add(interfaceId);
    }

    public static void unregisterComponent(int packedComponentId) {
        TEXT_COMPONENTS.remove(packedComponentId);
    }

    /** Call when a known text prompt opens (from click / script hook later). */
    public static void setActiveComponent(int packedComponentId) {
        activeComponentId = packedComponentId;
    }

    public static void clearActive() {
        activeComponentId = -1;
    }

    /**
     * True when a registered text modal should own the keyboard.
     * v1: activeComponentId in the set, or interface id match.
     * Expand later with InterfaceList.openInterfaces scans if needed.
     */
    public static boolean isActive() {
        if (activeComponentId != -1) {
            if (TEXT_COMPONENTS.contains(activeComponentId)) {
                return true;
            }
            int iface = activeComponentId >>> 16;
            if (TEXT_INTERFACES.contains(iface)) {
                return true;
            }
        }
        return false;
    }

    public static void reset() {
        activeComponentId = -1;
    }

    /** Human-readable label for debug logs, or null if none active. */
    public static String getActiveName() {
        if (activeComponentId == -1) {
            return null;
        }
        String registered = NAMES.get(activeComponentId);
        if (registered != null) {
            return registered;
        }
        int iface = activeComponentId >>> 16;
        int child = activeComponentId & 0xFFFF;
        return "component " + activeComponentId + " (iface " + iface + " child " + child + ")";
    }

    public static int getActiveComponentId() {
        return activeComponentId;
    }

    /** Optional friendly name when registering. */
    private static final java.util.Map<Integer, String> NAMES = new java.util.HashMap<Integer, String>();

    public static void registerComponent(int packedComponentId, String name) {
        TEXT_COMPONENTS.add(packedComponentId);
        if (name != null && !name.isEmpty()) {
            NAMES.put(packedComponentId, name);
        }
    }

}