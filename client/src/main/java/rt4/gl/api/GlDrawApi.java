package rt4.gl.api;

import java.nio.Buffer;

/**
 * OpenGL drawing operations including primitives, display lists, and raster operations.
 */
public interface GlDrawApi {

	// Primitives
	void glBegin(int mode);
	void glEnd();
	void glVertex2f(float x, float y);
	void glVertex3f(float x, float y, float z);

	void glDrawElements(int mode, int count, int type, Buffer indices);
	void glDrawElements(int mode, int count, int type, long offset);

	void glDrawPixels(int width, int height, int format, int type, Buffer pixels);

	// Display Lists
	int glGenLists(int range);
	void glNewList(int list, int mode);
	void glEndList();
	void glCallList(int list);
	void glDeleteLists(int list, int range);

	// Raster Operations
	void glRasterPos2i(int x, int y);
	void glPixelZoom(float xfactor, float yfactor);
	void glCopyPixels(int x, int y, int width, int height, int type);
}
