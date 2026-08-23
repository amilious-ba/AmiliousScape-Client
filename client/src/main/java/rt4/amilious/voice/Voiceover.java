package rt4.amilious.voice;

import rt4.GlobalJsonConfig;
import rt4.PlayerList;
import rt4.amilious.Gender;
import rt4.amilious.ModalController;
import rt4.amilious.npc.NpcGenderCatalog;
import rt4.amilious.voice.speakers.*;

/**
 * Voiceover facade. Call {@link #init()} once after config load.
 * Gender is resolved here; backends only receive Gender.
 */
public final class Voiceover {

    private static boolean enabled;
    private static ITextSpeaker backend = new DisabledSpeaker();
    private static boolean initialized;
    private static volatile boolean speaking;
    private static Runnable pendingComplete;

    private Voiceover() {
    }

    public static void TryAssignPlayerSpeaker() {
        if(initialized) return;
        if (PlayerList.self != null && PlayerList.self.username != null) {
            var playerName = PlayerList.self.username.toString();
            VoiceAssignment.setPlayerSpeaker(playerName);
            VoiceAssignment.resolve(playerName, resolveGender(playerName));
            initialized = true;
        }
    }

    /**
     * From AmiliousClient.Init() after GlobalJsonConfig is loaded.
     *
     * voiceoverSpeaker empty → off
     * sapi / system / windows / mac / linux → OS TTS
     * elevenlabs → ElevenLabs (needs elevenLabsKey)
     * unknown → log + OS TTS fallback
     */
    public static void init() {
        stop();
        enabled = false;
        backend = new DisabledSpeaker();

        GlobalJsonConfig cfg = GlobalJsonConfig.instance;
        if (cfg == null) {
            System.out.println("[voiceover] no config — disabled");
            return;
        }

        String type = cfg.voiceoverSpeaker;
        if (type == null || type.trim().isEmpty()) {
            System.out.println("[voiceover] disabled (voiceoverSpeaker not set)");
            return;
        }
        type = type.trim().toLowerCase();

        ITextSpeaker chosen = null;

        if (isOsTts(type)) {
            chosen = createOsDefault();
        } else if (type.equals("elevenlabs") || type.equals("11labs")) {
            String key = cfg.elevenLabsKey;
            if (key == null || key.trim().isEmpty()) {
                System.err.println("[voiceover] elevenlabs needs elevenLabsKey — falling back to OS TTS");
                chosen = createOsDefault();
            } else {
                chosen = new ElevenLabsSpeaker(
                        key.trim(),
                        cfg.elevenLabsMale,
                        cfg.elevenLabsFemale
                );
            }
        } else if (type.equals("openai") || type.equals("gpt") || type.equals("gpt-tts")) {
            String key = cfg.openaiKey;
            if (key == null || key.trim().isEmpty()) {
                System.err.println("[voiceover] openai needs openaiKey — falling back to OS TTS");
                chosen = createOsDefault();
            } else {
                chosen = new OpenAiSpeaker(
                        key.trim(),
                        cfg.openaiModel,
                        cfg.openaiVoiceMale,
                        cfg.openaiVoiceFemale
                );
            }
        } else {
            System.err.println("[voiceover] unknown voiceoverSpeaker=\"" + type + "\" — falling back to OS TTS");
            chosen = createOsDefault();
        }

        if (chosen instanceof AbstractProcessSpeaker) {
            AbstractProcessSpeaker aps = (AbstractProcessSpeaker) chosen;
            if (!aps.isAvailable()) {
                System.err.println("[voiceover] backend unavailable — disabled");
                backend = new DisabledSpeaker();
                enabled = false;
                return;
            }
        }

        backend = chosen != null ? chosen : new DisabledSpeaker();
        enabled = !(backend instanceof DisabledSpeaker);
        System.out.println("[voiceover] speaker=" + type + " enabled=" + enabled);
    }

    public static void tick(){
        ChatHeadReader.tick();
        NarratorReader.tick();
        TutorialGuideReader.tick();
    }

    public static void onLogin() {
        TryAssignPlayerSpeaker();
    }

    public static void onLogout() {
        ChatHeadReader.reset();
        NarratorReader.reset();
        TutorialGuideReader.reset();
        VoiceAssignment.clearPlayerSpeaker();
        initialized = false;
    }

    public static void onTutorialGuideText(int packetId, int iFaceId, int childId, String text) {
        TutorialGuideReader.onSetText(packetId,iFaceId, childId, text);
    }

    public static void onInterfaceOpen(int interfaceId) {
        ChatHeadReader.onInterfaceOpen(interfaceId);
        NarratorReader.onInterfaceOpen(interfaceId);
        TutorialGuideReader.onInterfaceOpen(interfaceId);
    }

    public static void onInterfaceClose(int interfaceId) {
        ChatHeadReader.onInterfaceClose(interfaceId);
        NarratorReader.onInterfaceClose(interfaceId);
    }

    public static void setBackend(ITextSpeaker speaker) {
        stop();
        backend = speaker != null ? speaker : new DisabledSpeaker();
        enabled = !(backend instanceof DisabledSpeaker);
    }

    public static void setEnabled(boolean on) {
        enabled = on;
        if (!on) {
            stop();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void speak(String text) {
        speak(null, text, null);
    }

    public static void speak(String text, Runnable onComplete) {
        speak(null, text, onComplete);
    }

    public static void speak(String speaker, String text) {
        speak(speaker, text, null);
    }

    public static void speak(String speaker, String text, Runnable onComplete) {
        if (!enabled || backend == null || text == null || text.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        // Cancel previous line if any
        stop();

        TryAssignPlayerSpeaker();
        Gender gender = resolveGender(speaker);

        speaking = true;
        pendingComplete = onComplete;

        backend.speak(speaker, text, gender, new Runnable() {
            @Override
            public void run() {
                speaking = false;
                Runnable cb = pendingComplete;
                pendingComplete = null;
                if (cb != null) {
                    cb.run();
                }
            }
        });
    }

    public static void stop() {
        if (backend != null) {
            backend.stop(); // if your backend has this
        }
        speaking = false;
        pendingComplete = null;
    }

    /**
     * Local player → appearance gender.
     * Else catalog → MALE if unknown.
     */
    public static Gender resolveGender(String speaker) {
        if (speaker != null && isLocalPlayer(speaker)) {
            try {
                if (PlayerList.self != null && PlayerList.self.appearance != null) {
                    return PlayerList.self.appearance.gender ? Gender.FEMALE : Gender.MALE;
                }
            } catch (Exception ignored) {
            }
            return Gender.MALE;
        }
        Gender fromCatalog = NpcGenderCatalog.getGender(speaker);
        if (fromCatalog != null
                && fromCatalog != Gender.UNKNOWN
                && fromCatalog != Gender.NEUTRAL) {
            return fromCatalog;
        }
        return Gender.MALE;
    }

    private static boolean isLocalPlayer(String speaker) {
        if (speaker == null || PlayerList.self == null || PlayerList.self.username == null) {
            return false;
        }
        String self = PlayerList.self.username.toString();
        return speaker.equalsIgnoreCase(self)
                || speaker.equalsIgnoreCase("you")
                || speaker.equalsIgnoreCase("player");
    }

    private static boolean isOsTts(String type) {
        return type.equals("sapi")
                || type.equals("system")
                || type.equals("windows")
                || type.equals("mac")
                || type.equals("linux")
                || type.equals("os");
    }

    private static ITextSpeaker createOsDefault() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return new WindowsSapiSpeaker();
        }
        if (os.contains("mac")) {
            return new MacSaySpeaker();
        }
        return new LinuxEspeakSpeaker();
    }


    public static boolean isSpeaking() {
        return speaking;
    }
}