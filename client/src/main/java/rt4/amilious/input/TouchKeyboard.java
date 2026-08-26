package rt4.amilious.input;

import java.io.File;
import java.util.Locale;

/**
 * Opens the Windows touch keyboard when running on a handheld-style device.
 * Prefers TabTip.exe (modern OSK); falls back to osk.exe.
 * No-ops on non-Windows.
 */
public final class TouchKeyboard {

    private static final String[] TABTIP_PATHS = {
            envPath("ProgramFiles") + "\\Common Files\\Microsoft Shared\\ink\\TabTip.exe",
            envPath("ProgramFiles(x86)") + "\\Common Files\\Microsoft Shared\\ink\\TabTip.exe",
            "C:\\Program Files\\Common Files\\Microsoft Shared\\ink\\TabTip.exe",
            "C:\\Program Files (x86)\\Common Files\\Microsoft Shared\\ink\\TabTip.exe"
    };
    private static boolean tipShown = false;

    private TouchKeyboard() {
    }

    /** True if OS is Windows and TabTip.exe exists on disk. */
    public static boolean isTabTipAvailable() {
        return isWindows() && findTabTip() != null;
    }

    /**
     * Heuristic: Windows + (TabTip present or ROG / Ally-style hints).
     * Not perfect — override with config if needed.
     */
    public static boolean isLikelyHandheld() {
        if (!isWindows()) {
            return false;
        }
        if (isTabTipAvailable()) {
            return true;
        }
        String product = System.getenv("COMPUTERNAME");
        String bios = ""; // optional: leave empty unless you query WMI later
        String joined = ((product != null ? product : "") + " " + bios).toLowerCase(Locale.ROOT);
        return joined.contains("ally") || joined.contains("legion go") || joined.contains("deck");
    }

    /** Show keyboard if Windows; prefers TabTip when found. */
    public static void show() {
        show(false);
    }

    /**
     * @param onlyIfHandheld if true, no-op unless {@link #isLikelyHandheld()}
     */
    public static void show(boolean onlyIfHandheld) {
        if (!isWindows()) {
            return;
        }
        if (onlyIfHandheld && !isLikelyHandheld()) {
            System.out.println("[TouchKeyboard] skip — not treated as handheld");
            return;
        }
        if(!tipShown){
            System.out.println("[TouchKeyboard] If TabTip process starts but no UI, set "
                    + "HKCU\\Software\\Microsoft\\TabletTip\\1.7\\EnableDesktopModeAutoInvoke=1 "
                    + "then retry (or use osk fallback).");
            tipShown = true;
        }
        File tabTip = findTabTip();
        if (tabTip != null) {
            if (launchTabTip(tabTip)) {
                return;
            }
        }
        launchOsk();
    }

    /** Close TabTip / osk if running. */
    public static void hide() {
        if (!isWindows()) {
            return;
        }
        taskkill("TabTip.exe");
        taskkill("osk.exe");
    }

    /** Show if hidden-style launch; hide is separate — this always tries to show. */
    public static void toggle() {
        if (!isWindows()) {
            return;
        }
        // Simple policy: kill then show (acts as open). Call hide() to close only.
        show(false);
    }

    // ---- internals ----

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase(Locale.ROOT).contains("win");
    }

    private static String envPath(String key) {
        String v = System.getenv(key);
        return v != null ? v : "";
    }

    private static File findTabTip() {
        for (String path : TABTIP_PATHS) {
            if (path == null || path.length() < 8) {
                continue;
            }
            File f = new File(path);
            if (f.isFile()) {
                return f;
            }
        }
        return null;
    }

    private static boolean launchTabTip(File tabTip) {
        String path = tabTip.getAbsolutePath();

        // 1) Preferred: shell start (avoids many 740 cases)
        if (shellStart(path)) {
            System.out.println("[TouchKeyboard] TabTip via shell: " + path);
            return true;
        }

        // 2) explorer.exe
        try {
            new ProcessBuilder("explorer.exe", path).start();
            System.out.println("[TouchKeyboard] TabTip via explorer: " + path);
            return true;
        } catch (Exception e) {
            System.err.println("[TouchKeyboard] explorer failed: " + e);
        }

        return false;
    }

    private static boolean shellStart(String path) {
        try {
            // kill idle TabTip first (optional)
            taskkill("TabTip.exe");
            Thread.sleep(150);

            Process p = new ProcessBuilder(
                    "cmd.exe", "/c", "start", "", path
            ).start();
            // don't wait forever on start
            return true;
        } catch (Exception e) {
            System.err.println("[TouchKeyboard] shell start failed: " + e);
            return false;
        }
    }

    private static void launchOsk() {
        try {
            new ProcessBuilder("cmd.exe", "/c", "start", "", "osk.exe").start();
            System.out.println("[TouchKeyboard] fallback osk.exe");
        } catch (Exception e) {
            System.err.println("[TouchKeyboard] osk failed: " + e);
        }
    }

    private static void taskkill(String image) {
        try {
            Process p = new ProcessBuilder("taskkill", "/F", "/IM", image)
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
        } catch (Exception ignored) {
        }
    }
}