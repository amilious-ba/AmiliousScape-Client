package rt4.amilious.input;

import rt4.amilious.debug.DebugConsole;
import rt4.amilious.input.action.Action;

public final class ChatHistory {
    private static final int MAX = 50;
    private static final java.util.ArrayList<String> lines = new java.util.ArrayList<String>();
    private static int cursor = -1; // -1 = live line
    private static String override = null;


    public static void push(String raw) {
        DebugConsole.log("ChatHistory: " + raw);
        String s = stripPrompt(raw);
        if (s == null || s.length() == 0) return;
        if (lines.isEmpty() || !s.equals(lines.get(lines.size() - 1))) {
            lines.add(s);
            if (lines.size() > MAX) lines.remove(0);
        }
        cursor = -1;
    }

    public static String prev() {
        if (lines.isEmpty()) return null;
        if (cursor < 0) cursor = lines.size() - 1;
        else if (cursor > 0) cursor--;
        return lines.get(cursor);
    }

    public static String next() {
        if (cursor < 0) return "";
        cursor++;
        if (cursor >= lines.size()) {
            cursor = -1;
            return "";
        }
        return lines.get(cursor);
    }

    private static String stripPrompt(String raw) {
        String stripped = raw.replaceAll("<[^>]+>", "").trim();
        if (stripped.endsWith("*")) stripped = stripped.substring(0, stripped.length() - 1).trim();
        int colon = stripped.lastIndexOf(':');
        return colon >= 0 ? stripped.substring(colon + 1).trim() : stripped;
    }

    public static void processActions() {
        if (InputManager.getMode() != InputMode.CHAT) {
            return;
        }
        if (InputManager.isActionPressed(Action.CHAT_HISTORY_PREV)) {
            DebugConsole.log("ChatHistory: prev");
            apply(prev());
        }
        if (InputManager.isActionPressed(Action.CHAT_HISTORY_NEXT)) {
            DebugConsole.log("ChatHistory: next");
            apply(next());
        }
    }

    private static final int CHAT_INPUT_ID = 8978483;

    private static void apply(String line) {
        if (line == null) {
            return;
        }
        try {
            rt4.Component c = rt4.InterfaceList.method1418(CHAT_INPUT_ID, -1);
            String current = (c != null && c.text != null) ? c.text.toString() : "";
            override = prefixOf(current) + line + "*";
            if (c != null) {
                c.text = rt4.JagString.parse(override);
                rt4.InterfaceList.redraw(c);
            }
            InputManager.updateChatInputText(rt4.JagString.parse(override));
            System.out.println("[history] override=" + override);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String prefixOf(String raw) {
        String s = raw.replaceAll("<[^>]+>", "");
        int colon = s.lastIndexOf(':');
        if (colon >= 0) {
            return s.substring(0, colon + 1) + " ";
        }
        return "";
    }

    public static boolean hasOverride() {
        return override != null;
    }

    /** CS2 setText / getText for the chat input */
    public static rt4.JagString filterText(int componentId, rt4.JagString incoming) {
        if (override == null || componentId != CHAT_INPUT_ID) {
            return incoming;
        }
        return rt4.JagString.parse(override);
    }

    public static void clearOverride() {
        override = null;
    }

}