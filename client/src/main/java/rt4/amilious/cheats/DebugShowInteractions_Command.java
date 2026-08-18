package rt4.amilious.cheats;

import rt4.Chat;
import rt4.JagString;
import rt4.amilious.DebugConsole;

public class DebugShowInteractions_Command implements ICommand {

    private static final String command = "::debugShowInteractions";

    @Override
    public boolean compare(String s) {
        return s.equalsIgnoreCase(command);
    }

    @Override
    public boolean execute(String s) {
        var show = DebugConsole.showInteractions = !DebugConsole.showInteractions;
        Chat.add(null, 0, JagString.parse("Debug console: " + (show ? "showing interactions" : "not showing interactions")));
        return true;
    }
}
