package rt4.amilious.input;

import rt4.GameShell;
import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Translates gamepad right stick input into mouse cursor movement.
 *
 * Architecture:
 * - Reads right stick axes from InputFrame
 * - Maintains virtual cursor position
 * - Generates synthetic MouseEvent objects
 * - Dispatches events to GameShell.canvas to trigger proper AWT flow
 *
 * Button mapping:
 * - Right stick (X/Y axes) → cursor movement
 * - RT (right trigger) → left click
 * - LT (left trigger) → right click
 *
 * This allows gamepad cursor control while keeping all existing mouse code working.
 */
public final class GamepadMouseController {

    // Configuration
    private static boolean enabled = true;
    private static boolean dispatchEvents = true; // Enable event dispatch
    private static boolean ignorePhysicalMouse = false; // Disable physical mouse (for botting)
    private static float sensitivity = 20.0f; // pixels per frame at full stick deflection
    private static float triggerThreshold = 0.3f; // 30% trigger press = click

    // Virtual cursor state
    private static int virtualX = 400; // Start at reasonable default
    private static int virtualY = 300;

    // Input source tracking (based on last movement, not idle detection)
    private static boolean lastMovementWasMouse = true; // Track which input last moved cursor
    private static int lastMouseX = 0;
    private static int lastMouseY = 0;

    // Previous trigger states for edge detection
    private static boolean wasLTPressed = false;
    private static boolean wasRTPressed = false;

    // Custom cursor (transparent)
    private static Cursor blankCursor = null;

    private GamepadMouseController() {
        // Static utility class
    }

    /**
     * Process gamepad input and generate mouse events.
     * Call this every frame from InputManager.tick() AFTER device polling.
     *
     * @param frame Current input frame with gamepad axis data
     * @param mode Current input mode
     */
    public static void tick(InputFrame frame, InputMode mode) {
        if (!enabled) {
            return;
        }

        // Skip only during text input modes (CHAT, SPECIAL_MODAL, CHATBOX_MODAL)
        // to avoid interference with typing
        if (mode == InputMode.CHAT || mode == InputMode.SPECIAL_MODAL || mode == InputMode.CHATBOX_MODAL) {
            // Reset trigger states when disabled to prevent stuck buttons
            wasLTPressed = false;
            wasRTPressed = false;
            return;
        }
        // Active in: WORLD, MAP, MAIN_MENU (login screen)

        // GameShell.canvas might be null during initialization
        if (GameShell.canvas == null) {
            // Reset trigger states when disabled
            wasLTPressed = false;
            wasRTPressed = false;
            return;
        }

        // Read right stick axes
        float rightX = 0.0f;
        float rightY = 0.0f;
        if (frame.axes != null && frame.axes.length > InputButtons.AXIS_RIGHT_Y) {
            rightX = frame.axes[InputButtons.AXIS_RIGHT_X]; // -1.0 to 1.0
            rightY = frame.axes[InputButtons.AXIS_RIGHT_Y]; // -1.0 to 1.0
        }

        // Check for actual movement from each input source
        boolean mouseMovedThisFrame = false;
        boolean gamepadMovedThisFrame = false;
        boolean botMovedThisFrame = false;

        // Detect physical mouse movement (unless disabled)
        // Only count as movement if position changed AND it's not from our own synthetic events
        if (!ignorePhysicalMouse && (frame.mouseX > 0 || frame.mouseY > 0)) {
            // Check if mouse position changed from last frame
            if (frame.mouseX != lastMouseX || frame.mouseY != lastMouseY) {
                lastMouseX = frame.mouseX;
                lastMouseY = frame.mouseY;

                // Only treat as mouse movement if it differs from virtual cursor
                // (If they match, it's probably our synthetic gamepad event)
                if (Math.abs(frame.mouseX - virtualX) > 1 || Math.abs(frame.mouseY - virtualY) > 1) {
                    mouseMovedThisFrame = true;
                }
            }
        }

        // Detect gamepad stick movement (with deadzone to ignore drift)
        float gamepadDeadzone = 0.15f;
        if (Math.abs(rightX) > gamepadDeadzone || Math.abs(rightY) > gamepadDeadzone) {
            gamepadMovedThisFrame = true;
        }

        // Detect bot movement (mouse position changed but physical mouse didn't move)
        if (ignorePhysicalMouse && (frame.mouseX > 0 || frame.mouseY > 0)) {
            if (frame.mouseX != lastMouseX || frame.mouseY != lastMouseY) {
                botMovedThisFrame = true;
                lastMouseX = frame.mouseX;
                lastMouseY = frame.mouseY;
            }
        }

        // Update cursor position based on which input moved
        // Priority: mouse > gamepad > bot, but gamepad can take over if moved significantly
        if (mouseMovedThisFrame) {
            // Physical mouse moved - update position and mark as last input
            virtualX = frame.mouseX;
            virtualY = frame.mouseY;
            lastMovementWasMouse = true;
        } else if (gamepadMovedThisFrame) {
            // Gamepad moved - apply movement and switch to gamepad mode
            // Apply quadratic curve for better precision at low values
            float curvedX = applyCurve(rightX);
            float curvedY = applyCurve(rightY);
            
            int deltaX = (int) (curvedX * sensitivity);
            int deltaY = (int) (curvedY * sensitivity);

            virtualX += deltaX;
            virtualY += deltaY;

            // Clamp to canvas bounds
            int canvasWidth = Math.max(1, GameShell.canvasWidth);
            int canvasHeight = Math.max(1, GameShell.canvasHeight);
            virtualX = Math.max(0, Math.min(canvasWidth - 1, virtualX));
            virtualY = Math.max(0, Math.min(canvasHeight - 1, virtualY));

            lastMovementWasMouse = false;
        } else if (botMovedThisFrame) {
            // Bot moved - update position and mark as last input
            virtualX = frame.mouseX;
            virtualY = frame.mouseY;
            lastMovementWasMouse = false;
        }

        // Update cursor visibility based on last movement source
        if (lastMovementWasMouse) {
            // Last movement was physical mouse - show system cursor
            restoreSystemCursor();
        } else {
            // Last movement was gamepad/bot - hide system cursor, show crosshair
            ensureBlankCursor();
        }

        // Dispatch synthetic mouse events for gamepad movement
        boolean hasMoved = gamepadMovedThisFrame;

        // Generate mouse movement event (only if event dispatch is enabled)
        if (hasMoved && dispatchEvents) {
            dispatchMouseMove(virtualX, virtualY);
        }

        // Handle trigger-based clicking (only if event dispatch is enabled)
        if (dispatchEvents) {
            processClicks(frame);
        }
    }

    /**
     * Apply quadratic curve to analog stick input for better precision.
     * Small movements remain small, but large movements get amplified.
     * @param value Raw stick input (-1.0 to 1.0)
     * @return Curved value (-1.0 to 1.0)
     */
    private static float applyCurve(float value) {
        // Preserve sign, apply quadratic curve to magnitude
        float sign = value < 0 ? -1.0f : 1.0f;
        float magnitude = Math.abs(value);
        return sign * (magnitude * magnitude);
    }

    /**
     * Process LT/RT triggers for left/right clicking.
     */
    private static void processClicks(InputFrame frame) {
        if (frame.axes == null || frame.axes.length <= InputButtons.AXIS_RT) {
            return;
        }

        // Read trigger values (0.0 to 1.0)
        float lt = Math.max(0, frame.axes[InputButtons.AXIS_LT]); // Left trigger
        float rt = Math.max(0, frame.axes[InputButtons.AXIS_RT]); // Right trigger

        // Check if triggers are pressed beyond threshold
        boolean ltPressed = lt > triggerThreshold;
        boolean rtPressed = rt > triggerThreshold;

        // RT = left click (primary action)
        if (rtPressed && !wasRTPressed) {
            dispatchMousePress(virtualX, virtualY, MouseEvent.BUTTON1);
        } else if (!rtPressed && wasRTPressed) {
            dispatchMouseRelease(virtualX, virtualY, MouseEvent.BUTTON1);
        }

        // LT = right click (secondary action)
        if (ltPressed && !wasLTPressed) {
            dispatchMousePress(virtualX, virtualY, MouseEvent.BUTTON3);
        } else if (!ltPressed && wasLTPressed) {
            dispatchMouseRelease(virtualX, virtualY, MouseEvent.BUTTON3);
        }

        // Update previous state
        wasLTPressed = ltPressed;
        wasRTPressed = rtPressed;
    }

    /**
     * Dispatch a synthetic mouse movement event.
     */
    private static void dispatchMouseMove(int x, int y) {
        MouseEvent event = new MouseEvent(
                GameShell.canvas,
                MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(),
                0, // no modifiers
                x, y,
                0, // click count
                false // not popup trigger
        );
        GameShell.canvas.dispatchEvent(event);
    }

    /**
     * Dispatch a synthetic mouse press event.
     */
    private static void dispatchMousePress(int x, int y, int button) {
        int modifiers = 0;
        if (button == MouseEvent.BUTTON1) {
            modifiers = MouseEvent.BUTTON1_DOWN_MASK;
        } else if (button == MouseEvent.BUTTON3) {
            modifiers = MouseEvent.BUTTON3_DOWN_MASK;
        }

        MouseEvent event = new MouseEvent(
                GameShell.canvas,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                modifiers,
                x, y,
                1, // click count
                button == MouseEvent.BUTTON3 // popup trigger for right-click
        );
        GameShell.canvas.dispatchEvent(event);
    }

    /**
     * Dispatch a synthetic mouse release event.
     */
    private static void dispatchMouseRelease(int x, int y, int button) {
        int modifiers = 0;
        if (button == MouseEvent.BUTTON1) {
            modifiers = MouseEvent.BUTTON1_DOWN_MASK;
        } else if (button == MouseEvent.BUTTON3) {
            modifiers = MouseEvent.BUTTON3_DOWN_MASK;
        }

        MouseEvent event = new MouseEvent(
                GameShell.canvas,
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                modifiers,
                x, y,
                1, // click count
                false
        );
        GameShell.canvas.dispatchEvent(event);
    }

    // ===== Public Configuration API =====

    /**
     * Enable or disable gamepad mouse control (visual cursor + event dispatch).
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * Check if gamepad mouse control is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable synthetic mouse event dispatch.
     * When false, only draws the virtual cursor without sending mouse events.
     */
    public static void setDispatchEvents(boolean value) {
        dispatchEvents = value;
    }

    /**
     * Check if event dispatch is enabled.
     */
    public static boolean isDispatchingEvents() {
        return dispatchEvents;
    }

    /**
     * Enable or disable physical mouse input.
     * When true, physical mouse movements and clicks are ignored (useful for botting).
     * Bot input via BotInputDevice will still work.
     */
    public static void setIgnorePhysicalMouse(boolean value) {
        ignorePhysicalMouse = value;
        if (value) {
            // When ignoring physical mouse, switch to gamepad/bot mode
            lastMovementWasMouse = false;
        }
    }

    /**
     * Check if physical mouse input is being ignored.
     */
    public static boolean isIgnoringPhysicalMouse() {
        return ignorePhysicalMouse;
    }

    /**
     * Set cursor movement sensitivity.
     * @param value Pixels per frame at full stick deflection (default: 20.0)
     */
    public static void setSensitivity(float value) {
        sensitivity = Math.max(1.0f, Math.min(100.0f, value));
    }

    /**
     * Get current sensitivity value.
     */
    public static float getSensitivity() {
        return sensitivity;
    }

    /**
     * Set trigger press threshold for clicks.
     * @param value Threshold (0.0 to 1.0, default: 0.3)
     */
    public static void setTriggerThreshold(float value) {
        triggerThreshold = Math.max(0.0f, Math.min(1.0f, value));
    }

    /**
     * Get current trigger threshold.
     */
    public static float getTriggerThreshold() {
        return triggerThreshold;
    }

    /**
     * Initialize virtual cursor to current physical mouse position.
     * Call this when switching to gamepad mode.
     */
    public static void syncVirtualCursorToMouse(int mouseX, int mouseY) {
        virtualX = mouseX;
        virtualY = mouseY;
    }

    /**
     * Get current virtual cursor X position.
     */
    public static int getVirtualX() {
        return virtualX;
    }

    /**
     * Get current virtual cursor Y position.
     */
    public static int getVirtualY() {
        return virtualY;
    }

    /**
     * Create and set a blank (invisible) cursor on the canvas.
     */
    private static void ensureBlankCursor() {
        if (GameShell.canvas == null) {
            return;
        }

        // Check if cursor is already blank
        if (blankCursor != null && GameShell.canvas.getCursor() == blankCursor) {
            return;
        }

        // Create transparent cursor (1x1 transparent image)
        if (blankCursor == null) {
            BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor"
            );
        }

        // Set the blank cursor
        GameShell.canvas.setCursor(blankCursor);
    }

    /**
     * Restore the default system cursor.
     */
    public static void restoreSystemCursor() {
        if (GameShell.canvas != null) {
            GameShell.canvas.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Draw the virtual cursor on screen.
     * Only draws when using gamepad or bot input (not physical mouse).
     * Call this from the game's 2D rendering pass.
     *
     * @param enabled Whether to show the virtual cursor overlay
     */
    public static void drawVirtualCursor(boolean enabled) {
        if (!enabled || !GamepadMouseController.enabled) {
            return;
        }

        // Only draw crosshair when last movement was NOT from physical mouse
        // Physical mouse uses the system arrow cursor
        if (!lastMovementWasMouse) {
            drawCrosshair(virtualX, virtualY);
        }
    }

    /**
     * Draw a crosshair at the specified position.
     * Uses the game's 2D rendering primitives.
     */
    private static void drawCrosshair(int x, int y) {
        // Draw crosshair (bright cyan for visibility)
        int color = 0x00FFFF; // Cyan
        int size = 10; // Crosshair arm length
        int thickness = 2; // Line thickness

        // Check which renderer is active
        boolean useGl = rt4.GlRenderer.enabled;

        // Draw horizontal line
        if (useGl) {
            rt4.GlRaster.drawRect(x - size, y - thickness / 2, size * 2, thickness, color);
        } else {
            rt4.SoftwareRaster.drawRect(x - size, y - thickness / 2, size * 2, thickness, color);
        }

        // Draw vertical line
        if (useGl) {
            rt4.GlRaster.drawRect(x - thickness / 2, y - size, thickness, size * 2, color);
        } else {
            rt4.SoftwareRaster.drawRect(x - thickness / 2, y - size, thickness, size * 2, color);
        }

        // Draw center dot for better precision
        if (useGl) {
            rt4.GlRaster.fillRect(x - 1, y - 1, 3, 3, 0xFFFFFF); // White center
        } else {
            rt4.SoftwareRaster.fillRect(x - 1, y - 1, 3, 3, 0xFFFFFF);
        }
    }
}
