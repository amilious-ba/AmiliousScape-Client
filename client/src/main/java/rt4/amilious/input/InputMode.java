package rt4.amilious.input;

/**
 * High-level input modes for the client.
 * WORLD = movement / binds; CHAT = typing into chat.
 */
public enum InputMode {
    /** Default: hotkeys, camera, movement. Keys do not go to chat. */
    WORLD,

    /** Chat armed: keys go to the chat buffer. */
    CHAT

    // Future: UI (bank, dialogs) if you want a third mode
}