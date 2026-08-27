package rt4.amilious;

import rt4.*;
import rt4.amilious.input.*;
import rt4.amilious.input.action.Action;
import rt4.amilious.menutab.MenuTabCycle;

public final class InputController {

    /**
     * Poll command binds using Actions. Call this from Protocol or game loop.
     * MUST be called after InputManager.tick() so Actions are up to date.
     */
    public static void pollCommandBinds() {
        if (client.gameState != 30) {
            return;
        }

        // Command binds ONLY work in WORLD mode - never steal digits in text modes
        if (!InputManager.shouldAllowWorldBinds()) {
            return;
        }

        // Use isActionPressed (edge-trigger) to avoid repeat firing every frame
        if (InputManager.isActionPressed(Action.HOTKEY_0)) {
            CommandBinds.run(0);
        } else if (InputManager.isActionPressed(Action.HOTKEY_1)) {
            CommandBinds.run(1);
        } else if (InputManager.isActionPressed(Action.HOTKEY_2)) {
            CommandBinds.run(2);
        } else if (InputManager.isActionPressed(Action.HOTKEY_3)) {
            CommandBinds.run(3);
        } else if (InputManager.isActionPressed(Action.HOTKEY_4)) {
            CommandBinds.run(4);
        } else if (InputManager.isActionPressed(Action.HOTKEY_5)) {
            CommandBinds.run(5);
        } else if (InputManager.isActionPressed(Action.HOTKEY_6)) {
            CommandBinds.run(6);
        } else if (InputManager.isActionPressed(Action.HOTKEY_7)) {
            CommandBinds.run(7);
        } else if (InputManager.isActionPressed(Action.HOTKEY_8)) {
            CommandBinds.run(8);
        } else if (InputManager.isActionPressed(Action.HOTKEY_9)) {
            CommandBinds.run(9);
        }
    }


    /** Called from InputManager.tick() after mapper.update(). */
    public static void pollSystemActions() {
        // Touch keyboard can work on login too
        var check = InputManager.getMode() == InputMode.MINI_MENU || InputManager.getMode() == InputMode.DIALOGUE;
        if (!check &&
                InputManager.isActionPressed(Action.TOUCH_KEYBOARD)) {
            TouchKeyboard.show(true);
        }

        if(LoginManager.staffModLevel > 0  && InputManager.isActionPressed(Action.CHEAT_TELEPORT)) {
            GamepadMouseController.syntheticLeftClick();
        }

        if (client.gameState != 30) {
            return;
        }

        if (InputManager.isActionPressed(Action.TOGGLE_MAP)) {
            MapController.toggle();
            return;
        }

        if (InputManager.isActionPressed(Action.TOGGLE_RUN)) {
            RunToggler.toggle();
            return;
        }

        // Map zoom (MAP mode binds)
        if (InputManager.isActionPressed(Action.MAP_ZOOM_OUT)) {
            MapController.zoomOut();
            return;
        }
        if (InputManager.isActionPressed(Action.MAP_ZOOM_IN)) {
            MapController.zoomIn();
            return;
        }

        // Page Up / LB / wheel — dual: zoom on map, else tab cycle
        if (InputManager.isActionPressed(Action.TAB_PREV) || wheelPrev()) {
            if (MapController.isOpen()) {
                MapController.zoomOut();
            } else {
                MenuTabCycle.previous();
            }
            return;
        }
        if (InputManager.isActionPressed(Action.TAB_NEXT) || wheelNext()) {
            if (MapController.isOpen()) {
                MapController.zoomIn();
            } else {
                MenuTabCycle.next();
            }
            return;
        }

        if (InputManager.isActionPressed(Action.ESCAPE)) {
            if (MapController.isOpen()) {
                MapController.close();
            } else if (ModalTools.hasModalOpen()) {
                ModalTools.closeOpenModalNextUpdate();
            } else if (InputManager.isChatMode()) {
                InputManager.enterWorldMode();
            } else {
                ClientProt.method4512(JagString.EMPTY, -1, 1, 48889868);
            }
        }
    }

    private static boolean wheelPrev() {
        return InputManager.getMouseWheelDelta() > 0;
    }

    private static boolean wheelNext() {
        return InputManager.getMouseWheelDelta() < 0;
    }

    public static void register() {  }
}