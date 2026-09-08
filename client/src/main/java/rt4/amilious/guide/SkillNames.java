package rt4.amilious.guide;

public final class SkillNames {

    public static final String[] NAMES = {
            "Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic",
            "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting",
            "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming",
            "Runecraft", "Hunter", "Construction", "Summoning"
    };

    public static String name(int id) {
        if (id < 0 || id >= NAMES.length) {
            return "Skill " + id;
        }
        return NAMES[id];
    }

    public static int id(String name) {
        if (name == null) {
            return -1;
        }
        String n = name.trim();
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equalsIgnoreCase(n)) {
                return i;
            }
        }
        return -1;
    }

    private SkillNames() {
    }
}