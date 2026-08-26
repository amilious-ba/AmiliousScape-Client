package rt4.amilious.input.action;

/**
 * Game intents. Bind keys or gamepad buttons to these —
 * gameplay code should not read raw keycodes for these paths.
 *
 * No MOVE_* — RuneScape is click-to-walk. Camera is primarily mouse.
 */
public enum Action {

    TOGGLE_MAP,
    TOGGLE_RUN,
    TAB_NEXT,
    TAB_PREV,
    MAP_ZOOM_IN,
    MAP_ZOOM_OUT,
    TOUCH_KEYBOARD,
    CHEAT_TELEPORT,


    // ===== Chat =====

    /** Open chat input (Enter in WORLD mode) */
    OPEN_CHAT,

    /** Close chat without submitting (Esc in CHAT mode) */
    CLOSE_CHAT,

    /** Submit chat message (Enter in CHAT mode) */
    SUBMIT_CHAT,

    /** Open Quick Chat menu (not currently bound) */
    QUICK_CHAT,

    // ===== System / UI =====

    /** Escape key - closes modals, opens logout menu (any mode) */
    ESCAPE,

    // ===== Camera Controls =====
    // Optional key-camera (mouse remains primary)
    // Also used for world map panning

    /**
     * Move camera up / pan map up.
     * - WORLD mode: Arrow Up or W - Protocol.java:2544
     * - MAP mode: Arrow Up or W - MapController.java:125
     */
    CAMERA_UP,

    /**
     * Move camera down / pan map down.
     * - WORLD mode: Arrow Down or S - Protocol.java:2544
     * - MAP mode: Arrow Down or S - MapController.java:126
     */
    CAMERA_DOWN,

    /**
     * Move camera left / pan map left.
     * - WORLD mode: Arrow Left or A - Protocol.java:2544
     * - MAP mode: Arrow Left or A - MapController.java:123
     */
    CAMERA_LEFT,

    /**
     * Move camera right / pan map right.
     * - WORLD mode: Arrow Right or D - Protocol.java:2544
     * - MAP mode: Arrow Right or D - MapController.java:124
     */
    CAMERA_RIGHT,

    // ===== Modifier Keys =====
    // Used for interface hotkeys (InterfaceList.java:681) and staff cheats

    /** Ctrl key held - InterfaceList.java:681, InterfaceList.java:742 */
    MODIFIER_CTRL,

    /** Alt key held - InterfaceList.java:681 */
    MODIFIER_ALT,

    /** Shift key held - InterfaceList.java:681 */
    MODIFIER_SHIFT,

    // ===== Menu Interaction =====

    /**
     * Shift held - use alternative menu action.
     * When shift-clicking an item/NPC with multiple options,
     * uses second-to-last option instead of last.
     *
     * Example: Shift-click "Withdraw-10" instead of "Withdraw-All"
     *
     * Used in:
     * - MiniMenu.java:1392 (menu text display)
     * - MiniMenu.java:1677 (menu action)
     * - MiniMenu.java:1685 (menu action fallback)
     * - LoginManager.java:1510 (cursor selection)
     */
    MENU_ALTERNATIVE_ACTION,

    // ===== Hotkeys =====
    // Placeholder hotkeys — add real ones as needed
    // Currently bound to digit keys 0-9 in WORLD mode

    /** Hotkey 1 (digit 1 in WORLD mode) */
    HOTKEY_1,

    /** Hotkey 2 (digit 2 in WORLD mode) */
    HOTKEY_2,

    /** Hotkey 3 (digit 3 in WORLD mode) */
    HOTKEY_3,

    /** Hotkey 4 (digit 4 in WORLD mode) */
    HOTKEY_4,

    /** Hotkey 5 (digit 5 in WORLD mode) */
    HOTKEY_5,

    /** Hotkey 6 (digit 6 in WORLD mode) */
    HOTKEY_6,

    /** Hotkey 7 (digit 7 in WORLD mode) */
    HOTKEY_7,

    /** Hotkey 8 (digit 8 in WORLD mode) */
    HOTKEY_8,

    /** Hotkey 9 (digit 9 in WORLD mode) */
    HOTKEY_9,

    /** Hotkey 0 (digit 0 in WORLD mode) */
    HOTKEY_0,

    // ===== Menu Tab Selection (F-keys) =====
    // Used for selecting main game interface tabs

    /** Select Combat tab (F1) */
    TAB_COMBAT,

    /** Select Skills tab (F2) */
    TAB_SKILLS,

    /** Select Quest tab (F3) */
    TAB_QUEST,

    /** Select Inventory tab (F4) */
    TAB_INVENTORY,

    /** Select Equipment tab (F5) */
    TAB_EQUIPMENT,

    /** Select Prayer tab (F6) */
    TAB_PRAYER,

    /** Select Magic tab (F7) */
    TAB_MAGIC,

    /** Select Clan tab (F8) */
    TAB_CLAN,

    /** Select Friends tab (F9) */
    TAB_FRIENDS,

    /** Select Ignore tab (F10) */
    TAB_IGNORE,

    /** Select Logout tab (F11) */
    TAB_LOGOUT,

    /** Select Options tab (F12) */
    TAB_OPTIONS,

    /** Select Emotes tab */
    TAB_EMOTES,

    /** Select Music tab */
    TAB_MUSIC
}
