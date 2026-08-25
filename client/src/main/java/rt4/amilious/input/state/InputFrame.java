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
    public final boolean[] buttonConsumed;  // Consumed by UI/interface this frame
    public final float[] axes;

    // Mouse state (from MouseDevice)
    public int mouseX = 0;
    public int mouseY = 0;
    public int mouseWheel = 0;
    public int clickX = 0;
    public int clickY = 0;
    public long clickTime = 0L;
    public long prevClickTime = 0L;

    // Idle tracking (for AFK detection)
    public int mouseIdleLoops = 0;
    public int keyboardIdleLoops = 0;

    public InputFrame(int buttonCount, int axisCount) {
        buttonDown = new boolean[buttonCount];
        buttonPressed = new boolean[buttonCount];
        buttonReleased = new boolean[buttonCount];
        buttonConsumed = new boolean[buttonCount];
        axes = new float[axisCount];
    }

    public void clear() {
        Arrays.fill(buttonDown, false);
        Arrays.fill(buttonPressed, false);
        Arrays.fill(buttonReleased, false);
        Arrays.fill(buttonConsumed, false);
        Arrays.fill(axes, 0f);
        mouseX = 0;
        mouseY = 0;
        mouseWheel = 0;
        clickX = 0;
        clickY = 0;
        clickTime = 0L;
        prevClickTime = 0L;
        mouseIdleLoops = 0;
        keyboardIdleLoops = 0;
    }

    public void copyDownFrom(InputFrame other) {
        System.arraycopy(other.buttonDown, 0, buttonDown, 0, buttonDown.length);
        System.arraycopy(other.axes, 0, axes, 0, axes.length);
        mouseX = other.mouseX;
        mouseY = other.mouseY;
        mouseWheel = other.mouseWheel;
        clickX = other.clickX;
        clickY = other.clickY;
        clickTime = other.clickTime;
        prevClickTime = other.prevClickTime;
        mouseIdleLoops = other.mouseIdleLoops;
        keyboardIdleLoops = other.keyboardIdleLoops;
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