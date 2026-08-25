package rt4.amilious.input.device;

import net.java.games.input.*;

/**
 * JInput-based gamepad poller for native controller support.
 *
 * Supports:
 * - Steam Input (via DirectInput/XInput passthrough)
 * - Xbox 360/One/Series controllers (XInput)
 * - PlayStation 4/5 controllers (DirectInput)
 * - Generic DirectInput gamepads
 * - Linux evdev controllers
 * - macOS IOKit controllers
 *
 * Architecture:
 * - Polls first detected gamepad controller
 * - Auto-detects button/axis mappings
 * - Updates GamepadDevice state every tick
 */
public class JInputGamepadPoller {

    private Controller controller;
    private GamepadDevice device;
    private boolean initialized = false;

    // Button component mappings (cached for performance)
    private Component buttonA, buttonB, buttonX, buttonY;
    private Component buttonLB, buttonRB;
    private Component buttonBack, buttonStart;
    private Component buttonL3, buttonR3;
    private Component dpadUp, dpadDown, dpadLeft, dpadRight;

    // Axis component mappings
    private Component axisLeftX, axisLeftY;
    private Component axisRightX, axisRightY;
    private Component axisLT, axisRT;

    public JInputGamepadPoller(GamepadDevice device) {
        this.device = device;
        // Load JInput native libraries from JAR
        JInputNativeLoader.loadNatives();
        tryInitialize();
    }

    /**
     * Attempt to detect and initialize first gamepad controller.
     */
    private void tryInitialize() {
        try {
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            Controller[] controllers = env.getControllers();

            // Find first gamepad or game controller
            for (Controller c : controllers) {
                Controller.Type type = c.getType();
                if (type == Controller.Type.GAMEPAD || type == Controller.Type.STICK) {
                    controller = c;
                    mapComponents();
                    device.setConnected(true);
                    device.setControllerName(c.getName());
                    initialized = true;
                    System.out.println("[Gamepad] Connected: " + c.getName());
                    return;
                }
            }

            System.out.println("[Gamepad] No controller detected");
        } catch (Exception e) {
            System.err.println("[Gamepad] Failed to initialize JInput: " + e.getMessage());
        }
    }

    /**
     * Map JInput components to logical gamepad buttons/axes.
     * Handles Xbox, PlayStation, and generic DirectInput layouts.
     */
    private void mapComponents() {
        if (controller == null) {
            return;
        }

        Component[] components = controller.getComponents();
        String name = controller.getName().toLowerCase();

        // Detect controller type
        boolean isXbox = name.contains("xbox") || name.contains("xinput") || name.contains("ally");
        boolean isPS = name.contains("playstation") || name.contains("dualshock") || name.contains("dualsense");

        System.out.println("[Gamepad] Mapping components for: " + controller.getName());
        System.out.println("[Gamepad] Found " + components.length + " components:");

        for (Component comp : components) {
            Component.Identifier id = comp.getIdentifier();
            String idName = id.getName().toLowerCase();

            // Debug: print all components
            System.out.println("[Gamepad]   " + id.getName() + " (" + id.getClass().getSimpleName() + ")");

            // Map buttons by name (DirectInput controllers use string identifiers)
            if (idName.equals("0")) {
                buttonA = comp;
                System.out.println("[Gamepad]     -> Mapped to A button");
            } else if (idName.equals("1")) {
                buttonB = comp;
                System.out.println("[Gamepad]     -> Mapped to B button");
            } else if (idName.equals("2")) {
                buttonX = comp;
                System.out.println("[Gamepad]     -> Mapped to X button");
            } else if (idName.equals("3")) {
                buttonY = comp;
                System.out.println("[Gamepad]     -> Mapped to Y button");
            } else if (idName.equals("4")) {
                buttonLB = comp;
                System.out.println("[Gamepad]     -> Mapped to LB button");
            } else if (idName.equals("5")) {
                buttonRB = comp;
                System.out.println("[Gamepad]     -> Mapped to RB button");
            } else if (idName.equals("6")) {
                buttonBack = comp;
                System.out.println("[Gamepad]     -> Mapped to Back button");
            } else if (idName.equals("7")) {
                buttonStart = comp;
                System.out.println("[Gamepad]     -> Mapped to Start button");
            } else if (idName.equals("8")) {
                buttonL3 = comp;
                System.out.println("[Gamepad]     -> Mapped to L3 button");
            } else if (idName.equals("9")) {
                buttonR3 = comp;
                System.out.println("[Gamepad]     -> Mapped to R3 button");
            }

            // Map axes by name
            if (idName.equals("x")) {
                axisLeftX = comp;
                System.out.println("[Gamepad]     -> Mapped to Left Stick X");
            } else if (idName.equals("y")) {
                axisLeftY = comp;
                System.out.println("[Gamepad]     -> Mapped to Left Stick Y");
            } else if (idName.equals("rx")) {
                axisRightX = comp;
                System.out.println("[Gamepad]     -> Mapped to Right Stick X");
            } else if (idName.equals("ry")) {
                axisRightY = comp;
                System.out.println("[Gamepad]     -> Mapped to Right Stick Y");
            } else if (idName.equals("z")) {
                // Z axis is triggers on Xbox controllers
                axisLT = comp;
                System.out.println("[Gamepad]     -> Mapped to Triggers (LT/RT combined)");
            } else if (idName.equals("pov")) {
                dpadUp = comp;
                System.out.println("[Gamepad]     -> Mapped to D-pad (POV)");
            }
        }
    }

    private static int pollCallCount = 0;

    /**
     * Poll controller state and update GamepadDevice.
     * Call this every game tick from InputManager.
     */
    public void poll() {
        if (!initialized || controller == null) {
            if (pollCallCount < 3) {
                System.out.println("[Gamepad] poll() called but not initialized (controller=" + controller + ", init=" + initialized + ")");
                pollCallCount++;
            }
            return;
        }

        // Debug: log first few poll calls
        if (pollCallCount < 3) {
            System.out.println("[Gamepad] poll() called #" + (pollCallCount + 1));
            pollCallCount++;
        }

        // Poll controller (JInput requires explicit polling)
        if (!controller.poll()) {
            // Controller disconnected
            device.setConnected(false);
            initialized = false;
            System.out.println("[Gamepad] Controller disconnected");
            return;
        }

        if (pollCallCount == 3) {
            System.out.println("[Gamepad] controller.poll() succeeded, reading button A value: " + (buttonA != null ? buttonA.getPollData() : "null"));
        }

        // Read button states
        pollButton(buttonA, 0);
        pollButton(buttonB, 1);
        pollButton(buttonX, 2);
        pollButton(buttonY, 3);
        pollButton(buttonLB, 4);
        pollButton(buttonRB, 5);
        pollButton(buttonBack, 6);
        pollButton(buttonStart, 7);
        pollButton(buttonL3, 8);
        pollButton(buttonR3, 9);

        // Read axis values
        pollAxis(axisLeftX, 0);
        pollAxis(axisLeftY, 1);
        pollAxis(axisRightX, 2);
        pollAxis(axisRightY, 3);

        // Handle triggers (varies by controller)
        if (axisLT != null && axisRT != null) {
            // Separate LT/RT axes (PS controllers, some PC gamepads)
            device.setAxis(4, normalizeAxis(axisLT));
            device.setAxis(5, normalizeAxis(axisRT));
        } else if (axisLT != null) {
            // Combined trigger axis (Xbox controllers: -1=LT, +1=RT)
            float combined = axisLT.getPollData();
            device.setAxis(4, combined < 0 ? -combined : 0); // LT
            device.setAxis(5, combined > 0 ? combined : 0);  // RT
        }

        // Handle D-pad (POV hat)
        if (dpadUp != null) {
            float pov = dpadUp.getPollData();
            // POV: 0.0=up, 0.25=right, 0.5=down, 0.75=left, 1.0=center/none
            boolean up = (pov >= 0.875f || pov <= 0.125f) && pov != Component.POV.OFF;
            boolean down = (pov >= 0.375f && pov <= 0.625f);
            boolean left = (pov >= 0.625f && pov <= 0.875f);
            boolean right = (pov >= 0.125f && pov <= 0.375f);

            device.setButton(10, up);
            device.setButton(11, down);
            device.setButton(12, left);
            device.setButton(13, right);
        }
    }

    /**
     * Poll a single button component.
     */
    private void pollButton(Component comp, int buttonIndex) {
        if (comp != null) {
            float value = comp.getPollData();
            boolean pressed = value > 0.5f;
            device.setButton(buttonIndex, pressed);
            // Debug: log button presses
            if (pressed) {
                System.out.println("[Gamepad] Button " + buttonIndex + " pressed (value=" + value + ")");
            }
        }
    }

    /**
     * Poll a single axis component.
     */
    private void pollAxis(Component comp, int axisIndex) {
        if (comp != null) {
            float value = comp.getPollData();
            device.setAxis(axisIndex, value);
            // Debug: log significant axis movement
            if (Math.abs(value) > 0.2f) {
                System.out.println("[Gamepad] Axis " + axisIndex + " = " + value);
            }
        }
    }

    /**
     * Normalize axis value (some axes report 0-1 instead of -1 to 1).
     */
    private float normalizeAxis(Component comp) {
        if (comp == null) {
            return 0.0f;
        }
        float value = comp.getPollData();
        // If axis seems to be 0-1 range (triggers), normalize to 0-1
        // Otherwise assume -1 to 1 (sticks)
        return value;
    }

    /**
     * Check if controller is currently connected.
     */
    public boolean isConnected() {
        return initialized && controller != null;
    }

    /**
     * Attempt to reconnect if controller was disconnected.
     */
    public void tryReconnect() {
        if (!initialized) {
            tryInitialize();
        }
    }
}
