package rt4.amilious.dialouge;

import rt4.*;
import rt4.amilious.voice.Voiceover;

/**
 * Chathead dialogues: iface 243 and 65 (left head).
 * Continue: prefer space key (83) bind on a component; fallback press space one frame.
 */
public final class ChatHeadReader {

    private static final int IFACE_A = 243;
    private static final int IFACE_B = 65;
    private static final int KEY_SPACE = 83; // Keyboard CODE_MAP[VK_SPACE]

    private static final String CONTINUE_TEXT = "Click here to continue";
    private static final String PLACEHOLDER_NAME = "Name";

    private static boolean dirty;
    private static int speakingInterfaceId = -1;

    private static String speaker;
    private static String spokenText;
    private static int continueId = -1;
    private static int continueChild = -1;
    /** Component that has space in aByteArray8 (real continue target). */
    private static int spaceBindComponentId = -1;
    private static int spaceBindOpIndex = 1; // i+1 passed to method4512

    private static String lastSpoken;

    private static boolean pendingAutoContinue;
    private static boolean spaceDown;
    private static int spaceClearTicks;

    private static final StringBuilder sb = new StringBuilder();

    private ChatHeadReader() {
    }

    public static void onInterfaceOpen(int interfaceId) {
        if (interfaceId == IFACE_A || interfaceId == IFACE_B) {
            speakingInterfaceId = interfaceId;
            dirty = true;
            spaceBindComponentId = -1;
            System.out.println("[dlg] open iface=" + interfaceId);
            dumpKeyBinds(interfaceId);
        }
    }

    public static void onInterfaceButton(JagString option, int child, int button, int componentId) {
        if (continueId != -1 && componentId == continueId
                || spaceBindComponentId != -1 && componentId == spaceBindComponentId) {
            Voiceover.stop();
            lastSpoken = null;
            dirty = true;
            pendingAutoContinue = false;
        }
    }

    public static void tick() {
        // Clear synthetic space after 1–2 frames
        if (spaceDown) {
            spaceClearTicks--;
            if (spaceClearTicks <= 0) {
                Keyboard.pressedKeys[KEY_SPACE] = false;
                spaceDown = false;
                System.out.println("[dlg] space released");
            }
        }

        if (pendingAutoContinue) {
            pendingAutoContinue = false;
            doContinue();
        }

        if (!dirty) {
            return;
        }
        dirty = false;

        if (speakingInterfaceId < 0) {
            return;
        }

        getContents(speakingInterfaceId);
        findSpaceBind(speakingInterfaceId);

        if (speaker == null || speaker.isEmpty() || PLACEHOLDER_NAME.equals(speaker)) {
            dirty = true;
            return;
        }
        if (spokenText == null || spokenText.isEmpty()) {
            return;
        }

        String key = speakingInterfaceId + "|" + speaker + "|" + spokenText;
        if (key.equals(lastSpoken)) {
            return;
        }
        lastSpoken = key;

        System.out.println("[dlg-speak] iface=" + speakingInterfaceId
                + " speaker=" + speaker + " text=" + spokenText);

        Voiceover.speak(speaker, spokenText, new Runnable() {
            @Override
            public void run() {
                pendingAutoContinue = true;
            }
        });
    }

    public static void onInterfaceClose(int interfaceId) {
        if (interfaceId != IFACE_A && interfaceId != IFACE_B) {
            return;
        }
        if (speakingInterfaceId != -1 && speakingInterfaceId != interfaceId) {
            return; // different chathead still open
        }
        clearDialogueState(); // Voiceover.stop + clear pending/cache
    }

    private static void clearDialogueState() {
        Voiceover.stop();
        pendingAutoContinue = false;
        dirty = false;
        speakingInterfaceId = -1;
        speaker = null;
        spokenText = null;
        continueId = -1;
        continueChild = -1;
        lastSpoken = null;
        spaceBindComponentId = -1;
        if (spaceDown) {
            Keyboard.pressedKeys[KEY_SPACE] = false;
            spaceDown = false;
        }
    }

    private static void getContents(int iface) {
        continueId = -1;
        continueChild = -1;
        speaker = null;
        spokenText = null;
        sb.setLength(0);

        try {
            if (InterfaceList.components == null || iface < 0 || iface >= InterfaceList.components.length) {
                return;
            }
            Component[] list = InterfaceList.components[iface];
            if (list == null) {
                return;
            }

            boolean first = true;
            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null || c.text == null) {
                    continue;
                }
                String t = c.text.toString();
                if (t.isEmpty()) {
                    continue;
                }
                if (first) {
                    first = false;
                    speaker = t;
                    continue;
                }
                if (CONTINUE_TEXT.equals(t)) {
                    continueId = c.id;
                    continueChild = i;
                    continue;
                }
                if (t.startsWith("Line")) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(t.trim());
            }
            spokenText = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Find component with space (83) in aByteArray8 — same as InterfaceList key path. */
    private static void findSpaceBind(int iface) {
        spaceBindComponentId = -1;
        spaceBindOpIndex = 1;
        try {
            Component[] list = InterfaceList.components[iface];
            if (list == null) {
                return;
            }
            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null || c.aByteArray8 == null) {
                    continue;
                }
                for (int k = 0; k < c.aByteArray8.length; k++) {
                    if ((c.aByteArray8[k] & 0xFF) == KEY_SPACE) {
                        spaceBindComponentId = c.id;
                        spaceBindOpIndex = k + 1;
                        System.out.println("[dlg] space bind id=" + c.id
                                + " child=" + i + " opIndex=" + spaceBindOpIndex);
                        return;
                    }
                }
            }
            System.out.println("[dlg] no space bind on iface=" + iface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dumpKeyBinds(int iface) {
        try {
            Component[] list = InterfaceList.components[iface];
            if (list == null) {
                return;
            }
            for (int i = 0; i < list.length; i++) {
                Component c = list[i];
                if (c == null || c.aByteArray8 == null) {
                    continue;
                }
                System.out.print("[dlg-key] iface=" + iface + " child=" + i + " id=" + c.id + " keys=");
                for (int k = 0; k < c.aByteArray8.length; k++) {
                    System.out.print((c.aByteArray8[k] & 0xFF) + " ");
                }
                System.out.println(" text=" + (c.text != null ? c.text : ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prefer method4512 on space-bound component; else hold space 2 frames
     * so InterfaceList.method946 does the same thing as a real keypress.
     */
    private static void doContinue() {
        Component c = getContinueComponent(continueId, continueChild);
        if (c == null && continueId != -1) {
            // still try with known id
            System.out.println("[dlg] resume-pause method10 id=" + continueId);
            MiniMenu.method10(0, continueId); // createdComponentId often 0 / -1
            return;
        }
        if (c == null) {
            System.out.println("[dlg] no continue component");
            return;
        }
        int created = c.createdComponentId; // often -1 for non-inv
        if (created < 0) {
            created = 0;
        }
        System.out.println("[dlg] resume-pause method10 created=" + created + " id=" + c.id);
        MiniMenu.method10(created, c.id);
    }

    private static Component getContinueComponent(int componentId, int child) {
        try {
            // Prefer child index on the open dialogue iface (243 or 65)
            int iface = speakingInterfaceId;
            if (iface < 0 && componentId > 0) {
                iface = componentId >>> 16;
            }
            if (InterfaceList.components != null
                    && iface >= 0
                    && iface < InterfaceList.components.length
                    && InterfaceList.components[iface] != null
                    && child >= 0
                    && child < InterfaceList.components[iface].length) {
                Component c = InterfaceList.components[iface][child];
                if (c != null) {
                    return c;
                }
            }
            // Fallback: packed id
            if (componentId != -1) {
                return InterfaceList.method1418(componentId, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void reset() {
        clearDialogueState();
    }
}