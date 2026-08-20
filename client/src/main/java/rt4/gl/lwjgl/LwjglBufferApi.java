package rt4.gl.lwjgl;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlBufferApi;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL implementation of buffer operations (VBOs and client arrays).
 */
public final class LwjglBufferApi implements GlBufferApi {

	// ============================================================
	// VBO Operations
	// ============================================================

	@Override
	public void glGenBuffers(int n, int[] buffers, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires IntBuffer
		IntBuffer buffer = LwjglBufferHelper.toIntBuffer(new int[n]);
		ARBVertexBufferObject.glGenBuffersARB(buffer);
		// Extract generated IDs back to array
		LwjglBufferHelper.fromIntBuffer(buffer, buffers, offset, n);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		// Already a buffer - ensure it's ready to write
		buffers.clear();
		ARBVertexBufferObject.glGenBuffersARB(buffers);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		ARBVertexBufferObject.glBindBufferARB(target, buffer);
	}

	@Override
	public void glBufferData(int target, long size, Buffer data, int usage) {
		// LWJGL accepts ByteBuffer for VBO data
		if (data == null) {
			ARBVertexBufferObject.glBufferDataARB(target, size, usage);
		} else if (data instanceof ByteBuffer) {
			ARBVertexBufferObject.glBufferDataARB(target, (ByteBuffer) data, usage);
		} else if (data instanceof FloatBuffer) {
			ARBVertexBufferObject.glBufferDataARB(target, (FloatBuffer) data, usage);
		} else if (data instanceof IntBuffer) {
			ARBVertexBufferObject.glBufferDataARB(target, (IntBuffer) data, usage);
		} else {
			// Fallback - try ByteBuffer cast
			ARBVertexBufferObject.glBufferDataARB(target, (ByteBuffer) data, usage);
		}
	}

	@Override
	public void glBufferSubData(int target, long offset, long size, Buffer data) {
		// LWJGL accepts ByteBuffer for VBO data
		if (data instanceof ByteBuffer) {
			ARBVertexBufferObject.glBufferSubDataARB(target, offset, (ByteBuffer) data);
		} else if (data instanceof FloatBuffer) {
			ARBVertexBufferObject.glBufferSubDataARB(target, offset, (FloatBuffer) data);
		} else if (data instanceof IntBuffer) {
			ARBVertexBufferObject.glBufferSubDataARB(target, offset, (IntBuffer) data);
		} else {
			// Fallback
			ARBVertexBufferObject.glBufferSubDataARB(target, offset, (ByteBuffer) data);
		}
	}

	@Override
	public void glDeleteBuffers(int n, int[] buffers, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires IntBuffer
		ARBVertexBufferObject.glDeleteBuffersARB(LwjglBufferHelper.toIntBuffer(buffers, offset, n));
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		// Already a buffer - prepare for reading
		ARBVertexBufferObject.glDeleteBuffersARB(LwjglBufferHelper.prepareIntBuffer(buffers));
	}

	// ============================================================
	// Client Arrays
	// ============================================================

	@Override
	public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
		// LWJGL requires type parameter for all buffer types
		if (pointer instanceof FloatBuffer) {
			GL11.glVertexPointer(size, type, stride, (FloatBuffer) pointer);
		} else if (pointer instanceof ByteBuffer) {
			GL11.glVertexPointer(size, type, stride, (ByteBuffer) pointer);
		} else {
			// Fallback
			GL11.glVertexPointer(size, type, stride, (ByteBuffer) pointer);
		}
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, long offset) {
		// VBO offset variant
		GL11.glVertexPointer(size, type, stride, offset);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, Buffer pointer) {
		// LWJGL requires type parameter for all buffer types
		if (pointer instanceof FloatBuffer) {
			GL11.glColorPointer(size, type, stride, (FloatBuffer) pointer);
		} else if (pointer instanceof ByteBuffer) {
			GL11.glColorPointer(size, type, stride, (ByteBuffer) pointer);
		} else {
			// Fallback
			GL11.glColorPointer(size, type, stride, (ByteBuffer) pointer);
		}
	}

	@Override
	public void glColorPointer(int size, int type, int stride, long offset) {
		// VBO offset variant
		GL11.glColorPointer(size, type, stride, offset);
	}

	@Override
	public void glNormalPointer(int type, int stride, Buffer pointer) {
		// LWJGL requires type parameter for all buffer types
		if (pointer instanceof FloatBuffer) {
			GL11.glNormalPointer(type, stride, (FloatBuffer) pointer);
		} else if (pointer instanceof ByteBuffer) {
			GL11.glNormalPointer(type, stride, (ByteBuffer) pointer);
		} else {
			// Fallback
			GL11.glNormalPointer(type, stride, (ByteBuffer) pointer);
		}
	}

	@Override
	public void glNormalPointer(int type, int stride, long offset) {
		// VBO offset variant
		GL11.glNormalPointer(type, stride, offset);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
		// LWJGL requires type parameter for all buffer types
		if (pointer instanceof FloatBuffer) {
			GL11.glTexCoordPointer(size, type, stride, (FloatBuffer) pointer);
		} else if (pointer instanceof ByteBuffer) {
			GL11.glTexCoordPointer(size, type, stride, (ByteBuffer) pointer);
		} else {
			// Fallback
			GL11.glTexCoordPointer(size, type, stride, (ByteBuffer) pointer);
		}
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, long offset) {
		// VBO offset variant
		GL11.glTexCoordPointer(size, type, stride, offset);
	}

	@Override
	public void glInterleavedArrays(int format, int stride, Buffer pointer) {
		// LWJGL accepts ByteBuffer for interleaved arrays
		if (pointer instanceof java.nio.ByteBuffer) {
			GL11.glInterleavedArrays(format, stride, (java.nio.ByteBuffer) pointer);
		} else {
			throw new IllegalArgumentException("glInterleavedArrays requires ByteBuffer");
		}
	}

	@Override
	public void glInterleavedArrays(int format, int stride, long offset) {
		// VBO offset variant
		GL11.glInterleavedArrays(format, stride, offset);
	}
}
