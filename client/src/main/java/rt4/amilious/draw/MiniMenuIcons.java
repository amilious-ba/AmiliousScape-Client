package rt4.amilious.draw;

import rt4.CursorType;
import rt4.CursorTypeList;
import rt4.GlRenderer;
import rt4.GlSprite;
import rt4.MiniMenu;
import rt4.SoftwareSprite;
import rt4.Sprite;
import rt4.Sprites;

import java.util.HashMap;

/**
 * Left-of-text icons for MiniMenuDrawer.
 * Prefers the option's cursor sprite (same art the mouse already uses).
 */
public final class MiniMenuIcons {

    public static final int SLOT = 20;

    private static final HashMap<Integer, Sprite> cursorCache = new HashMap<Integer, Sprite>();

    private MiniMenuIcons() {
    }

    public static Sprite forIndex(int index) {
        if (index < 0 || index >= MiniMenu.size) {
            return null;
        }

        int cursorId = MiniMenu.cursors[index];
        if (cursorId > 0) {
            Sprite fromCursor = spriteForCursor(cursorId);
            if (fromCursor != null) {
                return fromCursor;
            }
        }

        return fallbackForText(stripColors(MiniMenu.getOp(index).toString()).toLowerCase());
    }

    public static void render(Sprite sprite, int x, int y, int slotH) {
        if (sprite == null) {
            return;
        }
        int max = Math.min(SLOT - 2, slotH - 2);
        int sw = sprite.width;
        int sh = sprite.height;
        if (sw <= 0 || sh <= 0) {
            return;
        }
        int dw = sw;
        int dh = sh;
        if (sw > max || sh > max) {
            float s = Math.min(max / (float) sw, max / (float) sh);
            dw = Math.max(1, (int) (sw * s));
            dh = Math.max(1, (int) (sh * s));
        }
        int drawX = x + (SLOT - dw) / 2;
        int drawY = y + (slotH - dh) / 2;
        sprite.renderResized(drawX, drawY, dw, dh);
    }

    private static Sprite spriteForCursor(int cursorId) {
        Sprite cached = cursorCache.get(cursorId);
        if (cached != null) {
            return cached;
        }
        try {
            CursorType type = CursorTypeList.get(cursorId);
            if (type == null) {
                return null;
            }
            SoftwareSprite raw = type.getSprite();
            if (raw == null) {
                return null;
            }
            Sprite ready;
            if (GlRenderer.enabled) {
                ready = new GlSprite(raw);
            } else {
                ready = raw;
            }
            cursorCache.put(cursorId, ready);
            return ready;
        } catch (Exception e) {
            return null;
        }
    }

    private static Sprite fallbackForText(String op) {
        if (op.startsWith("walk here")) {
            return Sprites.mapflags;
        }
        if (op.startsWith("attack") || op.contains("attack ")) {
            return first(Sprites.headiconPks);
        }
        if (op.startsWith("talk")) {
            return first(Sprites.headhints);
        }
        if (op.startsWith("take") || op.startsWith("drop") || op.startsWith("use")) {
            return first(Sprites.mapdots);
        }
        if (op.startsWith("mine") || op.startsWith("prospect")) {
            return mapfuncOrNull();
        }
        if (op.startsWith("chop") || op.contains("chop down")) {
            return mapfuncOrNull();
        }
        if (op.startsWith("fish") || op.startsWith("bait") || op.startsWith("lure") || op.startsWith("net")) {
            return mapfuncOrNull();
        }
        return null;
    }

    private static Sprite first(Sprite[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        return arr[0];
    }

    private static Sprite mapfuncOrNull() {
        if (Sprites.mapfuncs != null && Sprites.mapfuncs.length > 0) {
            return Sprites.mapfuncs[0];
        }
        return null;
    }

    private static String stripColors(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '<' ) {
                int end = s.indexOf('>', i);
                if (end >= 0) {
                    i = end + 1;
                    continue;
                }
            }
            out.append(s.charAt(i));
            i++;
        }
        return out.toString().trim();
    }
}