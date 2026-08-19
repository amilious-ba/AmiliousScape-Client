package rt4.gl;

import rt4.gl.api.*;

/**
 * OpenGL API facade for dual backend support (JOGL / LWJGL).
 *
 * This interface abstracts all OpenGL calls used by the AmiliousScape client,
 * allowing the renderer to work with either JOGL (GL2) or LWJGL (GL11/GL13/etc.)
 * through a unified API.
 *
 * Method signatures mirror OpenGL 1.2+ fixed-function pipeline with ARB extensions.
 * Only methods actually used by this client are included (~85 methods total).
 *
 * Implementation notes:
 * - JOGL: 1:1 delegation to GL2
 * - LWJGL: May require Java array → NIO buffer conversion
 * - No shader/modern GL support (fixed-function only)
 *
 * @see rt4.gl.jogl.JoglGlApi
 * @see rt4.gl.lwjgl.LwjglGlApi
 */
public interface GlApi extends
	GlStateApi,
	GlClearApi,
	GlMatrixApi,
	GlTextureApi,
	GlBufferApi,
	GlDrawApi,
	GlLightingApi,
	GlColorApi,
	GlRenderStateApi,
	GlQueryApi,
	GlParticleApi {
}
