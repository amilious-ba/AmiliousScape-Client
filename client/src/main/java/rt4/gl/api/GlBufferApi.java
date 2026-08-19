package rt4.gl.api;

import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * OpenGL buffer operations including VBOs and client state arrays.
 */
public interface GlBufferApi {

	// VBO Operations
	void glGenBuffers(int n, int[] buffers, int offset);
	void glGenBuffers(int n, IntBuffer buffers);

	void glBindBuffer(int target, int buffer);

	void glBufferData(int target, long size, Buffer data, int usage);
	void glBufferSubData(int target, long offset, long size, Buffer data);

	void glDeleteBuffers(int n, int[] buffers, int offset);
	void glDeleteBuffers(int n, IntBuffer buffers);

	// Client Arrays
	void glVertexPointer(int size, int type, int stride, Buffer pointer);
	void glVertexPointer(int size, int type, int stride, long offset);

	void glColorPointer(int size, int type, int stride, Buffer pointer);
	void glColorPointer(int size, int type, int stride, long offset);

	void glNormalPointer(int type, int stride, Buffer pointer);
	void glNormalPointer(int type, int stride, long offset);

	void glTexCoordPointer(int size, int type, int stride, Buffer pointer);
	void glTexCoordPointer(int size, int type, int stride, long offset);
}
