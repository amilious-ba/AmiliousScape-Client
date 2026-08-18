package rt4.amilious;

import rt4.ClientProt;
import rt4.GameShell;
import rt4.JagString;
import rt4.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class InputController {

    private static final KeyAdapter LISTENER = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (client.gameState != 30) {return; }

            // if (shouldIgnoreHotkeys()) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_PAGE_UP:
                    if (MapController.isOpen()) return;
                    MenuTabCycle.previous();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    if (MapController.isOpen()) return;
                    MenuTabCycle.next();
                    break;
                case KeyEvent.VK_F12:
                    MenuTab current = MenuTabCycle.lastSelected();
                    if (current == null) {
                        current = MenuTab.COMBAT; // or skip
                    }
                    current.select();
                    break;
                case KeyEvent.VK_INSERT:
                    MapController.toggle();
                    break;
                case KeyEvent.VK_HOME:
                    RunToggler.toggle();
                    break;
                case KeyEvent.VK_END:
                    TouchKeyboard.show(true);
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (MapController.isOpen()) {
                        MapController.close();
                    } else {
                        ClientProt.method4512(JagString.EMPTY, -1, 1, 48889868);
                    }
                    break;
            }
        }
    };

    public static void register() {
        if (GameShell.canvas == null) {
            return;
        }
        GameShell.canvas.removeKeyListener(LISTENER);
        GameShell.canvas.addKeyListener(LISTENER);
    }
}