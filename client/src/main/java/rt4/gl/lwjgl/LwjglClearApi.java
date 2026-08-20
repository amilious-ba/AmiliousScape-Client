package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlClearApi;

/**
 * LWJGL implementation of clear, viewport, and scissor operations.
 * Simple 1:1 mappings to LWJGL GL11.
 */
public final class LwjglClearApi implements GlClearApi {

	@Override
	public void glClear(int mask) {
		GL11.glClear(mask);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepth(double depth) {
		GL11.glClearDepth(depth);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		GL11.glScissor(x, y, width, height);
	}
}
