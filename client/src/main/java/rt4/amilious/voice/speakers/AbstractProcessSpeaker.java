package rt4.amilious.voice.speakers;

import rt4.amilious.Gender;

/**
 * Base for speakers that run an external process (PowerShell / curl / etc.).
 * Subclasses implement buildCommand(...); base handles gen / process lifecycle.
 */
public abstract class AbstractProcessSpeaker implements ITextSpeaker {

    protected final String id;

    protected volatile int gen = 0;
    protected Process process;

    protected AbstractProcessSpeaker(String id) {
        this.id = id != null ? id : getClass().getSimpleName();
    }

    public String getId() {
        return id;
    }

    /** Override if the backend needs a platform check. Default: available. */
    public boolean isAvailable() {
        return true;
    }

    /** Short message when unavailable. */
    protected String hint() {
        return id + " unavailable.";
    }

    @Override
    public final void speak(String speaker, String text, Gender gender, Runnable onComplete) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!isAvailable()) {
            System.err.println("[" + id + "] " + hint());
            return;
        }
        if (gender == null || gender == Gender.UNKNOWN || gender == Gender.NEUTRAL) {
            gender = Gender.MALE;
        }

        final int myGen = ++gen;
        stopProcessOnly();

        final Gender g = gender;
        final String sp = speaker;
        final String tx = text;
        final Runnable done = onComplete;

        Thread t = new Thread(() -> {
            try {
                if (myGen != gen) {
                    return;
                }
                Process p = startProcess(sp, tx, g);
                if (p == null) {
                    return;
                }
                synchronized (this) {
                    if (myGen != gen) {
                        p.destroy();
                        return;
                    }
                    process = p;
                }
                p.waitFor();
                if (myGen == gen && done != null) {
                    done.run();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[" + id + "] " + e.getMessage());
            } finally {
                synchronized (this) {
                    if (myGen == gen) {
                        process = null;
                    }
                }
            }
        }, id + "-speak");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public final void stop() {
        gen++;
        stopProcessOnly();
    }

    private void stopProcessOnly() {
        Process p;
        synchronized (this) {
            p = process;
            process = null;
        }
        if (p != null) {
            try {
                p.destroy();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Default: buildCommand → ProcessBuilder.
     * Override only if you need a custom process (pipes, env, etc.).
     */
    protected Process startProcess(String speaker, String text, Gender gender) throws Exception {
        String[] cmd = buildCommand(speaker, text, gender);
        if (cmd == null || cmd.length == 0) {
            return null;
        }
        return new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();
    }

    /** OS argv for this utterance. */
    protected abstract String[] buildCommand(String speaker, String text, Gender gender);

    /** Escape for PowerShell single-quoted strings. */
    protected static String shellSingleQuote(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("'", "''");
    }
}