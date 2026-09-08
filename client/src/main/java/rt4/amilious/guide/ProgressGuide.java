package rt4.amilious.guide;

import rt4.client;
import rt4.amilious.AmiliousClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Evaluates OptimalRoute against live skills + quest completions.
 * Call init() from AmiliousClient.Init, tick() in-game, drawOverlay() when drawing HUD.
 */
public final class ProgressGuide {

    public static final String[] SKILL_NAMES = {
            "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic",
            "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting",
            "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming",
            "Runecraft", "Hunter", "Construction", "Summoning"
    };

    public static boolean enabled = true;
    public static boolean drawHud = true;

    private static GuideStep current;
    private static int currentIndex = -1;
    private static int doneCount;
    private static int totalCount;
    private static String statusLine = "Guide not ticked yet";


    public static int getDoneCount() { return doneCount; }
    public static int getTotalCount() { return totalCount; }

    private ProgressGuide() {
    }

    public static void init() {
        AmiliousClient.AddCommand(new GuideCommand());
        tick();
    }

    public static String skillName(int id) {
        if (id < 0 || id >= SKILL_NAMES.length) {
            return "Skill" + id;
        }
        return SKILL_NAMES[id];
    }

    public static int skillId(String name) {
        for (int i = 0; i < SKILL_NAMES.length; i++) {
            if (SKILL_NAMES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private static int xpForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        int[] table = rt4.PlayerSkillXpTable.xpLevelLookup;
        int idx = level - 2;
        if (idx < 0) {
            return 0;
        }
        if (idx >= table.length) {
            return table[table.length - 1];
        }
        return table[idx];
    }

    private static String trainProgress(GuideStep step) {
        int skill = step.trainSkillId;
        int goal = step.trainLevel;
        int level = skillLevel(skill);
        int xp = 0;
        if (skill >= 0 && skill < rt4.PlayerSkillXpTable.experience.length) {
            xp = rt4.PlayerSkillXpTable.experience[skill];
        }
        int need = xpForLevel(goal);
        if (need < 1) {
            need = 1;
        }
        if (xp > need) {
            xp = need;
        }
        int pct = (int) (xp * 100L / need);
        return "Progress " + xp + " / " + need + " xp  (lvl " + level + " -> " + goal + ", " + pct + "%)";
    }

    private static String questProgress(GuideStep step) {
        QuestVarps.Def def = QuestVarps.get(step.name);
        if (def == null) {
            return "Progress  unknown";
        }
        int value = 0;
        if (def.varp >= 0 && def.varp < rt4.VarpDomain.activeVarps.length) {
            value = rt4.VarpDomain.activeVarps[def.varp];
        }
        if (value < 0) {
            value = 0;
        }
        if (value > def.completeAt) {
            value = def.completeAt;
        }
        int pct = def.completeAt <= 0 ? 0 : (value * 100 / def.completeAt);
        return "Progress  " + value + "/" + def.completeAt + " (" + pct + "%)";
    }

    /**
     * Live skill level. Adjust the field names to your client
     * (often client.skillLevel[id] or PlayerSkill.level[id]).
     */
    public static int skillLevel(int id) {
        if (id < 0 || id >= rt4.PlayerSkillXpTable.baseLevels.length) {
            return 1;
        }
        int level = rt4.PlayerSkillXpTable.baseLevels[id];
        return level < 1 ? 1 : level;
    }

    public static void tick() {
        if (!enabled || client.gameState != 30) {
            return;
        }
        GuideStep[] route = OptimalRoute.STEPS;
        totalCount = route.length;
        doneCount = 0;
        current = null;
        currentIndex = -1;

        for (int i = 0; i < route.length; i++) {
            GuideStep step = route[i];
            if (isDone(step)) {
                doneCount++;
                continue;
            }
            if (step.type == GuideStep.Type.QUEST && step.optional
                    && !QuestProgress.isImplemented(step.name)) {
                doneCount++;
                continue;
            }
            current = step;
            currentIndex = i;
            break;
        }

        if (current == null) {
            statusLine = "Route complete (" + doneCount + "/" + totalCount + ")";
        } else if (current.type == GuideStep.Type.TRAIN) {
            int have = skillLevel(current.trainSkillId);
            statusLine = "TRAIN " + current.name + "  (" + have + "/" + current.trainLevel + ")  "
                    + current.trainHint;
        } else {
            statusLine = "QUEST " + current.name + " @ " + current.start;
        }
    }

    /** 0-100 for the CURRENT task (xp or quest varp), not the 1/88 route. */
    public static int currentProgressPct() {
        if (current == null) {
            return 0;
        }
        if (current.type == GuideStep.Type.TRAIN) {
            int need = xpForLevel(current.trainLevel);
            if (need < 1) {
                return 100;
            }
            int xp = 0;
            if (current.trainSkillId >= 0 && current.trainSkillId < rt4.PlayerSkillXpTable.experience.length) {
                xp = rt4.PlayerSkillXpTable.experience[current.trainSkillId];
            }
            if (xp > need) {
                xp = need;
            }
            return (int) (xp * 100L / need);
        }
        QuestVarps.Def def = QuestVarps.get(current.name);
        if (def == null || def.completeAt <= 0) {
            return 0;
        }
        int value = 0;
        if (def.varp >= 0 && def.varp < rt4.VarpDomain.activeVarps.length) {
            value = rt4.VarpDomain.activeVarps[def.varp];
        }
        if (value < 0) {
            value = 0;
        }
        if (value > def.completeAt) {
            value = def.completeAt;
        }
        return value * 100 / def.completeAt;
    }

    public static String[] overlayBody() {
        List<String> lines = new ArrayList<String>();
        if (current == null) {
            return new String[0];
        }
        if (current.type == GuideStep.Type.TRAIN) {
            if (current.trainHint.length() > 0) {
                lines.add(current.trainHint);
            }
        } else if (current.start.length() > 0) {
            lines.add("Start: " + current.start);
        }
        List<String> miss = missingFor(current);
        if (!miss.isEmpty()) {
            lines.add("Need:");
            for (int i = 0; i < miss.size() && i < 4; i++) {
                lines.add("  " + miss.get(i));
            }
        }
        GuideStep n = peekNext();
        if (n != null) {
            lines.add("Next: " + n.name);
        }
        return lines.toArray(new String[0]);
    }

    public static boolean isDone(GuideStep step) {
        if (step.type == GuideStep.Type.TRAIN) {
            return skillLevel(step.trainSkillId) >= step.trainLevel;
        }
        if (QuestProgress.isComplete(step.name)) {
            return true;
        }
        return false;
    }

    public static GuideStep current() {
        return current;
    }

    public static String statusLine() {
        return statusLine;
    }

    public static List<String> missingFor(GuideStep step) {
        List<String> miss = new ArrayList<String>();
        if (step == null) {
            return miss;
        }
        if (step.skillReqs != null) {
            for (Map.Entry<Integer, Integer> e : step.skillReqs.entrySet()) {
                int have = skillLevel(e.getKey());
                if (have < e.getValue()) {
                    miss.add(skillName(e.getKey()) + " " + have + "/" + e.getValue());
                }
            }
        }
        if (step.questReqs != null) {
            for (int i = 0; i < step.questReqs.length; i++) {
                if (step.questReqs[i] == null) {
                    continue;
                }
                if (!QuestProgress.isComplete(step.questReqs[i])) {
                    miss.add("Quest: " + step.questReqs[i]);
                }
            }
        }
        return miss;
    }

    public static String[] overlayLines() {
        List<String> lines = new ArrayList<String>();
        lines.add("Task  " + doneCount + " / " + totalCount);

        if (current == null) {
            lines.add(statusLine);
            return lines.toArray(new String[0]);
        }

        if (current.type == GuideStep.Type.TRAIN) {
            lines.add("TRAIN " + current.name);
            lines.add(trainProgress(current));
            if (current.trainHint != null && current.trainHint.length() > 0) {
                lines.add(current.trainHint);
            }
        } else {
            lines.add("QUEST " + current.name);
            lines.add(questProgress(current));
            String reward = rewardLine(current);
            if (reward.length() > 0) {
                lines.add(reward);
            }
            lines.add("Start: " + current.start);
        }

        List<String> miss = missingFor(current);
        if (!miss.isEmpty()) {
            lines.add("Missing:");
            for (int i = 0; i < miss.size() && i < 6; i++) {
                lines.add("  - " + miss.get(i));
            }
        }

        GuideStep peek = peekNext();
        if (peek != null) {
            lines.add("Next: " + peek.name);
        }
        return lines.toArray(new String[0]);
    }

    private static String rewardLine(GuideStep step) {
        if (step.xpRewards == null || step.xpRewards.isEmpty()) {
            return step.why != null ? step.why.replace(",", ", ") : "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : step.xpRewards.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getValue()).append(" ").append(skillName(e.getKey())).append(" XP");
        }
        return sb.toString();
    }

    private static String ascii(String s) {
        if (s == null) {
            return "";
        }
        return s.replace('\u2014', '-')
                .replace('\u2013', '-')
                .replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"');
    }

    private static GuideStep peekNext() {
        GuideStep[] route = OptimalRoute.STEPS;
        if (currentIndex < 0) {
            return null;
        }
        for (int i = currentIndex + 1; i < route.length; i++) {
            GuideStep s = route[i];
            if (isDone(s)) {
                continue;
            }
            if (s.type == GuideStep.Type.QUEST && s.optional && !QuestProgress.isImplemented(s.name)) {
                continue;
            }
            return s;
        }
        return null;
    }

    /** Call from your HUD / plugin draw when drawHud is true. */
    public static void drawOverlay() {
        GuideHud.draw();
    }

    public static String currentTaskTitle() {
        if (current == null) {
            return "Done";
        }
        if (current.type == GuideStep.Type.TRAIN) {
            return current.name;
        }
        return "QUEST " + current.name;
    }

    public static String printFull() {
        StringBuilder sb = new StringBuilder();
        GuideStep[] route = OptimalRoute.STEPS;
        for (int i = 0; i < route.length; i++) {
            GuideStep s = route[i];
            boolean skip = s.type == GuideStep.Type.QUEST && s.optional
                    && !QuestProgress.isImplemented(s.name);
            String mark = skip ? "[skip]" : (isDone(s) ? "[x]" : (s == current ? "[>]" : "[ ]"));
            sb.append(mark).append(' ').append(s.type).append(' ').append(s.name).append('\n');
        }
        return sb.toString();
    }
}