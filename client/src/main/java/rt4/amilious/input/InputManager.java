package rt4.amilious.input;

import rt4.amilious.input.action.Action;
import rt4.amilious.input.action.ActionMapper;
import rt4.amilious.input.device.GamepadDevice;
import rt4.amilious.input.device.InputDevice;
import rt4.amilious.input.device.KeyboardDevice;
import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for the input system.
 *
 * Background mode (current):
 *   init() / tick() / onLogin() may be called from AmiliousClient.
 *   Devices do not read hardware yet — tick uses empty poll + optional
 *   beginFrame(false, false) so nothing changes gameplay.
 *
 * Later:
 *   KeyboardDevice.poll fills the frame from rt4.Keyboard;
 *   Keyboard.java gates chat with shouldSendKeyToChat() etc.
 */
public final class InputManager {

    private static InputMode mode = InputMode.WORLD;

    private static final List<InputDevice> devices = new ArrayList<InputDevice>();
    private static final KeyboardDevice keyboardDevice = new KeyboardDevice();
    private static final GamepadDevice gamepadDevice = new GamepadDevice();

    private static final InputFrame currentFrame =
            new InputFrame(InputButtons.BUTTON_COUNT, InputButtons.AXIS_COUNT);
    private static final InputFrame previousFrame =
            new InputFrame(InputButtons.BUTTON_COUNT, InputButtons.AXIS_COUNT);

    private static final ActionMapper mapper = new ActionMapper();

    /** True this frame: Enter opened CHAT — do not treat as submit/QC. */
    private static boolean consumeEnterThisFrame;

    /** True this frame: user submitted chat. */
    private static boolean submittedThisFrame;

    /** When false, tick does not call device.poll (fully inert). */
    private static boolean pollDevices = false;

    /** When false, mode machine ignores real enter/escape even if passed in. */
    private static boolean processModeKeys = false;

    private static boolean prevEnter;
    private static boolean prevEscape;

    private static boolean initialized;

    private InputManager() {
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        devices.clear();
        devices.add(keyboardDevice);
        devices.add(gamepadDevice);
        mapper.installDefaultKeyboardBindings();
        reset();
    }

    /**
     * Background tick — safe to call every frame from AmiliousClient.update().
     * Does not read Keyboard/Mouse until pollDevices is enabled.
     */
    public static void tick() {
        if (!initialized) {
            init();
        }

        consumeEnterThisFrame = false;
        submittedThisFrame = false;

        previousFrame.copyDownFrom(currentFrame);
        currentFrame.clear();

        if (pollDevices) {
            for (int i = 0; i < devices.size(); i++) {
                InputDevice d = devices.get(i);
                if (d.isConnected()) {
                    d.poll(currentFrame);
                }
            }
        }

        currentFrame.computeEdges(previousFrame);

        // Mode machine: by default no real keys (processModeKeys=false)
        boolean enterDown = false;
        boolean escapeDown = false;
        if (processModeKeys) {
            enterDown = currentFrame.buttonDown[InputButtons.ENTER];
            escapeDown = currentFrame.buttonDown[InputButtons.ESCAPE];
        }
        updateMode(enterDown, escapeDown);

        mapper.update(currentFrame, mode);

    }

    /**
     * Optional alternate entry if you want to feed key state without devices yet.
     * Still safe: pass false,false for background.
     */
    public static void beginFrame(boolean enterDown, boolean escapeDown) {
        if (!initialized) {
            init();
        }
        consumeEnterThisFrame = false;
        submittedThisFrame = false;
        updateMode(enterDown, escapeDown);
        // No mapper update here unless you also poll — prefer tick()
    }

    public static void endFrame() {
        // reserved
    }

    public static void onLogin() {
        reset();
        ChatboxState.resetToLoginDefaults();
    }

    public static void reset() {
        mode = InputMode.WORLD;
        prevEnter = false;
        prevEscape = false;
        consumeEnterThisFrame = false;
        submittedThisFrame = false;
        currentFrame.clear();
        previousFrame.clear();
        ChatboxState.resetToLoginDefaults();
    }

    // -------------------------------------------------------------------------
    // Feature flags (turn on when ready to read / react)
    // -------------------------------------------------------------------------

    /** Enable KeyboardDevice/GamepadDevice.poll (still empty until devices are filled). */
    public static void setPollDevices(boolean value) {
        pollDevices = value;
    }

    /** Enable WORLD/CHAT transitions from Enter/Esc in the frame. */
    public static void setProcessModeKeys(boolean value) {
        processModeKeys = value;
    }

    public static boolean isPollDevices() {
        return pollDevices;
    }

    public static boolean isProcessModeKeys() {
        return processModeKeys;
    }

    // -------------------------------------------------------------------------
    // Mode machine
    // -------------------------------------------------------------------------

    private static void updateMode(boolean enterDown, boolean escapeDown) {
        InputConfig cfg = InputConfig.INSTANCE;
        if (!cfg.enabled) {
            mode = InputMode.CHAT;
            prevEnter = enterDown;
            prevEscape = escapeDown;
            return;
        }

        if (cfg.forceWorldWhenChatHidden && ChatboxState.isCollapsed()) {
            mode = InputMode.WORLD;
            prevEnter = enterDown;
            prevEscape = escapeDown;
            return;
        }

        boolean enterPressed = enterDown && !prevEnter;
        boolean escapePressed = escapeDown && !prevEscape;

        if (mode == InputMode.WORLD) {
            if (enterPressed && cfg.enterOpensChat && ChatboxState.isVisible()) {
                mode = InputMode.CHAT;
                consumeEnterThisFrame = true;
            }
        } else {
            if (escapePressed && cfg.escapeClosesChat) {
                mode = InputMode.WORLD;
            }
        }

        prevEnter = enterDown;
        prevEscape = escapeDown;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public static InputMode getMode() {
        return mode;
    }

    public static boolean isWorldMode() {
        if (!InputConfig.INSTANCE.enabled) {
            return false;
        }
        return mode == InputMode.WORLD;
    }

    public static boolean isChatMode() {
        if (!InputConfig.INSTANCE.enabled) {
            return true;
        }
        return mode == InputMode.CHAT;
    }

    public static boolean shouldSendKeyToChat() {
        return isChatMode() && ChatboxState.isVisible();
    }

    public static boolean shouldAllowWorldBinds() {
        if (!InputConfig.INSTANCE.enabled) {
            return false;
        }
        return mode == InputMode.WORLD;
    }

    public static boolean shouldConsumeEnter() {
        if (!InputConfig.INSTANCE.enabled) {
            return false;
        }
        if (consumeEnterThisFrame) {
            return true;
        }
        return mode == InputMode.WORLD;
    }

    public static boolean shouldBlockQuickChat() {
        if (!InputConfig.INSTANCE.enabled) {
            return false;
        }
        if (!InputConfig.INSTANCE.allowQuickChatOnEmptyEnter) {
            return true;
        }
        return mode != InputMode.CHAT;
    }

    public static boolean didSubmitThisFrame() {
        return submittedThisFrame;
    }

    public static boolean isActionDown(Action action) {
        return mapper.isDown(action);
    }

    public static boolean isActionPressed(Action action) {
        return mapper.isPressed(action);
    }

    public static boolean isActionReleased(Action action) {
        return mapper.isReleased(action);
    }

    public static ActionMapper getMapper() {
        return mapper;
    }

    public static InputFrame getCurrentFrame() {
        return currentFrame;
    }

    public static KeyboardDevice getKeyboardDevice() {
        return keyboardDevice;
    }

    public static GamepadDevice getGamepadDevice() {
        return gamepadDevice;
    }

    // -------------------------------------------------------------------------
    // Events (call from future chat hooks)
    // -------------------------------------------------------------------------

    public static void notifyChatSubmit() {
        submittedThisFrame = true;
        if (InputConfig.INSTANCE.autoWorldAfterSend) {
            mode = InputMode.WORLD;
        }
    }

    public static boolean notifyEmptyEnter() {
        if (!InputConfig.INSTANCE.allowQuickChatOnEmptyEnter) {
            mode = InputMode.WORLD;
            return false;
        }
        return mode == InputMode.CHAT;
    }

    public static void enterChatMode() {
        if (InputConfig.INSTANCE.forceWorldWhenChatHidden && ChatboxState.isCollapsed()) {
            return;
        }
        mode = InputMode.CHAT;
    }

    public static void enterWorldMode() {
        mode = InputMode.WORLD;
    }

    public static void setMode(InputMode newMode) {
        if (newMode == null) {
            return;
        }
        if (newMode == InputMode.CHAT
                && InputConfig.INSTANCE.forceWorldWhenChatHidden
                && ChatboxState.isCollapsed()) {
            mode = InputMode.WORLD;
            return;
        }
        mode = newMode;
    }
}