package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlColorApi;

import java.nio.FloatBuffer;

/**
 * JOGL implementation of color, blending, and depth operations.
 */
public final class JoglColorApi implements GlColorApi {

	private final GL2 gl;

	public JoglColorApi(GL2 gl) {
		this.gl = gl;
	}

	// Color

	@Override
	public void glColor3ub(byte red, byte green, byte blue) {
		gl.glColor3ub(red, green, blue);
	}

	@Override
	public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
		gl.glColor4ub(red, green, blue, alpha);
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		gl.glColor4f(red, green, blue, alpha);
	}

	@Override
	public void glColor4fv(float[] v, int offset) {
		gl.glColor4fv(v, offset);
	}

	@Override
	public void glColor4fv(FloatBuffer v) {
		gl.glColor4fv(v);
	}

	// Blending

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		gl.glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glAlphaFunc(int func, float ref) {
		gl.glAlphaFunc(func, ref);
	}

	// Depth & Stencil

	@Override
	public void glDepthFunc(int func) {
		gl.glDepthFunc(func);
	}

	@Override
	public void glDepthMask(boolean flag) {
		gl.glDepthMask(flag);
	}
}
