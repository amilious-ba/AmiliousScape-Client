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
import rt4.gl.lwjgl.LwjglGlApi;
import rt4.render.GlRenderer;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Canvas;

import static org.lwjgl.system.jawt.JAWTFunctions.*;

/**
 * LWJGL OpenGL backend using JAWT for AWT Canvas integration.
 * Requires per-frame locking via lockContext()/unlockContext().
 */
public final class LwjglBackend implements GlBackend {

    private JAWT jawt;
    private JAWTDrawingSurface surface;
    private JAWTDrawingSurfaceInfo surfaceInfo;
    private long hwnd;
    private long hdc;
    private long hglrc;
    private boolean enabled;
    private boolean locked;

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

                    // Initialize LWJGL GlApi implementation
                    GlRenderer.api = new LwjglGlApi();

                    enabled = true;
                    GlRenderer.enabled = true;
                    GlRenderer.canvasWidth = canvas.getSize().width;
                    GlRenderer.canvasHeight = canvas.getSize().height;

                    // Initialize materials, lighting, etc (same as JoglBackend)
                    GlRenderer.afterContextCreated();

                    // Debug: verify alpha test and blend are enabled
                    System.out.println("[LwjglBackend] GL_ALPHA_TEST enabled: " + GL11.glIsEnabled(GL11.GL_ALPHA_TEST));
                    System.out.println("[LwjglBackend] GL_BLEND enabled: " + GL11.glIsEnabled(GL11.GL_BLEND));

                    // Stub only: clear + swap (still under lock)
                    GL11.glViewport(0, 0, GlRenderer.canvasWidth, GlRenderer.canvasHeight);
                    GL11.glClearColor(0.1f, 0.1f, 0.15f, 1f);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    GDI32.SwapBuffers(hdc);

                    System.out.println("[LwjglBackend] init OK with LwjglGlApi - ready for per-frame rendering");

                    // Keep surface info for per-frame locking
                    this.surfaceInfo = dsi;
                    this.locked = true;

                    return 0;
                } catch (Exception e) {
                    // If we fail after getting surface info, clean it up
                    if (surfaceInfo != null) {
                        JAWT_DrawingSurface_FreeDrawingSurfaceInfo(surfaceInfo, surface.FreeDrawingSurfaceInfo());
                        surfaceInfo = null;
                    }
                    throw e;
                }
            } catch (Exception e) {
                // If we fail after locking, unlock
                JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
                throw e;
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

        // Clean up JAWT surface info if still held
        if (surfaceInfo != null && surface != null) {
            try {
                JAWT_DrawingSurface_FreeDrawingSurfaceInfo(surfaceInfo, surface.FreeDrawingSurfaceInfo());
            } catch (Throwable ignored) {
            }
            surfaceInfo = null;
        }

        // Unlock surface if locked
        if (locked && surface != null) {
            try {
                JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
            } catch (Throwable ignored) {
            }
            locked = false;
        }

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
        GlRenderer.gl = null; // LWJGL does not use JOGL GL2
        GlRenderer.api = null;
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

    @Override
    public boolean lockContext() {
        if (!enabled || surface == null) {
            return false;
        }

        // If already locked from init, just update HDC
        if (locked && surfaceInfo != null) {
            JAWTWin32DrawingSurfaceInfo dsiWin = JAWTWin32DrawingSurfaceInfo.create(surfaceInfo.platformInfo());
            hdc = dsiWin.hdc();
            if (hdc == NULL) {
                System.err.println("[LwjglBackend] lockContext: HDC became null");
                return false;
            }
            // Make context current with fresh HDC
            if (!WGL.wglMakeCurrent(hdc, hglrc)) {
                System.err.println("[LwjglBackend] lockContext: wglMakeCurrent failed");
                return false;
            }
            return true;
        }

        // Lock surface for this frame
        int lock = JAWT_DrawingSurface_Lock(surface, surface.Lock());
        if ((lock & JAWT_LOCK_ERROR) != 0) {
            System.err.println("[LwjglBackend] lockContext: surface lock failed");
            return false;
        }

        // Get surface info
        surfaceInfo = JAWT_DrawingSurface_GetDrawingSurfaceInfo(surface, surface.GetDrawingSurfaceInfo());
        if (surfaceInfo == null) {
            JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
            System.err.println("[LwjglBackend] lockContext: GetDrawingSurfaceInfo failed");
            return false;
        }

        // Get HDC
        JAWTWin32DrawingSurfaceInfo dsiWin = JAWTWin32DrawingSurfaceInfo.create(surfaceInfo.platformInfo());
        hdc = dsiWin.hdc();
        if (hdc == NULL) {
            JAWT_DrawingSurface_FreeDrawingSurfaceInfo(surfaceInfo, surface.FreeDrawingSurfaceInfo());
            JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
            surfaceInfo = null;
            System.err.println("[LwjglBackend] lockContext: HDC is null");
            return false;
        }

        // Make context current
        if (!WGL.wglMakeCurrent(hdc, hglrc)) {
            JAWT_DrawingSurface_FreeDrawingSurfaceInfo(surfaceInfo, surface.FreeDrawingSurfaceInfo());
            JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
            surfaceInfo = null;
            System.err.println("[LwjglBackend] lockContext: wglMakeCurrent failed");
            return false;
        }

        locked = true;
        return true;
    }

    @Override
    public void unlockContext() {
        if (!locked || surface == null) {
            return;
        }

        // Note: We keep surface locked across frames to avoid constant lock/unlock overhead
        // The HDC is refreshed in lockContext() each frame
        // Uncomment below to unlock per-frame (may reduce performance):
        /*
        if (surfaceInfo != null) {
            JAWT_DrawingSurface_FreeDrawingSurfaceInfo(surfaceInfo, surface.FreeDrawingSurfaceInfo());
            surfaceInfo = null;
        }
        JAWT_DrawingSurface_Unlock(surface, surface.Unlock());
        locked = false;
        */
    }
}