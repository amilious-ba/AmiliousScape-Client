package rt4.amilious.voice.speakers;

import rt4.amilious.Gender;
import rt4.amilious.voice.speakers.ITextSpeaker;

import java.io.ByteArrayOutputStream;
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

        final String voiceId = (gender == Gender.FEMALE) ? femaleVoiceId : maleVoiceId;
        stop();
        final int g = gen.incrementAndGet();

        new Thread(() -> {
            try {
                byte[] mp3 = synthesize(text, voiceId);
                if (g != gen.get()) {
                    return;
                }
                if (mp3 == null || mp3.length == 0) {
                    disable("ElevenLabs returned empty audio");
                    return;
                }
                playMp3(mp3, g);
                if (g == gen.get() && onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                System.err.println("[tts:elevenlabs] " + e.getMessage());
                if (e instanceof java.io.IOException) {
                    disable("ElevenLabs request failed: " + e.getMessage());
                }
            }
        }, "tts-elevenlabs").start();
    }

    @Override
    public void stop() {
        gen.incrementAndGet();
        Process p = playProcess;
        playProcess = null;
        if (p != null) {
            try {
                p.destroy();
            } catch (Exception ignored) {
            }
        }
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
        java.io.File tmp = java.io.File.createTempFile("amilious-tts-", ".mp3");
        tmp.deleteOnExit();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
        fos.write(mp3);
        fos.close();

        if (g != gen.get()) {
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        String[] cmd;
        if (os.contains("win")) {
            if (onPath("ffplay")) {
                cmd = new String[] {
                        "ffplay", "-nodisp", "-autoexit", "-loglevel", "quiet", tmp.getAbsolutePath()
                };
            } else {
                // SoundPlayer is WAV-only — needs ffplay for real mp3, or convert first
                disable("Install ffplay (ffmpeg) to play ElevenLabs mp3 on Windows");
                return;
            }
        } else if (os.contains("mac")) {
            cmd = new String[] { "afplay", tmp.getAbsolutePath() };
        } else if (onPath("ffplay")) {
            cmd = new String[] {
                    "ffplay", "-nodisp", "-autoexit", "-loglevel", "quiet", tmp.getAbsolutePath()
            };
        } else if (onPath("mpg123")) {
            cmd = new String[] { "mpg123", "-q", tmp.getAbsolutePath() };
        } else {
            disable("No mp3 player (ffplay/mpg123). Install ffmpeg or mpg123.");
            return;
        }

        playProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        playProcess.waitFor();
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