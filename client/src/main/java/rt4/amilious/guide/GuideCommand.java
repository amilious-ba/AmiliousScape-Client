package rt4.amilious.guide;

import rt4.amilious.Commands.ICommand;
import rt4.amilious.debug.DebugConsole;

/**
 * ::guide
 * ::guide next
 * ::guide list
 * ::guide done Cook's Assistant
 * ::guide hud | ::guide on | ::guide off
 * ::guide reset
 */
public final class GuideCommand implements ICommand {

    @Override
    public boolean compare(String s) {
        if (s == null) {
            return false;
        }
        String lower = s.toLowerCase().trim();
        return lower.equals("::guide") || lower.startsWith("::guide ");
    }

    @Override
    public boolean execute(String s) {
        String rest = s.trim();
        if (rest.toLowerCase().startsWith("::guide")) {
            rest = rest.substring("::guide".length()).trim();
        }

        String[] args;
        if (rest.isEmpty()) {
            args = new String[0];
        } else {
            args = rest.split("\\s+");
        }

        run(args);
        return true;
    }

    private static void run(String[] args) {
        if (args.length == 0 || "next".equalsIgnoreCase(args[0])) {
            ProgressGuide.tick();
            String[] lines = ProgressGuide.overlayLines();
            for (int i = 0; i < lines.length; i++) {
                log(lines[i]);
            }
            return;
        }
        if ("list".equalsIgnoreCase(args[0])) {
            log(ProgressGuide.printFull());
            return;
        }
        if ("hud".equalsIgnoreCase(args[0])
                || "toggle".equalsIgnoreCase(args[0])) {
            GuideHud.toggle();
            log("guide hud=" + GuideHud.isVisible());
            return;
        }
        if ("on".equalsIgnoreCase(args[0]) || "show".equalsIgnoreCase(args[0])) {
            GuideHud.setVisible(true);
            log("guide hud=true");
            return;
        }
        if ("off".equalsIgnoreCase(args[0]) || "hide".equalsIgnoreCase(args[0])) {
            GuideHud.setVisible(false);
            log("guide hud=false");
            return;
        }
        if ("reset".equalsIgnoreCase(args[0])) {
            QuestProgress.resetManual();
            ProgressGuide.tick();
            log("manual quest marks cleared");
            return;
        }
        if ("done".equalsIgnoreCase(args[0]) && args.length > 1) {
            StringBuilder n = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) {
                    n.append(' ');
                }
                n.append(args[i]);
            }
            QuestProgress.markDone(n.toString());
            ProgressGuide.tick();
            log("marked done: " + n);
            return;
        }
        log("usage: ::guide | ::guide hud | ::guide on | ::guide off | ::guide list | ::guide done <quest> | ::guide reset");
    }

    private static void log(String s) {
        try {
            DebugConsole.log("[guide] " + s);
        } catch (Throwable t) {
            System.out.println("[guide] " + s);
        }
    }
}