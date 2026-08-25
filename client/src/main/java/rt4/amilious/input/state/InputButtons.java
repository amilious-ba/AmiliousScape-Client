package rt4.amilious.input.state;

/**
 * Logical button indices inside InputFrame.
 * Not AWT keycodes — KeyboardDevice will map Keyboard.CODE_MAP → these indices.
 *
 * Naming convention:
 *   - Keyboard keys use KB_ prefix
 *   - Gamepad buttons use GP_ prefix
 *   - Legacy unprefixed names kept for backward compatibility (to be deprecated later)
 */
public final class InputButtons {

    private InputButtons() {
    }

    // ===== Core keyboard buttons (legacy names, no prefix for compatibility) =====
    public static final int ENTER = 0;
    public static final int ESCAPE = 1;
    public static final int BACKSPACE = 2;
    public static final int TAB = 3;
    public static final int SPACE = 4;

    // ===== Modifiers (CRITICAL - used everywhere) =====
    public static final int KB_CTRL = 5;
    public static final int KB_SHIFT = 6;
    public static final int KB_ALT = 7;

    // ===== WASD (legacy names) =====
    public static final int W = 10;
    public static final int A = 11;
    public static final int S = 12;
    public static final int D = 13;

    // ===== Arrow keys (legacy names) =====
    public static final int UP = 20;
    public static final int DOWN = 21;
    public static final int LEFT = 22;
    public static final int RIGHT = 23;

    // ===== Function keys =====
    public static final int F1 = 30;
    public static final int F2 = 31;
    public static final int F3 = 32;
    public static final int F4 = 33;
    public static final int F5 = 34;
    public static final int F6 = 35;
    public static final int F7 = 36;
    public static final int F8 = 37;
    public static final int F9 = 38;
    public static final int F10 = 39;
    public static final int F11 = 40;
    public static final int F12 = 41;

    // ===== Number row =====
    public static final int DIGIT_1 = 50;
    public static final int DIGIT_2 = 51;
    public static final int DIGIT_3 = 52;
    public static final int DIGIT_4 = 53;
    public static final int DIGIT_5 = 54;
    public static final int DIGIT_6 = 55;
    public static final int DIGIT_7 = 56;
    public static final int DIGIT_8 = 57;
    public static final int DIGIT_9 = 58;
    public static final int DIGIT_0 = 59;

    // ===== Letter keys (A-Z, mapped by Keyboard code) =====
    public static final int KB_Q = 60;  // Code 32
    public static final int KB_E = 61;  // Code 34
    public static final int KB_R = 62;  // Code 35
    public static final int KB_T = 63;  // Code 36
    public static final int KB_Y = 64;  // Code 37
    public static final int KB_U = 65;  // Code 38
    public static final int KB_I = 66;  // Code 39
    public static final int KB_O = 67;  // Code 40
    public static final int KB_P = 68;  // Code 41
    public static final int KB_F = 69;  // Code 51
    public static final int KB_G = 70;  // Code 52
    public static final int KB_H = 71;  // Code 53
    public static final int KB_J = 72;  // Code 54
    public static final int KB_K = 73;  // Code 55
    public static final int KB_L = 74;  // Code 56
    public static final int KB_Z = 75;  // Code 64
    public static final int KB_X = 76;  // Code 65
    public static final int KB_C = 77;  // Code 66
    public static final int KB_V = 78;  // Code 67
    public static final int KB_B = 79;  // Code 68
    public static final int KB_N = 80;  // Code 69
    public static final int KB_M = 81;  // Code 70

    // ===== Punctuation / Symbol keys =====
    public static final int KB_MINUS = 82;          // Code 26
    public static final int KB_EQUALS = 83;         // Code 27
    public static final int KB_OPEN_BRACKET = 84;   // Code 42
    public static final int KB_CLOSE_BRACKET = 85;  // Code 43
    public static final int KB_SEMICOLON = 86;      // Code 57
    public static final int KB_QUOTE = 87;          // Code 58/59 (depends on JVM)
    public static final int KB_BACK_QUOTE = 88;     // Code 28/58 (depends on JVM)
    public static final int KB_COMMA = 89;          // Code 71
    public static final int KB_PERIOD = 90;         // Code 72
    public static final int KB_SLASH = 91;          // Code 73
    public static final int KB_BACK_SLASH = 92;     // Code 74

    // ===== Navigation keys =====
    public static final int KB_INSERT = 93;    // Code 100
    public static final int KB_DELETE = 94;    // Code 101
    public static final int KB_HOME = 95;      // Code 102
    public static final int KB_END = 96;       // Code 103
    public static final int KB_PAGE_UP = 97;   // Code 104
    public static final int KB_PAGE_DOWN = 98; // Code 105

    // ===== Numpad keys =====
    public static final int KB_NUMPAD_0 = 110; // Code 228
    public static final int KB_NUMPAD_1 = 111; // Code 231
    public static final int KB_NUMPAD_2 = 112; // Code 227
    public static final int KB_NUMPAD_3 = 113; // Code 233
    public static final int KB_NUMPAD_4 = 114; // Code 224
    public static final int KB_NUMPAD_5 = 115; // Code 219
    public static final int KB_NUMPAD_6 = 116; // Code 225
    public static final int KB_NUMPAD_7 = 117; // Code 230
    public static final int KB_NUMPAD_8 = 118; // Code 226
    public static final int KB_NUMPAD_9 = 119; // Code 232
    public static final int KB_NUMPAD_MULTIPLY = 120; // Code 89
    public static final int KB_NUMPAD_ADD = 121;      // Code 87
    public static final int KB_NUMPAD_SUBTRACT = 122; // Code 88
    public static final int KB_NUMPAD_DECIMAL = 123;  // Code 229
    public static final int KB_NUMPAD_DIVIDE = 124;   // Code 90

    // ===== Other special keys =====
    public static final int KB_CLEAR = 125; // Code 91

    // ===== Mouse buttons (130-139) =====
    public static final int MOUSE_BUTTON_1 = 130; // Left click
    public static final int MOUSE_BUTTON_2 = 131; // Right click
    public static final int MOUSE_BUTTON_3 = 132; // Middle click
    public static final int MOUSE_CLICK = 133;    // Any click occurred this frame

    // ===== Gamepad buttons (reserved 200-255) =====
    public static final int GP_A = 200;
    public static final int GP_B = 201;
    public static final int GP_X = 202;
    public static final int GP_Y = 203;
    public static final int GP_LB = 204;
    public static final int GP_RB = 205;
    public static final int GP_START = 206;
    public static final int GP_BACK = 207;
    public static final int GP_DPAD_UP = 208;
    public static final int GP_DPAD_DOWN = 209;
    public static final int GP_DPAD_LEFT = 210;
    public static final int GP_DPAD_RIGHT = 211;

    // Virtual buttons for analog stick directions (converted from axes)
    public static final int GP_LSTICK_UP = 212;
    public static final int GP_LSTICK_DOWN = 213;
    public static final int GP_LSTICK_LEFT = 214;
    public static final int GP_LSTICK_RIGHT = 215;

    // Total button count (must be large enough for all buttons)
    public static final int BUTTON_COUNT = 256;

    // Axes (for analog sticks, triggers)
    public static final int AXIS_LEFT_X = 0;
    public static final int AXIS_LEFT_Y = 1;
    public static final int AXIS_RIGHT_X = 2;
    public static final int AXIS_RIGHT_Y = 3;
    public static final int AXIS_LT = 4; // Left trigger
    public static final int AXIS_RT = 5; // Right trigger
    public static final int AXIS_COUNT = 8;
}
