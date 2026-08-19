package rt4.gl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.WGL;                          // ← was system.windows.WGL
import org.lwjgl.system.jawt.JAWT;
import org.lwjgl.system.jawt.JAWTDrawingSurface;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.jawt.JAWTWin32DrawingSurfaceInfo;
import org.lwjgl.system.windows.GDI32;                 // pixel format + swap
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR;
import rt4.render.GlRenderer;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Canvas;

import static org.lwjgl.system.jawt.JAWTFunctions.*;

/**
 * Experimental LWJGL path. Stub: context + clear + swap only.
 * Not feature-parity with JOGL yet.
 */
public final class LwjglBackend implements GlBackend {

    private JAWT jawt;
    private JAWTDrawingSurface surface;
    private long hwnd;
    private long hdc;
    private long hglrc;
    private boolean enabled;

    @Override
    public int init(Canvas canvas, int samples) {
        try {
            if (!canvas.isDisplayable()) {
                return -1;
            }

            // JAWT: get HWND/HDC for the AWT Canvas
            jawt = JAWT.calloc();
            jawt.version(JAWT_VERSION_1_4);
            if (!JAWT_GetAWT(jawt)) {
                System.err.println("[LwjglBackend] JAWT_GetAWT failed");
                return -5;
            }

            surface = JAWT_GetDrawingSurface(canvas, jawt.GetDrawingSurface());
            if (surface == null) {
                System.err.println("[LwjglBackend] GetDrawingSurface failed");
                return -5;
            }

            int lock = JAWT_DrawingSurface_Lock(surface, surface.Lock());
            if ((lock & JAWT_LOCK_ERROR) != 0) {
                System.err.println("[LwjglBackend] DS lock failed");
                return -5;
            }

            try {
                JAWTDrawingSurfaceInfo dsi = JAWT_DrawingSurface_GetDrawingSurfaceInfo(
                        surface, surface.GetDrawingSurfaceInfo());
                if (dsi == null) {
                    System.err.println("[LwjglBackend] GetDrawingSurfaceInfo failed");
                    return -5;
                }

                try {
                    JAWTWin32DrawingSurfaceInfo dsiWin =
                            JAWTWin32DrawingSurfaceInfo.create(dsi.platformInfo());
                    hwnd = dsiWin.hwnd();
                    hdc = dsiWin.hdc();

                    if (hwnd == NULL || hdc == NULL) {
                        System.err.println("[LwjglBackend] hwnd/hdc null");
                        return -5;
                    }

                    // Pixel format + GL context WHILE SURFACE IS LOCKED
                    PIXELFORMATDESCRIPTOR pfd = PIXELFORMATDESCRIPTOR.calloc();
                    try {
                        pfd.nSize((short) PIXELFORMATDESCRIPTOR.SIZEOF);
                        pfd.nVersion((short) 1);
                        pfd.dwFlags(GDI32.PFD_DRAW_TO_WINDOW | GDI32.PFD_SUPPORT_OPENGL | GDI32.PFD_DOUBLEBUFFER);
                        pfd.iPixelType((byte) GDI32.PFD_TYPE_RGBA);
                        pfd.cColorBits((byte) 32);
                        pfd.cDepthBits((byte) 24);
                        pfd.cStencilBits((byte) 8);
                        pfd.iLayerType((byte) GDI32.PFD_MAIN_PLANE);

                        int pixelFormat = GDI32.ChoosePixelFormat(hdc, pfd);
                        if (pixelFormat == 0) {
                            System.err.println("[LwjglBackend] ChoosePixelFormat failed, hdc="
                                    + Long.toHexString(hdc));
                            return -5;
                        }
                        if (!GDI32.SetPixelFormat(hdc, pixelFormat, pfd)) {
                            System.err.println("[LwjglBackend] SetPixelFormat failed");
                            return -5;
                        }
                    } finally {
                        pfd.free();
                    }

                    hglrc = WGL.wglCreateContext(hdc);
                    if (hglrc == NULL) {
                        System.err.println("[LwjglBackend] wglCreateContext failed");
                        return -2;
                    }
                    if (!WGL.wglMakeCurrent(hdc, hglrc)) {
                        System.err.println("[LwjglBackend] wglMakeCurrent failed");
                        return -2;
                    }

                    GL.createCapabilities();

                    enabled = true;
                    GlRenderer.enabled = true;
                    GlRenderer.canvasWidth = canvas.getSize().width;
                    GlRenderer.canvasHeight = canvas.getSize().height;

                    // Stub only: clear + swap (still under lock)
                    GL11.glViewport(0, 0, GlRenderer.canvasWidth, GlRenderer.canvasHeight);
                    GL11.glClearColor(0.1f, 0.1f, 0.15f, 1f);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    GDI32.SwapBuffers(hdc);

                    System.out.println("[LwjglBackend] init OK (stub — dark clear only, no full HD yet)");
                    return 0;
                } finally {
                    JAWT_DrawingSurface_FreeDrawingSurfaceInfo(dsi, surface.FreeDrawingSurfaceInfo());
                }
            } finally {
                JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
            }
        } catch (Throwable t) {
            t.printStackTrace();
            quit();
            return -5;
        }
    }

    @Override
    public void quit() {
        try {
            if (hglrc != NULL) {
                WGL.wglMakeCurrent(NULL, NULL);
                WGL.wglDeleteContext(hglrc);
                hglrc = NULL;
            }
        } catch (Throwable ignored) {
        }
        hdc = NULL;
        hwnd = NULL;

        if (surface != null && jawt != null) {
            try {
                JAWT_FreeDrawingSurface(surface, jawt.FreeDrawingSurface());
            } catch (Throwable ignored) {
            }
            surface = null;
        }
        if (jawt != null) {
            jawt.free();
            jawt = null;
        }

        enabled = false;
        GlRenderer.enabled = false;
        GlRenderer.gl = null; // LWJGL stub does not set JOGL GL2
    }

    @Override
    public void swapBuffers() {
        if (hdc != NULL) {
            GDI32.SwapBuffers(hdc);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}