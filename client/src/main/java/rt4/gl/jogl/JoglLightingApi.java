package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlLightingApi;

import java.nio.FloatBuffer;

/**
 * JOGL implementation of lighting, material, and fog operations.
 */
public final class JoglLightingApi implements GlLightingApi {

	private final GL2 gl;

	public JoglLightingApi(GL2 gl) {
		this.gl = gl;
	}

	// Lighting

	@Override
	public void glLightfv(int light, int pname, float[] params, int offset) {
		gl.glLightfv(light, pname, params, offset);
	}

	@Override
	public void glLightfv(int light, int pname, FloatBuffer params) {
		gl.glLightfv(light, pname, params);
	}

	@Override
	public void glLightf(int light, int pname, float param) {
		gl.glLightf(light, pname, param);
	}

	@Override
	public void glLightModelfv(int pname, float[] params, int offset) {
		gl.glLightModelfv(pname, params, offset);
	}

	@Override
	public void glLightModelfv(int pname, FloatBuffer params) {
		gl.glLightModelfv(pname, params);
	}

	// Material

	@Override
	public void glColorMaterial(int face, int mode) {
		gl.glColorMaterial(face, mode);
	}

	// Fog

	@Override
	public void glFogi(int pname, int param) {
		gl.glFogi(pname, param);
	}

	@Override
	public void glFogf(int pname, float param) {
		gl.glFogf(pname, param);
	}

	@Override
	public void glFogfv(int pname, float[] params, int offset) {
		gl.glFogfv(pname, params, offset);
	}

	@Override
	public void glFogfv(int pname, FloatBuffer params) {
		gl.glFogfv(pname, params);
	}

	@Override
	public void glHint(int target, int mode) {
		gl.glHint(target, mode);
	}
}
