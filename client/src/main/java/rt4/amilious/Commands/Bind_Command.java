package rt4.amilious.Commands;

//#region Imports ######################################################################################################

import rt4.amilious.debug.DebugConsole;
import rt4.amilious.input.InputManager;
import rt4.amilious.input.action.Action;
import rt4.amilious.Commands.binding.CommandBinds;

//#endregion ###########################################################################################################

/**
 * ::bind <slot> <command>   e.g. ::bind 0 ::bank
 * ::bind <slot>             show binding
 * ::unbind <slot>
 * ::binds
 */
public final class Bind_Command implements ICommand {

    //#region Override Methods #########################################################################################

    @Override
    public boolean compare(String s) {
        String lower = s.toLowerCase();
        return lower.equals("::bind")
                || lower.startsWith("::bind ")
                || lower.equals("::unbind")
                || lower.startsWith("::unbind ")
                || lower.equals("::binds");
    }

    @Override
    public boolean execute(String s) {
        String lower = s.toLowerCase().trim();

        if (lower.equals("::binds")) {
            list();
            return true;
        }

        if (lower.equals("::bind") || lower.equals("::unbind")) {
            usage();
            return true;
        }

        if (lower.startsWith("::unbind ")) {
            String rest = s.substring("::unbind ".length()).trim();
            Integer slot = parseSlot(rest);
            if (slot == null) return true;
            CommandBinds.clear(slot);
            CommandBinds.chat("Unbound slot " + slot);
            return true;
        }

        // ::bind <slot> [command...]
        String rest = s.substring("::bind ".length()).trim();
        if (rest.isEmpty()) {
            usage();
            return true;
        }

        String[] parts = rest.split("\\s+", 2);
        Integer slot = parseSlot(parts[0]);
        if (slot == null) return true;

        if (parts.length == 1) {
            String cmd = CommandBinds.get(slot);
            CommandBinds.chat("Slot " + slot + ": " + (cmd == null ? "(empty)" : cmd));
            return true;
        }

        String command = parts[1].trim();
        if (!command.startsWith("::")) {
            command = "::" + command;
        }
        CommandBinds.set(slot, command);
        CommandBinds.chat("Bound slot " + slot + " → " + command);
        DebugConsole.log("bind " + slot + " = " + command);
        return true;
    }

    @Override
    public void processActions() {
        if (!InputManager.shouldAllowWorldBinds()) {return;}
        //process input
        if (InputManager.isActionPressed(Action.HOTKEY_0)) CommandBinds.run(0);
        else if (InputManager.isActionPressed(Action.HOTKEY_1)) CommandBinds.run(1);
        else if (InputManager.isActionPressed(Action.HOTKEY_2)) CommandBinds.run(2);
        else if (InputManager.isActionPressed(Action.HOTKEY_3)) CommandBinds.run(3);
        else if (InputManager.isActionPressed(Action.HOTKEY_4)) CommandBinds.run(4);
        else if (InputManager.isActionPressed(Action.HOTKEY_5)) CommandBinds.run(5);
        else if (InputManager.isActionPressed(Action.HOTKEY_6)) CommandBinds.run(6);
        else if (InputManager.isActionPressed(Action.HOTKEY_7)) CommandBinds.run(7);
        else if (InputManager.isActionPressed(Action.HOTKEY_8)) CommandBinds.run(8);
        else if (InputManager.isActionPressed(Action.HOTKEY_9)) CommandBinds.run(9);
    }

    @Override
    public void init(){
        CommandBinds.load();
    }

    //#endregion #######################################################################################################


    //#region Private Methods ##########################################################################################

    /**
     * Parses a string representing a slot number. If the string is a valid integer within the
     * allowable range of slots, the integer is returned. Otherwise, error messages are displayed
     * to the user, and null is returned.
     *
     * @param raw the string input to be parsed as a slot number
     * @return the parsed slot number if valid, or null if the input is invalid or out of range
     */
    private static Integer parseSlot(String raw) {
        try {
            int slot = Integer.parseInt(raw.trim());
            if (slot < 0 || slot >= CommandBinds.MAX_SLOTS) {
                CommandBinds.chat("Slot must be 0–" + (CommandBinds.MAX_SLOTS - 1));
                return null;
            }
            return slot;
        } catch (NumberFormatException e) {
            CommandBinds.chat("Invalid slot number.");
            return null;
        }
    }


    /**
     * Displays a list of all currently set command binds.
     *
     * Iterates through all available command bind slots, defined by the constant
     * {@code CommandBinds.MAX_SLOTS}, and retrieves the associated command using
     * {@code CommandBinds.get(int)}. If a command is found in a slot, it is
     * displayed in chat using {@code CommandBinds.chat(String)}. If no commands
     * are set, a message indicating that no binds are set is displayed.
     */
    private static void list() {
        boolean any = false;
        for (int i = 0; i < CommandBinds.MAX_SLOTS; i++) {
            String cmd = CommandBinds.get(i);
            if (cmd != null) {
                CommandBinds.chat("  " + i + " → " + cmd);
                any = true;
            }
        }
        if (!any) {
            CommandBinds.chat("No binds set.");
        }
    }


    /**
     * Displays instructions for using command binding functionality in the chat.
     *
     * This method provides usage information for the following commands:
     * - `::bind <slot> <command>`: Binds a command to a specific slot.
     * - `::bind <slot>`: Displays the command currently bound to the given slot.
     * - `::unbind <slot>`: Clears the command binding for the specified slot.
     * - `::binds`: Lists all currently set command bindings.
     *
     * The usage details are printed to the chat using the {@code CommandBinds.chat(String)} method.
     */
    private static void usage() {
        CommandBinds.chat("Usage: ::bind <slot> <command>   e.g. ::bind 0 ::bank");
        CommandBinds.chat("       ::bind <slot>             show binding");
        CommandBinds.chat("       ::unbind <slot>           clear");
        CommandBinds.chat("       ::binds                   list all");
    }

    //#endregion #######################################################################################################

}