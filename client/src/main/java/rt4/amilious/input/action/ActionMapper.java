package rt4.amilious.input.action;

import rt4.amilious.input.InputMode;
import rt4.amilious.input.state.InputFrame;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves InputFrame buttons → Action down/pressed/released.
 * Multiple bindings can OR into the same action.
 */
public final class ActionMapper {

    private final List<Binding> bindings = new ArrayList<Binding>();
    private final Map<Action, Boolean> down = new EnumMap<Action, Boolean>(Action.class);
    private final Map<Action, Boolean> pressed = new EnumMap<Action, Boolean>(Action.class);
    private final Map<Action, Boolean> released = new EnumMap<Action, Boolean>(Action.class);

    public ActionMapper() {
        for (Action a : Action.values()) {
            down.put(a, Boolean.FALSE);
            pressed.put(a, Boolean.FALSE);
            released.put(a, Boolean.FALSE);
        }
    }

    public void bind(Action action, int buttonId, InputMode modeFilter) {
        bindings.add(new Binding(action, buttonId, modeFilter));
    }

    public void bind(Action action, int buttonId) {
        bindings.add(new Binding(action, buttonId, null));
    }

    public void clearBindings() {
        bindings.clear();
    }

    public void installDefaultKeyboardBindings() {
        clearBindings();

        // Chat / system — mode filtered where it matters
        bind(Action.OPEN_CHAT, rt4.amilious.input.state.InputButtons.ENTER, InputMode.WORLD);
        bind(Action.SUBMIT_CHAT, rt4.amilious.input.state.InputButtons.ENTER, InputMode.CHAT);
        bind(Action.CLOSE_CHAT, rt4.amilious.input.state.InputButtons.ESCAPE, InputMode.CHAT);
        bind(Action.ESCAPE, rt4.amilious.input.state.InputButtons.ESCAPE);

        // Optional key-camera (WORLD only)
        bind(Action.CAMERA_UP, rt4.amilious.input.state.InputButtons.UP, InputMode.WORLD);
        bind(Action.CAMERA_DOWN, rt4.amilious.input.state.InputButtons.DOWN, InputMode.WORLD);
        bind(Action.CAMERA_LEFT, rt4.amilious.input.state.InputButtons.LEFT, InputMode.WORLD);
        bind(Action.CAMERA_RIGHT, rt4.amilious.input.state.InputButtons.RIGHT, InputMode.WORLD);
        bind(Action.CAMERA_UP, rt4.amilious.input.state.InputButtons.W, InputMode.WORLD);
        bind(Action.CAMERA_DOWN, rt4.amilious.input.state.InputButtons.S, InputMode.WORLD);
        bind(Action.CAMERA_LEFT, rt4.amilious.input.state.InputButtons.A, InputMode.WORLD);
        bind(Action.CAMERA_RIGHT, rt4.amilious.input.state.InputButtons.D, InputMode.WORLD);

        // Modifier keys (no mode filter - work everywhere)
        bind(Action.MODIFIER_CTRL, rt4.amilious.input.state.InputButtons.KB_CTRL);
        bind(Action.MODIFIER_ALT, rt4.amilious.input.state.InputButtons.KB_ALT);
        bind(Action.MODIFIER_SHIFT, rt4.amilious.input.state.InputButtons.KB_SHIFT);

        // Menu alternative action (WORLD only - shift-click)
        bind(Action.MENU_ALTERNATIVE_ACTION, rt4.amilious.input.state.InputButtons.KB_SHIFT, InputMode.WORLD);

        // Digit hotkeys (WORLD)
        bind(Action.HOTKEY_1, rt4.amilious.input.state.InputButtons.DIGIT_1, InputMode.WORLD);
        bind(Action.HOTKEY_2, rt4.amilious.input.state.InputButtons.DIGIT_2, InputMode.WORLD);
        bind(Action.HOTKEY_3, rt4.amilious.input.state.InputButtons.DIGIT_3, InputMode.WORLD);
        bind(Action.HOTKEY_4, rt4.amilious.input.state.InputButtons.DIGIT_4, InputMode.WORLD);
        bind(Action.HOTKEY_5, rt4.amilious.input.state.InputButtons.DIGIT_5, InputMode.WORLD);
        bind(Action.HOTKEY_6, rt4.amilious.input.state.InputButtons.DIGIT_6, InputMode.WORLD);
        bind(Action.HOTKEY_7, rt4.amilious.input.state.InputButtons.DIGIT_7, InputMode.WORLD);
        bind(Action.HOTKEY_8, rt4.amilious.input.state.InputButtons.DIGIT_8, InputMode.WORLD);
        bind(Action.HOTKEY_9, rt4.amilious.input.state.InputButtons.DIGIT_9, InputMode.WORLD);
        bind(Action.HOTKEY_0, rt4.amilious.input.state.InputButtons.DIGIT_0, InputMode.WORLD);
    }

    public void update(InputFrame frame, InputMode mode) {
        // Reset edges; rebuild down from active bindings
        for (Action a : Action.values()) {
            pressed.put(a, Boolean.FALSE);
            released.put(a, Boolean.FALSE);
            down.put(a, Boolean.FALSE);
        }

        boolean[] actionDown = new boolean[Action.values().length];
        boolean[] actionPressed = new boolean[Action.values().length];
        boolean[] actionReleased = new boolean[Action.values().length];

        for (Binding b : bindings) {
            if (b.modeFilter != null && b.modeFilter != mode) {
                continue;
            }
            if (b.buttonId < 0 || b.buttonId >= frame.buttonDown.length) {
                continue;
            }
            int idx = b.action.ordinal();
            if (frame.buttonDown[b.buttonId]) {
                actionDown[idx] = true;
            }
            if (frame.buttonPressed[b.buttonId]) {
                actionPressed[idx] = true;
            }
            if (frame.buttonReleased[b.buttonId]) {
                actionReleased[idx] = true;
            }
        }

        for (Action a : Action.values()) {
            int idx = a.ordinal();
            down.put(a, Boolean.valueOf(actionDown[idx]));
            pressed.put(a, Boolean.valueOf(actionPressed[idx]));
            released.put(a, Boolean.valueOf(actionReleased[idx]));
        }
    }

    public boolean isDown(Action a) {
        return Boolean.TRUE.equals(down.get(a));
    }

    public boolean isPressed(Action a) {
        return Boolean.TRUE.equals(pressed.get(a));
    }

    public boolean isReleased(Action a) {
        return Boolean.TRUE.equals(released.get(a));
    }
}