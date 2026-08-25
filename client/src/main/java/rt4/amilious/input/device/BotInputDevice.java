package rt4.amilious.input.device;

import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

/**
 * Simulated input device for bot/automated control.
 * Allows programmatic button presses without physical keyboard/mouse input.
 *
 * Usage:
 *   botDevice.pressButton(InputButtons.KB_W);
 *   // ... wait for action to process ...
 *   botDevice.releaseButton(InputButtons.KB_W);
 */
public class BotInputDevice implements InputDevice {

    // Simulated button states (mirrors InputFrame.buttonDown structure)
    private final boolean[] simulatedButtons = new boolean[InputButtons.BUTTON_COUNT];

    // Simulated mouse state
    private int mouseX = 0;
    private int mouseY = 0;
    private int mouseWheel = 0;

    @Override
    public String name() {
        return "Bot";
    }

    @Override
    public boolean isConnected() {
        return true; // Bot device is always "connected"
    }

    @Override
    public void poll(InputFrame out) {
        // Copy all simulated button states into the output frame
        System.arraycopy(simulatedButtons, 0, out.buttonDown, 0, InputButtons.BUTTON_COUNT);

        // Override mouse position and wheel
        out.mouseX = mouseX;
        out.mouseY = mouseY;
        out.mouseWheel = mouseWheel;
    }

    // ===== Button Control =====

    /**
     * Simulate pressing a button (will remain pressed until released).
     * @param button InputButtons constant (e.g., InputButtons.KB_W)
     */
    public void pressButton(int button) {
        if (button >= 0 && button < InputButtons.BUTTON_COUNT) {
            simulatedButtons[button] = true;
        }
    }

    /**
     * Simulate releasing a button.
     * @param button InputButtons constant
     */
    public void releaseButton(int button) {
        if (button >= 0 && button < InputButtons.BUTTON_COUNT) {
            simulatedButtons[button] = false;
        }
    }

    /**
     * Simulate a quick tap (press for one tick).
     * Button will auto-release after next poll().
     * @param button InputButtons constant
     */
    public void tapButton(int button) {
        pressButton(button);
        // Caller should wait one tick then call releaseButton()
    }

    /**
     * Check if a button is currently simulated as pressed.
     * @param button InputButtons constant
     * @return true if simulated as pressed
     */
    public boolean isButtonPressed(int button) {
        if (button >= 0 && button < InputButtons.BUTTON_COUNT) {
            return simulatedButtons[button];
        }
        return false;
    }

    /**
     * Release all currently pressed buttons.
     */
    public void releaseAll() {
        for (int i = 0; i < InputButtons.BUTTON_COUNT; i++) {
            simulatedButtons[i] = false;
        }
    }

    // ===== Mouse Control =====

    /**
     * Set simulated mouse position.
     * @param x Mouse X coordinate
     * @param y Mouse Y coordinate
     */
    public void setMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    /**
     * Set simulated mouse wheel delta.
     * @param delta Wheel scroll amount (positive = up, negative = down)
     */
    public void setMouseWheel(int delta) {
        this.mouseWheel = delta;
    }

    /**
     * Simulate a mouse click at current position.
     * @param button InputButtons.MOUSE_BUTTON_1 (left), MOUSE_BUTTON_2 (right), etc.
     */
    public void clickMouse(int button) {
        pressButton(button);
        // Caller should wait one tick then call releaseButton()
    }

    /**
     * Simulate moving mouse to position and clicking.
     * @param x Target X
     * @param y Target Y
     * @param button Mouse button constant
     */
    public void clickAt(int x, int y, int button) {
        setMousePosition(x, y);
        clickMouse(button);
    }
}
