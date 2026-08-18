package rt4.amilious;

import rt4.DisplayMode;
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
        initialized = true;
        InputController.register(); // canvas may already exist
    }

    /** Call at end of GameShell.addCanvas() — survives canvas replace. */
    public static void onCanvas() {
        InputController.register();
    }

    /** Call at start of ClientProt.method4512. */
    public static void onInterfaceButton(JagString arg0, int arg1, int arg2, int componentId) {
        MenuTab.onComponent(componentId);
        MapController.onComponent(componentId);
    }

    /** call after PluginRepository.Update(); in client.mainLoop */
    public static void update() {
        MapController.tickInput();
    }

    /**
     * @return true if the command was handled and Cheat should stop processing it
     */
    public static boolean handleCheat(JagString command) {
        // parse, handle ::borderless, etc.
        // return true when consumed
        // return false to let normal Cheat logic run
        return false;
    }

    public static void onDraw() {
    }

    public static void GameStateChange(int previous, int gameState) {
        if(previous !=30 && gameState ==30) onLogin();
        if(previous ==30 && gameState !=30) onLogout();
    }

    private static void onLogout() {
    }

    private static void onLogin() {
        MenuTab.onLogin(); //let the menu tab know we just logged in
    }


}