package rt4.amilious.input;

/**
 * Visibility / filter state for the chat UI.
 * Not driven by live components yet — set via setVisible() or tab clicks later.
 */
public final class ChatboxState {

    public static final int TAB_ALL = 49217538;
    public static final int TAB_GAME = 49217541;
    public static final int TAB_PUBLIC = 49217544;
    public static final int TAB_PRIVATE = 49217548;
    public static final int TAB_CLAN = 49217552;
    public static final int TAB_TRADE = 49217556;
    public static final int TAB_ASSIST = 49217560;

    private static boolean visible = true;
    private static int selectedTab = TAB_ALL;

    private ChatboxState() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static boolean isCollapsed() {
        return !visible;
    }

    public static void setVisible(boolean value) {
        visible = value;
    }

    public static int getSelectedTab() {
        return selectedTab;
    }

    public static void setSelectedTab(int packedComponentId) {
        selectedTab = packedComponentId;
    }

    public static boolean isChatTab(int packedComponentId) {
        return packedComponentId == TAB_ALL
                || packedComponentId == TAB_GAME
                || packedComponentId == TAB_PUBLIC
                || packedComponentId == TAB_PRIVATE
                || packedComponentId == TAB_CLAN
                || packedComponentId == TAB_TRADE
                || packedComponentId == TAB_ASSIST;
    }

    /**
     * Tab-click semantics (wire later from UI clicks):
     * collapsed + any tab → open + select
     * open + same tab → collapse
     * open + other tab → switch filter
     */
    public static void onChatTabClicked(int packedComponentId) {
        if (!isChatTab(packedComponentId)) {
            return;
        }
        if (!visible) {
            visible = true;
            selectedTab = packedComponentId;
            return;
        }
        if (packedComponentId == selectedTab) {
            visible = false;
        } else {
            selectedTab = packedComponentId;
        }
    }

    public static void resetToLoginDefaults() {
        visible = true;
        selectedTab = TAB_ALL;
    }
}