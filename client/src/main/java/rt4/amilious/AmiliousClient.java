package rt4.amilious;

import plugin.PluginRepository;
import rt4.*;
import rt4.amilious.cheats.*;

import java.util.ArrayList;

/**
 * All AmiliousScape client customizations.
 * Upstream hooks should only call AmiliousClient.init() / onCanvas() / onInterfaceButton().
 */
public final class AmiliousClient {

    private static final ArrayList<ICommand> commands = new ArrayList<>();
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
        //initialize components
        DebugConsole.Init();
        DebugConsole.log("AmiliousScape client initialized!");
    }

    public static void AddCommand(ICommand c){
        commands.add(c);
    }

    public static void RemoveCommand(ICommand c){
        commands.remove(c);
    }

    /**
     * @return true if the command was handled and Cheat should stop processing it
     */
    public static boolean processCommands(JagString command) {
        if(command == null) return false;
        String s = command.toString().trim();
        //loop through all cheats
        for (ICommand c : commands) if(c != null && c.compare(s))
            return c.execute(s);
        return false;
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
        ModalTools.update();
        MapController.tickInput();
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