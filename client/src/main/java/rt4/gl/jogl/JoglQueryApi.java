package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlQueryApi;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * JOGL implementation of query operations.
 */
public final class JoglQueryApi implements GlQueryApi {

	private final GL2 gl;

	public JoglQueryApi(GL2 gl) {
		this.gl = gl;
	}

	@Override
	public void glGetIntegerv(int pname, int[] params, int offset) {
		gl.glGetIntegerv(pname, params, offset);
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		gl.glGetIntegerv(pname, params);
	}

	@Override
	public void glGetFloatv(int pname, float[] params, int offset) {
		gl.glGetFloatv(pname, params, offset);
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		gl.glGetFloatv(pname, params);
	}

	@Override
	public String glGetString(int name) {
		return gl.glGetString(name);
	}

	@Override
	public boolean isExtensionAvailable(String extension) {
		return gl.isExtensionAvailable(extension);
	}

	@Override
	public void setSwapInterval(int interval) {
		gl.setSwapInterval(interval);
	}
}
