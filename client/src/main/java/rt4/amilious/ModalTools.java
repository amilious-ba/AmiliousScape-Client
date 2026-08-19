package rt4.amilious;

import rt4.network.ClientProt;
import rt4.ui.ComponentPointer;
import rt4.ui.InterfaceList;

public final class ModalTools {

    private static volatile boolean pendingCloseInterface = false;

    private ModalTools(){}

    public static void update(){
        if(!pendingCloseInterface) return;
        pendingCloseInterface = false;
        if(hasModalOpen()) ClientProt.closeWidget();
    }

    public static boolean hasModalOpen(){
        for (ComponentPointer p = (ComponentPointer) InterfaceList.openInterfaces.head();
             p != null;
             p = (ComponentPointer) InterfaceList.openInterfaces.next()) {
            if (p.anInt5879 == 0) return true;
        }
        return false;
    }

    public static void closeOpenModalNextUpdate(){
        pendingCloseInterface = true;
    }

}
