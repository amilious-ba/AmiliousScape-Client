package rt4.amilious;

import rt4.*;
import rt4.amilious.input.InputManager;
import rt4.amilious.input.action.Action;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class InputController {

    private static final KeyAdapter LISTENER = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_END) TouchKeyboard.show(true);
            if (client.gameState != 30) {return; }

            // Non-gameplay system keys (work in all modes)
            switch (e.getKeyCode()) {
                case KeyEvent.VK_PAGE_UP:
                    if (MapController.isOpen()) MapController.zoomOut();
                    else MenuTabCycle.previous();
                    return;
                case KeyEvent.VK_PAGE_DOWN:
                    if (MapController.isOpen()) MapController.zoomIn();
                    else MenuTabCycle.next();
            }
        }
    };

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


    public static void pollSystemActions() {
        if (client.gameState != 30) return;

        if (InputManager.isActionPressed(Action.TOGGLE_MAP)) {
            MapController.toggle();
            return;
        }

        if (InputManager.isActionPressed(Action.TOGGLE_RUN)) {
            RunToggler.toggle();
            return;
        }

        if (InputManager.isActionPressed(Action.ESCAPE)) {
            if (MapController.isOpen()) {
                MapController.close();
            } else if (ModalTools.hasModalOpen()) {
                ModalTools.closeOpenModalNextUpdate();
            } else if (InputManager.isChatMode()) {
                InputManager.enterWorldMode(); // or leave to processChatArming
            } else {
                ClientProt.method4512(JagString.EMPTY, -1, 1, 48889868);
            }
        }
    }


    public static void register() {
        if (GameShell.canvas == null) {
            return;
        }
        GameShell.canvas.removeKeyListener(LISTENER);
        GameShell.canvas.addKeyListener(LISTENER);
    }
}