package rt4.amilious.cheats;

import rt4.social.Chat;
import rt4.util.JagString;
import rt4.amilious.DebugConsole;

public final class Debug_Command implements ICommand {

    private static final String command1 = "::debug";
    private static final String command2 = "::debugconsole";

    @Override
    public boolean compare(String s) {
        if(s.equalsIgnoreCase(command1)) return true;
        return s.equalsIgnoreCase(command2);
    }

    @Override
    public boolean execute(String s) {
        DebugConsole.toggle();
        Chat.add(null, 0, JagString.parse("Debug console: " + (DebugConsole.enabled ? "on" : "off")));
        return true;
    }
}
