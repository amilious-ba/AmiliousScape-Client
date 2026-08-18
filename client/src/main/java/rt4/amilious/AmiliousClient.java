package rt4.amilious;

import plugin.PluginRepository;
import rt4.Chat;
import rt4.DisplayMode;
import rt4.GlobalJsonConfig;
import rt4.JagString;

/**
 * All AmiliousScape client customizations.
 * Upstream hooks should only call AmiliousClient.init() / onCanvas() / onInterfaceButton().
 */
public final class AmiliousClient {

    private static boolean initialized = false;

    /** Call once after main client init (after PluginRepository.Init()). */
    public static void Init() {
        if (initialized) return;
        DebugConsole.log("AmiliousScape client initializing...");
        initialized = true;
        InputController.register(); // canvas may already exist
        if (GlobalJsonConfig.instance != null && GlobalJsonConfig.instance.enableAmiliousDebugAtStart) {
            DebugConsole.enabled = true;
            DebugConsole.log("debug enabled at start");
        }
        DebugConsole.log("AmiliousScape client initialized!");
    }

    /** Call at end of GameShell.addCanvas() — survives canvas replace. */
    public static void onCanvas() {
        InputController.register();
    }

    /** Call at start of ClientProt.method4512. */
    public static void onInterfaceButton(JagString option, int child, int button, int componentId) {
        MenuTab.onComponent(componentId);
        MapController.onComponent(componentId);
        if(DebugConsole.showInteractions)DebugConsole.log(option.toString() + " " + child + " " + button + " " + componentId);
    }

    /** call after PluginRepository.Update(); in client.mainLoop */
    public static void update() {
        MapController.tickInput();
    }

    /**
     * @return true if the command was handled and Cheat should stop processing it
     */
    public static boolean handleCheat(JagString command) {
        if(command == null) return false;
        String s = command.toString().trim();

        if (s.equalsIgnoreCase("::debug") || s.equalsIgnoreCase("::debugconsole")) {
            DebugConsole.toggle();
            Chat.add(null, 0, JagString.parse("Debug console: " + (DebugConsole.enabled ? "on" : "off")));
            return true;
        }
        if (s.equalsIgnoreCase("::debugclear")) {
            DebugConsole.clear();
            Chat.add(null, 0, JagString.parse("Debug console cleared"));
            return true;
        }
        if (s.toLowerCase().startsWith("::debuglog ")) {
            DebugConsole.log(s.substring("::debuglog ".length()));
            return true;
        }
        if (s.equalsIgnoreCase("::debuglog")) {
            DebugConsole.log("(no message)");
            return true;
        }
        if (s.equalsIgnoreCase("::debugshowinteractions")) {
            var show = DebugConsole.showInteractions = !DebugConsole.showInteractions;
            Chat.add(null, 0, JagString.parse("Debug console: " + (show ? "showing interactions" : "not showing interactions")));
            return true;
        }

        return false;
    }

    public static void onDraw() {
        DebugConsole.draw();
    }

    public static void GameStateChange(int previous, int gameState) {
        if(previous !=30 && gameState ==30) onLogin();
        if(previous ==30 && gameState !=30) onLogout();
    }

    private static void onLogout() {
    }

    private static void onLogin() {
        MenuTab.onLogin(); //let the menu tab know we just logged in
        PluginRepository.OnLogin();
    }


}