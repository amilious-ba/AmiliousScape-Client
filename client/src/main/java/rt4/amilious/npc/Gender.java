package rt4.amilious.npc;

public enum Gender {
    MALE("male"),
    FEMALE("female"),
    NEUTRAL("neutral"),
    UNKNOWN("unknown");

    private final String key;

    Gender(String key) {
        this.key = key;
    }

    /** Config / file value: "male", "female", … */
    public String key() {
        return key;
    }

    public static Gender fromString(String s) {
        if (s == null) {
            return UNKNOWN;
        }
        String t = s.trim().toLowerCase();
        if (t.isEmpty()) {
            return UNKNOWN;
        }
        if (t.startsWith("female") || t.equals("f")) {
            return FEMALE;
        }
        if (t.startsWith("male") || t.equals("m")) {
            return MALE;
        }
        if (t.startsWith("neutral") || t.equals("n") || t.equals("none")) {
            return NEUTRAL;
        }
        return UNKNOWN;
    }

    public boolean isFemale() {
        return this == FEMALE;
    }

    public boolean isMale() {
        return this == MALE;
    }
}