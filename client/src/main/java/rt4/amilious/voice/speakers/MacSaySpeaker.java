package rt4.amilious.voice.speakers;

import rt4.amilious.npc.Gender;

/** macOS: built-in `say`. */
public final class MacSaySpeaker extends AbstractProcessSpeaker {

    public MacSaySpeaker() {
        super("mac-say");
    }

    @Override
    public boolean isAvailable() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("mac")) {
            return false;
        }
        try {
            Process p = new ProcessBuilder("which", "say").redirectErrorStream(true).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String[] buildCommand(String speaker, String text, Gender gender) {
        // Common macOS voices: Alex (male), Samantha (female)
        String voice = (gender == Gender.FEMALE) ? "Samantha" : "Alex";
        return new String[] { "say", "-v", voice, text };
    }

    @Override
    protected String hint() {
        return "macOS `say` command missing.";
    }
}