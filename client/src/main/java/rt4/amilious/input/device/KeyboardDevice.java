package rt4.amilious.input.device;

import rt4.Keyboard;
import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

/**
 * Samples rt4.Keyboard.pressedKeys into the shared InputFrame.
 * Maps all keys from Keyboard.CODE_MAP to InputButtons constants.
 * Does not consume or block keys — Keyboard.java still owns AWT event handling.
 */
public final class KeyboardDevice implements InputDevice {

    @Override
    public String name() {
        return "keyboard";
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public int getIdleLoops() {
        return Keyboard.getIdleLoops();
    }

    @Override
    public void poll(InputFrame out) {
        boolean[] keys = Keyboard.pressedKeys;
        if (keys == null) {
            return;
        }

        // ===== Core keys (legacy names, no prefix) =====
        set(out, InputButtons.ENTER, safe(keys, Keyboard.KEY_ENTER));       // Code 84
        set(out, InputButtons.ESCAPE, safe(keys, 13));                      // Code 13
        set(out, InputButtons.BACKSPACE, safe(keys, Keyboard.KEY_BACK_SPACE)); // Code 85
        set(out, InputButtons.TAB, safe(keys, 80));                         // Code 80
        set(out, InputButtons.SPACE, safe(keys, 83));                       // Code 83

        // ===== Modifiers (CRITICAL) =====
        set(out, InputButtons.KB_CTRL, safe(keys, Keyboard.KEY_CTRL));      // Code 82
        set(out, InputButtons.KB_SHIFT, safe(keys, Keyboard.KEY_SHIFT));    // Code 81
        set(out, InputButtons.KB_ALT, safe(keys, Keyboard.KEY_ALT));        // Code 86

        // ===== WASD (legacy names) =====
        set(out, InputButtons.W, safe(keys, 33)); // Code 33 - VK_W
        set(out, InputButtons.A, safe(keys, 48)); // Code 48 - VK_A
        set(out, InputButtons.S, safe(keys, 49)); // Code 49 - VK_S
        set(out, InputButtons.D, safe(keys, 50)); // Code 50 - VK_D

        // ===== Arrow keys (legacy names) =====
        set(out, InputButtons.LEFT, safe(keys, Keyboard.KEY_LEFT));         // Code 96
        set(out, InputButtons.RIGHT, safe(keys, Keyboard.KEY_RIGHT));       // Code 97
        set(out, InputButtons.UP, safe(keys, Keyboard.KEY_UP));             // Code 98
        set(out, InputButtons.DOWN, safe(keys, Keyboard.KEY_DOWN));         // Code 99

        // ===== Function keys =====
        set(out, InputButtons.F1, safe(keys, 1));   // Code 1
        set(out, InputButtons.F2, safe(keys, 2));   // Code 2
        set(out, InputButtons.F3, safe(keys, 3));   // Code 3
        set(out, InputButtons.F4, safe(keys, 4));   // Code 4
        set(out, InputButtons.F5, safe(keys, 5));   // Code 5
        set(out, InputButtons.F6, safe(keys, 6));   // Code 6
        set(out, InputButtons.F7, safe(keys, 7));   // Code 7
        set(out, InputButtons.F8, safe(keys, 8));   // Code 8
        set(out, InputButtons.F9, safe(keys, 9));   // Code 9
        set(out, InputButtons.F10, safe(keys, 10)); // Code 10
        set(out, InputButtons.F11, safe(keys, 11)); // Code 11
        set(out, InputButtons.F12, safe(keys, 12)); // Code 12

        // ===== Number row (top of keyboard) =====
        set(out, InputButtons.DIGIT_1, safe(keys, 16)); // Code 16 - VK_1
        set(out, InputButtons.DIGIT_2, safe(keys, 17)); // Code 17 - VK_2
        set(out, InputButtons.DIGIT_3, safe(keys, 18)); // Code 18 - VK_3
        set(out, InputButtons.DIGIT_4, safe(keys, 19)); // Code 19 - VK_4
        set(out, InputButtons.DIGIT_5, safe(keys, 20)); // Code 20 - VK_5
        set(out, InputButtons.DIGIT_6, safe(keys, 21)); // Code 21 - VK_6
        set(out, InputButtons.DIGIT_7, safe(keys, 22)); // Code 22 - VK_7
        set(out, InputButtons.DIGIT_8, safe(keys, 23)); // Code 23 - VK_8
        set(out, InputButtons.DIGIT_9, safe(keys, 24)); // Code 24 - VK_9
        set(out, InputButtons.DIGIT_0, safe(keys, 25)); // Code 25 - VK_0

        // ===== Letter keys (A-Z, excluding WASD which are above) =====
        set(out, InputButtons.KB_Q, safe(keys, 32)); // Code 32 - VK_Q
        set(out, InputButtons.KB_E, safe(keys, 34)); // Code 34 - VK_E
        set(out, InputButtons.KB_R, safe(keys, 35)); // Code 35 - VK_R
        set(out, InputButtons.KB_T, safe(keys, 36)); // Code 36 - VK_T
        set(out, InputButtons.KB_Y, safe(keys, 37)); // Code 37 - VK_Y
        set(out, InputButtons.KB_U, safe(keys, 38)); // Code 38 - VK_U
        set(out, InputButtons.KB_I, safe(keys, 39)); // Code 39 - VK_I
        set(out, InputButtons.KB_O, safe(keys, 40)); // Code 40 - VK_O
        set(out, InputButtons.KB_P, safe(keys, 41)); // Code 41 - VK_P
        set(out, InputButtons.KB_F, safe(keys, 51)); // Code 51 - VK_F
        set(out, InputButtons.KB_G, safe(keys, 52)); // Code 52 - VK_G
        set(out, InputButtons.KB_H, safe(keys, 53)); // Code 53 - VK_H
        set(out, InputButtons.KB_J, safe(keys, 54)); // Code 54 - VK_J
        set(out, InputButtons.KB_K, safe(keys, 55)); // Code 55 - VK_K
        set(out, InputButtons.KB_L, safe(keys, 56)); // Code 56 - VK_L
        set(out, InputButtons.KB_Z, safe(keys, 64)); // Code 64 - VK_Z
        set(out, InputButtons.KB_X, safe(keys, 65)); // Code 65 - VK_X
        set(out, InputButtons.KB_C, safe(keys, 66)); // Code 66 - VK_C
        set(out, InputButtons.KB_V, safe(keys, 67)); // Code 67 - VK_V
        set(out, InputButtons.KB_B, safe(keys, 68)); // Code 68 - VK_B
        set(out, InputButtons.KB_N, safe(keys, 69)); // Code 69 - VK_N
        set(out, InputButtons.KB_M, safe(keys, 70)); // Code 70 - VK_M

        // ===== Punctuation / Symbol keys =====
        // Note: Some codes vary by JVM/locale (see Keyboard.init())
        set(out, InputButtons.KB_MINUS, safe(keys, 26));         // Code 26 - VK_MINUS
        set(out, InputButtons.KB_EQUALS, safe(keys, 27));        // Code 27 - VK_EQUALS
        set(out, InputButtons.KB_OPEN_BRACKET, safe(keys, 42));  // Code 42 - VK_OPEN_BRACKET
        set(out, InputButtons.KB_CLOSE_BRACKET, safe(keys, 43)); // Code 43 - VK_CLOSE_BRACKET
        set(out, InputButtons.KB_SEMICOLON, safe(keys, 57));     // Code 57 - VK_SEMICOLON
        set(out, InputButtons.KB_QUOTE, safe(keys, 59));         // Code 59 - VK_QUOTE (or 58 depending on JVM)
        set(out, InputButtons.KB_BACK_QUOTE, safe(keys, 58));    // Code 58 - VK_BACK_QUOTE (or 28 depending on JVM)
        set(out, InputButtons.KB_COMMA, safe(keys, 71));         // Code 71 - VK_COMMA
        set(out, InputButtons.KB_PERIOD, safe(keys, 72));        // Code 72 - VK_PERIOD
        set(out, InputButtons.KB_SLASH, safe(keys, 73));         // Code 73 - VK_SLASH
        set(out, InputButtons.KB_BACK_SLASH, safe(keys, 74));    // Code 74 - VK_BACK_SLASH

        // ===== Navigation keys =====
        set(out, InputButtons.KB_INSERT, safe(keys, 100));    // Code 100 - VK_INSERT
        set(out, InputButtons.KB_DELETE, safe(keys, 101));    // Code 101 - VK_DELETE
        set(out, InputButtons.KB_HOME, safe(keys, 102));      // Code 102 - VK_HOME
        set(out, InputButtons.KB_END, safe(keys, 103));       // Code 103 - VK_END
        set(out, InputButtons.KB_PAGE_UP, safe(keys, 104));   // Code 104 - VK_PAGE_UP
        set(out, InputButtons.KB_PAGE_DOWN, safe(keys, 105)); // Code 105 - VK_PAGE_DOWN

        // ===== Numpad keys =====
        set(out, InputButtons.KB_NUMPAD_0, safe(keys, 228));        // Code 228 - VK_NUMPAD0
        set(out, InputButtons.KB_NUMPAD_1, safe(keys, 231));        // Code 231 - VK_NUMPAD1
        set(out, InputButtons.KB_NUMPAD_2, safe(keys, 227));        // Code 227 - VK_NUMPAD2
        set(out, InputButtons.KB_NUMPAD_3, safe(keys, 233));        // Code 233 - VK_NUMPAD3
        set(out, InputButtons.KB_NUMPAD_4, safe(keys, 224));        // Code 224 - VK_NUMPAD4
        set(out, InputButtons.KB_NUMPAD_5, safe(keys, 219));        // Code 219 - VK_NUMPAD5
        set(out, InputButtons.KB_NUMPAD_6, safe(keys, 225));        // Code 225 - VK_NUMPAD6
        set(out, InputButtons.KB_NUMPAD_7, safe(keys, 230));        // Code 230 - VK_NUMPAD7
        set(out, InputButtons.KB_NUMPAD_8, safe(keys, 226));        // Code 226 - VK_NUMPAD8
        set(out, InputButtons.KB_NUMPAD_9, safe(keys, 232));        // Code 232 - VK_NUMPAD9
        set(out, InputButtons.KB_NUMPAD_MULTIPLY, safe(keys, 89));  // Code 89 - VK_MULTIPLY
        set(out, InputButtons.KB_NUMPAD_ADD, safe(keys, 87));       // Code 87 - VK_ADD
        set(out, InputButtons.KB_NUMPAD_SUBTRACT, safe(keys, 88));  // Code 88 - VK_SUBTRACT
        set(out, InputButtons.KB_NUMPAD_DECIMAL, safe(keys, 229));  // Code 229 - VK_DECIMAL
        set(out, InputButtons.KB_NUMPAD_DIVIDE, safe(keys, 90));    // Code 90 - VK_DIVIDE

        // ===== Other special keys =====
        set(out, InputButtons.KB_CLEAR, safe(keys, 91)); // Code 91 - VK_CLEAR
    }

    /**
     * Safely checks if a key is pressed, guarding against array bounds.
     */
    private static boolean safe(boolean[] keys, int index) {
        return index >= 0 && index < keys.length && keys[index];
    }

    /**
     * Sets a button in the output frame if it's down.
     */
    private static void set(InputFrame out, int button, boolean down) {
        if (button >= 0 && button < out.buttonDown.length && down) {
            out.buttonDown[button] = true;
        }
    }
}