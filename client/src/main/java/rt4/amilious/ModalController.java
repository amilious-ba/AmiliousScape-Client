package rt4.amilious;

import rt4.JagString;
import rt4.amilious.input.SpecialModalRegistry;

/**
 * Special text modals (amount / name / string).
 * Open: mini-menu X options (temporary until real iface open is found).
 * Close: integer submit / closeWidget.
 */
public final class ModalController {

    public static final int AMOUNT_INPUT_COMPONENT = 49283077;

    private ModalController() {
    }

    public static void init() {
        SpecialModalRegistry.registerComponent(AMOUNT_INPUT_COMPONENT, "amount");
    }

    /** Called from AmiliousClient.update() — no amount open/clear here for now. */
    public static void tick() {
        // Intentionally empty until we have a reliable visibility/openInterfaces check.
    }

    public static void onInterfaceOpen(int interfaceId) {
        // Don't arm on load — 752/gameframe false positives.
    }

    public static void onMiniMenuAction(int index, int actionCode, JagString op, JagString opBase) {
        if (op == null) {
            return;
        }
        String s = op.toString().toLowerCase();
        if (isAmountOption(s)) {
            System.out.println("[modal] amount option chosen: " + s + " action=" + actionCode);
            SpecialModalRegistry.setActiveComponent(AMOUNT_INPUT_COMPONENT);
        }
    }

    private static boolean isAmountOption(String s) {
        return s.contains("withdraw-x")
                || s.contains("withdraw x")
                || s.contains("cook x")
                || s.contains("make x")
                || s.endsWith("-x")
                || s.endsWith(" x");
    }

    public static void onComponent(int packedComponentId) {
        if (packedComponentId == AMOUNT_INPUT_COMPONENT) {
            SpecialModalRegistry.setActiveComponent(AMOUNT_INPUT_COMPONENT);
        }
    }

    public static void onIntegerInputSubmitted() {
        SpecialModalRegistry.clearActive();
    }

    public static void onNameInputSubmitted() {
        SpecialModalRegistry.clearActive();
    }

    public static void onStringInputSubmitted() {
        SpecialModalRegistry.clearActive();
    }

    public static void onWidgetClosed() {
        SpecialModalRegistry.clearActive();
    }

    public static boolean isSpecialTextModalOpen() {
        return SpecialModalRegistry.isActive();
    }
}