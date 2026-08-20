package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlMatrixApi;

import java.nio.FloatBuffer;

/**
 * LWJGL implementation of matrix operations.
 * Uses LwjglBufferHelper for array→buffer conversion.
 */
public final class LwjglMatrixApi implements GlMatrixApi {

	@Override
	public void glMatrixMode(int mode) {
		GL11.glMatrixMode(mode);
	}

	@Override
	public void glLoadIdentity() {
		GL11.glLoadIdentity();
	}

	@Override
	public void glLoadMatrixf(float[] m, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires FloatBuffer
		// Matrix is always 16 floats (4x4)
		GL11.glLoadMatrixf(LwjglBufferHelper.toFloatBuffer(m, offset, 16));
	}

	@Override
	public void glLoadMatrixf(FloatBuffer m) {
		// Already a buffer - just ensure it's ready to read
		GL11.glLoadMatrixf(LwjglBufferHelper.prepareFloatBuffer(m));
	}

	@Override
	public void glPushMatrix() {
		GL11.glPushMatrix();
	}

	@Override
	public void glPopMatrix() {
		GL11.glPopMatrix();
	}

	@Override
	public void glTranslatef(float x, float y, float z) {
		GL11.glTranslatef(x, y, z);
	}

	@Override
	public void glRotatef(float angle, float x, float y, float z) {
		GL11.glRotatef(angle, x, y, z);
	}

	@Override
	public void glScalef(float x, float y, float z) {
		GL11.glScalef(x, y, z);
	}

	@Override
	public void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}
}
