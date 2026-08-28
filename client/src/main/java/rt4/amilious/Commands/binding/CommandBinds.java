package rt4.amilious.Commands.binding;

import rt4.Chat;
import rt4.Cheat;
import rt4.JagString;
import rt4.amilious.debug.DebugConsole;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class CommandBinds {

    public static final int MAX_SLOTS = 10;
    private static final String FILE_NAME = "binds.txt";
    private static final String[] binds = new String[MAX_SLOTS];

    private CommandBinds() {}

    /** Call once from AmiliousClient.Init() or DebugConsole.Init(). */
    public static void load() {
        File file = bindsFile();
        if (!file.isFile()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                try {
                    int slot = Integer.parseInt(line.substring(0, eq).trim());
                    String cmd = line.substring(eq + 1).trim();
                    if (slot >= 0 && slot < MAX_SLOTS && !cmd.isEmpty()) {
                        binds[slot] = cmd;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            DebugConsole.log("Loaded binds from " + file.getAbsolutePath());
        } catch (IOException e) {
            DebugConsole.log("Failed to load binds: " + e.getMessage());
        }
    }

    private static void save() {
        File file = bindsFile();
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write("# AmiliousScape command binds");
            bw.newLine();
            bw.write("# slot=::command");
            bw.newLine();
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (binds[i] != null && !binds[i].isEmpty()) {
                    bw.write(i + "=" + binds[i]);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            chat("Failed to save binds: " + e.getMessage());
            DebugConsole.log("Failed to save binds: " + e.getMessage());
        }
    }

    private static File bindsFile() {
        try {
            // Location of the code source (the client jar, or classes/ in IDE)
            File codeSource = new File(
                    CommandBinds.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            File dir;
            if (codeSource.isFile()) {
                // Running from a .jar → use the jar's parent folder
                dir = codeSource.getParentFile();
            } else {
                // IDE / classes dir → fall back to working directory
                dir = new File(".");
            }

            return new File(dir, FILE_NAME);
        } catch (Exception e) {
            // Last resort
            return new File(FILE_NAME);
        }
    }

    public static void set(int slot, String command) {
        if (slot < 0 || slot >= MAX_SLOTS) return;
        binds[slot] = command;
        save();
    }

    public static void clear(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return;
        binds[slot] = null;
        save();
    }

    public static String get(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return null;
        return binds[slot];
    }

    public static boolean run(int slot) {
        String cmd = get(slot);
        if (cmd == null || cmd.isEmpty()) return false;
        Cheat.sendCheatPacket(JagString.parse(cmd));
        DebugConsole.log("ran bind " + slot + " → " + cmd);
        return true;
    }

    public static void chat(String msg) {
        Chat.add(null, 0, JagString.parse(msg));
    }
}