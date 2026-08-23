package rt4.amilious;

import rt4.*;
import java.util.ArrayList;
import plugin.PluginRepository;
import rt4.amilious.Commands.DumpNames_Command;
import rt4.amilious.voice.ChatHeadReader;
import rt4.amilious.input.InputManager;
import rt4.amilious.Commands.Bind_Command;
import rt4.amilious.Commands.ICommand;
import rt4.amilious.voice.NarratorReader;
import rt4.amilious.voice.TutorialGuideReader;
import rt4.amilious.voice.Voiceover;

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

        CommandBinds.load();
        AmiliousClient.AddCommand(new Bind_Command());
        AmiliousClient.AddCommand(new DumpNames_Command());

        // Input system
        InputManager.init();
        InputManager.setPollDevices(true);
        InputManager.setProcessModeKeys(true); // Enter/Esc arm chat → CHAT mode

        // Special text modals (amount dialog iface 752, etc.)
        ModalController.init();

        DebugConsole.Init();
        Voiceover.init();
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
        ModalController.onComponent(componentId);
        MapController.onComponent(componentId);
        rt4.amilious.input.ChatboxState.onChatTabClicked(componentId);
        if(DebugConsole.showInteractions)DebugConsole.log(option.toString() + " " + child + " " + button + " " + componentId);
        ChatHeadReader.onInterfaceButton(option, child, button, componentId);
    }

    /** call after PluginRepository.Update(); in client.mainLoop */
    public static void update() {

        if (rt4.client.gameState == 30) {
            ModalController.tick();
        }
        InputManager.tick();
        ModalTools.update();
        MapController.tickInput();
        Voiceover.tick();
        TutorialPatch.tick();
    }



    public static void onDraw() {
        DebugConsole.draw();
    }

    public static void GameStateChange(int previous, int gameState) {
        if(previous !=30 && gameState ==30) onLogin();
        if(previous ==30 && gameState !=30) onLogout();
    }

    private static void onLogout() {
        Voiceover.onLogout();
    }

    private static void onLogin() {
        MenuTab.onLogin(); //let the menu tab know we just logged in
        PluginRepository.OnLogin();
        InputManager.onLogin();
        Voiceover.onLogin();
    }

    public static void onInterfaceOpen(int interfaceId) {
        ModalController.onInterfaceOpen(interfaceId);
        Voiceover.onInterfaceOpen(interfaceId);
    }

    public static void onIntegerInputSubmitted() {
        ModalController.onIntegerInputSubmitted();
    }

    public static void onNameInputSubmitted() {
        ModalController.onNameInputSubmitted();
    }

    public static void onStringInputSubmitted() {
        ModalController.onStringInputSubmitted();
    }

    public static void onWidgetClosed() {
        ModalController.onWidgetClosed();

    }

    public static void onMiniMenuAction(int index, int actionCode, JagString op, JagString opBase,
                                        int arg1, int arg2) {
    }

    public static void onComponentHiddenChanged(int componentId, boolean hidden) {
        ModalController.onComponentHiddenChanged(componentId, hidden);
    }

    public static void onQuickChatShown() {
        ModalController.onQuickChatShown();
    }

    public static void onQuickChatHidden() {
        ModalController.onQuickChatHidden();
    }

    public static void onRun_CS2(int scriptId, JagString argTypes, Object[] scriptArgs) {
        ModalController.onRun_CS2(scriptId, argTypes, scriptArgs);
    }

    /**
     * Client-side interface option CS2 (ClientProt.method4512 / onOptionClick).
     * scriptId = arguments[0] when present; otherwise -1.
     */
    public static void onClientOptionScript(int scriptId, int componentId, JagString opBase, int op,Object[] scriptArgs) {
        ModalController.onClientOptionScript(scriptId, componentId, opBase, op, scriptArgs);
    }

    public static void onTutorialGuideText(int packetId, int iFaceId, int childId, String text) {
        Voiceover.onTutorialGuideText(packetId, iFaceId, childId, text);
    }

    public static void onInterfaceClose(int interfaceId) {
        Voiceover.onInterfaceClose(interfaceId);
    }
}