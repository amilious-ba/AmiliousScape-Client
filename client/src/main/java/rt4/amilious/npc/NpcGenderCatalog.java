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

    private static final String RESOURCE = "/rt4/amilious/npc/npc-genders.json";

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