package rt4.amilious;

import rt4.ui.Fonts;
import rt4.util.JagString;
import rt4.amilious.cheats.DebugClear_Command;
import rt4.amilious.cheats.DebugLog_Command;
import rt4.amilious.cheats.DebugShowInteractions_Command;
import rt4.amilious.cheats.Debug_Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DebugConsole {

    public static boolean enabled = false;
    public static boolean showInteractions = false;

    /** Total time on screen (ms). */
    public static long LINE_TTL_MS = 7000;
    /** Fade / slide in at start (ms). */
    public static long FADE_IN_MS = 350;
    /** Fade / slide out at end (ms). */
    public static long FADE_OUT_MS = 500;
    /** How far a line slides (pixels). */
    public static int SLIDE_PX = 12;

    private static final int MAX_LINES = 6;
    private static final int LINE_HEIGHT = 15;

    /** Tooltip-ish: yellow + black shadow (matches top hover look). */
    private static final int COLOR = 0xFFFF00;
    private static final int SHADOW = 0;

    private static final List<Entry> lines = new ArrayList<>();

    public static void Init() {
        AmiliousClient.AddCommand(new Debug_Command());
        AmiliousClient.AddCommand(new DebugClear_Command());
        AmiliousClient.AddCommand(new Debug_Command());
        AmiliousClient.AddCommand(new DebugLog_Command());
        AmiliousClient.AddCommand(new DebugShowInteractions_Command());
    }

    private static final class Entry {
        final String text;
        final long bornAt;
        final long expiresAt;
        /** Display slot 0 = top; animates when rows above are removed. */
        float slot;
        float slotTarget;

        Entry(String text, long now) {
            this.text = text;
            this.bornAt = now;
            this.expiresAt = now + LINE_TTL_MS;
            this.slot = MAX_LINES;      // start below stack
            this.slotTarget = 0;        // set when inserted
        }

        float lifeAlpha(long now) {
            long age = now - bornAt;
            long left = expiresAt - now;
            if (left <= 0) {
                return 0f;
            }
            float a = 1f;
            if (age < FADE_IN_MS) {
                a = age / (float) FADE_IN_MS;
            }
            if (left < FADE_OUT_MS) {
                a = Math.min(a, left / (float) FADE_OUT_MS);
            }
            return clamp01(a);
        }

        /** 0 = settled, 1 = fully slid from below during fade-in. */
        float slideIn(long now) {
            long age = now - bornAt;
            if (age >= FADE_IN_MS) {
                return 0f;
            }
            return 1f - age / (float) FADE_IN_MS;
        }

        /** Extra downward drift while fading out. */
        float slideOut(long now) {
            long left = expiresAt - now;
            if (left >= FADE_OUT_MS || left <= 0) {
                return 0f;
            }
            return 1f - left / (float) FADE_OUT_MS;
        }
    }

    private DebugConsole() {
    }

    public static void log(String message) {
        if (message == null) {
            return;
        }
        long now = System.currentTimeMillis();
        synchronized (lines) {
            Entry e = new Entry(message, now);
            lines.add(e);
            while (lines.size() > MAX_LINES) {
                lines.remove(0);
            }
            reindexSlots();
            e.slot = e.slotTarget + 1.2f; // drop in from below its target
        }
        System.out.println("[Amilious] " + message);
    }

    public static void clear() {
        synchronized (lines) {
            lines.clear();
        }
    }

    public static void toggle() {
        enabled = !enabled;
        log("console " + (enabled ? "ON" : "OFF"));
    }

    private static void reindexSlots() {
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).slotTarget = i;
        }
    }

    public static void draw() {
        if (!enabled || Fonts.p12Full == null) {
            return;
        }

        long now = System.currentTimeMillis();
        List<Entry> live = new ArrayList<>();

        synchronized (lines) {
            Iterator<Entry> it = lines.iterator();
            while (it.hasNext()) {
                Entry e = it.next();
                if (now >= e.expiresAt) {
                    it.remove();
                    continue;
                }
                live.add(e);
            }
            reindexSlots();

            // smooth slot move (following lines slide up)
            for (Entry e : lines) {
                float d = e.slotTarget - e.slot;
                e.slot += d * 0.25f; // lerp speed
                if (Math.abs(d) < 0.02f) {
                    e.slot = e.slotTarget;
                }
            }
            live.clear();
            live.addAll(lines);
        }

        // Below top hover tooltip strip
        int baseX = 8;
        int baseY = 48;

        for (Entry e : live) {
            float a = e.lifeAlpha(now);
            if (a <= 0.02f) {
                continue;
            }

            int y = baseY
                    + Math.round(e.slot * LINE_HEIGHT)
                    + Math.round(e.slideIn(now) * SLIDE_PX)
                    + Math.round(e.slideOut(now) * SLIDE_PX);

            // Font API has no real alpha — dim color while fading
            int color = a >= 0.85f ? COLOR : mixRgb(COLOR, 0x808080, 1f - a);

            // Same idea as top tooltip: yellow + shadow 0
            Fonts.p12Full.renderLeft(JagString.parse(e.text), baseX, y, color, SHADOW);
        }
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static int mixRgb(int c1, int c2, float t) {
        t = clamp01(t);
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (r << 16) | (g << 8) | b;
    }
}