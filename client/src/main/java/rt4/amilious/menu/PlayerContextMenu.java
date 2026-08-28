package rt4.amilious.menu;

import rt4.Cheat;
import rt4.JagString;
import plugin.api.API;
import rt4.LoginManager;
import plugin.api.MiniMenuType;
import plugin.api.MiniMenuEntry;

/**
 * Extra right-click options when the mini-menu is on a player.
 * Hook: AmiliousClient.onMiniMenuCreate() -> PlayerContextMenu.onMiniMenuCreate()
 */
public final class PlayerContextMenu {

    //#region Constructors #############################################################################################

    private PlayerContextMenu() {    }

    //#endregion #######################################################################################################


    //#region Public Methods ###########################################################################################

    public static void onMiniMenuCreate() {
        String name = resolvePlayerName(API.GetMiniMenuEntries());
        if (name == null) {return;}

        // Anyone
        add("View stats", name, () -> viewStats(name));

        // Staff
        if (LoginManager.staffModLevel > 0) {
            add("Teleport to me", name, () -> run("::teletome " + name));
        }
    }

    //#endregion #######################################################################################################


    //#region Private Methods ##########################################################################################

    private static void viewStats(String name) {
        // Swap this if your server uses another cheat
        run("::stats " + name);
    }

    private static void run(String command) {
        Cheat.execute(JagString.parse(command));
    }

    private static void add(String verb, String name, Runnable onClick) {
        API.InsertMiniMenuEntry(verb, name, onClick);
    }

    private static String resolvePlayerName(MiniMenuEntry[] entries) {
        if (entries == null) {
            return null;
        }

        boolean social = false;
        String fromPlayerType = null;
        String fromSocial = null;

        for (MiniMenuEntry e : entries) {
            String verb = e.getVerb() == null ? "" : e.getVerb().toLowerCase();
            String sub = stripColTags(e.getSubject());

            if (verb.contains("add friend")
                    || verb.contains("add ignore")
                    || verb.contains("report")) {
                social = true;
            }

            if (e.getType() == MiniMenuType.PLAYER && isUsableName(sub)) {
                fromPlayerType = sub;
            }
            if (social && fromSocial == null && isUsableName(sub)) {
                fromSocial = sub;
            }
        }

        if (fromPlayerType != null) {
            return fromPlayerType;
        }
        if (social) {
            return fromSocial;
        }
        return null;
    }

    private static boolean isUsableName(String s) {
        if (s == null || s.isEmpty()) return false;
        if (s.equalsIgnoreCase("null")) return false;
        return !s.equalsIgnoreCase("walk here") && !s.equalsIgnoreCase("cancel");
    }

    private static String stripColTags(String s) {
        if (s == null) return null;
        return s.replaceAll("(?i)<col=[0-9a-f]+>", "")
                .replaceAll("(?i)</col>", "")
                .trim();
    }

    //#endregion #######################################################################################################

}