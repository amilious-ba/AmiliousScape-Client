package rt4.amilious.input.device;

import rt4.amilious.input.state.InputFrame;

public interface InputDevice {
    String name();
    boolean isConnected();

    /** Sample hardware into the shared frame (OR into device-local, then merge). */
    void poll(InputFrame out);

    /**
     * Returns the number of game ticks since last input activity on this device.
     * Used for AFK detection (Protocol checks if keyboard + mouse > 15000).
     * @return idle loop count, or 0 if device doesn't track idle time
     */
    default int getIdleLoops() {
        return 0;
    }
}
