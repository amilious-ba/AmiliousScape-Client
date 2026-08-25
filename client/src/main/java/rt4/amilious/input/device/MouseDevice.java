package rt4.amilious.input.device;

import rt4.Mouse;
import rt4.MouseWheel;
import rt4.amilious.input.state.InputButtons;
import rt4.amilious.input.state.InputFrame;

/**
 * Polls mouse state from rt4.Mouse and rt4.MouseWheel into the input system.
 *
 * Mouse state polled:
 *   - lastMouseX/Y: Current mouse position
 *   - pressedButton: Which button is held (1=left, 2=right, 3=middle)
 *   - clickButton: Which button was clicked this frame
 *   - clickX/Y: Where the last click occurred
 *   - clickTime/prevClickTime: Timing for click detection and double-click
 *
 * MouseWheel state polled:
 *   - wheelRotation: Scroll delta (positive=up, negative=down)
 *
 * Note: MouseRecorder is NOT polled here (separate telemetry/debugging feature)
 */
public class MouseDevice implements InputDevice {

    private MouseWheel mouseWheel;

    @Override
    public String name() {
        return "Mouse";
    }

    @Override
    public boolean isConnected() {
        return Mouse.instance != null;
    }

    @Override
    public int getIdleLoops() {
        return Mouse.getIdleLoops();
    }

    @Override
    public void poll(InputFrame out) {
        if (Mouse.instance == null) {
            return;
        }

        // Sample current mouse position
        out.mouseX = Mouse.lastMouseX;
        out.mouseY = Mouse.lastMouseY;

        // Sample click position and timing
        out.clickX = Mouse.clickX;
        out.clickY = Mouse.clickY;
        out.clickTime = Mouse.clickTime;
        out.prevClickTime = Mouse.prevClickTime;

        // Sample mouse button state
        // Mouse.pressedButton: 0=none, 1=left, 2=right, 3=middle
        int pressed = Mouse.pressedButton;
        out.buttonDown[InputButtons.MOUSE_BUTTON_1] = (pressed == 1);
        out.buttonDown[InputButtons.MOUSE_BUTTON_2] = (pressed == 2);
        out.buttonDown[InputButtons.MOUSE_BUTTON_3] = (pressed == 3);

        // Mouse.clickButton: which button was clicked this frame (0=none, 1=left, 2=right)
        int clicked = Mouse.clickButton;
        out.buttonDown[InputButtons.MOUSE_CLICK] = (clicked != 0);

        // Sample mouse wheel (lazy init since MouseWheel.create() can fail on some platforms)
        if (mouseWheel == null) {
            mouseWheel = MouseWheel.create();
        }
        if (mouseWheel != null) {
            out.mouseWheel = mouseWheel.getRotation();
        } else {
            out.mouseWheel = 0;
        }
    }

}
