package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlBufferApi;

import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * JOGL implementation of buffer operations (VBOs and client arrays).
 */
public final class JoglBufferApi implements GlBufferApi {

	private final GL2 gl;

	public JoglBufferApi(GL2 gl) {
		this.gl = gl;
	}

	// VBO Operations

	@Override
	public void glGenBuffers(int n, int[] buffers, int offset) {
		gl.glGenBuffers(n, buffers, offset);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		gl.glGenBuffers(n, buffers);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		gl.glBindBuffer(target, buffer);
	}

	@Override
	public void glBufferData(int target, long size, Buffer data, int usage) {
		gl.glBufferData(target, size, data, usage);
	}

	@Override
	public void glBufferSubData(int target, long offset, long size, Buffer data) {
		gl.glBufferSubData(target, offset, size, data);
	}

	@Override
	public void glDeleteBuffers(int n, int[] buffers, int offset) {
		gl.glDeleteBuffers(n, buffers, offset);
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		gl.glDeleteBuffers(n, buffers);
	}

	// Client Arrays

	@Override
	public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
		gl.glVertexPointer(size, type, stride, pointer);
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, long offset) {
		gl.glVertexPointer(size, type, stride, offset);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, Buffer pointer) {
		gl.glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, long offset) {
		gl.glColorPointer(size, type, stride, offset);
	}

	@Override
	public void glNormalPointer(int type, int stride, Buffer pointer) {
		gl.glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glNormalPointer(int type, int stride, long offset) {
		gl.glNormalPointer(type, stride, offset);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
		gl.glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, long offset) {
		gl.glTexCoordPointer(size, type, stride, offset);
	}
}
