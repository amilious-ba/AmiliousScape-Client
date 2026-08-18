package rt4.amilious;

import rt4.ClientProt;
import rt4.JagString;
import rt4.client;

public final class RunToggler {

    public static final int RUN_BUTTON = 49152001;

    private RunToggler() {
    }

    public static void toggle() {
        if (client.gameState != 30) {
            return;
        }
        ClientProt.method4512(JagString.EMPTY, -1, 1, RUN_BUTTON);
    }
}