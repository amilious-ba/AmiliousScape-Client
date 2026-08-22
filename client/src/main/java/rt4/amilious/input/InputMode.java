package rt4.amilious.input;

/**
 * Priority (highest first):
 * MAIN_MENU → MAP → SPECIAL_MODAL → CHATBOX_MODAL → CHAT → WORLD
 */
public enum InputMode {
    /** Not in game world (gameState != 30). */
    MAIN_MENU,

    /** World map open. */
    MAP,

    /** True modal text UI (Report Abuse, etc.) — not the chat layer. */
    SPECIAL_MODAL,

    /** Over-chat prompts (amount X, shared iface 752 chrome). */
    CHATBOX_MODAL,

    /** Chat armed and visible. */
    CHAT,

    /** Default in-game. */
    WORLD
}