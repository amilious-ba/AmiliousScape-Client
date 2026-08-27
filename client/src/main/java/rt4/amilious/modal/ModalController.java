package rt4.amilious.modal;

import rt4.InterfaceList;
import rt4.JagString;
import rt4.amilious.input.ChatBoxModalRegistry;
import rt4.amilious.input.InputManager;
import rt4.amilious.input.SpecialModalRegistry;
import rt4.client;

import java.util.HashMap;

/**
 * Over-chat modals (amount, quick chat) and special text modals.
 *
 * Amount open:  RUN_CS2 109 → ChatBoxModalRegistry
 * Amount close: chrome / submit → evaluate()
 * QC open/close: setHidden on QC_ROOT 8978432 → ChatBoxModalRegistry
 * Report Abuse: RUN_CS2 508 + iface 553 → SpecialModalRegistry
 * GE Search: iface 389 → SpecialModalRegistry
 *
 * Always call InputManager.refreshMode() after registry changes so mode
 * updates this frame (not only on the next tick).
 */
public final class ModalController {


    //#region Fields ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int AMOUNT_CS2 = 109;
    public static final int REPORT_ABUSE_CS2 = 508;
    public static final int REPORT_ABUSE_IFACE = 553;
    public static final int GE_SEARCH_IFACE = 389;

    public static final int QC_ROOT = 8978432;
    public static final int QC_TITLE = 8978433;
    public static final int QC_OPEN_CS2 = 1049;
    public static final int QC_CLOSE_BUTTON_CS2 = 1053;
    public static final int QC_CLOSE_ESC_CS2 = 1303;

    private static final HashMap<Integer, Boolean> hiddenById = new HashMap<Integer, Boolean>();

    /** null = no chatbox modal (amount / quickchat) */
    private static String lastKind = null;

    //#endregion ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private ModalController() {
    }

    public static void init() {
        SpecialModalRegistry.registerComponent(ChatBoxModalRegistry.INPUT, "amount");
        SpecialModalRegistry.registerInterface(REPORT_ABUSE_IFACE, "report-abuse");
        SpecialModalRegistry.registerInterface(GE_SEARCH_IFACE, "ge-search");
        resetChromeState();
    }

    /** Call from AmiliousClient.update() before InputManager.tick() when in-game. */
    public static void tick() {
        if (!interfacesReady()) {
            return;
        }
        evaluate();
    }

    public static void onInterfaceOpen(int interfaceId) {
        if (interfaceId == REPORT_ABUSE_IFACE) {
            SpecialModalRegistry.setActiveInterface(REPORT_ABUSE_IFACE);
            InputManager.refreshMode();
        }
        if (interfaceId == GE_SEARCH_IFACE) {
            SpecialModalRegistry.setActiveInterface(GE_SEARCH_IFACE);
            InputManager.refreshMode();
        }
    }    

    /**
     * Protocol RUN_CS2 after scriptArgs built.
     */
    public static void onRun_CS2(int scriptId, JagString argTypes, Object[] scriptArgs) {
        if (rt4.client.gameState != 30) {
            return;
        }

        if (scriptId == AMOUNT_CS2) {
            String arg = "";
            if (scriptArgs != null && scriptArgs.length > 1 && scriptArgs[1] instanceof JagString) {
                arg = ((JagString) scriptArgs[1]).toString();
            }
            System.out.println("[chatbox-modal] amount via RUN_CS2 109 arg=" + arg);
            lastKind = "amount";
            ChatBoxModalRegistry.setActive("amount");
            InputManager.refreshMode();
            return;
        }

        if (scriptId == REPORT_ABUSE_CS2) {
            SpecialModalRegistry.setActiveInterface(REPORT_ABUSE_IFACE);
            System.out.println("[special-modal] report-abuse via RUN_CS2 508 name="
                    + SpecialModalRegistry.getActiveName());
            InputManager.refreshMode();
            return;
        }
    }

    public static void onComponentHiddenChanged(int componentId, boolean hidden) {
        hiddenById.put(componentId, Boolean.valueOf(hidden));
        if (rt4.client.gameState != 30) {
            return;
        }
        if (componentId == ChatBoxModalRegistry.CHROME_A
                || componentId == ChatBoxModalRegistry.CHROME_B) {
            evaluate();
            InputManager.refreshMode();
        }
    }

    public static void onQuickChatShown() {
        if (rt4.client.gameState != 30) {
            return;
        }
        // Amount on top of QC still wins
        if (stillAmount()) {
            return;
        }
        lastKind = "quickchat";
        ChatBoxModalRegistry.setActive("quickchat");
        InputManager.refreshMode();
    }

    public static void onQuickChatHidden() {
        // Force closed this frame — avoid live-title false positive
        hiddenById.put(QC_ROOT, Boolean.TRUE);
        hiddenById.put(QC_TITLE, Boolean.TRUE);

        if ("quickchat".equals(lastKind)) {
            lastKind = null;
        }

        if (stillAmount()) {
            lastKind = "amount";
            ChatBoxModalRegistry.setActive("amount");
        } else {
            ChatBoxModalRegistry.clearActive();
        }

        InputManager.refreshMode();
    }

    private static void evaluate() {
        if (!interfacesReady()) {
            return;
        }

        if (stillAmount()) {
            if (!"amount".equals(lastKind) || !ChatBoxModalRegistry.isActive()) {
                lastKind = "amount";
                ChatBoxModalRegistry.setActive("amount");
            }
            return;
        }

        // Amount chrome closed — clear only if we were in amount
        if ("amount".equals(lastKind)) {
            System.out.println("[chatbox-modal] CLOSED (was amount)");
            lastKind = null;
            if (ChatBoxModalRegistry.isActive()
                    && "amount".equals(ChatBoxModalRegistry.getActiveName())) {
                ChatBoxModalRegistry.clearActive();
            }
            // QC is owned by 1049/1053/1303 — do not clear quickchat here
        }
    }

    /** Amount chrome both visible, or CS2 amount not finished closing. */
    private static boolean stillAmount() {
        if (isShown(ChatBoxModalRegistry.CHROME_A) && isShown(ChatBoxModalRegistry.CHROME_B)) {
            return true;
        }
        if ("amount".equals(lastKind) && ChatBoxModalRegistry.isActive()) {
            Boolean a = hiddenById.get(ChatBoxModalRegistry.CHROME_A);
            Boolean b = hiddenById.get(ChatBoxModalRegistry.CHROME_B);
            boolean chromeSeen = a != null || b != null;
            boolean chromeClosed = chromeSeen
                    && (a == null || a.booleanValue())
                    && (b == null || b.booleanValue());
            return !chromeClosed;
        }
        return false;
    }

    private static boolean isShown(int id) {
        Boolean h = hiddenById.get(id);
        return h != null && !h.booleanValue();
    }

    private static boolean interfacesReady() {
        return rt4.client.gameState == 30 && InterfaceList.components != null;
    }

    public static void onIntegerInputSubmitted() {
        lastKind = null;
        evaluate(); // may restore QC underneath
        InputManager.refreshMode();
    }

    public static void onNameInputSubmitted() {
        SpecialModalRegistry.clearActive();
        InputManager.refreshMode();
    }

    public static void onStringInputSubmitted() {
        SpecialModalRegistry.clearActive();
        InputManager.refreshMode();
    }

    public static void onWidgetClosed() {
        SpecialModalRegistry.clearActive();
        lastKind = null;
        evaluate();
        InputManager.refreshMode();
    }

    public static void resetChromeState() {
        hiddenById.clear();
        lastKind = null;
        ChatBoxModalRegistry.clearActive();
        SpecialModalRegistry.clearActive();
    }

    public static boolean isChatBoxModalOpen() {
        return ChatBoxModalRegistry.isActive();
    }

    public static boolean isSpecialTextModalOpen() {
        return SpecialModalRegistry.isActive();
    }

    /**
     * Generic client option-click CS2 (from ClientProt.method4512).
     */
    public static void onClientOptionScript(int scriptId, int componentId, JagString opBase, int op,
                                            Object[] scriptArgs) {
        if (client.gameState != 30) {
            return;
        }
        if (scriptId == QC_OPEN_CS2) {
            onQuickChatShown();
            return;
        }
        if (scriptId == QC_CLOSE_BUTTON_CS2) {
            onQuickChatHidden();
            return;
        }
        if (scriptId == QC_CLOSE_ESC_CS2) {
            if (ChatBoxModalRegistry.isActive()
                    && "quickchat".equals(ChatBoxModalRegistry.getActiveName())) {
                onQuickChatHidden();
            }
        }
    }

    public static void onComponent(int componentId) {
    }
}