package rt4.amilious.input.action;

/**
 * Game intents. Bind keys or gamepad buttons to these —
 * gameplay code should not read raw keycodes for these paths.
 *
 * No MOVE_* — RuneScape is click-to-walk. Camera is primarily mouse.
 */
public enum Action {
    // Chat
    OPEN_CHAT,
    CLOSE_CHAT,
    SUBMIT_CHAT,
    QUICK_CHAT,

    // System / UI
    ESCAPE,

    // Optional key-camera (mouse remains primary)
    CAMERA_UP,
    CAMERA_DOWN,
    CAMERA_LEFT,
    CAMERA_RIGHT,

    // Placeholder hotkeys — add real ones as needed
    HOTKEY_1,
    HOTKEY_2,
    HOTKEY_3,
    HOTKEY_4,
    HOTKEY_5,
    HOTKEY_6,
    HOTKEY_7,
    HOTKEY_8,
    HOTKEY_9,
    HOTKEY_0
}