package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlClearApi;

/**
 * JOGL implementation of clear, viewport, and scissor operations.
 */
public final class JoglClearApi implements GlClearApi {

	private final GL2 gl;

	public JoglClearApi(GL2 gl) {
		this.gl = gl;
	}

	@Override
	public void glClear(int mask) {
		gl.glClear(mask);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		gl.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepth(double depth) {
		gl.glClearDepth(depth);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		gl.glViewport(x, y, width, height);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		gl.glScissor(x, y, width, height);
	}
}
