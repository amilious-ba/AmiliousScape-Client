package rt4.amilious.Commands;

import rt4.amilious.DebugConsole;

public class DebugLog_Command implements ICommand {

    private static final String command = "::debugLog";

    @Override
    public boolean compare(String s) {
        return s.toLowerCase().startsWith(command.toLowerCase());
    }

    @Override
    public boolean execute(String s) {
        if (s.equalsIgnoreCase("::debuglog"))
            DebugConsole.log("(no message)");
        else
            DebugConsole.log(s.substring("::debuglog ".length()));
        return false;
    }
}
