package rt4.amilious.input.device;

import rt4.amilious.input.state.InputFrame;

public interface InputDevice {
    String name();
    boolean isConnected();

    /** Sample hardware into the shared frame (OR into device-local, then merge). */
    void poll(InputFrame out);
}
