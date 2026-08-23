package rt4.amilious.voice;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class TtsCache {

    private TtsCache() {
    }

    public static File jarDir() {
        try {
            File code = new File(
                    TtsCache.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI());
            return code.isFile() ? code.getParentFile() : code;
        } catch (Exception e) {
            return new File(".");
        }
    }

    public static File file(String provider, String voiceKey, String text, String ext) {
        String hash = sha256(provider + "|" + voiceKey + "|" + text);
        File dir = new File(jarDir(), "tts-cache/" + provider + "/" + safe(voiceKey));
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        return new File(dir, hash + "." + ext);
    }

    /*public static File elevenLabsMp3(String voiceId, String text) {
        String hash = sha256(voiceId + "|" + text);
        File dir = new File(jarDir(), "tts-cache/elevenlabs/" + safe(voiceId));
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        return new File(dir, hash + ".mp3");
    }*/

    private static String safe(String s) {
        return s == null ? "default" : s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}