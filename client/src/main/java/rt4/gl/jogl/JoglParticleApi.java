package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlParticleApi;

import java.nio.FloatBuffer;

/**
 * JOGL implementation of particle/point sprite operations.
 */
public final class JoglParticleApi implements GlParticleApi {

	private final GL2 gl;

	public JoglParticleApi(GL2 gl) {
		this.gl = gl;
	}

	@Override
	public void glPointParameterfv(int pname, float[] params, int offset) {
		gl.glPointParameterfv(pname, params, offset);
	}

	@Override
	public void glPointParameterfv(int pname, FloatBuffer params) {
		gl.glPointParameterfv(pname, params);
	}

	@Override
	public void glPointParameterf(int pname, float param) {
		gl.glPointParameterf(pname, param);
	}
}
