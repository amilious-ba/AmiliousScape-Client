package rt4.amilious.input.device;

import rt4.amilious.input.state.InputFrame;

/**
 * Placeholder for JInput / GLFW / pure Java gamepad later.
 */
public final class GamepadDevice implements InputDevice {

    private boolean connected;

    @Override
    public String name() {
        return "gamepad0";
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean value) {
        connected = value;
    }

    @Override
    public void poll(InputFrame out) {
        if (!connected) {
            return;
        }
        // Later:
        // out.axes[InputButtons.AXIS_RIGHT_X] = stickX;
        // out.buttonDown[InputButtons.GP_A] = aButton;
    }
}