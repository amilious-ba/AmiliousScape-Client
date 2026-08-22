package rt4.amilious.input;

/**
 * Over-chat text prompts (amount, etc.) on the chat-adjacent layer (iface 752).
 * Driven by setHidden on chrome components — not mini-menu strings.
 */
public final class ChatBoxModalRegistry {

    public static final int CHROME_A = 49283074; // 752:2
    public static final int CHROME_B = 49283075; // 752:3
    public static final int INPUT = 49283077;    // 752:5 *

    private static boolean active;
    private static String activeName = "chatbox-modal";

    private ChatBoxModalRegistry() {
    }

    public static void setActive(String name) {
        active = true;
        activeName = name != null ? name : "chatbox-modal";
    }

    public static void clearActive() {
        active = false;
        activeName = null;
    }

    public static boolean isActive() {
        return active;
    }

    public static String getActiveName() {
        return active ? activeName : null;
    }

    public static void reset() {
        clearActive();
    }

    public static boolean isChromeComponent(int packedId) {
        return packedId == CHROME_A || packedId == CHROME_B;
    }
}