package rt4.amilious.guide;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GuideStep {

    public enum Type {
        QUEST,
        TRAIN
    }

    public final String id;
    public final Type type;
    public final String name;
    public final String start;
    public final String why;
    public final String[] questReqs;
    /** skill id -> required level */
    public final Map<Integer, Integer> skillReqs;
    /** skill id -> quest XP reward (may be empty) */
    public final Map<Integer, Integer> xpRewards;
    public final boolean optional;

    public final int trainSkillId;
    public final int trainLevel;
    public final String trainHint;

    private GuideStep(String id, Type type, String name, String start, String why,
                      String[] questReqs, Map<Integer, Integer> skillReqs,
                      Map<Integer, Integer> xpRewards, boolean optional,
                      int trainSkillId, int trainLevel, String trainHint) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.start = start != null ? start : "";
        this.why = why != null ? why : "";
        this.questReqs = questReqs != null ? questReqs : new String[0];
        this.skillReqs = skillReqs != null ? skillReqs : Collections.<Integer, Integer>emptyMap();
        this.xpRewards = xpRewards != null ? xpRewards : Collections.<Integer, Integer>emptyMap();
        this.optional = optional;
        this.trainSkillId = trainSkillId;
        this.trainLevel = trainLevel;
        this.trainHint = trainHint != null ? trainHint : "";
    }

    public static GuideStep quest(String id, String name, String start, String why,
                                  String[] questReqs, int[][] skillPairs, int[][] xpPairs) {
        return new GuideStep(id, Type.QUEST, name, start, why, questReqs,
                toMap(skillPairs), toMap(xpPairs), false, -1, 0, "");
    }

    public static GuideStep optionalQuest(String id, String name, String start, String why,
                                          String[] questReqs, int[][] skillPairs, int[][] xpPairs) {
        return new GuideStep(id, Type.QUEST, name, start, why, questReqs,
                toMap(skillPairs), toMap(xpPairs), true, -1, 0, "");
    }

    public static GuideStep train(String id, int skillId, int targetLevel, String hint) {
        Map<Integer, Integer> req = new LinkedHashMap<Integer, Integer>();
        req.put(skillId, targetLevel);
        String skillName = skillName(skillId);
        return new GuideStep(
                id,
                Type.TRAIN,
                "Train " + skillName + " to " + targetLevel,
                hint,
                skillName + " " + targetLevel,
                new String[0],
                req,
                Collections.<Integer, Integer>emptyMap(),
                false,
                skillId,
                targetLevel,
                hint
        );
    }

    private static Map<Integer, Integer> toMap(int[][] pairs) {
        if (pairs == null || pairs.length == 0) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i] != null && pairs[i].length >= 2) {
                map.put(pairs[i][0], pairs[i][1]);
            }
        }
        return map;
    }

    private static String skillName(int id) {
        try {
            return SkillNames.name(id);
        } catch (Throwable t) {
            return "Skill " + id;
        }
    }
}