package rt4.amilious.voice.speakers;

import rt4.amilious.npc.Gender;
import rt4.amilious.voice.TtsCache;
import rt4.amilious.voice.TtsMp3Player;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OpenAI Audio Speech API.
 * POST https://api.openai.com/v1/audio/speech
 */
public final class OpenAiSpeaker implements ITextSpeaker {

    private static final String API = "https://api.openai.com/v1/audio/speech";

    private final String apiKey;
    private final String model;
    private final String maleVoice;
    private final String femaleVoice;

    private final AtomicInteger gen = new AtomicInteger();
    private Process playProcess;
    private volatile boolean disabled;
    private boolean missingLogged;

    public OpenAiSpeaker(String apiKey, String model, String maleVoice, String femaleVoice) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = (model == null || model.isEmpty()) ? "tts-1" : model.trim();
        this.maleVoice = (maleVoice == null || maleVoice.isEmpty()) ? "onyx" : maleVoice.trim();
        this.femaleVoice = (femaleVoice == null || femaleVoice.isEmpty()) ? "nova" : femaleVoice.trim();
    }

    @Override
    public void speak(String speaker, String text, Gender gender, Runnable onComplete) {
        if (disabled || text == null || text.isEmpty()) {
            return;
        }
        if (apiKey.isEmpty()) {
            disable("openaiKey is empty");
            return;
        }
        if (gender == null || gender == Gender.UNKNOWN || gender == Gender.NEUTRAL) {
            gender = Gender.MALE;
        }

        final String voice = (gender == Gender.FEMALE) ? femaleVoice : maleVoice;
        stop();
        final int g = gen.incrementAndGet();
        final Runnable done = onComplete;

        Thread t = new Thread(() -> {
            try {
                File cache = TtsCache.file("openai", voice + "_" + model, text, ".mp3");
                byte[] mp3;

                if (cache.isFile() && cache.length() > 0) {
                    System.out.println("[tts:openai] cache hit " + cache.getName());
                    mp3 = Files.readAllBytes(cache.toPath());
                } else {
                    System.out.println("[tts:openai] cache miss → API");
                    mp3 = synthesize(text, voice);
                    if (mp3 != null && mp3.length > 0) {
                        Files.write(cache.toPath(), mp3);
                    }
                }

                if (g != gen.get()) {
                    return;
                }
                if (mp3 == null || mp3.length == 0) {
                    disable("OpenAI returned empty audio");
                    return;
                }

                playMp3(mp3, g);

                if (g == gen.get() && done != null) {
                    done.run();
                }
            } catch (Exception e) {
                System.err.println("[tts:openai] " + e.getMessage());
            }
        }, "openai-speak");
        t.setDaemon(true);
        t.start();
    }

    private byte[] synthesize(String text, String voice) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(API).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        String body = "{\"model\":\"" + jsonEscape(model) + "\","
                + "\"input\":\"" + jsonEscape(text) + "\","
                + "\"voice\":\"" + jsonEscape(voice) + "\","
                + "\"response_format\":\"mp3\"}";

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        OutputStream os = conn.getOutputStream();
        os.write(bodyBytes);
        os.close();

        int code = conn.getResponseCode();
        InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while (in != null && (n = in.read(buf)) >= 0) {
            bos.write(buf, 0, n);
        }
        if (in != null) {
            in.close();
        }
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("OpenAI HTTP " + code + " " + bos.toString("UTF-8"));
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
            System.err.println("[tts:openai] " + msg);
        }
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
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