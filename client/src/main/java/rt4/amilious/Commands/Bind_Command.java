package rt4.amilious.Commands;

import rt4.amilious.CommandBinds;
import rt4.amilious.debug.DebugConsole;

/**
 * ::bind <slot> <command>   e.g. ::bind 0 ::bank
 * ::bind <slot>             show binding
 * ::unbind <slot>
 * ::binds
 */
public final class Bind_Command implements ICommand {

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

    private static void usage() {
        CommandBinds.chat("Usage: ::bind <slot> <command>   e.g. ::bind 0 ::bank");
        CommandBinds.chat("       ::bind <slot>             show binding");
        CommandBinds.chat("       ::unbind <slot>           clear");
        CommandBinds.chat("       ::binds                   list all");
    }
}