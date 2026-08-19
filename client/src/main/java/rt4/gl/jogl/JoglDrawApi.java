package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlDrawApi;

import java.nio.Buffer;

/**
 * JOGL implementation of drawing operations.
 */
public final class JoglDrawApi implements GlDrawApi {

	private final GL2 gl;

	public JoglDrawApi(GL2 gl) {
		this.gl = gl;
	}

	// Primitives

	@Override
	public void glBegin(int mode) {
		gl.glBegin(mode);
	}

	@Override
	public void glEnd() {
		gl.glEnd();
	}

	@Override
	public void glVertex2f(float x, float y) {
		gl.glVertex2f(x, y);
	}

	@Override
	public void glVertex3f(float x, float y, float z) {
		gl.glVertex3f(x, y, z);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		gl.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, long offset) {
		gl.glDrawElements(mode, count, type, offset);
	}

	@Override
	public void glDrawPixels(int width, int height, int format, int type, Buffer pixels) {
		gl.glDrawPixels(width, height, format, type, pixels);
	}

	// Display Lists

	@Override
	public int glGenLists(int range) {
		return gl.glGenLists(range);
	}

	@Override
	public void glNewList(int list, int mode) {
		gl.glNewList(list, mode);
	}

	@Override
	public void glEndList() {
		gl.glEndList();
	}

	@Override
	public void glCallList(int list) {
		gl.glCallList(list);
	}

	@Override
	public void glDeleteLists(int list, int range) {
		gl.glDeleteLists(list, range);
	}

	// Raster Operations

	@Override
	public void glRasterPos2i(int x, int y) {
		gl.glRasterPos2i(x, y);
	}

	@Override
	public void glPixelZoom(float xfactor, float yfactor) {
		gl.glPixelZoom(xfactor, yfactor);
	}

	@Override
	public void glCopyPixels(int x, int y, int width, int height, int type) {
		gl.glCopyPixels(x, y, width, height, type);
	}
}
