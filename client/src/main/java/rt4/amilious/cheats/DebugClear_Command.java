package rt4.amilious.cheats;

import rt4.Chat;
import rt4.JagString;
import rt4.amilious.DebugConsole;

public class DebugClear_Command implements ICommand {

    private static final String command = "::debugClear";

    @Override
    public boolean compare(String s) {
        return s.equalsIgnoreCase(command);
    }

    @Override
    public boolean execute(String s) {
        DebugConsole.clear();
        Chat.add(null, 0, JagString.parse("Debug console cleared"));
        return true;
    }
}
