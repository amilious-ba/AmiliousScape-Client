package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlMatrixApi;

import java.nio.FloatBuffer;

/**
 * JOGL implementation of matrix operations.
 */
public final class JoglMatrixApi implements GlMatrixApi {

	private final GL2 gl;

	public JoglMatrixApi(GL2 gl) {
		this.gl = gl;
	}

	@Override
	public void glMatrixMode(int mode) {
		gl.glMatrixMode(mode);
	}

	@Override
	public void glLoadIdentity() {
		gl.glLoadIdentity();
	}

	@Override
	public void glLoadMatrixf(float[] m, int offset) {
		gl.glLoadMatrixf(m, offset);
	}

	@Override
	public void glLoadMatrixf(FloatBuffer m) {
		gl.glLoadMatrixf(m);
	}

	@Override
	public void glPushMatrix() {
		gl.glPushMatrix();
	}

	@Override
	public void glPopMatrix() {
		gl.glPopMatrix();
	}

	@Override
	public void glTranslatef(float x, float y, float z) {
		gl.glTranslatef(x, y, z);
	}

	@Override
	public void glRotatef(float angle, float x, float y, float z) {
		gl.glRotatef(angle, x, y, z);
	}

	@Override
	public void glScalef(float x, float y, float z) {
		gl.glScalef(x, y, z);
	}

	@Override
	public void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
		gl.glOrtho(left, right, bottom, top, zNear, zFar);
	}
}
