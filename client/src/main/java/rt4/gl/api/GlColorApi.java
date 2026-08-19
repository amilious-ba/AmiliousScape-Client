package rt4.gl.api;

import java.nio.FloatBuffer;

/**
 * OpenGL color, blending, and depth operations.
 */
public interface GlColorApi {

	// Color
	void glColor3ub(byte red, byte green, byte blue);
	void glColor4ub(byte red, byte green, byte blue, byte alpha);
	void glColor4f(float red, float green, float blue, float alpha);
	void glColor4fv(float[] v, int offset);
	void glColor4fv(FloatBuffer v);

	// Blending
	void glBlendFunc(int sfactor, int dfactor);
	void glAlphaFunc(int func, float ref);

	// Depth & Stencil
	void glDepthFunc(int func);
	void glDepthMask(boolean flag);
}
