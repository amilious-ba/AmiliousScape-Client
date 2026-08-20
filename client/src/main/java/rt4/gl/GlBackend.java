package rt4.gl;

import java.awt.Canvas;

public interface GlBackend {

    /** Same return codes as GlRenderer.init today: 0 ok, negative = error */
    int init(Canvas canvas, int samples);

    void quit();

    void swapBuffers();

    boolean isEnabled();

    /**
     * Lock the rendering context for the current frame.
     * Must be called before any OpenGL operations.
     * LWJGL: Locks JAWT surface and ensures HDC is valid
     * JOGL: No-op (handles internally)
     * @return true if lock succeeded, false if context is invalid
     */
    boolean lockContext();

    /**
     * Unlock the rendering context after frame is complete.
     * Should be called after swapBuffers.
     * LWJGL: Unlocks JAWT surface
     * JOGL: No-op (handles internally)
     */
    void unlockContext();
}
