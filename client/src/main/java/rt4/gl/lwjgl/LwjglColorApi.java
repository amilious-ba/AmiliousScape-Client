package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlColorApi;

import java.nio.FloatBuffer;

/**
 * LWJGL implementation of color, blending, and depth operations.
 */
public final class LwjglColorApi implements GlColorApi {

	@Override
	public void glColor3ub(byte red, byte green, byte blue) {
		GL11.glColor3ub(red, green, blue);
	}

	@Override
	public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
		GL11.glColor4ub(red, green, blue, alpha);
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}

	@Override
	public void glColor4fv(float[] v, int offset) {
		// LWJGL requires FloatBuffer, JOGL accepts array + offset
		GL11.glColor4fv(LwjglBufferHelper.toFloatBuffer(v, offset, 4));
	}

	@Override
	public void glColor4fv(FloatBuffer v) {
		// Already a buffer - just ensure it's ready to read
		GL11.glColor4fv(LwjglBufferHelper.prepareFloatBuffer(v));
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glAlphaFunc(int func, float ref) {
		GL11.glAlphaFunc(func, ref);
	}

	@Override
	public void glDepthFunc(int func) {
		GL11.glDepthFunc(func);
	}

	@Override
	public void glDepthMask(boolean flag) {
		GL11.glDepthMask(flag);
	}
}
