package rt4.amilious.input.device.poller;

import de.ralleytn.wrapper.microsoft.xinput.XInput;
import de.ralleytn.wrapper.microsoft.xinput.XInputState;
import de.ralleytn.wrapper.microsoft.xinput.XInputGamepad;
import com.sun.jna.Native;
import rt4.amilious.input.device.GamepadDevice;

/**
 * Polls XInput controllers using RalleYTN's XInput-Wrapper.
 *
 * XInput provides better compatibility with Xbox controllers than DirectInput:
 * - Handles exclusive access conflicts better (e.g., with Armoury Crate)
 * - Native Xbox controller support on Windows
 * - Simpler button/axis mapping (no string identifiers)
 *
 * Button mapping (Xbox controller):
 * - A (0), B (1), X (2), Y (3)
 * - LB (4), RB (5), Back (6), Start (7)
 * - L3 (8), R3 (9)
 * - D-pad: Up (10), Down (11), Left (12), Right (13)
 *
 * Axis mapping:
 * - Left stick: X (0), Y (1)
 * - Right stick: X (2), Y (3)
 * - Triggers: LT (4), RT (5)
 */
public class XInputGamepadPoller {

    // XInput button bitmasks (from xinput.h)
    private static final int XINPUT_GAMEPAD_DPAD_UP        = 0x0001;
    private static final int XINPUT_GAMEPAD_DPAD_DOWN      = 0x0002;
    private static final int XINPUT_GAMEPAD_DPAD_LEFT      = 0x0004;
    private static final int XINPUT_GAMEPAD_DPAD_RIGHT     = 0x0008;
    private static final int XINPUT_GAMEPAD_START          = 0x0010;
    private static final int XINPUT_GAMEPAD_BACK           = 0x0020;
    private static final int XINPUT_GAMEPAD_LEFT_THUMB     = 0x0040;
    private static final int XINPUT_GAMEPAD_RIGHT_THUMB    = 0x0080;
    private static final int XINPUT_GAMEPAD_LEFT_SHOULDER  = 0x0100;
    private static final int XINPUT_GAMEPAD_RIGHT_SHOULDER = 0x0200;
    private static final int XINPUT_GAMEPAD_A              = 0x1000;
    private static final int XINPUT_GAMEPAD_B              = 0x2000;
    private static final int XINPUT_GAMEPAD_X              = 0x4000;
    private static final int XINPUT_GAMEPAD_Y              = 0x8000;

    // Axis normalization constants
    private static final float STICK_MAX = 32767.0f;
    private static final float TRIGGER_MAX = 255.0f;

    private final GamepadDevice device;
    private int playerIndex = -1;
    private boolean connected = false;
    private XInputState state;
    private XInput xinput;

    public XInputGamepadPoller(GamepadDevice device) {
        this.device = device;
        this.state = new XInputState();

        // Load XInput library using JNA
        try {
            xinput = Native.load("xinput1_4", XInput.class);
        } catch (UnsatisfiedLinkError e) {
            // Try older XInput version
            try {
                xinput = Native.load("xinput1_3", XInput.class);
            } catch (UnsatisfiedLinkError e2) {
                // Try XInput 9.1.0 (Windows 7/8)
                try {
                    xinput = Native.load("xinput9_1_0", XInput.class);
                } catch (UnsatisfiedLinkError e3) {
                    System.err.println("[XInput] Failed to load XInput DLL");
                    throw e3;
                }
            }
        }

        tryDetectController();
    }

    /**
     * Try to detect and connect to the first available XInput controller.
     */
    private void tryDetectController() {
        if (xinput == null) {
            return;
        }

        try {
            // XInput supports up to 4 controllers (player index 0-3)
            for (int i = 0; i < 4; i++) {
                int result = xinput.XInputGetState(i, state);
                if (result == 0) { // ERROR_SUCCESS
                    playerIndex = i;
                    connected = true;
                    device.setConnected(true);
                    device.setControllerName("Xbox Controller (Player " + (i + 1) + ")");
                    System.out.println("[XInput] Detected controller at player index " + i);
                    return;
                }
            }

            // No controller found
            System.out.println("[XInput] No controllers detected");
            connected = false;
            device.setConnected(false);

        } catch (Throwable e) {
            System.err.println("[XInput] Failed to detect controller: " + e.getMessage());
            e.printStackTrace();
            connected = false;
            device.setConnected(false);
        }
    }

    /**
     * Poll XInput controller and update GamepadDevice state.
     */
    public void poll() {
        if (xinput == null || playerIndex == -1 || !connected) {
            return;
        }

        try {
            // Poll the controller state
            int result = xinput.XInputGetState(playerIndex, state);

            if (result != 0) { // Controller disconnected
                System.out.println("[XInput] Controller disconnected (error code: " + result + ")");
                connected = false;
                device.setConnected(false);
                return;
            }

            // Get gamepad state
            XInputGamepad gamepad = state.Gamepad;
            int buttons = gamepad.wButtons;

            // Map face buttons (A, B, X, Y)
            device.setButton(0, (buttons & XINPUT_GAMEPAD_A) != 0);
            device.setButton(1, (buttons & XINPUT_GAMEPAD_B) != 0);
            device.setButton(2, (buttons & XINPUT_GAMEPAD_X) != 0);
            device.setButton(3, (buttons & XINPUT_GAMEPAD_Y) != 0);

            // Map shoulder buttons (LB, RB)
            device.setButton(4, (buttons & XINPUT_GAMEPAD_LEFT_SHOULDER) != 0);
            device.setButton(5, (buttons & XINPUT_GAMEPAD_RIGHT_SHOULDER) != 0);

            // Map menu buttons (Back/Select, Start)
            device.setButton(6, (buttons & XINPUT_GAMEPAD_BACK) != 0);
            device.setButton(7, (buttons & XINPUT_GAMEPAD_START) != 0);

            // Map stick buttons (L3, R3)
            device.setButton(8, (buttons & XINPUT_GAMEPAD_LEFT_THUMB) != 0);
            device.setButton(9, (buttons & XINPUT_GAMEPAD_RIGHT_THUMB) != 0);

            // Map D-pad (Up, Down, Left, Right)
            device.setButton(10, (buttons & XINPUT_GAMEPAD_DPAD_UP) != 0);
            device.setButton(11, (buttons & XINPUT_GAMEPAD_DPAD_DOWN) != 0);
            device.setButton(12, (buttons & XINPUT_GAMEPAD_DPAD_LEFT) != 0);
            device.setButton(13, (buttons & XINPUT_GAMEPAD_DPAD_RIGHT) != 0);

            // Map analog sticks (-1.0 to 1.0)
            // Note: XInput reports Y-down as positive, we invert to match standard convention (Y-up positive)
            device.setAxis(0, gamepad.sThumbLX / STICK_MAX);
            device.setAxis(1, -(gamepad.sThumbLY / STICK_MAX));
            device.setAxis(2, gamepad.sThumbRX / STICK_MAX);
            device.setAxis(3, -(gamepad.sThumbRY / STICK_MAX));

            // Map triggers (0.0 to 1.0)
            //device.setAxis(4, gamepad.bLeftTrigger / TRIGGER_MAX);
            //device.setAxis(5, gamepad.bRightTrigger / TRIGGER_MAX);
            device.setAxis(4, (gamepad.bLeftTrigger & 0xFF) / TRIGGER_MAX);
            device.setAxis(5, (gamepad.bRightTrigger & 0xFF) / TRIGGER_MAX);

        } catch (Throwable e) {
            System.err.println("[XInput] Poll error: " + e.getMessage());
            e.printStackTrace();
            connected = false;
            device.setConnected(false);
        }
    }

    /**
     * Attempt to reconnect if controller was disconnected.
     */
    public void tryReconnect() {
        if (!connected) {
            tryDetectController();
        }
    }
}
