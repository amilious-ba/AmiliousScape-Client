package rt4.amilious.input.state;

import java.util.Arrays;

/**
 * One frame of merged device input (before action mapping).
 * Button indices are defined by InputButtons.
 */
public final class InputFrame {

    public final boolean[] buttonDown;
    public final boolean[] buttonPressed;
    public final boolean[] buttonReleased;
    public final float[] axes;

    public InputFrame(int buttonCount, int axisCount) {
        buttonDown = new boolean[buttonCount];
        buttonPressed = new boolean[buttonCount];
        buttonReleased = new boolean[buttonCount];
        axes = new float[axisCount];
    }

    public void clear() {
        Arrays.fill(buttonDown, false);
        Arrays.fill(buttonPressed, false);
        Arrays.fill(buttonReleased, false);
        Arrays.fill(axes, 0f);
    }

    public void copyDownFrom(InputFrame other) {
        System.arraycopy(other.buttonDown, 0, buttonDown, 0, buttonDown.length);
        System.arraycopy(other.axes, 0, axes, 0, axes.length);
    }

    /**
     * Compute pressed/released from previous frame's buttonDown vs this frame's buttonDown.
     */
    public void computeEdges(InputFrame previous) {
        for (int i = 0; i < buttonDown.length; i++) {
            boolean now = buttonDown[i];
            boolean was = previous != null && i < previous.buttonDown.length && previous.buttonDown[i];
            buttonPressed[i] = now && !was;
            buttonReleased[i] = !now && was;
        }
    }
}