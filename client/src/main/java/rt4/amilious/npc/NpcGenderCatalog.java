package rt4.amilious.npc;

import rt4.amilious.Gender;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps dialogue speaker names → Gender from classpath JSON.
 * Resource: /rt4/amilious/npc/npc-genders.json (inside jar).
 */
public final class NpcGenderCatalog {

    private static final String RESOURCE = "/rt4/amilious/npc/npc_genders.json";

    /** Normalized name → gender from file */
    private static final Map<String, Gender> FROM_FILE = new HashMap<String, Gender>();

    /** Runtime cache including misses / defaults */
    private static final Map<String, Gender> CACHE = new ConcurrentHashMap<String, Gender>();

    private static boolean loaded;
    private static Gender defaultGender = Gender.MALE;

    private NpcGenderCatalog() {
    }

    public static void setDefault(Gender g) {
        if (g != null) {
            defaultGender = g;
        }
    }

    public static Gender getGender(String speaker) {
        if (speaker == null || speaker.isEmpty()) {
            return defaultGender;
        }

        String key = normalize(speaker);

        Gender cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        ensureLoaded();

        Gender g = FROM_FILE.get(key);
        if (g == null) {
            g = guessGender(key);
        }
        if (g == null) {
            g = defaultGender;
        }
        CACHE.put(key, g);
        return g;
    }
    /** Optional: remember a decision without editing JSON */
    public static void putOverride(String speaker, Gender gender) {
        if (speaker == null || gender == null) {
            return;
        }
        String key = normalize(speaker);
        CACHE.put(key, gender);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static String normalize(String speaker) {
        return speaker.trim().toLowerCase();
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        InputStream in = NpcGenderCatalog.class.getResourceAsStream(RESOURCE);
        if (in == null) {
            // try relative to class package
            in = NpcGenderCatalog.class.getResourceAsStream("npc-genders.json");
        }
        if (in == null) {
            System.err.println("[npc-gender] resource not found: " + RESOURCE);
            return;
        }

        try {
            String json = readAll(in);
            parseSimpleJsonObject(json, FROM_FILE);
            System.out.println("[npc-gender] loaded " + FROM_FILE.size() + " entries");
        } catch (Exception e) {
            System.err.println("[npc-gender] load failed: " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static String readAll(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /**
     * Best-effort guess from the display name. Returns null if unsure
     * (caller uses defaultGender).
     */
    private static Gender guessGender(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        // Phrases / exact tokens
        if (containsWord(key, "woman") || containsWord(key, "girl")
                || containsWord(key, "lady") || containsWord(key, "miss")
                || containsWord(key, "mrs") || containsWord(key, "ms")
                || containsWord(key, "mother") || containsWord(key, "sister")
                || containsWord(key, "daughter") || containsWord(key, "wife")
                || containsWord(key, "queen") || containsWord(key, "princess")
                || containsWord(key, "priestess") || containsWord(key, "witch")
                || containsWord(key, "nurse") || containsWord(key, "maid")) {
            return Gender.FEMALE;
        }

        if (containsWord(key, "man") || containsWord(key, "boy")
                || containsWord(key, "mr") || containsWord(key, "sir")
                || containsWord(key, "father") || containsWord(key, "brother")
                || containsWord(key, "son") || containsWord(key, "husband")
                || containsWord(key, "king") || containsWord(key, "prince")
                || containsWord(key, "lord") || containsWord(key, "monk")
                || containsWord(key, "priest") || containsWord(key, "guard")
                || containsWord(key, "sailor") || containsWord(key, "wizard")
                || containsWord(key, "warrior") || containsWord(key, "knight")
                || containsWord(key, "fisherman") || containsWord(key, "master")) {
            // "master chef" etc. — still male-leaning titles in RS
            return Gender.MALE;
        }

        // Leading title
        if (key.startsWith("mr ") || key.startsWith("sir ")) {
            return Gender.MALE;
        }
        if (key.startsWith("mrs ") || key.startsWith("ms ") || key.startsWith("miss ")) {
            return Gender.FEMALE;
        }

        // Weak suffix heuristics (easy to get wrong — keep conservative)
        // Many RS female names end in a, ia, elle — optional:
        if (key.endsWith("ia") || key.endsWith("elle") || key.endsWith("ette")
                || key.endsWith("wyn") || key.endsWith("a") && key.length() >= 4) {
            // "a" ending is noisy (e.g. "bryona" ok, "extra" rare as NPC)
            // Only apply 'a' if not a known neutral word
            if (!key.endsWith("a") || looksFeminineA(key)) {
                return Gender.FEMALE;
            }
        }

        return null; // unknown → defaultGender
    }

    private static boolean containsWord(String hay, String needle) {
        // word boundary-ish: start/end or spaces
        int i = 0;
        while (i <= hay.length() - needle.length()) {
            int at = hay.indexOf(needle, i);
            if (at < 0) {
                return false;
            }
            boolean leftOk = at == 0 || hay.charAt(at - 1) == ' ';
            int end = at + needle.length();
            boolean rightOk = end == hay.length() || hay.charAt(end) == ' ';
            if (leftOk && rightOk) {
                return true;
            }
            i = at + 1;
        }
        return false;
    }

    private static boolean looksFeminineA(String key) {
        // skip short / obvious non-names
        if (key.length() < 4) {
            return false;
        }
        // avoid "man" already handled; skip ends like "ia" already true above
        return key.endsWith("na") || key.endsWith("ra") || key.endsWith("la")
                || key.endsWith("sa") || key.endsWith("ta") || key.endsWith("ya");
    }

    /**
     * Minimal {"key":"MALE","key2":"FEMALE"} parser — no full JSON library required.
     * Values must be Gender enum names.
     */
    private static void parseSimpleJsonObject(String json, Map<String, Gender> out) {
        String s = json.trim();
        if (s.startsWith("{")) {
            s = s.substring(1);
        }
        if (s.endsWith("}")) {
            s = s.substring(0, s.length() - 1);
        }

        // split on commas not inside quotes — good enough for flat maps
        int i = 0;
        while (i < s.length()) {
            int keyStart = s.indexOf('"', i);
            if (keyStart < 0) {
                break;
            }
            int keyEnd = s.indexOf('"', keyStart + 1);
            if (keyEnd < 0) {
                break;
            }
            String key = s.substring(keyStart + 1, keyEnd).trim().toLowerCase();

            int colon = s.indexOf(':', keyEnd + 1);
            if (colon < 0) {
                break;
            }
            int valStart = s.indexOf('"', colon + 1);
            if (valStart < 0) {
                break;
            }
            int valEnd = s.indexOf('"', valStart + 1);
            if (valEnd < 0) {
                break;
            }
            String val = s.substring(valStart + 1, valEnd).trim().toUpperCase();

            try {
                out.put(key, Gender.valueOf(val));
            } catch (IllegalArgumentException e) {
                System.err.println("[npc-gender] bad gender for \"" + key + "\": " + val);
            }
            i = valEnd + 1;
        }
    }
}