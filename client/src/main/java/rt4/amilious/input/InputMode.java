package rt4.amilious.input;

/**
 * High-level input modes.
 * Priority (highest first): MAIN_MENU → MAP → SPECIAL_MODAL → CHAT → WORLD
 */
public enum InputMode {
    /** Not in game world (gameState != 30). */
    MAIN_MENU,

    /** World map open (chat is hidden). Nav keys pan/zoom. */
    MAP,

    /** Text-hungry interface (report, quantity type-in, etc.). Above chat. */
    SPECIAL_MODAL,

    /** Chat armed and visible. Keys go to chat buffer. */
    CHAT,

    /** Default in-game: hotkeys, optional key-camera. Bank/normal overlays stay here. */
    WORLD
}