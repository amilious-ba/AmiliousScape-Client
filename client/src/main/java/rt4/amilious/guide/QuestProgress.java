package rt4.amilious.guide;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Quest completion lookup.
 *
 * Manual overrides work immediately (::guide done Name).
 * Wire fromJournal to the 530 quest list when you have the varp map.
 */
public final class QuestProgress {

    private static final Set<String> manualDone = new HashSet<String>();
    private static final Set<String> implemented = new HashSet<String>();

    private QuestProgress() {
    }

    public static void markDone(String questName) {
        manualDone.add(norm(questName));
    }

    public static void unmark(String questName) {
        manualDone.remove(norm(questName));
    }

    public static void resetManual() {
        manualDone.clear();
    }

    public static boolean isComplete(String questName) {
        String key = norm(questName);
        if (manualDone.contains(key)) {
            return true;
        }
        return fromJournal(questName);
    }

    /**
     * True if this world has the quest (startable). Optional route steps are
     * skipped when this returns false.
     *
     * Default: assume implemented unless you populate implemented.
     */
    public static boolean isImplemented(String questName) {
        if (implemented.isEmpty()) {
            return true;
        }
        return implemented.contains(norm(questName));
    }

    public static void setImplemented(String[] names) {
        implemented.clear();
        if (names == null) {
            return;
        }
        for (int i = 0; i < names.length; i++) {
            implemented.add(norm(names[i]));
        }
    }

    /**
     * TODO: read Interface 274 / quest varps.
     * Return true only when the journal paints the quest green / complete.
     */
    private static boolean fromJournal(String questName) {
        QuestVarps.Def def = QuestVarps.get(questName);
        if (def == null) {
            return false;
        }
        if (def.varp < 0 || def.varp >= rt4.VarpDomain.activeVarps.length) {
            return false;
        }
        int value = rt4.VarpDomain.activeVarps[def.varp];
        return value >= def.completeAt;
    }

    public static String norm(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replace("&", "and").replace("'", "").trim();
    }
}