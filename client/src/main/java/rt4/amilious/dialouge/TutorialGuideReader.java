package rt4.amilious.dialouge;

import rt4.amilious.DebugConsole;
import rt4.amilious.voice.Voiceover;

public final class TutorialGuideReader {

    private static final int IFACE = 372;
    private static final int MAX_CHILD = 8;

    private static final String[] lines = new String[MAX_CHILD];
    private static boolean dirty;
    private static String lastSpoken;

    public static void onSetText(int packetId,int iFaceId, int childId, String text) {
        if(iFaceId != IFACE) return;
        if (childId < 0 || childId >= MAX_CHILD) return;
        lines[childId] = text != null ? text : "";
        dirty = true;
    }

    /** Call from AmiliousClient.update() once per frame (in-game). */
    public static void tick() {

        if (!dirty) return;
        dirty = false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_CHILD; i++) {
            if (lines[i] == null || lines[i].isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(lines[i].trim());
        }
        String full = sb.toString();
        if (full.isEmpty() || full.equals(lastSpoken)) return;
        lastSpoken = full;

        // Voiceover.speak(full);
        Voiceover.speak(full);
    }
}