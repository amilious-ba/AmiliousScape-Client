package rt4.amilious.menutab;

import rt4.client;

public final class MenuTabCycle {

    private MenuTabCycle() { }


    public static MenuTab lastSelected() {
        return MenuTab.current();
    }

    /** Page Down — first tab if none selected, otherwise next */
    public static void next() {
        if (client.gameState != 30) return;
        var tab = MenuTab.current();
        if(tab == null){
            MenuTab.INVENTORY.select();
            return;
        }
        tab = tab.nextEnabled();
        if(tab == null) return;
        tab.select();
    }

    /** Page Up — last tab if none selected, otherwise previous */
    public static void previous() {
        if (client.gameState != 30) return;
        var tab = MenuTab.current();
        if(tab == null){
            MenuTab.INVENTORY.select();
            return;
        }
        tab = tab.prevEnabled();
        if(tab == null) return;
        tab.select();
    }

}