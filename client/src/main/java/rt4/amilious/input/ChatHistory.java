package rt4.amilious.input;

public final class ChatHistory {
    private static final int MAX = 50;
    private static final java.util.ArrayList<String> lines = new java.util.ArrayList<String>();
    private static int cursor = -1; // -1 = live line

    public static void push(String raw) {
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
}