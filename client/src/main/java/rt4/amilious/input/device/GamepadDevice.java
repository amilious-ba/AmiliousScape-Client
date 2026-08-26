package rt4.amilious.input.device;

import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Cross-platform gamepad support with XInput and JInput.
 *
 * Architecture:
 * - Tries XInput first (better exclusive access handling for Xbox controllers)
 * - Falls back to JInput (supports more controller types: DirectInput, evdev, IOKit)
 * - Polls button/axis state every tick
 * - Auto-detects first connected controller
 *
 * Button mapping (Xbox-style):
 * - A/Cross (0), B/Circle (1), X/Square (2), Y/Triangle (3)
 * - LB/L1 (4), RB/R1 (5), Back/Select (6), Start (7)
 * - L3 (8), R3 (9), D-pad: Up (10), Down (11), Left (12), Right (13)
 *
 * Axis mapping:
 * - Left stick: X (0), Y (1)
 * - Right stick: X (2), Y (3)
 * - Triggers: LT (4), RT (5)
 */
public final class GamepadDevice implements InputDevice {

    private static final float DEADZONE = 0.15f;
    private static final float TRIGGER_THRESHOLD = 0.1f;

    // Gamepad state
    private boolean connected = false;
    private String controllerName = "Unknown";

    // Button states (16 buttons max for most controllers)
    private final boolean[] buttons = new boolean[16];

    // Axis values (-1.0 to 1.0 for sticks, 0.0 to 1.0 for triggers)
    private final float[] axes = new float[6];

    // Native controller index
    private int controllerIndex = -1;

    // XInput poller (preferred - better exclusive access handling)
    private XInputGamepadPoller xinputPoller;

    // JInput poller (fallback for non-Xbox controllers)
    private JInputGamepadPoller jinputPoller;

    // Debug logging
    private boolean debugLogging = false;
    private int debugLogInterval = 60; // Log every 60 ticks (1 second at 60fps)
    private int debugLogCounter = 0;

    public GamepadDevice() {
        tryDetectController();
    }

    @Override
    public String name() {
        return connected ? controllerName : "gamepad0";
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Manually set connected state (for testing or external controller APIs).
     */
    public void setConnected(boolean value) {
        connected = value;
    }

    /**
     * Attempt to detect and connect to first available gamepad.
     * Tries XInput first (better exclusive access handling), falls back to JInput.
     */
    private void tryDetectController() {
        // Try XInput first (better for Xbox controllers, handles exclusive access better)
        try {
            System.out.println("[Gamepad] Trying XInput...");
            xinputPoller = new XInputGamepadPoller(this);
            if (connected) {
                System.out.println("[Gamepad] XInput controller detected: " + controllerName);
                return; // Success!
            }
        } catch (Throwable e) {
            System.err.println("[Gamepad] XInput failed: " + e.getMessage());
            xinputPoller = null;
        }

        // Fall back to JInput (supports more controller types but has exclusive access issues)
        try {
            System.out.println("[Gamepad] Trying JInput...");
            jinputPoller = new JInputGamepadPoller(this);
            if (connected) {
                System.out.println("[Gamepad] JInput controller detected: " + controllerName);
                return; // Success!
            }
        } catch (Throwable e) {
            System.err.println("[Gamepad] JInput failed: " + e.getMessage());
            jinputPoller = null;
        }

        // No controller found
        System.out.println("[Gamepad] No controllers detected");
        connected = false;
    }

    @Override
    public void poll(InputFrame out) {
        // Poll native controller (XInput takes priority if available)
        if (xinputPoller != null) {
            xinputPoller.poll();
        } else if (jinputPoller != null) {
            jinputPoller.poll();
        }

        if (!connected) {
            return;
        }

        // Poll button states
        pollButton(out, InputButtons.GP_A, 0);
        pollButton(out, InputButtons.GP_B, 1);
        pollButton(out, InputButtons.GP_X, 2);
        pollButton(out, InputButtons.GP_Y, 3);
        pollButton(out, InputButtons.GP_LB, 4);
        pollButton(out, InputButtons.GP_RB, 5);
        pollButton(out, InputButtons.GP_BACK, 6);
        pollButton(out, InputButtons.GP_START, 7);
        pollButton(out, InputButtons.GP_L3, 8);
        pollButton(out, InputButtons.GP_R3, 9);
        pollButton(out, InputButtons.GP_DPAD_UP, 10);
        pollButton(out, InputButtons.GP_DPAD_DOWN, 11);
        pollButton(out, InputButtons.GP_DPAD_LEFT, 12);
        pollButton(out, InputButtons.GP_DPAD_RIGHT, 13);

        // Poll axis values with deadzone
        out.axes[InputButtons.AXIS_LEFT_X] = applyDeadzone(axes[0]);
        out.axes[InputButtons.AXIS_LEFT_Y] = applyDeadzone(axes[1]);
        out.axes[InputButtons.AXIS_RIGHT_X] = applyDeadzone(axes[2]);
        out.axes[InputButtons.AXIS_RIGHT_Y] = applyDeadzone(axes[3]);

        // Triggers (0.0 to 1.0)
        out.axes[InputButtons.AXIS_LT] = Math.max(0, axes[4]);
        out.axes[InputButtons.AXIS_RT] = Math.max(0, axes[5]);

        // Debug logging removed - was spamming console with periodic state logs
        // Kept initial detection and connection logs only
    }

    /**
     * Poll a single button and set it in the output frame.
     */
    private void pollButton(InputFrame out, int outButton, int buttonIndex) {
        if (buttonIndex >= 0 && buttonIndex < buttons.length) {
            out.buttonDown[outButton] = buttons[buttonIndex];
        }
    }

    /**
     * Apply circular deadzone to stick axis.
     */
    private float applyDeadzone(float value) {
        if (Math.abs(value) < DEADZONE) {
            return 0.0f;
        }
        // Rescale from deadzone to 1.0
        float sign = value < 0 ? -1.0f : 1.0f;
        return sign * ((Math.abs(value) - DEADZONE) / (1.0f - DEADZONE));
    }

    // ===== Public API for external controller libraries =====

    /**
     * Set button state (for JInput or other controller API integration).
     * @param buttonIndex Button index (0-15)
     * @param pressed Whether button is pressed
     */
    public void setButton(int buttonIndex, boolean pressed) {
        if (buttonIndex >= 0 && buttonIndex < buttons.length) {
            buttons[buttonIndex] = pressed;
        }
    }

    /**
     * Set axis value (for JInput or other controller API integration).
     * @param axisIndex Axis index (0=LX, 1=LY, 2=RX, 3=RY, 4=LT, 5=RT)
     * @param value Axis value (-1.0 to 1.0 for sticks, 0.0 to 1.0 for triggers)
     */
    public void setAxis(int axisIndex, float value) {
        if (axisIndex >= 0 && axisIndex < axes.length) {
            axes[axisIndex] = value;
        }
    }

    /**
     * Set controller name.
     */
    public void setControllerName(String name) {
        this.controllerName = name != null ? name : "Unknown";
    }

    /**
     * Get current button state (for debugging).
     */
    public boolean getButton(int buttonIndex) {
        return buttonIndex >= 0 && buttonIndex < buttons.length && buttons[buttonIndex];
    }

    /**
     * Get current axis value (for debugging).
     */
    public float getAxis(int axisIndex) {
        return axisIndex >= 0 && axisIndex < axes.length ? axes[axisIndex] : 0.0f;
    }

    /**
     * Attempt to reconnect controller if disconnected.
     * Call periodically (e.g., every few seconds) to detect hot-plugged controllers.
     */
    public void tryReconnect() {
        if (!connected) {
            if (xinputPoller != null) {
                xinputPoller.tryReconnect();
            } else if (jinputPoller != null) {
                jinputPoller.tryReconnect();
            } else {
                // No poller exists, try full detection again
                tryDetectController();
            }
        }
    }

    // ===== Debug Logging =====

    /**
     * Enable/disable debug logging of controller state.
     * Logs button presses and axis values to console.
     * @param enabled true to enable logging
     */
    public void setDebugLogging(boolean enabled) {
        this.debugLogging = enabled;
        if (enabled) {
            System.out.println("[Gamepad] Debug logging enabled");
        }
    }

    /**
     * Set how often to log controller state (in ticks).
     * Default is 60 (once per second at 60fps).
     * @param ticks Number of ticks between log messages
     */
    public void setDebugLogInterval(int ticks) {
        this.debugLogInterval = Math.max(1, ticks);
    }

    /**
     * Log current controller state to console.
     */
    private void logControllerState() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Gamepad] ").append(controllerName).append(" | ");

        // Log pressed buttons
        boolean anyButton = false;
        String[] buttonNames = {"A", "B", "X", "Y", "LB", "RB", "Back", "Start", "L3", "R3",
                                "DUp", "DDown", "DLeft", "DRight"};
        for (int i = 0; i < Math.min(buttons.length, buttonNames.length); i++) {
            if (buttons[i]) {
                if (anyButton) sb.append("+");
                sb.append(buttonNames[i]);
                anyButton = true;
            }
        }
        if (!anyButton) {
            sb.append("(no buttons)");
        }

        // Log axes if non-zero
        sb.append(" | ");
        boolean anyAxis = false;
        String[] axisNames = {"LX", "LY", "RX", "RY", "LT", "RT"};
        for (int i = 0; i < Math.min(axes.length, axisNames.length); i++) {
            if (Math.abs(axes[i]) > 0.01f) {
                if (anyAxis) sb.append(" ");
                sb.append(axisNames[i]).append("=").append(String.format("%.2f", axes[i]));
                anyAxis = true;
            }
        }
        if (!anyAxis) {
            sb.append("(no axes)");
        }

        System.out.println(sb.toString());
    }

    /**
     * Log a single input event immediately (for button press/release detection).
     */
    public void logInputEvent(String event) {
        if (debugLogging) {
            System.out.println("[Gamepad] " + controllerName + " | " + event);
        }
    }
}