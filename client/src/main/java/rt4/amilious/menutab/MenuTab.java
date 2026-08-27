package rt4.amilious.menutab;

import rt4.*;
import rt4.amilious.debug.DebugConsole;

public enum MenuTab {

    COMBAT(48889897),
    STATS(48889898),
    QUESTS(48889899),
    INVENTORY(48889900),
    EQUIPMENT(48889901),
    PRAYER(48889902),
    MAGIC(48889903),
    FRIENDS(48889905),
    IGNORE(48889906),
    CLAN(48889907),
    OPTIONS(48889908),
    EMOTES(48889909),
    MUSIC(48889910);


    public final int componentId;

    /** Last known selected sidebar tab */
    private static MenuTab current = null;

    public static void onLogin(){
        int mode = DisplayMode.getWindowMode();
        setCurrent(mode==0?MenuTab.INVENTORY:null);
    }
    private static void setCurrent(MenuTab current){
        if(MenuTab.current == current) return;
        if(DebugConsole.showInteractions)
            DebugConsole.log("MenuTab.setCurrent: " + (current==null?"none":current.name()));
        MenuTab.current = current;
    }

    MenuTab(int componentId) {
        this.componentId = componentId;
    }

    public int index() {return ordinal();}

    public static final MenuTab[] ORDER = values();

    public static MenuTab current() {return current;}

    public boolean isEnabled(){
        Component c = InterfaceList.method1418(this.componentId, -1);
        if (c == null || c.hidden) {
            return false;
        }
        return InterfaceList.getServerActiveProperties(c).isButtonEnabled(0);
    }

    public boolean isDisabled(){return !isEnabled();}

    public boolean isVisible(){
        Component c = InterfaceList.method1418(this.componentId, -1);
        return c != null && !c.hidden;
    }

    public static void onComponent(int componentId) {
        if (client.gameState != 30) return;
        MenuTab tab = fromComponentId(componentId);
        if(tab == null) return; //not a tab click
        int mode = DisplayMode.getWindowMode();
        if(mode!=0&&tab== current) setCurrent(null);
        else setCurrent(tab);
    }

    public static MenuTab fromIndex(int index) {
        return ORDER[Math.floorMod(index, ORDER.length)];
    }

    public static MenuTab fromComponentId(int componentId) {
        for (MenuTab tab : ORDER) {
            if (tab.componentId == componentId) {
                return tab;
            }
        }
        return null;
    }

    public MenuTab next() {
        return fromIndex(ordinal() + 1);
    }

    public MenuTab nextEnabled() {
        MenuTab t = this;
        for (int i = 0; i < ORDER.length; i++) {
            t = t.next();
            if (t.isEnabled()) return t;
        }
        return this;
    }

    public MenuTab prevEnabled() {
        MenuTab t = this;
        for (int i = 0; i < ORDER.length; i++) {
            t = t.prev();
            if (t.isEnabled()) return t;
        }
        return this;
    }

    public MenuTab prev() {
        return fromIndex(ordinal() - 1);
    }

    public void select() {
        if (client.gameState != 30) return;
        ClientProt.method4512(JagString.EMPTY, -1, 1, this.componentId);
        // current is updated via onComponent when method4512 runs
    }

}