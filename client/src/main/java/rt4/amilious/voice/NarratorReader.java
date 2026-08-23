package rt4.amilious.voice;

import rt4.*;

/**
 * Non-chathead dialogue: item boxes, message boxes, etc.
 * Speaks as Narrator. Yields to ChatHeadReader.
 */
public final class NarratorReader {

    /** 530 object / message interfaces (no chathead). */
    private static final int[] NARRATOR_IFACES = {
            131,  // doubleobjbox
            389,  // objdialog
            519,  // objbox
            760,  // tutorial2_objbox
            210, 211, 212, 213, 214,           // message1–5
            215, 216, 217, 218, 219,           // message_np1–5
            173,  // chatlarge
            757   // npcchatlarge
    };

    private static final String CONTINUE_TEXT = "Click here to continue";
    private static final String SPEAKER = "Narrator";

    private static int activeIface = -1;
    private static String lastSpoken;
    private static boolean pendingAutoContinue;
    private static int continueId = -1;
    private static int continueChild = -1;

    private NarratorReader() {
    }

    public static boolean isActive() {
        return activeIface >= 0;
    }

    private static boolean isNarratorIface(int iface) {
        for (int id : NARRATOR_IFACES) {
            if (id == iface) {
                return true;
            }
        }
        return false;
    }

    public static void onInterfaceOpen(int interfaceId) {
        if (!isNarratorIface(interfaceId)) {
            return;
        }
        activeIface = interfaceId;
        lastSpoken = null;
        continueId = -1;
        continueChild = -1;
        System.out.println("[narrator] open iface=" + interfaceId);
    }

    public static void onInterfaceClose(int interfaceId) {
        if (activeIface == interfaceId) {
            clear();
        }
    }

    public static void tick() {
        if (pendingAutoContinue) {
            pendingAutoContinue = false;
            doContinue();
        }

        if (activeIface < 0) {
            return;
        }
        // Chathead owns the mic
        /*if (ChatHeadReader.isActive()) {
            return;
        }*/

        String text = readBody(activeIface);
        if (text == null || text.isEmpty() || text.equals(lastSpoken)) {
            return;
        }
        lastSpoken = text;

        System.out.println("[narrator-speak] iface=" + activeIface + " text=" + text);

        final boolean canContinue = continueId != -1;
        Voiceover.speak(SPEAKER, text, new Runnable() {
            @Override
            public void run() {
                if (canContinue) {
                    pendingAutoContinue = true;
                }
            }
        });
    }

    private static String readBody(int iface) {
        continueId = -1;
        continueChild = -1;
        StringBuilder sb = new StringBuilder();
        try {
            if (InterfaceList.components == null
                    || iface < 0
                    || iface >= InterfaceList.components.length) {
                return null;
            }
            Component[] list = InterfaceList.components[iface];
            if (list == null) {
                return null;
            }
            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null || c.text == null) {
                    continue;
                }
                String t = c.text.toString().trim();
                if (t.isEmpty() || t.startsWith("Line")) {
                    continue;
                }
                if (CONTINUE_TEXT.equals(t)) {
                    continueId = c.id;
                    continueChild = i;
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static void doContinue() {
        // same MiniMenu.method10 path as ChatHeadReader
        if (continueId == -1) {
            return;
        }
        try {
            MiniMenu.method10(0, continueId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clear() {
        Voiceover.stop();
        activeIface = -1;
        lastSpoken = null;
        pendingAutoContinue = false;
        continueId = -1;
        continueChild = -1;
    }

    public static void reset() {
        clear();
    }
}