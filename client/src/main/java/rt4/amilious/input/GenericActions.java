package rt4.amilious.input;

import rt4.*;
import rt4.amilious.MapController;
import rt4.amilious.input.action.Action;
import rt4.amilious.menutab.MenuTabCycle;
import rt4.amilious.modal.ModalTools;

public final class GenericActions {

    /** Called from InputManager.tick() after mapper.update(). */
    public static void processActions() {
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

}