package rt4.amilious.voice;

import rt4.Component;
import rt4.InterfaceList;
import rt4.amilious.DebugConsole;

public final class TutorialGuideReader {

    private static final int[] TUTORIAL_TEXT_IFACES = { 372, 421 /*, 769 */ };
    private static final int MAX_CHILD = 16;

    private static final String[] lines = new String[MAX_CHILD];
    private static int activeIface = -1;
    private static boolean dirty;
    private static String lastSpoken;

    public static void onSetText(int packetId, int iFaceId, int childId, String text) {
        if (!isTutorialTextIFace(iFaceId)) {
            return;
        }
        if (childId < 0 || childId >= MAX_CHILD) {
            return;
        }
        // New tutorial panel → drop old lines
        if (activeIface != iFaceId) {
            clearLines();
            activeIface = iFaceId;
            lastSpoken = null;
        }
        lines[childId] = text != null ? text : "";
        dirty = true;
    }



    private static boolean isTutorialTextIFace(int iface) {
        for (int id : TUTORIAL_TEXT_IFACES) {
            if (id == iface) {
                return true;
            }
        }
        return false;
    }

    private static void clearLines() {
        for (int i = 0; i < MAX_CHILD; i++) {
            lines[i] = null;
        }
    }

    /** Call from AmiliousClient.update() once per frame (in-game). */
    public static void tick() {
        if (!dirty) {
            return;
        }
        // Yield to dialogue / active TTS
        if (ChatHeadReader.isActive()
                || NarratorReader.isActive()
                || Voiceover.isSpeaking()) {
            return; // keep dirty so we speak after they finish
        }
        dirty = false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_CHILD; i++) {
            if (lines[i] == null || lines[i].isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(lines[i].trim());
        }
        String full = sb.toString();
        if (full.isEmpty() || full.equals(lastSpoken)) {
            return;
        }
        lastSpoken = full;

        Voiceover.speak("Narrator", full, null);
    }

    public static void reset() {
        clearLines();
        activeIface = -1;
        dirty = false;
        lastSpoken = null;
        isFixed = false;
    }

    public static void onInterfaceOpen(int interfaceId) {

    }

    private static boolean isFixed = false;

    public static void fixTutorialProgressPosition() {
        if (isFixed) {
            return;
        }
        try {
            if (InterfaceList.components == null
                    || 371 >= InterfaceList.components.length) {
                return;
            }
            Component[] list = InterfaceList.components[371];
            if (list == null) {
                return;
            }

            final int NUDGE_UP = 150; // tune once you see the bar + labels

            int moved = 0;
            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null) {
                    continue;
                }
                c.y -= NUDGE_UP;
                //c.baseY -= NUDGE_UP;
                InterfaceList.redraw(c);
                moved++;
            }



            DebugConsole.log("Fixed tutorial progress: moved " + moved
                    + " children by -" + NUDGE_UP);
            isFixed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}