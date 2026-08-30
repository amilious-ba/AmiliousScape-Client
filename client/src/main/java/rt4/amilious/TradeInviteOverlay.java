package rt4.amilious;

import rt4.Fonts;
import rt4.GameShell;
import rt4.GlRaster;
import rt4.GlRenderer;
import rt4.JagString;
import rt4.Mouse;
import rt4.PathFinder;
import rt4.Player;
import rt4.PlayerList;
import rt4.Protocol;
import rt4.SoftwareRaster;
import rt4.amilious.input.InputManager;
import rt4.client;

public final class TradeInviteOverlay {

    private static String pendingName;

    private static int boxX, boxY, boxW, boxH;
    private static int acceptX, acceptY, rejectX, rejectY, btnW, btnH;

    private TradeInviteOverlay() {
    }

    public static void onChat(int type, JagString name, JagString message) {
        if (name == null || message == null) {
            return;
        }
        String msg = message.toString().toLowerCase();
        if (!msg.contains("wishes to trade") && !msg.contains("wish to trade")) {
            return;
        }
        System.out.println("[trade] type=" + type + " name=" + name + " msg=" + message);
        show(strip(name.toString()));
    }

    public static void show(String name) {
        if (name == null || name.length() == 0) {
            return;
        }
        pendingName = name;
        System.out.println("[trade] invite from " + pendingName);
    }

    public static void clear() {
        pendingName = null;
    }

    public static boolean isVisible() {
        return pendingName != null && client.gameState == 30;
    }

    public static void tick() {
        if (!isVisible()) {
            return;
        }
        layout();
        if (Mouse.clickButton == 0) {
            return;
        }
        int mx = Mouse.clickX;
        int my = Mouse.clickY;
        if (hit(mx, my, acceptX, acceptY, btnW, btnH)) {
            InputManager.consumeMouseClick();
            accept();
        } else if (hit(mx, my, rejectX, rejectY, btnW, btnH)) {
            InputManager.consumeMouseClick();
            System.out.println("[trade] rejected " + pendingName);
            clear();
        }
    }

    public static void draw() {
        if (!isVisible() || Fonts.b12Full == null) {
            return;
        }
        layout();

        fillRect(boxX, boxY, boxW, boxH, 0x2A2218);
        fillRect(boxX, boxY, boxW, 1, 0xC8B070);
        fillRect(boxX, boxY + boxH - 1, boxW, 1, 0xC8B070);
        fillRect(boxX, boxY, 1, boxH, 0xC8B070);
        fillRect(boxX + boxW - 1, boxY, 1, boxH, 0xC8B070);

        Fonts.b12Full.renderCenter(
                JagString.of(pendingName + " would like to trade"),
                boxX + boxW / 2,
                boxY + 22,
                0xFFE6B0,
                0);

        fillRect(acceptX, acceptY, btnW, btnH, 0x2E6B2E);
        fillRect(rejectX, rejectY, btnW, btnH, 0x6B2E2E);

        Fonts.b12Full.renderCenter(JagString.of("Accept"), acceptX + btnW / 2, acceptY + 24, 0xFFFFFF, 0);
        Fonts.b12Full.renderCenter(JagString.of("Reject"), rejectX + btnW / 2, rejectY + 24, 0xFFFFFF, 0);
    }

    private static void accept() {
        int index = findPlayerIndex(pendingName);
        if (index < 0) {
            System.out.println("[trade] player not nearby: " + pendingName);
            return;
        }
        Player p = PlayerList.players[index];
        if (p != null && PlayerList.self != null) {
            PathFinder.findPath(
                    PlayerList.self.movementQueueZ[0], 0, 1, false, 0,
                    p.movementQueueX[0], 1, 0, 2, p.movementQueueZ[0],
                    PlayerList.self.movementQueueX[0]);
        }
        Protocol.outboundBuffer.p1isaac(180);
        Protocol.outboundBuffer.ip2add(index);
        System.out.println("[trade] accepted " + pendingName + " idx=" + index);
        clear();
    }

    private static int findPlayerIndex(String raw) {
        String want = strip(raw).toLowerCase();
        if (PlayerList.players == null) {
            return -1;
        }
        for (int i = 0; i < PlayerList.players.length; i++) {
            Player p = PlayerList.players[i];
            if (p == null || p.username == null) {
                continue;
            }
            if (strip(p.username.toString()).toLowerCase().equals(want)) {
                return i;
            }
        }
        return -1;
    }

    private static void layout() {
        boxW = 420;
        boxH = 90;
        boxX = (GameShell.canvasWidth - boxW) / 2;
        if (boxX < 8) {
            boxX = 8;
        }
        boxY = 24;
        btnW = 120;
        btnH = 36;
        acceptX = boxX + 60;
        rejectX = boxX + boxW - 60 - btnW;
        acceptY = rejectY = boxY + 44;
        OverlayClickBlocker.add(boxX, boxY, boxW, boxH);
    }

    private static boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private static void fillRect(int x, int y, int w, int h, int rgb) {
        if (GlRenderer.enabled) {
            GlRaster.fillRect(x, y, w, h, rgb);
        } else {
            SoftwareRaster.fillRect(x, y, w, h, rgb & 0xFFFFFF);
        }
    }

    private static String strip(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("<[^>]+>", "").trim();
    }
}