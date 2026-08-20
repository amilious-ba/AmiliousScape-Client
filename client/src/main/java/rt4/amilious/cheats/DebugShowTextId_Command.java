package rt4.amilious.cheats;

import rt4.Chat;
import rt4.JagString;
import rt4.amilious.ChatController;

public class DebugShowTextId_Command implements ICommand {

    private static final String command = "::debugshowtextid";

    @Override
    public boolean compare(String s) {
        return s.equalsIgnoreCase(command);
    }

    @Override
    public boolean execute(String s) {
        ChatController.debugShowTextId = !ChatController.debugShowTextId;
        Chat.add(null, 0, JagString.parse("Debug show text ID: " + (ChatController.debugShowTextId ? "enabled" : "disabled")));
        return true;
    }
}
