package rt4.amilious.voice;

import java.io.File;

/**
 * Resolves ffplay: bundled tools/ffplay first, then PATH.
 */
public final class FfplayLocator {

    private static String cached;

    private FfplayLocator() {
    }

    /** Full path, "ffplay" if on PATH, or null. */
    public static String resolve() {
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }

        boolean win = os().contains("win");
        String name = win ? "ffplay.exe" : "ffplay";

        File cwd = new File("tools/ffplay", name);
        if (cwd.isFile()) {
            return cache(cwd.getAbsolutePath());
        }

        try {
            File jar = new File(
                    FfplayLocator.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI());
            File dir = jar.isFile() ? jar.getParentFile() : jar;
            File beside = new File(new File(dir, "tools/ffplay"), name);
            if (beside.isFile()) {
                return cache(beside.getAbsolutePath());
            }
        } catch (Exception ignored) {
        }

        if (onPath("ffplay")) {
            return cache("ffplay");
        }

        cache("");
        return null;
    }

    public static boolean isMac() {
        return os().contains("mac");
    }

    public static boolean canPlayMp3() {
        return isMac() || resolve() != null || (!os().contains("win") && onPath("mpg123"));
    }

    public static String[] playCommand(String filePath, int volumePercent) {
        if (volumePercent < 0) volumePercent = 0;
        if (volumePercent > 100) volumePercent = 100;

        if (isMac()) {
            return new String[] { "afplay", filePath };
        }
        String ff = resolve();
        if (ff != null) {
            return new String[] {
                    ff, "-nodisp", "-autoexit", "-loglevel", "quiet",
                    "-volume", String.valueOf(volumePercent),
                    filePath
            };
        }
        if (!os().contains("win") && onPath("mpg123")) {
            // mpg123 -f is 0–32768
            int amp = (volumePercent * 32768) / 100;
            return new String[] { "mpg123", "-q", "-f", String.valueOf(amp), filePath };
        }
        return null;
    }

    public static String[] playCommand(String filePath) {
        return playCommand(filePath, 100);
    }

    private static String cache(String v) {
        cached = v;
        return v.isEmpty() ? null : v;
    }

    private static String os() {
        return System.getProperty("os.name", "").toLowerCase();
    }

    static boolean onPath(String bin) {
        try {
            if (os().contains("win")) {
                return new ProcessBuilder("where", bin).start().waitFor() == 0;
            }
            return new ProcessBuilder("which", bin).start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}