package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlDrawApi;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * LWJGL implementation of drawing operations.
 * Includes primitives, display lists, and raster operations.
 */
public final class LwjglDrawApi implements GlDrawApi {

	// ============================================================
	// Primitives
	// ============================================================

	@Override
	public void glBegin(int mode) {
		GL11.glBegin(mode);
	}

	@Override
	public void glEnd() {
		GL11.glEnd();
	}

	@Override
	public void glVertex2f(float x, float y) {
		GL11.glVertex2f(x, y);
	}

	@Override
	public void glVertex3f(float x, float y, float z) {
		GL11.glVertex3f(x, y, z);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		// LWJGL glDrawElements only accepts ByteBuffer (not typed buffers)
		// The 'type' parameter specifies the data type of the indices
		if (indices instanceof ByteBuffer) {
			GL11.glDrawElements(mode, type, (ByteBuffer) indices);
		} else {
			// Convert to ByteBuffer view for other buffer types
			ByteBuffer byteBuffer;
			if (indices instanceof IntBuffer) {
				IntBuffer intBuf = (IntBuffer) indices;
				byteBuffer = ByteBuffer.allocateDirect(intBuf.remaining() * 4);
				byteBuffer.asIntBuffer().put(intBuf);
				byteBuffer.rewind();
			} else if (indices instanceof ShortBuffer) {
				ShortBuffer shortBuf = (ShortBuffer) indices;
				byteBuffer = ByteBuffer.allocateDirect(shortBuf.remaining() * 2);
				byteBuffer.asShortBuffer().put(shortBuf);
				byteBuffer.rewind();
			} else {
				throw new IllegalArgumentException("Unsupported buffer type: " + indices.getClass());
			}
			GL11.glDrawElements(mode, type, byteBuffer);
		}
	}

	@Override
	public void glDrawElements(int mode, int count, int type, long offset) {
		// VBO offset variant
		GL11.glDrawElements(mode, count, type, offset);
	}

	@Override
	public void glDrawPixels(int width, int height, int format, int type, Buffer pixels) {
		// LWJGL accepts ByteBuffer for pixel data
		if (pixels instanceof ByteBuffer) {
			GL11.glDrawPixels(width, height, format, type, (ByteBuffer) pixels);
		} else {
			// Fallback
			GL11.glDrawPixels(width, height, format, type, (ByteBuffer) pixels);
		}
	}

	// ============================================================
	// Display Lists
	// ============================================================

	@Override
	public int glGenLists(int range) {
		return GL11.glGenLists(range);
	}

	@Override
	public void glNewList(int list, int mode) {
		GL11.glNewList(list, mode);
	}

	@Override
	public void glEndList() {
		GL11.glEndList();
	}

	@Override
	public void glCallList(int list) {
		GL11.glCallList(list);
	}

	@Override
	public void glDeleteLists(int list, int range) {
		GL11.glDeleteLists(list, range);
	}

	// ============================================================
	// Raster Operations
	// ============================================================

	@Override
	public void glRasterPos2i(int x, int y) {
		GL11.glRasterPos2i(x, y);
	}

	@Override
	public void glPixelZoom(float xfactor, float yfactor) {
		GL11.glPixelZoom(xfactor, yfactor);
	}

	@Override
	public void glCopyPixels(int x, int y, int width, int height, int type) {
		GL11.glCopyPixels(x, y, width, height, type);
	}
}
