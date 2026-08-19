package rt4.amilious;

import rt4.core.GameShell;
import rt4.core.client;
import rt4.network.ClientProt;
import rt4.util.JagString;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class InputController {

    private static final KeyAdapter LISTENER = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_END) TouchKeyboard.show(true);
            if (client.gameState != 30) {return; }

            // if (shouldIgnoreHotkeys()) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_PAGE_UP:
                    if (MapController.isOpen()) MapController.zoomOut();
                    else MenuTabCycle.previous();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    if (MapController.isOpen()) MapController.zoomIn();
                    else MenuTabCycle.next();
                    break;
                case KeyEvent.VK_F9:
                    CommandBinds.run(0);
                    break;
                case KeyEvent.VK_F10:
                    CommandBinds.run(1);
                    break;
                case KeyEvent.VK_F11:
                    CommandBinds.run(2);
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
                case KeyEvent.VK_ESCAPE:
                    if (MapController.isOpen()) //close map
                        MapController.close();
                    else if(ModalTools.hasModalOpen()) { //close modal
                        ModalTools.closeOpenModalNextUpdate();
                        break;
                    }else //open the close game window
                        ClientProt.method4512(JagString.EMPTY, -1, 1, 48889868);
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