package rt4.amilious.voice;

import rt4.Preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Writes TTS bytes to a temp mp3 and starts playback.
 * Voiceover.stop() / speaker.stop() must call TtsMp3Player.stop().
 */
public final class TtsMp3Player {

    private static volatile Process current;

    private TtsMp3Player() {
    }

    /**
     * @return running process, or null if no player is available
     */
    public static Process start(byte[] mp3) throws Exception {
        stop();

        if (mp3 == null || mp3.length == 0) {
            return null;
        }

        File tmp = File.createTempFile("amilious-tts-", ".mp3");
        tmp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmp);
        try {
            fos.write(mp3);
        } finally {
            fos.close();
        }

        String[] cmd = FfplayLocator.playCommand(tmp.getAbsolutePath(), volumePercent());
        if (cmd == null) {
            return null;
        }

        current = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        return current;
    }

    public static void stop() {
        Process p = current;
        current = null;
        if (p == null) {
            return;
        }
        try {
            p.destroy();
            if (!p.waitFor(200, TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean canPlay() {
        return FfplayLocator.canPlayMp3();
    }

    /** 0–100 from in-game sound effects slider (0–127). */
    public static int volumePercent() {
        int v = Preferences.soundEffectVolume;
        if (v <= 0) {
            return 0;
        }
        if (v >= 127) {
            return 100;
        }
        return (v * 100) / 127;
    }
}