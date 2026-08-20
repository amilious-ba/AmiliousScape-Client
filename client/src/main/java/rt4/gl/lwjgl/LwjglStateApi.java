package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlStateApi;

/**
 * LWJGL implementation of state management operations.
 * These are simple 1:1 mappings with no buffer conversions needed.
 */
public final class LwjglStateApi implements GlStateApi {

	@Override
	public void glEnable(int cap) {
		GL11.glEnable(cap);
	}

	@Override
	public void glDisable(int cap) {
		GL11.glDisable(cap);
	}

	@Override
	public void glEnableClientState(int array) {
		GL11.glEnableClientState(array);
	}

	@Override
	public void glDisableClientState(int array) {
		GL11.glDisableClientState(array);
	}
}
