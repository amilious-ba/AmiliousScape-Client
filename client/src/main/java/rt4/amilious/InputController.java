package rt4.amilious;

import rt4.*;
import rt4.amilious.input.InputManager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class InputController {

    private static final KeyAdapter LISTENER = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_END) TouchKeyboard.show(true);
            if (client.gameState != 30) {return; }

            // if (shouldIgnoreHotkeys()) return;

            var skip = ChatController.isFocused() || InputManager.isSpecialModalMode();

            switch (e.getKeyCode()) {
                case KeyEvent.VK_PAGE_UP:
                    if (MapController.isOpen()) MapController.zoomOut();
                    else MenuTabCycle.previous();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    if (MapController.isOpen()) MapController.zoomIn();
                    else MenuTabCycle.next();
                    break;
                case KeyEvent.VK_0:
                    if(!skip) CommandBinds.run(0);
                    break;
                case KeyEvent.VK_1:
                    if(!skip) CommandBinds.run(1);
                    break;
                case KeyEvent.VK_2:
                    if(!skip) CommandBinds.run(2);
                    break;
                case KeyEvent.VK_3:
                    if(!skip) CommandBinds.run(3);
                    break;
                case KeyEvent.VK_4:
                    if(!skip) CommandBinds.run(4);
                    break;
                case KeyEvent.VK_5:
                    if(!skip) CommandBinds.run(5);
                    break;
                case KeyEvent.VK_6:
                    if(!skip) CommandBinds.run(6);
                    break;
                case KeyEvent.VK_7:
                    if(!skip) CommandBinds.run(7);
                    break;
                case KeyEvent.VK_8:
                    if(!skip) CommandBinds.run(8);
                    break;
                case KeyEvent.VK_9:
                    if(!skip) CommandBinds.run(9);
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