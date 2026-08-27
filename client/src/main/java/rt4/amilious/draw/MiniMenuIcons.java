package rt4.amilious.draw;

import rt4.CursorType;
import rt4.CursorTypeList;
import rt4.GlRenderer;
import rt4.GlSprite;
import rt4.JagString;
import rt4.Js5;
import rt4.MiniMenu;
import rt4.SoftwareSprite;
import rt4.Sprite;
import rt4.SpriteLoader;
import rt4.Sprites;

import java.util.HashMap;

public final class MiniMenuIcons {

    public static final int SLOT = 20;

    private static final HashMap<Integer, Sprite> cursorCache = new HashMap<Integer, Sprite>();

    private static Sprite[] staticons;
    private static Sprite[] staticons2;

    private MiniMenuIcons() {
    }

    /** Call once from client mainLoad state 140, BEFORE js5Archive8.discardNames(true). */
    public static void preload(Js5 archive) {
        if (archive == null) {
            return;
        }
        try {
            staticons = loadNamed(archive, "staticons");
            staticons2 = loadNamed(archive, "staticons2");
            System.out.println("[menu-icons] staticons="
                    + (staticons == null ? 0 : staticons.length)
                    + " staticons2="
                    + (staticons2 == null ? 0 : staticons2.length));
        } catch (Exception e) {
            System.out.println("[menu-icons] preload failed: " + e);
            e.printStackTrace();
        }
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
        sprite.renderResized(x + (SLOT - dw) / 2, y + (slotH - dh) / 2, dw, dh);
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
            Sprite ready = GlRenderer.enabled ? new GlSprite(raw) : raw;
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
        if (op.startsWith("attack")) {
            return skill(0);
        }
        if (op.startsWith("chop") || op.contains("chop down")) {
            return skill2(1);
        }
        if (op.startsWith("mine") || op.startsWith("prospect")) {
            return skill2(7);
        }
        if (op.startsWith("fish") || op.startsWith("bait") || op.startsWith("lure") || op.startsWith("net")) {
            return skill2(3);
        }
        if (op.startsWith("cook")) {
            return skill2(0);
        }
        if (op.startsWith("smith") || op.startsWith("smelt")) {
            return skill2(6);
        }
        if (op.startsWith("craft")) {
            return skill2(5);
        }
        if (op.startsWith("fletch")) {
            return skill2(2);
        }
        if (op.startsWith("steal") || op.startsWith("pickpocket") || op.startsWith("pick-lock")) {
            return skill2(10);
        }
        if (op.startsWith("pray")) {
            return skill(5);
        }
        if (op.startsWith("cast")) {
            return skill(6);
        }
        if (op.startsWith("talk")) {
            return first(Sprites.headhints);
        }
        return null;
    }

    private static Sprite skill(int file) {
        if (staticons == null || file < 0 || file >= staticons.length) {
            return null;
        }
        return staticons[file];
    }

    private static Sprite skill2(int file) {
        if (staticons2 == null || file < 0 || file >= staticons2.length) {
            return null;
        }
        return staticons2[file];
    }

    private static Sprite[] loadNamed(Js5 archive, String name) {
        int group = archive.getGroupId(JagString.parse(name));
        System.out.println("[menu-icons] group " + name + "=" + group);
        if (group < 0) {
            return null;
        }
        SoftwareSprite[] raw = SpriteLoader.loadSoftwareSprites(group, archive);
        if (raw == null || raw.length == 0) {
            return null;
        }
        Sprite[] out = new Sprite[raw.length];
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] == null) {
                continue;
            }
            out[i] = GlRenderer.enabled ? new GlSprite(raw[i]) : raw[i];
        }
        return out;
    }

    private static Sprite first(Sprite[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        return arr[0];
    }

    private static String stripColors(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '<') {
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