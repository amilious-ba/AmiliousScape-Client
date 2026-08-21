package rt4.amilious.cheats;

import rt4.amilious.ChatController;
import rt4.social.Chat;
import rt4.util.JagString;

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
