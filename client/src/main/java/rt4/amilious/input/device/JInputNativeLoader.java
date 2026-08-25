package rt4.amilious.input.device;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;

/**
 * Extracts JInput native libraries from JAR to temporary directory.
 * JInput requires native DLLs/SOs to be on disk, not inside JAR.
 */
public class JInputNativeLoader {

    private static boolean loaded = false;
    private static Path nativeDir;

    /**
     * Extract and load JInput native libraries.
     * Call this before creating ControllerEnvironment.
     */
    public static synchronized void loadNatives() {
        if (loaded) {
            return;
        }

        try {
            // Determine OS and architecture
            String osName = System.getProperty("os.name").toLowerCase();
            String osArch = System.getProperty("os.arch").toLowerCase();

            String nativeLibName;
            if (osName.contains("win")) {
                nativeLibName = osArch.contains("64") ? "jinput-dx8_64.dll" : "jinput-dx8.dll";
            } else if (osName.contains("linux")) {
                nativeLibName = osArch.contains("64") ? "libjinput-linux64.so" : "libjinput-linux.so";
            } else if (osName.contains("mac")) {
                nativeLibName = "libjinput-osx.jnilib";
            } else {
                System.err.println("[JInput] Unsupported OS: " + osName);
                return;
            }

            // Create temp directory for natives
            nativeDir = Files.createTempDirectory("jinput-natives-");
            nativeDir.toFile().deleteOnExit();

            // Extract all potential native libraries from JAR
            extractNativeLibraries(nativeDir);

            // Set JInput-specific library path property (safer than reflection)
            System.setProperty("net.java.games.input.librarypath", nativeDir.toAbsolutePath().toString());

            // Also set java.library.path for fallback (but don't force reload - that breaks AWT!)
            String existingPath = System.getProperty("java.library.path", "");
            String newPath = nativeDir.toAbsolutePath().toString();
            if (!existingPath.isEmpty()) {
                newPath = newPath + File.pathSeparator + existingPath;
            }
            System.setProperty("java.library.path", newPath);

            System.out.println("[JInput] Extracted natives to: " + nativeDir);
            System.out.println("[JInput] Set net.java.games.input.librarypath=" + nativeDir);
            loaded = true;

        } catch (Exception e) {
            System.err.println("[JInput] Failed to load natives: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extract all JInput native libraries from JAR resources.
     */
    private static void extractNativeLibraries(Path targetDir) throws IOException {
        String[] nativeFiles = {
            "jinput-dx8.dll",
            "jinput-dx8_64.dll",
            "jinput-raw.dll",
            "jinput-raw_64.dll",
            "jinput-wintab.dll",
            "libjinput-linux.so",
            "libjinput-linux64.so",
            "libjinput-osx.jnilib"
        };

        for (String fileName : nativeFiles) {
            try {
                extractResource(fileName, targetDir);
            } catch (Exception e) {
                // File might not exist in JAR, that's OK
            }
        }
    }

    /**
     * Extract a single resource file from JAR to disk.
     */
    private static void extractResource(String resourceName, Path targetDir) throws IOException {
        // Try root of JAR first
        InputStream in = JInputNativeLoader.class.getResourceAsStream("/" + resourceName);
        if (in == null) {
            // Try in a lib/ folder
            in = JInputNativeLoader.class.getResourceAsStream("/lib/" + resourceName);
        }
        if (in == null) {
            return; // Resource not found
        }

        Path targetFile = targetDir.resolve(resourceName);
        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        targetFile.toFile().deleteOnExit();
        in.close();

        System.out.println("[JInput] Extracted: " + resourceName);
    }
}
