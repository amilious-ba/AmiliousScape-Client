package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlRenderStateApi;

/**
 * JOGL implementation of rendering state operations.
 */
public final class JoglRenderStateApi implements GlRenderStateApi {

	private final GL2 gl;

	public JoglRenderStateApi(GL2 gl) {
		this.gl = gl;
	}

	@Override
	public void glShadeModel(int mode) {
		gl.glShadeModel(mode);
	}

	@Override
	public void glCullFace(int mode) {
		gl.glCullFace(mode);
	}

	@Override
	public void glPolygonMode(int face, int mode) {
		gl.glPolygonMode(face, mode);
	}

	@Override
	public void glLineWidth(float width) {
		gl.glLineWidth(width);
	}

	@Override
	public void glPushAttrib(int mask) {
		gl.glPushAttrib(mask);
	}

	@Override
	public void glPopAttrib() {
		gl.glPopAttrib();
	}

	@Override
	public void glDrawBuffer(int mode) {
		gl.glDrawBuffer(mode);
	}

	@Override
	public void glReadBuffer(int mode) {
		gl.glReadBuffer(mode);
	}
}
