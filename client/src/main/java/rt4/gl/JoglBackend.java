package rt4.gl;

import com.jogamp.nativewindow.awt.AWTGraphicsConfiguration;
import com.jogamp.nativewindow.awt.JAWTWindow;
import com.jogamp.opengl.*;
import jogamp.newt.awt.NewtFactoryAWT;
import rt4.core.GameShell;
import rt4.render.GlCleaner;
import rt4.render.GlRenderer;
import rt4.render.LightingManager;
import rt4.render.MaterialManager;
import rt4.core.ThreadUtils;
import rt4.gl.jogl.JoglGlApi;

import java.awt.Canvas;

/**
 * Current production GL path (JOGL).
 * Owns context/drawable/window; still fills GlRenderer.gl / enabled / sizes
 * so the rest of the client is unchanged.
 */
public final class JoglBackend implements GlBackend {

    private GLContext context;
    private GLDrawable drawable;
    private JAWTWindow window;
    private boolean enabled;

    @Override
    public int init(Canvas canvas, int samples) {
        try {
            if (!canvas.isDisplayable()) {
                return -1;
            }

            GLProfile profile = GLProfile.get(GLProfile.GL3bc);
            GLCapabilities capabilities = new GLCapabilities(profile);
            if (samples > 0) {
                capabilities.setSampleBuffers(true);
                capabilities.setNumSamples(samples * 4);
            }

            GLDrawableFactory factory = GLDrawableFactory.getFactory(profile);
            AWTGraphicsConfiguration config = AWTGraphicsConfiguration.create(
                    canvas.getGraphicsConfiguration(), capabilities, capabilities);
            window = NewtFactoryAWT.getNativeWindow(canvas, config);

            if (!window.getLock().isLocked()) {
                window.lockSurface();
            }
            try {
                drawable = factory.createGLDrawable(window);
                drawable.setRealized(true);
            } finally {
                window.unlockSurface();
            }

            int attempts = 0;
            int result;
            while (true) {
                context = drawable.createContext(null);
                try {
                    result = context.makeCurrent();
                    if (result != 0) {
                        break;
                    }
                } catch (Exception ignored) {
                }
                if (attempts++ > 5) {
                    return -2;
                }
                ThreadUtils.sleep(1000L);
            }

            if (window.getLock().isLocked()) {
                window.unlockSurface();
            }

            GlRenderer.gl = GLContext.getCurrentGL().getGL2();
            GlRenderer.api = new JoglGlApi(GlRenderer.gl);
            GlRenderer.gl.glLineWidth((float) GameShell.canvasScale);
            enabled = true;
            GlRenderer.enabled = true;
            GlRenderer.canvasWidth = canvas.getSize().width;
            GlRenderer.canvasHeight = canvas.getSize().height;

            result = GlRenderer.checkContext();
            if (result != 0) {
                quit();
                return result;
            }

            GlRenderer.afterContextCreated(); // see step 3 below
            GlRenderer.gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

            attempts = 0;
            while (true) {
                try {
                    drawable.swapBuffers();
                    break;
                } catch (Exception ex) {
                    if (attempts++ > 5) {
                        quit();
                        return -3;
                    }
                    ThreadUtils.sleep(100L);
                }
            }

            GlRenderer.gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
            return 0;
        } catch (Throwable ex) {
            quit();
            return -5;
        }
    }

    @Override
    public void quit() {
        if (GlRenderer.gl != null) {
            try {
                MaterialManager.quit();
            } catch (Throwable ignored) {
            }
        }

        if (window != null) {
            if (!window.getLock().isLocked()) {
                window.lockSurface();
            }
            if (context != null) {
                GlCleaner.clear();
                try {
                    if (GLContext.getCurrent() == context) {
                        context.release();
                    }
                } catch (Throwable ignored) {
                }
                try {
                    context.destroy();
                } catch (Throwable ignored) {
                }
            }
        }

        if (drawable != null) {
            try {
                drawable.setRealized(false);
            } catch (Throwable ignored) {
            }
        }

        window = null;
        context = null;
        drawable = null;
        enabled = false;

        GlRenderer.gl = null;
        GlRenderer.api = null;
        GlRenderer.enabled = false;
        LightingManager.method2398();
    }

    @Override
    public void swapBuffers() {
        try {
            if (drawable != null) {
                drawable.swapBuffers();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean lockContext() {
        // No-op: JOGL handles context locking internally
        return true;
    }

    @Override
    public void unlockContext() {
        // No-op: JOGL handles context unlocking internally
    }
}