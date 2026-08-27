package rt4.amilious.voice.speakers;

import rt4.amilious.npc.Gender;
import rt4.amilious.voice.TtsCache;
import rt4.amilious.voice.TtsMp3Player;
import rt4.amilious.voice.VoiceAssignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ElevenLabs text-to-speech.
 * Male/female voice ids from config; gender is passed in (resolved by Voiceover).
 *
 * Docs: POST https://api.elevenlabs.io/v1/text-to-speech/{voice_id}
 */
public final class ElevenLabsSpeaker implements ITextSpeaker {

    private static final String DEFAULT_MALE = "pNInz6obpgDQGcFmaJgB";
    private static final String DEFAULT_FEMALE = "21m00Tcm4TlvDq8ikWAM";
    private static final String API = "https://api.elevenlabs.io/v1/text-to-speech/";

    private final String apiKey;
    private final String maleVoiceId;
    private final String femaleVoiceId;

    private final AtomicInteger gen = new AtomicInteger();
    private Process playProcess;
    private volatile boolean disabled;
    private boolean missingLogged;

    public ElevenLabsSpeaker(String apiKey, String maleVoiceId, String femaleVoiceId) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.maleVoiceId = (maleVoiceId == null || maleVoiceId.isEmpty())
                ? DEFAULT_MALE : maleVoiceId.trim();
        this.femaleVoiceId = (femaleVoiceId == null || femaleVoiceId.isEmpty())
                ? DEFAULT_FEMALE : femaleVoiceId.trim();
    }

    @Override
    public void speak(String speaker, String text, Gender gender, Runnable onComplete) {
        if (disabled || text == null || text.isEmpty()) {
            return;
        }
        if (apiKey.isEmpty()) {
            disable("elevenLabsKey is empty");
            return;
        }
        if (gender == null || gender == Gender.UNKNOWN || gender == Gender.NEUTRAL) {
            gender = Gender.MALE;
        }

        var voice = VoiceAssignment.resolve(speaker, gender);
        if (voice == null || voice.isEmpty()) {
            voice = (gender == Gender.FEMALE) ? femaleVoiceId : maleVoiceId;
        }
        final String voiceId = voice;

        stop();
        final int g = gen.incrementAndGet();
        final Runnable done = onComplete;

        new Thread(() -> {
            try {
                File cache = TtsCache.file("elevenlabs",voiceId, text,"mp3");
                byte[] mp3;

                if (cache.isFile() && cache.length() > 0) {
                    System.out.println("[tts:elevenlabs] cache hit " + cache.getName());
                    mp3 = java.nio.file.Files.readAllBytes(cache.toPath());
                } else {
                    System.out.println("[tts:elevenlabs] cache miss → API");
                    mp3 = synthesize(text, voiceId);
                    if (mp3 != null && mp3.length > 0) {
                        java.nio.file.Files.write(cache.toPath(), mp3);
                    }
                }

                if (g != gen.get()) {
                    return;
                }
                if (mp3 == null || mp3.length == 0) {
                    disable("ElevenLabs returned empty audio");
                    return;
                }

                playMp3(mp3, g); // existing method (temp file + ffplay)

                if (g == gen.get() && done != null) {
                    done.run();
                }
            } catch (Exception e) {
                System.err.println("[tts:elevenlabs] " + e.getMessage());
            }
        }, "elevenlabs-speak").start();
    }



    private byte[] synthesize(String text, String voiceId) throws Exception {
        URL url = new URL(API + voiceId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("xi-api-key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "audio/mpeg");

        String body = "{\"text\":\"" + jsonEscape(text)
                + "\",\"model_id\":\"eleven_turbo_v2_5\"}";
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

        OutputStream os = conn.getOutputStream();
        os.write(bodyBytes);
        os.close();

        int code = conn.getResponseCode();
        InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (in != null) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) {
                bos.write(buf, 0, n);
            }
            in.close();
        }
        if (code < 200 || code >= 300) {
            throw new java.io.IOException("HTTP " + code + " " + bos.toString("UTF-8"));
        }
        return bos.toByteArray();
    }

    private void playMp3(byte[] mp3, int g) throws Exception {
        if (g != gen.get()) {
            return;
        }
        playProcess = TtsMp3Player.start(mp3);
        if (g != gen.get()) {
            TtsMp3Player.stop();
            playProcess = null;
            return;
        }
        if (playProcess == null) {
            if (!TtsMp3Player.canPlay()) {
                disable("No ffplay (tools/ffplay or PATH) / mpg123 / afplay");
            }
            return;
        }
        try {
            playProcess.waitFor();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            playProcess = null;
        }
    }

    @Override
    public void stop() {
        gen.incrementAndGet();
        playProcess = null;
        TtsMp3Player.stop();
    }


    private void disable(String msg) {
        disabled = true;
        if (!missingLogged) {
            missingLogged = true;
            System.err.println("[tts:elevenlabs] " + msg);
        }
    }

    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static boolean onPath(String bin) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                return new ProcessBuilder("where", bin).start().waitFor() == 0;
            }
            return new ProcessBuilder("which", bin).start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}