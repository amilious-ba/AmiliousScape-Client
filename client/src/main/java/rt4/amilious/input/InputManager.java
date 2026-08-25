package rt4.amilious.input;

import rt4.amilious.MapController;
import rt4.amilious.input.action.Action;
import rt4.amilious.input.action.ActionMapper;
import rt4.amilious.input.device.BotInputDevice;
import rt4.amilious.input.device.GamepadDevice;
import rt4.amilious.input.device.InputDevice;
import rt4.amilious.input.device.KeyboardDevice;
import rt4.amilious.input.device.MouseDevice;
import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;
import rt4.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for the input system.
 *
 * Lifecycle (from AmiliousClient):
 *   init() / tick() / onLogin()
 *
 * Mode priority (highest first):
 *   MAIN_MENU → MAP → SPECIAL_MODAL → CHATBOX_MODAL → CHAT → WORLD
 *
 * Protocol key drain uses shouldAcceptTextInput().
 * Enter consume (QC) uses shouldConsumeEnter() — WORLD/MAP only.
 */
public final class InputManager {

    public interface ModeListener {
        void onModeChanged(InputMode from, InputMode to);
    }

    private static InputMode mode = InputMode.WORLD;
    private static InputMode previousMode = InputMode.WORLD;

    private static boolean chatArmed = false;
    private static boolean disarmChatIfNoSubmit;
    private static boolean submittedSinceArmEnter;

    private static final List<InputDevice> devices = new ArrayList<InputDevice>();
    private static final KeyboardDevice keyboardDevice = new KeyboardDevice();
    private static final MouseDevice mouseDevice = new MouseDevice();
    private static final GamepadDevice gamepadDevice = new GamepadDevice();
    private static final BotInputDevice botDevice = new BotInputDevice();

    private static final InputFrame currentFrame =
            new InputFrame(InputButtons.BUTTON_COUNT, InputButtons.AXIS_COUNT);
    private static final InputFrame previousFrame =
            new InputFrame(InputButtons.BUTTON_COUNT, InputButtons.AXIS_COUNT);

    private static final ActionMapper mapper = new ActionMapper();
    private static final List<ModeListener> modeListeners = new ArrayList<ModeListener>();

    private static boolean consumeEnterThisFrame;
    private static boolean submittedThisFrame;

    private static boolean pollDevices = false;
    private static boolean processModeKeys = false;
    private static boolean logModeChanges = true;

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
        devices.add(mouseDevice);
        devices.add(gamepadDevice);
        devices.add(botDevice);  // Bot device polls AFTER real devices (can override)
        mapper.installDefaultKeyboardBindings();
        reset();
    }

    public static void tick() {
        if (!initialized) {
            init();
        }

        if (disarmChatIfNoSubmit) {
            disarmChatIfNoSubmit = false;
            if (!submittedSinceArmEnter) {
                chatArmed = false;
            }
            submittedSinceArmEnter = false;
        }

        consumeEnterThisFrame = false;
        submittedThisFrame = false;

        // IMPORTANT: We swap previous/current frames BEFORE clearing.
        // This keeps currentFrame valid throughout the entire game loop,
        // including event handlers that fire AFTER tick() completes.
        // We only clear current when we're about to poll fresh data.
        previousFrame.copyDownFrom(currentFrame);

        if (pollDevices) {
            // Clear current frame JUST before polling new data
            currentFrame.clear();

            for (int i = 0; i < devices.size(); i++) {
                InputDevice d = devices.get(i);
                if (d.isConnected()) {
                    d.poll(currentFrame);
                    // Poll idle counters for AF K detection
                    if (d == keyboardDevice) {
                        currentFrame.keyboardIdleLoops = d.getIdleLoops();
                    } else if (d == mouseDevice) {
                        currentFrame.mouseIdleLoops = d.getIdleLoops();
                    }
                }
            }
        }

        // Convert left stick axes to virtual buttons BEFORE computing edges
        convertLeftStickToButtons(currentFrame);

        currentFrame.computeEdges(previousFrame);

        // Apply gamepad mouse control (right stick → cursor movement, triggers → clicks)
        GamepadMouseController.tick(currentFrame, mode);

        if (processModeKeys) {
            boolean enterDown = currentFrame.buttonDown[InputButtons.ENTER];
            boolean escapeDown = currentFrame.buttonDown[InputButtons.ESCAPE];
            processChatArming(enterDown, escapeDown);
        } else {
            prevEnter = false;
            prevEscape = false;
        }

        applyMode(deriveMode());
        mapper.update(currentFrame, mode);
    }

    public static void beginFrame(boolean enterDown, boolean escapeDown) {
        if (!initialized) {
            init();
        }
        consumeEnterThisFrame = false;
        submittedThisFrame = false;
        if (processModeKeys) {
            processChatArming(enterDown, escapeDown);
        }
        applyMode(deriveMode());
    }

    public static void endFrame() {
    }

    public static void onLogin() {
        reset();
        ChatboxState.resetToLoginDefaults();
    }

    public static void reset() {
        InputMode old = mode;
        mode = InputMode.WORLD;
        previousMode = InputMode.WORLD;
        chatArmed = false;
        prevEnter = false;
        prevEscape = false;
        consumeEnterThisFrame = false;
        submittedThisFrame = false;
        disarmChatIfNoSubmit = false;
        submittedSinceArmEnter = false;
        currentFrame.clear();
        previousFrame.clear();
        ChatboxState.resetToLoginDefaults();
        ChatBoxModalRegistry.reset();
        SpecialModalRegistry.reset();
        if (old != mode && initialized) {
            fireModeChanged(old, mode);
        }
    }

    /** Call after registry open/close so mode updates this frame, not only next tick. */
    public static void refreshMode() {
        if (!initialized) {
            init();
        }
        applyMode(deriveMode());
    }

    /**
     * Convert left stick axes to virtual buttons for camera control.
     * This allows the Action system to treat analog stick input like button presses.
     * Uses a threshold of 0.5 to trigger the virtual buttons.
     */
    private static void convertLeftStickToButtons(InputFrame frame) {
        float leftX = frame.axes[InputButtons.AXIS_LEFT_X];
        float leftY = frame.axes[InputButtons.AXIS_LEFT_Y];
        float threshold = 0.5f;

        // Y axis: negative = up, positive = down
        frame.buttonDown[InputButtons.GP_LSTICK_UP] = leftY < -threshold;
        frame.buttonDown[InputButtons.GP_LSTICK_DOWN] = leftY > threshold;

        // X axis: negative = left, positive = right
        frame.buttonDown[InputButtons.GP_LSTICK_LEFT] = leftX < -threshold;
        frame.buttonDown[InputButtons.GP_LSTICK_RIGHT] = leftX > threshold;
    }

    // -------------------------------------------------------------------------
    // Feature flags
    // -------------------------------------------------------------------------

    public static void setPollDevices(boolean value) {
        pollDevices = value;
    }

    public static void setProcessModeKeys(boolean value) {
        processModeKeys = value;
    }

    public static void setLogModeChanges(boolean value) {
        logModeChanges = value;
    }

    public static boolean isPollDevices() {
        return pollDevices;
    }

    public static boolean isProcessModeKeys() {
        return processModeKeys;
    }

    public static boolean isLogModeChanges() {
        return logModeChanges;
    }

    // -------------------------------------------------------------------------
    // Mode listeners
    // -------------------------------------------------------------------------

    public static void addModeListener(ModeListener listener) {
        if (listener != null && !modeListeners.contains(listener)) {
            modeListeners.add(listener);
        }
    }

    public static void removeModeListener(ModeListener listener) {
        modeListeners.remove(listener);
    }

    private static void applyMode(InputMode next) {
        if (next == null) {
            next = InputMode.WORLD;
        }
        if (next == mode) {
            return;
        }
        previousMode = mode;
        mode = next;
        fireModeChanged(previousMode, mode);
    }

    private static void fireModeChanged(InputMode from, InputMode to) {
        if (logModeChanges) {
            logModeTransition(from, to);
        }
        for (int i = 0; i < modeListeners.size(); i++) {
            try {
                modeListeners.get(i).onModeChanged(from, to);
            } catch (Exception e) {
                System.err.println("[input] ModeListener error: " + e.getMessage());
            }
        }
    }

    private static void logModeTransition(InputMode from, InputMode to) {
        StringBuilder sb = new StringBuilder();
        sb.append("[input] mode ").append(from).append(" -> ").append(to);
        if (to == InputMode.SPECIAL_MODAL) {
            String name = SpecialModalRegistry.getActiveName();
            sb.append(name != null ? " (" + name + ")" : " (unnamed)");
        } else if (to == InputMode.CHATBOX_MODAL) {
            String name = ChatBoxModalRegistry.getActiveName();
            sb.append(name != null ? " (" + name + ")" : " (chatbox)");
        } else if (from == InputMode.SPECIAL_MODAL) {
            sb.append(" (left special modal)");
        } else if (from == InputMode.CHATBOX_MODAL) {
            sb.append(" (left chatbox modal)");
        }
        if (to == InputMode.CHAT || from == InputMode.CHAT) {
            sb.append(" chatArmed=").append(chatArmed);
        }
        if (to == InputMode.MAP || from == InputMode.MAP) {
            sb.append(" mapOpen=").append(MapController.isOpen());
        }
        System.out.println(sb.toString());
    }

    // -------------------------------------------------------------------------
    // Mode machine
    // -------------------------------------------------------------------------

    private static void processChatArming(boolean enterDown, boolean escapeDown) {
        InputConfig cfg = InputConfig.INSTANCE;
        if (!cfg.enabled) {
            prevEnter = enterDown;
            prevEscape = escapeDown;
            return;
        }

        boolean enterPressed = enterDown && !prevEnter;
        boolean escapePressed = escapeDown && !prevEscape;

        if (client.gameState != 30
                || MapController.isOpen()
                || SpecialModalRegistry.isActive()
                || ChatBoxModalRegistry.isActive()) {
            chatArmed = false;
            disarmChatIfNoSubmit = false;
            submittedSinceArmEnter = false;
            prevEnter = enterDown;
            prevEscape = escapeDown;
            return;
        }

        if (escapePressed && cfg.escapeClosesChat && chatArmed) {
            chatArmed = false;
            disarmChatIfNoSubmit = false;
            submittedSinceArmEnter = false;
        } else if (enterPressed && cfg.enterOpensChat) {
            if (!chatArmed) {
                if (ChatboxState.isVisible() || !cfg.forceWorldWhenChatHidden) {
                    chatArmed = true;
                    consumeEnterThisFrame = true;
                }
            } else {
                disarmChatIfNoSubmit = true;
                submittedSinceArmEnter = false;
            }
        }

        prevEnter = enterDown;
        prevEscape = escapeDown;
    }

    /**
     * Priority: MAIN_MENU → MAP → SPECIAL_MODAL → CHATBOX_MODAL → CHAT → WORLD
     */
    public static InputMode deriveMode() {
        InputConfig cfg = InputConfig.INSTANCE;
        if (!cfg.enabled) {
            return InputMode.CHAT;
        }

        if (client.gameState != 30) {
            return InputMode.MAIN_MENU;
        }

        if (MapController.isOpen()) {
            chatArmed = false;
            return InputMode.MAP;
        }

        if (SpecialModalRegistry.isActive()) {
            chatArmed = false;
            return InputMode.SPECIAL_MODAL;
        }

        if (ChatBoxModalRegistry.isActive()) {
            chatArmed = false;
            return InputMode.CHATBOX_MODAL;
        }

        if (chatArmed && ChatboxState.isVisible()) {
            return InputMode.CHAT;
        }

        if (chatArmed && cfg.forceWorldWhenChatHidden && ChatboxState.isCollapsed()) {
            chatArmed = false;
        }

        return InputMode.WORLD;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public static InputMode getMode() {
        return mode;
    }

    public static InputMode getPreviousMode() {
        return previousMode;
    }

    public static boolean didModeChangeThisFrame() {
        return mode != previousMode;
    }

    public static boolean isWorldMode() {
        return getMode() == InputMode.WORLD;
    }

    public static boolean isChatMode() {
        return getMode() == InputMode.CHAT;
    }

    public static boolean isMapMode() {
        return getMode() == InputMode.MAP;
    }

    public static boolean isMainMenuMode() {
        return getMode() == InputMode.MAIN_MENU;
    }

    public static boolean isSpecialModalMode() {
        return getMode() == InputMode.SPECIAL_MODAL;
    }

    public static boolean isChatBoxModalMode() {
        return getMode() == InputMode.CHATBOX_MODAL;
    }

    public static boolean isChatArmed() {
        return chatArmed;
    }

    public static boolean shouldSendKeyToChat() {
        return getMode() == InputMode.CHAT && ChatboxState.isVisible();
    }

    public static boolean shouldAllowWorldBinds() {
        return getMode() == InputMode.WORLD;
    }

    /** Protocol: accept typed keys for chat + amount + report abuse + login. */
    public static boolean shouldAcceptTextInput() {
        InputMode m = getMode();
        return m == InputMode.CHAT
                || m == InputMode.CHATBOX_MODAL
                || m == InputMode.SPECIAL_MODAL
                || m == InputMode.MAIN_MENU
                || m == InputMode.MAP; // Allow typing in map search field
    }

    /** Skip Enter for QC only in WORLD. MAP search / special modals need Enter. */
    public static boolean shouldConsumeEnter() {
        if (!InputConfig.INSTANCE.enabled) {
            return false;
        }
        if (consumeEnterThisFrame) {
            return true; // same-frame arm chat in WORLD only
        }
        // Do NOT include MAP — world map search submits on Enter
        return getMode() == InputMode.WORLD;
    }

    public static boolean shouldBlockQuickChat() {
        if (!InputConfig.INSTANCE.enabled) {
            return false;
        }
        if (!InputConfig.INSTANCE.allowQuickChatOnEmptyEnter) {
            return true;
        }
        return getMode() != InputMode.CHAT;
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

    // -------------------------------------------------------------------------
    // Consumption API
    // -------------------------------------------------------------------------

    /**
     * Mark a button as consumed (handled by UI/interface).
     * Consumed buttons won't trigger world actions.
     */
    public static void consumeButton(int buttonId) {
        if (currentFrame != null && buttonId >= 0 && buttonId < currentFrame.buttonConsumed.length) {
            currentFrame.buttonConsumed[buttonId] = true;
        }
    }

    /**
     * Mark mouse click as consumed (interface click handled, don't walk).
     */
    public static void consumeMouseClick() {
        consumeButton(InputButtons.MOUSE_CLICK);
        consumeButton(InputButtons.MOUSE_BUTTON_1);
        consumeButton(InputButtons.MOUSE_BUTTON_2);
        consumeButton(InputButtons.MOUSE_BUTTON_3);
    }

    /**
     * Check if a button has been consumed this frame.
     */
    public static boolean isButtonConsumed(int buttonId) {
        if (currentFrame == null || buttonId < 0 || buttonId >= currentFrame.buttonConsumed.length) {
            return false;
        }
        return currentFrame.buttonConsumed[buttonId];
    }

    /**
     * Check if mouse click has been consumed (don't send world clicks).
     */
    public static boolean isMouseClickConsumed() {
        return isButtonConsumed(InputButtons.MOUSE_CLICK);
    }

    public static ActionMapper getMapper() {
        return mapper;
    }

    public static InputFrame getCurrentFrame() {
        return currentFrame;
    }

    // ===== Convenience Methods for Common Patterns =====

    /**
     * Check if Ctrl+Shift are both held (for staff teleport cheats).
     * This is a composite check - both MODIFIER_CTRL and MODIFIER_SHIFT must be down.
     *
     * Only functional when LoginManager.staffModLevel > 0.
     *
     * Usage locations:
     * - Protocol.java:2846 (mouse wheel plane change)
     * - Protocol.java:2856 (click teleport)
     * - MiniMenu.java:622 (walk-here teleport)
     * - MiniMenu.java:1100 (examine teleport)
     *
     * Replaces: Keyboard.pressedKeys[KEY_CTRL] && Keyboard.pressedKeys[KEY_SHIFT]
     */
    public static boolean isCheatTeleportModifierDown() {
        return isActionDown(Action.MODIFIER_CTRL) && isActionDown(Action.MODIFIER_SHIFT);
    }

    /**
     * Check if Shift is held (for alternative menu actions).
     *
     * Replaces: Keyboard.pressedKeys[KEY_SHIFT]
     */
    public static boolean isMenuAlternativeActionDown() {
        // Use MENU_ALTERNATIVE_ACTION which is bound to Shift in WORLD mode.
        // For compatibility with non-WORLD modes where modifier keys should still work,
        // we check the raw MODIFIER_SHIFT action which has no mode filter.
        return isActionDown(Action.MODIFIER_SHIFT);
    }

    /**
     * Check if any camera arrow key is pressed.
     * Replaces: Keyboard.pressedKeys[KEY_LEFT] || Keyboard.pressedKeys[KEY_RIGHT] || ...
     */
    public static boolean isAnyCameraKeyDown() {
        return isActionDown(Action.CAMERA_UP) ||
               isActionDown(Action.CAMERA_DOWN) ||
               isActionDown(Action.CAMERA_LEFT) ||
               isActionDown(Action.CAMERA_RIGHT);
    }

    /**
     * Check if a raw key code is pressed (for interface hotkeys).
     * This is a direct pass-through to the current input frame for interface components
     * that have custom key bindings stored in their data.
     *
     * @param keyCode Keyboard key code from Keyboard.CODE_MAP
     * @return true if the key is currently pressed
     */
    public static boolean isRawKeyPressed(int keyCode) {
        // Must check Keyboard.pressedKeys directly, NOT buttonDown array
        // buttonDown is indexed by InputButtons constants, not raw key codes
        if (rt4.Keyboard.pressedKeys == null || keyCode < 0 || keyCode >= rt4.Keyboard.pressedKeys.length) {
            return false;
        }
        return rt4.Keyboard.pressedKeys[keyCode];
    }

    public static KeyboardDevice getKeyboardDevice() {
        return keyboardDevice;
    }

    public static GamepadDevice getGamepadDevice() {
        return gamepadDevice;
    }

    public static MouseDevice getMouseDevice() {
        return mouseDevice;
    }

    public static BotInputDevice getBotDevice() {
        return botDevice;
    }

    // -------------------------------------------------------------------------
    // Debug
    // -------------------------------------------------------------------------

    /**
     * Enable debug logging for gamepad input.
     * Logs controller name, buttons, and axes to console.
     * @param enabled true to enable logging
     */
    public static void setGamepadDebugLogging(boolean enabled) {
        gamepadDevice.setDebugLogging(enabled);
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    public static void notifyChatSubmit() {
        submittedThisFrame = true;
        submittedSinceArmEnter = true;
        disarmChatIfNoSubmit = false;
        if (InputConfig.INSTANCE.autoWorldAfterSend) {
            chatArmed = false;
        }
        applyMode(deriveMode());
    }

    public static boolean notifyEmptyEnter() {
        if (!InputConfig.INSTANCE.allowQuickChatOnEmptyEnter) {
            chatArmed = false;
            applyMode(deriveMode());
            return false;
        }
        return getMode() == InputMode.CHAT;
    }

    public static void enterChatMode() {
        if (client.gameState != 30) {
            return;
        }
        if (MapController.isOpen()) {
            return;
        }
        if (SpecialModalRegistry.isActive()) {
            return;
        }
        if (ChatBoxModalRegistry.isActive()) {
            return;
        }
        if (InputConfig.INSTANCE.forceWorldWhenChatHidden && ChatboxState.isCollapsed()) {
            return;
        }
        chatArmed = true;
        applyMode(deriveMode());
    }

    public static void enterWorldMode() {
        chatArmed = false;
        applyMode(deriveMode());
    }

    public static void setMode(InputMode newMode) {
        if (newMode == null) {
            return;
        }
        if (newMode == InputMode.CHAT) {
            enterChatMode();
        } else if (newMode == InputMode.WORLD) {
            enterWorldMode();
        } else {
            if (newMode == InputMode.MAP
                    || newMode == InputMode.SPECIAL_MODAL
                    || newMode == InputMode.CHATBOX_MODAL
                    || newMode == InputMode.MAIN_MENU) {
                chatArmed = false;
            }
            applyMode(newMode);
        }
    }

    // ===== Mouse/Cursor Helper Methods =====
    // Unified cursor position from GamepadMouseController (works for mouse, gamepad, and bot)

    /**
     * Get current cursor X position.
     * This returns the unified virtual cursor position that works for mouse, gamepad, and bot input.
     */
    public static int getCursorX() {
        return rt4.amilious.input.GamepadMouseController.getVirtualX();
    }

    /**
     * Get current cursor Y position.
     * This returns the unified virtual cursor position that works for mouse, gamepad, and bot input.
     */
    public static int getCursorY() {
        return rt4.amilious.input.GamepadMouseController.getVirtualY();
    }

    /**
     * Get last click X position from current input frame.
     */
    public static int getLastClickX() {
        return currentFrame != null ? currentFrame.clickX : 0;
    }

    /**
     * Get last click Y position from current input frame.
     */
    public static int getLastClickY() {
        return currentFrame != null ? currentFrame.clickY : 0;
    }

    /**
     * Get last click time from current input frame.
     */
    public static long getLastClickTime() {
        return currentFrame != null ? currentFrame.clickTime : 0L;
    }

    /**
     * Get previous click time from current input frame (for double-click detection).
     */
    public static long getPreviousClickTime() {
        return currentFrame != null ? currentFrame.prevClickTime : 0L;
    }

    /**
     * Get mouse wheel rotation delta from current input frame.
     * Positive = scroll up, negative = scroll down.
     */
    public static int getMouseWheel() {
        return currentFrame != null ? currentFrame.mouseWheel : 0;
    }

    /**
     * Check if a mouse button is currently held down.
     * @param button InputButtons.MOUSE_BUTTON_1 (left), MOUSE_BUTTON_2 (right), or MOUSE_BUTTON_3 (middle)
     */
    public static boolean isMouseButtonDown(int button) {
        if (currentFrame == null || button < 0 || button >= currentFrame.buttonDown.length) {
            return false;
        }
        return currentFrame.buttonDown[button];
    }

    /**
     * Check if a mouse button was just pressed this frame.
     * @param button InputButtons.MOUSE_BUTTON_1 (left), MOUSE_BUTTON_2 (right), or MOUSE_BUTTON_3 (middle)
     */
    public static boolean isMouseButtonPressed(int button) {
        if (currentFrame == null || button < 0 || button >= currentFrame.buttonPressed.length) {
            return false;
        }
        return currentFrame.buttonPressed[button];
    }

    /**
     * Check if a mouse button was just released this frame.
     * @param button InputButtons.MOUSE_BUTTON_1 (left), MOUSE_BUTTON_2 (right), or MOUSE_BUTTON_3 (middle)
     */
    public static boolean isMouseButtonReleased(int button) {
        if (currentFrame == null || button < 0 || button >= currentFrame.buttonReleased.length) {
            return false;
        }
        return currentFrame.buttonReleased[button];
    }
}