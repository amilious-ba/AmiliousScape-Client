package rt4.amilious.voice.speakers;

import rt4.amilious.npc.Gender;

/** Linux: espeak or espeak-ng, then spd-say. */
public final class LinuxEspeakSpeaker extends AbstractProcessSpeaker {

    private String binary; // cached first hit

    public LinuxEspeakSpeaker() {
        super("linux-espeak");
    }

    @Override
    public boolean isAvailable() {
        if (binary != null) {
            return true;
        }
        if (onPath("espeak-ng")) {
            binary = "espeak-ng";
            return true;
        }
        if (onPath("espeak")) {
            binary = "espeak";
            return true;
        }
        if (onPath("spd-say")) {
            binary = "spd-say";
            return true;
        }
        return false;
    }

    @Override
    protected String[] buildCommand(String speaker, String text, Gender gender) {
        if (!isAvailable()) {
            return null;
        }
        boolean female = gender == Gender.FEMALE;

        if ("spd-say".equals(binary)) {
            // -e = wait until finished; no reliable gender flag on all distros
            return new String[] { "spd-say", "-e", text };
        }

        // espeak / espeak-ng: en+f2 / en+m3 are common gender variants
        String voice = female ? "en+f2" : "en+m3";
        return new String[] { binary, "-v", voice, text };
    }

    @Override
    protected String hint() {
        return "Install with: sudo apt install espeak-ng   (or espeak / speech-dispatcher)";
    }

    private static boolean onPath(String bin) {
        try {
            Process p = new ProcessBuilder("which", bin).redirectErrorStream(true).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}