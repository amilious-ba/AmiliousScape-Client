package rt4.amilious.input;

import rt4.amilious.debug.DebugConsole;
import rt4.amilious.input.action.Action;

public final class ChatHistory {

    private static boolean prevHistPrev;
    private static boolean prevHistNext;
    private static final int MAX = 50;
    private static final java.util.ArrayList<String> lines = new java.util.ArrayList<String>();
    private static int cursor = -1; // -1 = live line
    private static String pendingInject = null;


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
            prevHistPrev = false;
            prevHistNext = false;
            return;
        }
        boolean up = InputManager.isActionDown(Action.CHAT_HISTORY_PREV);
        boolean down = InputManager.isActionDown(Action.CHAT_HISTORY_NEXT);
        if (up && !prevHistPrev) {
            apply(prev());
        }
        if (down && !prevHistNext) {
            apply(next());
        }
        prevHistPrev = up;
        prevHistNext = down;
    }

    private static final int CHAT_INPUT_ID = 8978483;

    private static void apply(String line) {
        if (line == null || line.length() == 0) {
            return;
        }
        pendingInject = line;
        System.out.println("[history] inject " + line);
    }

    public static String takePendingInject() {
        String s = pendingInject;
        pendingInject = null;
        return s;
    }

    public static int currentTypedLength() {
        try {
            rt4.Component c = rt4.InterfaceList.method1418(8978483, -1);
            if (c == null || c.text == null) {
                return 0;
            }
            String s = stripPrompt(c.text.toString());
            return s == null ? 0 : s.length();
        } catch (Exception e) {
            return 0;
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


}