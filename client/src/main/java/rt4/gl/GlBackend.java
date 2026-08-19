package rt4.gl;

import java.awt.Canvas;

public interface GlBackend {

    /** Same return codes as GlRenderer.init today: 0 ok, negative = error */
    int init(Canvas canvas, int samples);

    void quit();

    void swapBuffers();

    boolean isEnabled();
}
