package rt4.amilious.input.device;

import rt4.Keyboard;
import rt4.amilious.input.device.InputDevice;
import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

/**
 * Samples rt4.Keyboard.pressedKeys into the shared InputFrame.
 * Does not consume or block keys — Keyboard.java still owns behavior.
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
    public void poll(InputFrame out) {
        boolean[] keys = Keyboard.pressedKeys;
        if (keys == null) {
            return;
        }

        // Helpers — pressedKeys is length 112; ignore out-of-range
        set(out, InputButtons.ENTER, safe(keys, Keyboard.KEY_ENTER));
        set(out, InputButtons.ESCAPE, safe(keys, 13)); // CODE_MAP[VK_ESCAPE] = 13
        set(out, InputButtons.BACKSPACE, safe(keys, Keyboard.KEY_BACK_SPACE));
        set(out, InputButtons.TAB, safe(keys, 80));
        set(out, InputButtons.SPACE, safe(keys, 83));

        set(out, InputButtons.W, safe(keys, 33));
        set(out, InputButtons.A, safe(keys, 48));
        set(out, InputButtons.S, safe(keys, 49));
        set(out, InputButtons.D, safe(keys, 50));

        set(out, InputButtons.LEFT, safe(keys, Keyboard.KEY_LEFT));
        set(out, InputButtons.RIGHT, safe(keys, Keyboard.KEY_RIGHT));
        set(out, InputButtons.UP, safe(keys, Keyboard.KEY_UP));
        set(out, InputButtons.DOWN, safe(keys, Keyboard.KEY_DOWN));

        set(out, InputButtons.F1, safe(keys, 1));
        set(out, InputButtons.F2, safe(keys, 2));
        set(out, InputButtons.F3, safe(keys, 3));
        set(out, InputButtons.F4, safe(keys, 4));
        set(out, InputButtons.F5, safe(keys, 5));
        set(out, InputButtons.F6, safe(keys, 6));
        set(out, InputButtons.F7, safe(keys, 7));
        set(out, InputButtons.F8, safe(keys, 8));
        set(out, InputButtons.F9, safe(keys, 9));
        set(out, InputButtons.F10, safe(keys, 10));
        set(out, InputButtons.F11, safe(keys, 11));
        set(out, InputButtons.F12, safe(keys, 12));

        set(out, InputButtons.DIGIT_1, safe(keys, 16));
        set(out, InputButtons.DIGIT_2, safe(keys, 17));
        set(out, InputButtons.DIGIT_3, safe(keys, 18));
        set(out, InputButtons.DIGIT_4, safe(keys, 19));
        set(out, InputButtons.DIGIT_5, safe(keys, 20));
        set(out, InputButtons.DIGIT_6, safe(keys, 21));
        set(out, InputButtons.DIGIT_7, safe(keys, 22));
        set(out, InputButtons.DIGIT_8, safe(keys, 23));
        set(out, InputButtons.DIGIT_9, safe(keys, 24));
        set(out, InputButtons.DIGIT_0, safe(keys, 25));
    }

    private static boolean safe(boolean[] keys, int index) {
        return index >= 0 && index < keys.length && keys[index];
    }

    private static void set(InputFrame out, int button, boolean down) {
        if (button >= 0 && button < out.buttonDown.length && down) {
            out.buttonDown[button] = true;
        }
    }
}