package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlStateApi;

/**
 * JOGL implementation of state management operations.
 */
public final class JoglStateApi implements GlStateApi {

	private final GL2 gl;

	public JoglStateApi(GL2 gl) {
		this.gl = gl;
	}

	@Override
	public void glEnable(int cap) {
		gl.glEnable(cap);
	}

	@Override
	public void glDisable(int cap) {
		gl.glDisable(cap);
	}

	@Override
	public void glEnableClientState(int array) {
		gl.glEnableClientState(array);
	}

	@Override
	public void glDisableClientState(int array) {
		gl.glDisableClientState(array);
	}
}
