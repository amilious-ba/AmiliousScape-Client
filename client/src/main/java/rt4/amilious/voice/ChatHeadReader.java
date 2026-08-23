package rt4.amilious.voice;

import rt4.*;

/**
 * Chathead dialogues (530 official iface names).
 *
 * Player:  chat1–4 (64–67), chat_np1–4 (68–71)
 * NPC:     npcchat1–4 (241–244), npcchat_np1–4 (245–248)
 *
 * Multi-line: re-read open iface every tick; speak when speaker|text changes.
 * Continue: MiniMenu.method10 when "Click here to continue" is present.
 */
public final class ChatHeadReader {

    private static final int KEY_SPACE = 83; // Keyboard CODE_MAP[VK_SPACE]

    /** 530 chathead interfaces only — no heuristic discovery. */
    private static final int[] CHATHEAD_IFACES = {
            // player continue
            64, 65, 66, 67,
            // player no-continue
            68, 69, 70, 71,
            // npc continue
            241, 242, 243, 244,
            // npc no-continue
            245, 246, 247, 248
    };

    private static final String CONTINUE_TEXT = "Click here to continue";
    private static final String PLACEHOLDER_NAME = "Name";

    private static int speakingInterfaceId = -1;

    private static String speaker;
    private static String spokenText;
    private static int continueId = -1;
    private static int continueChild = -1;
    private static int spaceBindComponentId = -1;
    private static int spaceBindOpIndex = 1;

    private static String lastSpoken;

    private static boolean pendingAutoContinue;
    private static boolean spaceDown;
    private static int spaceClearTicks;

    private static final StringBuilder sb = new StringBuilder();

    private ChatHeadReader() {
    }

    private static boolean isChatheadDialogue(int iface) {
        for (int id : CHATHEAD_IFACES) {
            if (id == iface) {
                return true;
            }
        }
        return false;
    }

    public static void onInterfaceOpen(int interfaceId) {
        if (!isChatheadDialogue(interfaceId)) {
            return;
        }
        speakingInterfaceId = interfaceId;
        spaceBindComponentId = -1;
        lastSpoken = null;
        System.out.println("[dlg] open iface=" + interfaceId);
    }

    public static void onInterfaceButton(JagString option, int child, int button, int componentId) {
        if (speakingInterfaceId < 0) {
            return;
        }
        if (continueId != -1 && componentId == continueId
                || spaceBindComponentId != -1 && componentId == spaceBindComponentId) {
            Voiceover.stop();
            lastSpoken = null;
            pendingAutoContinue = false;
        }
    }

    public static void tick() {
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

        if (speakingInterfaceId < 0) {
            return;
        }

        getContents(speakingInterfaceId);
        if (spaceBindComponentId == -1) {
            findSpaceBind(speakingInterfaceId);
        }

        if (speaker == null || speaker.isEmpty() || PLACEHOLDER_NAME.equals(speaker)) {
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

        final boolean canAutoContinue = continueId != -1 || spaceBindComponentId != -1;
        Voiceover.speak(speaker, spokenText, new Runnable() {
            @Override
            public void run() {
                if (canAutoContinue) {
                    pendingAutoContinue = true;
                }
            }
        });
    }

    public static void onInterfaceClose(int interfaceId) {
        if (speakingInterfaceId < 0 || speakingInterfaceId != interfaceId) {
            return;
        }
        clearDialogueState();
    }

    private static void clearDialogueState() {
        Voiceover.stop();
        pendingAutoContinue = false;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doContinue() {
        Component c = getContinueComponent(continueId, continueChild);
        if (c == null && continueId != -1) {
            System.out.println("[dlg] resume-pause method10 id=" + continueId);
            MiniMenu.method10(0, continueId);
            return;
        }
        if (c == null) {
            System.out.println("[dlg] no continue component");
            return;
        }
        int created = c.createdComponentId;
        if (created < 0) {
            created = 0;
        }
        System.out.println("[dlg] resume-pause method10 created=" + created + " id=" + c.id);
        MiniMenu.method10(created, c.id);
    }

    private static Component getContinueComponent(int componentId, int child) {
        try {
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

    public static boolean isActive() {
        return speakingInterfaceId >= 0;
    }
}