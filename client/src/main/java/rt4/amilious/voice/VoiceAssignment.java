package rt4.amilious.voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rt4.amilious.Gender;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stable voice id per speaker from voices.json / assigned.json.
 * Returns null when there is no pool entry — backends apply their own defaults.
 *
 * Player voice is reserved: NPCs will not be assigned the same id as the local player.
 *
 * voiceUseCounts = number of speakers currently assigned that voice (not speak events).
 *
 * voices.json   — next to jar (male / female / neutral)
 * assigned.json — tts-cache/assigned.json
 */
public final class VoiceAssignment {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Random RNG = new Random();
    private static final Object LOCK = new Object();

    private static boolean loaded;
    private static VoicesFile voices = new VoicesFile();
    private static AssignedFile assigned = new AssignedFile();
    private static VoiceValidator validator;

    /** Normalized local player name, or null. */
    private static String playerKey = null;

    public interface VoiceValidator {
        boolean isValid(String voiceId);
    }

    private VoiceAssignment() {
    }

    public static void setValidator(VoiceValidator v) {
        validator = v;
    }

    /** Call on login when local player name is known. */
    public static void setPlayerSpeaker(String playerName) {
        synchronized (LOCK) {
            playerKey = normalizeSpeaker(playerName);
        }
    }

    public static void clearPlayerSpeaker() {
        synchronized (LOCK) {
            playerKey = null;
        }
    }

    /**
     * Assigned voice for this speaker, or a least-used pick from the gender pool.
     * Non-player speakers never receive the player's current voice id.
     * Counts track speakers per voice, not how often someone talks.
     *
     * @return voice id, or null if no speaker / empty pool (backend should default)
     */
    public static String resolve(String speaker, Gender gender) {
        ensureLoaded();

        if (gender == null || gender == Gender.UNKNOWN) {
            gender = Gender.MALE;
        }

        String key = normalizeSpeaker(speaker);
        if (key.isEmpty()) {
            return null;
        }

        synchronized (LOCK) {
            boolean forPlayer = isPlayerSpeaker(key);
            String reservedPlayerVoice = forPlayer ? null : playerVoiceIdLocked();

            AssignedEntry existing = assigned.assignments.get(key);
            if (existing != null && existing.voiceId != null && !existing.voiceId.isEmpty()) {
                boolean ok = isVoiceStillAllowed(existing.voiceId);
                if (ok && reservedPlayerVoice != null
                        && reservedPlayerVoice.equals(existing.voiceId)) {
                    ok = false;
                    System.out.println("[voice-assign] \"" + key
                            + "\" had player voice — reassigning");
                }
                if (ok) {
                    existing.uses++; // per-speaker chat stats only
                    saveAssigned();
                    // do not bumpVoiceCount — already counted as one speaker
                    return existing.voiceId;
                }
                // drop bad / player-colliding assignment
                String old = existing.voiceId;
                assigned.assignments.remove(key);
                decVoiceCount(old);
            }

            String picked = pickVoice(gender, reservedPlayerVoice);
            if (picked == null) {
                return null;
            }

            AssignedEntry e = new AssignedEntry();
            e.voiceId = picked;
            e.gender = gender.name();
            e.uses = 1;
            assigned.assignments.put(key, e);
            bumpVoiceCount(picked); // +1 speaker on this voice
            saveAssigned();
            System.out.println("[voice-assign] \"" + key + "\" → " + picked
                    + " (" + gender + ")" + (forPlayer ? " [player]" : ""));
            return picked;
        }
    }

    public static void invalidateVoice(String voiceId) {
        if (voiceId == null || voiceId.isEmpty()) {
            return;
        }
        synchronized (LOCK) {
            ensureLoaded();
            System.out.println("[voice-assign] invalidate " + voiceId);
            removeVoiceEverywhere(voiceId);

            List<String> toRemove = new ArrayList<String>();
            for (Map.Entry<String, AssignedEntry> e : assigned.assignments.entrySet()) {
                if (e.getValue() != null && voiceId.equals(e.getValue().voiceId)) {
                    toRemove.add(e.getKey());
                }
            }
            for (String k : toRemove) {
                assigned.assignments.remove(k);
            }
            // counts for this voice cleared in removeVoiceEverywhere
            saveVoices();
            saveAssigned();
        }
    }

    public static void reload() {
        synchronized (LOCK) {
            loaded = false;
            voices = new VoicesFile();
            assigned = new AssignedFile();
            // keep playerKey across reload
            ensureLoaded();
        }
    }

    /**
     * Add voice to voices.json for gender if not already listed. Creates file if missing.
     * @return true if newly added
     */
    public static boolean addVoice(String voice, Gender gender) {
        if (voice == null) {
            return false;
        }
        voice = voice.trim();
        if (voice.isEmpty()) {
            return false;
        }
        if (gender == null || gender == Gender.UNKNOWN) {
            gender = Gender.NEUTRAL;
        }

        synchronized (LOCK) {
            ensureLoaded();

            List<String> list = listFor(gender);
            if (list == null) {
                list = new ArrayList<String>();
                setListFor(gender, list);
            }

            if (list.contains(voice)) {
                return false;
            }

            if (voices.male != null) {
                voices.male.remove(voice);
            }
            if (voices.female != null) {
                voices.female.remove(voice);
            }
            if (voices.neutral != null) {
                voices.neutral.remove(voice);
            }

            list.add(voice);
            saveVoicesAlways();
            System.out.println("[voice-assign] addVoice " + voice + " → " + gender);
            return true;
        }
    }

    // ---- internals ----

    private static boolean isPlayerSpeaker(String key) {
        return playerKey != null && !playerKey.isEmpty() && playerKey.equals(key);
    }

    /** Caller must hold LOCK. */
    private static String playerVoiceIdLocked() {
        if (playerKey == null || playerKey.isEmpty()) {
            return null;
        }
        AssignedEntry e = assigned.assignments.get(playerKey);
        if (e == null || e.voiceId == null || e.voiceId.isEmpty()) {
            return null;
        }
        return e.voiceId;
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (LOCK) {
            if (loaded) {
                return;
            }
            loadVoices();
            loadAssigned();
            pruneInvalidFromPool();
            loaded = true;
        }
    }

    private static String normalizeSpeaker(String speaker) {
        if (speaker == null) {
            return "";
        }
        return speaker.trim().toLowerCase();
    }

    private static boolean isVoiceStillAllowed(String voiceId) {
        if (validator != null && !validator.isValid(voiceId)) {
            return false;
        }
        return (voices.male != null && voices.male.contains(voiceId))
                || (voices.female != null && voices.female.contains(voiceId))
                || (voices.neutral != null && voices.neutral.contains(voiceId));
    }

    private static String pickVoice(Gender gender, String excludeVoiceId) {
        List<String> pool = new ArrayList<String>(poolFor(gender));
        if (pool.isEmpty() && voices.neutral != null) {
            pool.addAll(voices.neutral);
        }
        pruneList(pool);

        if (excludeVoiceId != null && !excludeVoiceId.isEmpty()) {
            pool.remove(excludeVoiceId);
        }

        if (pool.isEmpty()) {
            return null;
        }

        // Prefer voices with fewest speakers assigned
        int best = Integer.MAX_VALUE;
        List<String> candidates = new ArrayList<String>();
        for (String id : pool) {
            int c = voiceCount(id);
            if (c < best) {
                best = c;
                candidates.clear();
                candidates.add(id);
            } else if (c == best) {
                candidates.add(id);
            }
        }
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    private static List<String> poolFor(Gender gender) {
        if (gender == Gender.FEMALE) {
            return voices.female != null ? voices.female : Collections.<String>emptyList();
        }
        if (gender == Gender.NEUTRAL) {
            if (voices.neutral != null && !voices.neutral.isEmpty()) {
                return voices.neutral;
            }
        }
        return voices.male != null ? voices.male : Collections.<String>emptyList();
    }

    private static List<String> listFor(Gender gender) {
        if (gender == Gender.FEMALE) {
            return voices.female;
        }
        if (gender == Gender.NEUTRAL) {
            return voices.neutral;
        }
        return voices.male;
    }

    private static void setListFor(Gender gender, List<String> list) {
        if (gender == Gender.FEMALE) {
            voices.female = list;
        } else if (gender == Gender.NEUTRAL) {
            voices.neutral = list;
        } else {
            voices.male = list;
        }
    }

    private static void pruneInvalidFromPool() {
        pruneList(voices.male);
        pruneList(voices.female);
        pruneList(voices.neutral);
    }

    private static void pruneList(List<String> list) {
        if (list == null) {
            return;
        }
        list.removeIf(id -> id == null || id.trim().isEmpty());
        if (validator != null) {
            list.removeIf(id -> !validator.isValid(id));
        }
    }

    private static void removeVoiceEverywhere(String voiceId) {
        if (voices.male != null) {
            voices.male.remove(voiceId);
        }
        if (voices.female != null) {
            voices.female.remove(voiceId);
        }
        if (voices.neutral != null) {
            voices.neutral.remove(voiceId);
        }
        if (assigned.voiceUseCounts != null) {
            assigned.voiceUseCounts.remove(voiceId);
        }
    }

    private static int voiceCount(String id) {
        if (assigned.voiceUseCounts == null) {
            return 0;
        }
        Integer n = assigned.voiceUseCounts.get(id);
        return n == null ? 0 : n;
    }

    /** +1 speaker assigned this voice. */
    private static void bumpVoiceCount(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        if (assigned.voiceUseCounts == null) {
            assigned.voiceUseCounts = new HashMap<String, Integer>();
        }
        assigned.voiceUseCounts.put(id, voiceCount(id) + 1);
    }

    /** -1 speaker assigned this voice. */
    private static void decVoiceCount(String id) {
        if (id == null || id.isEmpty() || assigned.voiceUseCounts == null) {
            return;
        }
        int n = voiceCount(id) - 1;
        if (n <= 0) {
            assigned.voiceUseCounts.remove(id);
        } else {
            assigned.voiceUseCounts.put(id, n);
        }
    }

    /**
     * Rebuild speaker counts from assignments (fixes old inflated data).
     * Caller should hold LOCK or be in load path before concurrent use.
     */
    private static void rebuildVoiceUseCounts() {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        if (assigned.assignments != null) {
            for (AssignedEntry e : assigned.assignments.values()) {
                if (e == null || e.voiceId == null || e.voiceId.isEmpty()) {
                    continue;
                }
                Integer n = counts.get(e.voiceId);
                counts.put(e.voiceId, n == null ? 1 : n + 1);
            }
        }
        assigned.voiceUseCounts = counts;
    }

    private static File jarDir() {
        try {
            File code = new File(
                    VoiceAssignment.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI());
            return code.isFile() ? code.getParentFile() : code;
        } catch (Exception e) {
            return new File(".");
        }
    }

    private static File voicesFile() {
        return new File(jarDir(), "voices.json");
    }

    private static File assignedFile() {
        File dir = new File(jarDir(), "tts-cache");
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        return new File(dir, "assigned.json");
    }

    private static void loadVoices() {
        File f = voicesFile();
        if (!f.isFile()) {
            System.out.println("[voice-assign] no voices.json — pool empty (backends use defaults)");
            voices = new VoicesFile();
            return;
        }
        try (FileReader r = new FileReader(f)) {
            VoicesFile v = GSON.fromJson(r, VoicesFile.class);
            voices = v != null ? v : new VoicesFile();
            if (voices.male == null) {
                voices.male = new ArrayList<String>();
            }
            if (voices.female == null) {
                voices.female = new ArrayList<String>();
            }
            if (voices.neutral == null) {
                voices.neutral = new ArrayList<String>();
            }
            System.out.println("[voice-assign] voices.json male=" + voices.male.size()
                    + " female=" + voices.female.size()
                    + " neutral=" + voices.neutral.size());
        } catch (Exception e) {
            System.err.println("[voice-assign] voices.json read failed: " + e.getMessage());
            voices = new VoicesFile();
        }
    }

    private static void loadAssigned() {
        File f = assignedFile();
        if (!f.isFile()) {
            assigned = new AssignedFile();
            return;
        }
        try (FileReader r = new FileReader(f)) {
            AssignedFile a = GSON.fromJson(r, AssignedFile.class);
            assigned = a != null ? a : new AssignedFile();
            if (assigned.assignments == null) {
                assigned.assignments = new ConcurrentHashMap<String, AssignedEntry>();
            } else {
                assigned.assignments = new ConcurrentHashMap<String, AssignedEntry>(assigned.assignments);
            }
            // Always rebuild from assignments so counts = speakers, not old chat bumps
            rebuildVoiceUseCounts();
            System.out.println("[voice-assign] assigned.json speakers=" + assigned.assignments.size()
                    + " voiceSlots=" + assigned.voiceUseCounts.size());
        } catch (Exception e) {
            System.err.println("[voice-assign] assigned.json read failed: " + e.getMessage());
            assigned = new AssignedFile();
        }
    }

    private static void saveAssigned() {
        try (FileWriter w = new FileWriter(assignedFile())) {
            GSON.toJson(assigned, w);
        } catch (Exception e) {
            System.err.println("[voice-assign] assigned.json write failed: " + e.getMessage());
        }
    }

    private static void saveVoices() {
        if (!voicesFile().isFile()) {
            return;
        }
        saveVoicesAlways();
    }

    private static void saveVoicesAlways() {
        if (voices.male == null) {
            voices.male = new ArrayList<String>();
        }
        if (voices.female == null) {
            voices.female = new ArrayList<String>();
        }
        if (voices.neutral == null) {
            voices.neutral = new ArrayList<String>();
        }
        try (FileWriter w = new FileWriter(voicesFile())) {
            GSON.toJson(voices, w);
        } catch (Exception e) {
            System.err.println("[voice-assign] voices.json write failed: " + e.getMessage());
        }
    }

    // ---- JSON shapes ----

    public static final class VoicesFile {
        public List<String> male = new ArrayList<String>();
        public List<String> female = new ArrayList<String>();
        public List<String> neutral = new ArrayList<String>();
    }

    public static final class AssignedFile {
        public Map<String, AssignedEntry> assignments = new ConcurrentHashMap<String, AssignedEntry>();
        /** Speakers currently assigned each voice id. */
        public Map<String, Integer> voiceUseCounts = new HashMap<String, Integer>();
    }

    public static final class AssignedEntry {
        public String voiceId;
        public String gender;
        public int uses;
    }
}