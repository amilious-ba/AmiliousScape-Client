package rt4.amilious;

import rt4.Camera;
import rt4.Preferences;
import rt4.amilious.input.InputMode;
import rt4.amilious.input.InputManager;
import rt4.amilious.input.action.Action;

/**
 * World/chat camera rotate. Map pan stays in MapController.
 * Call after InputManager.tick() from AmiliousClient.update().
 */
public final class CameraController {

    private static final int PITCH_UP = 47;
    private static final int PITCH_DOWN = 17;
    private static final int YAW_LEFT = 65;
    private static final int YAW_RIGHT = 191;

    private CameraController() {
    }

    public static void processActions() {
        if (!Preferences.aBoolean63) {
            return;
        }

        InputMode mode = InputManager.getMode();
        if (mode != InputMode.WORLD && mode != InputMode.CHAT) {
            return;
        }

        boolean changed = false;
        if (InputManager.isActionDown(Action.CAMERA_UP)) {
            Camera.pitchTarget += PITCH_UP;
            changed = true;
        }
        if (InputManager.isActionDown(Action.CAMERA_DOWN)) {
            Camera.pitchTarget -= PITCH_DOWN;
            changed = true;
        }
        if (InputManager.isActionDown(Action.CAMERA_LEFT)) {
            Camera.yawTarget -= YAW_LEFT;
            changed = true;
        }
        if (InputManager.isActionDown(Action.CAMERA_RIGHT)) {
            Camera.yawTarget += YAW_RIGHT;
            changed = true;
        }

        if (changed) {
            Camera.clampCameraAngle();
        }
    }
}