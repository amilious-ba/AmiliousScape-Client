package rt4.amilious.menu;

import rt4.CursorType;
import rt4.CursorTypeList;
import rt4.GlRenderer;
import rt4.GlSprite;
import rt4.InterfaceList;
import rt4.MiniMenu;
import rt4.SoftwareSprite;
import rt4.Sprite;
import rt4.SpriteLoader;
import rt4.Sprites;

import java.util.HashMap;

public final class MiniMenuIcons {

    public static final int SLOT = 20;

    public static final int SPR_ATTACK = 197;
    public static final int SPR_STRENGTH = 198;
    public static final int SPR_DEFENCE = 199;
    public static final int SPR_RANGED = 200;
    public static final int SPR_PRAYER = 201;
    public static final int SPR_MAGIC = 202;
    public static final int SPR_HITPOINTS = 203;
    public static final int SPR_AGILITY = 204;
    public static final int SPR_HERBLORE = 205;
    public static final int SPR_THIEVING = 206;
    public static final int SPR_CRAFTING = 207;
    public static final int SPR_FLETCHING = 208;
    public static final int SPR_MINING = 209;
    public static final int SPR_SMITHING = 210;
    public static final int SPR_FISHING = 211;
    public static final int SPR_COOKING = 212;
    public static final int SPR_FIREMAKING = 213;
    public static final int SPR_WOODCUTTING = 214;
    public static final int SPR_RUNECRAFT = 215;
    public static final int SPR_SLAYER = 216;
    public static final int SPR_FARMING = 217;
    public static final int SPR_HUNTER = 220;
    public static final int SPR_CONSTRUCTION = 221;
    public static final int SPR_SUMMONING = 222;

    /** 530 mapfunction bank booth. Change if 5 is not the bank. */
    public static final int MAPFUNC_BANK = 5;

    private static final HashMap<Integer, Sprite> cursorCache = new HashMap<Integer, Sprite>();
    private static final HashMap<Integer, Sprite> uiCache = new HashMap<Integer, Sprite>();

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

    private static Sprite ui(int spriteId) {
        Sprite cached = uiCache.get(spriteId);
        if (cached != null) {
            return cached;
        }
        if (InterfaceList.aClass153_12 == null) {
            return null;
        }
        try {
            SoftwareSprite raw = SpriteLoader.loadSoftwareSprite(0, InterfaceList.aClass153_12, spriteId);
            if (raw == null) {
                return null;
            }
            Sprite ready = GlRenderer.enabled ? new GlSprite(raw) : raw;
            uiCache.put(spriteId, ready);
            return ready;
        } catch (Exception e) {
            return null;
        }
    }

    private static Sprite fallbackForText(String op) {
        if (op.startsWith("cancel")) {
            return cross(4);
        }
        if (op.startsWith("walk here")) {
            return Sprites.mapflags;
        }
        if (op.startsWith("follow")) {
            return followArrows();
        }
        if (op.startsWith("trade")) {
            return dot(0);
        }
        if (op.startsWith("req assist") || op.startsWith("req-assist") || op.contains("assist")) {
            return ui(SPR_HITPOINTS);
        }
        if (op.startsWith("teleport")) {
            return ui(SPR_MAGIC);
        }
        if (op.startsWith("view stats") || op.equals("stats")) {
            Sprite tab = tabIcon(rt4.amilious.menutab.MenuTab.STATS);
            return tab != null ? tab : ui(SPR_HITPOINTS); // fallback: HP icon
        }
        if (op.startsWith("examine")) {
            return magnifier();
        }
        if (op.startsWith("talk")) {
            return speechBubble();
        }
        if (op.contains("bank") && !op.startsWith("talk") && !op.startsWith("examine")) {
            return bankIcon();
        }
        if (op.startsWith("use-quickly") || op.startsWith("use quickly")) {
            return bankIcon();
        }
        if (op.startsWith("collect")) {
            return bankIcon();
        }
        if (op.startsWith("take") || op.startsWith("pick-up") || op.startsWith("pickup")) {
            return dot(2);
        }
        if (op.startsWith("attack") && op.contains("level")) {
            Sprite pk = first(Sprites.headiconPks);
            return pk != null ? pk : ui(SPR_ATTACK);
        }
        if (op.startsWith("attack")) {
            return ui(SPR_ATTACK);
        }
        if (op.startsWith("chop") || op.contains("chop down")) {
            return ui(SPR_WOODCUTTING);
        }
        if (op.startsWith("mine") || op.startsWith("prospect")) {
            return ui(SPR_MINING);
        }
        if (op.startsWith("fish") || op.startsWith("bait") || op.startsWith("lure") || op.startsWith("net")) {
            return ui(SPR_FISHING);
        }
        if (op.startsWith("cook")) {
            return ui(SPR_COOKING);
        }
        if (op.startsWith("smith") || op.startsWith("smelt")) {
            return ui(SPR_SMITHING);
        }
        if (op.startsWith("craft")) {
            return ui(SPR_CRAFTING);
        }
        if (op.startsWith("fletch")) {
            return ui(SPR_FLETCHING);
        }
        if (op.startsWith("steal") || op.startsWith("pickpocket") || op.startsWith("pick-lock")) {
            return ui(SPR_THIEVING);
        }
        if (op.startsWith("pray")) {
            return ui(SPR_PRAYER);
        }
        if (op.startsWith("cast") || op.startsWith("magic")) {
            return ui(SPR_MAGIC);
        }
        if (op.startsWith("bury") || op.contains("bones")) {
            return ui(SPR_PRAYER);
        }
        if (op.startsWith("light") || op.startsWith("burn")) {
            return ui(SPR_FIREMAKING);
        }
        return null;
    }

    private static Sprite first(Sprite[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        return arr[0];
    }

    private static Sprite dot(int i) {
        if (Sprites.mapdots == null || i < 0 || i >= Sprites.mapdots.length) {
            return null;
        }
        return Sprites.mapdots[i];
    }

    private static Sprite cross(int i) {
        if (Sprites.crosses == null || Sprites.crosses.length == 0) {
            return null;
        }
        if (i < 0 || i >= Sprites.crosses.length) {
            i = Sprites.crosses.length - 1;
        }
        return Sprites.crosses[i];
    }

    private static Sprite mapfunc(int i) {
        Sprite[] arr = Sprites.mapfuncs;
        if (arr == null) {
            arr = Sprites.mapfunctions;
        }
        if (arr == null || i < 0 || i >= arr.length) {
            return null;
        }
        return arr[i];
    }

    private static Sprite bankIcon() {
        Sprite s = mapfunc(MAPFUNC_BANK);
        return s != null ? s : mapfunc(0);
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

    private static Sprite speechBubble;

    private static Sprite speechBubble() {
        if (speechBubble != null) {
            return speechBubble;
        }
        int w = 16;
        int h = 16;
        SoftwareSprite raw = new SoftwareSprite(w, h);
        int[] p = raw.pixels;
        int outline = 0xFF000000;
        int fill = 0xFFFFFFF0;

        // body 1..14 x 1..10
        for (int y = 1; y <= 10; y++) {
            for (int x = 1; x <= 14; x++) {
                boolean edge = x == 1 || x == 14 || y == 1 || y == 10;
                p[y * w + x] = edge ? outline : fill;
            }
        }
        // round the 4 corners
        p[1 * w + 1] = 0;
        p[1 * w + 14] = 0;
        p[10 * w + 1] = 0;
        p[10 * w + 14] = 0;
        p[1 * w + 2] = outline;
        p[2 * w + 1] = outline;
        p[1 * w + 13] = outline;
        p[2 * w + 14] = outline;
        p[10 * w + 2] = outline;
        p[9 * w + 1] = outline;
        p[10 * w + 13] = outline;
        p[9 * w + 14] = outline;

        // tail
        p[11 * w + 4] = outline;
        p[11 * w + 5] = fill;
        p[11 * w + 6] = outline;
        p[12 * w + 4] = outline;
        p[12 * w + 5] = fill;
        p[13 * w + 4] = outline;

        speechBubble = GlRenderer.enabled ? new GlSprite(raw) : raw;
        return speechBubble;
    }

    private static Sprite magnifier;

    private static Sprite magnifier() {
        if (magnifier != null) {
            return magnifier;
        }
        int w = 16;
        int h = 16;
        SoftwareSprite raw = new SoftwareSprite(w, h);
        int[] p = raw.pixels;
        int ring = 0xFFD8D8D8;
        int glass = 0xFF6EC4FF;
        int handle = 0xFFE8C070;
        int outline = 0xFF000000;
        int cx = 6;
        int cy = 6;
        int r = 5;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int dx = x - cx;
                int dy = y - cy;
                int d2 = dx * dx + dy * dy;
                if (d2 <= (r - 1) * (r - 1)) {
                    p[y * w + x] = glass;
                } else if (d2 <= r * r) {
                    p[y * w + x] = ring;
                }
            }
        }
        // handle, bottom-right
        for (int i = 0; i < 6; i++) {
            int x = 9 + i;
            int y = 9 + i;
            if (x + 1 >= w || y >= h) {
                continue;
            }
            p[y * w + x] = outline;
            p[y * w + x + 1] = handle;
            if (x + 2 < w) {
                p[y * w + x + 2] = outline;
            }
        }

        magnifier = GlRenderer.enabled ? new GlSprite(raw) : raw;
        return magnifier;
    }

    private static Sprite followArrows;

    private static Sprite followArrows() {
        if (followArrows != null) {
            return followArrows;
        }
        int w = 16;
        int h = 16;
        SoftwareSprite raw = new SoftwareSprite(w, h);
        int[] p = raw.pixels;
        int fill = 0xFFFFFF00;
        int outline = 0xFF202020;

        drawLeftChevron(p, w, 1, fill, outline);
        drawLeftChevron(p, w, 8, fill, outline);

        followArrows = GlRenderer.enabled ? new GlSprite(raw) : raw;
        return followArrows;
    }

    private static Sprite tabIcon(rt4.amilious.menutab.MenuTab tab) {
        try {
            rt4.Component c = rt4.InterfaceList.method1418(tab.componentId, -1);
            if (c == null) {
                return null;
            }
            return c.method489(false); // idle tab sprite
        } catch (Exception e) {
            return null;
        }
    }

    private static void drawLeftChevron(int[] p, int w, int ox, int fill, int outline) {
        int[] rows = {
                0b0001000,
                0b0011000,
                0b0111000,
                0b1111100,
                0b0111000,
                0b0011000,
                0b0001000
        };
        int oy = 4;
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < 7; x++) {
                if ((rows[y] & (1 << x)) == 0) {
                    continue;
                }
                p[(oy + y) * w + ox + x] = fill;
            }
        }
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < 7; x++) {
                int px = ox + x;
                int py = oy + y;
                if (p[py * w + px] != fill) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = px + dx;
                        int ny = py + dy;
                        if (nx < 0 || ny < 0 || nx >= w || ny >= 16) {
                            continue;
                        }
                        int i = ny * w + nx;
                        if (p[i] == 0) {
                            p[i] = outline;
                        }
                    }
                }
            }
        }
    }

    private static void drawChevron(int[] p, int w, int ox, int fill, int outline) {
        // 7px tall >> centered vertically
        int[] rows = {
                0b0010000,
                0b0110000,
                0b1110000,
                0b1111000,
                0b1110000,
                0b0110000,
                0b0010000
        };
        int oy = 4;
        for (int y = 0; y < rows.length; y++) {
            int bits = rows[y];
            for (int x = 0; x < 7; x++) {
                if ((bits & (1 << (6 - x))) == 0) {
                    continue;
                }
                int px = ox + x;
                int py = oy + y;
                p[py * w + px] = fill;
            }
        }
        // thin outline on empty neighbors
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < 7; x++) {
                int px = ox + x;
                int py = oy + y;
                if (p[py * w + px] != fill) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = px + dx;
                        int ny = py + dy;
                        if (nx < 0 || ny < 0 || nx >= w || ny >= 16) {
                            continue;
                        }
                        int i = ny * w + nx;
                        if (p[i] == 0) {
                            p[i] = outline;
                        }
                    }
                }
            }
        }
    }

}