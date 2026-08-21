package rt4.amilious.input;

/**
 * Tunables for the input system. Can later mirror GlobalJsonConfig.
 */
public final class InputConfig {

    /** Master switch. When false, behave closer to vanilla (always chat-capable). */
    public boolean enabled = true;

    /** Press Enter in WORLD to enter CHAT mode. */
    public boolean enterOpensChat = true;

    /** Esc always returns to WORLD from CHAT. */
    public boolean escapeClosesChat = true;

    /** After sending a message, return to WORLD automatically. */
    public boolean autoWorldAfterSend = true;

    /**
     * Empty Enter in CHAT:
     * false = close chat / no Quick Chat
     * true  = allow vanilla Quick Chat path when hooked
     */
    public boolean allowQuickChatOnEmptyEnter = false;

    /** If chat UI is collapsed/hidden, force WORLD and ignore open-chat. */
    public boolean forceWorldWhenChatHidden = true;

    private InputConfig() {
    }

    public static final InputConfig INSTANCE = new InputConfig();
}